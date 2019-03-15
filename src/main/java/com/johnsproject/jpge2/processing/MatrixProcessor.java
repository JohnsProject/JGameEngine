package com.johnsproject.jpge2.processing;

public class MatrixProcessor {

	public static final int[][] IDENTITY = MatrixProcessor.generate();
	
	private static final int[][] matrixCache1 = MatrixProcessor.generate();
	private static final int[][] matrixCache2 = MatrixProcessor.generate();
	private static final int[][] transformMatrix = MatrixProcessor.generate();
	
	/**
	 * Returns an identity matrix.
	 * 
	 * @return
	 */
	public static int[][] generate() {
		return new int[][] {
			{MathProcessor.FP_VALUE, 0, 0, 0},
			{0, MathProcessor.FP_VALUE, 0, 0},
			{0, 0, MathProcessor.FP_VALUE, 0},
			{0, 0, 0, MathProcessor.FP_VALUE}
		};
	}
	
	/**
	 * Sets out equals the result of the addition of matrix1 and matrix2.
	 * 
	 * @param matrix1
	 * @param matrix2
	 * @param out
	 */
	public static void add(int[][] matrix1, int[][] matrix2, int[][] out) {
		// ensures that will return right values if matrix or matrix two is the same as out
		copy(matrixCache1, matrix1);
		copy(matrixCache2, matrix2);
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 4; j++) {
				out[i][j] = matrixCache1[i][j] + matrixCache2[i][j];
			}
		}
	}
	
	/**
	 * Sets out equals the result of the multiplication of matrix1 and matrix2.
	 * 
	 * @param matrix1
	 * @param matrix2
	 * @param out
	 */
	public static void multiply(int[][] matrix1, int[][] matrix2, int[][] out) {
		// ensures that will return right values if matrix or matrix two is the same as out
		copy(matrixCache1, matrix1);
		copy(matrixCache2, matrix2);
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 4; j++) {
				int result = MathProcessor.multiply(matrixCache1[0][j], matrixCache2[i][0]);
				result += MathProcessor.multiply(matrixCache1[1][j], matrixCache2[i][1]);
				result += MathProcessor.multiply(matrixCache1[2][j], matrixCache2[i][2]);
				result += MathProcessor.multiply(matrixCache1[3][j], matrixCache2[i][3]);
				out[i][j] = result;
			}
		}
	}
	
	
	/**
	 * Translates the matrix to the given position.
	 * 
	 * @param matrix
	 * @param x
	 * @param y
	 * @param z
	 */
	public static void translate(int[][] matrix, int x, int y, int z) {
		reset(transformMatrix);
		//not so big translation values needed
		transformMatrix[3][0] = x << MathProcessor.FP_SHIFT;
		transformMatrix[3][1] = y << MathProcessor.FP_SHIFT;
		transformMatrix[3][2] = z << MathProcessor.FP_SHIFT;
		multiply(transformMatrix, matrix, matrix);
	}

	/**
	 * Scales the matrix by the given scale.
	 * 
	 * @param matrix
	 * @param x
	 * @param y
	 * @param z
	 */
	public static void scale(int[][] matrix, int x, int y, int z) {
		reset(transformMatrix);
		transformMatrix[0][0] = x << MathProcessor.FP_SHIFT;
		transformMatrix[1][1] = y << MathProcessor.FP_SHIFT;
		transformMatrix[2][2] = z << MathProcessor.FP_SHIFT;
		multiply(transformMatrix, matrix, matrix);
	}

	/**
	 * Rotates the matrix around the x axis by the given angle.
	 * 
	 * @param matrix
	 * @param angle
	 */
	public static void rotateX(int[][] matrix, int angle) {
		reset(transformMatrix);
		int cos = MathProcessor.cos(angle);
		int sin = MathProcessor.sin(angle);
		transformMatrix[1][1] = cos;
		transformMatrix[1][2] = sin;
		transformMatrix[2][1] = -sin;
		transformMatrix[2][2] = cos;
		multiply(transformMatrix, matrix, matrix);
	}

	/**
	 * Rotates the matrix around the y axis by the given angle.
	 * 
	 * @param matrix
	 * @param angle
	 */
	public static void rotateY(int[][] matrix, int angle) {
		reset(transformMatrix);
		int cos = MathProcessor.cos(angle);
		int sin = MathProcessor.sin(angle);
		transformMatrix[0][0] = cos;
		transformMatrix[0][2] = -sin;
		transformMatrix[2][0] = sin;
		transformMatrix[2][2] = cos;
		multiply(transformMatrix, matrix, matrix);
	}

	/**
	 * Rotates the matrix around the z axis by the given angle.
	 * 
	 * @param matrix
	 * @param angle
	 */
	public static void rotateZ(int[][] matrix, int angle) {
		reset(transformMatrix);
		int cos = MathProcessor.cos(angle);
		int sin = MathProcessor.sin(angle);
		transformMatrix[0][0] = cos;
		transformMatrix[0][1] = sin;
		transformMatrix[1][0] = -sin;
		transformMatrix[1][1] = cos;
		multiply(transformMatrix, matrix, matrix);
	}
	
	/**
	 * Resets the given matrix to its identity.
	 * 
	 * @param matrix
	 */
	public static void reset(int[][] matrix) {
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 4; j++) {
				matrix[i][j] = 0;
			}
		}
		matrix[0][0] = MathProcessor.FP_VALUE;
		matrix[1][1] = MathProcessor.FP_VALUE;
		matrix[2][2] = MathProcessor.FP_VALUE;
		matrix[3][3] = MathProcessor.FP_VALUE;
	}
	
	/**
	 * Copies the value of matrix to the target.
	 * 
	 * @param target
	 * @param matrix
	 */
	public static void copy(int[][] target, int[][] matrix) {
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 4; j++) {
				target[i][j] = matrix[i][j];
			}
		}
	}	
	
	/**
	 * Returns if matrix1 is equals to matrix2.
	 * 
	 * @param matrix1
	 * @param matrix2
	 * @return
	 */
	public static boolean equals(int[][] matrix1, int[][] matrix2) {
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 4; j++) {
				if (matrix1[i][j] != matrix2[i][j]) return false;
			}
		}
		return true;
	}	
	
	/**
	 * Returns a string containing the data of the given matrix.
	 * 
	 * @param matrix
	 * @return
	 */
	public static String toString(int[][] matrix) {
		String result = "";
		for (int i = 0; i < 4; i++) {
			result += '|';
			for (int j = 0; j < 4; j++) {
				result += matrix[j][i] + ",";
			}
			result += "|\n";
		}
		return result;
	}	
	
}
