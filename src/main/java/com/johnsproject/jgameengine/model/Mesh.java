package com.johnsproject.jgameengine.model;

import com.johnsproject.jgameengine.util.ColorUtils;
import com.johnsproject.jgameengine.util.VectorUtils;

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
			int[] materialData = materials[i];
			int color = ColorUtils.toColor(materialData[0], materialData[1], materialData[2], materialData[3]);
			Material material = new Material(i, "Material");
			material.setDiffuseColor(color);
			this.materials[i] = material;
		}
		this.vertices = new Vertex[vertices.length];
		for (int i = 0; i < vertices.length; i++) {
			int[] vertex = vertices[i];
			int[] location = VectorUtils.emptyVector();
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
			Vertex[] faceVertices = new Vertex[] {vertex1, vertex2, vertex3};
			int[] normal = new int[4];
			int[][] uvs = new int[3][VectorUtils.VECTOR_SIZE];
			uvs[0] = VectorUtils.emptyVector();
			uvs[1] = VectorUtils.emptyVector();
			uvs[2] = VectorUtils.emptyVector();
			Material material = this.materials[face[3]];
			this.faces[i] = new Face(i, faceVertices, normal, uvs, material);
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
