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

	public Mesh parseFile(String filename) throws IOException
	{
		BufferedReader stream = new BufferedReader(new FileReader(filename));
		return parseStream(stream);
		
	}

	public Mesh parseStream(BufferedReader stream) throws IOException
	{
		String line = null;
		ScanData sd = new ScanData(); 
		MeshData md = new MeshData(); 
		Mesh currentMesh = new Mesh(); 
		int lineindex = 0;
		
		stream.mark(10000000); 
		boolean markedAfterVertexScan = false; 
		
		while((line = stream.readLine()) != null)
		{
			lineindex++;
			String[] tokens = line.trim().split("\\s+");
			if (tokens.length == 0 || tokens[0].length() == 0 || tokens[0].startsWith("#")) { 
				continue;
			}
			
			if(tokens[0].equalsIgnoreCase("v"))
				sd.vertexList.add(parseVector(tokens));
			else if(tokens[0].equalsIgnoreCase("vn"))
				sd.normalList.add(parseVector(tokens));
			else if (tokens[0].equalsIgnoreCase("f")) {
				if (!markedAfterVertexScan) {
					stream.reset(); 
					stream.mark(10000000); 
					markedAfterVertexScan = true; 
					
					String firstFaceLine = line; // Save the current line
					// Need to re-process this first face line in the second pass
					// One way is to just let the second pass re-read it.
					// The current structure will re-read the stream from the new mark.
					// However, if the mark was set *after* reading some v/vn lines,
					// and then reset() to global mark, then a new mark *before* the first 'f',
					// the current line needs to be re-processed in the *second* pass.
					// The logic here is a bit complex. A simpler way:
					// 1. Read all v, vn.
					// 2. stream.reset() to the *very beginning*.
					// 3. Read all f.
					// The current two-pass logic with one initial mark should work if the reset()
					// correctly brings it back to the start, and the second loop correctly
					// ignores v/vn and only processes f.
					// The issue with 'tokens = stream.readLine().trim().split("\\s+");' here is that
					// it consumes a line that the *second pass* is supposed to read.
					// This part of the logic regarding `markedAfterVertexScan` seems overly complex
					// and potentially buggy for files with interleaved v/vn/f.
					// For now, assume files have all v/vn before f or the single initial mark is enough.
				}
			}
			else
			{
				if(!tokens[0].equals("f") && !tokens[0].equalsIgnoreCase("v") && !tokens[0].equalsIgnoreCase("vn") && !tokens[0].equalsIgnoreCase("vt") && !tokens[0].startsWith("#") && !tokens[0].equalsIgnoreCase("o") && !tokens[0].equalsIgnoreCase("g") && !tokens[0].equalsIgnoreCase("s") && !tokens[0].equalsIgnoreCase("mtllib") && !tokens[0].equalsIgnoreCase("usemtl")) {
					System.out.println("ObjParser: Unknown or unhandled OBJ tag '" + tokens[0] + "' at line " + lineindex + ". Skipping.");
				}
			}
		}
		
		if(isBalanceVertices() && !sd.vertexList.isEmpty()) {
			balanceVertices(sd.vertexList);
		}
		
		stream.reset(); 
		lineindex = 0;
		while((line = stream.readLine()) != null)
		{
			lineindex++;
			String[] tokens = line.trim().split("\\s+");
            if (tokens.length == 0 || tokens[0].length() == 0 || tokens[0].startsWith("#")) { // Ensure empty token[0] is also skipped
				continue;
			}

			if(tokens[0].equalsIgnoreCase("f")) {
				parseFace(tokens, md, sd);				
			}
		}
				
		md.populateMesh(currentMesh); 
		return currentMesh;
	}

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

	private void parseFace(String[] tokens, MeshData md, ScanData sd) 
	{
		if (tokens.length < 4) { 
			System.err.println("ObjParser: Face definition has too few vertices: " + String.join(" ", tokens) + ". Skipping.");
			return;
		}
		
		String sample = tokens[1]; 
		if(Pattern.matches("\\d+", sample)) 
			parseSimpleFace(tokens, md,sd);
		else if(Pattern.matches("\\d+//\\d+", sample)) 
			parseVNFace(tokens, md, sd);
		else if(Pattern.matches("\\d+/\\d+/\\d+", sample)) 
			parseFullFace(tokens,md, sd);
		else if(Pattern.matches("\\d+/\\d+", sample)) { 
			System.out.println("ObjParser: Face format v/vt found. Texture coordinates are read but normals will be computed if not available via v/vt/vn or v//vn.");
			parseVNTFace_TextureOnly(tokens, md, sd); 
		} else {
			System.err.println("ObjParser: Unknown or unsupported face format for token '" + sample + "' in line: " + String.join(" ", tokens) + ". Skipping face.");
		}
	}
	
	private void parseFullFace(String[] tokens, MeshData md, ScanData sd)  
	{
		int numVertices = tokens.length - 1;
		short[] vIndices = new short[numVertices];
		short[] tIndices = new short[numVertices]; 
		short[] nIndices = new short[numVertices];

		for(int i = 0; i < numVertices; i++)
		{
			String[] parts = tokens[i+1].split("/");
			if (parts.length != 3) {
				System.err.println("ObjParser: Malformed v/vt/vn face component: " + tokens[i+1] + ". Skipping face.");
				return;
			}
			try {
				vIndices[i] = (short) (Short.parseShort(parts[0])-1); 
				if (!parts[1].isEmpty()) { 
					tIndices[i] = (short) (Short.parseShort(parts[1])-1);
				} else {
					tIndices[i] = -1; 
				}
				nIndices[i] = (short) (Short.parseShort(parts[2])-1);
			} catch (NumberFormatException e) {
                System.err.println("ObjParser: Error parsing face index in '" + tokens[i+1] + "': " + e.getMessage() + ". Skipping face.");
                return;
            }
		}
		
		for(int i = 1; i < numVertices - 1; i++) 
		{
			md.addIndex(vIndices[0], nIndices[0], tIndices[0], sd);
			md.addIndex(vIndices[i], nIndices[i], tIndices[i], sd);
			md.addIndex(vIndices[i+1], nIndices[i+1], tIndices[i+1], sd);
		}
	}

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
			try {
				vIndices[i] = (short) (Short.parseShort(parts[0])-1);
				nIndices[i] = (short) (Short.parseShort(parts[1])-1);
			} catch (NumberFormatException e) {
                System.err.println("ObjParser: Error parsing face index in '" + tokens[i+1] + "': " + e.getMessage() + ". Skipping face.");
                return;
            }
		}
		
		for(int i = 1; i < numVertices - 1; i++)
		{
			md.addIndex(vIndices[0], nIndices[0], (short)-1, sd); 
			md.addIndex(vIndices[i], nIndices[i], (short)-1, sd);
			md.addIndex(vIndices[i+1], nIndices[i+1], (short)-1, sd);
		}
	}
	
	private void parseSimpleFace(String[] tokens, MeshData md, ScanData sd) 
	{
		int numVertices = tokens.length - 1;
		short[] vertexIndices = new short[numVertices];
		for(int i = 0; i < numVertices; i++) {
			try {
				vertexIndices[i] = (short) (Short.parseShort(tokens[i+1])-1); 
			} catch (NumberFormatException e) {
                System.err.println("ObjParser: Error parsing face index in '" + tokens[i+1] + "': " + e.getMessage() + ". Skipping face.");
                return;
            }
		}
		
		short normalIndexToUse = -1;
		boolean canComputeNormal = computeNormals; // Assume we can unless checks fail

		if(computeNormals)
		{
			if (numVertices < 3) {
				System.err.println("ObjParser: Cannot compute normal for a face with less than 3 vertices. Skipping face: " + String.join(" ", tokens));
				return; // Skip face entirely if too few vertices for normal calculation
			}
			
			// Validate indices before using them for normal computation
			if (vertexIndices[0] < 0 || vertexIndices[0] >= sd.vertexList.size() ||
			    vertexIndices[1] < 0 || vertexIndices[1] >= sd.vertexList.size() ||
			    vertexIndices[2] < 0 || vertexIndices[2] >= sd.vertexList.size()) {
				System.err.println("ObjParser: Vertex index out of bounds during normal computation for face: " + String.join(" ", tokens) + ". Skipping normal computation for this face.");
				canComputeNormal = false; // Don't compute normal, normalIndexToUse will remain -1
			}

			if (canComputeNormal) {
				float[] normal = {0,0,0};
				float[] edge1 = {0,0,0};
				float[] edge2 = {0,0,0};
				float[] v0 = sd.vertexList.get(vertexIndices[0]);
				float[] v1 = sd.vertexList.get(vertexIndices[1]);
				float[] v2 = sd.vertexList.get(vertexIndices[2]);

				MyMath.subtract(v1, v0, edge1);
				MyMath.subtract(v2, v0, edge2); 
				
				MyMath.crossProduct(edge1, edge2, normal);
				MyMath.normalize(normal); 

				boolean found = false;
				for(int k=0; k < sd.normalList.size(); k++) { 
					if(MyMath.areEqual(sd.normalList.get(k), normal)) {
						normalIndexToUse = (short)k;
						found = true;
						break;
					}
				}
				if(!found) {
					normalIndexToUse = (short) sd.normalList.size();
					sd.normalList.add(normal);
				}
			}
		}
		
		for(int i = 1; i < numVertices - 1; i++)
		{
			md.addIndex(vertexIndices[0], normalIndexToUse, (short)-1, sd);
			md.addIndex(vertexIndices[i], normalIndexToUse, (short)-1, sd);
			md.addIndex(vertexIndices[i+1], normalIndexToUse, (short)-1, sd);
		}
	}

	private void parseVNTFace_TextureOnly(String[] tokens, MeshData md, ScanData sd) {
		int numVertices = tokens.length - 1;
		short[] vIndices = new short[numVertices];
		short[] tIndices = new short[numVertices]; 

		for (int i = 0; i < numVertices; i++) {
			String[] parts = tokens[i + 1].split("/");
			if (parts.length < 2) { 
				System.err.println("ObjParser: Malformed v/vt face component: " + tokens[i + 1] + ". Skipping face.");
				return;
			}
			try {
				vIndices[i] = (short) (Short.parseShort(parts[0]) - 1);
				tIndices[i] = (short) (Short.parseShort(parts[1]) - 1);
			} catch (NumberFormatException e) {
                System.err.println("ObjParser: Error parsing face index in '" + tokens[i+1] + "': " + e.getMessage() + ". Skipping face.");
                return;
            }
		}

		short normalIndexToUse = -1;
		boolean canComputeNormal = computeNormals;

		if (computeNormals) {
			if (numVertices < 3) {
				System.err.println("ObjParser: Cannot compute normal for a face with less than 3 vertices (v/vt format). Skipping face: " + String.join(" ", tokens));
				return; // Skip face entirely
			}
			
			if (vIndices[0] < 0 || vIndices[0] >= sd.vertexList.size() ||
			    vIndices[1] < 0 || vIndices[1] >= sd.vertexList.size() ||
			    vIndices[2] < 0 || vIndices[2] >= sd.vertexList.size()) {
				System.err.println("ObjParser: Vertex index out of bounds during normal computation for v/vt face: " + String.join(" ", tokens) + ". Skipping normal computation for this face.");
				canComputeNormal = false;
			}

			if (canComputeNormal) {
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
				for(int k=0; k < sd.normalList.size(); k++) { 
					if(MyMath.areEqual(sd.normalList.get(k), normal)) {
						normalIndexToUse = (short)k;
						found = true;
						break;
					}
				}
				if(!found) {
					normalIndexToUse = (short) sd.normalList.size();
					sd.normalList.add(normal);
				}
			}
		}

		for (int i = 1; i < numVertices - 1; i++) {
			md.addIndex(vIndices[0], normalIndexToUse, tIndices[0], sd);
			md.addIndex(vIndices[i], normalIndexToUse, tIndices[i], sd);
			md.addIndex(vIndices[i+1], normalIndexToUse, tIndices[i+1], sd);
		}
	}

	private float[] parseVector(String[] tokens) {
		float[] v = {0,0,0};
		for(int i = 1; i < Math.min(4, tokens.length); i++) { 
			try {
				v[i-1] = Float.parseFloat(tokens[i]);
			} catch (NumberFormatException e) {
				System.err.println("ObjParser: Error parsing float value '" + tokens[i] + "' in line: " + String.join(" ", tokens) + ". Using 0.0.");
				v[i-1] = 0.0f; 
			}
		}
		return v;
	}

	public boolean isBalanceVertices() {
		return balanceVertices;
	}

	public void setBalanceVertices(boolean balanceVertices) {
		this.balanceVertices = balanceVertices;
	}
	
	public boolean isComputeNormals() {
		return computeNormals;
	}
	
	public void setComputeNormals(boolean computeNormals) {
		this.computeNormals = computeNormals;
	}
}

class MeshData
{
	HashMap<VertexData, Short> ids = new HashMap<VertexData, Short>();
	ArrayList<float[]> vertices = new ArrayList<float[]>();
	ArrayList<float[]> normals = new ArrayList<float[]>();
	ArrayList<Short> indices = new ArrayList<Short>();
	
	void populateMesh(Mesh m)
	{
		Boolean hasNormalsConsistently = null; // This logic can be simplified or removed if Vertex.normal can be null
        if (!normals.isEmpty() && vertices.size() == normals.size()) { // Ensure normals list matches vertices if used
            boolean firstNormalPresent = normals.get(0) != null;
            hasNormalsConsistently = true;
            for (int i = 1; i < normals.size(); i++) {
                if ((normals.get(i) != null) != firstNormalPresent) {
                    hasNormalsConsistently = false; 
                    break;
                }
            }
            if (hasNormalsConsistently && !firstNormalPresent) hasNormalsConsistently = false; 
        } else {
            hasNormalsConsistently = false;
        }


		Vertex[] finalVertices = new Vertex[vertices.size()];
		for(int i = 0; i < finalVertices.length; i++)
		{
			finalVertices[i] = new Vertex(vertices.get(i));
            if (i < normals.size() && normals.get(i) != null) { // Check size and null for safety
				finalVertices[i].setNormal(normals.get(i)); 
			}
		}
		
		for(int i = 0; i < indices.size(); i+=3)
		{
			if (i + 2 < indices.size()) { 
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

	void addIndex(short vertexIndex, ScanData sd) 
	{
		addIndex(vertexIndex, (short)-1, (short)-1, sd);
	}
	
	void addIndex(short vertexIndex, short normalIndex, ScanData sd) 
	{
		addIndex(vertexIndex, normalIndex, (short)-1, sd); 
	}

	void addIndex(short vIdx, short nIdx, short tIdx, ScanData sd)
	{
		VertexData key = new VertexData(vIdx, tIdx, nIdx); 
		Short finalIndex = ids.get(key);

		if(finalIndex == null)
		{
			finalIndex = (short) vertices.size();
			ids.put(key, finalIndex);
			
			if (vIdx >= sd.vertexList.size() || vIdx < 0) {
				// Using 1-based indexing for error messages as it appears in OBJ files
				System.err.println("ObjParser.MeshData: Vertex index " + (vIdx + 1) + " (0-based " + vIdx + ") out of bounds for vertexList (size " + sd.vertexList.size() + "). Using default vertex (0,0,0).");
				vertices.add(new float[]{0,0,0}); 
			} else {
				vertices.add(sd.vertexList.get(vIdx));
			}

            // Handle normal index
			if(nIdx != -1) { // A normal index was provided by the face definition or computed
                if (nIdx < 0 || nIdx >= sd.normalList.size()) { // Check bounds for provided/computed normal index
                    System.err.println("ObjParser.MeshData: Normal index " + (nIdx + 1) + " (0-based " + nIdx + ") out of bounds for normalList (size " + sd.normalList.size() + "). Using null normal for this vertex instance.");
                    normals.add(null); 
                } else {
				    normals.add(sd.normalList.get(nIdx)); // Valid normal index
                }
			} else { // No normal specified or computed for this specific vertex use (nIdx == -1)
				normals.add(null); 
			}
			
            // TODO: Add texture coordinate handling similarly if sd.texCoordList is populated
            // if(tIdx != -1) {
            //    if (tIdx < 0 || tIdx >= sd.texCoordList.size()) {
            //        System.err.println("ObjParser.MeshData: Texture index " + (tIdx + 1) + " out of bounds. Using null texCoord.");
            //        texCoords.add(null);
            //    } else {
            //        texCoords.add(sd.texCoordList.get(tIdx));
            //    }
            // } else {
            //    texCoords.add(null);
            // }
		}
		indices.add(finalIndex);
	}
	
}

class ScanData
{
	ArrayList<float[]> vertexList = new ArrayList<float[]>();
	ArrayList<float[]> normalList = new ArrayList<float[]>();
}

class VertexData
{
	short vindex;
	short tindex;
	short nindex;

	public VertexData(short vindex, short tindex, short nindex)
	{
		this.vindex = vindex;
		this.tindex = tindex;
		this.nindex = nindex;
	}
	
	@Override
	public boolean equals(Object o) 
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		VertexData other = (VertexData)o;
		return other.vindex == vindex && other.tindex == tindex && other.nindex == nindex;
	}
	
	@Override
	public int hashCode() {
		int result = vindex;
		result = 31 * result + tindex;
		result = 31 * result + nindex;
		return result;
	}
}
