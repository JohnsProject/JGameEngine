/**
 * MIT License
 *
 * Copyright (c) 2018 John Salomon - John´s Project
 *  
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.johnsproject.jpge2.shader;

import com.johnsproject.jpge2.dto.Camera;
import com.johnsproject.jpge2.library.VectorLibrary;

public class GouraudTriangle extends FlatTriangle {
	
	protected final int[] red;
	protected final int[] green;
	protected final int[] blue;
	
	public GouraudTriangle(Shader shader) {
		super(shader);
		VectorLibrary vectorLibrary = new VectorLibrary();
		red = vectorLibrary.generate();
		green = vectorLibrary.generate();
		blue = vectorLibrary.generate();
	}

	public final int[] getRed() {
		return red;
	}

	public final int[] getGreen() {
		return green;
	}

	public final int[] getBlue() {
		return blue;
	}
	
	public void drawTriangle(int[] cameraFrustum) {
		int tmp = 0;
		if (location1[VECTOR_Y] > location2[VECTOR_Y]) {
			vectorLibrary.swap(location1, location2);
			tmp = red[0]; red[0] = red[1]; red[1] = tmp;
			tmp = green[0]; green[0] = green[1]; green[1] = tmp;
			tmp = blue[0]; blue[0] = blue[1]; blue[1] = tmp;
		}
		if (location2[VECTOR_Y] > location3[VECTOR_Y]) {
			vectorLibrary.swap(location2, location3);
			tmp = red[2]; red[2] = red[1]; red[1] = tmp;
			tmp = green[2]; green[2] = green[1]; green[1] = tmp;
			tmp = blue[2]; blue[2] = blue[1]; blue[1] = tmp;
		}
		if (location1[VECTOR_Y] > location2[VECTOR_Y]) {
			vectorLibrary.swap(location1, location2);
			tmp = red[0]; red[0] = red[1]; red[1] = tmp;
			tmp = green[0]; green[0] = green[1]; green[1] = tmp;
			tmp = blue[0]; blue[0] = blue[1]; blue[1] = tmp;
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
            int r = red[0];
            multiplier = red[2] - red[0];
            r += mathLibrary.multiply(dy, multiplier);
            int g = green[0];
            multiplier = green[2] - green[0];
            g += mathLibrary.multiply(dy, multiplier);
            int b = blue[0];
            multiplier = blue[2] - blue[0];
            b += mathLibrary.multiply(dy, multiplier);
            vectorCache[VECTOR_X] = x;
            vectorCache[VECTOR_Y] = y;
            vectorCache[VECTOR_Z] = z;
            vectorLibrary.swap(vectorCache, location3);
            tmp = red[2]; red[2] = r; r = tmp;
            tmp = green[2]; green[2] = g; g = tmp;
            tmp = blue[2]; blue[2] = b; b = tmp;
            drawBottomTriangle(cameraFrustum);
            vectorLibrary.swap(vectorCache, location3);
            vectorLibrary.swap(location1, location2);
            vectorLibrary.swap(location2, vectorCache);
            tmp = red[2]; red[2] = r; r = tmp;
            tmp = red[0]; red[0] = red[1]; red[1] = tmp;
            tmp = red[1]; red[1] = r; r = tmp;
            tmp = green[2]; green[2] = g; g = tmp;
            tmp = green[0]; green[0] = green[1]; green[1] = tmp;
            tmp = green[1]; green[1] = g; g = tmp;
            tmp = blue[2]; blue[2] = b; b = tmp;
            tmp = blue[0]; blue[0] = blue[1]; blue[1] = tmp;
            tmp = blue[1]; blue[1] = b; b = tmp;
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
        int dr1 = mathLibrary.divide(red[1] - red[0], y2y1);
        int dr2 = mathLibrary.divide(red[2] - red[0], y3y1);
        int dg1 = mathLibrary.divide(green[1] - green[0], y2y1);
        int dg2 = mathLibrary.divide(green[2] - green[0], y3y1);
        int db1 = mathLibrary.divide(blue[1] - blue[0], y2y1);
        int db2 = mathLibrary.divide(blue[2] - blue[0], y3y1);
        if(dx1 < dx2) {
        	int dxdx = dx2 - dx1;
        	dxdx = dxdx == 0 ? 1 : dxdx;
        	int dz = mathLibrary.divide(dz2 - dz1, dxdx);
        	int dr = mathLibrary.divide(dr2 - dr1, dxdx);
        	int dg = mathLibrary.divide(dg2 - dg1, dxdx);
        	int db = mathLibrary.divide(db2 - db1, dxdx);
        	int x1 = xShifted;
            int x2 = xShifted;
            int z = location1[VECTOR_Z] << FP_BITS;
            int r = red[0] << FP_BITS;
            int g = green[0] << FP_BITS;
            int b = blue[0] << FP_BITS;
	        for (int y = location1[VECTOR_Y]; y <= location2[VECTOR_Y]; y++) {
	        	drawScanline(x1, x2, y, z, r, g, b, dz, dr, dg, db, cameraFrustum);
	            x1 += dx1;
	            x2 += dx2;
	            z += dz1;
	            r += dr1;
	            g += dg1;
	            b += db1;
	        }
        } else {
        	int dxdx = dx1 - dx2;
        	dxdx = dxdx == 0 ? 1 : dxdx;
        	int dz = mathLibrary.divide(dz1 - dz2, dxdx);
        	int dr = mathLibrary.divide(dr1 - dr2, dxdx);
        	int dg = mathLibrary.divide(dg1 - dg2, dxdx);
        	int db = mathLibrary.divide(db1 - db2, dxdx);
        	int x1 = xShifted;
            int x2 = xShifted;
            int z = location1[VECTOR_Z] << FP_BITS;
            int r = red[0] << FP_BITS;
            int g = green[0] << FP_BITS;
            int b = blue[0] << FP_BITS;
        	for (int y = location1[VECTOR_Y]; y <= location2[VECTOR_Y]; y++) {
        		drawScanline(x1, x2, y, z, r, g, b, dz, dr, dg, db, cameraFrustum);
	            x1 += dx2;
	            x2 += dx1;
	            z += dz2;
	            r += dr2;
	            g += dg2;
	            b += db2;
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
		int dr1 = mathLibrary.divide(red[2] - red[0], y3y1);
		int dr2 = mathLibrary.divide(red[2] - red[1], y3y2);
		int dg1 = mathLibrary.divide(green[2] - green[0], y3y1);
		int dg2 = mathLibrary.divide(green[2] - green[1], y3y2);
		int db1 = mathLibrary.divide(blue[2] - blue[0], y3y1);
		int db2 = mathLibrary.divide(blue[2] - blue[1], y3y2);
		if (dx1 > dx2) {
			int dxdx = dx1 - dx2;
			dxdx = dxdx == 0 ? 1 : dxdx;
			int dz = mathLibrary.divide(dz1 - dz2, dxdx);
			int dr = mathLibrary.divide(dr1 - dr2, dxdx);
			int dg = mathLibrary.divide(dg1 - dg2, dxdx);
			int db = mathLibrary.divide(db1 - db2, dxdx);
			int x1 = xShifted;
			int x2 = xShifted;
			int z = location3[VECTOR_Z] << FP_BITS;
			int r = red[2] << FP_BITS;
			int g = green[2] << FP_BITS;
			int b = blue[2] << FP_BITS;
	        for (int y = location3[VECTOR_Y]; y > location1[VECTOR_Y]; y--) {
	        	drawScanline(x1, x2, y, z, r, g, b, dz, dr, dg, db, cameraFrustum);
	            x1 -= dx1;
	            x2 -= dx2;
	            z -= dz1;
	            r -= dr1;
	            g -= dg1;
	            b -= db1;
	        }
		} else {
			int dxdx = dx2 - dx1;
			dxdx = dxdx == 0 ? 1 : dxdx;
			int dz = mathLibrary.divide(dz2 - dz1, dxdx);
			int dr = mathLibrary.divide(dr2 - dr1, dxdx);
			int dg = mathLibrary.divide(dg2 - dg1, dxdx);
			int db = mathLibrary.divide(db2 - db1, dxdx);
			int x1 = xShifted;
			int x2 = xShifted;
			int z = location3[VECTOR_Z] << FP_BITS;
			int r = red[2] << FP_BITS;
			int g = green[2] << FP_BITS;
			int b = blue[2] << FP_BITS;
	        for (int y = location3[VECTOR_Y]; y > location1[VECTOR_Y]; y--) {
	        	drawScanline(x1, x2, y, z, r, g, b, dz, dr, dg, db, cameraFrustum);
	            x1 -= dx2;
	            x2 -= dx1;
	            z -= dz2;
	            r -= dr2;
	            g -= dg2;
	            b -= db2;
	        }
		}
    }
	
	private void drawScanline(int x1, int x2, int y, int z, int r, int g, int b, int dz, int dr, int dg, int db, int[] cameraFrustum) {
		boolean yInside = (y > cameraFrustum[Camera.FRUSTUM_TOP] + 1) & (y < cameraFrustum[Camera.FRUSTUM_BOTTOM] - 1);
		x1 >>= FP_BITS;
		x2 >>= FP_BITS;
		for (; x1 <= x2; x1++) {
			if (yInside & (x1 > cameraFrustum[Camera.FRUSTUM_LEFT] + 1) & (x1 < cameraFrustum[Camera.FRUSTUM_RIGHT] - 1)) {
				pixelCache[VECTOR_X] = x1;
				pixelCache[VECTOR_Y] = y;
				pixelCache[VECTOR_Z] = z >> FP_BITS;
				red[3] = r >> FP_BITS;
				green[3] = g >> FP_BITS;
				blue[3] = b >> FP_BITS;
				shader.fragment(pixelCache);
			}
			z += dz;
			r += dr;
			g += dg;
			b += db;
		}
	}
}
