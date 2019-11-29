/**
 * MIT License
 *
 * Copyright (c) 2018 John Salomon - John´s Project
 *  
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.johnsproject.jgameengine.rasterizer;

import static com.johnsproject.jgameengine.math.FixedPointMath.*;
import static com.johnsproject.jgameengine.math.VectorMath.*;

import com.johnsproject.jgameengine.math.FixedPointMath;
import com.johnsproject.jgameengine.math.VectorMath;
import com.johnsproject.jgameengine.model.Camera;
import com.johnsproject.jgameengine.shader.FragmentBuffer;
import com.johnsproject.jgameengine.shader.GeometryBuffer;
import com.johnsproject.jgameengine.shader.Shader;


public class FlatRasterizer {
	
	public static final byte INTERPOLATE_BIT = 5;
	public static final byte INTERPOLATE_ONE = 1 << INTERPOLATE_BIT;
	public static final byte FP_PLUS_INTERPOLATE_BIT = FP_BIT + INTERPOLATE_BIT;

	protected final Shader shader;
	protected final FragmentBuffer fragmentBuffer;
	protected final int[] location0;
	protected final int[] location1;
	protected final int[] location2;
	protected final int[] cameraFrustum;
	protected final int[] vectorCache;
	protected boolean frustumCull;
	protected int faceCull;
	
	public FlatRasterizer(Shader shader) {
		this.shader = shader;
		this.fragmentBuffer = new FragmentBuffer();
		this.vectorCache = VectorMath.toVector();
		this.location0 = VectorMath.toVector();
		this.location1 = VectorMath.toVector();
		this.location2 = VectorMath.toVector();
		this.cameraFrustum = new int[Camera.FRUSTUM_SIZE];
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

	protected final void setLocation0(int[] location) {
		VectorMath.copy(location0, location);
	}
	
	protected final void setLocation1(int[] location) {
		VectorMath.copy(location1, location);
	}
	
	protected final void setLocation2(int[] location) {
		VectorMath.copy(location2, location);
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
	public void draw(GeometryBuffer geometryBuffer) {
		copyFrustum(this.cameraFrustum, shader.getShaderBuffer().getCamera().getRenderTargetPortedFrustum());
		VectorMath.copy(location0, geometryBuffer.getVertexBuffer(0).getLocation());
		VectorMath.copy(location1, geometryBuffer.getVertexBuffer(1).getLocation());
		VectorMath.copy(location2, geometryBuffer.getVertexBuffer(2).getLocation());
		if(cull()) {
			return;
		}
		if (location0[VECTOR_Y] > location1[VECTOR_Y]) {
			VectorMath.swap(location0, location1);
		}
		if (location1[VECTOR_Y] > location2[VECTOR_Y]) {
			VectorMath.swap(location1, location2);
		}
		if (location0[VECTOR_Y] > location1[VECTOR_Y]) {
			VectorMath.swap(location0, location1);
		}
        if (location1[VECTOR_Y] == location2[VECTOR_Y]) {
        	drawBottomTriangle();
        } else if (location0[VECTOR_Y] == location1[VECTOR_Y]) {
        	drawTopTriangle();
        } else {
            int x = location0[VECTOR_X];
            int y = location1[VECTOR_Y];
            int z = location0[VECTOR_Z];
            int dy = FixedPointMath.divide(location1[VECTOR_Y] - location0[VECTOR_Y], location2[VECTOR_Y] - location0[VECTOR_Y]);
            int multiplier = location2[VECTOR_X] - location0[VECTOR_X];
            x += FixedPointMath.multiply(dy, multiplier);
            multiplier = location2[VECTOR_Z] - location0[VECTOR_Z];
            z += FixedPointMath.multiply(dy, multiplier);
            vectorCache[VECTOR_X] = x;
            vectorCache[VECTOR_Y] = y;
            vectorCache[VECTOR_Z] = z;
            VectorMath.swap(vectorCache, location2);
            drawBottomTriangle();
            VectorMath.swap(vectorCache, location2);
            VectorMath.swap(location0, location1);
            VectorMath.swap(location1, vectorCache);
            drawTopTriangle();
        }
	}
	
	private void drawBottomTriangle() {
		int xShifted = location0[VECTOR_X] << FP_BIT;
		int y2y1 = location1[VECTOR_Y] - location0[VECTOR_Y];
		int z2z1 = location1[VECTOR_Z] - location0[VECTOR_Z];
		y2y1 = y2y1 == 0 ? 1 : y2y1;
		z2z1 = z2z1 == 0 ? 1 : z2z1;
		int y3y1 = y2y1;
        int dx1 = FixedPointMath.divide(location1[VECTOR_X] - location0[VECTOR_X], y2y1);
        int dx2 = FixedPointMath.divide(location2[VECTOR_X] - location0[VECTOR_X], y3y1);
        int dz1 = FixedPointMath.divide(location1[VECTOR_Z] - location0[VECTOR_Z], y2y1);
        int dz2 = FixedPointMath.divide(location2[VECTOR_Z] - location0[VECTOR_Z], y3y1);
        int x1 = xShifted;
        int x2 = xShifted;
        int z = location0[VECTOR_Z] << FP_BIT;
        int y1 = location0[VECTOR_Y];
        int y2 = location1[VECTOR_Y];
        if(cameraFrustum[Camera.FRUSTUM_BOTTOM] < y2)
	    	y2 = cameraFrustum[Camera.FRUSTUM_BOTTOM];
        if(dx1 < dx2) {
        	int dxdx = dx2 - dx1;
        	dxdx = dxdx == 0 ? 1 : dxdx;
        	int dz = FixedPointMath.divide(dz2 - dz1, dxdx);
	        if (y1 < cameraFrustum[Camera.FRUSTUM_TOP]) {
	        	int step = cameraFrustum[Camera.FRUSTUM_TOP] - y1;
	        	y1 += step;
		    	x1 += dx1 * step;
	            x2 += dx2 * step;
	            z += dz1 * step;
	    	}
	        for (; y1 <= y2; y1++) {
	        	drawScanline(x1, x2, y1, z, dz);
	            x1 += dx1;
	            x2 += dx2;
	            z += dz1;
	        }
        } else {
        	int dxdx = dx1 - dx2;
        	dxdx = dxdx == 0 ? 1 : dxdx;
        	int dz = FixedPointMath.divide(dz1 - dz2, dxdx);
            if (y1 < cameraFrustum[Camera.FRUSTUM_TOP]) {
	        	int step = cameraFrustum[Camera.FRUSTUM_TOP] - y1;
	        	y1 += step;
		    	x1 += dx2 * step;
	            x2 += dx1 * step;
	            z += dz2 * step;
	    	}
        	for (; y1 <= y2; y1++) {
        		drawScanline(x1, x2, y1, z, dz);
	            x1 += dx2;
	            x2 += dx1;
	            z += dz2;
	        }
        }
    }
    
	private void drawTopTriangle() {
		int xShifted = location2[VECTOR_X] << FP_BIT;
		int y3y1 = location2[VECTOR_Y] - location0[VECTOR_Y];
		int y3y2 = location2[VECTOR_Y] - location1[VECTOR_Y];
		int z3z1 = location2[VECTOR_Z] - location0[VECTOR_Z];
		int z3z2 = location2[VECTOR_Z] - location1[VECTOR_Z];
		y3y1 = y3y1 == 0 ? 1 : y3y1;
		y3y2 = y3y2 == 0 ? 1 : y3y2;
		z3z1 = z3z1 == 0 ? 1 : z3z1;
		z3z2 = z3z2 == 0 ? 1 : z3z2;
		int dx1 = FixedPointMath.divide(location2[VECTOR_X] - location0[VECTOR_X], y3y1);
		int dx2 = FixedPointMath.divide(location2[VECTOR_X] - location1[VECTOR_X], y3y2);
		int dz1 = FixedPointMath.divide(location2[VECTOR_Z] - location0[VECTOR_Z], y3y1);
		int dz2 = FixedPointMath.divide(location2[VECTOR_Z] - location1[VECTOR_Z], y3y2);
		int x1 = xShifted;
		int x2 = xShifted;
		int z = location2[VECTOR_Z] << FP_BIT;
		int y1 = location2[VECTOR_Y];
        int y2 = location0[VECTOR_Y];
        if(cameraFrustum[Camera.FRUSTUM_TOP] > y2)
	    	y2 = cameraFrustum[Camera.FRUSTUM_TOP];
		if (dx1 > dx2) {
			int dxdx = dx1 - dx2;
			dxdx = dxdx == 0 ? 1 : dxdx;
			int dz = FixedPointMath.divide(dz1 - dz2, dxdx);
	        if (y1 > cameraFrustum[Camera.FRUSTUM_BOTTOM]) {
	        	int step = y1 - cameraFrustum[Camera.FRUSTUM_BOTTOM];
	        	y1 -= step;
		        x1 -= dx1 * step;
		        x2 -= dx2 * step;
		        z -= dz1 * step;
	    	}
	        for (; y1 > y2; y1--) {
	        	drawScanline(x1, x2, y1, z, dz);
	            x1 -= dx1;
	            x2 -= dx2;
	            z -= dz1;
	        }
		} else {
			int dxdx = dx2 - dx1;
			dxdx = dxdx == 0 ? 1 : dxdx;
			int dz = FixedPointMath.divide(dz2 - dz1, dxdx);
	        if (y1 > cameraFrustum[Camera.FRUSTUM_BOTTOM]) {
	        	int step = y1 - cameraFrustum[Camera.FRUSTUM_BOTTOM];
	        	y1 -= step;
		        x1 -= dx2 * step;
		        x2 -= dx1 * step;
		        z -= dz2 * step;
	    	}
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
	    if (x1 < cameraFrustum[Camera.FRUSTUM_LEFT]) {
	    	int step = cameraFrustum[Camera.FRUSTUM_LEFT] - x1;
	    	x1 += step;
	    	z += dz * step;
	    }
	    if(cameraFrustum[Camera.FRUSTUM_RIGHT] < x2)
	    	x2 = cameraFrustum[Camera.FRUSTUM_RIGHT];
		for (; x1 <= x2; x1++) {
			fragmentBuffer.getLocation()[VECTOR_X] = x1;
			fragmentBuffer.getLocation()[VECTOR_Y] = y;
			fragmentBuffer.getLocation()[VECTOR_Z] = z >> FP_BIT;
			shader.fragment(fragmentBuffer);
			z += dz;
		}
	}
	
	protected boolean cull() {
		if(frustumCull) {
			int left = cameraFrustum[Camera.FRUSTUM_LEFT];
			int right = cameraFrustum[Camera.FRUSTUM_RIGHT];
			int top = cameraFrustum[Camera.FRUSTUM_TOP];
			int bottom = cameraFrustum[Camera.FRUSTUM_BOTTOM];
			int near = 1;
			int far = FP_ONE;
			boolean insideWidth1 = (location0[VECTOR_X] > left) && (location0[VECTOR_X] < right);
			boolean insideWidth2 = (location1[VECTOR_X] > left) && (location1[VECTOR_X] < right);
			boolean insideWidth3 = (location2[VECTOR_X] > left) && (location2[VECTOR_X] < right);
			boolean insideHeight1 = (location0[VECTOR_Y] > top) && (location0[VECTOR_Y] < bottom);
			boolean insideHeight2 = (location1[VECTOR_Y] > top) && (location1[VECTOR_Y] < bottom);
			boolean insideHeight3 = (location2[VECTOR_Y] > top) && (location2[VECTOR_Y] < bottom);
			boolean insideDepth1 = (location0[VECTOR_Z] > near) && (location0[VECTOR_Z] < far);
			boolean insideDepth2 = (location1[VECTOR_Z] > near) && (location1[VECTOR_Z] < far);
			boolean insideDepth3 = (location2[VECTOR_Z] > near) && (location2[VECTOR_Z] < far);
			if ((!insideDepth1 && !insideDepth2 && !insideDepth3) 
					|| (!insideHeight1 && !insideHeight2 && !insideHeight3)
						|| (!insideWidth1 && !insideWidth2 && !insideWidth3)) {
						return true;
			}
		}
		int size = (location1[VECTOR_X] - location0[VECTOR_X]) * (location2[VECTOR_Y] - location0[VECTOR_Y])
				- (location2[VECTOR_X] - location0[VECTOR_X]) * (location1[VECTOR_Y] - location0[VECTOR_Y]);
		return size * faceCull < 0;
	}
	
	protected void divideOneByZ() {
		location0[VECTOR_Z] = FixedPointMath.divide(INTERPOLATE_ONE, location0[VECTOR_Z]);
		location1[VECTOR_Z] = FixedPointMath.divide(INTERPOLATE_ONE, location1[VECTOR_Z]);
		location2[VECTOR_Z] = FixedPointMath.divide(INTERPOLATE_ONE, location2[VECTOR_Z]);
	}
	
	protected void zMultiply(int[] vector) {
		vector[0] = FixedPointMath.multiply(vector[0], location0[VECTOR_Z]);
		vector[1] = FixedPointMath.multiply(vector[1], location1[VECTOR_Z]);
		vector[2] = FixedPointMath.multiply(vector[2], location2[VECTOR_Z]);
	}
	
	protected void copyFrustum(int[] target, int[] source) {
		for (int i = 0; i < Camera.FRUSTUM_SIZE; i++) {
			target[i] = source[i];
		}
	}
	
	protected void swapVector(int[] vector0, int[] vector1, int currentIndex, int indexToSet) {
		int tmp = 0;
		tmp = vector0[currentIndex]; vector0[currentIndex] = vector0[indexToSet]; vector0[indexToSet] = tmp;
		tmp = vector1[currentIndex]; vector1[currentIndex] = vector1[indexToSet]; vector1[indexToSet] = tmp;
	}
	
	protected void swapVector(int[] vector0, int[] vector1, int[] vector2, int currentIndex, int indexToSet) {
		int tmp = 0;
		tmp = vector0[currentIndex]; vector0[currentIndex] = vector0[indexToSet]; vector0[indexToSet] = tmp;
		tmp = vector1[currentIndex]; vector1[currentIndex] = vector1[indexToSet]; vector1[indexToSet] = tmp;
		tmp = vector2[currentIndex]; vector2[currentIndex] = vector2[indexToSet]; vector2[indexToSet] = tmp;
	}
	
	protected void swapCache(int[] vector0, int[] vector1, int[] cache, int indexToSet) {
		int tmp = 0;
		tmp = cache[0]; cache[0] = vector0[indexToSet]; vector0[indexToSet] = tmp;
		tmp = cache[1]; cache[1] = vector1[indexToSet]; vector1[indexToSet] = tmp;
	}
	
	protected void swapCache(int[] vector0, int[] vector1, int[] vector2, int[] cache, int indexToSet) {
		int tmp = 0;
		tmp = cache[0]; cache[0] = vector0[indexToSet]; vector0[indexToSet] = tmp;
		tmp = cache[1]; cache[1] = vector1[indexToSet]; vector1[indexToSet] = tmp;
		tmp = cache[2]; cache[2] = vector2[indexToSet]; vector2[indexToSet] = tmp;
	}
}
