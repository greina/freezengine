package com.codnyx.myengine.tests;

import static org.junit.Assert.*;
import org.junit.Test;

import com.codnyx.myengine.Mesh;
import com.codnyx.myengine.Polygon;
import com.codnyx.myengine.Vertex;
import com.codnyx.myengine.PolygonRenderer; // Assuming this interface/class exists

import java.awt.Graphics; // For render method testing (mocking)
import java.util.LinkedList;

// Mocking classes for testing render method
class MockPolygonRenderer implements PolygonRenderer {
    public int renderCount = 0;
    public Polygon lastRenderedPolygon = null;

    @Override
    public void render(Graphics g, Polygon p) {
        renderCount++;
        lastRenderedPolygon = p;
    }

    // This method might not be part of the actual PolygonRenderer interface
    // but is added here for completeness based on potential needs.
    // If the interface only has render(Graphics, Polygon), this can be removed.
    public void render(Graphics g, Polygon[] p) {
        for (Polygon poly : p) {
            render(g, poly);
        }
    }
}

class MockGraphics extends Graphics {
    // Implement abstract methods from Graphics as needed for the tests to compile/run.
    // Most of these can be minimal implementations if not directly used by Mesh.render logic.

    @Override
    public Graphics create() { return this; }
    @Override
    public void translate(int x, int y) {}
    @Override
    public java.awt.Color getColor() { return null; }
    @Override
    public void setColor(java.awt.Color c) {}
    @Override
    public void setPaintMode() {}
    @Override
    public void setXORMode(java.awt.Color c1) {}
    @Override
    public java.awt.Font getFont() { return null; }
    @Override
    public void setFont(java.awt.Font font) {}
    @Override
    public java.awt.FontMetrics getFontMetrics(java.awt.Font f) { return null; }
    @Override
    public java.awt.Rectangle getClipBounds() { return null; }
    @Override
    public void clipRect(int x, int y, int width, int height) {}
    @Override
    public void setClip(int x, int y, int width, int height) {}
    @Override
    public java.awt.Shape getClip() { return null; }
    @Override
    public void setClip(java.awt.Shape clip) {}
    @Override
    public void copyArea(int x, int y, int width, int height, int dx, int dy) {}
    @Override
    public void drawLine(int x1, int y1, int x2, int y2) {}
    @Override
    public void fillRect(int x, int y, int width, int height) {}
    @Override
    public void clearRect(int x, int y, int width, int height) {}
    @Override
    public void drawRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight) {}
    @Override
    public void fillRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight) {}
    @Override
    public void drawOval(int x, int y, int width, int height) {}
    @Override
    public void fillOval(int x, int y, int width, int height) {}
    @Override
    public void drawArc(int x, int y, int width, int height, int startAngle, int arcAngle) {}
    @Override
    public void fillArc(int x, int y, int width, int height, int startAngle, int arcAngle) {}
    @Override
    public void drawPolyline(int[] xPoints, int[] yPoints, int nPoints) {}
    @Override
    public void drawPolygon(int[] xPoints, int[] yPoints, int nPoints) {}
    @Override
    public void fillPolygon(int[] xPoints, int[] yPoints, int nPoints) {}
    @Override
    public void drawString(String str, int x, int y) {}
    @Override
    public void drawString(java.text.AttributedCharacterIterator iterator, int x, int y) {}
    @Override
    public boolean drawImage(java.awt.Image img, int x, int y, java.awt.image.ImageObserver observer) { return false; }
    @Override
    public boolean drawImage(java.awt.Image img, int x, int y, int width, int height, java.awt.image.ImageObserver observer) { return false; }
    @Override
    public boolean drawImage(java.awt.Image img, int x, int y, java.awt.Color bgcolor, java.awt.image.ImageObserver observer) { return false; }
    @Override
    public boolean drawImage(java.awt.Image img, int x, int y, int width, int height, java.awt.Color bgcolor, java.awt.image.ImageObserver observer) { return false; }
    @Override
    public boolean drawImage(java.awt.Image img, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2, int sy2, java.awt.image.ImageObserver observer) { return false; }
    @Override
    public boolean drawImage(java.awt.Image img, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2, int sy2, java.awt.Color bgcolor, java.awt.image.ImageObserver observer) { return false; }
    @Override
    public void dispose() {}
}


/**
 * JUnit test class for the Mesh class.
 * This class tests constructor, polygon manipulation, and rendering logic.
 */
public class TestMesh {

    /**
     * Tests the default constructor of Mesh.
     * Verifies that a new Mesh instance has an empty list of polygons.
     */
    @Test
    public void testDefaultConstructor() {
        Mesh mesh = new Mesh();
        assertNotNull("Mesh instance should not be null", mesh);
        assertNotNull("Polygons list should be initialized", mesh.polygons);
        assertTrue("Polygons list should be empty initially", mesh.polygons.isEmpty());
    }

    /**
     * Tests the addPolygon(Polygon p) method.
     * Verifies that a single polygon can be added to the mesh and is stored correctly.
     */
    @Test
    public void testAddPolygon() {
        Mesh mesh = new Mesh();
        Polygon poly1 = new Polygon(new Vertex(0,0,0), new Vertex(1,0,0), new Vertex(0,1,0));
        
        mesh.addPolygon(poly1);
        assertEquals("Mesh should contain 1 polygon after adding one", 1, mesh.polygons.size());
        assertSame("The added polygon should be the one in the list", poly1, mesh.polygons.get(0));

        Polygon poly2 = new Polygon(new Vertex(2,0,0), new Vertex(3,0,0), new Vertex(2,1,0));
        mesh.addPolygon(poly2);
        assertEquals("Mesh should contain 2 polygons after adding another", 2, mesh.polygons.size());
        assertSame("The second added polygon should be at index 1", poly2, mesh.polygons.get(1));
    }

    /**
     * Tests adding a null polygon using addPolygon(Polygon p).
     * It should handle this gracefully, e.g., by not adding it or throwing an exception
     * (current LinkedList behavior is to allow nulls).
     */
    @Test
    public void testAddNullPolygon() {
        Mesh mesh = new Mesh();
        mesh.addPolygon(null);
        assertEquals("Mesh should contain 1 element (null) after adding null", 1, mesh.polygons.size());
        assertNull("The element in the list should be null", mesh.polygons.get(0));
    }


    /**
     * Tests the addPolygons(Polygon[] polygonArray) method.
     * Verifies that an array of polygons can be added to the mesh.
     */
    @Test
    public void testAddPolygonsFromArray() {
        Mesh mesh = new Mesh();
        Polygon poly1 = new Polygon(new Vertex(0,0,0), new Vertex(1,0,0), new Vertex(0,1,0));
        Polygon poly2 = new Polygon(new Vertex(1,1,1), new Vertex(2,1,1), new Vertex(1,2,1));
        Polygon poly3 = new Polygon(new Vertex(2,2,2), new Vertex(3,2,2), new Vertex(2,3,2));

        Polygon[] polygonsToAdd = {poly1, poly2};
        mesh.addPolygons(polygonsToAdd);
        assertEquals("Mesh should contain 2 polygons after adding array of 2", 2, mesh.polygons.size());
        assertSame("First polygon from array should be at index 0", poly1, mesh.polygons.get(0));
        assertSame("Second polygon from array should be at index 1", poly2, mesh.polygons.get(1));

        Polygon[] morePolygons = {poly3};
        mesh.addPolygons(morePolygons);
        assertEquals("Mesh should contain 3 polygons after adding another array of 1", 3, mesh.polygons.size());
        assertSame("Third polygon from array should be at index 2", poly3, mesh.polygons.get(2));
    }
    
    /**
     * Tests adding an empty array of polygons using addPolygons(Polygon[] polygonArray).
     */
    @Test
    public void testAddEmptyPolygonsArray() {
        Mesh mesh = new Mesh();
        Polygon[] emptyArray = {};
        mesh.addPolygons(emptyArray);
        assertTrue("Polygons list should be empty after adding an empty array", mesh.polygons.isEmpty());
    }

    /**
     * Tests adding an array containing null polygons using addPolygons(Polygon[] polygonArray).
     */
    @Test
    public void testAddPolygonsArrayWithNull() {
        Mesh mesh = new Mesh();
        Polygon poly1 = new Polygon(new Vertex(0,0,0), new Vertex(1,0,0), new Vertex(0,1,0));
        Polygon[] arrayWithNulls = {poly1, null};
        mesh.addPolygons(arrayWithNulls);
        assertEquals("Mesh should contain 2 elements after adding array with one polygon and one null", 2, mesh.polygons.size());
        assertSame("First element should be poly1", poly1, mesh.polygons.get(0));
        assertNull("Second element should be null", mesh.polygons.get(1));
    }


    /**
     * Tests direct access to the public 'polygons' LinkedList.
     * Verifies that modifications to the list directly are possible (though perhaps not advisable).
     */
    @Test
    public void testDirectAccessToPolygonsList() {
        Mesh mesh = new Mesh();
        Polygon poly1 = new Polygon(new Vertex(0,0,0), new Vertex(1,0,0), new Vertex(0,1,0));
        
        // Directly add to the public list
        mesh.polygons.add(poly1);
        assertEquals("Mesh should contain 1 polygon after direct add", 1, mesh.polygons.size());
        assertSame("The directly added polygon should be in the list", poly1, mesh.polygons.get(0));

        // Test retrieving the list
        LinkedList<Polygon> retrievedPolygons = mesh.polygons;
        assertNotNull("Retrieved polygon list should not be null", retrievedPolygons);
        assertSame("Retrieved list should be the same instance as mesh.polygons", mesh.polygons, retrievedPolygons);
    }

    /**
     * Tests the render(PolygonRenderer renderer, Graphics g) method.
     * Verifies that the render method of the provided PolygonRenderer is called
     * for each polygon in the mesh.
     */
    @Test
    public void testRenderMethod() {
        Mesh mesh = new Mesh();
        Polygon poly1 = new Polygon(new Vertex(0,0,0), new Vertex(1,0,0), new Vertex(0,1,0));
        Polygon poly2 = new Polygon(new Vertex(1,1,1), new Vertex(2,1,1), new Vertex(1,2,1));
        
        mesh.addPolygon(poly1);
        mesh.addPolygon(poly2);

        MockPolygonRenderer mockRenderer = new MockPolygonRenderer();
        MockGraphics mockGraphics = new MockGraphics(); // Dummy graphics object

        mesh.render(mockRenderer, mockGraphics);

        assertEquals("Renderer's render method should have been called 2 times", 2, mockRenderer.renderCount);
        // We can also check if the last polygon rendered was poly2, assuming sequential iteration
        assertSame("Last polygon rendered should be poly2", poly2, mockRenderer.lastRenderedPolygon);
    }

    /**
     * Tests the render method with an empty mesh.
     * Verifies that the renderer is not called if the mesh has no polygons.
     */
    @Test
    public void testRenderEmptyMesh() {
        Mesh mesh = new Mesh();
        MockPolygonRenderer mockRenderer = new MockPolygonRenderer();
        MockGraphics mockGraphics = new MockGraphics();

        mesh.render(mockRenderer, mockGraphics);
        assertEquals("Renderer's render method should not be called for an empty mesh", 0, mockRenderer.renderCount);
    }
    
    /**
     * Tests the render method when the list of polygons contains null.
     * The PolygonRenderer's render method will receive null.
     * This test assumes the renderer is responsible for handling null polygons if necessary.
     */
    @Test
    public void testRenderMeshWithNullPolygon() {
        Mesh mesh = new Mesh();
        Polygon poly1 = new Polygon(new Vertex(0,0,0), new Vertex(1,0,0), new Vertex(0,1,0));
        mesh.addPolygon(poly1);
        mesh.addPolygon(null); // Add a null polygon

        MockPolygonRenderer mockRenderer = new MockPolygonRenderer();
        MockGraphics mockGraphics = new MockGraphics();

        mesh.render(mockRenderer, mockGraphics);

        assertEquals("Renderer's render method should have been called 2 times (once for poly1, once for null)", 2, mockRenderer.renderCount);
        assertNull("Last polygon passed to renderer should be null", mockRenderer.lastRenderedPolygon);
    }
}
