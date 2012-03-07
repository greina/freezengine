package com.codnyx.myengine;

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
	private ProjectedVertex[] vertices;
	private int numVertices;
	private int htx;
	private int hty;
	private HitTestHandler handler;
	
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
		this.vertices = new ProjectedVertex[8];
		for(int i = 0; i < vertices.length; i++)
			this.vertices[i] = new ProjectedVertex();
		numVertices = 0;
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
		/*
		 * Variables definition and initialization
		 */
		// Transformed normal of the polygon
		float[] tnormal = {0,0,0};
		
		// Texture origin, U and V vector properly transformed
		float[] tu = null, tv = null, to = null;
		
		// Texture coordinates in the image
		int tx, ty;
		
		// ZBuffer values
		float zb0, zb1;
		
		// Loop dumb variables
		int step_fp, n;
		n = 0;
		
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
		
		frustumClipping(p.vertices, projT.getZMin(), projT.getZMax());
		
		
		if(numVertices == 0)
			return;
		
		/*
		 * Rendering
		 */
		int[] p1;
		int[] p0;
		int i = 0;
		
		this.vertices[i].depth = projT.project(this.vertices[i].point, this.vertices[i].projection);
		i++;
		
		/*
		 * Scan all the edges in the polygon
		 */
		for(; i <= numVertices; i++)
		{
			ProjectedVertex cur = this.vertices[i%numVertices];
			ProjectedVertex prev = this.vertices[i-1];
			
			cur.depth = projT.project(cur.point, cur.projection);
			
			zb0 = prev.depth;
			zb1 = cur.depth;
			
			// Modify y bounds to include the new vertices
			correctYBounds(cur);
			correctYBounds(prev);
			
			int y_start, y_end;
			ProjectedVertex c0;
			ProjectedVertex c1;
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
								interpolateColor(y-y_start,n,step_fp, c0.color, c1.color), zb);
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

			if(scan.x0 < xMinBound)
				xMinBound = scan.x0;
			if(scan.x1 > xMaxBound)
				xMaxBound = scan.x1;
			
			if(!textureEnabled)
			{
				n = scan.x1-scan.x0+1;
				step_fp = FixedPoint16.FromFloat(1.0f/n);
				float zstep = scan.computeZStep();
	
				int x = scan.x0;
				x = x < 0?0:x;
				int end = scan.x1;
				end = end >= width?(width-1):end;
				for( ; x <= end; x++)
				{
					if(y < 0 || y >= height)
						continue;
					float zb = scan.depth0 + zstep*(x - scan.x0);
					if(zb < zBuffer[y*width+x])
					{
						data[y*width + x] = interpolateColor(x-scan.x0,n,step_fp, scan.min_color, scan.max_color);
						zBuffer[y*width+x] = zb;
						if(handler != null && x == htx && y == hty)
							handler.hit(x, y, zb, data[y*width + x], p);
					}
				}
			}
			else
			{

				float zstep = scan.computeZStep();

				int x = scan.x0;
				x = x < 0?0:x;
				int end = scan.x1;
				end = end >= width?(width-1):end;
				for( ; x <= end; x++)
				{
					if(y < 0 || y >= height)
						continue;

					float zb = scan.depth0 + zstep*(x-scan.x0);
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
						tx = (int) (c1*p.texture.u[0] + c2*p.texture.v[0] + p.texture.o[0]);
						ty = (int) (c1*p.texture.u[1] + c2*p.texture.v[1] + p.texture.o[1]);
						data[y*width + x] = p.texture.getColor(tx, ty);
						zBuffer[y*width+x] = zb;
						if(handler != null && x == htx && y == hty)
							handler.hit(x, y, zb, data[y*width + x], p);
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

	private final static int interpolateColor(int i, int n, int oneovern_fp, int color_0, int color_1)
	{		
		int h = i*oneovern_fp;
		int l = (n-i)*oneovern_fp;
		int a = FixedPoint16.ToInt(l*ColorUtils.getAlpha(color_0) + h*ColorUtils.getAlpha(color_1));
		int r = FixedPoint16.ToInt(l*ColorUtils.getRed(color_0) + h*ColorUtils.getRed(color_1) );
		int g = FixedPoint16.ToInt(l*ColorUtils.getGreen(color_0)  + h*ColorUtils.getGreen(color_1));
		int b = FixedPoint16.ToInt(l*ColorUtils.getBlue(color_0) + h*ColorUtils.getBlue(color_1));
		
		
		return ColorUtils.getRGB(a, r, g, b);
	}
	

	private final static int interpolateColor(int color_0, int color_1, float t)
	{		
		float l = (1-t);
		float h = t;
		int a = (int) (l*ColorUtils.getAlpha(color_0) + h*ColorUtils.getAlpha(color_1));
		int r = (int) (l*ColorUtils.getRed(color_0) + h*ColorUtils.getRed(color_1)) ;
		int g = (int) (l*ColorUtils.getGreen(color_0)  + h*ColorUtils.getGreen(color_1));
		int b = (int) (l*ColorUtils.getBlue(color_0) + h*ColorUtils.getBlue(color_1));
		
		
		return ColorUtils.getRGB(a, r, g, b);
	}

	private final void correctYBounds(ProjectedVertex v) 
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
		if(handler != null)
		{
			handler.commit();
			handler = null;
		}
		g.drawImage(image, bounds[0], bounds[1], bounds[2], bounds[3], bounds[0], bounds[1], bounds[2], bounds[3], observer);
	}
	
	public void hitTest(int x, int y, HitTestHandler handler)
	{
		this.htx = x;
		this.hty = y;
		this.handler = handler;
	}
	
	private final void frustumClipping(Vertex[] vs, float zMin, float zMax)
	{
		if(vs.length + 1 > vertices.length)
			expandVertexArray(vs.length + 1);
		
		numVertices = 0;
		
		int exit = -1;
		int enter = -1;
		int inVertices = 0;
		
		boolean wasIn = false;
		boolean firstIn = false;
		boolean isIn = false;
		
		for(int i = 0; i < vs.length; i++)
		{
			vertices[i].setTo(vs[i]);
			modelT.transform(vertices[i].point,vertices[i].point);
			modelT.normal_transform(vertices[i].normal, vertices[i].normal);
			isIn = vertices[i].point[2] >= zMin;
			if (i == 0)
				firstIn = isIn;
			else
			{
				if(wasIn && !isIn)
					exit = i - 1;
				if(isIn && !wasIn)
					enter = i - 1 ;
			}
			if(isIn)
				inVertices++;
			
			wasIn =  isIn;
			
		}
		
		if(wasIn && !firstIn)
			exit = vs.length-1;
		if(firstIn && !wasIn)
			enter = vs.length-1;
		
		if(inVertices > 0)
		{
			numVertices = vs.length;			
		}
		else
		{
			numVertices = 0;
			return;
		}
		
		if(enter != -1 || exit != -1)
		{
			/*
			 * Clip all the vertices greater than zMin 
			 */
			clip(enter, exit, zMin, firstIn);
			
			if(numVertices == 0)
				return;
		}
			
		
		/*
		 * Check enter and exit point from the plane zMax
		 */
		inVertices = 0;
		enter = -1;
		exit = -1;
		for(int i = 0; i < numVertices; i++)
		{
			
			isIn = vertices[i].point[2]  <= zMax;
			if (i == 0)
				firstIn = isIn;
			else
			{
				if(wasIn && !isIn)
					exit = i - 1;
				if(isIn && !wasIn)
					enter = i - 1;
			}

			if(isIn)
				inVertices++;
			wasIn =  isIn;
		}
		
		if(wasIn && !firstIn)
			exit = vs.length-1;
		if(firstIn && !wasIn)
			enter = vs.length-1;
		
		if(inVertices == 0)
		{
			numVertices = 0;
			return;
		}
		
		if(enter == -1 && exit == -1)	return;
		
		clip(enter, exit, zMax, firstIn);
		
	}
	
	private final void clip(int enter, int exit, float zPlane, boolean firstIn)
	{
		boolean isIn = firstIn;
		
		for(int i = 0; i < numVertices; i++)
		{
			ProjectedVertex cur = vertices[i];
			ProjectedVertex next = vertices[(i+1)%numVertices];
			if(i == enter)
			{
				interpolatePoints(cur, next, zPlane);
				isIn = true;
			}
			else if (i == exit)
			{
				if(i+1 == enter)
				{
					/*
					 * Here we need to add a new vertex
					 */
					
					// Shift the vector array to the right
					for(int j = numVertices; j > i; j--)
						vertices[j].setTo(vertices[j-1]);
					numVertices++;
				}
				interpolatePoints(next,cur, zPlane);
			}
			else
			{
				if(!isIn && (i != 0 || (i == 0 && exit != numVertices -1)))
				{
					/*
					 * Shift all the vector array to the left
					 */
					for(int j = i; j < numVertices; j++)
						vertices[j].setTo(vertices[j+1]);

					if(enter > i)
						enter--;
					if(exit > i)
						exit--;
					i--;
					numVertices--;
				}
			}
		}	
	}
	
	
	private final void interpolatePoints(ProjectedVertex p0, ProjectedVertex p1, float zPlane)
	{
		float[] buffer = {0,0,0};
		/*
		 * First interpolate the point
		 */
		// t = -z0 / (z1 - z0)
		float t = (zPlane-p0.point[2])/(p1.point[2]-p0.point[2]);
		// Q = P0 + t*(P1-P0)
		MyMath.subtract(p1.point, p0.point, buffer);
		MyMath.scale(t, buffer, buffer);
		MyMath.add(p0.point, buffer, p0.point);
		/*
		 * ..then the normal
		 */
		if(p0.normal != null && p1.normal != null)
		{
			// N = N0 + t*(N1-N0)
			MyMath.subtract(p1.normal, p0.normal, buffer);
			MyMath.scale(t, buffer, buffer);
			MyMath.add(p0.normal, buffer, p0.normal);
		}
		/*
		 * ...finally the color
		 */
		if(p0.color != p1.color)
			p0.color = interpolateColor(p0.color, p1.color, t);
	}
	
	private final void expandVertexArray(int requiredSize)
	{
		ProjectedVertex[] oldArray = this.vertices;
		int newSize = this.vertices.length*2;
		while(newSize < requiredSize)
			newSize *= 1;
		
		this.vertices = new ProjectedVertex[newSize];
		for(int i = 0; i < oldArray.length; i++)
			this.vertices[i] = oldArray[i];
		for(int i = oldArray.length; i < this.vertices.length; i++)
			this.vertices[i] = new ProjectedVertex();
	}
	
	public static interface HitTestHandler
	{
		public void hit(int x, int y, float z, int color, Polygon p);
		public void commit();
	}
	
}

class Scan
{
	int x0;
	int x1;
	float depth0;
	float depth1;
	int  max_color = 0;
	int min_color = 0;
	
	public Scan()
	{
		reset();
	}
	
	public float computeZStep() 
	{
		return (depth1-depth0)/(x1-x0);
	}

	public void touch(int x, float zb)
	{
		if(x > x1)
		{
			x1 = x;
			depth1 = zb;
		}
		if(x < x0)
		{
			x0 = x;
			depth0 = zb;
		}
	}

	void touch(int x, int color, float zb)
	{
		if(x > x1)
		{
			x1 = x;
			max_color = color;
			depth1 = zb;
		}
		if(x < x0)
		{
			x0 = x;
			min_color = color;
			depth0 = zb;
		}
	}
	
	void reset()
	{
		this.x0 = Integer.MAX_VALUE;
		this.x1 = Integer.MIN_VALUE;
	}
}