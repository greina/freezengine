package com.codnyx.myengine.testlaunchers;
import java.awt.Color;
import java.awt.Graphics;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.swing.JFrame;

import com.codnyx.myengine.Mesh;
import com.codnyx.myengine.ObjParser;



public class ObjTestLauncher extends Launcher
{
	private static final long serialVersionUID = 110959145651340442L;
	
	public ObjTestLauncher(double yangle, float z_min, float z_max, int width, int height)
	{
		super(yangle,z_min,z_max, width, height);
	}
	
	Mesh m;
	
	protected void init()
	{
			InputStream stream = this.getClass().getResourceAsStream("/cow.obj");
			try {
				this.m = new ObjParser().parseStream(new BufferedReader(new InputStreamReader(stream)));
			} catch (IOException e) {
				e.printStackTrace();
			}
	
	}	

	int counter = -1;
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
		a.rotateY(2*angle);
//		a.rotateZ(4*angle);
		a.translateTo(0,0,ztr);
		m.render(renderer, g);
		
		renderer.commit(g);
	}

	public static void main(String[] args)
	{
		int width = 1000;
		int height = 750;
		ObjTestLauncher main = new ObjTestLauncher(Math.PI/4.0, -30f, -1f, width, height);
		main.setSize(width, height);
		main.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		main.setVisible(true);
		main.createBufferStrategy(2);
	}
}
