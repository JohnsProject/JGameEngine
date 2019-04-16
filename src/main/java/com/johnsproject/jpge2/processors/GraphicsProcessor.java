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
import com.johnsproject.jpge2.dto.FrameBuffer;
import com.johnsproject.jpge2.dto.Light;
import com.johnsproject.jpge2.dto.Model;
import com.johnsproject.jpge2.dto.Transform;
import com.johnsproject.jpge2.dto.Vertex;

public class GraphicsProcessor {

	private static final byte vx = VectorProcessor.VECTOR_X;
	private static final byte vy = VectorProcessor.VECTOR_Y;
	private static final byte vz = VectorProcessor.VECTOR_Z;
	private static final byte vw = VectorProcessor.VECTOR_W;
	
	private static final int INTERPOLATE_SHIFT = MathProcessor.FP_SHIFT * 2;
	
	private static final long[] depth = new long[3];
	private static final int[] barycentric = VectorProcessor.generate();
	private static final int[] pixel = VectorProcessor.generate();
	
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
		
		MatrixProcessor.rotateX(out, rotation[vx], out);
		MatrixProcessor.rotateY(out, -rotation[vy], out);
		MatrixProcessor.rotateZ(out, -rotation[vz], out);
		MatrixProcessor.scale(out, scale[vx], scale[vy], scale[vz], out);
		MatrixProcessor.translate(out, -location[vx], location[vy], location[vz], out);
		return out;
	}

	public static int[][] getNormalMatrix(Transform transform, int[][] out) {
		int[] rotation = transform.getRotation();
		int[] scale = transform.getScale();
		
		MatrixProcessor.rotateX(out, rotation[vx], out);
		MatrixProcessor.rotateY(out, -rotation[vy], out);
		MatrixProcessor.rotateZ(out, -rotation[vz], out);
		MatrixProcessor.scale(out, scale[vx], scale[vy], scale[vz], out);
		return out;
	}
	
	public static int[][] getViewMatrix(Transform transform, int[][] out) {
		int[] location = transform.getLocation();
		int[] rotation = transform.getRotation();
	
		MatrixProcessor.translate(out, location[vx], -location[vy], -location[vz], out);
		MatrixProcessor.rotateX(out, -rotation[vx], out);
		MatrixProcessor.rotateY(out, rotation[vy], out);
		MatrixProcessor.rotateZ(out, rotation[vz], out);
		return out;
	}

	public static int[][] getOrthographicMatrix(int[] frustum, int[][] out) {
		int scaleFactor = (frameBufferSize[1] >> 6) + 1;
		out[0][0] = (MathProcessor.FP_ONE * scaleFactor) << MathProcessor.FP_SHIFT;
		out[1][1] = (MathProcessor.FP_ONE * scaleFactor) << MathProcessor.FP_SHIFT;
		out[2][2] = -MathProcessor.FP_SHIFT;
		out[3][3] = (frustum[3] - frustum[2]) << (MathProcessor.FP_SHIFT * 2);
		return out;
	}
	
	public static int[][] getPerspectiveMatrix(int[] frustum, int[][] out) {
		int scaleFactor = (frameBufferSize[1] >> 6) + 1;
		out[0][0] = (frustum[0] * scaleFactor) << MathProcessor.FP_SHIFT;
		out[1][1] = (frustum[0] * scaleFactor) << MathProcessor.FP_SHIFT;
		out[2][2] = -MathProcessor.FP_SHIFT;
		out[2][3] = MathProcessor.FP_ONE * MathProcessor.FP_ONE;
		return out;
	}

	public static int[] viewport(int[] location, int[] out) {
		int portX = MathProcessor.multiply(cameraCanvas[vx] + ((cameraCanvas[2] - cameraCanvas[vx]) >> 1), frameBufferSize[0] - 1);
		int portY = MathProcessor.multiply(cameraCanvas[vy] + ((cameraCanvas[3] - cameraCanvas[vy]) >> 1), frameBufferSize[1] - 1);
		out[vx] = MathProcessor.divide(location[vx], location[vw]) + portX;
		out[vy] = MathProcessor.divide(location[vy], location[vw]) + portY;
		return out;
	}
	
	public static void drawTriangle(int[] location1, int[] location2, int[] location3) {
		
		int one = 1 << INTERPOLATE_SHIFT;
		depth[0] = one / location1[vz];
		depth[1] = one / location2[vz];
		depth[2] = one / location3[vz];
		barycentric[vw] = barycentric(location1, location2, location3);
		
		// compute boundig box of faces
		int minX = Math.min(location1[vx], Math.min(location2[vx], location3[vx]));
		int minY = Math.min(location1[vy], Math.min(location2[vy], location3[vy]));
		int maxX = Math.max(location1[vx], Math.max(location2[vx], location3[vx]));
		int maxY = Math.max(location1[vy], Math.max(location2[vy], location3[vy]));

		// clip against screen limits
		minX = Math.max(minX, MathProcessor.multiply(cameraCanvas[vx], frameBufferSize[0] - 1));
		minY = Math.max(minY, MathProcessor.multiply(cameraCanvas[vy], frameBufferSize[1] - 1));
		maxX = Math.min(maxX, MathProcessor.multiply(cameraCanvas[2], frameBufferSize[0] - 1));
		maxY = Math.min(maxY, MathProcessor.multiply(cameraCanvas[3], frameBufferSize[1] - 1));
		
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
					shader.fragment(pixel, barycentric);
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
	
	private static int interpolatDepth(long[] depth, int[] barycentric) {
		long dotProduct = barycentric[vx] * depth[0]
						+ barycentric[vy] * depth[1]
						+ barycentric[vz] * depth[2];
		return (int) (((long)barycentric[vw] << INTERPOLATE_SHIFT) / dotProduct);
	}
	
	public static int interpolate(int[] values, int[] barycentric) {
		// depth = vectorCache1;
		// pixel = vectorCache3;
		long dotProduct = values[vx] * depth[0] * barycentric[vx]
						+ values[vy] * depth[1] * barycentric[vy]
						+ values[vz] * depth[2] * barycentric[vz];
		// normalize values
		return (int) ((((dotProduct * pixel[vz])) / barycentric[vw]) >> INTERPOLATE_SHIFT);
	}

	public static int barycentric(int[] vector1, int[] vector2, int[] vector3) {
		return (vector2[vx] - vector1[vx]) * (vector3[vy] - vector1[vy])
				- (vector3[vx] - vector1[vx]) * (vector2[vy] - vector1[vy]);
	}

	public static interface Shader {

		public void update(List<Light> lights, FrameBuffer frameBuffer);
		
		public void setup(Model model, Camera camera);
		
		public void vertex(int index, Vertex vertex);

		public void geometry(Face face);

		public void fragment(int[] location, int[] barycentric);
	}
}
