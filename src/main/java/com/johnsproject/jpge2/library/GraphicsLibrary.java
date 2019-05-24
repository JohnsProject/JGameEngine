package com.johnsproject.jpge2.library;

import com.johnsproject.jpge2.dto.Camera;
import com.johnsproject.jpge2.dto.Transform;
import com.johnsproject.jpge2.shader.Shader;

public class GraphicsLibrary {
	private static final byte VECTOR_X = VectorLibrary.VECTOR_X;
	private static final byte VECTOR_Y = VectorLibrary.VECTOR_Y;
	private static final byte VECTOR_Z = VectorLibrary.VECTOR_Z;
	private static final byte VECTOR_W = VectorLibrary.VECTOR_W;

	private static final int FP_ONE = MathLibrary.FP_ONE;
	private static final int FP_HALF = MathLibrary.FP_HALF;

	private static final byte RASTERIZE_BITS = 10;
	private static final int RASTERIZE_HALF = 1 << (RASTERIZE_BITS - 1);
	private static final byte INTERPOLATE_BITS = 25;
	private static final int INTERPOLATE_ONE = 1 << INTERPOLATE_BITS;

	private final int[] depthCache;
	private final int[] barycentricCache;
	private final int[] pixelCache;

	private final MathLibrary mathLibrary;
	private final MatrixLibrary matrixLibrary;
	private final VectorLibrary vectorLibrary;

	public GraphicsLibrary() {
		this.mathLibrary = new MathLibrary();
		this.matrixLibrary = new MatrixLibrary();
		this.vectorLibrary = new VectorLibrary();

		this.depthCache = vectorLibrary.generate();
		this.barycentricCache = vectorLibrary.generate();
		this.pixelCache = vectorLibrary.generate();
	}

	public int[][] modelMatrix(int[][] matrix, Transform transform) {
		int[] location = transform.getLocation();
		int[] rotation = transform.getRotation();
		int[] scale = transform.getScale();
		matrixLibrary.copy(matrix, MatrixLibrary.MATRIX_IDENTITY);
		matrixLibrary.scale(matrix, scale, matrix);
		matrixLibrary.rotateXYZ(matrix, rotation, matrix);
		matrixLibrary.translate(matrix, location, matrix);
		return matrix;
	}

	public int[][] normalMatrix(int[][] matrix, Transform transform) {
		int[] rotation = transform.getRotation();
		int[] scale = transform.getScale();
		matrixLibrary.copy(matrix, MatrixLibrary.MATRIX_IDENTITY);
		matrixLibrary.scale(matrix, scale, matrix);
		matrixLibrary.rotateXYZ(matrix, rotation, matrix);
		return matrix;
	}

	public int[][] viewMatrix(int[][] matrix, Transform transform) {
		int[] location = transform.getLocation();
		int[] rotation = transform.getRotation();
		matrixLibrary.copy(matrix, MatrixLibrary.MATRIX_IDENTITY);
		vectorLibrary.invert(location, location);
		vectorLibrary.invert(rotation, rotation);
		matrixLibrary.translate(matrix, location, matrix);
		matrixLibrary.rotateZYX(matrix, rotation, matrix);
		vectorLibrary.invert(location, location);
		vectorLibrary.invert(rotation, rotation);
		return matrix;
	}

	public int[][] orthographicMatrix(int[][] matrix, int[] cameraFrustum) {
		matrixLibrary.copy(matrix, MatrixLibrary.MATRIX_IDENTITY);
		int scaleFactor = (cameraFrustum[Camera.FRUSTUM_NEAR]) / 16;
		matrix[0][0] = scaleFactor;
		matrix[1][1] = scaleFactor;
		matrix[2][2] = -FP_ONE / 10;
		matrix[3][3] = -FP_ONE * FP_HALF;
		return matrix;
	}

	public int[][] perspectiveMatrix(int[][] matrix, int[] cameraFrustum) {
		matrixLibrary.copy(matrix, MatrixLibrary.MATRIX_IDENTITY);
		int scaleFactor = (cameraFrustum[Camera.FRUSTUM_NEAR]) / 16;
		matrix[0][0] = scaleFactor;
		matrix[1][1] = scaleFactor;
		matrix[2][2] = -FP_ONE / 10;
		matrix[2][3] = FP_ONE;
		matrix[3][3] = 0;
		return matrix;
	}

	public int[] viewport(int[] location, int[] cameraFrustum, int[] result) {
		int scaleFactor = ((cameraFrustum[Camera.FRUSTUM_BOTTOM] - cameraFrustum[Camera.FRUSTUM_TOP]) >> 6) + 1;
		int halfX = cameraFrustum[Camera.FRUSTUM_LEFT]
				+ ((cameraFrustum[Camera.FRUSTUM_RIGHT] - cameraFrustum[Camera.FRUSTUM_LEFT]) >> 1);
		int halfY = cameraFrustum[Camera.FRUSTUM_TOP]
				+ ((cameraFrustum[Camera.FRUSTUM_BOTTOM] - cameraFrustum[Camera.FRUSTUM_TOP]) >> 1);
		int w = Math.min(-1, location[VECTOR_W]);
		result[VECTOR_X] = mathLibrary.divide(location[VECTOR_X] * scaleFactor, w) + halfX;
		result[VECTOR_Y] = mathLibrary.divide(location[VECTOR_Y] * scaleFactor, w) + halfY;
		return result;
	}

	public int[] portFrustum(int[] cameraFrustum, int width, int height, int[] result) {
		result[Camera.FRUSTUM_LEFT] = mathLibrary.multiply(width, cameraFrustum[Camera.FRUSTUM_LEFT]);
		result[Camera.FRUSTUM_RIGHT] = mathLibrary.multiply(width, cameraFrustum[Camera.FRUSTUM_RIGHT]);
		result[Camera.FRUSTUM_TOP] = mathLibrary.multiply(height, cameraFrustum[Camera.FRUSTUM_TOP]);
		result[Camera.FRUSTUM_BOTTOM] = mathLibrary.multiply(height, cameraFrustum[Camera.FRUSTUM_BOTTOM]);
		result[Camera.FRUSTUM_NEAR] = cameraFrustum[Camera.FRUSTUM_NEAR];
		result[Camera.FRUSTUM_FAR] = cameraFrustum[Camera.FRUSTUM_FAR];
		return result;
	}

	public void drawTriangle(int[] location1, int[] location2, int[] location3, int[] cameraFrustum, Shader shader) {
		int triangleSize = barycentric(location1, location2, location3);
		if (triangleSize < 0) // backface culling
			return;
		int left = cameraFrustum[Camera.FRUSTUM_LEFT] + 1;
		int right = cameraFrustum[Camera.FRUSTUM_RIGHT] - 1;
		int top = cameraFrustum[Camera.FRUSTUM_TOP] + 1;
		int bottom = cameraFrustum[Camera.FRUSTUM_BOTTOM] - 1;
		int near = cameraFrustum[Camera.FRUSTUM_NEAR];
		int far = (cameraFrustum[Camera.FRUSTUM_FAR] / 10);
		boolean insideWidth1 = (location1[VECTOR_X] > left) & (location1[VECTOR_X] < right);
		boolean insideWidth2 = (location2[VECTOR_X] > left) & (location2[VECTOR_X] < right);
		boolean insideWidth3 = (location3[VECTOR_X] > left) & (location3[VECTOR_X] < right);
		boolean insideHeight1 = (location1[VECTOR_Y] > top) & (location1[VECTOR_Y] < bottom);
		boolean insideHeight2 = (location2[VECTOR_Y] > top) & (location2[VECTOR_Y] < bottom);
		boolean insideHeight3 = (location3[VECTOR_Y] > top) & (location3[VECTOR_Y] < bottom);
		boolean insideDepth1 = (location1[VECTOR_Z] > near) & (location1[VECTOR_Z] < far);
		boolean insideDepth2 = (location2[VECTOR_Z] > near) & (location2[VECTOR_Z] < far);
		boolean insideDepth3 = (location3[VECTOR_Z] > near) & (location3[VECTOR_Z] < far);
		if ((!insideDepth1 | !insideDepth2 | !insideDepth3) 
				| (!insideHeight1 & !insideHeight2 & !insideHeight3)
					| (!insideWidth1 & !insideWidth2 & !insideWidth3))
					return;
		if (location1[VECTOR_Y] > location2[VECTOR_Y]) {
			vectorLibrary.swap(location1, location2);
		}
		if (location2[VECTOR_Y] > location3[VECTOR_Y]) {
			vectorLibrary.swap(location2, location3);
		}
		if (location1[VECTOR_Y] > location2[VECTOR_Y]) {
			vectorLibrary.swap(location1, location2);
		}
        if (location2[VECTOR_Y] == location3[VECTOR_Y]) {
            drawBottomTriangle(location1, location2, location3, cameraFrustum, shader);
        }
        else if (location1[VECTOR_Y] == location2[VECTOR_Y]) {
            drawTopTriangle(location1, location2, location3, cameraFrustum, shader);
        } else {
            int x = location1[VECTOR_X];
            long dividend = ((long)location2[VECTOR_Y] - location1[VECTOR_Y]) << RASTERIZE_BITS;
            long divisor = location3[VECTOR_Y] - location1[VECTOR_Y];
            long multiplier = location3[VECTOR_X] - location1[VECTOR_X];
            x += ((dividend / divisor) * multiplier) >> RASTERIZE_BITS;
            int y = location2[VECTOR_Y];
            int z = location1[VECTOR_Z];
            dividend = ((long)location2[VECTOR_Y] - location1[VECTOR_Y]) << RASTERIZE_BITS;
            divisor = location3[VECTOR_Y] - location1[VECTOR_Y];
            multiplier = location3[VECTOR_Z] - location1[VECTOR_Z];
            z += ((dividend / divisor) * multiplier) >> RASTERIZE_BITS;
            barycentricCache[VECTOR_X] = x;
            barycentricCache[VECTOR_Y] = y;
            barycentricCache[VECTOR_Z] = z;
            drawBottomTriangle(location1, location2, barycentricCache, cameraFrustum, shader);
            drawTopTriangle(location2, barycentricCache, location3, cameraFrustum, shader);
        }
	}
	
	private void drawBottomTriangle(int[] location1, int[] location2, int[] location3, int[] cameraFrustum, Shader shader) {
		int xShifted = location1[VECTOR_X] << RASTERIZE_BITS;
		int y2y1 = location2[VECTOR_Y] - location1[VECTOR_Y];
		int y3y1 = location2[VECTOR_Y] - location1[VECTOR_Y];
		y2y1 = y2y1 == 0 ? 1 : y2y1;
		y3y1 = y3y1 == 0 ? 1 : y3y1;
        int dx1 = (int)((((long)location2[VECTOR_X] - location1[VECTOR_X]) << RASTERIZE_BITS) / y2y1);
        int dx2 = (int)((((long)location3[VECTOR_X] - location1[VECTOR_X]) << RASTERIZE_BITS) / y3y1);
        int dz1 = (int)((((long)location2[VECTOR_Z] - location1[VECTOR_Z]) << RASTERIZE_BITS) / y2y1);
        int dz2 = (int)((((long)location3[VECTOR_Z] - location1[VECTOR_Z]) << RASTERIZE_BITS) / y3y1);
        if(dx1 < dx2) {
        	int dxdx = dx2 - dx1;
        	dxdx = dxdx == 0 ? 1 : dxdx;
        	int dz = (int)((((long)dz2 - dz1) << RASTERIZE_BITS) / dxdx);
        	int x1 = xShifted;
            int x2 = xShifted + RASTERIZE_HALF;
            int z = location1[VECTOR_Z] << RASTERIZE_BITS;
	        for (int y = location1[VECTOR_Y]; y <= location2[VECTOR_Y]; y++) {
	        	drawScanline(x1, x2, y, z, dz, cameraFrustum, shader);
	            x1 += dx1;
	            x2 += dx2;
	            z += dz1;
	        }
        } else {
        	int dxdx = dx1 - dx2;
        	dxdx = dxdx == 0 ? 1 : dxdx;
        	int dz = (int)((((long)dz1 - dz2) << RASTERIZE_BITS) / dxdx);
        	int x1 = xShifted + RASTERIZE_HALF;
            int x2 = xShifted;
            int z = location1[VECTOR_Z] << RASTERIZE_BITS;
        	for (int y = location1[VECTOR_Y]; y <= location2[VECTOR_Y]; y++) {
        		drawScanline(x1, x2, y, z, dz, cameraFrustum, shader);
	            x1 += dx2;
	            x2 += dx1;
	            z += dz2;
	        }
        }
    }
    
	private void drawTopTriangle(int[] location1, int[] location2, int[] location3, int[] cameraFrustum, Shader shader) {
		int xShifted = location3[VECTOR_X] << RASTERIZE_BITS;
		int y3y1 = location3[VECTOR_Y] - location1[VECTOR_Y];
		int y3y2 = location3[VECTOR_Y] - location2[VECTOR_Y];
		y3y1 = y3y1 == 0 ? 1 : y3y1;
		y3y2 = y3y2 == 0 ? 1 : y3y2;
		int dx1 = (int)((((long)location3[VECTOR_X] - location1[VECTOR_X]) << RASTERIZE_BITS) / y3y1);
		int dx2 = (int)((((long)location3[VECTOR_X] - location2[VECTOR_X]) << RASTERIZE_BITS) / y3y2);
		int dz1 = (int)((((long)location3[VECTOR_Z] - location1[VECTOR_Z]) << RASTERIZE_BITS) / y3y1);
		int dz2 = (int)((((long)location3[VECTOR_Z] - location2[VECTOR_Z]) << RASTERIZE_BITS) / y3y2);
		if (dx1 > dx2) {
			int dxdx = dx1 - dx2;
			dxdx = dxdx == 0 ? 1 : dxdx;
			int dz = (int)((((long)dz1 - dz2) << RASTERIZE_BITS) / dxdx);
			int x1 = xShifted;
			int x2 = xShifted + RASTERIZE_HALF;
			int z = location3[VECTOR_Z] << RASTERIZE_BITS;
	        for (int y = location3[VECTOR_Y]; y > location1[VECTOR_Y]; y--) {
	        	drawScanline(x1, x2, y, z, dz, cameraFrustum, shader);
	            x1 -= dx1;
	            x2 -= dx2;
	            z -= dz1;
	        }
		} else {
			int dxdx = dx2 - dx1;
			dxdx = dxdx == 0 ? 1 : dxdx;
			int dz = (int)((((long)dz2 - dz1) << RASTERIZE_BITS) / dxdx);
			int x1 = xShifted + RASTERIZE_HALF;
			int x2 = xShifted;
			int z = location3[VECTOR_Z] << RASTERIZE_BITS;
	        for (int y = location3[VECTOR_Y]; y > location1[VECTOR_Y]; y--) {
	        	drawScanline(x1, x2, y, z, dz, cameraFrustum, shader);
	            x1 -= dx2;
	            x2 -= dx1;
	            z -= dz2;
	        }
		}
    }
	
	private void drawScanline(int x1, int x2, int y, int z, int dz, int[] cameraFrustum, Shader shader) {
		boolean yInside = (y > cameraFrustum[Camera.FRUSTUM_TOP] + 1) & (y < cameraFrustum[Camera.FRUSTUM_BOTTOM] - 1);
		x1 >>= RASTERIZE_BITS;
		x2 >>= RASTERIZE_BITS;
		for (; x1 <= x2; x1++) {
				if (yInside & (x1 > cameraFrustum[Camera.FRUSTUM_LEFT] + 1) & (x1 < cameraFrustum[Camera.FRUSTUM_RIGHT] - 1)) {
					pixelCache[VECTOR_X] = x1;
					pixelCache[VECTOR_Y] = y;
					pixelCache[VECTOR_Z] = z >> RASTERIZE_BITS;
					shader.fragment(pixelCache);
				}
			z += dz;
		}
	}
	
//	public void drawTriangle(int[] location1, int[] location2, int[] location3, int[] cameraFrustum, Shader shader) {
//		int triangleSize = barycentric(location1, location2, location3);
//		if (triangleSize < 0) // backface culling
//			return;
//		int left = cameraFrustum[Camera.FRUSTUM_LEFT] + 1;
//		int right = cameraFrustum[Camera.FRUSTUM_RIGHT] - 1;
//		int top = cameraFrustum[Camera.FRUSTUM_TOP] + 1;
//		int bottom = cameraFrustum[Camera.FRUSTUM_BOTTOM] - 1;
//		int near = cameraFrustum[Camera.FRUSTUM_NEAR];
//		int far = (cameraFrustum[Camera.FRUSTUM_FAR] / 10);
//		boolean insideDepth1 = (location1[VECTOR_Z] > near) & (location1[VECTOR_Z] < far);
//		boolean insideDepth2 = (location2[VECTOR_Z] > near) & (location2[VECTOR_Z] < far);
//		boolean insideDepth3 = (location3[VECTOR_Z] > near) & (location3[VECTOR_Z] < far);
//		if (!insideDepth1 & !insideDepth2 & !insideDepth3)
//			return;
//		// compute boundig box of faces
//		int bbMinX = Math.min(location1[VECTOR_X], Math.min(location2[VECTOR_X], location3[VECTOR_X]));
//		int bbMinY = Math.min(location1[VECTOR_Y], Math.min(location2[VECTOR_Y], location3[VECTOR_Y]));
//
//		int bbMaxX = Math.max(location1[VECTOR_X], Math.max(location2[VECTOR_X], location3[VECTOR_X]));
//		int bbMaxY = Math.max(location1[VECTOR_Y], Math.max(location2[VECTOR_Y], location3[VECTOR_Y]));
//
//		// clip against screen limits
//		int minX = Math.max(bbMinX, left);
//		int minY = Math.max(bbMinY, top);
//		int maxX = Math.min(bbMaxX, right);
//		int maxY = Math.min(bbMaxY, bottom);
//
//		location1[VECTOR_Z] = Math.max(1, location1[VECTOR_Z]);
//		location2[VECTOR_Z] = Math.max(1, location2[VECTOR_Z]);
//		location3[VECTOR_Z] = Math.max(1, location3[VECTOR_Z]);
//		// triangle setup
//		int a01 = location1[VECTOR_Y] - location2[VECTOR_Y];
//		int a12 = location2[VECTOR_Y] - location3[VECTOR_Y];
//		int a20 = location3[VECTOR_Y] - location1[VECTOR_Y];
//
//		int b01 = location2[VECTOR_X] - location1[VECTOR_X];
//		int b12 = location3[VECTOR_X] - location2[VECTOR_X];
//		int b20 = location1[VECTOR_X] - location3[VECTOR_X];
//
//		barycentricCache[3] = triangleSize == 0 ? 1 : triangleSize;
//		depthCache[0] = INTERPOLATE_ONE / location1[VECTOR_Z];
//		depthCache[1] = INTERPOLATE_ONE / location2[VECTOR_Z];
//		depthCache[2] = INTERPOLATE_ONE / location3[VECTOR_Z];
//		int oneByBarycentric = INTERPOLATE_ONE / barycentricCache[3];
//
//		// barycentric coordinates at minX/minY edge
//		pixelCache[VECTOR_X] = minX;
//		pixelCache[VECTOR_Y] = minY;
//
//		int barycentric0_row = barycentric(location2, location3, pixelCache);
//		int barycentric1_row = barycentric(location3, location1, pixelCache);
//		int barycentric2_row = barycentric(location1, location2, pixelCache);
//
//		for (pixelCache[VECTOR_Y] = minY; pixelCache[VECTOR_Y] < maxY; pixelCache[VECTOR_Y]++) {
//			boolean found = false;
//			barycentricCache[0] = barycentric0_row;
//			barycentricCache[1] = barycentric1_row;
//			barycentricCache[2] = barycentric2_row;
//			for (pixelCache[VECTOR_X] = minX; pixelCache[VECTOR_X] < maxX; pixelCache[VECTOR_X]++) {
//				if ((barycentricCache[0] | barycentricCache[1] | barycentricCache[2]) > 0) {
//					pixelCache[VECTOR_Z] = interpolateDepth(depthCache, barycentricCache);
//					for (int i = 0; i < shader.getVariables().length; i++) {
//						int[] variable = shader.getVariables()[i];
//						variable[3] = interpolate(variable, pixelCache, depthCache, barycentricCache, oneByBarycentric);
//					}
//					shader.fragment(pixelCache);
//					found = true;
//				} else {
//					if (found) {
//						break;
//					}
//				}
//				barycentricCache[0] += a12;
//				barycentricCache[1] += a20;
//				barycentricCache[2] += a01;
//			}
//			barycentric0_row += b12;
//			barycentric1_row += b20;
//			barycentric2_row += b01;
//		}
//	}
//
//	private int interpolateDepth(int[] depth, int[] barycentric) {
//		long dotProduct = (long) barycentric[0] * depth[0]
//				+ (long) barycentric[1] * depth[1]
//				+ (long) barycentric[2] * depth[2];
//		return (int) (((long) barycentric[3] << INTERPOLATE_BITS) / dotProduct);
//	}
//
//	private int interpolate(int[] values, int[] pixel, int[] depth, int[] barycentric, int oneByBarycentric) {
//		long dotProduct = (long) values[VECTOR_X] * depth[0] * barycentric[0]
//				+ (long) values[VECTOR_Y] * depth[1] * barycentric[1]
//				+ (long) values[VECTOR_Z] * depth[2] * barycentric[2];
//		// normalize values
//		long result = (dotProduct * pixel[VECTOR_Z]) >> INTERPOLATE_BITS;
//		result = (result * oneByBarycentric) >> INTERPOLATE_BITS;
//		return (int) result;
//	}
//
	private int barycentric(int[] vector1, int[] vector2, int[] vector3) {
		return (vector2[VECTOR_X] - vector1[VECTOR_X]) * (vector3[VECTOR_Y] - vector1[VECTOR_Y])
				- (vector3[VECTOR_X] - vector1[VECTOR_X]) * (vector2[VECTOR_Y] - vector1[VECTOR_Y]);
	}
}
