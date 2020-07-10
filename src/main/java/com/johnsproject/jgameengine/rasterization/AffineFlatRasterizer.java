package com.johnsproject.jgameengine.rasterization;

import static com.johnsproject.jgameengine.util.FixedPointUtils.FP_BIT;
import static com.johnsproject.jgameengine.util.VectorUtils.VECTOR_X;
import static com.johnsproject.jgameengine.util.VectorUtils.VECTOR_Y;
import static com.johnsproject.jgameengine.util.VectorUtils.VECTOR_Z;

import com.johnsproject.jgameengine.model.Face;
import com.johnsproject.jgameengine.model.Texture;
import com.johnsproject.jgameengine.shading.Shader;
import com.johnsproject.jgameengine.util.FixedPointUtils;
import com.johnsproject.jgameengine.util.VectorUtils;

public class AffineFlatRasterizer extends FlatRasterizer {
	
	protected final int[] u;
	protected final int[] v;
	protected final int[] uv;
	protected final int[] uvCache;
	
	public AffineFlatRasterizer(Shader shader) {
		super(shader);
		u = VectorUtils.emptyVector();
		v = VectorUtils.emptyVector();
		uv = VectorUtils.emptyVector();
		uvCache = VectorUtils.emptyVector();
	}
	
	/**
	 * This method tells the rasterizer to draw the given {@link GeometryBuffer geometryBuffer}.
	 * This rasterizer draws a triangle using the x, y coordinates of each vertex of the geometryBuffer. 
	 * It uses linear interpolation to find out the z and the uv coordinate for each pixel.
	 * While rasterizing the geometryBuffer, for each pixel/fragment the {@link Shader#fragment} 
	 * method of this rasterizer's {@link Shader} will be called.
	 * 
	 * @param geometryBuffer
	 */
	public void affineDraw(Face face, Texture texture) {
		copyLocations(face);
		copyFrustum();
		if(isCulled())
			return;
		fragment.setLightColor(face.getLightColor());
		fragment.setMaterial(face.getMaterial());
		copyUV(face, texture);
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
	
	protected void copyUV(Face face, Texture texture) {
		int[] uv = face.getUV(0);
		u[0] = FixedPointUtils.multiply(uv[VECTOR_X], texture.getWidth());
		v[0] = FixedPointUtils.multiply(uv[VECTOR_Y], texture.getHeight());
		uv = face.getUV(1);
		u[1] = FixedPointUtils.multiply(uv[VECTOR_X], texture.getWidth());
		v[1] = FixedPointUtils.multiply(uv[VECTOR_Y], texture.getHeight());
		uv = face.getUV(2);
		u[2] = FixedPointUtils.multiply(uv[VECTOR_X], texture.getWidth());
		v[2] = FixedPointUtils.multiply(uv[VECTOR_Y], texture.getHeight());
	}
	
	protected void sortY() {
		if (location0[VECTOR_Y] > location1[VECTOR_Y]) {
			VectorUtils.swap(location0, location1);
			RasterizerUtils.swapVector(u, 0, 1);
			RasterizerUtils.swapVector(v, 0, 1);
		}
		if (location1[VECTOR_Y] > location2[VECTOR_Y]) {
			VectorUtils.swap(location1, location2);
			RasterizerUtils.swapVector(u, 2, 1);
			RasterizerUtils.swapVector(v, 2, 1);
		}
		if (location0[VECTOR_Y] > location1[VECTOR_Y]) {
			VectorUtils.swap(location0, location1);
			RasterizerUtils.swapVector(u, 0, 1);
			RasterizerUtils.swapVector(v, 0, 1);
		}
	}
	
	protected int splitTriangle() {
		int dy = super.splitTriangle();
        uvCache[VECTOR_X] = u[0] + FixedPointUtils.multiply(dy, u[2] - u[0]);
        uvCache[VECTOR_Y] = v[0] + FixedPointUtils.multiply(dy, v[2] - v[0]);
        return dy;
	}
	
	private void drawSplitedTriangle() {
		VectorUtils.swap(vectorCache, location2);
		RasterizerUtils.swapCache(u, uvCache, 0, 2);
		RasterizerUtils.swapCache(v, uvCache, 1, 2);
        drawBottomTriangle();
        VectorUtils.swap(vectorCache, location2);
        VectorUtils.swap(location0, location1);
        VectorUtils.swap(location1, vectorCache);
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
        int du1 = FixedPointUtils.divide(u[1] - u[0], y2y1);
        int du2 = FixedPointUtils.divide(u[2] - u[0], y3y1);
        int dv1 = FixedPointUtils.divide(v[1] - v[0], y2y1);
        int dv2 = FixedPointUtils.divide(v[2] - v[0], y3y1);
    	int x1 = xShifted;
        int x2 = xShifted;
        int z = location0[VECTOR_Z] << FP_BIT;
        int u = this.u[0] << FP_BIT;
        int v = this.v[0] << FP_BIT;
        if(dx1 < dx2) {
        	int dxdx = dx2 - dx1;
        	dxdx = dxdx == 0 ? 1 : dxdx;
        	int dz = FixedPointUtils.divide(dz2 - dz1, dxdx);
        	int du = FixedPointUtils.divide(du2 - du1, dxdx);
        	int dv = FixedPointUtils.divide(dv2 - dv1, dxdx);
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
		int x1 = xShifted;
		int x2 = xShifted;
		int z = location2[VECTOR_Z] << FP_BIT;
		int u = this.u[2] << FP_BIT;
		int v = this.v[2] << FP_BIT;
		if (dx1 > dx2) {
			int dxdx = dx1 - dx2;
			dxdx = dxdx == 0 ? 1 : dxdx;
			int dz = FixedPointUtils.divide(dz1 - dz2, dxdx);
			int du = FixedPointUtils.divide(du1 - du2, dxdx);
			int dv = FixedPointUtils.divide(dv1 - dv2, dxdx);
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
	
	private void drawScanline(int x1, int x2, int y, int z, int u, int v,int dz, int du, int dv) {
		x1 >>= FP_BIT;
		x2 >>= FP_BIT;
		for (; x1 <= x2; x1++) {
			fragment.getLocation()[VECTOR_X] = x1;
			fragment.getLocation()[VECTOR_Y] = y;
			fragment.getLocation()[VECTOR_Z] = z >> FP_BIT;
			fragment.getUV()[VECTOR_X] = u >> FP_BIT;
			fragment.getUV()[VECTOR_Y] = v >> FP_BIT;
			shader.fragment(fragment);
			z += dz;
			u += du;
			v += dv;
		}
	}
}
