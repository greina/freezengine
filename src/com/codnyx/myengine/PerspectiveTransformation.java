package com.codnyx.myengine;

public class PerspectiveTransformation 
{
	private float a11, a13, a22, a23, a33, a34;
	private float i11, i13, i22, i23;  
	
	public PerspectiveTransformation(double yangle, int width, int height, int top, int left, float z_min, float z_max)
	{
		float h_2 = height/2f;
		float w_2 = width/2f;
		a11 = (float) (h_2/Math.tan(yangle));
		a22 = -a11;
		a13 = -(left+w_2);
		a23 = -(top+h_2);
		w_2 = 1/(z_max - z_min);
		a33 = -z_max*w_2;
		a34 = z_max*z_min*w_2;
		i11 = 1/a11;
		i13 = -a13/a11;
		i22 = 1/a22;
		i23 = -a23/a22;
	}
	
	public final float project(float[] eye_coord, int[] result)
	{
		float wc = -1/eye_coord[2];
		result[0] = (int) ((a11*eye_coord[0]+a13*eye_coord[2])*wc);
		result[1] = (int) ((a22*eye_coord[1]+a23*eye_coord[2])*wc);
		return (a33*eye_coord[2]+a34)*wc; // Depth
	}
	
	public static final boolean isVisible(float depth, float zBuffer)
	{
		return depth < zBuffer && depth >= 0f;
	}
	
	public void unproject(int[] screen_coord, float z, float[] result)
	{
		 result[0] = -z*(i11*screen_coord[0] + i13);
		 result[1] = -z*(i22*screen_coord[1] + i23);
	}

	public void aTrasform(int x, int y, float[] result) 
	{
		 result[0] = (i11*(x+1) - i13);
		 result[1] = (i22*(y+1) - i23);
	}
	
}
