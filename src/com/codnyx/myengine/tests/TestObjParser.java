package com.codnyx.myengine.tests;

import static org.junit.Assert.*;
import org.junit.Test;

import com.codnyx.myengine.Mesh;
import com.codnyx.myengine.ObjParser;
import com.codnyx.myengine.Polygon;
import com.codnyx.myengine.Vertex;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;

public class TestObjParser {

    private static final float DELTA = 1e-5f; // Increased precision for vertex comparisons

    private void assertVertexPosition(Vertex v, float x, float y, float z, String message) {
        assertNotNull(message + " - vertex should not be null", v);
        assertNotNull(message + " - vertex point array should not be null", v.point);
        assertEquals(message + " - X coordinate", x, v.point[0], DELTA);
        assertEquals(message + " - Y coordinate", y, v.point[1], DELTA);
        assertEquals(message + " - Z coordinate", z, v.point[2], DELTA);
    }

    private void assertVertexNormal(Vertex v, float nx, float ny, float nz, String message) {
        assertNotNull(message + " - vertex should not be null", v);
        assertNotNull(message + " - vertex normal array should not be null", v.normal);
        assertEquals(message + " - Normal X", nx, v.normal[0], DELTA);
        assertEquals(message + " - Normal Y", ny, v.normal[1], DELTA);
        assertEquals(message + " - Normal Z", nz, v.normal[2], DELTA);
    }
    
    private void assertPolygonHasVertex(Polygon p, float[] expectedPosition, String message) {
        boolean found = false;
        for (Vertex v : p.getVertices()) {
            if (Math.abs(v.point[0] - expectedPosition[0]) < DELTA &&
                Math.abs(v.point[1] - expectedPosition[1]) < DELTA &&
                Math.abs(v.point[2] - expectedPosition[2]) < DELTA) {
                found = true;
                break;
            }
        }
        assertTrue(message + " - Polygon should contain vertex (" + expectedPosition[0] + "," + expectedPosition[1] + "," + expectedPosition[2] + ")", found);
    }


    @Test
    public void testParseSimpleCube_VerticesOnlyFaces() throws IOException {
        String objContent = 
            "v 1.0 1.0 -1.0\n" +
            "v 1.0 -1.0 -1.0\n" +
            "v 1.0 1.0 1.0\n" +
            "v 1.0 -1.0 1.0\n" +
            "f 1 2 4\n" +  // Triangle 1
            "f 1 4 3\n";   // Triangle 2

        ObjParser parser = new ObjParser();
        parser.setBalanceVertices(false); // Keep original coordinates for easier testing
        parser.setComputeNormals(true); // Default, should compute
        Mesh mesh = parser.parseStream(new BufferedReader(new StringReader(objContent)));

        assertEquals("Should have 2 polygons", 2, mesh.polygons.size());
        
        Polygon p1 = mesh.polygons.get(0);
        assertEquals("Polygon 1 should have 3 vertices", 3, p1.getVertices().length);
        assertVertexPosition(p1.getVertices()[0], 1.0f, 1.0f, -1.0f, "P1V1"); // v1
        assertVertexPosition(p1.getVertices()[1], 1.0f, -1.0f, -1.0f, "P1V2"); // v2
        assertVertexPosition(p1.getVertices()[2], 1.0f, -1.0f, 1.0f, "P1V3");  // v4

        // Check computed normal for the first polygon (1,2,4) -> (1,1,-1), (1,-1,-1), (1,-1,1)
        // Edge1 (v2-v1): (0, -2, 0)
        // Edge2 (v4-v1): (0, -2, 2)
        // Normal (Edge1 x Edge2): (-4, 0, 0). Normalized: (-1, 0, 0)
        for (Vertex v : p1.getVertices()) {
            assertVertexNormal(v, -1.0f, 0.0f, 0.0f, "P1 Computed Normal");
        }
    }

    @Test
    public void testParseCube_VertexNormalFaces() throws IOException {
        String objContent =
            "v 1.0 0.0 0.0\n" +
            "v 0.0 1.0 0.0\n" +
            "v 0.0 0.0 1.0\n" +
            "vn 0.0 0.0 1.0\n" + // Normal 1 (points to +Z)
            "vn 1.0 0.0 0.0\n" + // Normal 2 (points to +X)
            "f 1//1 2//1 3//1\n" + // Face using normal 1
            "f 1//2 3//2 2//2\n";  // Face using normal 2

        ObjParser parser = new ObjParser();
        parser.setBalanceVertices(false);
        Mesh mesh = parser.parseStream(new BufferedReader(new StringReader(objContent)));

        assertEquals("Should have 2 polygons", 2, mesh.polygons.size());

        Polygon p1 = mesh.polygons.get(0);
        assertEquals("P1 should have 3 vertices", 3, p1.getVertices().length);
        assertVertexPosition(p1.getVertices()[0], 1.0f, 0.0f, 0.0f, "P1V1");
        assertVertexNormal(p1.getVertices()[0], 0.0f, 0.0f, 1.0f, "P1V1 Normal");
        assertVertexPosition(p1.getVertices()[1], 0.0f, 1.0f, 0.0f, "P1V2");
        assertVertexNormal(p1.getVertices()[1], 0.0f, 0.0f, 1.0f, "P1V2 Normal");
        assertVertexPosition(p1.getVertices()[2], 0.0f, 0.0f, 1.0f, "P1V3");
        assertVertexNormal(p1.getVertices()[2], 0.0f, 0.0f, 1.0f, "P1V3 Normal");

        Polygon p2 = mesh.polygons.get(1);
        assertEquals("P2 should have 3 vertices", 3, p2.getVertices().length);
        assertVertexPosition(p2.getVertices()[0], 1.0f, 0.0f, 0.0f, "P2V1");
        assertVertexNormal(p2.getVertices()[0], 1.0f, 0.0f, 0.0f, "P2V1 Normal");
    }

    @Test
    public void testParseCube_VertexTextureNormalFaces() throws IOException {
        String objContent =
            "v 1.0 0.0 0.0\n" +
            "v 0.0 1.0 0.0\n" +
            "v 0.0 0.0 1.0\n" +
            "vt 0.0 0.0\n" +     // Texcoord 1
            "vt 1.0 0.0\n" +     // Texcoord 2
            "vt 0.0 1.0\n" +     // Texcoord 3
            "vn 0.0 0.0 1.0\n" + // Normal 1
            "f 1/1/1 2/2/1 3/3/1\n";

        ObjParser parser = new ObjParser();
        parser.setBalanceVertices(false);
        Mesh mesh = parser.parseStream(new BufferedReader(new StringReader(objContent)));
        
        assertEquals("Should have 1 polygon", 1, mesh.polygons.size());
        Polygon p1 = mesh.polygons.get(0);
        assertEquals("Polygon should have 3 vertices", 3, p1.getVertices().length);
        assertVertexPosition(p1.getVertices()[0], 1.0f, 0.0f, 0.0f, "P1V1");
        assertVertexNormal(p1.getVertices()[0], 0.0f, 0.0f, 1.0f, "P1V1 Normal");
        // Texture coordinates are parsed for VertexData uniqueness but not stored on Vertex object by current parser
    }
    
    @Test
    public void testParseCube_VertexTextureFaces_ComputeNormals() throws IOException {
        String objContent =
            "v 1.0 0.0 0.0\n" +
            "v 0.0 1.0 0.0\n" +
            "v 0.0 0.0 0.0\n" + // For flat XY plane face
            "vt 0.0 0.0\n" +
            "vt 1.0 0.0\n" +
            "vt 0.0 1.0\n" +
            "f 1/1 2/2 3/3\n"; // Face on XY plane, normal should be (0,0,1) or (0,0,-1)

        ObjParser parser = new ObjParser();
        parser.setBalanceVertices(false);
        parser.setComputeNormals(true); // Ensure normals are computed
        Mesh mesh = parser.parseStream(new BufferedReader(new StringReader(objContent)));
        
        assertEquals("Should have 1 polygon", 1, mesh.polygons.size());
        Polygon p1 = mesh.polygons.get(0);
        // Vertices: (1,0,0), (0,1,0), (0,0,0)
        // Edge1 (v2-v1): (-1, 1, 0)
        // Edge2 (v3-v1): (-1, 0, 0)
        // Normal (Edge1 x Edge2): (0, 0, 1)
        for(Vertex v : p1.getVertices()) {
            assertVertexNormal(v, 0.0f, 0.0f, 1.0f, "Computed Normal for v/vt face");
        }
    }

    @Test
    public void testParseQuadFace_Triangulation() throws IOException {
        String objContent =
            "v 0.0 0.0 0.0\n" +
            "v 1.0 0.0 0.0\n" +
            "v 1.0 1.0 0.0\n" +
            "v 0.0 1.0 0.0\n" +
            "f 1 2 3 4\n"; // Quad face

        ObjParser parser = new ObjParser();
        parser.setBalanceVertices(false);
        Mesh mesh = parser.parseStream(new BufferedReader(new StringReader(objContent)));

        assertEquals("Quad should be triangulated into 2 polygons", 2, mesh.polygons.size());
        // Triangle 1: v1, v2, v3
        Polygon p1 = mesh.polygons.get(0);
        assertVertexPosition(p1.getVertices()[0], 0.0f, 0.0f, 0.0f, "QuadP1V1");
        assertVertexPosition(p1.getVertices()[1], 1.0f, 0.0f, 0.0f, "QuadP1V2");
        assertVertexPosition(p1.getVertices()[2], 1.0f, 1.0f, 0.0f, "QuadP1V3");
        
        // Triangle 2: v1, v3, v4
        Polygon p2 = mesh.polygons.get(1);
        assertVertexPosition(p2.getVertices()[0], 0.0f, 0.0f, 0.0f, "QuadP2V1");
        assertVertexPosition(p2.getVertices()[1], 1.0f, 1.0f, 0.0f, "QuadP2V2");
        assertVertexPosition(p2.getVertices()[2], 0.0f, 1.0f, 0.0f, "QuadP2V3");
    }
    
    @Test
    public void testParsePentagonFace_Triangulation() throws IOException {
        String objContent =
            "v 0.0 0.0 0.0\n" + //1
            "v 1.0 0.0 0.0\n" + //2
            "v 1.5 1.0 0.0\n" + //3
            "v 0.5 1.5 0.0\n" + //4
            "v -0.5 1.0 0.0\n" +//5
            "f 1 2 3 4 5\n"; // Pentagon face

        ObjParser parser = new ObjParser();
        parser.setBalanceVertices(false);
        Mesh mesh = parser.parseStream(new BufferedReader(new StringReader(objContent)));

        assertEquals("Pentagon should be triangulated into 3 polygons", 3, mesh.polygons.size());
        // Triangle 1: v1, v2, v3
        assertPolygonHasVertex(mesh.polygons.get(0), new float[]{0.0f, 0.0f, 0.0f}, "PentagonP1V1");
        assertPolygonHasVertex(mesh.polygons.get(0), new float[]{1.0f, 0.0f, 0.0f}, "PentagonP1V2");
        assertPolygonHasVertex(mesh.polygons.get(0), new float[]{1.5f, 1.0f, 0.0f}, "PentagonP1V3");

        // Triangle 2: v1, v3, v4
        assertPolygonHasVertex(mesh.polygons.get(1), new float[]{0.0f, 0.0f, 0.0f}, "PentagonP2V1");
        assertPolygonHasVertex(mesh.polygons.get(1), new float[]{1.5f, 1.0f, 0.0f}, "PentagonP2V3");
        assertPolygonHasVertex(mesh.polygons.get(1), new float[]{0.5f, 1.5f, 0.0f}, "PentagonP2V4");
        
        // Triangle 3: v1, v4, v5
        assertPolygonHasVertex(mesh.polygons.get(2), new float[]{0.0f, 0.0f, 0.0f}, "PentagonP3V1");
        assertPolygonHasVertex(mesh.polygons.get(2), new float[]{0.5f, 1.5f, 0.0f}, "PentagonP3V4");
        assertPolygonHasVertex(mesh.polygons.get(2), new float[]{-0.5f, 1.0f, 0.0f}, "PentagonP3V5");
    }


    @Test
    public void testCommentsAndEmptyLines() throws IOException {
        String objContent =
            "# This is a comment\n" +
            "v 1.0 0.0 0.0\n" +
            "\n" + // Empty line
            "v 0.0 1.0 0.0\n" +
            "     # Another comment with leading spaces\n" +
            "v 0.0 0.0 1.0\n" +
            "f 1 2 3\n";

        ObjParser parser = new ObjParser();
        parser.setBalanceVertices(false);
        Mesh mesh = parser.parseStream(new BufferedReader(new StringReader(objContent)));
        
        assertEquals("Should have 1 polygon", 1, mesh.polygons.size());
        assertEquals("Polygon should have 3 vertices", 3, mesh.polygons.get(0).getVertices().length);
        assertVertexPosition(mesh.polygons.get(0).getVertices()[0], 1.0f, 0.0f, 0.0f, "V1");
    }

    @Test
    public void testParseFile() throws IOException {
        // Uses src/com/codnyx/myengine/tests/res/test_cube_basic.obj
        // This file has 8 vertices and 12 faces (triangles).
        ObjParser parser = new ObjParser();
        // balanceVertices is true by default. Let's test that.
        // computeNormals is true by default.
        Mesh mesh = parser.parseFile("src/com/codnyx/myengine/tests/res/test_cube_basic.obj");

        assertEquals("Should have 12 polygons from file", 12, mesh.polygons.size());
        
        // Verify balancing: sum of vertex coordinates should be close to zero for each component
        float sumX = 0, sumY = 0, sumZ = 0;
        int totalVerticesInMesh = 0;
        // Need to iterate through unique vertices in the mesh, not just polygon vertices
        // This is hard without direct access to MeshData's internal vertices list.
        // Instead, check a known vertex from the file if its position is now balanced.
        // Original v1: 1.0 1.0 -1.0. Center of cube (0,0,0) with these vertices would be (0,0,0).
        // So balancing shouldn't change coordinates for this specific symmetric cube.
        // Let's find a vertex that was originally (1,1,-1)
        boolean foundOriginalV1 = false;
        for (Polygon p : mesh.polygons) {
            for (Vertex v : p.getVertices()) {
                // Check if a vertex is close to the balanced version of (1,1,-1)
                // Since the cube is symmetric around origin, balanceVertices will not change coords.
                if (Math.abs(v.point[0] - 1.0f) < DELTA &&
                    Math.abs(v.point[1] - 1.0f) < DELTA &&
                    Math.abs(v.point[2] - (-1.0f)) < DELTA) {
                    foundOriginalV1 = true;
                    // Check its normal (computed) for one of the faces it's part of.
                    // e.g. face 1 2 4 (1,1,-1), (1,-1,-1), (1,-1,1). Normal: (-1,0,0)
                    // e.g. face 1 4 3 (1,1,-1), (1,-1,1), (1,1,1). Normal: (1,0,0)
                    // Since vertex normals are shared if computed this way, it's tricky.
                    // The Vertex.normal will be one of the normals of the faces it belongs to.
                    // Let's just check that it *has* a normal.
                    assertNotNull("Balanced vertex should have a computed normal", v.normal);
                    break;
                }
            }
            if (foundOriginalV1) break;
        }
        assertTrue("A vertex corresponding to original (1,1,-1) should exist", foundOriginalV1);
    }
    
    @Test
    public void testParseStream_FromByteArrayInputStream() throws IOException {
         String objContent = 
            "v 1.0 1.0 -1.0\n" +
            "v 1.0 -1.0 -1.0\n" +
            "v 1.0 1.0 1.0\n" +
            "f 1 2 3\n";
        
        ByteArrayInputStream bais = new ByteArrayInputStream(objContent.getBytes(StandardCharsets.UTF_8));
        BufferedReader streamReader = new BufferedReader(new InputStreamReader(bais));
        
        ObjParser parser = new ObjParser();
        parser.setBalanceVertices(false);
        Mesh mesh = parser.parseStream(streamReader);
        
        assertEquals("Should have 1 polygon from stream", 1, mesh.polygons.size());
        assertVertexPosition(mesh.polygons.get(0).getVertices()[0], 1.0f, 1.0f, -1.0f, "StreamV1");
    }


    @Test
    public void testBalanceVerticesOption_False() throws IOException {
        String objContent = 
            "v 2.0 2.0 1.0\n" + // Deliberately off-center
            "v 2.0 0.0 1.0\n" +
            "v 2.0 2.0 3.0\n" +
            "f 1 2 3\n";
        ObjParser parser = new ObjParser();
        parser.setBalanceVertices(false); // Test with balancing OFF
        Mesh mesh = parser.parseStream(new BufferedReader(new StringReader(objContent)));
        
        assertEquals(1, mesh.polygons.size());
        assertVertexPosition(mesh.polygons.get(0).getVertices()[0], 2.0f, 2.0f, 1.0f, "V1 No Balance");
    }

    @Test
    public void testBalanceVerticesOption_True() throws IOException {
        String objContent = 
            "v 2.0 3.0 0.0\n" + // v1
            "v 4.0 3.0 0.0\n" + // v2
            "v 3.0 5.0 0.0\n" + // v3
            "f 1 2 3\n";
        // Center: x = (2+4+3)/3 = 9/3 = 3
        //         y = (3+3+5)/3 = 11/3 = 3.66667
        //         z = 0
        // Balanced v1: (2-3, 3-11/3, 0-0) = (-1, -2/3, 0) = (-1, -0.66667, 0)
        
        ObjParser parser = new ObjParser();
        parser.setBalanceVertices(true); // Test with balancing ON (default)
        Mesh mesh = parser.parseStream(new BufferedReader(new StringReader(objContent)));
        
        assertEquals(1, mesh.polygons.size());
        assertVertexPosition(mesh.polygons.get(0).getVertices()[0], -1.0f, -2.0f/3.0f, 0.0f, "V1 Balanced");
    }

    @Test
    public void testComputeNormalsOption_False() throws IOException {
        String objContent = 
            "v 1.0 0.0 0.0\n" +
            "v 0.0 1.0 0.0\n" +
            "v 0.0 0.0 0.0\n" +
            "f 1 2 3\n"; // Simple face
        ObjParser parser = new ObjParser();
        parser.setBalanceVertices(false);
        parser.setComputeNormals(false); // Test with computeNormals OFF
        Mesh mesh = parser.parseStream(new BufferedReader(new StringReader(objContent)));
        
        assertEquals(1, mesh.polygons.size());
        Polygon p1 = mesh.polygons.get(0);
        // Normals should be null or default (0,0,0) if not computed and not provided
        // MeshData.populateMesh sets Vertex.normal if normals list is consistent.
        // If computeNormals is false, sd.normalList might be empty or normals added via vn.
        // For a simple face 'f v1 v2 v3' with computeNormals=false, normalIndexToUse remains -1.
        // MeshData.addIndex with nIdx=-1 adds null to its normals list.
        // MeshData.populateMesh then won't set Vertex.normal if all normals are null.
        // However, Vertex.normal defaults to {0,0,1} if not explicitly set. This needs checking in Vertex class.
        // Assuming Vertex.normal is null if not set by MeshData:
        // For this test, since computeNormals is false, and no 'vn' lines,
        // the Vertex.normal in the final Mesh objects should reflect this.
        // The ObjParser's MeshData.populateMesh logic: if `normals.get(i)` is null, `finalVertices[i].normal` is not set.
        // The default Vertex constructor does not initialize `normal`. So it remains null.
        assertNull("Vertex normal should be null if not computed and not provided", p1.getVertices()[0].normal);
    }
    
    @Test
    public void testMalformedFace_TooFewVertices() throws IOException {
        // This test implicitly checks error handling by observing parser behavior.
        // The parser prints to System.err and skips the face.
        String objContent =
            "v 1.0 0.0 0.0\n" +
            "v 0.0 1.0 0.0\n" +
            "v 0.0 0.0 1.0\n" +
            "f 1 2\n"; // Malformed face (needs at least 3 vertices)

        ObjParser parser = new ObjParser();
        Mesh mesh = parser.parseStream(new BufferedReader(new StringReader(objContent)));
        assertEquals("Mesh should have 0 polygons as malformed face is skipped", 0, mesh.polygons.size());
        // To verify System.err, one would need to redirect System.err, which is complex in standard JUnit.
    }
    
    @Test
    public void testMalformedFace_BadIndex() throws IOException {
        String objContent =
            "v 1.0 0.0 0.0\n" +
            "f 1 2 300\n"; // Vertex 300 does not exist

        ObjParser parser = new ObjParser();
        Mesh mesh = parser.parseStream(new BufferedReader(new StringReader(objContent)));
        // MeshData.addIndex has a check:
        // if (vIdx >= sd.vertexList.size() || vIdx < 0) { ... vertices.add(new float[]{0,0,0}); }
        // So, it will add a polygon but with a default vertex at (0,0,0) for the bad index.
        assertEquals("Mesh should have 1 polygon despite bad index", 1, mesh.polygons.size());
        Polygon p1 = mesh.polygons.get(0);
        // v1, v2 (default), v3 (default)
        assertVertexPosition(p1.getVertices()[0], 1.0f, 0.0f, 0.0f, "BadIndexP1V1");
        assertVertexPosition(p1.getVertices()[1], 0.0f, 0.0f, 0.0f, "BadIndexP1V2 (defaulted)");
        assertVertexPosition(p1.getVertices()[2], 0.0f, 0.0f, 0.0f, "BadIndexP1V3 (defaulted)");
    }
}
