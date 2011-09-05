package com.codnyx.myengine;

public abstract class Texture
{
	public int[] u;
	public int[] v;
	public int[] o;
	public float[] U;
	public float[] V;
	public float[] O;
	
	public abstract int getColor(int x, int y);

}
