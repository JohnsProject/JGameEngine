package com.johnsproject.jgameengine.model;


import static com.johnsproject.jgameengine.util.FixedPointUtils.FP_ONE;
import static com.johnsproject.jgameengine.util.VectorUtils.VECTOR_X;
import static com.johnsproject.jgameengine.util.VectorUtils.VECTOR_Y;
import static com.johnsproject.jgameengine.util.VectorUtils.VECTOR_Z;

import com.johnsproject.jgameengine.util.FixedPointUtils;
import com.johnsproject.jgameengine.util.MatrixUtils;
import com.johnsproject.jgameengine.util.TransformationUtils;
import com.johnsproject.jgameengine.util.VectorUtils;

public class Transform {
	
	private final int[] location;
	private final int[] rotation;
	private final int[] scale;
	
	private final int[][] matrixCache1;
	private final int[][] matrixCache2;
	private final int[][] spaceEnterMatrix;
	private final int[][] spaceEnterNormalMatrix;
	private final int[][] spaceExitMatrix;
	private final int[][] spaceExitNormalMatrix;
	
	public Transform() {
		this(VectorUtils.emptyVector(), VectorUtils.emptyVector(), VectorUtils.VECTOR_ONE.clone());
	}
	
	public Transform(int[] location, int[] rotation, int[] scale) {
		this.location = location;
		this.rotation = rotation;
		this.scale = scale;
		this.matrixCache1 = MatrixUtils.indentityMatrix();
		this.matrixCache2 = MatrixUtils.indentityMatrix();
		this.spaceEnterMatrix = MatrixUtils.indentityMatrix();
		this.spaceEnterNormalMatrix = MatrixUtils.indentityMatrix();
		this.spaceExitMatrix = MatrixUtils.indentityMatrix();
		this.spaceExitNormalMatrix = MatrixUtils.indentityMatrix();
		recalculateMatrices();
	}
	
	private void recalculateMatrices() {
		spaceExitMatrix();
		spaceExitNormalMatrix();
		spaceEnterMatrix();
		spaceEnterNormalMatrix();
	}
	
	private void spaceExitMatrix() {
		MatrixUtils.copy(spaceExitMatrix, MatrixUtils.MATRIX_IDENTITY);
		scale(spaceExitMatrix);
		rotateX(spaceExitMatrix, rotation[VECTOR_X]);
		rotateY(spaceExitMatrix, rotation[VECTOR_Y]);
		rotateZ(spaceExitMatrix, rotation[VECTOR_Z]);
		translate(spaceExitMatrix);
	}

	private void spaceExitNormalMatrix() {
		MatrixUtils.copy(spaceExitNormalMatrix, MatrixUtils.MATRIX_IDENTITY);
		scale(spaceExitNormalMatrix);
		rotateX(spaceExitNormalMatrix, rotation[VECTOR_X]);
		rotateY(spaceExitNormalMatrix, rotation[VECTOR_Y]);
		rotateZ(spaceExitNormalMatrix, rotation[VECTOR_Z]);
		if ((scale[VECTOR_X] != scale[VECTOR_Y]) || (scale[VECTOR_Y] != scale[VECTOR_Z])) {
			MatrixUtils.inverse(spaceExitNormalMatrix, matrixCache2);
			MatrixUtils.transpose(matrixCache2, spaceExitNormalMatrix);
		}
	}
	
	private void spaceEnterMatrix() {
		int scaleX = FixedPointUtils.divide(FP_ONE, scale[VECTOR_X] == 0 ? 1 : scale[VECTOR_X]);
		int scaleY = FixedPointUtils.divide(FP_ONE, scale[VECTOR_Y] == 0 ? 1 : scale[VECTOR_Y]);
		int scaleZ = FixedPointUtils.divide(FP_ONE, scale[VECTOR_Z] == 0 ? 1 : scale[VECTOR_Z]);
		VectorUtils.invert(location);
		VectorUtils.invert(rotation);
		MatrixUtils.copy(spaceEnterMatrix, MatrixUtils.MATRIX_IDENTITY);
		translate(spaceEnterMatrix);
		rotateZ(spaceEnterMatrix, rotation[VECTOR_Z]);
		rotateY(spaceEnterMatrix, rotation[VECTOR_Y]);
		rotateX(spaceEnterMatrix, rotation[VECTOR_X]);
		scale(spaceEnterMatrix, scaleX, scaleY, scaleZ);
		VectorUtils.invert(location);
		VectorUtils.invert(rotation);
	}
	
	private void spaceEnterNormalMatrix() {
		int scaleX = FixedPointUtils.divide(FP_ONE, scale[VECTOR_X] == 0 ? 1 : scale[VECTOR_X]);
		int scaleY = FixedPointUtils.divide(FP_ONE, scale[VECTOR_Y] == 0 ? 1 : scale[VECTOR_Y]);
		int scaleZ = FixedPointUtils.divide(FP_ONE, scale[VECTOR_Z] == 0 ? 1 : scale[VECTOR_Z]);
		VectorUtils.invert(rotation);
		MatrixUtils.copy(spaceEnterNormalMatrix, MatrixUtils.MATRIX_IDENTITY);
		rotateZ(spaceEnterNormalMatrix, rotation[VECTOR_Z]);
		rotateY(spaceEnterNormalMatrix, rotation[VECTOR_Y]);
		rotateX(spaceEnterNormalMatrix, rotation[VECTOR_X]);
		scale(spaceEnterNormalMatrix, scaleX, scaleY, scaleZ);
		VectorUtils.invert(rotation);
		if ((scale[VECTOR_X] != scale[VECTOR_Y]) || (scale[VECTOR_Y] != scale[VECTOR_Z])) {
			MatrixUtils.inverse(spaceEnterNormalMatrix, matrixCache2);
			MatrixUtils.transpose(matrixCache2, spaceEnterNormalMatrix);
		}
	}
	
	private void translate(int[][] matrix) {
		TransformationUtils.translationMatrix(matrixCache1, location);
		MatrixUtils.copy(matrixCache2, matrix);
		MatrixUtils.multiply(matrixCache1, matrixCache2, matrix);
	}
	
	private void scale(int[][] matrix) {
		scale(matrix, scale[VECTOR_X], scale[VECTOR_Y], scale[VECTOR_Z]);
	}
	
	private void scale(int[][] matrix, int x, int y, int z) {
		TransformationUtils.scaleMatrix(matrixCache1, x, y, z);
		MatrixUtils.copy(matrixCache2, matrix);
		MatrixUtils.multiply(matrixCache1, matrixCache2, matrix);
	}
	
	private void rotateX(int[][] matrix, int angle) {
		TransformationUtils.xRotationMatrix(matrixCache1, angle);
		MatrixUtils.copy(matrixCache2, matrix);
		MatrixUtils.multiply(matrixCache1, matrixCache2, matrix);
	}
	
	private void rotateY(int[][] matrix, int angle) {
		TransformationUtils.yRotationMatrix(matrixCache1, angle);
		MatrixUtils.copy(matrixCache2, matrix);
		MatrixUtils.multiply(matrixCache1, matrixCache2, matrix);
	}
	
	private void rotateZ(int[][] matrix, int angle) {
		TransformationUtils.zRotationMatrix(matrixCache1, angle);
		MatrixUtils.copy(matrixCache2, matrix);
		MatrixUtils.multiply(matrixCache1, matrixCache2, matrix);
	}
	
	public void setLocation(int x, int y, int z) {
		location[VECTOR_X] = x;
		location[VECTOR_Y] = y;
		location[VECTOR_Z] = z;
		recalculateMatrices();
	}

	public void setRotation(int x, int y, int z) {
		rotation[VECTOR_X] = x;
		rotation[VECTOR_Y] = y;
		rotation[VECTOR_Z] = z;
		recalculateMatrices();
	}
	
	public void setScale(int x, int y, int z) {
		scale[VECTOR_X] = x;
		scale[VECTOR_Y] = y;
		scale[VECTOR_Z] = z;
		recalculateMatrices();
	}
	
	public void worldTranslate(int x, int y, int z) {
		setLocation(location[VECTOR_X] + x, location[VECTOR_Y] + y, location[VECTOR_Z] + z);
	}
	
	public void worldTranslate(int[] vector) {
		worldTranslate(vector[VECTOR_X], vector[VECTOR_Y], vector[VECTOR_Z]);
	}
	
	public void localTranslate(int x, int y, int z) {
		worldTranslate(localToWorld(x, y, z));
	}
	
	public void localTranslate(int[] vector) {
		localTranslate(vector[VECTOR_X], vector[VECTOR_Y], vector[VECTOR_Z]);
	}

	public void worldRotate(int x, int y, int z) {
		setRotation(rotation[VECTOR_X] + x, rotation[VECTOR_Y] + y, rotation[VECTOR_Z] + z);
	}

	public void worldRotate(int[] angles) {
		worldRotate(angles[VECTOR_X], angles[VECTOR_Y], angles[VECTOR_Z]);
	}
	
	public void localRotate(int x, int y, int z) {
		worldRotate(localToWorld(x, y, z));
	}
	
	public void localRotate(int[] angles) {
		localRotate(angles[VECTOR_X], angles[VECTOR_Y], angles[VECTOR_Z]);
	}
	
	public void worldScale(int x, int y, int z) {
		setScale(scale[VECTOR_X] + x, scale[VECTOR_Y] + y, scale[VECTOR_Z] + z);
	}
	
	public void worldScale(int[] vector) {
		worldScale(vector[VECTOR_X], vector[VECTOR_Y], vector[VECTOR_Z]);
	}
	
	public void localScale(int x, int y, int z) {
		worldScale(localToWorld(x, y, z));
	}
	
	public void localScale(int[] vector) {
		localScale(vector[VECTOR_X], vector[VECTOR_Y], vector[VECTOR_Z]);
	}
	
	private int[] localToWorld(int x, int y, int z) {
		final int[] vector = matrixCache1[0];
		VectorUtils.copy(vector, VectorUtils.VECTOR_ZERO);
		vector[VECTOR_X] = x;
		vector[VECTOR_Y] = y;
		vector[VECTOR_Z] = z;
		TransformationUtils.rotateX(vector, rotation[VectorUtils.VECTOR_X]);
		TransformationUtils.rotateY(vector, rotation[VectorUtils.VECTOR_Y]);
		TransformationUtils.rotateZ(vector, rotation[VectorUtils.VECTOR_Z]);
		return vector;
	}

	public int[] getLocation() {
		return location;
	}

	public int[] getRotation() {
		return rotation;
	}

	public int[] getScale() {
		return scale;
	}

	public int[][] getSpaceEnterMatrix() {
		return spaceEnterMatrix;
	}

	public int[][] getSpaceEnterNormalMatrix() {
		return spaceEnterNormalMatrix;
	}

	public int[][] getSpaceExitMatrix() {
		return spaceExitMatrix;
	}

	public int[][] getSpaceExitNormalMatrix() {
		return spaceExitNormalMatrix;
	}
}