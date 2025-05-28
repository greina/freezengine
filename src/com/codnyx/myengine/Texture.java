package com.codnyx.myengine;

/**
 * Abstract base class for textures. A texture defines how color is applied to a surface.
 * This class provides fields that can be used to define the texture's coordinate system
 * and mapping behavior. Subclasses must implement the {@link #getColor(int, int)} method
 * to return a color for given texture coordinates.
 * <p>
 * The fields {@code u, v, o} (integer arrays) and {@code U, V, O} (float arrays)
 * likely represent parameters for transforming world or model space coordinates
 * into texture space coordinates (u,v). For example, they could define:
 * <ul>
 *   <li>An origin point (O or o) on the 3D model where the texture's (0,0) is anchored.</li>
 *   <li>Basis vectors (U/u and V/v) in 3D space that correspond to the texture's U and V axes.
 *       Their magnitude could influence scaling.</li>
 * </ul>
 * The integer arrays might be used for fixed-point calculations or direct pixel mapping factors,
 * while the float arrays would be for higher-precision transformations in world/eye space.
 * The exact interpretation and usage of these fields would depend on the polygon rendering
 * logic (e.g., how {@link PolygonRenderer} uses them).
 */
public abstract class Texture
{
	/** 
	 * Integer array, potentially for U-axis mapping parameters in texture space or pixel space.
	 * For example, could be {u_scale_x, u_scale_y} or similar mapping factors.
	 * Typically a float[2] or float[3] if representing a vector. Size is not fixed here.
	 */
	public int[] u; 
	/** 
	 * Integer array, potentially for V-axis mapping parameters in texture space or pixel space.
	 * For example, could be {v_scale_x, v_scale_y} or similar mapping factors.
	 */
	public int[] v;
	/** 
	 * Integer array, potentially for origin offset or other parameters in texture/pixel space.
	 */
	public int[] o;
	
	/** 
	 * Float array, likely representing the U-axis vector of the texture in 3D (model or world) space.
	 * This vector, along with {@link #V} and {@link #O}, can define the texture's orientation
	 * and scale on a 3D surface. Typically a float[3].
	 */
	public float[] U;
	/** 
	 * Float array, likely representing the V-axis vector of the texture in 3D (model or world) space.
	 * Typically a float[3].
	 */
	public float[] V;
	/** 
	 * Float array, likely representing the origin point of the texture in 3D (model or world) space.
	 * This is where the (0,0) coordinate of the texture is anchored. Typically a float[3].
	 */
	public float[] O;
	
	/**
	 * Abstract method to get the color from the texture at the given texture coordinates (x, y).
	 * Subclasses (e.g., image-based textures, procedural textures) must implement this method
	 * to define how color is determined.
	 * 
	 * @param x The x-coordinate in the texture's own coordinate system.
	 * @param y The y-coordinate in the texture's own coordinate system.
	 * @return The ARGB color value at the specified texture coordinates.
	 */
	public abstract int getColor(int x, int y);

}
