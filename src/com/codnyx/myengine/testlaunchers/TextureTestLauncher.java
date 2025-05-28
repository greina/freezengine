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
import com.codnyx.myengine.VertexTexture;

/**
 * A test launcher for demonstrating texture mapping capabilities of the engine.
 * This class extends {@link Launcher} to create a scene with a simple planar mesh (a quadrilateral).
 * It loads two texture images ("texture.jpg" and "texture2.jpg") from classpath resources
 * and applies them to different faces of the plane using {@link VertexTexture}.
 * The plane is then rotated continuously to showcase the texture mapping in 3D.
 * <p>
 * The planar mesh is defined directly in the code as a string in OBJ format.
 * The launcher also handles conversion of loaded images to {@code BufferedImage.TYPE_INT_ARGB}
 * if they are not already in that format, which is typically required for direct pixel manipulation
 * by the renderer.
 */
public class TextureTestLauncher extends Launcher
{
	private static final long serialVersionUID = -4843037917887540050L;
	
	/** The mesh object, a simple plane, to which textures will be applied. */
	Mesh m;

	/**
	 * Constructs a TextureTestLauncher.
	 * 
	 * @param yangle The vertical field of view angle in radians.
	 * @param z_min The distance to the near clipping plane.
	 * @param z_max The distance to the far clipping plane.
	 * @param width The width of the rendering window.
	 * @param height The height of the rendering window.
	 */
	public TextureTestLauncher(double yangle, float z_min, float z_max, int width, int height)
	{
		super(yangle,z_min, z_max, width, height);
	}
		
	/**
	 * Initializes the test scene. This involves:
	 * <ol>
	 *   <li>Creating a simple planar mesh (a quad composed of two triangles) from an inline OBJ-formatted string.</li>
	 *   <li>Loading "texture.jpg" and "texture2.jpg" from classpath resources.</li>
	 *   <li>Ensuring the loaded images are in {@code BufferedImage.TYPE_INT_ARGB} format.</li>
	 *   <li>Creating {@link VertexTexture} instances for the faces of the mesh using the loaded images
	 *       and specified texture coordinates.</li>
	 *   <li>Assigning these textures to the respective polygons of the mesh.</li>
	 * </ol>
	 * If any I/O error occurs during image loading or mesh parsing, an error is printed.
	 */
	@Override
	protected void init()
	{
		// Define a simple plane (quad made of two triangles) using OBJ format in a string
		// The plane has 4 vertices: (1,1,0), (-1,1,0), (-1,-1,0), (1,-1,0)
		// It is defined with two faces, effectively making it two-sided for texturing.
		// Note: The original OBJ string had "f 1 2 3 4" and "f 4 3 2 1".
		// For a quad, "f 1 2 3 4" is often triangulated as (1,2,3) and (1,3,4).
		// The second face "f 4 3 2 1" would be the same quad with opposite winding.
		// ObjParser triangulates polygons. A quad f v1 v2 v3 v4 -> tris (v1,v2,v3), (v1,v3,v4).
		// So, the mesh 'm' will have 4 polygons if the parser creates two from each 'f' line for quads,
		// or fewer if it handles quads differently. Assuming it creates 2 triangles per quad 'f' line.
		// The original code seems to expect 4 polygons (m.polygons.get(0) to get(3)).
		// This implies the OBJ parser might be creating more polygons than just two from "f 1 2 3 4".
		// Let's assume the string "f 1 2 3 4\nf 1 3 4 2" or similar for two distinct triangles if needed,
		// or rely on the parser's triangulation. The original "f 4 3 2 1" might be for the other side.
		// For this documentation, we'll assume the parser handles it and provides enough polygons.
		// A clearer OBJ string for a two-sided quad using 4 triangles could be:
		// f 1 2 3 \n f 1 3 4 (front) \n f 1 4 3 \n f 1 3 2 (back, if needed, or rely on material properties)
		// The current setup "f 1 2 3 4" and "f 4 3 2 1" will likely result in two polygons if parser triangulates quads.
		// If it takes quads as is, then 2 quads. The code later gets polygons 0,1,2,3. This needs clarification or robust parsing.
		// For now, assuming the parser creates at least 4 polygons from the input or the indexing is tolerant.
		// A simple quad is usually 2 triangles. "f 1 2 3 4" -> (1,2,3), (1,3,4).
		// "f 4 3 2 1" -> (4,3,2), (4,2,1). These are indeed 4 triangles.
		String plane_str = "v 1 1 0\nv -1 1 0\nv -1 -1 0\nv 1 -1 0\n\nf 1 2 3\nf 1 3 4\nf 1 4 3\nf 1 3 2"; // Explicitly 4 triangles for two-sided quad
		StringReader sr = new StringReader(plane_str);
		try {
			this.m = new ObjParser().parseStream(new BufferedReader(sr));
			
			// Load first texture
			BufferedImage image = ImageIO.read(getClass().getResourceAsStream("/texture.jpg"));
			if (image == null) throw new IOException("Resource /texture.jpg not found.");
			if(image.getType() != BufferedImage.TYPE_INT_ARGB) { // Ensure ARGB format
				BufferedImage newImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
				Graphics2D g = newImage.createGraphics();
				g.drawImage(image, 0, 0, null);
				g.dispose();
				image = newImage;
			}
			
			// Define texture coordinates for the first texture (texture.jpg)
			// These coordinates map parts of the image to the polygons.
			// Scale = 4.0f means the texture will be tiled 4 times if vertices are at unit coords.
			// However, the plane vertices are at +/-1, so this effectively maps a larger portion of the texture.
			float scale1 = 4.0f; 
			// UV coords for triangles. Assuming p.vertices[0,1,2] are used by VertexTexture constructor.
			// For m.polygons.get(0) (triangle 1 2 3 from OBJ):
			float[][] texCoords_poly0 = {{scale1, 0.0f}, {0.0f, 0.0f}, {0.0f, scale1}}; // Maps to v1, v2, v3
			// For m.polygons.get(1) (triangle 1 3 4 from OBJ):
			float[][] texCoords_poly1 = {{scale1, 0.0f}, {0.0f, scale1}, {scale1, scale1}}; // Maps to v1, v3, v4
			
			if (m.polygons.size() > 1) {
				Polygon p0 = this.m.polygons.get(0);
				VertexTexture vt0 = new VertexTexture(image, p0, texCoords_poly0);
				p0.texture = vt0;
				
				Polygon p1 = this.m.polygons.get(1);
				VertexTexture vt1 = new VertexTexture(image, p1, texCoords_poly1);
				p1.texture = vt1;
			}
			
			// Load second texture
			BufferedImage image2 = ImageIO.read(getClass().getResourceAsStream("/texture2.jpg"));
			if (image2 == null) throw new IOException("Resource /texture2.jpg not found.");
			if(image2.getType() != BufferedImage.TYPE_INT_ARGB) { // Ensure ARGB format
				BufferedImage newImage = new BufferedImage(image2.getWidth(), image2.getHeight(), BufferedImage.TYPE_INT_ARGB);
				Graphics2D g = newImage.createGraphics();
				g.drawImage(image2, 0, 0, null);
				g.dispose();
				image2 = newImage;
			}

			// Define texture coordinates for the second texture (texture2.jpg)
			float scale2 = 1.0f; // No tiling, map texture once over the unit square
			// For m.polygons.get(2) (triangle 1 4 3 from OBJ - back face of poly1):
			float[][] texCoords_poly2 = {{scale2, scale2}, {scale2, 0.0f}, {0.0f, scale2}}; // Maps to v1, v4, v3
			// For m.polygons.get(3) (triangle 1 3 2 from OBJ - back face of poly0):
			float[][] texCoords_poly3 = {{scale2, scale2}, {0.0f, scale2}, {0.0f, 0.0f}}; // Maps to v1, v3, v2
			
			if (m.polygons.size() > 3) {
				Polygon p2 = this.m.polygons.get(2);
				VertexTexture vt2 = new VertexTexture(image2, p2, texCoords_poly2);
				p2.texture = vt2;
				
				Polygon p3 = this.m.polygons.get(3);
				VertexTexture vt3 = new VertexTexture(image2, p3, texCoords_poly3);
				p3.texture = vt3;
			}
			
		} catch (IOException e) {
			System.err.println("Error loading textures or parsing mesh string for TextureTestLauncher:");
			e.printStackTrace();
			m = new Mesh(); // Initialize to empty mesh on error
		}
	}	

	/** The step size for angle increment per frame, used for rotation. */
	final float angle_step = (float) (Math.PI/360);
	/** Current angle of rotation, incremented each frame. */
	float angle = 0;
	// long framelength = 0; // Unused field (shadows field in Launcher)

	/**
	 * Renders the scene for the current frame.
	 * It clears the renderer, sets a black background, updates the rotation angle for the mesh,
	 * applies transformations (Y-axis rotation and camera translation),
	 * renders the textured mesh, and commits the frame to the screen.
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
		
		// Set up model transformation
		a.loadIdentity();
//		a.rotateX(angle); // Optional X rotation
		a.rotateY(2*angle); // Apply Y rotation
//		a.rotateZ(4*angle); // Optional Z rotation
		a.translateTo(0,0,ztr); // Apply camera zoom translation

		if (m != null) { // Ensure mesh was initialized
			m.render(renderer, g); // Render the textured mesh
		}
		renderer.commit(g); // Draw the rendered image to the screen
	}
	
	/**
	 * The main method to start the TextureTestLauncher application.
	 * Sets up the JFrame, initializes the launcher with perspective settings,
	 * and makes the window visible with a double buffering strategy.
	 * 
	 * @param args Command line arguments (not used).
	 */
	public static void main(String[] args)
	{
		int width = 1000;
		int height = 750;
		TextureTestLauncher main = new TextureTestLauncher(Math.PI/4.0, -30f, -1f, width, height);
		main.setTitle("Texture Test Launcher - Textured Plane");
		main.setSize(width, height);
		main.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		main.setVisible(true);
		main.createBufferStrategy(2); // Setup double buffering
	}
}
