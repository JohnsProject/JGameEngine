package com.johnsproject.jgameengine.shader;

import com.johnsproject.jgameengine.math.VectorMath;

public class VertexBuffer {

	private final int[] location;
	private final int[] worldNormal;
	private final int[] worldLocation;
	private int color;
	
	public VertexBuffer() {
		this.location = VectorMath.emptyVector();
		this.worldNormal = VectorMath.emptyVector();
		this.worldLocation = VectorMath.emptyVector();
		this.color = 0;
	}
	
	public int[] getLocation() {
		return location;
	}
	
	public int[] getWorldNormal() {
		return worldNormal;
	}

	public int[] getWorldLocation() {
		return worldLocation;
	}
	
	public int getColor() {
		return color;
	}

	public void setColor(int color) {
		this.color = color;
	}
}
