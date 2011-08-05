package com.codnyx.myengine;

public class PerspectiveTransformation 
{
	private float a11, a13, a22, a23, a33, a34;
	
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
	}
	
	public boolean project(float[] eye_coord, int[] result)
	{
		float wc = -1/eye_coord[2];
		result[0] = (int) ((a11*eye_coord[0]+a13*eye_coord[2])*wc);
		result[1] = (int) ((a22*eye_coord[1]+a23*eye_coord[2])*wc);
		float check = (a33*eye_coord[2]+a34)*wc;
		return !(check < 0 || check > 1);
	}
	
}
