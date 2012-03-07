package com.codnyx.myengine;

public class ColorUtils
{
	public static final int getAlpha(int value) {
		return (value >> 24) & 0xff;
	}

	public static final  int getRed(int value) {
		return (value >> 16) & 0xFF;
	}
	public static final  int getGreen(int value) {
		return (value >> 8) & 0xFF;
	}

	public static final  int getBlue(int value) {
		return (value >> 0) & 0xFF;
	}
	
	public static final int getRGB(int a, int r, int g, int b)
	{
		return ((a & 0xFF) << 24) | ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | ((b & 0xFF) << 0);
	}
}