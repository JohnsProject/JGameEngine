package com.johnsproject.jgameengine.rasterization;

import static com.johnsproject.jgameengine.util.FixedPointUtils.FP_BIT;
import static com.johnsproject.jgameengine.util.VectorUtils.VECTOR_X;
import static com.johnsproject.jgameengine.util.VectorUtils.VECTOR_Y;
import static com.johnsproject.jgameengine.util.VectorUtils.VECTOR_Z;

import com.johnsproject.jgameengine.model.Face;
import com.johnsproject.jgameengine.shading.Shader;
import com.johnsproject.jgameengine.util.FixedPointUtils;
import com.johnsproject.jgameengine.util.VectorUtils;

public class PhongRasterizer extends FlatRasterizer {
	
//	protected static final byte INTERPOLATE_BIT = 5;
//	// in phong rasterizer it's needed to decrease precision or a overflow will happen.
//	public static final byte FP_MINUS_INTERPOLATE_BIT = FP_BIT - INTERPOLATE_BIT;
	
	protected final int[] worldX;
	protected final int[] worldY;
	protected final int[] worldZ;
	protected final int[] worldLocation;
	protected final int[] worldCache;
	protected final int[] normalX;
	protected final int[] normalY;
	protected final int[] normalZ;
	protected final int[] normal;
	protected final int[] normalCache;
	
	public PhongRasterizer(Shader shader) {
		super(shader);
		worldX = VectorUtils.emptyVector();
		worldY = VectorUtils.emptyVector();
		worldZ = VectorUtils.emptyVector();
		worldLocation = VectorUtils.emptyVector();
		worldCache = VectorUtils.emptyVector();
		normalX = VectorUtils.emptyVector();
		normalY = VectorUtils.emptyVector();
		normalZ = VectorUtils.emptyVector();
		normal = VectorUtils.emptyVector();
		normalCache = VectorUtils.emptyVector();
	}

	/**
	 * This method tells the rasterizer to draw the given {@link GeometryBuffer geometryBuffer}.
	 * This rasterizer draws a triangle using the x, y coordinates of each vertex of the geometryBuffer. 
	 * It uses linear interpolation to find out the z coordinate, the world coordinates and the normals for each pixel.
	 * While rasterizing the geometryBuffer, for each pixel/fragment the {@link Shader#fragment} 
	 * method of this rasterizer's {@link Shader} will be called.
	 * 
	 * @param geometryBuffer
	 */
	public void draw(Face face) {
		copyLocations(face);
		copyFrustum();
		if(isCulled())
			return;
		fragment.setMaterial(face.getMaterial());
		copyWorldLocation(face);
		copyWorldNormal(face);
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
	
	protected void copyWorldLocation(Face face) {
		copyWorldLocation(face, 0);
		copyWorldLocation(face, 1);
		copyWorldLocation(face, 2);
	}
	
	private void copyWorldLocation(Face face, int index) {
		int[] location = face.getVertex(index).getWorldLocation();
		worldX[index] = location[VECTOR_X];
		worldY[index] = location[VECTOR_Y];
		worldZ[index] = location[VECTOR_Z];
	}
	
	protected void copyWorldNormal(Face face) {
		copyWorldNormal(face, 0);
		copyWorldNormal(face, 1);
		copyWorldNormal(face, 2);
	}
	
	private void copyWorldNormal(Face face, int index) {
		int[] normal = face.getVertex(index).getWorldNormal();
		normalX[index] = normal[VECTOR_X];
		normalY[index] = normal[VECTOR_Y];
		normalZ[index] = normal[VECTOR_Z];
	}
	
	private void sortY() {
		if (location0[VECTOR_Y] > location1[VECTOR_Y]) {
			VectorUtils.swap(location0, location1);
			RasterizerUtils.swapVector(worldX, 0, 1);
			RasterizerUtils.swapVector(worldY, 0, 1);
			RasterizerUtils.swapVector(worldZ, 0, 1);
			RasterizerUtils.swapVector(normalX, 0, 1);
			RasterizerUtils.swapVector(normalY, 0, 1);
			RasterizerUtils.swapVector(normalZ, 0, 1);
		}
		if (location1[VECTOR_Y] > location2[VECTOR_Y]) {
			VectorUtils.swap(location1, location2);
			RasterizerUtils.swapVector(worldX, 2, 1);
			RasterizerUtils.swapVector(worldY, 2, 1);
			RasterizerUtils.swapVector(worldZ, 2, 1);
			RasterizerUtils.swapVector(normalX, 2, 1);
			RasterizerUtils.swapVector(normalY, 2, 1);
			RasterizerUtils.swapVector(normalZ, 2, 1);
		}
		if (location0[VECTOR_Y] > location1[VECTOR_Y]) {
			VectorUtils.swap(location0, location1);
			RasterizerUtils.swapVector(worldX, 0, 1);
			RasterizerUtils.swapVector(worldY, 0, 1);
			RasterizerUtils.swapVector(worldZ, 0, 1);
			RasterizerUtils.swapVector(normalX, 0, 1);
			RasterizerUtils.swapVector(normalY, 0, 1);
			RasterizerUtils.swapVector(normalZ, 0, 1);
		}
	}
	
	protected int splitTriangle() {
        int dy = super.splitTriangle();
        worldCache[VECTOR_X] = worldX[0] + FixedPointUtils.multiply(dy, worldX[2] - worldX[0]);
        worldCache[VECTOR_Y] = worldY[0] + FixedPointUtils.multiply(dy, worldY[2] - worldY[0]);
        worldCache[VECTOR_Z] = worldZ[0] + FixedPointUtils.multiply(dy, worldZ[2] - worldZ[0]);
        normalCache[VECTOR_X] = normalX[0] + FixedPointUtils.multiply(dy, normalX[2] - normalX[0]);
        normalCache[VECTOR_Y] = normalY[0] + FixedPointUtils.multiply(dy, normalY[2] - normalY[0]);
        normalCache[VECTOR_Z] = normalZ[0] + FixedPointUtils.multiply(dy, normalZ[2] - normalZ[0]);
        return dy;
	}
	
	private void drawSplitedTriangle() {
		VectorUtils.swap(vectorCache, location2);
        RasterizerUtils.swapCache(worldX, worldCache, 0, 2);
        RasterizerUtils.swapCache(worldY, worldCache, 1, 2);
        RasterizerUtils.swapCache(worldZ, worldCache, 2, 2);
        RasterizerUtils.swapCache(normalX, normalCache, 0, 2);
        RasterizerUtils.swapCache(normalY, normalCache, 1, 2);
        RasterizerUtils.swapCache(normalZ, normalCache, 2, 2);
        drawBottomTriangle();
        VectorUtils.swap(vectorCache, location2);
        VectorUtils.swap(location0, location1);
        VectorUtils.swap(location1, vectorCache);
        RasterizerUtils.swapCache(worldX, worldCache, 0, 2);
        RasterizerUtils.swapCache(worldY, worldCache, 1, 2);
        RasterizerUtils.swapCache(worldZ, worldCache, 2, 2);
        RasterizerUtils.swapVector(worldX, 0, 1);
        RasterizerUtils.swapVector(worldY, 0, 1);
        RasterizerUtils.swapVector(worldZ, 0, 1);
        RasterizerUtils.swapCache(worldX, worldCache, 0, 1);
        RasterizerUtils.swapCache(worldY, worldCache, 1, 1);
        RasterizerUtils.swapCache(worldZ, worldCache, 2, 1);
        RasterizerUtils.swapCache(normalX, normalCache, 0, 2);
        RasterizerUtils.swapCache(normalY, normalCache, 1, 2);
        RasterizerUtils.swapCache(normalZ, normalCache, 2, 2);
        RasterizerUtils.swapVector(normalX, 0, 1);
        RasterizerUtils.swapVector(normalY, 0, 1);
        RasterizerUtils.swapVector(normalZ, 0, 1);
        RasterizerUtils.swapCache(normalX, normalCache, 0, 1);
        RasterizerUtils.swapCache(normalY, normalCache, 1, 1);
        RasterizerUtils.swapCache(normalZ, normalCache, 2, 1);
        drawTopTriangle();
	}
	
	private void drawBottomTriangle() {
		int xShifted = location0[VECTOR_X] << FP_BIT;
		int y2y1 = location1[VECTOR_Y] - location0[VECTOR_Y];
		int y3y1 = location1[VECTOR_Y] - location0[VECTOR_Y];
		y2y1 = y2y1 == 0 ? 1 : y2y1;
		y3y1 = y3y1 == 0 ? 1 : y3y1;
		final int y2y1Shifted = y2y1 << FP_BIT;
		final int y3y1Shifted = y3y1 << FP_BIT;
        int dx1 = FixedPointUtils.divide(location1[VECTOR_X] - location0[VECTOR_X], y2y1);
        int dx2 = FixedPointUtils.divide(location2[VECTOR_X] - location0[VECTOR_X], y3y1);
        int dz1 = FixedPointUtils.divide(location1[VECTOR_Z] - location0[VECTOR_Z], y2y1);
        int dz2 = FixedPointUtils.divide(location2[VECTOR_Z] - location0[VECTOR_Z], y3y1);
        int dwx1 = FixedPointUtils.divide(worldX[1] - worldX[0], y2y1Shifted);
        int dwx2 = FixedPointUtils.divide(worldX[2] - worldX[0], y3y1Shifted);
        int dwy1 = FixedPointUtils.divide(worldY[1] - worldY[0], y2y1Shifted);
        int dwy2 = FixedPointUtils.divide(worldY[2] - worldY[0], y3y1Shifted);
        int dwz1 = FixedPointUtils.divide(worldZ[1] - worldZ[0], y2y1Shifted);
        int dwz2 = FixedPointUtils.divide(worldZ[2] - worldZ[0], y3y1Shifted);
        int dnx1 = FixedPointUtils.divide(normalX[1] - normalX[0], y2y1Shifted);
        int dnx2 = FixedPointUtils.divide(normalX[2] - normalX[0], y3y1Shifted);
        int dny1 = FixedPointUtils.divide(normalY[1] - normalY[0], y2y1Shifted);
        int dny2 = FixedPointUtils.divide(normalY[2] - normalY[0], y3y1Shifted);
        int dnz1 = FixedPointUtils.divide(normalZ[1] - normalZ[0], y2y1Shifted);
        int dnz2 = FixedPointUtils.divide(normalZ[2] - normalZ[0], y3y1Shifted);
    	int x1 = xShifted;
        int x2 = xShifted;
        int z = location0[VECTOR_Z] << FP_BIT;
        int wx = worldX[0];
        int wy = worldY[0];
        int wz = worldZ[0];
        int nx = normalX[0];
        int ny = normalY[0];
        int nz = normalZ[0];
        if(dx1 < dx2) {
        	int dxdx = dx2 - dx1;
        	dxdx = dxdx == 0 ? 1 : dxdx;
        	int dz = FixedPointUtils.divide(dz2 - dz1, dxdx);
        	int dwx = FixedPointUtils.divide(dwx2 - dwx1, dxdx);
        	int dwy = FixedPointUtils.divide(dwy2 - dwy1, dxdx);
        	int dwz = FixedPointUtils.divide(dwz2 - dwz1, dxdx);
        	int dnx = FixedPointUtils.divide(dnx2 - dnx1, dxdx);
        	int dny = FixedPointUtils.divide(dny2 - dny1, dxdx);
        	int dnz = FixedPointUtils.divide(dnz2 - dnz1, dxdx);
	        for (int y = location0[VECTOR_Y]; y <= location1[VECTOR_Y]; y++) {
	        	drawScanline(x1, x2, y, z, wx, wy, wz, nx, ny, nz, dz, dwx, dwy, dwz, dnx, dny, dnz);
	            x1 += dx1;
	            x2 += dx2;
	            z += dz1;
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
        	int dwx = FixedPointUtils.divide(dwx1 - dwx2, dxdx);
        	int dwy = FixedPointUtils.divide(dwy1 - dwy2, dxdx);
        	int dwz = FixedPointUtils.divide(dwz1 - dwz2, dxdx);
        	int dnx = FixedPointUtils.divide(dnx1 - dnx2, dxdx);
        	int dny = FixedPointUtils.divide(dny1 - dny2, dxdx);
        	int dnz = FixedPointUtils.divide(dnz1 - dnz2, dxdx);
        	for (int y = location0[VECTOR_Y]; y <= location1[VECTOR_Y]; y++) {
        		drawScanline(x1, x2, y, z, wx, wy, wz, nx, ny, nz, dz, dwx, dwy, dwz, dnx, dny, dnz);
	            x1 += dx2;
	            x2 += dx1;
	            z += dz2;
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
		final int y3y1Shifted = y3y1 << FP_BIT;
		final int y3y2Shifted = y3y2 << FP_BIT;
		int dx1 = FixedPointUtils.divide(location2[VECTOR_X] - location0[VECTOR_X], y3y1);
		int dx2 = FixedPointUtils.divide(location2[VECTOR_X] - location1[VECTOR_X], y3y2);
		int dz1 = FixedPointUtils.divide(location2[VECTOR_Z] - location0[VECTOR_Z], y3y1);
		int dz2 = FixedPointUtils.divide(location2[VECTOR_Z] - location1[VECTOR_Z], y3y2);
		int dwx1 = FixedPointUtils.divide(worldX[2] - worldX[0], y3y1Shifted);
		int dwx2 = FixedPointUtils.divide(worldX[2] - worldX[1], y3y2Shifted);
		int dwy1 = FixedPointUtils.divide(worldY[2] - worldY[0], y3y1Shifted);
		int dwy2 = FixedPointUtils.divide(worldY[2] - worldY[1], y3y2Shifted);
		int dwz1 = FixedPointUtils.divide(worldZ[2] - worldZ[0], y3y1Shifted);
		int dwz2 = FixedPointUtils.divide(worldZ[2] - worldZ[1], y3y2Shifted);
		int dnx1 = FixedPointUtils.divide(normalX[2] - normalX[0], y3y1Shifted);
		int dnx2 = FixedPointUtils.divide(normalX[2] - normalX[1], y3y2Shifted);
		int dny1 = FixedPointUtils.divide(normalY[2] - normalY[0], y3y1Shifted);
		int dny2 = FixedPointUtils.divide(normalY[2] - normalY[1], y3y2Shifted);
		int dnz1 = FixedPointUtils.divide(normalZ[2] - normalZ[0], y3y1Shifted);
		int dnz2 = FixedPointUtils.divide(normalZ[2] - normalZ[1], y3y2Shifted);
		int x1 = xShifted;
		int x2 = xShifted;
		int z = location2[VECTOR_Z] << FP_BIT;
		int wx = worldX[2];
        int wy = worldY[2];
        int wz = worldZ[2];
        int nx = normalX[2];
        int ny = normalY[2];
        int nz = normalZ[2];
		if (dx1 > dx2) {
			int dxdx = dx1 - dx2;
			dxdx = dxdx == 0 ? 1 : dxdx;
			int dz = FixedPointUtils.divide(dz1 - dz2, dxdx);
			int dwx = FixedPointUtils.divide(dwx1 - dwx2, dxdx);
			int dwy = FixedPointUtils.divide(dwy1 - dwy2, dxdx);
			int dwz = FixedPointUtils.divide(dwz1 - dwz2, dxdx);
			int dnx = FixedPointUtils.divide(dnx1 - dnx2, dxdx);
			int dny = FixedPointUtils.divide(dny1 - dny2, dxdx);
			int dnz = FixedPointUtils.divide(dnz1 - dnz2, dxdx);
	        for (int y = location2[VECTOR_Y]; y > location0[VECTOR_Y]; y--) {
	        	drawScanline(x1, x2, y, z, wx, wy, wz, nx, ny, nz, dz, dwx, dwy, dwz, dnx, dny, dnz);
	            x1 -= dx1;
	            x2 -= dx2;
	            z -= dz1;
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
			int dwx = FixedPointUtils.divide(dwx2 - dwx1, dxdx);
			int dwy = FixedPointUtils.divide(dwy2 - dwy1, dxdx);
			int dwz = FixedPointUtils.divide(dwz2 - dwz1, dxdx);
			int dnx = FixedPointUtils.divide(dnx2 - dnx1, dxdx);
			int dny = FixedPointUtils.divide(dny2 - dny1, dxdx);
			int dnz = FixedPointUtils.divide(dnz2 - dnz1, dxdx);
	        for (int y = location2[VECTOR_Y]; y > location0[VECTOR_Y]; y--) {
	        	drawScanline(x1, x2, y, z, wx, wy, wz, nx, ny, nz, dz, dwx, dwy, dwz, dnx, dny, dnz);
	            x1 -= dx2;
	            x2 -= dx1;
	            z -= dz2;
	            wx -= dwx2;
	            wy -= dwy2;
	            wz -= dwz2;
	            nx -= dnx2;
	            ny -= dny2;
	            nz -= dnz2;
	        }
		}
    }
	
	private void drawScanline(int x1, int x2, int y, int z, int wx, int wy, int wz, int nx, int ny, int nz,
							int dz, int dwx, int dwy, int dwz, int dnx, int dny, int dnz) {
		x1 >>= FP_BIT;
		x2 >>= FP_BIT;
		for (; x1 <= x2; x1++) {
			fragment.getLocation()[VECTOR_X] = x1;
			fragment.getLocation()[VECTOR_Y] = y;
			fragment.getLocation()[VECTOR_Z] = z >> FP_BIT;
			fragment.getWorldLocation()[VECTOR_X] = wx;
            fragment.getWorldLocation()[VECTOR_Y] = wy;
            fragment.getWorldLocation()[VECTOR_Z] = wz;
			fragment.getWorldNormal()[VECTOR_X] = nx;
			fragment.getWorldNormal()[VECTOR_Y] = ny;
            fragment.getWorldNormal()[VECTOR_Z] = nz;
			shader.fragment(fragment);
			z += dz;
			wx += dwx;
			wy += dwy;
			wz += dwz;
			nx += dnx;
			ny += dny;
			nz += dnz;
		}
	}
}
