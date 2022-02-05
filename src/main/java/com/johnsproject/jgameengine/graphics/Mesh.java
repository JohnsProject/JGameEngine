package com.johnsproject.jgameengine.graphics;

import com.johnsproject.jgameengine.math.Point;
import com.johnsproject.jgameengine.math.Shape;
import com.johnsproject.jgameengine.math.Triangle;

public class Mesh {

	private final Shape shape;
	private final Vertex[] vertices;
	private final Face[] faces;
	private final Material[] materials;
	
	public Mesh(Vertex[] vertices, Face[] faces, Material[] materials) {
		this.vertices = vertices;
		this.faces = faces;
		this.materials = materials;
		
		final Point[] points = new Point[vertices.length];
		for (int i = 0; i < points.length; i++)
			points[i] = vertices[i].getPoint();
		
		final Triangle[] triangles = new Triangle[faces.length];
		for (int i = 0; i < triangles.length; i++)
			triangles[i] = faces[i].getTriangle();
		
		this.shape = new Shape(points, triangles);
	}

	public Shape getShape() {
		return shape;
	}
	
	public Vertex[] getVertices(){
		return vertices;
	}
	
	public Vertex getVertex(int index){
		return vertices[index];
	}
	
	public Face[] getFaces() {
		return faces;
	}
	
	public Face getFace(int index) {
		return faces[index];
	}
	
	public Material[] getMaterials() {
		return materials;
	}
	
	public Material getMaterial(int index) {
		return materials[index];
	}
	
	public Material getMaterial(String name) {
		for (int i = 0; i < materials.length; i++) {
			Material material = materials[i];
			if (material.getName().equals(name)) {
				return material;
			}
		}
		return materials[0];
	}	
}
