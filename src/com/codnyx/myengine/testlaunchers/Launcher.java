package com.codnyx.myengine.testlaunchers;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.VolatileImage;

import javax.swing.JFrame;

import com.codnyx.myengine.AffineTransformation;
import com.codnyx.myengine.ColorUtils;
import com.codnyx.myengine.MyMath;
import com.codnyx.myengine.Polygon;
import com.codnyx.myengine.PolygonRenderer;



public abstract class Launcher extends JFrame implements Runnable, KeyListener, MouseListener{
	
	private static final long serialVersionUID = 1L;
	protected AffineTransformation a;
	boolean isRunning = false;
	protected PolygonRenderer renderer;
	private final static boolean SMOOTHSIMRATE = false;
	private final static boolean USEVIRTUALIMAGE = false;

	public Launcher(double yangle, float z_min, float z_max, int width, int height)
	{
		this.renderer = new PolygonRenderer(width, height);
		this.renderer.setPerspective(yangle, z_min, z_max);
		this.a = renderer.getModelT();
		isRunning = true;
		this.init();
		this.addKeyListener(this);
		this.addMouseListener(this);
		new Thread(Launcher.this).start();
	}
	
	protected abstract void init();

	int counter = -1;
	final float angle_step = (float) (Math.PI/360);
	float angle = 0;
	protected float ztr = -10;
	long framelength = 0;
	private VolatileImage volatileImg;
	
	public void run()
	{
		float fps = 0;
		long oldTick = 0;
		float maxfps = 0;
		while(isRunning)
		{
			if(fps == 0)
			{
				if(oldTick != 0)
				{
					framelength = System.nanoTime() - oldTick;
					fps = (float) (1e9/(float)framelength);
				}
			}
			else
			{
				framelength = System.nanoTime() - oldTick;
				float newfps = (float) (1e9/(float)framelength);
				fps = .05f*newfps+.95f*fps; 
			}
			if(!this.isVisible())
				try {
					Thread.sleep(1000);
					continue;
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			

			if(SMOOTHSIMRATE)
				try {
					long st = (long) (framelength/1e6 - 1000.0f/fps);
					if(st > 0)
						Thread.sleep(st);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			
			oldTick = System.nanoTime();
			if(USEVIRTUALIMAGE)
			{
			createBackBuffer();
			do {
				   GraphicsConfiguration gc = this.getGraphicsConfiguration();
				   int valCode = volatileImg.validate(gc);
				   
				   if(valCode==VolatileImage.IMAGE_INCOMPATIBLE){
				    createBackBuffer(); 
				   }
				  
				   Graphics offscreenGraphics = volatileImg.getGraphics();
				   Graphics g = offscreenGraphics;
				   doPaint(g); // call core paint method.

				   g.setColor(Color.white);
				   g.drawString(String.format("FPS: %.2f",fps), 50, 50);
				   g.drawString(String.format("Camera z=%.2f ",-ztr), 50, 70);
				   if(fps > maxfps)
				   {
					   maxfps = fps;
//					   System.out.println("Spike " + maxfps);
				   }
				   
				   this.getBufferStrategy().getDrawGraphics().drawImage(volatileImg, 0, 0, this);

					getBufferStrategy().show();
				   // Test if content is lost   
			  } while(volatileImg.contentsLost());
			
			}
			else
			{
				   Graphics g = getBufferStrategy().getDrawGraphics();
				   doPaint(g); // call core paint method.

				   g.setColor(Color.white);
				   g.drawString(String.format("FPS: %.2f",fps), 50, 50);
				   g.drawString(String.format("Camera z=%.2f ",-ztr), 50, 70);
				   if(fps > maxfps)
				   {
					   maxfps = fps;
//					   System.out.println("Spike " + maxfps);
				   }
				   

					getBufferStrategy().show();
				
			}

		}
	}

	protected abstract void doPaint(Graphics g);

	private void createBackBuffer() 
	{
		GraphicsConfiguration gc = getGraphicsConfiguration();
		volatileImg = gc.createCompatibleVolatileImage(getWidth(), getHeight());
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
	@Override
	public void mouseEntered(MouseEvent e)
	{
	}
	@Override
	public void mouseExited(MouseEvent e)
	{
	}
	
	@Override
	public void mousePressed(MouseEvent e)
	{
	}

	@Override
	public void mouseReleased(MouseEvent e)
	{
	}
	
	@Override
	public void mouseClicked(MouseEvent e)
	{
		int x = e.getX();
		int y = e.getY();
		renderer.hitTest(x, y, handler);
	}

	
	PolygonRenderer.HitTestHandler handler = new PolygonRenderer.HitTestHandler()
	{
		int color;
		float[] coord3D = {Float.NEGATIVE_INFINITY,Float.NEGATIVE_INFINITY,Float.NEGATIVE_INFINITY};
		int x, y;
		float depth;
		@Override
		public void hit(int x, int y, float depth, int color, Polygon p)
		{
			if(p != null)
			{
				float[] u = {0,0,0};
				float[] n = p.getNormal().clone();
				float[] c = p.getCenter().clone();
				
				renderer.getModelT().transform(c);
				renderer.getModelT().normal_transform(n);
				
				MyMath.init(-1,u);
				renderer.getProjT().aTrasform(x, y, u);				

				MyMath.scale(MyMath.dotProduct(n, c)/MyMath.dotProduct(u, n), u, coord3D);
			}
			this.color = color;
			this.x = x;
			this.y = y;
			this.depth = depth;
		}
		
		public void commit() 
		{
			System.out.println(String.format("Click on (%d,%d)->(%.3f,%.3f,%.3f): color=rgba-0x%02X%02X%02X%02X depth=%.4f", x,y,coord3D[0], coord3D[1], coord3D[2], ColorUtils.getRed(color), ColorUtils.getGreen(color), ColorUtils.getBlue(color),ColorUtils.getAlpha(color), depth));
		};
	};
}
