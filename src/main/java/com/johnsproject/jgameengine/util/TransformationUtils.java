package com.johnsproject.jgameengine.util;

import static com.johnsproject.jgameengine.util.FixedPointUtils.*;
import static com.johnsproject.jgameengine.util.MatrixUtils.*;
import static com.johnsproject.jgameengine.util.VectorUtils.*;

import com.johnsproject.jgameengine.model.Frustum;
import com.johnsproject.jgameengine.model.Transform;

public final class TransformationUtils {
	
	private TransformationUtils() { }
	
	public static int[][] spaceExitMatrix(int[][] matrix, Transform transform, int[][] matrixCache1, int[][] matrixCache2) {
		int[] location = transform.getLocation();
		int[] rotation = transform.getRotation();
		int[] scale = transform.getScale();
		MatrixUtils.copy(matrix, MatrixUtils.MATRIX_IDENTITY);
		scale(matrix, scale, matrixCache1, matrixCache2);
		rotateX(matrix, rotation[VECTOR_X], matrixCache1, matrixCache2);
		rotateY(matrix, rotation[VECTOR_Y], matrixCache1, matrixCache2);
		rotateZ(matrix, rotation[VECTOR_Z], matrixCache1, matrixCache2);
		translate(matrix, location, matrixCache1, matrixCache2);
		return matrix;
	}
	
	public static int[][] spaceExitNormalMatrix(int[][] matrix, Transform transform, int[][] matrixCache1, int[][] matrixCache2) {
		int[] rotation = transform.getRotation();
		int[] scale = transform.getScale();
		MatrixUtils.copy(matrix, MatrixUtils.MATRIX_IDENTITY);
		scale(matrix, scale, matrixCache1, matrixCache2);
		rotateX(matrix, rotation[VECTOR_X], matrixCache1, matrixCache2);
		rotateY(matrix, rotation[VECTOR_Y], matrixCache1, matrixCache2);
		rotateZ(matrix, rotation[VECTOR_Z], matrixCache1, matrixCache2);
		if ((scale[VECTOR_X] != scale[VECTOR_Y]) || (scale[VECTOR_Y] != scale[VECTOR_Z])) {
			MatrixUtils.inverse(matrix, matrixCache2);
			MatrixUtils.transpose(matrixCache2, matrix);
		}
		return matrix;
	}

	public static int[][] spaceEnterMatrix(int[][] matrix, Transform transform, int[][] matrixCache1, int[][] matrixCache2) {
		int[] location = transform.getLocation();
		int[] rotation = transform.getRotation();
		int[] scale = transform.getScale();
		int scaleX = FixedPointUtils.divide(FP_ONE, scale[VECTOR_X] == 0 ? 1 : scale[VECTOR_X]);
		int scaleY = FixedPointUtils.divide(FP_ONE, scale[VECTOR_Y] == 0 ? 1 : scale[VECTOR_Y]);
		int scaleZ = FixedPointUtils.divide(FP_ONE, scale[VECTOR_Z] == 0 ? 1 : scale[VECTOR_Z]);
		VectorUtils.invert(location);
		VectorUtils.invert(rotation);
		MatrixUtils.copy(matrix, MatrixUtils.MATRIX_IDENTITY);
		translate(matrix, location, matrixCache1, matrixCache2);
		rotateZ(matrix, rotation[VECTOR_Z], matrixCache1, matrixCache2);
		rotateY(matrix, rotation[VECTOR_Y], matrixCache1, matrixCache2);
		rotateX(matrix, rotation[VECTOR_X], matrixCache1, matrixCache2);
		scale(matrix, scaleX, scaleY, scaleZ, matrixCache1, matrixCache2);
		VectorUtils.invert(location);
		VectorUtils.invert(rotation);
		return matrix;
	}
	
	public static int[][] spaceEnterNormalMatrix(int[][] matrix, Transform transform, int[][] matrixCache1, int[][] matrixCache2) {
		int[] rotation = transform.getRotation();
		int[] scale = transform.getScale();
		int scaleX = FixedPointUtils.divide(FP_ONE, scale[VECTOR_X] == 0 ? 1 : scale[VECTOR_X]);
		int scaleY = FixedPointUtils.divide(FP_ONE, scale[VECTOR_Y] == 0 ? 1 : scale[VECTOR_Y]);
		int scaleZ = FixedPointUtils.divide(FP_ONE, scale[VECTOR_Z] == 0 ? 1 : scale[VECTOR_Z]);
		VectorUtils.invert(rotation);
		MatrixUtils.copy(matrix, MatrixUtils.MATRIX_IDENTITY);
		rotateZ(matrix, rotation[VECTOR_Z], matrixCache1, matrixCache2);
		rotateY(matrix, rotation[VECTOR_Y], matrixCache1, matrixCache2);
		rotateX(matrix, rotation[VECTOR_X], matrixCache1, matrixCache2);
		scale(matrix, scaleX, scaleY, scaleZ, matrixCache1, matrixCache2);
		VectorUtils.invert(rotation);
		if ((scale[VECTOR_X] != scale[VECTOR_Y]) || (scale[VECTOR_Y] != scale[VECTOR_Z])) {
			MatrixUtils.inverse(matrix, matrixCache2);
			MatrixUtils.transpose(matrixCache2, matrix);
		}
		return matrix;
	}

	public static int[][] orthographicMatrix(int[][] matrix, Frustum frustum) {
		int top = frustum.getRenderTargetTop();
		int bottom = frustum.getRenderTargetBottom();
		int near = frustum.getNear();
		int far = frustum.getFar();		
		int farNear = far - near;
		int scaleFactor = FixedPointUtils.multiply(frustum.getFocalLength(), bottom - top + 1);
		int[][] projectionMatrix = MatrixUtils.copy(matrix, MatrixUtils.MATRIX_IDENTITY);
		projectionMatrix[0][0] = scaleFactor;
		projectionMatrix[1][1] = scaleFactor;
		projectionMatrix[2][2] = -FixedPointUtils.divide(FP_ONE, farNear);
		projectionMatrix[3][2] = -FixedPointUtils.divide(near, farNear);
		projectionMatrix[3][3] = -FP_ONE << 4;
		return projectionMatrix;
	}

	public static int[][] perspectiveMatrix(int[][] matrix, Frustum frustum) {
		int top = frustum.getRenderTargetTop();
		int bottom = frustum.getRenderTargetBottom();
		int near = frustum.getNear();
		int far = frustum.getFar();	
		int farNear = far - near;
		int scaleFactor = FixedPointUtils.multiply(frustum.getFocalLength(), bottom - top + 1);
		int[][] projectionMatrix = MatrixUtils.copy(matrix, MatrixUtils.MATRIX_IDENTITY);
		projectionMatrix[0][0] = -scaleFactor;
		projectionMatrix[1][1] = scaleFactor;
		projectionMatrix[2][2] = -FixedPointUtils.divide(FP_ONE, farNear);
		projectionMatrix[3][2] = -FixedPointUtils.divide(near, farNear);
		projectionMatrix[2][3] = FP_ONE;
		projectionMatrix[3][3] = 0;
		return projectionMatrix;
	}

	public static int[] viewportVector(int[] location, Frustum frustum) {
		int top = frustum.getRenderTargetTop();
		int bottom = frustum.getRenderTargetBottom();
		int left = frustum.getRenderTargetLeft();
		int right = frustum.getRenderTargetRight();
		int halfX = left + ((right - left) >> 1);
		int halfY = top + ((bottom - top) >> 1);
		int w = location[VECTOR_W];
		w = FixedPointUtils.divide(FP_ONE, w == 0 ? 1 : w);
		location[VECTOR_X] = FixedPointUtils.multiply(location[VECTOR_X], w) + halfX;
		location[VECTOR_Y] = FixedPointUtils.multiply(location[VECTOR_Y], w) + halfY;
		return location;
	}

	public static Frustum viewportFrustum(Frustum frustum) {
		final int renderTargetWidth = frustum.getRenderTargetWidth();
		final int renderTargetHeight = frustum.getRenderTargetHeight();
		final int left = FixedPointUtils.multiply(renderTargetWidth, frustum.getLeft());
		final int right = FixedPointUtils.multiply(renderTargetWidth, frustum.getRight());
		final int top = FixedPointUtils.multiply(renderTargetHeight, frustum.getTop());
		final int bottom = FixedPointUtils.multiply(renderTargetHeight, frustum.getBottom());
		frustum.setRenderTargetFrustum(left, right, top, bottom);
		return frustum;
	}
	
	public static int[] translate(int[] vector, int[] direction) {
		return translate(vector, direction[VECTOR_X], direction[VECTOR_Y], direction[VECTOR_Z]);
	}
	
	public static int[] translate(int[] vector, int x, int y, int z) {
		vector[VECTOR_X] = vector[VECTOR_X] + x;
		vector[VECTOR_Y] = vector[VECTOR_Y] + y;
		vector[VECTOR_Z] = vector[VECTOR_Z] + z;
		return vector;
	}
	
	public static int[] scale(int[] vector, int factor) {
		vector[VECTOR_X] = FixedPointUtils.multiply(vector[VECTOR_X], factor);
		vector[VECTOR_Y] = FixedPointUtils.multiply(vector[VECTOR_Y], factor);
		vector[VECTOR_Z] = FixedPointUtils.multiply(vector[VECTOR_Z], factor);
		return vector;
	}
	
	/**
	 * Sets result equals the vector reflected across reflectionVector.
	 * 
	 * @param vector
	 * @param reflectionVector
	 * @param result
	 */
	public static int[] reflect(int[] vector, int[] reflectionVector) {
		int x = reflectionVector[VECTOR_X];
		int y = reflectionVector[VECTOR_Y];
		int z = reflectionVector[VECTOR_Z];
		int dot = (int)(2 * VectorUtils.dotProduct(vector, reflectionVector));
		VectorUtils.multiply(reflectionVector, dot);
		VectorUtils.subtract(vector, reflectionVector);
		reflectionVector[VECTOR_X] = x;
		reflectionVector[VECTOR_Y] = y;
		reflectionVector[VECTOR_Z] = z;
		return vector;
	}

	/**
	 * Sets result equals the vector rotated around (0, 0, 0) at x axis by the given angle.
	 * 
	 * @param vector
	 * @param angle
	 * @param result
	 * @return
	 */
	public static int[] rotateX(int[] vector, int angle) {
		int sin = FixedPointUtils.sin(angle);
		int cos = FixedPointUtils.cos(angle);
		int x = vector[VECTOR_X];
		int y = vector[VECTOR_Y];
		int z = vector[VECTOR_Z];
		vector[VECTOR_X] = x;
		vector[VECTOR_Y] = FixedPointUtils.multiply(y, cos);
		vector[VECTOR_Y] -= FixedPointUtils.multiply(z, sin);
		vector[VECTOR_Z] = FixedPointUtils.multiply(z, cos);
		vector[VECTOR_Z] += FixedPointUtils.multiply(y, sin);
		return vector;
	}
	
	/**
	 * Sets result equals the vector rotated around (0, 0, 0) at y axis by the given angle.
	 * 
	 * @param vector
	 * @param angle
	 * @param result
	 * @return
	 */
	public static int[] rotateY(int[] vector, int angle) {
		int sin = FixedPointUtils.sin(angle);
		int cos = FixedPointUtils.cos(angle);
		int x = vector[VECTOR_X];
		int y = vector[VECTOR_Y];
		int z = vector[VECTOR_Z];
		vector[VECTOR_X] = FixedPointUtils.multiply(x, cos);
		vector[VECTOR_X] += FixedPointUtils.multiply(z, sin);
		vector[VECTOR_Y] = y;
		vector[VECTOR_Z] = FixedPointUtils.multiply(z, cos);
		vector[VECTOR_Z] -= FixedPointUtils.multiply(x, sin);
		return vector;
	}
	
	/**
	 * Sets result equals the vector rotated around (0, 0, 0) at z axis by the given angle.
	 * 
	 * @param vector
	 * @param angle
	 * @param result
	 * @return
	 */
	public static int[] rotateZ(int[] vector, int angle) {
		int sin = FixedPointUtils.sin(angle);
		int cos = FixedPointUtils.cos(angle);
		int x = vector[VECTOR_X];
		int y = vector[VECTOR_Y];
		int z = vector[VECTOR_Z];
		vector[VECTOR_X] = FixedPointUtils.multiply(x, cos);
		vector[VECTOR_X] += FixedPointUtils.multiply(y, sin);
		vector[VECTOR_Y] = FixedPointUtils.multiply(y, cos);
		vector[VECTOR_Y] -= FixedPointUtils.multiply(x, sin);
		vector[VECTOR_Z] = z;
		return vector;
	}	
	
	/**
	 * Sets result equals the translated matrix.
	 *
	 * @param matrix
	 * @param x
	 * @param y
	 * @param z
	 * @param result
	 */
	public static int[][] translate(int[][] matrix, int[] vector, int[][] matrixCache1, int[][] matrixCache2) {
		return translate(matrix, vector[VECTOR_X], vector[VECTOR_Y], vector[VECTOR_Z], matrixCache1, matrixCache2);
	}
	
	/**
	 * Sets result equals the translated matrix.
	 *
	 * @param matrix
	 * @param x
	 * @param y
	 * @param z
	 * @param result
	 */
	public static int[][] translate(int[][] matrix, int x, int y , int z, int[][] matrixCache1, int[][] matrixCache2) {
		translationMatrix(matrixCache1, x, y, z);
		MatrixUtils.copy(matrixCache2, matrix);
		return multiply(matrixCache1, matrixCache2, matrix);
	}
	
	/**
	 * Sets result equals the translated matrix.
	 *
	 * @param matrix
	 * @param x
	 * @param y
	 * @param z
	 * @param result
	 */
	public static int[][] translationMatrix(int[][] matrix, int[] vector) {
		return translationMatrix(matrix, vector[VECTOR_X], vector[VECTOR_Y], vector[VECTOR_Z]);
	}
	
	/**
	 * Sets result equals the translated matrix.
	 *
	 * @param matrix
	 * @param x
	 * @param y
	 * @param z
	 * @param result
	 */
	public static int[][] translationMatrix(int[][] matrix, int x, int y , int z) {
		MatrixUtils.copy(matrix, MATRIX_IDENTITY);
		matrix[3][0] = x;
		matrix[3][1] = y;
		matrix[3][2] = z;
		return matrix;
	}

	/**
	 * Sets result equals the scaled matrix.
	 *
	 * @param matrix
	 * @param x
	 * @param y
	 * @param z
	 * @param result
	 */
	public static int[][] scale(int[][] matrix, int[] vector, int[][] matrixCache1, int[][] matrixCache2) {
		return scale(matrix, vector[VECTOR_X], vector[VECTOR_Y], vector[VECTOR_Z], matrixCache1, matrixCache2);
	}
	
	/**
	 * Sets result equals the scaled matrix.
	 *
	 * @param matrix
	 * @param x
	 * @param y
	 * @param z
	 * @param result
	 */
	public static int[][] scale(int[][] matrix, int x, int y, int z, int[][] matrixCache1, int[][] matrixCache2) {
		scaleMatrix(matrixCache1, x, y, z);
		MatrixUtils.copy(matrixCache2, matrix);
		return multiply(matrixCache1, matrixCache2, matrix);
	}
	
	public static int[][] scaleMatrix(int[][] matrix, int[] vector) {
		return scaleMatrix(matrix, vector[VECTOR_X], vector[VECTOR_Y], vector[VECTOR_Z]);
	}
	
	public static int[][] scaleMatrix(int[][] matrix, int x, int y, int z) {
		MatrixUtils.copy(matrix, MATRIX_IDENTITY);
		matrix[0][0] = x;
		matrix[1][1] = y;
		matrix[2][2] = z;
		return matrix;
	}

	/**
	 * Sets result equals the matrix rotated around (0, 0, 0) at x axis by the given
	 * angle.
	 *
	 * @param matrix
	 * @param angle
	 * @param result
	 */
	public static int[][] rotateX(int[][] matrix, int angle, int[][] matrixCache1, int[][] matrixCache2) {
		xRotationMatrix(matrixCache1, angle);
		MatrixUtils.copy(matrixCache2, matrix);
		return multiply(matrixCache1, matrixCache2, matrix);
	}
	
	public static int[][] xRotationMatrix(int[][] matrix, int angle) {
		MatrixUtils.copy(matrix, MATRIX_IDENTITY);
		int cos = FixedPointUtils.cos(angle);
		int sin = FixedPointUtils.sin(angle);
		matrix[1][1] = cos;
		matrix[1][2] = sin;
		matrix[2][1] = -sin;
		matrix[2][2] = cos;
		return matrix;
	}

	/**
	 * Sets result equals the matrix rotated around (0, 0, 0) at y axis by the given
	 * angle.
	 *
	 * @param matrix
	 * @param angle
	 * @param result
	 */
	public static int[][] rotateY(int[][] matrix, int angle, int[][] matrixCache1, int[][] matrixCache2) {
		yRotationMatrix(matrixCache1, angle);
		MatrixUtils.copy(matrixCache2, matrix);
		return multiply(matrixCache1, matrixCache2, matrix);
	}
	
	public static int[][] yRotationMatrix(int[][] matrix, int angle) {
		MatrixUtils.copy(matrix, MATRIX_IDENTITY);
		int cos = FixedPointUtils.cos(angle);
		int sin = FixedPointUtils.sin(angle);
		matrix[0][0] = cos;
		matrix[0][2] = -sin;
		matrix[2][0] = sin;
		matrix[2][2] = cos;
		return matrix;
	}

	/**
	 * Sets result equals the matrix rotated around (0, 0, 0) at z axis by the given
	 * angle.
	 *
	 * @param matrix
	 * @param angle
	 * @param result
	 */
	public static int[][] rotateZ(int[][] matrix, int angle, int[][] matrixCache1, int[][] matrixCache2) {
		yRotationMatrix(matrixCache1, angle);
		MatrixUtils.copy(matrixCache2, matrix);
		return multiply(matrixCache1, matrixCache2, matrix);
	}
	
	public static int[][] zRotationMatrix(int[][] matrix, int angle) {
		MatrixUtils.copy(matrix, MATRIX_IDENTITY);
		int cos = FixedPointUtils.cos(angle);
		int sin = FixedPointUtils.sin(angle);
		matrix[0][0] = cos;
		matrix[0][1] = sin;
		matrix[1][0] = -sin;
		matrix[1][1] = cos;
		return matrix;
	}
}
