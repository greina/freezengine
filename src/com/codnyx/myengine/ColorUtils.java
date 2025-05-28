package com.codnyx.myengine;

/**
 * Utility class for handling color conversions and component extraction.
 * Colors are represented as 32-bit integers in ARGB format (Alpha, Red, Green, Blue).
 */
public class ColorUtils
{
	/**
	 * Extracts the alpha component from an ARGB color value.
	 * 
	 * @param value The ARGB color integer.
	 * @return The alpha component (0-255).
	 */
	public static final int getAlpha(int value) {
		return (value >> 24) & 0xff;
	}

	/**
	 * Extracts the red component from an ARGB color value.
	 * 
	 * @param value The ARGB color integer.
	 * @return The red component (0-255).
	 */
	public static final  int getRed(int value) {
		return (value >> 16) & 0xFF;
	}

	/**
	 * Extracts the green component from an ARGB color value.
	 * 
	 * @param value The ARGB color integer.
	 * @return The green component (0-255).
	 */
	public static final  int getGreen(int value) {
		return (value >> 8) & 0xFF;
	}

	/**
	 * Extracts the blue component from an ARGB color value.
	 * 
	 * @param value The ARGB color integer.
	 * @return The blue component (0-255).
	 */
	public static final  int getBlue(int value) {
		return (value >> 0) & 0xFF;
	}
	
	/**
	 * Combines individual ARGB components into a single integer color value.
	 * 
	 * @param a The alpha component (0-255).
	 * @param r The red component (0-255).
	 * @param g The green component (0-255).
	 * @param b The blue component (0-255).
	 * @return The combined ARGB color integer.
	 */
	public static final int getRGB(int a, int r, int g, int b)
	{
		return ((a & 0xFF) << 24) | ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | ((b & 0xFF) << 0);
	}
}