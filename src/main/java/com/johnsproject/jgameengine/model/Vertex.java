package com.johnsproject.jgameengine.model;

public class Vertex {
	
	private final int index;
	private final int[] localLocation;
	private final int[] localNormal;
	private final int[] worldLocation;
	private final int[] worldNormal;
	private final int[] location;
	private final Material material;
	private int shadedColor;
	
	public Vertex(int index, int[] location, int[] normal, Material material) {
		this.index = index;
		this.localLocation = location;
		this.localNormal = normal;
		this.worldLocation = location.clone();
		this.worldNormal = normal.clone();
		this.location = location.clone();
		this.material = material;
	}

	public int getIndex() {
		return index;
	}
	
	public int[] getLocalLocation() {
		return localLocation;
	}

	public int[] getLocalNormal() {
		return localNormal;
	}

	public int[] getWorldLocation() {
		return worldLocation;
	}

	public int[] getWorldNormal() {
		return worldNormal;
	}

	public int[] getLocation() {
		return location;
	}

	public Material getMaterial() {
		return material;
	}

	public int getShadedColor() {
		return shadedColor;
	}

	public void setShadedColor(int shadedColor) {
		this.shadedColor = shadedColor;
	}
}
