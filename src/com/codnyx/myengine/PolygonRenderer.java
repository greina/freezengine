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
	private ImageObserver observer = new ImageObserver() {
		@Override
		public boolean imageUpdate(Image img, int infoflags, int x, int y,
				int width, int height) {
			return false;
		}
	};
	
	public void render(Graphics gc, Polygon p)
	{
		int a1,r1,g1,b1,a0,r0,g0,b0,n;
		MutableColor colorBuffer = new MutableColor(0);
		float step;
		/*
		 * Backface culling
		 */
		modelT.normal_transform(p.normal, vecBuffer);
		modelT.transform(p.center,vecBuffer2);
		if(MyMath.dotProduct(vecBuffer, vecBuffer2)>=0)
			return;
		reset();
		
		int[][] data = ((DataBufferInt)image.getRaster().getDataBuffer()).getBankData();
		
		/*
		 * Rendering
		 */
		int[] p1;
		int[] p0;
		int i = 0;
		
		modelT.transform(p.vertices[i].point,vecBuffer);
		p.vertices[i].isVisible = projT.project(vecBuffer, p.vertices[i].projection);
		modelT.normal_transform(p.vertices[i].normal, p.vertices[i].tnormal);
		i++;
		
		for(; i <= p.vertices.length; i++)
		{
			Vertex cur = p.vertices[i%p.vertices.length];
			Vertex prev = p.vertices[i-1];
			if(cur != p.vertices[0])
			{
				modelT.transform(cur.point,vecBuffer);
				cur.isVisible = projT.project(vecBuffer, cur.projection);
				modelT.normal_transform(cur.normal, cur.tnormal);
			}
			
			if(!cur.isVisible || !prev.isVisible)
				continue;
			
			touch(cur);
			touch(prev);
			
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
			}

			y_end = c1.projection[1];
			y_start = c0.projection[1];
			p1 = c1.projection;
			p0 = c0.projection;
						
			float m = ((float)p1[0] - p0[0])/((float)p1[1] - p0[1]);
			n = y_end-y_start+1;
			step = 1.0f/n;
			a1 = c1.color.getAlpha();
			r1 = c1.color.getRed();
			g1 = c1.color.getGreen();
			b1 = c1.color.getBlue();

			a0 = c0.color.getAlpha();
			r0 = c0.color.getRed();
			g0 = c0.color.getGreen();
			b0 = c0.color.getBlue();

			if(p1[1] == p0[1])
			{
				scanLines[y_start>0?y_start:0].touch(p0[0], prev.color);
				scanLines[y_start>0?y_start:0].touch(p1[0], cur.color);
			}
			else
			for(int j = y_start>0?y_start:0; j <= y_end && j < scanLines.length; j++)
			{
				scanLines[j].touch((int) (m*(j-p0[1])+p0[0]),// cur.color);
						interpolateColor(colorBuffer, j-y_start,n,step,r0,r1,g0,g1,b0,b1, a0, a1));
			}
		}
		Scan scan;
		int xMinBound = Integer.MAX_VALUE;
		int xMaxBound = Integer.MIN_VALUE;
		for(int j = yMinBound; j <= yMaxBound; scan.reset(), j++)
		{
			scan = scanLines[j];
//			if(scan.min_color.equals(scan.max_color))
//			{
//				drawLine(data, scan.min_color, scan.min, j, scan.max, j);
//				g.setColor(scan.min_color);
//				g.drawLine(scan.min, j, scan.max, j);
////				System.out.println(String.format("Writing line from (%d,%d) to (%d,%d) and color %s", scan.min, j, scan.max, j, scan.min_color.toString()));
//			}
//			else
//			{
				n = scan.max-scan.min+1;
				step = 1.0f/n;
				a1 = scan.max_color.getAlpha();
				r1 = scan.max_color.getRed();
				g1 = scan.max_color.getGreen();
				b1 = scan.max_color.getBlue();

				a0 = scan.min_color.getAlpha();
				r0 = scan.min_color.getRed();
				g0 = scan.min_color.getGreen();
				b0 = scan.min_color.getBlue();
				
				if(scan.min < xMinBound)
					xMinBound = scan.min;
				if(scan.max > xMaxBound)
					xMaxBound = scan.max;
				
				for(int x = scan.min; x <= scan.max; x++)
				{
//					g.setColor(interpolateColor(colorBuffer, x-scan.min,n,step,r0,r1,g0,g1,b0,b1, a0, a1));
//					g.fillRect(x, j, 1, 1);
					if(j < 0 || j >= height || x < 0 || x >= width)
						continue;
					data[0][j*width + x] = interpolateColor(colorBuffer, x-scan.min,n,step,r0,r1,g0,g1,b0,b1, a0, a1).getRGB();
//					g.drawLine(x, j, x, j);
				}
//			}
		}
		
		gc.drawImage(image, xMinBound, yMinBound, xMaxBound, yMaxBound, xMinBound, yMinBound, xMaxBound, yMaxBound, observer);
		
	}

	private final static Color interpolateColor(MutableColor colorBuffer, int i, int n, float oneovern, int r0, int r1, int g0,
			int g1, int b0, int b1, int a0, int a1)
	{
		float h = i*oneovern;
		float l = (n-i)*oneovern;
		int a = (int) (l*a0 + h*a1);
		int r = (int) (l*r0 + h*r1);
		int g = (int) (l*g0 + h*g1);
		int b = (int) (l*b0 + h*b1);
		
		colorBuffer.setRGBA(r,g,b,a);
		return colorBuffer;
	}

	private void touch(Vertex v) 
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
		Arrays.fill(((DataBufferInt)image.getRaster().getDataBuffer()).getBankData()[0], 0);
	}
	

}

class Scan
{
	int min;
	int max;
	MutableColor max_color = new MutableColor(0);
	MutableColor min_color = new MutableColor(0);
	
	public Scan()
	{
		reset();
	}
	
	void touch(int x, Color color)
	{
//		if(min == null || min.projection[0] > v.projection[0])
//			min = v;
//		
//		if(max == null || max.projection[0] < v.projection[0])
//			max = v;
		if(x > max)
		{
			max = x;
			max_color.setRGBA(color.getRGB());
		}
		if(x < min)
		{
			min = x;
			min_color.setRGBA(color.getRGB());
		}
	}
	
	void reset()
	{
		this.min = Integer.MAX_VALUE;
		this.max = Integer.MIN_VALUE;
	}
}