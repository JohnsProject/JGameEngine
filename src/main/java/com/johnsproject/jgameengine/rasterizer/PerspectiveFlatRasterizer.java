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

import com.johnsproject.jgameengine.model.Texture;
import com.johnsproject.jgameengine.shader.GeometryBuffer;
import com.johnsproject.jgameengine.shader.Shader;

import static com.johnsproject.jgameengine.library.VectorLibrary.*;
import static com.johnsproject.jgameengine.library.MathLibrary.*;

public class PerspectiveFlatRasterizer extends AffineFlatRasterizer {
	
	public PerspectiveFlatRasterizer(Shader shader) {
		super(shader);
	}
	
	/**
	 * This method tells the rasterizer to draw the given {@link GeometryBuffer geometryBuffer}.
	 * This rasterizer draws a triangle using the x, y coordinates of each vertex of the geometryBuffer. 
	 * It uses perspective interpolation to find out the z and the uv coordinate for each pixel.
	 * While rasterizing the geometryBuffer, for each pixel/fragment the {@link Shader#fragment} 
	 * method of this rasterizer's {@link Shader} will be called.
	 * 
	 * @param geometryBuffer
	 */
	public void perspectiveDraw(GeometryBuffer geometryBuffer, Texture texture) {
		copyFrustum(this.cameraFrustum, shader.getShaderBuffer().getPortedFrustum());
		vectorLibrary.copy(location0, geometryBuffer.getVertexBuffer(0).getLocation());
		vectorLibrary.copy(location1, geometryBuffer.getVertexBuffer(1).getLocation());
		vectorLibrary.copy(location2, geometryBuffer.getVertexBuffer(2).getLocation());
		if(cull()) {
			return;
		}
		setUV0(geometryBuffer.getUV(0), texture);
		setUV1(geometryBuffer.getUV(1), texture);
		setUV2(geometryBuffer.getUV(2), texture);
		divideOneByZ();
		zMultiply(u);
		zMultiply(v);
		if (location0[VECTOR_Y] > location1[VECTOR_Y]) {
			vectorLibrary.swap(location0, location1);
			swapVector(u, v, 0, 1);
		}
		if (location1[VECTOR_Y] > location2[VECTOR_Y]) {
			vectorLibrary.swap(location1, location2);
			swapVector(u, v, 2, 1);
		}
		if (location0[VECTOR_Y] > location1[VECTOR_Y]) {
			vectorLibrary.swap(location0, location1);
			swapVector(u, v, 0, 1);
		}
        if (location1[VECTOR_Y] == location2[VECTOR_Y]) {
        	drawBottomTriangle(cameraFrustum);
        } else if (location0[VECTOR_Y] == location1[VECTOR_Y]) {
            drawTopTriangle(cameraFrustum);
        } else {
        	 int x = location0[VECTOR_X];
             int y = location1[VECTOR_Y];
             int z = location0[VECTOR_Z];
             int uvx = u[0];
             int uvy = v[0];
             int dy = mathLibrary.divide(location1[VECTOR_Y] - location0[VECTOR_Y], location2[VECTOR_Y] - location0[VECTOR_Y]);
             int multiplier = location2[VECTOR_X] - location0[VECTOR_X];
             x += mathLibrary.multiply(dy, multiplier);
             multiplier = location2[VECTOR_Z] - location0[VECTOR_Z];
             z += mathLibrary.multiply(dy, multiplier);
             multiplier = u[2] - u[0];
             uvx += mathLibrary.multiply(dy, multiplier);
             multiplier = v[2] - v[0];
             uvy += mathLibrary.multiply(dy, multiplier);
             vectorCache[VECTOR_X] = x;
             vectorCache[VECTOR_Y] = y;
             vectorCache[VECTOR_Z] = z;
             uvCache[VECTOR_X] = uvx;
             uvCache[VECTOR_Y] = uvy;
             vectorLibrary.swap(vectorCache, location2);
             swapCache(u, v, uvCache, 2);
             drawBottomTriangle(cameraFrustum);
             vectorLibrary.swap(vectorCache, location2);
             vectorLibrary.swap(location0, location1);
             vectorLibrary.swap(location1, vectorCache);
             swapCache(u, v, uvCache, 2);
             swapVector(u, v, 0, 1);
             swapCache(u, v, uvCache, 1);
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
        int du1 = mathLibrary.divide(u[1] - u[0], y2y1);
        int du2 = mathLibrary.divide(u[2] - u[0], y3y1);
        int dv1 = mathLibrary.divide(v[1] - v[0], y2y1);
        int dv2 = mathLibrary.divide(v[2] - v[0], y3y1);
        if(dx1 < dx2) {
        	int dxdx = dx2 - dx1;
        	dxdx = dxdx == 0 ? 1 : dxdx;
        	int dz = mathLibrary.divide(dz2 - dz1, dxdx);
        	int du = mathLibrary.divide(du2 - du1, dxdx);
        	int dv = mathLibrary.divide(dv2 - dv1, dxdx);
        	int x1 = xShifted;
            int x2 = xShifted;
            int z = location0[VECTOR_Z] << FP_BIT;
            int u = this.u[0] << FP_BIT;
            int v = this.v[0] << FP_BIT;
	        for (int y = location0[VECTOR_Y]; y <= location1[VECTOR_Y]; y++) {
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
            int z = location0[VECTOR_Z] << FP_BIT;
            int u = this.u[0] << FP_BIT;
            int v = this.v[0] << FP_BIT;
        	for (int y = location0[VECTOR_Y]; y <= location1[VECTOR_Y]; y++) {
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
		int xShifted = location2[VECTOR_X] << FP_BIT;
		int y3y1 = location2[VECTOR_Y] - location0[VECTOR_Y];
		int y3y2 = location2[VECTOR_Y] - location1[VECTOR_Y];
		y3y1 = y3y1 == 0 ? 1 : y3y1;
		y3y2 = y3y2 == 0 ? 1 : y3y2;
		int dx1 = mathLibrary.divide(location2[VECTOR_X] - location0[VECTOR_X], y3y1);
		int dx2 = mathLibrary.divide(location2[VECTOR_X] - location1[VECTOR_X], y3y2);
		int dz1 = mathLibrary.divide(location2[VECTOR_Z] - location0[VECTOR_Z], y3y1);
		int dz2 = mathLibrary.divide(location2[VECTOR_Z] - location1[VECTOR_Z], y3y2);
		int du1 = mathLibrary.divide(u[2] - u[0], y3y1);
		int du2 = mathLibrary.divide(u[2] - u[1], y3y2);
		int dv1 = mathLibrary.divide(v[2] - v[0], y3y1);
		int dv2 = mathLibrary.divide(v[2] - v[1], y3y2);
		if (dx1 > dx2) {
			int dxdx = dx1 - dx2;
			dxdx = dxdx == 0 ? 1 : dxdx;
			int dz = mathLibrary.divide(dz1 - dz2, dxdx);
			int du = mathLibrary.divide(du1 - du2, dxdx);
			int dv = mathLibrary.divide(dv1 - dv2, dxdx);
			int x1 = xShifted;
			int x2 = xShifted;
			int z = location2[VECTOR_Z] << FP_BIT;
			int u = this.u[2] << FP_BIT;
			int v = this.v[2] << FP_BIT;
	        for (int y = location2[VECTOR_Y]; y > location0[VECTOR_Y]; y--) {
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
			int z = location2[VECTOR_Z] << FP_BIT;
			int u = this.u[2] << FP_BIT;
			int v = this.v[2] << FP_BIT;
	        for (int y = location2[VECTOR_Y]; y > location0[VECTOR_Y]; y--) {
	        	drawScanline(x1, x2, y, z, u, v, dz, du, dv, cameraFrustum);
	            x1 -= dx2;
	            x2 -= dx1;
	            z -= dz2;
	            u -= du2;
	            v -= dv2;
	        }
		}
    }
	
	private static final int DIVISION_ONE = FP_ONE << FP_BIT;
	private static final int INTERPOLATE_BIT_2 = INTERPOLATE_BIT * 2;
	private void drawScanline(int x1, int x2, int y, int z, int u, int v, int dz, int du, int dv, int[] cameraFrustum) {
		x1 >>= FP_BIT;
		x2 >>= FP_BIT;
		int oneByZ;
		for (; x1 <= x2; x1++) {
			fragmentBuffer.getLocation()[VECTOR_X] = x1;
			fragmentBuffer.getLocation()[VECTOR_Y] = y;
			oneByZ = DIVISION_ONE / (z >> INTERPOLATE_BIT);
			fragmentBuffer.getLocation()[VECTOR_Z] = oneByZ;
			fragmentBuffer.getUV()[VECTOR_X] = mathLibrary.multiply(u, oneByZ) >> INTERPOLATE_BIT_2;
			fragmentBuffer.getUV()[VECTOR_Y] = mathLibrary.multiply(v, oneByZ) >> INTERPOLATE_BIT_2;
			shader.fragment(fragmentBuffer);
			z += dz;
			u += du;
			v += dv;
		}
	}
}
