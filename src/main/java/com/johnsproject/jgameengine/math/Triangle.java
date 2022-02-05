package com.johnsproject.jgameengine.math;

public class Triangle {

	private final Point[] points;
	
	public Triangle() {
		this.points = new Point[] {new Point(), new Point(), new Point()};
	}
	
	public Triangle(Point[] points) {
		this.points = points;
	}

	public Point[] getPoints() {
		return points;
	}	
	
}
