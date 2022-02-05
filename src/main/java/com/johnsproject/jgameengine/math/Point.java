package com.johnsproject.jgameengine.math;

public class Point {

	private final int[] location;
	
	public Point() {
		this.location = new int[Vector.VECTOR_SIZE];
	}
	
	public Point(int[] location) {
		this.location = location;
	}

	public int[] getLocation() {
		return location;
	}	
}
