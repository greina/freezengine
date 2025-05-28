package com.codnyx.myengine;

import java.awt.Color;

/**
 * Represents a single polygon in a 3D mesh.
 * A polygon is defined by a set of vertices, a normal vector, a center point, and an optional texture.
 */
public class Polygon 
{
	/**
	 * The vertices that make up this polygon.
	 */
	protected Vertex[] vertices;
	/**
	 * The normal vector of this polygon.
	 */
	protected float[] normal = {0,0,0};
	/**
	 * The center point of this polygon.
	 */
	protected float[] center = {0,0,0};
	/**
	 * The texture applied to this polygon.
	 */
	public Texture texture;
	
	/**
	 * Constructs a new Polygon with the given vertices.
	 * The normal and center of the polygon are computed automatically.
	 * 
	 * @param vertices The vertices of the polygon.
	 */
	public Polygon(Vertex[] vertices)
	{
		this.vertices = vertices;
		computeNormal();
		computeCenter();
	}
	
	/**
	 * Constructs a new Polygon with the given vertex coordinates and color.
	 * The normal and center of the polygon are computed automatically.
	 * The polygon's normal is also set to each vertex.
	 * 
	 * @param vertices A 2D array of float values representing the coordinates of the vertices.
	 * @param color The color of the polygon.
	 */
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
	
	/**
	 * Sets the normal of this polygon to each of its vertices.
	 */
	public void setPNormalToVertices() 
	{
		for(Vertex c:this.vertices)
			c.normal = this.normal;
		
	}

	/**
	 * Computes the center point of this polygon.
	 * The center is calculated as the average of its vertices' coordinates.
	 */
	private void computeCenter() 
	{
		center[0] = center[1] = center[2] = 0;
		for(Vertex v: vertices)
			MyMath.add(v.point, center, center);
		MyMath.multiply(center, 1.0f/vertices.length, center);
	}

	/**
	 * Computes the normal vector of this polygon.
	 * The normal is calculated using the cross product of two edges of the polygon.
	 * Requires at least 3 vertices to compute the normal.
	 */
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

	/**
	 * Returns the center point of this polygon.
	 * 
	 * @return A float array representing the coordinates of the center point.
	 */
	public float[] getCenter()
	{
		return center;
	}
	
	/**
	 * Returns the normal vector of this polygon.
	 * 
	 * @return A float array representing the normal vector.
	 */
	public float[] getNormal()
	{
		return normal;
	}

	/**
	 * Returns the vertices of this polygon.
	 * 
	 * @return An array of Vertex objects.
	 */
	public Vertex[] getVertices()
	{
		return vertices;
	}
}
