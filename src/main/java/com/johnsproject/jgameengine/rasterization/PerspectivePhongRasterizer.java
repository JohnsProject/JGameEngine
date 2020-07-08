package com.johnsproject.jgameengine.rasterization;

import static com.johnsproject.jgameengine.util.FixedPointUtils.FP_BIT;
import static com.johnsproject.jgameengine.util.FixedPointUtils.FP_ONE;
import static com.johnsproject.jgameengine.util.VectorUtils.VECTOR_X;
import static com.johnsproject.jgameengine.util.VectorUtils.VECTOR_Y;
import static com.johnsproject.jgameengine.util.VectorUtils.VECTOR_Z;

import com.johnsproject.jgameengine.model.Face;
import com.johnsproject.jgameengine.model.Texture;
import com.johnsproject.jgameengine.shading.Shader;
import com.johnsproject.jgameengine.util.FixedPointUtils;
import com.johnsproject.jgameengine.util.VectorUtils;

public class PerspectivePhongRasterizer extends AffinePhongRasterizer {
	
	public PerspectivePhongRasterizer(Shader shader) {
		super(shader);
	}
	
	/**
	 * This method tells the rasterizer to draw the given {@link GeometryBuffer geometryBuffer}.
	 * This rasterizer draws a triangle using the x, y coordinates of each vertex of the geometryBuffer. 
	 * It uses perspective interpolation to find out the z and the uv coordinate, as well as the world coordinates and the normals for each pixel.
	 * While rasterizing the geometryBuffer, for each pixel/fragment the {@link Shader#fragment} 
	 * method of this rasterizer's {@link Shader} will be called.
	 * 
	 * @param geometryBuffer
	 */	
	public void perspectiveDraw(Face face, Texture texture) {
		copyFrustum(shader.getShaderBuffer().getCamera().getFrustum());
		VectorUtils.copy(location0, face.getVertex(0).getLocation());
		VectorUtils.copy(location1, face.getVertex(1).getLocation());
		VectorUtils.copy(location2, face.getVertex(2).getLocation());
		if(cull()) {
			return;
		}
		fragment.setMaterial(face.getMaterial());
		setWorldLocation0(face.getVertex(0).getWorldLocation());
		setWorldLocation1(face.getVertex(1).getWorldLocation());
		setWorldLocation2(face.getVertex(2).getWorldLocation());
		setNormal0(face.getVertex(0).getWorldNormal());
		setNormal1(face.getVertex(1).getWorldNormal());
		setNormal2(face.getVertex(2).getWorldNormal());
		setUV0(face.getUV(0), texture);
		setUV1(face.getUV(1), texture);
		setUV2(face.getUV(2), texture);
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
			VectorUtils.swap(location0, location1);
			swapVector(u, v, 0, 1);
			swapVector(worldX, worldY, worldZ, 0, 1);
			swapVector(normalX, normalY, normalZ, 0, 1);
		}
		if (location1[VECTOR_Y] > location2[VECTOR_Y]) {
			VectorUtils.swap(location1, location2);
			swapVector(u, v, 2, 1);
			swapVector(worldX, worldY, worldZ, 2, 1);
			swapVector(normalX, normalY, normalZ, 2, 1);
		}
		if (location0[VECTOR_Y] > location1[VECTOR_Y]) {
			VectorUtils.swap(location0, location1);
			swapVector(u, v, 0, 1);
			swapVector(worldX, worldY, worldZ, 0, 1);
			swapVector(normalX, normalY, normalZ, 0, 1);
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
            int wx = worldX[0];
            int wy = worldY[0];
            int wz = worldZ[0];
            int nx = normalX[0];
            int ny = normalY[0];
            int nz = normalZ[0];
            int dy = FixedPointUtils.divide(location1[VECTOR_Y] - location0[VECTOR_Y], location2[VECTOR_Y] - location0[VECTOR_Y]);
            int multiplier = location2[VECTOR_X] - location0[VECTOR_X];
            x += FixedPointUtils.multiply(dy, multiplier);
            multiplier = location2[VECTOR_Z] - location0[VECTOR_Z];
            z += FixedPointUtils.multiply(dy, multiplier);
            multiplier = u[2] - u[0];
            uvx += FixedPointUtils.multiply(dy, multiplier);
            multiplier = v[2] - v[0];
            uvy += FixedPointUtils.multiply(dy, multiplier);
            multiplier = worldX[2] - worldX[0];
            wx += FixedPointUtils.multiply(dy, multiplier);
            multiplier = worldY[2] - worldY[0];
            wy += FixedPointUtils.multiply(dy, multiplier);
            multiplier = worldZ[2] - worldZ[0];
            wz += FixedPointUtils.multiply(dy, multiplier);
            multiplier = normalX[2] - normalX[0];
            nx += FixedPointUtils.multiply(dy, multiplier);
            multiplier = normalY[2] - normalY[0];
            ny += FixedPointUtils.multiply(dy, multiplier);
            multiplier = normalZ[2] - normalZ[0];
            nz += FixedPointUtils.multiply(dy, multiplier);
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
            VectorUtils.swap(vectorCache, location2);
            swapCache(u, v, uvCache, 2);
            swapCache(worldX, worldY, worldZ, worldCache, 2);
            swapCache(normalX, normalY, normalZ, normalCache, 2);
            drawBottomTriangle();
            VectorUtils.swap(vectorCache, location2);
            VectorUtils.swap(location0, location1);
            VectorUtils.swap(location1, vectorCache);
            swapCache(u, v, uvCache, 2);
            swapVector(u, v, 0, 1);
            swapCache(u, v, uvCache, 1);
            swapCache(worldX, worldY, worldZ, worldCache, 2);
            swapVector(worldX, worldY, worldZ, 0, 1);
            swapCache(worldX, worldY, worldZ, worldCache, 1);
            swapCache(normalX, normalY, normalZ, normalCache, 2);
            swapVector(normalX, normalY, normalZ, 0, 1);
            swapCache(normalX, normalY, normalZ, normalCache, 1);
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
        int du1 = FixedPointUtils.divide(this.u[1] - this.u[0], y2y1);
        int du2 = FixedPointUtils.divide(this.u[2] - this.u[0], y3y1);
        int dv1 = FixedPointUtils.divide(this.v[1] - this.v[0], y2y1);
        int dv2 = FixedPointUtils.divide(this.v[2] - this.v[0], y3y1);
        int dwx1 = FixedPointUtils.divide(worldX[1] - worldX[0], y2y1);
        int dwx2 = FixedPointUtils.divide(worldX[2] - worldX[0], y3y1);
        int dwy1 = FixedPointUtils.divide(worldY[1] - worldY[0], y2y1);
        int dwy2 = FixedPointUtils.divide(worldY[2] - worldY[0], y3y1);
        int dwz1 = FixedPointUtils.divide(worldZ[1] - worldZ[0], y2y1);
        int dwz2 = FixedPointUtils.divide(worldZ[2] - worldZ[0], y3y1);
        int dnx1 = FixedPointUtils.divide(normalX[1] - normalX[0], y2y1);
        int dnx2 = FixedPointUtils.divide(normalX[2] - normalX[0], y3y1);
        int dny1 = FixedPointUtils.divide(normalY[1] - normalY[0], y2y1);
        int dny2 = FixedPointUtils.divide(normalY[2] - normalY[0], y3y1);
        int dnz1 = FixedPointUtils.divide(normalZ[1] - normalZ[0], y2y1);
        int dnz2 = FixedPointUtils.divide(normalZ[2] - normalZ[0], y3y1);
        if(dx1 < dx2) {
        	int dxdx = dx2 - dx1;
        	dxdx = dxdx == 0 ? 1 : dxdx;
        	int dz = FixedPointUtils.divide(dz2 - dz1, dxdx);
        	int du = FixedPointUtils.divide(du2 - du1, dxdx);
        	int dv = FixedPointUtils.divide(dv2 - dv1, dxdx);
        	int dwx = FixedPointUtils.divide(dwx2 - dwx1, dxdx);
        	int dwy = FixedPointUtils.divide(dwy2 - dwy1, dxdx);
        	int dwz = FixedPointUtils.divide(dwz2 - dwz1, dxdx);
        	int dnx = FixedPointUtils.divide(dnx2 - dnx1, dxdx);
        	int dny = FixedPointUtils.divide(dny2 - dny1, dxdx);
        	int dnz = FixedPointUtils.divide(dnz2 - dnz1, dxdx);
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
	        	drawScanline(x1, x2, y, z, u, v, wx, wy, wz, nx, ny, nz, dz, du, dv, dwx, dwy, dwz, dnx, dny, dnz);
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
        	int dz = FixedPointUtils.divide(dz1 - dz2, dxdx);
        	int du = FixedPointUtils.divide(du1 - du2, dxdx);
        	int dv = FixedPointUtils.divide(dv1 - dv2, dxdx);
        	int dwx = FixedPointUtils.divide(dwx1 - dwx2, dxdx);
        	int dwy = FixedPointUtils.divide(dwy1 - dwy2, dxdx);
        	int dwz = FixedPointUtils.divide(dwz1 - dwz2, dxdx);
        	int dnx = FixedPointUtils.divide(dnx1 - dnx2, dxdx);
        	int dny = FixedPointUtils.divide(dny1 - dny2, dxdx);
        	int dnz = FixedPointUtils.divide(dnz1 - dnz2, dxdx);
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
        		drawScanline(x1, x2, y, z, u, v, wx, wy, wz, nx, ny, nz, dz, du, dv, dwx, dwy, dwz, dnx, dny, dnz);
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
		int dwx1 = FixedPointUtils.divide(worldX[2] - worldX[0], y3y1);
		int dwx2 = FixedPointUtils.divide(worldX[2] - worldX[1], y3y2);
		int dwy1 = FixedPointUtils.divide(worldY[2] - worldY[0], y3y1);
		int dwy2 = FixedPointUtils.divide(worldY[2] - worldY[1], y3y2);
		int dwz1 = FixedPointUtils.divide(worldZ[2] - worldZ[0], y3y1);
		int dwz2 = FixedPointUtils.divide(worldZ[2] - worldZ[1], y3y2);
		int dnx1 = FixedPointUtils.divide(normalX[2] - normalX[0], y3y1);
		int dnx2 = FixedPointUtils.divide(normalX[2] - normalX[1], y3y2);
		int dny1 = FixedPointUtils.divide(normalY[2] - normalY[0], y3y1);
		int dny2 = FixedPointUtils.divide(normalY[2] - normalY[1], y3y2);
		int dnz1 = FixedPointUtils.divide(normalZ[2] - normalZ[0], y3y1);
		int dnz2 = FixedPointUtils.divide(normalZ[2] - normalZ[1], y3y2);
		if (dx1 > dx2) {
			int dxdx = dx1 - dx2;
			dxdx = dxdx == 0 ? 1 : dxdx;
			int dz = FixedPointUtils.divide(dz1 - dz2, dxdx);
			int du = FixedPointUtils.divide(du1 - du2, dxdx);
			int dv = FixedPointUtils.divide(dv1 - dv2, dxdx);
			int dwx = FixedPointUtils.divide(dwx1 - dwx2, dxdx);
			int dwy = FixedPointUtils.divide(dwy1 - dwy2, dxdx);
			int dwz = FixedPointUtils.divide(dwz1 - dwz2, dxdx);
			int dnx = FixedPointUtils.divide(dnx1 - dnx2, dxdx);
			int dny = FixedPointUtils.divide(dny1 - dny2, dxdx);
			int dnz = FixedPointUtils.divide(dnz1 - dnz2, dxdx);
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
	        	drawScanline(x1, x2, y, z, u, v, wx, wy, wz, nx, ny, nz, dz, du, dv, dwx, dwy, dwz, dnx, dny, dnz);
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
			int dz = FixedPointUtils.divide(dz2 - dz1, dxdx);
			int du = FixedPointUtils.divide(du2 - du1, dxdx);
			int dv = FixedPointUtils.divide(dv2 - dv1, dxdx);
			int dwx = FixedPointUtils.divide(dwx2 - dwx1, dxdx);
			int dwy = FixedPointUtils.divide(dwy2 - dwy1, dxdx);
			int dwz = FixedPointUtils.divide(dwz2 - dwz1, dxdx);
			int dnx = FixedPointUtils.divide(dnx2 - dnx1, dxdx);
			int dny = FixedPointUtils.divide(dny2 - dny1, dxdx);
			int dnz = FixedPointUtils.divide(dnz2 - dnz1, dxdx);
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
	        	drawScanline(x1, x2, y, z, u, v, wx, wy, wz, nx, ny, nz, dz, du, dv, dwx, dwy, dwz, dnx, dny, dnz);
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
							int dz, int du, int dv, int dwx, int dwy, int dwz, int dnx, int dny, int dnz) {
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
            fragment.getWorldLocation()[VECTOR_X] = FixedPointUtils.multiply(wx, oneByZ);
            fragment.getWorldLocation()[VECTOR_Y] = FixedPointUtils.multiply(wy, oneByZ);
            fragment.getWorldLocation()[VECTOR_Z] = FixedPointUtils.multiply(wz, oneByZ);
            fragment.getWorldNormal()[VECTOR_X] = FixedPointUtils.multiply(nx, oneByZ);
            fragment.getWorldNormal()[VECTOR_Y] = FixedPointUtils.multiply(ny, oneByZ);
            fragment.getWorldNormal()[VECTOR_Z] = FixedPointUtils.multiply(nz, oneByZ);
			shader.fragment(fragment);
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
