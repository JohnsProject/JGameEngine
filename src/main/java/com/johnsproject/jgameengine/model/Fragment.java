package com.johnsproject.jgameengine.model;

import com.johnsproject.jgameengine.math.VectorMath;

public class Fragment {

	private final int[] location;
	private final int[] worldLocation;
	private final int[] worldNormal;
	private final int[] uv;
	private int color;
	
	public Fragment() {
		this.location = VectorMath.emptyVector();
		this.worldLocation = VectorMath.emptyVector();
		this.worldNormal = VectorMath.emptyVector();
		this.uv = VectorMath.emptyVector();
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
