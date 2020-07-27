package com.johnsproject.jgameengine.rasterization;

import static com.johnsproject.jgameengine.util.FixedPointUtils.FP_BIT;
import static com.johnsproject.jgameengine.util.VectorUtils.VECTOR_X;
import static com.johnsproject.jgameengine.util.VectorUtils.VECTOR_Y;
import static com.johnsproject.jgameengine.util.VectorUtils.VECTOR_Z;

import com.johnsproject.jgameengine.model.Face;
import com.johnsproject.jgameengine.model.Frustum;
import com.johnsproject.jgameengine.shading.Shader;
import com.johnsproject.jgameengine.util.FixedPointUtils;
import com.johnsproject.jgameengine.util.VectorUtils;

public class LinearRasterizer2 extends Rasterizer {

	protected final int[] vector0;
	protected final int[] vector00;
	protected final int[] vector01;
	protected final int[] vector02;
	protected final int[] vector03;
	protected final int[] vector1;
	protected final int[] vector10;
	protected final int[] vector11;
	protected final int[] vector12;
	protected final int[] vector13;
	
	protected int y2y1Shifted, y3y1Shifted, y3y2Shifted;
	protected int dv0x1, dv0x2;
	protected int dv0y1, dv0y2;
	protected int dv0z1, dv0z2;
	protected int dv1x1, dv1x2;
	protected int dv1y1, dv1y2;
	protected int dv1z1, dv1z2;
	protected int v0x, v0y, v0z;
	protected int v1x, v1y, v1z;
	protected int dv0x, dv0y, dv0z;
	protected int dv1x, dv1y, dv1z;
	
	
	public LinearRasterizer2(Shader shader) {
		super(shader);
		this.vector0 = VectorUtils.emptyVector();
		this.vector00 = VectorUtils.emptyVector();
		this.vector01 = VectorUtils.emptyVector();
		this.vector02 = VectorUtils.emptyVector();
		this.vector03 = VectorUtils.emptyVector();
		this.vector1 = VectorUtils.emptyVector();
		this.vector10 = VectorUtils.emptyVector();
		this.vector11 = VectorUtils.emptyVector();
		this.vector12 = VectorUtils.emptyVector();
		this.vector13 = VectorUtils.emptyVector();
	}
	
	public void linearDraw2(Face face, Frustum frustum) {
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
	
	protected void sortY() {
		super.sortY();
		if (sortY00 > sortY01) {
			VectorUtils.swap(vector00, vector01);
			VectorUtils.swap(vector10, vector11);
		}
		if (sortY10 > sortY11) {
			VectorUtils.swap(vector01, vector02);
			VectorUtils.swap(vector11, vector12);
		}
		if (sortY20 > sortY21) {
			VectorUtils.swap(vector00, vector01);
			VectorUtils.swap(vector10, vector11);
		}
	}
	
	protected int splitTriangle() {
        int dy = super.splitTriangle();
        vector03[VECTOR_X] = vector00[VECTOR_X] + FixedPointUtils.multiply(dy, vector02[VECTOR_X] - vector00[VECTOR_X]);
        vector03[VECTOR_Y] = vector00[VECTOR_Y] + FixedPointUtils.multiply(dy, vector02[VECTOR_Y] - vector00[VECTOR_Y]);
        vector03[VECTOR_Z] = vector00[VECTOR_Z] + FixedPointUtils.multiply(dy, vector02[VECTOR_Z] - vector00[VECTOR_Z]);
        vector13[VECTOR_X] = vector10[VECTOR_X] + FixedPointUtils.multiply(dy, vector12[VECTOR_X] - vector10[VECTOR_X]);
        vector13[VECTOR_Y] = vector10[VECTOR_Y] + FixedPointUtils.multiply(dy, vector12[VECTOR_Y] - vector10[VECTOR_Y]);
        vector13[VECTOR_Z] = vector10[VECTOR_Z] + FixedPointUtils.multiply(dy, vector12[VECTOR_Z] - vector10[VECTOR_Z]);
        return dy;
	}
	
	protected void swapSplitedBottomTriangle() {
		super.swapSplitedBottomTriangle();
		VectorUtils.swap(vector03, vector02);
		VectorUtils.swap(vector13, vector12);
	}
	
	protected void swapSplitedTopTriangle() {
		super.swapSplitedTopTriangle();
	    VectorUtils.swap(vector03, vector02);
	    VectorUtils.swap(vector13, vector12);	
	    VectorUtils.swap(vector00, vector01);
	    VectorUtils.swap(vector10, vector11);
	    VectorUtils.swap(vector01, vector03);
	    VectorUtils.swap(vector11, vector13);
	}
	
	private void drawBottomTriangle() {
		initializeBottomTriangle();
        if(dx1 < dx2) {
        	initializeDx2GreaterDx1();
	        for (; y1 <= y2; y1++) {
	        	drawScanline(x1, x2, y1, z, dz,
	        			v0x, dv0x, v0y, dv0y, v0z, dv0z,
	        			v1x, dv1x, v1y, dv1y, v1z, dv1z);
	        	incrementBottomDx2GreaterDx1();
	        }
        } else {
        	initializeDx1GreaterDx2();
        	for (; y1 <= y2; y1++) {
        		drawScanline(x1, x2, y1, z, dz,
	        			v0x, dv0x, v0y, dv0y, v0z, dv0z,
	        			v1x, dv1x, v1y, dv1y, v1z, dv1z);
        		incrementBottomDx1GreaterDx2();
	        }
        }
    }
	
	protected void initializeBottomTriangle() {
		super.initializeBottomTriangle();
		y2y1Shifted = y2y1 << FP_BIT;
		y3y1Shifted = y3y1 << FP_BIT;
		dv0x1 = FixedPointUtils.divide(vector01[VECTOR_X] - vector00[VECTOR_X], y2y1Shifted);
		dv0x2 = FixedPointUtils.divide(vector02[VECTOR_X] - vector00[VECTOR_X], y3y1Shifted);
		dv0y1 = FixedPointUtils.divide(vector01[VECTOR_Y] - vector00[VECTOR_Y], y2y1Shifted);
		dv0y2 = FixedPointUtils.divide(vector02[VECTOR_Y] - vector00[VECTOR_Y], y3y1Shifted);
		dv0z1 = FixedPointUtils.divide(vector01[VECTOR_Z] - vector00[VECTOR_Z], y2y1Shifted);
		dv0z2 = FixedPointUtils.divide(vector02[VECTOR_Z] - vector00[VECTOR_Z], y3y1Shifted);
		dv1x1 = FixedPointUtils.divide(vector11[VECTOR_X] - vector10[VECTOR_X], y2y1Shifted);
		dv1x2 = FixedPointUtils.divide(vector12[VECTOR_X] - vector10[VECTOR_X], y3y1Shifted);
		dv1y1 = FixedPointUtils.divide(vector11[VECTOR_Y] - vector10[VECTOR_Y], y2y1Shifted);
		dv1y2 = FixedPointUtils.divide(vector12[VECTOR_Y] - vector10[VECTOR_Y], y3y1Shifted);
		dv1z1 = FixedPointUtils.divide(vector11[VECTOR_Z] - vector10[VECTOR_Z], y2y1Shifted);
		dv1z2 = FixedPointUtils.divide(vector12[VECTOR_Z] - vector10[VECTOR_Z], y3y1Shifted);
        v0x = vector00[VECTOR_X];
        v0y = vector00[VECTOR_Y];
        v0z = vector00[VECTOR_Z];
        v1x = vector10[VECTOR_X];
        v1y = vector10[VECTOR_Y];
        v1z = vector10[VECTOR_Z];
	}
	
	protected void incrementBottomDx2GreaterDx1() {
		super.incrementBottomDx2GreaterDx1();
        v0x += dv0x1;
        v0y += dv0y1;
        v0z += dv0z1;
        v1x += dv1x1;
        v1y += dv1y1;
        v1z += dv1z1;
	}
	
	protected void incrementBottomDx1GreaterDx2() {
		super.incrementBottomDx1GreaterDx2();
        v0x += dv0x2;
        v0y += dv0y2;
        v0z += dv0z2;
        v1x += dv1x2;
        v1y += dv1y2;
        v1z += dv1z2;
	}
    
	private void drawTopTriangle() {
		initializeTopTriangle();
		if (dx1 > dx2) {
			initializeDx1GreaterDx2();
	        for (; y1 > y2; y1--) {
	        	drawScanline(x1, x2, y1, z, dz,
	        			v0x, dv0x, v0y, dv0y, v0z, dv0z,
	        			v1x, dv1x, v1y, dv1y, v1z, dv1z);
	        	incrementTopDx2GreaterDx1();
	        }
		} else {
			initializeDx2GreaterDx1();
	        for (; y1 > y2; y1--) {
	        	drawScanline(x1, x2, y1, z, dz,
	        			v0x, dv0x, v0y, dv0y, v0z, dv0z,
	        			v1x, dv1x, v1y, dv1y, v1z, dv1z);
	        	incrementTopDx1GreaterDx2();
	        }
		}
    }
	
	protected void initializeTopTriangle() {
		super.initializeTopTriangle();
		y3y1Shifted = y3y1 << FP_BIT;
		y3y2Shifted = y3y2 << FP_BIT;
		dv0x1 = FixedPointUtils.divide(vector02[VECTOR_X] - vector00[VECTOR_X], y3y1Shifted);
		dv0x2 = FixedPointUtils.divide(vector02[VECTOR_X] - vector01[VECTOR_X], y3y2Shifted);
		dv0y1 = FixedPointUtils.divide(vector02[VECTOR_Y] - vector00[VECTOR_Y], y3y1Shifted);
		dv0y2 = FixedPointUtils.divide(vector02[VECTOR_Y] - vector01[VECTOR_Y], y3y2Shifted);
		dv0z1 = FixedPointUtils.divide(vector02[VECTOR_Z] - vector00[VECTOR_Z], y3y1Shifted);
		dv0z2 = FixedPointUtils.divide(vector02[VECTOR_Z] - vector01[VECTOR_Z], y3y2Shifted);
		dv1x1 = FixedPointUtils.divide(vector12[VECTOR_X] - vector10[VECTOR_X], y3y1Shifted);
		dv1x2 = FixedPointUtils.divide(vector12[VECTOR_X] - vector11[VECTOR_X], y3y2Shifted);
		dv1y1 = FixedPointUtils.divide(vector12[VECTOR_Y] - vector10[VECTOR_Y], y3y1Shifted);
		dv1y2 = FixedPointUtils.divide(vector12[VECTOR_Y] - vector11[VECTOR_Y], y3y2Shifted);
		dv1z1 = FixedPointUtils.divide(vector12[VECTOR_Z] - vector10[VECTOR_Z], y3y1Shifted);
		dv1z2 = FixedPointUtils.divide(vector12[VECTOR_Z] - vector11[VECTOR_Z], y3y2Shifted);
        v0x = vector02[VECTOR_X];
        v0y = vector02[VECTOR_Y];
        v0z = vector02[VECTOR_Z];
        v1x = vector12[VECTOR_X];
        v1y = vector12[VECTOR_Y];
        v1z = vector12[VECTOR_Z];
	}
	
	protected void incrementTopDx2GreaterDx1() {
		super.incrementTopDx2GreaterDx1();
        v0x -= dv0x1;
        v0y -= dv0y1;
        v0z -= dv0z1;
        v1x -= dv1x1;
        v1y -= dv1y1;
        v1z -= dv1z1;
	}
	
	protected void incrementTopDx1GreaterDx2() {
		super.incrementTopDx1GreaterDx2();
        v0x -= dv0x2;
        v0y -= dv0y2;
        v0z -= dv0z2;
        v1x -= dv1x2;
        v1y -= dv1y2;
        v1z -= dv1z2;
	}
	
	protected void initializeDx1GreaterDx2() {
		super.initializeDx1GreaterDx2();
		dv0x = FixedPointUtils.divide(dv0x1 - dv0x2, dxdx);
    	dv0y = FixedPointUtils.divide(dv0y1 - dv0y2, dxdx);
    	dv0z = FixedPointUtils.divide(dv0z1 - dv0z2, dxdx);
    	dv1x = FixedPointUtils.divide(dv1x1 - dv1x2, dxdx);
    	dv1y = FixedPointUtils.divide(dv1y1 - dv1y2, dxdx);
    	dv1z = FixedPointUtils.divide(dv1z1 - dv1z2, dxdx);
	}
	
	protected void initializeDx2GreaterDx1() {
		super.initializeDx2GreaterDx1();
		dv0x = FixedPointUtils.divide(dv0x2 - dv0x1, dxdx);
    	dv0y = FixedPointUtils.divide(dv0y2 - dv0y1, dxdx);
    	dv0z = FixedPointUtils.divide(dv0z2 - dv0z1, dxdx);
    	dv1x = FixedPointUtils.divide(dv1x2 - dv1x1, dxdx);
    	dv1y = FixedPointUtils.divide(dv1y2 - dv1y1, dxdx);
    	dv1z = FixedPointUtils.divide(dv1z2 - dv1z1, dxdx);
	}
	
	private void drawScanline(int x1, int x2, int y, int z, int dz,
			int v0x, int dv0x, int v0y, int dv0y, int v0z, int dv0z,
			int v1x, int dv1x, int v1y, int dv1y, int v1z, int dv1z) {
		x1 >>= FP_BIT;
		x2 >>= FP_BIT;
		for (; x1 <= x2; x1++) {
			location[VECTOR_X] = x1;
			location[VECTOR_Y] = y;
			location[VECTOR_Z] = z >> FP_BIT;
			vector0[VECTOR_X] = v0x;
			vector0[VECTOR_Y] = v0y;
			vector0[VECTOR_Z] = v0z;
			vector1[VECTOR_X] = v1x;
			vector1[VECTOR_Y] = v1y;
			vector1[VECTOR_Z] = v1z;
			shader.fragment();
			z += dz;
			v0x += dv0x;
			v0y += dv0y;
			v0z += dv0z;
			v1x += dv1x;
			v1y += dv1y;
			v1z += dv1z;
		}
	}
	
	public void setVector00(int[] vector) {
		VectorUtils.copy(vector00, vector);
	}
	
	public void setVector01(int[] vector) {
		VectorUtils.copy(vector01, vector);
	}
	
	public void setVector02(int[] vector) {
		VectorUtils.copy(vector02, vector);
	}
	
	public void setVector10(int[] vector) {
		VectorUtils.copy(vector10, vector);
	}
	
	public void setVector11(int[] vector) {
		VectorUtils.copy(vector11, vector);
	}
	
	public void setVector12(int[] vector) {
		VectorUtils.copy(vector12, vector);
	}
	
	public void setVector00(int x, int y, int z) {
		vector00[VECTOR_X] = x;
		vector00[VECTOR_Y] = y;
		vector00[VECTOR_Z] = z;
	}
	
	public void setVector01(int x, int y, int z) {
		vector01[VECTOR_X] = x;
		vector01[VECTOR_Y] = y;
		vector01[VECTOR_Z] = z;
	}
	
	public void setVector02(int x, int y, int z) {
		vector02[VECTOR_X] = x;
		vector02[VECTOR_Y] = y;
		vector02[VECTOR_Z] = z;
	}
	
	public void setVector10(int x, int y, int z) {
		vector10[VECTOR_X] = x;
		vector10[VECTOR_Y] = y;
		vector10[VECTOR_Z] = z;
	}
	
	public void setVector11(int x, int y, int z) {
		vector11[VECTOR_X] = x;
		vector11[VECTOR_Y] = y;
		vector11[VECTOR_Z] = z;
	}
	
	public void setVector12(int x, int y, int z) {
		vector12[VECTOR_X] = x;
		vector12[VECTOR_Y] = y;
		vector12[VECTOR_Z] = z;
	}

	public int[] getVector0() {
		return vector0;
	}

	public int[] getVector1() {
		return vector1;
	}
}
