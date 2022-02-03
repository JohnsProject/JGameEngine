package com.johnsproject.jgameengine.graphics.rasterization;

import static com.johnsproject.jgameengine.math.Fixed.FP_BIT;
import static com.johnsproject.jgameengine.math.Vector.VECTOR_X;
import static com.johnsproject.jgameengine.math.Vector.VECTOR_Y;
import static com.johnsproject.jgameengine.math.Vector.VECTOR_Z;

import com.johnsproject.jgameengine.graphics.Face;
import com.johnsproject.jgameengine.graphics.shading.Shader;
import com.johnsproject.jgameengine.math.Fixed;
import com.johnsproject.jgameengine.math.Frustum;
import com.johnsproject.jgameengine.math.Vector;

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
		this.vector4 = Vector.emptyVector();
		this.vector40 = Vector.emptyVector();
		this.vector41 = Vector.emptyVector();
		this.vector42 = Vector.emptyVector();
		this.vector43 = Vector.emptyVector();
		this.vector5 = Vector.emptyVector();
		this.vector50 = Vector.emptyVector();
		this.vector51 = Vector.emptyVector();
		this.vector52 = Vector.emptyVector();
		this.vector53 = Vector.emptyVector();
	}

	public void linearDraw6(Face face, Frustum frustum) {
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

	@Override
	protected void sortY() {
		super.sortY();
		if (sortY00 > sortY01) {
			Vector.swap(vector40, vector41);
			Vector.swap(vector50, vector51);
		}
		if (sortY10 > sortY11) {
			Vector.swap(vector41, vector42);
			Vector.swap(vector51, vector52);
		}
		if (sortY20 > sortY21) {
			Vector.swap(vector40, vector41);
			Vector.swap(vector50, vector51);
		}
	}

	@Override
	protected int splitTriangle() {
        int dy = super.splitTriangle();
        vector43[VECTOR_X] = vector40[VECTOR_X] + Fixed.multiply(dy, vector42[VECTOR_X] - vector40[VECTOR_X]);
        vector43[VECTOR_Y] = vector40[VECTOR_Y] + Fixed.multiply(dy, vector42[VECTOR_Y] - vector40[VECTOR_Y]);
        vector43[VECTOR_Z] = vector40[VECTOR_Z] + Fixed.multiply(dy, vector42[VECTOR_Z] - vector40[VECTOR_Z]);
        vector53[VECTOR_X] = vector50[VECTOR_X] + Fixed.multiply(dy, vector52[VECTOR_X] - vector50[VECTOR_X]);
        vector53[VECTOR_Y] = vector50[VECTOR_Y] + Fixed.multiply(dy, vector52[VECTOR_Y] - vector50[VECTOR_Y]);
        vector53[VECTOR_Z] = vector50[VECTOR_Z] + Fixed.multiply(dy, vector52[VECTOR_Z] - vector50[VECTOR_Z]);
        return dy;
	}

	@Override
	protected void swapSplitedBottomTriangle() {
		super.swapSplitedBottomTriangle();
		Vector.swap(vector43, vector42);
		Vector.swap(vector53, vector52);
	}

	@Override
	protected void swapSplitedTopTriangle() {
		super.swapSplitedTopTriangle();
	    Vector.swap(vector43, vector42);
	    Vector.swap(vector53, vector52);	
	    Vector.swap(vector40, vector41);
	    Vector.swap(vector50, vector51);
	    Vector.swap(vector41, vector43);
	    Vector.swap(vector51, vector53);
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
	        	incrementBottomDx2GreaterDx1();
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
        		incrementBottomDx1GreaterDx2();
	        }
        }
    }

	@Override
	protected void initializeBottomTriangle() {
		super.initializeBottomTriangle();
		dv4x1 = Fixed.multiply(vector41[VECTOR_X] - vector40[VECTOR_X], y2y1Shifted);
		dv4x2 = Fixed.multiply(vector42[VECTOR_X] - vector40[VECTOR_X], y3y1Shifted);
		dv4y1 = Fixed.multiply(vector41[VECTOR_Y] - vector40[VECTOR_Y], y2y1Shifted);
		dv4y2 = Fixed.multiply(vector42[VECTOR_Y] - vector40[VECTOR_Y], y3y1Shifted);
		dv4z1 = Fixed.multiply(vector41[VECTOR_Z] - vector40[VECTOR_Z], y2y1Shifted);
		dv4z2 = Fixed.multiply(vector42[VECTOR_Z] - vector40[VECTOR_Z], y3y1Shifted);
		dv5x1 = Fixed.multiply(vector51[VECTOR_X] - vector50[VECTOR_X], y2y1Shifted);
		dv5x2 = Fixed.multiply(vector52[VECTOR_X] - vector50[VECTOR_X], y3y1Shifted);
		dv5y1 = Fixed.multiply(vector51[VECTOR_Y] - vector50[VECTOR_Y], y2y1Shifted);
		dv5y2 = Fixed.multiply(vector52[VECTOR_Y] - vector50[VECTOR_Y], y3y1Shifted);
		dv5z1 = Fixed.multiply(vector51[VECTOR_Z] - vector50[VECTOR_Z], y2y1Shifted);
		dv5z2 = Fixed.multiply(vector52[VECTOR_Z] - vector50[VECTOR_Z], y3y1Shifted);
        v4x = vector40[VECTOR_X];
        v4y = vector40[VECTOR_Y];
        v4z = vector40[VECTOR_Z];
        v5x = vector50[VECTOR_X];
        v5y = vector50[VECTOR_Y];
        v5z = vector50[VECTOR_Z];
	}

	@Override
	protected void incrementBottomDx2GreaterDx1() {
		super.incrementBottomDx2GreaterDx1();
		v4x += dv4x1;
        v4y += dv4y1;
        v4z += dv4z1;
        v5x += dv5x1;
        v5y += dv5y1;
        v5z += dv5z1;
	}

	@Override
	protected void incrementBottomDx1GreaterDx2() {
		super.incrementBottomDx1GreaterDx2();
		v4x += dv4x2;
        v4y += dv4y2;
        v4z += dv4z2;
        v5x += dv5x2;
        v5y += dv5y2;
        v5z += dv5z2;
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
	        	incrementTopDx2GreaterDx1();
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
	        	incrementTopDx1GreaterDx2();
	        }
		}
    }

	@Override
	protected void initializeTopTriangle() {
		super.initializeTopTriangle();
		dv4x1 = Fixed.multiply(vector42[VECTOR_X] - vector40[VECTOR_X], y3y1Shifted);
		dv4x2 = Fixed.multiply(vector42[VECTOR_X] - vector41[VECTOR_X], y3y2Shifted);
		dv4y1 = Fixed.multiply(vector42[VECTOR_Y] - vector40[VECTOR_Y], y3y1Shifted);
		dv4y2 = Fixed.multiply(vector42[VECTOR_Y] - vector41[VECTOR_Y], y3y2Shifted);
		dv4z1 = Fixed.multiply(vector42[VECTOR_Z] - vector40[VECTOR_Z], y3y1Shifted);
		dv4z2 = Fixed.multiply(vector42[VECTOR_Z] - vector41[VECTOR_Z], y3y2Shifted);
		dv5x1 = Fixed.multiply(vector52[VECTOR_X] - vector50[VECTOR_X], y3y1Shifted);
		dv5x2 = Fixed.multiply(vector52[VECTOR_X] - vector51[VECTOR_X], y3y2Shifted);
		dv5y1 = Fixed.multiply(vector52[VECTOR_Y] - vector50[VECTOR_Y], y3y1Shifted);
		dv5y2 = Fixed.multiply(vector52[VECTOR_Y] - vector51[VECTOR_Y], y3y2Shifted);
		dv5z1 = Fixed.multiply(vector52[VECTOR_Z] - vector50[VECTOR_Z], y3y1Shifted);
		dv5z2 = Fixed.multiply(vector52[VECTOR_Z] - vector51[VECTOR_Z], y3y2Shifted);
        v4x = vector42[VECTOR_X];
        v4y = vector42[VECTOR_Y];
        v4z = vector42[VECTOR_Z];
        v5x = vector52[VECTOR_X];
        v5y = vector52[VECTOR_Y];
        v5z = vector52[VECTOR_Z];
	}

	@Override
	protected void incrementTopDx2GreaterDx1() {
		super.incrementTopDx2GreaterDx1();
		 v4x -= dv4x1;
         v4y -= dv4y1;
         v4z -= dv4z1;
         v5x -= dv5x1;
         v5y -= dv5y1;
         v5z -= dv5z1;
	}

	@Override
	protected void incrementTopDx1GreaterDx2() {
		super.incrementTopDx1GreaterDx2();
		v4x -= dv4x2;
        v4y -= dv4y2;
        v4z -= dv4z2;
        v5x -= dv5x2;
        v5y -= dv5y2;
        v5z -= dv5z2;
	}

	@Override
	protected void initializeDx1GreaterDx2() {
		super.initializeDx1GreaterDx2();
		dv4x = Fixed.multiply(dv4x1 - dv4x2, dxdx);
    	dv4y = Fixed.multiply(dv4y1 - dv4y2, dxdx);
    	dv4z = Fixed.multiply(dv4z1 - dv4z2, dxdx);
    	dv5x = Fixed.multiply(dv5x1 - dv5x2, dxdx);
    	dv5y = Fixed.multiply(dv5y1 - dv5y2, dxdx);
    	dv5z = Fixed.multiply(dv5z1 - dv5z2, dxdx);
	}

	@Override
	protected void initializeDx2GreaterDx1() {
		super.initializeDx2GreaterDx1();
		dv4x = Fixed.multiply(dv4x2 - dv4x1, dxdx);
    	dv4y = Fixed.multiply(dv4y2 - dv4y1, dxdx);
    	dv4z = Fixed.multiply(dv4z2 - dv4z1, dxdx);
    	dv5x = Fixed.multiply(dv5x2 - dv5x1, dxdx);
    	dv5y = Fixed.multiply(dv5y2 - dv5y1, dxdx);
    	dv5z = Fixed.multiply(dv5z2 - dv5z1, dxdx);
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
		Vector.copy(vector40, vector);
	}
	
	public void setVector41(int[] vector) {
		Vector.copy(vector41, vector);
	}
	
	public void setVector42(int[] vector) {
		Vector.copy(vector42, vector);
	}
	
	public void setVector50(int[] vector) {
		Vector.copy(vector50, vector);
	}
	
	public void setVector51(int[] vector) {
		Vector.copy(vector51, vector);
	}
	
	public void setVector52(int[] vector) {
		Vector.copy(vector52, vector);
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
