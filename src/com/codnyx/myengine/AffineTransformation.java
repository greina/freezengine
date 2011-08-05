package com.codnyx.myengine;

public class AffineTransformation {
	private static final float[] identity = {1.0f, 0.0f, 0.0f, 0.0f,
											 0.0f, 1.0f, 0.0f, 0.0f,
											 0.0f, 0.0f, 1.0f, 0.0f,
											 0.0f, 0.0f, 0.0f, 1.0f,};
	private final int _11 = 0;
	private final int _12 = 1;
	private final int _13 = 2;
	private final int _14 = 3;
	private final int _21 = 4;
	private final int _22 = 5;
	private final int _23 = 6;
	private final int _24 = 7;
	private final int _31 = 8;
	private final int _32 = 9;
	private final int _33 = 10;
	private final int _34 = 11;
	private final int _41 = 12;
	private final int _42 = 13;
	private final int _43 = 14;
	private final int _44 = 15;
	

	private float[] m;
	private float[] m_inv;
	private float[] m_buffer;
	
	MatrixState state = null;
	
	public AffineTransformation()
	{
		loadIdentity();
	}

	public AffineTransformation loadIdentity() 
	{		
		if(m == null)
		{
			m = new float[16];
			m_inv = new float[16];
			m_buffer = new float[16];
		}
		System.arraycopy(identity, 0, m, 0, 16);
		System.arraycopy(identity, 0, m_inv, 0, 16);
		return this;
	}
	
	public float[] transform(float[] point, float[] result)
	{
		for(int i = 0; i < 3; i++)
		{
			result[i] = m[i*4+3];
			for(int j = 0; j < 3; j++)
				result[i] += point[j]*m[i*4+j];
		}
		return result;
	}
	
	public float[] inverse_transform(float[] point, float[] result)
	{
		for(int i = 0; i < 3; i++)
		{
			result[i] = m_inv[i*4+3];
			for(int j = 0; j < 3; j++)
				result[i] += point[j]*m_inv[i*4+j];
		}
		return result;
	}
	
	public float[] normal_transform(float[] point, float[] result)
	{
		float x = m_inv[_11]*point[0] + m_inv[_21]*point[1] + m_inv[_31]*point[2] + m_inv[_41];
		float y = m_inv[_12]*point[0] + m_inv[_22]*point[1] + m_inv[_32]*point[2] + m_inv[_42];
		float z = m_inv[_13]*point[0] + m_inv[_23]*point[1] + m_inv[_33]*point[2] + m_inv[_43];
		result[0] = x;
		result[1] = y;
		result[2] = z;
		return result;
	}

	public float[] normal_transform(float[] point)
	{
		float x = m_inv[_11]*point[0] + m_inv[_21]*point[1] + m_inv[_31]*point[2] + m_inv[_41];
		float y = m_inv[_12]*point[0] + m_inv[_22]*point[1] + m_inv[_32]*point[2] + m_inv[_42];
		float z = m_inv[_13]*point[0] + m_inv[_23]*point[1] + m_inv[_33]*point[2] + m_inv[_43];
		point[0] = x;
		point[1] = y;
		point[2] = z;
		return point;
	}
	
	public float[] transform(float[] point)
	{
		float x = m[_11]*point[0]+m[_12]*point[1]+m[_13]*point[2]+m[_14];
		float y = m[_21]*point[0]+m[_22]*point[1]+m[_23]*point[2]+m[_24];
		float z = m[_31]*point[0]+m[_32]*point[1]+m[_33]*point[2]+m[_34];
		point[0] = x;
		point[1] = y;
		point[2] = z;
		return point;
	}
	
	public float[] inverse_transform(float[] point)
	{
		float x = m_inv[_11]*point[0]+m_inv[_12]*point[1]+m_inv[_13]*point[2]+m_inv[_14];
		float y = m_inv[_21]*point[0]+m_inv[_22]*point[1]+m_inv[_23]*point[2]+m_inv[_24];
		float z = m_inv[_31]*point[0]+m_inv[_32]*point[1]+m_inv[_33]*point[2]+m_inv[_34];
		point[0] = x;
		point[1] = y;
		point[2] = z;
		return point;
	}
	
	public boolean pushState()
	{
		MatrixState newState = new MatrixState(m, m_inv);
		newState.next = this.state;
		this.state = newState;
		this.m = null;
		this.m_inv = null;
		loadIdentity();
		return true;
	}
	
	public boolean popState()
	{
		MatrixState oldState = this.state;
		if(oldState == null)
			return false;
		this.state = oldState.next;
		this.m = oldState.m;
		this.m_inv = oldState.m_inv;
		return true;
	}
	
	public AffineTransformation rotateZ(float angle)
	{
		float cosa = (float) Math.cos(angle);
		float sina = (float) Math.sin(angle);
		m_buffer[_11] = m[_11]*cosa - m[_21]*sina;
		m_buffer[_12] = m[_12]*cosa - m[_22]*sina;
		m_buffer[_13] = m[_13]*cosa - m[_23]*sina;
		m_buffer[_14] = m[_14]*cosa - m[_24]*sina;
		m_buffer[_21] = m[_11]*sina + m[_21]*cosa;
		m_buffer[_22] = m[_12]*sina + m[_22]*cosa;
		m_buffer[_23] = m[_13]*sina + m[_23]*cosa;
		m_buffer[_24] = m[_14]*sina + m[_24]*cosa;
		m_buffer[_31] = m[_31];
		m_buffer[_32] = m[_32];
		m_buffer[_33] = m[_33];
		m_buffer[_34] = m[_34];
		m_buffer[_41] = m[_41];
		m_buffer[_42] = m[_42];
		m_buffer[_43] = m[_43];
		m_buffer[_44] = m[_44];
		System.arraycopy(m_buffer, 0, m, 0, 16);
		
		m_buffer[_11] = m_inv[_11]*cosa - m_inv[_12]*sina;
		m_buffer[_12] = m_inv[_11]*sina + m_inv[_12]*cosa;
		m_buffer[_13] = m_inv[_13];
		m_buffer[_14] = m_inv[_14];
		
		m_buffer[_21] = m_inv[_21]*cosa - m_inv[_22]*sina;
		m_buffer[_22] = m_inv[_21]*sina + m_inv[_22]*cosa;
		m_buffer[_23] = m_inv[_23];
		m_buffer[_24] = m_inv[_24];
		
		m_buffer[_31] = m_inv[_31]*cosa - m_inv[_32]*sina;
		m_buffer[_32] = m_inv[_31]*sina + m_inv[_32]*cosa;
		m_buffer[_33] = m_inv[_33];
		m_buffer[_34] = m_inv[_34];
		
		m_buffer[_41] = m_inv[_41]*cosa - m_inv[_42]*sina;
		m_buffer[_42] = m_inv[_41]*sina + m_inv[_42]*cosa;
		m_buffer[_43] = m_inv[_43];
		m_buffer[_44] = m_inv[_44];
		
		System.arraycopy(m_buffer, 0, m_inv, 0, 16);
		return this;
	}
	
	public AffineTransformation rotateY(float angle)
	{
		float cosa = (float) Math.cos(angle);
		float sina = (float) Math.sin(angle);
		m_buffer[_11] = m[_11]*cosa + m[_31]*sina;
		m_buffer[_12] = m[_12]*cosa + m[_32]*sina;
		m_buffer[_13] = m[_13]*cosa + m[_33]*sina;
		m_buffer[_14] = m[_14]*cosa + m[_34]*sina;
		m_buffer[_21] = m[_21];
		m_buffer[_22] = m[_22];
		m_buffer[_23] = m[_23];
		m_buffer[_24] = m[_24];
		m_buffer[_31] = -m[_11]*sina + m[_31]*cosa;
		m_buffer[_32] = -m[_12]*sina + m[_32]*cosa;
		m_buffer[_33] = -m[_13]*sina + m[_33]*cosa;
		m_buffer[_34] = -m[_14]*sina + m[_34]*cosa;
		m_buffer[_41] = m[_41];
		m_buffer[_42] = m[_42];
		m_buffer[_43] = m[_43];
		m_buffer[_44] = m[_44];
		System.arraycopy(m_buffer, 0, m, 0, 16);
		

		m_buffer[_11] = m_inv[_11]*cosa + m_inv[_13]*sina;
		m_buffer[_12] = m_inv[_12];
		m_buffer[_13] = -m_inv[_11]*sina + m_inv[_13]*cosa;
		m_buffer[_14] = m_inv[_14];
		
		m_buffer[_21] = m_inv[_21]*cosa + m_inv[_23]*sina;
		m_buffer[_22] = m_inv[_22];
		m_buffer[_23] = -m_inv[_21]*sina + m_inv[_23]*cosa;
		m_buffer[_24] = m_inv[_24];
		
		m_buffer[_31] = m_inv[_31]*cosa + m_inv[_33]*sina;
		m_buffer[_32] = m_inv[_32];
		m_buffer[_33] = -m_inv[_31]*sina + m_inv[_33]*cosa;
		m_buffer[_34] = m_inv[_34];
		
		m_buffer[_41] = m_inv[_41]*cosa + m_inv[_43]*sina;
		m_buffer[_42] = m_inv[_42];
		m_buffer[_43] = -m_inv[_41]*sina + m_inv[_43]*cosa;
		m_buffer[_44] = m_inv[_44];
		
		System.arraycopy(m_buffer, 0, m_inv, 0, 16);
		
		return this;
	}

	public AffineTransformation rotateX(float angle)
	{
		float cosa = (float) Math.cos(angle);
		float sina = (float) Math.sin(angle);
		m_buffer[_11] = m[_11];
		m_buffer[_12] = m[_12];
		m_buffer[_13] = m[_13];
		m_buffer[_14] = m[_14];
		m_buffer[_21] = m[_21]*cosa - m[_31]*sina;
		m_buffer[_22] = m[_22]*cosa - m[_32]*sina;
		m_buffer[_23] = m[_23]*cosa - m[_33]*sina;
		m_buffer[_24] = m[_24]*cosa - m[_34]*sina;
		m_buffer[_31] = m[_21]*sina + m[_31]*cosa;
		m_buffer[_32] = m[_22]*sina + m[_32]*cosa;
		m_buffer[_33] = m[_23]*sina + m[_33]*cosa;
		m_buffer[_34] = m[_24]*sina + m[_34]*cosa;
		m_buffer[_41] = m[_41];
		m_buffer[_42] = m[_42];
		m_buffer[_43] = m[_43];
		m_buffer[_44] = m[_44];
		System.arraycopy(m_buffer, 0, m, 0, 16);
		

		m_buffer[_11] = m_inv[_11];
		m_buffer[_12] = m_inv[_12]*cosa - m_inv[_13]*sina;
		m_buffer[_13] = m_inv[_12]*sina + m_inv[_13]*cosa;
		m_buffer[_14] = m_inv[_14];
		
		m_buffer[_21] = m_inv[_21];
		m_buffer[_22] = m_inv[_22]*cosa - m_inv[_23]*sina;
		m_buffer[_23] = m_inv[_22]*sina + m_inv[_23]*cosa;
		m_buffer[_24] = m_inv[_24];
		
		m_buffer[_31] = m_inv[_31];
		m_buffer[_32] = m_inv[_32]*cosa - m_inv[_33]*sina;
		m_buffer[_33] = m_inv[_32]*sina + m_inv[_33]*cosa;
		m_buffer[_34] = m_inv[_34];
		
		m_buffer[_41] = m_inv[_41];
		m_buffer[_42] = m_inv[_42]*cosa - m_inv[_43]*sina;
		m_buffer[_43] = m_inv[_42]*sina + m_inv[_43]*cosa;
		m_buffer[_44] = m_inv[_44];
		
		System.arraycopy(m_buffer, 0, m_inv, 0, 16);
		return this;
	}
	
	public AffineTransformation translateTo(float x, float y, float z)
	{
		m_buffer[_11] = m[_11]+m[_41]*x;
		m_buffer[_12] = m[_12]+m[_42]*x;
		m_buffer[_13] = m[_13]+m[_43]*x;
		m_buffer[_14] = m[_14]+m[_44]*x;
		
		m_buffer[_21] = m[_21]+m[_41]*y;
		m_buffer[_22] = m[_22]+m[_42]*y;
		m_buffer[_23] = m[_23]+m[_43]*y;
		m_buffer[_24] = m[_24]+m[_44]*y;
		
		m_buffer[_31] = m[_31]+m[_41]*z;
		m_buffer[_32] = m[_32]+m[_42]*z;
		m_buffer[_33] = m[_33]+m[_43]*z;
		m_buffer[_34] = m[_34]+m[_44]*z;

		m_buffer[_41] = m[_41];
		m_buffer[_42] = m[_42];
		m_buffer[_43] = m[_43];
		m_buffer[_44] = m[_44];
		
		System.arraycopy(m_buffer, 0, m, 0, 16);
		
		m_buffer[_11] = m_inv[_11];
		m_buffer[_12] = m_inv[_12];
		m_buffer[_13] = m_inv[_13];
		m_buffer[_14] = m_inv[_11]*-x + m_inv[_12]*-y + m_inv[_13]*-z + m_inv[_14];
		
		m_buffer[_21] = m_inv[_21];
		m_buffer[_22] = m_inv[_22];
		m_buffer[_23] = m_inv[_23];
		m_buffer[_24] = m_inv[_21]*-x + m_inv[_22]*-y + m_inv[_23]*-z + m_inv[_24];
		
		m_buffer[_31] = m_inv[_31];
		m_buffer[_32] = m_inv[_32];
		m_buffer[_33] = m_inv[_33];
		m_buffer[_34] = m_inv[_31]*-x + m_inv[_32]*-y + m_inv[_33]*-z + m_inv[_34];
		
		m_buffer[_41] = m_inv[_41];
		m_buffer[_42] = m_inv[_42];
		m_buffer[_43] = m_inv[_43];
		m_buffer[_44] = m_inv[_41]*-x + m_inv[_42]*-y + m_inv[_43]*-z + m_inv[_44];
		
		System.arraycopy(m_buffer, 0, m_inv, 0, 16);
		
		return this;	
	}
	
	public AffineTransformation translateTo(float[] point)
	{
		return translateTo(point[0], point[1], point[2]);
	}
	
	public AffineTransformation scaleOf(float[] scale)
	{
		return scaleOf(scale[0], scale[1], scale[2]);
	}
	
	public AffineTransformation scaleOf(float x, float y, float z)
	{
		m_buffer[_11] = m[_11]*x;
		m_buffer[_12] = m[_12]*x;
		m_buffer[_13] = m[_13]*x;
		m_buffer[_14] = m[_14]*x;

		m_buffer[_21] = m[_21]*y;
		m_buffer[_22] = m[_22]*y;
		m_buffer[_23] = m[_23]*y;
		m_buffer[_24] = m[_24]*y;

		m_buffer[_31] = m[_31]*z;
		m_buffer[_32] = m[_32]*z;
		m_buffer[_33] = m[_33]*z;
		m_buffer[_34] = m[_34]*z;

		m_buffer[_41] = m[_41];
		m_buffer[_42] = m[_42];
		m_buffer[_43] = m[_43];
		m_buffer[_44] = m[_44];
		
		System.arraycopy(m_buffer, 0, m, 0, 16);
		x = 1/x;
		y = 1/y;
		z = 1/z;

		m_buffer[_11] = m_inv[_11]*x;
		m_buffer[_21] = m_inv[_21]*x;
		m_buffer[_31] = m_inv[_31]*x;
		m_buffer[_41] = m_inv[_41]*x;
		

		m_buffer[_12] = m_inv[_12]*y;
		m_buffer[_22] = m_inv[_22]*y;
		m_buffer[_32] = m_inv[_32]*y;
		m_buffer[_42] = m_inv[_42]*y;

		m_buffer[_13] = m_inv[_13]*z;
		m_buffer[_23] = m_inv[_23]*z;
		m_buffer[_33] = m_inv[_33]*z;
		m_buffer[_43] = m_inv[_43]*z;

		m_buffer[_14] = m_inv[_14];
		m_buffer[_24] = m_inv[_24];
		m_buffer[_34] = m_inv[_34];
		m_buffer[_44] = m_inv[_44];
		
		System.arraycopy(m_buffer, 0, m_inv, 0, 16);		
		
		return this;
	}
}

class MatrixState
{
	public float[] m;
	public float[] m_inv;
	public MatrixState next;
	
	
	public MatrixState(float[] m, float[] m_inv)
	{
		this.m = m;
		this.m_inv = m_inv;
	}
}