package com.codnyx.myengine.testlaunchers;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

import com.codnyx.myengine.Mesh;
import com.codnyx.myengine.ObjParser;
import com.codnyx.myengine.Polygon;
import com.codnyx.myengine.Texture;



public class TextureTestLauncher extends Launcher
{
	private static final long serialVersionUID = -4843037917887540050L;

	public TextureTestLauncher(double yangle, float z_min, float z_max, int width, int height)
	{
		super(yangle,z_min, z_max, width, height);
	}
	
	Mesh m;
	
	protected void init()
	{
		String plane_str = "v 1 1 0\nv -1 1 0\nv -1 -1 0\nv 1 -1 0\n\nf 1 2 3 4\nf 4 3 2 1";
		StringReader sr = new StringReader(plane_str);
		try {
			this.m = new ObjParser().parseStream(new BufferedReader(sr));
			BufferedImage image = ImageIO.read(getClass().getResourceAsStream("/texture.jpg"));
			if(image.getType() != BufferedImage.TYPE_INT_ARGB)
			{
				BufferedImage newImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
				Graphics2D g = newImage.createGraphics();
				g.drawImage(image, 0, 0, null);
				g.dispose();
				image = newImage;
			}
			float scale = 4.f;
			float[][] coords1 = {{scale, 0.0f}, {0.0f, 0.0f}, {0.0f, scale}};
			float[][] coords2 = {{scale, 0.0f}, {0.0f, scale}, {scale,scale}};
			Polygon p = this.m.polygons.get(0);
			Texture t = new Texture(image, p, coords1);
			p.texture = t;
			p = this.m.polygons.get(1);
			t = new Texture(image, p, coords2);
			p.texture = t;
			BufferedImage image2 = ImageIO.read(getClass().getResourceAsStream("/texture2.jpg"));
			if(image2.getType() != BufferedImage.TYPE_INT_ARGB)
			{
				BufferedImage newImage = new BufferedImage(image2.getWidth(), image2.getHeight(), BufferedImage.TYPE_INT_ARGB);
				Graphics2D g = newImage.createGraphics();
				g.drawImage(image2, 0, 0, null);
				g.dispose();
				image2 = newImage;
			}
			scale = 1;
			float[][] coords3 = {{scale,scale}, {0.0f, scale}, {0.0f,0.0f}};
			float[][] coords4 = {{scale,scale}, {0.0f,0.0f}, {scale, 0.0f}};

			p = this.m.polygons.get(2);
			t = new Texture(image2, p, coords3);
			p.texture = t;
			p = this.m.polygons.get(3);
			t = new Texture(image2, p, coords4);
			p.texture = t;
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}	

	final float angle_step = (float) (Math.PI/360);
	float angle = 0;
	long framelength = 0;

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
		TextureTestLauncher main = new TextureTestLauncher(Math.PI/4.0, -1f, -30.0f, width, height);
		main.setSize(width, height);
		main.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		main.setVisible(true);
		main.createBufferStrategy(2);
	}
}
