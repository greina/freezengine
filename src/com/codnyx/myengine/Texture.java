package com.codnyx.myengine;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

public class Texture 
{
	int[] u, v, o;
	float[] U,V,O;
	private BufferedImage image;
	private int[] data;
	private int xmask, ymask, w;
	
	
 	public Texture(BufferedImage image, Polygon p, float[][] textureVertex)
	{
		setImage(image);		
		O = p.vertices[1].point.clone();
		U = p.vertices[0].point.clone();
		MyMath.subtract(U, O, U);
		
		V = p.vertices[2].point.clone();
		MyMath.subtract(V, O, V);
		float[] proj = {0,0,0};
		// Grahm-Smith for orthogonalize the U and V vectors
		MyMath.scale(MyMath.dotProduct(V, U)/MyMath.dotProduct(U, U), U, proj);
		MyMath.subtract(V, proj, V);
		MyMath.scale(1/MyMath.dotProduct(V, V), V, V);
		MyMath.scale(1/MyMath.dotProduct(U, U), U, U);
		
		o = createTV(textureVertex[1]);
		u = createTV(textureVertex[0]);
		u[0] -= o[0];
		u[1] -= o[1];
		v = createTV(textureVertex[2]);
		v[0] -= o[0];
		v[1] -= o[1];
		// Grahm-Smith for orthogonalize the u and v vectors
		float c = ((float)(v[0]*u[0]+v[1]*u[1]))/(u[0]*u[0]+u[1]*u[1]);
		v[0] = (int) (v[0] - c*u[0]);
		v[1] = (int) (v[1] - c*u[1]);
	}
	
	private int[] createTV(float[] fs) 
	{
		int[] tv = new int[2];
		tv[0] = (int) (fs[0]*w);
		tv[1] = (int) (fs[1]*image.getHeight());
		return tv;
	}

	public int getColor(int x, int y)
	{
		x &= xmask;
		y &= ymask;
		return data[w*y+x];
	}
	
	public void setImage(BufferedImage image) 
	{
		this.image = image;
		this.w = image.getWidth();
		this.xmask = w-1;
		this.ymask = image.getHeight() -1;
		this.data = ((DataBufferInt)image.getRaster().getDataBuffer()).getData();
	}
}
