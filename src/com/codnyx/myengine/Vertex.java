package com.codnyx.myengine;

import java.awt.Color;

public class Vertex {
	float[] point;
	float[] normal;
	int[] projection = {0,0};
	Color color = Color.white;
	float[] tnormal = {0,0,0};
	public float depth;
	
	public Vertex()
	{
		projection = new int[2];
	}
	
	public Vertex(float[] point)
	{
		this.point = point;
	}
	
	public Vertex(float[] point, float[] normal)
	{
		this(point);
		this.normal = normal;
	}
	
	public Vertex(float[] point, float[] normal, Color color)
	{
		this(point, normal);
		this.color = color;
	}
	
	public Vertex(float[] point, Color color)
	{
		this(point);
		this.color = color;
	}
	
	
	
}
