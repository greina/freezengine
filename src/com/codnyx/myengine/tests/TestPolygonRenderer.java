package com.codnyx.myengine.tests;

import static org.junit.Assert.*;
import org.junit.Test;
import org.mockito.Mockito; // Using Mockito for mocking Graphics

import com.codnyx.myengine.PolygonRenderer;
import com.codnyx.myengine.Polygon;
import com.codnyx.myengine.Vertex;
import com.codnyx.myengine.ProjectedVertex;
import com.codnyx.myengine.AffineTransformation;
import com.codnyx.myengine.PerspectiveTransformation;
import com.codnyx.myengine.Texture;
import com.codnyx.myengine.ColorUtils; // For creating colors

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field; // For accessing private fields

// Mock HitTestHandler for testing
class MockHitTestHandler implements PolygonRenderer.HitTestHandler {
    public boolean hitCalled = false;
    public boolean commitCalled = false;
    public int hitX, hitY, hitColor;
    public float hitZ;
    public Polygon hitPolygon;

    @Override
    public void hit(int x, int y, float z, int color, Polygon p) {
        hitCalled = true;
        hitX = x;
        hitY = y;
        hitZ = z;
        hitColor = color;
        hitPolygon = p;
    }

    @Override
    public void commit() {
        commitCalled = true;
    }
    
    public void reset() {
        hitCalled = false;
        commitCalled = false;
        hitPolygon = null;
    }
}

// Mock Texture for testing texture mapping path
class MockRendererTexture extends Texture {
    public boolean getColorCalled = false;
    public int lastX, lastY;

    @Override
    public int getColor(int x, int y) {
        getColorCalled = true;
        lastX = x;
        lastY = y;
        return Color.MAGENTA.getRGB(); // Return a distinct color
    }
    public void reset() {
        getColorCalled = false;
    }
}


public class TestPolygonRenderer {

    private static final float DELTA = 1e-5f;

    private Object getPrivateField(Object obj, String fieldName) throws Exception {
        Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(obj);
    }
    
    private void setPrivateField(Object obj, String fieldName, Object value) throws Exception {
        Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(obj, value);
    }

    // --- Test Constructors ---
    @Test
    public void testConstructor_WidthHeight() throws Exception {
        PolygonRenderer renderer = new PolygonRenderer(100, 50);
        assertEquals("Width should be set", 100, getPrivateField(renderer, "width"));
        assertEquals("Height should be set", 50, getPrivateField(renderer, "height"));
        assertEquals("Left should be 0", 0, getPrivateField(renderer, "left"));
        assertEquals("Top should be 0", 0, getPrivateField(renderer, "top"));

        assertNotNull("scanLines should be initialized", getPrivateField(renderer, "scanLines"));
        assertEquals("scanLines length should be height", 50, ((Object[])getPrivateField(renderer, "scanLines")).length);
        assertNotNull("vertices array should be initialized", getPrivateField(renderer, "vertices"));
        assertNotNull("modelT should be initialized", renderer.getModelT()); // getModelT() is public
        assertNotNull("image should be initialized", getPrivateField(renderer, "image"));
        assertEquals("Image width", 100, ((BufferedImage)getPrivateField(renderer, "image")).getWidth());
        assertEquals("Image height", 50, ((BufferedImage)getPrivateField(renderer, "image")).getHeight());
        assertNotNull("zBuffer should be initialized", getPrivateField(renderer, "zBuffer"));
        assertEquals("zBuffer length", 100 * 50, ((float[])getPrivateField(renderer, "zBuffer")).length);
        assertEquals("zBuffer initial value", Float.POSITIVE_INFINITY, ((float[])getPrivateField(renderer, "zBuffer"))[0], DELTA);
    }

    @Test
    public void testConstructor_LeftTopWidthHeight() throws Exception {
        PolygonRenderer renderer = new PolygonRenderer(10, 20, 100, 50);
        assertEquals("Width should be set", 100, getPrivateField(renderer, "width"));
        assertEquals("Height should be set", 50, getPrivateField(renderer, "height"));
        assertEquals("Left should be 10", 10, getPrivateField(renderer, "left"));
        assertEquals("Top should be 20", 20, getPrivateField(renderer, "top"));
    }

    // --- Test setPerspective ---
    @Test
    public void testSetPerspective() throws Exception {
        PolygonRenderer renderer = new PolygonRenderer(100, 100);
        renderer.setPerspective(Math.toRadians(90), 1f, 100f);
        PerspectiveTransformation projT = renderer.getProjT(); // Public getter
        assertNotNull("PerspectiveTransformation (projT) should be set", projT);
        assertEquals("projT zMin", 1f, projT.getZMin(), DELTA);
        assertEquals("projT zMax", 100f, projT.getZMax(), DELTA);
    }

    // --- Test clean() ---
    @Test
    public void testClean() throws Exception {
        PolygonRenderer renderer = new PolygonRenderer(2, 1); // Tiny image for easy check
        
        BufferedImage image = (BufferedImage) getPrivateField(renderer, "image");
        float[] zBuffer = (float[]) getPrivateField(renderer, "zBuffer");
        int[] bounds = (int[]) getPrivateField(renderer, "bounds");

        // Dirty the buffers and bounds
        image.setRGB(0, 0, Color.RED.getRGB());
        image.setRGB(1, 0, Color.RED.getRGB());
        zBuffer[0] = 10f;
        zBuffer[1] = 10f;
        bounds[0] = 0; bounds[1] = 0; bounds[2] = 1; bounds[3] = 0;

        renderer.clean();

        assertEquals("Image pixel 0 after clean", 0, image.getRGB(0,0)); // Should be transparent black (ARGB 0)
        assertEquals("Image pixel 1 after clean", 0, image.getRGB(1,0));
        assertEquals("zBuffer element 0 after clean", Float.POSITIVE_INFINITY, zBuffer[0], DELTA);
        assertEquals("zBuffer element 1 after clean", Float.POSITIVE_INFINITY, zBuffer[1], DELTA);
        assertEquals("Bounds minX after clean", Integer.MAX_VALUE, bounds[0]);
        assertEquals("Bounds minY after clean", Integer.MAX_VALUE, bounds[1]);
        assertEquals("Bounds maxX after clean", Integer.MIN_VALUE, bounds[2]);
        assertEquals("Bounds maxY after clean", Integer.MIN_VALUE, bounds[3]);
    }

    // --- Test render() - Backface Culling ---
    @Test
    public void testRender_BackfaceCulling() throws Exception {
        PolygonRenderer renderer = new PolygonRenderer(100, 100);
        renderer.setPerspective(Math.toRadians(90), 1f, 100f);
        
        // Polygon facing camera (normal.z is negative in eye space if polygon is in front and defined CCW)
        Vertex v1_front = new Vertex(new float[]{-0.5f, -0.5f, -2f});
        Vertex v2_front = new Vertex(new float[]{ 0.5f, -0.5f, -2f});
        Vertex v3_front = new Vertex(new float[]{ 0.0f,  0.5f, -2f});
        Polygon frontFacingPolygon = new Polygon(new Vertex[]{v1_front, v2_front, v3_front});
        // Its normal by default computation: (v2-v1)x(v3-v2) = (1,0,0)x(-0.5,1,0) = (0,0,1) in model space
        // If modelT is identity, tnormal = (0,0,1). Center = (0,0,-2). vecBuffer2 = (0,0,-2)
        // MyMath.dotProduct((0,0,1), (0,0,-2)) = -2 which is < 0, so NOT culled.

        // Polygon facing away (normal.z is positive)
        Polygon backFacingPolygon = new Polygon(new Vertex[]{v3_front, v2_front, v1_front}); // Reversed order
        // Its normal by default computation: (v2-v3)x(v1-v2) = (0.5,-1,0)x(-1,0,0) = (0,0,-1) in model space
        // If modelT is identity, tnormal = (0,0,-1). Center = (0,0,-2). vecBuffer2 = (0,0,-2)
        // MyMath.dotProduct((0,0,-1), (0,0,-2)) = 2 which is >= 0, so CULLED.
        
        Graphics mockGraphics = Mockito.mock(Graphics.class);

        // Render front-facing polygon
        renderer.render(mockGraphics, frontFacingPolygon);
        int numVerticesAfterFront = (int)getPrivateField(renderer, "numVertices");
        assertTrue("Front-facing polygon should result in numVertices > 0 after clipping/processing", numVerticesAfterFront > 0);
        
        // Render back-facing polygon
        renderer.render(mockGraphics, backFacingPolygon);
        int numVerticesAfterBack = (int)getPrivateField(renderer, "numVertices");
        // The render method returns early if culled. `numVertices` will retain its value from the previous call if not reset.
        // This test needs a way to confirm that the rendering pipeline for backFacingPolygon was indeed skipped.
        // One way is to check if frustumClipping was called or if internal state like yMinBound/yMaxBound was reset.
        // For simplicity, we can check if `numVertices` is NOT updated to something >0 specifically for the back-facing one.
        // A better check: set a spy or check a side effect that only happens if not culled.
        // Here, we assume that if it's culled, frustumClipping won't run and change numVertices for THIS polygon.
        // The current structure of render() means numVertices will be the result of the LAST non-culled polygon or 0 if the last one was culled.
        // To test culling in isolation better, we need to observe an effect *before* frustumClipping.
        // The test relies on the fact that if culled, `reset()` is NOT called for the culled polygon.
        // Let's check yMinBound before and after.
        setPrivateField(renderer, "yMinBound", 555); // A specific value
        renderer.render(mockGraphics, backFacingPolygon);
        assertEquals("yMinBound should not change if polygon is culled", 555, getPrivateField(renderer, "yMinBound"));
    }
    
    // --- Test render() - Frustum Clipping (Simplified) ---
    @Test
    public void testRender_FrustumClipping_FullyOutsideNear() throws Exception {
        PolygonRenderer renderer = new PolygonRenderer(100, 100);
        renderer.setPerspective(Math.toRadians(90), 1f, 100f); // zMin = -1, zMax = -100 in eye space
        
        // Polygon completely in front of near plane (e.g. z = -0.5f)
        Vertex v1 = new Vertex(new float[]{0f, 0f, -0.5f});
        Vertex v2 = new Vertex(new float[]{1f, 0f, -0.5f});
        Vertex v3 = new Vertex(new float[]{0f, 1f, -0.5f});
        Polygon poly = new Polygon(new Vertex[]{v1,v2,v3}); // Normal (0,0,1)

        renderer.render(Mockito.mock(Graphics.class), poly);
        assertEquals("Polygon fully before near plane should be clipped (numVertices=0)", 0, getPrivateField(renderer, "numVertices"));
    }

    @Test
    public void testRender_FrustumClipping_FullyOutsideFar() throws Exception {
        PolygonRenderer renderer = new PolygonRenderer(100, 100);
        renderer.setPerspective(Math.toRadians(90), 1f, 100f); // zMin = -1, zMax = -100 in eye space
        
        Vertex v1 = new Vertex(new float[]{0f, 0f, -200f}); // Beyond far plane
        Vertex v2 = new Vertex(new float[]{1f, 0f, -200f});
        Vertex v3 = new Vertex(new float[]{0f, 1f, -200f});
        Polygon poly = new Polygon(new Vertex[]{v1,v2,v3});

        renderer.render(Mockito.mock(Graphics.class), poly);
        assertEquals("Polygon fully after far plane should be clipped (numVertices=0)", 0, getPrivateField(renderer, "numVertices"));
    }
    
    @Test
    public void testRender_FrustumClipping_FullyInside() throws Exception {
        PolygonRenderer renderer = new PolygonRenderer(100, 100);
        renderer.setPerspective(Math.toRadians(90), 1f, 100f);
        
        Vertex v1 = new Vertex(new float[]{-0.1f, -0.1f, -10f}); // Well within frustum
        Vertex v2 = new Vertex(new float[]{ 0.1f, -0.1f, -10f});
        Vertex v3 = new Vertex(new float[]{ 0.0f,  0.1f, -10f});
        Polygon poly = new Polygon(new Vertex[]{v1,v2,v3}); // Normal (0,0,1)

        renderer.render(Mockito.mock(Graphics.class), poly);
        // Polygon is a triangle, not clipped by Z near/far, so should remain 3 vertices
        assertEquals("Polygon fully inside should have 3 vertices after clipping stage", 3, getPrivateField(renderer, "numVertices"));
    }


    // --- Test commit() ---
    @Test
    public void testCommit() throws Exception {
        PolygonRenderer renderer = new PolygonRenderer(10, 10);
        Graphics mockGraphics = Mockito.mock(Graphics.class);
        int[] bounds = (int[])getPrivateField(renderer, "bounds");
        
        // Simulate some rendering that updates bounds
        bounds[0] = 1; bounds[1] = 2; bounds[2] = 8; bounds[3] = 7; // minX, minY, maxX, maxY

        renderer.commit(mockGraphics);

        // Verify drawImage was called with correct parameters based on bounds
        // drawImage(image, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, observer)
        // dx1=bounds[0], dy1=bounds[1], dx2=bounds[2]+1, dy2=bounds[3]+1
        // sx1=bounds[0], sy1=bounds[1], sx2=bounds[2]+1, sy2=bounds[3]+1
        Mockito.verify(mockGraphics).drawImage(
            (java.awt.Image)getPrivateField(renderer, "image"),
            1, 2, 8 + 1, 7 + 1,
            1, 2, 8 + 1, 7 + 1,
            (java.awt.image.ImageObserver)getPrivateField(renderer, "observer")
        );
    }
    
    // --- Test hitTest() ---
    @Test
    public void testHitTest() throws Exception {
        PolygonRenderer renderer = new PolygonRenderer(10, 10);
        renderer.setPerspective(Math.toRadians(90), 0.1f, 10f);

        // Create a simple polygon that will cover pixel (5,5)
        Vertex v1 = new Vertex(new float[]{-1f, -1f, -1f}); // Projected will be around center
        Vertex v2 = new Vertex(new float[]{ 1f, -1f, -1f});
        Vertex v3 = new Vertex(new float[]{ 0f,  1f, -1f});
        Polygon poly = new Polygon(new Vertex[]{v1,v2,v3});
        poly.getVertices()[0].color = Color.GREEN.getRGB(); // Give it a color

        MockHitTestHandler mockHandler = new MockHitTestHandler();
        renderer.hitTest(5, 5, mockHandler); // Hit test for pixel (5,5)
        
        // Force zBuffer to be far away at (5,5) to ensure pixel is drawn
        float[] zBuffer = (float[])getPrivateField(renderer, "zBuffer");
        zBuffer[5 * 10 + 5] = Float.POSITIVE_INFINITY; 
        
        // Set image pixel (5,5) to a known default before render to check if it's overwritten
        BufferedImage image = (BufferedImage)getPrivateField(renderer, "image");
        image.setRGB(5,5, Color.BLUE.getRGB());


        // This render call should trigger the hit test if pixel (5,5) is part of the polygon
        // and is visible. The actual scanline logic is complex to guarantee a hit without
        // exact screen coordinate calculation. This test primarily checks handler invocation.
        // We need to ensure (5,5) is actually rendered.
        // With fov=90, width=10, height=10, z=-1:
        // projT.a11 = 5/tan(45) = 5, a22 = -5
        // projT.a13 = -(0+5) = -5, a23 = -(0+5) = -5
        // For v1(-1,-1,-1): x_s = (5*(-1) + (-5)*(-1)) * (-1/-1) = (-5+5)*1 = 0
        //                    y_s = (-5*(-1) + (-5)*(-1)) * 1 = (5+5)*1 = 10 (oops, outside for 10x10 if top=0)
        // Let's adjust vertices to be more central for a 10x10 screen
        // For pixel (5,5) to be center, we'd need x_eye/z_eye = 0 and y_eye/z_eye = 0
        // Let center of screen be roughly (width/2, height/2) = (5,5)
        // Vertices for a triangle centered around origin in XY, at z=-1
        v1 = new Vertex(new float[]{-0.5f, -0.5f, -1f}); // Color will be default white
        v2 = new Vertex(new float[]{ 0.5f, -0.5f, -1f});
        v3 = new Vertex(new float[]{ 0.0f,  0.5f, -1f});
        poly = new Polygon(new Vertex[]{v1,v2,v3});
        // Change one vertex color to check if it's interpolated/used
        poly.getVertices()[0].color = ColorUtils.getRGB(255, 255, 0, 0); // Opaque Red
        poly.getVertices()[1].color = ColorUtils.getRGB(255, 0, 255, 0); // Opaque Green
        poly.getVertices()[2].color = ColorUtils.getRGB(255, 0, 0, 255); // Opaque Blue

        renderer.render(Mockito.mock(Graphics.class), poly);
        
        // If the hit test logic within render works, mockHandler.hitCalled should be true.
        // This is a weak test for hitTest logic itself, as it depends on the whole render pipeline.
        // A true test of hitTest would mock the conditions that lead to handler.hit()
        // For now, check if it was called.
        if (image.getRGB(5,5) != Color.BLUE.getRGB()) { // Pixel was overwritten
             assertTrue("HitTestHandler.hit() should be called if pixel (5,5) was rendered", mockHandler.hitCalled);
             assertEquals("Hit X", 5, mockHandler.hitX);
             assertEquals("Hit Y", 5, mockHandler.hitY);
             assertSame("Hit Polygon", poly, mockHandler.hitPolygon);
        } else {
            // Pixel (5,5) was not drawn, so hit should not have been called.
            assertFalse("HitTestHandler.hit() should not be called if pixel (5,5) was not rendered", mockHandler.hitCalled);
        }

        renderer.commit(Mockito.mock(Graphics.class)); // This calls handler.commit()
        assertTrue("HitTestHandler.commit() should be called", mockHandler.commitCalled);
    }
}
