package com.codnyx.myengine;

public class MyMath
{
	public static final float dotProduct(float[] a, float[] b)
	{
		return a[0]*b[0]+a[1]*b[1]+a[2]*b[2];
	}
	
	public static final  float[] crossProduct(float[] a, float[] b, float[] result)
	{
		result[0] = a[1]*b[2] - a[2]*b[1];
		result[1] = a[2]*b[0] - a[0]*b[2];
		result[2] = a[0]*b[1] - a[1]*b[0];
		return result;
	}
	
	public static final  float[] subtract(float[] a, float[] b, float[] result)
	{
		result[0] = a[0] - b[0];
		result[1] = a[1] - b[1];
		result[2] = a[2] - b[2];
		return result;
	}
	
	public static final  float[] add(float[] a, float[] b, float[] result)
	{
		result[0] = a[0] + b[0];
		result[1] = a[1] + b[1];
		result[2] = a[2] + b[2];
		return result;
	}

	public static final float[] multiply(float[] vec, float s, float[] result)
	{
		result[0] = vec[0]*s;
		result[1] = vec[1]*s;
		result[2] = vec[2]*s;
		return result;		
	}
	
	public static final float length(float[] vec)
	{
		return (float)Math.sqrt(vec[0]*vec[0] + vec[1]*vec[1] + vec[2]*vec[2]);
	}

	public static final float[] normalize(float[] vec)
	{
		float l = length(vec);
		vec[0] /= l;
		vec[1] /= l;
		vec[2] /= l;
		return vec;		
	}
}
