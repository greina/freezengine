package com.codnyx.myengine;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.awt.image.ImageObserver;
import java.util.Arrays;

public class PolygonRenderer 
{
	Scan[] scanLines;
	private int height;
	private int width;
	private int top;
	private int left;
	private int yMinBound;
	private int yMaxBound;
	private PerspectiveTransformation projT;
	private AffineTransformation modelT;
	private BufferedImage image;
	private float[] zBuffer;
	
	public PolygonRenderer(int width, int height)
	{
		init(0, 0, width, height);
	}
	
	public PolygonRenderer(int left, int top, int width, int height)
	{
		init(left, top, width, height);
	}
	
	public void setPerspective(double yangle, float z_min, float z_max)
	{
		this.projT = new PerspectiveTransformation(yangle, width, height, top, left, z_min, z_max);
		
	}

	private void init(int left, int top, int width, int height) 
	{
		this.left = left;
		this.top = top;
		this.width = width;
		this.height = height;
		this.scanLines = new Scan[this.height];
		for(int i = 0; i < scanLines.length; i++)
			this.scanLines[i] = new Scan();
		this.modelT = new AffineTransformation();
		this.image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		this.zBuffer = new float[width*height];
		
	}
	
	public AffineTransformation getModelT() {
		return modelT;
	}
	
	public PerspectiveTransformation getProjT() {
		return projT;
	}
	
	public void reset()
	{
		this.yMinBound = Integer.MAX_VALUE;
		this.yMaxBound = Integer.MIN_VALUE;
	}

	float[] vecBuffer = {0,0,0};
	float[] vecBuffer2 = {0,0,0};
	float[] vecBuffer3 = {0,0,0};
	private ImageObserver observer = new ImageObserver() {
		@Override
		public boolean imageUpdate(Image img, int infoflags, int x, int y,
				int width, int height) {
			return false;
		}
	};
	private int[] bounds = {Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE};
	
	public void render(Graphics gc, Polygon p)
	{
		float[] tnormal = {0,0,0}, tu = null, tv = null, to = null;
		int a1,r1,g1,b1,a0,r0,g0,b0,n;
		a1 = r1 = g1 = b1 = a0 = r0 = g0 = b0 = n = 0;
		MutableColor colorBuffer = new MutableColor(0);
		
		int step_fp;
		/*
		 * Backface culling
		 */
		modelT.normal_transform(p.normal, tnormal);
		modelT.transform(p.center,vecBuffer2);
		if(MyMath.dotProduct(tnormal, vecBuffer2)>=0)
			return;
		reset();
		
		int[] data = ((DataBufferInt)image.getRaster().getDataBuffer()).getData();
		
		boolean textureEnabled = p.texture != null;
		
		if(textureEnabled)
		{
			to = p.texture.O.clone();
			modelT.transform(to);
			tu = p.texture.U.clone();
			modelT.normal_transform(tu);
			tv = p.texture.V.clone();
			modelT.normal_transform(tv);
		}
		
		
		/*
		 * Rendering
		 */
		int[] p1;
		int[] p0;
		int i = 0;
		
		modelT.transform(p.vertices[i].point,vecBuffer);
		p.vertices[i].depth = projT.project(vecBuffer, p.vertices[i].projection);
		modelT.normal_transform(p.vertices[i].normal, p.vertices[i].tnormal);
		i++;
		
		/*
		 * Scan all the edges in the polygon
		 */
		for(; i <= p.vertices.length; i++)
		{
			Vertex cur = p.vertices[i%p.vertices.length];
			Vertex prev = p.vertices[i-1];
			if(cur != p.vertices[0])
			{
				modelT.transform(cur.point,vecBuffer);
				cur.depth = projT.project(vecBuffer, cur.projection);
				modelT.normal_transform(cur.normal, cur.tnormal);
			}

			int yy = prev.projection[1];
			yy = yy>=0?(yy<height?yy:(height-1)):0;
			int xx = prev.projection[0];
			xx = xx>=0?(xx<width?xx:(width-1)):0;
			float zb0 = zBuffer[yy*width+xx];
			
			yy = cur.projection[1];
			yy = yy>=0?(yy<height?yy:(height-1)):0;
			xx = cur.projection[0];
			xx = xx>=0?(xx<width?xx:(width-1)):0;
			float zb1 = zBuffer[yy*width+xx];
			
			zb0 = prev.depth;
			zb1 = cur.depth;
			// Modify y bounds to include the new vertices
			correctYBounds(cur);
			correctYBounds(prev);
			
			int y_start, y_end;
			Vertex c0;
			Vertex c1;
			if(cur.projection[1] > prev.projection[1])
			{
				c0 = prev;
				c1 = cur;
			}
			else
			{
				c0 = cur;
				c1 = prev;
				float temp = zb0;
				zb0 = zb1;
				zb1 = temp;
			}

			y_end = c1.projection[1];
			y_start = c0.projection[1];
			p1 = c1.projection;
			p0 = c0.projection;
			
			int m_fp = FixedPoint16.FromFloat(((float)p1[0] - p0[0])/((float)p1[1] - p0[1]));
			n = y_end-y_start+1;
			step_fp = FixedPoint16.FromFloat(1.0f/n);
			
			if(!textureEnabled)
			{
				a1 = c1.color.getAlpha();
				r1 = c1.color.getRed();
				g1 = c1.color.getGreen();
				b1 = c1.color.getBlue();
	
				a0 = c0.color.getAlpha();
				r0 = c0.color.getRed();
				g0 = c0.color.getGreen();
				b0 = c0.color.getBlue();
			}

			if(p1[1] == p0[1])
			{
				y_start = y_start>0?y_start:0;
				y_start = y_start<height?y_start:height-1;
				if(!textureEnabled)
				{
					scanLines[y_start].touch(p0[0], c0.color, zb0);
					scanLines[y_start].touch(p1[0], c1.color, zb1);
				}
				else
				{
					scanLines[y_start].touch(p0[0], zb0);
					scanLines[y_start].touch(p1[0], zb1);
				}
			}
			else
			{
				int dx = p0[0] - p1[0];
				int dy = p0[1] - p1[1];
				float dd = (zb1 - zb0)/(dx*dx + dy*dy);
				for(int y = y_start>0?y_start:0; y <= y_end && y < scanLines.length; y++)
				{
					int x = FixedPoint16.ToInt(m_fp*(y-p0[1]))+p0[0];
					dx = x - p0[0];
					dy = y - p0[1];
					float zb = zb0 + dd*(dx*dx + dy*dy);
					if(!textureEnabled)
						scanLines[y].touch(x,
								interpolateColor(colorBuffer, y-y_start,n,step_fp,r0,r1,g0,g1,b0,b1, a0, a1, c0.color, c1.color), zb);
					else
						scanLines[y].touch(x, zb);
				}
			}
		}
		Scan scan;
		int xMinBound = Integer.MAX_VALUE;
		int xMaxBound = Integer.MIN_VALUE;
		for(int y = yMinBound; y <= yMaxBound; scan.reset(), y++)
		{
			scan = scanLines[y];

			if(scan.min < xMinBound)
				xMinBound = scan.min;
			if(scan.max > xMaxBound)
				xMaxBound = scan.max;
			
			if(!textureEnabled)
			{
				n = scan.max-scan.min+1;
				step_fp = FixedPoint16.FromFloat(1.0f/n);
				a1 = scan.max_color.getAlpha();
				r1 = scan.max_color.getRed();
				g1 = scan.max_color.getGreen();
				b1 = scan.max_color.getBlue();
	
				a0 = scan.min_color.getAlpha();
				r0 = scan.min_color.getRed();
				g0 = scan.min_color.getGreen();
				b0 = scan.min_color.getBlue();
				
				float zstep = scan.computeZStep();
	
				for(int x = scan.min; x <= scan.max; x++)
				{
					if(y < 0 || y >= height || x < 0 || x >= width)
						continue;
					float zb = scan.minzb + zstep*(x-scan.min)*(x-scan.min);
					if(zb <= zBuffer[y*width+x])
					{
						data[y*width + x] = interpolateColor(colorBuffer, x-scan.min,n,step_fp,r0,r1,g0,g1,b0,b1, a0, a1, scan.min_color, scan.max_color).getRGB();
						zBuffer[y*width+x] = zb;
					}
				}
			}
			else
			{

				float zstep = scan.computeZStep();
				for(int x = scan.min; x <= scan.max; x++)
				{
					if(y < 0 || y >= height || x < 0 || x >= width)
						continue;

					float zb = scan.minzb + zstep*(x-scan.min)*(x-scan.min);
					if(zb <= zBuffer[y*width+x])
					{
						// Compute w = VecBuffer
						MyMath.init(-1,vecBuffer);
						this.projT.aTrasform(x,y, vecBuffer);
						// Compute P -> VecBuffer
						MyMath.scale(MyMath.dotProduct(tnormal, to)/MyMath.dotProduct(vecBuffer, tnormal), vecBuffer, vecBuffer);
						// VecBuffer = P-O
						MyMath.subtract(vecBuffer, to, vecBuffer);
	
						float c1 = MyMath.dotProduct(vecBuffer, tu);
						float c2 = MyMath.dotProduct(vecBuffer, tv);
						a0 = (int) (c1*p.texture.u[0] + c2*p.texture.v[0] + p.texture.o[0]);
						a1 = (int) (c1*p.texture.u[1] + c2*p.texture.v[1] + p.texture.o[1]);
						data[y*width + x] = p.texture.getColor(a0, a1);
						zBuffer[y*width+x] = zb;
					}
				}
			}
		}
		if(bounds[0] > xMinBound)
			bounds [0] = xMinBound;
		if(bounds[1] > yMinBound)
			bounds[1] = yMinBound;
		if(bounds[2] < xMaxBound)
			bounds[2] = xMaxBound;
		if(bounds[3] < yMaxBound)
			bounds[3] = yMaxBound;
			
	}

	private final static Color interpolateColor(MutableColor colorBuffer, int i, int n, int oneovern_fp, int r0, int r1, int g0,
			int g1, int b0, int b1, int a0, int a1, Color c1, Color c2)
	{
		int h = i*oneovern_fp;
		int l = (n-i)*oneovern_fp;
		int a = FixedPoint16.ToInt(l*a0 + h*a1);
		int r = FixedPoint16.ToInt(l*r0 + h*r1);
		int g = FixedPoint16.ToInt(l*g0 + h*g1);
		int b = FixedPoint16.ToInt(l*b0 + h*b1);
		
		colorBuffer.setRGBA(r,g,b,a);
		return colorBuffer;
	}

	private final void correctYBounds(Vertex v) 
	{
		int y = v.projection[1];
		y = y>0?y:0;
		y = y<scanLines.length?y:scanLines.length-1;
		if(this.yMinBound > y)
			this.yMinBound = y;
		if(this.yMaxBound < y)
			this.yMaxBound = y;
	}

	public void clean() 
	{
		bounds[0] = bounds[1] = Integer.MAX_VALUE;
		bounds[2] = bounds[3] = Integer.MIN_VALUE;
		Arrays.fill(((DataBufferInt)image.getRaster().getDataBuffer()).getBankData()[0], 0);
		Arrays.fill(zBuffer, 1.0f);
	}

	public void commit(Graphics g) 
	{
		g.drawImage(image, bounds[0], bounds[1], bounds[2], bounds[3], bounds[0], bounds[1], bounds[2], bounds[3], observer);
	}
	

}

class Scan
{
	int min;
	int max;
	float minzb;
	float maxzb;
	MutableColor max_color = new MutableColor(0);
	MutableColor min_color = new MutableColor(0);
	
	public Scan()
	{
		reset();
	}
	
	public float computeZStep() 
	{
		return (maxzb - minzb)/((min-max)*(min-max)); 
	}

	public void touch(int x, float zb)
	{
		if(x > max)
		{
			max = x;
			maxzb = zb;
		}
		if(x < min)
		{
			min = x;
			minzb = zb;
		}
	}

	void touch(int x, Color color, float zb)
	{
		if(x > max)
		{
			max = x;
			max_color.setRGBA(color.getRGB());
			maxzb = zb;
		}
		if(x < min)
		{
			min = x;
			min_color.setRGBA(color.getRGB());
			minzb = zb;
		}
	}
	
	void reset()
	{
		this.min = Integer.MAX_VALUE;
		this.max = Integer.MIN_VALUE;
	}
}