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

/**
 * A test launcher that demonstrates loading and displaying a 3D model from a Wavefront OBJ file.
 * It extends the base {@link Launcher} class, initializing a specific mesh (a cow model embedded as a resource)
 * and applying continuous Y-axis rotation to it in the rendering loop.
 * The camera can be zoomed using the standard Launcher controls (up/down arrow keys or W/S).
 */
public class ObjTestLauncher extends Launcher
{
	private static final long serialVersionUID = 110959145651340442L;
	
	/** The mesh object loaded from the OBJ file. */
	Mesh m;

	/**
	 * Constructs an ObjTestLauncher.
	 * 
	 * @param yangle The vertical field of view angle in radians.
	 * @param z_min The distance to the near clipping plane.
	 * @param z_max The distance to the far clipping plane.
	 * @param width The width of the rendering window.
	 * @param height The height of the rendering window.
	 */
	public ObjTestLauncher(double yangle, float z_min, float z_max, int width, int height)
	{
		super(yangle,z_min,z_max, width, height);
	}
		
	/**
	 * Initializes the test scene by loading the "cow.obj" model from the classpath resources.
	 * The loaded mesh is stored in the {@link #m} field.
	 * If an IOException occurs during parsing, an error is printed to the console.
	 */
	@Override
	protected void init()
	{
			InputStream stream = this.getClass().getResourceAsStream("/cow.obj");
			if (stream == null) {
				System.err.println("Could not find resource /cow.obj. Make sure it's in the classpath.");
				// Initialize m to an empty mesh to prevent NullPointerExceptions later
				m = new Mesh(); 
				return;
			}
			try {
				this.m = new ObjParser().parseStream(new BufferedReader(new InputStreamReader(stream)));
			} catch (IOException e) {
				System.err.println("Error loading or parsing OBJ file /cow.obj:");
				e.printStackTrace();
				m = new Mesh(); // Initialize to empty mesh on error
			}
	}	

	// int counter = -1; // Unused field
	/** The step size for angle increment per frame, used for rotation. */
	final float angle_step = (float) (Math.PI/360);
	/** Current angle of rotation, incremented each frame. */
	float angle = 0;
	
	/**
	 * Renders the scene for the current frame.
	 * It clears the renderer, sets a black background, updates the rotation angle,
	 * applies transformations (rotation and translation based on {@link #ztr}),
	 * renders the loaded mesh ({@link #m}), and then commits the frame to the screen.
	 * 
	 * @param g The {@link Graphics} context to draw on (typically from the back buffer).
	 */
	@Override
	protected void doPaint(Graphics g) 
	{
		renderer.clean(); // Clear internal image and Z-buffer
		g.setColor(Color.black);
		g.fillRect(0, 0, getWidth(), getHeight()); // Use getWidth()/getHeight() for dynamic window size

		angle += angle_step; // Increment rotation angle
		
		// Set up model transformation: identity -> rotate -> translate
		a.loadIdentity();
//		a.rotateX(angle); // Example X rotation (commented out)
		a.rotateY(2*angle); // Apply Y rotation
//		a.rotateZ(4*angle); // Example Z rotation (commented out)
		a.translateTo(0,0,ztr); // Apply camera zoom translation
		
		if (m != null) { // Ensure mesh was loaded
			m.render(renderer, g); // Render the mesh
		}
		
		renderer.commit(g); // Draw the rendered image to the screen
	}

	/**
	 * The main method to start the ObjTestLauncher application.
	 * It creates a new ObjTestLauncher instance, sets its size, default close operation,
	 * makes it visible, and creates a double buffering strategy.
	 * 
	 * @param args Command line arguments (not used).
	 */
	public static void main(String[] args)
	{
		int width = 1000;
		int height = 750;
		// Perspective: PI/4 FOV, near plane -30, far plane -1
		ObjTestLauncher main = new ObjTestLauncher(Math.PI/4.0, -30f, -1f, width, height);
		main.setTitle("OBJ Test Launcher - Cow Model");
		main.setSize(width, height);
		main.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		main.setVisible(true);
		main.createBufferStrategy(2); // Setup double buffering
	}
}
