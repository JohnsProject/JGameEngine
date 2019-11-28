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
package com.johnsproject.jgameengine.library;

import com.johnsproject.jgameengine.model.Camera;
import com.johnsproject.jgameengine.model.Transform;

import static com.johnsproject.jgameengine.library.VectorLibrary.*;
import static com.johnsproject.jgameengine.library.MathLibrary.*;

/**
 * The GraphicsLibrary class contains methods for generating matrices needed to move 
 * vectors between spaces and triangle drawing algorithms.
 * 
 * @author John Ferraz Salomon
 */
public class GraphicsLibrary {
	
	private final MathLibrary mathLibrary;
	private final MatrixLibrary matrixLibrary;
	private final VectorLibrary vectorLibrary;
	
	public GraphicsLibrary() {
		this.mathLibrary = new MathLibrary();
		this.matrixLibrary = new MatrixLibrary();
		this.vectorLibrary = new VectorLibrary();
	}

	/**
	 * Fills the given matrix with the values of the model matrix of the given transform.
	 * This matrix can be used to transform location vectors from local/model to world space.
	 * 
	 * @param matrix
	 * @param transform
	 * @return
	 */
	public int[] modelMatrix(int[] matrix, Transform transform, int[] translationMatrix, int[] result) {
		int[] location = transform.getLocation();
		int[] rotation = transform.getRotation();
		int[] scale = transform.getScale();
		matrixLibrary.copy(matrix, MatrixLibrary.MATRIX_IDENTITY);
		matrixLibrary.scale(matrix, scale, translationMatrix, result);
		matrixLibrary.rotateX(result, rotation[VECTOR_X], translationMatrix, matrix);
		matrixLibrary.rotateY(matrix, rotation[VECTOR_Y], translationMatrix, result);
		matrixLibrary.rotateZ(result, rotation[VECTOR_Z], translationMatrix, matrix);
		matrixLibrary.translate(matrix, location, translationMatrix, result);
		matrixLibrary.copy(matrix, result);
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
	public int[] normalMatrix(int[] matrix, Transform transform, int[] zRotationMatrix, int[] result) {
		int[] rotation = transform.getRotation();
		int[] scale = transform.getScale();
		matrixLibrary.copy(matrix, MatrixLibrary.MATRIX_IDENTITY);
		matrixLibrary.scale(matrix, scale, zRotationMatrix, result);
		matrixLibrary.rotateX(result, rotation[VECTOR_X], zRotationMatrix, matrix);
		matrixLibrary.rotateY(matrix, rotation[VECTOR_Y], zRotationMatrix, result);
		matrixLibrary.rotateZ(result, rotation[VECTOR_Z], zRotationMatrix, matrix);
		if ((scale[VECTOR_X] != scale[VECTOR_Y]) || (scale[VECTOR_Y] != scale[VECTOR_Z])) {
			matrixLibrary.inverse(matrix, result);
			matrixLibrary.transpose(result, matrix);
		}
		matrixLibrary.copy(result, matrix);
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
	public int[] viewMatrix(int[] matrix, Transform transform, int[] xRotationMatrix, int[] result) {
		int[] location = transform.getLocation();
		int[] rotation = transform.getRotation();
		int[] invertedLocation = vectorLibrary.invert(location, location);
		int[] invertedRotation = vectorLibrary.invert(rotation, rotation);
		matrixLibrary.copy(matrix, MatrixLibrary.MATRIX_IDENTITY);
		matrixLibrary.translate(matrix, location, xRotationMatrix, result);
		matrixLibrary.rotateZ(result, rotation[VECTOR_Z], xRotationMatrix, matrix);
		matrixLibrary.rotateY(matrix, rotation[VECTOR_Y], xRotationMatrix, result);
		matrixLibrary.rotateX(result, rotation[VECTOR_X], xRotationMatrix, matrix);
		location = vectorLibrary.invert(invertedLocation, location);
		rotation = vectorLibrary.invert(invertedRotation, rotation);
		matrixLibrary.copy(result, matrix);
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
	public int[] orthographicMatrix(int[] matrix, int focalLength) {
		int[] projectionMatrix = matrixLibrary.copy(matrix, MatrixLibrary.MATRIX_IDENTITY);
		matrixLibrary.set(projectionMatrix, 0, 0, focalLength);
		matrixLibrary.set(projectionMatrix, 1, 1, focalLength);
		matrixLibrary.set(projectionMatrix, 2, 2, -FP_ONE);
		matrixLibrary.set(projectionMatrix, 3, 3, -FP_ONE << 4);
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
	public int[] perspectiveMatrix(int[] matrix, int focalLength) {
		int[] projectionMatrix = matrixLibrary.copy(matrix, MatrixLibrary.MATRIX_IDENTITY);
		matrixLibrary.set(projectionMatrix, 0, 0, -focalLength);
		matrixLibrary.set(projectionMatrix, 1, 1, focalLength);
		matrixLibrary.set(projectionMatrix, 2, 2, -FP_ONE);
		matrixLibrary.set(projectionMatrix, 2, 3, FP_ONE);
		matrixLibrary.set(projectionMatrix, 3, 3, 0);
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
	public int[] screenportVector(int[] location, int[] cameraFrustum, int[] result) {
		int top = cameraFrustum[Camera.FRUSTUM_TOP];
		int bottom = cameraFrustum[Camera.FRUSTUM_BOTTOM];
		int left = cameraFrustum[Camera.FRUSTUM_LEFT];
		int right = cameraFrustum[Camera.FRUSTUM_RIGHT];
		int near = cameraFrustum[Camera.FRUSTUM_NEAR];
		int far = cameraFrustum[Camera.FRUSTUM_FAR];
		int scaleFactor = bottom - top + 1;
		int halfX = left + ((right - left) >> 1);
		int halfY = top + ((bottom - top) >> 1);
		int w = location[VECTOR_W];
		w = mathLibrary.divide(FP_ONE, w == 0 ? 1 : w);
		result[VECTOR_X] = mathLibrary.multiply(location[VECTOR_X], scaleFactor);
		result[VECTOR_X] = mathLibrary.multiply(result[VECTOR_X], w) + halfX;
		result[VECTOR_Y] = mathLibrary.multiply(location[VECTOR_Y], scaleFactor);
		result[VECTOR_Y] = mathLibrary.multiply(result[VECTOR_Y], w) + halfY;
		result[VECTOR_Z] = mathLibrary.divide(location[VECTOR_Z] - near, far - near);
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
	public int[] screenportFrustum(int[] cameraFrustum, int screenWidth, int screenHeight, int[] result) {
		result[Camera.FRUSTUM_LEFT] = mathLibrary.multiply(screenWidth, cameraFrustum[Camera.FRUSTUM_LEFT]);
		result[Camera.FRUSTUM_RIGHT] = mathLibrary.multiply(screenWidth, cameraFrustum[Camera.FRUSTUM_RIGHT]);
		result[Camera.FRUSTUM_TOP] = mathLibrary.multiply(screenHeight, cameraFrustum[Camera.FRUSTUM_TOP]);
		result[Camera.FRUSTUM_BOTTOM] = mathLibrary.multiply(screenHeight, cameraFrustum[Camera.FRUSTUM_BOTTOM]);
		result[Camera.FRUSTUM_NEAR] = cameraFrustum[Camera.FRUSTUM_NEAR];
		result[Camera.FRUSTUM_FAR] = cameraFrustum[Camera.FRUSTUM_FAR];
		return result;
	}
}
