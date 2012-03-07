package com.codnyx.myengine.testlaunchers;
import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JFrame;

import com.codnyx.myengine.Polygon;
import com.codnyx.myengine.Vertex;



public class PolyTestLauncher extends Launcher
{
	private static final long serialVersionUID = -1058628550412652050L;

	public PolyTestLauncher(double yangle, float z_min, float z_max, int width, int height)
	{
		super(yangle, z_min, z_max, width, height);
	}
	
	Polygon[] cube;
	private Polygon face;
	
	protected void init()
	{
		cube = new Polygon[6];
		float sr3 = (float) (1.0f/Math.sqrt(3)); 
		Vertex v1 = new Vertex(new float[]{1,1,1}, new float[]{sr3, sr3, sr3},Color.red);
		Vertex v2 = new Vertex(new float[]{-1,1,1}, new float[]{-sr3, sr3, sr3}, Color.yellow);
		Vertex v3 = new Vertex(new float[]{-1,-1,1}, new float[]{-sr3, -sr3, sr3}, Color.blue);
		Vertex v4 = new Vertex(new float[]{1,-1,1}, new float[]{sr3, -sr3, sr3}, Color.green);
		Vertex v5 = new Vertex(new float[]{1,1,-1}, new float[]{sr3, sr3, -sr3}, Color.magenta);
		Vertex v6 = new Vertex(new float[]{1,-1,-1}, new float[]{sr3, -sr3, -sr3}, Color.orange);
		Vertex v7 = new Vertex(new float[]{-1,-1,-1}, new float[]{-sr3, -sr3, -sr3}, Color.cyan);
		Vertex v8 = new Vertex(new float[]{-1,1,-1}, new float[]{-sr3, sr3, -sr3}, Color.pink);

		cube[0] = new Polygon(new Vertex[]{v1,v2,v3,v4});
		cube[1] = new Polygon(new Vertex[]{v5,v6,v7,v8});
		cube[2] = new Polygon(new Vertex[]{v1,v4,v6,v5});
		cube[3] = new Polygon(new Vertex[]{v8,v7,v3,v2});
		cube[4] = new Polygon(new Vertex[]{v1,v5,v8,v2});
		cube[5] = new Polygon(new Vertex[]{v7,v6,v4,v3});


		v1 = new Vertex(new float[]{1,1,0}, new float[]{sr3, sr3, sr3},Color.red);
		v2 = new Vertex(new float[]{-1,1,0}, new float[]{-sr3, sr3, sr3}, Color.blue);
		v3 = new Vertex(new float[]{-1,-1,0}, new float[]{-sr3, -sr3, sr3}, Color.green);
		v4 = new Vertex(new float[]{1,-1,0}, new float[]{sr3, -sr3, sr3}, Color.cyan);
		face = new Polygon(new Vertex[]{v1,v2,v3,v4});
	}	
	
	final float angle_step = (float) (Math.PI/360);
	float angle = 0;
	
	protected void doPaint(Graphics g) 
	{
		renderer.clean();
		g.setColor(Color.black);
		g.fillRect(0, 0, 1000, 750);

		angle += angle_step;
		a.loadIdentity();
//		a.rotateX(angle);
		a.rotateY(angle);
//		a.rotateZ(4*angle);
		a.translateTo(0,0,ztr);

		for(Polygon face:cube)
			renderer.render(g,face);
				
		a.loadIdentity();
		a.rotateZ(angle);
		a.translateTo(5,0,ztr);
		a.rotateZ(angle);
		renderer.render(g, face);


		a.loadIdentity();
		a.translateTo(0,0,ztr/2);
		a.rotateY(angle);
		a.translateTo(0,0,ztr);
		renderer.render(g, face);
		
		renderer.commit(g);
	}
	
	public static void main(String[] args)
	{
		int width = 1000;
		int height = 750;
		PolyTestLauncher main = new PolyTestLauncher(Math.PI/4.0, -30f, -1f, width, height);
		main.setSize(width, height);
		main.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		main.setVisible(true);
		main.createBufferStrategy(2);
	}
}
