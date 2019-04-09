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

import java.util.List;

import com.johnsproject.jpge2.dto.Camera;
import com.johnsproject.jpge2.dto.Face;
import com.johnsproject.jpge2.dto.GraphicsBuffer;
import com.johnsproject.jpge2.dto.Light;
import com.johnsproject.jpge2.dto.Model;
import com.johnsproject.jpge2.dto.Transform;
import com.johnsproject.jpge2.dto.Vertex;

public class GraphicsProcessor {

	private static final int vx = VectorProcessor.VECTOR_X;
	private static final int vy = VectorProcessor.VECTOR_Y;
	private static final int vz = VectorProcessor.VECTOR_Z;
	private static final int vw = VectorProcessor.VECTOR_W;
	
	private static final int[] vectorCache1 = VectorProcessor.generate();
	private static final int[] vectorCache2 = VectorProcessor.generate();
	private static final int[] vectorCache3 = VectorProcessor.generate();

	public static int[][] modelMatrix(int[][] matrix, Transform transform) {
		int[] location = transform.getLocation();
		int[] rotation = transform.getRotation();
		int[] scale = transform.getScale();
		MatrixProcessor.reset(matrix);
		MatrixProcessor.rotateX(matrix, rotation[vx]);
		MatrixProcessor.rotateY(matrix, -rotation[vy]);
		MatrixProcessor.rotateZ(matrix, -rotation[vz]);
		MatrixProcessor.scale(matrix, scale[vx], scale[vy], scale[vz]);
		MatrixProcessor.translate(matrix, -location[vx], location[vy], location[vz]);
		return matrix;
	}

	public static int[][] normalMatrix(int[][] matrix, Transform transform) {
		int[] rotation = transform.getRotation();
		int[] scale = transform.getScale();
		MatrixProcessor.reset(matrix);
		MatrixProcessor.rotateX(matrix, rotation[vx]);
		MatrixProcessor.rotateY(matrix, -rotation[vy]);
		MatrixProcessor.rotateZ(matrix, -rotation[vz]);
		MatrixProcessor.scale(matrix, scale[vx], scale[vy], scale[vz]);
		return matrix;
	}
	
	public static int[][] viewMatrix(int[][] matrix, Transform transform) {
		int[] location = transform.getLocation();
		int[] rotation = transform.getRotation();
		MatrixProcessor.reset(matrix);
		MatrixProcessor.translate(matrix, location[vx], -location[vy], -location[vz]);
		MatrixProcessor.rotateX(matrix, -rotation[vx]);
		MatrixProcessor.rotateY(matrix, rotation[vy]);
		MatrixProcessor.rotateZ(matrix, rotation[vz]);
		return matrix;
	}

	public static int[][] orthographicMatrix(int[][] matrix, int[] frustum) {
		MatrixProcessor.reset(matrix);
		matrix[0][0] = (MathProcessor.FP_VALUE) << MathProcessor.FP_SHIFT;
		matrix[1][1] = (MathProcessor.FP_VALUE) << MathProcessor.FP_SHIFT;
		matrix[2][2] = -MathProcessor.FP_SHIFT;
		matrix[3][3] = -(frustum[3] - frustum[2]) << MathProcessor.FP_SHIFT;
		return matrix;
	}
	
	public static int[][] perspectiveMatrix(int[][] matrix, int[] frustum) {
		MatrixProcessor.reset(matrix);
		matrix[0][0] = (frustum[vx] * 10) << MathProcessor.FP_SHIFT;
		matrix[1][1] = (frustum[vx] * 10) << MathProcessor.FP_SHIFT;
		matrix[2][2] = -MathProcessor.FP_SHIFT;
		matrix[2][3] = MathProcessor.FP_VALUE;
		return matrix;
	}

	public static void viewport(int[] location, int[] canvas, int[] frustum) {
		location[vx] = MathProcessor.divide(location[vx], location[vw]) + (canvas[vz] >> 1);
		location[vy] = MathProcessor.divide(location[vy], location[vw]) + (canvas[vw] >> 1);
	}
	
	public static void drawTriangle(int[] location1, int[] location2, int[] location3, Shader shader, GraphicsBuffer graphicsBuffer) {

		int[] depth = vectorCache1;
		int[] barycentric = vectorCache2;
		int[] pixel = vectorCache3;
		
		depth[0] = location1[vz];
		depth[1] = location2[vz];
		depth[2] = location3[vz];
		barycentric[vw] = barycentric(location1, location2, location3);
		
		// compute boundig box of faces
		int minX = Math.min(location1[vx], Math.min(location2[vx], location3[vx]));
		int minY = Math.min(location1[vy], Math.min(location2[vy], location3[vy]));
		int maxX = Math.max(location1[vx], Math.max(location2[vx], location3[vx]));
		int maxY = Math.max(location1[vy], Math.max(location2[vy], location3[vy]));

		// clip against screen limits
		minX = Math.max(minX, 0);
		minY = Math.max(minY, 0);
		maxX = Math.min(maxX, graphicsBuffer.getWidth() - 1);
		maxY = Math.min(maxY, graphicsBuffer.getHeight() - 1);
		
		// triangle setup
		int a01 = location1[vy] - location2[vy], b01 = location2[vx] - location1[vx];
	    int a12 = location2[vy] - location3[vy], b12 = location3[vx] - location2[vx];
	    int a20 = location3[vy] - location1[vy], b20 = location1[vx] - location3[vx];

	    // barycentric coordinates at minX/minY edge
	    pixel[vx] = minX;
	    pixel[vy] = minY;
	    pixel[vz] = 0;
	    int barycentric0_row = barycentric(location2, location3, pixel);
	    int barycentric1_row = barycentric(location3, location1, pixel);
	    int barycentric2_row = barycentric(location1, location2, pixel);
	    
		for (pixel[vy] = minY; pixel[vy] < maxY; pixel[vy]++) {
			
			barycentric[vx] = barycentric0_row;
			barycentric[vy] = barycentric1_row;
			barycentric[vz] = barycentric2_row;
			
			for (pixel[vx] = minX; pixel[vx] < maxX; pixel[vx]++) {
				
				if ((barycentric[vx] | barycentric[vy] | barycentric[vz]) >= 0) {
					pixel[vz] = interpolatDepth(depth, barycentric);
					int color = shader.fragment(pixel, barycentric);
					graphicsBuffer.setPixel(pixel[vx], pixel[vy], pixel[vz], color);
				}
				
				barycentric[vx] += a12;
				barycentric[vy] += a20;
				barycentric[vz] += a01;
			}
			
			barycentric0_row += b12;
			barycentric1_row += b20;
			barycentric2_row += b01;
		}
	}

	private static int interpolatDepth(int[] depth, int[] barycentric) {
		// 10 bits of precision are not enought
		final byte shift = MathProcessor.FP_SHIFT << 1;
		long dotProduct = ((long) barycentric[vx] << shift) / depth[0]
						+ ((long) barycentric[vy] << shift) / depth[1]
						+ ((long) barycentric[vz] << shift) / depth[2];
		return (int) (((long) barycentric[vw] << shift) / dotProduct);
	}

	public static int interpolate(int[] values, int[] barycentric) {
		// depth = vectorCache1;
		// pixel = vectorCache3;
		long dotProduct = (((long) values[vx] << MathProcessor.FP_SHIFT) / vectorCache1[0]) * barycentric[vx]
				+ (((long) values[vy] << MathProcessor.FP_SHIFT) / vectorCache1[1]) * barycentric[vy]
				+ (((long) values[vz] << MathProcessor.FP_SHIFT) / vectorCache1[2]) * barycentric[vz];
		// normalize values
		return (int) ((((long) dotProduct * (long) vectorCache3[vz]) / barycentric[vw]) >> MathProcessor.FP_SHIFT);
	}

	public static int barycentric(int[] vector1, int[] vector2, int[] vector3) {
		return (vector2[vx] - vector1[vx]) * (vector3[vy] - vector1[vy])
				- (vector3[vx] - vector1[vx]) * (vector2[vy] - vector1[vy]);
	}

	public static abstract class Shader {

		protected static final int vx = VectorProcessor.VECTOR_X;
		protected static final int vy = VectorProcessor.VECTOR_Y;
		protected static final int vz = VectorProcessor.VECTOR_Z;
		protected static final int vw = VectorProcessor.VECTOR_W;

		public static Model model;
		public static Camera camera;
		public static GraphicsBuffer graphicsBuffer;
		public static List<Light> lights;

		public abstract void vertex(int index, Vertex vertex);

		public abstract void geometry(Face face);

		public abstract int fragment(int[] location, int[] barycentric);
	}
}
