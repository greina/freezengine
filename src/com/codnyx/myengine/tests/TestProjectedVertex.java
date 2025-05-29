package com.codnyx.myengine.tests;

import static org.junit.Assert.*;
import org.junit.Test;

import com.codnyx.myengine.ProjectedVertex;
import com.codnyx.myengine.Vertex;

import java.awt.Color;
import java.util.Arrays;

public class TestProjectedVertex {

    private static final float DELTA = 1e-6f;

    private void assertFloatArrayEquals(String message, float[] expected, float[] actual, float delta) {
        if (expected == null && actual == null) return;
        assertNotNull(message + " expected array was null, actual was not (or vice versa)", expected);
        assertNotNull(message + " actual array was null, expected was not (or vice versa)", actual);
        assertEquals(message + " (array length)", expected.length, actual.length);
        for (int i = 0; i < expected.length; i++) {
            assertEquals(message + " (element " + i + ")", expected[i], actual[i], delta);
        }
    }
    
    private void assertIntArrayEquals(String message, int[] expected, int[] actual) {
        if (expected == null && actual == null) return;
        assertNotNull(message + " expected array was null, actual was not (or vice versa)", expected);
        assertNotNull(message + " actual array was null, expected was not (or vice versa)", actual);
        assertArrayEquals(message, expected, actual);
    }


    // --- Test Constructors ---

    @Test
    public void testDefaultConstructor() {
        ProjectedVertex pv = new ProjectedVertex();
        assertFloatArrayEquals("Default point", new float[]{0f,0f,0f}, pv.point, DELTA);
        assertNull("Default normal should be null", pv.normal); // Accessing package-private field for test
        assertEquals("Default color", Vertex.COLOR_WHITE, pv.color); // Accessing package-private field for test
        assertIntArrayEquals("Default projection", new int[]{0,0}, pv.projection);
        assertEquals("Default depth", 0f, pv.depth, DELTA);
    }

    @Test
    public void testConstructorWithPoint() {
        float[] p = {1f, 2f, 3f};
        ProjectedVertex pv = new ProjectedVertex(p);
        assertFloatArrayEquals("Point from constructor", p, pv.point, DELTA);
        assertNotSame("Point array should be a new instance or same if constructor assigns directly", p, pv.point); // Vertex constructor assigns directly
        assertIntArrayEquals("Default projection", new int[]{0,0}, pv.projection);
        assertEquals("Default depth", 0f, pv.depth, DELTA);
    }

    @Test
    public void testConstructorWithPointAndNormal() {
        float[] p = {1f, 2f, 3f};
        float[] n = {0f, 1f, 0f};
        ProjectedVertex pv = new ProjectedVertex(p, n);
        assertFloatArrayEquals("Point from constructor", p, pv.point, DELTA);
        assertFloatArrayEquals("Normal from constructor", n, pv.normal, DELTA); // Accessing package-private field
        assertNotSame("Normal array should be a new instance or same if constructor assigns directly", n, pv.normal); // Vertex constructor assigns directly
        assertIntArrayEquals("Default projection", new int[]{0,0}, pv.projection);
        assertEquals("Default depth", 0f, pv.depth, DELTA);
    }

    @Test
    public void testConstructorWithPointNormalAndColor() {
        float[] p = {1f, 2f, 3f};
        float[] n = {0f, 1f, 0f};
        Color c = Color.RED;
        ProjectedVertex pv = new ProjectedVertex(p, n, c);
        assertFloatArrayEquals("Point from constructor", p, pv.point, DELTA);
        assertFloatArrayEquals("Normal from constructor", n, pv.normal, DELTA);
        assertEquals("Color from constructor", c.getRGB(), pv.color);
        assertIntArrayEquals("Default projection", new int[]{0,0}, pv.projection);
        assertEquals("Default depth", 0f, pv.depth, DELTA);
    }

    @Test
    public void testConstructorWithPointAndColor() {
        float[] p = {1f, 2f, 3f};
        Color c = Color.BLUE;
        ProjectedVertex pv = new ProjectedVertex(p, c);
        assertFloatArrayEquals("Point from constructor", p, pv.point, DELTA);
        assertNull("Normal should be null", pv.normal);
        assertEquals("Color from constructor", c.getRGB(), pv.color);
        assertIntArrayEquals("Default projection", new int[]{0,0}, pv.projection);
        assertEquals("Default depth", 0f, pv.depth, DELTA);
    }

    @Test
    public void testConstructorWithVertex() {
        Vertex baseV = new Vertex(new float[]{5f,6f,7f}, new float[]{0.1f,0.2f,0.3f}, Color.GREEN);
        ProjectedVertex pv = new ProjectedVertex(baseV);
        
        assertFloatArrayEquals("Point from base Vertex", baseV.point, pv.point, DELTA);
        assertNotSame("Point array should be cloned from base Vertex", baseV.point, pv.point);
        assertFloatArrayEquals("Normal from base Vertex", baseV.normal, pv.normal, DELTA);
        assertNotSame("Normal array should be cloned from base Vertex", baseV.normal, pv.normal);
        assertEquals("Color from base Vertex", baseV.color, pv.color);
        
        assertIntArrayEquals("Default projection for Vertex constructor", new int[]{0,0}, pv.projection);
        assertEquals("Default depth for Vertex constructor", 0f, pv.depth, DELTA);
    }

    @Test
    public void testCopyConstructor() {
        ProjectedVertex original = new ProjectedVertex();
        original.point = new float[]{10f,11f,12f};
        original.normal = new float[]{0.5f,0.5f,0.0f};
        original.color = Color.CYAN.getRGB();
        original.projection = new int[]{100, 200};
        original.depth = 50.5f;

        ProjectedVertex copy = new ProjectedVertex(original);

        assertFloatArrayEquals("Copied point", original.point, copy.point, DELTA);
        assertNotSame("Copied point array should be new instance", original.point, copy.point);
        assertFloatArrayEquals("Copied normal", original.normal, copy.normal, DELTA);
        assertNotSame("Copied normal array should be new instance", original.normal, copy.normal);
        assertEquals("Copied color", original.color, copy.color);
        assertIntArrayEquals("Copied projection", original.projection, copy.projection);
        assertNotSame("Copied projection array should be new instance", original.projection, copy.projection);
        assertEquals("Copied depth", original.depth, copy.depth, DELTA);
    }

    // --- Test Data Storage and Access ---
    @Test
    public void testDataStorageAndAccess() {
        ProjectedVertex pv = new ProjectedVertex();
        
        pv.projection[0] = 123;
        pv.projection[1] = 456;
        pv.depth = 78.9f;

        assertEquals("Projection X stored", 123, pv.projection[0]);
        assertEquals("Projection Y stored", 456, pv.projection[1]);
        assertEquals("Depth stored", 78.9f, pv.depth, DELTA);

        // Inherited fields
        pv.point[0] = 1.1f;
        assertEquals("Point X stored", 1.1f, pv.point[0], DELTA);
        pv.color = Color.MAGENTA.getRGB();
        assertEquals("Color stored", Color.MAGENTA.getRGB(), pv.color);
    }

    // --- Test setTo(Vertex v) ---
    @Test
    public void testSetTo_Vertex() {
        ProjectedVertex pv = new ProjectedVertex();
        pv.projection = new int[]{100,200}; // Set some non-default projection data
        pv.depth = 50f;

        Vertex baseV = new Vertex(new float[]{1f,2f,3f}, new float[]{0f,1f,0f}, Color.YELLOW);
        pv.setTo(baseV);

        assertFloatArrayEquals("Point after setTo(Vertex)", baseV.point, pv.point, DELTA);
        assertNotSame("Point array should be cloned", baseV.point, pv.point);
        assertFloatArrayEquals("Normal after setTo(Vertex)", baseV.normal, pv.normal, DELTA);
        assertNotSame("Normal array should be cloned", baseV.normal, pv.normal);
        assertEquals("Color after setTo(Vertex)", baseV.color, pv.color);

        // Check that projection fields were reset by init()
        assertIntArrayEquals("Projection after setTo(Vertex) should be default", new int[]{0,0}, pv.projection);
        assertEquals("Depth after setTo(Vertex) should be default", 0f, pv.depth, DELTA);
    }

    // --- Test setTo(ProjectedVertex pv) ---
    @Test
    public void testSetTo_ProjectedVertex() {
        ProjectedVertex source = new ProjectedVertex();
        source.point = new float[]{1f,2f,3f};
        source.normal = new float[]{0f,1f,0f};
        source.color = Color.ORANGE.getRGB();
        source.projection = new int[]{320, 240};
        source.depth = 0.75f;

        ProjectedVertex target = new ProjectedVertex();
        target.setTo(source);

        assertFloatArrayEquals("Point after setTo(ProjectedVertex)", source.point, target.point, DELTA);
        assertNotSame("Point array should be cloned", source.point, target.point);
        assertFloatArrayEquals("Normal after setTo(ProjectedVertex)", source.normal, target.normal, DELTA);
        assertNotSame("Normal array should be cloned", source.normal, target.normal);
        assertEquals("Color after setTo(ProjectedVertex)", source.color, target.color);
        assertIntArrayEquals("Projection after setTo(ProjectedVertex)", source.projection, target.projection);
        assertNotSame("Projection array should be cloned", source.projection, target.projection);
        assertEquals("Depth after setTo(ProjectedVertex)", source.depth, target.depth, DELTA);
        
        // Test with null normal in source
        source.normal = null;
        target.setTo(source);
        assertNull("Normal should be null if source normal is null", target.normal);
    }

    // --- Test reset() ---
    @Test
    public void testReset() {
        ProjectedVertex pv = new ProjectedVertex();
        pv.point = new float[]{1f,2f,3f};
        pv.normal = new float[]{0f,1f,0f};
        pv.color = Color.PINK.getRGB();
        pv.projection = new int[]{111, 222};
        pv.depth = 33.3f;

        pv.reset();

        assertFloatArrayEquals("Point after reset", new float[]{0f,0f,0f}, pv.point, DELTA);
        assertNull("Normal after reset should be null", pv.normal);
        assertEquals("Color after reset", Vertex.COLOR_WHITE, pv.color);
        assertIntArrayEquals("Projection after reset", new int[]{0,0}, pv.projection);
        assertEquals("Depth after reset", 0f, pv.depth, DELTA);
    }
}
