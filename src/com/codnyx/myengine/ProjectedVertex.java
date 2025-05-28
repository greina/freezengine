package com.codnyx.myengine;
import java.awt.Color;
import java.util.Arrays;

/**
 * Represents a {@link Vertex} that has been projected into 2D screen space.
 * It extends the basic {@code Vertex} by adding 2D screen coordinates (x, y)
 * and a depth value (typically the Z-coordinate after perspective transformation,
 * used for Z-buffering).
 */
public class ProjectedVertex extends Vertex
{
	/** 
	 * The 2D screen coordinates {x, y} of the projected vertex. 
	 * Initialized to {0,0}.
	 */
	int[] projection = {0,0}; 
	/** 
	 * The depth of the vertex after perspective projection. 
	 * This is typically the transformed Z-value used for Z-buffering.
	 * Initialized to 0.
	 */
	public float depth = 0;
	
	/**
	 * Default constructor. Initializes a ProjectedVertex at the origin,
	 * with default color, no normal, zero depth, and projection at (0,0).
	 */
	public ProjectedVertex()
	{
		super(); // Call Vertex() constructor
		init();  // Initialize projection-specific fields
	}
	
	/**
	 * Constructs a ProjectedVertex with the given 3D point coordinates.
	 * Normal and color will be default. Depth and projection are initialized.
	 * @param point A float array representing the 3D coordinates {x, y, z}.
	 */
	public ProjectedVertex(float[] point)
	{
		super(point);
		init();
	}
	
	/**
	 * Constructs a ProjectedVertex with the given 3D point and normal vector.
	 * Color will be default. Depth and projection are initialized.
	 * @param point A float array representing the 3D coordinates {x, y, z}.
	 * @param normal A float array representing the normal vector {nx, ny, nz}.
	 */
	public ProjectedVertex(float[] point, float[] normal)
	{
		super(point, normal);
		init();
	}
	
	/**
	 * Constructs a ProjectedVertex with 3D point, normal vector, and color.
	 * Depth and projection are initialized.
	 * @param point A float array representing the 3D coordinates {x, y, z}.
	 * @param normal A float array representing the normal vector {nx, ny, nz}.
	 * @param color The color of the vertex.
	 */
	public ProjectedVertex(float[] point, float[] normal, Color color)
	{
		super(point, normal, color);
		init();
	}
	
	/**
	 * Constructs a ProjectedVertex with 3D point and color.
	 * Normal will be null. Depth and projection are initialized.
	 * @param point A float array representing the 3D coordinates {x, y, z}.
	 * @param color The color of the vertex.
	 */
	public ProjectedVertex(float[] point, Color color)
	{
		super(point, color);
		init(); // Ensure projection fields are initialized
	}

	/**
	 * Constructs a ProjectedVertex by copying properties from an existing {@link Vertex}.
	 * Depth and projection are initialized to default values.
	 * @param vertex The Vertex to copy base properties from.
	 */
	public ProjectedVertex(Vertex vertex)
	{
		super(vertex);	
		init();	
	}

	/**
	 * Copy constructor. Creates a new ProjectedVertex by copying all properties
	 * from another ProjectedVertex, including its projection and depth.
	 * @param vertex The ProjectedVertex to copy.
	 */
	public ProjectedVertex(ProjectedVertex vertex)
	{
		// super(vertex); // This would call Vertex(Vertex)
		// setTo(vertex); // This is more direct for full copy
		this.setTo(vertex); // Use specific setTo for ProjectedVertex
	}
	
	/**
	 * Sets the properties of this ProjectedVertex from a base {@link Vertex}.
	 * The projection-specific fields (projection array and depth) are reset to their defaults.
	 * Overrides {@link Vertex#setTo(Vertex)}.
	 * @param v The Vertex to copy base properties from.
	 */
	@Override
	public void setTo(Vertex v)
	{
		super.setTo(v);
		init(); // Reset projection-specific fields
	}
	
	
	/**
	 * Sets the properties of this ProjectedVertex to match another ProjectedVertex.
	 * This includes copying the 3D point, normal, color, screen projection, and depth.
	 * @param pv The ProjectedVertex to copy properties from.
	 */
	public void setTo(ProjectedVertex pv)
	{
		super.setTo(pv); // Set base Vertex properties

		this.depth = pv.depth;
		// Ensure projection array is cloned, not shared
		if (pv.projection != null) {
			this.projection = pv.projection.clone();
		} else {
			this.projection = new int[]{0,0}; // Or handle as an error/default
		}
	}
	
	/**
	 * Initializes or resets the projection-specific fields of this vertex:
	 * the {@link #projection} array is filled with zeros, and {@link #depth} is set to 0.
	 */
	private final void init()
	{
		Arrays.fill(projection, 0);
		depth = 0;
	}
	
	/**
	 * Resets this ProjectedVertex to its default state.
	 * This calls the superclass {@link Vertex#reset()} and then re-initializes
	 * the projection-specific fields (projection array and depth) using {@link #init()}.
	 */
	@Override
	public void reset()
	{
		super.reset();
		init();
	}
}
