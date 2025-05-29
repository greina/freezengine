package com.codnyx.myengine.tests;

import static org.junit.Assert.*;
import org.junit.Test;

import com.codnyx.myengine.Polygon;
import com.codnyx.myengine.Vertex;
import com.codnyx.myengine.Texture; // For the public 'texture' field
import com.codnyx.myengine.MyMath; // For vector comparisons if needed

import java.awt.Color;

// Mock Texture class for testing the public 'texture' field
class MockTexture extends Texture {
    public String id;
    public MockTexture(String id) { this.id = id; }
    @Override
    public int getColor(int x, int y) { return 0; } // Dummy implementation
}

public class TestPolygon {

    private static final float DELTA = 1e-5f;

    private void assertFloatArrayEquals(String message, float[] expected, float[] actual, float delta) {
        assertEquals(message + " (array length)", expected.length, actual.length);
        for (int i = 0; i < expected.length; i++) {
            assertEquals(message + " (element " + i + ")", expected[i], actual[i], delta);
        }
    }

    // --- Test Constructor Polygon(Vertex[] vertices) ---
    @Test
    public void testConstructorWithVertices_Triangle() {
        Vertex v1 = new Vertex(new float[]{0f, 0f, 0f});
        Vertex v2 = new Vertex(new float[]{1f, 0f, 0f});
        Vertex v3 = new Vertex(new float[]{0f, 1f, 0f});
        Polygon p = new Polygon(new Vertex[]{v1, v2, v3});

        assertArrayEquals("Vertices should be stored", new Vertex[]{v1, v2, v3}, p.getVertices());
        
        // Normal: (v2-v1) x (v3-v1) = (1,0,0) x (0,1,0) = (0,0,1)
        // The computeNormal uses (v2-v1) x (v3-v2), let's check:
        // edge1 (v2-v1) = (1,0,0)
        // edge2 (v3-v2) = (-1,1,0)
        // cross (edge1, edge2) = (0,0,1)
        assertFloatArrayEquals("Normal should be computed (0,0,1)", new float[]{0f, 0f, 1f}, p.getNormal(), DELTA);
        
        // Center: ( (0+1+0)/3, (0+0+1)/3, (0+0+0)/3 ) = (1/3, 1/3, 0)
        assertFloatArrayEquals("Center should be computed", new float[]{1f/3f, 1f/3f, 0f}, p.getCenter(), DELTA);
    }

    @Test
    public void testConstructorWithVertices_Quad() {
        // Polygon class handles any number of vertices, normal/center based on first few
        Vertex v1 = new Vertex(new float[]{0f, 0f, 0f});
        Vertex v2 = new Vertex(new float[]{1f, 0f, 0f});
        Vertex v3 = new Vertex(new float[]{1f, 1f, 0f});
        Vertex v4 = new Vertex(new float[]{0f, 1f, 0f});
        Polygon p = new Polygon(new Vertex[]{v1, v2, v3, v4});

        assertEquals("Number of vertices for quad", 4, p.getVertices().length);
        // Normal based on v1,v2,v3: (v2-v1) x (v3-v2) = (1,0,0) x (0,1,0) = (0,0,1)
        assertFloatArrayEquals("Normal for quad (based on first 3 verts)", new float[]{0f, 0f, 1f}, p.getNormal(), DELTA);
        // Center: ( (0+1+1+0)/4, (0+0+1+1)/4, 0 ) = (0.5, 0.5, 0)
        assertFloatArrayEquals("Center for quad", new float[]{0.5f, 0.5f, 0f}, p.getCenter(), DELTA);
    }
    
    @Test
    public void testConstructorWithVertices_Collinear() {
        Vertex v1 = new Vertex(new float[]{0f, 0f, 0f});
        Vertex v2 = new Vertex(new float[]{1f, 0f, 0f});
        Vertex v3 = new Vertex(new float[]{2f, 0f, 0f}); // Collinear
        Polygon p = new Polygon(new Vertex[]{v1, v2, v3});
        // Normal: (1,0,0) x (1,0,0) = (0,0,0)
        assertFloatArrayEquals("Normal for collinear vertices should be (0,0,0)", new float[]{0f, 0f, 0f}, p.getNormal(), DELTA);
    }

    @Test
    public void testConstructorWithVertices_LessThan3() {
        Vertex v1 = new Vertex(new float[]{0f, 0f, 0f});
        Vertex v2 = new Vertex(new float[]{1f, 0f, 0f});
        Polygon p = new Polygon(new Vertex[]{v1, v2}); // Only 2 vertices
        // computeNormal returns early if vertices.length < 3. Normal remains {0,0,0} (default)
        assertFloatArrayEquals("Normal for <3 vertices should be default (0,0,0)", new float[]{0f, 0f, 0f}, p.getNormal(), DELTA);
        // Center calculation will still proceed: (0.5, 0, 0)
        assertFloatArrayEquals("Center for 2 vertices", new float[]{0.5f, 0f, 0f}, p.getCenter(), DELTA);

        Polygon p1 = new Polygon(new Vertex[]{v1}); // Only 1 vertex
        assertFloatArrayEquals("Normal for 1 vertex should be default (0,0,0)", new float[]{0f, 0f, 0f}, p1.getNormal(), DELTA);
        assertFloatArrayEquals("Center for 1 vertex", new float[]{0f, 0f, 0f}, p1.getCenter(), DELTA);
    }

    // --- Test Constructor Polygon(float[][] vertices, Color color) ---
    @Test
    public void testConstructorWithFloatArrayAndColor() {
        float[][] points = {
            {0f, 0f, 0f},
            {1f, 0f, 0f},
            {0f, 1f, 0f}
        };
        Color c = Color.RED;
        Polygon p = new Polygon(points, c);

        assertEquals("Number of vertices", 3, p.getVertices().length);
        assertFloatArrayEquals("V0 point", points[0], p.getVertices()[0].point, DELTA);
        assertEquals("V0 color", c.getRGB(), p.getVertices()[0].getRGBColor());
        
        // Normal: (0,0,1) as per previous test
        assertFloatArrayEquals("Normal should be computed (0,0,1)", new float[]{0f, 0f, 1f}, p.getNormal(), DELTA);
        // Center: (1/3, 1/3, 0)
        assertFloatArrayEquals("Center should be computed", new float[]{1f/3f, 1f/3f, 0f}, p.getCenter(), DELTA);

        // Check if setPNormalToVertices was effective
        for (Vertex v : p.getVertices()) {
            assertNotNull("Vertex normal should not be null after setPNormalToVertices", v.getNormal());
            assertFloatArrayEquals("Vertex normal should match polygon normal", p.getNormal(), v.getNormal(), DELTA);
        }
    }

    // --- Test Method setPNormalToVertices() ---
    @Test
    public void testSetPNormalToVertices() {
        Vertex v1 = new Vertex(new float[]{0f, 0f, 0f});
        Vertex v2 = new Vertex(new float[]{1f, 0f, 0f});
        Vertex v3 = new Vertex(new float[]{0f, 1f, 0f});
        Polygon p = new Polygon(new Vertex[]{v1, v2, v3}); // Normal is (0,0,1)

        // Clear vertex normals first
        for(Vertex v : p.getVertices()) v.setNormal(null); 
        
        p.setPNormalToVertices();
        for (Vertex v : p.getVertices()) {
            assertNotNull("Vertex normal should not be null", v.getNormal());
            assertFloatArrayEquals("Vertex normal should be polygon normal", p.getNormal(), v.getNormal(), DELTA);
        }
    }

    // --- Test Method computeNormal() ---
    @Test
    public void testComputeNormal_DirectCall() {
        Vertex v1 = new Vertex(new float[]{0f, 0f, 0f});
        Vertex v2 = new Vertex(new float[]{2f, 0f, 0f}); // Use different values to distinguish
        Vertex v3 = new Vertex(new float[]{0f, 3f, 0f});
        Polygon p = new Polygon(new Vertex[]{v1, v2, v3});

        // Manually change one vertex to alter the normal, then recompute
        p.getVertices()[2].point = new float[]{1f,1f,1f}; // New v3: (1,1,1)
        // Original normal was (0,0,6) normalized to (0,0,1)
        // New normal with v3=(1,1,1):
        // v1=(0,0,0), v2=(2,0,0)
        // edge1 (v2-v1) = (2,0,0)
        // edge2 (v3-v2) = (1-2, 1-0, 1-0) = (-1,1,1)
        // cross (edge1, edge2) = (0*1 - 0*1, 0*(-1) - 2*1, 2*1 - 0*(-1)) = (0, -2, 2)
        // Normalized: (0, -1/sqrt(2), 1/sqrt(2)) which is (0, -0.7071, 0.7071)
        
        p.computeNormal(); // Recalculate
        float[] expectedNormal = {0f, (float)(-1.0/Math.sqrt(2)), (float)(1.0/Math.sqrt(2))};
        assertFloatArrayEquals("Recomputed normal", expectedNormal, p.getNormal(), DELTA);
    }

    // --- Test Getters ---
    @Test
    public void testGetters() {
        Vertex v1 = new Vertex(new float[]{0f,0f,0f});
        Vertex v2 = new Vertex(new float[]{1f,0f,0f});
        Vertex v3 = new Vertex(new float[]{0f,1f,0f});
        Vertex[] verts = {v1,v2,v3};
        Polygon p = new Polygon(verts);

        assertArrayEquals("getVertices()", verts, p.getVertices());
        assertFloatArrayEquals("getNormal()", new float[]{0f,0f,1f}, p.getNormal(), DELTA);
        assertFloatArrayEquals("getCenter()", new float[]{1f/3f,1f/3f,0f}, p.getCenter(), DELTA);
    }

    // --- Test Field texture ---
    @Test
    public void testTextureField() {
        Vertex v1 = new Vertex(new float[]{0f,0f,0f});
        Polygon p = new Polygon(new Vertex[]{v1,v1,v1}); // Dummy vertices for constructor

        assertNull("Texture should be null initially", p.texture);
        
        MockTexture tex = new MockTexture("test_id");
        p.texture = tex;
        
        assertSame("Texture should be the MockTexture instance", tex, p.texture);
        assertEquals("MockTexture ID check", "test_id", ((MockTexture)p.texture).id);
    }
}
