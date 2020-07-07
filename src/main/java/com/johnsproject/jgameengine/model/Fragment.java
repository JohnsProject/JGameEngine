package com.johnsproject.jgameengine.model;

import com.johnsproject.jgameengine.util.VectorUtils;

public class Fragment {

	private final int[] location;
	private final int[] worldLocation;
	private final int[] worldNormal;
	private final int[] uv;
	private int color;
	
	public Fragment() {
		this.location = VectorUtils.emptyVector();
		this.worldLocation = VectorUtils.emptyVector();
		this.worldNormal = VectorUtils.emptyVector();
		this.uv = VectorUtils.emptyVector();
		this.color = 0;
	}

	public int getColor() {
		return color;
	}

	public void setColor(int color) {
		this.color = color;
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
}
