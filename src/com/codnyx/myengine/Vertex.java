package com.codnyx.myengine;

import java.awt.Color;

public class Vertex {
	public float[] point = {0,0,0};
	float[] normal;
	int[] projection = {0,0};
	Color color = Color.white;
	float[] tnormal = {0,0,0};
	public float depth;
	
	public Vertex()
	{
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

	public Vertex(Vertex vertex)
	{
		setTo(vertex);		
	}

	public void setTo(Vertex v)
	{
		this.color = v.color;
		this.depth = v.depth;
		this.normal = v.normal.clone();
		this.point = v.normal.clone();
		this.tnormal = v.tnormal.clone();
		this.projection = v.projection.clone();
	}
	
	
	
}
