package com.johnsproject.jpge2.dto;

public class GeometryDataBuffer {

	private final Face face;
	private final int[] normal;
	private final int[] uv1;
	private final int[] uv2;
	private final int[] uv3;
	private final Material material;
	private final VertexDataBuffer[] vertexDataBuffers;
	
	public GeometryDataBuffer(Face face) {
		this.face = face;
		this.normal = face.getNormal().clone();
		this.uv1 = face.getUV1().clone();
		this.uv2 = face.getUV2().clone();
		this.uv3 = face.getUV3().clone();
		this.material = face.getMaterial();
		this.vertexDataBuffers = new VertexDataBuffer[3];
		this.vertexDataBuffers[0] = face.getVertex(0).getDataBuffer();
		this.vertexDataBuffers[1] = face.getVertex(1).getDataBuffer();
		this.vertexDataBuffers[2] = face.getVertex(2).getDataBuffer();
	}

	public int[] getNormal() {
		return normal;
	}

	public int[] getUV1() {
		return uv1;
	}

	public int[] getUV2() {
		return uv2;
	}

	public int[] getUV3() {
		return uv3;
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
			uv1[i] = face.getUV1()[i];
			uv2[i] = face.getUV2()[i];
			uv3[i] = face.getUV3()[i];
		}
	}
}
