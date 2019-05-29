package com.johnsproject.jpge2.dto;

import com.johnsproject.jpge2.library.VectorLibrary;

public class Triangle {

	private static final byte VECTOR_X = VectorLibrary.VECTOR_X;
	private static final byte VECTOR_Y = VectorLibrary.VECTOR_Y;
	private static final byte VECTOR_Z = VectorLibrary.VECTOR_Z;
	
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

	public void setLocation1(int[] location) {
		this.location1[VECTOR_X] = location[VECTOR_X];
		this.location1[VECTOR_Y] = location[VECTOR_Y];
		this.location1[VECTOR_Z] = location[VECTOR_Z];
	}
	
	public void setLocation2(int[] location) {
		this.location2[VECTOR_X] = location[VECTOR_X];
		this.location2[VECTOR_Y] = location[VECTOR_Y];
		this.location2[VECTOR_Z] = location[VECTOR_Z];
	}
	
	public void setLocation3(int[] location) {
		this.location3[VECTOR_X] = location[VECTOR_X];
		this.location3[VECTOR_Y] = location[VECTOR_Y];
		this.location3[VECTOR_Z] = location[VECTOR_Z];
	}
	
	public void setUV1(int[] uv) {
		this.u[0] = uv[VECTOR_X];
		this.v[0] = uv[VECTOR_Y];
	}
	
	public void setUV2(int[] uv) {
		this.u[1] = uv[VECTOR_X];
		this.v[1] = uv[VECTOR_Y];
	}
	
	public void setUV3(int[] uv) {
		this.u[2] = uv[VECTOR_X];
		this.v[2] = uv[VECTOR_Y];
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
