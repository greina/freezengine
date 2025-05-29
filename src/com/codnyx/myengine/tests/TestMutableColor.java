package com.codnyx.myengine.tests;

import static org.junit.Assert.*;
import org.junit.Test;
import com.codnyx.myengine.MutableColor;
// No need to import java.awt.Color explicitly unless for static members or type comparison not via instanceOf

/**
 * JUnit test class for the MutableColor class.
 * This class tests constructors, setters, and getter methods,
 * including interaction with inherited java.awt.Color methods.
 */
public class TestMutableColor {

    // --- Helper method to create an ARGB integer ---
    private int packRGBA(int r, int g, int b, int a) {
        return ((a & 0xFF) << 24) | ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF);
    }

    // --- Test Constructor MutableColor(int r, int g, int b, int a) ---
    @Test
    public void testRGBAConstructor_ValidValues() {
        int r = 10, g = 20, b = 30, a = 40;
        MutableColor mc = new MutableColor(r, g, b, a);
        
        assertEquals("getRGB() should return correct packed ARGB value", packRGBA(r,g,b,a), mc.getRGB());
        assertEquals("getRed() should return correct red value", r, mc.getRed());
        assertEquals("getGreen() should return correct green value", g, mc.getGreen());
        assertEquals("getBlue() should return correct blue value", b, mc.getBlue());
        assertEquals("getAlpha() should return correct alpha value", a, mc.getAlpha());

        // Test with boundary values
        r = 0; g = 255; b = 0; a = 255;
        mc = new MutableColor(r, g, b, a);
        assertEquals("getRGB() for boundary values", packRGBA(r,g,b,a), mc.getRGB());
        assertEquals("getRed() for boundary values", r, mc.getRed());
        assertEquals("getGreen() for boundary values", g, mc.getGreen());
        assertEquals("getBlue() for boundary values", b, mc.getBlue());
        assertEquals("getAlpha() for boundary values", a, mc.getAlpha());
    }

    @Test
    public void testRGBAConstructor_InvalidRedLow() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new MutableColor(-1, 20, 30, 40);
        });
        assertTrue(exception.getMessage().contains("Red"));
    }

    @Test
    public void testRGBAConstructor_InvalidRedHigh() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new MutableColor(256, 20, 30, 40);
        });
        assertTrue(exception.getMessage().contains("Red"));
    }

    @Test
    public void testRGBAConstructor_InvalidGreenLow() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new MutableColor(10, -1, 30, 40);
        });
        assertTrue(exception.getMessage().contains("Green"));
    }
    
    @Test
    public void testRGBAConstructor_InvalidGreenHigh() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new MutableColor(10, 256, 30, 40);
        });
        assertTrue(exception.getMessage().contains("Green"));
    }

    @Test
    public void testRGBAConstructor_InvalidBlueLow() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new MutableColor(10, 20, -1, 40);
        });
        assertTrue(exception.getMessage().contains("Blue"));
    }

    @Test
    public void testRGBAConstructor_InvalidBlueHigh() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new MutableColor(10, 20, 256, 40);
        });
        assertTrue(exception.getMessage().contains("Blue"));
    }

    @Test
    public void testRGBAConstructor_InvalidAlphaLow() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new MutableColor(10, 20, 30, -1);
        });
        assertTrue(exception.getMessage().contains("Alpha"));
    }
    
    @Test
    public void testRGBAConstructor_InvalidAlphaHigh() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new MutableColor(10, 20, 30, 256);
        });
        assertTrue(exception.getMessage().contains("Alpha"));
    }

    // --- Test Constructor MutableColor(int rgb) ---
    @Test
    public void testRGBConstructor_ValidValue() {
        int r = 50, g = 60, b = 70;
        int packedRGB = ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF); // 0x00323C46
        
        MutableColor mc = new MutableColor(packedRGB);
        
        int expectedARGB = 0xFF000000 | packedRGB; // Alpha should be 255
        assertEquals("getRGB() should return correct ARGB (alpha=255)", expectedARGB, mc.getRGB());
        assertEquals("getRed() should return correct red value", r, mc.getRed());
        assertEquals("getGreen() should return correct green value", g, mc.getGreen());
        assertEquals("getBlue() should return correct blue value", b, mc.getBlue());
        assertEquals("getAlpha() should return 255", 255, mc.getAlpha());

        // Test with an RGB value that has bits in alpha position (should be ignored and alpha forced to 255)
        int rgbWithAlphaBits = 0x12345678; // Alpha bits 0x12 will be overridden
        r = (rgbWithAlphaBits >> 16) & 0xFF; // 0x34
        g = (rgbWithAlphaBits >> 8) & 0xFF;  // 0x56
        b = rgbWithAlphaBits & 0xFF;         // 0x78
        mc = new MutableColor(rgbWithAlphaBits);
        expectedARGB = 0xFF000000 | (rgbWithAlphaBits & 0x00FFFFFF);
        assertEquals("getRGB() for RGB with alpha bits", expectedARGB, mc.getRGB());
        assertEquals("getRed() for RGB with alpha bits", r, mc.getRed());
        assertEquals("getGreen() for RGB with alpha bits", g, mc.getGreen());
        assertEquals("getBlue() for RGB with alpha bits", b, mc.getBlue());
        assertEquals("getAlpha() for RGB with alpha bits should be 255", 255, mc.getAlpha());
    }

    // --- Test Method getRGB() (implicitly tested, but good for clarity) ---
    @Test
    public void testGetRGB_Consistency() {
        MutableColor mc = new MutableColor(10,20,30,40);
        assertEquals("getRGB initial consistency", packRGBA(10,20,30,40), mc.getRGB());
        mc.setRGBA(50,60,70,80);
        assertEquals("getRGB after setRGBA(r,g,b,a)", packRGBA(50,60,70,80), mc.getRGB());
        mc.setRGBA(0x00112233); // packed RGB
        assertEquals("getRGB after setRGBA(rgb)", packRGBA(0x11,0x22,0x33,255), mc.getRGB());
    }


    // --- Test Method setRGBA(int r, int g, int b, int a) ---
    @Test
    public void testSetRGBA_Args_ValidValues() {
        MutableColor mc = new MutableColor(0,0,0,0); // Initial color
        
        int r = 100, g = 110, b = 120, a = 130;
        mc.setRGBA(r, g, b, a);
        
        assertEquals("getRGB() after setRGBA", packRGBA(r,g,b,a), mc.getRGB());
        assertEquals("getRed() after setRGBA", r, mc.getRed());
        assertEquals("getGreen() after setRGBA", g, mc.getGreen());
        assertEquals("getBlue() after setRGBA", b, mc.getBlue());
        assertEquals("getAlpha() after setRGBA", a, mc.getAlpha());

        // Change again
        r = 5; g = 15; b = 25; a = 35;
        mc.setRGBA(r, g, b, a);
        assertEquals("getRGB() after second setRGBA", packRGBA(r,g,b,a), mc.getRGB());
        assertEquals("getRed() after second setRGBA", r, mc.getRed());
    }

    @Test
    public void testSetRGBA_Args_InvalidRedLow() {
        MutableColor mc = new MutableColor(0,0,0,0);
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            mc.setRGBA(-1, 20, 30, 40);
        });
        assertTrue(exception.getMessage().contains("Red"));
    }
    
    @Test
    public void testSetRGBA_Args_InvalidAlphaHigh() {
        MutableColor mc = new MutableColor(0,0,0,0);
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            mc.setRGBA(10, 20, 30, 256);
        });
        assertTrue(exception.getMessage().contains("Alpha"));
    }
    // Add more specific invalid component tests for setRGBA(r,g,b,a) if desired, similar to constructor tests.

    // --- Test Method setRGBA(int rgb) ---
    @Test
    public void testSetRGBA_PackedRGB_ValidValue() {
        MutableColor mc = new MutableColor(0,0,0,0); // Initial color

        int r = 70, g = 80, b = 90;
        int packedRGB = ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF); // 0x0046505A
        
        mc.setRGBA(packedRGB);
        
        int expectedARGB = 0xFF000000 | packedRGB; // Alpha should be 255
        assertEquals("getRGB() after setRGBA(rgb)", expectedARGB, mc.getRGB());
        assertEquals("getRed() after setRGBA(rgb)", r, mc.getRed());
        assertEquals("getGreen() after setRGBA(rgb)", g, mc.getGreen());
        assertEquals("getBlue() after setRGBA(rgb)", b, mc.getBlue());
        assertEquals("getAlpha() after setRGBA(rgb) should be 255", 255, mc.getAlpha());

        // Change again, with alpha bits in input (should be ignored)
        int newRGBWithAlpha = 0xAA112233; // Alpha bits 0xAA will be overridden
        r = (newRGBWithAlpha >> 16) & 0xFF; // 0x11
        g = (newRGBWithAlpha >> 8) & 0xFF;  // 0x22
        b = newRGBWithAlpha & 0xFF;         // 0x33
        mc.setRGBA(newRGBWithAlpha);
        expectedARGB = 0xFF000000 | (newRGBWithAlpha & 0x00FFFFFF);
        assertEquals("getRGB() after second setRGBA(rgb with alpha bits)", expectedARGB, mc.getRGB());
        assertEquals("getRed() after second setRGBA(rgb with alpha bits)", r, mc.getRed());
        assertEquals("getAlpha() after second setRGBA(rgb with alpha bits) should be 255", 255, mc.getAlpha());
    }
    
    @Test
    public void testSuperClassMethodsWorkCorrectly() {
        // java.awt.Color methods like brighter(), darker() create NEW Color objects.
        // We are testing that the getters (getRed, etc.) from Color work on MutableColor's state.
        MutableColor mc = new MutableColor(100, 100, 100, 100);
        
        // getRed, getGreen, getBlue, getAlpha are from java.awt.Color
        // but they use getRGB(), which is overridden in MutableColor.
        assertEquals(100, mc.getRed());
        assertEquals(100, mc.getGreen());
        assertEquals(100, mc.getBlue());
        assertEquals(100, mc.getAlpha());

        mc.setRGBA(50, 60, 70, 80);
        assertEquals(50, mc.getRed());
        assertEquals(60, mc.getGreen());
        assertEquals(70, mc.getBlue());
        assertEquals(80, mc.getAlpha());

        // equals(Object) is inherited from java.awt.Color
        MutableColor mc1 = new MutableColor(10, 20, 30, 40);
        MutableColor mc2 = new MutableColor(10, 20, 30, 40);
        java.awt.Color c1 = new java.awt.Color(10,20,30,40);

        assertTrue("MutableColor should be equal to another MutableColor with same values", mc1.equals(mc2));
        assertTrue("MutableColor should be equal to java.awt.Color with same values", mc1.equals(c1));
        assertTrue("java.awt.Color should be equal to MutableColor with same values", c1.equals(mc1));

        MutableColor mc3 = new MutableColor(11, 20, 30, 40);
        assertFalse("MutableColor should not be equal if values differ", mc1.equals(mc3));
    }
}
