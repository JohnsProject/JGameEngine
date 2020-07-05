package com.johnsproject.jgameengine.model;

import com.johnsproject.jgameengine.math.VectorMath;
import com.johnsproject.jgameengine.shader.GeometryBuffer;

public class Face {
		
	private final int index;
	private final Vertex[] vertices;
	private final int[] normal;
	private final int[][] uvs;
	private final Material material;
	private GeometryBuffer buffer;

	public Face(int index, int[] normal, Vertex vertex1, Vertex vertex2, Vertex vertex3, Material material, int[] uv1, int[] uv2, int[] uv3) {
		this.index = index;
		this.vertices = new Vertex[3];
		this.vertices[0] = vertex1;
		this.vertices[1] = vertex2;
		this.vertices[2] = vertex3;
		this.normal = normal;
		this.uvs = new int[3][4];
		this.uvs[0] = uv1;
		this.uvs[1] = uv2;
		this.uvs[2] = uv3;
		this.material = material;
		this.buffer = new GeometryBuffer();
	}
	
	public Face(int index, int[] normal, Vertex vertex1, Vertex vertex2, Vertex vertex3, Material material) {
		this.index = index;
		this.vertices = new Vertex[3];
		this.vertices[0] = vertex1;
		this.vertices[1] = vertex2;
		this.vertices[2] = vertex3;
		this.normal = normal;
		this.uvs = new int[3][4];
		this.uvs[0] = VectorMath.emptyVector();
		this.uvs[1] = VectorMath.emptyVector();
		this.uvs[2] = VectorMath.emptyVector();
		this.material = material;
		this.buffer = new GeometryBuffer();
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

	public GeometryBuffer getBuffer() {
		return buffer;
	}

	public void setBuffer(GeometryBuffer buffer) {
		this.buffer = buffer;
	}
}
