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
		this.vector2 = Vector.emptyVector();
		this.vector20 = Vector.emptyVector();
		this.vector21 = Vector.emptyVector();
		this.vector22 = Vector.emptyVector();
		this.vector23 = Vector.emptyVector();
		this.vector3 = Vector.emptyVector();
		this.vector30 = Vector.emptyVector();
		this.vector31 = Vector.emptyVector();
		this.vector32 = Vector.emptyVector();
		this.vector33 = Vector.emptyVector();
	}
	
	public void linearDraw4(Face face, Frustum frustum) {
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
			Vector.swap(vector20, vector21);
			Vector.swap(vector30, vector31);
		}
		if (sortY10 > sortY11) {
			Vector.swap(vector21, vector22);
			Vector.swap(vector31, vector32);
		}
		if (sortY20 > sortY21) {
			Vector.swap(vector20, vector21);
			Vector.swap(vector30, vector31);
		}
	}

	@Override
	protected int splitTriangle() {
        int dy = super.splitTriangle();
        vector23[VECTOR_X] = vector20[VECTOR_X] + Fixed.multiply(dy, vector22[VECTOR_X] - vector20[VECTOR_X]);
        vector23[VECTOR_Y] = vector20[VECTOR_Y] + Fixed.multiply(dy, vector22[VECTOR_Y] - vector20[VECTOR_Y]);
        vector23[VECTOR_Z] = vector20[VECTOR_Z] + Fixed.multiply(dy, vector22[VECTOR_Z] - vector20[VECTOR_Z]);
        vector33[VECTOR_X] = vector30[VECTOR_X] + Fixed.multiply(dy, vector32[VECTOR_X] - vector30[VECTOR_X]);
        vector33[VECTOR_Y] = vector30[VECTOR_Y] + Fixed.multiply(dy, vector32[VECTOR_Y] - vector30[VECTOR_Y]);
        vector33[VECTOR_Z] = vector30[VECTOR_Z] + Fixed.multiply(dy, vector32[VECTOR_Z] - vector30[VECTOR_Z]);
        return dy;
	}

	@Override
	protected void swapSplitedBottomTriangle() {
		super.swapSplitedBottomTriangle();
		Vector.swap(vector23, vector22);
		Vector.swap(vector33, vector32);
	}

	@Override
	protected void swapSplitedTopTriangle() {
		super.swapSplitedTopTriangle();
	    Vector.swap(vector23, vector22);
	    Vector.swap(vector33, vector32);	
	    Vector.swap(vector20, vector21);
	    Vector.swap(vector30, vector31);
	    Vector.swap(vector21, vector23);
	    Vector.swap(vector31, vector33);
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

	@Override
	protected void initializeBottomTriangle() {
		super.initializeBottomTriangle();
		dv2x1 = Fixed.multiply(vector21[VECTOR_X] - vector20[VECTOR_X], y2y1Shifted);
		dv2x2 = Fixed.multiply(vector22[VECTOR_X] - vector20[VECTOR_X], y3y1Shifted);
		dv2y1 = Fixed.multiply(vector21[VECTOR_Y] - vector20[VECTOR_Y], y2y1Shifted);
		dv2y2 = Fixed.multiply(vector22[VECTOR_Y] - vector20[VECTOR_Y], y3y1Shifted);
		dv2z1 = Fixed.multiply(vector21[VECTOR_Z] - vector20[VECTOR_Z], y2y1Shifted);
		dv2z2 = Fixed.multiply(vector22[VECTOR_Z] - vector20[VECTOR_Z], y3y1Shifted);
		dv3x1 = Fixed.multiply(vector31[VECTOR_X] - vector30[VECTOR_X], y2y1Shifted);
		dv3x2 = Fixed.multiply(vector32[VECTOR_X] - vector30[VECTOR_X], y3y1Shifted);
		dv3y1 = Fixed.multiply(vector31[VECTOR_Y] - vector30[VECTOR_Y], y2y1Shifted);
		dv3y2 = Fixed.multiply(vector32[VECTOR_Y] - vector30[VECTOR_Y], y3y1Shifted);
		dv3z1 = Fixed.multiply(vector31[VECTOR_Z] - vector30[VECTOR_Z], y2y1Shifted);
		dv3z2 = Fixed.multiply(vector32[VECTOR_Z] - vector30[VECTOR_Z], y3y1Shifted);
        v2x = vector20[VECTOR_X];
        v2y = vector20[VECTOR_Y];
        v2z = vector20[VECTOR_Z];
        v3x = vector30[VECTOR_X];
        v3y = vector30[VECTOR_Y];
        v3z = vector30[VECTOR_Z];
	}

	@Override
	protected void incrementBottomDx2GreaterDx1() {
		super.incrementBottomDx2GreaterDx1();
		v2x += dv2x1;
        v2y += dv2y1;
        v2z += dv2z1;
        v3x += dv3x1;
        v3y += dv3y1;
        v3z += dv3z1;
	}

	@Override
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

	@Override
	protected void initializeTopTriangle() {
		super.initializeTopTriangle();
		dv2x1 = Fixed.multiply(vector22[VECTOR_X] - vector20[VECTOR_X], y3y1Shifted);
		dv2x2 = Fixed.multiply(vector22[VECTOR_X] - vector21[VECTOR_X], y3y2Shifted);
		dv2y1 = Fixed.multiply(vector22[VECTOR_Y] - vector20[VECTOR_Y], y3y1Shifted);
		dv2y2 = Fixed.multiply(vector22[VECTOR_Y] - vector21[VECTOR_Y], y3y2Shifted);
		dv2z1 = Fixed.multiply(vector22[VECTOR_Z] - vector20[VECTOR_Z], y3y1Shifted);
		dv2z2 = Fixed.multiply(vector22[VECTOR_Z] - vector21[VECTOR_Z], y3y2Shifted);
		dv3x1 = Fixed.multiply(vector32[VECTOR_X] - vector30[VECTOR_X], y3y1Shifted);
		dv3x2 = Fixed.multiply(vector32[VECTOR_X] - vector31[VECTOR_X], y3y2Shifted);
		dv3y1 = Fixed.multiply(vector32[VECTOR_Y] - vector30[VECTOR_Y], y3y1Shifted);
		dv3y2 = Fixed.multiply(vector32[VECTOR_Y] - vector31[VECTOR_Y], y3y2Shifted);
		dv3z1 = Fixed.multiply(vector32[VECTOR_Z] - vector30[VECTOR_Z], y3y1Shifted);
		dv3z2 = Fixed.multiply(vector32[VECTOR_Z] - vector31[VECTOR_Z], y3y2Shifted);
        v2x = vector22[VECTOR_X];
        v2y = vector22[VECTOR_Y];
        v2z = vector22[VECTOR_Z];
        v3x = vector32[VECTOR_X];
        v3y = vector32[VECTOR_Y];
        v3z = vector32[VECTOR_Z];
	}

	@Override
	protected void incrementTopDx2GreaterDx1() {
		super.incrementTopDx2GreaterDx1();
        v2x -= dv2x1;
        v2y -= dv2y1;
        v2z -= dv2z1;
        v3x -= dv3x1;
        v3y -= dv3y1;
        v3z -= dv3z1;
	}

	@Override
	protected void incrementTopDx1GreaterDx2() {
		super.incrementTopDx1GreaterDx2();
        v2x -= dv2x2;
        v2y -= dv2y2;
        v2z -= dv2z2;
        v3x -= dv3x2;
        v3y -= dv3y2;
        v3z -= dv3z2;
	}

	@Override
	protected void initializeDx1GreaterDx2() {
		super.initializeDx1GreaterDx2();
		dv2x = Fixed.multiply(dv2x1 - dv2x2, dxdx);
    	dv2y = Fixed.multiply(dv2y1 - dv2y2, dxdx);
    	dv2z = Fixed.multiply(dv2z1 - dv2z2, dxdx);
    	dv3x = Fixed.multiply(dv3x1 - dv3x2, dxdx);
    	dv3y = Fixed.multiply(dv3y1 - dv3y2, dxdx);
    	dv3z = Fixed.multiply(dv3z1 - dv3z2, dxdx);
	}

	@Override
	protected void initializeDx2GreaterDx1() {
		super.initializeDx2GreaterDx1();
		dv2x = Fixed.multiply(dv2x2 - dv2x1, dxdx);
    	dv2y = Fixed.multiply(dv2y2 - dv2y1, dxdx);
    	dv2z = Fixed.multiply(dv2z2 - dv2z1, dxdx);
    	dv3x = Fixed.multiply(dv3x2 - dv3x1, dxdx);
    	dv3y = Fixed.multiply(dv3y2 - dv3y1, dxdx);
    	dv3z = Fixed.multiply(dv3z2 - dv3z1, dxdx);
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
		Vector.copy(vector20, vector);
	}
	
	public void setVector21(int[] vector) {
		Vector.copy(vector21, vector);
	}
	
	public void setVector22(int[] vector) {
		Vector.copy(vector22, vector);
	}
	
	public void setVector30(int[] vector) {
		Vector.copy(vector30, vector);
	}
	
	public void setVector31(int[] vector) {
		Vector.copy(vector31, vector);
	}
	
	public void setVector32(int[] vector) {
		Vector.copy(vector32, vector);
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
