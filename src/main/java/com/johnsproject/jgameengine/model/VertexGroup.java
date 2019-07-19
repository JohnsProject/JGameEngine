package com.johnsproject.jgameengine.model;

public class VertexGroup {

	private final int boneIndex;
	private final Vertex[] vertices;
	private final int[] weights;
	
	public VertexGroup(int boneIndex, Vertex[] vertices, int[] weights) {
		this.boneIndex = boneIndex;
		this.vertices = vertices;
		this.weights = weights;
	}
	
	public int getBoneIndex() {
		return boneIndex;
	}
	
	public Vertex getVertex(int index) {
		return vertices[index];
	}
	
	public Vertex[] getVertices() {
		return vertices;
	}
	
	public int getWeight(int index) {
		return weights[index];
	}
	
	public int[] getWeights() {
		return weights;
	}
}
