package com.johnsproject.jpge2.shader;

import com.johnsproject.jpge2.library.VectorLibrary;

public class GouraudTriangle extends FlatTriangle {
	
	private final int[] red;
	private final int[] green;
	private final int[] blue;
	
	public GouraudTriangle() {
		VectorLibrary vectorLibrary = new VectorLibrary();
		red = vectorLibrary.generate();
		green = vectorLibrary.generate();
		blue = vectorLibrary.generate();
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
