package com.johnsproject.jgameengine.rasterizer;

import static com.johnsproject.jgameengine.util.FixedPointUtils.*;
import static com.johnsproject.jgameengine.util.VectorUtils.*;

import com.johnsproject.jgameengine.model.Face;
import com.johnsproject.jgameengine.model.Texture;
import com.johnsproject.jgameengine.shader.Shader;
import com.johnsproject.jgameengine.util.FixedPointUtils;
import com.johnsproject.jgameengine.util.VectorUtils;

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
		VectorUtils.copy(location0, face.getVertex(0).getLocation());
		VectorUtils.copy(location1, face.getVertex(1).getLocation());
		VectorUtils.copy(location2, face.getVertex(2).getLocation());
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
			VectorUtils.swap(location0, location1);
			swapVector(u, v, 0, 1);
		}
		if (location1[VECTOR_Y] > location2[VECTOR_Y]) {
			VectorUtils.swap(location1, location2);
			swapVector(u, v, 2, 1);
		}
		if (location0[VECTOR_Y] > location1[VECTOR_Y]) {
			VectorUtils.swap(location0, location1);
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
             int dy = FixedPointUtils.divide(location1[VECTOR_Y] - location0[VECTOR_Y], location2[VECTOR_Y] - location0[VECTOR_Y]);
             int multiplier = location2[VECTOR_X] - location0[VECTOR_X];
             x += FixedPointUtils.multiply(dy, multiplier);
             multiplier = location2[VECTOR_Z] - location0[VECTOR_Z];
             z += FixedPointUtils.multiply(dy, multiplier);
             multiplier = u[2] - u[0];
             uvx += FixedPointUtils.multiply(dy, multiplier);
             multiplier = v[2] - v[0];
             uvy += FixedPointUtils.multiply(dy, multiplier);
             vectorCache[VECTOR_X] = x;
             vectorCache[VECTOR_Y] = y;
             vectorCache[VECTOR_Z] = z;
             uvCache[VECTOR_X] = uvx;
             uvCache[VECTOR_Y] = uvy;
             VectorUtils.swap(vectorCache, location2);
             swapCache(u, v, uvCache, 2);
             drawBottomTriangle();
             VectorUtils.swap(vectorCache, location2);
             VectorUtils.swap(location0, location1);
             VectorUtils.swap(location1, vectorCache);
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
        int dx1 = FixedPointUtils.divide(location1[VECTOR_X] - location0[VECTOR_X], y2y1);
        int dx2 = FixedPointUtils.divide(location2[VECTOR_X] - location0[VECTOR_X], y3y1);
        int dz1 = FixedPointUtils.divide(location1[VECTOR_Z] - location0[VECTOR_Z], y2y1);
        int dz2 = FixedPointUtils.divide(location2[VECTOR_Z] - location0[VECTOR_Z], y3y1);
        int du1 = FixedPointUtils.divide(u[1] - u[0], y2y1);
        int du2 = FixedPointUtils.divide(u[2] - u[0], y3y1);
        int dv1 = FixedPointUtils.divide(v[1] - v[0], y2y1);
        int dv2 = FixedPointUtils.divide(v[2] - v[0], y3y1);
        if(dx1 < dx2) {
        	int dxdx = dx2 - dx1;
        	dxdx = dxdx == 0 ? 1 : dxdx;
        	int dz = FixedPointUtils.divide(dz2 - dz1, dxdx);
        	int du = FixedPointUtils.divide(du2 - du1, dxdx);
        	int dv = FixedPointUtils.divide(dv2 - dv1, dxdx);
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
        	int dz = FixedPointUtils.divide(dz1 - dz2, dxdx);
        	int du = FixedPointUtils.divide(du1 - du2, dxdx);
        	int dv = FixedPointUtils.divide(dv1 - dv2, dxdx);
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
		int dx1 = FixedPointUtils.divide(location2[VECTOR_X] - location0[VECTOR_X], y3y1);
		int dx2 = FixedPointUtils.divide(location2[VECTOR_X] - location1[VECTOR_X], y3y2);
		int dz1 = FixedPointUtils.divide(location2[VECTOR_Z] - location0[VECTOR_Z], y3y1);
		int dz2 = FixedPointUtils.divide(location2[VECTOR_Z] - location1[VECTOR_Z], y3y2);
		int du1 = FixedPointUtils.divide(u[2] - u[0], y3y1);
		int du2 = FixedPointUtils.divide(u[2] - u[1], y3y2);
		int dv1 = FixedPointUtils.divide(v[2] - v[0], y3y1);
		int dv2 = FixedPointUtils.divide(v[2] - v[1], y3y2);
		if (dx1 > dx2) {
			int dxdx = dx1 - dx2;
			dxdx = dxdx == 0 ? 1 : dxdx;
			int dz = FixedPointUtils.divide(dz1 - dz2, dxdx);
			int du = FixedPointUtils.divide(du1 - du2, dxdx);
			int dv = FixedPointUtils.divide(dv1 - dv2, dxdx);
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
			int dz = FixedPointUtils.divide(dz2 - dz1, dxdx);
			int du = FixedPointUtils.divide(du2 - du1, dxdx);
			int dv = FixedPointUtils.divide(dv2 - dv1, dxdx);
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
			fragment.getLocation()[VECTOR_X] = x1;
			fragment.getLocation()[VECTOR_Y] = y;
			oneByZ = DIVISION_ONE / (z >> INTERPOLATE_BIT);
			fragment.getLocation()[VECTOR_Z] = oneByZ;
			fragment.getUV()[VECTOR_X] = FixedPointUtils.multiply(u, oneByZ) >> INTERPOLATE_BIT_2;
			fragment.getUV()[VECTOR_Y] = FixedPointUtils.multiply(v, oneByZ) >> INTERPOLATE_BIT_2;
			shader.fragment(fragment);
			z += dz;
			u += du;
			v += dv;
		}
	}
}
