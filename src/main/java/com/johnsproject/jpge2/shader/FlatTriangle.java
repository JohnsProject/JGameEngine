package com.johnsproject.jpge2.shader;

import com.johnsproject.jpge2.dto.Camera;
import com.johnsproject.jpge2.library.MathLibrary;
import com.johnsproject.jpge2.library.VectorLibrary;

public class FlatTriangle {

	protected static final byte VECTOR_X = VectorLibrary.VECTOR_X;
	protected static final byte VECTOR_Y = VectorLibrary.VECTOR_Y;
	protected static final byte VECTOR_Z = VectorLibrary.VECTOR_Z;

	protected static final int FP_BITS = MathLibrary.FP_BITS;
	
	protected static final int PERSPECTIVE_BITS = 25;
	protected static final int PERSPECTIVE_ONE = 1 << PERSPECTIVE_BITS;
	protected static final int PERSPECTIVE_HALF = PERSPECTIVE_ONE >> 1;
	
	protected final int[] vectorCache;
	protected final int[] pixelCache;
	
	protected final MathLibrary mathLibrary;
	protected final VectorLibrary vectorLibrary;
	
	protected final int[] location1;
	protected final int[] location2;
	protected final int[] location3;
	
	protected final Shader shader;
	
	public FlatTriangle(Shader shader) {
		this.shader = shader;
		this.mathLibrary = new MathLibrary();
		this.vectorLibrary = new VectorLibrary();
		this.vectorCache = vectorLibrary.generate();
		this.pixelCache = vectorLibrary.generate();
		this.location1 = vectorLibrary.generate();
		this.location2 = vectorLibrary.generate();
		this.location3 = vectorLibrary.generate();
	}
	
	public final int[] getLocation1() {
		return location1;
	}

	public final int[] getLocation2() {
		return location2;
	}

	public final int[] getLocation3() {
		return location3;
	}
	
	public void drawTriangle(int[] cameraFrustum) {
		if (location1[VECTOR_Y] > location2[VECTOR_Y]) {
			vectorLibrary.swap(location1, location2);
		}
		if (location2[VECTOR_Y] > location3[VECTOR_Y]) {
			vectorLibrary.swap(location2, location3);
		}
		if (location1[VECTOR_Y] > location2[VECTOR_Y]) {
			vectorLibrary.swap(location1, location2);
		}
        if (location2[VECTOR_Y] == location3[VECTOR_Y]) {
        	drawBottomTriangle(cameraFrustum);
        } else if (location1[VECTOR_Y] == location2[VECTOR_Y]) {
        	drawTopTriangle(cameraFrustum);
        } else {
            int x = location1[VECTOR_X];
            int dy = mathLibrary.divide(location2[VECTOR_Y] - location1[VECTOR_Y], location3[VECTOR_Y] - location1[VECTOR_Y]);
            int multiplier = location3[VECTOR_X] - location1[VECTOR_X];
            x += mathLibrary.multiply(dy, multiplier);
            int y = location2[VECTOR_Y];
            int z = location1[VECTOR_Z];
            multiplier = location3[VECTOR_Z] - location1[VECTOR_Z];
            z += mathLibrary.multiply(dy, multiplier);
            vectorCache[VECTOR_X] = x;
            vectorCache[VECTOR_Y] = y;
            vectorCache[VECTOR_Z] = z;
            vectorLibrary.swap(vectorCache, location3);
            drawBottomTriangle(cameraFrustum);
            vectorLibrary.swap(vectorCache, location3);
            vectorLibrary.swap(location1, location2);
            vectorLibrary.swap(location2, vectorCache);
            drawTopTriangle(cameraFrustum);
        }
	}
	
	private void drawBottomTriangle(int[] cameraFrustum) {
		int xShifted = location1[VECTOR_X] << FP_BITS;
		int y2y1 = location2[VECTOR_Y] - location1[VECTOR_Y];
		int y3y1 = location2[VECTOR_Y] - location1[VECTOR_Y];
		y2y1 = y2y1 == 0 ? 1 : y2y1;
		y3y1 = y3y1 == 0 ? 1 : y3y1;
        int dx1 = mathLibrary.divide(location2[VECTOR_X] - location1[VECTOR_X], y2y1);
        int dx2 = mathLibrary.divide(location3[VECTOR_X] - location1[VECTOR_X], y3y1);
        int dz1 = mathLibrary.divide(location2[VECTOR_Z] - location1[VECTOR_Z], y2y1);
        int dz2 = mathLibrary.divide(location3[VECTOR_Z] - location1[VECTOR_Z], y3y1);
        if(dx1 < dx2) {
        	int dxdx = dx2 - dx1;
        	dxdx = dxdx == 0 ? 1 : dxdx;
        	int dz = mathLibrary.divide(dz2 - dz1, dxdx);
        	int x1 = xShifted;
            int x2 = xShifted;
            int z = location1[VECTOR_Z] << FP_BITS;
	        for (int y = location1[VECTOR_Y]; y <= location2[VECTOR_Y]; y++) {
	        	drawScanline(x1, x2, y, z, dz, cameraFrustum);
	            x1 += dx1;
	            x2 += dx2;
	            z += dz1;
	        }
        } else {
        	int dxdx = dx1 - dx2;
        	dxdx = dxdx == 0 ? 1 : dxdx;
        	int dz = mathLibrary.divide(dz1 - dz2, dxdx);
        	int x1 = xShifted;
            int x2 = xShifted;
            int z = location1[VECTOR_Z] << FP_BITS;
        	for (int y = location1[VECTOR_Y]; y <= location2[VECTOR_Y]; y++) {
        		drawScanline(x1, x2, y, z, dz, cameraFrustum);
	            x1 += dx2;
	            x2 += dx1;
	            z += dz2;
	        }
        }
    }
    
	private void drawTopTriangle(int[] cameraFrustum) {
		int xShifted = location3[VECTOR_X] << FP_BITS;
		int y3y1 = location3[VECTOR_Y] - location1[VECTOR_Y];
		int y3y2 = location3[VECTOR_Y] - location2[VECTOR_Y];
		y3y1 = y3y1 == 0 ? 1 : y3y1;
		y3y2 = y3y2 == 0 ? 1 : y3y2;
		int dx1 = mathLibrary.divide(location3[VECTOR_X] - location1[VECTOR_X], y3y1);
		int dx2 = mathLibrary.divide(location3[VECTOR_X] - location2[VECTOR_X], y3y2);
		int dz1 = mathLibrary.divide(location3[VECTOR_Z] - location1[VECTOR_Z], y3y1);
		int dz2 = mathLibrary.divide(location3[VECTOR_Z] - location2[VECTOR_Z], y3y2);
		if (dx1 > dx2) {
			int dxdx = dx1 - dx2;
			dxdx = dxdx == 0 ? 1 : dxdx;
			int dz = mathLibrary.divide(dz1 - dz2, dxdx);
			int x1 = xShifted;
			int x2 = xShifted;
			int z = location3[VECTOR_Z] << FP_BITS;
	        for (int y = location3[VECTOR_Y]; y > location1[VECTOR_Y]; y--) {
	        	drawScanline(x1, x2, y, z, dz, cameraFrustum);
	            x1 -= dx1;
	            x2 -= dx2;
	            z -= dz1;
	        }
		} else {
			int dxdx = dx2 - dx1;
			dxdx = dxdx == 0 ? 1 : dxdx;
			int dz = mathLibrary.divide(dz2 - dz1, dxdx);
			int x1 = xShifted;
			int x2 = xShifted;
			int z = location3[VECTOR_Z] << FP_BITS;
	        for (int y = location3[VECTOR_Y]; y > location1[VECTOR_Y]; y--) {
	        	drawScanline(x1, x2, y, z, dz, cameraFrustum);
	            x1 -= dx2;
	            x2 -= dx1;
	            z -= dz2;
	        }
		}
    }
	
	private void drawScanline(int x1, int x2, int y, int z, int dz, int[] cameraFrustum) {
		boolean yInside = (y > cameraFrustum[Camera.FRUSTUM_TOP] + 1) & (y < cameraFrustum[Camera.FRUSTUM_BOTTOM] - 1);
		x1 >>= FP_BITS;
		x2 >>= FP_BITS;
		for (; x1 <= x2; x1++) {
			if (yInside & (x1 > cameraFrustum[Camera.FRUSTUM_LEFT] + 1) & (x1 < cameraFrustum[Camera.FRUSTUM_RIGHT] - 1)) {
				pixelCache[VECTOR_X] = x1;
				pixelCache[VECTOR_Y] = y;
				pixelCache[VECTOR_Z] = z >> FP_BITS;
				shader.fragment(pixelCache);
			}
			z += dz;
		}
	}
	
	protected int multiply(int value1, int value2) {
		long a = value1;
		long b = value2;
		long result = a * b + PERSPECTIVE_HALF;
		return (int) (result >> PERSPECTIVE_BITS);
	}
}
