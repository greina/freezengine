package com.codnyx.myengine;

import java.awt.Graphics;
import java.util.LinkedList;

public class Mesh 
{
	LinkedList<Polygon> polygons = new LinkedList<Polygon>();
	
	public void addPolygon(Polygon polygon)
	{
		polygons.add(polygon);
	}
	
	public void addPolygons(Polygon[] polygon)
	{
		for(Polygon p: polygon)
			addPolygon(p);
	}
	
	public void render(PolygonRenderer renderer, Graphics g)
	{
		for(Polygon p: polygons)
			renderer.render(g, p);
	}

}
