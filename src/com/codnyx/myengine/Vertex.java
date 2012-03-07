package com.codnyx.myengine;

import java.awt.Color;
import java.util.Arrays;

public class Vertex {
	public float[] point = {0,0,0};
	float[] normal;
	public static final int COLOR_WHITE = Color.white.getRGB() ;
	int color = COLOR_WHITE;
	
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
		this.color = color.getRGB();
	}
	
	public Vertex(float[] point, Color color)
	{
		this(point);
		this.color = color.getRGB();
	}

	public Vertex(Vertex vertex)
	{
		setTo(vertex);		
	}

	public void setTo(Vertex v)
	{
		this.color = v.color;
		this.normal = v.normal != null?v.normal.clone():null;
		this.point = v.point.clone();
	}
	
	public void reset()
	{
		Arrays.fill(point, 0);
		if(normal != null)
			normal = null;
		color = COLOR_WHITE;		
	}
	
	
}
