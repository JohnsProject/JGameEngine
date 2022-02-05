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
		this.vector0 = Vector.emptyVector();
		this.vector00 = Vector.emptyVector();
		this.vector01 = Vector.emptyVector();
		this.vector02 = Vector.emptyVector();
		this.vector03 = Vector.emptyVector();
		this.vector1 = Vector.emptyVector();
		this.vector10 = Vector.emptyVector();
		this.vector11 = Vector.emptyVector();
		this.vector12 = Vector.emptyVector();
		this.vector13 = Vector.emptyVector();
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
	
	@Override
	protected void sortY() {
		super.sortY();
		if (sortY00 > sortY01) {
			Vector.swap(vector00, vector01);
			Vector.swap(vector10, vector11);
		}
		if (sortY10 > sortY11) {
			Vector.swap(vector01, vector02);
			Vector.swap(vector11, vector12);
		}
		if (sortY20 > sortY21) {
			Vector.swap(vector00, vector01);
			Vector.swap(vector10, vector11);
		}
	}

	@Override
	protected int splitTriangle() {
        int dy = super.splitTriangle();
        vector03[VECTOR_X] = vector00[VECTOR_X] + Fixed.multiply(dy, vector02[VECTOR_X] - vector00[VECTOR_X]);
        vector03[VECTOR_Y] = vector00[VECTOR_Y] + Fixed.multiply(dy, vector02[VECTOR_Y] - vector00[VECTOR_Y]);
        vector03[VECTOR_Z] = vector00[VECTOR_Z] + Fixed.multiply(dy, vector02[VECTOR_Z] - vector00[VECTOR_Z]);
        vector13[VECTOR_X] = vector10[VECTOR_X] + Fixed.multiply(dy, vector12[VECTOR_X] - vector10[VECTOR_X]);
        vector13[VECTOR_Y] = vector10[VECTOR_Y] + Fixed.multiply(dy, vector12[VECTOR_Y] - vector10[VECTOR_Y]);
        vector13[VECTOR_Z] = vector10[VECTOR_Z] + Fixed.multiply(dy, vector12[VECTOR_Z] - vector10[VECTOR_Z]);
        return dy;
	}

	@Override
	protected void swapSplitedBottomTriangle() {
		super.swapSplitedBottomTriangle();
		Vector.swap(vector03, vector02);
		Vector.swap(vector13, vector12);
	}

	@Override
	protected void swapSplitedTopTriangle() {
		super.swapSplitedTopTriangle();
	    Vector.swap(vector03, vector02);
	    Vector.swap(vector13, vector12);	
	    Vector.swap(vector00, vector01);
	    Vector.swap(vector10, vector11);
	    Vector.swap(vector01, vector03);
	    Vector.swap(vector11, vector13);
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

	@Override
	protected void initializeBottomTriangle() {
		super.initializeBottomTriangle();
		y2y1Shifted = y2y1 >> FP_BIT;
		y3y1Shifted = y3y1 >> FP_BIT;
		dv0x1 = Fixed.multiply(vector01[VECTOR_X] - vector00[VECTOR_X], y2y1Shifted);
		dv0x2 = Fixed.multiply(vector02[VECTOR_X] - vector00[VECTOR_X], y3y1Shifted);
		dv0y1 = Fixed.multiply(vector01[VECTOR_Y] - vector00[VECTOR_Y], y2y1Shifted);
		dv0y2 = Fixed.multiply(vector02[VECTOR_Y] - vector00[VECTOR_Y], y3y1Shifted);
		dv0z1 = Fixed.multiply(vector01[VECTOR_Z] - vector00[VECTOR_Z], y2y1Shifted);
		dv0z2 = Fixed.multiply(vector02[VECTOR_Z] - vector00[VECTOR_Z], y3y1Shifted);
		dv1x1 = Fixed.multiply(vector11[VECTOR_X] - vector10[VECTOR_X], y2y1Shifted);
		dv1x2 = Fixed.multiply(vector12[VECTOR_X] - vector10[VECTOR_X], y3y1Shifted);
		dv1y1 = Fixed.multiply(vector11[VECTOR_Y] - vector10[VECTOR_Y], y2y1Shifted);
		dv1y2 = Fixed.multiply(vector12[VECTOR_Y] - vector10[VECTOR_Y], y3y1Shifted);
		dv1z1 = Fixed.multiply(vector11[VECTOR_Z] - vector10[VECTOR_Z], y2y1Shifted);
		dv1z2 = Fixed.multiply(vector12[VECTOR_Z] - vector10[VECTOR_Z], y3y1Shifted);
        v0x = vector00[VECTOR_X];
        v0y = vector00[VECTOR_Y];
        v0z = vector00[VECTOR_Z];
        v1x = vector10[VECTOR_X];
        v1y = vector10[VECTOR_Y];
        v1z = vector10[VECTOR_Z];
	}

	@Override
	protected void incrementBottomDx2GreaterDx1() {
		super.incrementBottomDx2GreaterDx1();
        v0x += dv0x1;
        v0y += dv0y1;
        v0z += dv0z1;
        v1x += dv1x1;
        v1y += dv1y1;
        v1z += dv1z1;
	}

	@Override
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

	@Override
	protected void initializeTopTriangle() {
		super.initializeTopTriangle();
		y3y1Shifted = y3y1 >> FP_BIT;
		y3y2Shifted = y3y2 >> FP_BIT;
		dv0x1 = Fixed.multiply(vector02[VECTOR_X] - vector00[VECTOR_X], y3y1Shifted);
		dv0x2 = Fixed.multiply(vector02[VECTOR_X] - vector01[VECTOR_X], y3y2Shifted);
		dv0y1 = Fixed.multiply(vector02[VECTOR_Y] - vector00[VECTOR_Y], y3y1Shifted);
		dv0y2 = Fixed.multiply(vector02[VECTOR_Y] - vector01[VECTOR_Y], y3y2Shifted);
		dv0z1 = Fixed.multiply(vector02[VECTOR_Z] - vector00[VECTOR_Z], y3y1Shifted);
		dv0z2 = Fixed.multiply(vector02[VECTOR_Z] - vector01[VECTOR_Z], y3y2Shifted);
		dv1x1 = Fixed.multiply(vector12[VECTOR_X] - vector10[VECTOR_X], y3y1Shifted);
		dv1x2 = Fixed.multiply(vector12[VECTOR_X] - vector11[VECTOR_X], y3y2Shifted);
		dv1y1 = Fixed.multiply(vector12[VECTOR_Y] - vector10[VECTOR_Y], y3y1Shifted);
		dv1y2 = Fixed.multiply(vector12[VECTOR_Y] - vector11[VECTOR_Y], y3y2Shifted);
		dv1z1 = Fixed.multiply(vector12[VECTOR_Z] - vector10[VECTOR_Z], y3y1Shifted);
		dv1z2 = Fixed.multiply(vector12[VECTOR_Z] - vector11[VECTOR_Z], y3y2Shifted);
        v0x = vector02[VECTOR_X];
        v0y = vector02[VECTOR_Y];
        v0z = vector02[VECTOR_Z];
        v1x = vector12[VECTOR_X];
        v1y = vector12[VECTOR_Y];
        v1z = vector12[VECTOR_Z];
	}

	@Override
	protected void incrementTopDx2GreaterDx1() {
		super.incrementTopDx2GreaterDx1();
        v0x -= dv0x1;
        v0y -= dv0y1;
        v0z -= dv0z1;
        v1x -= dv1x1;
        v1y -= dv1y1;
        v1z -= dv1z1;
	}

	@Override
	protected void incrementTopDx1GreaterDx2() {
		super.incrementTopDx1GreaterDx2();
        v0x -= dv0x2;
        v0y -= dv0y2;
        v0z -= dv0z2;
        v1x -= dv1x2;
        v1y -= dv1y2;
        v1z -= dv1z2;
	}

	@Override
	protected void initializeDx1GreaterDx2() {
		super.initializeDx1GreaterDx2();
		dv0x = Fixed.multiply(dv0x1 - dv0x2, dxdx);
    	dv0y = Fixed.multiply(dv0y1 - dv0y2, dxdx);
    	dv0z = Fixed.multiply(dv0z1 - dv0z2, dxdx);
    	dv1x = Fixed.multiply(dv1x1 - dv1x2, dxdx);
    	dv1y = Fixed.multiply(dv1y1 - dv1y2, dxdx);
    	dv1z = Fixed.multiply(dv1z1 - dv1z2, dxdx);
	}

	@Override
	protected void initializeDx2GreaterDx1() {
		super.initializeDx2GreaterDx1();
		dv0x = Fixed.multiply(dv0x2 - dv0x1, dxdx);
    	dv0y = Fixed.multiply(dv0y2 - dv0y1, dxdx);
    	dv0z = Fixed.multiply(dv0z2 - dv0z1, dxdx);
    	dv1x = Fixed.multiply(dv1x2 - dv1x1, dxdx);
    	dv1y = Fixed.multiply(dv1y2 - dv1y1, dxdx);
    	dv1z = Fixed.multiply(dv1z2 - dv1z1, dxdx);
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
		Vector.copy(vector00, vector);
	}
	
	public void setVector01(int[] vector) {
		Vector.copy(vector01, vector);
	}
	
	public void setVector02(int[] vector) {
		Vector.copy(vector02, vector);
	}
	
	public void setVector10(int[] vector) {
		Vector.copy(vector10, vector);
	}
	
	public void setVector11(int[] vector) {
		Vector.copy(vector11, vector);
	}
	
	public void setVector12(int[] vector) {
		Vector.copy(vector12, vector);
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
