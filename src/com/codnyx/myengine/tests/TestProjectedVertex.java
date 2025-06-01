package com.codnyx.myengine.tests;

import static org.junit.Assert.*;
import org.junit.Test;

import com.codnyx.myengine.ProjectedVertex;
import com.codnyx.myengine.Vertex;

import java.awt.Color;
import java.util.Arrays;
import java.lang.reflect.Field; // Correct placement of import

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
        assertNull("Default normal should be null", pv.getNormal()); 
        assertEquals("Default color", Vertex.COLOR_WHITE, pv.getRGBColor()); 
        assertIntArrayEquals("Default projection", new int[]{0,0}, pv.projection); // projection is public in ProjectedVertex
        assertEquals("Default depth", 0f, pv.depth, DELTA);
    }

    @Test
    public void testConstructorWithPoint() {
        float[] p = {1f, 2f, 3f};
        ProjectedVertex pv = new ProjectedVertex(p);
        assertFloatArrayEquals("Point from constructor", p, pv.point, DELTA);
        assertSame("Point array should be the same instance (Vertex constructor assigns directly)", p, pv.point); 
        assertIntArrayEquals("Default projection", new int[]{0,0}, pv.projection);
        assertEquals("Default depth", 0f, pv.depth, DELTA);
    }

    @Test
    public void testConstructorWithPointAndNormal() {
        float[] p = {1f, 2f, 3f};
        float[] n = {0f, 1f, 0f};
        ProjectedVertex pv = new ProjectedVertex(p, n);
        assertFloatArrayEquals("Point from constructor", p, pv.point, DELTA);
        assertFloatArrayEquals("Normal from constructor", n, pv.getNormal(), DELTA); 
        assertSame("Normal array should be the same instance (Vertex constructor assigns directly)", n, pv.getNormal()); 
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
        assertFloatArrayEquals("Normal from constructor", n, pv.getNormal(), DELTA);
        assertEquals("Color from constructor", c.getRGB(), pv.getRGBColor());
        assertIntArrayEquals("Default projection", new int[]{0,0}, pv.projection);
        assertEquals("Default depth", 0f, pv.depth, DELTA);
    }

    @Test
    public void testConstructorWithPointAndColor() {
        float[] p = {1f, 2f, 3f};
        Color c = Color.BLUE;
        ProjectedVertex pv = new ProjectedVertex(p, c);
        assertFloatArrayEquals("Point from constructor", p, pv.point, DELTA);
        assertNull("Normal should be null", pv.getNormal());
        assertEquals("Color from constructor", c.getRGB(), pv.getRGBColor());
        assertIntArrayEquals("Default projection", new int[]{0,0}, pv.projection);
        assertEquals("Default depth", 0f, pv.depth, DELTA);
    }

    @Test
    public void testConstructorWithVertex() {
        Vertex baseV = new Vertex(new float[]{5f,6f,7f}, new float[]{0.1f,0.2f,0.3f}, Color.GREEN);
        ProjectedVertex pv = new ProjectedVertex(baseV);
        
        assertFloatArrayEquals("Point from base Vertex", baseV.point, pv.point, DELTA);
        assertNotSame("Point array should be cloned from base Vertex", baseV.point, pv.point); // setTo clones
        assertFloatArrayEquals("Normal from base Vertex", baseV.getNormal(), pv.getNormal(), DELTA);
        if (baseV.getNormal() != null) { // only assertNotSame if not null, as nulls will be same
             assertNotSame("Normal array should be cloned from base Vertex", baseV.getNormal(), pv.getNormal());
        }
        assertEquals("Color from base Vertex", baseV.getRGBColor(), pv.getRGBColor());
        
        assertIntArrayEquals("Default projection for Vertex constructor", new int[]{0,0}, pv.projection);
        assertEquals("Default depth for Vertex constructor", 0f, pv.depth, DELTA);
    }

    @Test
    public void testCopyConstructor() {
        ProjectedVertex original = new ProjectedVertex();
        original.point = new float[]{10f,11f,12f};
        original.setNormal(new float[]{0.5f,0.5f,0.0f});
        original.setColor(Color.CYAN);
        original.projection = new int[]{100, 200};
        original.depth = 50.5f;

        ProjectedVertex copy = new ProjectedVertex(original);

        assertFloatArrayEquals("Copied point", original.point, copy.point, DELTA);
        assertNotSame("Copied point array should be new instance", original.point, copy.point);
        assertFloatArrayEquals("Copied normal", original.getNormal(), copy.getNormal(), DELTA);
        if (original.getNormal() != null) {
            assertNotSame("Copied normal array should be new instance", original.getNormal(), copy.getNormal());
        }
        assertEquals("Copied color", original.getRGBColor(), copy.getRGBColor());
        assertIntArrayEquals("Copied projection", original.projection, copy.projection);
        assertNotSame("Copied projection array should be new instance", original.projection, copy.projection);
        assertEquals("Copied depth", original.depth, copy.depth, DELTA);
    }

    // --- Test Data Storage and Access ---
    @Test
    public void testDataStorageAndAccess() {
        ProjectedVertex pv = new ProjectedVertex();
        
        pv.projection[0] = 123; // projection is public in ProjectedVertex
        pv.projection[1] = 456;
        pv.depth = 78.9f;    // depth is public in ProjectedVertex

        assertEquals("Projection X stored", 123, pv.projection[0]);
        assertEquals("Projection Y stored", 456, pv.projection[1]);
        assertEquals("Depth stored", 78.9f, pv.depth, DELTA);

        // Inherited fields
        pv.point[0] = 1.1f; // point is public in Vertex
        assertEquals("Point X stored", 1.1f, pv.point[0], DELTA);
        pv.setColor(Color.MAGENTA); 
        assertEquals("Color stored", Color.MAGENTA.getRGB(), pv.getRGBColor());
    }

    // --- Test setTo(Vertex v) ---
    @Test
    public void testSetTo_Vertex() {
        ProjectedVertex pv = new ProjectedVertex();
        pv.projection = new int[]{100,200}; 
        pv.depth = 50f;

        Vertex baseV = new Vertex(new float[]{1f,2f,3f}, new float[]{0f,1f,0f}, Color.YELLOW);
        pv.setTo(baseV);

        assertFloatArrayEquals("Point after setTo(Vertex)", baseV.point, pv.point, DELTA);
        assertNotSame("Point array should be cloned", baseV.point, pv.point);
        assertFloatArrayEquals("Normal after setTo(Vertex)", baseV.getNormal(), pv.getNormal(), DELTA);
        if (baseV.getNormal() != null) {
            assertNotSame("Normal array should be cloned", baseV.getNormal(), pv.getNormal());
        }
        assertEquals("Color after setTo(Vertex)", baseV.getRGBColor(), pv.getRGBColor());

        // Check that projection fields were NOT reset by setTo(Vertex v)
        // This is because init() was removed from ProjectedVertex.setTo(Vertex v) to fix NPE during super() constructor
        assertIntArrayEquals("Projection after setTo(Vertex) should be UNCHANGED", new int[]{100,200}, pv.projection);
        assertEquals("Depth after setTo(Vertex) should be UNCHANGED", 50f, pv.depth, DELTA);
    }

    // --- Test setTo(ProjectedVertex pv) ---
    @Test
    public void testSetTo_ProjectedVertex() {
        ProjectedVertex source = new ProjectedVertex();
        source.point = new float[]{1f,2f,3f};
        source.setNormal(new float[]{0f,1f,0f});
        source.setColor(Color.ORANGE);
        source.projection = new int[]{320, 240};
        source.depth = 0.75f;

        ProjectedVertex target = new ProjectedVertex();
        target.setTo(source);

        assertFloatArrayEquals("Point after setTo(ProjectedVertex)", source.point, target.point, DELTA);
        assertNotSame("Point array should be cloned", source.point, target.point);
        assertFloatArrayEquals("Normal after setTo(ProjectedVertex)", source.getNormal(), target.getNormal(), DELTA);
         if (source.getNormal() != null) {
            assertNotSame("Normal array should be cloned", source.getNormal(), target.getNormal());
        }
        assertEquals("Color after setTo(ProjectedVertex)", source.getRGBColor(), target.getRGBColor());
        assertIntArrayEquals("Projection after setTo(ProjectedVertex)", source.projection, target.projection);
        assertNotSame("Projection array should be cloned", source.projection, target.projection);
        assertEquals("Depth after setTo(ProjectedVertex)", source.depth, target.depth, DELTA);
        
        // Test with null normal in source
        source.setNormal(null);
        target.setTo(source);
        assertNull("Normal should be null if source normal is null", target.getNormal());
    }

    // --- Test reset() ---
    @Test
    public void testReset() {
        ProjectedVertex pv = new ProjectedVertex();
        pv.point = new float[]{1f,2f,3f};
        pv.setNormal(new float[]{0f,1f,0f});
        pv.setColor(Color.PINK);
        pv.projection = new int[]{111, 222};
        pv.depth = 33.3f;

        pv.reset();

        assertFloatArrayEquals("Point after reset", new float[]{0f,0f,0f}, pv.point, DELTA);
        assertNull("Normal after reset should be null", pv.getNormal());
        assertEquals("Color after reset", Vertex.COLOR_WHITE, pv.getRGBColor());
        assertIntArrayEquals("Projection after reset", new int[]{0,0}, pv.projection);
        assertEquals("Depth after reset", 0f, pv.depth, DELTA);
    }

    // Reflection helpers to access package-private fields from Vertex
    private float[] getVertexNormal(Vertex v) {
        try {
            Field normalField = Vertex.class.getDeclaredField("normal");
            normalField.setAccessible(true);
            return (float[]) normalField.get(v);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private void setVertexNormal(Vertex v, float[] normalValue) {
        try {
            Field normalField = Vertex.class.getDeclaredField("normal");
            normalField.setAccessible(true);
            normalField.set(v, normalValue);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private int getVertexColor(Vertex v) {
        try {
            Field colorField = Vertex.class.getDeclaredField("color");
            colorField.setAccessible(true);
            return colorField.getInt(v);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private void setVertexColor(Vertex v, int colorValue) {
        try {
            Field colorField = Vertex.class.getDeclaredField("color");
            colorField.setAccessible(true);
            colorField.setInt(v, colorValue);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
