package com.johnsproject.jgameengine.model;

import com.johnsproject.jgameengine.util.VectorUtils;

public class Fragment {

	private final int[] location;
	private final int[] worldLocation;
	private final int[] worldNormal;
	private final int[] uv;
	private Material material;
	private int lightColor;
	
	public Fragment() {
		this.location = VectorUtils.emptyVector();
		this.worldLocation = VectorUtils.emptyVector();
		this.worldNormal = VectorUtils.emptyVector();
		this.uv = VectorUtils.emptyVector();
	}

	public int[] getLocation() {
		return location;
	}

	public int[] getWorldLocation() {
		return worldLocation;
	}

	public int[] getWorldNormal() {
		return worldNormal;
	}

	public int[] getUV() {
		return uv;
	}
	
	public Material getMaterial() {
		return material;
	}

	public void setMaterial(Material material) {
		this.material = material;
	}

	/**
	 * Returns the light color of this {@link Fragment}.
	 * The light color is the color of all lights that reach and affect the illumination of
	 * this Fragment put together.
	 * 
	 * @return The light color of this Fragment.
	 */
	public int getLightColor() {
		return lightColor;
	}

	/**
	 * Sets the light color of this {@link Fragment}.
	 * The light color is the color of all lights that reach and affect the illumination of 
	 * this Fragment put together.
	 * 
	 * @param lightColor to set.
	 */
	public void setLightColor(int lightColor) {
		this.lightColor = lightColor;
	}
}
