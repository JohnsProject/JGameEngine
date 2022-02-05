package com.johnsproject.jgameengine.math;

public class Shape {
	
	private final Point[] points;
	private final Triangle[] triangles;
	
	public Shape(Point[] points, Triangle[] triangles) {
		this.points = points;
		this.triangles = triangles;
	}

	public Point[] getPoints() {
		return points;
	}

	public Triangle[] getTriangles() {
		return triangles;
	}
	
}
