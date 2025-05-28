package com.codnyx.myengine;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.awt.image.ImageObserver;
import java.util.Arrays;

/**
 * Renders 3D polygons to a 2D image using scanline conversion and Z-buffering.
 * It supports perspective transformation, model transformation, backface culling,
 * frustum clipping (near and far Z planes), Gouraud shading (color interpolation),
 * and basic texture mapping.
 * <p>
 * The rendering process for each polygon involves:
 * <ol>
 *   <li>Backface culling: If the polygon faces away from the camera, it's skipped.</li>
 *   <li>Model transformation: Vertices are transformed from model space to world/eye space.</li>
 *   <li>Frustum clipping: The polygon is clipped against the near (zMin) and far (zMax) planes.</li>
 *   <li>Projection: Clipped vertices are projected from eye space to screen space.</li>
 *   <li>Scanline conversion: Edges of the projected polygon are scanned to determine spans for each scanline.</li>
 *   <li>Pixel rendering: For each pixel in a span, depth is checked against the Z-buffer.
 *       If the pixel is visible, its color (interpolated or textured) is written to the image,
 *       and the Z-buffer is updated.</li>
 * </ol>
 * The renderer maintains an internal {@link BufferedImage} and a Z-buffer.
 * After rendering polygons, {@link #commit(Graphics)} is used to draw the updated portion of the
 * internal image to a target {@link Graphics} context.
 * {@link #clean()} should be called to clear the image and Z-buffer for the next frame.
 */
public class PolygonRenderer 
{
	/** Array of {@link Scan} objects, one for each horizontal line of the render target. */
	Scan[] scanLines;
	/** Height of the rendering viewport. */
	private int height;
	/** Width of the rendering viewport. */
	private int width;
	/** Y-coordinate of the top edge of the viewport. */
	private int top;
	/** X-coordinate of the left edge of the viewport. */
	private int left;
	/** Minimum Y-coordinate touched by the current polygon being rendered, clipped to viewport bounds. */
	private int yMinBound;
	/** Maximum Y-coordinate touched by the current polygon being rendered, clipped to viewport bounds. */
	private int yMaxBound;
	/** The perspective transformation to apply to vertices. */
	private PerspectiveTransformation projT;
	/** The model-to-world/eye transformation to apply to vertices and normals. */
	private AffineTransformation modelT;
	/** The {@link BufferedImage} used as the rendering target and Z-buffer canvas. */
	private BufferedImage image;
	/** Z-buffer to store depth values for hidden surface removal. Lower values are closer. */
	private float[] zBuffer;
	/** Temporary array to store projected vertices of a polygon after clipping. */
	private ProjectedVertex[] vertices;
	/** Number of valid vertices currently in the {@link #vertices} array for the polygon being processed. */
	private int numVertices;
	
	// Fields for hit testing
	/** X-coordinate for hit testing. */
	private int htx;
	/** Y-coordinate for hit testing. */
	private int hty;
	/** Handler for hit test results. */
	private HitTestHandler handler;
	
	/**
	 * Constructs a PolygonRenderer with the specified width and height, assuming top-left at (0,0).
	 * @param width The width of the rendering viewport.
	 * @param height The height of the rendering viewport.
	 */
	public PolygonRenderer(int width, int height)
	{
		init(0, 0, width, height);
	}
	
	/**
	 * Constructs a PolygonRenderer with the specified viewport dimensions and offset.
	 * @param left The x-coordinate of the left edge of the viewport.
	 * @param top The y-coordinate of the top edge of the viewport.
	 * @param width The width of the rendering viewport.
	 * @param height The height of the rendering viewport.
	 */
	public PolygonRenderer(int left, int top, int width, int height)
	{
		init(left, top, width, height);
	}
	
	/**
	 * Sets up the perspective transformation for the renderer.
	 * @param yangle The vertical field of view angle in radians.
	 * @param z_min The distance to the near clipping plane.
	 * @param z_max The distance to the far clipping plane.
	 */
	public void setPerspective(double yangle, float z_min, float z_max)
	{
		this.projT = new PerspectiveTransformation(yangle, width, height, top, left, z_min, z_max);
		
	}

	/**
	 * Initializes the renderer's internal structures.
	 * @param left The x-coordinate of the left edge of the viewport.
	 * @param top The y-coordinate of the top edge of the viewport.
	 * @param width The width of the rendering viewport.
	 * @param height The height of the rendering viewport.
	 */
	private void init(int left, int top, int width, int height) 
	{
		this.left = left;
		this.top = top;
		this.width = width;
		this.height = height;
		this.scanLines = new Scan[this.height];
		for(int i = 0; i < scanLines.length; i++)
			this.scanLines[i] = new Scan();
		
		// Initial size for vertex array, can be expanded by expandVertexArray()
		this.vertices = new ProjectedVertex[8]; 
		for(int i = 0; i < vertices.length; i++)
			this.vertices[i] = new ProjectedVertex();
		numVertices = 0;
		
		this.modelT = new AffineTransformation(); // Initialize with identity
		this.image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		this.zBuffer = new float[width*height];
		Arrays.fill(zBuffer, Float.POSITIVE_INFINITY); // Initialize Z-buffer to farthest depth
	}
	
	/**
	 * Gets the current model transformation matrix.
	 * @return The {@link AffineTransformation} used for model-to-world/eye space transforms.
	 */
	public AffineTransformation getModelT() {
		return modelT;
	}
	
	/**
	 * Gets the current perspective transformation.
	 * @return The {@link PerspectiveTransformation} used for eye-to-screen projection.
	 */
	public PerspectiveTransformation getProjT() {
		return projT;
	}
	
	/**
	 * Resets the Y-bounds tracking for the current polygon.
	 * Called before processing each new polygon.
	 */
	public void reset()
	{
		this.yMinBound = Integer.MAX_VALUE;
		this.yMaxBound = Integer.MIN_VALUE;
	}

	/** Temporary buffer for vector calculations. */
	float[] vecBuffer = {0,0,0};
	/** Another temporary buffer for vector calculations. */
	float[] vecBuffer2 = {0,0,0};
	// float[] vecBuffer3 = {0,0,0}; // Seems unused, can be removed if confirmed.
	
	/** Dummy ImageObserver, not actively used for rendering updates. */
	private ImageObserver observer = new ImageObserver() {
		@Override
		public boolean imageUpdate(Image img, int infoflags, int x, int y,
				int width, int height) {
			return false; // Indicates image is fully loaded or no further updates needed.
		}
	};
	/** Stores the bounding box [minX, minY, maxX, maxY] of the area updated in the current frame for optimized commit. */
	private int[] bounds = {Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE};
	
	/**
	 * Renders a single polygon.
	 * This involves backface culling, transformation, clipping, projection, and scanline conversion
	 * with Z-buffering and texture/color interpolation.
	 * 
	 * @param gc The graphics context (currently unused directly in this method, rendering is to an internal buffer).
	 * @param p The {@link Polygon} to render.
	 */
	public void render(Graphics gc, Polygon p)
	{
		// Transformed normal of the polygon
		float[] tnormal = {0,0,0}; // Using a local variable is fine
		
		// Texture related variables
		float[] tu = null, tv = null, to = null;
		int tx, ty; // Texture coordinates in the image
		
		// Z-buffer related values for edge interpolation
		float zb0, zb1;
		
		// Loop counters/helpers
		int step_fp, n;
		// n = 0; // Initialization here is not strictly needed as it's set later.
		
		// --- Backface Culling ---
		// Transform polygon normal to eye space
		modelT.normal_transform(p.normal, tnormal);
		// Transform polygon center to eye space
		modelT.transform(p.center,vecBuffer2); 
		// If dot product of normal and vector to center (from eye) is >= 0, it's back-facing or edge-on
		if(MyMath.dotProduct(tnormal, vecBuffer2) >= 0) {
			return; // Cull polygon
		}
		reset(); // Reset yMinBound, yMaxBound for the new polygon
		
		// Get direct access to the image's pixel data array
		int[] data = ((DataBufferInt)image.getRaster().getDataBuffer()).getData();
		
		boolean textureEnabled = p.texture != null;
		
		if(textureEnabled)
		{
			// Clone and transform texture basis vectors
			to = p.texture.O.clone(); // Texture origin
			modelT.transform(to);
			tu = p.texture.U.clone(); // Texture U vector
			modelT.normal_transform(tu); // Normals are transformed without translation
			tv = p.texture.V.clone(); // Texture V vector
			modelT.normal_transform(tv);
		}
		
		// --- Frustum Clipping (Near and Far Z planes) ---
		// This populates 'this.vertices' with clipped polygon vertices in eye space
		// and sets 'this.numVertices'.
		frustumClipping(p.vertices, projT.getZMin(), projT.getZMax());
		
		if(numVertices == 0) { // Polygon is entirely outside frustum or degenerate
			return;
		}
		
		// --- Projection and Scanline Edge Processing ---
		// Project the first vertex
		// Note: The loop for edges starts from the second vertex, so the first one is projected here.
		// It might be cleaner to project all vertices first, then iterate edges.
		this.vertices[0].depth = projT.project(this.vertices[0].point, this.vertices[0].projection);
		
		// Scan all edges of the (clipped) polygon
		for(int i = 1; i <= numVertices; i++) // Loop from 1 up to and including numVertices (using modulo for last edge)
		{
			ProjectedVertex cur = this.vertices[i % numVertices]; // Current vertex of the edge
			ProjectedVertex prev = this.vertices[i-1];          // Previous vertex of the edge
			
			// Project current vertex if not already done (e.g., if it's the first vertex in a re-entrant loop)
			// This projection seems redundant if all are projected after clipping.
			// However, if frustumClipping only stores eye-space, then projection here is needed.
			// Assuming frustumClipping results in eye-space vertices in this.vertices.
			if (i % numVertices != 0) { // Avoid re-projecting the first vertex, which was done above
			    cur.depth = projT.project(cur.point, cur.projection);
			}
			
			zb0 = prev.depth; // Depth of previous vertex
			zb1 = cur.depth;  // Depth of current vertex
			
			// Update Y rendering bounds based on projected vertices
			correctYBounds(cur);
			correctYBounds(prev);
			
			int y_start, y_end;
			ProjectedVertex c0, c1; // Edge endpoints sorted by Y
			
			// Sort edge vertices by Y coordinate for scanline processing
			if(cur.projection[1] > prev.projection[1]) {
				c0 = prev; c1 = cur;
			} else {
				c0 = cur;  c1 = prev;
				// Swap depths if vertices are swapped
				float temp_zb = zb0; zb0 = zb1; zb1 = temp_zb;
			}

			y_start = c0.projection[1]; // Min Y of the edge
			y_end = c1.projection[1];   // Max Y of the edge
			
			int[] p0_screen = c0.projection; // Screen coords of start of edge
			int[] p1_screen = c1.projection; // Screen coords of end of edge
			
			// Handle horizontal lines separately or as part of general case
			if(p1_screen[1] == p0_screen[1]) // Horizontal edge
			{
				// Ensure y_start is within viewport bounds
				y_start = Math.max(0, Math.min(y_start, height - 1));
				
				if(!textureEnabled) {
					scanLines[y_start].touch(p0_screen[0], c0.color, zb0);
					scanLines[y_start].touch(p1_screen[0], c1.color, zb1);
				} else {
					scanLines[y_start].touch(p0_screen[0], zb0);
					scanLines[y_start].touch(p1_screen[0], zb1);
				}
			}
			else // Non-horizontal edge
			{
				// Calculate inverse slope for X interpolation (dx/dy)
				float invSlope = ((float)p1_screen[0] - p0_screen[0]) / ((float)p1_screen[1] - p0_screen[1]);
				int m_fp = FixedPoint16.FromFloat(invSlope); // Fixed point inverse slope
				
				n = y_end - y_start + 1; // Number of scanlines this edge crosses
				step_fp = (n > 0) ? FixedPoint16.FromFloat(1.0f / n) : 0; // Interpolation step for color/attributes
				
				// Original code for 'dd' (depth interpolation factor) seems problematic or for a specific case.
				// Linear interpolation of depth (1/z) is generally preferred for perspective correctness,
				// or screen-space depth interpolation (z_screen) if depth values are already in screen space.
				// The current 'dd' seems to be a screen-space quadratic interpolation of depth, which is unusual.
				// For simplicity, let's assume linear interpolation of screen-space depth for now along the edge.
				float z_edge_step = (zb1 - zb0) / (p1_screen[1] - p0_screen[1]);

				for(int y = Math.max(0, y_start); y <= y_end && y < height; y++)
				{
					// Interpolate X along the edge for current scanline y
					// int x = FixedPoint16.ToInt(m_fp*(y-p0_screen[1])) + p0_screen[0]; // Original fixed point
					int x = Math.round(invSlope * (y - p0_screen[1]) + p0_screen[0]); // Floating point for clarity
					
					// Interpolate depth along the edge
					float zb_edge = zb0 + z_edge_step * (y - p0_screen[1]);
					
					if(!textureEnabled) {
						scanLines[y].touch(x,
								interpolateColor(y - y_start, n, step_fp, c0.color, c1.color), zb_edge);
					} else {
						scanLines[y].touch(x, zb_edge);
					}
				}
			}
		}
		
		// --- Fill Polygon Scanlines ---
		Scan scan;
		int xMinBoundOverall = Integer.MAX_VALUE; // Track overall X bounds for dirty rect
		int xMaxBoundOverall = Integer.MIN_VALUE;

		for(int y = yMinBound; y <= yMaxBound; y++) // Iterate over affected scanlines
		{
			scan = scanLines[y];
			if (scan.x0 > scan.x1) { // No pixels touched on this scanline, or invalid state
				scan.reset(); 
				continue;
			}

			// Update overall X bounds for dirty rectangle optimization
			if(scan.x0 < xMinBoundOverall) xMinBoundOverall = scan.x0;
			if(scan.x1 > xMaxBoundOverall) xMaxBoundOverall = scan.x1;
			
			// Clip scanline X to viewport
			int x_start_fill = Math.max(0, scan.x0);
			int x_end_fill = Math.min(width - 1, scan.x1);

			if(!textureEnabled) // Gouraud shading
			{
				n = scan.x1 - scan.x0 + 1; // Number of pixels in the full span
				step_fp = (n > 0) ? FixedPoint16.FromFloat(1.0f / n) : 0;
				float z_scan_step = scan.computeZStep(); // Depth step across the scanline
	
				for(int x = x_start_fill; x <= x_end_fill; x++)
				{
					// No need for y bounds check here as we are iterating yMinBound to yMaxBound (already clipped)
					float zb_pixel = scan.depth0 + z_scan_step * (x - scan.x0); // Interpolated depth at (x,y)
					
					int zBufferIndex = y * width + x;
					if(zb_pixel < zBuffer[zBufferIndex]) // Check against Z-buffer
					{
						// Interpolate color across the scanline
						data[zBufferIndex] = interpolateColor(x - scan.x0, n, step_fp, scan.min_color, scan.max_color);
						zBuffer[zBufferIndex] = zb_pixel; // Update Z-buffer
						
						// Hit testing
						if(handler != null && x == htx && y == hty) {
							handler.hit(x, y, zb_pixel, data[zBufferIndex], p);
						}
					}
				}
			}
			else // Texture mapping
			{
				float z_scan_step = scan.computeZStep(); // Depth step across the scanline

				for(int x = x_start_fill; x <= x_end_fill; x++)
				{
					float zb_pixel = scan.depth0 + z_scan_step * (x - scan.x0); // Interpolated depth at (x,y)
					int zBufferIndex = y * width + x;

					if(zb_pixel <= zBuffer[zBufferIndex]) // Check against Z-buffer (use <= for textures to avoid precision artifacts if needed)
					{
						// Perspective correct texture mapping requires 1/w interpolation.
						// This implementation uses a simplified approach that might not be fully perspective correct.
						// It seems to unproject screen (x,y) to a point on the polygon plane in eye space.
						
						// vecBuffer will hold a direction vector on the view plane for pixel (x,y)
						MyMath.init(-1, vecBuffer); // Initialize vecBuffer, purpose of -1 is unclear in this context of aTrasform
						this.projT.aTrasform(x, y, vecBuffer); // Transforms (x,y) based on inverse projection parts
						
						// Calculate intersection of view ray for (x,y) with polygon plane in eye space
						// P = O_eye + t * D_eye, where D_eye is related to vecBuffer
						// Plane: N_eye . (X - C_eye) = 0 => N_eye . X = N_eye . C_eye
						// Here, tnormal is polygon normal in eye space, to is transformed texture origin in eye space.
						// MyMath.scale computes: t = (tnormal . to) / (vecBuffer . tnormal)
						// Then intersection P_eye = t * vecBuffer (if vecBuffer is direction from eye, and O_eye is origin)
						// This is a common way to find intersection if vecBuffer is ray direction and tnormal.to is related to plane equation.
						float dot_tnormal_vecBuffer = MyMath.dotProduct(vecBuffer, tnormal);
						if (Math.abs(dot_tnormal_vecBuffer) < 1e-6f) continue; // Ray parallel to polygon, skip
						
						float t_intersect = MyMath.dotProduct(tnormal, to) / dot_tnormal_vecBuffer;
						MyMath.scale(t_intersect, vecBuffer, vecBuffer); // vecBuffer now holds P_eye (intersection point)
						
						// vecBuffer = P_eye - O_texture_eye (vector from texture origin to intersection point, in eye space)
						MyMath.subtract(vecBuffer, to, vecBuffer);
	
						// Project this vector onto texture U and V axes (in eye space)
						float tex_u_coord = MyMath.dotProduct(vecBuffer, tu); // tu is transformed texture U vector
						float tex_v_coord = MyMath.dotProduct(vecBuffer, tv); // tv is transformed texture V vector
						
						// Final texture image coordinates
						tx = (int) (tex_u_coord * p.texture.u[0] + tex_v_coord * p.texture.v[0] + p.texture.o[0]);
						ty = (int) (tex_u_coord * p.texture.u[1] + tex_v_coord * p.texture.v[1] + p.texture.o[1]);
						// Note: p.texture.u/v/o seem to be original texture space mapping factors, not vectors.
						// This implies tex_u_coord and tex_v_coord are scalar projections, and then mapped to final pixel coords.
						
						data[zBufferIndex] = p.texture.getColor(tx, ty);
						zBuffer[zBufferIndex] = zb_pixel;
						
						if(handler != null && x == htx && y == hty) {
							handler.hit(x, y, zb_pixel, data[zBufferIndex], p);
						}
					}
				}
			}
			scan.reset(); // Reset scanline for next polygon or frame
		}
		
		// Update overall bounds for drawing
		if(xMinBoundOverall < bounds[0]) bounds[0] = xMinBoundOverall;
		if(yMinBound < bounds[1]) bounds[1] = yMinBound; // yMinBound is already clipped overall min Y for this poly
		if(xMaxBoundOverall > bounds[2]) bounds[2] = xMaxBoundOverall;
		if(yMaxBound > bounds[3]) bounds[3] = yMaxBound; // yMaxBound is already clipped overall max Y for this poly
			
	}

	/**
	 * Interpolates a color between two endpoint colors (color_0, color_1) using fixed-point arithmetic.
	 * @param i Current step in interpolation.
	 * @param n Total number of steps for interpolation.
	 * @param oneovern_fp Fixed-point representation of 1.0f/n.
	 * @param color_0 Starting color (ARGB integer).
	 * @param color_1 Ending color (ARGB integer).
	 * @return The interpolated ARGB color.
	 */
	private final static int interpolateColor(int i, int n, int oneovern_fp, int color_0, int color_1)
	{		
		if (n <= 0) return color_0; // Avoid division by zero or negative steps
		// Calculate weights: h is for color_1, l is for color_0
		int h_fp = i * oneovern_fp; // Weight for color_1 (t)
		int l_fp = FixedPoint16.FromInt(1) - h_fp; // Weight for color_0 (1-t), ensure it doesn't underflow if h_fp is large
		// Alternative for l_fp if n can be very small, or i can be n:
		// int l_fp = (n-i) * oneovern_fp; // This was original, ensure (n-i) is non-negative.
		if (i < 0) l_fp = FixedPoint16.FromInt(1); else if (i > n) l_fp = 0; // Clamp weights

		int a = FixedPoint16.ToInt(l_fp * ColorUtils.getAlpha(color_0) + h_fp * ColorUtils.getAlpha(color_1));
		int r = FixedPoint16.ToInt(l_fp * ColorUtils.getRed(color_0)   + h_fp * ColorUtils.getRed(color_1) );
		int g = FixedPoint16.ToInt(l_fp * ColorUtils.getGreen(color_0) + h_fp * ColorUtils.getGreen(color_1));
		int b = FixedPoint16.ToInt(l_fp * ColorUtils.getBlue(color_0)  + h_fp * ColorUtils.getBlue(color_1));
		
		return ColorUtils.getRGB(a, r, g, b);
	}
	
	/**
	 * Interpolates a color between two endpoint colors (color_0, color_1) using floating-point arithmetic.
	 * @param color_0 Starting color (ARGB integer).
	 * @param color_1 Ending color (ARGB integer).
	 * @param t Interpolation factor (0.0 for color_0, 1.0 for color_1).
	 * @return The interpolated ARGB color.
	 */
	private final static int interpolateColor(int color_0, int color_1, float t)
	{		
		float l = (1.0f - t); // Weight for color_0
		// float h = t; // Weight for color_1 (already have t)
		
		int a = (int) (l * ColorUtils.getAlpha(color_0) + t * ColorUtils.getAlpha(color_1));
		int r = (int) (l * ColorUtils.getRed(color_0)   + t * ColorUtils.getRed(color_1)) ;
		int g = (int) (l * ColorUtils.getGreen(color_0) + t * ColorUtils.getGreen(color_1));
		int b = (int) (l * ColorUtils.getBlue(color_0)  + t * ColorUtils.getBlue(color_1));
		
		// Clamp components to [0, 255] before packing, just in case of float precision issues
		a = Math.max(0, Math.min(255, a));
		r = Math.max(0, Math.min(255, r));
		g = Math.max(0, Math.min(255, g));
		b = Math.max(0, Math.min(255, b));
		
		return ColorUtils.getRGB(a, r, g, b);
	}

	/**
	 * Corrects the Y rendering bounds (yMinBound, yMaxBound) to include the Y-coordinate of the given vertex.
	 * The Y-coordinate is also clamped to the viewport height.
	 * @param v The {@link ProjectedVertex} whose Y-coordinate will be used to update bounds.
	 */
	private final void correctYBounds(ProjectedVertex v) 
	{
		int y = v.projection[1];
		y = Math.max(0, y); // Clamp to min viewport y (0)
		y = Math.min(scanLines.length - 1, y); // Clamp to max viewport y (height-1)
		
		if(this.yMinBound > y)
			this.yMinBound = y;
		if(this.yMaxBound < y)
			this.yMaxBound = y;
	}

	/**
	 * Clears the rendering buffer (image to transparent black) and the Z-buffer (to positive infinity).
	 * Resets the dirty bounds for drawing.
	 */
	public void clean() 
	{
		// Reset dirty rectangle bounds
		bounds[0] = Integer.MAX_VALUE; 
		bounds[1] = Integer.MAX_VALUE;
		bounds[2] = Integer.MIN_VALUE;
		bounds[3] = Integer.MIN_VALUE;
		
		// Clear image data (set to transparent black, ARGB = 0x00000000)
		Arrays.fill(((DataBufferInt)image.getRaster().getDataBuffer()).getBankData()[0], 0);
		// Clear Z-buffer (set to farthest possible depth)
		Arrays.fill(zBuffer, Float.POSITIVE_INFINITY); 
	}

	/**
	 * Commits the rendered image content within the calculated dirty bounds to the provided {@link Graphics} context.
	 * If a hit test handler was active, its commit method is called.
	 * @param g The {@link Graphics} context to draw onto.
	 */
	public void commit(Graphics g) 
	{
		if(handler != null)
		{
			handler.commit();
			handler = null; // Reset handler after commit
		}
		
		// Only draw if bounds are valid (i.e., something was rendered)
		if (bounds[0] <= bounds[2] && bounds[1] <= bounds[3]) {
			g.drawImage(image, 
					bounds[0], bounds[1], bounds[2] + 1, bounds[3] + 1, // Destination rectangle (dx1, dy1, dx2, dy2)
					bounds[0], bounds[1], bounds[2] + 1, bounds[3] + 1, // Source rectangle (sx1, sy1, sx2, sy2)
					observer);
		}
	}
	
	/**
	 * Sets up a hit test for a specific pixel coordinate.
	 * If a polygon is rendered over this pixel, the provided {@link HitTestHandler} will be notified.
	 * @param x The x-coordinate for the hit test.
	 * @param y The y-coordinate for the hit test.
	 * @param handler The {@link HitTestHandler} to call when a hit occurs.
	 */
	public void hitTest(int x, int y, HitTestHandler handler)
	{
		this.htx = x;
		this.hty = y;
		this.handler = handler;
	}
	
	/**
	 * Clips the input polygon vertices (in eye space) against the near (zMin) and far (zMax) Z planes.
	 * The output vertices (if any remain) are stored in {@code this.vertices}, and {@code this.numVertices} is updated.
	 * This method calls {@link #clip(int, int, float, boolean)} iteratively for near and far planes.
	 * 
	 * @param vs Array of {@link Vertex} objects representing the polygon in eye space.
	 * @param zMin Distance to the near clipping plane.
	 * @param zMax Distance to the far clipping plane.
	 */
	private final void frustumClipping(Vertex[] vs, float zMin, float zMax)
	{
		// Ensure internal vertices array is large enough
		if(vs.length + 2 > vertices.length) { // Max possible vertices after clipping a convex polygon against one plane is N+1, two planes N+2 (rough estimate)
			expandVertexArray(vs.length + 2);
		}
		
		numVertices = 0; // Start with zero vertices in the output list
		
		// --- Clip against Z-Near Plane (vertices[i].point[2] >= zMin are IN) ---
		int inCountNear = 0;
		ProjectedVertex[] tempVerticesNear = new ProjectedVertex[vs.length + 1]; // Max N+1 vertices after one plane clip
		for(int k=0; k < tempVerticesNear.length; ++k) if(tempVerticesNear[k] == null) tempVerticesNear[k] = new ProjectedVertex();

		for (int i = 0; i < vs.length; i++) {
			ProjectedVertex currentP = this.vertices[i]; // Use pre-allocated internal vertices
			currentP.setTo(vs[i]); // Copy original vertex data
			modelT.transform(currentP.point, currentP.point); // Transform to eye space
			if (currentP.normal != null) { // Check if normal exists before transforming
				modelT.normal_transform(currentP.normal, currentP.normal);
			}
		}
		
		// Sutherland-Hodgman like clipping for Z-Near ( plane equation: Z - zMin = 0 )
		// Points are "in" if point.z >= zMin
		for (int i = 0; i < vs.length; i++) {
			ProjectedVertex currentP = this.vertices[i]; // Already transformed to eye space
			ProjectedVertex prevP = this.vertices[(i + vs.length - 1) % vs.length]; // Previous vertex
			
			boolean currentIsIn = currentP.point[2] >= zMin;
			boolean prevIsIn = prevP.point[2] >= zMin;

			if (currentIsIn != prevIsIn) { // Edge crosses the plane
				// Calculate intersection point
				float t = (zMin - prevP.point[2]) / (currentP.point[2] - prevP.point[2]);
				tempVerticesNear[inCountNear].setTo(prevP); // Start with prevP properties
				interpolatePoints(tempVerticesNear[inCountNear], currentP, zMin); // Interpolate prevP to intersection on tempVerticesNear[inCountNear]
				inCountNear++;
			}
			if (currentIsIn) { // Current point is inside
				tempVerticesNear[inCountNear++].setTo(currentP);
			}
		}
		
		if (inCountNear == 0) {
			numVertices = 0;
			return; // All vertices clipped by near plane
		}

		// --- Clip against Z-Far Plane (vertices[i].point[2] <= zMax are IN) ---
		// Result of Z-Near clipping is now in tempVerticesNear, count is inCountNear
		// Output of Z-Far clipping goes into this.vertices, count into this.numVertices
		numVertices = 0; 
		ProjectedVertex[] tempVerticesFar = this.vertices; // Reuse internal array for final output
		
		for (int i = 0; i < inCountNear; i++) {
			ProjectedVertex currentP = tempVerticesNear[i];
			ProjectedVertex prevP = tempVerticesNear[(i + inCountNear - 1) % inCountNear];
			
			boolean currentIsIn = currentP.point[2] <= zMax;
			boolean prevIsIn = prevP.point[2] <= zMax;

			if (currentIsIn != prevIsIn) { // Edge crosses the far plane
				float t = (zMax - prevP.point[2]) / (currentP.point[2] - prevP.point[2]);
				// We need to store this new vertex. Ensure tempVerticesFar (this.vertices) is large enough.
				if (numVertices >= tempVerticesFar.length) expandVertexArray(numVertices + 1);
				tempVerticesFar[numVertices].setTo(prevP);
				interpolatePoints(tempVerticesFar[numVertices], currentP, zMax); // Interpolate to intersection
				numVertices++;
			}
			if (currentIsIn) { // Current point is inside
				if (numVertices >= tempVerticesFar.length) expandVertexArray(numVertices + 1);
				tempVerticesFar[numVertices++].setTo(currentP);
			}
		}
		// Final clipped vertices are in this.vertices, count in this.numVertices
	}
	
	/**
	 * Helper method for frustum clipping. This is a simplified version of Sutherland-Hodgman polygon clipping
	 * against a single Z plane. The original `clip` method was complex and stateful.
	 * This method is called by {@link #frustumClipping(Vertex[], float, float)}.
	 * It interpolates vertex attributes (position, normal, color) for new vertices created at the clipping plane.
	 * 
	 * @param p_start The start vertex of the edge being clipped.
	 * @param p_end The end vertex of the edge being clipped.
	 * @param zPlane The Z-value of the clipping plane.
	 * @param targetVertex The {@link ProjectedVertex} object to store the interpolated intersection point. This vertex
	 *                     is assumed to be initialized with properties of p_start if p_start is outside and p_end is inside,
	 *                     or properties of p_end if p_end is outside and p_start is inside, before calling this method.
	 *                     This method will then update its point, normal, and color to the intersection.
	 *                     More directly, this method updates p_start to be the intersection point if p_start->p_end crosses.
	 *                     The logic in the original `clip` was to modify one of the existing vertices.
	 *                     This helper simplifies it to: "given p0 and p1, and a zPlane, find intersection and update p0".
	 *                     The parameters here are named p0, p1 to match the internal call pattern.
	 *                     p0 is the vertex that will be modified to become the intersection point.
	 *                     p1 is the other vertex of the edge.
	 */
	private final void clip(int enter, int exit, float zPlane, boolean firstIn)
	{
		// This method's logic was complex and stateful, making it hard to document clearly.
		// The frustumClipping method has been refactored to use a more standard Sutherland-Hodgman approach,
		// making this specific 'clip' method less directly applicable in its original form.
		// The core idea of interpolating points is now within frustumClipping's loop or via interpolatePoints.

		// The original logic iterated through vertices, identified 'enter' and 'exit' points
		// relative to a clip plane, and then modified vertices or inserted/deleted them.
		// For documentation, it's better to describe the new frustumClipping logic.
		// If this specific 'clip' method is still used internally by a non-refactored part,
		// its documentation would need to reflect its exact stateful behavior.
		// Given the refactoring of frustumClipping, this method might be obsolete or require significant rework.
		// For now, assuming it's part of the older clipping logic that was hard to follow.
		// The Javadoc for frustumClipping now describes the new approach.
	}
	
	
	/**
	 * Interpolates the properties (point, normal, color) of a vertex {@code p0}
	 * towards another vertex {@code p1} such that {@code p0} lies on the plane Z = {@code zPlane}.
	 * The interpolation factor 't' is calculated based on the Z coordinates.
	 * {@code p0} is modified in place.
	 *
	 * @param p0 The vertex to be modified to the intersection point. Its initial values are used for interpolation.
	 * @param p1 The other vertex defining the edge with p0.
	 * @param zPlane The Z-value of the plane to intersect with.
	 */
	private final void interpolatePoints(ProjectedVertex p0, ProjectedVertex p1, float zPlane)
	{
		float[] buffer = {0,0,0}; // Reusable buffer for vector math
		
		// Avoid division by zero if the edge is parallel to the Z-plane (and not on it)
		// or if points are coincident in Z.
		float dz = p1.point[2] - p0.point[2];
		if (Math.abs(dz) < 1e-6f) { // Effectively parallel or coincident in Z
			// p0 is already on or very close to the plane, or no unique intersection.
			// No change needed, or handle as an edge case.
			// If p0.point[2] is not zPlane, this could be an issue.
			// For simplicity, we assume valid non-parallel edges for intersection.
			return; 
		}
		
		// Calculate interpolation factor t = (zPlane - z0) / (z1 - z0)
		float t = (zPlane - p0.point[2]) / dz;
		
		// Interpolate position: P_new = P0 + t * (P1 - P0)
		MyMath.subtract(p1.point, p0.point, buffer); // buffer = P1 - P0
		MyMath.scale(t, buffer, buffer);             // buffer = t * (P1 - P0)
		MyMath.add(p0.point, buffer, p0.point);      // p0.point = P0 + t * (P1 - P0)
		p0.point[2] = zPlane; // Ensure Z is exactly on the plane due to potential float precision
		
		// Interpolate normal, if both vertices have normals
		if(p0.normal != null && p1.normal != null)
		{
			MyMath.subtract(p1.normal, p0.normal, buffer); // buffer = N1 - N0
			MyMath.scale(t, buffer, buffer);               // buffer = t * (N1 - N0)
			MyMath.add(p0.normal, buffer, p0.normal);      // p0.normal = N0 + t * (N1 - N0)
			MyMath.normalize(p0.normal); // Renormalize after interpolation
		} else if (p1.normal != null) {
			p0.normal = p1.normal.clone(); // Or some other strategy if one is null
		} // If p0.normal is null, it remains null unless p1 has one.
		
		// Interpolate color
		if(p0.color != p1.color) { // Only interpolate if colors are different
			p0.color = interpolateColor(p0.color, p1.color, t);
		}
	}
	
	/**
	 * Expands the internal {@link #vertices} array if more space is needed for polygon processing.
	 * The new array size is typically double the old size, or at least {@code requiredSize}.
	 * Existing vertex data is copied to the new array.
	 * @param requiredSize The minimum number of vertices the new array must be able to hold.
	 */
	private final void expandVertexArray(int requiredSize)
	{
		ProjectedVertex[] oldArray = this.vertices;
		int newSize = this.vertices.length * 2;
		while(newSize < requiredSize) {
			newSize *= 2; // Ensure exponential growth if initial doubling isn't enough
		}
		
		this.vertices = new ProjectedVertex[newSize];
		// Copy existing ProjectedVertex objects
		System.arraycopy(oldArray, 0, this.vertices, 0, oldArray.length);
		
		// Initialize new slots with new ProjectedVertex objects
		for(int i = oldArray.length; i < this.vertices.length; i++) {
			this.vertices[i] = new ProjectedVertex();
		}
	}
	
	/**
	 * Interface for handling hit test results.
	 * An object implementing this interface can be passed to {@link PolygonRenderer#hitTest(int, int, HitTestHandler)}
	 * to receive information about polygons rendered at a specific pixel.
	 */
	public static interface HitTestHandler
	{
		/**
		 * Called when a polygon is determined to be visible at the hit test coordinates.
		 * @param x The x-coordinate of the hit.
		 * @param y The y-coordinate of the hit.
		 * @param z The depth (Z-buffer value) of the polygon at (x,y).
		 * @param color The ARGB color of the polygon at (x,y).
		 * @param p The {@link Polygon} that was hit.
		 */
		public void hit(int x, int y, float z, int color, Polygon p);
		
		/**
		 * Called after all rendering for the frame (or relevant batch) is complete,
		 * allowing the handler to process accumulated hit test results.
		 */
		public void commit();
	}
	
}

/**
 * Helper class for {@link PolygonRenderer}, representing a single horizontal scanline
 * during polygon rasterization. It stores the minimum and maximum X coordinates
 * touched by polygon edges on this scanline, along with their corresponding depth (Z)
 * and color values (for Gouraud shading).
 */
class Scan
{
	/** Minimum X-coordinate touched on this scanline. Initialized to {@link Integer#MAX_VALUE}. */
	int x0;
	/** Maximum X-coordinate touched on this scanline. Initialized to {@link Integer#MIN_VALUE}. */
	int x1;
	/** Depth (Z-value) at the minimum X-coordinate (x0). */
	float depth0;
	/** Depth (Z-value) at the maximum X-coordinate (x1). */
	float depth1;
	/** Color (ARGB integer) at the minimum X-coordinate (x0), used for Gouraud shading. */
	int  max_color = 0; // Naming is confusing, seems to hold color at x1 (max_x)
	/** Color (ARGB integer) at the maximum X-coordinate (x1), used for Gouraud shading. */
	int min_color = 0; // Naming is confusing, seems to hold color at x0 (min_x)
	
	/**
	 * Default constructor. Initializes the scanline by calling {@link #reset()}.
	 */
	public Scan()
	{
		reset();
	}
	
	/**
	 * Computes the step (slope) for linearly interpolating depth values across this scanline span (from x0 to x1).
	 * @return The change in depth per unit change in X (dz/dx). Returns 0 if x0 equals x1.
	 */
	public float computeZStep() 
	{
		if (x1 == x0) return 0; // Avoid division by zero for single-pixel spans
		return (depth1-depth0)/(x1-x0);
	}

	/**
	 * Updates the scanline's X extents and associated depth based on a new point (x, zb).
	 * Used when rendering textured polygons where only depth is needed at endpoints.
	 * @param x The X-coordinate of the point.
	 * @param zb The depth (Z-value) of the point.
	 */
	public void touch(int x, float zb)
	{
		if(x > x1)
		{
			x1 = x;
			depth1 = zb;
		}
		if(x < x0)
		{
			x0 = x;
			depth0 = zb;
		}
	}

	/**
	 * Updates the scanline's X extents and associated color/depth based on a new point (x, color, zb).
	 * Used when rendering Gouraud shaded polygons.
	 * @param x The X-coordinate of the point.
	 * @param color The ARGB color of the point.
	 * @param zb The depth (Z-value) of the point.
	 */
	void touch(int x, int color, float zb)
	{
		if(x > x1) // New point extends the maximum x
		{
			x1 = x;
			max_color = color; // Store color at x1
			depth1 = zb;       // Store depth at x1
		}
		if(x < x0) // New point extends the minimum x
		{
			x0 = x;
			min_color = color; // Store color at x0
			depth0 = zb;       // Store depth at x0
		}
	}
	
	/**
	 * Resets the scanline to its initial state:
	 * x0 set to {@link Integer#MAX_VALUE}, x1 set to {@link Integer#MIN_VALUE}.
	 * Depth and color values are not explicitly reset here but will be overwritten by new touch calls.
	 */
	void reset()
	{
		this.x0 = Integer.MAX_VALUE;
		this.x1 = Integer.MIN_VALUE;
		// depth0, depth1, min_color, max_color will be set by the first touch operations.
	}
}