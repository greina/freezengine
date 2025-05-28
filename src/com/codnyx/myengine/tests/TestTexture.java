package com.codnyx.myengine.tests;

import static org.junit.Assert.*;
import org.junit.Test;

import com.codnyx.myengine.Texture; // The abstract class to "test"

/**
 * Test class for the abstract Texture class.
 * Since Texture is abstract, a minimal concrete subclass (MockConcreteTexture)
 * is created within this test file to enable instantiation and testing of
 * field accessibility and the contract of the abstract getColor method.
 *
 * This test does NOT cover:
 * - Texture loading from files (as Texture has no such constructor).
 * - Complex UV mapping, filtering, or wrapping (as these depend on concrete implementations
 *   and Texture itself doesn't define u_mode, v_mode, width, height, or pixel data fields).
 * Those features would be tested in concrete subclasses of Texture (e.g., TestVertexTexture).
 */
class MockConcreteTexture extends Texture {
    public int[][] pixelData; // Example: a simple 2x2 texture
    public int width;
    public int height;

    /**
     * Constructor for the mock concrete texture.
     * @param width Width of the texture.
     * @param height Height of the texture.
     * @param data A 1D array of ARGB pixel data (row-major). Size must be width*height.
     */
    public MockConcreteTexture(int width, int height, int[] data) {
        if (data == null || data.length != width * height) {
            throw new IllegalArgumentException("Data array size must match width*height.");
        }
        this.width = width;
        this.height = height;
        this.pixelData = new int[height][width];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                this.pixelData[y][x] = data[y * width + x];
            }
        }
    }

    /**
     * Simple getColor implementation for the mock.
     * Returns pixelData[y][x] if in bounds, otherwise returns a default color (e.g., black).
     * This mock does not implement complex wrapping or filtering.
     */
    @Override
    public int getColor(int x, int y) {
        if (x >= 0 && x < width && y >= 0 && y < height) {
            return pixelData[y][x];
        }
        return 0; // Default color (black) for out-of-bounds
    }
}

public class TestTexture {

    @Test
    public void testAbstractTextureFieldAccess() {
        // Instantiate the mock concrete subclass
        MockConcreteTexture concreteTexture = new MockConcreteTexture(1, 1, new int[]{0xFFFFFFFF});

        // Test that public fields from the abstract Texture class can be set and get
        concreteTexture.u = new int[]{1, 2};
        concreteTexture.v = new int[]{3, 4};
        concreteTexture.o = new int[]{5, 6};
        assertArrayEquals("Field 'u' should be accessible and retrievable", new int[]{1, 2}, concreteTexture.u);
        assertArrayEquals("Field 'v' should be accessible and retrievable", new int[]{3, 4}, concreteTexture.v);
        assertArrayEquals("Field 'o' should be accessible and retrievable", new int[]{5, 6}, concreteTexture.o);

        concreteTexture.U = new float[]{1.1f, 2.2f, 3.3f};
        concreteTexture.V = new float[]{4.4f, 5.5f, 6.6f};
        concreteTexture.O = new float[]{7.7f, 8.8f, 9.9f};
        assertArrayEquals("Field 'U' should be accessible and retrievable", new float[]{1.1f, 2.2f, 3.3f}, concreteTexture.U, 1e-6f);
        assertArrayEquals("Field 'V' should be accessible and retrievable", new float[]{4.4f, 5.5f, 6.6f}, concreteTexture.V, 1e-6f);
        assertArrayEquals("Field 'O' should be accessible and retrievable", new float[]{7.7f, 8.8f, 9.9f}, concreteTexture.O, 1e-6f);
    }

    @Test
    public void testAbstractTextureGetColorMethodContract() {
        // Create a 2x2 mock texture
        // Row 0: RED, GREEN
        // Row 1: BLUE, WHITE
        int RED = 0xFFFF0000;
        int GREEN = 0xFF00FF00;
        int BLUE = 0xFF0000FF;
        int WHITE = 0xFFFFFFFF;
        int BLACK = 0xFF000000; // Mock's out-of-bounds color

        int[] data = {RED, GREEN, BLUE, WHITE};
        MockConcreteTexture concreteTexture = new MockConcreteTexture(2, 2, data);

        // Test the getColor method of the concrete subclass
        assertEquals("getColor(0,0) should return RED", RED, concreteTexture.getColor(0, 0));
        assertEquals("getColor(1,0) should return GREEN", GREEN, concreteTexture.getColor(1, 0));
        assertEquals("getColor(0,1) should return BLUE", BLUE, concreteTexture.getColor(0, 1));
        assertEquals("getColor(1,1) should return WHITE", WHITE, concreteTexture.getColor(1, 1));

        // Test out-of-bounds access (as defined by MockConcreteTexture)
        assertEquals("getColor(-1,0) out of bounds", BLACK, concreteTexture.getColor(-1, 0));
        assertEquals("getColor(0,-1) out of bounds", BLACK, concreteTexture.getColor(0, -1));
        assertEquals("getColor(2,0) out of bounds (width is 2)", BLACK, concreteTexture.getColor(2, 0));
        assertEquals("getColor(0,2) out of bounds (height is 2)", BLACK, concreteTexture.getColor(0, 2));
    }

    @Test
    public void testTextureCanBeExtended() {
        // This test simply demonstrates that the abstract class can be extended
        // and an instance of a subclass can be treated as Texture.
        Texture texInstance = new MockConcreteTexture(1,1, new int[]{0});
        assertNotNull(texInstance);
        assertTrue(texInstance instanceof Texture);
        assertTrue(texInstance instanceof MockConcreteTexture);
    }
}
