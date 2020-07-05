package com.johnsproject.jgameengine.model;

import com.johnsproject.jgameengine.math.ColorMath;
import com.johnsproject.jgameengine.math.VectorMath;
import com.johnsproject.jgameengine.shader.FlatSpecularShader;
import com.johnsproject.jgameengine.shader.SpecularProperties;

public class Mesh {

	private final Vertex[] vertices;
	private final Face[] faces;
	private final Material[] materials;
	
	public Mesh(Vertex[] vertices, Face[] faces, Material[] materials) {
		this.vertices = vertices;
		this.faces = faces;
		this.materials = materials;
	}
	
	public Mesh(int[][] vertices, int[][] faces, int[][] materials) {
		this.materials = new Material[materials.length];
		for (int i = 0; i < materials.length; i++) {
			int[] material = materials[i];
			FlatSpecularShader shader = new FlatSpecularShader();
			int color = ColorMath.toColor(material[0], material[1], material[2], material[3]);
			((SpecularProperties)shader.getProperties()).setDiffuseColor(color);
			this.materials[i] = new Material(i, "Material", shader);
		}
		this.vertices = new Vertex[vertices.length];
		for (int i = 0; i < vertices.length; i++) {
			int[] vertex = vertices[i];
			int[] location = VectorMath.emptyVector();
			location[0] = vertex[0];
			location[1] = vertex[1];
			location[2] = vertex[2];
			location[3] = vertex[3];
			int[] normal = location.clone();
			Material material = this.materials[vertex[4]];
			this.vertices[i] = new Vertex(i, location, normal, material);
		}
		this.faces = new Face[faces.length];
		for (int i = 0; i < faces.length; i++) {
			int[] face = faces[i];
			Vertex vertex1 = this.vertices[face[0]];
			Vertex vertex2 = this.vertices[face[1]];
			Vertex vertex3 = this.vertices[face[2]];
			Material material = this.materials[face[3]];
			int[] normal = new int[4];
			this.faces[i] = new Face(i, normal, vertex1, vertex2, vertex3, material);
		}
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
