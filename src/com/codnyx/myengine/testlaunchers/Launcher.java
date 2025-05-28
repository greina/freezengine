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

/**
 * Abstract base class for creating test applications (launchers) for the 3D engine.
 * It provides a basic window (JFrame), a rendering loop (Runnable), and input handling
 * (KeyListener, MouseListener). Subclasses must implement {@link #init()} to set up
 * specific test scenes and {@link #doPaint(Graphics)} to define the rendering logic for each frame.
 * <p>
 * The launcher manages a {@link PolygonRenderer} for drawing polygons and an
 * {@link AffineTransformation} for model transformations. It also includes basic
 * camera controls (zoom via up/down arrow keys or W/S) and FPS display.
 * Mouse clicks can trigger hit testing via {@link PolygonRenderer#hitTest(int, int, PolygonRenderer.HitTestHandler)}.
 */
public abstract class Launcher extends JFrame implements Runnable, KeyListener, MouseListener{
	
	private static final long serialVersionUID = 1L;
	/** The main affine transformation for positioning models in the world. Accessible to subclasses. */
	protected AffineTransformation a;
	/** Flag to control the main rendering loop. */
	boolean isRunning = false;
	/** The polygon renderer instance used for drawing. Accessible to subclasses. */
	protected PolygonRenderer renderer;
	
	/** If true, attempts to smooth the simulation rate by introducing sleeps. Defaults to false. */
	private final static boolean SMOOTHSIMRATE = false;
	/** If true, uses a {@link VolatileImage} as a back buffer for rendering. Defaults to false. */
	private final static boolean USEVIRTUALIMAGE = false; // Note: VolatileImage usage requires careful handling of content loss.

	/**
	 * Constructs a new Launcher.
	 * Initializes the {@link PolygonRenderer} with the specified perspective settings and viewport dimensions.
	 * Sets up the JFrame, adds key and mouse listeners, and starts the rendering thread.
	 * 
	 * @param yangle The vertical field of view angle in radians for the perspective projection.
	 * @param z_min The distance to the near clipping plane.
	 * @param z_max The distance to the far clipping plane.
	 * @param width The width of the rendering window in pixels.
	 * @param height The height of the rendering window in pixels.
	 */
	public Launcher(double yangle, float z_min, float z_max, int width, int height)
	{
		this.renderer = new PolygonRenderer(width, height);
		this.renderer.setPerspective(yangle, z_min, z_max);
		this.a = renderer.getModelT(); // Get the model transformation matrix from the renderer
		isRunning = true;
		this.init(); // Call subclass-specific initialization
		this.addKeyListener(this);
		this.addMouseListener(this);
		new Thread(this).start(); // Start the rendering loop in a new thread
	}
	
	/**
	 * Abstract method to be implemented by subclasses for one-time setup of the test scene,
	 * such as creating meshes, lights, or other initial configurations.
	 */
	protected abstract void init();

	// int counter = -1; // Unused field
	// final float angle_step = (float) (Math.PI/360); // Unused field
	// float angle = 0; // Unused field
	
	/** Current Z-translation of the camera (negative values move camera away from origin along Z). Modified by input keys. */
	protected float ztr = -10; // Default camera Z position
	/** Duration of the last frame in nanoseconds. */
	long framelength = 0;
	/** VolatileImage used as a back buffer if USEVIRTUALIMAGE is true. */
	private VolatileImage volatileImg;
	
	/**
	 * The main rendering loop.
	 * Calculates FPS, handles frame rate smoothing (if enabled), and calls {@link #doPaint(Graphics)}
	 * to render the scene. It uses a BufferStrategy for double buffering.
	 * Displays FPS and camera Z position on screen.
	 */
	@Override
	public void run()
	{
		float fps = 0;
		long oldTick = 0;
		float maxfps = 0; // Tracks maximum FPS achieved during the session
		
		while(isRunning)
		{
			// Calculate smoothed FPS
			if(fps == 0) { // First frame or after a pause
				if(oldTick != 0) {
					framelength = System.nanoTime() - oldTick;
					if (framelength > 0) fps = (float) (1e9 / (float)framelength); else fps = 0;
				}
			} else {
				framelength = System.nanoTime() - oldTick;
				if (framelength > 0) {
					float newfps = (float) (1e9 / (float)framelength);
					fps = 0.05f * newfps + 0.95f * fps; // Exponential moving average
				}
			}
			
			// If window is not visible, pause to save resources
			if(!this.isVisible()) {
				try {
					Thread.sleep(1000); // Sleep for 1 second
					oldTick = System.nanoTime(); // Reset tick to avoid large framelength on resume
					continue;
				} catch (InterruptedException e1) {
					Thread.currentThread().interrupt(); // Restore interrupt status
					e1.printStackTrace();
					isRunning = false; // Optionally stop if interrupted
				}
			}
			
			// Optional frame rate smoothing
			if(SMOOTHSIMRATE && fps > 0) { // Avoid division by zero if fps is 0
				try {
					// Target frame duration based on current smoothed FPS, minus actual frame processing time
					long targetFrameNanos = (long)(1e9 / fps);
					long sleepMillis = (targetFrameNanos - framelength) / (long)1e6;
					if(sleepMillis > 0) {
						Thread.sleep(sleepMillis);
					}
				} catch (InterruptedException e1) {
					Thread.currentThread().interrupt();
					e1.printStackTrace();
				}
			}
			
			oldTick = System.nanoTime(); // Record time before rendering
			
			Graphics g;
			if(USEVIRTUALIMAGE)
			{
				// Ensure VolatileImage is valid and ready
				createBackBuffer(); // Recreate if null or incompatible
				do {
					   GraphicsConfiguration gc = this.getGraphicsConfiguration();
					   int valCode = volatileImg.validate(gc);
					   
					   if(valCode == VolatileImage.IMAGE_INCOMPATIBLE){
						   createBackBuffer(); // Image lost due to display mode change, recreate
					   }
					  
					   g = volatileImg.getGraphics(); // Get graphics from back buffer
					   doPaint(g); // Call subclass's core paint method
	
					   // Draw FPS and camera info
					   g.setColor(Color.white);
					   g.drawString(String.format("FPS: %.2f",fps), 50, 50);
					   g.drawString(String.format("Camera z=%.2f ",-ztr), 50, 70);
					   if(fps > maxfps) maxfps = fps;
					   
					   // Draw VolatileImage to the screen (using BufferStrategy from JFrame)
					   Graphics screenGraphics = getBufferStrategy().getDrawGraphics();
					   screenGraphics.drawImage(volatileImg, 0, 0, this);
					   screenGraphics.dispose(); // Release system resources
	
					   if (!getBufferStrategy().contentsLost()) {
						   getBufferStrategy().show(); // Flip/show buffer
					   }
				  } while(volatileImg.contentsLost()); // Repeat if content was lost during rendering
			}
			else // Standard BufferStrategy rendering
			{
				   g = getBufferStrategy().getDrawGraphics();
				   try {
					   doPaint(g); // Call subclass's core paint method
	
					   // Draw FPS and camera info
					   g.setColor(Color.white);
					   g.drawString(String.format("FPS: %.2f",fps), 50, 50);
					   g.drawString(String.format("Camera z=%.2f ",-ztr), 50, 70);
					   if(fps > maxfps) maxfps = fps;
					   
				   } finally {
					   g.dispose(); // Always dispose graphics context
				   }
				   
				   if (!getBufferStrategy().contentsLost()) {
					   getBufferStrategy().show(); // Flip/show buffer
				   }
			}
		}
	}

	/**
	 * Abstract method to be implemented by subclasses to perform the actual rendering
	 * for each frame. This is where polygons are transformed and drawn using the
	 * {@link #renderer}.
	 * 
	 * @param g The {@link Graphics} context to draw on. For double buffering, this will typically
	 *          be the graphics from the back buffer.
	 */
	protected abstract void doPaint(Graphics g);

	/**
	 * Creates or recreates the {@link VolatileImage} back buffer.
	 * This is called if {@link #USEVIRTUALIMAGE} is true, initially or if the
	 * VolatileImage becomes incompatible (e.g., due to display mode changes).
	 */
	private void createBackBuffer() 
	{
		GraphicsConfiguration gc = getGraphicsConfiguration();
		if (volatileImg == null || volatileImg.getWidth() != getWidth() || volatileImg.getHeight() != getHeight() || volatileImg.validate(gc) == VolatileImage.IMAGE_INCOMPATIBLE) {
			if (volatileImg != null) {
				volatileImg.flush(); // Release resources of old image
			}
			volatileImg = gc.createCompatibleVolatileImage(getWidth(), getHeight());
		}
	}

	/**
	 * Handles key typed events. Currently empty.
	 * @param e The KeyEvent.
	 */
	@Override
	public void keyTyped(KeyEvent e) {
		// Not used
	}

	/**
	 * Handles key pressed events.
	 * Implements basic camera zoom controls:
	 * <ul>
	 *   <li>Up arrow or 'W' key: Zooms in (increases {@link #ztr}).</li>
	 *   <li>Down arrow or 'S' key: Zooms out (decreases {@link #ztr}).</li>
	 * </ul>
	 * @param e The KeyEvent.
	 */
	@Override
	public void keyPressed(KeyEvent e) 
	{
		if(e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_W)
			this.ztr += .2f;
		if(e.getKeyCode() == KeyEvent.VK_DOWN || e.getKeyCode() == KeyEvent.VK_S)
			this.ztr -= .2f;		
	}

	/**
	 * Handles key released events. Currently empty.
	 * @param e The KeyEvent.
	 */
	@Override
	public void keyReleased(KeyEvent e) {
		// Not used
	}
	
	/**
	 * Handles mouse entered events. Currently empty.
	 * @param e The MouseEvent.
	 */
	@Override
	public void mouseEntered(MouseEvent e)
	{
		// Not used
	}
	
	/**
	 * Handles mouse exited events. Currently empty.
	 * @param e The MouseEvent.
	 */
	@Override
	public void mouseExited(MouseEvent e)
	{
		// Not used
	}
	
	/**
	 * Handles mouse pressed events. Currently empty.
	 * @param e The MouseEvent.
	 */
	@Override
	public void mousePressed(MouseEvent e)
	{
		// Not used
	}

	/**
	 * Handles mouse released events. Currently empty.
	 * @param e The MouseEvent.
	 */
	@Override
	public void mouseReleased(MouseEvent e)
	{
		// Not used
	}
	
	/**
	 * Handles mouse clicked events.
	 * Initiates a hit test at the click coordinates (x, y) using the {@link #renderer}
	 * and the internal {@link #handler}.
	 * @param e The MouseEvent.
	 */
	@Override
	public void mouseClicked(MouseEvent e)
	{
		int x = e.getX();
		int y = e.getY();
		// Ensure renderer is not null before calling hitTest
		if (renderer != null) {
			renderer.hitTest(x, y, handler);
		}
	}

	
	/**
	 * An instance of {@link PolygonRenderer.HitTestHandler} to process and print information
	 * about polygons found at the mouse click location.
	 * It attempts to unproject the 2D click coordinates back to 3D space on the hit polygon's plane.
	 */
	PolygonRenderer.HitTestHandler handler = new PolygonRenderer.HitTestHandler()
	{
		int color_hit; // Renamed to avoid conflict with java.awt.Color
		float[] coord3D = {Float.NEGATIVE_INFINITY,Float.NEGATIVE_INFINITY,Float.NEGATIVE_INFINITY};
		int x_hit, y_hit; // Renamed to avoid conflict with parameters in hit()
		float depth_hit;  // Renamed for clarity

		/**
		 * Callback method for hit testing. Stores information about the hit polygon.
		 * @param x The x-coordinate of the hit.
		 * @param y The y-coordinate of the hit.
		 * @param depth The depth of the polygon at (x,y).
		 * @param color The color of the polygon at (x,y).
		 * @param p The polygon that was hit.
		 */
		@Override
		public void hit(int x, int y, float depth, int color, Polygon p)
		{
			if(p != null && renderer != null && renderer.getProjT() != null && renderer.getModelT() != null)
			{
				float[] u_ray_dir_eye = {0,0,0}; // Direction vector for the ray in eye space
				float[] normal_eye = p.getNormal().clone();
				float[] center_eye = p.getCenter().clone();
				
				// Transform polygon normal and center to eye space using the renderer's current model transform
				renderer.getModelT().transform(center_eye);
				renderer.getModelT().normal_transform(normal_eye);
				
				// Get a direction vector in eye space corresponding to screen (x,y)
				// MyMath.init(-1,u_ray_dir_eye); // Original init, purpose of -1 unclear
				// aTrasform's output meaning is a bit ambiguous. Assuming it gives view plane coords.
				// For a proper unprojection to a ray, one would typically use inverse projection.
				// Let's assume aTrasform gives components proportional to x_eye/z_eye, y_eye/z_eye
				renderer.getProjT().aTrasform(x, y, u_ray_dir_eye); 
				// u_ray_dir_eye might need to be {u_ray_dir_eye[0], u_ray_dir_eye[1], -1} or similar for a direction
				// and then normalized if it's a direction.
				// The original code MyMath.init(-1,u) then aTransform then scale is complex.
				// A more standard unprojection:
				// 1. Convert (x,y) to NDC.
				// 2. Use inverse projection matrix to get ray direction in eye space.
				// 3. Intersect ray O_eye=0, Dir_eye with plane N_eye . (X - C_eye) = 0
				// t = (N_eye . C_eye) / (N_eye . Dir_eye)
				// Intersection_eye = t * Dir_eye

				// The original scaling logic: MyMath.scale(MyMath.dotProduct(n, c)/MyMath.dotProduct(u, n), u, coord3D);
				// This assumes 'u' is a direction vector and computes intersection.
				// Let's attempt to replicate it with clearer variable names.
				// u_ray_dir_eye is effectively the direction component from aTransform.
				// For it to be a direction vector, it needs a Z component, often -1 or -focal_length for eye space rays.
				// Let's assume u_ray_dir_eye from aTransform implicitly works with the scaling math below.
				// A proper setup might be:
				// float[] ray_dir_for_calc = {u_ray_dir_eye[0], u_ray_dir_eye[1], -1.0f}; // Assuming -Z is into screen
				// MyMath.normalize(ray_dir_for_calc);
				// float NdotRay = MyMath.dotProduct(normal_eye, ray_dir_for_calc);
				// if (Math.abs(NdotRay) > 1e-6) {
				//    float t = MyMath.dotProduct(normal_eye, center_eye) / NdotRay;
				//    MyMath.scale(t, ray_dir_for_calc, coord3D);
				// }
				// The original logic:
				float denom = MyMath.dotProduct(u_ray_dir_eye, normal_eye);
				if (Math.abs(denom) > 1e-6f) { // Avoid division by zero
					MyMath.scale(MyMath.dotProduct(normal_eye, center_eye) / denom, u_ray_dir_eye, coord3D);
				} else {
					// Ray is parallel to plane or error, set to a default
					coord3D[0] = coord3D[1] = coord3D[2] = Float.NaN;
				}
			} else {
				coord3D[0] = coord3D[1] = coord3D[2] = Float.NaN;
			}
			this.color_hit = color;
			this.x_hit = x;
			this.y_hit = y;
			this.depth_hit = depth;
		}
		
		/**
		 * Called after rendering to process and typically print the hit test results.
		 */
		@Override
		public void commit() 
		{
			System.out.println(String.format("Click on (%d,%d)->(%.3f,%.3f,%.3f): color=rgba-0x%02X%02X%02X%02X depth=%.4f", 
					x_hit,y_hit,
					coord3D[0], coord3D[1], coord3D[2], 
					ColorUtils.getRed(color_hit), ColorUtils.getGreen(color_hit), ColorUtils.getBlue(color_hit),ColorUtils.getAlpha(color_hit), 
					depth_hit));
		}
	};
}
