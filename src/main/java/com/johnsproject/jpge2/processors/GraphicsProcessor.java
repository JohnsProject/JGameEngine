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
	
	private static final byte VECTOR_X = VectorProcessor.VECTOR_X;
	private static final byte VECTOR_Y = VectorProcessor.VECTOR_Y;
	private static final byte VECTOR_Z = VectorProcessor.VECTOR_Z;
	private static final byte VECTOR_W = VectorProcessor.VECTOR_W;
	
	private static final byte FP_BITS = MathProcessor.FP_BITS;
	private static final int FP_ONE = MathProcessor.FP_ONE;
	
	private static final int INTERPOLATE_SHIFT = FP_BITS * 2;
	
	private static final long[] depthCache = new long[3];
	private static final int[] barycentricCache = VectorProcessor.generate();
	private static final int[] pixelChache = VectorProcessor.generate();
	
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
		
		MatrixProcessor.rotateX(out, rotation[VECTOR_X], out);
		MatrixProcessor.rotateY(out, -rotation[VECTOR_Y], out);
		MatrixProcessor.rotateZ(out, -rotation[VECTOR_Z], out);
		MatrixProcessor.scale(out, scale[VECTOR_X], scale[VECTOR_Y], scale[VECTOR_Z], out);
		MatrixProcessor.translate(out, -location[VECTOR_X], location[VECTOR_Y], location[VECTOR_Z], out);
		return out;
	}

	public static int[][] getNormalMatrix(Transform transform, int[][] out) {
		int[] rotation = transform.getRotation();
		int[] scale = transform.getScale();
		
		MatrixProcessor.rotateX(out, rotation[VECTOR_X], out);
		MatrixProcessor.rotateY(out, -rotation[VECTOR_Y], out);
		MatrixProcessor.rotateZ(out, -rotation[VECTOR_Z], out);
		MatrixProcessor.scale(out, scale[VECTOR_X], scale[VECTOR_Y], scale[VECTOR_Z], out);
		return out;
	}
	
	public static int[][] getViewMatrix(Transform transform, int[][] out) {
		int[] location = transform.getLocation();
		int[] rotation = transform.getRotation();
	
		MatrixProcessor.translate(out, location[VECTOR_X], -location[VECTOR_Y], -location[VECTOR_Z], out);
		MatrixProcessor.rotateX(out, -rotation[VECTOR_X], out);
		MatrixProcessor.rotateY(out, rotation[VECTOR_Y], out);
		MatrixProcessor.rotateZ(out, rotation[VECTOR_Z], out);
		return out;
	}

	public static int[][] getOrthographicMatrix(int[] frustum, int[][] out) {
		int scaleFactor = (MathProcessor.multiply(frameBufferSize[1], cameraCanvas[3]) >> 6) + 1;
		out[0][0] = (FP_ONE * scaleFactor) << FP_BITS;
		out[1][1] = (FP_ONE * scaleFactor) << FP_BITS;
		out[2][2] = -FP_BITS;
		out[3][3] = (frustum[3] - frustum[2]) << (FP_BITS * 2);
		return out;
	}
	
	public static int[][] getPerspectiveMatrix(int[] frustum, int[][] out) {
		int scaleFactor = (MathProcessor.multiply(frameBufferSize[1], cameraCanvas[3]) >> 6) + 1;
		out[0][0] = (frustum[0] * scaleFactor) << FP_BITS;
		out[1][1] = (frustum[0] * scaleFactor) << FP_BITS;
		out[2][2] = -FP_BITS;
		out[2][3] = FP_ONE * FP_ONE;
		return out;
	}

	public static int[] viewport(int[] location, int[] out) {
		int portX = MathProcessor.multiply(cameraCanvas[VECTOR_X] + ((cameraCanvas[2] - cameraCanvas[VECTOR_X]) >> 1), frameBufferSize[0] - 1);
		int portY = MathProcessor.multiply(cameraCanvas[VECTOR_Y] + ((cameraCanvas[3] - cameraCanvas[VECTOR_Y]) >> 1), frameBufferSize[1] - 1);
		out[VECTOR_X] = MathProcessor.divide(location[VECTOR_X], location[VECTOR_W]) + portX;
		out[VECTOR_Y] = MathProcessor.divide(location[VECTOR_Y], location[VECTOR_W]) + portY;
		return out;
	}
	
	public static boolean isBackface(int[] location1, int[] location2, int[] location3) {
		return barycentric(location1, location2, location3) <= 0;
	}
	
	public static boolean isInsideFrustum(int[] location1, int[] location2, int[] location3, int[] frustum) {
		int xleft = MathProcessor.multiply(cameraCanvas[VECTOR_X], frameBufferSize[0]);
		int yleft = MathProcessor.multiply(cameraCanvas[VECTOR_Y], frameBufferSize[1]);
		int xright = MathProcessor.multiply(cameraCanvas[2], frameBufferSize[0]);
		int yright = MathProcessor.multiply(cameraCanvas[3], frameBufferSize[1]);
		
		boolean insideWidth = (location1[VECTOR_X] > xleft) && (location1[VECTOR_X] < xright);
		boolean insideHeight = (location1[VECTOR_Y] > yleft) && (location1[VECTOR_Y] < yright);
		boolean insideDepth = (location1[VECTOR_Z] > frustum[1]) && (location1[VECTOR_Z] < frustum[2]);
		boolean location1Inside = insideWidth && insideHeight && insideDepth;
		
		insideWidth = (location2[VECTOR_X] > xleft) && (location2[VECTOR_X] < xright);
		insideHeight = (location2[VECTOR_Y] > yleft) && (location2[VECTOR_Y] < yright);
		insideDepth = (location2[VECTOR_Z] > frustum[1]) && (location2[VECTOR_Z] < frustum[2]);
		boolean location2Inside = insideWidth && insideHeight && insideDepth;
		
		insideWidth = (location3[VECTOR_X] > xleft) && (location3[VECTOR_X] < xright);
		insideHeight = (location3[VECTOR_Y] > yleft) && (location3[VECTOR_Y] < yright);
		insideDepth = (location3[VECTOR_Z] > frustum[1]) && (location3[VECTOR_Z] < frustum[2]);
		boolean location3Inside = insideWidth && insideHeight && insideDepth;
		
		return location1Inside || location2Inside || location3Inside;
	}
	
	public static void drawTriangle(int[] location1, int[] location2, int[] location3) {
		
		// compute boundig box of faces
		int minX = Math.min(location1[VECTOR_X], Math.min(location2[VECTOR_X], location3[VECTOR_X]));
		int minY = Math.min(location1[VECTOR_Y], Math.min(location2[VECTOR_Y], location3[VECTOR_Y]));
		
		int maxX = Math.max(location1[VECTOR_X], Math.max(location2[VECTOR_X], location3[VECTOR_X]));
		int maxY = Math.max(location1[VECTOR_Y], Math.max(location2[VECTOR_Y], location3[VECTOR_Y]));

		// clip against screen limits
		minX = Math.max(minX, MathProcessor.multiply(cameraCanvas[VECTOR_X], frameBufferSize[0] - 1));
		minY = Math.max(minY, MathProcessor.multiply(cameraCanvas[VECTOR_Y], frameBufferSize[1] - 1));
		maxX = Math.min(maxX, MathProcessor.multiply(cameraCanvas[2], frameBufferSize[0] - 1));
		maxY = Math.min(maxY, MathProcessor.multiply(cameraCanvas[3], frameBufferSize[1] - 1));
		
		location1[VECTOR_Z] = Math.max(1, location1[VECTOR_Z]);
		location2[VECTOR_Z] = Math.max(1, location2[VECTOR_Z]);
		location3[VECTOR_Z] = Math.max(1, location3[VECTOR_Z]);
		
		// triangle setup
		int a01 = location1[VECTOR_Y] - location2[VECTOR_Y], b01 = location2[VECTOR_X] - location1[VECTOR_X];
	    int a12 = location2[VECTOR_Y] - location3[VECTOR_Y], b12 = location3[VECTOR_X] - location2[VECTOR_X];
	    int a20 = location3[VECTOR_Y] - location1[VECTOR_Y], b20 = location1[VECTOR_X] - location3[VECTOR_X];

	    int one = 1 << INTERPOLATE_SHIFT;
		depthCache[0] = one / location1[VECTOR_Z];
		depthCache[1] = one / location2[VECTOR_Z];
		depthCache[2] = one / location3[VECTOR_Z];
		barycentricCache[VECTOR_W] = barycentric(location1, location2, location3);
	    
	    // barycentric coordinates at minX/minY edge
	    pixelChache[VECTOR_X] = minX;
	    pixelChache[VECTOR_Y] = minY;
	    pixelChache[VECTOR_Z] = 0;
	    int barycentric0_row = barycentric(location2, location3, pixelChache);
	    int barycentric1_row = barycentric(location3, location1, pixelChache);
	    int barycentric2_row = barycentric(location1, location2, pixelChache);
	    
		for (pixelChache[VECTOR_Y] = minY; pixelChache[VECTOR_Y] < maxY; pixelChache[VECTOR_Y]++) {
			
			barycentricCache[VECTOR_X] = barycentric0_row;
			barycentricCache[VECTOR_Y] = barycentric1_row;
			barycentricCache[VECTOR_Z] = barycentric2_row;
			
			for (pixelChache[VECTOR_X] = minX; pixelChache[VECTOR_X] < maxX; pixelChache[VECTOR_X]++) {
				
				if ((barycentricCache[VECTOR_X] | barycentricCache[VECTOR_Y] | barycentricCache[VECTOR_Z]) > 0) {
					pixelChache[VECTOR_Z] = interpolatDepth(depthCache, barycentricCache);
					shader.fragment(pixelChache, barycentricCache);
				}
				
				barycentricCache[VECTOR_X] += a12;
				barycentricCache[VECTOR_Y] += a20;
				barycentricCache[VECTOR_Z] += a01;
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
		long dotProduct = values[VECTOR_X] * depthCache[0] * barycentric[VECTOR_X]
						+ values[VECTOR_Y] * depthCache[1] * barycentric[VECTOR_Y]
						+ values[VECTOR_Z] * depthCache[2] * barycentric[VECTOR_Z];
		// normalize values
		return (int) ((((dotProduct * pixelChache[VECTOR_Z])) / barycentric[VECTOR_W]) >> INTERPOLATE_SHIFT);
	}

	public static int barycentric(int[] vector1, int[] vector2, int[] vector3) {
		return (vector2[VECTOR_X] - vector1[VECTOR_X]) * (vector3[VECTOR_Y] - vector1[VECTOR_Y])
				- (vector3[VECTOR_X] - vector1[VECTOR_X]) * (vector2[VECTOR_Y] - vector1[VECTOR_Y]);
	}
	
	public interface Shader {
		
		public abstract void update(List<Light> lights, FrameBuffer frameBuffer);
		
		public abstract void setup(Model model, Camera camera);
		
		public abstract void vertex(int index, Vertex vertex);

		public abstract void geometry(Face face);

		public abstract void fragment(int[] location, int[] barycentric);
		
	}
	
}
