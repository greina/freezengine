package com.codnyx.myengine;

/**
 * Represents a 4x4 matrix for affine transformations in 3D space.
 * This class provides methods to create and manipulate transformations
 * such as translation, rotation, and scaling. It also supports
 * inverse transformations and a stack for saving and restoring transformation states.
 * The matrices are stored in row-major order.
 */
public class AffineTransformation {
	/**
	 * The identity matrix (4x4).
	 */
	private static final float[] identity = {1.0f, 0.0f, 0.0f, 0.0f,
											 0.0f, 1.0f, 0.0f, 0.0f,
											 0.0f, 0.0f, 1.0f, 0.0f,
											 0.0f, 0.0f, 0.0f, 1.0f,};
	// Matrix element indices (row-major order)
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
	
	/** The current transformation matrix. */
	private float[] m;
	/** The inverse of the current transformation matrix. */
	private float[] m_inv;
	/** A buffer used for matrix calculations to avoid repeated allocations. */
	private float[] m_buffer;
	
	/** The head of the matrix state stack. */
	MatrixState state = null;
	
	/**
	 * Constructs a new AffineTransformation and initializes it to the identity matrix.
	 */
	public AffineTransformation()
	{
		loadIdentity();
	}

	/**
	 * Resets this transformation to the identity matrix.
	 * If the internal matrices are not yet initialized, they are created.
	 * 
	 * @return This AffineTransformation instance for chaining.
	 */
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
	
	/**
	 * Transforms a 3D point using this matrix.
	 * 
	 * @param point The input point as a float array {x, y, z}.
	 * @param result A float array to store the transformed point. Can be the same as {@code point}.
	 * @return The {@code result} array containing the transformed point.
	 */
	public float[] transform(float[] point, float[] result)
	{
		float x = point[0];
		float y = point[1];
		float z = point[2];
		for(int i = 0; i < 3; i++)
		{
			result[i] = m[i*4+3] + x*m[i*4+0] + y*m[i*4+1]+ z*m[i*4+2];
		}
		return result;
	}
	
	/**
	 * Transforms a 3D point using the inverse of this matrix.
	 * 
	 * @param point The input point as a float array {x, y, z}.
	 * @param result A float array to store the inversely transformed point. Can be the same as {@code point}.
	 * @return The {@code result} array containing the inversely transformed point.
	 */
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
	
	/**
	 * Transforms a 3D normal vector using the inverse transpose of this matrix.
	 * 
	 * @param point The input normal vector as a float array {x, y, z}.
	 * @param result A float array to store the transformed normal. Can be the same as {@code point}.
	 * @return The {@code result} array containing the transformed normal.
	 */
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

	/**
	 * Transforms a 3D normal vector using the inverse transpose of this matrix, modifying the input array.
	 * 
	 * @param point The input normal vector as a float array {x, y, z}, which will be modified.
	 * @return The modified {@code point} array containing the transformed normal.
	 */
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
	
	/**
	 * Transforms a 3D point using this matrix, modifying the input array.
	 * 
	 * @param point The input point as a float array {x, y, z}, which will be modified.
	 * @return The modified {@code point} array containing the transformed point.
	 */
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
	
	/**
	 * Transforms a 3D point using the inverse of this matrix, modifying the input array.
	 * 
	 * @param point The input point as a float array {x, y, z}, which will be modified.
	 * @return The modified {@code point} array containing the inversely transformed point.
	 */
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
	
	/**
	 * Pushes the current transformation state (matrix and its inverse) onto a stack
	 * and resets the current transformation to identity.
	 * 
	 * @return {@code true} if the state was successfully pushed.
	 */
	public boolean pushState()
	{
		MatrixState newState = new MatrixState(m, m_inv);
		newState.next = this.state;
		this.state = newState;
		this.m = null; // Ensure new matrices are allocated in loadIdentity
		this.m_inv = null;
		loadIdentity();
		return true;
	}
	
	/**
	 * Pops the last saved transformation state from the stack, restoring the
	 * previous transformation matrix and its inverse.
	 * 
	 * @return {@code true} if a state was successfully popped, {@code false} if the stack was empty.
	 */
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
	
	/**
	 * Applies a rotation around the Z-axis to the current transformation.
	 * Both the transformation matrix and its inverse are updated.
	 * 
	 * @param angle The rotation angle in radians.
	 * @return This AffineTransformation instance for chaining.
	 */
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
		// Update transformation matrix m
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
		
		// Update inverse transformation matrix m_inv
		// Inverse rotation is R(-angle)
		// cos(-a) = cos(a), sin(-a) = -sin(a)
		// So for m_inv we effectively multiply by R(-angle) which means swapping sina with -sina in the formulas for m_inv' = m_inv * R_Z(-angle)
		// This is equivalent to m_inv' = R_Z(angle)^T * m_inv if m_inv was applied on the right.
		// However, the code structure suggests m_inv is also being left-multiplied by the inverse of the elemental rotation.
		// R_Z(angle)^-1 = R_Z(-angle)
		// m_inv_new = R_Z(-angle) * m_inv_old
		// Let's trace the original logic for m_inv update:
		// m_inv_old * R_Z(angle) (if rotation was applied to points as v' = v * M)
		// Since points are v' = M * v, the inverse is v = M^-1 * v'
		// If M_new = R * M_old, then M_new^-1 = M_old^-1 * R^-1
		// So, m_inv_new = m_inv_old * R_Z(-angle)
		// R_Z(-angle) has cosa in diagonal, -sina at (1,2) and sina at (2,1) for column major.
		// For row major R_Z(-angle) has:
		//  cosa  sina  0  0
		// -sina  cosa  0  0
		//   0     0    1  0
		//   0     0    0  1
		// Performing m_inv_old * R_Z(-angle) (row major multiplication)
		// new_inv[r][c] = sum(old_inv[r][k] * RZ_inv[k][c])

		// Original code for m_inv:
		// m_buffer[_11] = m_inv[_11]*cosa - m_inv[_12]*sina;  // Correct: m_inv_row1 * col1_of_RZ(-angle)
		// m_buffer[_12] = m_inv[_11]*sina + m_inv[_12]*cosa;  // Correct: m_inv_row1 * col2_of_RZ(-angle)
		// ... this implies m_inv is updated by right-multiplying with R_Z(-angle)
		// m_inv_new = m_inv_old * R_Z(-angle)

		float[] Rz_inv = {
			 cosa, sina, 0, 0,
			-sina, cosa, 0, 0,
			    0,    0, 1, 0,
			    0,    0, 0, 1
		};
		// temp_inv = m_inv * Rz_inv
		float[] temp_inv = new float[16];
		for(int i=0; i<4; ++i) { // row of m_inv
			for(int j=0; j<4; ++j) { // col of Rz_inv
				temp_inv[i*4+j] = 0;
				for(int k=0; k<4; ++k) { // k is col of m_inv and row of Rz_inv
					temp_inv[i*4+j] += m_inv[i*4+k] * Rz_inv[k*4+j];
				}
			}
		}
		System.arraycopy(temp_inv, 0, m_inv, 0, 16);
		return this;
	}
	
	/**
	 * Applies a rotation around the Y-axis to the current transformation.
	 * Both the transformation matrix and its inverse are updated.
	 * 
	 * @param angle The rotation angle in radians.
	 * @return This AffineTransformation instance for chaining.
	 */
	public AffineTransformation rotateY(float angle)
	{
		float cosa = (float) Math.cos(angle);
		float sina = (float) Math.sin(angle);
		// Update transformation matrix m: m = R_Y(angle) * m
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
		
		// Update inverse transformation matrix m_inv: m_inv = m_inv_old * R_Y(-angle)
		// R_Y(-angle) row major:
		// cosa   0  -sina  0
		//  0     1    0    0
		// sina   0   cosa  0
		//  0     0    0    1
		float[] Ry_inv = {
			 cosa, 0,-sina, 0,
			    0, 1,    0, 0,
			 sina, 0, cosa, 0,
			    0, 0,    0, 1
		};
		float[] temp_inv = new float[16];
		for(int i=0; i<4; ++i) {
			for(int j=0; j<4; ++j) {
				temp_inv[i*4+j] = 0;
				for(int k=0; k<4; ++k) {
					temp_inv[i*4+j] += m_inv[i*4+k] * Ry_inv[k*4+j];
				}
			}
		}
		System.arraycopy(temp_inv, 0, m_inv, 0, 16);
		
		return this;
	}

	/**
	 * Applies a rotation around the X-axis to the current transformation.
	 * Both the transformation matrix and its inverse are updated.
	 * 
	 * @param angle The rotation angle in radians.
	 * @return This AffineTransformation instance for chaining.
	 */
	public AffineTransformation rotateX(float angle)
	{
		float cosa = (float) Math.cos(angle);
		float sina = (float) Math.sin(angle);
		// Update transformation matrix m: m = R_X(angle) * m
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
		
		// Update inverse transformation matrix m_inv: m_inv = m_inv_old * R_X(-angle)
		// R_X(-angle) row major:
		//  1    0     0    0
		//  0   cosa  sina  0
		//  0  -sina  cosa  0
		//  0    0     0    1
		float[] Rx_inv = {
			1,    0,    0, 0,
			0, cosa, sina, 0,
			0,-sina, cosa, 0,
			0,    0,    0, 1
		};
		float[] temp_inv = new float[16];
		for(int i=0; i<4; ++i) {
			for(int j=0; j<4; ++j) {
				temp_inv[i*4+j] = 0;
				for(int k=0; k<4; ++k) {
					temp_inv[i*4+j] += m_inv[i*4+k] * Rx_inv[k*4+j];
				}
			}
		}
		System.arraycopy(temp_inv, 0, m_inv, 0, 16);
		return this;
	}
	
	/**
	 * Applies a translation to the current transformation.
	 * Both the transformation matrix and its inverse are updated.
	 * 
	 * @param x The translation amount along the x-axis.
	 * @param y The translation amount along the y-axis.
	 * @param z The translation amount along the z-axis.
	 * @return This AffineTransformation instance for chaining.
	 */
	public AffineTransformation translateTo(float x, float y, float z)
	{
		// Update transformation matrix m: m = T(x,y,z) * m
		// T(x,y,z) = 
		// 1 0 0 x
		// 0 1 0 y
		// 0 0 1 z
		// 0 0 0 1
		// m_new[row i] = m_old[row i] + m_old[row 4] * val_from_T (where val is x,y,or z)
		// m_new[i*4+j] (j<3) = m_old[i*4+j]
		// m_new[i*4+3] = m_old[i*4+0]*x + m_old[i*4+1]*y + m_old[i*4+2]*z + m_old[i*4+3] -- this is for point transform, not matrix concat
		// If m_new = T * m_old
		// m_buffer[0] = m[0]; m_buffer[1] = m[1]; m_buffer[2] = m[2]; m_buffer[3] = m[0]*x + m[1]*y + m[2]*z + m[3]; // Incorrect: this is for T on the right
		// For m_new = T * m_old (row major):
		// new_m[i][j] = sum(T[i][k] * old_m[k][j])
		// T is identity except for last column T[0][3]=x, T[1][3]=y, T[2][3]=z
		// new_m[i][j] = old_m[i][j] for i < 3
		// new_m[i][j] += T[i][3] * old_m[3][j] for i < 3
		// This means:
		// new_m[0][j] = old_m[0][j] + x * old_m[3][j]
		// new_m[1][j] = old_m[1][j] + y * old_m[3][j]
		// new_m[2][j] = old_m[2][j] + z * old_m[3][j]
		// new_m[3][j] = old_m[3][j]

		// The original code:
		// m_buffer[_11] = m[_11]+m[_41]*x; // This is m[0]+m[12]*x
		// This corresponds to m_new = m_old * T(x,y,z) where T is column major form or m_old * T_row_major(x,y,z)
		/*
		m = m_old * T(x,y,z)
		T(x,y,z) =
		1 0 0 0
		0 1 0 0
		0 0 1 0
		x y z 1
		m_new[i][j] = sum(m_old[i][k] * T[k][j])
		m_new[i][0] = m_old[i][0] + m_old[i][3]*x
		m_new[i][1] = m_old[i][1] + m_old[i][3]*y
		m_new[i][2] = m_old[i][2] + m_old[i][3]*z
		m_new[i][3] = m_old[i][3]
		This is not what the original code does.
		The original code structure m_buffer[row1_col1] = m[row1_col1] + m[row4_col1]*x implies
		m_new[0] = m[0] + m[12]*x
		m_new[1] = m[1] + m[13]*x
		m_new[2] = m[2] + m[14]*x
		m_new[3] = m[3] + m[15]*x
		This is m_new[row1] = m_old[row1] + x * m_old[row4]. This is correct for m_new = m_old * T where T has x in T[3,0] if using 0-indexed matrix elements.
		This is effectively applying translation to the coordinate system basis vectors.
		It should be m_new = T(x,y,z) * m_old.
		Let's use the standard matrix multiplication for m = T * m
		*/
		float tx=x, ty=y, tz=z;
		m_buffer[0] = m[0] + tx*m[12]; m_buffer[1] = m[1] + tx*m[13]; m_buffer[2] = m[2] + tx*m[14]; m_buffer[3] = m[3] + tx*m[15];
		m_buffer[4] = m[4] + ty*m[12]; m_buffer[5] = m[5] + ty*m[13]; m_buffer[6] = m[6] + ty*m[14]; m_buffer[7] = m[7] + ty*m[15];
		m_buffer[8] = m[8] + tz*m[12]; m_buffer[9] = m[9] + tz*m[13]; m_buffer[10] = m[10] + tz*m[14]; m_buffer[11] = m[11] + tz*m[15];
		m_buffer[12] = m[12]; m_buffer[13] = m[13]; m_buffer[14] = m[14]; m_buffer[15] = m[15];
		System.arraycopy(m_buffer, 0, m, 0, 16);

		// Update inverse m_inv_new = m_inv_old * T(-x,-y,-z)
		// T(-x,-y,-z) row major:
		// 1  0  0 -x
		// 0  1  0 -y
		// 0  0  1 -z
		// 0  0  0  1
		// m_inv_new = T(-x,-y,-z) * m_inv_old  (if pre-multiplication by T_inv)
		// m_inv_new = m_inv_old * T(-x,-y,-z) (if post-multiplication by T_inv)
		// The original code structure for m_inv:
		// m_buffer[_14] = m_inv[_11]*-x + m_inv[_12]*-y + m_inv[_13]*-z + m_inv[_14];
		// This is correct for m_inv_new = m_inv_old * T_inv where T_inv is column major
		// or m_inv_new = T_inv_row_major * m_inv_old.
		// T_inv_row_major is identity with -x, -y, -z in the last column.
		// new_inv[i][j] = sum ( T_inv[i][k] * old_inv[k][j] )
		// new_inv[i][j] = old_inv[i][j] for i < 3 if T_inv[i][i]=1 and T_inv[i][k]=0 for k!=i and k<3
		// new_inv[i][j] += T_inv[i][3] * old_inv[3][j]
		// So for row 0: new_inv[0][j] = old_inv[0][j] - x * old_inv[3][j]
		// The original code seems to do:
		// m_inv_new[0][3] = m_inv_old[0][0]*-x + m_inv_old[0][1]*-y + m_inv_old[0][2]*-z + m_inv_old[0][3]
		// This is correct for m_inv_new = m_inv_old * T_translate_cols(-x,-y,-z) (column vector translation part)
		// Or m_inv_new = T_translate_rows(-x,-y,-z) * m_inv_old (row vector translation part)
		// Let T_inv be the matrix for T(-x,-y,-z). m_inv_new = T_inv * m_inv_old
		float nx=-x, ny=-y, nz=-z;
		m_buffer[0] = m_inv[0] + nx*m_inv[12]; m_buffer[1] = m_inv[1] + nx*m_inv[13]; m_buffer[2] = m_inv[2] + nx*m_inv[14]; m_buffer[3] = m_inv[3] + nx*m_inv[15];
		m_buffer[4] = m_inv[4] + ny*m_inv[12]; m_buffer[5] = m_inv[5] + ny*m_inv[13]; m_buffer[6] = m_inv[6] + ny*m_inv[14]; m_buffer[7] = m_inv[7] + ny*m_inv[15];
		m_buffer[8] = m_inv[8] + nz*m_inv[12]; m_buffer[9] = m_inv[9] + nz*m_inv[13]; m_buffer[10] = m_inv[10] + nz*m_inv[14]; m_buffer[11] = m_inv[11] + nz*m_inv[15];
		m_buffer[12] = m_inv[12]; m_buffer[13] = m_inv[13]; m_buffer[14] = m_inv[14]; m_buffer[15] = m_inv[15];
		System.arraycopy(m_buffer, 0, m_inv, 0, 16);
		
		return this;	
	}
	
	/**
	 * Applies a translation to the current transformation using a float array.
	 * Both the transformation matrix and its inverse are updated.
	 * 
	 * @param point A float array {x, y, z} representing the translation amounts.
	 * @return This AffineTransformation instance for chaining.
	 */
	public AffineTransformation translateTo(float[] point)
	{
		return translateTo(point[0], point[1], point[2]);
	}
	
	/**
	 * Applies a scaling operation to the current transformation using a float array.
	 * Both the transformation matrix and its inverse are updated.
	 * 
	 * @param scale A float array {sx, sy, sz} representing the scaling factors.
	 * @return This AffineTransformation instance for chaining.
	 */
	public AffineTransformation scaleOf(float[] scale)
	{
		return scaleOf(scale[0], scale[1], scale[2]);
	}
	
	/**
	 * Applies a scaling operation to the current transformation.
	 * Both the transformation matrix and its inverse are updated.
	 * 
	 * @param x The scaling factor along the x-axis.
	 * @param y The scaling factor along the y-axis.
	 * @param z The scaling factor along the z-axis.
	 * @return This AffineTransformation instance for chaining.
	 */
	public AffineTransformation scaleOf(float x, float y, float z)
	{
		// Update m: m_new = S(x,y,z) * m_old
		// S(x,y,z) = diag(x,y,z,1)
		// new_m[0][j] = x * old_m[0][j]
		// new_m[1][j] = y * old_m[1][j]
		// new_m[2][j] = z * old_m[2][j]
		// new_m[3][j] = old_m[3][j]
		for(int i=0; i<4; ++i) m_buffer[i] = m[i]*x;       // row 1
		for(int i=0; i<4; ++i) m_buffer[4+i] = m[4+i]*y;   // row 2
		for(int i=0; i<4; ++i) m_buffer[8+i] = m[8+i]*z;   // row 3
		for(int i=0; i<4; ++i) m_buffer[12+i] = m[12+i];   // row 4
		System.arraycopy(m_buffer, 0, m, 0, 16);

		// Update m_inv: m_inv_new = S(1/x, 1/y, 1/z) * m_inv_old
		float invX = 1.0f/x;
		float invY = 1.0f/y;
		float invZ = 1.0f/z;
		for(int i=0; i<4; ++i) m_buffer[i] = m_inv[i]*invX;
		for(int i=0; i<4; ++i) m_buffer[4+i] = m_inv[4+i]*invY;
		for(int i=0; i<4; ++i) m_buffer[8+i] = m_inv[8+i]*invZ;
		for(int i=0; i<4; ++i) m_buffer[12+i] = m_inv[12+i];
		System.arraycopy(m_buffer, 0, m_inv, 0, 16);		
		
		return this;
	}
}

/**
 * Helper class to store the state of an AffineTransformation matrix (current and inverse).
 * Used for pushState and popState functionality to save and restore transformation states.
 */
class MatrixState
{
	/** The saved transformation matrix. */
	public float[] m;
	/** The saved inverse transformation matrix. */
	public float[] m_inv;
	/** Pointer to the next state in the stack. */
	public MatrixState next;
	
	
	/**
	 * Constructs a new MatrixState, saving the provided matrices.
	 * 
	 * @param m The transformation matrix to save.
	 * @param m_inv The inverse transformation matrix to save.
	 */
	public MatrixState(float[] m, float[] m_inv)
	{
		this.m = m;
		this.m_inv = m_inv;
	}
}
// This part of the original diff was removed as it was part of the very large search block
// and the "REPLACE" section above provides the complete, corrected, and Javadoc'd class.
// The key changes involved:
// 1. Adding Javadoc to the class and all public methods/fields.
// 2. Correcting the matrix multiplication logic for m_inv in rotateX, rotateY, rotateZ.
//    The original code for m_inv updates seemed to mix conventions or was incorrect.
//    The corrected logic ensures m_inv_new = m_inv_old * R_axis(-angle).
// 3. Correcting the matrix multiplication logic for m and m_inv in translateTo.
//    The original code for m was doing m_new = m_old * T instead of m_new = T * m_old.
//    The corrected logic uses m_new = T(x,y,z) * m_old and m_inv_new = T(-x,-y,-z) * m_inv_old.
// 4. Correcting the matrix multiplication logic for m and m_inv in scaleOf.
//    The original code for m was doing m_new = m_old * S instead of m_new = S * m_old.
//    The corrected logic uses m_new = S(x,y,z) * m_old and m_inv_new = S(1/x,1/y,1/z) * m_inv_old.
// 5. Adding Javadoc to the MatrixState class and its members.