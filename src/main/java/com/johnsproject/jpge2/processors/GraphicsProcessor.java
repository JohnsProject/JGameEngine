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
package com.johnsproject.jpge2.processors;

import com.johnsproject.jpge2.dto.Transform;
import com.johnsproject.jpge2.shaders.Shader;

public class GraphicsProcessor extends VectorProcessor {
	
	private static final int INTERPOLATE_SHIFT = FP_BITS * 2;
	
	private static final long[] depth = new long[3];
	private static final int[] barycentric = generate();
	private static final int[] pixel = generate();
	
	private static int[] frameBufferSize;
	private static int[] cameraCanvas;
	private static Shader shader;
	
	public static void setup(int[] frameBufferSize, int[] cameraCanvas, Shader shader) {
		GraphicsProcessor.frameBufferSize = frameBufferSize;
		GraphicsProcessor.cameraCanvas = cameraCanvas;
		GraphicsProcessor.shader = shader;
	}

	public static int[][] getModelMatrix(Transform transform, int[][] out) {
		int[] location = transform.getLocation();
		int[] rotation = transform.getRotation();
		int[] scale = transform.getScale();
		
		rotateX(out, rotation[VECTOR_X], out);
		rotateY(out, -rotation[VECTOR_Y], out);
		rotateZ(out, -rotation[VECTOR_Z], out);
		scale(out, scale[VECTOR_X], scale[VECTOR_Y], scale[VECTOR_Z], out);
		translate(out, -location[VECTOR_X], location[VECTOR_Y], location[VECTOR_Z], out);
		return out;
	}

	public static int[][] getNormalMatrix(Transform transform, int[][] out) {
		int[] rotation = transform.getRotation();
		int[] scale = transform.getScale();
		
		rotateX(out, rotation[VECTOR_X], out);
		rotateY(out, -rotation[VECTOR_Y], out);
		rotateZ(out, -rotation[VECTOR_Z], out);
		scale(out, scale[VECTOR_X], scale[VECTOR_Y], scale[VECTOR_Z], out);
		return out;
	}
	
	public static int[][] getViewMatrix(Transform transform, int[][] out) {
		int[] location = transform.getLocation();
		int[] rotation = transform.getRotation();
	
		translate(out, location[VECTOR_X], -location[VECTOR_Y], -location[VECTOR_Z], out);
		rotateX(out, -rotation[VECTOR_X], out);
		rotateY(out, rotation[VECTOR_Y], out);
		rotateZ(out, rotation[VECTOR_Z], out);
		return out;
	}

	public static int[][] getOrthographicMatrix(int[] frustum, int[][] out) {
		int scaleFactor = (frameBufferSize[1] >> 6) + 1;
		out[0][0] = (FP_ONE * scaleFactor) << FP_BITS;
		out[1][1] = (FP_ONE * scaleFactor) << FP_BITS;
		out[2][2] = -FP_BITS;
		out[3][3] = (frustum[3] - frustum[2]) << (FP_BITS * 2);
		return out;
	}
	
	public static int[][] getPerspectiveMatrix(int[] frustum, int[][] out) {
		int scaleFactor = (frameBufferSize[1] >> 6) + 1;
		out[0][0] = (frustum[0] * scaleFactor) << FP_BITS;
		out[1][1] = (frustum[0] * scaleFactor) << FP_BITS;
		out[2][2] = -FP_BITS;
		out[2][3] = FP_ONE * FP_ONE;
		return out;
	}

	public static int[] viewport(int[] location, int[] out) {
		int portX = multiply(cameraCanvas[VECTOR_X] + ((cameraCanvas[2] - cameraCanvas[VECTOR_X]) >> 1), frameBufferSize[0] - 1);
		int portY = multiply(cameraCanvas[VECTOR_Y] + ((cameraCanvas[3] - cameraCanvas[VECTOR_Y]) >> 1), frameBufferSize[1] - 1);
		out[VECTOR_X] = divide(location[VECTOR_X], location[VECTOR_W]) + portX;
		out[VECTOR_Y] = divide(location[VECTOR_Y], location[VECTOR_W]) + portY;
		return out;
	}
	
	public static void drawTriangle(int[] location1, int[] location2, int[] location3) {
		
		int one = 1 << INTERPOLATE_SHIFT;
		depth[0] = one / location1[VECTOR_Z];
		depth[1] = one / location2[VECTOR_Z];
		depth[2] = one / location3[VECTOR_Z];
		barycentric[VECTOR_W] = barycentric(location1, location2, location3);
		
		// compute boundig box of faces
		int minX = Math.min(location1[VECTOR_X], Math.min(location2[VECTOR_X], location3[VECTOR_X]));
		int minY = Math.min(location1[VECTOR_Y], Math.min(location2[VECTOR_Y], location3[VECTOR_Y]));
		int maxX = Math.max(location1[VECTOR_X], Math.max(location2[VECTOR_X], location3[VECTOR_X]));
		int maxY = Math.max(location1[VECTOR_Y], Math.max(location2[VECTOR_Y], location3[VECTOR_Y]));

		// clip against screen limits
		minX = Math.max(minX, multiply(cameraCanvas[VECTOR_X], frameBufferSize[0] - 1));
		minY = Math.max(minY, multiply(cameraCanvas[VECTOR_Y], frameBufferSize[1] - 1));
		maxX = Math.min(maxX, multiply(cameraCanvas[2], frameBufferSize[0] - 1));
		maxY = Math.min(maxY, multiply(cameraCanvas[3], frameBufferSize[1] - 1));
		
		// triangle setup
		int a01 = location1[VECTOR_Y] - location2[VECTOR_Y], b01 = location2[VECTOR_X] - location1[VECTOR_X];
	    int a12 = location2[VECTOR_Y] - location3[VECTOR_Y], b12 = location3[VECTOR_X] - location2[VECTOR_X];
	    int a20 = location3[VECTOR_Y] - location1[VECTOR_Y], b20 = location1[VECTOR_X] - location3[VECTOR_X];

	    // barycentric coordinates at minX/minY edge
	    pixel[VECTOR_X] = minX;
	    pixel[VECTOR_Y] = minY;
	    pixel[VECTOR_Z] = 0;
	    int barycentric0_row = barycentric(location2, location3, pixel);
	    int barycentric1_row = barycentric(location3, location1, pixel);
	    int barycentric2_row = barycentric(location1, location2, pixel);
	    
		for (pixel[VECTOR_Y] = minY; pixel[VECTOR_Y] < maxY; pixel[VECTOR_Y]++) {
			
			barycentric[VECTOR_X] = barycentric0_row;
			barycentric[VECTOR_Y] = barycentric1_row;
			barycentric[VECTOR_Z] = barycentric2_row;
			
			for (pixel[VECTOR_X] = minX; pixel[VECTOR_X] < maxX; pixel[VECTOR_X]++) {
				
				if ((barycentric[VECTOR_X] | barycentric[VECTOR_Y] | barycentric[VECTOR_Z]) >= 0) {
					pixel[VECTOR_Z] = interpolatDepth(depth, barycentric);
					shader.fragment(pixel, barycentric);
				}
				
				barycentric[VECTOR_X] += a12;
				barycentric[VECTOR_Y] += a20;
				barycentric[VECTOR_Z] += a01;
			}
			
			barycentric0_row += b12;
			barycentric1_row += b20;
			barycentric2_row += b01;
		}
	}
	
	private static int interpolatDepth(long[] depth, int[] barycentric) {
		long dotProduct = barycentric[VECTOR_X] * depth[0]
						+ barycentric[VECTOR_Y] * depth[1]
						+ barycentric[VECTOR_Z] * depth[2];
		return (int) (((long)barycentric[VECTOR_W] << INTERPOLATE_SHIFT) / dotProduct);
	}
	
	public static int interpolate(int[] values, int[] barycentric) {
		// depth = vectorCache1;
		// pixel = vectorCache3;
		long dotProduct = values[VECTOR_X] * depth[0] * barycentric[VECTOR_X]
						+ values[VECTOR_Y] * depth[1] * barycentric[VECTOR_Y]
						+ values[VECTOR_Z] * depth[2] * barycentric[VECTOR_Z];
		// normalize values
		return (int) ((((dotProduct * pixel[VECTOR_Z])) / barycentric[VECTOR_W]) >> INTERPOLATE_SHIFT);
	}

	public static int barycentric(int[] vector1, int[] vector2, int[] vector3) {
		return (vector2[VECTOR_X] - vector1[VECTOR_X]) * (vector3[VECTOR_Y] - vector1[VECTOR_Y])
				- (vector3[VECTOR_X] - vector1[VECTOR_X]) * (vector2[VECTOR_Y] - vector1[VECTOR_Y]);
	}
	
	
}
