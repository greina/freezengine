package com.codnyx.myengine.testlaunchers;
import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JFrame;

import com.codnyx.myengine.Polygon;
import com.codnyx.myengine.Vertex;

/**
 * A test launcher for demonstrating basic polygon rendering and transformations.
 * This class extends {@link Launcher} to create a scene with a colored cube and a separate quadrilateral face.
 * Both the cube and the face undergo continuous rotations and translations to showcase the 3D engine's
 * capabilities in handling multiple objects with independent transformations.
 * Camera zoom is available via standard Launcher controls.
 */
public class PolyTestLauncher extends Launcher
{
	private static final long serialVersionUID = -1058628550412652050L;

	/** The array of polygons forming the cube. */
	Polygon[] cube;
	/** A separate quadrilateral polygon. */
	private Polygon face;
	
	/**
	 * Constructs a PolyTestLauncher.
	 * 
	 * @param yangle The vertical field of view angle in radians.
	 * @param z_min The distance to the near clipping plane.
	 * @param z_max The distance to the far clipping plane.
	 * @param width The width of the rendering window.
	 * @param height The height of the rendering window.
	 */
	public PolyTestLauncher(double yangle, float z_min, float z_max, int width, int height)
	{
		super(yangle, z_min, z_max, width, height);
	}
		
	/**
	 * Initializes the test scene by creating the vertices and polygons for a cube and a separate face.
	 * The cube has differently colored vertices to aid in visualizing its orientation.
	 * The normals for the cube vertices are set pointing outwards from the origin, assuming a unit cube centered at origin.
	 * The separate face also has colored vertices.
	 */
	@Override
	protected void init()
	{
		cube = new Polygon[6]; // A cube has 6 faces
		float sr3 = (float) (1.0f/Math.sqrt(3)); // Pre-calculate 1/sqrt(3) for normalized diagonals
		
		// Define vertices for a cube [-1,1] on each axis
		Vertex v1 = new Vertex(new float[]{1,1,1},   new float[]{sr3, sr3, sr3},   Color.red);
		Vertex v2 = new Vertex(new float[]{-1,1,1},  new float[]{-sr3, sr3, sr3},  Color.yellow);
		Vertex v3 = new Vertex(new float[]{-1,-1,1}, new float[]{-sr3, -sr3, sr3}, Color.blue);
		Vertex v4 = new Vertex(new float[]{1,-1,1},  new float[]{sr3, -sr3, sr3},  Color.green);
		Vertex v5 = new Vertex(new float[]{1,1,-1},   new float[]{sr3, sr3, -sr3},  Color.magenta);
		Vertex v6 = new Vertex(new float[]{1,-1,-1},  new float[]{sr3, -sr3, -sr3}, Color.orange);
		Vertex v7 = new Vertex(new float[]{-1,-1,-1}, new float[]{-sr3, -sr3, -sr3},Color.cyan);
		Vertex v8 = new Vertex(new float[]{-1,1,-1},  new float[]{-sr3, sr3, -sr3}, Color.pink);

		// Define faces of the cube using the vertices
		// Each polygon takes an array of vertices. The order matters for front/back face determination if normals are auto-computed by Polygon.
		cube[0] = new Polygon(new Vertex[]{v1,v2,v3,v4}); // Front face (+Z)
		cube[1] = new Polygon(new Vertex[]{v5,v6,v7,v8}); // Back face (-Z) - careful with winding for culling if v5,v6,v7,v8 is used directly
		// For correct backface culling with typical view from outside:
		// cube[1] = new Polygon(new Vertex[]{v8,v7,v6,v5}); // Back face (-Z), ensuring CCW from outside
		cube[2] = new Polygon(new Vertex[]{v1,v4,v6,v5}); // Right face (+X)
		cube[3] = new Polygon(new Vertex[]{v8,v7,v3,v2}); // Left face (-X)
		cube[4] = new Polygon(new Vertex[]{v1,v5,v8,v2}); // Top face (+Y)
		cube[5] = new Polygon(new Vertex[]{v4,v3,v7,v6}); // Bottom face (-Y) - corrected winding

		// Define a separate face (quadrilateral)
		Vertex fv1 = new Vertex(new float[]{1,1,0}, new float[]{0, 0, 1}, Color.red);    // Normals for this face could be simply (0,0,1) if it's flat on XY plane
		Vertex fv2 = new Vertex(new float[]{-1,1,0}, new float[]{0, 0, 1}, Color.blue);
		Vertex fv3 = new Vertex(new float[]{-1,-1,0}, new float[]{0, 0, 1}, Color.green);
		Vertex fv4 = new Vertex(new float[]{1,-1,0}, new float[]{0, 0, 1}, Color.cyan);
		face = new Polygon(new Vertex[]{fv1,fv2,fv3,fv4});
	}	
	
	/** The step size for angle increment per frame, used for rotation. */
	final float angle_step = (float) (Math.PI/360);
	/** Current angle of rotation, incremented each frame. */
	float angle = 0;
	
	/**
	 * Renders the scene for the current frame.
	 * Clears the renderer, sets a black background, updates the rotation angle,
	 * and applies various transformations to the cube and the separate face before rendering them.
	 * 
	 * @param g The {@link Graphics} context to draw on (typically from the back buffer).
	 */
	@Override
	protected void doPaint(Graphics g) 
	{
		renderer.clean(); // Clear image and Z-buffer
		g.setColor(Color.black);
		g.fillRect(0, 0, getWidth(), getHeight()); // Fill background

		angle += angle_step; // Increment rotation angle
		
		// --- Render the Cube ---
		a.loadIdentity(); // Reset model transformation matrix
//		a.rotateX(angle); // Optional X rotation
		a.rotateY(angle);   // Apply Y rotation to the cube
//		a.rotateZ(4*angle); // Optional Z rotation
		a.translateTo(0,0,ztr); // Translate cube based on camera zoom
		for(Polygon poly : cube) { // Use a different variable name to avoid conflict with 'face' field
			if (poly != null) renderer.render(g,poly);
		}
				
		// --- Render the first instance of the separate face ---
		a.loadIdentity();
		a.rotateZ(angle);         // Rotate around Z
		a.translateTo(5,0,ztr);   // Translate it to the right and apply camera zoom
		a.rotateZ(angle);         // Rotate again around its new local Z (after translation)
		if (face != null) renderer.render(g, face);

		// --- Render the second instance of the separate face ---
		a.loadIdentity();
		a.translateTo(0,0,ztr/2); // Translate closer to the camera
		a.rotateY(angle);         // Rotate around Y
		a.translateTo(0,0,ztr);   // Then translate it further back relative to its new orientation
		if (face != null) renderer.render(g, face);
		
		renderer.commit(g); // Draw the rendered image to the screen
	}
	
	/**
	 * The main method to start the PolyTestLauncher application.
	 * Sets up the JFrame, initializes the launcher with perspective settings,
	 * and makes the window visible with a double buffering strategy.
	 * 
	 * @param args Command line arguments (not used).
	 */
	public static void main(String[] args)
	{
		int width = 1000;
		int height = 750;
		PolyTestLauncher main = new PolyTestLauncher(Math.PI/4.0, -30f, -1f, width, height);
		main.setTitle("Polygon Test Launcher - Cube and Faces");
		main.setSize(width, height);
		main.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		main.setVisible(true);
		main.createBufferStrategy(2); // Setup double buffering
	}
}
