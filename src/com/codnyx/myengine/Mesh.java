package com.codnyx.myengine;

import java.awt.Graphics;
import java.util.LinkedList;

/**
 * Represents a 3D object as a collection of polygons.
 */
public class Mesh 
{
	/**
	 * The list of polygons that make up this mesh.
	 */
	public LinkedList<Polygon> polygons = new LinkedList<Polygon>();
	
	/**
	 * Adds a single polygon to this mesh.
	 * 
	 * @param polygon The polygon to add.
	 */
	public void addPolygon(Polygon polygon)
	{
		polygons.add(polygon);
	}
	
	/**
	 * Adds an array of polygons to this mesh.
	 * 
	 * @param polygon The array of polygons to add.
	 */
	public void addPolygons(Polygon[] polygon)
	{
		for(Polygon p: polygon)
			addPolygon(p);
	}
	
	/**
	 * Renders all polygons in this mesh using the provided renderer and graphics context.
	 * 
	 * @param renderer The polygon renderer to use.
	 * @param g The graphics context to render on.
	 */
	public void render(PolygonRenderer renderer, Graphics g)
	{
		for(Polygon p: polygons)
			renderer.render(g, p);
	}

}
