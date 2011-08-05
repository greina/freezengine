package com.codnyx.myengine;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JFrame;



public class Main extends JFrame implements Runnable, KeyListener{
	
	private static final long serialVersionUID = 1L;
	private AffineTransformation a;
	boolean isRunning = false;
	private PolygonRenderer renderer;

	public Main(double yangle, float z_min, float z_max, int width, int height)
	{
		this.renderer = new PolygonRenderer(width, height);
		this.renderer.setPerspective(yangle, z_min, z_max);
		this.a = renderer.getModelT();
		isRunning = true;
		this.init();
		this.addKeyListener(this);
		new Thread(Main.this).start();
	}
	
	Polygon[] cube;
	
	
	float[][] cubepoint = {
			{1,1,1}, // 0
			{-1,1,1}, //1
			{-1,-1,1}, //2
			{1,-1,1}, //3

			{1,1,-1}, //4
			{-1,1,-1}, //5
			{-1,-1,-1}, //6
			{1,-1,-1}, //7		
			
	};
	private Polygon face;
	
	private void init()
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
	
	float[][] points = {
			{-1,-1,1},
			{-1,1,1},
			{1,1,1},
			{1,-1,1},

			{-1,-1,1},
			{-1,-1,-1},
			{-1,1,-1},	
			{1,1,-1},	
			{1,-1,-1},	
			{1,-1,1},		
			{1,-1,-1},		
			{-1,-1,-1},		
			{-1,1,-1},		
			{-1,1,1},		
			{1,1,1},		
			{1,1,-1},	
	};

	int counter = -1;
	final float angle_step = (float) (Math.PI/360);
	float angle = 0;
	private float ztr = -10;
	public void run()
	{
		float fps = 0;
		long oldTick = 0;
		while(isRunning)
		{
			if(fps == 0)
			{
				if(oldTick != 0)
				{
					long framelength = System.nanoTime() - oldTick;
					fps = (float) (1e9/(float)framelength);
				}
			}
			else
			{
				long framelength = System.nanoTime() - oldTick;
				float newfps = (float) (1e9/(float)framelength);
				fps = .05f*newfps+.95f*fps; 
			}
			oldTick = System.nanoTime();
			if(!this.isVisible())
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			Graphics g = this.getBufferStrategy().getDrawGraphics();
			renderer.clean();
			g.setColor(Color.black);
			g.fillRect(0, 0, 1000, 750);

			angle += angle_step;
			a.loadIdentity();
			a.rotateX(angle);
			a.rotateY(2*angle);
//			a.rotateZ(4*angle);
			a.translateTo(0,0,ztr);
			
			for(Polygon face:cube)
				renderer.render(g,face);
			
			a.loadIdentity();
			a.rotateZ(angle);
			a.translateTo(5,0,ztr);
			a.rotateZ(angle);
			renderer.render(g, face);
					
				
//			renderer.render(g, face);
			g.setColor(Color.white);
					g.drawString(String.format("FPS: %.2f",fps), 50, 50);
					g.drawString(String.format("Camera z=%.2f ",-ztr), 50, 70);
			g.dispose();
			getBufferStrategy().show();
		}
	}

	public static void main(String[] args)
	{
		int width = 1000;
		int height = 750;
		Main main = new Main(Math.PI/4.0, -1f, -30.0f, width, height);
		main.setSize(width, height);
		main.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		main.setVisible(true);
		main.createBufferStrategy(2);
	}

	@Override
	public void keyTyped(KeyEvent e) {
	}

	@Override
	public void keyPressed(KeyEvent e) 
	{
		if(e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_W)
			this.ztr += .2f;
		if(e.getKeyCode() == KeyEvent.VK_DOWN || e.getKeyCode() == KeyEvent.VK_S)
			this.ztr -= .2f;		
	}

	@Override
	public void keyReleased(KeyEvent e) {
	}
}
