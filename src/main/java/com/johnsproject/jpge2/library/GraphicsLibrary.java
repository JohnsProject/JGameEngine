package com.johnsproject.jpge2.library;

import com.johnsproject.jpge2.dto.Transform;
import com.johnsproject.jpge2.shader.Shader;

public class GraphicsLibrary {
	private static final byte VECTOR_X = VectorLibrary.VECTOR_X;
	private static final byte VECTOR_Y = VectorLibrary.VECTOR_Y;
	private static final byte VECTOR_Z = VectorLibrary.VECTOR_Z;
	private static final byte VECTOR_W = VectorLibrary.VECTOR_W;
	
	private static final byte FP_BITS = MathLibrary.FP_BITS;
	private static final int FP_ONE = MathLibrary.FP_ONE;
	private static final int FP_HALF = MathLibrary.FP_HALF;
	
	private static final byte INTERPOLATE_BITS = 25;
	private static final int INTERPOLATE_ONE = 1 << INTERPOLATE_BITS;
	
	private final int[] depthCache;
	private final int[] barycentricCache;
	private final int[] pixelChache;
	
	private final MathLibrary mathLibrary;
	private final MatrixLibrary matrixLibrary;
	private final VectorLibrary vectorLibrary;
	
	
	public GraphicsLibrary() {
		this.mathLibrary = new MathLibrary();
		this.matrixLibrary = new MatrixLibrary();
		this.vectorLibrary = new VectorLibrary();
		
		this.depthCache = vectorLibrary.generate();
		this.barycentricCache = vectorLibrary.generate();
		this.pixelChache = vectorLibrary.generate();
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

	public int[][] orthographicMatrix(int[][] matrix, int[] frustum) {
		matrixLibrary.copy(matrix, MatrixLibrary.MATRIX_IDENTITY);
		matrix[0][0] = frustum[0];
		matrix[1][1] = frustum[0];
		matrix[2][2] = -FP_BITS;
		matrix[3][3] = -FP_ONE * FP_HALF;
		return matrix;
	}

	public int[][] perspectiveMatrix(int[][] matrix, int[] frustum) {
		matrixLibrary.copy(matrix, MatrixLibrary.MATRIX_IDENTITY);
		matrix[0][0] = frustum[0];
		matrix[1][1] = frustum[0];
		matrix[2][2] = -FP_BITS;
		matrix[2][3] = FP_ONE;
		return matrix;
	}	
	
	public int[] viewport(int[] location, int[] cameraCanvas, int[] out) {
		int scaleFactor = (cameraCanvas[3] >> 6) + 1;
		int halfX = cameraCanvas[VECTOR_X] + ((cameraCanvas[2] - cameraCanvas[VECTOR_X]) >> 1);
		int halfY = cameraCanvas[VECTOR_Y] + ((cameraCanvas[3] - cameraCanvas[VECTOR_Y]) >> 1);
		int w = Math.min(-1, location[VECTOR_W]);
		out[VECTOR_X] = mathLibrary.divide(location[VECTOR_X] * scaleFactor, w) + halfX;
		out[VECTOR_Y] = mathLibrary.divide(location[VECTOR_Y] * scaleFactor, w) + halfY;
		return out;
	}

	public boolean isBackface(int[] location1, int[] location2, int[] location3) {
		return barycentric(location1, location2, location3) <= 0;
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
	
	public int[] portCanvas(int[] cameraCanvas, int width, int height, int[] out) {
		out[0] = (width * cameraCanvas[0]) / 100;
		out[1] = (height * cameraCanvas[1]) / 100;
		out[2] = (width * cameraCanvas[2]) / 100;
		out[3] = (height * cameraCanvas[3]) / 100;
		return out;
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
		int oneByBarycentric = INTERPOLATE_ONE / barycentricCache[3];

		// barycentric coordinates at minX/minY edge
		pixelChache[VECTOR_X] = minX;
		pixelChache[VECTOR_Y] = minY;

		int barycentric0_row = barycentric(location2, location3, pixelChache);
		int barycentric1_row = barycentric(location3, location1, pixelChache);
		int barycentric2_row = barycentric(location1, location2, pixelChache);
		
		for (pixelChache[VECTOR_Y] = minY; pixelChache[VECTOR_Y] < maxY; pixelChache[VECTOR_Y]++) {

			boolean found = false;
			barycentricCache[0] = barycentric0_row;
			barycentricCache[1] = barycentric1_row;
			barycentricCache[2] = barycentric2_row;
			
			for (pixelChache[VECTOR_X] = minX; pixelChache[VECTOR_X] < maxX; pixelChache[VECTOR_X]++) {
				if ((barycentricCache[0] | barycentricCache[1] | barycentricCache[2]) > 0) {
					pixelChache[VECTOR_Z] = interpolatDepth(depthCache, barycentricCache);
					for (int i = 0; i < shader.getVariables().length; i++) {
						int[] variable = shader.getVariables()[i];
						variable[3] = interpolate(variable, pixelChache, depthCache, barycentricCache, oneByBarycentric);
					}
					shader.fragment(pixelChache);
					found = true;
				} else {
					if (found) {
						break;
					}
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

	private int interpolatDepth(int[] depth, int[] barycentric) {
		 long dotProduct = (long)barycentric[0] * depth[0]
						 + (long)barycentric[1] * depth[1]
						 + (long)barycentric[2] * depth[2];
		 return (int) (((long)barycentric[3] << INTERPOLATE_BITS) / dotProduct);
	}

	private int interpolate(int[] values, int[] pixel, int[] depth, int[] barycentric, int oneByBarycentric) {
		 long dotProduct = (long)values[VECTOR_X] * depth[0] * barycentric[0]
						 + (long)values[VECTOR_Y] * depth[1] * barycentric[1]
						 + (long)values[VECTOR_Z] * depth[2] * barycentric[2];
		 // normalize values
		 long result = (dotProduct * pixel[VECTOR_Z]) >> INTERPOLATE_BITS;
		 result = (result * oneByBarycentric) >> INTERPOLATE_BITS;
		 return (int)result;
	}

	private int barycentric(int[] vector1, int[] vector2, int[] vector3) {
		return (vector2[VECTOR_X] - vector1[VECTOR_X]) * (vector3[VECTOR_Y] - vector1[VECTOR_Y])
				- (vector3[VECTOR_X] - vector1[VECTOR_X]) * (vector2[VECTOR_Y] - vector1[VECTOR_Y]);
	}
}
