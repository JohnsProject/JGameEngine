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

import static com.johnsproject.jgameengine.library.VectorLibrary.*;
import static com.johnsproject.jgameengine.library.MathLibrary.*;

public class AffineGouraudRasterizer extends GouraudRasterizer {
	
	protected final int[] u;
	protected final int[] v;
	protected final int[] uv;
	protected final int[] uvCache;
	
	public AffineGouraudRasterizer(Shader shader) {
		super(shader);
		u = VectorLibrary.generate();
		v = VectorLibrary.generate();
		uv = VectorLibrary.generate();
		uvCache = VectorLibrary.generate();
	}
	
	public final void setUV0(int[] uv, Texture texture) {
		u[0] = mathLibrary.multiply(uv[VECTOR_X], texture.getWidth() << INTERPOLATE_BIT);
		v[0] = mathLibrary.multiply(uv[VECTOR_Y], texture.getHeight() << INTERPOLATE_BIT);
	}
	
	public final void setUV1(int[] uv, Texture texture) {
		u[1] = mathLibrary.multiply(uv[VECTOR_X], texture.getWidth() << INTERPOLATE_BIT);
		v[1] = mathLibrary.multiply(uv[VECTOR_Y], texture.getHeight() << INTERPOLATE_BIT);
	}
	
	public final void setUV2(int[] uv, Texture texture) {
		u[2] = mathLibrary.multiply(uv[VECTOR_X], texture.getWidth() << INTERPOLATE_BIT);
		v[2] = mathLibrary.multiply(uv[VECTOR_Y], texture.getHeight() << INTERPOLATE_BIT);
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
	public final void drawAffineGouraudTriangle(int[] cameraFrustum) {
		if (location0[VECTOR_Y] > location1[VECTOR_Y]) {
			vectorLibrary.swap(location0, location1);
			swapVector(u, v, 0, 1);
			swapVector(red, green, blue, 0, 1);
		}
		if (location1[VECTOR_Y] > location2[VECTOR_Y]) {
			vectorLibrary.swap(location1, location2);
			swapVector(u, v, 2, 1);
			swapVector(red, green, blue, 2, 1);
		}
		if (location0[VECTOR_Y] > location1[VECTOR_Y]) {
			vectorLibrary.swap(location0, location1);
			swapVector(u, v, 0, 1);
			swapVector(red, green, blue, 0, 1);
		}
        if (location1[VECTOR_Y] == location2[VECTOR_Y]) {
            drawBottomTriangle(cameraFrustum);
        } else if (location0[VECTOR_Y] == location1[VECTOR_Y]) {
            drawTopTriangle(cameraFrustum);
        } else {
        	int x = location0[VECTOR_X];
            int y = location1[VECTOR_Y];
            int z = location0[VECTOR_Z];
            int r = red[0];
            int g = green[0];
            int b = blue[0];
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
            multiplier = red[2] - red[0];
            r += mathLibrary.multiply(dy, multiplier);
            multiplier = green[2] - green[0];
            g += mathLibrary.multiply(dy, multiplier);
            multiplier = blue[2] - blue[0];
            b += mathLibrary.multiply(dy, multiplier);
            vectorCache[VECTOR_X] = x;
            vectorCache[VECTOR_Y] = y;
            vectorCache[VECTOR_Z] = z;
            colorCache[0] = r;
            colorCache[1] = g;
            colorCache[2] = b;
            uvCache[VECTOR_X] = uvx;
            uvCache[VECTOR_Y] = uvy;
            vectorLibrary.swap(vectorCache, location2);
            swapCache(red, green, blue, colorCache, 2);
            swapCache(u, v, uvCache, 2);
            drawBottomTriangle(cameraFrustum);
            vectorLibrary.swap(vectorCache, location2);
            vectorLibrary.swap(location0, location1);
            vectorLibrary.swap(location1, vectorCache);
            swapCache(red, green, blue, colorCache, 2);
            swapVector(red, green, blue, 0, 1);
            swapCache(red, green, blue, colorCache, 1);
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
            int z = location0[VECTOR_Z] << FP_BIT;
            int u = this.u[0] << FP_BIT;
            int v = this.v[0] << FP_BIT;
            int r = red[0] << FP_BIT;
            int g = green[0] << FP_BIT;
            int b = blue[0] << FP_BIT;
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
            int z = location0[VECTOR_Z] << FP_BIT;
            int u = this.u[0] << FP_BIT;
            int v = this.v[0] << FP_BIT;
            int r = red[0] << FP_BIT;
            int g = green[0] << FP_BIT;
            int b = blue[0] << FP_BIT;
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
			int z = location2[VECTOR_Z] << FP_BIT;
			int u = this.u[2] << FP_BIT;
			int v = this.v[2] << FP_BIT;
			int r = red[2] << FP_BIT;
			int g = green[2] << FP_BIT;
			int b = blue[2] << FP_BIT;
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
			int z = location2[VECTOR_Z] << FP_BIT;
			int u = this.u[2] << FP_BIT;
			int v = this.v[2] << FP_BIT;
			int r = red[2] << FP_BIT;
			int g = green[2] << FP_BIT;
			int b = blue[2] << FP_BIT;
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
		x1 >>= FP_BIT;
		x2 >>= FP_BIT;
		for (; x1 <= x2; x1++) {
			pixelCache[VECTOR_X] = x1;
			pixelCache[VECTOR_Y] = y;
			pixelCache[VECTOR_Z] = z >> FP_BIT;
			this.u[3] = u >> FP_PLUS_INTERPOLATE_BIT;
			this.v[3] = v >> FP_PLUS_INTERPOLATE_BIT;
			this.red[3] = r >> FP_PLUS_INTERPOLATE_BIT;
			this.green[3] = g >> FP_PLUS_INTERPOLATE_BIT;
			this.blue[3] = b >> FP_PLUS_INTERPOLATE_BIT;
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

