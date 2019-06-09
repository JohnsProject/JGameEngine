package com.johnsproject.jpge2.dto;

public class VertexDataBuffer {

	private final Vertex vertex;
	private final int[] location;
	private final int[] normal;
	private final Material material;
	private int lightColor;
	
	public VertexDataBuffer(Vertex vertex) {
		this.vertex = vertex;
		this.location = vertex.getLocation().clone();
		this.normal = vertex.getNormal().clone();
		this.material = vertex.getMaterial();
		this.lightColor = 0;
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

	public int getLightColor() {
		return lightColor;
	}

	public void setLightColor(int lightColor) {
		this.lightColor = lightColor;
	}

	public void reset() {
		for (int i = 0; i < location.length; i++) {
			location[i] = vertex.getLocation()[i];
			normal[i] = vertex.getNormal()[i];
		}
		lightColor = 0;
	}
	
}
