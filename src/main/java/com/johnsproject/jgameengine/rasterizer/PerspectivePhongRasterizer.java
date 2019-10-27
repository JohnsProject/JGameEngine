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
import com.johnsproject.jgameengine.shader.Shader;

import static com.johnsproject.jgameengine.library.VectorLibrary.*;
import static com.johnsproject.jgameengine.library.MathLibrary.*;

public class PerspectivePhongRasterizer extends AffinePhongRasterizer {
	
	public PerspectivePhongRasterizer(Shader shader) {
		super(shader);
	}
	
	/**
	 * THIS METHOD SHOULD NOT BE CALLED. 
	 * Use the triangle drawing methods in {@link GraphicsLibrary} class.
	 * 
	 * @param cameraFrustum
	 */
	public final void drawPerspectivePhongTriangle(int[] cameraFrustum) {
		divideOneByZ();
		zMultiply(u);
		zMultiply(v);
		zMultiply(worldX);
		zMultiply(worldY);
		zMultiply(worldZ);
		zMultiply(normalX);
		zMultiply(normalY);
		zMultiply(normalZ);
		if (location0[VECTOR_Y] > location1[VECTOR_Y]) {
			vectorLibrary.swap(location0, location1);
			swapVector(u, v, 0, 1);
			swapVector(worldX, worldY, worldZ, 0, 1);
			swapVector(normalX, normalY, normalZ, 0, 1);
		}
		if (location1[VECTOR_Y] > location2[VECTOR_Y]) {
			vectorLibrary.swap(location1, location2);
			swapVector(u, v, 2, 1);
			swapVector(worldX, worldY, worldZ, 2, 1);
			swapVector(normalX, normalY, normalZ, 2, 1);
		}
		if (location0[VECTOR_Y] > location1[VECTOR_Y]) {
			vectorLibrary.swap(location0, location1);
			swapVector(u, v, 0, 1);
			swapVector(worldX, worldY, worldZ, 0, 1);
			swapVector(normalX, normalY, normalZ, 0, 1);
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
            int wx = worldX[0];
            int wy = worldY[0];
            int wz = worldZ[0];
            int nx = normalX[0];
            int ny = normalY[0];
            int nz = normalZ[0];
            int dy = mathLibrary.divide(location1[VECTOR_Y] - location0[VECTOR_Y], location2[VECTOR_Y] - location0[VECTOR_Y]);
            int multiplier = location2[VECTOR_X] - location0[VECTOR_X];
            x += mathLibrary.multiply(dy, multiplier);
            multiplier = location2[VECTOR_Z] - location0[VECTOR_Z];
            z += mathLibrary.multiply(dy, multiplier);
            multiplier = u[2] - u[0];
            uvx += mathLibrary.multiply(dy, multiplier);
            multiplier = v[2] - v[0];
            uvy += mathLibrary.multiply(dy, multiplier);
            multiplier = worldX[2] - worldX[0];
            wx += mathLibrary.multiply(dy, multiplier);
            multiplier = worldY[2] - worldY[0];
            wy += mathLibrary.multiply(dy, multiplier);
            multiplier = worldZ[2] - worldZ[0];
            wz += mathLibrary.multiply(dy, multiplier);
            multiplier = normalX[2] - normalX[0];
            nx += mathLibrary.multiply(dy, multiplier);
            multiplier = normalY[2] - normalY[0];
            ny += mathLibrary.multiply(dy, multiplier);
            multiplier = normalZ[2] - normalZ[0];
            nz += mathLibrary.multiply(dy, multiplier);
            vectorCache[VECTOR_X] = x;
            vectorCache[VECTOR_Y] = y;
            vectorCache[VECTOR_Z] = z;
            uvCache[VECTOR_X] = uvx;
            uvCache[VECTOR_Y] = uvy;
            worldCache[VECTOR_X] = wx;
            worldCache[VECTOR_Y] = wy;
            worldCache[VECTOR_Z] = wz;
            normalCache[VECTOR_X] = nx;
            normalCache[VECTOR_Y] = ny;
            normalCache[VECTOR_Z] = nz;
            vectorLibrary.swap(vectorCache, location2);
            swapCache(u, v, uvCache, 2);
            swapCache(worldX, worldY, worldZ, worldCache, 2);
            swapCache(normalX, normalY, normalZ, normalCache, 2);
            drawBottomTriangle(cameraFrustum);
            vectorLibrary.swap(vectorCache, location2);
            vectorLibrary.swap(location0, location1);
            vectorLibrary.swap(location1, vectorCache);
            swapCache(u, v, uvCache, 2);
            swapVector(u, v, 0, 1);
            swapCache(u, v, uvCache, 1);
            swapCache(worldX, worldY, worldZ, worldCache, 2);
            swapVector(worldX, worldY, worldZ, 0, 1);
            swapCache(worldX, worldY, worldZ, worldCache, 1);
            swapCache(normalX, normalY, normalZ, normalCache, 2);
            swapVector(normalX, normalY, normalZ, 0, 1);
            swapCache(normalX, normalY, normalZ, normalCache, 1);
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
        int du1 = mathLibrary.divide(this.u[1] - this.u[0], y2y1);
        int du2 = mathLibrary.divide(this.u[2] - this.u[0], y3y1);
        int dv1 = mathLibrary.divide(this.v[1] - this.v[0], y2y1);
        int dv2 = mathLibrary.divide(this.v[2] - this.v[0], y3y1);
        int dwx1 = mathLibrary.divide(worldX[1] - worldX[0], y2y1);
        int dwx2 = mathLibrary.divide(worldX[2] - worldX[0], y3y1);
        int dwy1 = mathLibrary.divide(worldY[1] - worldY[0], y2y1);
        int dwy2 = mathLibrary.divide(worldY[2] - worldY[0], y3y1);
        int dwz1 = mathLibrary.divide(worldZ[1] - worldZ[0], y2y1);
        int dwz2 = mathLibrary.divide(worldZ[2] - worldZ[0], y3y1);
        int dnx1 = mathLibrary.divide(normalX[1] - normalX[0], y2y1);
        int dnx2 = mathLibrary.divide(normalX[2] - normalX[0], y3y1);
        int dny1 = mathLibrary.divide(normalY[1] - normalY[0], y2y1);
        int dny2 = mathLibrary.divide(normalY[2] - normalY[0], y3y1);
        int dnz1 = mathLibrary.divide(normalZ[1] - normalZ[0], y2y1);
        int dnz2 = mathLibrary.divide(normalZ[2] - normalZ[0], y3y1);
        if(dx1 < dx2) {
        	int dxdx = dx2 - dx1;
        	dxdx = dxdx == 0 ? 1 : dxdx;
        	int dz = mathLibrary.divide(dz2 - dz1, dxdx);
        	int du = mathLibrary.divide(du2 - du1, dxdx);
        	int dv = mathLibrary.divide(dv2 - dv1, dxdx);
        	int dwx = mathLibrary.divide(dwx2 - dwx1, dxdx);
        	int dwy = mathLibrary.divide(dwy2 - dwy1, dxdx);
        	int dwz = mathLibrary.divide(dwz2 - dwz1, dxdx);
        	int dnx = mathLibrary.divide(dnx2 - dnx1, dxdx);
        	int dny = mathLibrary.divide(dny2 - dny1, dxdx);
        	int dnz = mathLibrary.divide(dnz2 - dnz1, dxdx);
        	int x1 = xShifted;
            int x2 = xShifted;
            int z = location0[VECTOR_Z] << FP_BIT;
            int u = this.u[0] << FP_BIT;
            int v = this.v[0] << FP_BIT;
            int wx = worldX[0] << FP_BIT;
            int wy = worldY[0] << FP_BIT;
            int wz = worldZ[0] << FP_BIT;
            int nx = normalX[0] << FP_BIT;
            int ny = normalY[0] << FP_BIT;
            int nz = normalZ[0] << FP_BIT;
	        for (int y = location0[VECTOR_Y]; y <= location1[VECTOR_Y]; y++) {
	        	drawScanline(x1, x2, y, z, u, v, wx, wy, wz, nx, ny, nz, dz, du, dv, dwx, dwy, dwz, dnx, dny, dnz, cameraFrustum);
	            x1 += dx1;
	            x2 += dx2;
	            z += dz1;
	            u += du1;
	            v += dv1;
	            wx += dwx1;
	            wy += dwy1;
	            wz += dwz1;
	            nx += dnx1;
	            ny += dny1;
	            nz += dnz1;
	        }
        } else {
        	int dxdx = dx1 - dx2;
        	dxdx = dxdx == 0 ? 1 : dxdx;
        	int dz = mathLibrary.divide(dz1 - dz2, dxdx);
        	int du = mathLibrary.divide(du1 - du2, dxdx);
        	int dv = mathLibrary.divide(dv1 - dv2, dxdx);
        	int dwx = mathLibrary.divide(dwx1 - dwx2, dxdx);
        	int dwy = mathLibrary.divide(dwy1 - dwy2, dxdx);
        	int dwz = mathLibrary.divide(dwz1 - dwz2, dxdx);
        	int dnx = mathLibrary.divide(dnx1 - dnx2, dxdx);
        	int dny = mathLibrary.divide(dny1 - dny2, dxdx);
        	int dnz = mathLibrary.divide(dnz1 - dnz2, dxdx);
        	int x1 = xShifted;
            int x2 = xShifted;
            int z = location0[VECTOR_Z] << FP_BIT;
            int u = this.u[0] << FP_BIT;
            int v = this.v[0] << FP_BIT;
            int wx = worldX[0] << FP_BIT;
            int wy = worldY[0] << FP_BIT;
            int wz = worldZ[0] << FP_BIT;
            int nx = normalX[0] << FP_BIT;
            int ny = normalY[0] << FP_BIT;
            int nz = normalZ[0] << FP_BIT;
        	for (int y = location0[VECTOR_Y]; y <= location1[VECTOR_Y]; y++) {
        		drawScanline(x1, x2, y, z, u, v, wx, wy, wz, nx, ny, nz, dz, du, dv, dwx, dwy, dwz, dnx, dny, dnz, cameraFrustum);
	            x1 += dx2;
	            x2 += dx1;
	            z += dz2;
	            u += du2;
	            v += dv2;
	            wx += dwx2;
	            wy += dwy2;
	            wz += dwz2;
	            nx += dnx2;
	            ny += dny2;
	            nz += dnz2;
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
		int du1 = mathLibrary.divide(this.u[2] - this.u[0], y3y1);
		int du2 = mathLibrary.divide(this.u[2] - this.u[1], y3y2);
		int dv1 = mathLibrary.divide(this.v[2] - this.v[0], y3y1);
		int dv2 = mathLibrary.divide(this.v[2] - this.v[1], y3y2);
		int dwx1 = mathLibrary.divide(worldX[2] - worldX[0], y3y1);
		int dwx2 = mathLibrary.divide(worldX[2] - worldX[1], y3y2);
		int dwy1 = mathLibrary.divide(worldY[2] - worldY[0], y3y1);
		int dwy2 = mathLibrary.divide(worldY[2] - worldY[1], y3y2);
		int dwz1 = mathLibrary.divide(worldZ[2] - worldZ[0], y3y1);
		int dwz2 = mathLibrary.divide(worldZ[2] - worldZ[1], y3y2);
		int dnx1 = mathLibrary.divide(normalX[2] - normalX[0], y3y1);
		int dnx2 = mathLibrary.divide(normalX[2] - normalX[1], y3y2);
		int dny1 = mathLibrary.divide(normalY[2] - normalY[0], y3y1);
		int dny2 = mathLibrary.divide(normalY[2] - normalY[1], y3y2);
		int dnz1 = mathLibrary.divide(normalZ[2] - normalZ[0], y3y1);
		int dnz2 = mathLibrary.divide(normalZ[2] - normalZ[1], y3y2);
		if (dx1 > dx2) {
			int dxdx = dx1 - dx2;
			dxdx = dxdx == 0 ? 1 : dxdx;
			int dz = mathLibrary.divide(dz1 - dz2, dxdx);
			int du = mathLibrary.divide(du1 - du2, dxdx);
			int dv = mathLibrary.divide(dv1 - dv2, dxdx);
			int dwx = mathLibrary.divide(dwx1 - dwx2, dxdx);
			int dwy = mathLibrary.divide(dwy1 - dwy2, dxdx);
			int dwz = mathLibrary.divide(dwz1 - dwz2, dxdx);
			int dnx = mathLibrary.divide(dnx1 - dnx2, dxdx);
			int dny = mathLibrary.divide(dny1 - dny2, dxdx);
			int dnz = mathLibrary.divide(dnz1 - dnz2, dxdx);
			int x1 = xShifted;
			int x2 = xShifted;
			int z = location2[VECTOR_Z] << FP_BIT;
			int u = this.u[2] << FP_BIT;
			int v = this.v[2] << FP_BIT;
			int wx = worldX[2] << FP_BIT;
            int wy = worldY[2] << FP_BIT;
            int wz = worldZ[2] << FP_BIT;
            int nx = normalX[2] << FP_BIT;
            int ny = normalY[2] << FP_BIT;
            int nz = normalZ[2] << FP_BIT;
	        for (int y = location2[VECTOR_Y]; y > location0[VECTOR_Y]; y--) {
	        	drawScanline(x1, x2, y, z, u, v, wx, wy, wz, nx, ny, nz, dz, du, dv, dwx, dwy, dwz, dnx, dny, dnz, cameraFrustum);
	            x1 -= dx1;
	            x2 -= dx2;
	            z -= dz1;
	            u -= du1;
	            v -= dv1;
	            wx -= dwx1;
	            wy -= dwy1;
	            wz -= dwz1;
	            nx -= dnx1;
	            ny -= dny1;
	            nz -= dnz1;
	        }
		} else {
			int dxdx = dx2 - dx1;
			dxdx = dxdx == 0 ? 1 : dxdx;
			int dz = mathLibrary.divide(dz2 - dz1, dxdx);
			int du = mathLibrary.divide(du2 - du1, dxdx);
			int dv = mathLibrary.divide(dv2 - dv1, dxdx);
			int dwx = mathLibrary.divide(dwx2 - dwx1, dxdx);
			int dwy = mathLibrary.divide(dwy2 - dwy1, dxdx);
			int dwz = mathLibrary.divide(dwz2 - dwz1, dxdx);
			int dnx = mathLibrary.divide(dnx2 - dnx1, dxdx);
			int dny = mathLibrary.divide(dny2 - dny1, dxdx);
			int dnz = mathLibrary.divide(dnz2 - dnz1, dxdx);
			int x1 = xShifted;
			int x2 = xShifted;
			int z = location2[VECTOR_Z] << FP_BIT;
			int u = this.u[2] << FP_BIT;
			int v = this.v[2] << FP_BIT;
			int wx = worldX[2] << FP_BIT;
            int wy = worldY[2] << FP_BIT;
            int wz = worldZ[2] << FP_BIT;
            int nx = normalX[2] << FP_BIT;
            int ny = normalY[2] << FP_BIT;
            int nz = normalZ[2] << FP_BIT;
	        for (int y = location2[VECTOR_Y]; y > location0[VECTOR_Y]; y--) {
	        	drawScanline(x1, x2, y, z, u, v, wx, wy, wz, nx, ny, nz, dz, du, dv, dwx, dwy, dwz, dnx, dny, dnz, cameraFrustum);
	            x1 -= dx2;
	            x2 -= dx1;
	            z -= dz2;
	            u -= du2;
	            v -= dv2;
	            wx -= dwx2;
	            wy -= dwy2;
	            wz -= dwz2;
	            nx -= dnx2;
	            ny -= dny2;
	            nz -= dnz2;
	        }
		}
    }
	
	private static final int DIVISION_ONE = FP_ONE << FP_BIT;
	private static final int INTERPOLATE_BIT_2 = INTERPOLATE_BIT * 2;
	private void drawScanline(int x1, int x2, int y, int z, int u, int v, int wx, int wy, int wz, int nx, int ny, int nz,
							int dz, int du, int dv, int dwx, int dwy, int dwz, int dnx, int dny, int dnz, int[] cameraFrustum) {
		x1 >>= FP_BIT;
		x2 >>= FP_BIT;
        int oneByZ;
		for (; x1 <= x2; x1++) {
			pixelCache[VECTOR_X] = x1;
			pixelCache[VECTOR_Y] = y;
			oneByZ = DIVISION_ONE / (z >> INTERPOLATE_BIT);
			pixelCache[VECTOR_Z] = oneByZ;
			this.u[3] = mathLibrary.multiply(u, oneByZ) >> INTERPOLATE_BIT_2;
			this.v[3] = mathLibrary.multiply(v, oneByZ) >> INTERPOLATE_BIT_2;
			worldX[3] = mathLibrary.multiply(wx, oneByZ) >> INTERPOLATE_BIT;
			worldY[3] = mathLibrary.multiply(wy, oneByZ) >> INTERPOLATE_BIT;
			worldZ[3] = mathLibrary.multiply(wz, oneByZ) >> INTERPOLATE_BIT;
			normalX[3] = mathLibrary.multiply(nx, oneByZ) >> INTERPOLATE_BIT;
			normalY[3] = mathLibrary.multiply(ny, oneByZ) >> INTERPOLATE_BIT;
			normalZ[3] = mathLibrary.multiply(nz, oneByZ) >> INTERPOLATE_BIT;
			shader.fragment(pixelCache);
			z += dz;
			u += du;
			v += dv;
			wx += dwx;
			wy += dwy;
			wz += dwz;
			nx += dnx;
			ny += dny;
			nz += dnz;
		}
	}
}
