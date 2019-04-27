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
	
	private static final byte RASTERIZE_BITS = 12;
	
	private static final byte INTERPOLATE_BITS = 25;
	private static final long INTERPOLATE_ONE = 1 << INTERPOLATE_BITS;
	
	private long oneByBarycentric = 0;
	
	private final long[] depthCache;
	private final int[] barycentricCache;
	private final int[] pixelChache;
	
	private int[] frameBufferSize;
	private int[] cameraCanvas;
	private Shader shader;
	
	private boolean swap1;
	private boolean swap2;
	private boolean swap3;
	private boolean swap4;
	
	private int part;
	private int condition;
	
	private int dxdx;
	
	private int ydist;
	private int xdist;
	
	private final int[] loc1;
	private final int[] loc2;
	private final int[] loc3;
	
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
		
		this.loc1 = this.vectorProcessor.generate();
		this.loc2 = this.vectorProcessor.generate();
		this.loc3 = this.vectorProcessor.generate();
	}
	
	public void setup(int[] frameBufferSize, int[] cameraCanvas, Shader shader) {
		this.frameBufferSize = frameBufferSize;
		this.cameraCanvas = cameraCanvas;
		this.shader = shader;
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
		
		return location1Inside && location2Inside && location3Inside;
	}
	
//	public void drawTriangle(int[] location1, int[] location2, int[] location3) {
//		// compute boundig box of faces
//		int minX = Math.min(location1[VECTOR_X], Math.min(location2[VECTOR_X], location3[VECTOR_X]));
//		int minY = Math.min(location1[VECTOR_Y], Math.min(location2[VECTOR_Y], location3[VECTOR_Y]));
//		
//		int maxX = Math.max(location1[VECTOR_X], Math.max(location2[VECTOR_X], location3[VECTOR_X]));
//		int maxY = Math.max(location1[VECTOR_Y], Math.max(location2[VECTOR_Y], location3[VECTOR_Y]));
//
//		// clip against screen limits
//		minX = Math.max(minX, mathProcessor.multiply(cameraCanvas[VECTOR_X], frameBufferSize[0]));
//		minY = Math.max(minY, mathProcessor.multiply(cameraCanvas[VECTOR_Y], frameBufferSize[1]));
//		maxX = Math.min(maxX, mathProcessor.multiply(cameraCanvas[2], frameBufferSize[0]));
//		maxY = Math.min(maxY, mathProcessor.multiply(cameraCanvas[3], frameBufferSize[1]));
//		
//		location1[VECTOR_Z] = Math.max(1, location1[VECTOR_Z]);
//		location2[VECTOR_Z] = Math.max(1, location2[VECTOR_Z]);
//		location3[VECTOR_Z] = Math.max(1, location3[VECTOR_Z]);
//		
//		// triangle setup
//		int a01 = location1[VECTOR_Y] - location2[VECTOR_Y];
//	    int a12 = location2[VECTOR_Y] - location3[VECTOR_Y];
//	    int a20 = location3[VECTOR_Y] - location1[VECTOR_Y];
//	    
//	    int b01 = location2[VECTOR_X] - location1[VECTOR_X];
//	    int b12 = location3[VECTOR_X] - location2[VECTOR_X];
//	    int b20 = location1[VECTOR_X] - location3[VECTOR_X];
//
//	    barycentricCache[3] = barycentric(location1, location2, location3);
//		depthCache[0] = INTERPOLATE_ONE / location1[VECTOR_Z];
//		depthCache[1] = INTERPOLATE_ONE / location2[VECTOR_Z];
//		depthCache[2] = INTERPOLATE_ONE / location3[VECTOR_Z];
//		oneByBarycentric = INTERPOLATE_ONE / barycentricCache[3];
//	    
//	    // barycentric coordinates at minX/minY edge
//	    pixelChache[VECTOR_X] = minX;
//	    pixelChache[VECTOR_Y] = minY;
//	    
//	    int barycentric0_row = barycentric(location2, location3, pixelChache);
//	    int barycentric1_row = barycentric(location3, location1, pixelChache);
//	    int barycentric2_row = barycentric(location1, location2, pixelChache);
//	    
//		for (pixelChache[VECTOR_Y] = minY; pixelChache[VECTOR_Y] < maxY; pixelChache[VECTOR_Y]++) {
//			
//			barycentricCache[0] = barycentric0_row;
//			barycentricCache[1] = barycentric1_row;
//			barycentricCache[2] = barycentric2_row;
//			
//			
//			for (pixelChache[VECTOR_X] = minX; pixelChache[VECTOR_X] < maxX; pixelChache[VECTOR_X]++) {
//				if ((barycentricCache[0] | barycentricCache[1] | barycentricCache[2]) > 0) {	
//					pixelChache[VECTOR_Z] = interpolatDepth(depthCache, barycentricCache);
//					shader.fragment(pixelChache, barycentricCache);
//				}
//				
//				barycentricCache[0] += a12;
//				barycentricCache[1] += a20;
//				barycentricCache[2] += a01;
//			}
//			
//			barycentric0_row += b12;
//			barycentric1_row += b20;
//			barycentric2_row += b01;
//		}
//	}
	
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
	
	public void drawTriangle(int[] location1, int[] location2, int[] location3) {
		vectorProcessor.copy(loc1, location1);
		vectorProcessor.copy(loc2, location2);
		vectorProcessor.copy(loc3, location3);
		// y sorting
		if (loc1[VECTOR_Y] > loc2[VECTOR_Y]) {
			vectorProcessor.swap(loc1, loc2);
		}
		if (loc2[VECTOR_Y] > loc3[VECTOR_Y]) {
			vectorProcessor.swap(loc2, loc3);
		}
		if (loc1[VECTOR_Y] > loc2[VECTOR_Y]) {
			vectorProcessor.swap(loc1, loc2);
		}
		if ((loc1[VECTOR_Y] == loc2[VECTOR_Y])
				&& (loc1[VECTOR_X] - loc3[VECTOR_X] < loc2[VECTOR_X] - loc3[VECTOR_X])) {
			vectorProcessor.swap(loc1, loc2);
		}
		
		barycentricCache[3] = barycentric(location1, location2, location3);
		depthCache[0] = INTERPOLATE_ONE / location1[VECTOR_Z];
		depthCache[1] = INTERPOLATE_ONE / location2[VECTOR_Z];
		depthCache[2] = INTERPOLATE_ONE / location3[VECTOR_Z];
		oneByBarycentric = INTERPOLATE_ONE / barycentricCache[3];
	    
	   	int barycentric11 = barycentric(location2, location3, loc1);
	   	int barycentric12 = barycentric(location3, location1, loc1);
	   	int barycentric13 = barycentric(location1, location2, loc1);
		
	   	int barycentric21 = barycentric(location2, location3, loc2);
	   	int barycentric22 = barycentric(location3, location1, loc2);
	   	int barycentric23 = barycentric(location1, location2, loc2);
	   	
		int barycentric31 = barycentric(location2, location3, loc3);
	   	int barycentric32 = barycentric(location3, location1, loc3);
	   	int barycentric33 = barycentric(location1, location2, loc3);
		
		// delta x (how much x changes for each y value)
		int dx1 = 0;
		int dx2 = 0;
		int dx3 = 0;
		// delta z (how much z changes for each x value)
		int dz = 0;
		// delta z (how much z changes for each y value)
		int dz1 = 0;
		int dz2 = 0;
		int dz3 = 0;
		// delta z (how much z changes for each x value)
		int db1 = 0;
		int db2 = 0;
		int db3 = 0;
		// delta z (how much z changes for each y value)
		int db11 = 0;
		int db12 = 0;
		int db13 = 0;
		int db21 = 0;
		int db22 = 0;
		int db23 = 0;
		int db31 = 0;
		int db32 = 0;
		int db33 = 0;
		// y distance
		int y2y1 = loc2[VECTOR_Y] - loc1[VECTOR_Y];
		int y3y1 = loc3[VECTOR_Y] - loc1[VECTOR_Y];
		int y3y2 = loc3[VECTOR_Y] - loc2[VECTOR_Y];
		if (y2y1 > 0) {
			dx1 = ((loc2[VECTOR_X] - loc1[VECTOR_X]) << RASTERIZE_BITS) /  y2y1;
			dz1 = ((loc2[VECTOR_Z] - loc1[VECTOR_Z]) << RASTERIZE_BITS) /  y2y1;
			db11 = ((barycentric12 - barycentric11) << RASTERIZE_BITS) /  y2y1;
			db21 = ((barycentric22 - barycentric21) << RASTERIZE_BITS) /  y2y1;
			db31 = ((barycentric32 - barycentric31) << RASTERIZE_BITS) /  y2y1;
		}
		if (y3y1 > 0) {
			dx2 = ((loc3[VECTOR_X] - loc1[VECTOR_X]) << RASTERIZE_BITS) /  y3y1;
			dz2 = ((loc3[VECTOR_Z] - loc1[VECTOR_Z]) << RASTERIZE_BITS) /  y3y1;
			db12 = ((barycentric13 - barycentric11) << RASTERIZE_BITS) /  y3y1;
			db22 = ((barycentric23 - barycentric21) << RASTERIZE_BITS) /  y3y1;
			db32 = ((barycentric33 - barycentric31) << RASTERIZE_BITS) /  y3y1;
		}
		if (y3y2 > 0) {
			dx3 = ((loc3[VECTOR_X] - loc2[VECTOR_X]) << RASTERIZE_BITS) /  y3y2;
			dz3 = ((loc3[VECTOR_Z] - loc2[VECTOR_Z]) << RASTERIZE_BITS) /  y3y2;
			db13 = ((barycentric13 - barycentric12) << RASTERIZE_BITS) /  y3y2;
			db23 = ((barycentric23 - barycentric22) << RASTERIZE_BITS) /  y3y2;
			db33 = ((barycentric33 - barycentric32) << RASTERIZE_BITS) /  y3y2;
		}
		
		int sx = loc1[VECTOR_X] << RASTERIZE_BITS;
		int ex = loc1[VECTOR_X] << RASTERIZE_BITS;
		int sz = loc1[VECTOR_Z] << RASTERIZE_BITS;
		int sb1 = barycentric11 << RASTERIZE_BITS;
		int sb2 = barycentric21 << RASTERIZE_BITS;
		int sb3 = barycentric31 << RASTERIZE_BITS;
		int sy = loc1[VECTOR_Y];
		if ((dx1 >= dx2) && (loc1[VECTOR_Y] != loc2[VECTOR_Y])) {
			int dxdx = dx1 - dx2;
			if (dxdx > 0) {
				dz = ((dz1 - dz2) << RASTERIZE_BITS) / dxdx;
				db1 = ((db11 - db12) << RASTERIZE_BITS) / dxdx;
				db2 = ((db21 - db22) << RASTERIZE_BITS) / dxdx;
				db3 = ((db31 - db32) << RASTERIZE_BITS) / dxdx;
			}
			for (; sy < loc2[VECTOR_Y]; sy++) {
				drawHorizontalLine(sx >> RASTERIZE_BITS, ex >> RASTERIZE_BITS, sz, sb1, sb2, sb3, dz, db1, db2, db3, sy, location1, location2, location3);
				sx += dx2;
				ex += dx1;
				sz += dz2;
				sb1 += db12;
				sb2 += db22;
				sb3 += db32;
			}
			ex = loc2[VECTOR_X] << RASTERIZE_BITS;
			dxdx = dx3 - dx2;
			if (dxdx > 0) {
				dz = ((dz3 - dz2) << RASTERIZE_BITS) / dxdx;
				db1 = ((db13 - db12) << RASTERIZE_BITS) / dxdx;
				db2 = ((db23 - db22) << RASTERIZE_BITS) / dxdx;
				db3 = ((db33 - db32) << RASTERIZE_BITS) / dxdx;
			}
			for (; sy < loc3[VECTOR_Y]; sy++) {
				drawHorizontalLine(sx >> RASTERIZE_BITS, ex >> RASTERIZE_BITS, sz, sb1, sb2, sb3, dz, db1, db2, db3, sy, location1, location2, location3);
				sx += dx2;
				ex += dx3;
				sz += dz2;
				sb1 += db12;
				sb2 += db22;
				sb3 += db32;
			}
		} else {
			int dxdx = dx2 - dx1;
			if (dxdx > 0) {
				dz = ((dz2 - dz1) << RASTERIZE_BITS) / dxdx;
				db1 = ((db12 - db11) << RASTERIZE_BITS) / dxdx;
				db2 = ((db22 - db21) << RASTERIZE_BITS) / dxdx;
				db3 = ((db32 - db31) << RASTERIZE_BITS) / dxdx;
			}
			for (; sy < loc2[VECTOR_Y]; sy++) {
				drawHorizontalLine(sx >> RASTERIZE_BITS, ex >> RASTERIZE_BITS, sz, sb1, sb2, sb3, dz, db1, db2, db3, sy, location1, location2, location3);
				sx += dx1;
				ex += dx2;
				sz += dz1;
				sb1 += db11;
				sb2 += db21;
				sb3 += db31;
			}
			sx = loc2[VECTOR_X] << RASTERIZE_BITS;
			dxdx = dx2 - dx3;
			if (dxdx > 0) {
				dz = ((dz2 - dz3) << RASTERIZE_BITS) / dxdx;
				db1 = ((db12 - db13) << RASTERIZE_BITS) / dxdx;
				db2 = ((db22 - db23) << RASTERIZE_BITS) / dxdx;
				db3 = ((db32 - db33) << RASTERIZE_BITS) / dxdx;
			}
			for (; sy < loc3[VECTOR_Y]; sy++) {
				drawHorizontalLine(sx >> RASTERIZE_BITS, ex >> RASTERIZE_BITS, sz, sb1, sb2, sb3, dz, db1, db2, db3, sy, location1, location2, location3);
				sx += dx3;
				ex += dx2;
				sz += dz3;
				sb1 += db13;
				sb2 += db23;
				sb3 += db33;
			}
		}
	}

	private void drawHorizontalLine(int sx, int ex, int sz, int sb1, int sb2, int sb3, int dz, int db1, int db2, int db3, int sy, int[] location1, int[] location2, int[] location3) {
		for (; sx < ex; sx++) {
			pixelChache[0] = sx;
			pixelChache[1] = sy;
			
			barycentricCache[0] = barycentric(location2, location3, pixelChache);
			barycentricCache[1] = barycentric(location3, location1, pixelChache);
			barycentricCache[2] = barycentric(location1, location2, pixelChache);
			
//			barycentricCache[0] = sb1 >> RASTERIZE_BITS;
//			barycentricCache[1] = sb2 >> RASTERIZE_BITS;
//			barycentricCache[2] = sb3 >> RASTERIZE_BITS;
			
			pixelChache[2] = interpolatDepth(depthCache, barycentricCache);
		
//			pixelChache[2] = sz >> RASTERIZE_BITS;
			shader.fragment(pixelChache, barycentricCache);
			sz += dz;
			sb1 += db1;
			sb2 += db2;
			sb3 += db3;
		}
	}
	
//	public int interpolate(int[] values, int[] barycentric) {		
//		int tmp = 0;
//		int value1 = values[0];
//		int value2 = values[1];
//		int value3 = values[2];
//		if (swap1) {
//			tmp = value1;
//			value1 = value2;
//			value2 = tmp;
//		}
//		if (swap2) {
//			tmp = value3;
//			value3 = value2;
//			value2 = tmp;
//		}
//		if (swap3) {
//			tmp = value1;
//			value1 = value2;
//			value2 = tmp;
//		}
//		if (swap4) {
//			tmp = value1;
//			value1 = value2;
//			value2 = tmp;
//		}
//		// delta z (how much z changes for each x value)
//		int dv = 0;
//		// delta z (how much z changes for each y value)
//		int dv1 = 0;
//		int dv2 = 0;
//		int dv3 = 0;
//		int y2y1 = loc2[VECTOR_Y] - loc1[VECTOR_Y];
//		int y3y1 = loc3[VECTOR_Y] - loc1[VECTOR_Y];
//		int y3y2 = loc3[VECTOR_Y] - loc2[VECTOR_Y];
//		if (y2y1 > 0) {
//			dv1 = ((value2 - value1) << RASTERIZE_BITS) / y2y1;
//		}
//		if (y3y1 > 0) {
//			dv2 = ((value3 - value1) << RASTERIZE_BITS) / y3y1;
//		}
//		if (y3y2 > 0) {
//			dv3 = ((value3 - value2) << RASTERIZE_BITS) / y3y2;
//		}
//		int sv = value1 << RASTERIZE_BITS;
//		if (condition == 0) {
//			if (part == 0) {
//				if (dxdx > 0) {
//					dv = ((dv1 - dv2) << RASTERIZE_BITS) / dxdx;
//				}
//			}
//			if (part == 1) {
//				if (dxdx > 0) {
//					dv = ((dv3 - dv2) << RASTERIZE_BITS) / dxdx;
//				}
//			}
//			sv += dv2 * ydist;
//		}
//		if (condition == 1) {
//			if (part == 0) {
//				if (dxdx > 0) {
//					dv = ((dv2 - dv1) << RASTERIZE_BITS) / dxdx;
//				}
//				sv += dv1 * ydist;
//			}
//			if (part == 1) {
//				if (dxdx > 0) {
//					dv = ((dv2 - dv3) << RASTERIZE_BITS) / dxdx;
//				}
//				sv += dv3 * ydist;
//			}
//		}
//		sv += dv * xdist;
//		return sv >> RASTERIZE_BITS;
//	}
//	
//	
//	public void drawTriangle(int[] location1, int[] location2, int[] location3) {
//		vectorProcessor.copy(loc1, location1);
//		vectorProcessor.copy(loc2, location2);
//		vectorProcessor.copy(loc3, location3);
//		swap1 = false;
//		swap2 = false;
//		swap3 = false;
//		swap4 = false;
//		// y sorting
//		if (loc1[VECTOR_Y] > loc2[VECTOR_Y]) {
//			vectorProcessor.swap(loc1, loc2);
//			swap1 = true;
//		}
//		if (loc2[VECTOR_Y] > loc3[VECTOR_Y]) {
//			vectorProcessor.swap(loc2, loc3);
//			swap2 = true;
//		}
//		if (loc1[VECTOR_Y] > loc2[VECTOR_Y]) {
//			vectorProcessor.swap(loc1, loc2);
//			swap3 = true;
//		}
//		if ((loc1[VECTOR_Y] == loc2[VECTOR_Y])
//				&& (loc1[VECTOR_X] - loc3[VECTOR_X] < loc2[VECTOR_X] - loc3[VECTOR_X])) {
//			vectorProcessor.swap(loc1, loc2);
//			swap4 = true;
//		}
//		
//		// delta x (how much x changes for each y value)
//		int dx1 = 0;
//		int dx2 = 0;
//		int dx3 = 0;
//		// delta z (how much z changes for each x value)
//		int dz = 0;
//		// delta z (how much z changes for each y value)
//		int dz1 = 0;
//		int dz2 = 0;
//		int dz3 = 0;
//		// y distance
//		int y2y1 = loc2[VECTOR_Y] - loc1[VECTOR_Y];
//		int y3y1 = loc3[VECTOR_Y] - loc1[VECTOR_Y];
//		int y3y2 = loc3[VECTOR_Y] - loc2[VECTOR_Y];
//		if (y2y1 > 0) {
//			dx1 = ((loc2[VECTOR_X] - loc1[VECTOR_X]) << RASTERIZE_BITS) /  y2y1;
//			dz1 = ((loc2[VECTOR_Z] - loc1[VECTOR_Z]) << RASTERIZE_BITS) /  y2y1;
//		}
//		if (y3y1 > 0) {
//			dx2 = ((loc3[VECTOR_X] - loc1[VECTOR_X]) << RASTERIZE_BITS) /  y3y1;
//			dz2 = ((loc3[VECTOR_Z] - loc1[VECTOR_Z]) << RASTERIZE_BITS) /  y3y1;
//		}
//		if (y3y2 > 0) {
//			dx3 = ((loc3[VECTOR_X] - loc2[VECTOR_X]) << RASTERIZE_BITS) /  y3y2;
//			dz3 = ((loc3[VECTOR_Z] - loc2[VECTOR_Z]) << RASTERIZE_BITS) /  y3y2;
//		}
//		
//		int sx = loc1[VECTOR_X] << RASTERIZE_BITS;
//		int ex = loc1[VECTOR_X] << RASTERIZE_BITS;
//		int sz = loc1[VECTOR_Z] << RASTERIZE_BITS;
//		int sy = loc1[VECTOR_Y];
//		if ((dx1 >= dx2) && (loc1[VECTOR_Y] != loc2[VECTOR_Y])) {
//			condition = 0;
//			part = 0;
//			dxdx = dx1 - dx2;
//			if (dxdx > 0) {
//				dz = ((dz1 - dz2) << RASTERIZE_BITS) / dxdx;
//			}
//			ydist = 0;
//			for (; sy < loc2[VECTOR_Y]; sy++) {
//				drawHorizontalLine(sx >> RASTERIZE_BITS, ex >> RASTERIZE_BITS, sz, dz, sy);
//				sx += dx2;
//				ex += dx1;
//				sz += dz2;
//				++ydist;
//			}
//			ex = loc2[VECTOR_X] << RASTERIZE_BITS;
//			part = 1;
//			dxdx = dx3 - dx2;
//			if (dxdx > 0) {
//				dz = ((dz3 - dz2) << RASTERIZE_BITS) / dxdx;
//			}
//			for (; sy < loc3[VECTOR_Y]; sy++) {
//				drawHorizontalLine(sx >> RASTERIZE_BITS, ex >> RASTERIZE_BITS, sz, dz, sy);
//				sx += dx2;
//				ex += dx3;
//				sz += dz2;
//				++ydist;
//			}
//		} else {
//			condition = 1;
//			part = 0;
//			dxdx = dx2 - dx1;
//			if (dxdx > 0) {
//				dz = ((dz2 - dz1) << RASTERIZE_BITS) / dxdx;
//			}
//			ydist = 0;
//			for (; sy < loc2[VECTOR_Y]; sy++) {
//				drawHorizontalLine(sx >> RASTERIZE_BITS, ex >> RASTERIZE_BITS, sz, dz, sy);
//				sx += dx1;
//				ex += dx2;
//				sz += dz1;
//				++ydist;
//			}
//			sx = loc2[VECTOR_X] << RASTERIZE_BITS;
//			part = 1;
//			dxdx = dx2 - dx3;
//			if (dxdx > 0) {
//				dz = ((dz2 - dz3) << RASTERIZE_BITS) / dxdx;
//			}
//			for (; sy < loc3[VECTOR_Y]; sy++) {
//				drawHorizontalLine(sx >> RASTERIZE_BITS, ex >> RASTERIZE_BITS, sz, dz, sy);
//				sx += dx3;
//				ex += dx2;
//				sz += dz3;
//				++ydist;
//			}
//		}
//	}
//
//	private void drawHorizontalLine(int sx, int ex, int sz, int dz, int sy) {
//		xdist = 0;
//		for (; sx < ex; sx++) {
//			pixelChache[0] = sx;
//			pixelChache[1] = sy;
//			pixelChache[2] = sz >> RASTERIZE_BITS;
//			shader.fragment(pixelChache, barycentricCache);
//			sz += dz;
//			++xdist;
//		}
//	}
}
