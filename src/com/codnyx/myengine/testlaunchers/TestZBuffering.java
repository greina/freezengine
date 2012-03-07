package com.codnyx.myengine.testlaunchers;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JFrame;

import com.codnyx.myengine.Polygon;
import com.codnyx.myengine.Vertex;

public class TestZBuffering extends Launcher
{
	private static final long serialVersionUID = -8374811509718775972L;

	public TestZBuffering(double yangle, float z_min, float z_max, int width, int height)
	{
		super(yangle, z_min, z_max, width, height);
	}
	Polygon face1, face2;
	
	@Override
	protected void init()
	{
		Vertex v1, v2, v3, v4;
		float sr3 = (float) (1.0f/Math.sqrt(3)); 

		v1 = new Vertex(new float[]{1,1,0}, new float[]{sr3, sr3, sr3},Color.red);
		v2 = new Vertex(new float[]{-1,1,0}, new float[]{-sr3, sr3, sr3}, Color.red);
		v3 = new Vertex(new float[]{-1,-1,0}, new float[]{-sr3, -sr3, sr3}, Color.red);
		v4 = new Vertex(new float[]{1,-1,0}, new float[]{sr3, -sr3, sr3}, Color.red);
		face1 = new Polygon(new Vertex[]{v1,v2,v3,v4});

		v1 = new Vertex(new float[]{1,1,0}, new float[]{sr3, sr3, sr3},Color.blue);
		v2 = new Vertex(new float[]{-1,1,0}, new float[]{-sr3, sr3, sr3}, Color.blue);
		v3 = new Vertex(new float[]{-1,-1,0}, new float[]{-sr3, -sr3, sr3},Color.blue);
		v4 = new Vertex(new float[]{1,-1,0}, new float[]{sr3, -sr3, sr3}, Color.blue);
		face2 = new Polygon(new Vertex[]{v1,v2,v3,v4});
		angle = (float) (Math.PI/6);
	}

	@Override
	protected void doPaint(Graphics g)
	{
		renderer.clean();
		g.setColor(Color.black);
		g.fillRect(0, 0, 1000, 750);

		a.loadIdentity();
		a.rotateY(-angle);
		a.translateTo(0,0,ztr);
		renderer.render(g, face1);

				
		a.loadIdentity();
		a.rotateY(angle);
		a.translateTo(0,0,ztr);
		renderer.render(g, face2);
		
		renderer.commit(g);
	}
	

	public static void main(String[] args)
	{
		int width = 1000;
		int height = 750;
		TestZBuffering main = new TestZBuffering(Math.PI/4.0, -30.0f, -.1f, width, height);
		main.setSize(width, height);
		main.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		main.setVisible(true);
		main.createBufferStrategy(2);
	}
}
