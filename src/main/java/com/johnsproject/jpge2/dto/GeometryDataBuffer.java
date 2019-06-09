package com.johnsproject.jpge2.dto;

public class GeometryDataBuffer {

	private final Face face;
	private final int[] normal;
	private final int[][] uvs;
	private final Material material;
	private final VertexDataBuffer[] vertexDataBuffers;
	
	public GeometryDataBuffer(Face face) {
		this.face = face;
		this.normal = face.getNormal().clone();
		this.uvs = face.getUVs().clone();
		this.uvs[0] = face.getUV(0).clone();
		this.uvs[0] = face.getUV(1).clone();
		this.uvs[0] = face.getUV(2).clone();
		this.material = face.getMaterial();
		this.vertexDataBuffers = new VertexDataBuffer[3];
		this.vertexDataBuffers[0] = face.getVertex(0).getDataBuffer();
		this.vertexDataBuffers[1] = face.getVertex(1).getDataBuffer();
		this.vertexDataBuffers[2] = face.getVertex(2).getDataBuffer();
	}

	public int[] getNormal() {
		return normal;
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

	public VertexDataBuffer getVertexDataBuffer(int index) {
		return vertexDataBuffers[index];
	}
	
	public VertexDataBuffer[] getVertexDataBuffers() {
		return vertexDataBuffers;
	}
	
	public void reset() {
		for (int i = 0; i < normal.length; i++) {
			normal[i] = face.getNormal()[i];
			uvs[0][i] = face.getUV(0)[i];
			uvs[1][i] = face.getUV(1)[i];
			uvs[2][i] = face.getUV(2)[i];
		}
	}
}
