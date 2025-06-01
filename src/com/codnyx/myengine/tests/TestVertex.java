package com.codnyx.myengine.tests;

import static org.junit.Assert.*;
import org.junit.Test;

import com.codnyx.myengine.Vertex; // The class to test
import java.awt.Color; // For constructors that take java.awt.Color
import java.util.Arrays; // For Arrays.equals

public class TestVertex {

    private static final float DELTA = 1e-6f;

    // Helper to assert float arrays, handling nulls
    private void assertFloatArrayEquals(String message, float[] expected, float[] actual, float delta) {
        if (expected == null && actual == null) {
            return; // Both null is considered equal in this context
        }
        assertNotNull(message + " - expected array was null while actual was not (or vice versa)", expected);
        assertNotNull(message + " - actual array was null while expected was not (or vice versa)", actual);
        assertEquals(message + " (array length)", expected.length, actual.length);
        for (int i = 0; i < expected.length; i++) {
            assertEquals(message + " (element " + i + ")", expected[i], actual[i], delta);
        }
    }

    // --- Test Constructors ---

    @Test
    public void testDefaultConstructor() {
        Vertex v = new Vertex();
        assertFloatArrayEquals("Default constructor point", new float[]{0f, 0f, 0f}, v.point, DELTA);
        assertNull("Default constructor normal should be null", v.getNormal());
        assertEquals("Default constructor color", Vertex.COLOR_WHITE, v.getRGBColor());
    }

    @Test
    public void testConstructorWithPoint() {
        float[] p = {1.0f, 2.0f, 3.0f};
        Vertex v = new Vertex(p);
        assertSame("Constructor with point should assign the array reference directly", p, v.point);
        assertFloatArrayEquals("Constructor with point value check", p, v.point, DELTA);
        assertNull("Normal should be null by default", v.getNormal());
        assertEquals("Color should be default white", Vertex.COLOR_WHITE, v.getRGBColor());
    }

    @Test
    public void testConstructorWithPointAndNormal() {
        float[] p = {1.5f, 2.5f, 3.5f};
        float[] n = {0.0f, 1.0f, 0.0f};
        Vertex v = new Vertex(p, n);
        assertSame("Constructor with point (and normal) should assign point array reference", p, v.point);
        assertFloatArrayEquals("Constructor with point (and normal) value check", p, v.point, DELTA);
        assertSame("Constructor with normal should assign the array reference directly", n, v.getNormal());
        assertFloatArrayEquals("Constructor with normal value check", n, v.getNormal(), DELTA);
        assertEquals("Color should be default white", Vertex.COLOR_WHITE, v.getRGBColor());
    }

    @Test
    public void testConstructorWithPointNormalAndColor() {
        float[] p = {1f, 0f, 0f};
        float[] n = {1f, 0f, 0f};
        Color c = Color.RED;
        Vertex v = new Vertex(p, n, c);
        assertSame("Point array (full constructor)", p, v.point);
        assertSame("Normal array (full constructor)", n, v.getNormal());
        assertEquals("Color (full constructor)", c.getRGB(), v.getRGBColor());
    }

    @Test
    public void testConstructorWithPointAndColor() {
        float[] p = {0f, 1f, 0f};
        Color c = Color.GREEN;
        Vertex v = new Vertex(p, c);
        assertSame("Point array (point, color constructor)", p, v.point);
        assertNull("Normal (point, color constructor) should be null", v.getNormal());
        assertEquals("Color (point, color constructor)", c.getRGB(), v.getRGBColor());
    }

    @Test
    public void testCopyConstructor() {
        Vertex original = new Vertex();
        original.point = new float[]{10f, 20f, 30f};
        original.setNormal(new float[]{0.1f, 0.2f, 0.3f});
        original.setRGBColor(Color.BLUE.getRGB());

        Vertex copy = new Vertex(original);

        assertFloatArrayEquals("Copy constructor point", original.point, copy.point, DELTA);
        assertNotSame("Copy constructor point should be a clone", original.point, copy.point);
        
        assertFloatArrayEquals("Copy constructor normal", original.getNormal(), copy.getNormal(), DELTA);
        if (original.getNormal() != null) {
            assertNotSame("Copy constructor normal should be a clone", original.getNormal(), copy.getNormal());
        }
        
        assertEquals("Copy constructor color", original.getRGBColor(), copy.getRGBColor());

        original.setNormal(null);
        Vertex copyFromNullNormal = new Vertex(original);
        assertNull("Copy constructor with null normal should result in null normal", copyFromNullNormal.getNormal());
    }

    @Test
    public void testFieldAccessAndModification() {
        Vertex v = new Vertex();

        v.point[0] = 5f;
        v.point[1] = 6f;
        v.point[2] = 7f;
        assertFloatArrayEquals("Modified point array", new float[]{5f,6f,7f}, v.point, DELTA);

        v.setNormal(new float[]{0.5f, 0.5f, 0.0f});
        assertFloatArrayEquals("Assigned normal array", new float[]{0.5f,0.5f,0.0f}, v.getNormal(), DELTA);

        v.setColor(Color.CYAN); // Using new public setter
        assertEquals("Assigned color", Color.CYAN.getRGB(), v.getRGBColor());
    }
    
    @Test
    public void testSetTo() {
        Vertex source = new Vertex();
        source.point = new float[]{1.1f, 2.2f, 3.3f};
        source.setNormal(new float[]{0.4f, 0.5f, 0.6f});
        source.setRGBColor(Color.MAGENTA.getRGB());

        Vertex target = new Vertex();
        target.setTo(source);

        assertFloatArrayEquals("setTo point", source.point, target.point, DELTA);
        assertNotSame("setTo point should be a clone", source.point, target.point);
        
        assertFloatArrayEquals("setTo normal", source.getNormal(), target.getNormal(), DELTA);
        if (source.getNormal() != null) {
            assertNotSame("setTo normal should be a clone", source.getNormal(), target.getNormal());
        }
        
        assertEquals("setTo color", source.getRGBColor(), target.getRGBColor());

        source.setNormal(null);
        target.setTo(source);
        assertNull("setTo with null normal should result in null normal", target.getNormal());
    }

    @Test
    public void testReset() {
        Vertex v = new Vertex(
            new float[]{10f, 10f, 10f}, 
            new float[]{1f, 0f, 0f}, 
            Color.RED
        );
        
        v.reset();

        assertFloatArrayEquals("Point after reset", new float[]{0f, 0f, 0f}, v.point, DELTA);
        assertNull("Normal after reset should be null", v.getNormal());
        assertEquals("Color after reset should be COLOR_WHITE", Vertex.COLOR_WHITE, v.getRGBColor());
    }
}
