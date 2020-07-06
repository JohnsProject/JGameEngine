package com.johnsproject.jgameengine.rasterizer;

import static com.johnsproject.jgameengine.math.FixedPointMath.*;
import static com.johnsproject.jgameengine.math.VectorMath.*;

import com.johnsproject.jgameengine.math.FixedPointMath;
import com.johnsproject.jgameengine.math.VectorMath;
import com.johnsproject.jgameengine.model.Face;
import com.johnsproject.jgameengine.model.Texture;
import com.johnsproject.jgameengine.shader.Shader;

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
	public void perspectiveDraw(Face face, Texture texture) {
		copyFrustum(shader.getShaderBuffer().getCamera().getRenderTargetPortedFrustum());
		VectorMath.copy(location0, face.getVertex(0).getLocation());
		VectorMath.copy(location1, face.getVertex(1).getLocation());
		VectorMath.copy(location2, face.getVertex(2).getLocation());
		if(cull()) {
			return;
		}
		setUV0(face.getUV(0), texture);
		setUV1(face.getUV(1), texture);
		setUV2(face.getUV(2), texture);
		divideOneByZ();
		zMultiply(u);
		zMultiply(v);
		if (location0[VECTOR_Y] > location1[VECTOR_Y]) {
			VectorMath.swap(location0, location1);
			swapVector(u, v, 0, 1);
		}
		if (location1[VECTOR_Y] > location2[VECTOR_Y]) {
			VectorMath.swap(location1, location2);
			swapVector(u, v, 2, 1);
		}
		if (location0[VECTOR_Y] > location1[VECTOR_Y]) {
			VectorMath.swap(location0, location1);
			swapVector(u, v, 0, 1);
		}
        if (location1[VECTOR_Y] == location2[VECTOR_Y]) {
        	drawBottomTriangle();
        } else if (location0[VECTOR_Y] == location1[VECTOR_Y]) {
            drawTopTriangle();
        } else {
        	 int x = location0[VECTOR_X];
             int y = location1[VECTOR_Y];
             int z = location0[VECTOR_Z];
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
             vectorCache[VECTOR_X] = x;
             vectorCache[VECTOR_Y] = y;
             vectorCache[VECTOR_Z] = z;
             uvCache[VECTOR_X] = uvx;
             uvCache[VECTOR_Y] = uvy;
             VectorMath.swap(vectorCache, location2);
             swapCache(u, v, uvCache, 2);
             drawBottomTriangle();
             VectorMath.swap(vectorCache, location2);
             VectorMath.swap(location0, location1);
             VectorMath.swap(location1, vectorCache);
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
        int du1 = FixedPointMath.divide(u[1] - u[0], y2y1);
        int du2 = FixedPointMath.divide(u[2] - u[0], y3y1);
        int dv1 = FixedPointMath.divide(v[1] - v[0], y2y1);
        int dv2 = FixedPointMath.divide(v[2] - v[0], y3y1);
        if(dx1 < dx2) {
        	int dxdx = dx2 - dx1;
        	dxdx = dxdx == 0 ? 1 : dxdx;
        	int dz = FixedPointMath.divide(dz2 - dz1, dxdx);
        	int du = FixedPointMath.divide(du2 - du1, dxdx);
        	int dv = FixedPointMath.divide(dv2 - dv1, dxdx);
        	int x1 = xShifted;
            int x2 = xShifted;
            int z = location0[VECTOR_Z] << FP_BIT;
            int u = this.u[0] << FP_BIT;
            int v = this.v[0] << FP_BIT;
	        for (int y = location0[VECTOR_Y]; y <= location1[VECTOR_Y]; y++) {
	        	drawScanline(x1, x2, y, z, u, v, dz, du, dv);
	            x1 += dx1;
	            x2 += dx2;
	            z += dz1;
	            u += du1;
	            v += dv1;
	        }
        } else {
        	int dxdx = dx1 - dx2;
        	dxdx = dxdx == 0 ? 1 : dxdx;
        	int dz = FixedPointMath.divide(dz1 - dz2, dxdx);
        	int du = FixedPointMath.divide(du1 - du2, dxdx);
        	int dv = FixedPointMath.divide(dv1 - dv2, dxdx);
        	int x1 = xShifted;
            int x2 = xShifted;
            int z = location0[VECTOR_Z] << FP_BIT;
            int u = this.u[0] << FP_BIT;
            int v = this.v[0] << FP_BIT;
        	for (int y = location0[VECTOR_Y]; y <= location1[VECTOR_Y]; y++) {
        		drawScanline(x1, x2, y, z, u, v, dz, du, dv);
	            x1 += dx2;
	            x2 += dx1;
	            z += dz2;
	            u += du2;
	            v += dv2;
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
		int du1 = FixedPointMath.divide(u[2] - u[0], y3y1);
		int du2 = FixedPointMath.divide(u[2] - u[1], y3y2);
		int dv1 = FixedPointMath.divide(v[2] - v[0], y3y1);
		int dv2 = FixedPointMath.divide(v[2] - v[1], y3y2);
		if (dx1 > dx2) {
			int dxdx = dx1 - dx2;
			dxdx = dxdx == 0 ? 1 : dxdx;
			int dz = FixedPointMath.divide(dz1 - dz2, dxdx);
			int du = FixedPointMath.divide(du1 - du2, dxdx);
			int dv = FixedPointMath.divide(dv1 - dv2, dxdx);
			int x1 = xShifted;
			int x2 = xShifted;
			int z = location2[VECTOR_Z] << FP_BIT;
			int u = this.u[2] << FP_BIT;
			int v = this.v[2] << FP_BIT;
	        for (int y = location2[VECTOR_Y]; y > location0[VECTOR_Y]; y--) {
	        	drawScanline(x1, x2, y, z, u, v, dz, du, dv);
	            x1 -= dx1;
	            x2 -= dx2;
	            z -= dz1;
	            u -= du1;
	            v -= dv1;
	        }
		} else {
			int dxdx = dx2 - dx1;
			dxdx = dxdx == 0 ? 1 : dxdx;
			int dz = FixedPointMath.divide(dz2 - dz1, dxdx);
			int du = FixedPointMath.divide(du2 - du1, dxdx);
			int dv = FixedPointMath.divide(dv2 - dv1, dxdx);
			int x1 = xShifted;
			int x2 = xShifted;
			int z = location2[VECTOR_Z] << FP_BIT;
			int u = this.u[2] << FP_BIT;
			int v = this.v[2] << FP_BIT;
	        for (int y = location2[VECTOR_Y]; y > location0[VECTOR_Y]; y--) {
	        	drawScanline(x1, x2, y, z, u, v, dz, du, dv);
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
	private void drawScanline(int x1, int x2, int y, int z, int u, int v, int dz, int du, int dv) {
		x1 >>= FP_BIT;
		x2 >>= FP_BIT;
		int oneByZ;
		for (; x1 <= x2; x1++) {
			fragmentBuffer.getLocation()[VECTOR_X] = x1;
			fragmentBuffer.getLocation()[VECTOR_Y] = y;
			oneByZ = DIVISION_ONE / (z >> INTERPOLATE_BIT);
			fragmentBuffer.getLocation()[VECTOR_Z] = oneByZ;
			fragmentBuffer.getUV()[VECTOR_X] = FixedPointMath.multiply(u, oneByZ) >> INTERPOLATE_BIT_2;
			fragmentBuffer.getUV()[VECTOR_Y] = FixedPointMath.multiply(v, oneByZ) >> INTERPOLATE_BIT_2;
			shader.fragment(fragmentBuffer);
			z += dz;
			u += du;
			v += dv;
		}
	}
}
