package com.johnsproject.jgameengine.rasterization;

import static com.johnsproject.jgameengine.util.FixedPointUtils.FP_BIT;
import static com.johnsproject.jgameengine.util.FixedPointUtils.FP_ONE;
import static com.johnsproject.jgameengine.util.VectorUtils.VECTOR_X;
import static com.johnsproject.jgameengine.util.VectorUtils.VECTOR_Y;
import static com.johnsproject.jgameengine.util.VectorUtils.VECTOR_Z;
import static com.johnsproject.jgameengine.rasterization.RasterizerUtils.*;

import com.johnsproject.jgameengine.model.Face;
import com.johnsproject.jgameengine.model.Texture;
import com.johnsproject.jgameengine.shading.Shader;
import com.johnsproject.jgameengine.util.ColorUtils;
import com.johnsproject.jgameengine.util.FixedPointUtils;
import com.johnsproject.jgameengine.util.VectorUtils;

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
	public void perspectiveDraw(Face face, Texture texture) {
		copyLocations(face);
		copyFrustum();
		if(isCulled())
			return;
		fragment.setMaterial(face.getMaterial());
		copyColors(face);
		copyUV(face, texture);
		RasterizerUtils.divideOneByZ(location0, location1, location2);
		RasterizerUtils.zMultiply(u, location0, location1, location2);
		RasterizerUtils.zMultiply(v, location0, location1, location2);
		RasterizerUtils.zMultiply(red, location0, location1, location2);
		RasterizerUtils.zMultiply(green, location0, location1, location2);
		RasterizerUtils.zMultiply(blue, location0, location1, location2);
		sortY();
        if (location1[VECTOR_Y] == location2[VECTOR_Y]) {
            drawBottomTriangle();
        } else if (location0[VECTOR_Y] == location1[VECTOR_Y]) {
            drawTopTriangle();
        } else {
        	splitTriangle();
            drawSplitedTriangle();
        }
	}
	
	private void drawSplitedTriangle() {
		VectorUtils.swap(vectorCache, location2);
		RasterizerUtils.swapCache(red, colorCache, 0, 2);
		RasterizerUtils.swapCache(green, colorCache, 1, 2);
		RasterizerUtils.swapCache(red, colorCache, 2, 2);
		RasterizerUtils.swapCache(u, uvCache, 0, 2);
		RasterizerUtils.swapCache(v, uvCache, 1, 2);
        drawBottomTriangle();
        VectorUtils.swap(vectorCache, location2);
        VectorUtils.swap(location0, location1);
        VectorUtils.swap(location1, vectorCache);
        RasterizerUtils.swapCache(red, colorCache, 0, 2);
        RasterizerUtils.swapCache(green, colorCache, 1, 2);
        RasterizerUtils.swapCache(blue, colorCache, 2, 2);
        RasterizerUtils.swapVector(red, 0, 1);
        RasterizerUtils.swapVector(green, 0, 1);
        RasterizerUtils.swapVector(blue, 0, 1);
        RasterizerUtils.swapCache(red, colorCache, 0, 1);
        RasterizerUtils.swapCache(green, colorCache, 1, 1);
        RasterizerUtils.swapCache(red, colorCache, 2, 1);
        RasterizerUtils.swapCache(u, uvCache, 0, 2);
        RasterizerUtils.swapCache(v, uvCache, 1, 2);
        RasterizerUtils.swapVector(u, 0, 1);
        RasterizerUtils.swapVector(v, 0, 1);
        RasterizerUtils.swapCache(u, uvCache, 0, 1);
        RasterizerUtils.swapCache(v, uvCache, 1, 1);
        drawTopTriangle();
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
        int du1 = FixedPointUtils.divide(this.u[1] - this.u[0], y2y1);
        int du2 = FixedPointUtils.divide(this.u[2] - this.u[0], y3y1);
        int dv1 = FixedPointUtils.divide(this.v[1] - this.v[0], y2y1);
        int dv2 = FixedPointUtils.divide(this.v[2] - this.v[0], y3y1);
        int dr1 = FixedPointUtils.divide(red[1] - red[0], y2y1);
        int dr2 = FixedPointUtils.divide(red[2] - red[0], y3y1);
        int dg1 = FixedPointUtils.divide(green[1] - green[0], y2y1);
        int dg2 = FixedPointUtils.divide(green[2] - green[0], y3y1);
        int db1 = FixedPointUtils.divide(blue[1] - blue[0], y2y1);
        int db2 = FixedPointUtils.divide(blue[2] - blue[0], y3y1);
        if(dx1 < dx2) {
        	int dxdx = dx2 - dx1;
        	dxdx = dxdx == 0 ? 1 : dxdx;
        	int dz = FixedPointUtils.divide(dz2 - dz1, dxdx);
        	int du = FixedPointUtils.divide(du2 - du1, dxdx);
        	int dv = FixedPointUtils.divide(dv2 - dv1, dxdx);
        	int dr = FixedPointUtils.divide(dr2 - dr1, dxdx);
        	int dg = FixedPointUtils.divide(dg2 - dg1, dxdx);
        	int db = FixedPointUtils.divide(db2 - db1, dxdx);
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
        	int dz = FixedPointUtils.divide(dz1 - dz2, dxdx);
        	int du = FixedPointUtils.divide(du1 - du2, dxdx);
        	int dv = FixedPointUtils.divide(dv1 - dv2, dxdx);
        	int dr = FixedPointUtils.divide(dr1 - dr2, dxdx);
        	int dg = FixedPointUtils.divide(dg1 - dg2, dxdx);
        	int db = FixedPointUtils.divide(db1 - db2, dxdx);
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
		int dx1 = FixedPointUtils.divide(location2[VECTOR_X] - location0[VECTOR_X], y3y1);
		int dx2 = FixedPointUtils.divide(location2[VECTOR_X] - location1[VECTOR_X], y3y2);
		int dz1 = FixedPointUtils.divide(location2[VECTOR_Z] - location0[VECTOR_Z], y3y1);
		int dz2 = FixedPointUtils.divide(location2[VECTOR_Z] - location1[VECTOR_Z], y3y2);
		int du1 = FixedPointUtils.divide(this.u[2] - this.u[0], y3y1);
		int du2 = FixedPointUtils.divide(this.u[2] - this.u[1], y3y2);
		int dv1 = FixedPointUtils.divide(this.v[2] - this.v[0], y3y1);
		int dv2 = FixedPointUtils.divide(this.v[2] - this.v[1], y3y2);
		int dr1 = FixedPointUtils.divide(red[2] - red[0], y3y1);
		int dr2 = FixedPointUtils.divide(red[2] - red[1], y3y2);
		int dg1 = FixedPointUtils.divide(green[2] - green[0], y3y1);
		int dg2 = FixedPointUtils.divide(green[2] - green[1], y3y2);
		int db1 = FixedPointUtils.divide(blue[2] - blue[0], y3y1);
		int db2 = FixedPointUtils.divide(blue[2] - blue[1], y3y2);
		if (dx1 > dx2) {
			int dxdx = dx1 - dx2;
			dxdx = dxdx == 0 ? 1 : dxdx;
			int dz = FixedPointUtils.divide(dz1 - dz2, dxdx);
			int du = FixedPointUtils.divide(du1 - du2, dxdx);
			int dv = FixedPointUtils.divide(dv1 - dv2, dxdx);
			int dr = FixedPointUtils.divide(dr1 - dr2, dxdx);
			int dg = FixedPointUtils.divide(dg1 - dg2, dxdx);
			int db = FixedPointUtils.divide(db1 - db2, dxdx);
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
			int dz = FixedPointUtils.divide(dz2 - dz1, dxdx);
			int du = FixedPointUtils.divide(du2 - du1, dxdx);
			int dv = FixedPointUtils.divide(dv2 - dv1, dxdx);
			int dr = FixedPointUtils.divide(dr2 - dr1, dxdx);
			int dg = FixedPointUtils.divide(dg2 - dg1, dxdx);
			int db = FixedPointUtils.divide(db2 - db1, dxdx);
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
			fragment.getLocation()[VECTOR_X] = x1;
			fragment.getLocation()[VECTOR_Y] = y;
			oneByZ = DIVISION_ONE / (z >> INTERPOLATE_BIT);
			fragment.getLocation()[VECTOR_Z] = oneByZ;
			fragment.getUV()[VECTOR_X] = FixedPointUtils.multiply(u, oneByZ) >> INTERPOLATE_BIT_2;
			fragment.getUV()[VECTOR_Y] = FixedPointUtils.multiply(v, oneByZ) >> INTERPOLATE_BIT_2;
			cr = FixedPointUtils.multiply(r, oneByZ) >> INTERPOLATE_BIT_2;
			cg = FixedPointUtils.multiply(g, oneByZ) >> INTERPOLATE_BIT_2;
			cb = FixedPointUtils.multiply(b, oneByZ) >> INTERPOLATE_BIT_2;
			fragment.setLightColor(ColorUtils.toColor(cr, cg, cb));
			shader.fragment(fragment);
			z += dz;
			u += du;
			v += dv;
			r += dr;
			g += dg;
			b += db;
		}
	}
}
