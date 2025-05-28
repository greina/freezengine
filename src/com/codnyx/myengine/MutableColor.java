package com.codnyx.myengine;

import java.awt.Color;

/**
 * Represents a mutable version of {@link java.awt.Color}.
 * Unlike {@code java.awt.Color}, the RGBA components of this class can be changed after instantiation.
 * The color is stored internally as a 32-bit ARGB integer.
 */
public class MutableColor extends Color {
	/**
	 * The internal ARGB integer representation of the color.
	 */
	int value = 0;
	
	/**
	 * Constructs a new {@code MutableColor} with the specified red, green, blue, and alpha values.
	 * 
	 * @param r the red component in the range (0 - 255)
	 * @param g the green component in the range (0 - 255)
	 * @param b the blue component in the range (0 - 255)
	 * @param a the alpha component in the range (0 - 255)
	 * @throws IllegalArgumentException if any of the color component values are outside the range 0-255
	 */
	public MutableColor(int r, int g, int b, int a) {
		super(r, g, b, a); // Calls Color(r,g,b,a) which itself calls testColorValueRange
		setRGBA(r,g,b,a); // Sets the internal 'value' field
	}
	
	/**
	 * Constructs a new {@code MutableColor} with the specified combined RGB value.
	 * The alpha component is set to 255 (fully opaque).
	 * 
	 * @param rgb the combined RGB components. The individual components can be extracted using bitwise operations.
	 *            Bits 24-31 are alpha, 16-23 are red, 8-15 are green, 0-7 are blue.
	 *            However, this constructor assumes the input is primarily an RGB value and makes it opaque.
	 */
	public MutableColor(int rgb)
	{
		super(rgb); // Calls Color(rgb)
		setRGBA(rgb); // Sets the internal 'value' field, ensuring full opacity
	}
	
	/**
	 * Returns the ARGB value representing this color.
	 * Bits 24-31 are alpha, 16-23 are red, 8-15 are green, 0-7 are blue.
	 * This overrides the method in {@link java.awt.Color}.
	 * 
	 * @return the integer ARGB value of this color.
	 */
	@Override
	public int getRGB() {
		return value;
	}
	

	/**
	 * Sets the color's RGBA components.
	 * 
	 * @param r the red component in the range (0 - 255)
	 * @param g the green component in the range (0 - 255)
	 * @param b the blue component in the range (0 - 255)
	 * @param a the alpha component in the range (0 - 255)
	 * @throws IllegalArgumentException if any of the color component values are outside the range 0-255
	 */
	public void setRGBA(int r, int g, int b, int a) {
		testColorValueRange(r, g, b, a); // Explicitly test range before setting value
		value = ((a & 0xFF) << 24) | ((r & 0xFF) << 16) | ((g & 0xFF) << 8)
				| ((b & 0xFF) << 0);
	}
	
	/**
	 * Sets the color's RGB components, with alpha set to fully opaque (255).
	 * 
	 * @param rgb the combined RGB components. Alpha component of the input is ignored.
	 *            Bits 16-23 are red, 8-15 are green, 0-7 are blue.
	 */
	public void setRGBA(int rgb) 
	{
        value = 0xff000000 | (rgb & 0x00FFFFFF); // Ensure alpha is 255 and only RGB part of input is used
	}

	/**
	 * Checks if the given color component values are within the valid range (0-255).
	 * This method is also called by the superclass {@link java.awt.Color} constructor.
	 * 
	 * @param r the red component
	 * @param g the green component
	 * @param b the blue component
	 * @param a the alpha component
	 * @throws IllegalArgumentException if any of the color component values are outside the range 0-255
	 */
	private static void testColorValueRange(int r, int g, int b, int a) {
		boolean rangeError = false;
		String badComponentString = "";

		if (a < 0 || a > 255) {
			rangeError = true;
			badComponentString = badComponentString + " Alpha";
		}
		if (r < 0 || r > 255) {
			rangeError = true;
			badComponentString = badComponentString + " Red";
		}
		if (g < 0 || g > 255) {
			rangeError = true;
			badComponentString = badComponentString + " Green";
		}
		if (b < 0 || b > 255) {
			rangeError = true;
			badComponentString = badComponentString + " Blue";
		}
		if (rangeError == true) {
			throw new IllegalArgumentException(
					"Color parameter outside of expected range:"
							+ badComponentString);
		}
	}

	/**
	 * The serialization version UID.
	 */
	private static final long serialVersionUID = -8892889053819839463L;


}
