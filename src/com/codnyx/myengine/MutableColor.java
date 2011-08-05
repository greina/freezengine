package com.codnyx.myengine;

import java.awt.Color;

public class MutableColor extends Color {
	int value = 0;
	
	public MutableColor(int r, int g, int b, int a) {
		super(r, g, b, a);
		setRGBA(r,g,b,a);
	}
	
	public MutableColor(int rgb)
	{
		super(rgb);
		setRGBA(rgb);
	}
	
	@Override
	public int getRGB() {
		return value;
	}
	

	public void setRGBA(int r, int g, int b, int a) {
		value = ((a & 0xFF) << 24) | ((r & 0xFF) << 16) | ((g & 0xFF) << 8)
				| ((b & 0xFF) << 0);
		testColorValueRange(r, g, b, a);
	}
	
	public void setRGBA(int rgb) 
	{
        value = 0xff000000 | rgb;
	}

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

	private static final long serialVersionUID = -8892889053819839463L;


}
