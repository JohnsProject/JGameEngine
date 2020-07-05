package com.johnsproject.jgameengine.model;

import com.johnsproject.jgameengine.shader.VertexBuffer;

public class Vertex {
	
	private final int index;
	private final int[] location;
	private final int[] normal;
	private final Material material;
	private VertexBuffer buffer;
	
	public Vertex(int index, int[] location, int[] normal, Material material) {
		this.index = index;
		this.location = location;
		this.normal = normal;
		this.material = material;
		this.buffer = new VertexBuffer();
	}

	public int getIndex() {
		return index;
	}

	public int[] getLocation() {
		return location;
	}
	
	public int[] getNormal() {
		return normal;
	}
	
	public Material getMaterial() {
		return material;
	}

	public VertexBuffer getBuffer() {
		return buffer;
	}

	public void setBuffer(VertexBuffer buffer) {
		this.buffer = buffer;
	}
}
