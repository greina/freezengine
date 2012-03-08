package com.codnyx.myengine;

import java.awt.Color;

public class Polygon 
{
	protected Vertex[] vertices;
	protected float[] normal = {0,0,0};
	protected float[] center = {0,0,0};
	public Texture texture;
	
	public Polygon(Vertex[] vertices)
	{
		this.vertices = vertices;
		computeNormal();
		computeCenter();
	}
	
	public Polygon(float[][] vertices, Color color)
	{
		this.vertices = new Vertex[vertices.length];
		int i = 0;
		for(float[] v: vertices)
		{
			this.vertices[i++] = new Vertex(v);
		}
		computeCenter();
		computeNormal();
		setPNormalToVertices();
		for(Vertex c:this.vertices)
		{
			c.color = color.getRGB();
		}
	}
	
	public void setPNormalToVertices() 
	{
		for(Vertex c:this.vertices)
			c.normal = this.normal;
		
	}

	private void computeCenter() 
	{
		center[0] = center[1] = center[2] = 0;
		for(Vertex v: vertices)
			MyMath.add(v.point, center, center);
		MyMath.multiply(center, 1.0f/vertices.length, center);
	}

	public void computeNormal()
	{
		if(vertices.length < 3)
			return;

		float[] vec1 = {0,0,0};
		float[] vec2 = {0,0,0};

		MyMath.subtract(vertices[1].point, vertices[0].point, vec1);
		MyMath.subtract(vertices[2].point, vertices[1].point, vec2);

		MyMath.crossProduct(vec1, vec2, this.normal);
		MyMath.normalize(this.normal);
	}

	public float[] getCenter()
	{
		return center;
	}
	
	public float[] getNormal()
	{
		return normal;
	}

	public Vertex[] getVertices()
	{
		return vertices;
	}
}
