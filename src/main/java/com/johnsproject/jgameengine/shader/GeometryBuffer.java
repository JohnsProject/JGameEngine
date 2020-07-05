package com.johnsproject.jgameengine.shader;

import com.johnsproject.jgameengine.math.VectorMath;

public class GeometryBuffer {

	private final int[] worldNormal;
	private final int[][] uvs;
	private final VertexBuffer[] vertexBuffers;
	
	public GeometryBuffer() {
		this.worldNormal = VectorMath.emptyVector();
		this.uvs = new int[3][VectorMath.VECTOR_SIZE];
		this.uvs[0] = VectorMath.emptyVector();
		this.uvs[1] = VectorMath.emptyVector();
		this.uvs[2] = VectorMath.emptyVector();
		this.vertexBuffers = new VertexBuffer[3];
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

	public VertexBuffer getVertexBuffer(int index) {
		return vertexBuffers[index];
	}
	
	public VertexBuffer[] getVertexBuffers() {
		return vertexBuffers;
	}
}
