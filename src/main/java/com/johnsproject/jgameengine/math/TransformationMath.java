package com.johnsproject.jgameengine.math;

import static com.johnsproject.jgameengine.math.FixedPointMath.*;
import static com.johnsproject.jgameengine.math.MatrixMath.*;
import static com.johnsproject.jgameengine.math.VectorMath.*;
import static com.johnsproject.jgameengine.model.Camera.*;

import com.johnsproject.jgameengine.model.Transform;

public final class TransformationMath {
	
	private TransformationMath() { }
	
	public static int[] spaceExitMatrix(int[] matrix, Transform transform, int[] translationMatrix, int[] result) {
		int[] location = transform.getLocation();
		int[] rotation = transform.getRotation();
		int[] scale = transform.getScale();
		MatrixMath.copy(matrix, MatrixMath.MATRIX_IDENTITY);
		scale(matrix, scale, translationMatrix, result);
		rotateX(result, rotation[VECTOR_X], translationMatrix, matrix);
		rotateY(matrix, rotation[VECTOR_Y], translationMatrix, result);
		rotateZ(result, rotation[VECTOR_Z], translationMatrix, matrix);
		translate(matrix, location, translationMatrix, result);
		MatrixMath.copy(matrix, result);
		return result;
	}
	
	public static int[] spaceExitNormalMatrix(int[] matrix, Transform transform, int[] zRotationMatrix, int[] result) {
		int[] rotation = transform.getRotation();
		int[] scale = transform.getScale();
		MatrixMath.copy(matrix, MatrixMath.MATRIX_IDENTITY);
		scale(matrix, scale, zRotationMatrix, result);
		rotateX(result, rotation[VECTOR_X], zRotationMatrix, matrix);
		rotateY(matrix, rotation[VECTOR_Y], zRotationMatrix, result);
		rotateZ(result, rotation[VECTOR_Z], zRotationMatrix, matrix);
		if ((scale[VECTOR_X] != scale[VECTOR_Y]) || (scale[VECTOR_Y] != scale[VECTOR_Z])) {
			MatrixMath.inverse(matrix, result);
			MatrixMath.transpose(result, matrix);
		}
		MatrixMath.copy(result, matrix);
		return result;
	}

	public static int[] spaceEnterMatrix(int[] matrix, Transform transform, int[] scaleMatrix, int[] result) {
		int[] location = transform.getLocation();
		int[] rotation = transform.getRotation();
		int[] scale = transform.getScale();
		int scaleX = FixedPointMath.divide(FP_ONE, scale[VECTOR_X] == 0 ? 1 : scale[VECTOR_X]);
		int scaleY = FixedPointMath.divide(FP_ONE, scale[VECTOR_Y] == 0 ? 1 : scale[VECTOR_Y]);
		int scaleZ = FixedPointMath.divide(FP_ONE, scale[VECTOR_Z] == 0 ? 1 : scale[VECTOR_Z]);
		VectorMath.invert(location);
		VectorMath.invert(rotation);
		MatrixMath.copy(matrix, MatrixMath.MATRIX_IDENTITY);
		translate(matrix, location, scaleMatrix, result);
		rotateZ(result, rotation[VECTOR_Z], scaleMatrix, matrix);
		rotateY(matrix, rotation[VECTOR_Y], scaleMatrix, result);
		rotateX(result, rotation[VECTOR_X], scaleMatrix, matrix);
		scale(matrix, scaleX, scaleY, scaleZ, scaleMatrix, result);
		VectorMath.invert(location);
		VectorMath.invert(rotation);
		MatrixMath.copy(result, matrix);
		return result;
	}
	
	public static int[] spaceEnterNormalMatrix(int[] matrix, Transform transform, int[] scaleMatrix, int[] result) {
		int[] rotation = transform.getRotation();
		int[] scale = transform.getScale();
		int scaleX = FixedPointMath.divide(FP_ONE, scale[VECTOR_X] == 0 ? 1 : scale[VECTOR_X]);
		int scaleY = FixedPointMath.divide(FP_ONE, scale[VECTOR_Y] == 0 ? 1 : scale[VECTOR_Y]);
		int scaleZ = FixedPointMath.divide(FP_ONE, scale[VECTOR_Z] == 0 ? 1 : scale[VECTOR_Z]);
		VectorMath.invert(rotation);
		MatrixMath.copy(matrix, MatrixMath.MATRIX_IDENTITY);
		rotateZ(result, rotation[VECTOR_Z], scaleMatrix, matrix);
		rotateY(matrix, rotation[VECTOR_Y], scaleMatrix, result);
		rotateX(result, rotation[VECTOR_X], scaleMatrix, matrix);
		scale(matrix, scaleX, scaleY, scaleZ, scaleMatrix, result);
		VectorMath.invert(rotation);
		if ((scale[VECTOR_X] != scale[VECTOR_Y]) || (scale[VECTOR_Y] != scale[VECTOR_Z])) {
			MatrixMath.inverse(matrix, result);
			MatrixMath.transpose(result, matrix);
		}
		MatrixMath.copy(result, matrix);
		return result;
	}

	public static int[] orthographicMatrix(int[] matrix, int[] cameraFrustum, int focalLength) {
		int top = cameraFrustum[FRUSTUM_TOP];
		int bottom = cameraFrustum[FRUSTUM_BOTTOM];
		int near = cameraFrustum[FRUSTUM_NEAR];
		int far = cameraFrustum[FRUSTUM_FAR];		
		int farNear = far - near;
		int scaleFactor = FixedPointMath.multiply(focalLength, bottom - top + 1);
		int[] projectionMatrix = MatrixMath.copy(matrix, MatrixMath.MATRIX_IDENTITY);
		MatrixMath.set(projectionMatrix, 0, 0, scaleFactor);
		MatrixMath.set(projectionMatrix, 1, 1, scaleFactor);
		MatrixMath.set(projectionMatrix, 2, 2, -FixedPointMath.divide(FP_ONE, farNear));
		MatrixMath.set(projectionMatrix, 3, 2, -FixedPointMath.divide(near, farNear));
		MatrixMath.set(projectionMatrix, 3, 3, -FP_ONE << 4);
		return projectionMatrix;
	}

	public static int[] perspectiveMatrix(int[] matrix, int[] cameraFrustum, int focalLength) {
		int top = cameraFrustum[FRUSTUM_TOP];
		int bottom = cameraFrustum[FRUSTUM_BOTTOM];
		int near = cameraFrustum[FRUSTUM_NEAR];
		int far = cameraFrustum[FRUSTUM_FAR];
		int farNear = far - near;
		int scaleFactor = FixedPointMath.multiply(focalLength, bottom - top + 1);
		int[] projectionMatrix = MatrixMath.copy(matrix, MatrixMath.MATRIX_IDENTITY);
		MatrixMath.set(projectionMatrix, 0, 0, -scaleFactor);
		MatrixMath.set(projectionMatrix, 1, 1, scaleFactor);
		MatrixMath.set(projectionMatrix, 2, 2, -FixedPointMath.divide(FP_ONE, farNear));
		MatrixMath.set(projectionMatrix, 3, 2, -FixedPointMath.divide(near, farNear));
		MatrixMath.set(projectionMatrix, 2, 3, FP_ONE);
		MatrixMath.set(projectionMatrix, 3, 3, 0);
		return projectionMatrix;
	}

	public static int[] screenportVector(int[] location, int[] cameraFrustum) {
		int top = cameraFrustum[FRUSTUM_TOP];
		int bottom = cameraFrustum[FRUSTUM_BOTTOM];
		int left = cameraFrustum[FRUSTUM_LEFT];
		int right = cameraFrustum[FRUSTUM_RIGHT];
		int halfX = left + ((right - left) >> 1);
		int halfY = top + ((bottom - top) >> 1);
		int w = location[VECTOR_W];
		w = FixedPointMath.divide(FP_ONE, w == 0 ? 1 : w);
		location[VECTOR_X] = FixedPointMath.multiply(location[VECTOR_X], w) + halfX;
		location[VECTOR_Y] = FixedPointMath.multiply(location[VECTOR_Y], w) + halfY;
		return location;
	}

	public static int[] screenportFrustum(int[] cameraFrustum, int screenWidth, int screenHeight) {
		cameraFrustum[FRUSTUM_LEFT] = FixedPointMath.multiply(screenWidth, cameraFrustum[FRUSTUM_LEFT]);
		cameraFrustum[FRUSTUM_RIGHT] = FixedPointMath.multiply(screenWidth, cameraFrustum[FRUSTUM_RIGHT]);
		cameraFrustum[FRUSTUM_TOP] = FixedPointMath.multiply(screenHeight, cameraFrustum[FRUSTUM_TOP]);
		cameraFrustum[FRUSTUM_BOTTOM] = FixedPointMath.multiply(screenHeight, cameraFrustum[FRUSTUM_BOTTOM]);
		cameraFrustum[FRUSTUM_NEAR] = cameraFrustum[FRUSTUM_NEAR];
		cameraFrustum[FRUSTUM_FAR] = cameraFrustum[FRUSTUM_FAR];
		return cameraFrustum;
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
		vector[VECTOR_X] = FixedPointMath.multiply(vector[VECTOR_X], factor);
		vector[VECTOR_Y] = FixedPointMath.multiply(vector[VECTOR_Y], factor);
		vector[VECTOR_Z] = FixedPointMath.multiply(vector[VECTOR_Z], factor);
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
		int dot = 2 * VectorMath.dotProduct(vector, reflectionVector);
		VectorMath.multiply(reflectionVector, dot);
		VectorMath.subtract(vector, reflectionVector);
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
		int sin = FixedPointMath.sin(angle);
		int cos = FixedPointMath.cos(angle);
		int x = vector[VECTOR_X];
		int y = vector[VECTOR_Y];
		int z = vector[VECTOR_Z];
		vector[VECTOR_X] = x;
		vector[VECTOR_Y] = FixedPointMath.multiply(y, cos);
		vector[VECTOR_Y] -= FixedPointMath.multiply(z, sin);
		vector[VECTOR_Z] = FixedPointMath.multiply(z, cos);
		vector[VECTOR_Z] += FixedPointMath.multiply(y, sin);
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
		// TODO fix the need of negative angles as in rotation matrix
		int sin = FixedPointMath.sin(-angle);
		int cos = FixedPointMath.cos(-angle);
		int x = vector[VECTOR_X];
		int y = vector[VECTOR_Y];
		int z = vector[VECTOR_Z];
		vector[VECTOR_X] = FixedPointMath.multiply(x, cos);
		vector[VECTOR_X] -= FixedPointMath.multiply(z, sin);
		vector[VECTOR_Y] = y;
		vector[VECTOR_Z] = FixedPointMath.multiply(z, cos);
		vector[VECTOR_Z] += FixedPointMath.multiply(x, sin);
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
		// TODO fix the need of negative angles as in rotation matrix
		int sin = FixedPointMath.sin(-angle);
		int cos = FixedPointMath.cos(-angle);
		int x = vector[VECTOR_X];
		int y = vector[VECTOR_Y];
		int z = vector[VECTOR_Z];
		vector[VECTOR_X] = FixedPointMath.multiply(x, cos);
		vector[VECTOR_X] -= FixedPointMath.multiply(y, sin);
		vector[VECTOR_Y] = FixedPointMath.multiply(y, cos);
		vector[VECTOR_Y] += FixedPointMath.multiply(x, sin);
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
	public static int[] translate(int[] matrix, int[] vector, int[] translationMatrix, int[] result) {
		return translate(matrix, vector[VECTOR_X], vector[VECTOR_Y], vector[VECTOR_Z], translationMatrix, result);
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
	public static int[] translate(int[] matrix, int x, int y , int z, int[] translationMatrix, int[] result) {
		translationMatrix(translationMatrix, x, y, z);
		return multiply(translationMatrix, matrix, result);
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
	public static int[] translationMatrix(int[] matrix, int[] vector) {
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
	public static int[] translationMatrix(int[] matrix, int x, int y , int z) {
		MatrixMath.copy(matrix, MATRIX_IDENTITY);
		MatrixMath.set(matrix, 3, 0, x);
		MatrixMath.set(matrix, 3, 1, y);
		MatrixMath.set(matrix, 3, 2, z);
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
	public static int[] scale(int[] matrix, int[] vector, int[] scaleMatrix, int[] result) {
		return scale(matrix, vector[VECTOR_X], vector[VECTOR_Y], vector[VECTOR_Z], scaleMatrix, result);
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
	public static int[] scale(int[] matrix, int x, int y, int z, int[] scaleMatrix, int[] result) {
		scaleMatrix(scaleMatrix, x, y, z);
		return multiply(scaleMatrix, matrix, result);
	}
	
	public static int[] scaleMatrix(int[] matrix, int[] vector) {
		return scaleMatrix(matrix, vector[VECTOR_X], vector[VECTOR_Y], vector[VECTOR_Z]);
	}
	
	public static int[] scaleMatrix(int[] matrix, int x, int y, int z) {
		MatrixMath.copy(matrix, MATRIX_IDENTITY);
		MatrixMath.set(matrix, 0, 0, x);
		MatrixMath.set(matrix, 1, 1, y);
		MatrixMath.set(matrix, 2, 2, z);
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
	public static int[] rotateX(int[] matrix, int angle, int[] xRotationMatrix, int[] result) {
		xRotationMatrix(xRotationMatrix, angle);
		return multiply(xRotationMatrix, matrix, result);
	}
	
	public static int[] xRotationMatrix(int[] matrix, int angle) {
		MatrixMath.copy(matrix, MATRIX_IDENTITY);
		int cos = FixedPointMath.cos(angle);
		int sin = FixedPointMath.sin(angle);
		MatrixMath.set(matrix, 1, 1, cos);
		MatrixMath.set(matrix, 1, 2, sin);
		MatrixMath.set(matrix, 2, 1, -sin);
		MatrixMath.set(matrix, 2, 2, cos);
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
	public static int[] rotateY(int[] matrix, int angle, int[] yRotationMatrix, int[] result) {
		yRotationMatrix(yRotationMatrix, angle);
		return multiply(yRotationMatrix, matrix, result);
	}
	
	public static int[] yRotationMatrix(int[] matrix, int angle) {
		MatrixMath.copy(matrix, MATRIX_IDENTITY);
		int cos = FixedPointMath.cos(angle);
		int sin = FixedPointMath.sin(angle);
		MatrixMath.set(matrix, 0, 0, cos);
		MatrixMath.set(matrix, 0, 2, -sin);
		MatrixMath.set(matrix, 2, 0, sin);
		MatrixMath.set(matrix, 2, 2, cos);
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
	public static int[] rotateZ(int[] matrix, int angle, int[] zRotationMatrix, int[] result) {
		zRotationMatrix(zRotationMatrix, angle);
		return multiply(zRotationMatrix, matrix, result);
	}
	
	public static int[] zRotationMatrix(int[] matrix, int angle) {
		MatrixMath.copy(matrix, MATRIX_IDENTITY);
		int cos = FixedPointMath.cos(angle);
		int sin = FixedPointMath.sin(angle);
		MatrixMath.set(matrix, 0, 0, cos);
		MatrixMath.set(matrix, 0, 1, sin);
		MatrixMath.set(matrix, 1, 0, -sin);
		MatrixMath.set(matrix, 1, 1, cos);
		return matrix;
	}
}
