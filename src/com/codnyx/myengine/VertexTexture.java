package com.codnyx.myengine;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

/**
 * A specific implementation of {@link Texture} that maps a {@link BufferedImage} onto a polygon.
 * The texture's orientation and mapping on the polygon are defined by specifying 3D coordinates
 * for three of the polygon's vertices, which correspond to specific 2D texture coordinates (u,v)
 * from the image. These are used to establish a 3D texture coordinate system (Origin O, U-axis, V-axis)
 * in world space, and a corresponding 2D pixel coordinate system (origin o, u-axis, v-axis) in texture image space.
 * <p>
 * The constructor uses a Gram-Schmidt-like process to orthogonalize the U and V basis vectors in both
 * 3D space and 2D texture pixel space. This ensures that the texture axes are perpendicular,
 * simplifying the mapping.
 * <p>
 * Colors are retrieved from the underlying image data. Texture coordinates are masked to ensure they
 * wrap around if they go outside the image dimensions (assuming power-of-two dimensions for masking to work as wrap).
 */
public class VertexTexture extends Texture
{
	/** The {@link BufferedImage} source for this texture. */
	protected BufferedImage image;
	/** Direct access to the pixel data of the {@link #image} (ARGB integers). */
	protected int[] data;
	/** Bitmask for the x-coordinate to handle wrapping (width - 1, assumes width is power of two). */
	private int xmask;
	/** Bitmask for the y-coordinate to handle wrapping (height - 1, assumes height is power of two). */
	private int ymask;
	/** Width of the texture image in pixels. */
	private int w;
	
	
	/**
	 * Constructs a new VertexTexture.
	 * It initializes the texture's 3D coordinate system (O, U, V) based on three vertices of the polygon {@code p},
	 * and the 2D texture pixel coordinate system (o, u, v) based on corresponding 2D texture coordinates {@code textureVertex}.
	 * 
	 * The 3D system is set up as:
	 * O = p.vertices[1].point
	 * U = p.vertices[0].point - O
	 * V = p.vertices[2].point - O
	 * U and V are then orthogonalized and normalized (scaled to unit length in their contribution to texture mapping).
	 * 
	 * The 2D system is set up similarly using {@code textureVertex} data, which are expected to be normalized [0,1] UV coordinates,
	 * then scaled by image dimensions.
	 * o = textureVertex[1] * (image_width, image_height)
	 * u = (textureVertex[0] - textureVertex[1]) * (image_width, image_height)
	 * v = (textureVertex[2] - textureVertex[1]) * (image_width, image_height)
	 * u and v in 2D are also orthogonalized.
	 * 
	 * @param image The {@link BufferedImage} to use as the texture source.
	 * @param p The {@link Polygon} to which this texture will be applied. The first three vertices (indices 0, 1, 2)
	 *          are used to define the 3D texture space. It's assumed p.vertices[0], p.vertices[1], p.vertices[2]
	 *          correspond to textureVertex[0], textureVertex[1], textureVertex[2].
	 * @param textureVertex A 2D array of float values (e.g., float[3][2]) representing the (u,v) texture coordinates
	 *                      for the corresponding polygon vertices. E.g., textureVertex[0] are UVs for p.vertices[0].
	 *                      These coordinates are typically normalized (0.0 to 1.0).
	 */
 	public VertexTexture(BufferedImage image, Polygon p, float[][] textureVertex)
	{
		setImage(image); // Sets image, data, w, xmask, ymask
		
		// Setup 3D texture coordinate system (O, U, V) based on polygon vertices
		// O becomes the 3D point corresponding to textureVertex[1]
		O = p.vertices[1].point.clone(); 
		// U_world = P0_world - O_world
		U = p.vertices[0].point.clone(); 
		MyMath.subtract(U, O, U); // U = P0 - P1
		
		// V_world = P2_world - O_world
		V = p.vertices[2].point.clone();
		MyMath.subtract(V, O, V); // V = P2 - P1
		
		float[] proj_uv_on_uu = {0,0,0}; // Buffer for projection vector
		// Orthogonalize V with respect to U in 3D (Gram-Schmidt)
		// V_new = V_old - proj_U(V_old) = V_old - ( (V_old . U) / (U . U) ) * U
		if (MyMath.dotProduct(U,U) == 0) { /* Handle U is zero vector */ U[0]=1; /* Avoid div by zero, set default U */ }
		MyMath.scale(MyMath.dotProduct(V, U) / MyMath.dotProduct(U, U), U, proj_uv_on_uu);
		MyMath.subtract(V, proj_uv_on_uu, V);
		
		// Normalize U and V (inverse of squared magnitude, effectively scaling them for dot product mapping)
		// This makes U and V such that dot(X-O, U) gives a coordinate from 0 to 1 if X is P0.
		if (MyMath.dotProduct(V,V) == 0) { /* Handle V is zero vector */ V[1]=1; /* Avoid div by zero, set default V */ }
		MyMath.scale(1.0f / MyMath.dotProduct(V, V), V, V);
		MyMath.scale(1.0f / MyMath.dotProduct(U, U), U, U); // U was already scaled relative to P0-P1, this normalizes it further based on its own magnitude.
		
		// Setup 2D texture pixel coordinate system (o, u, v)
		// o_tex = TV1 (scaled to pixel dimensions)
		o = createTV(textureVertex[1]); 
		// u_tex = TV0 - TV1 (scaled)
		u = createTV(textureVertex[0]);
		u[0] -= o[0]; 
		u[1] -= o[1];
		// v_tex = TV2 - TV1 (scaled)
		v = createTV(textureVertex[2]);
		v[0] -= o[0];
		v[1] -= o[1];

		// Orthogonalize v with respect to u in 2D texture pixel space (Gram-Schmidt)
		// v_new_2d = v_old_2d - proj_u_2d(v_old_2d)
		float dot_uu_2d = (float)(u[0]*u[0] + u[1]*u[1]);
		if (dot_uu_2d == 0) { /* Handle u_2d is zero vector */ u[0]=1; dot_uu_2d=1; }
		float c_2d = ((float)(v[0]*u[0] + v[1]*u[1])) / dot_uu_2d;
		v[0] = (int) (v[0] - c_2d * u[0]);
		v[1] = (int) (v[1] - c_2d * u[1]);
		// Note: u and v in 2D are not normalized here, their magnitudes define the pixel scale.
	}
	
	/**
	 * Helper method to convert normalized texture coordinates (float array [u,v] from 0.0-1.0 range)
	 * into pixel coordinates within the texture image.
	 * @param fs A float array of two elements: {normalized_u, normalized_v}.
	 * @return An integer array of two elements: {pixel_x, pixel_y}.
	 */
	private int[] createTV(float[] fs) 
	{
		int[] tv = new int[2];
		tv[0] = (int) (fs[0] * w); // Scale normalized u by image width
		tv[1] = (int) (fs[1] * image.getHeight()); // Scale normalized v by image height
		return tv;
	}

	/**
	 * Retrieves the ARGB color from the texture image at the given pixel coordinates (x, y).
	 * Coordinates are masked using {@link #xmask} and {@link #ymask} to achieve wrapping
	 * (assuming power-of-two image dimensions for correct bitwise AND wrapping).
	 * @param x The x-coordinate in texture pixel space.
	 * @param y The y-coordinate in texture pixel space.
	 * @return The ARGB color value at the specified (and possibly wrapped) coordinates.
	 */
	@Override
	public int getColor(int x, int y)
	{
		// Apply bitmask for coordinate wrapping (texture repeat)
		// This works correctly as a modulo if width/height are powers of two.
		x &= xmask; 
		y &= ymask;
		// Access pixel data from the linearized array
		return data[w * y + x];
	}
	
	/**
	 * Sets the {@link BufferedImage} to be used as the texture source.
	 * This also updates internal fields related to the image, such as its dimensions,
	 * pixel data array, and masks for coordinate wrapping.
	 * @param image The BufferedImage to use.
	 */
	public void setImage(BufferedImage image) 
	{
		this.image = image;
		this.w = image.getWidth();
		// Assumes power-of-two dimensions for mask-based wrapping.
		// If not power-of-two, x % w and y % h would be more general for wrapping.
		this.xmask = w - 1; 
		this.ymask = image.getHeight() - 1;
		// Get direct access to the integer pixel data of the image
		this.data = ((DataBufferInt)image.getRaster().getDataBuffer()).getData();
	}
}
