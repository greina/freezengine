package com.codnyx.myengine;

public class MyMath
{
	/*
	 * Order - 5
	 * 3 multiplications
	 * 2 sums
	 */
	public static final float dotProduct(float[] a, float[] b)
	{
		return a[0]*b[0]+a[1]*b[1]+a[2]*b[2];
	}
	
	/*
	 * Order - 9 
	 * 6 multiplications
	 * 3 subtractions 
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
	
	public static final  float[] subtract(float[] a, float[] b, float[] result)
	{
		result[0] = a[0] - b[0];
		result[1] = a[1] - b[1];
		result[2] = a[2] - b[2];
		return result;
	}
	
	public static final  float[] add(float[] a, float[] b, float[] result)
	{
		result[0] = a[0] + b[0];
		result[1] = a[1] + b[1];
		result[2] = a[2] + b[2];
		return result;
	}

	public static final float[] multiply(float[] vec, float s, float[] result)
	{
		result[0] = vec[0]*s;
		result[1] = vec[1]*s;
		result[2] = vec[2]*s;
		return result;		
	}
	
	public static final float length(float[] vec)
	{
		return (float)Math.sqrt(vec[0]*vec[0] + vec[1]*vec[1] + vec[2]*vec[2]);
	}

	public static final float[] normalize(float[] vec)
	{
		float l = length(vec);
		vec[0] /= l;
		vec[1] /= l;
		vec[2] /= l;
		return vec;		
	}

	public static float[] scale(float c, float[] vec, float[] result) 
	{
		result[0] = c*vec[0];
		result[1] = c*vec[1];
		result[2] = c*vec[2];
		return result;
	}

	public static void init(float value, float[] vec) 
	{
		vec[0] = value;
		vec[1] = value;
		vec[2] = value;
	}

/*
 * Quaternions are 4-tuples of (i, j, k, s)
 * remember
 * i*i = j*j = k*k = -1
 * i*j = -j*i = k
 * j*k = -k*j = i
 * k*i = -i*k = j
 * 
 */
	public static final float[] createRotationQuaterion(float[] unit_axis, double angle, float[] result)
	{
		/*
		 * rot. of a around u = (cos(a/2), u*sin(a/2))
		 */
		double a = angle/2;
		double sine = Math.sin(a);
		double cosine = Math.cos(a);
		result[0] = (float) (unit_axis[0]*sine);
		result[1] = (float) (unit_axis[1]*sine);
		result[2] = (float) (unit_axis[2]*sine);
		result[3] = (float) cosine;
		return result;
	}
	
	public static final float[] createRotationQuaterion(float[] unit_axis, double angle)
	{
		return createRotationQuaterion(unit_axis, angle, new float[4]);
	}
	
	/*
	 * Order - 28
	 */
	public static final float[] quaternionProduct(float[] q1, float[] q2, float[] result)
	{
		/*
		 * It might look a bit cryptic but I inlined the cross product
		 * and the dot product to avoid function calls
		 */		
		//v1 x v2 + s1*v2 + s2*v1		
		result[0] = q1[1]*q2[2] - q1[2]*q2[1] + q1[3]*q2[0] + q2[3]*q1[0];  
		result[1] = q1[2]*q2[0] - q1[0]*q2[2] + q1[3]*q2[1] + q2[3]*q1[1];
		result[2] = q1[0]*q2[1] - q1[1]*q2[0] + q1[3]*q2[2] + q2[3]*q1[2];
		// s1*s2 - v1.v2
		result[3] = q1[3]*q2[3] - q1[0]*q2[0] - q1[1]*q2[1] - q1[2]*q2[2];
		return result;
	}
	
	public static final float[] quaternionProduct(float[] q1, float[] q2)
	{
		return quaternionProduct(q1,q2, new float[4]);
	}
	
	public static final float[] quaternionNormalize(float[] q)
	{
		float len = (float) Math.sqrt(q[0]*q[0] + q[1]*q[1]+q[2]*q[2]+q[3]*q[3]);
		q[0] /= len;
		q[1] /= len;
		q[2] /= len;
		q[3] /= len;
		return q;
	}

	public static final float quaternionLength(float[] q)
	{
		return (float) Math.sqrt(q[0]*q[0] + q[1]*q[1]+q[2]*q[2]+q[3]*q[3]);
	}
	
	public static final float[] quaternionInvert(float[] q, float[] result)
	{
		float mag = q[0]*q[0] + q[1]*q[1]+q[2]*q[2]+q[3]*q[3];
		result[0] = -q[0]/mag;
		result[1] = -q[1]/mag;
		result[2] = -q[2]/mag;
		result[3] = q[3]/mag;		
		return result;
	}

	
	public static final float[] quaternionInvert(float[] q)
	{
		float mag = q[0]*q[0] + q[1]*q[1]+q[2]*q[2]+q[3]*q[3];
		q[0] /= -mag;
		q[1] /= -mag;
		q[2] /= -mag;
		q[3] /= mag;		
		return q;
	}
	

	public static final float[] rotate(float[] q, float[] q_inverse, float[] p)
	{
		float mag = q[0]*q[0] + q[1]*q[1]+q[2]*q[2]+q[3]*q[3];
		q[0] /= -mag;
		q[1] /= -mag;
		q[2] /= -mag;
		q[3] /= mag;		
		return q;
	}

	public static boolean areEqual(float[] v1, float[] v2)
	{
		return v1[0] == v2[0] && v1[1] == v2[1] && v1[2] == v2[2];
	}
}
