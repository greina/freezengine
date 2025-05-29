package com.codnyx.myengine.tests;

import static org.junit.Assert.*;
import org.junit.Test;

import com.codnyx.myengine.Mesh;
import com.codnyx.myengine.Polygon;
import com.codnyx.myengine.Vertex;
import com.codnyx.myengine.PolygonRenderer;
import org.mockito.Mockito;
import static org.mockito.Mockito.*;

import java.awt.Graphics;
import java.util.LinkedList;


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
        Polygon poly1 = new Polygon(new Vertex[]{new Vertex(new float[]{0f,0f,0f}), new Vertex(new float[]{1f,0f,0f}), new Vertex(new float[]{0f,1f,0f})});
        
        mesh.addPolygon(poly1);
        assertEquals("Mesh should contain 1 polygon after adding one", 1, mesh.polygons.size());
        assertSame("The added polygon should be the one in the list", poly1, mesh.polygons.get(0));

        Polygon poly2 = new Polygon(new Vertex[]{new Vertex(new float[]{2f,0f,0f}), new Vertex(new float[]{3f,0f,0f}), new Vertex(new float[]{2f,1f,0f})});
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
        Polygon poly1 = new Polygon(new Vertex[]{new Vertex(new float[]{0f,0f,0f}), new Vertex(new float[]{1f,0f,0f}), new Vertex(new float[]{0f,1f,0f})});
        Polygon poly2 = new Polygon(new Vertex[]{new Vertex(new float[]{1f,1f,1f}), new Vertex(new float[]{2f,1f,1f}), new Vertex(new float[]{1f,2f,1f})});
        Polygon poly3 = new Polygon(new Vertex[]{new Vertex(new float[]{2f,2f,2f}), new Vertex(new float[]{3f,2f,2f}), new Vertex(new float[]{2f,3f,2f})});

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
        Polygon poly1 = new Polygon(new Vertex[]{new Vertex(new float[]{0f,0f,0f}), new Vertex(new float[]{1f,0f,0f}), new Vertex(new float[]{0f,1f,0f})});
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
        Polygon poly1 = new Polygon(new Vertex[]{new Vertex(new float[]{0f,0f,0f}), new Vertex(new float[]{1f,0f,0f}), new Vertex(new float[]{0f,1f,0f})});
        
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
        Polygon poly1 = new Polygon(new Vertex[]{new Vertex(new float[]{0f,0f,0f}), new Vertex(new float[]{1f,0f,0f}), new Vertex(new float[]{0f,1f,0f})});
        Polygon poly2 = new Polygon(new Vertex[]{new Vertex(new float[]{1f,1f,1f}), new Vertex(new float[]{2f,1f,1f}), new Vertex(new float[]{1f,2f,1f})});
        
        mesh.addPolygon(poly1);
        mesh.addPolygon(poly2);

        PolygonRenderer mockRenderer = Mockito.mock(PolygonRenderer.class);
        Graphics mockGraphics = Mockito.mock(Graphics.class);

        mesh.render(mockRenderer, mockGraphics);

        // Verify render was called for each polygon
        Mockito.verify(mockRenderer, times(1)).render(mockGraphics, poly1);
        Mockito.verify(mockRenderer, times(1)).render(mockGraphics, poly2);
        // Verify it was called exactly 2 times in total
        Mockito.verify(mockRenderer, times(2)).render(eq(mockGraphics), any(Polygon.class));
    }

    /**
     * Tests the render method with an empty mesh.
     * Verifies that the renderer is not called if the mesh has no polygons.
     */
    @Test
    public void testRenderEmptyMesh() {
        Mesh mesh = new Mesh();
        PolygonRenderer mockRenderer = Mockito.mock(PolygonRenderer.class);
        Graphics mockGraphics = Mockito.mock(Graphics.class);

        mesh.render(mockRenderer, mockGraphics);
        // Verify render was never called
        Mockito.verify(mockRenderer, never()).render(any(Graphics.class), any(Polygon.class));
    }
    
    /**
     * Tests the render method when the list of polygons contains null.
     * The PolygonRenderer's render method will receive null.
     * This test assumes the renderer is responsible for handling null polygons if necessary.
     */
    @Test
    public void testRenderMeshWithNullPolygon() {
        Mesh mesh = new Mesh();
        Polygon poly1 = new Polygon(new Vertex[]{new Vertex(new float[]{0f,0f,0f}), new Vertex(new float[]{1f,0f,0f}), new Vertex(new float[]{0f,1f,0f})});
        mesh.addPolygon(poly1);
        mesh.addPolygon(null); // Add a null polygon

        PolygonRenderer mockRenderer = Mockito.mock(PolygonRenderer.class);
        Graphics mockGraphics = Mockito.mock(Graphics.class);
        
        mesh.render(mockRenderer, mockGraphics);

        Mockito.verify(mockRenderer, times(1)).render(eq(mockGraphics), eq(poly1));
        Mockito.verify(mockRenderer, times(1)).render(eq(mockGraphics), (Polygon) Mockito.isNull());
        // The two lines above ensure exactly one call with poly1 and one call with null.
        // This implicitly means two calls in total with the expected arguments.
        // No need for an additional "times(2)" with a broader matcher if these pass.
    }
}
