package com.johnsproject.jgameengine.graphics;

import com.johnsproject.jgameengine.math.Point;
import com.johnsproject.jgameengine.math.Vector;

public class Vertex {
	
	private final int index;
	private final Point point;
	private final int[] worldLocation;
	private final int[] worldNormal;
	private final int[] location;
	private final Material material;
	private int lightColor;
	
	public Vertex(int index, int[] location, Material material) {
		this(index, new Point(location), material);
	}
	
	public Vertex(int index, Point point, Material material) {
		this.index = index;
		this.point = point;
		this.worldLocation = Vector.emptyVector();
		this.worldNormal = Vector.emptyVector();
		this.location = Vector.emptyVector();
		this.material = material;
	}

	public int getIndex() {
		return index;
	}
	
	public Point getPoint() {
		return point;
	}
	
	public int[] getLocalLocation() {
		return point.getLocation();
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

	/**
	 * Returns the light color of this {@link Vertex}.
	 * The light color is the color of all lights that reach and affect the illumination of
	 * this Vertex put together.
	 * 
	 * @return The light color of this Vertex.
	 */
	public int getLightColor() {
		return lightColor;
	}

	/**
	 * Sets the light color of this {@link Vertex}.
	 * The light color is the color of all lights that reach and affect the illumination of 
	 * this Vertex put together.
	 * 
	 * @param lightColor to set.
	 */
	public void setLightColor(int lightColor) {
		this.lightColor = lightColor;
	}
}