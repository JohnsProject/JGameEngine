package com.johnsproject.jgameengine.shader;

import com.johnsproject.jgameengine.math.VectorMath;
import com.johnsproject.jgameengine.model.Vertex;

public class GeometryBuffer {

	private final int[] worldNormal;
	private final int[][] uvs;
	private final Vertex[] vertices;
	
	public GeometryBuffer() {
		this.worldNormal = VectorMath.emptyVector();
		this.uvs = new int[3][VectorMath.VECTOR_SIZE];
		this.uvs[0] = VectorMath.emptyVector();
		this.uvs[1] = VectorMath.emptyVector();
		this.uvs[2] = VectorMath.emptyVector();
		this.vertices = new Vertex[3];
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

	public Vertex getVertex(int index) {
		return vertices[index];
	}
	
	public Vertex[] getVertices() {
		return vertices;
	}
}
