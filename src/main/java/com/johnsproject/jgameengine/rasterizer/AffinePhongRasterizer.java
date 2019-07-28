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
import com.johnsproject.jgameengine.library.VectorLibrary;
import com.johnsproject.jgameengine.model.Texture;
import com.johnsproject.jgameengine.shader.Shader;

public class AffinePhongRasterizer extends PhongRasterizer {
	
	protected final int[] u;
	protected final int[] v;
	protected final int[] uv;
	
	public AffinePhongRasterizer(Shader shader) {
		super(shader);
		u = VectorLibrary.generate();
		v = VectorLibrary.generate();
		uv = VectorLibrary.generate();
	}

	public final void setUV0(int[] uv, Texture texture) {
		u[0] = mathLibrary.multiply(uv[VECTOR_X], texture.getWidth());
		v[0] = mathLibrary.multiply(uv[VECTOR_Y], texture.getHeight());
	}
	
	public final void setUV1(int[] uv, Texture texture) {
		u[1] = mathLibrary.multiply(uv[VECTOR_X], texture.getWidth());
		v[1] = mathLibrary.multiply(uv[VECTOR_Y], texture.getHeight());
	}
	
	public final void setUV2(int[] uv, Texture texture) {
		u[2] = mathLibrary.multiply(uv[VECTOR_X], texture.getWidth());
		v[2] = mathLibrary.multiply(uv[VECTOR_Y], texture.getHeight());
	}
	
	public final int[] getUV() {
		uv[VECTOR_X] = u[3];
		uv[VECTOR_Y] = v[3];
		return uv;
	}
	
	/**
	 * THIS METHOD SHOULD NOT BE CALLED. 
	 * Use the triangle drawing methods in {@link GraphicsLibrary} class.
	 * 
	 * @param cameraFrustum
	 */
	public final void drawAffinePhongTriangle(int[] cameraFrustum) {
		int tmp = 0;
		if (location0[VECTOR_Y] > location1[VECTOR_Y]) {
			vectorLibrary.swap(location0, location1);
			tmp = this.u[0]; this.u[0] = this.u[1]; this.u[1] = tmp;
			tmp = this.v[0]; this.v[0] = this.v[1]; this.v[1] = tmp;
			tmp = worldX[0]; worldX[0] = worldX[1]; worldX[1] = tmp;
			tmp = worldY[0]; worldY[0] = worldY[1]; worldY[1] = tmp;
			tmp = worldZ[0]; worldZ[0] = worldZ[1]; worldZ[1] = tmp;
			tmp = normalX[0]; normalX[0] = normalX[1]; normalX[1] = tmp;
			tmp = normalY[0]; normalY[0] = normalY[1]; normalY[1] = tmp;
			tmp = normalZ[0]; normalZ[0] = normalZ[1]; normalZ[1] = tmp;
		}
		if (location1[VECTOR_Y] > location2[VECTOR_Y]) {
			vectorLibrary.swap(location1, location2);
			tmp = this.u[2]; this.u[2] = this.u[1]; this.u[1] = tmp;
			tmp = this.v[2]; this.v[2] = this.v[1]; this.v[1] = tmp;
			tmp = worldX[2]; worldX[2] = worldX[1]; worldX[1] = tmp;
			tmp = worldY[2]; worldY[2] = worldY[1]; worldY[1] = tmp;
			tmp = worldZ[2]; worldZ[2] = worldZ[1]; worldZ[1] = tmp;
			tmp = normalX[2]; normalX[2] = normalX[1]; normalX[1] = tmp;
			tmp = normalY[2]; normalY[2] = normalY[1]; normalY[1] = tmp;
			tmp = normalZ[2]; normalZ[2] = normalZ[1]; normalZ[1] = tmp;
		}
		if (location0[VECTOR_Y] > location1[VECTOR_Y]) {
			vectorLibrary.swap(location0, location1);
			tmp = this.u[0]; this.u[0] = this.u[1]; this.u[1] = tmp;
			tmp = this.v[0]; this.v[0] = this.v[1]; this.v[1] = tmp;
			tmp = worldX[0]; worldX[0] = worldX[1]; worldX[1] = tmp;
			tmp = worldY[0]; worldY[0] = worldY[1]; worldY[1] = tmp;
			tmp = worldZ[0]; worldZ[0] = worldZ[1]; worldZ[1] = tmp;
			tmp = normalX[0]; normalX[0] = normalX[1]; normalX[1] = tmp;
			tmp = normalY[0]; normalY[0] = normalY[1]; normalY[1] = tmp;
			tmp = normalZ[0]; normalZ[0] = normalZ[1]; normalZ[1] = tmp;
		}
        if (location1[VECTOR_Y] == location2[VECTOR_Y]) {
        	drawBottomTriangle(cameraFrustum);
        } else if (location0[VECTOR_Y] == location1[VECTOR_Y]) {
        	drawTopTriangle(cameraFrustum);
        } else {
            int x = location0[VECTOR_X];
            int dy = mathLibrary.divide(location1[VECTOR_Y] - location0[VECTOR_Y], location2[VECTOR_Y] - location0[VECTOR_Y]);
            int multiplier = location2[VECTOR_X] - location0[VECTOR_X];
            x += mathLibrary.multiply(dy, multiplier);
            int y = location1[VECTOR_Y];
            int z = location0[VECTOR_Z];
            multiplier = location2[VECTOR_Z] - location0[VECTOR_Z];
            z += mathLibrary.multiply(dy, multiplier);
            int uvx = this.u[0];
            multiplier = this.u[2] - this.u[0];
            uvx += mathLibrary.multiply(dy, multiplier);
            int uvy = this.v[0];
            multiplier = this.v[2] - this.v[0];
            uvy += mathLibrary.multiply(dy, multiplier);
            int wx = worldX[0];
            multiplier = worldX[2] - worldX[0];
            wx += mathLibrary.multiply(dy, multiplier);
            int wy = worldY[0];
            multiplier = worldY[2] - worldY[0];
            wy += mathLibrary.multiply(dy, multiplier);
            int wz = worldZ[0];
            multiplier = worldZ[2] - worldZ[0];
            wz += mathLibrary.multiply(dy, multiplier);
            int nx = normalX[0];
            multiplier = normalX[2] - normalX[0];
            nx += mathLibrary.multiply(dy, multiplier);
            int ny = normalY[0];
            multiplier = normalY[2] - normalY[0];
            ny += mathLibrary.multiply(dy, multiplier);
            int nz = normalZ[0];
            multiplier = normalZ[2] - normalZ[0];
            nz += mathLibrary.multiply(dy, multiplier);
            vectorCache[VECTOR_X] = x;
            vectorCache[VECTOR_Y] = y;
            vectorCache[VECTOR_Z] = z;
            vectorLibrary.swap(vectorCache, location2);
            tmp = this.u[2]; this.u[2] = uvx; uvx = tmp;
            tmp = this.v[2]; this.v[2] = uvy; uvy = tmp;
            tmp = worldX[2]; worldX[2] = wx; wx = tmp;
            tmp = worldY[2]; worldY[2] = wy; wy = tmp;
            tmp = worldZ[2]; worldZ[2] = wz; wz = tmp;
            tmp = normalX[2]; normalX[2] = nx; nx = tmp;
            tmp = normalY[2]; normalY[2] = ny; ny = tmp;
            tmp = normalZ[2]; normalZ[2] = nz; nz = tmp;
            drawBottomTriangle(cameraFrustum);
            vectorLibrary.swap(vectorCache, location2);
            vectorLibrary.swap(location0, location1);
            vectorLibrary.swap(location1, vectorCache);
            tmp = this.u[2]; this.u[2] = uvx; uvx = tmp;
            tmp = this.u[0]; this.u[0] = this.u[1]; this.u[1] = tmp;
            tmp = this.u[1]; this.u[1] = uvx; uvx = tmp;
            tmp = this.v[2]; this.v[2] = uvy; uvy = tmp;
            tmp = this.v[0]; this.v[0] = this.v[1]; this.v[1] = tmp;
            tmp = this.v[1]; this.v[1] = uvy; uvy = tmp;
            tmp = worldX[2]; worldX[2] = wx; wx = tmp;
            tmp = worldX[0]; worldX[0] = worldX[1]; worldX[1] = tmp;
            tmp = worldX[1]; worldX[1] = wx; wx = tmp;
            tmp = worldY[2]; worldY[2] = wy; wy = tmp;
            tmp = worldY[0]; worldY[0] = worldY[1]; worldY[1] = tmp;
            tmp = worldY[1]; worldY[1] = wy; wy = tmp;
            tmp = worldZ[2]; worldZ[2] = wz; wz = tmp;
            tmp = worldZ[0]; worldZ[0] = worldZ[1]; worldZ[1] = tmp;
            tmp = worldZ[1]; worldZ[1] = wz; wz = tmp;
            tmp = normalX[2]; normalX[2] = nx; nx = tmp;
            tmp = normalX[0]; normalX[0] = normalX[1]; normalX[1] = tmp;
            tmp = normalX[1]; normalX[1] = nx; nx = tmp;
            tmp = normalY[2]; normalY[2] = ny; ny = tmp;
            tmp = normalY[0]; normalY[0] = normalY[1]; normalY[1] = tmp;
            tmp = normalY[1]; normalY[1] = ny; ny = tmp;
            tmp = normalZ[2]; normalZ[2] = nz; nz = tmp;
            tmp = normalZ[0]; normalZ[0] = normalZ[1]; normalZ[1] = tmp;
            tmp = normalZ[1]; normalZ[1] = nz; nz = tmp;
            drawTopTriangle(cameraFrustum);
        }
	}
	
	private void drawBottomTriangle(int[] cameraFrustum) {
		int xShifted = location0[VECTOR_X] << FP_BITS;
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
            int z = location0[VECTOR_Z] << FP_BITS;
            int u = this.u[0] << FP_BITS;
            int v = this.v[0] << FP_BITS;
            int wx = worldX[0] << FP_BITS;
            int wy = worldY[0] << FP_BITS;
            int wz = worldZ[0] << FP_BITS;
            int nx = normalX[0] << FP_BITS;
            int ny = normalY[0] << FP_BITS;
            int nz = normalZ[0] << FP_BITS;
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
            int z = location0[VECTOR_Z] << FP_BITS;
            int u = this.u[0] << FP_BITS;
            int v = this.v[0] << FP_BITS;
            int wx = worldX[0] << FP_BITS;
            int wy = worldY[0] << FP_BITS;
            int wz = worldZ[0] << FP_BITS;
            int nx = normalX[0] << FP_BITS;
            int ny = normalY[0] << FP_BITS;
            int nz = normalZ[0] << FP_BITS;
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
		int xShifted = location2[VECTOR_X] << FP_BITS;
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
			int z = location2[VECTOR_Z] << FP_BITS;
			int u = this.u[2] << FP_BITS;
			int v = this.v[2] << FP_BITS;
			int wx = worldX[2] << FP_BITS;
            int wy = worldY[2] << FP_BITS;
            int wz = worldZ[2] << FP_BITS;
            int nx = normalX[2] << FP_BITS;
            int ny = normalY[2] << FP_BITS;
            int nz = normalZ[2] << FP_BITS;
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
			int z = location2[VECTOR_Z] << FP_BITS;
			int u = this.u[2] << FP_BITS;
			int v = this.v[2] << FP_BITS;
			int wx = worldX[2] << FP_BITS;
            int wy = worldY[2] << FP_BITS;
            int wz = worldZ[2] << FP_BITS;
            int nx = normalX[2] << FP_BITS;
            int ny = normalY[2] << FP_BITS;
            int nz = normalZ[2] << FP_BITS;
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
	
	private void drawScanline(int x1, int x2, int y, int z, int u, int v, int wx, int wy, int wz, int nx, int ny, int nz,
							int dz, int du, int dv, int dwx, int dwy, int dwz, int dnx, int dny, int dnz, int[] cameraFrustum) {
		x1 >>= FP_BITS;
		x2 >>= FP_BITS;
		for (; x1 <= x2; x1++) {
			pixelCache[VECTOR_X] = x1;
			pixelCache[VECTOR_Y] = y;
			pixelCache[VECTOR_Z] = z >> FP_BITS;
			this.u[3] = u >> FP_BITS;
			this.v[3] = v >> FP_BITS;
			worldX[3] = wx >> FP_BITS;
        	worldY[3] = wy >> FP_BITS;
        	worldZ[3] = wz >> FP_BITS;
			normalX[3] = nx >> FP_BITS;
			normalY[3] = ny >> FP_BITS;
            normalZ[3] = nz >> FP_BITS;
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
