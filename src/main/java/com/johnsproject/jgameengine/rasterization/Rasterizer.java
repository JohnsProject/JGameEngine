package com.johnsproject.jgameengine.rasterization;

import static com.johnsproject.jgameengine.util.FixedPointUtils.FP_BIT;
import static com.johnsproject.jgameengine.util.FixedPointUtils.FP_ONE;
import static com.johnsproject.jgameengine.util.VectorUtils.VECTOR_X;
import static com.johnsproject.jgameengine.util.VectorUtils.VECTOR_Y;
import static com.johnsproject.jgameengine.util.VectorUtils.VECTOR_Z;

import com.johnsproject.jgameengine.model.Face;
import com.johnsproject.jgameengine.model.Frustum;
import com.johnsproject.jgameengine.shading.Shader;
import com.johnsproject.jgameengine.util.FixedPointUtils;
import com.johnsproject.jgameengine.util.VectorUtils;

public class Rasterizer {

	//TODO implement perspective rasterizers
	
	protected final Shader shader;
	protected final int[] location;
	protected final int[] location0;
	protected final int[] location1;
	protected final int[] location2;
	protected final int[] location3;
	
	protected int sortY00, sortY01;
	protected int sortY10, sortY11;
	protected int sortY20, sortY21;
	
	protected int y2y1, y3y1, y3y2;
	protected int dx1, dx2;
	protected int dz1, dz2;
	protected int x1, x2;
	protected int y1, y2;
	protected int z;
	protected int dxdx;
	protected int dz;
	
	private int renderTargetLeft;
	private int renderTargetRight;
	private int renderTargetTop;
	private int renderTargetBottom;
	private boolean frustumCull;
	private int faceCull;
	
	
	public Rasterizer(Shader shader) {
		this.shader = shader;
		this.location = VectorUtils.emptyVector();
		this.location0 = VectorUtils.emptyVector();
		this.location1 = VectorUtils.emptyVector();
		this.location2 = VectorUtils.emptyVector();
		this.location3 = VectorUtils.emptyVector();
		this.frustumCull = true;
		this.faceCull = -1;
	}

	public void setFrustumCull(boolean frustumCull) {
		this.frustumCull = frustumCull;
	}
	
	public void setFaceCull(int faceCull) {
		this.faceCull = faceCull;
	}
	
	public void draw(Face face, Frustum frustum) {
		copyLocations(face);
		copyFrustum(frustum);
		if(isCulled())
			return;
		sortY();
        if (location1[VECTOR_Y] == location2[VECTOR_Y]) {
        	drawBottomTriangle();
        } else if (location0[VECTOR_Y] == location1[VECTOR_Y]) {
        	drawTopTriangle();
        } else {
            splitTriangle();
            swapSplitedBottomTriangle();
            drawBottomTriangle();
            swapSplitedTopTriangle();
            drawTopTriangle();
        }
	}
	
	protected void copyLocations(Face face) {
		VectorUtils.copy(location0, face.getVertex(0).getLocation());
		VectorUtils.copy(location1, face.getVertex(1).getLocation());
		VectorUtils.copy(location2, face.getVertex(2).getLocation());
	}
	
	protected void copyFrustum(Frustum frustum) {
		renderTargetLeft = frustum.getRenderTargetLeft();
		renderTargetRight = frustum.getRenderTargetRight();
		renderTargetTop = frustum.getRenderTargetTop();
		renderTargetBottom = frustum.getRenderTargetBottom();
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
	
	protected void sortY() {
		sortY00 = location0[VECTOR_Y];
		sortY01 = location1[VECTOR_Y];
		if (sortY00 > sortY01) {
			VectorUtils.swap(location0, location1);
		}
		sortY10 = location1[VECTOR_Y];
		sortY11 = location2[VECTOR_Y];
		if (sortY10 > sortY11) {
			VectorUtils.swap(location1, location2);
		}
		sortY20 = location0[VECTOR_Y];
		sortY21 = location1[VECTOR_Y];
		if (sortY20 > sortY21) {
			VectorUtils.swap(location0, location1);
		}
	}
	
	protected int splitTriangle() {
        int dy = FixedPointUtils.divide(location1[VECTOR_Y] - location0[VECTOR_Y], location2[VECTOR_Y] - location0[VECTOR_Y]);
        location3[VECTOR_X] = location0[VECTOR_X] + FixedPointUtils.multiply(dy, location2[VECTOR_X] - location0[VECTOR_X]);
        location3[VECTOR_Y] = location1[VECTOR_Y];
        location3[VECTOR_Z] = location0[VECTOR_Z] + FixedPointUtils.multiply(dy, location2[VECTOR_Z] - location0[VECTOR_Z]);
        return dy;
	}
	
	protected void swapSplitedBottomTriangle() {
        VectorUtils.swap(location3, location2);
	}
	
	protected void swapSplitedTopTriangle() {
        VectorUtils.swap(location3, location2);
        VectorUtils.swap(location0, location1);
        VectorUtils.swap(location1, location3);		
	}
	
	private void drawBottomTriangle() {
		initializeBottomTriangle();
        if(dx1 < dx2) {
        	initializeDx2GreaterDx1();
	        for (; y1 <= y2; y1++) {
	        	drawScanline(x1, x2, y1, z, dz);
	        	incrementBottomDx2GreaterDx1();
	        }
        } else {
        	initializeDx1GreaterDx2();
        	for (; y1 <= y2; y1++) {
        		drawScanline(x1, x2, y1, z, dz);
        		incrementBottomDx1GreaterDx2();
	        }
        }
    }
	
	protected void initializeBottomTriangle() {
		final int xShifted = location0[VECTOR_X] << FP_BIT;
		y2y1 = location1[VECTOR_Y] - location0[VECTOR_Y];
		y2y1 = y2y1 == 0 ? 1 : y2y1;
		y3y1 = y2y1;
		y2y1 = FixedPointUtils.divide(FP_ONE, y2y1);
		y3y1 = FixedPointUtils.divide(FP_ONE, y3y1);
		dx1 = FixedPointUtils.multiply(location1[VECTOR_X] - location0[VECTOR_X], y2y1);
		dx2 = FixedPointUtils.multiply(location2[VECTOR_X] - location0[VECTOR_X], y3y1);
		dz1 = FixedPointUtils.multiply(location1[VECTOR_Z] - location0[VECTOR_Z], y2y1);
		dz2 = FixedPointUtils.multiply(location2[VECTOR_Z] - location0[VECTOR_Z], y3y1);
        x1 = xShifted;
        x2 = xShifted;
        y1 = location0[VECTOR_Y];
        y2 = location1[VECTOR_Y];
        z = location0[VECTOR_Z] << FP_BIT;
	}
	
	protected void incrementBottomDx2GreaterDx1() {
        x1 += dx1;
        x2 += dx2;
        z += dz1;		
	}
	
	protected void incrementBottomDx1GreaterDx2() {
        x1 += dx2;
        x2 += dx1;
        z += dz2;	
	}
    
	private void drawTopTriangle() {
		initializeTopTriangle();
		if (dx1 > dx2) {
			initializeDx1GreaterDx2();
	        for (; y1 > y2; y1--) {
	        	drawScanline(x1, x2, y1, z, dz);
	        	incrementTopDx2GreaterDx1();
	        }
		} else {
			initializeDx2GreaterDx1();
	        for (; y1 > y2; y1--) {
	        	drawScanline(x1, x2, y1, z, dz);
	        	incrementTopDx1GreaterDx2();
	        }
		}
    }
	
	protected void initializeTopTriangle() {
		final int xShifted = location2[VECTOR_X] << FP_BIT;
		y3y1 = location2[VECTOR_Y] - location0[VECTOR_Y];
		y3y2 = location2[VECTOR_Y] - location1[VECTOR_Y];
		y3y1 = y3y1 == 0 ? 1 : y3y1;
		y3y2 = y3y2 == 0 ? 1 : y3y2;
		y3y1 = FixedPointUtils.divide(FP_ONE, y3y1);
		y3y2 = FixedPointUtils.divide(FP_ONE, y3y2);
		dx1 = FixedPointUtils.multiply(location2[VECTOR_X] - location0[VECTOR_X], y3y1);
		dx2 = FixedPointUtils.multiply(location2[VECTOR_X] - location1[VECTOR_X], y3y2);
		dz1 = FixedPointUtils.multiply(location2[VECTOR_Z] - location0[VECTOR_Z], y3y1);
		dz2 = FixedPointUtils.multiply(location2[VECTOR_Z] - location1[VECTOR_Z], y3y2);
		x1 = xShifted;
		x2 = xShifted;
		y1 = location2[VECTOR_Y];
        y2 = location0[VECTOR_Y];
		z = location2[VECTOR_Z] << FP_BIT;
	}
	
	protected void incrementTopDx2GreaterDx1() {
        x1 -= dx1;
        x2 -= dx2;
        z -= dz1;	
	}
	
	protected void incrementTopDx1GreaterDx2() {
        x1 -= dx2;
        x2 -= dx1;
        z -= dz2;
	}
	
	protected void initializeDx2GreaterDx1() {
		dxdx = dx2 - dx1;
		dxdx = dxdx == 0 ? 1 : dxdx;
		dxdx = FixedPointUtils.divide(FP_ONE, dxdx);
		dz = FixedPointUtils.multiply(dz2 - dz1, dxdx);
	}
	
	protected void initializeDx1GreaterDx2() {
		dxdx = dx1 - dx2;
		dxdx = dxdx == 0 ? 1 : dxdx;
		dxdx = FixedPointUtils.divide(FP_ONE, dxdx);
		dz = FixedPointUtils.multiply(dz1 - dz2, dxdx);
	}
	
	private void drawScanline(int x1, int x2, int y, int z, int dz) {
		x1 >>= FP_BIT;
		x2 >>= FP_BIT;
		for (; x1 <= x2; x1++) {
			location[VECTOR_X] = x1;
			location[VECTOR_Y] = y;
			location[VECTOR_Z] = z >> FP_BIT;
			shader.fragment();
			z += dz;
		}
	}

	public int[] getLocation() {
		return location;
	}
}
