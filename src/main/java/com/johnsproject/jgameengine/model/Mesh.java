package com.johnsproject.jgameengine.model;

public class Mesh {

	private final Vertex[] vertices;
	private final Face[] faces;
	private final Material[] materials;
	
	public Mesh(Vertex[] vertices, Face[] faces, Material[] materials) {
		this.vertices = vertices;
		this.faces = faces;
		this.materials = materials;
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
