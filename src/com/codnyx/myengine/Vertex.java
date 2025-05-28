package com.codnyx.myengine;

import java.awt.Color;
import java.util.Arrays;

/**
 * Represents a vertex in 3D space, typically a corner of a polygon.
 * A vertex has a position (point), a normal vector, and a color.
 */
public class Vertex {
	/**
	 * The 3D coordinates of the vertex (x, y, z).
	 */
	public float[] point = {0,0,0};
	/**
	 * The normal vector at this vertex.
	 */
	float[] normal;
	/**
	 * The default color for vertices (white).
	 */
	public static final int COLOR_WHITE = Color.white.getRGB() ;
	/**
	 * The color of the vertex, represented as an integer (RGB).
	 */
	int color = COLOR_WHITE;
	
	/**
	 * Default constructor. Initializes the vertex at the origin (0,0,0) with the default color and no normal.
	 */
	public Vertex()
	{
	}
	
	/**
	 * Constructs a new Vertex with the given coordinates.
	 * 
	 * @param point A float array representing the 3D coordinates (x, y, z) of the vertex.
	 */
	public Vertex(float[] point)
	{
		this.point = point;
	}
	
	/**
	 * Constructs a new Vertex with the given coordinates and normal vector.
	 * 
	 * @param point A float array representing the 3D coordinates (x, y, z) of the vertex.
	 * @param normal A float array representing the normal vector at this vertex.
	 */
	public Vertex(float[] point, float[] normal)
	{
		this(point);
		this.normal = normal;
	}
	
	/**
	 * Constructs a new Vertex with the given coordinates, normal vector, and color.
	 * 
	 * @param point A float array representing the 3D coordinates (x, y, z) of the vertex.
	 * @param normal A float array representing the normal vector at this vertex.
	 * @param color The color of the vertex.
	 */
	public Vertex(float[] point, float[] normal, Color color)
	{
		this(point, normal);
		this.color = color.getRGB();
	}
	
	/**
	 * Constructs a new Vertex with the given coordinates and color.
	 * 
	 * @param point A float array representing the 3D coordinates (x, y, z) of the vertex.
	 * @param color The color of the vertex.
	 */
	public Vertex(float[] point, Color color)
	{
		this(point);
		this.color = color.getRGB();
	}

	/**
	 * Copy constructor. Creates a new Vertex by copying the properties of another Vertex.
	 * 
	 * @param vertex The Vertex to copy.
	 */
	public Vertex(Vertex vertex)
	{
		setTo(vertex);		
	}

	/**
	 * Sets the properties of this Vertex to match the properties of another Vertex.
	 * This includes cloning the point and normal arrays.
	 * 
	 * @param v The Vertex to copy properties from.
	 */
	public void setTo(Vertex v)
	{
		this.color = v.color;
		this.normal = v.normal != null?v.normal.clone():null;
		this.point = v.point.clone();
	}
	
	/**
	 * Resets the Vertex to its default state:
	 * - Point coordinates are set to (0,0,0).
	 * - Normal is set to null.
	 * - Color is set to the default white color.
	 */
	public void reset()
	{
		Arrays.fill(point, 0);
		if(normal != null)
			normal = null;
		color = COLOR_WHITE;		
	}
	
	
}
