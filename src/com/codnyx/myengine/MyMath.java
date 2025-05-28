package com.codnyx.myengine;

/**
 * Provides static utility methods for 3D vector and quaternion mathematics.
 * Vectors are typically represented as float arrays of size 3 (x, y, z).
 * Quaternions are represented as float arrays of size 4 (x, y, z, w), where
 * x, y, z are the vector part and w is the scalar part.
 */
public class MyMath
{
	/**
	 * Calculates the dot product of two 3D vectors.
	 * Assumes vectors 'a' and 'b' have at least 3 elements.
	 * Computational cost: 3 multiplications, 2 additions.
	 * 
	 * @param a The first vector (float[3]).
	 * @param b The second vector (float[3]).
	 * @return The dot product of the two vectors.
	 */
	public static final float dotProduct(float[] a, float[] b)
	{
		return a[0]*b[0]+a[1]*b[1]+a[2]*b[2];
	}
	
	/**
	 * Calculates the cross product of two 3D vectors.
	 * Assumes vectors 'a', 'b', and 'result' have at least 3 elements.
	 * The result is stored in the 'result' array.
	 * Computational cost: 6 multiplications, 3 subtractions.
	 * 
	 * @param a The first vector (float[3]).
	 * @param b The second vector (float[3]).
	 * @param result The float array (float[3]) to store the resulting cross product.
	 * @return The 'result' array containing the cross product.
	 */
	public static final  float[] crossProduct(float[] a, float[] b, float[] result)
	{
		float x = a[1]*b[2] - a[2]*b[1];
		float y = a[2]*b[0] - a[0]*b[2];
		float z = a[0]*b[1] - a[1]*b[0];
		result[0] = x;
		result[1] = y;
		result[2] = z;
		return result;
	}
	
	/**
	 * Subtracts vector 'b' from vector 'a' (a - b).
	 * Assumes vectors 'a', 'b', and 'result' have at least 3 elements.
	 * The result is stored in the 'result' array.
	 * 
	 * @param a The first vector (minuend) (float[3]).
	 * @param b The second vector (subtrahend) (float[3]).
	 * @param result The float array (float[3]) to store the resulting vector.
	 * @return The 'result' array containing the difference vector.
	 */
	public static final  float[] subtract(float[] a, float[] b, float[] result)
	{
		result[0] = a[0] - b[0];
		result[1] = a[1] - b[1];
		result[2] = a[2] - b[2];
		return result;
	}
	
	/**
	 * Adds vector 'b' to vector 'a' (a + b).
	 * Assumes vectors 'a', 'b', and 'result' have at least 3 elements.
	 * The result is stored in the 'result' array.
	 * 
	 * @param a The first vector (float[3]).
	 * @param b The second vector (float[3]).
	 * @param result The float array (float[3]) to store the resulting vector.
	 * @return The 'result' array containing the sum vector.
	 */
	public static final  float[] add(float[] a, float[] b, float[] result)
	{
		result[0] = a[0] + b[0];
		result[1] = a[1] + b[1];
		result[2] = a[2] + b[2];
		return result;
	}

	/**
	 * Multiplies a 3D vector by a scalar.
	 * Assumes 'vec' and 'result' have at least 3 elements.
	 * The result is stored in the 'result' array.
	 * 
	 * @param vec The vector to multiply (float[3]).
	 * @param s The scalar value.
	 * @param result The float array (float[3]) to store the resulting scaled vector.
	 * @return The 'result' array containing the scaled vector.
	 */
	public static final float[] multiply(float[] vec, float s, float[] result)
	{
		result[0] = vec[0]*s;
		result[1] = vec[1]*s;
		result[2] = vec[2]*s;
		return result;		
	}
	
	/**
	 * Calculates the length (magnitude) of a 3D vector.
	 * Assumes 'vec' has at least 3 elements.
	 * 
	 * @param vec The vector (float[3]).
	 * @return The length of the vector.
	 */
	public static final float length(float[] vec)
	{
		return (float)Math.sqrt(vec[0]*vec[0] + vec[1]*vec[1] + vec[2]*vec[2]);
	}

	/**
	 * Normalizes a 3D vector (scales it to unit length).
	 * Assumes 'vec' has at least 3 elements. The vector is modified in place.
	 * 
	 * @param vec The vector (float[3]) to normalize.
	 * @return The 'vec' array (now normalized).
	 */
	public static final float[] normalize(float[] vec)
	{
		float l = length(vec);
		// Avoid division by zero if length is very small
		if (l > 1e-6) { // A small epsilon
			vec[0] /= l;
			vec[1] /= l;
			vec[2] /= l;
		} else {
			// Optionally set to a default vector like (0,0,0) or handle error
			vec[0] = 0;
			vec[1] = 0;
			vec[2] = 0;
		}
		return vec;		
	}

	/**
	 * Scales a 3D vector by a scalar 'c'. This is an alias for {@link #multiply(float[], float, float[])}.
	 * Assumes 'vec' and 'result' have at least 3 elements.
	 * The result is stored in the 'result' array.
	 * 
	 * @param c The scalar value to scale by.
	 * @param vec The vector to scale (float[3]).
	 * @param result The float array (float[3]) to store the resulting scaled vector.
	 * @return The 'result' array containing the scaled vector.
	 */
	public static float[] scale(float c, float[] vec, float[] result) 
	{
		result[0] = c*vec[0];
		result[1] = c*vec[1];
		result[2] = c*vec[2];
		return result;
	}

	/**
	 * Initializes all elements of a 3D vector to a specified value.
	 * Assumes 'vec' has at least 3 elements.
	 * 
	 * @param value The value to set for each component of the vector.
	 * @param vec The vector (float[3]) to initialize.
	 */
	public static void init(float value, float[] vec) 
	{
		vec[0] = value;
		vec[1] = value;
		vec[2] = value;
	}

	/**
	 * Creates a rotation quaternion from a unit axis and an angle.
	 * The quaternion is of the form (v*sin(angle/2), cos(angle/2)), where v is the unit axis.
	 * Assumes 'unit_axis' is a normalized 3D vector and 'result' is a float[4].
	 * 
	 * @param unit_axis The normalized axis of rotation (float[3]).
	 * @param angle The angle of rotation in radians.
	 * @param result The float array (float[4]) to store the resulting quaternion (x,y,z,w).
	 * @return The 'result' array containing the rotation quaternion.
	 */
	public static final float[] createRotationQuaterion(float[] unit_axis, double angle, float[] result)
	{
		double a = angle/2.0; // Ensure floating point division
		double sine = Math.sin(a);
		double cosine = Math.cos(a);
		result[0] = (float) (unit_axis[0]*sine); // x
		result[1] = (float) (unit_axis[1]*sine); // y
		result[2] = (float) (unit_axis[2]*sine); // z
		result[3] = (float) cosine;              // w (scalar part)
		return result;
	}
	
	/**
	 * Creates a new rotation quaternion from a unit axis and an angle.
	 * Allocates a new float[4] for the result.
	 * 
	 * @param unit_axis The normalized axis of rotation (float[3]).
	 * @param angle The angle of rotation in radians.
	 * @return A new float array (float[4]) containing the rotation quaternion (x,y,z,w).
	 */
	public static final float[] createRotationQuaterion(float[] unit_axis, double angle)
	{
		return createRotationQuaterion(unit_axis, angle, new float[4]);
	}
	
	/**
	 * Multiplies two quaternions (q1 * q2).
	 * Assumes q1, q2, and result are float[4] arrays (x,y,z,w).
	 * The formula used is:
	 * result.v = q1.v x q2.v + q1.w * q2.v + q2.w * q1.v
	 * result.w = q1.w * q2.w - q1.v . q2.v
	 * Computational cost (as per original comment): Order - 28 (likely operations count).
	 * 
	 * @param q1 The first quaternion (float[4]).
	 * @param q2 The second quaternion (float[4]).
	 * @param result The float array (float[4]) to store the resulting quaternion.
	 * @return The 'result' array containing the product quaternion.
	 */
	public static final float[] quaternionProduct(float[] q1, float[] q2, float[] result)
	{	
		// v1 x v2 components
		float cross_x = q1[1]*q2[2] - q1[2]*q2[1];
		float cross_y = q1[2]*q2[0] - q1[0]*q2[2];
		float cross_z = q1[0]*q2[1] - q1[1]*q2[0];
		
		// s1*v2 components
		float s1v2_x = q1[3]*q2[0];
		float s1v2_y = q1[3]*q2[1];
		float s1v2_z = q1[3]*q2[2];
		
		// s2*v1 components
		float s2v1_x = q2[3]*q1[0];
		float s2v1_y = q2[3]*q1[1];
		float s2v1_z = q2[3]*q1[2];
		
		result[0] = cross_x + s1v2_x + s2v1_x;  
		result[1] = cross_y + s1v2_y + s2v1_y;
		result[2] = cross_z + s1v2_z + s2v1_z;
		// s1*s2 - v1.v2
		result[3] = q1[3]*q2[3] - (q1[0]*q2[0] + q1[1]*q2[1] + q1[2]*q2[2]);
		return result;
	}
	
	/**
	 * Multiplies two quaternions (q1 * q2) and returns a new quaternion.
	 * Allocates a new float[4] for the result.
	 * 
	 * @param q1 The first quaternion (float[4]).
	 * @param q2 The second quaternion (float[4]).
	 * @return A new float array (float[4]) containing the product quaternion.
	 */
	public static final float[] quaternionProduct(float[] q1, float[] q2)
	{
		return quaternionProduct(q1,q2, new float[4]);
	}
	
	/**
	 * Normalizes a quaternion.
	 * Assumes 'q' is a float[4] array. The quaternion is modified in place.
	 * 
	 * @param q The quaternion (float[4]) to normalize.
	 * @return The 'q' array (now normalized).
	 */
	public static final float[] quaternionNormalize(float[] q)
	{
		float len = quaternionLength(q);
		if (len > 1e-6) { // Avoid division by zero
			q[0] /= len;
			q[1] /= len;
			q[2] /= len;
			q[3] /= len;
		} else {
			q[0] = 0; q[1] = 0; q[2] = 0; q[3] = 1; // Default to identity or handle error
		}
		return q;
	}

	/**
	 * Calculates the length (magnitude) of a quaternion.
	 * Assumes 'q' is a float[4] array.
	 * 
	 * @param q The quaternion (float[4]).
	 * @return The length of the quaternion.
	 */
	public static final float quaternionLength(float[] q)
	{
		return (float) Math.sqrt(q[0]*q[0] + q[1]*q[1]+q[2]*q[2]+q[3]*q[3]);
	}
	
	/**
	 * Calculates the inverse (conjugate divided by square of magnitude) of a quaternion.
	 * q^-1 = q* / |q|^2
	 * Assumes 'q' and 'result' are float[4] arrays.
	 * 
	 * @param q The quaternion (float[4]) to invert.
	 * @param result The float array (float[4]) to store the resulting inverted quaternion.
	 * @return The 'result' array containing the inverted quaternion.
	 */
	public static final float[] quaternionInvert(float[] q, float[] result)
	{
		float magSq = q[0]*q[0] + q[1]*q[1]+q[2]*q[2]+q[3]*q[3];
		if (magSq > 1e-9) { // Avoid division by zero
			result[0] = -q[0]/magSq;
			result[1] = -q[1]/magSq;
			result[2] = -q[2]/magSq;
			result[3] = q[3]/magSq;		
		} else {
			// Handle error or set to a default (e.g., identity quaternion)
			result[0] = 0; result[1] = 0; result[2] = 0; result[3] = 1;
		}
		return result;
	}

	
	/**
	 * Calculates the inverse of a quaternion and stores it in the input array.
	 * q^-1 = q* / |q|^2. The quaternion 'q' is modified in place.
	 * 
	 * @param q The quaternion (float[4]) to invert (modified in place).
	 * @return The 'q' array (now inverted).
	 */
	public static final float[] quaternionInvert(float[] q)
	{
		float magSq = q[0]*q[0] + q[1]*q[1]+q[2]*q[2]+q[3]*q[3];
		if (magSq > 1e-9) { // Avoid division by zero
			q[0] = -q[0]/magSq; // Corrected: was q[0] /= -magSq
			q[1] = -q[1]/magSq; // Corrected: was q[1] /= -magSq
			q[2] = -q[2]/magSq; // Corrected: was q[2] /= -magSq
			q[3] = q[3]/magSq;		
		} else {
			q[0] = 0; q[1] = 0; q[2] = 0; q[3] = 1;
		}
		return q;
	}
	

	/**
	 * Rotates a 3D point 'p' using a unit quaternion 'q' and its inverse 'q_inverse'.
	 * The rotation is performed as p' = q * p * q_inverse, where p is treated as a pure quaternion (0, p_vec).
	 * This method seems to have an issue in its implementation as it recalculates the inverse of 'q'
	 * and modifies 'q' instead of 'p'. It should be p_rotated = q * (0,p) * q_inv.
	 * 
	 * @param q The unit rotation quaternion (float[4]).
	 * @param q_inverse The inverse of the quaternion 'q' (float[4]). (Currently unused effectively due to re-inversion of q)
	 * @param p The 3D point/vector to rotate (float[3]), treated as (p[0], p[1], p[2], 0) for quaternion multiplication.
	 * @return The input quaternion 'q' (which has been modified to its inverse in the current implementation), not the rotated point.
	 *         WARNING: This method needs correction to return the rotated point and use q_inverse properly.
	 */
	public static final float[] rotate(float[] q, float[] q_inverse, float[] p)
	{
		// Current implementation re-inverts q and returns it, which is incorrect for rotating point p.
		// This method needs to be rewritten to correctly perform p' = q * p_quat * q_inv
		// where p_quat = (p[0], p[1], p[2], 0).
		
		// Example of how it should be structured (conceptual):
		// float[] p_quat = {p[0], p[1], p[2], 0};
		// float[] temp_q = quaternionProduct(q, p_quat, new float[4]);
		// float[] rotated_p_quat = quaternionProduct(temp_q, q_inverse, new float[4]);
		// return new float[]{rotated_p_quat[0], rotated_p_quat[1], rotated_p_quat[2]};

		// The original implementation is flawed for its stated purpose of rotating p.
		// It inverts q and returns q.
		float mag = q[0]*q[0] + q[1]*q[1]+q[2]*q[2]+q[3]*q[3];
		if (mag > 1e-9) {
			q[0] /= -mag;
			q[1] /= -mag;
			q[2] /= -mag;
			q[3] /= mag;
		} else {
			// handle error
			q[0]=0; q[1]=0; q[2]=0; q[3]=1;
		}
		return q; // Returns the modified (inverted) q, not the rotated point p.
	}

	/**
	 * Checks if two 3D vectors are equal by comparing their corresponding components.
	 * Assumes v1 and v2 are float[3].
	 * 
	 * @param v1 The first vector.
	 * @param v2 The second vector.
	 * @return {@code true} if all components are equal, {@code false} otherwise.
	 */
	public static boolean areEqual(float[] v1, float[] v2)
	{
		return v1[0] == v2[0] && v1[1] == v2[1] && v1[2] == v2[2];
	}

	/**
	 * Returns a string representation of a 3D vector, formatted to four decimal places.
	 * Assumes v is float[3].
	 * 
	 * @param v The vector.
	 * @return A string in the format "(x.xxxx,y.yyyy,z.zzzz)".
	 */
	public static String toString(float[] v)
	{
		return String.format("(%.4f,%.4f,%.4f)", v[0], v[1], v[2]);
	}
}
