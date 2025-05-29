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
        // Accessing package-private fields for verification (valid as tests are in same conceptual module/purpose)
        assertNull("Default constructor normal should be null", v.normal);
        assertEquals("Default constructor color", Vertex.COLOR_WHITE, v.color);
    }

    @Test
    public void testConstructorWithPoint() {
        float[] p = {1.0f, 2.0f, 3.0f};
        Vertex v = new Vertex(p);
        // The constructor Vertex(float[] point) assigns the array directly, not a clone.
        assertSame("Constructor with point should assign the array reference directly", p, v.point);
        assertFloatArrayEquals("Constructor with point value check", p, v.point, DELTA);
        assertNull("Normal should be null by default", v.normal);
        assertEquals("Color should be default white", Vertex.COLOR_WHITE, v.color);
    }

    @Test
    public void testConstructorWithPointAndNormal() {
        float[] p = {1.5f, 2.5f, 3.5f};
        float[] n = {0.0f, 1.0f, 0.0f};
        Vertex v = new Vertex(p, n);
        assertSame("Constructor with point (and normal) should assign point array reference", p, v.point);
        assertFloatArrayEquals("Constructor with point (and normal) value check", p, v.point, DELTA);
        assertSame("Constructor with normal should assign the array reference directly", n, v.normal);
        assertFloatArrayEquals("Constructor with normal value check", n, v.normal, DELTA);
        assertEquals("Color should be default white", Vertex.COLOR_WHITE, v.color);
    }

    @Test
    public void testConstructorWithPointNormalAndColor() {
        float[] p = {1f, 0f, 0f};
        float[] n = {1f, 0f, 0f};
        Color c = Color.RED;
        Vertex v = new Vertex(p, n, c);
        assertSame("Point array (full constructor)", p, v.point);
        assertSame("Normal array (full constructor)", n, v.normal);
        assertEquals("Color (full constructor)", c.getRGB(), v.color);
    }

    @Test
    public void testConstructorWithPointAndColor() {
        float[] p = {0f, 1f, 0f};
        Color c = Color.GREEN;
        Vertex v = new Vertex(p, c);
        assertSame("Point array (point, color constructor)", p, v.point);
        assertNull("Normal (point, color constructor) should be null", v.normal);
        assertEquals("Color (point, color constructor)", c.getRGB(), v.color);
    }

    @Test
    public void testCopyConstructor() {
        Vertex original = new Vertex();
        original.point = new float[]{10f, 20f, 30f};
        original.normal = new float[]{0.1f, 0.2f, 0.3f};
        original.color = Color.BLUE.getRGB();

        Vertex copy = new Vertex(original);

        // setTo method (called by copy constructor) creates clones of arrays
        assertFloatArrayEquals("Copy constructor point", original.point, copy.point, DELTA);
        assertNotSame("Copy constructor point should be a clone", original.point, copy.point);
        
        assertFloatArrayEquals("Copy constructor normal", original.normal, copy.normal, DELTA);
        assertNotSame("Copy constructor normal should be a clone", original.normal, copy.normal);
        
        assertEquals("Copy constructor color", original.color, copy.color);

        // Test copy constructor with null normal
        original.normal = null;
        Vertex copyFromNullNormal = new Vertex(original);
        assertNull("Copy constructor with null normal should result in null normal", copyFromNullNormal.normal);
    }

    // --- Test Data Storage and Access ---
    // Fields are public (point) or package-private (normal, color), directly accessible for tests.
    @Test
    public void testFieldAccessAndModification() {
        Vertex v = new Vertex();

        // Test point (public)
        v.point[0] = 5f;
        v.point[1] = 6f;
        v.point[2] = 7f;
        assertFloatArrayEquals("Modified point array", new float[]{5f,6f,7f}, v.point, DELTA);

        // Test normal (package-private)
        v.normal = new float[]{0.5f, 0.5f, 0.0f};
        assertFloatArrayEquals("Assigned normal array", new float[]{0.5f,0.5f,0.0f}, v.normal, DELTA);

        // Test color (package-private)
        v.color = Color.CYAN.getRGB();
        assertEquals("Assigned color", Color.CYAN.getRGB(), v.color);
    }
    
    // --- Test setTo(Vertex v) ---
    @Test
    public void testSetTo() {
        Vertex source = new Vertex();
        source.point = new float[]{1.1f, 2.2f, 3.3f};
        source.normal = new float[]{0.4f, 0.5f, 0.6f};
        source.color = Color.MAGENTA.getRGB();

        Vertex target = new Vertex();
        target.setTo(source);

        assertFloatArrayEquals("setTo point", source.point, target.point, DELTA);
        assertNotSame("setTo point should be a clone", source.point, target.point);
        
        assertFloatArrayEquals("setTo normal", source.normal, target.normal, DELTA);
        assertNotSame("setTo normal should be a clone", source.normal, target.normal);
        
        assertEquals("setTo color", source.color, target.color);

        // Test setTo with source having null normal
        source.normal = null;
        target.setTo(source);
        assertNull("setTo with null normal should result in null normal", target.normal);
    }

    // --- Test reset() ---
    @Test
    public void testReset() {
        Vertex v = new Vertex(
            new float[]{10f, 10f, 10f}, 
            new float[]{1f, 0f, 0f}, 
            Color.RED
        );
        
        v.reset();

        assertFloatArrayEquals("Point after reset", new float[]{0f, 0f, 0f}, v.point, DELTA);
        assertNull("Normal after reset should be null", v.normal);
        assertEquals("Color after reset should be COLOR_WHITE", Vertex.COLOR_WHITE, v.color);
    }
}
