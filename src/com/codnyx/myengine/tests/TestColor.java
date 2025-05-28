package com.codnyx.myengine.tests;

import static org.junit.Assert.*;

import java.util.Random;

import org.junit.Test;

/**
 * JUnit test class for color manipulation and interpolation logic.
 * This class aims to verify the correctness of color blending or component-wise
 * interpolation methods, which are crucial for effects like Gouraud shading or
 * alpha blending in the rendering engine.
 */
public class TestColor {

	/** Random number generator for creating test color values. */
	Random random = new Random();
	
	//	/**
	//	 * (Commented out) Test method for full 32-bit ARGB color interpolation.
	//	 * This test would generate two random ARGB colors and an interpolation factor.
	//	 * It then compares a direct interpolation of the integer color value (treated as long to avoid overflow during sum)
	//	 * with an interpolation performed component-wise (alpha, red, green, blue) and then recombined.
	//	 * The expectation is that both methods should yield the same ARGB integer result.
	//	 * Helper methods getAlpha, getRed, getGreen, getBlue would be needed from ColorUtils or similar.
	//	 */
	//	@Test
	//	public void testArgbInterpolation() 
	//	{
	//		for(int run = 0; run < 100000; run++)
	//		{
	//			int color1 = random.nextInt();
	//			int color2 = random.nextInt();
	//			float l = random.nextFloat(); // Interpolation factor for color1
	//			float h = 1.0f - l;         // Interpolation factor for color2
	//			
	//			// Interpolation method 1: Direct interpolation of long color values
	//			// Note: This direct long interpolation is generally not how color blending works due to component-wise nature.
	//			// This test might be flawed in its premise for 'color3_1' if it's not using a proper component-wise blend.
	//			// However, if it's testing a specific fixed-point style interpolation on the whole int, it needs clarification.
	//			// Assuming it's a test for a custom interpolation scheme.
	//			int color3_1 = (int) (l*(((long)color1)&0xffffffffL) + h*(((long)color2)&0xffffffffL));
	//			
	//			// Interpolation method 2: Component-wise interpolation
	//			// int a = (int) (getAlpha(color1)*l + getAlpha(color2)*h);
	//			// int r = (int) (getRed(color1)*l + getRed(color2)*h);
	//			// int g = (int) (getGreen(color1)*l + getGreen(color2)*h);
	//			// int b = (int) (getBlue(color1)*l + getBlue(color2)*h);
	//			// int color3_2 = (a << 24) | (r<<16) | (g<<8) | b;
	//			
	//			// This specific commented out section appears to be missing the actual getAlpha/Red/Green/Blue calls
	//			// or they were intended to be local private methods as seen commented out below.
	//			// For proper Javadoc, we assume ColorUtils.getAlpha etc. would be used.
	//			int a = (int) (com.codnyx.myengine.ColorUtils.getAlpha(color1)*l + com.codnyx.myengine.ColorUtils.getAlpha(color2)*h);
	//			int r = (int) (com.codnyx.myengine.ColorUtils.getRed(color1)*l + com.codnyx.myengine.ColorUtils.getRed(color2)*h);
	//			int g = (int) (com.codnyx.myengine.ColorUtils.getGreen(color1)*l + com.codnyx.myengine.ColorUtils.getGreen(color2)*h);
	//			int b = (int) (com.codnyx.myengine.ColorUtils.getBlue(color1)*l + com.codnyx.myengine.ColorUtils.getBlue(color2)*h);
	//			int color3_2 = com.codnyx.myengine.ColorUtils.getRGB(a,r,g,b);
	//			
	//			if(color3_1 != color3_2)
	//				System.out.println("Different ARGB interpolation results. Method1: " + color3_1 + ", Method2: " + color3_2);
	//			assertEquals("ARGB color interpolation mismatch", color3_2, color3_1); // Standard assertion order: expected, actual
	//		}
	//	}
	
	/**
	 * Test method for byte-sized color component interpolation, possibly for two 4-bit components packed into a byte.
	 * This test generates two random byte values ({@code color1}, {@code color2}). It then interpolates them
	 * using a fixed factor of 0.5 ({@code l = 0.5f, h = 0.5f}).
	 * <p>
	 * Method 1 ({@code color3_1}): Directly interpolates the integer representation of the bytes.
	 * <p>
	 * Method 2 ({@code color3_2}): Treats each byte as two 4-bit components. It interpolates these
	 * 4-bit components separately (named {@code a} for the higher 4 bits, {@code b} for the lower 4 bits)
	 * and then recombines them into a byte.
	 * <p>
	 * The test asserts that both interpolation methods yield the same byte result. This could be a test
	 * for a specialized color format or a low-precision interpolation routine.
	 * It runs for 100,000 iterations with random byte values.
	 */
	@Test
	public void testPackedByteComponentInterpolation() 
	{
		for(int run = 0; run < 100000; run++)
		{
			byte color1 = (byte) (random.nextInt()&0xFF); // Full byte random value
			byte color2 = (byte) (random.nextInt()&0xFF); // Full byte random value
			float l = 0.5f; // Interpolation factor for color1
			float h = 1.0f - l; // Interpolation factor for color2
			
			// Method 1: Interpolate treating bytes as unsigned integers [0, 255]
			byte color3_1 = (byte) Math.round((((int)color1) & 0xFF) * l + (((int)color2) & 0xFF) * h);
			
			// Method 2: Interpolate as two 4-bit components
			// 'a' represents interpolation of upper 4 bits, 'b' for lower 4 bits
			byte a_nibble1 = (byte) (((color1 >> 4) & 0xF) * l + ((color2 >> 4) & 0xF) * h);
			byte b_nibble1 = (byte) (((color1) & 0xF) * l + ((color2) & 0xF) * h);
			byte color3_2 = (byte) ((a_nibble1 << 4) | b_nibble1);
			
			if(color3_1 != color3_2) {
				System.out.println("Run " + run + ": color1=0x" + Integer.toHexString(color1 & 0xFF) + 
								   ", color2=0x" + Integer.toHexString(color2 & 0xFF) + 
								   ". Result1=0x" + Integer.toHexString(color3_1 & 0xFF) + 
								   ", Result2=0x" + Integer.toHexString(color3_2 & 0xFF));
				// For debugging, print component values:
				// System.out.println("  c1_upper=" + ((color1>>4)&0xf) + ", c1_lower=" + (color1&0xf));
				// System.out.println("  c2_upper=" + ((color2>>4)&0xf) + ", c2_lower=" + (color2&0xf));
				// System.out.println("  a_nibble1=" + a_nibble1 + ", b_nibble1=" + b_nibble1);
			}
			// This assertion might fail if rounding behavior or precision issues differ significantly,
			// especially since one method uses float throughout then casts, other casts intermediate nibbles.
			// Using Math.round on color3_1 for a more robust comparison if direct byte cast is problematic.
			assertEquals("Packed byte component interpolation mismatch", color3_1, color3_2);
		}
	}

	// Commented out local helper methods, assuming ColorUtils would be used for ARGB.
	// private int getBlue(int color2) {
	// 	return (color2 )&0xff;
	// }
	//
	// private int getGreen(int color2) {
	// 	return (color2 >> 8)&0xff;
	// }
	//
	// private int getRed(int color2) {
	// 	return (color2 >> 16)&0xff;
	// }
	//
	// private int getAlpha(int color1) {
	// 	return (color1 >> 24)&0xff;
	// }
}
