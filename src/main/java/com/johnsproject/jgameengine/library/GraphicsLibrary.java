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

import com.johnsproject.jgameengine.dto.Camera;
import com.johnsproject.jgameengine.dto.GeometryDataBuffer;
import com.johnsproject.jgameengine.dto.Texture;
import com.johnsproject.jgameengine.dto.Transform;
import com.johnsproject.jgameengine.shader.AffineFlatTriangle;
import com.johnsproject.jgameengine.shader.AffineGouraudTriangle;
import com.johnsproject.jgameengine.shader.FlatTriangle;
import com.johnsproject.jgameengine.shader.GouraudTriangle;
import com.johnsproject.jgameengine.shader.PerspectiveFlatTriangle;
import com.johnsproject.jgameengine.shader.PerspectiveGouraudTriangle;

/**
 * The GraphicsLibrary class contains methods for generating 
 * matrices needed to move location vectors between spaces 
 * and triangle drawing algorithms.
 * 
 * @author John Ferraz Salomon
 */
public class GraphicsLibrary {
	
	private static final byte VECTOR_X = VectorLibrary.VECTOR_X;
	private static final byte VECTOR_Y = VectorLibrary.VECTOR_Y;
	private static final byte VECTOR_Z = VectorLibrary.VECTOR_Z;
	private static final byte VECTOR_W = VectorLibrary.VECTOR_W;

	private static final int FP_ONE = MathLibrary.FP_ONE;
	private static final int FP_HALF = MathLibrary.FP_HALF;
	
	private final MathLibrary mathLibrary;
	private final MatrixLibrary matrixLibrary;
	private final VectorLibrary vectorLibrary;
	private final ColorLibrary colorLibrary;
	
	public GraphicsLibrary() {
		this.mathLibrary = new MathLibrary();
		this.matrixLibrary = new MatrixLibrary();
		this.vectorLibrary = new VectorLibrary();
		this.colorLibrary = new ColorLibrary();
	}

	/**
	 * Fills the given matrix with the values of the model matrix of the given transform.
	 * This matrix can be used to transform location vectors from local/model to world space.
	 * 
	 * @param matrix
	 * @param transform
	 * @return
	 */
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

	/**
	 * Fills the given matrix with the values of the normal matrix of the given transform.
	 * This matrix can be used to transform normal vectors from local/model to world space.
	 * 
	 * @param matrix
	 * @param transform
	 * @return
	 */
	public int[][] normalMatrix(int[][] matrix, Transform transform) {
		int[] rotation = transform.getRotation();
		int[] scale = transform.getScale();
		matrixLibrary.copy(matrix, MatrixLibrary.MATRIX_IDENTITY);
		matrixLibrary.scale(matrix, scale, matrix);
		matrixLibrary.rotateXYZ(matrix, rotation, matrix);
		if ((scale[VECTOR_X] != scale[VECTOR_Y]) | (scale[VECTOR_Y] != scale[VECTOR_Z])) {
			matrixLibrary.inverse(matrix, matrix);
			matrixLibrary.transpose(matrix, matrix);
		}
		return matrix;
	}
	
	/**
	 * Fills the given matrix with the values of the view matrix of the given transform.
	 * This matrix can be used to transform location vectors from world to view/camera space.
	 * 
	 * @param matrix
	 * @param transform
	 * @return
	 */
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

	
	/**
	 * Fills the given matrix with the values of the projection matrix of the given cameraFrustum.
	 * This matrix can be used to orthographic project location vectors from view/camera to projection space.
	 * To get the vectors into screen space it's needed to {@link #screenportVector} them.
	 * This method requires cameraFrustum to be already {@link #screenportFrustum ported}.
	 * 
	 * @param matrix
	 * @param cameraFrustum
	 * @return
	 */
	public int[][] orthographicMatrix(int[][] matrix, int[] cameraFrustum) {
		matrixLibrary.copy(matrix, MatrixLibrary.MATRIX_IDENTITY);
		int scaleFactor = cameraFrustum[Camera.FRUSTUM_NEAR];
		matrix[0][0] = scaleFactor;
		matrix[1][1] = scaleFactor;
		matrix[2][2] = -FP_ONE / 10;
		matrix[3][3] = FP_ONE * -FP_HALF;
		return matrix;
	}

	/**
	 * Fills the given matrix with the values of the projection matrix of the given cameraFrustum.
	 * This matrix can be used to perspective project location vectors from view/camera to projection space.
	 * To get the vectors into screen space it's needed to {@link #screenportVector} them.
	 * This method requires cameraFrustum to be already {@link #screenportFrustum ported}.
	 * 
	 * @param matrix
	 * @param cameraFrustum
	 * @return
	 */
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
		int scaleFactor = ((bottom - top) >> 6) + 1;
		int halfX = left + ((right - left) >> 1);
		int halfY = top + ((bottom - top) >> 1);
		int w = Math.min(-1, location[VECTOR_W]);
		result[VECTOR_X] = mathLibrary.divide(location[VECTOR_X] * scaleFactor, w) + halfX;
		result[VECTOR_Y] = mathLibrary.divide(location[VECTOR_Y] * scaleFactor, w) + halfY;
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
	
	/**
	 * Draws a triangle using flat shading.
	 * This method requires cameraFrustum to be already {@link #screenportFrustum ported}.
	 * 
	 * @param triangle
	 * @param dataBuffer
	 * @param cameraFrustum
	 */
	public void drawFlatTriangle(FlatTriangle triangle, GeometryDataBuffer dataBuffer, int[] cameraFrustum) {
		if (clip(dataBuffer, cameraFrustum))
			return;
		copyLocations(triangle, dataBuffer);
		triangle.drawTriangle(cameraFrustum);
	}
	
	/**
	 * Draws a triangle using gouraud shading.
	 * This method requires cameraFrustum to be already {@link #screenportFrustum ported}.
	 * 
	 * @param triangle
	 * @param dataBuffer
	 * @param cameraFrustum
	 */
	public void drawGouraudTriangle(GouraudTriangle triangle, GeometryDataBuffer dataBuffer, int[] cameraFrustum) {
		if (clip(dataBuffer, cameraFrustum))
			return;
		copyColors(triangle, dataBuffer);
		copyLocations(triangle, dataBuffer);
		triangle.drawTriangle(cameraFrustum);
	}
	
	/**
	 * Draws a triangle using flat shading and affine texture mapping.
	 * This method requires cameraFrustum to be already {@link #screenportFrustum ported}.
	 * 
	 * @param triangle
	 * @param dataBuffer
	 * @param texture
	 * @param cameraFrustum
	 */
	public void drawAffineFlatTriangle(AffineFlatTriangle triangle, GeometryDataBuffer dataBuffer, Texture texture, int[] cameraFrustum) {
		if (clip(dataBuffer, cameraFrustum))
			return;
		copyUVs(triangle, dataBuffer, texture);
		copyLocations(triangle, dataBuffer);
		triangle.drawTriangle(cameraFrustum);
	}
	
	/**
	 * Draws a triangle using flat shading and perspective correct texture mapping.
	 * This method requires cameraFrustum to be already {@link #screenportFrustum ported}.
	 * 
	 * @param triangle
	 * @param dataBuffer
	 * @param texture
	 * @param cameraFrustum
	 */
	public void drawPerspectiveFlatTriangle(PerspectiveFlatTriangle triangle, GeometryDataBuffer dataBuffer, Texture texture, int[] cameraFrustum) {
		if (clip(dataBuffer, cameraFrustum))
			return;
		copyUVs(triangle, dataBuffer, texture);
		copyLocations(triangle, dataBuffer);
		triangle.drawTriangle(cameraFrustum);
	}
	
	/**
	 * Draws a triangle using gouraud shading and affine texture mapping.
	 * This method requires cameraFrustum to be already {@link #screenportFrustum ported}.
	 * 
	 * @param triangle
	 * @param dataBuffer
	 * @param texture
	 * @param cameraFrustum
	 */
	public void drawAffineGouraudTriangle(AffineGouraudTriangle triangle, GeometryDataBuffer dataBuffer, Texture texture, int[] cameraFrustum) {
		if (clip(dataBuffer, cameraFrustum))
			return;
		copyUVs(triangle, dataBuffer, texture);
		copyColors(triangle, dataBuffer);
		copyLocations(triangle, dataBuffer);
		triangle.drawTriangle(cameraFrustum);
	}
	
	/**
	 * Draws a triangle using gouraud shading and perspective correct texture mapping.
	 * This method requires cameraFrustum to be already {@link #screenportFrustum ported}.
	 * 
	 * @param triangle
	 * @param dataBuffer
	 * @param texture
	 * @param cameraFrustum
	 */
	public void drawPerspectiveGouraudTriangle(PerspectiveGouraudTriangle triangle, GeometryDataBuffer dataBuffer, Texture texture, int[] cameraFrustum) {
		if (clip(dataBuffer, cameraFrustum))
			return;
		copyUVs(triangle, dataBuffer, texture);
		copyColors(triangle, dataBuffer);
		copyLocations(triangle, dataBuffer);
		triangle.drawTriangle(cameraFrustum);
	}
	
	private boolean clip(GeometryDataBuffer dataBuffer, int[] cameraFrustum) {
		int[] location1 = dataBuffer.getVertexDataBuffer(0).getLocation();
		int[] location2 = dataBuffer.getVertexDataBuffer(1).getLocation();
		int[] location3 = dataBuffer.getVertexDataBuffer(2).getLocation();
		int triangleSize = shoelace(location1, location2, location3);
		if (triangleSize > 0) // backface culling
			return true;
		int left = cameraFrustum[Camera.FRUSTUM_LEFT] + 1;
		int right = cameraFrustum[Camera.FRUSTUM_RIGHT] - 1;
		int top = cameraFrustum[Camera.FRUSTUM_TOP] + 1;
		int bottom = cameraFrustum[Camera.FRUSTUM_BOTTOM] - 1;
		int near = cameraFrustum[Camera.FRUSTUM_NEAR] / 10;
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
					| (!insideWidth1 & !insideWidth2 & !insideWidth3)) {
					return true;
		}
		return false;
	}
	
	private int shoelace(int[] vector1, int[] vector2, int[] vector3) {
		return (vector2[VECTOR_X] - vector1[VECTOR_X]) * (vector3[VECTOR_Y] - vector1[VECTOR_Y])
				- (vector3[VECTOR_X] - vector1[VECTOR_X]) * (vector2[VECTOR_Y] - vector1[VECTOR_Y]);
	}
	
	private void copyUVs(AffineFlatTriangle triangle, GeometryDataBuffer dataBuffer, Texture texture) {
		if (texture != null) {
			int width = texture.getWidth() - 1;
			int height = texture.getHeight() - 1;
			int[] uv0 = dataBuffer.getUV(0);
			int[] uv1 = dataBuffer.getUV(1);
			int[] uv2 = dataBuffer.getUV(2);
			triangle.getU()[0] = mathLibrary.multiply(uv0[VECTOR_X], width);
			triangle.getU()[1] = mathLibrary.multiply(uv1[VECTOR_X], width);
			triangle.getU()[2] = mathLibrary.multiply(uv2[VECTOR_X], width);
			triangle.getV()[0] = mathLibrary.multiply(uv0[VECTOR_Y], height);
			triangle.getV()[1] = mathLibrary.multiply(uv1[VECTOR_Y], height);
			triangle.getV()[2] = mathLibrary.multiply(uv2[VECTOR_Y], height);
		}
	}
	
	private void copyUVs(AffineGouraudTriangle triangle, GeometryDataBuffer dataBuffer, Texture texture) {
		if (texture != null) {
			int width = texture.getWidth() - 1;
			int height = texture.getHeight() - 1;
			int[] uv0 = dataBuffer.getUV(0);
			int[] uv1 = dataBuffer.getUV(1);
			int[] uv2 = dataBuffer.getUV(2);
			triangle.getU()[0] = mathLibrary.multiply(uv0[VECTOR_X], width);
			triangle.getU()[1] = mathLibrary.multiply(uv1[VECTOR_X], width);
			triangle.getU()[2] = mathLibrary.multiply(uv2[VECTOR_X], width);
			triangle.getV()[0] = mathLibrary.multiply(uv0[VECTOR_Y], height);
			triangle.getV()[1] = mathLibrary.multiply(uv1[VECTOR_Y], height);
			triangle.getV()[2] = mathLibrary.multiply(uv2[VECTOR_Y], height);
		}
	}
	
	private void copyColors(GouraudTriangle triangle, GeometryDataBuffer dataBuffer) {
		int lightColor0 = dataBuffer.getVertexDataBuffer(0).getLightColor();
		int lightColor1 = dataBuffer.getVertexDataBuffer(1).getLightColor();
		int lightColor2 = dataBuffer.getVertexDataBuffer(2).getLightColor();
		triangle.getRed()[0] = colorLibrary.getRed(lightColor0);
		triangle.getGreen()[0] = colorLibrary.getGreen(lightColor0);
		triangle.getBlue()[0] = colorLibrary.getBlue(lightColor0);
		triangle.getRed()[1] = colorLibrary.getRed(lightColor1);
		triangle.getGreen()[1] = colorLibrary.getGreen(lightColor1);
		triangle.getBlue()[1] = colorLibrary.getBlue(lightColor1);
		triangle.getRed()[2] = colorLibrary.getRed(lightColor2);
		triangle.getGreen()[2] = colorLibrary.getGreen(lightColor2);
		triangle.getBlue()[2] = colorLibrary.getBlue(lightColor2);
	}
	
	private void copyLocations(FlatTriangle triangle, GeometryDataBuffer dataBuffer) {
		vectorLibrary.copy(triangle.getLocation1(), dataBuffer.getVertexDataBuffer(0).getLocation());
		vectorLibrary.copy(triangle.getLocation2(), dataBuffer.getVertexDataBuffer(1).getLocation());
		vectorLibrary.copy(triangle.getLocation3(), dataBuffer.getVertexDataBuffer(2).getLocation());
	}
}
