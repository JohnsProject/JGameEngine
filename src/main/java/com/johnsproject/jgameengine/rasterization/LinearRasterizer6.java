package com.johnsproject.jgameengine.rasterization;

import static com.johnsproject.jgameengine.util.FixedPointUtils.FP_BIT;
import static com.johnsproject.jgameengine.util.VectorUtils.VECTOR_X;
import static com.johnsproject.jgameengine.util.VectorUtils.VECTOR_Y;
import static com.johnsproject.jgameengine.util.VectorUtils.VECTOR_Z;

import com.johnsproject.jgameengine.model.Face;
import com.johnsproject.jgameengine.shading.Shader;
import com.johnsproject.jgameengine.util.FixedPointUtils;
import com.johnsproject.jgameengine.util.VectorUtils;

public class LinearRasterizer6 extends LinearRasterizer4 {

	protected final int[] vector4;
	protected final int[] vector40;
	protected final int[] vector41;
	protected final int[] vector42;
	protected final int[] vector43;
	protected final int[] vector5;
	protected final int[] vector50;
	protected final int[] vector51;
	protected final int[] vector52;
	protected final int[] vector53;
	
	protected int dv4x1, dv4x2;
	protected int dv4y1, dv4y2;
	protected int dv4z1, dv4z2;
	protected int dv5x1, dv5x2;
	protected int dv5y1, dv5y2;
	protected int dv5z1, dv5z2;
	protected int v4x, v4y, v4z;
	protected int v5x, v5y, v5z;
	protected int dv4x, dv4y, dv4z;
	protected int dv5x, dv5y, dv5z;
	
	public LinearRasterizer6(Shader shader) {
		super(shader);
		this.vector4 = VectorUtils.emptyVector();
		this.vector40 = VectorUtils.emptyVector();
		this.vector41 = VectorUtils.emptyVector();
		this.vector42 = VectorUtils.emptyVector();
		this.vector43 = VectorUtils.emptyVector();
		this.vector5 = VectorUtils.emptyVector();
		this.vector50 = VectorUtils.emptyVector();
		this.vector51 = VectorUtils.emptyVector();
		this.vector52 = VectorUtils.emptyVector();
		this.vector53 = VectorUtils.emptyVector();
	}

	public void linearDraw6(Face face) {
		copyLocations(face);
		copyFrustum();
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
			VectorUtils.swap(vector40, vector41);
			VectorUtils.swap(vector50, vector51);
		}
		if (sortY10 > sortY11) {
			VectorUtils.swap(vector41, vector42);
			VectorUtils.swap(vector51, vector52);
		}
		if (sortY20 > sortY21) {
			VectorUtils.swap(vector40, vector41);
			VectorUtils.swap(vector50, vector51);
		}
	}
	
	protected int splitTriangle() {
        int dy = super.splitTriangle();
        vector43[VECTOR_X] = vector40[VECTOR_X] + FixedPointUtils.multiply(dy, vector42[VECTOR_X] - vector40[VECTOR_X]);
        vector43[VECTOR_Y] = vector40[VECTOR_Y] + FixedPointUtils.multiply(dy, vector42[VECTOR_Y] - vector40[VECTOR_Y]);
        vector43[VECTOR_Z] = vector40[VECTOR_Z] + FixedPointUtils.multiply(dy, vector42[VECTOR_Z] - vector40[VECTOR_Z]);
        vector53[VECTOR_X] = vector50[VECTOR_X] + FixedPointUtils.multiply(dy, vector52[VECTOR_X] - vector50[VECTOR_X]);
        vector53[VECTOR_Y] = vector50[VECTOR_Y] + FixedPointUtils.multiply(dy, vector52[VECTOR_Y] - vector50[VECTOR_Y]);
        vector53[VECTOR_Z] = vector50[VECTOR_Z] + FixedPointUtils.multiply(dy, vector52[VECTOR_Z] - vector50[VECTOR_Z]);
        return dy;
	}
	
	protected void swapSplitedBottomTriangle() {
		super.swapSplitedBottomTriangle();
		VectorUtils.swap(vector43, vector42);
		VectorUtils.swap(vector53, vector52);
	}
	
	protected void swapSplitedTopTriangle() {
		super.swapSplitedTopTriangle();
	    VectorUtils.swap(vector43, vector42);
	    VectorUtils.swap(vector53, vector52);	
	    VectorUtils.swap(vector40, vector41);
	    VectorUtils.swap(vector50, vector51);
	    VectorUtils.swap(vector41, vector43);
	    VectorUtils.swap(vector51, vector53);
	}
	
	private void drawBottomTriangle() {
		initializeBottomTriangle();
        if(dx1 < dx2) {
        	initializeDx2GreaterDx1();
	        for (; y1 <= y2; y1++) {
	        	drawScanline(x1, x2, y1, z, dz,
	        			v0x, dv0x, v0y, dv0y, v0z, dv0z,
	        			v1x, dv1x, v1y, dv1y, v1z, dv1z,
	        			v2x, dv2x, v2y, dv2y, v2z, dv2z,
	        			v3x, dv3x, v3y, dv3y, v3z, dv3z,
	        			v4x, dv4x, v4y, dv4y, v4z, dv4z,
	        			v5x, dv5x, v5y, dv5y, v5z, dv5z);
	            x1 += dx1;
	            x2 += dx2;
	            z += dz1;
	            v0x += dv0x1;
	            v0y += dv0y1;
	            v0z += dv0z1;
	            v1x += dv1x1;
	            v1y += dv1y1;
	            v1z += dv1z1;
	            v2x += dv2x1;
	            v2y += dv2y1;
	            v2z += dv2z1;
	            v3x += dv3x1;
	            v3y += dv3y1;
	            v3z += dv3z1;
	            v4x += dv4x1;
	            v4y += dv4y1;
	            v4z += dv4z1;
	            v5x += dv5x1;
	            v5y += dv5y1;
	            v5z += dv5z1;
	        }
        } else {
        	initializeDx1GreaterDx2();
        	for (; y1 <= y2; y1++) {
        		drawScanline(x1, x2, y1, z, dz,
	        			v0x, dv0x, v0y, dv0y, v0z, dv0z,
	        			v1x, dv1x, v1y, dv1y, v1z, dv1z,
	        			v2x, dv2x, v2y, dv2y, v2z, dv2z,
	        			v3x, dv3x, v3y, dv3y, v3z, dv3z,
	        			v4x, dv4x, v4y, dv4y, v4z, dv4z,
	        			v5x, dv5x, v5y, dv5y, v5z, dv5z);
	            x1 += dx2;
	            x2 += dx1;
	            z += dz2;
	            v0x += dv0x2;
	            v0y += dv0y2;
	            v0z += dv0z2;
	            v1x += dv1x2;
	            v1y += dv1y2;
	            v1z += dv1z2;
	            v2x += dv2x2;
	            v2y += dv2y2;
	            v2z += dv2z2;
	            v3x += dv3x2;
	            v3y += dv3y2;
	            v3z += dv3z2;
	            v4x += dv4x2;
	            v4y += dv4y2;
	            v4z += dv4z2;
	            v5x += dv5x2;
	            v5y += dv5y2;
	            v5z += dv5z2;
	        }
        }
    }
	
	protected void initializeBottomTriangle() {
		super.initializeBottomTriangle();
		dv4x1 = FixedPointUtils.divide(vector41[VECTOR_X] - vector40[VECTOR_X], y2y1Shifted);
		dv4x2 = FixedPointUtils.divide(vector42[VECTOR_X] - vector40[VECTOR_X], y3y1Shifted);
		dv4y1 = FixedPointUtils.divide(vector41[VECTOR_Y] - vector40[VECTOR_Y], y2y1Shifted);
		dv4y2 = FixedPointUtils.divide(vector42[VECTOR_Y] - vector40[VECTOR_Y], y3y1Shifted);
		dv4z1 = FixedPointUtils.divide(vector41[VECTOR_Z] - vector40[VECTOR_Z], y2y1Shifted);
		dv4z2 = FixedPointUtils.divide(vector42[VECTOR_Z] - vector40[VECTOR_Z], y3y1Shifted);
		dv5x1 = FixedPointUtils.divide(vector51[VECTOR_X] - vector50[VECTOR_X], y2y1Shifted);
		dv5x2 = FixedPointUtils.divide(vector52[VECTOR_X] - vector50[VECTOR_X], y3y1Shifted);
		dv5y1 = FixedPointUtils.divide(vector51[VECTOR_Y] - vector50[VECTOR_Y], y2y1Shifted);
		dv5y2 = FixedPointUtils.divide(vector52[VECTOR_Y] - vector50[VECTOR_Y], y3y1Shifted);
		dv5z1 = FixedPointUtils.divide(vector51[VECTOR_Z] - vector50[VECTOR_Z], y2y1Shifted);
		dv5z2 = FixedPointUtils.divide(vector52[VECTOR_Z] - vector50[VECTOR_Z], y3y1Shifted);
        v4x = vector40[VECTOR_X];
        v4y = vector40[VECTOR_Y];
        v4z = vector40[VECTOR_Z];
        v5x = vector50[VECTOR_X];
        v5y = vector50[VECTOR_Y];
        v5z = vector50[VECTOR_Z];
	}
    
	private void drawTopTriangle() {
		initializeTopTriangle();
		if (dx1 > dx2) {
			initializeDx1GreaterDx2();
	        for (; y1 > y2; y1--) {
	        	drawScanline(x1, x2, y1, z, dz,
	        			v0x, dv0x, v0y, dv0y, v0z, dv0z,
	        			v1x, dv1x, v1y, dv1y, v1z, dv1z,
	        			v2x, dv2x, v2y, dv2y, v2z, dv2z,
	        			v3x, dv3x, v3y, dv3y, v3z, dv3z,
	        			v4x, dv4x, v4y, dv4y, v4z, dv4z,
	        			v5x, dv5x, v5y, dv5y, v5z, dv5z);
	            x1 -= dx1;
	            x2 -= dx2;
	            z -= dz1;
	            v0x -= dv0x1;
	            v0y -= dv0y1;
	            v0z -= dv0z1;
	            v1x -= dv1x1;
	            v1y -= dv1y1;
	            v1z -= dv1z1;
	            v2x -= dv2x1;
	            v2y -= dv2y1;
	            v2z -= dv2z1;
	            v3x -= dv3x1;
	            v3y -= dv3y1;
	            v3z -= dv3z1;
	            v4x -= dv4x1;
	            v4y -= dv4y1;
	            v4z -= dv4z1;
	            v5x -= dv5x1;
	            v5y -= dv5y1;
	            v5z -= dv5z1;
	        }
		} else {
			initializeDx2GreaterDx1();
	        for (; y1 > y2; y1--) {
	        	drawScanline(x1, x2, y1, z, dz,
	        			v0x, dv0x, v0y, dv0y, v0z, dv0z,
	        			v1x, dv1x, v1y, dv1y, v1z, dv1z,
	        			v2x, dv2x, v2y, dv2y, v2z, dv2z,
	        			v3x, dv3x, v3y, dv3y, v3z, dv3z,
	        			v4x, dv4x, v4y, dv4y, v4z, dv4z,
	        			v5x, dv5x, v5y, dv5y, v5z, dv5z);
	            x1 -= dx2;
	            x2 -= dx1;
	            z -= dz2;
	            v0x -= dv0x2;
	            v0y -= dv0y2;
	            v0z -= dv0z2;
	            v1x -= dv1x2;
	            v1y -= dv1y2;
	            v1z -= dv1z2;
	            v2x -= dv2x2;
	            v2y -= dv2y2;
	            v2z -= dv2z2;
	            v3x -= dv3x2;
	            v3y -= dv3y2;
	            v3z -= dv3z2;
	            v4x -= dv4x2;
	            v4y -= dv4y2;
	            v4z -= dv4z2;
	            v5x -= dv5x2;
	            v5y -= dv5y2;
	            v5z -= dv5z2;
	        }
		}
    }
	
	protected void initializeTopTriangle() {
		super.initializeTopTriangle();
		dv4x1 = FixedPointUtils.divide(vector42[VECTOR_X] - vector40[VECTOR_X], y3y1Shifted);
		dv4x2 = FixedPointUtils.divide(vector42[VECTOR_X] - vector41[VECTOR_X], y3y2Shifted);
		dv4y1 = FixedPointUtils.divide(vector42[VECTOR_Y] - vector40[VECTOR_Y], y3y1Shifted);
		dv4y2 = FixedPointUtils.divide(vector42[VECTOR_Y] - vector41[VECTOR_Y], y3y2Shifted);
		dv4z1 = FixedPointUtils.divide(vector42[VECTOR_Z] - vector40[VECTOR_Z], y3y1Shifted);
		dv4z2 = FixedPointUtils.divide(vector42[VECTOR_Z] - vector41[VECTOR_Z], y3y2Shifted);
		dv5x1 = FixedPointUtils.divide(vector52[VECTOR_X] - vector50[VECTOR_X], y3y1Shifted);
		dv5x2 = FixedPointUtils.divide(vector52[VECTOR_X] - vector51[VECTOR_X], y3y2Shifted);
		dv5y1 = FixedPointUtils.divide(vector52[VECTOR_Y] - vector50[VECTOR_Y], y3y1Shifted);
		dv5y2 = FixedPointUtils.divide(vector52[VECTOR_Y] - vector51[VECTOR_Y], y3y2Shifted);
		dv5z1 = FixedPointUtils.divide(vector52[VECTOR_Z] - vector50[VECTOR_Z], y3y1Shifted);
		dv5z2 = FixedPointUtils.divide(vector52[VECTOR_Z] - vector51[VECTOR_Z], y3y2Shifted);
        v4x = vector42[VECTOR_X];
        v4y = vector42[VECTOR_Y];
        v4z = vector42[VECTOR_Z];
        v5x = vector52[VECTOR_X];
        v5y = vector52[VECTOR_Y];
        v5z = vector52[VECTOR_Z];
	}
	
	protected void initializeDx1GreaterDx2() {
		super.initializeDx1GreaterDx2();
		dv4x = FixedPointUtils.divide(dv4x1 - dv4x2, dxdx);
    	dv4y = FixedPointUtils.divide(dv4y1 - dv4y2, dxdx);
    	dv4z = FixedPointUtils.divide(dv4z1 - dv4z2, dxdx);
    	dv5x = FixedPointUtils.divide(dv5x1 - dv5x2, dxdx);
    	dv5y = FixedPointUtils.divide(dv5y1 - dv5y2, dxdx);
    	dv5z = FixedPointUtils.divide(dv5z1 - dv5z2, dxdx);
	}
	
	protected void initializeDx2GreaterDx1() {
		super.initializeDx2GreaterDx1();
		dv4x = FixedPointUtils.divide(dv4x2 - dv4x1, dxdx);
    	dv4y = FixedPointUtils.divide(dv4y2 - dv4y1, dxdx);
    	dv4z = FixedPointUtils.divide(dv4z2 - dv4z1, dxdx);
    	dv5x = FixedPointUtils.divide(dv5x2 - dv5x1, dxdx);
    	dv5y = FixedPointUtils.divide(dv5y2 - dv5y1, dxdx);
    	dv5z = FixedPointUtils.divide(dv5z2 - dv5z1, dxdx);
	}
	
	private void drawScanline(int x1, int x2, int y, int z, int dz,
			int v0x, int dv0x, int v0y, int dv0y, int v0z, int dv0z,
			int v1x, int dv1x, int v1y, int dv1y, int v1z, int dv1z,
			int v2x, int dv2x, int v2y, int dv2y, int v2z, int dv2z,
			int v3x, int dv3x, int v3y, int dv3y, int v3z, int dv3z,
			int v4x, int dv4x, int v4y, int dv4y, int v4z, int dv4z,
			int v5x, int dv5x, int v5y, int dv5y, int v5z, int dv5z) {
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
			vector2[VECTOR_X] = v2x;
			vector2[VECTOR_Y] = v2y;
			vector2[VECTOR_Z] = v2z;
			vector3[VECTOR_X] = v3x;
			vector3[VECTOR_Y] = v3y;
			vector3[VECTOR_Z] = v3z;
			vector4[VECTOR_X] = v4x;
			vector4[VECTOR_Y] = v4y;
			vector4[VECTOR_Z] = v4z;
			vector5[VECTOR_X] = v5x;
			vector5[VECTOR_Y] = v5y;
			vector5[VECTOR_Z] = v5z;
			shader.fragment();
			z += dz;
			v0x += dv0x;
			v0y += dv0y;
			v0z += dv0z;
			v1x += dv1x;
			v1y += dv1y;
			v1z += dv1z;
			v2x += dv2x;
			v2y += dv2y;
			v2z += dv2z;
			v3x += dv3x;
			v3y += dv3y;
			v3z += dv3z;
			v4x += dv4x;
			v4y += dv4y;
			v4z += dv4z;
			v5x += dv5x;
			v5y += dv5y;
			v5z += dv5z;
		}
	}
	
	public void setVector40(int[] vector) {
		VectorUtils.copy(vector40, vector);
	}
	
	public void setVector41(int[] vector) {
		VectorUtils.copy(vector41, vector);
	}
	
	public void setVector42(int[] vector) {
		VectorUtils.copy(vector42, vector);
	}
	
	public void setVector50(int[] vector) {
		VectorUtils.copy(vector50, vector);
	}
	
	public void setVector51(int[] vector) {
		VectorUtils.copy(vector51, vector);
	}
	
	public void setVector52(int[] vector) {
		VectorUtils.copy(vector52, vector);
	}
	
	public void setVector40(int x, int y, int z) {
		vector40[VECTOR_X] = x;
		vector40[VECTOR_Y] = y;
		vector40[VECTOR_Z] = z;
	}
	
	public void setVector41(int x, int y, int z) {
		vector41[VECTOR_X] = x;
		vector41[VECTOR_Y] = y;
		vector41[VECTOR_Z] = z;
	}
	
	public void setVector42(int x, int y, int z) {
		vector42[VECTOR_X] = x;
		vector42[VECTOR_Y] = y;
		vector42[VECTOR_Z] = z;
	}
	
	public void setVector50(int x, int y, int z) {
		vector50[VECTOR_X] = x;
		vector50[VECTOR_Y] = y;
		vector50[VECTOR_Z] = z;
	}
	
	public void setVector51(int x, int y, int z) {
		vector51[VECTOR_X] = x;
		vector51[VECTOR_Y] = y;
		vector51[VECTOR_Z] = z;
	}
	
	public void setVector52(int x, int y, int z) {
		vector52[VECTOR_X] = x;
		vector52[VECTOR_Y] = y;
		vector52[VECTOR_Z] = z;
	}

	public int[] getVector4() {
		return vector4;
	}

	public int[] getVector5() {
		return vector5;
	}
}
