package com.johnsproject.jgameengine.library;

import static com.johnsproject.jgameengine.library.MathLibrary.*;
import static com.johnsproject.jgameengine.library.VectorLibrary.*;
import static com.johnsproject.jgameengine.library.MatrixLibrary.*;
import static com.johnsproject.jgameengine.model.Camera.*;

import com.johnsproject.jgameengine.model.Transform;

public final class TransformationLibrary {
	
	private TransformationLibrary() { }
	
	/**
	 * Fills the given matrix with the values of the model matrix of the given transform.
	 * This matrix can be used to transform location vectors from local/model to world space.
	 * 
	 * @param matrix
	 * @param transform
	 * @return
	 */
	public static int[] modelMatrix(int[] matrix, Transform transform, int[] translationMatrix, int[] result) {
		int[] location = transform.getLocation();
		int[] rotation = transform.getRotation();
		int[] scale = transform.getScale();
		MatrixLibrary.copy(matrix, MatrixLibrary.MATRIX_IDENTITY);
		scale(matrix, scale, translationMatrix, result);
		rotateX(result, rotation[VECTOR_X], translationMatrix, matrix);
		rotateY(matrix, rotation[VECTOR_Y], translationMatrix, result);
		rotateZ(result, rotation[VECTOR_Z], translationMatrix, matrix);
		translate(matrix, location, translationMatrix, result);
		MatrixLibrary.copy(matrix, result);
		return result;
	}

	/**
	 * Fills the given matrix with the values of the normal matrix of the given transform.
	 * This matrix can be used to transform normal vectors from local/model to world space.
	 * 
	 * @param matrix
	 * @param transform
	 * @return
	 */
	public static int[] normalMatrix(int[] matrix, Transform transform, int[] zRotationMatrix, int[] result) {
		int[] rotation = transform.getRotation();
		int[] scale = transform.getScale();
		MatrixLibrary.copy(matrix, MatrixLibrary.MATRIX_IDENTITY);
		scale(matrix, scale, zRotationMatrix, result);
		rotateX(result, rotation[VECTOR_X], zRotationMatrix, matrix);
		rotateY(matrix, rotation[VECTOR_Y], zRotationMatrix, result);
		rotateZ(result, rotation[VECTOR_Z], zRotationMatrix, matrix);
		if ((scale[VECTOR_X] != scale[VECTOR_Y]) || (scale[VECTOR_Y] != scale[VECTOR_Z])) {
			MatrixLibrary.inverse(matrix, result);
			MatrixLibrary.transpose(result, matrix);
		}
		MatrixLibrary.copy(result, matrix);
		return result;
	}
	
	/**
	 * Fills the given matrix with the values of the view matrix of the given transform.
	 * This matrix can be used to transform location vectors from world to view/camera space.
	 * 
	 * @param matrix
	 * @param transform
	 * @return
	 */
	public static int[] viewMatrix(int[] matrix, Transform transform, int[] scaleMatrix, int[] result) {
		int[] location = transform.getLocation();
		int[] rotation = transform.getRotation();
		int[] scale = transform.getScale();
		int scaleX = MathLibrary.divide(FP_ONE, scale[VECTOR_X] == 0 ? 1 : scale[VECTOR_X]);
		int scaleY = MathLibrary.divide(FP_ONE, scale[VECTOR_Y] == 0 ? 1 : scale[VECTOR_Y]);
		int scaleZ = MathLibrary.divide(FP_ONE, scale[VECTOR_Z] == 0 ? 1 : scale[VECTOR_Z]);
		VectorLibrary.invert(location);
		VectorLibrary.invert(rotation);
		MatrixLibrary.copy(matrix, MatrixLibrary.MATRIX_IDENTITY);
		translate(matrix, location, scaleMatrix, result);
		rotateZ(result, rotation[VECTOR_Z], scaleMatrix, matrix);
		rotateY(matrix, rotation[VECTOR_Y], scaleMatrix, result);
		rotateX(result, rotation[VECTOR_X], scaleMatrix, matrix);
		scale(matrix, scaleX, scaleY, scaleZ, scaleMatrix, result);
		VectorLibrary.invert(location);
		VectorLibrary.invert(rotation);
		MatrixLibrary.copy(result, matrix);
		return result;
	}

	
	/**
	 * Fills the given matrix with the values of the projection matrix of the given focal length.
	 * This matrix can be used to orthographic project location vectors from view/camera to projection space.
	 * To get the vectors into screen space it's needed to {@link #screenportVector} them.
	 * 
	 * @param matrix
	 * @param focalLength
	 * @return
	 */
	public static int[] orthographicMatrix(int[] matrix, int focalLength) {
		int[] projectionMatrix = MatrixLibrary.copy(matrix, MatrixLibrary.MATRIX_IDENTITY);
		MatrixLibrary.set(projectionMatrix, 0, 0, focalLength);
		MatrixLibrary.set(projectionMatrix, 1, 1, focalLength);
		MatrixLibrary.set(projectionMatrix, 2, 2, -FP_ONE);
		MatrixLibrary.set(projectionMatrix, 3, 3, -FP_ONE << 4);
		return projectionMatrix;
	}

	/**
	 * Fills the given matrix with the values of the projection matrix of the given focal length.
	 * This matrix can be used to perspective project location vectors from view/camera to projection space.
	 * To get the vectors into screen space it's needed to {@link #screenportVector} them.
	 * 
	 * @param matrix
	 * @param focalLength
	 * @return
	 */
	public static int[] perspectiveMatrix(int[] matrix, int focalLength) {
		int[] projectionMatrix = MatrixLibrary.copy(matrix, MatrixLibrary.MATRIX_IDENTITY);
		MatrixLibrary.set(projectionMatrix, 0, 0, -focalLength);
		MatrixLibrary.set(projectionMatrix, 1, 1, focalLength);
		MatrixLibrary.set(projectionMatrix, 2, 2, -FP_ONE);
		MatrixLibrary.set(projectionMatrix, 2, 3, FP_ONE);
		MatrixLibrary.set(projectionMatrix, 3, 3, 0);
		return projectionMatrix;
	}

	/**
	 * Sets result equals location in screen space.
	 * This methods needs location to be in projection space, 
	 * vectors can be transformed into projection space by multiplying them by the 
	 * {@link #perspectiveMatrix perspective} or the {@link #orthographicMatrix orthographic} projection matrix.
	 * This method requires cameraFrustum to be already {@link #screenportFrustum ported}.
	 * 
	 * @param location
	 * @param cameraFrustum
	 * @param result
	 * @return
	 */
	public static int[] screenportVector(int[] location, int[] cameraFrustum, int[] result) {
		int top = cameraFrustum[FRUSTUM_TOP];
		int bottom = cameraFrustum[FRUSTUM_BOTTOM];
		int left = cameraFrustum[FRUSTUM_LEFT];
		int right = cameraFrustum[FRUSTUM_RIGHT];
		int near = cameraFrustum[FRUSTUM_NEAR];
		int far = cameraFrustum[FRUSTUM_FAR];
		int scaleFactor = bottom - top + 1;
		int halfX = left + ((right - left) >> 1);
		int halfY = top + ((bottom - top) >> 1);
		int w = location[VECTOR_W];
		w = MathLibrary.divide(FP_ONE, w == 0 ? 1 : w);
		result[VECTOR_X] = MathLibrary.multiply(location[VECTOR_X], scaleFactor);
		result[VECTOR_X] = MathLibrary.multiply(result[VECTOR_X], w) + halfX;
		result[VECTOR_Y] = MathLibrary.multiply(location[VECTOR_Y], scaleFactor);
		result[VECTOR_Y] = MathLibrary.multiply(result[VECTOR_Y], w) + halfY;
		result[VECTOR_Z] = MathLibrary.divide(location[VECTOR_Z] - near, far - near);
		result[VECTOR_W] = FP_ONE;
		return result;
	}

	/**
	 * Sets result equals the cameraFrustum ported into the given width and height.
	 * This method fits the given cameraFrustum into the given screen size, 
	 * this is needed to correctly {@link #screenportVector screenport} vectors.
	 * 
	 * 
	 * @param cameraFrustum
	 * @param screenWidth
	 * @param screenHeight
	 * @param result
	 * @return
	 */
	public static int[] screenportFrustum(int[] cameraFrustum, int screenWidth, int screenHeight, int[] result) {
		result[FRUSTUM_LEFT] = MathLibrary.multiply(screenWidth, cameraFrustum[FRUSTUM_LEFT]);
		result[FRUSTUM_RIGHT] = MathLibrary.multiply(screenWidth, cameraFrustum[FRUSTUM_RIGHT]);
		result[FRUSTUM_TOP] = MathLibrary.multiply(screenHeight, cameraFrustum[FRUSTUM_TOP]);
		result[FRUSTUM_BOTTOM] = MathLibrary.multiply(screenHeight, cameraFrustum[FRUSTUM_BOTTOM]);
		result[FRUSTUM_NEAR] = cameraFrustum[FRUSTUM_NEAR];
		result[FRUSTUM_FAR] = cameraFrustum[FRUSTUM_FAR];
		return result;
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
		vector[VECTOR_X] = MathLibrary.multiply(vector[VECTOR_X], factor);
		vector[VECTOR_Y] = MathLibrary.multiply(vector[VECTOR_Y], factor);
		vector[VECTOR_Z] = MathLibrary.multiply(vector[VECTOR_Z], factor);
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
		int dot = 2 * VectorLibrary.dotProduct(vector, reflectionVector);
		VectorLibrary.multiply(reflectionVector, dot);
		VectorLibrary.subtract(vector, reflectionVector);
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
		int sin = MathLibrary.sin(angle);
		int cos = MathLibrary.cos(angle);
		int x = vector[VECTOR_X];
		int y = vector[VECTOR_Y];
		int z = vector[VECTOR_Z];
		vector[VECTOR_X] = x;
		vector[VECTOR_Y] = MathLibrary.multiply(y, cos);
		vector[VECTOR_Y] -= MathLibrary.multiply(z, sin);
		vector[VECTOR_Z] = MathLibrary.multiply(z, cos);
		vector[VECTOR_Z] += MathLibrary.multiply(y, sin);
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
		int sin = MathLibrary.sin(-angle);
		int cos = MathLibrary.cos(-angle);
		int x = vector[VECTOR_X];
		int y = vector[VECTOR_Y];
		int z = vector[VECTOR_Z];
		vector[VECTOR_X] = MathLibrary.multiply(x, cos);
		vector[VECTOR_X] -= MathLibrary.multiply(z, sin);
		vector[VECTOR_Y] = y;
		vector[VECTOR_Z] = MathLibrary.multiply(z, cos);
		vector[VECTOR_Z] += MathLibrary.multiply(x, sin);
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
		int sin = MathLibrary.sin(-angle);
		int cos = MathLibrary.cos(-angle);
		int x = vector[VECTOR_X];
		int y = vector[VECTOR_Y];
		int z = vector[VECTOR_Z];
		vector[VECTOR_X] = MathLibrary.multiply(x, cos);
		vector[VECTOR_X] -= MathLibrary.multiply(y, sin);
		vector[VECTOR_Y] = MathLibrary.multiply(y, cos);
		vector[VECTOR_Y] += MathLibrary.multiply(x, sin);
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
		MatrixLibrary.copy(matrix, MATRIX_IDENTITY);
		MatrixLibrary.set(matrix, 3, 0, x);
		MatrixLibrary.set(matrix, 3, 1, y);
		MatrixLibrary.set(matrix, 3, 2, z);
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
		MatrixLibrary.copy(matrix, MATRIX_IDENTITY);
		MatrixLibrary.set(matrix, 0, 0, x);
		MatrixLibrary.set(matrix, 1, 1, y);
		MatrixLibrary.set(matrix, 2, 2, z);
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
		MatrixLibrary.copy(matrix, MATRIX_IDENTITY);
		int cos = MathLibrary.cos(angle);
		int sin = MathLibrary.sin(angle);
		MatrixLibrary.set(matrix, 1, 1, cos);
		MatrixLibrary.set(matrix, 1, 2, sin);
		MatrixLibrary.set(matrix, 2, 1, -sin);
		MatrixLibrary.set(matrix, 2, 2, cos);
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
		MatrixLibrary.copy(matrix, MATRIX_IDENTITY);
		int cos = MathLibrary.cos(angle);
		int sin = MathLibrary.sin(angle);
		MatrixLibrary.set(matrix, 0, 0, cos);
		MatrixLibrary.set(matrix, 0, 2, -sin);
		MatrixLibrary.set(matrix, 2, 0, sin);
		MatrixLibrary.set(matrix, 2, 2, cos);
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
		MatrixLibrary.copy(matrix, MATRIX_IDENTITY);
		int cos = MathLibrary.cos(angle);
		int sin = MathLibrary.sin(angle);
		MatrixLibrary.set(matrix, 0, 0, cos);
		MatrixLibrary.set(matrix, 0, 1, sin);
		MatrixLibrary.set(matrix, 1, 0, -sin);
		MatrixLibrary.set(matrix, 1, 1, cos);
		return matrix;
	}
}
