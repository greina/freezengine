package com.codnyx.myengine;

/**
 * Handles perspective projection transformations from 3D eye coordinates to 2D screen coordinates,
 * and reverse unprojection. It sets up a perspective projection matrix based on camera parameters
 * like field of view, screen dimensions, and near/far clipping planes.
 */
public class PerspectiveTransformation 
{
	// Projection matrix components (simplified, specific to this implementation)
	private float a11, a13, a22, a23, a33, a34; 
	// Inverse projection components (simplified) for unprojection
	private float i11, i13, i22, i23;  
	/** The near clipping plane distance. */
	private float z_min;
	/** The far clipping plane distance. */
	private float z_max;
	
	/**
	 * Constructs a new PerspectiveTransformation.
	 * The parameters define the viewing frustum and screen mapping.
	 * 
	 * @param yangle The vertical field of view angle in radians.
	 * @param width The width of the viewport/screen in pixels.
	 * @param height The height of the viewport/screen in pixels.
	 * @param top The y-coordinate of the top edge of the viewport (typically 0).
	 * @param left The x-coordinate of the left edge of the viewport (typically 0).
	 * @param z_min The distance to the near clipping plane (must be positive).
	 * @param z_max The distance to the far clipping plane (must be greater than z_min).
	 */
	public PerspectiveTransformation(double yangle, int width, int height, int top, int left, float z_min, float z_max)
	{
		float h_2 = height/2.0f;
		float w_2 = width/2.0f; // This w_2 is distinct from the one used for z calculation later
		
		// Perspective projection matrix components (derived from standard projection matrices)
		// These seem to map to a specific coordinate system and projection logic.
		// a11 and a22 relate to focal length and aspect ratio.
		// a13 and a23 relate to principal point offset.
		this.a11 = (float) (h_2 / Math.tan(yangle / 2.0)); // More standard to use yangle/2 for cot(fov/2)
		this.a22 = -this.a11; // Assuming y points downwards in screen space or similar convention
		
		// These components a13, a23 seem to incorporate viewport mapping directly.
		// If standard projection, principal point (cx,cy) is usually width/2, height/2.
		// The formula used (-(left+w_2)) suggests 'left' might be an offset or
		// that the coordinate system for screen space is being adjusted.
		// For a typical projection mapping to NDC then viewport:
		// x_screen = (x_ndc * w_2) + (left + w_2)
		// y_screen = (y_ndc * h_2) + (top + h_2)
		// The matrix elements here seem to be part of a combined transform.
		this.a13 = -(left + w_2); 
		this.a23 = -(top + h_2); 
		
		// Depth mapping components (a33, a34)
		// These map eye-space z to a normalized depth value, then potentially to screen depth.
		// The formula 1/(z_min - z_max) for w_2_depth is unusual, usually it's 1/(z_far - z_near).
		// Let's assume z_eye is positive going into the screen.
		// Standard perspective depth: z_ndc = (A * z_eye + B) / -z_eye
		// A = (z_far + z_near) / (z_far - z_near)
		// B = (2 * z_far * z_near) / (z_far - z_near)
		// Here, a33 and a34 are used with a wc = -1/eye_coord[2] factor later.
		// So, depth = (a33 * z_eye + a34) * (-1/z_eye) = -a33 - a34/z_eye
		float w_2_depth_calc = 1.0f / (this.z_min - this.z_max); // This is 1/-(z_max - z_min)
		this.a33 = -this.z_min * w_2_depth_calc; // So a33 = z_min / (z_max - z_min)
		this.a34 = this.z_max * this.z_min * w_2_depth_calc; // So a34 = - (z_max * z_min) / (z_max - z_min)
		// If depth_out = (a33*z_eye + a34) * (-1/z_eye)
		// depth_out = (-z_min/(z_max-z_min)) + (z_max*z_min)/((z_max-z_min)*z_eye)
		// This mapping needs careful interpretation for z-buffer values (e.g. [0,1] or [-1,1]).

		// Inverse components for unprojection (screen to eye)
		// These would be derived from inverting the x and y projection steps.
		// x_screen = (a11*x_eye + a13*z_eye) * (-1/z_eye) => x_screen = -a11*(x_eye/z_eye) - a13
		// x_eye/z_eye = (-x_screen - a13) / a11
		// x_eye = z_eye * (-x_screen/a11 - a13/a11)
		// So i11 = -1/a11 and i13 = -a13/a11. The signs in the code might differ based on conventions.
		this.i11 = 1.0f / this.a11;
		this.i13 = -this.a13 / this.a11;
		this.i22 = 1.0f / this.a22;
		this.i23 = -this.a23 / this.a22;
		
		this.z_max = z_max;
		this.z_min = z_min;
	}
	
	/**
	 * Projects 3D eye coordinates to 2D screen coordinates and calculates depth.
	 * Eye coordinates are assumed to be in a system where -Z is into the screen.
	 * The projection involves a perspective divide by -eye_coord[2] (-Z_eye).
	 * 
	 * @param eye_coord The input 3D point in eye space {x_eye, y_eye, z_eye}. z_eye should be negative for points in front of the camera.
	 * @param result An integer array {x_screen, y_screen} to store the 2D screen coordinates.
	 * @return The calculated depth value, typically for Z-buffering. The range of this depth depends on the matrix setup.
	 */
	public final float project(float[] eye_coord, int[] result)
	{
		if (eye_coord[2] == 0) { // Avoid division by zero; point is on the eye plane
			// Handle this case: either return an error, a very large/small value, or specific screen coords
			result[0] = Integer.MAX_VALUE; // Or some other indicator of an issue
			result[1] = Integer.MAX_VALUE;
			return Float.POSITIVE_INFINITY; // Or an appropriate depth error value
		}
		float wc = -1.0f / eye_coord[2]; // Perspective divide factor (w_clip = -z_eye)
		result[0] = (int) ((a11 * eye_coord[0] + a13 * eye_coord[2]) * wc);
		result[1] = (int) ((a22 * eye_coord[1] + a23 * eye_coord[2]) * wc);
		return (a33 * eye_coord[2] + a34) * wc; // Calculated depth value
	}
	
	/**
	 * Checks if a given depth value is visible with respect to a Z-buffer value.
	 * Assumes depth values increase further into the scene.
	 * A point is visible if its depth is less than the current zBuffer value and non-negative.
	 * 
	 * @param depth The depth of the point being tested.
	 * @param zBuffer The current value in the Z-buffer at that screen location.
	 * @return True if the point is visible (should be drawn and zBuffer updated), false otherwise.
	 */
	public static final boolean isVisible(float depth, float zBuffer)
	{
		// Assumes depth is typically [0, far_val] where 0 is near plane.
		// And zBuffer stores the depth of the closest object found so far.
		return depth < zBuffer && depth >= 0f; 
	}
	
	/**
	 * Unprojects 2D screen coordinates (with a given Z depth in eye space) back to 3D eye coordinates.
	 * 
	 * @param screen_coord The input 2D screen coordinates {x_screen, y_screen}.
	 * @param z The Z depth of the point in eye space (should be negative for points in front of camera).
	 * @param result A float array {x_eye, y_eye, z_eye} to store the unprojected 3D point.
	 */
	public void unproject(int[] screen_coord, float z, float[] result)
	{
		 // Based on derivation: x_eye = z_eye * (-x_screen/a11 - a13/a11)
		 // result[0] = z * (i11 * screen_coord[0] + i13); // if i11 = -1/a11
		 // The original code has -z * (i11*sc[0] + i13).
		 // If z is negative (eye_coord[2]), then -z is positive.
		 // x_eye = (-z_eye) * ( (1/a11)*screen_coord[0] + (-a13/a11) )
		 // x_eye = (-z_eye/a11) * (screen_coord[0] - a13)
		 // This matches the derivation if z (input) is eye_coord[2] (negative).
		 result[0] = -z * (i11 * screen_coord[0] + i13);
		 result[1] = -z * (i22 * screen_coord[1] + i23);
		 result[2] = z; // z_eye is directly given
	}

	/**
	 * This method name 'aTrasform' is unclear. It seems to perform some transformation
	 * using the inverse projection components and screen coordinates.
	 * Based on the calculation, it might be part of an unprojection step or
	 * calculating something related to view rays or screen space derivatives.
	 * (x+1) and (y+1) suggest it might be transforming pixel grid points.
	 * 
	 * Tentative interpretation: Transforms screen pixel coordinates (possibly from a corner or center convention)
	 * to a representation in the eye/camera space, perhaps on the near plane or related to view direction components.
	 * The exact meaning requires more context on its usage.
	 * 
	 * @param x The screen x-coordinate.
	 * @param y The screen y-coordinate.
	 * @param result A float array (presumably float[2] or float[3]) to store the transformed values.
	 *               The first two components are filled.
	 */
	public void aTrasform(int x, int y, float[] result) 
	{
		// If result is (x_eye/z_eye, y_eye/z_eye) for a point on screen (x,y)
		// x_eye/z_eye = i11*x_screen + i13 (if i11 = -1/a11 and i13 = -a13/a11 and screen_coord[0] = x)
		// The (x+1) and (y+1) are unusual without knowing the coordinate system origin for x,y.
		// If x,y are 0-indexed pixel coords, x+1, y+1 might refer to pixel centers or edges.
		 result[0] = (i11 * (x + 1) - i13); // This seems to be - ( (x+1)/a11 - (-a13/a11) ) = -(x+1-a13)/a11
		 result[1] = (i22 * (y + 1) - i23); // This seems to be - ( (y+1)/a22 - (-a23/a22) ) = -(y+1-a23)/a22
		 // The subtraction of i13 and i23 is different from unproject's addition.
		 // If i13 = -a13/a11, then -i13 = a13/a11.
		 // result[0] = (1/a11)*(x+1) + a13/a11 = (x+1+a13)/a11.
		 // This could be related to (x_eye * -wc / z_eye) if x_screen = (a11*x_eye + a13*z_eye)*wc
	}

	/**
	 * Gets the near clipping plane distance (z_min).
	 * @return The z_min value.
	 */
	public float getZMin()
	{
		return z_min;
	}
	
	/**
	 * Gets the far clipping plane distance (z_max).
	 * @return The z_max value.
	 */
	public float getZMax()
	{
		return z_max;
	}
	
}
