package com.johnsproject.jpge2.shader;

import com.johnsproject.jpge2.library.VectorLibrary;

public class TexturedFlatTriangle extends FlatTriangle {

	private final int[] u;
	private final int[] v;
	
	public TexturedFlatTriangle() {
		VectorLibrary vectorLibrary = new VectorLibrary();
		u = vectorLibrary.generate();
		v = vectorLibrary.generate();
	}
	
	public int[] getU() {
		return u;
	}

	public int[] getV() {
		return v;
	}
}
