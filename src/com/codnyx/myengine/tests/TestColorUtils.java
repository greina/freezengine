package com.codnyx.myengine.tests;

import static org.junit.Assert.*;
import org.junit.Test;
import com.codnyx.myengine.ColorUtils;

/**
 * JUnit test class for the ColorUtils class.
 * This class tests the static utility methods for ARGB color component
 * extraction and combination.
 */
public class TestColorUtils {

    // --- Test getAlpha(int value) ---
    @Test
    public void testGetAlpha() {
        // Opaque color (alpha = 255)
        int opaqueColor = 0xFF123456; // Alpha: FF, Red: 12, Green: 34, Blue: 56
        assertEquals("Alpha of opaqueColor (0xFF123456) should be 255", 255, ColorUtils.getAlpha(opaqueColor));

        // Fully transparent color (alpha = 0)
        int transparentColor = 0x00ABCDEF; // Alpha: 00, Red: AB, Green: CD, Blue: EF
        assertEquals("Alpha of transparentColor (0x00ABCDEF) should be 0", 0, ColorUtils.getAlpha(transparentColor));

        // Semi-transparent color (alpha = 128)
        int semiTransparentColor = 0x80A1B2C3; // Alpha: 80 (128), Red: A1, Green: B2, Blue: C3
        assertEquals("Alpha of semiTransparentColor (0x80A1B2C3) should be 128", 128, ColorUtils.getAlpha(semiTransparentColor));
        
        // Test with alpha being the only non-zero part
        int onlyAlpha = 0xAA000000;
        assertEquals("Alpha of 0xAA000000 should be 170 (0xAA)", 170, ColorUtils.getAlpha(onlyAlpha));

        // Test with maximum integer value (all bits set)
        int maxInt = 0xFFFFFFFF;
        assertEquals("Alpha of 0xFFFFFFFF should be 255", 255, ColorUtils.getAlpha(maxInt));
        
        // Test with zero integer value
        int zeroInt = 0x00000000;
        assertEquals("Alpha of 0x00000000 should be 0", 0, ColorUtils.getAlpha(zeroInt));
    }

    // --- Test getRed(int value) ---
    @Test
    public void testGetRed() {
        // Pure Red (Red = 255, Green = 0, Blue = 0, Alpha = 255 for opaque)
        int pureRed = 0xFFFF0000;
        assertEquals("Red component of pureRed (0xFFFF0000) should be 255", 255, ColorUtils.getRed(pureRed));

        // White (Red = 255, Green = 255, Blue = 255, Alpha = 255)
        int white = 0xFFFFFFFF;
        assertEquals("Red component of white (0xFFFFFFFF) should be 255", 255, ColorUtils.getRed(white));

        // Black (Red = 0, Green = 0, Blue = 0, Alpha = 255)
        int black = 0xFF000000;
        assertEquals("Red component of black (0xFF000000) should be 0", 0, ColorUtils.getRed(black));

        // Custom color
        int customColor = 0xFF1A2B3C; // Red: 1A (26)
        assertEquals("Red component of customColor (0xFF1A2B3C) should be 26", 26, ColorUtils.getRed(customColor));
        
        // Color where red is 0 but other components are not
        int noRed = 0xFF00ABCD; // Red: 00
        assertEquals("Red component of noRed (0xFF00ABCD) should be 0", 0, ColorUtils.getRed(noRed));
    }

    // --- Test getGreen(int value) ---
    @Test
    public void testGetGreen() {
        // Pure Green (Red = 0, Green = 255, Blue = 0, Alpha = 255)
        int pureGreen = 0xFF00FF00;
        assertEquals("Green component of pureGreen (0xFF00FF00) should be 255", 255, ColorUtils.getGreen(pureGreen));

        // White (Red = 255, Green = 255, Blue = 255, Alpha = 255)
        int white = 0xFFFFFFFF;
        assertEquals("Green component of white (0xFFFFFFFF) should be 255", 255, ColorUtils.getGreen(white));

        // Black (Red = 0, Green = 0, Blue = 0, Alpha = 255)
        int black = 0xFF000000;
        assertEquals("Green component of black (0xFF000000) should be 0", 0, ColorUtils.getGreen(black));

        // Custom color
        int customColor = 0xFF1A2B3C; // Green: 2B (43)
        assertEquals("Green component of customColor (0xFF1A2B3C) should be 43", 43, ColorUtils.getGreen(customColor));

        // Color where green is 0 but other components are not
        int noGreen = 0xFFAB00CD; // Green: 00
        assertEquals("Green component of noGreen (0xFFAB00CD) should be 0", 0, ColorUtils.getGreen(noGreen));
    }

    // --- Test getBlue(int value) ---
    @Test
    public void testGetBlue() {
        // Pure Blue (Red = 0, Green = 0, Blue = 255, Alpha = 255)
        int pureBlue = 0xFF0000FF;
        assertEquals("Blue component of pureBlue (0xFF0000FF) should be 255", 255, ColorUtils.getBlue(pureBlue));

        // White (Red = 255, Green = 255, Blue = 255, Alpha = 255)
        int white = 0xFFFFFFFF;
        assertEquals("Blue component of white (0xFFFFFFFF) should be 255", 255, ColorUtils.getBlue(white));

        // Black (Red = 0, Green = 0, Blue = 0, Alpha = 255)
        int black = 0xFF000000;
        assertEquals("Blue component of black (0xFF000000) should be 0", 0, ColorUtils.getBlue(black));

        // Custom color
        int customColor = 0xFF1A2B3C; // Blue: 3C (60)
        assertEquals("Blue component of customColor (0xFF1A2B3C) should be 60", 60, ColorUtils.getBlue(customColor));
        
        // Color where blue is 0 but other components are not
        int noBlue = 0xFFABCD00; // Blue: 00
        assertEquals("Blue component of noBlue (0xFFABCD00) should be 0", 0, ColorUtils.getBlue(noBlue));
    }

    // --- Test getRGB(int a, int r, int g, int b) ---
    @Test
    public void testGetRGB() {
        // Test with all components at 0 (transparent black)
        int color1 = ColorUtils.getRGB(0, 0, 0, 0);
        assertEquals("getRGB(0,0,0,0) should be 0x00000000", 0x00000000, color1);

        // Test with all components at 255 (opaque white)
        int color2 = ColorUtils.getRGB(255, 255, 255, 255);
        assertEquals("getRGB(255,255,255,255) should be 0xFFFFFFFF", 0xFFFFFFFF, color2);

        // Test with mid-values
        int color3 = ColorUtils.getRGB(128, 64, 32, 16); // 0x80402010
        assertEquals("getRGB(128,64,32,16) should be 0x80402010", 0x80402010, color3);
        
        // Test specific known color (opaque red)
        int colorRed = ColorUtils.getRGB(255, 255, 0, 0);
        assertEquals("getRGB(255,255,0,0) for opaque red should be 0xFFFF0000", 0xFFFF0000, colorRed);

        // Test values that might exceed 255 (should be masked to 0xFF)
        // The implementation uses (val & 0xFF), so this is implicitly handled.
        // For example, getRGB(256, 257, 258, 259) would effectively be getRGB(0,1,2,3)
        // because (256 & 0xFF) = 0, (257 & 0xFF) = 1, etc.
        int colorValOver = ColorUtils.getRGB(256, 257, 258, 259); // effectively (0,1,2,3)
        assertEquals("getRGB with values > 255 should mask them (e.g., 256->0, 257->1)", 0x00010203, colorValOver);
        
        // Test negative values (should also be masked, e.g. -1 & 0xFF = 255)
        int colorValNegative = ColorUtils.getRGB(-1, -2, -3, -128); // effectively (255, 254, 253, 128)
        assertEquals("getRGB with negative values should mask them (e.g., -1->255, -128->128)", 0xFFFEFD80, colorValNegative);
    }

    // --- Test getRGB in conjunction with getters ---
    @Test
    public void testGetRGBAndGettersConsistency() {
        int a = 200;
        int r = 100;
        int g = 50;
        int b = 25;

        int combinedColor = ColorUtils.getRGB(a, r, g, b);

        assertEquals("Alpha consistency: getAlpha(getRGB(...))", a, ColorUtils.getAlpha(combinedColor));
        assertEquals("Red consistency: getRed(getRGB(...))", r, ColorUtils.getRed(combinedColor));
        assertEquals("Green consistency: getGreen(getRGB(...))", g, ColorUtils.getGreen(combinedColor));
        assertEquals("Blue consistency: getBlue(getRGB(...))", b, ColorUtils.getBlue(combinedColor));

        // Test with edge values (0 and 255)
        a = 0; r = 255; g = 0; b = 255;
        combinedColor = ColorUtils.getRGB(a, r, g, b);
        assertEquals("Alpha consistency (edge): getAlpha(getRGB(0,255,0,255))", a, ColorUtils.getAlpha(combinedColor));
        assertEquals("Red consistency (edge): getRed(getRGB(0,255,0,255))", r, ColorUtils.getRed(combinedColor));
        assertEquals("Green consistency (edge): getGreen(getRGB(0,255,0,255))", g, ColorUtils.getGreen(combinedColor));
        assertEquals("Blue consistency (edge): getBlue(getRGB(0,255,0,255))", b, ColorUtils.getBlue(combinedColor));
    }
}
