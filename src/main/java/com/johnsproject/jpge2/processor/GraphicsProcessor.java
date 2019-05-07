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
import com.johnsproject.jpge2.primitive.Matrix;
import com.johnsproject.jpge2.primitive.Vector;
import com.johnsproject.jpge2.shader.Shader;

public class GraphicsProcessor {

	private static final byte VECTOR_X = Vector.VECTOR_X;
	private static final byte VECTOR_Y = Vector.VECTOR_Y;
	private static final byte VECTOR_Z = Vector.VECTOR_Z;
	private static final byte VECTOR_W = Vector.VECTOR_W;

	private static final byte FP_BITS = MathProcessor.FP_BITS;
	private static final int FP_ONE = MathProcessor.FP_ONE;

	private static final byte INTERPOLATE_BITS = 25;
	private static final long INTERPOLATE_ONE = 1 << INTERPOLATE_BITS;

	private static long oneByBarycentric = 0;

	private static final long[] depthCache = new long[3];
	private static final int[] barycentricCache = new int[4];
	private static final Vector pixelChache = new Vector();

	private GraphicsProcessor() { }

	public static Matrix getModelMatrix(Transform transform, Matrix out) {
		Vector location = transform.getLocation();
		Vector rotation = transform.getRotation();
		Vector scale = transform.getScale();
		out.scale(scale);
		out.rotateXYZ(rotation);
		out.translate(location);
		return out;
	}

	public static Matrix getNormalMatrix(Transform transform, Matrix out) {
		Vector rotation = transform.getRotation();
		Vector scale = transform.getScale();
		out.scale(scale);
		out.rotateXYZ(rotation);
		return out;
	}

	public static Matrix getViewMatrix(Transform transform, Matrix out) {
		Vector location = transform.getLocation();
		Vector rotation = transform.getRotation();
		location.invert();
		rotation.invert();
		out.translate(location);
		out.rotateZYX(rotation);
		location.invert();
		rotation.invert();
		return out;
	}

	public static Matrix getOrthographicMatrix(int[] cameraCanvas, int[] cameraFrustum, Matrix out) {
		int[][] values = out.getValues();
		int scaleFactor = (cameraCanvas[3] >> 6) + 1;
		values[0][0] = (cameraFrustum[0] * scaleFactor * FP_BITS);
		values[1][1] = (cameraFrustum[0] * scaleFactor * FP_BITS);
		values[2][2] = -FP_BITS;
		values[3][3] = -FP_ONE * FP_ONE;
		return out;
	}

	public static Matrix getPerspectiveMatrix(int[] cameraCanvas, int[] cameraFrustum, Matrix out) {
		int[][] values = out.getValues();
		int scaleFactor = (cameraCanvas[3] >> 6) + 1;
		values[0][0] = (cameraFrustum[0] * scaleFactor) << FP_BITS;
		values[1][1] = (cameraFrustum[0] * scaleFactor) << FP_BITS;
		values[2][2] = -FP_BITS;
		values[2][3] = FP_ONE * FP_ONE;
		return out;
	}

	public static Vector viewport(Vector location, int[] cameraCanvas, Vector out) {
		int portX = cameraCanvas[VECTOR_X] + ((cameraCanvas[2] - cameraCanvas[VECTOR_X]) >> 1);
		int portY = cameraCanvas[VECTOR_Y] + ((cameraCanvas[3] - cameraCanvas[VECTOR_Y]) >> 1);
		int[] outValues = out.getValues();
		outValues[VECTOR_X] = MathProcessor.divide(location.getX(), location.getW()) + portX;
		outValues[VECTOR_Y] = MathProcessor.divide(location.getY(), location.getW()) + portY;
		return out;
	}

	public static boolean isBackface(Vector location1, Vector location2, Vector location3) {
		return (location2.getX() - location1.getX()) * (location3.getY() - location1.getY())
				- (location3.getX() - location1.getX()) * (location2.getY() - location1.getY()) <= 0;
	}

	public static boolean isInsideFrustum(Vector location1, Vector location2, Vector location3, int[] cameraCanvas, int[] cameraFrustum) {
		int xleft = cameraCanvas[VECTOR_X];
		int yleft = cameraCanvas[VECTOR_Y];
		int xright = cameraCanvas[2];
		int yright = cameraCanvas[3];

		boolean insideWidth = (location1.getX() > xleft) & (location1.getX() < xright);
		boolean insideHeight = (location1.getY() > yleft) & (location1.getY() < yright);
		boolean insideDepth = (location1.getZ() > cameraFrustum[1]) & (location1.getZ() < cameraFrustum[2]);
		boolean location1Inside = insideWidth & insideHeight & insideDepth;

		insideWidth = (location2.getX() > xleft) & (location2.getX() < xright);
		insideHeight = (location2.getY() > yleft) & (location2.getY() < yright);
		insideDepth = (location2.getZ() > cameraFrustum[1]) & (location2.getZ() < cameraFrustum[2]);
		boolean location2Inside = insideWidth & insideHeight & insideDepth;

		insideWidth = (location3.getX() > xleft) & (location3.getX() < xright);
		insideHeight = (location3.getY() > yleft) & (location3.getY() < yright);
		insideDepth = (location3.getZ() > cameraFrustum[1]) & (location3.getZ() < cameraFrustum[2]);
		boolean location3Inside = insideWidth & insideHeight & insideDepth;

		return location1Inside | location2Inside | location3Inside;
	}

	public static void drawTriangle(Vector location1, Vector location2, Vector location3, int[] cameraCanvas, Shader shader) {
		// compute boundig box of faces
		int minX = Math.min(location1.getX(), Math.min(location2.getX(), location3.getX()));
		int minY = Math.min(location1.getY(), Math.min(location2.getY(), location3.getY()));

		int maxX = Math.max(location1.getX(), Math.max(location2.getX(), location3.getX()));
		int maxY = Math.max(location1.getY(), Math.max(location2.getY(), location3.getY()));
		
		// clip against screen limits
		minX = Math.max(minX, cameraCanvas[VECTOR_X]);
		minY = Math.max(minY, cameraCanvas[VECTOR_Y]);
		maxX = Math.min(maxX, cameraCanvas[2]);
		maxY = Math.min(maxY, cameraCanvas[3]);

		location1.getValues()[VECTOR_Z] = Math.max(1, location1.getZ());
		location2.getValues()[VECTOR_Z] = Math.max(1, location2.getZ());
		location3.getValues()[VECTOR_Z] = Math.max(1, location3.getZ());

		// triangle setup
		int a01 = location1.getY() - location2.getY();
		int a12 = location2.getY() - location3.getY();
		int a20 = location3.getY() - location1.getY();

		int b01 = location2.getX() - location1.getX();
		int b12 = location3.getX() - location2.getX();
		int b20 = location1.getX() - location3.getX();

		barycentricCache[3] = barycentric(location1, location2, location3);
		depthCache[0] = INTERPOLATE_ONE / location1.getZ();
		depthCache[1] = INTERPOLATE_ONE / location2.getZ();
		depthCache[2] = INTERPOLATE_ONE / location3.getZ();
		oneByBarycentric = INTERPOLATE_ONE / barycentricCache[3];

		// barycentric coordinates at minX/minY edge
		pixelChache.getValues()[VECTOR_X] = minX;
		pixelChache.getValues()[VECTOR_Y] = minY;

		int barycentric0_row = barycentric(location2, location3, pixelChache);
		int barycentric1_row = barycentric(location3, location1, pixelChache);
		int barycentric2_row = barycentric(location1, location2, pixelChache);

		for (pixelChache.getValues()[VECTOR_Y] = minY; pixelChache.getY() < maxY; pixelChache.getValues()[VECTOR_Y]++) {
			barycentricCache[0] = barycentric0_row;
			barycentricCache[1] = barycentric1_row;
			barycentricCache[2] = barycentric2_row;
			
			for (pixelChache.getValues()[VECTOR_X] = minX; pixelChache.getX() < maxX; pixelChache.getValues()[VECTOR_X]++) {
				if ((barycentricCache[0] | barycentricCache[1] | barycentricCache[2]) > 0) {
					pixelChache.getValues()[VECTOR_Z] = interpolatDepth(depthCache, barycentricCache);
					shader.fragment(pixelChache);
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

	private static int interpolatDepth(long[] depth, int[] barycentric) {
		 long dotProduct = barycentric[0] * depth[0]
						 + barycentric[1] * depth[1]
						 + barycentric[2] * depth[2];
		 return (int) (((long)barycentric[3] << INTERPOLATE_BITS) / dotProduct);
	}

	public static int interpolate(Vector values) {
		 long dotProduct = values.getX() * depthCache[0] * barycentricCache[0]
						 + values.getY() * depthCache[1] * barycentricCache[1]
						 + values.getZ() * depthCache[2] * barycentricCache[2];
		 // normalize values
		 long result = (dotProduct * pixelChache.getZ()) >> INTERPOLATE_BITS;
		 result = (result * oneByBarycentric) >> INTERPOLATE_BITS;
		 return (int)result;
	}

	public static int barycentric(Vector location1, Vector location2, Vector location3) {
		return (location2.getX() - location1.getX()) * (location3.getY() - location1.getY())
				- (location3.getX() - location1.getX()) * (location2.getY() - location1.getY());
	}
	
	public static int[] portCanvas(int[] cameraCanvas, int width, int height, int[] out) {
		out[0] = (width * cameraCanvas[0]) / 100;
		out[1] = (height * cameraCanvas[1]) / 100;
		out[2] = (width * cameraCanvas[2]) / 100;
		out[3] = (height * cameraCanvas[3]) / 100;
		return out;
	}
}
