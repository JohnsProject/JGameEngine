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
package com.johnsproject.jgameengine.rasterizer;

import com.johnsproject.jgameengine.library.GraphicsLibrary;
import com.johnsproject.jgameengine.library.MathLibrary;
import com.johnsproject.jgameengine.library.VectorLibrary;
import com.johnsproject.jgameengine.shader.Shader;

import static com.johnsproject.jgameengine.library.MathLibrary.*;
import static com.johnsproject.jgameengine.library.VectorLibrary.*;

public class FlatRasterizer {
	
	public static final byte INTERPOLATE_BIT = 5;
	public static final byte INTERPOLATE_ONE = 1 << INTERPOLATE_BIT;
	public static final byte FP_PLUS_INTERPOLATE_BIT = FP_BIT + INTERPOLATE_BIT;
	
	protected final int[] vectorCache;
	protected final int[] pixelCache;
	
	protected final MathLibrary mathLibrary;
	protected final VectorLibrary vectorLibrary;
	
	protected final int[] location0;
	protected final int[] location1;
	protected final int[] location2;
	
	protected final Shader shader;
	
	public FlatRasterizer(Shader shader) {
		this.shader = shader;
		this.mathLibrary = new MathLibrary();
		this.vectorLibrary = new VectorLibrary();
		this.vectorCache = VectorLibrary.generate();
		this.pixelCache = VectorLibrary.generate();
		this.location0 = VectorLibrary.generate();
		this.location1 = VectorLibrary.generate();
		this.location2 = VectorLibrary.generate();
	}
	
	public final void setLocation0(int[] location) {
		vectorLibrary.copy(location0, location);
	}
	
	public final void setLocation1(int[] location) {
		vectorLibrary.copy(location1, location);
	}
	
	public final void setLocation2(int[] location) {
		vectorLibrary.copy(location2, location);
	}
	
	public final int[] getLocation0() {
		return location0;
	}

	public final int[] getLocation1() {
		return location1;
	}

	public final int[] getLocation2() {
		return location2;
	}
	
	/**
	 * THIS METHOD SHOULD NOT BE CALLED. 
	 * Use the triangle drawing methods in {@link GraphicsLibrary} class.
	 * 
	 * @param cameraFrustum
	 */
	public final void drawFlatTriangle(int[] cameraFrustum) {
		if (location0[VECTOR_Y] > location1[VECTOR_Y]) {
			vectorLibrary.swap(location0, location1);
		}
		if (location1[VECTOR_Y] > location2[VECTOR_Y]) {
			vectorLibrary.swap(location1, location2);
		}
		if (location0[VECTOR_Y] > location1[VECTOR_Y]) {
			vectorLibrary.swap(location0, location1);
		}
        if (location1[VECTOR_Y] == location2[VECTOR_Y]) {
        	drawBottomTriangle(cameraFrustum);
        } else if (location0[VECTOR_Y] == location1[VECTOR_Y]) {
        	drawTopTriangle(cameraFrustum);
        } else {
            int x = location0[VECTOR_X];
            int y = location1[VECTOR_Y];
            int z = location0[VECTOR_Z];
            int dy = mathLibrary.divide(location1[VECTOR_Y] - location0[VECTOR_Y], location2[VECTOR_Y] - location0[VECTOR_Y]);
            int multiplier = location2[VECTOR_X] - location0[VECTOR_X];
            x += mathLibrary.multiply(dy, multiplier);
            multiplier = location2[VECTOR_Z] - location0[VECTOR_Z];
            z += mathLibrary.multiply(dy, multiplier);
            vectorCache[VECTOR_X] = x;
            vectorCache[VECTOR_Y] = y;
            vectorCache[VECTOR_Z] = z;
            vectorLibrary.swap(vectorCache, location2);
            drawBottomTriangle(cameraFrustum);
            vectorLibrary.swap(vectorCache, location2);
            vectorLibrary.swap(location0, location1);
            vectorLibrary.swap(location1, vectorCache);
            drawTopTriangle(cameraFrustum);
        }
	}
	
	private void drawBottomTriangle(int[] cameraFrustum) {
		int xShifted = location0[VECTOR_X] << FP_BIT;
		int y2y1 = location1[VECTOR_Y] - location0[VECTOR_Y];
		int y3y1 = location1[VECTOR_Y] - location0[VECTOR_Y];
		y2y1 = y2y1 == 0 ? 1 : y2y1;
		y3y1 = y3y1 == 0 ? 1 : y3y1;
        int dx1 = mathLibrary.divide(location1[VECTOR_X] - location0[VECTOR_X], y2y1);
        int dx2 = mathLibrary.divide(location2[VECTOR_X] - location0[VECTOR_X], y3y1);
        int dz1 = mathLibrary.divide(location1[VECTOR_Z] - location0[VECTOR_Z], y2y1);
        int dz2 = mathLibrary.divide(location2[VECTOR_Z] - location0[VECTOR_Z], y3y1);
        if(dx1 < dx2) {
        	int dxdx = dx2 - dx1;
        	dxdx = dxdx == 0 ? 1 : dxdx;
        	int dz = mathLibrary.divide(dz2 - dz1, dxdx);
        	int x1 = xShifted;
            int x2 = xShifted;
            int z = location0[VECTOR_Z] << FP_BIT;
	        for (int y = location0[VECTOR_Y]; y <= location1[VECTOR_Y]; y++) {
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
            int z = location0[VECTOR_Z] << FP_BIT;
        	for (int y = location0[VECTOR_Y]; y <= location1[VECTOR_Y]; y++) {
        		drawScanline(x1, x2, y, z, dz, cameraFrustum);
	            x1 += dx2;
	            x2 += dx1;
	            z += dz2;
	        }
        }
    }
    
	private void drawTopTriangle(int[] cameraFrustum) {
		int xShifted = location2[VECTOR_X] << FP_BIT;
		int y3y1 = location2[VECTOR_Y] - location0[VECTOR_Y];
		int y3y2 = location2[VECTOR_Y] - location1[VECTOR_Y];
		y3y1 = y3y1 == 0 ? 1 : y3y1;
		y3y2 = y3y2 == 0 ? 1 : y3y2;
		int dx1 = mathLibrary.divide(location2[VECTOR_X] - location0[VECTOR_X], y3y1);
		int dx2 = mathLibrary.divide(location2[VECTOR_X] - location1[VECTOR_X], y3y2);
		int dz1 = mathLibrary.divide(location2[VECTOR_Z] - location0[VECTOR_Z], y3y1);
		int dz2 = mathLibrary.divide(location2[VECTOR_Z] - location1[VECTOR_Z], y3y2);
		if (dx1 > dx2) {
			int dxdx = dx1 - dx2;
			dxdx = dxdx == 0 ? 1 : dxdx;
			int dz = mathLibrary.divide(dz1 - dz2, dxdx);
			int x1 = xShifted;
			int x2 = xShifted;
			int z = location2[VECTOR_Z] << FP_BIT;
	        for (int y = location2[VECTOR_Y]; y > location0[VECTOR_Y]; y--) {
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
			int z = location2[VECTOR_Z] << FP_BIT;
	        for (int y = location2[VECTOR_Y]; y > location0[VECTOR_Y]; y--) {
	        	drawScanline(x1, x2, y, z, dz, cameraFrustum);
	            x1 -= dx2;
	            x2 -= dx1;
	            z -= dz2;
	        }
		}
    }
	
	private void drawScanline(int x1, int x2, int y, int z, int dz, int[] cameraFrustum) {
		x1 >>= FP_BIT;
		x2 >>= FP_BIT;
		for (; x1 <= x2; x1++) {
			pixelCache[VECTOR_X] = x1;
			pixelCache[VECTOR_Y] = y;
			pixelCache[VECTOR_Z] = z >> FP_BIT;
			shader.fragment(pixelCache);
			z += dz;
		}
	}
	
	protected void divideOneByZ() {
		location0[VECTOR_Z] = mathLibrary.divide(INTERPOLATE_ONE, location0[VECTOR_Z]);
		location1[VECTOR_Z] = mathLibrary.divide(INTERPOLATE_ONE, location1[VECTOR_Z]);
		location2[VECTOR_Z] = mathLibrary.divide(INTERPOLATE_ONE, location2[VECTOR_Z]);
	}
	
	protected void zMultiply(int[] vector) {
		vector[0] = mathLibrary.multiply(vector[0], location0[VECTOR_Z]);
		vector[1] = mathLibrary.multiply(vector[1], location1[VECTOR_Z]);
		vector[2] = mathLibrary.multiply(vector[2], location2[VECTOR_Z]);
	}
	
	protected void swapVector(int[] vector1, int[] vector2, int currentIndex, int indexToSet) {
		int tmp = 0;
		tmp = vector1[currentIndex]; vector1[currentIndex] = vector1[indexToSet]; vector1[indexToSet] = tmp;
		tmp = vector2[currentIndex]; vector2[currentIndex] = vector2[indexToSet]; vector2[indexToSet] = tmp;
	}
	
	protected void swapVector(int[] vector1, int[] vector2, int[] vector3, int currentIndex, int indexToSet) {
		int tmp = 0;
		tmp = vector1[currentIndex]; vector1[currentIndex] = vector1[indexToSet]; vector1[indexToSet] = tmp;
		tmp = vector2[currentIndex]; vector2[currentIndex] = vector2[indexToSet]; vector2[indexToSet] = tmp;
		tmp = vector3[currentIndex]; vector3[currentIndex] = vector3[indexToSet]; vector3[indexToSet] = tmp;
	}
	
	protected void swapCache(int[] vector1, int[] vector2, int[] vector3, int[] cache, int indexToSet) {
		int tmp = 0;
		tmp = cache[0]; cache[0] = vector1[indexToSet]; vector1[indexToSet] = tmp;
		tmp = cache[1]; cache[1] = vector2[indexToSet]; vector2[indexToSet] = tmp;
		tmp = cache[2]; cache[2] = vector3[indexToSet]; vector3[indexToSet] = tmp;
	}
	
	protected void swapCache(int[] vector1, int[] vector2, int[] cache, int indexToSet) {
		int tmp = 0;
		tmp = cache[0]; cache[0] = vector1[indexToSet]; vector1[indexToSet] = tmp;
		tmp = cache[1]; cache[1] = vector2[indexToSet]; vector2[indexToSet] = tmp;
	}
}
