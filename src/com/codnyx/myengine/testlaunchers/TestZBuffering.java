package com.codnyx.myengine.testlaunchers;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JFrame;

import com.codnyx.myengine.Polygon;
import com.codnyx.myengine.Vertex;

/**
 * A test launcher designed to demonstrate and verify the Z-buffering (depth buffering)
 * functionality of the rendering engine. It creates a scene with two large, overlapping
 * quadrilateral polygons, one red and one blue. These polygons are rotated in opposite
 * directions around the Y-axis, causing them to continuously intersect and pass through
 * each other. Correct Z-buffering should ensure that the parts of polygons that are
 * closer to the camera correctly obscure parts that are further away.
 */
public class TestZBuffering extends Launcher
{
	private static final long serialVersionUID = -8374811509718775972L;

	/** The first polygon, typically colored red. */
	Polygon face1;
	/** The second polygon, typically colored blue. */
	Polygon face2;
	/** Angle used for rotating the polygons. Initialized in {@link #init()}. */
	float angle;

	/**
	 * Constructs a TestZBuffering launcher.
	 * 
	 * @param yangle The vertical field of view angle in radians.
	 * @param z_min The distance to the near clipping plane.
	 * @param z_max The distance to the far clipping plane.
	 * @param width The width of the rendering window.
	 * @param height The height of the rendering window.
	 */
	public TestZBuffering(double yangle, float z_min, float z_max, int width, int height)
	{
		super(yangle, z_min, z_max, width, height);
	}
	
	/**
	 * Initializes the test scene by creating two quadrilateral polygons.
	 * {@code face1} is red and {@code face2} is blue. Both are initially positioned
	 * at Z=0. The {@link #angle} for rotation is also initialized here to PI/6 radians.
	 * Vertex normals are set, though they might not be critical for this specific Z-buffering test
	 * if lighting is not the primary focus.
	 */
	@Override
	protected void init()
	{
		Vertex v1, v2, v3, v4;
		float sr3 = (float) (1.0f/Math.sqrt(3)); // For normalized diagonal normals (if used)

		// Define vertices for the first face (red)
		v1 = new Vertex(new float[]{1,1,0}, new float[]{0,0,1}, Color.red); // Simplified normal Z=1
		v2 = new Vertex(new float[]{-1,1,0}, new float[]{0,0,1}, Color.red);
		v3 = new Vertex(new float[]{-1,-1,0}, new float[]{0,0,1}, Color.red);
		v4 = new Vertex(new float[]{1,-1,0}, new float[]{0,0,1}, Color.red);
		face1 = new Polygon(new Vertex[]{v1,v2,v3,v4});

		// Define vertices for the second face (blue) - same position, different color
		v1 = new Vertex(new float[]{1,1,0}, new float[]{0,0,1}, Color.blue);
		v2 = new Vertex(new float[]{-1,1,0}, new float[]{0,0,1}, Color.blue);
		v3 = new Vertex(new float[]{-1,-1,0}, new float[]{0,0,1}, Color.blue);
		v4 = new Vertex(new float[]{1,-1,0}, new float[]{0,0,1}, Color.blue);
		face2 = new Polygon(new Vertex[]{v1,v2,v3,v4});
		
		// Initialize rotation angle
		angle = (float) (Math.PI/6.0); // Approx 30 degrees
	}

	/**
	 * Renders the scene for the current frame.
	 * It clears the renderer, sets a black background, and then renders the two faces
	 * with opposing Y-rotations and the standard camera translation ({@link #ztr}).
	 * This setup ensures the faces intersect and test the Z-buffer.
	 * 
	 * @param g The {@link Graphics} context to draw on (typically from the back buffer).
	 */
	@Override
	protected void doPaint(Graphics g)
	{
		renderer.clean(); // Clear image and Z-buffer
		g.setColor(Color.black);
		g.fillRect(0, 0, getWidth(), getHeight()); // Fill background

		// Render the first face (red), rotated by -angle around Y
		a.loadIdentity();
		a.rotateY(-angle);
		a.translateTo(0,0,ztr); // Apply camera zoom
		if (face1 != null) renderer.render(g, face1);

		// Render the second face (blue), rotated by +angle around Y
		a.loadIdentity();
		a.rotateY(angle);
		a.translateTo(0,0,ztr); // Apply camera zoom
		if (face2 != null) renderer.render(g, face2);
		
		renderer.commit(g); // Draw the rendered image to the screen
	}
	
	/**
	 * The main method to start the TestZBuffering application.
	 * Sets up the JFrame, initializes the launcher with perspective settings,
	 * and makes the window visible with a double buffering strategy.
	 * 
	 * @param args Command line arguments (not used).
	 */
	public static void main(String[] args)
	{
		int width = 1000;
		int height = 750;
		// Perspective: PI/4 FOV, near plane -30, far plane -0.1 (closer far plane to make intersections more prominent)
		TestZBuffering main = new TestZBuffering(Math.PI/4.0, -30.0f, -0.1f, width, height);
		main.setTitle("Z-Buffering Test");
		main.setSize(width, height);
		main.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		main.setVisible(true);
		main.createBufferStrategy(2); // Setup double buffering
	}
}
