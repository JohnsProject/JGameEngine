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
package com.johnsproject.jpge2.processor;

import com.johnsproject.jpge2.dto.Transform;
import com.johnsproject.jpge2.shader.Shader;

public class GraphicsProcessor {

	private static final byte VECTOR_X = VectorProcessor.VECTOR_X;
	private static final byte VECTOR_Y = VectorProcessor.VECTOR_Y;
	private static final byte VECTOR_Z = VectorProcessor.VECTOR_Z;
	private static final byte VECTOR_W = VectorProcessor.VECTOR_W;

	private static final byte FP_BITS = MathProcessor.FP_BITS;
	private static final int FP_ONE = MathProcessor.FP_ONE;

	private static final byte INTERPOLATE_BITS = 25;
	private static final long INTERPOLATE_ONE = 1 << INTERPOLATE_BITS;

	private long oneByBarycentric = 0;

	private final long[] depthCache;
	private final int[] barycentricCache;
	private final int[] pixelChache;

	private final MathProcessor mathProcessor;
	private final MatrixProcessor matrixProcessor;
	private final VectorProcessor vectorProcessor;

	GraphicsProcessor(MathProcessor mathProcessor, MatrixProcessor matrixProcessor, VectorProcessor vectorProcessor) {
		this.mathProcessor = mathProcessor;
		this.matrixProcessor = matrixProcessor;
		this.vectorProcessor = vectorProcessor;

		this.depthCache = new long[3];
		this.barycentricCache = this.vectorProcessor.generate();
		this.pixelChache = this.vectorProcessor.generate();
	}

	public int[][] getModelMatrix(Transform transform, int[][] out) {
		int[] location = transform.getLocation();
		int[] rotation = transform.getRotation();
		int[] scale = transform.getScale();

		matrixProcessor.scale(out, scale[VECTOR_X], scale[VECTOR_Y], scale[VECTOR_Z], out);
		matrixProcessor.rotateX(out, rotation[VECTOR_X], out);
		matrixProcessor.rotateY(out, rotation[VECTOR_Y], out);
		matrixProcessor.rotateZ(out, rotation[VECTOR_Z], out);
		matrixProcessor.translate(out, location[VECTOR_X], location[VECTOR_Y], location[VECTOR_Z], out);
		return out;
	}

	public int[][] getNormalMatrix(Transform transform, int[][] out) {
		int[] rotation = transform.getRotation();
		int[] scale = transform.getScale();

		matrixProcessor.scale(out, scale[VECTOR_X], scale[VECTOR_Y], scale[VECTOR_Z], out);
		matrixProcessor.rotateX(out, rotation[VECTOR_X], out);
		matrixProcessor.rotateY(out, rotation[VECTOR_Y], out);
		matrixProcessor.rotateZ(out, rotation[VECTOR_Z], out);
		return out;
	}

	public int[][] getViewMatrix(Transform transform, int[][] out) {
		int[] location = transform.getLocation();
		int[] rotation = transform.getRotation();

		matrixProcessor.translate(out, -location[VECTOR_X], -location[VECTOR_Y], -location[VECTOR_Z], out);
		matrixProcessor.rotateZ(out, -rotation[VECTOR_Z], out);
		matrixProcessor.rotateY(out, -rotation[VECTOR_Y], out);
		matrixProcessor.rotateX(out, -rotation[VECTOR_X], out);
		return out;
	}

	public int[][] getOrthographicMatrix(int[] cameraCanvas, int[] cameraFrustum, int[][] out) {
		int scaleFactor = (cameraCanvas[3] >> 6) + 1;
		out[0][0] = (cameraFrustum[0] * scaleFactor * FP_BITS);
		out[1][1] = (cameraFrustum[0] * scaleFactor * FP_BITS);
		out[2][2] = -FP_BITS;
		out[3][3] = -FP_ONE * FP_ONE;
		return out;
	}

	public int[][] getPerspectiveMatrix(int[] cameraCanvas, int[] cameraFrustum, int[][] out) {
		int scaleFactor = (cameraCanvas[3] >> 6) + 1;
		out[0][0] = (cameraFrustum[0] * scaleFactor) << FP_BITS;
		out[1][1] = (cameraFrustum[0] * scaleFactor) << FP_BITS;
		out[2][2] = -FP_BITS;
		out[2][3] = FP_ONE * FP_ONE;
		return out;
	}

	public int[] viewport(int[] location, int[] cameraCanvas, int[] out) {
		int portX = cameraCanvas[VECTOR_X] + ((cameraCanvas[2] - cameraCanvas[VECTOR_X]) >> 1);
		int portY = cameraCanvas[VECTOR_Y] + ((cameraCanvas[3] - cameraCanvas[VECTOR_Y]) >> 1);
		out[VECTOR_X] = mathProcessor.divide(location[VECTOR_X], location[VECTOR_W]) + portX;
		out[VECTOR_Y] = mathProcessor.divide(location[VECTOR_Y], location[VECTOR_W]) + portY;
		return out;
	}

	public boolean isBackface(int[] location1, int[] location2, int[] location3) {
		return (location2[VECTOR_X] - location1[VECTOR_X]) * (location3[VECTOR_Y] - location1[VECTOR_Y])
				- (location3[VECTOR_X] - location1[VECTOR_X]) * (location2[VECTOR_Y] - location1[VECTOR_Y]) <= 0;
	}

	public boolean isInsideFrustum(int[] location1, int[] location2, int[] location3, int[] cameraCanvas, int[] cameraFrustum) {
		int xleft = cameraCanvas[VECTOR_X];
		int yleft = cameraCanvas[VECTOR_Y];
		int xright = cameraCanvas[2];
		int yright = cameraCanvas[3];

		boolean insideWidth = (location1[VECTOR_X] > xleft) & (location1[VECTOR_X] < xright);
		boolean insideHeight = (location1[VECTOR_Y] > yleft) & (location1[VECTOR_Y] < yright);
		boolean insideDepth = (location1[VECTOR_Z] > cameraFrustum[1]) & (location1[VECTOR_Z] < cameraFrustum[2]);
		boolean location1Inside = insideWidth & insideHeight & insideDepth;

		insideWidth = (location2[VECTOR_X] > xleft) & (location2[VECTOR_X] < xright);
		insideHeight = (location2[VECTOR_Y] > yleft) & (location2[VECTOR_Y] < yright);
		insideDepth = (location2[VECTOR_Z] > cameraFrustum[1]) & (location2[VECTOR_Z] < cameraFrustum[2]);
		boolean location2Inside = insideWidth & insideHeight & insideDepth;

		insideWidth = (location3[VECTOR_X] > xleft) & (location3[VECTOR_X] < xright);
		insideHeight = (location3[VECTOR_Y] > yleft) & (location3[VECTOR_Y] < yright);
		insideDepth = (location3[VECTOR_Z] > cameraFrustum[1]) & (location3[VECTOR_Z] < cameraFrustum[2]);
		boolean location3Inside = insideWidth & insideHeight & insideDepth;

		return location1Inside | location2Inside | location3Inside;
	}

	public void drawTriangle(int[] location1, int[] location2, int[] location3, int[] cameraCanvas, Shader shader) {
		// compute boundig box of faces
		int minX = Math.min(location1[VECTOR_X], Math.min(location2[VECTOR_X], location3[VECTOR_X]));
		int minY = Math.min(location1[VECTOR_Y], Math.min(location2[VECTOR_Y], location3[VECTOR_Y]));

		int maxX = Math.max(location1[VECTOR_X], Math.max(location2[VECTOR_X], location3[VECTOR_X]));
		int maxY = Math.max(location1[VECTOR_Y], Math.max(location2[VECTOR_Y], location3[VECTOR_Y]));

		// clip against screen limits
		minX = Math.max(minX, cameraCanvas[VECTOR_X]);
		minY = Math.max(minY, cameraCanvas[VECTOR_Y]);
		maxX = Math.min(maxX, cameraCanvas[2]);
		maxY = Math.min(maxY, cameraCanvas[3]);

		location1[VECTOR_Z] = Math.max(1, location1[VECTOR_Z]);
		location2[VECTOR_Z] = Math.max(1, location2[VECTOR_Z]);
		location3[VECTOR_Z] = Math.max(1, location3[VECTOR_Z]);

		// triangle setup
		int a01 = location1[VECTOR_Y] - location2[VECTOR_Y];
		int a12 = location2[VECTOR_Y] - location3[VECTOR_Y];
		int a20 = location3[VECTOR_Y] - location1[VECTOR_Y];

		int b01 = location2[VECTOR_X] - location1[VECTOR_X];
		int b12 = location3[VECTOR_X] - location2[VECTOR_X];
		int b20 = location1[VECTOR_X] - location3[VECTOR_X];

		barycentricCache[3] = barycentric(location1, location2, location3);
		depthCache[0] = INTERPOLATE_ONE / location1[VECTOR_Z];
		depthCache[1] = INTERPOLATE_ONE / location2[VECTOR_Z];
		depthCache[2] = INTERPOLATE_ONE / location3[VECTOR_Z];
		oneByBarycentric = INTERPOLATE_ONE / barycentricCache[3];

		// barycentric coordinates at minX/minY edge
		pixelChache[VECTOR_X] = minX;
		pixelChache[VECTOR_Y] = minY;

		int barycentric0_row = barycentric(location2, location3, pixelChache);
		int barycentric1_row = barycentric(location3, location1, pixelChache);
		int barycentric2_row = barycentric(location1, location2, pixelChache);

		for (pixelChache[VECTOR_Y] = minY; pixelChache[VECTOR_Y] < maxY; pixelChache[VECTOR_Y]++) {

			barycentricCache[0] = barycentric0_row;
			barycentricCache[1] = barycentric1_row;
			barycentricCache[2] = barycentric2_row;

			for (pixelChache[VECTOR_X] = minX; pixelChache[VECTOR_X] < maxX; pixelChache[VECTOR_X]++) {
				if ((barycentricCache[0] | barycentricCache[1] | barycentricCache[2]) > 0) {
					pixelChache[VECTOR_Z] = interpolatDepth(depthCache, barycentricCache);
					shader.fragment(pixelChache, barycentricCache);
				}

				barycentricCache[0] += a12;
				barycentricCache[1] += a20;
				barycentricCache[2] += a01;
			}

			barycentric0_row += b12;
			barycentric1_row += b20;
			barycentric2_row += b01;
		}
	}

	private int interpolatDepth(long[] depth, int[] barycentric) {
		 long dotProduct = barycentric[0] * depth[0]
						 + barycentric[1] * depth[1]
						 + barycentric[2] * depth[2];
		 return (int) (((long)barycentric[3] << INTERPOLATE_BITS) / dotProduct);
	}

	public int interpolate(int[] values, int[] barycentric) {
		 long dotProduct = values[VECTOR_X] * depthCache[0] * barycentric[0]
						 + values[VECTOR_Y] * depthCache[1] * barycentric[1]
						 + values[VECTOR_Z] * depthCache[2] * barycentric[2];
		 // normalize values
		 long result = (dotProduct * pixelChache[VECTOR_Z]) >> INTERPOLATE_BITS;
		 result = (result * oneByBarycentric) >> INTERPOLATE_BITS;
		 return (int)result;
	}

	public int barycentric(int[] vector1, int[] vector2, int[] vector3) {
		return (vector2[VECTOR_X] - vector1[VECTOR_X]) * (vector3[VECTOR_Y] - vector1[VECTOR_Y])
				- (vector3[VECTOR_X] - vector1[VECTOR_X]) * (vector2[VECTOR_Y] - vector1[VECTOR_Y]);
	}
	
	public int[] portCanvas(int[] cameraCanvas, int[] frameBufferSize, int[] out) {
		out[0] = (frameBufferSize[0] * cameraCanvas[0]) / 100;
		out[1] = (frameBufferSize[1] * cameraCanvas[1]) / 100;
		out[2] = (frameBufferSize[0] * cameraCanvas[2]) / 100;
		out[3] = (frameBufferSize[1] * cameraCanvas[3]) / 100;
		return out;
	}
}
