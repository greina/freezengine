package com.codnyx.myengine.tests;

import static org.junit.Assert.*;

import java.util.Random;

import org.junit.Test;

public class TestColor {

	Random random = new Random();
	
//	@Test
//	public void test() 
//	{
//		for(int run = 0; run < 100000; run++)
//		{
//			int color1 = random.nextInt();
//			int color2 = random.nextInt();
//			float l = random.nextFloat();
//			float h = 1 - l;
//			int color3_1 = (int) (l*(((long)color1)&0xffffffff) + h*(((long)color2)&0xffffffff));
//			int a = (int) (getAlpha(color1)*l + getAlpha(color2)*h);
//			int r = (int) (getRed(color1)*l +getRed(color2)*h);
//			int g = (int) (getGreen(color1)*l +getGreen(color2)*h);
//			int b = (int) (getBlue(color1)*l +getBlue(color2)*h);
//			int color3_2 = (a << 24) | (r<<16) | (g<<8) | b;
//			if(color3_1 != color3_2)
//				System.out.println("Different");
//			assertEquals(color3_1, color3_2);
//		}
//	}
	
	@Test
	public void test() 
	{
		for(int run = 0; run < 100000; run++)
		{
			byte color1 = (byte) (random.nextInt()&0xFF);
			byte color2 = (byte) (random.nextInt()&0xFF);
			float l = .5f;//random.nextFloat();
			float h = 1 - l;
			byte color3_1 = (byte)((((int)color1)&0xFF)*l + (((int)color2)&0xFF)*h);
			byte a = (byte) (((color1>>4)&0xf)*l + ((color2>>4)&0xf)*h);
			byte b = (byte) (((color1)&0xf)*l + ((color2)&0xf)*h);
			byte color3_2 = (byte) ((a << 4) |  b);
			if(color3_1 != color3_2)
				System.out.println("Different");
			assertEquals(color3_1, color3_2);
		}
	}
//
//	private int getBlue(int color2) {
//		return (color2 )&0xff;
//	}
//
//	private int getGreen(int color2) {
//		return (color2 >> 8)&0xff;
//	}
//
//	private int getRed(int color2) {
//		return (color2 >> 16)&0xff;
//	}
//
//	private int getAlpha(int color1) {
//		return (color1 >> 24)&0xff;
//	}

}
