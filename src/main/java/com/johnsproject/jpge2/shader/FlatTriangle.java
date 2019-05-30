package com.johnsproject.jpge2.shader;

import com.johnsproject.jpge2.library.VectorLibrary;

public class FlatTriangle {

	private final int[] location1;
	private final int[] location2;
	private final int[] location3;
	
	public FlatTriangle() {
		VectorLibrary vectorLibrary = new VectorLibrary();
		location1 = vectorLibrary.generate();
		location2 = vectorLibrary.generate();
		location3 = vectorLibrary.generate();
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
}
