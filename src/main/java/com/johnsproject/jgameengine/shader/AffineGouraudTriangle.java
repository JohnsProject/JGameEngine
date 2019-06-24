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
package com.johnsproject.jgameengine.shader;

import com.johnsproject.jgameengine.dto.Texture;

public class AffineGouraudTriangle extends GouraudTriangle {
	
	protected final int[] u;
	protected final int[] v;
	protected final int[] uv;
	
	public AffineGouraudTriangle(Shader shader) {
		super(shader);
		u = vectorLibrary.generate();
		v = vectorLibrary.generate();
		uv = vectorLibrary.generate();
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
	
	public final void drawAffineGouraudTriangle(int[] cameraFrustum) {
		int tmp = 0;
		if (location0[VECTOR_Y] > location1[VECTOR_Y]) {
			vectorLibrary.swap(location0, location1);
			tmp = this.u[0]; this.u[0] = this.u[1]; this.u[1] = tmp;
			tmp = this.v[0]; this.v[0] = this.v[1]; this.v[1] = tmp;
			tmp = red[0]; red[0] = red[1]; red[1] = tmp;
			tmp = green[0]; green[0] = green[1]; green[1] = tmp;
			tmp = blue[0]; blue[0] = blue[1]; blue[1] = tmp;
		}
		if (location1[VECTOR_Y] > location2[VECTOR_Y]) {
			vectorLibrary.swap(location1, location2);
			tmp = this.u[2]; this.u[2] = this.u[1]; this.u[1] = tmp;
			tmp = this.v[2]; this.v[2] = this.v[1]; this.v[1] = tmp;
			tmp = red[2]; red[2] = red[1]; red[1] = tmp;
			tmp = green[2]; green[2] = green[1]; green[1] = tmp;
			tmp = blue[2]; blue[2] = blue[1]; blue[1] = tmp;
		}
		if (location0[VECTOR_Y] > location1[VECTOR_Y]) {
			vectorLibrary.swap(location0, location1);
			tmp = this.u[0]; this.u[0] = this.u[1]; this.u[1] = tmp;
			tmp = this.v[0]; this.v[0] = this.v[1]; this.v[1] = tmp;
			tmp = red[0]; red[0] = red[1]; red[1] = tmp;
			tmp = green[0]; green[0] = green[1]; green[1] = tmp;
			tmp = blue[0]; blue[0] = blue[1]; blue[1] = tmp;
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
            vectorLibrary.swap(vectorCache, location2);
            tmp = this.u[2]; this.u[2] = uvx; uvx = tmp;
            tmp = this.v[2]; this.v[2] = uvy; uvy = tmp;
            tmp = red[2]; red[2] = r; r = tmp;
            tmp = green[2]; green[2] = g; g = tmp;
            tmp = blue[2]; blue[2] = b; b = tmp;
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
        	int du = mathLibrary.divide(du2 - du1, dxdx);
        	int dv = mathLibrary.divide(dv2 - dv1, dxdx);
        	int dr = mathLibrary.divide(dr2 - dr1, dxdx);
        	int dg = mathLibrary.divide(dg2 - dg1, dxdx);
        	int db = mathLibrary.divide(db2 - db1, dxdx);
        	int x1 = xShifted;
            int x2 = xShifted;
            int z = location0[VECTOR_Z] << FP_BITS;
            int u = this.u[0] << FP_BITS;
            int v = this.v[0] << FP_BITS;
            int r = red[0] << FP_BITS;
            int g = green[0] << FP_BITS;
            int b = blue[0] << FP_BITS;
	        for (int y = location0[VECTOR_Y]; y <= location1[VECTOR_Y]; y++) {
	        	drawScanline(x1, x2, y, z, u, v, r, g, b, dz, du, dv, dr, dg, db, cameraFrustum);
	            x1 += dx1;
	            x2 += dx2;
	            z += dz1;
	            u += du1;
	            v += dv1;
	            r += dr1;
	            g += dg1;
	            b += db1;
	        }
        } else {
        	int dxdx = dx1 - dx2;
        	dxdx = dxdx == 0 ? 1 : dxdx;
        	int dz = mathLibrary.divide(dz1 - dz2, dxdx);
        	int du = mathLibrary.divide(du1 - du2, dxdx);
        	int dv = mathLibrary.divide(dv1 - dv2, dxdx);
        	int dr = mathLibrary.divide(dr1 - dr2, dxdx);
        	int dg = mathLibrary.divide(dg1 - dg2, dxdx);
        	int db = mathLibrary.divide(db1 - db2, dxdx);
        	int x1 = xShifted;
            int x2 = xShifted;
            int z = location0[VECTOR_Z] << FP_BITS;
            int u = this.u[0] << FP_BITS;
            int v = this.v[0] << FP_BITS;
            int r = red[0] << FP_BITS;
            int g = green[0] << FP_BITS;
            int b = blue[0] << FP_BITS;
        	for (int y = location0[VECTOR_Y]; y <= location1[VECTOR_Y]; y++) {
        		drawScanline(x1, x2, y, z, u, v, r, g, b, dz, du, dv, dr, dg, db, cameraFrustum);
	            x1 += dx2;
	            x2 += dx1;
	            z += dz2;
	            u += du2;
	            v += dv2;
	            r += dr2;
	            g += dg2;
	            b += db2;
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
			int du = mathLibrary.divide(du1 - du2, dxdx);
			int dv = mathLibrary.divide(dv1 - dv2, dxdx);
			int dr = mathLibrary.divide(dr1 - dr2, dxdx);
			int dg = mathLibrary.divide(dg1 - dg2, dxdx);
			int db = mathLibrary.divide(db1 - db2, dxdx);
			int x1 = xShifted;
			int x2 = xShifted;
			int z = location2[VECTOR_Z] << FP_BITS;
			int u = this.u[2] << FP_BITS;
			int v = this.v[2] << FP_BITS;
			int r = red[2] << FP_BITS;
			int g = green[2] << FP_BITS;
			int b = blue[2] << FP_BITS;
	        for (int y = location2[VECTOR_Y]; y > location0[VECTOR_Y]; y--) {
	        	drawScanline(x1, x2, y, z, u, v, r, g, b, dz, du, dv, dr, dg, db, cameraFrustum);
	            x1 -= dx1;
	            x2 -= dx2;
	            z -= dz1;
	            u -= du1;
	            v -= dv1;
	            r -= dr1;
	            g -= dg1;
	            b -= db1;
	        }
		} else {
			int dxdx = dx2 - dx1;
			dxdx = dxdx == 0 ? 1 : dxdx;
			int dz = mathLibrary.divide(dz2 - dz1, dxdx);
			int du = mathLibrary.divide(du2 - du1, dxdx);
			int dv = mathLibrary.divide(dv2 - dv1, dxdx);
			int dr = mathLibrary.divide(dr2 - dr1, dxdx);
			int dg = mathLibrary.divide(dg2 - dg1, dxdx);
			int db = mathLibrary.divide(db2 - db1, dxdx);
			int x1 = xShifted;
			int x2 = xShifted;
			int z = location2[VECTOR_Z] << FP_BITS;
			int u = this.u[2] << FP_BITS;
			int v = this.v[2] << FP_BITS;
			int r = red[2] << FP_BITS;
			int g = green[2] << FP_BITS;
			int b = blue[2] << FP_BITS;
	        for (int y = location2[VECTOR_Y]; y > location0[VECTOR_Y]; y--) {
	        	drawScanline(x1, x2, y, z, u, v, r, g, b, dz, du, dv, dr, dg, db, cameraFrustum);
	            x1 -= dx2;
	            x2 -= dx1;
	            z -= dz2;
	            u -= du2;
	            v -= dv2;
	            r -= dr2;
	            g -= dg2;
	            b -= db2;
	        }
		}
    }
	
	private void drawScanline(int x1, int x2, int y, int z, int u, int v, int r, int g, int b, int dz, int du, int dv, int dr, int dg, int db, int[] cameraFrustum) {
		x1 >>= FP_BITS;
		x2 >>= FP_BITS;
		for (; x1 <= x2; x1++) {
			pixelCache[VECTOR_X] = x1;
			pixelCache[VECTOR_Y] = y;
			pixelCache[VECTOR_Z] = z >> FP_BITS;
			this.u[3] = u >> FP_BITS;
			this.v[3] = v >> FP_BITS;
			this.red[3] = r >> FP_BITS;
			this.green[3] = g >> FP_BITS;
			this.blue[3] = b >> FP_BITS;
			shader.fragment(pixelCache);
			z += dz;
			u += du;
			v += dv;
			r += dr;
			g += dg;
			b += db;
		}
	}
}

