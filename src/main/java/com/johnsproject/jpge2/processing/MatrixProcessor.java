package com.johnsproject.jpge2.processing;

public class MatrixProcessor {

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
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 4; j++) {
				out[i][j] = matrix1[i][j] + matrix2[i][j];
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
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 4; j++) {
				long result = (long)matrix1[0][j] * (long)matrix2[i][0] +
						(long)matrix1[1][j] * (long)matrix2[i][1] +
						(long)matrix1[2][j] * (long)matrix2[i][2] +
						(long)matrix1[3][j] * (long)matrix2[i][3];
				out[i][j] = (int)(result >> MathProcessor.FP_SHIFT);
			}
		}
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
				result += matrix[i][j] + ",";
			}
			result += "|\n";
		}
		return result;
	}	
	
}
