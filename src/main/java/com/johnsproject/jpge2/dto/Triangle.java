package com.johnsproject.jpge2.dto;

import com.johnsproject.jpge2.library.VectorLibrary;

public class Triangle {

	private final int[] location1;
	private final int[] location2;
	private final int[] location3;
	private final int[] u;
	private final int[] v;
	private final int[] red;
	private final int[] green;
	private final int[] blue;
	
	public Triangle() {
		VectorLibrary vectorLibrary = new VectorLibrary();
		location1 = vectorLibrary.generate();
		location2 = vectorLibrary.generate();
		location3 = vectorLibrary.generate();
		u = vectorLibrary.generate();
		v = vectorLibrary.generate();
		red = vectorLibrary.generate();
		green = vectorLibrary.generate();
		blue = vectorLibrary.generate();
	}

	public int[] getLocation1() {
		return location1;
	}

	public int[] getLocation2() {
		return location2;
	}

	public int[] getLocation3() {
		return location3;
	}

	public int[] getU() {
		return u;
	}

	public int[] getV() {
		return v;
	}

	public int[] getRed() {
		return red;
	}

	public int[] getGreen() {
		return green;
	}

	public int[] getBlue() {
		return blue;
	}
}
