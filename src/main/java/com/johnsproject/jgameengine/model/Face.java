package com.johnsproject.jgameengine.model;

public class Face {
		
	private final int index;
	private final Vertex[] vertices;
	private final int[] localNormal;
	private final int[] worldNormal;
	private final int[][] uvs;
	private final Material material;
	
	public Face(int index, Vertex[] vertices, int[] normal, int[][] uvs, Material material) {
		this.index = index;
		this.vertices = vertices;
		this.localNormal = normal;
		this.worldNormal = normal.clone();
		this.uvs = uvs;
		this.material = material;
	}

	public int getIndex() {
		return index;
	}

	public Vertex getVertex(int index) {
		return vertices[index];
	}
	
	public Vertex[] getVertices() {
		return vertices;
	}
	
	public int[] getLocalNormal() {
		return localNormal;
	}

	public int[] getWorldNormal() {
		return worldNormal;
	}

	public int[] getUV(int index) {
		return uvs[index];
	}

	public int[][] getUVs() {
		return uvs;
	}

	public Material getMaterial() {
		return material;
	}
}
