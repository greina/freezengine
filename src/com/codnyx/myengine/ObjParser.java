package com.codnyx.myengine;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;

public class ObjParser 
{
	private boolean balanceVertices = true;
	private boolean computeNormals = true;

	public Mesh parseFile(String filename) throws IOException
	{
		BufferedReader stream = new BufferedReader(new FileReader(filename));
		return parseStream(stream);
		
	}

	public Mesh parseStream(BufferedReader stream) throws IOException
	{
		String line = null;
		ScanData sd = new ScanData();
		MeshData md = new MeshData();
		Mesh currentMesh = new Mesh();
		int lineindex = 0;
		stream.mark(10000000);
		boolean marked = false;
		// First read all the vertexData
		while((line = stream.readLine()) != null)
		{
			lineindex++;
			String[] tokens = line.trim().split("\\s+");
			if(tokens[0].equalsIgnoreCase("v"))
				sd.vertexList.add(parseVector(tokens));
			else if(tokens[0].equalsIgnoreCase("vn"))
				sd.normalList.add(parseVector(tokens));
			else
			{
				if(!marked)
				{
					marked = true;
					stream.mark(10000000);	
				}
				if(!tokens[0].equals("f"))
					System.out.println("Unknown value " + tokens[0] + " at line " + lineindex);
			}
			
		}
		
		if(isBalanceVertices())
			balanceVertices(sd.vertexList);
		
		// Read the faces
		stream.reset();
		lineindex = 0;
		while((line = stream.readLine()) != null)
		{
			lineindex++;
			String[] tokens = line.trim().split("\\s+");

			if(tokens[0].equalsIgnoreCase("f"))
				parseFace(tokens, md, sd);				
			
		}
				
		md.populateMesh(currentMesh);
		return currentMesh;
	}

	private void balanceVertices(ArrayList<float[]> array) {
		float[] c = {0,0,0};
		for(float[] v: array)
		{
			c[0] += v[0];
			c[1] += v[1];
			c[2] += v[2];
		}
		c[0] /= array.size();
		c[1] /= array.size();
		c[2] /= array.size();
		
		for(float[] v: array)
		{
			v[0] -= c[0];
			v[1] -= c[1];
			v[2] -= c[2];
		}
	}

	private void parseFace(String[] tokens, MeshData md, ScanData sd) 
	{
		
		String sample = tokens[1]; 	
		if(Pattern.matches("\\d+", sample))
			parseSimpleFace(tokens, md,sd);
		else if(Pattern.matches("\\d+//\\d+", sample))
			parseVNFace(tokens, md, sd);
		else if(Pattern.matches("\\d+/\\d+/\\d+", sample))
			parseFullFace(tokens,md, sd);
		
	}

	private void parseFullFace(String[] tokens, MeshData md, ScanData sd)  
	{
		short[] vindices = new short[tokens.length-1];
		short[] tindices = new short[tokens.length-1];
		short[] nindices = new short[tokens.length-1];
		for(int i = 1; i < tokens.length; i++)
		{
			String[] vn = tokens[i].split("/");
			vindices[i-1] = (short) (Short.parseShort(vn[0])-1);
			tindices[i-1] = (short) (Short.parseShort(vn[1])-1);	
			nindices[i-1] = (short) (Short.parseShort(vn[2])-1);	
		}
		
		for(int i = 2; i < vindices.length; i++)
		{
			md.addIndex(vindices[0], nindices[0], sd);
			md.addIndex(vindices[i-1], nindices[i-1], sd);
			md.addIndex(vindices[i], nindices[i], sd);
		}
	}

	private void parseVNFace(String[] tokens, MeshData md, ScanData sd) 
	{
		short[] vindices = new short[tokens.length-1];
		short[] nindices = new short[tokens.length-1];
		for(int i = 1; i < tokens.length; i++)
		{
			String[] vn = tokens[i].split("//");
			vindices[i-1] = (short) (Short.parseShort(vn[0])-1);
			nindices[i-1] = (short) (Short.parseShort(vn[1])-1);	
		}
		
		for(int i = 2; i < vindices.length; i++)
		{
			md.addIndex(vindices[0], nindices[0], sd);
			md.addIndex(vindices[i-1], nindices[i-1], sd);
			md.addIndex(vindices[i], nindices[i], sd);
		}
	}

	private void parseSimpleFace(String[] tokens, MeshData md, ScanData sd) 
	{
		short[] indices = new short[tokens.length-1];
		for(int i = 1; i < tokens.length; i++)
			indices[i-1] = (short) (Short.parseShort(tokens[i])-1);
		
		if(computeNormals)
		{
			// First we compute the normal
			float[] normal = {0,0,0};
			float[] v1 = {0,0,0};
			float[] v2 = {0,0,0};
			// Compute v1 from vertex1 to vertex2
			float[] vec1 = sd.vertexList.get(indices[0]);
			float[] vec2 = sd.vertexList.get(indices[1]);
			float[] vec3 = sd.vertexList.get(indices[2]);

			MyMath.subtract(vec2, vec1, v1);
			MyMath.subtract(vec3, vec2, v2);
			
			MyMath.crossProduct(v1, v2, normal);
			MyMath.normalize(normal);

			short normalIndex = -1;
			
			// Search for an existing normal in the list
			for(float[] n: sd.normalList)
				if(n[0] == normal[0] && n[1] == normal[1] && n[2] == normal[2])
				{
					normalIndex = (short) sd.normalList.indexOf(n);
				}
			if(normalIndex < 0)
			{
				normalIndex = (short) sd.normalList.size();
				sd.normalList.add(normal);
			}
			
			for(int i = 2; i < indices.length; i++)
			{
				md.addIndex(indices[0], normalIndex, sd);
				md.addIndex(indices[i-1], normalIndex, sd);
				md.addIndex(indices[i], normalIndex, sd);
			}
			
		}
		else
			for(int i = 2; i < indices.length; i++)
			{
				md.addIndex(indices[0], sd);
				md.addIndex(indices[i-1], sd);
				md.addIndex(indices[i], sd);
			}
		
	}

	private float[] parseVector(String[] tokens) {
		float[] v = {0,0,0};
		for(int i = 1; i < Math.min(4, tokens.length); i++)
			v[i-1] = Float.parseFloat(tokens[i]);
		return v;
	}

	public boolean isBalanceVertices() {
		return balanceVertices;
	}

	public void setBalanceVertices(boolean balanceVertices) {
		this.balanceVertices = balanceVertices;
	}
}

class MeshData
{
	HashMap<VertexData, Short> ids = new HashMap<VertexData, Short>();
	ArrayList<float[]> vertices = new ArrayList<float[]>();
	ArrayList<float[]> normals = new ArrayList<float[]>();
	ArrayList<Short> indices = new ArrayList<Short>();
	
	
	void populateMesh(Mesh m)
	{
		Boolean areNormals = null;
		for(float[] normal:normals)
		{
			if(normal == null)
			{
				if(areNormals == null)
					areNormals = false;
				else if(areNormals == true)
					throw new RuntimeException();
			}
			else
			{
				if (areNormals == null)
					areNormals = true;
				else if(areNormals == false)
					throw new RuntimeException();
			}
		}
		Vertex[] vs = new Vertex[vertices.size()];
		for(int i = 0; i < vs.length; i++)
		{
			vs[i] = new Vertex(vertices.get(i));
			if(areNormals)
				vs[i].normal = normals.get(i);
		}
		for(int i = 0; i < indices.size(); i+=3)
		{
			Vertex[] v = new Vertex[]{
					vs[indices.get(i)], vs[indices.get(i+1)], vs[indices.get(i+2)]
			};
			m.addPolygon(new Polygon(v));
		}
	}

	void addIndex(short vertexIndex, ScanData sd)
	{
		VertexData key = new VertexData(vertexIndex,(short)-1,(short) -1);
		Short index = ids.get(key);
		if(index == null)
		{
			short newIndex = (short) vertices.size();
			vertices.add(sd.vertexList.get(vertexIndex));
			normals.add(null);
			ids.put(key, newIndex);
			indices.add(newIndex);
		}
		else
			indices.add(index);
	}
	
	void addIndex(short vertexIndex, short normalIndex, ScanData sd)
	{
		VertexData key = new VertexData(vertexIndex,normalIndex,(short) -1);
		Short index = ids.get(key);
		if(index == null)
		{
			short newIndex = (short) vertices.size();
			vertices.add(sd.vertexList.get(vertexIndex));
			normals.add(sd.normalList.get(normalIndex));
			ids.put(key, newIndex);
			indices.add(newIndex);
		}
		else
			indices.add(index);
	}
	
}

class ScanData
{
	ArrayList<float[]> vertexList = new ArrayList<float[]>();
	ArrayList<float[]> normalList = new ArrayList<float[]>();
}

class VertexData
{
	short vindex;
	short tindex;
	short nindex;

	public VertexData(short vindex, short tindex, short nindex)
	{
		this.vindex = vindex;
		this.tindex = tindex;
		this.nindex = nindex;
	}
	
	@Override
	public boolean equals(Object o) 
	{
		VertexData other = (VertexData)o;
		return other.vindex == vindex && other.tindex == tindex && other.nindex == nindex;
	}
	
	@Override
	public int hashCode() {
		return vindex ^ tindex ^ nindex;
	}
}
