package com.johnsproject.jgameengine.rasterizer;

import static com.johnsproject.jgameengine.math.FixedPointMath.*;
import static com.johnsproject.jgameengine.math.VectorMath.*;

import com.johnsproject.jgameengine.math.ColorMath;
import com.johnsproject.jgameengine.math.FixedPointMath;
import com.johnsproject.jgameengine.math.VectorMath;
import com.johnsproject.jgameengine.model.Texture;
import com.johnsproject.jgameengine.shader.GeometryBuffer;
import com.johnsproject.jgameengine.shader.Shader;

public class PerspectiveGouraudRasterizer extends AffineGouraudRasterizer {
	
	public PerspectiveGouraudRasterizer(Shader shader) {
		super(shader);
	}
	
	/**
	 * This method tells the rasterizer to draw the given {@link GeometryBuffer geometryBuffer}.
	 * This rasterizer draws a triangle using the x, y coordinates of each vertex of the geometryBuffer. 
	 * It uses perspective interpolation to find out the z and the uv coordinate, as well as the colors for each pixel.
	 * While rasterizing the geometryBuffer, for each pixel/fragment the {@link Shader#fragment} 
	 * method of this rasterizer's {@link Shader} will be called.
	 * 
	 * @param geometryBuffer
	 */
	public void perspectiveDraw(GeometryBuffer geometryBuffer, Texture texture) {
		copyFrustum(shader.getShaderBuffer().getCamera().getRenderTargetPortedFrustum());
		VectorMath.copy(location0, geometryBuffer.getVertexBuffer(0).getLocation());
		VectorMath.copy(location1, geometryBuffer.getVertexBuffer(1).getLocation());
		VectorMath.copy(location2, geometryBuffer.getVertexBuffer(2).getLocation());
		if(cull()) {
			return;
		}
		setColor0(geometryBuffer.getVertexBuffer(0).getColor());
		setColor1(geometryBuffer.getVertexBuffer(1).getColor());
		setColor2(geometryBuffer.getVertexBuffer(2).getColor());
		setUV0(geometryBuffer.getUV(0), texture);
		setUV1(geometryBuffer.getUV(1), texture);
		setUV2(geometryBuffer.getUV(2), texture);
		divideOneByZ();
		zMultiply(u);
		zMultiply(v);
		zMultiply(red);
		zMultiply(green);
		zMultiply(blue);
		if (location0[VECTOR_Y] > location1[VECTOR_Y]) {
			VectorMath.swap(location0, location1);
			swapVector(u, v, 0, 1);
			swapVector(red, green, blue, 0, 1);
		}
		if (location1[VECTOR_Y] > location2[VECTOR_Y]) {
			VectorMath.swap(location1, location2);
			swapVector(u, v, 2, 1);
			swapVector(red, green, blue, 2, 1);
		}
		if (location0[VECTOR_Y] > location1[VECTOR_Y]) {
			VectorMath.swap(location0, location1);
			swapVector(u, v, 0, 1);
			swapVector(red, green, blue, 0, 1);
		}
        if (location1[VECTOR_Y] == location2[VECTOR_Y]) {
            drawBottomTriangle();
        } else if (location0[VECTOR_Y] == location1[VECTOR_Y]) {
            drawTopTriangle();
        } else {
        	int x = location0[VECTOR_X];
            int y = location1[VECTOR_Y];
            int z = location0[VECTOR_Z];
            int r = red[0];
            int g = green[0];
            int b = blue[0];
            int uvx = u[0];
            int uvy = v[0];
            int dy = FixedPointMath.divide(location1[VECTOR_Y] - location0[VECTOR_Y], location2[VECTOR_Y] - location0[VECTOR_Y]);
            int multiplier = location2[VECTOR_X] - location0[VECTOR_X];
            x += FixedPointMath.multiply(dy, multiplier);
            multiplier = location2[VECTOR_Z] - location0[VECTOR_Z];
            z += FixedPointMath.multiply(dy, multiplier);
            multiplier = u[2] - u[0];
            uvx += FixedPointMath.multiply(dy, multiplier);
            multiplier = v[2] - v[0];
            uvy += FixedPointMath.multiply(dy, multiplier);
            multiplier = red[2] - red[0];
            r += FixedPointMath.multiply(dy, multiplier);
            multiplier = green[2] - green[0];
            g += FixedPointMath.multiply(dy, multiplier);
            multiplier = blue[2] - blue[0];
            b += FixedPointMath.multiply(dy, multiplier);
            vectorCache[VECTOR_X] = x;
            vectorCache[VECTOR_Y] = y;
            vectorCache[VECTOR_Z] = z;
            colorCache[0] = r;
            colorCache[1] = g;
            colorCache[2] = b;
            uvCache[VECTOR_X] = uvx;
            uvCache[VECTOR_Y] = uvy;
            VectorMath.swap(vectorCache, location2);
            swapCache(red, green, blue, colorCache, 2);
            swapCache(u, v, uvCache, 2);
            drawBottomTriangle();
            VectorMath.swap(vectorCache, location2);
            VectorMath.swap(location0, location1);
            VectorMath.swap(location1, vectorCache);
            swapCache(red, green, blue, colorCache, 2);
            swapVector(red, green, blue, 0, 1);
            swapCache(red, green, blue, colorCache, 1);
            swapCache(u, v, uvCache, 2);
            swapVector(u, v, 0, 1);
            swapCache(u, v, uvCache, 1);
            drawTopTriangle();
        }
	}
	
	private void drawBottomTriangle() {
		int xShifted = location0[VECTOR_X] << FP_BIT;
		int y2y1 = location1[VECTOR_Y] - location0[VECTOR_Y];
		int y3y1 = location1[VECTOR_Y] - location0[VECTOR_Y];
		y2y1 = y2y1 == 0 ? 1 : y2y1;
		y3y1 = y3y1 == 0 ? 1 : y3y1;
        int dx1 = FixedPointMath.divide(location1[VECTOR_X] - location0[VECTOR_X], y2y1);
        int dx2 = FixedPointMath.divide(location2[VECTOR_X] - location0[VECTOR_X], y3y1);
        int dz1 = FixedPointMath.divide(location1[VECTOR_Z] - location0[VECTOR_Z], y2y1);
        int dz2 = FixedPointMath.divide(location2[VECTOR_Z] - location0[VECTOR_Z], y3y1);
        int du1 = FixedPointMath.divide(this.u[1] - this.u[0], y2y1);
        int du2 = FixedPointMath.divide(this.u[2] - this.u[0], y3y1);
        int dv1 = FixedPointMath.divide(this.v[1] - this.v[0], y2y1);
        int dv2 = FixedPointMath.divide(this.v[2] - this.v[0], y3y1);
        int dr1 = FixedPointMath.divide(red[1] - red[0], y2y1);
        int dr2 = FixedPointMath.divide(red[2] - red[0], y3y1);
        int dg1 = FixedPointMath.divide(green[1] - green[0], y2y1);
        int dg2 = FixedPointMath.divide(green[2] - green[0], y3y1);
        int db1 = FixedPointMath.divide(blue[1] - blue[0], y2y1);
        int db2 = FixedPointMath.divide(blue[2] - blue[0], y3y1);
        if(dx1 < dx2) {
        	int dxdx = dx2 - dx1;
        	dxdx = dxdx == 0 ? 1 : dxdx;
        	int dz = FixedPointMath.divide(dz2 - dz1, dxdx);
        	int du = FixedPointMath.divide(du2 - du1, dxdx);
        	int dv = FixedPointMath.divide(dv2 - dv1, dxdx);
        	int dr = FixedPointMath.divide(dr2 - dr1, dxdx);
        	int dg = FixedPointMath.divide(dg2 - dg1, dxdx);
        	int db = FixedPointMath.divide(db2 - db1, dxdx);
        	int x1 = xShifted;
            int x2 = xShifted;
            int z = location0[VECTOR_Z] << FP_BIT;
            int u = this.u[0] << FP_BIT;
            int v = this.v[0] << FP_BIT;
            int r = red[0] << FP_BIT;
            int g = green[0] << FP_BIT;
            int b = blue[0] << FP_BIT;
	        for (int y = location0[VECTOR_Y]; y <= location1[VECTOR_Y]; y++) {
	        	drawScanline(x1, x2, y, z, u, v, r, g, b, dz, du, dv, dr, dg, db);
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
        	int dz = FixedPointMath.divide(dz1 - dz2, dxdx);
        	int du = FixedPointMath.divide(du1 - du2, dxdx);
        	int dv = FixedPointMath.divide(dv1 - dv2, dxdx);
        	int dr = FixedPointMath.divide(dr1 - dr2, dxdx);
        	int dg = FixedPointMath.divide(dg1 - dg2, dxdx);
        	int db = FixedPointMath.divide(db1 - db2, dxdx);
        	int x1 = xShifted;
            int x2 = xShifted;
            int z = location0[VECTOR_Z] << FP_BIT;
            int u = this.u[0] << FP_BIT;
            int v = this.v[0] << FP_BIT;
            int r = red[0] << FP_BIT;
            int g = green[0] << FP_BIT;
            int b = blue[0] << FP_BIT;
        	for (int y = location0[VECTOR_Y]; y <= location1[VECTOR_Y]; y++) {
        		drawScanline(x1, x2, y, z, u, v, r, g, b, dz, du, dv, dr, dg, db);
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
    
	private void drawTopTriangle() {
		int xShifted = location2[VECTOR_X] << FP_BIT;
		int y3y1 = location2[VECTOR_Y] - location0[VECTOR_Y];
		int y3y2 = location2[VECTOR_Y] - location1[VECTOR_Y];
		y3y1 = y3y1 == 0 ? 1 : y3y1;
		y3y2 = y3y2 == 0 ? 1 : y3y2;
		int dx1 = FixedPointMath.divide(location2[VECTOR_X] - location0[VECTOR_X], y3y1);
		int dx2 = FixedPointMath.divide(location2[VECTOR_X] - location1[VECTOR_X], y3y2);
		int dz1 = FixedPointMath.divide(location2[VECTOR_Z] - location0[VECTOR_Z], y3y1);
		int dz2 = FixedPointMath.divide(location2[VECTOR_Z] - location1[VECTOR_Z], y3y2);
		int du1 = FixedPointMath.divide(this.u[2] - this.u[0], y3y1);
		int du2 = FixedPointMath.divide(this.u[2] - this.u[1], y3y2);
		int dv1 = FixedPointMath.divide(this.v[2] - this.v[0], y3y1);
		int dv2 = FixedPointMath.divide(this.v[2] - this.v[1], y3y2);
		int dr1 = FixedPointMath.divide(red[2] - red[0], y3y1);
		int dr2 = FixedPointMath.divide(red[2] - red[1], y3y2);
		int dg1 = FixedPointMath.divide(green[2] - green[0], y3y1);
		int dg2 = FixedPointMath.divide(green[2] - green[1], y3y2);
		int db1 = FixedPointMath.divide(blue[2] - blue[0], y3y1);
		int db2 = FixedPointMath.divide(blue[2] - blue[1], y3y2);
		if (dx1 > dx2) {
			int dxdx = dx1 - dx2;
			dxdx = dxdx == 0 ? 1 : dxdx;
			int dz = FixedPointMath.divide(dz1 - dz2, dxdx);
			int du = FixedPointMath.divide(du1 - du2, dxdx);
			int dv = FixedPointMath.divide(dv1 - dv2, dxdx);
			int dr = FixedPointMath.divide(dr1 - dr2, dxdx);
			int dg = FixedPointMath.divide(dg1 - dg2, dxdx);
			int db = FixedPointMath.divide(db1 - db2, dxdx);
			int x1 = xShifted;
			int x2 = xShifted;
			int z = location2[VECTOR_Z] << FP_BIT;
			int u = this.u[2] << FP_BIT;
			int v = this.v[2] << FP_BIT;
			int r = red[2] << FP_BIT;
			int g = green[2] << FP_BIT;
			int b = blue[2] << FP_BIT;
	        for (int y = location2[VECTOR_Y]; y > location0[VECTOR_Y]; y--) {
	        	drawScanline(x1, x2, y, z, u, v, r, g, b, dz, du, dv, dr, dg, db);
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
			int dz = FixedPointMath.divide(dz2 - dz1, dxdx);
			int du = FixedPointMath.divide(du2 - du1, dxdx);
			int dv = FixedPointMath.divide(dv2 - dv1, dxdx);
			int dr = FixedPointMath.divide(dr2 - dr1, dxdx);
			int dg = FixedPointMath.divide(dg2 - dg1, dxdx);
			int db = FixedPointMath.divide(db2 - db1, dxdx);
			int x1 = xShifted;
			int x2 = xShifted;
			int z = location2[VECTOR_Z] << FP_BIT;
			int u = this.u[2] << FP_BIT;
			int v = this.v[2] << FP_BIT;
			int r = red[2] << FP_BIT;
			int g = green[2] << FP_BIT;
			int b = blue[2] << FP_BIT;
	        for (int y = location2[VECTOR_Y]; y > location0[VECTOR_Y]; y--) {
	        	drawScanline(x1, x2, y, z, u, v, r, g, b, dz, du, dv, dr, dg, db);
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
	
	private static final int DIVISION_ONE = FP_ONE << FP_BIT;
	private static final int INTERPOLATE_BIT_2 = INTERPOLATE_BIT * 2;
	private void drawScanline(int x1, int x2, int y, int z, int u, int v, int r, int g, int b, int dz, int du, int dv, int dr, int dg, int db) {
		x1 >>= FP_BIT;
		x2 >>= FP_BIT;
		int oneByZ, cr, cg, cb;
		for (; x1 <= x2; x1++) {
			fragmentBuffer.getLocation()[VECTOR_X] = x1;
			fragmentBuffer.getLocation()[VECTOR_Y] = y;
			oneByZ = DIVISION_ONE / (z >> INTERPOLATE_BIT);
			fragmentBuffer.getLocation()[VECTOR_Z] = oneByZ;
			fragmentBuffer.getUV()[VECTOR_X] = FixedPointMath.multiply(u, oneByZ) >> INTERPOLATE_BIT_2;
			fragmentBuffer.getUV()[VECTOR_Y] = FixedPointMath.multiply(v, oneByZ) >> INTERPOLATE_BIT_2;
			cr = FixedPointMath.multiply(r, oneByZ) >> INTERPOLATE_BIT_2;
			cg = FixedPointMath.multiply(g, oneByZ) >> INTERPOLATE_BIT_2;
			cb = FixedPointMath.multiply(b, oneByZ) >> INTERPOLATE_BIT_2;
			fragmentBuffer.setColor(ColorMath.toColor(cr, cg, cb));
			shader.fragment(fragmentBuffer);
			z += dz;
			u += du;
			v += dv;
			r += dr;
			g += dg;
			b += db;
		}
	}
}
