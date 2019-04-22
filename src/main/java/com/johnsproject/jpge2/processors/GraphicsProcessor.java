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

import java.util.ArrayList;
import java.util.List;

import com.johnsproject.jpge2.dto.Camera;
import com.johnsproject.jpge2.dto.Face;
import com.johnsproject.jpge2.dto.FrameBuffer;
import com.johnsproject.jpge2.dto.Light;
import com.johnsproject.jpge2.dto.Model;
import com.johnsproject.jpge2.dto.Transform;
import com.johnsproject.jpge2.dto.Vertex;
import com.johnsproject.jpge2.processors.GraphicsProcessor.Shader.ShaderPass;

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
	
	private int[] frameBufferSize;
	private int[] cameraCanvas;
	private ShaderPass shaderPass;
	
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
	
	public void setup(int[] frameBufferSize, int[] cameraCanvas, ShaderPass shaderPass) {
		this.frameBufferSize = frameBufferSize;
		this.cameraCanvas = cameraCanvas;
		this.shaderPass = shaderPass;
	}

	public int[][] getModelMatrix(Transform transform, int[][] out) {
		int[] location = transform.getLocation();
		int[] rotation = transform.getRotation();
		int[] scale = transform.getScale();
		
		matrixProcessor.rotateX(out, rotation[VECTOR_X], out);
		matrixProcessor.rotateY(out, -rotation[VECTOR_Y], out);
		matrixProcessor.rotateZ(out, rotation[VECTOR_Z], out);
		matrixProcessor.scale(out, scale[VECTOR_X], scale[VECTOR_Y], scale[VECTOR_Z], out);
		matrixProcessor.translate(out, -location[VECTOR_X], location[VECTOR_Y], location[VECTOR_Z], out);
		return out;
	}

	public int[][] getNormalMatrix(Transform transform, int[][] out) {
		int[] rotation = transform.getRotation();
		int[] scale = transform.getScale();
		
		matrixProcessor.rotateX(out, rotation[VECTOR_X], out);
		matrixProcessor.rotateY(out, -rotation[VECTOR_Y], out);
		matrixProcessor.rotateZ(out, rotation[VECTOR_Z], out);
		matrixProcessor.scale(out, scale[VECTOR_X], scale[VECTOR_Y], scale[VECTOR_Z], out);
		return out;
	}
	
	public int[][] getViewMatrix(Transform transform, int[][] out) {
		int[] location = transform.getLocation();
		int[] rotation = transform.getRotation();
	
		matrixProcessor.translate(out, location[VECTOR_X], -location[VECTOR_Y], -location[VECTOR_Z], out);
		matrixProcessor.rotateZ(out, rotation[VECTOR_Z], out);
		matrixProcessor.rotateY(out, rotation[VECTOR_Y], out);
		matrixProcessor.rotateX(out, -rotation[VECTOR_X], out);
		return out;
	}

	public int[][] getOrthographicMatrix(int[] frustum, int[][] out) {
		int scaleFactor = (mathProcessor.multiply(frameBufferSize[1], cameraCanvas[3]) >> 6) + 1;
		out[0][0] = (frustum[0] * scaleFactor * FP_BITS);
		out[1][1] = (frustum[0] * scaleFactor * FP_BITS);
		out[2][2] = -FP_BITS;
		out[3][3] = -FP_ONE * FP_ONE;
		return out;
	}
	
	public int[][] getPerspectiveMatrix(int[] frustum, int[][] out) {
		int scaleFactor = (mathProcessor.multiply(frameBufferSize[1], cameraCanvas[3]) >> 6) + 1;
		out[0][0] = (frustum[0] * scaleFactor) << FP_BITS;
		out[1][1] = (frustum[0] * scaleFactor) << FP_BITS;
		out[2][2] = -FP_BITS;
		out[2][3] = FP_ONE * FP_ONE;
		return out;
	}

	public int[] viewport(int[] location, int[] out) {
		int portX = mathProcessor.multiply(cameraCanvas[VECTOR_X] + ((cameraCanvas[2] - cameraCanvas[VECTOR_X]) >> 1), frameBufferSize[0] - 1);
		int portY = mathProcessor.multiply(cameraCanvas[VECTOR_Y] + ((cameraCanvas[3] - cameraCanvas[VECTOR_Y]) >> 1), frameBufferSize[1] - 1);
		out[VECTOR_X] = mathProcessor.divide(location[VECTOR_X], location[VECTOR_W]) + portX;
		out[VECTOR_Y] = mathProcessor.divide(location[VECTOR_Y], location[VECTOR_W]) + portY;
		return out;
	}
	
	public boolean isBackface(int[] location1, int[] location2, int[] location3) {
		return barycentric(location1, location2, location3) <= 0;
	}
	
	public boolean isInsideFrustum(int[] location1, int[] location2, int[] location3, int[] frustum) {
		int xleft = mathProcessor.multiply(cameraCanvas[VECTOR_X], frameBufferSize[0]);
		int yleft = mathProcessor.multiply(cameraCanvas[VECTOR_Y], frameBufferSize[1]);
		int xright = mathProcessor.multiply(cameraCanvas[2], frameBufferSize[0]);
		int yright = mathProcessor.multiply(cameraCanvas[3], frameBufferSize[1]);
		
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
	
	public void drawTriangle(int[] location1, int[] location2, int[] location3) {
		
		// compute boundig box of faces
		int minX = Math.min(location1[VECTOR_X], Math.min(location2[VECTOR_X], location3[VECTOR_X]));
		int minY = Math.min(location1[VECTOR_Y], Math.min(location2[VECTOR_Y], location3[VECTOR_Y]));
		
		int maxX = Math.max(location1[VECTOR_X], Math.max(location2[VECTOR_X], location3[VECTOR_X]));
		int maxY = Math.max(location1[VECTOR_Y], Math.max(location2[VECTOR_Y], location3[VECTOR_Y]));

		// clip against screen limits
		minX = Math.max(minX, mathProcessor.multiply(cameraCanvas[VECTOR_X], frameBufferSize[0] - 1));
		minY = Math.max(minY, mathProcessor.multiply(cameraCanvas[VECTOR_Y], frameBufferSize[1] - 1));
		maxX = Math.min(maxX, mathProcessor.multiply(cameraCanvas[2], frameBufferSize[0] - 1));
		maxY = Math.min(maxY, mathProcessor.multiply(cameraCanvas[3], frameBufferSize[1] - 1));
		
		location1[VECTOR_Z] = Math.max(1, location1[VECTOR_Z]);
		location2[VECTOR_Z] = Math.max(1, location2[VECTOR_Z]);
		location3[VECTOR_Z] = Math.max(1, location3[VECTOR_Z]);
		
		// triangle setup
		int a01 = location1[VECTOR_Y] - location2[VECTOR_Y], b01 = location2[VECTOR_X] - location1[VECTOR_X];
	    int a12 = location2[VECTOR_Y] - location3[VECTOR_Y], b12 = location3[VECTOR_X] - location2[VECTOR_X];
	    int a20 = location3[VECTOR_Y] - location1[VECTOR_Y], b20 = location1[VECTOR_X] - location3[VECTOR_X];

	    barycentricCache[VECTOR_W] = barycentric(location1, location2, location3);
		depthCache[0] = INTERPOLATE_ONE / location1[VECTOR_Z];
		depthCache[1] = INTERPOLATE_ONE / location2[VECTOR_Z];
		depthCache[2] = INTERPOLATE_ONE / location3[VECTOR_Z];
		oneByBarycentric = INTERPOLATE_ONE / barycentricCache[VECTOR_W];
	    
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
					shaderPass.fragment(pixelChache, barycentricCache);
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
	
	private int interpolatDepth(long[] depth, int[] barycentric) {
		long dotProduct = barycentric[VECTOR_X] * depth[0]
						+ barycentric[VECTOR_Y] * depth[1]
						+ barycentric[VECTOR_Z] * depth[2];
		return (int) (((long)barycentric[VECTOR_W] << INTERPOLATE_BITS) / dotProduct);
	}
	
	public int interpolate(int[] values, int[] barycentric) {		
		long dotProduct = values[VECTOR_X] * depthCache[0] * barycentric[VECTOR_X]
						+ values[VECTOR_Y] * depthCache[1] * barycentric[VECTOR_Y]
						+ values[VECTOR_Z] * depthCache[2] * barycentric[VECTOR_Z];
		// normalize values
		long result = (dotProduct * pixelChache[VECTOR_Z]) >> INTERPOLATE_BITS;
		result = (result * oneByBarycentric) >> INTERPOLATE_BITS;
		return (int)result;
	}

	public int barycentric(int[] vector1, int[] vector2, int[] vector3) {
		return (vector2[VECTOR_X] - vector1[VECTOR_X]) * (vector3[VECTOR_Y] - vector1[VECTOR_Y])
				- (vector3[VECTOR_X] - vector1[VECTOR_X]) * (vector2[VECTOR_Y] - vector1[VECTOR_Y]);
	}
	
	public static abstract class Shader {
		
		private List<ShaderPass> passes;
		
		public Shader(CentralProcessor centralProcessor) {
			passes = new ArrayList<ShaderPass>();
		}
		
		public abstract void update(ShaderDataBuffer shaderDataBuffer);
		
		public final void addPass(ShaderPass pass) {
			passes.add(pass);
		}
		
		public final void removePass(ShaderPass pass) {
			passes.remove(pass);
		}
		
		public final List<ShaderPass> getPasses(){
			return passes;
		}
		
		public final void setup(int pass, Model model, Camera camera) {
			passes.get(pass).setup(model, camera);
		}
		
		public final void vertex(int pass, int index, Vertex vertex) {
			passes.get(pass).vertex(index, vertex);
		}

		public final void geometry(int pass, Face face) {
			passes.get(pass).geometry(face);
		}

		public final void fragment(int pass, int[] location, int[] barycentric) {
			passes.get(pass).fragment(location, barycentric);
		}
		
		public static abstract class ShaderPass {
			
			public ShaderPass(Shader shader) {
				shader.addPass(this);
			}
			
			public abstract void setup(Model model, Camera camera);
			
			public abstract void vertex(int index, Vertex vertex);

			public abstract void geometry(Face face);

			public abstract void fragment(int[] location, int[] barycentric);
			
		}
		
	}
	
	public static class ShaderDataBuffer {
		
		private FrameBuffer frameBuffer;
		private List<Light> lights;
		
		public FrameBuffer getFrameBuffer() {
			return frameBuffer;
		}
		
		public void setFrameBuffer(FrameBuffer frameBuffer) {
			this.frameBuffer = frameBuffer;
		}
		
		public List<Light> getLights() {
			return lights;
		}
		
		public void setLights(List<Light> lights) {
			this.lights = lights;
		}
	}
}
