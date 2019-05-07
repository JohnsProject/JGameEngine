package com.johnsproject.jpge2.primitive;

import java.util.Arrays;

import com.johnsproject.jpge2.processor.MathProcessor;

public class Matrix {
	private static final int FP_ONE = MathProcessor.FP_ONE;

	public static final byte MATRIX_SIZE = 4;
	public static final Matrix MATRIX_IDENTITY = new Matrix();
	
	private static final Matrix CACHE1 = new Matrix();
	private static final Matrix CACHE2 = new Matrix();
	
	private final int[][] values;
	
	public Matrix() {
		values = new int[][] {
			{FP_ONE, 0, 0, 0},
			{0, FP_ONE, 0, 0},
			{0, 0, FP_ONE, 0},
			{0, 0, 0, FP_ONE}
		};
	}

	public int[][] getValues() {
		return values;
	}
	
	public void add(Matrix matrix) {
		copy(CACHE1);
		int[][] cacheValues = CACHE1.getValues();
		int[][] matrixValues = matrix.getValues();
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 4; j++) {
				long result = (long)cacheValues[0][j] + matrixValues[i][0];
				result += (long)cacheValues[1][j] + matrixValues[i][1];
				result += (long)cacheValues[2][j] + matrixValues[i][2];
				result += (long)cacheValues[3][j] + matrixValues[i][3];
				values[i][j] = (int)MathProcessor.multiply(result, 1);
			}
		}
	}
	
	public void subtract(Matrix matrix) {
		copy(CACHE1);
		int[][] cacheValues = CACHE1.getValues();
		int[][] matrixValues = matrix.getValues();
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 4; j++) {
				long result = (long)cacheValues[0][j] - matrixValues[i][0];
				result += (long)cacheValues[1][j] - matrixValues[i][1];
				result += (long)cacheValues[2][j] - matrixValues[i][2];
				result += (long)cacheValues[3][j] - matrixValues[i][3];
				values[i][j] = (int)MathProcessor.multiply(result, 1);
			}
		}
	}
	
	public void multiply(Matrix matrix) {
		copy(CACHE1);
		int[][] cacheValues = CACHE1.getValues();
		int[][] matrixValues = matrix.getValues();
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 4; j++) {
				long result = (long)cacheValues[0][j] * matrixValues[i][0];
				result += (long)cacheValues[1][j] * matrixValues[i][1];
				result += (long)cacheValues[2][j] * matrixValues[i][2];
				result += (long)cacheValues[3][j] * matrixValues[i][3];
				values[i][j] = (int)MathProcessor.multiply(result, 1);
			}
		}
	}
	
	public void divide(Matrix matrix) {
		copy(CACHE1);
		int[][] cacheValues = CACHE1.getValues();
		int[][] matrixValues = matrix.getValues();
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 4; j++) {
				long result = (long)cacheValues[0][j] / matrixValues[i][0];
				result += (long)cacheValues[1][j] / matrixValues[i][1];
				result += (long)cacheValues[2][j] / matrixValues[i][2];
				result += (long)cacheValues[3][j] / matrixValues[i][3];
				values[i][j] = (int)MathProcessor.multiply(result, 1);
			}
		}
	}
	
	public void translate(Vector vector) {
		MATRIX_IDENTITY.copy(CACHE2);
		int[][] cacheValues = CACHE2.getValues();
		cacheValues[3][0] = vector.getX();
		cacheValues[3][1] = vector.getY();
		cacheValues[3][2] = vector.getZ();
		multiply(CACHE2);
	}
	
	public void scale(Vector vector) {
		MATRIX_IDENTITY.copy(CACHE2);
		int[][] cacheValues = CACHE2.getValues();
		cacheValues[0][0] = vector.getX();
		cacheValues[1][1] = vector.getY();
		cacheValues[2][2] = vector.getZ();
		multiply(CACHE2);
	}
	
	public void rotateX(int angle) {
		MATRIX_IDENTITY.copy(CACHE2);
		int[][] cacheValues = CACHE2.getValues();
		int cos = MathProcessor.cos(angle);
		int sin = MathProcessor.sin(angle);
		cacheValues[1][1] = cos;
		cacheValues[1][2] = sin;
		cacheValues[2][1] = -sin;
		cacheValues[2][2] = cos;
		multiply(CACHE2);
	}
	
	public void rotateY(int angle) {
		MATRIX_IDENTITY.copy(CACHE2);
		int[][] cacheValues = CACHE2.getValues();
		int cos = MathProcessor.cos(angle);
		int sin = MathProcessor.sin(angle);
		cacheValues[0][0] = cos;
		cacheValues[0][2] = -sin;
		cacheValues[2][0] = sin;
		cacheValues[2][2] = cos;
		multiply(CACHE2);
	}
	
	public void rotateZ(int angle) {
		MATRIX_IDENTITY.copy(CACHE2);
		int[][] cacheValues = CACHE2.getValues();
		int cos = MathProcessor.cos(angle);
		int sin = MathProcessor.sin(angle);
		cacheValues[0][0] = cos;
		cacheValues[0][1] = sin;
		cacheValues[1][0] = -sin;
		cacheValues[1][1] = cos;
		multiply(CACHE2);
	}
	
	public void rotateXYZ(Vector angles) {
		rotateX(angles.getX());
		rotateY(angles.getY());
		rotateZ(angles.getZ());
	}
	
	public void rotateZYX(Vector angles) {
		rotateZ(angles.getZ());
		rotateY(angles.getY());
		rotateX(angles.getX());
	}
	
	public void copy(Matrix target) {
		int[][] targetValues = target.getValues();
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 4; j++) {
				targetValues[i][j] = values[i][j];
			}
		}
	}

	@Override
	public String toString() {
		return "FPMatrix|" + values[0][0] + ", "  + values[1][0] + ", "  + values[2][0] + ", "  + values[3][0] + "|\n"
			+ "        |" + values[0][1] + ", "  + values[1][1] + ", "  + values[2][1] + ", "  + values[3][1] + "|\n"
			+ "        |" + values[0][2] + ", "  + values[1][2] + ", "  + values[2][2] + ", "  + values[3][2] + "|\n"
			+ "        |" + values[0][3] + ", "  + values[1][3] + ", "  + values[2][3] + ", "  + values[3][3] + "|";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.deepHashCode(values);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Matrix other = (Matrix) obj;
		if (!Arrays.deepEquals(values, other.values))
			return false;
		return true;
	}
	
	@Override
	public Matrix clone() {
		Matrix result = new Matrix();
		this.copy(result);
		return result;
	}
}
