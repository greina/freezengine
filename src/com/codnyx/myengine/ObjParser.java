package com.codnyx.myengine;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;

/**
 * Parses Wavefront OBJ files to create {@link Mesh} objects.
 * This parser handles vertex positions ('v'), vertex normals ('vn'), and face definitions ('f').
 * It supports different face formats:
 * <ul>
 *   <li>f v1 v2 v3 ... (vertex indices only)</li>
 *   <li>f v1//vn1 v2//vn2 v3//vn3 ... (vertex and normal indices)</li>
 *   <li>f v1/vt1/vn1 v2/vt2/vn2 v3/vt3/vn3 ... (vertex, texture, and normal indices - texture coordinate currently ignored)</li>
 * </ul>
 * The parser can optionally center the mesh around the origin (balanceVertices) and
 * compute face normals if they are not provided (computeNormals).
 */
public class ObjParser 
{
	/** 
	 * If true, the parsed mesh vertices will be translated so their average position is at the origin. 
	 * Defaults to true.
	 */
	private boolean balanceVertices = true;
	/** 
	 * If true, and if faces are defined without normals (e.g., "f v1 v2 v3"), 
	 * face normals will be computed. Defaults to true.
	 */
	private boolean computeNormals = true;

	/**
	 * Parses an OBJ file from the given filename.
	 * 
	 * @param filename The path to the OBJ file.
	 * @return A {@link Mesh} object representing the parsed OBJ file.
	 * @throws IOException If an I/O error occurs reading the file.
	 */
	public Mesh parseFile(String filename) throws IOException
	{
		BufferedReader stream = new BufferedReader(new FileReader(filename));
		return parseStream(stream);
		
	}

	/**
	 * Parses an OBJ file from a {@link BufferedReader}.
	 * This method performs two passes over the stream:
	 * 1. Reads all vertex ('v') and vertex normal ('vn') data.
	 * 2. Resets the stream and reads face ('f') data to construct the mesh.
	 * 
	 * @param stream The BufferedReader providing the OBJ data. The stream must support mark/reset.
	 * @return A {@link Mesh} object.
	 * @throws IOException If an I/O error occurs reading the stream.
	 */
	public Mesh parseStream(BufferedReader stream) throws IOException
	{
		String line = null;
		ScanData sd = new ScanData(); // Stores raw vertex and normal data from the file
		MeshData md = new MeshData(); // Stores processed, unique vertices and indices for the mesh
		Mesh currentMesh = new Mesh(); // The mesh to be populated
		int lineindex = 0;
		
		// Mark the beginning of the stream to be able to reset for the second pass (faces)
		// A large readAheadLimit is used, assuming OBJ files are not excessively large.
		// Consider alternative strategies for very large files if memory becomes an issue.
		stream.mark(10000000); 
		boolean markedAfterVertexScan = false; // To ensure mark is set after initial v/vn scan
		
		// First pass: Read all vertex data (v, vn)
		while((line = stream.readLine()) != null)
		{
			lineindex++;
			String[] tokens = line.trim().split("\\s+");
			if (tokens.length == 0 || tokens[0].startsWith("#")) { // Skip empty lines and comments
				continue;
			}
			
			if(tokens[0].equalsIgnoreCase("v"))
				sd.vertexList.add(parseVector(tokens));
			else if(tokens[0].equalsIgnoreCase("vn"))
				sd.normalList.add(parseVector(tokens));
			else if (tokens[0].equalsIgnoreCase("f")) {
				// If we encounter a face, and haven't marked for reset yet (after v/vn scan),
				// this is the point to mark. This handles files where 'f' might appear before all 'v'/'vn'.
				if (!markedAfterVertexScan) {
					stream.reset(); // Go back to the global mark
					stream.mark(10000000); // Set a new mark here, this is where face parsing will actually start
					markedAfterVertexScan = true; // Only mark once
					// Re-read the current line as it's the first face line now
					tokens = stream.readLine().trim().split("\\s+"); 
					// Fall through to face parsing logic below (which is in the second pass, but this handles the edge case)
				}
				// In the first pass, we primarily care about v and vn. Faces are handled in the second pass.
				// However, the original code had a complex marking logic. Simplifying it:
				// The global mark at the beginning is sufficient.
			}
			else
			{
				// If we haven't set the specific mark for face processing yet, do it.
				// This logic seems intended to mark the stream just before the first face definition,
				// after all vertex/normal data has been read.
				if(!markedAfterVertexScan && !tokens[0].equalsIgnoreCase("v") && !tokens[0].equalsIgnoreCase("vn"))
				{
					// This means we've passed all v and vn lines.
					// We need to reset to the global mark, then mark again at *this* position
					// to correctly process faces later.
					// However, the original marking logic was a bit convoluted.
					// A simpler approach is one mark at the start, read v/vn, then reset and read faces.
					// The provided code structure already does this.
				}
				if(!tokens[0].equals("f") && !tokens[0].equalsIgnoreCase("v") && !tokens[0].equalsIgnoreCase("vn") && !tokens[0].equalsIgnoreCase("vt") && !tokens[0].startsWith("#") && !tokens[0].equalsIgnoreCase("o") && !tokens[0].equalsIgnoreCase("g") && !tokens[0].equalsIgnoreCase("s") && !tokens[0].equalsIgnoreCase("mtllib") && !tokens[0].equalsIgnoreCase("usemtl")) {
					System.out.println("ObjParser: Unknown or unhandled OBJ tag '" + tokens[0] + "' at line " + lineindex + ". Skipping.");
				}
			}
		}
		
		// Center the vertices if requested
		if(isBalanceVertices() && !sd.vertexList.isEmpty()) {
			balanceVertices(sd.vertexList);
		}
		
		// Second pass: Read the faces
		stream.reset(); // Reset to the mark set at the beginning of the stream
		lineindex = 0;
		while((line = stream.readLine()) != null)
		{
			lineindex++;
			String[] tokens = line.trim().split("\\s+");
			if (tokens.length == 0 || tokens[0].startsWith("#")) { // Skip empty lines and comments
				continue;
			}

			if(tokens[0].equalsIgnoreCase("f")) {
				parseFace(tokens, md, sd);				
			}
		}
				
		md.populateMesh(currentMesh); // Convert intermediate MeshData to the final Mesh object
		return currentMesh;
	}

	/**
	 * Translates the given list of vertices such that their geometric center is at the origin (0,0,0).
	 * Modifies the vertex coordinates in the input list directly.
	 * @param vertexList The list of vertices (float[3] arrays) to balance.
	 */
	private void balanceVertices(ArrayList<float[]> vertexList) {
		if (vertexList.isEmpty()) return;
		
		float[] center = {0,0,0};
		for(float[] v: vertexList)
		{
			center[0] += v[0];
			center[1] += v[1];
			center[2] += v[2];
		}
		center[0] /= vertexList.size();
		center[1] /= vertexList.size();
		center[2] /= vertexList.size();
		
		for(float[] v: vertexList)
		{
			v[0] -= center[0];
			v[1] -= center[1];
			v[2] -= center[2];
		}
	}

	/**
	 * Parses a face definition line from an OBJ file.
	 * A face line starts with 'f' followed by vertex references.
	 * This method detects the format of vertex references (e.g., v, v//vn, v/vt/vn)
	 * and calls the appropriate specialized parsing method.
	 * Faces are triangulated if they have more than 3 vertices.
	 * 
	 * @param tokens The tokens from the face line (e.g., ["f", "v1//vn1", "v2//vn2", "v3//vn3"]).
	 * @param md The {@link MeshData} object to populate with vertex and index data.
	 * @param sd The {@link ScanData} object containing raw vertex/normal lists.
	 */
	private void parseFace(String[] tokens, MeshData md, ScanData sd) 
	{
		if (tokens.length < 4) { // A face must have at least 3 vertices
			System.err.println("ObjParser: Face definition has too few vertices: " + String.join(" ", tokens) + ". Skipping.");
			return;
		}
		
		String sample = tokens[1]; // Use the first vertex reference to determine format	
		if(Pattern.matches("\\d+", sample)) // Matches "v"
			parseSimpleFace(tokens, md,sd);
		else if(Pattern.matches("\\d+//\\d+", sample)) // Matches "v//vn"
			parseVNFace(tokens, md, sd);
		else if(Pattern.matches("\\d+/\\d+/\\d+", sample)) // Matches "v/vt/vn"
			parseFullFace(tokens,md, sd);
		else if(Pattern.matches("\\d+/\\d+", sample)) { // Matches "v/vt" (texture coordinate, currently ignored for normals)
			System.out.println("ObjParser: Face format v/vt found. Texture coordinates are read but normals will be computed if not available via v/vt/vn or v//vn.");
			parseVNTFace_TextureOnly(tokens, md, sd); // Similar to simple face, but acknowledges texture coord
		} else {
			System.err.println("ObjParser: Unknown or unsupported face format for token '" + sample + "' in line: " + String.join(" ", tokens) + ". Skipping face.");
		}
	}
	
	/**
	 * Parses a face defined with vertex, texture coordinate, and normal indices (e.g., "f v1/vt1/vn1 v2/vt2/vn2 ...").
	 * Texture coordinates (vt) are parsed but currently not used in the final Mesh construction by MeshData beyond VertexData keying.
	 * Triangulates the face if it has more than 3 vertices.
	 * 
	 * @param tokens The tokens from the face line.
	 * @param md The MeshData to populate.
	 * @param sd The ScanData containing raw vertex/normal lists.
	 */
	private void parseFullFace(String[] tokens, MeshData md, ScanData sd)  
	{
		int numVertices = tokens.length - 1;
		short[] vIndices = new short[numVertices];
		short[] tIndices = new short[numVertices]; // Texture indices, parsed but may not be used by MeshData if normals are primary
		short[] nIndices = new short[numVertices];

		for(int i = 0; i < numVertices; i++)
		{
			String[] parts = tokens[i+1].split("/");
			if (parts.length != 3) {
				System.err.println("ObjParser: Malformed v/vt/vn face component: " + tokens[i+1] + ". Skipping face.");
				return;
			}
			vIndices[i] = (short) (Short.parseShort(parts[0])-1); // OBJ indices are 1-based
			if (!parts[1].isEmpty()) { // Texture index can be empty
				tIndices[i] = (short) (Short.parseShort(parts[1])-1);
			} else {
				tIndices[i] = -1; // Indicate no texture index
			}
			nIndices[i] = (short) (Short.parseShort(parts[2])-1);
		}
		
		// Triangulate the face (e.g., a quad f v1 v2 v3 v4 becomes two triangles: f v1 v2 v3 and f v1 v3 v4)
		for(int i = 1; i < numVertices - 1; i++) // Create (numVertices - 2) triangles
		{
			// Args for addIndex: vIndex, nIndex, tIndex (if used by VertexData), sd
			md.addIndex(vIndices[0], nIndices[0], tIndices[0], sd);
			md.addIndex(vIndices[i], nIndices[i], tIndices[i], sd);
			md.addIndex(vIndices[i+1], nIndices[i+1], tIndices[i+1], sd);
		}
	}

	/**
	 * Parses a face defined with vertex and normal indices (e.g., "f v1//vn1 v2//vn2 ...").
	 * Triangulates the face if it has more than 3 vertices.
	 * 
	 * @param tokens The tokens from the face line.
	 * @param md The MeshData to populate.
	 * @param sd The ScanData containing raw vertex/normal lists.
	 */
	private void parseVNFace(String[] tokens, MeshData md, ScanData sd) 
	{
		int numVertices = tokens.length - 1;
		short[] vIndices = new short[numVertices];
		short[] nIndices = new short[numVertices];

		for(int i = 0; i < numVertices; i++)
		{
			String[] parts = tokens[i+1].split("//");
			if (parts.length != 2) {
				System.err.println("ObjParser: Malformed v//vn face component: " + tokens[i+1] + ". Skipping face.");
				return;
			}
			vIndices[i] = (short) (Short.parseShort(parts[0])-1);
			nIndices[i] = (short) (Short.parseShort(parts[1])-1);	
		}
		
		for(int i = 1; i < numVertices - 1; i++)
		{
			md.addIndex(vIndices[0], nIndices[0], (short)-1, sd); // No texture index
			md.addIndex(vIndices[i], nIndices[i], (short)-1, sd);
			md.addIndex(vIndices[i+1], nIndices[i+1], (short)-1, sd);
		}
	}
	
	/**
	 * Parses a face defined with only vertex indices (e.g., "f v1 v2 v3 ...").
	 * If {@link #computeNormals} is true, a face normal is computed using the first three vertices.
	 * This normal is then used for all vertices of the generated triangles.
	 * Triangulates the face if it has more than 3 vertices.
	 * 
	 * @param tokens The tokens from the face line.
	 * @param md The MeshData to populate.
	 * @param sd The ScanData containing raw vertex/normal lists.
	 */
	private void parseSimpleFace(String[] tokens, MeshData md, ScanData sd) 
	{
		int numVertices = tokens.length - 1;
		short[] vertexIndices = new short[numVertices];
		for(int i = 0; i < numVertices; i++) {
			vertexIndices[i] = (short) (Short.parseShort(tokens[i+1])-1); // OBJ indices are 1-based
		}
		
		short normalIndexToUse = -1;

		if(computeNormals)
		{
			if (numVertices < 3) {
				System.err.println("ObjParser: Cannot compute normal for a face with less than 3 vertices. Skipping face: " + String.join(" ", tokens));
				return;
			}
			// Compute the face normal using the first three vertices
			float[] normal = {0,0,0};
			float[] edge1 = {0,0,0};
			float[] edge2 = {0,0,0};
			
			float[] v0 = sd.vertexList.get(vertexIndices[0]);
			float[] v1 = sd.vertexList.get(vertexIndices[1]);
			float[] v2 = sd.vertexList.get(vertexIndices[2]);

			MyMath.subtract(v1, v0, edge1);
			MyMath.subtract(v2, v0, edge2); // Corrected: use v2 - v0 for the second edge from the same origin
			
			MyMath.crossProduct(edge1, edge2, normal);
			MyMath.normalize(normal); // Ensure it's a unit normal

			// Check if an identical normal already exists in sd.normalList to reuse it
			boolean found = false;
			for(int i=0; i < sd.normalList.size(); i++) {
				if(MyMath.areEqual(sd.normalList.get(i), normal)) {
					normalIndexToUse = (short)i;
					found = true;
					break;
				}
			}
			if(!found) {
				normalIndexToUse = (short) sd.normalList.size();
				sd.normalList.add(normal);
			}
		}
		
		// Triangulate the face
		for(int i = 1; i < numVertices - 1; i++)
		{
			if(computeNormals) {
				md.addIndex(vertexIndices[0], normalIndexToUse, (short)-1, sd);
				md.addIndex(vertexIndices[i], normalIndexToUse, (short)-1, sd);
				md.addIndex(vertexIndices[i+1], normalIndexToUse, (short)-1, sd);
			} else {
				md.addIndex(vertexIndices[0], (short)-1, (short)-1, sd); // No normal, no texture
				md.addIndex(vertexIndices[i], (short)-1, (short)-1, sd);
				md.addIndex(vertexIndices[i+1], (short)-1, (short)-1, sd);
			}
		}
	}

	/**
	 * Parses a face defined with vertex and texture coordinate indices (e.g., "f v1/vt1 v2/vt2 ...").
	 * Normals are computed if {@link #computeNormals} is true.
	 * This is similar to {@link #parseSimpleFace} but acknowledges texture coordinates are present (though not fully utilized in normal computation here).
	 * 
	 * @param tokens The tokens from the face line.
	 * @param md The MeshData to populate.
	 * @param sd The ScanData containing raw vertex/normal lists.
	 */
	private void parseVNTFace_TextureOnly(String[] tokens, MeshData md, ScanData sd) {
		int numVertices = tokens.length - 1;
		short[] vIndices = new short[numVertices];
		short[] tIndices = new short[numVertices]; // Texture indices

		for (int i = 0; i < numVertices; i++) {
			String[] parts = tokens[i + 1].split("/");
			if (parts.length < 2) { // Must have v/t
				System.err.println("ObjParser: Malformed v/vt face component: " + tokens[i + 1] + ". Skipping face.");
				return;
			}
			vIndices[i] = (short) (Short.parseShort(parts[0]) - 1);
			tIndices[i] = (short) (Short.parseShort(parts[1]) - 1);
		}

		short normalIndexToUse = -1;
		if (computeNormals) {
			if (numVertices < 3) {
				System.err.println("ObjParser: Cannot compute normal for a face with less than 3 vertices (v/vt format). Skipping face: " + String.join(" ", tokens));
				return;
			}
			float[] normal = {0,0,0};
			float[] edge1 = {0,0,0};
			float[] edge2 = {0,0,0};
			
			float[] v0 = sd.vertexList.get(vIndices[0]);
			float[] v1 = sd.vertexList.get(vIndices[1]);
			float[] v2 = sd.vertexList.get(vIndices[2]);

			MyMath.subtract(v1, v0, edge1);
			MyMath.subtract(v2, v0, edge2);
			
			MyMath.crossProduct(edge1, edge2, normal);
			MyMath.normalize(normal);

			boolean found = false;
			for(int i=0; i < sd.normalList.size(); i++) {
				if(MyMath.areEqual(sd.normalList.get(i), normal)) {
					normalIndexToUse = (short)i;
					found = true;
					break;
				}
			}
			if(!found) {
				normalIndexToUse = (short) sd.normalList.size();
				sd.normalList.add(normal);
			}
		}

		for (int i = 1; i < numVertices - 1; i++) {
			md.addIndex(vIndices[0], normalIndexToUse, tIndices[0], sd);
			md.addIndex(vIndices[i], normalIndexToUse, tIndices[i], sd);
			md.addIndex(vIndices[i+1], normalIndexToUse, tIndices[i+1], sd);
		}
	}

	/**
	 * Parses a 3D vector (vertex or normal) from an array of string tokens.
	 * Expects tokens like ["v", "x", "y", "z"] or ["vn", "nx", "ny", "nz"].
	 * 
	 * @param tokens The string tokens from a 'v' or 'vn' line.
	 * @return A float array of size 3 containing the parsed (x,y,z) values.
	 *         Returns {0,0,0} if parsing fails or not enough components.
	 */
	private float[] parseVector(String[] tokens) {
		float[] v = {0,0,0};
		// Tokens[0] is "v" or "vn", actual coordinates start from tokens[1]
		for(int i = 1; i < Math.min(4, tokens.length); i++) { // Read up to 3 coordinates (x,y,z)
			try {
				v[i-1] = Float.parseFloat(tokens[i]);
			} catch (NumberFormatException e) {
				System.err.println("ObjParser: Error parsing float value '" + tokens[i] + "' in line: " + String.join(" ", tokens) + ". Using 0.0.");
				v[i-1] = 0.0f; // Default to 0 if parsing fails
			}
		}
		return v;
	}

	/**
	 * Checks if vertex balancing is enabled.
	 * @return True if vertices will be centered around the origin, false otherwise.
	 */
	public boolean isBalanceVertices() {
		return balanceVertices;
	}

	/**
	 * Enables or disables vertex balancing.
	 * @param balanceVertices True to enable balancing, false to disable.
	 */
	public void setBalanceVertices(boolean balanceVertices) {
		this.balanceVertices = balanceVertices;
	}
	
	/**
	 * Checks if automatic normal computation for simple faces is enabled.
	 * @return True if normals will be computed for faces defined only with vertex indices.
	 */
	public boolean isComputeNormals() {
		return computeNormals;
	}
	
	/**
	 * Enables or disables automatic normal computation for simple faces.
	 * @param computeNormals True to enable, false to disable.
	 */
	public void setComputeNormals(boolean computeNormals) {
		this.computeNormals = computeNormals;
	}
}

/**
 * Internal helper class for {@link ObjParser}.
 * Stores processed, unique vertex data (positions and normals) and the indices
 * that form triangles for the final {@link Mesh}. This helps in creating an indexed mesh,
 * reducing redundant vertex data.
 */
class MeshData
{
	/** Maps unique VertexData (v/vn/vt combination) to its final index in the mesh's vertex list. */
	HashMap<VertexData, Short> ids = new HashMap<VertexData, Short>();
	/** List of unique vertex positions (float[3]) for the mesh. */
	ArrayList<float[]> vertices = new ArrayList<float[]>();
	/** List of unique normal vectors (float[3]) corresponding to the vertices. Can contain nulls if normals are not used/found for a vertex. */
	ArrayList<float[]> normals = new ArrayList<float[]>();
    // ArrayList<float[]> texCoords = new ArrayList<float[]>(); // Could be added if texture coordinates are fully processed
	/** List of short indices forming triangles. Every three indices define one triangle. */
	ArrayList<Short> indices = new ArrayList<Short>();
	
	
	/**
	 * Populates a {@link Mesh} object using the vertex and index data stored in this MeshData instance.
	 * It creates {@link Vertex} objects and then {@link Polygon} objects for the mesh.
	 * 
	 * @param m The {@link Mesh} object to populate.
	 * @throws RuntimeException if there's an inconsistency in normal data (e.g., some vertices have normals, others don't, in a way that's problematic for structured mesh creation).
	 */
	void populateMesh(Mesh m)
	{
		// Determine if consistent normal data is available
		Boolean hasNormalsConsistently = null;
		if (!normals.isEmpty()) {
			boolean firstNormalPresent = normals.get(0) != null;
			hasNormalsConsistently = true;
			for (int i = 1; i < normals.size(); i++) {
				if ((normals.get(i) != null) != firstNormalPresent) {
					// This check might be too strict if some vertices intentionally lack normals,
					// but for simple mesh population, consistency is often assumed.
					// System.err.println("ObjParser.MeshData: Inconsistent normal data. Some vertices have normals, others do not. This might lead to unexpected rendering.");
					// For now, proceed by checking individual normals.
					hasNormalsConsistently = false; // Mark as inconsistent for this simple check.
					break;
				}
			}
			if (hasNormalsConsistently && !firstNormalPresent) hasNormalsConsistently = false; // All null is not "has normals"
		} else {
			hasNormalsConsistently = false;
		}

		Vertex[] finalVertices = new Vertex[vertices.size()];
		for(int i = 0; i < finalVertices.length; i++)
		{
			finalVertices[i] = new Vertex(vertices.get(i));
			if(hasNormalsConsistently && i < normals.size() && normals.get(i) != null) { // Check size and null
				finalVertices[i].normal = normals.get(i);
			}
			// TODO: Add texture coordinate assignment here if texCoords list is populated
		}
		
		// Create polygons (triangles)
		for(int i = 0; i < indices.size(); i+=3)
		{
			if (i + 2 < indices.size()) { // Ensure there are enough indices for a full triangle
				Vertex[] triVertices = new Vertex[]{
						finalVertices[indices.get(i)], 
						finalVertices[indices.get(i+1)], 
						finalVertices[indices.get(i+2)]
				};
				m.addPolygon(new Polygon(triVertices));
			} else {
				System.err.println("ObjParser.MeshData: Incomplete triangle at end of index list. Expected 3 indices, found " + (indices.size() - i));
			}
		}
	}

	/**
	 * Adds a vertex to the mesh data based on its original index in the OBJ file's vertex list.
	 * This version is used when only vertex position is known (no explicit normal or texture coord).
	 * It checks if this vertex (by its original index) has already been added; if so, reuses it.
	 * Otherwise, adds it to the list of unique vertices.
	 * 
	 * @param vertexIndex The 0-based index of the vertex in {@link ScanData#vertexList}.
	 * @param sd The {@link ScanData} object containing the raw vertex list.
	 */
	void addIndex(short vertexIndex, ScanData sd) // Legacy or simplified path
	{
		// This signature is ambiguous if normals might be computed later.
		// Assuming this means no normal and no texture coord for this specific vertex instance.
		addIndex(vertexIndex, (short)-1, (short)-1, sd);
	}
	
	/**
	 * Adds a vertex to the mesh data, defined by its original vertex index and normal index.
	 * Texture coordinate index is assumed to be absent (-1).
	 * Checks if this combination of (vertex index, normal index) has been added before.
	 * If so, reuses the existing final vertex. Otherwise, creates a new final vertex.
	 * 
	 * @param vertexIndex The 0-based index in {@link ScanData#vertexList}.
	 * @param normalIndex The 0-based index in {@link ScanData#normalList}.
	 * @param sd The {@link ScanData} object.
	 */
	void addIndex(short vertexIndex, short normalIndex, ScanData sd) // Original signature
	{
		addIndex(vertexIndex, normalIndex, (short)-1, sd); // Call the full signature with no texture index
	}

	/**
	 * Adds a vertex to the mesh data, defined by its original vertex, normal, and texture coordinate indices.
	 * This is the most complete way to define a unique vertex for the mesh.
	 * It checks if this specific combination (v/vn/vt) already exists. If so, its existing
	 * final index is reused. Otherwise, a new entry is created in {@code vertices}, {@code normals},
	 * (and potentially {@code texCoords} if implemented), and its new final index is recorded.
	 * 
	 * @param vIdx The 0-based index of the vertex in {@link ScanData#vertexList}.
	 * @param nIdx The 0-based index of the normal in {@link ScanData#normalList} (-1 if not present).
	 * @param tIdx The 0-based index of the texture coordinate in a hypothetical ScanData.texCoordList (-1 if not present).
	 * @param sd The {@link ScanData} object.
	 */
	void addIndex(short vIdx, short nIdx, short tIdx, ScanData sd)
	{
		VertexData key = new VertexData(vIdx, tIdx, nIdx); // Key uses v, t, n
		Short finalIndex = ids.get(key);

		if(finalIndex == null)
		{
			finalIndex = (short) vertices.size();
			ids.put(key, finalIndex);
			
			if (vIdx >= sd.vertexList.size() || vIdx < 0) {
				System.err.println("ObjParser.MeshData: Vertex index " + vIdx + " out of bounds for vertexList size " + sd.vertexList.size() + ". Using default vertex.");
				vertices.add(new float[]{0,0,0}); // Add a default/dummy vertex
			} else {
				vertices.add(sd.vertexList.get(vIdx));
			}

			if(nIdx != -1 && nIdx < sd.normalList.size()) {
				normals.add(sd.normalList.get(nIdx));
			} else {
				// If nIdx is -1 (no normal specified) or out of bounds, decide what to do.
				// Option 1: Add null, handle later (current approach).
				// Option 2: If computeNormals was true and this path is hit, it might indicate an issue,
				//           as simple faces should have had normals computed and added to sd.normalList.
				//           However, v//vn or v/vt/vn might provide valid nIdx.
				normals.add(null); 
			}
			
			// TODO: Add texture coordinate handling if ScanData and VertexData are extended for it
			// if(tIdx != -1 && tIdx < sd.texCoordList.size()) {
			//    texCoords.add(sd.texCoordList.get(tIdx));
			// } else {
			//    texCoords.add(null);
			// }
		}
		indices.add(finalIndex);
	}
	
}

/**
 * Internal helper class for {@link ObjParser}.
 * Temporarily stores raw data read from the OBJ file during the first pass,
 * primarily lists of vertex positions and vertex normals.
 */
class ScanData
{
	/** List of vertex positions (float[3]) as read directly from 'v' lines in the OBJ file. */
	ArrayList<float[]> vertexList = new ArrayList<float[]>();
	/** List of vertex normals (float[3]) as read directly from 'vn' lines or computed for simple faces. */
	ArrayList<float[]> normalList = new ArrayList<float[]>();
	// ArrayList<float[]> texCoordList = new ArrayList<float[]>(); // Could be added for 'vt' lines
}

/**
 * Internal helper class for {@link ObjParser}.
 * Represents a unique combination of vertex index, texture coordinate index, and normal index.
 * Used as a key in a HashMap within {@link MeshData} to ensure that identical vertices
 * (in terms of position, normal, and texture coordinate) are reused rather than duplicated
 * when constructing the final mesh. This is crucial for creating indexed meshes.
 */
class VertexData
{
	/** The 0-based index into the original list of vertices read from the OBJ file ({@link ScanData#vertexList}). */
	short vindex;
	/** The 0-based index into the original list of texture coordinates ({@link ScanData#texCoordList}, if implemented). -1 if not present. */
	short tindex;
	/** The 0-based index into the original list of normals ({@link ScanData#normalList}). -1 if not present. */
	short nindex;

	/**
	 * Constructs a VertexData object.
	 * @param vindex Index of the vertex position.
	 * @param tindex Index of the texture coordinate (-1 if none).
	 * @param nindex Index of the normal (-1 if none).
	 */
	public VertexData(short vindex, short tindex, short nindex)
	{
		this.vindex = vindex;
		this.tindex = tindex;
		this.nindex = nindex;
	}
	
	/**
	 * Compares this VertexData with another object for equality.
	 * Two VertexData objects are equal if their vindex, tindex, and nindex are all identical.
	 * @param o The object to compare with.
	 * @return True if the objects are equal, false otherwise.
	 */
	@Override
	public boolean equals(Object o) 
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		VertexData other = (VertexData)o;
		return other.vindex == vindex && other.tindex == tindex && other.nindex == nindex;
	}
	
	/**
	 * Computes a hash code for this VertexData object.
	 * The hash code is based on a combination of vindex, tindex, and nindex.
	 * @return The hash code.
	 */
	@Override
	public int hashCode() {
		// A simple XOR hash. More sophisticated hashing could be used if performance issues arise with HashMap.
		int result = vindex;
		result = 31 * result + tindex;
		result = 31 * result + nindex;
		return result;
	}
}
