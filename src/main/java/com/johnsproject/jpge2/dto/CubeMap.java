package com.johnsproject.jpge2.dto;

import com.johnsproject.jpge2.library.MathLibrary;
import com.johnsproject.jpge2.library.VectorLibrary;

public class CubeMap {

	private static final byte VECTOR_X = VectorLibrary.VECTOR_X;
	private static final byte VECTOR_Y = VectorLibrary.VECTOR_Y;
	private static final byte VECTOR_Z = VectorLibrary.VECTOR_Z;
	
	private static final int FP_BITS = MathLibrary.FP_BITS;
	private static final int FP_HALF = MathLibrary.FP_HALF;

	public static final byte FACE_LEFT = 0;
	public static final byte FACE_RIGHT = 1;
	public static final byte FACE_TOP = 4;
	public static final byte FACE_BOTTOM = 5;
	public static final byte FACE_FRONT = 2;
	public static final byte FACE_BACK = 3;

	private final Texture[] faces;

	public CubeMap(int width, int height) {
		faces = new Texture[6];
		for (int i = 0; i < faces.length; i++) {
			faces[i] = new Texture(width, height);
		}
	}

	public CubeMap(Texture left, Texture right, Texture top, Texture bottom, Texture front, Texture back) {
		faces = new Texture[6];
		faces[FACE_LEFT] = left;
		faces[FACE_RIGHT] = right;
		faces[FACE_TOP] = top;
		faces[FACE_BOTTOM] = bottom;
		faces[FACE_FRONT] = front;
		faces[FACE_BACK] = back;
	}

	public int getPixel(int[] direction) {
		int absX = Math.abs(direction[VECTOR_X]);
		int absY = Math.abs(direction[VECTOR_Y]);
		int absZ = Math.abs(direction[VECTOR_Z]);
		long faceIndex = 0, ma = 0, u = 0, v = 0;
		if(absZ >= absX && absZ >= absY) {
			faceIndex = direction[VECTOR_Z] < 0 ? FACE_BACK : FACE_FRONT;
			ma = absZ;
			u = direction[VECTOR_Z] < 0 ? -direction[VECTOR_X] : direction[VECTOR_X];
			v = -direction[VECTOR_Y];
		} else if(absY >= absX) {
			faceIndex = direction[VECTOR_Y] < 0 ? FACE_BOTTOM : FACE_TOP;
			ma = absY;
			u = direction[VECTOR_X];
			v = direction[VECTOR_Y] < 0 ? -direction[VECTOR_Z] : direction[VECTOR_Z];
		} else {
			faceIndex = direction[VECTOR_X] < 0 ? FACE_RIGHT : FACE_LEFT;
			ma = absX;
			u = direction[VECTOR_X] < 0 ? direction[VECTOR_Z] : -direction[VECTOR_Z];
			v = -direction[VECTOR_Y];
		}
		Texture face = faces[(int)faceIndex];
		ma <<= 1;
		u = ((u << FP_BITS) / ma) + FP_HALF;
		v = ((v << FP_BITS) / ma) + FP_HALF;
		u = (u * face.getWidth() + FP_HALF) >> FP_BITS;
		v = (v * face.getHeight() + FP_HALF) >> FP_BITS;
		return face.getPixel((int)u, (int)v);
	}
	
	public void setPixel(int[] direction, int value) {
		int absX = Math.abs(direction[VECTOR_X]);
		int absY = Math.abs(direction[VECTOR_Y]);
		int absZ = Math.abs(direction[VECTOR_Z]);
		long faceIndex = 0, ma = 0, u = 0, v = 0;
		if(absZ >= absX && absZ >= absY) {
			faceIndex = direction[VECTOR_Z] < 0 ? FACE_BACK : FACE_FRONT;
			ma = absZ;
			u = direction[VECTOR_Z] < 0 ? -direction[VECTOR_X] : direction[VECTOR_X];
			v = -direction[VECTOR_Y];
		} else if(absY >= absX) {
			faceIndex = direction[VECTOR_Y] < 0 ? FACE_BOTTOM : FACE_TOP;
			ma = absY;
			u = direction[VECTOR_X];
			v = direction[VECTOR_Y] < 0 ? -direction[VECTOR_Z] : direction[VECTOR_Z];
		} else {
			faceIndex = direction[VECTOR_X] < 0 ? FACE_RIGHT : FACE_LEFT;
			ma = absX;
			u = direction[VECTOR_X] < 0 ? direction[VECTOR_Z] : -direction[VECTOR_Z];
			v = -direction[VECTOR_Y];
		}
		Texture face = faces[(int)faceIndex];
		ma <<= 1;
		u = ((u << FP_BITS) / ma) + FP_HALF;
		v = ((v << FP_BITS) / ma) + FP_HALF;
		u = (u * face.getWidth() + FP_HALF) >> FP_BITS;
		v = (v * face.getHeight() + FP_HALF) >> FP_BITS;
		face.setPixel((int)u, (int)v, value);
	}

	public int getFace(int[] direction) {
		int absX = Math.abs(direction[VECTOR_X]);
		int absY = Math.abs(direction[VECTOR_Y]);
		int absZ = Math.abs(direction[VECTOR_Z]);
		int faceIndex = 0;
		if(absZ >= absX && absZ >= absY) {
			faceIndex = direction[VECTOR_Z] < 0 ? FACE_BACK : FACE_FRONT;
		} else if(absY >= absX) {
			faceIndex = direction[VECTOR_Y] < 0 ? FACE_BOTTOM : FACE_TOP;
		} else {
			faceIndex = direction[VECTOR_X] < 0 ? FACE_RIGHT : FACE_LEFT;
		}
		return faceIndex;
	}
	
	public Texture[] getFaces() {
		return faces;
	}
}
