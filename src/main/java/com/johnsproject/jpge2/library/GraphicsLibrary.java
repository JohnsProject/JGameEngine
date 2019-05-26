package com.johnsproject.jpge2.library;

import com.johnsproject.jpge2.dto.Camera;
import com.johnsproject.jpge2.dto.Transform;
import com.johnsproject.jpge2.dto.Triangle;
import com.johnsproject.jpge2.shader.Shader;

public class GraphicsLibrary {
	private static final byte VECTOR_X = VectorLibrary.VECTOR_X;
	private static final byte VECTOR_Y = VectorLibrary.VECTOR_Y;
	private static final byte VECTOR_Z = VectorLibrary.VECTOR_Z;
	private static final byte VECTOR_W = VectorLibrary.VECTOR_W;

	private static final int FP_BITS = MathLibrary.FP_BITS;
	private static final int FP_ONE = MathLibrary.FP_ONE;
	private static final int FP_HALF = MathLibrary.FP_HALF;
	
	private static final int PERSPECTIVE_BITS = 25;
	private static final int PERSPECTIVE_ONE = 1 << PERSPECTIVE_BITS;
	private static final int PERSPECTIVE_HALF = PERSPECTIVE_ONE >> 1;
	
	private final int[] vectorCache;
	private final int[] pixelCache;

	private final MathLibrary mathLibrary;
	private final MatrixLibrary matrixLibrary;
	private final VectorLibrary vectorLibrary;

	public GraphicsLibrary() {
		this.mathLibrary = new MathLibrary();
		this.matrixLibrary = new MatrixLibrary();
		this.vectorLibrary = new VectorLibrary();

		this.vectorCache = vectorLibrary.generate();
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

	public void drawTriangle(Triangle triangle, int[] cameraFrustum, Shader shader) {
		int[] location1 = triangle.getLocation1();
		int[] location2 = triangle.getLocation2();
		int[] location3 = triangle.getLocation3();
		int[] uvX = triangle.getU();
		int[] uvY = triangle.getV();
		int[] red = triangle.getRed();
		int[] green = triangle.getGreen();
		int[] blue = triangle.getBlue();
		int triangleSize = shoelace(location1, location2, location3);
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
		location1[VECTOR_Z] = PERSPECTIVE_ONE / location1[VECTOR_Z];
		location2[VECTOR_Z] = PERSPECTIVE_ONE / location2[VECTOR_Z];
		location3[VECTOR_Z] = PERSPECTIVE_ONE / location3[VECTOR_Z];
		uvX[0] = mathLibrary.multiply(uvX[0], location1[VECTOR_Z]);
		uvX[1] = mathLibrary.multiply(uvX[1], location2[VECTOR_Z]);
		uvX[2] = mathLibrary.multiply(uvX[2], location3[VECTOR_Z]);
		uvY[0] = mathLibrary.multiply(uvY[0], location1[VECTOR_Z]);
		uvY[1] = mathLibrary.multiply(uvY[1], location2[VECTOR_Z]);
		uvY[2] = mathLibrary.multiply(uvY[2], location3[VECTOR_Z]);
		red[0] = mathLibrary.multiply(red[0], location1[VECTOR_Z]);
		red[1] = mathLibrary.multiply(red[1], location2[VECTOR_Z]);
		red[2] = mathLibrary.multiply(red[2], location3[VECTOR_Z]);
		green[0] = mathLibrary.multiply(green[0], location1[VECTOR_Z]);
		green[1] = mathLibrary.multiply(green[1], location2[VECTOR_Z]);
		green[2] = mathLibrary.multiply(green[2], location3[VECTOR_Z]);
		blue[0] = mathLibrary.multiply(blue[0], location1[VECTOR_Z]);
		blue[1] = mathLibrary.multiply(blue[1], location2[VECTOR_Z]);
		blue[2] = mathLibrary.multiply(blue[2], location3[VECTOR_Z]);
		if (location1[VECTOR_Y] > location2[VECTOR_Y]) {
			vectorLibrary.swap(location1, location2);
			int tmp = uvX[0]; uvX[0] = uvX[1]; uvX[1] = tmp;
			tmp = uvY[0]; uvY[0] = uvY[1]; uvY[1] = tmp;
			tmp = red[0]; red[0] = red[1]; red[1] = tmp;
			tmp = green[0]; green[0] = green[1]; green[1] = tmp;
			tmp = blue[0]; blue[0] = blue[1]; blue[1] = tmp;
		}
		if (location2[VECTOR_Y] > location3[VECTOR_Y]) {
			vectorLibrary.swap(location2, location3);
			int tmp = uvX[2]; uvX[2] = uvX[1]; uvX[1] = tmp;
			tmp = uvY[2]; uvY[2] = uvY[1]; uvY[1] = tmp;
			tmp = red[2]; red[2] = red[1]; red[1] = tmp;
			tmp = green[2]; green[2] = green[1]; green[1] = tmp;
			tmp = blue[2]; blue[2] = blue[1]; blue[1] = tmp;
		}
		if (location1[VECTOR_Y] > location2[VECTOR_Y]) {
			vectorLibrary.swap(location1, location2);
			int tmp = uvX[0]; uvX[0] = uvX[1]; uvX[1] = tmp;
			tmp = uvY[0]; uvY[0] = uvY[1]; uvY[1] = tmp;
			tmp = red[0]; red[0] = red[1]; red[1] = tmp;
			tmp = green[0]; green[0] = green[1]; green[1] = tmp;
			tmp = blue[0]; blue[0] = blue[1]; blue[1] = tmp;
		}
        if (location2[VECTOR_Y] == location3[VECTOR_Y]) {
            drawBottomTriangle(triangle, cameraFrustum, shader);
        } else if (location1[VECTOR_Y] == location2[VECTOR_Y]) {
            drawTopTriangle(triangle, cameraFrustum, shader);
        } else {
            int x = location1[VECTOR_X];
            int dy = mathLibrary.divide(location2[VECTOR_Y] - location1[VECTOR_Y], location3[VECTOR_Y] - location1[VECTOR_Y]);
            int multiplier = location3[VECTOR_X] - location1[VECTOR_X];
            x += mathLibrary.multiply(dy, multiplier);
            int y = location2[VECTOR_Y];
            int z = location1[VECTOR_Z];
            multiplier = location3[VECTOR_Z] - location1[VECTOR_Z];
            z += mathLibrary.multiply(dy, multiplier);
            int uvx = uvX[0];
            multiplier = uvX[2] - uvX[0];
            uvx += mathLibrary.multiply(dy, multiplier);
            int uvy = uvY[0];
            multiplier = uvY[2] - uvY[0];
            uvy += mathLibrary.multiply(dy, multiplier);
            int r = red[0];
            multiplier = red[2] - red[0];
            r += mathLibrary.multiply(dy, multiplier);
            int g = green[0];
            multiplier = green[2] - green[0];
            g += mathLibrary.multiply(dy, multiplier);
            int b = blue[0];
            multiplier = blue[2] - blue[0];
            b += mathLibrary.multiply(dy, multiplier);
            vectorCache[VECTOR_X] = x;
            vectorCache[VECTOR_Y] = y;
            vectorCache[VECTOR_Z] = z;
            vectorLibrary.swap(vectorCache, location3);
            int tmp = uvX[2]; uvX[2] = uvx; uvx = tmp;
            tmp = uvY[2]; uvY[2] = uvy; uvy = tmp;
            tmp = red[2]; red[2] = r; r = tmp;
            tmp = green[2]; green[2] = g; g = tmp;
            tmp = blue[2]; blue[2] = b; b = tmp;
            drawBottomTriangle(triangle, cameraFrustum, shader);
            vectorLibrary.swap(vectorCache, location3);
            vectorLibrary.swap(location1, location2);
            vectorLibrary.swap(location2, vectorCache);
            tmp = uvX[2]; uvX[2] = uvx; uvx = tmp;
            tmp = uvX[0]; uvX[0] = uvX[1]; uvX[1] = tmp;
            tmp = uvX[1]; uvX[1] = uvx; uvx = tmp;
            tmp = uvY[2]; uvY[2] = uvy; uvy = tmp;
            tmp = uvY[0]; uvY[0] = uvY[1]; uvY[1] = tmp;
            tmp = uvY[1]; uvY[1] = uvy; uvy = tmp;
            tmp = red[2]; red[2] = r; r = tmp;
            tmp = red[0]; red[0] = red[1]; red[1] = tmp;
            tmp = red[1]; red[1] = r; r = tmp;
            tmp = green[2]; green[2] = g; g = tmp;
            tmp = green[0]; green[0] = green[1]; green[1] = tmp;
            tmp = green[1]; green[1] = g; g = tmp;
            tmp = blue[2]; blue[2] = b; b = tmp;
            tmp = blue[0]; blue[0] = blue[1]; blue[1] = tmp;
            tmp = blue[1]; blue[1] = b; b = tmp;
            drawTopTriangle(triangle, cameraFrustum, shader);
        }
	}
	
	private void drawBottomTriangle(Triangle triangle, int[] cameraFrustum, Shader shader) {
		int[] location1 = triangle.getLocation1();
		int[] location2 = triangle.getLocation2();
		int[] location3 = triangle.getLocation3();
		int[] uvX = triangle.getU();
		int[] uvY = triangle.getV();
		int[] red = triangle.getRed();
		int[] green = triangle.getGreen();
		int[] blue = triangle.getBlue();
		int xShifted = location1[VECTOR_X] << FP_BITS;
		int y2y1 = location2[VECTOR_Y] - location1[VECTOR_Y];
		int y3y1 = location2[VECTOR_Y] - location1[VECTOR_Y];
		y2y1 = y2y1 == 0 ? 1 : y2y1;
		y3y1 = y3y1 == 0 ? 1 : y3y1;
        int dx1 = mathLibrary.divide(location2[VECTOR_X] - location1[VECTOR_X], y2y1);
        int dx2 = mathLibrary.divide(location3[VECTOR_X] - location1[VECTOR_X], y3y1);
        int dz1 = mathLibrary.divide(location2[VECTOR_Z] - location1[VECTOR_Z], y2y1);
        int dz2 = mathLibrary.divide(location3[VECTOR_Z] - location1[VECTOR_Z], y3y1);
        int du1 = mathLibrary.divide(uvX[1] - uvX[0], y2y1);
        int du2 = mathLibrary.divide(uvX[2] - uvX[0], y3y1);
        int dv1 = mathLibrary.divide(uvY[1] - uvY[0], y2y1);
        int dv2 = mathLibrary.divide(uvY[2] - uvY[0], y3y1);
        int dr1 = mathLibrary.divide(red[1] - red[0], y2y1);
        int dr2 = mathLibrary.divide(red[2] - red[0], y3y1);
        int dg1 = mathLibrary.divide(green[1] - green[0], y2y1);
        int dg2 = mathLibrary.divide(green[2] - green[0], y3y1);
        int db1 = mathLibrary.divide(blue[1] - blue[0], y2y1);
        int db2 = mathLibrary.divide(blue[2] - blue[0], y3y1);
        if(dx1 < dx2) {
        	int dxdx = dx2 - dx1;
        	dxdx = dxdx == 0 ? 1 : dxdx;
        	int dz = mathLibrary.divide(dz2 - dz1, dxdx);
        	int du = mathLibrary.divide(du2 - du1, dxdx);
        	int dv = mathLibrary.divide(dv2 - dv1, dxdx);
        	int dr = mathLibrary.divide(dr2 - dr1, dxdx);
        	int dg = mathLibrary.divide(dg2 - dg1, dxdx);
        	int db = mathLibrary.divide(db2 - db1, dxdx);
        	int x1 = xShifted;
            int x2 = xShifted;
            int z = location1[VECTOR_Z] << FP_BITS;
            int u = uvX[0] << FP_BITS;
            int v = uvY[0] << FP_BITS;
            int r = red[0] << FP_BITS;
            int g = green[0] << FP_BITS;
            int b = blue[0] << FP_BITS;
	        for (int y = location1[VECTOR_Y]; y <= location2[VECTOR_Y]; y++) {
	        	drawScanline(x1, x2, y, z, u, v, r, g, b, dz, du, dv, dr, dg, db, triangle, cameraFrustum, shader);
	            x1 += dx1;
	            x2 += dx2;
	            z += dz1;
	            u += du1;
	            v += dv1;
	            r += dr1;
	            g += dg1;
	            b += db1;
	        }
        } else {
        	int dxdx = dx1 - dx2;
        	dxdx = dxdx == 0 ? 1 : dxdx;
        	int dz = mathLibrary.divide(dz1 - dz2, dxdx);
        	int du = mathLibrary.divide(du1 - du2, dxdx);
        	int dv = mathLibrary.divide(dv1 - dv2, dxdx);
        	int dr = mathLibrary.divide(dr1 - dr2, dxdx);
        	int dg = mathLibrary.divide(dg1 - dg2, dxdx);
        	int db = mathLibrary.divide(db1 - db2, dxdx);
        	int x1 = xShifted;
            int x2 = xShifted;
            int z = location1[VECTOR_Z] << FP_BITS;
            int u = uvX[0] << FP_BITS;
            int v = uvY[0] << FP_BITS;
            int r = red[0] << FP_BITS;
            int g = green[0] << FP_BITS;
            int b = blue[0] << FP_BITS;
        	for (int y = location1[VECTOR_Y]; y <= location2[VECTOR_Y]; y++) {
        		drawScanline(x1, x2, y, z, u, v, r, g, b, dz, du, dv, dr, dg, db, triangle, cameraFrustum, shader);
	            x1 += dx2;
	            x2 += dx1;
	            z += dz2;
	            u += du2;
	            v += dv2;
	            r += dr2;
	            g += dg2;
	            b += db2;
	        }
        }
    }
    
	private void drawTopTriangle(Triangle triangle, int[] cameraFrustum, Shader shader) {
		int[] location1 = triangle.getLocation1();
		int[] location2 = triangle.getLocation2();
		int[] location3 = triangle.getLocation3();
		int[] uvX = triangle.getU();
		int[] uvY = triangle.getV();
		int[] red = triangle.getRed();
		int[] green = triangle.getGreen();
		int[] blue = triangle.getBlue();
		int xShifted = location3[VECTOR_X] << FP_BITS;
		int y3y1 = location3[VECTOR_Y] - location1[VECTOR_Y];
		int y3y2 = location3[VECTOR_Y] - location2[VECTOR_Y];
		y3y1 = y3y1 == 0 ? 1 : y3y1;
		y3y2 = y3y2 == 0 ? 1 : y3y2;
		int dx1 = mathLibrary.divide(location3[VECTOR_X] - location1[VECTOR_X], y3y1);
		int dx2 = mathLibrary.divide(location3[VECTOR_X] - location2[VECTOR_X], y3y2);
		int dz1 = mathLibrary.divide(location3[VECTOR_Z] - location1[VECTOR_Z], y3y1);
		int dz2 = mathLibrary.divide(location3[VECTOR_Z] - location2[VECTOR_Z], y3y2);
		int du1 = mathLibrary.divide(uvX[2] - uvX[0], y3y1);
		int du2 = mathLibrary.divide(uvX[2] - uvX[1], y3y2);
		int dv1 = mathLibrary.divide(uvY[2] - uvY[0], y3y1);
		int dv2 = mathLibrary.divide(uvY[2] - uvY[1], y3y2);
		int dr1 = mathLibrary.divide(red[2] - red[0], y3y1);
		int dr2 = mathLibrary.divide(red[2] - red[1], y3y2);
		int dg1 = mathLibrary.divide(green[2] - green[0], y3y1);
		int dg2 = mathLibrary.divide(green[2] - green[1], y3y2);
		int db1 = mathLibrary.divide(blue[2] - blue[0], y3y1);
		int db2 = mathLibrary.divide(blue[2] - blue[1], y3y2);
		if (dx1 > dx2) {
			int dxdx = dx1 - dx2;
			dxdx = dxdx == 0 ? 1 : dxdx;
			int dz = mathLibrary.divide(dz1 - dz2, dxdx);
			int du = mathLibrary.divide(du1 - du2, dxdx);
			int dv = mathLibrary.divide(dv1 - dv2, dxdx);
			int dr = mathLibrary.divide(dr1 - dr2, dxdx);
			int dg = mathLibrary.divide(dg1 - dg2, dxdx);
			int db = mathLibrary.divide(db1 - db2, dxdx);
			int x1 = xShifted;
			int x2 = xShifted;
			int z = location3[VECTOR_Z] << FP_BITS;
			int u = uvX[2] << FP_BITS;
			int v = uvY[2] << FP_BITS;
			int r = red[2] << FP_BITS;
			int g = green[2] << FP_BITS;
			int b = blue[2] << FP_BITS;
	        for (int y = location3[VECTOR_Y]; y > location1[VECTOR_Y]; y--) {
	        	drawScanline(x1, x2, y, z, u, v, r, g, b, dz, du, dv, dr, dg, db, triangle, cameraFrustum, shader);
	            x1 -= dx1;
	            x2 -= dx2;
	            z -= dz1;
	            u -= du1;
	            v -= dv1;
	            r -= dr1;
	            g -= dg1;
	            b -= db1;
	        }
		} else {
			int dxdx = dx2 - dx1;
			dxdx = dxdx == 0 ? 1 : dxdx;
			int dz = mathLibrary.divide(dz2 - dz1, dxdx);
			int du = mathLibrary.divide(du2 - du1, dxdx);
			int dv = mathLibrary.divide(dv2 - dv1, dxdx);
			int dr = mathLibrary.divide(dr2 - dr1, dxdx);
			int dg = mathLibrary.divide(dg2 - dg1, dxdx);
			int db = mathLibrary.divide(db2 - db1, dxdx);
			int x1 = xShifted;
			int x2 = xShifted;
			int z = location3[VECTOR_Z] << FP_BITS;
			int u = uvX[2] << FP_BITS;
			int v = uvY[2] << FP_BITS;
			int r = red[2] << FP_BITS;
			int g = green[2] << FP_BITS;
			int b = blue[2] << FP_BITS;
	        for (int y = location3[VECTOR_Y]; y > location1[VECTOR_Y]; y--) {
	        	drawScanline(x1, x2, y, z, u, v, r, g, b, dz, du, dv, dr, dg, db, triangle, cameraFrustum, shader);
	            x1 -= dx2;
	            x2 -= dx1;
	            z -= dz2;
	            u -= du2;
	            v -= dv2;
	            r -= dr2;
	            g -= dg2;
	            b -= db2;
	        }
		}
    }
	
	private void drawScanline(int x1, int x2, int y, int z, int u, int v, int r, int g, int b, int dz, int du, int dv, int dr, int dg, int db, Triangle triangle, int[] cameraFrustum, Shader shader) {
		boolean yInside = (y > cameraFrustum[Camera.FRUSTUM_TOP] + 1) & (y < cameraFrustum[Camera.FRUSTUM_BOTTOM] - 1);
		x1 >>= FP_BITS;
		x2 >>= FP_BITS;
		for (; x1 <= x2; x1++) {
			if (yInside & (x1 > cameraFrustum[Camera.FRUSTUM_LEFT] + 1) & (x1 < cameraFrustum[Camera.FRUSTUM_RIGHT] - 1)) {
				pixelCache[VECTOR_X] = x1;
				pixelCache[VECTOR_Y] = y;
				pixelCache[VECTOR_Z] = z >> FP_BITS;
				pixelCache[VECTOR_Z] = PERSPECTIVE_ONE / pixelCache[VECTOR_Z];
				triangle.getU()[3] = multiply(u, pixelCache[VECTOR_Z]);
				triangle.getV()[3] = multiply(v, pixelCache[VECTOR_Z]);
				triangle.getRed()[3] = multiply(r, pixelCache[VECTOR_Z]);
				triangle.getGreen()[3] = multiply(g, pixelCache[VECTOR_Z]);
				triangle.getBlue()[3] = multiply(b, pixelCache[VECTOR_Z]);
				shader.fragment(pixelCache);
			}
			z += dz;
			u += du;
			v += dv;
			r += dr;
			g += dg;
			b += db;
		}
	}
	
	public int multiply(int value1, int value2) {
		long a = value1;
		long b = value2;
		long result = a * b + PERSPECTIVE_HALF;
		return (int) (result >> PERSPECTIVE_BITS);
	}
	
	private int shoelace(int[] vector1, int[] vector2, int[] vector3) {
		return (vector2[VECTOR_X] - vector1[VECTOR_X]) * (vector3[VECTOR_Y] - vector1[VECTOR_Y])
				- (vector3[VECTOR_X] - vector1[VECTOR_X]) * (vector2[VECTOR_Y] - vector1[VECTOR_Y]);
	}
}
