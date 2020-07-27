package com.johnsproject.jgameengine.rasterization;

import static com.johnsproject.jgameengine.util.FixedPointUtils.FP_BIT;
import static com.johnsproject.jgameengine.util.VectorUtils.VECTOR_X;
import static com.johnsproject.jgameengine.util.VectorUtils.VECTOR_Y;
import static com.johnsproject.jgameengine.util.VectorUtils.VECTOR_Z;

import com.johnsproject.jgameengine.model.Face;
import com.johnsproject.jgameengine.shading.Shader;
import com.johnsproject.jgameengine.util.FixedPointUtils;
import com.johnsproject.jgameengine.util.VectorUtils;

public class LinearRasterizer4 extends LinearRasterizer2 {

	protected final int[] vector2;
	protected final int[] vector20;
	protected final int[] vector21;
	protected final int[] vector22;
	protected final int[] vector23;
	protected final int[] vector3;
	protected final int[] vector30;
	protected final int[] vector31;
	protected final int[] vector32;
	protected final int[] vector33;
	
	protected int dv2x1, dv2x2;
	protected int dv2y1, dv2y2;
	protected int dv2z1, dv2z2;
	protected int dv3x1, dv3x2;
	protected int dv3y1, dv3y2;
	protected int dv3z1, dv3z2;
	protected int v2x, v2y, v2z;
	protected int v3x, v3y, v3z;
	protected int dv2x, dv2y, dv2z;
	protected int dv3x, dv3y, dv3z;
	
	public LinearRasterizer4(Shader shader) {
		super(shader);
		this.vector2 = VectorUtils.emptyVector();
		this.vector20 = VectorUtils.emptyVector();
		this.vector21 = VectorUtils.emptyVector();
		this.vector22 = VectorUtils.emptyVector();
		this.vector23 = VectorUtils.emptyVector();
		this.vector3 = VectorUtils.emptyVector();
		this.vector30 = VectorUtils.emptyVector();
		this.vector31 = VectorUtils.emptyVector();
		this.vector32 = VectorUtils.emptyVector();
		this.vector33 = VectorUtils.emptyVector();
	}
	
	public void linearDraw4(Face face) {
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
			VectorUtils.swap(vector20, vector21);
			VectorUtils.swap(vector30, vector31);
		}
		if (sortY10 > sortY11) {
			VectorUtils.swap(vector21, vector22);
			VectorUtils.swap(vector31, vector32);
		}
		if (sortY20 > sortY21) {
			VectorUtils.swap(vector20, vector21);
			VectorUtils.swap(vector30, vector31);
		}
	}
	
	protected int splitTriangle() {
        int dy = super.splitTriangle();
        vector23[VECTOR_X] = vector20[VECTOR_X] + FixedPointUtils.multiply(dy, vector22[VECTOR_X] - vector20[VECTOR_X]);
        vector23[VECTOR_Y] = vector20[VECTOR_Y] + FixedPointUtils.multiply(dy, vector22[VECTOR_Y] - vector20[VECTOR_Y]);
        vector23[VECTOR_Z] = vector20[VECTOR_Z] + FixedPointUtils.multiply(dy, vector22[VECTOR_Z] - vector20[VECTOR_Z]);
        vector33[VECTOR_X] = vector30[VECTOR_X] + FixedPointUtils.multiply(dy, vector32[VECTOR_X] - vector30[VECTOR_X]);
        vector33[VECTOR_Y] = vector30[VECTOR_Y] + FixedPointUtils.multiply(dy, vector32[VECTOR_Y] - vector30[VECTOR_Y]);
        vector33[VECTOR_Z] = vector30[VECTOR_Z] + FixedPointUtils.multiply(dy, vector32[VECTOR_Z] - vector30[VECTOR_Z]);
        return dy;
	}
	
	protected void swapSplitedBottomTriangle() {
		super.swapSplitedBottomTriangle();
		VectorUtils.swap(vector23, vector22);
		VectorUtils.swap(vector33, vector32);
	}
	
	protected void swapSplitedTopTriangle() {
		super.swapSplitedTopTriangle();
	    VectorUtils.swap(vector23, vector22);
	    VectorUtils.swap(vector33, vector32);	
	    VectorUtils.swap(vector20, vector21);
	    VectorUtils.swap(vector30, vector31);
	    VectorUtils.swap(vector21, vector23);
	    VectorUtils.swap(vector31, vector33);
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
	        			v3x, dv3x, v3y, dv3y, v3z, dv3z);
	        	incrementBottomDx2GreaterDx1();
	        }
        } else {
        	initializeDx1GreaterDx2();
        	for (; y1 <= y2; y1++) {
        		drawScanline(x1, x2, y1, z, dz,
	        			v0x, dv0x, v0y, dv0y, v0z, dv0z,
	        			v1x, dv1x, v1y, dv1y, v1z, dv1z,
	        			v2x, dv2x, v2y, dv2y, v2z, dv2z,
	        			v3x, dv3x, v3y, dv3y, v3z, dv3z);
        		incrementBottomDx1GreaterDx2();
	        }
        }
    }
	
	protected void initializeBottomTriangle() {
		super.initializeBottomTriangle();
		dv2x1 = FixedPointUtils.divide(vector21[VECTOR_X] - vector20[VECTOR_X], y2y1Shifted);
		dv2x2 = FixedPointUtils.divide(vector22[VECTOR_X] - vector20[VECTOR_X], y3y1Shifted);
		dv2y1 = FixedPointUtils.divide(vector21[VECTOR_Y] - vector20[VECTOR_Y], y2y1Shifted);
		dv2y2 = FixedPointUtils.divide(vector22[VECTOR_Y] - vector20[VECTOR_Y], y3y1Shifted);
		dv2z1 = FixedPointUtils.divide(vector21[VECTOR_Z] - vector20[VECTOR_Z], y2y1Shifted);
		dv2z2 = FixedPointUtils.divide(vector22[VECTOR_Z] - vector20[VECTOR_Z], y3y1Shifted);
		dv3x1 = FixedPointUtils.divide(vector31[VECTOR_X] - vector30[VECTOR_X], y2y1Shifted);
		dv3x2 = FixedPointUtils.divide(vector32[VECTOR_X] - vector30[VECTOR_X], y3y1Shifted);
		dv3y1 = FixedPointUtils.divide(vector31[VECTOR_Y] - vector30[VECTOR_Y], y2y1Shifted);
		dv3y2 = FixedPointUtils.divide(vector32[VECTOR_Y] - vector30[VECTOR_Y], y3y1Shifted);
		dv3z1 = FixedPointUtils.divide(vector31[VECTOR_Z] - vector30[VECTOR_Z], y2y1Shifted);
		dv3z2 = FixedPointUtils.divide(vector32[VECTOR_Z] - vector30[VECTOR_Z], y3y1Shifted);
        v2x = vector20[VECTOR_X];
        v2y = vector20[VECTOR_Y];
        v2z = vector20[VECTOR_Z];
        v3x = vector30[VECTOR_X];
        v3y = vector30[VECTOR_Y];
        v3z = vector30[VECTOR_Z];
	}
	
	protected void incrementBottomDx2GreaterDx1() {
		super.incrementBottomDx2GreaterDx1();
		v2x += dv2x1;
        v2y += dv2y1;
        v2z += dv2z1;
        v3x += dv3x1;
        v3y += dv3y1;
        v3z += dv3z1;
	}
	
	protected void incrementBottomDx1GreaterDx2() {
		super.incrementBottomDx1GreaterDx2();
		v2x += dv2x2;
        v2y += dv2y2;
        v2z += dv2z2;
        v3x += dv3x2;
        v3y += dv3y2;
        v3z += dv3z2;
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
	        			v3x, dv3x, v3y, dv3y, v3z, dv3z);
	        	incrementTopDx2GreaterDx1();
	        }
		} else {
			initializeDx2GreaterDx1();
	        for (; y1 > y2; y1--) {
	        	drawScanline(x1, x2, y1, z, dz,
	        			v0x, dv0x, v0y, dv0y, v0z, dv0z,
	        			v1x, dv1x, v1y, dv1y, v1z, dv1z,
	        			v2x, dv2x, v2y, dv2y, v2z, dv2z,
	        			v3x, dv3x, v3y, dv3y, v3z, dv3z);
	        	incrementTopDx1GreaterDx2();
	        }
		}
    }
	
	protected void initializeTopTriangle() {
		super.initializeTopTriangle();
		dv2x1 = FixedPointUtils.divide(vector22[VECTOR_X] - vector20[VECTOR_X], y3y1Shifted);
		dv2x2 = FixedPointUtils.divide(vector22[VECTOR_X] - vector21[VECTOR_X], y3y2Shifted);
		dv2y1 = FixedPointUtils.divide(vector22[VECTOR_Y] - vector20[VECTOR_Y], y3y1Shifted);
		dv2y2 = FixedPointUtils.divide(vector22[VECTOR_Y] - vector21[VECTOR_Y], y3y2Shifted);
		dv2z1 = FixedPointUtils.divide(vector22[VECTOR_Z] - vector20[VECTOR_Z], y3y1Shifted);
		dv2z2 = FixedPointUtils.divide(vector22[VECTOR_Z] - vector21[VECTOR_Z], y3y2Shifted);
		dv3x1 = FixedPointUtils.divide(vector32[VECTOR_X] - vector30[VECTOR_X], y3y1Shifted);
		dv3x2 = FixedPointUtils.divide(vector32[VECTOR_X] - vector31[VECTOR_X], y3y2Shifted);
		dv3y1 = FixedPointUtils.divide(vector32[VECTOR_Y] - vector30[VECTOR_Y], y3y1Shifted);
		dv3y2 = FixedPointUtils.divide(vector32[VECTOR_Y] - vector31[VECTOR_Y], y3y2Shifted);
		dv3z1 = FixedPointUtils.divide(vector32[VECTOR_Z] - vector30[VECTOR_Z], y3y1Shifted);
		dv3z2 = FixedPointUtils.divide(vector32[VECTOR_Z] - vector31[VECTOR_Z], y3y2Shifted);
        v2x = vector22[VECTOR_X];
        v2y = vector22[VECTOR_Y];
        v2z = vector22[VECTOR_Z];
        v3x = vector32[VECTOR_X];
        v3y = vector32[VECTOR_Y];
        v3z = vector32[VECTOR_Z];
	}
	
	protected void incrementTopDx2GreaterDx1() {
		super.incrementTopDx2GreaterDx1();
        v2x -= dv2x1;
        v2y -= dv2y1;
        v2z -= dv2z1;
        v3x -= dv3x1;
        v3y -= dv3y1;
        v3z -= dv3z1;
	}
	
	protected void incrementTopDx1GreaterDx2() {
		super.incrementTopDx1GreaterDx2();
        v2x -= dv2x2;
        v2y -= dv2y2;
        v2z -= dv2z2;
        v3x -= dv3x2;
        v3y -= dv3y2;
        v3z -= dv3z2;
	}
	
	protected void initializeDx1GreaterDx2() {
		super.initializeDx1GreaterDx2();
		dv2x = FixedPointUtils.divide(dv2x1 - dv2x2, dxdx);
    	dv2y = FixedPointUtils.divide(dv2y1 - dv2y2, dxdx);
    	dv2z = FixedPointUtils.divide(dv2z1 - dv2z2, dxdx);
    	dv3x = FixedPointUtils.divide(dv3x1 - dv3x2, dxdx);
    	dv3y = FixedPointUtils.divide(dv3y1 - dv3y2, dxdx);
    	dv3z = FixedPointUtils.divide(dv3z1 - dv3z2, dxdx);
	}
	
	protected void initializeDx2GreaterDx1() {
		super.initializeDx2GreaterDx1();
		dv2x = FixedPointUtils.divide(dv2x2 - dv2x1, dxdx);
    	dv2y = FixedPointUtils.divide(dv2y2 - dv2y1, dxdx);
    	dv2z = FixedPointUtils.divide(dv2z2 - dv2z1, dxdx);
    	dv3x = FixedPointUtils.divide(dv3x2 - dv3x1, dxdx);
    	dv3y = FixedPointUtils.divide(dv3y2 - dv3y1, dxdx);
    	dv3z = FixedPointUtils.divide(dv3z2 - dv3z1, dxdx);
	}
	
	private void drawScanline(int x1, int x2, int y, int z, int dz,
			int v0x, int dv0x, int v0y, int dv0y, int v0z, int dv0z,
			int v1x, int dv1x, int v1y, int dv1y, int v1z, int dv1z,
			int v2x, int dv2x, int v2y, int dv2y, int v2z, int dv2z,
			int v3x, int dv3x, int v3y, int dv3y, int v3z, int dv3z) {
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
		}
	}
	
	public void setVector20(int[] vector) {
		VectorUtils.copy(vector20, vector);
	}
	
	public void setVector21(int[] vector) {
		VectorUtils.copy(vector21, vector);
	}
	
	public void setVector22(int[] vector) {
		VectorUtils.copy(vector22, vector);
	}
	
	public void setVector30(int[] vector) {
		VectorUtils.copy(vector30, vector);
	}
	
	public void setVector31(int[] vector) {
		VectorUtils.copy(vector31, vector);
	}
	
	public void setVector32(int[] vector) {
		VectorUtils.copy(vector32, vector);
	}
	
	public void setVector20(int x, int y, int z) {
		vector20[VECTOR_X] = x;
		vector20[VECTOR_Y] = y;
		vector20[VECTOR_Z] = z;
	}
	
	public void setVector21(int x, int y, int z) {
		vector21[VECTOR_X] = x;
		vector21[VECTOR_Y] = y;
		vector21[VECTOR_Z] = z;
	}
	
	public void setVector22(int x, int y, int z) {
		vector22[VECTOR_X] = x;
		vector22[VECTOR_Y] = y;
		vector22[VECTOR_Z] = z;
	}
	
	public void setVector30(int x, int y, int z) {
		vector30[VECTOR_X] = x;
		vector30[VECTOR_Y] = y;
		vector30[VECTOR_Z] = z;
	}
	
	public void setVector31(int x, int y, int z) {
		vector31[VECTOR_X] = x;
		vector31[VECTOR_Y] = y;
		vector31[VECTOR_Z] = z;
	}
	
	public void setVector32(int x, int y, int z) {
		vector32[VECTOR_X] = x;
		vector32[VECTOR_Y] = y;
		vector32[VECTOR_Z] = z;
	}

	public int[] getVector2() {
		return vector2;
	}

	public int[] getVector3() {
		return vector3;
	}
}
