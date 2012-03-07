package com.codnyx.myengine;
import java.awt.Color;
import java.util.Arrays;



public class ProjectedVertex extends Vertex
{
	int[] projection = {0,0};
	public float depth = 0;
	
	public ProjectedVertex()
	{
	}
	
	public ProjectedVertex(float[] point)
	{
		super(point);
		init();
	}
	
	public ProjectedVertex(float[] point, float[] normal)
	{
		super(point, normal);
		init();
	}
	
	public ProjectedVertex(float[] point, float[] normal, Color color)
	{
		super(point, normal, color);
		init();
	}
	
	public ProjectedVertex(float[] point, Color color)
	{
		super(point, color);
	}

	public ProjectedVertex(Vertex vertex)
	{
		super(vertex);	
		init();	
	}


	public ProjectedVertex(ProjectedVertex vertex)
	{
		setTo(vertex);
	}
	
	@Override
	public void setTo(Vertex v)
	{
		super.setTo(v);
		init();
	}
	
	
	public void setTo(ProjectedVertex pv)
	{
		super.setTo(pv);

		this.depth = pv.depth;
		this.projection = pv.projection.clone();
	}
	
	private final void init()
	{
		Arrays.fill(projection, 0);
		depth = 0;
	}
	
	@Override
	public void reset()
	{
		super.reset();
		init();
	}
}
