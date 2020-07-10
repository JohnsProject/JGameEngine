package com.johnsproject.jgameengine.rasterization;

import static com.johnsproject.jgameengine.util.FixedPointUtils.FP_BIT;
import static com.johnsproject.jgameengine.util.FixedPointUtils.FP_ONE;
import static com.johnsproject.jgameengine.util.VectorUtils.VECTOR_X;
import static com.johnsproject.jgameengine.util.VectorUtils.VECTOR_Y;
import static com.johnsproject.jgameengine.util.VectorUtils.VECTOR_Z;

import com.johnsproject.jgameengine.model.Face;
import com.johnsproject.jgameengine.model.Fragment;
import com.johnsproject.jgameengine.model.Frustum;
import com.johnsproject.jgameengine.shading.Shader;
import com.johnsproject.jgameengine.util.FixedPointUtils;
import com.johnsproject.jgameengine.util.VectorUtils;


public class FlatRasterizer {

	protected final Shader shader;
	protected final Fragment fragment;
	protected final int[] location0;
	protected final int[] location1;
	protected final int[] location2;
	protected final int[] vectorCache;
	protected int renderTargetLeft;
	protected int renderTargetRight;
	protected int renderTargetTop;
	protected int renderTargetBottom;
	protected boolean frustumCull;
	protected int faceCull;
	
	public FlatRasterizer(Shader shader) {
		this.shader = shader;
		this.fragment = new Fragment();
		this.vectorCache = VectorUtils.emptyVector();
		this.location0 = VectorUtils.emptyVector();
		this.location1 = VectorUtils.emptyVector();
		this.location2 = VectorUtils.emptyVector();
		this.frustumCull = true;
		this.faceCull = -1;
	}

	/**
	 * Sets if the rasterizer should cull the triangles that are outside of the view frustum. 
	 * Note that this method only sets if the rasterizer culls the whole triangle before even 
	 * beginning the rasterization process, a per pixel culling will still happen even if 
	 * frustumCull is set to false. 
	 * 
	 * @param frustumCull
	 */
	public void setFrustumCull(boolean frustumCull) {
		this.frustumCull = frustumCull;
	}
	
	/**
	 * Sets if the rasterizer should cull the triangle based on it's facing direction.
	 * 
	 * @param faceCull -1 = backface culling, 0 = no culling and 1 = frontface culling.
	 */
	public void setFaceCull(int faceCull) {
		this.faceCull = faceCull;
	}
	
	/**
	 * This method tells the rasterizer to draw the given {@link GeometryBuffer geometryBuffer}.
	 * This rasterizer draws a triangle using the x, y coordinates of each vertex of the geometryBuffer. 
	 * It uses linear interpolation to find out the z coordinate for each pixel.
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
		fragment.setLightColor(face.getLightColor());
		fragment.setMaterial(face.getMaterial());
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
	
	protected void copyLocations(Face face) {
		VectorUtils.copy(location0, face.getVertex(0).getLocation());
		VectorUtils.copy(location1, face.getVertex(1).getLocation());
		VectorUtils.copy(location2, face.getVertex(2).getLocation());
	}
	
	protected void copyFrustum() {
		final Frustum frustum = shader.getShaderBuffer().getCamera().getFrustum();
		renderTargetLeft = frustum.getRenderTargetLeft();
		renderTargetRight = frustum.getRenderTargetRight();
		renderTargetTop = frustum.getRenderTargetTop();
		renderTargetBottom = frustum.getRenderTargetBottom();
	}
	
	private void sortY() {
		if (location0[VECTOR_Y] > location1[VECTOR_Y]) {
			VectorUtils.swap(location0, location1);
		}
		if (location1[VECTOR_Y] > location2[VECTOR_Y]) {
			VectorUtils.swap(location1, location2);
		}
		if (location0[VECTOR_Y] > location1[VECTOR_Y]) {
			VectorUtils.swap(location0, location1);
		}
	}
	
	protected boolean isCulled() {
		if(isBiggerThanRenderTarget()) {
			return true;
		}
		else if(isOutOfFrustum()) {
			return true;
		}
		else if(isBackface()) {
			return true;
		}
		else {
			return false;
		}
	}
	
	private boolean isBiggerThanRenderTarget() {
		final int width0 = Math.abs(location0[VECTOR_X] - location1[VECTOR_X]);
		final int width1 = Math.abs(location2[VECTOR_X] - location1[VECTOR_X]);
		final int width2 = Math.abs(location2[VECTOR_X] - location0[VECTOR_X]);
		final int height0 = Math.abs(location0[VECTOR_Y] - location1[VECTOR_Y]);
		final int height1 = Math.abs(location2[VECTOR_Y] - location1[VECTOR_Y]);
		final int height2 = Math.abs(location2[VECTOR_Y] - location0[VECTOR_Y]);
		final int width = Math.max(width0, Math.max(width1, width2));
		final int height = Math.max(height0, Math.max(height1, height2));
		if((width > (renderTargetRight - renderTargetLeft)) || (height > (renderTargetBottom - renderTargetTop))) {
			return true;
		}
		return false;
	}
	
	private boolean isOutOfFrustum() {
		if(frustumCull) {
			final boolean insideWidth1 = (location0[VECTOR_X] > renderTargetLeft) && (location0[VECTOR_X] < renderTargetRight);
			final boolean insideWidth2 = (location1[VECTOR_X] > renderTargetLeft) && (location1[VECTOR_X] < renderTargetRight);
			final boolean insideWidth3 = (location2[VECTOR_X] > renderTargetLeft) && (location2[VECTOR_X] < renderTargetRight);
			final boolean insideHeight1 = (location0[VECTOR_Y] > renderTargetTop) && (location0[VECTOR_Y] < renderTargetBottom);
			final boolean insideHeight2 = (location1[VECTOR_Y] > renderTargetTop) && (location1[VECTOR_Y] < renderTargetBottom);
			final boolean insideHeight3 = (location2[VECTOR_Y] > renderTargetTop) && (location2[VECTOR_Y] < renderTargetBottom);
			final boolean insideDepth1 = (location0[VECTOR_Z] > 0) && (location0[VECTOR_Z] < FP_ONE);
			final boolean insideDepth2 = (location1[VECTOR_Z] > 0) && (location1[VECTOR_Z] < FP_ONE);
			final boolean insideDepth3 = (location2[VECTOR_Z] > 0) && (location2[VECTOR_Z] < FP_ONE);
			if ((!insideDepth1 && !insideDepth2 && !insideDepth3) 
					|| (!insideHeight1 && !insideHeight2 && !insideHeight3)
						|| (!insideWidth1 && !insideWidth2 && !insideWidth3)) {
							return true;
			}
		}
		return false;
	}
	
	private boolean isBackface() {
		final int x0 = location0[VECTOR_X];
		final int x1 = location1[VECTOR_X];
		final int x2 = location2[VECTOR_X];
		final int y0 = location0[VECTOR_Y];
		final int y1 = location1[VECTOR_Y];
		final int y2 = location2[VECTOR_Y];
		final int triangleSize = (x1 - x0) * (y2 - y0) - (x2 - x0) * (y1 - y0);
		return triangleSize * faceCull < 0;
	}
	
	protected int splitTriangle() {
        int dy = FixedPointUtils.divide(location1[VECTOR_Y] - location0[VECTOR_Y], location2[VECTOR_Y] - location0[VECTOR_Y]);
        vectorCache[VECTOR_X] = location0[VECTOR_X] + FixedPointUtils.multiply(dy, location2[VECTOR_X] - location0[VECTOR_X]);
        vectorCache[VECTOR_Y] = location1[VECTOR_Y];
        vectorCache[VECTOR_Z] = location0[VECTOR_Z] + FixedPointUtils.multiply(dy, location2[VECTOR_Z] - location0[VECTOR_Z]);
        return dy;
	}
	
	private void drawSplitedTriangle() {
        VectorUtils.swap(vectorCache, location2);
        drawBottomTriangle();
        VectorUtils.swap(vectorCache, location2);
        VectorUtils.swap(location0, location1);
        VectorUtils.swap(location1, vectorCache);
        drawTopTriangle();
	}
	
	private void drawBottomTriangle() {
		final int xShifted = location0[VECTOR_X] << FP_BIT;
		int y2y1 = location1[VECTOR_Y] - location0[VECTOR_Y];
		y2y1 = y2y1 == 0 ? 1 : y2y1;
		final int y3y1 = y2y1;
		final int dx1 = FixedPointUtils.divide(location1[VECTOR_X] - location0[VECTOR_X], y2y1);
		final int dx2 = FixedPointUtils.divide(location2[VECTOR_X] - location0[VECTOR_X], y3y1);
		final int dz1 = FixedPointUtils.divide(location1[VECTOR_Z] - location0[VECTOR_Z], y2y1);
		final int dz2 = FixedPointUtils.divide(location2[VECTOR_Z] - location0[VECTOR_Z], y3y1);
        int x1 = xShifted;
        int x2 = xShifted;
        int z = location0[VECTOR_Z] << FP_BIT;
        int y1 = location0[VECTOR_Y];
        int y2 = location1[VECTOR_Y];
        if(dx1 < dx2) {
        	int dxdx = dx2 - dx1;
        	dxdx = dxdx == 0 ? 1 : dxdx;
        	int dz = FixedPointUtils.divide(dz2 - dz1, dxdx);
	        for (; y1 <= y2; y1++) {
	        	drawScanline(x1, x2, y1, z, dz);
	            x1 += dx1;
	            x2 += dx2;
	            z += dz1;
	        }
        } else {
        	int dxdx = dx1 - dx2;
        	dxdx = dxdx == 0 ? 1 : dxdx;
        	int dz = FixedPointUtils.divide(dz1 - dz2, dxdx);
        	for (; y1 <= y2; y1++) {
        		drawScanline(x1, x2, y1, z, dz);
	            x1 += dx2;
	            x2 += dx1;
	            z += dz2;
	        }
        }
    }
    
	private void drawTopTriangle() {
		final int xShifted = location2[VECTOR_X] << FP_BIT;
		int y3y1 = location2[VECTOR_Y] - location0[VECTOR_Y];
		int y3y2 = location2[VECTOR_Y] - location1[VECTOR_Y];
		y3y1 = y3y1 == 0 ? 1 : y3y1;
		y3y2 = y3y2 == 0 ? 1 : y3y2;
		final int dx1 = FixedPointUtils.divide(location2[VECTOR_X] - location0[VECTOR_X], y3y1);
		final int dx2 = FixedPointUtils.divide(location2[VECTOR_X] - location1[VECTOR_X], y3y2);
		final int dz1 = FixedPointUtils.divide(location2[VECTOR_Z] - location0[VECTOR_Z], y3y1);
		final int dz2 = FixedPointUtils.divide(location2[VECTOR_Z] - location1[VECTOR_Z], y3y2);
		int x1 = xShifted;
		int x2 = xShifted;
		int z = location2[VECTOR_Z] << FP_BIT;
		int y1 = location2[VECTOR_Y];
        int y2 = location0[VECTOR_Y];
		if (dx1 > dx2) {
			int dxdx = dx1 - dx2;
			dxdx = dxdx == 0 ? 1 : dxdx;
			int dz = FixedPointUtils.divide(dz1 - dz2, dxdx);
	        for (; y1 > y2; y1--) {
	        	drawScanline(x1, x2, y1, z, dz);
	            x1 -= dx1;
	            x2 -= dx2;
	            z -= dz1;
	        }
		} else {
			int dxdx = dx2 - dx1;
			dxdx = dxdx == 0 ? 1 : dxdx;
			int dz = FixedPointUtils.divide(dz2 - dz1, dxdx);
	        for (; y1 > y2; y1--) {
	        	drawScanline(x1, x2, y1, z, dz);
	            x1 -= dx2;
	            x2 -= dx1;
	            z -= dz2;
	        }
		}
    }
	
	private void drawScanline(int x1, int x2, int y, int z, int dz) {
		x1 >>= FP_BIT;
		x2 >>= FP_BIT;
		for (; x1 <= x2; x1++) {
			fragment.getLocation()[VECTOR_X] = x1;
			fragment.getLocation()[VECTOR_Y] = y;
			fragment.getLocation()[VECTOR_Z] = z >> FP_BIT;
			shader.fragment(fragment);
			z += dz;
		}
	}
}
