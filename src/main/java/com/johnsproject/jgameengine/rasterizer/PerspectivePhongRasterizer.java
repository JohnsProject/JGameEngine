package com.johnsproject.jgameengine.rasterizer;

import static com.johnsproject.jgameengine.math.FixedPointMath.*;
import static com.johnsproject.jgameengine.math.VectorMath.*;

import com.johnsproject.jgameengine.math.FixedPointMath;
import com.johnsproject.jgameengine.math.VectorMath;
import com.johnsproject.jgameengine.model.Texture;
import com.johnsproject.jgameengine.shader.GeometryBuffer;
import com.johnsproject.jgameengine.shader.Shader;

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
	public void perspectiveDraw(GeometryBuffer geometryBuffer, Texture texture) {
		copyFrustum(this.cameraFrustum, shader.getShaderBuffer().getCamera().getRenderTargetPortedFrustum());
		VectorMath.copy(location0, geometryBuffer.getVertexBuffer(0).getLocation());
		VectorMath.copy(location1, geometryBuffer.getVertexBuffer(1).getLocation());
		VectorMath.copy(location2, geometryBuffer.getVertexBuffer(2).getLocation());
		if(cull()) {
			return;
		}
		setWorldLocation0(geometryBuffer.getVertexBuffer(0).getWorldLocation());
		setWorldLocation1(geometryBuffer.getVertexBuffer(1).getWorldLocation());
		setWorldLocation2(geometryBuffer.getVertexBuffer(2).getWorldLocation());
		setNormal0(geometryBuffer.getVertexBuffer(0).getWorldNormal());
		setNormal1(geometryBuffer.getVertexBuffer(1).getWorldNormal());
		setNormal2(geometryBuffer.getVertexBuffer(2).getWorldNormal());
		setUV0(geometryBuffer.getUV(0), texture);
		setUV1(geometryBuffer.getUV(1), texture);
		setUV2(geometryBuffer.getUV(2), texture);
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
			VectorMath.swap(location0, location1);
			swapVector(u, v, 0, 1);
			swapVector(worldX, worldY, worldZ, 0, 1);
			swapVector(normalX, normalY, normalZ, 0, 1);
		}
		if (location1[VECTOR_Y] > location2[VECTOR_Y]) {
			VectorMath.swap(location1, location2);
			swapVector(u, v, 2, 1);
			swapVector(worldX, worldY, worldZ, 2, 1);
			swapVector(normalX, normalY, normalZ, 2, 1);
		}
		if (location0[VECTOR_Y] > location1[VECTOR_Y]) {
			VectorMath.swap(location0, location1);
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
            int dy = FixedPointMath.divide(location1[VECTOR_Y] - location0[VECTOR_Y], location2[VECTOR_Y] - location0[VECTOR_Y]);
            int multiplier = location2[VECTOR_X] - location0[VECTOR_X];
            x += FixedPointMath.multiply(dy, multiplier);
            multiplier = location2[VECTOR_Z] - location0[VECTOR_Z];
            z += FixedPointMath.multiply(dy, multiplier);
            multiplier = u[2] - u[0];
            uvx += FixedPointMath.multiply(dy, multiplier);
            multiplier = v[2] - v[0];
            uvy += FixedPointMath.multiply(dy, multiplier);
            multiplier = worldX[2] - worldX[0];
            wx += FixedPointMath.multiply(dy, multiplier);
            multiplier = worldY[2] - worldY[0];
            wy += FixedPointMath.multiply(dy, multiplier);
            multiplier = worldZ[2] - worldZ[0];
            wz += FixedPointMath.multiply(dy, multiplier);
            multiplier = normalX[2] - normalX[0];
            nx += FixedPointMath.multiply(dy, multiplier);
            multiplier = normalY[2] - normalY[0];
            ny += FixedPointMath.multiply(dy, multiplier);
            multiplier = normalZ[2] - normalZ[0];
            nz += FixedPointMath.multiply(dy, multiplier);
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
            VectorMath.swap(vectorCache, location2);
            swapCache(u, v, uvCache, 2);
            swapCache(worldX, worldY, worldZ, worldCache, 2);
            swapCache(normalX, normalY, normalZ, normalCache, 2);
            drawBottomTriangle(cameraFrustum);
            VectorMath.swap(vectorCache, location2);
            VectorMath.swap(location0, location1);
            VectorMath.swap(location1, vectorCache);
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
        int dx1 = FixedPointMath.divide(location1[VECTOR_X] - location0[VECTOR_X], y2y1);
        int dx2 = FixedPointMath.divide(location2[VECTOR_X] - location0[VECTOR_X], y3y1);
        int dz1 = FixedPointMath.divide(location1[VECTOR_Z] - location0[VECTOR_Z], y2y1);
        int dz2 = FixedPointMath.divide(location2[VECTOR_Z] - location0[VECTOR_Z], y3y1);
        int du1 = FixedPointMath.divide(this.u[1] - this.u[0], y2y1);
        int du2 = FixedPointMath.divide(this.u[2] - this.u[0], y3y1);
        int dv1 = FixedPointMath.divide(this.v[1] - this.v[0], y2y1);
        int dv2 = FixedPointMath.divide(this.v[2] - this.v[0], y3y1);
        int dwx1 = FixedPointMath.divide(worldX[1] - worldX[0], y2y1);
        int dwx2 = FixedPointMath.divide(worldX[2] - worldX[0], y3y1);
        int dwy1 = FixedPointMath.divide(worldY[1] - worldY[0], y2y1);
        int dwy2 = FixedPointMath.divide(worldY[2] - worldY[0], y3y1);
        int dwz1 = FixedPointMath.divide(worldZ[1] - worldZ[0], y2y1);
        int dwz2 = FixedPointMath.divide(worldZ[2] - worldZ[0], y3y1);
        int dnx1 = FixedPointMath.divide(normalX[1] - normalX[0], y2y1);
        int dnx2 = FixedPointMath.divide(normalX[2] - normalX[0], y3y1);
        int dny1 = FixedPointMath.divide(normalY[1] - normalY[0], y2y1);
        int dny2 = FixedPointMath.divide(normalY[2] - normalY[0], y3y1);
        int dnz1 = FixedPointMath.divide(normalZ[1] - normalZ[0], y2y1);
        int dnz2 = FixedPointMath.divide(normalZ[2] - normalZ[0], y3y1);
        if(dx1 < dx2) {
        	int dxdx = dx2 - dx1;
        	dxdx = dxdx == 0 ? 1 : dxdx;
        	int dz = FixedPointMath.divide(dz2 - dz1, dxdx);
        	int du = FixedPointMath.divide(du2 - du1, dxdx);
        	int dv = FixedPointMath.divide(dv2 - dv1, dxdx);
        	int dwx = FixedPointMath.divide(dwx2 - dwx1, dxdx);
        	int dwy = FixedPointMath.divide(dwy2 - dwy1, dxdx);
        	int dwz = FixedPointMath.divide(dwz2 - dwz1, dxdx);
        	int dnx = FixedPointMath.divide(dnx2 - dnx1, dxdx);
        	int dny = FixedPointMath.divide(dny2 - dny1, dxdx);
        	int dnz = FixedPointMath.divide(dnz2 - dnz1, dxdx);
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
        	int dz = FixedPointMath.divide(dz1 - dz2, dxdx);
        	int du = FixedPointMath.divide(du1 - du2, dxdx);
        	int dv = FixedPointMath.divide(dv1 - dv2, dxdx);
        	int dwx = FixedPointMath.divide(dwx1 - dwx2, dxdx);
        	int dwy = FixedPointMath.divide(dwy1 - dwy2, dxdx);
        	int dwz = FixedPointMath.divide(dwz1 - dwz2, dxdx);
        	int dnx = FixedPointMath.divide(dnx1 - dnx2, dxdx);
        	int dny = FixedPointMath.divide(dny1 - dny2, dxdx);
        	int dnz = FixedPointMath.divide(dnz1 - dnz2, dxdx);
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
		int dx1 = FixedPointMath.divide(location2[VECTOR_X] - location0[VECTOR_X], y3y1);
		int dx2 = FixedPointMath.divide(location2[VECTOR_X] - location1[VECTOR_X], y3y2);
		int dz1 = FixedPointMath.divide(location2[VECTOR_Z] - location0[VECTOR_Z], y3y1);
		int dz2 = FixedPointMath.divide(location2[VECTOR_Z] - location1[VECTOR_Z], y3y2);
		int du1 = FixedPointMath.divide(this.u[2] - this.u[0], y3y1);
		int du2 = FixedPointMath.divide(this.u[2] - this.u[1], y3y2);
		int dv1 = FixedPointMath.divide(this.v[2] - this.v[0], y3y1);
		int dv2 = FixedPointMath.divide(this.v[2] - this.v[1], y3y2);
		int dwx1 = FixedPointMath.divide(worldX[2] - worldX[0], y3y1);
		int dwx2 = FixedPointMath.divide(worldX[2] - worldX[1], y3y2);
		int dwy1 = FixedPointMath.divide(worldY[2] - worldY[0], y3y1);
		int dwy2 = FixedPointMath.divide(worldY[2] - worldY[1], y3y2);
		int dwz1 = FixedPointMath.divide(worldZ[2] - worldZ[0], y3y1);
		int dwz2 = FixedPointMath.divide(worldZ[2] - worldZ[1], y3y2);
		int dnx1 = FixedPointMath.divide(normalX[2] - normalX[0], y3y1);
		int dnx2 = FixedPointMath.divide(normalX[2] - normalX[1], y3y2);
		int dny1 = FixedPointMath.divide(normalY[2] - normalY[0], y3y1);
		int dny2 = FixedPointMath.divide(normalY[2] - normalY[1], y3y2);
		int dnz1 = FixedPointMath.divide(normalZ[2] - normalZ[0], y3y1);
		int dnz2 = FixedPointMath.divide(normalZ[2] - normalZ[1], y3y2);
		if (dx1 > dx2) {
			int dxdx = dx1 - dx2;
			dxdx = dxdx == 0 ? 1 : dxdx;
			int dz = FixedPointMath.divide(dz1 - dz2, dxdx);
			int du = FixedPointMath.divide(du1 - du2, dxdx);
			int dv = FixedPointMath.divide(dv1 - dv2, dxdx);
			int dwx = FixedPointMath.divide(dwx1 - dwx2, dxdx);
			int dwy = FixedPointMath.divide(dwy1 - dwy2, dxdx);
			int dwz = FixedPointMath.divide(dwz1 - dwz2, dxdx);
			int dnx = FixedPointMath.divide(dnx1 - dnx2, dxdx);
			int dny = FixedPointMath.divide(dny1 - dny2, dxdx);
			int dnz = FixedPointMath.divide(dnz1 - dnz2, dxdx);
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
			int dz = FixedPointMath.divide(dz2 - dz1, dxdx);
			int du = FixedPointMath.divide(du2 - du1, dxdx);
			int dv = FixedPointMath.divide(dv2 - dv1, dxdx);
			int dwx = FixedPointMath.divide(dwx2 - dwx1, dxdx);
			int dwy = FixedPointMath.divide(dwy2 - dwy1, dxdx);
			int dwz = FixedPointMath.divide(dwz2 - dwz1, dxdx);
			int dnx = FixedPointMath.divide(dnx2 - dnx1, dxdx);
			int dny = FixedPointMath.divide(dny2 - dny1, dxdx);
			int dnz = FixedPointMath.divide(dnz2 - dnz1, dxdx);
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
			fragmentBuffer.getLocation()[VECTOR_X] = x1;
			fragmentBuffer.getLocation()[VECTOR_Y] = y;
			oneByZ = DIVISION_ONE / (z >> INTERPOLATE_BIT);
			fragmentBuffer.getLocation()[VECTOR_Z] = oneByZ;
			fragmentBuffer.getUV()[VECTOR_X] = FixedPointMath.multiply(u, oneByZ) >> INTERPOLATE_BIT_2;
			fragmentBuffer.getUV()[VECTOR_Y] = FixedPointMath.multiply(v, oneByZ) >> INTERPOLATE_BIT_2;
            fragmentBuffer.getWorldLocation()[VECTOR_X] = FixedPointMath.multiply(wx, oneByZ);
            fragmentBuffer.getWorldLocation()[VECTOR_Y] = FixedPointMath.multiply(wy, oneByZ);
            fragmentBuffer.getWorldLocation()[VECTOR_Z] = FixedPointMath.multiply(wz, oneByZ);
            fragmentBuffer.getWorldNormal()[VECTOR_X] = FixedPointMath.multiply(nx, oneByZ);
            fragmentBuffer.getWorldNormal()[VECTOR_Y] = FixedPointMath.multiply(ny, oneByZ);
            fragmentBuffer.getWorldNormal()[VECTOR_Z] = FixedPointMath.multiply(nz, oneByZ);
			shader.fragment(fragmentBuffer);
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
