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

public class PerspectiveFlatTriangle extends AffineFlatTriangle {
	
	public PerspectiveFlatTriangle(Shader shader) {
		super(shader);
	}
	
	public void drawTriangle(int[] cameraFrustum) {
		location1[VECTOR_Z] = PERSPECTIVE_ONE / location1[VECTOR_Z];
		location2[VECTOR_Z] = PERSPECTIVE_ONE / location2[VECTOR_Z];
		location3[VECTOR_Z] = PERSPECTIVE_ONE / location3[VECTOR_Z];
		this.u[0] = mathLibrary.multiply(this.u[0], location1[VECTOR_Z]);
		this.u[1] = mathLibrary.multiply(this.u[1], location2[VECTOR_Z]);
		this.u[2] = mathLibrary.multiply(this.u[2], location3[VECTOR_Z]);
		this.v[0] = mathLibrary.multiply(this.v[0], location1[VECTOR_Z]);
		this.v[1] = mathLibrary.multiply(this.v[1], location2[VECTOR_Z]);
		this.v[2] = mathLibrary.multiply(this.v[2], location3[VECTOR_Z]);
		int tmp = 0;
		if (location1[VECTOR_Y] > location2[VECTOR_Y]) {
			vectorLibrary.swap(location1, location2);
			tmp = this.u[0]; this.u[0] = this.u[1]; this.u[1] = tmp;
			tmp = this.v[0]; this.v[0] = this.v[1]; this.v[1] = tmp;
		}
		if (location2[VECTOR_Y] > location3[VECTOR_Y]) {
			vectorLibrary.swap(location2, location3);
			tmp = this.u[2]; this.u[2] = this.u[1]; this.u[1] = tmp;
			tmp = this.v[2]; this.v[2] = this.v[1]; this.v[1] = tmp;
		}
		if (location1[VECTOR_Y] > location2[VECTOR_Y]) {
			vectorLibrary.swap(location1, location2);
			tmp = this.u[0]; this.u[0] = this.u[1]; this.u[1] = tmp;
			tmp = this.v[0]; this.v[0] = this.v[1]; this.v[1] = tmp;
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
            int uvx = this.u[0];
            multiplier = this.u[2] - this.u[0];
            uvx += mathLibrary.multiply(dy, multiplier);
            int uvy = this.v[0];
            multiplier = this.v[2] - this.v[0];
            uvy += mathLibrary.multiply(dy, multiplier);
            vectorCache[VECTOR_X] = x;
            vectorCache[VECTOR_Y] = y;
            vectorCache[VECTOR_Z] = z;
            vectorLibrary.swap(vectorCache, location3);
            tmp = this.u[2]; this.u[2] = uvx; uvx = tmp;
            tmp = this.v[2]; this.v[2] = uvy; uvy = tmp;
            drawBottomTriangle(cameraFrustum);
            vectorLibrary.swap(vectorCache, location3);
            vectorLibrary.swap(location1, location2);
            vectorLibrary.swap(location2, vectorCache);
            tmp = this.u[2]; this.u[2] = uvx; uvx = tmp;
            tmp = this.u[0]; this.u[0] = this.u[1]; this.u[1] = tmp;
            tmp = this.u[1]; this.u[1] = uvx; uvx = tmp;
            tmp = this.v[2]; this.v[2] = uvy; uvy = tmp;
            tmp = this.v[0]; this.v[0] = this.v[1]; this.v[1] = tmp;
            tmp = this.v[1]; this.v[1] = uvy; uvy = tmp;
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
        int du1 = mathLibrary.divide(this.u[1] - this.u[0], y2y1);
        int du2 = mathLibrary.divide(this.u[2] - this.u[0], y3y1);
        int dv1 = mathLibrary.divide(this.v[1] - this.v[0], y2y1);
        int dv2 = mathLibrary.divide(this.v[2] - this.v[0], y3y1);
        if(dx1 < dx2) {
        	int dxdx = dx2 - dx1;
        	dxdx = dxdx == 0 ? 1 : dxdx;
        	int dz = mathLibrary.divide(dz2 - dz1, dxdx);
        	int du = mathLibrary.divide(du2 - du1, dxdx);
        	int dv = mathLibrary.divide(dv2 - dv1, dxdx);
        	int x1 = xShifted;
            int x2 = xShifted;
            int z = location1[VECTOR_Z] << FP_BITS;
            int u = this.u[0] << FP_BITS;
            int v = this.v[0] << FP_BITS;
	        for (int y = location1[VECTOR_Y]; y <= location2[VECTOR_Y]; y++) {
	        	drawScanline(x1, x2, y, z, u, v, dz, du, dv, cameraFrustum);
	            x1 += dx1;
	            x2 += dx2;
	            z += dz1;
	            u += du1;
	            v += dv1;
	        }
        } else {
        	int dxdx = dx1 - dx2;
        	dxdx = dxdx == 0 ? 1 : dxdx;
        	int dz = mathLibrary.divide(dz1 - dz2, dxdx);
        	int du = mathLibrary.divide(du1 - du2, dxdx);
        	int dv = mathLibrary.divide(dv1 - dv2, dxdx);
        	int x1 = xShifted;
            int x2 = xShifted;
            int z = location1[VECTOR_Z] << FP_BITS;
            int u = this.u[0] << FP_BITS;
            int v = this.v[0] << FP_BITS;
        	for (int y = location1[VECTOR_Y]; y <= location2[VECTOR_Y]; y++) {
        		drawScanline(x1, x2, y, z, u, v, dz, du, dv, cameraFrustum);
	            x1 += dx2;
	            x2 += dx1;
	            z += dz2;
	            u += du2;
	            v += dv2;
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
		int du1 = mathLibrary.divide(this.u[2] - this.u[0], y3y1);
		int du2 = mathLibrary.divide(this.u[2] - this.u[1], y3y2);
		int dv1 = mathLibrary.divide(this.v[2] - this.v[0], y3y1);
		int dv2 = mathLibrary.divide(this.v[2] - this.v[1], y3y2);
		if (dx1 > dx2) {
			int dxdx = dx1 - dx2;
			dxdx = dxdx == 0 ? 1 : dxdx;
			int dz = mathLibrary.divide(dz1 - dz2, dxdx);
			int du = mathLibrary.divide(du1 - du2, dxdx);
			int dv = mathLibrary.divide(dv1 - dv2, dxdx);
			int x1 = xShifted;
			int x2 = xShifted;
			int z = location3[VECTOR_Z] << FP_BITS;
			int u = this.u[2] << FP_BITS;
			int v = this.v[2] << FP_BITS;
	        for (int y = location3[VECTOR_Y]; y > location1[VECTOR_Y]; y--) {
	        	drawScanline(x1, x2, y, z, u, v, dz, du, dv, cameraFrustum);
	            x1 -= dx1;
	            x2 -= dx2;
	            z -= dz1;
	            u -= du1;
	            v -= dv1;
	        }
		} else {
			int dxdx = dx2 - dx1;
			dxdx = dxdx == 0 ? 1 : dxdx;
			int dz = mathLibrary.divide(dz2 - dz1, dxdx);
			int du = mathLibrary.divide(du2 - du1, dxdx);
			int dv = mathLibrary.divide(dv2 - dv1, dxdx);
			int x1 = xShifted;
			int x2 = xShifted;
			int z = location3[VECTOR_Z] << FP_BITS;
			int u = this.u[2] << FP_BITS;
			int v = this.v[2] << FP_BITS;
	        for (int y = location3[VECTOR_Y]; y > location1[VECTOR_Y]; y--) {
	        	drawScanline(x1, x2, y, z, u, v, dz, du, dv, cameraFrustum);
	            x1 -= dx2;
	            x2 -= dx1;
	            z -= dz2;
	            u -= du2;
	            v -= dv2;
	        }
		}
    }
	
	private void drawScanline(int x1, int x2, int y, int z, int u, int v, int dz, int du, int dv, int[] cameraFrustum) {
		boolean yInside = (y > cameraFrustum[Camera.FRUSTUM_TOP] + 1) & (y < cameraFrustum[Camera.FRUSTUM_BOTTOM] - 1);
		x1 >>= FP_BITS;
		x2 >>= FP_BITS;
		for (; x1 <= x2; x1++) {
			if (yInside & (x1 > cameraFrustum[Camera.FRUSTUM_LEFT] + 1) & (x1 < cameraFrustum[Camera.FRUSTUM_RIGHT] - 1)) {
				pixelCache[VECTOR_X] = x1;
				pixelCache[VECTOR_Y] = y;
				pixelCache[VECTOR_Z] = z >> FP_BITS;
				pixelCache[VECTOR_Z] = PERSPECTIVE_ONE / pixelCache[VECTOR_Z];
				this.u[3] = multiply(u, pixelCache[VECTOR_Z]);
				this.v[3] = multiply(v, pixelCache[VECTOR_Z]);
				shader.fragment(pixelCache);
			}
			z += dz;
			u += du;
			v += dv;
		}
	}
}