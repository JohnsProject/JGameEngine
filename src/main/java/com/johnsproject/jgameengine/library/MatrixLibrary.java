/**
 * MIT License
 *
 * Copyright (c) 2018 John Salomon - John´s Project
 *  
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software withresult restriction, including withresult limitation the rights
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

/**
 * The MatrixLibrary class contains methods for generating matrices and performing matrix 
 * operations such as multiply, translate, scale, rotate.
 * 
 * @author John Ferraz Salomon
 */
public class MatrixLibrary {

	private static final int FP_ONE = MathLibrary.FP_ONE;
	
	private static final byte VECTOR_X = VectorLibrary.VECTOR_X;
	private static final byte VECTOR_Y = VectorLibrary.VECTOR_Y;
	private static final byte VECTOR_Z = VectorLibrary.VECTOR_Z;

	public static final int[][] MATRIX_IDENTITY = new int[][] {
		{ FP_ONE, 0, 0, 0 },
		{ 0, FP_ONE, 0, 0 },
		{ 0, 0, FP_ONE, 0 },
		{ 0, 0, 0, FP_ONE }
	};

	private final int[][] matrixCache1 = generate();
	private final int[][] matrixCache2 = generate();

	private final MathLibrary mathLibrary;

	public MatrixLibrary() {
		this.mathLibrary = new MathLibrary();
	}

	/**
	 * Returns an identity matrix.
	 * 
	 * @return
	 */
	public int[][] generate() {
		return new int[][] {
			{ FP_ONE, 0, 0, 0 },
			{ 0, FP_ONE, 0, 0 },
			{ 0, 0, FP_ONE, 0 },
			{ 0, 0, 0, FP_ONE }
		};
	}
	
	/**
	 * Sets result equals the result of the addition of matrix1 and matrix2.
	 * 
	 * @param matrix1
	 * @param matrix2
	 * @param result
	 */
	public int[][] add(int[][] matrix1, int[][] matrix2, int[][] result) {
		// ensures that will return right values if matrix or matrix two is the same as result
		copy(matrixCache1, matrix1);
		copy(matrixCache2, matrix2);
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 4; j++) {
				int res = matrixCache1[0][j] + matrixCache2[i][0];
				res += matrixCache1[1][j] + matrixCache2[i][1];
				res += matrixCache1[2][j] + matrixCache2[i][2];
				res += matrixCache1[3][j] + matrixCache2[i][3];
				result[i][j] = res;
			}
		}
		return result;
	}

	/**
	 * Sets result equals the result of the subtraction of matrix1 and matrix2.
	 * 
	 * @param matrix1
	 * @param matrix2
	 * @param result
	 */
	public int[][] subtract(int[][] matrix1, int[][] matrix2, int[][] result) {
		// ensures that will return right values if matrix or matrix two is the same as result
		copy(matrixCache1, matrix1);
		copy(matrixCache2, matrix2);
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 4; j++) {
				int res = matrixCache1[0][j] - matrixCache2[i][0];
				res += matrixCache1[1][j] - matrixCache2[i][1];
				res += matrixCache1[2][j] - matrixCache2[i][2];
				res += matrixCache1[3][j] - matrixCache2[i][3];
				result[i][j] = res;
			}
		}
		return result;
	}

	/**
	 * Sets result equals the result of the multiplication of matrix1 and matrix2.
	 * 
	 * @param matrix1
	 * @param matrix2
	 * @param result
	 */
	public int[][] multiply(int[][] matrix1, int[][] matrix2, int[][] result) {
		// ensures that will return right values if matrix or matrix two is the same as result
		copy(matrixCache1, matrix1);
		copy(matrixCache2, matrix2);
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 4; j++) {
				int res = mathLibrary.multiply(matrixCache1[0][j], matrixCache2[i][0]);
				res += mathLibrary.multiply(matrixCache1[1][j], matrixCache2[i][1]);
				res += mathLibrary.multiply(matrixCache1[2][j], matrixCache2[i][2]);
				res += mathLibrary.multiply(matrixCache1[3][j], matrixCache2[i][3]);
				result[i][j] = res;
			}
		}
		return result;
	}
	
	/**
	 * Sets result equals the result of the division of matrix1 and matrix2.
	 * 
	 * @param matrix1
	 * @param matrix2
	 * @param result
	 */
	public int[][] divide(int[][] matrix1, int[][] matrix2, int[][] result) {
		// ensures that will return right values if matrix or matrix two is the same as result
		copy(matrixCache1, matrix1);
		copy(matrixCache2, matrix2);
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 4; j++) {
				int res = mathLibrary.divide(matrixCache1[0][j], matrixCache2[i][0]);
				res += mathLibrary.divide(matrixCache1[1][j], matrixCache2[i][1]);
				res += mathLibrary.divide(matrixCache1[2][j], matrixCache2[i][2]);
				res += mathLibrary.divide(matrixCache1[3][j], matrixCache2[i][3]);
				result[i][j] = res;
			}
		}
		return result;
	}
	
	/**
	 * Sets result equals the result of the addition of matrix by value.
	 * 
	 * @param matrix
	 * @param value
	 * @param result
	 */
	public int[][] add(int[][] matrix, int value, int[][] result) {
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 4; j++) {
				result[i][j] = matrix[i][j] + value;
			}
		}
		return result;
	}
	
	/**
	 * Sets result equals the result of the subtraction of matrix by value.
	 * 
	 * @param matrix
	 * @param value
	 * @param result
	 */
	public int[][] subtract(int[][] matrix, int value, int[][] result) {
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 4; j++) {
				result[i][j] = matrix[i][j] - value;
			}
		}
		return result;
	}
	
	/**
	 * Sets result equals the result of the multiplication of matrix by value.
	 * 
	 * @param matrix
	 * @param value
	 * @param result
	 */
	public int[][] multiply(int[][] matrix, int value, int[][] result) {
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 4; j++) {
				result[i][j] = mathLibrary.multiply(matrix[i][j], value);
			}
		}
		return result;
	}
	
	/**
	 * Sets result equals the result of the division of matrix by value.
	 * 
	 * @param matrix
	 * @param value
	 * @param result
	 */
	public int[][] divide(int[][] matrix, int value, int[][] result) {
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 4; j++) {
				result[i][j] = mathLibrary.divide(matrix[i][j], value);
			}
		}
		return result;
	}

	/**
	 * Sets result equals the transposed matrix.
	 * 
	 * @param matrix
	 * @param result
	 */
	public int[][] transpose(int[][] matrix, int[][] result) {
		// ensures that will return right values if matrix or matrix two is the same as result
		copy(matrixCache1, matrix);
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 4; j++) {
				result[i][j] = matrixCache1[j][i];
			}
		}
		return result;
	}
	
	/**
	 * Returns the determinant of the given matrix.
	 * 
	 * @param matrix
	 */
	public int determinant(int[][] matrix) {
		return	mathLibrary.multiply(mathLibrary.multiply(matrix[3][0], matrix[2][1]), mathLibrary.multiply(matrix[1][2], matrix[0][3])) - 
				mathLibrary.multiply(mathLibrary.multiply(matrix[2][0], matrix[3][1]), mathLibrary.multiply(matrix[1][2], matrix[0][3])) -
				mathLibrary.multiply(mathLibrary.multiply(matrix[3][0], matrix[1][1]), mathLibrary.multiply(matrix[2][2], matrix[0][3])) + 
				mathLibrary.multiply(mathLibrary.multiply(matrix[1][0], matrix[3][1]), mathLibrary.multiply(matrix[2][2], matrix[0][3])) +
				mathLibrary.multiply(mathLibrary.multiply(matrix[2][0], matrix[1][1]), mathLibrary.multiply(matrix[3][2], matrix[0][3])) - 
				mathLibrary.multiply(mathLibrary.multiply(matrix[1][0], matrix[2][1]), mathLibrary.multiply(matrix[3][2], matrix[0][3])) -
				mathLibrary.multiply(mathLibrary.multiply(matrix[3][0], matrix[2][1]), mathLibrary.multiply(matrix[0][2], matrix[1][3])) + 
				mathLibrary.multiply(mathLibrary.multiply(matrix[2][0], matrix[3][1]), mathLibrary.multiply(matrix[0][2], matrix[1][3])) +
				mathLibrary.multiply(mathLibrary.multiply(matrix[3][0], matrix[0][1]), mathLibrary.multiply(matrix[2][2], matrix[1][3])) - 
				mathLibrary.multiply(mathLibrary.multiply(matrix[0][0], matrix[3][1]), mathLibrary.multiply(matrix[2][2], matrix[1][3])) -
				mathLibrary.multiply(mathLibrary.multiply(matrix[2][0], matrix[0][1]), mathLibrary.multiply(matrix[3][2], matrix[1][3])) + 
				mathLibrary.multiply(mathLibrary.multiply(matrix[0][0], matrix[2][1]), mathLibrary.multiply(matrix[3][2], matrix[1][3])) +
				mathLibrary.multiply(mathLibrary.multiply(matrix[3][0], matrix[1][1]), mathLibrary.multiply(matrix[0][2], matrix[2][3])) - 
				mathLibrary.multiply(mathLibrary.multiply(matrix[1][0], matrix[3][1]), mathLibrary.multiply(matrix[0][2], matrix[2][3])) -
				mathLibrary.multiply(mathLibrary.multiply(matrix[3][0], matrix[0][1]), mathLibrary.multiply(matrix[1][2], matrix[2][3])) + 
				mathLibrary.multiply(mathLibrary.multiply(matrix[0][0], matrix[3][1]), mathLibrary.multiply(matrix[1][2], matrix[2][3])) +
				mathLibrary.multiply(mathLibrary.multiply(matrix[1][0], matrix[0][1]), mathLibrary.multiply(matrix[3][2], matrix[2][3])) - 
				mathLibrary.multiply(mathLibrary.multiply(matrix[0][0], matrix[1][1]), mathLibrary.multiply(matrix[3][2], matrix[2][3])) -
				mathLibrary.multiply(mathLibrary.multiply(matrix[2][0], matrix[1][1]), mathLibrary.multiply(matrix[0][2], matrix[3][3])) + 
				mathLibrary.multiply(mathLibrary.multiply(matrix[1][0], matrix[2][1]), mathLibrary.multiply(matrix[0][2], matrix[3][3])) +
				mathLibrary.multiply(mathLibrary.multiply(matrix[2][0], matrix[0][1]), mathLibrary.multiply(matrix[1][2], matrix[3][3])) - 
				mathLibrary.multiply(mathLibrary.multiply(matrix[0][0], matrix[2][1]), mathLibrary.multiply(matrix[1][2], matrix[3][3])) -
				mathLibrary.multiply(mathLibrary.multiply(matrix[1][0], matrix[0][1]), mathLibrary.multiply(matrix[2][2], matrix[3][3])) + 
				mathLibrary.multiply(mathLibrary.multiply(matrix[0][0], matrix[1][1]), mathLibrary.multiply(matrix[2][2], matrix[3][3]));
	}
	
	/**
	 * Sets result equals the inverse of matrix.
	 * 
	 * @param matrix
	 * @param result
	 */
	public int[][] inverse(int[][] matrix, int[][] result) {
		// ensures that will return right values if matrix or matrix two is the same as result
		copy(matrixCache1, matrix);
		int determinant = determinant(matrixCache1);
		result[0][0] = mathLibrary.multiply(matrixCache1[2][1], mathLibrary.multiply(matrixCache1[3][2], matrixCache1[1][3])) -
						mathLibrary.multiply(matrixCache1[3][1], mathLibrary.multiply(matrixCache1[2][2], matrixCache1[1][3])) +
						mathLibrary.multiply(matrixCache1[3][1], mathLibrary.multiply(matrixCache1[1][2], matrixCache1[2][3])) -
						mathLibrary.multiply(matrixCache1[1][1], mathLibrary.multiply(matrixCache1[3][2], matrixCache1[2][3])) -
						mathLibrary.multiply(matrixCache1[2][1], mathLibrary.multiply(matrixCache1[1][2], matrixCache1[3][3])) +
						mathLibrary.multiply(matrixCache1[1][1], mathLibrary.multiply(matrixCache1[2][2], matrixCache1[3][3]));
		result[1][0] = mathLibrary.multiply(matrixCache1[3][0], mathLibrary.multiply(matrixCache1[2][2], matrixCache1[1][3])) -
						mathLibrary.multiply(matrixCache1[2][0], mathLibrary.multiply(matrixCache1[2][3], matrixCache1[1][3])) -
						mathLibrary.multiply(matrixCache1[3][0], mathLibrary.multiply(matrixCache1[2][1], matrixCache1[2][3])) +
						mathLibrary.multiply(matrixCache1[1][0], mathLibrary.multiply(matrixCache1[2][3], matrixCache1[2][3])) +
						mathLibrary.multiply(matrixCache1[2][0], mathLibrary.multiply(matrixCache1[2][1], matrixCache1[3][3])) -
						mathLibrary.multiply(matrixCache1[1][0], mathLibrary.multiply(matrixCache1[2][2], matrixCache1[3][3]));
		result[2][0] = mathLibrary.multiply(matrixCache1[2][0], mathLibrary.multiply(matrixCache1[3][1], matrixCache1[1][3])) -
						mathLibrary.multiply(matrixCache1[3][0], mathLibrary.multiply(matrixCache1[2][1], matrixCache1[1][3])) +
						mathLibrary.multiply(matrixCache1[3][0], mathLibrary.multiply(matrixCache1[1][1], matrixCache1[2][3])) -
						mathLibrary.multiply(matrixCache1[1][0], mathLibrary.multiply(matrixCache1[3][1], matrixCache1[2][3])) -
						mathLibrary.multiply(matrixCache1[2][0], mathLibrary.multiply(matrixCache1[1][1], matrixCache1[3][3])) +
						mathLibrary.multiply(matrixCache1[1][0], mathLibrary.multiply(matrixCache1[2][1], matrixCache1[3][3]));
		result[3][0] = mathLibrary.multiply(matrixCache1[3][0], mathLibrary.multiply(matrixCache1[2][1], matrixCache1[1][2])) -
						mathLibrary.multiply(matrixCache1[2][0], mathLibrary.multiply(matrixCache1[3][1], matrixCache1[1][2])) -
						mathLibrary.multiply(matrixCache1[3][0], mathLibrary.multiply(matrixCache1[1][1], matrixCache1[2][2])) +
						mathLibrary.multiply(matrixCache1[1][0], mathLibrary.multiply(matrixCache1[3][1], matrixCache1[2][2])) +
						mathLibrary.multiply(matrixCache1[2][0], mathLibrary.multiply(matrixCache1[1][1], matrixCache1[3][2])) -
						mathLibrary.multiply(matrixCache1[1][0], mathLibrary.multiply(matrixCache1[2][1], matrixCache1[3][2]));
		result[0][1] = mathLibrary.multiply(matrixCache1[3][1], mathLibrary.multiply(matrixCache1[2][2], matrixCache1[0][3])) -
						mathLibrary.multiply(matrixCache1[2][1], mathLibrary.multiply(matrixCache1[3][2], matrixCache1[0][3])) -
						mathLibrary.multiply(matrixCache1[3][1], mathLibrary.multiply(matrixCache1[0][2], matrixCache1[2][3])) +
						mathLibrary.multiply(matrixCache1[0][1], mathLibrary.multiply(matrixCache1[3][2], matrixCache1[2][3])) +
						mathLibrary.multiply(matrixCache1[2][1], mathLibrary.multiply(matrixCache1[0][2], matrixCache1[3][3])) -
						mathLibrary.multiply(matrixCache1[0][1], mathLibrary.multiply(matrixCache1[2][2], matrixCache1[3][3]));
		result[1][1] = mathLibrary.multiply(matrixCache1[2][0], mathLibrary.multiply(matrixCache1[3][2], matrixCache1[0][3])) -
						mathLibrary.multiply(matrixCache1[3][0], mathLibrary.multiply(matrixCache1[2][2], matrixCache1[0][3])) +
						mathLibrary.multiply(matrixCache1[3][0], mathLibrary.multiply(matrixCache1[0][2], matrixCache1[2][3])) -
						mathLibrary.multiply(matrixCache1[0][0], mathLibrary.multiply(matrixCache1[3][2], matrixCache1[2][3])) -
						mathLibrary.multiply(matrixCache1[2][0], mathLibrary.multiply(matrixCache1[0][2], matrixCache1[3][3])) +
						mathLibrary.multiply(matrixCache1[0][0], mathLibrary.multiply(matrixCache1[2][2], matrixCache1[3][3]));
		result[2][1] = mathLibrary.multiply(matrixCache1[3][0], mathLibrary.multiply(matrixCache1[2][1], matrixCache1[0][3])) -
						mathLibrary.multiply(matrixCache1[2][0], mathLibrary.multiply(matrixCache1[3][1], matrixCache1[0][3])) -
						mathLibrary.multiply(matrixCache1[3][0], mathLibrary.multiply(matrixCache1[0][1], matrixCache1[2][3])) +
						mathLibrary.multiply(matrixCache1[0][0], mathLibrary.multiply(matrixCache1[3][1], matrixCache1[2][3])) +
						mathLibrary.multiply(matrixCache1[2][0], mathLibrary.multiply(matrixCache1[0][1], matrixCache1[3][3])) -
						mathLibrary.multiply(matrixCache1[0][0], mathLibrary.multiply(matrixCache1[2][1], matrixCache1[3][3]));
		result[3][1] = mathLibrary.multiply(matrixCache1[2][0], mathLibrary.multiply(matrixCache1[3][1], matrixCache1[0][2])) -
						mathLibrary.multiply(matrixCache1[3][0], mathLibrary.multiply(matrixCache1[2][1], matrixCache1[0][2])) +
						mathLibrary.multiply(matrixCache1[3][0], mathLibrary.multiply(matrixCache1[0][1], matrixCache1[2][2])) -
						mathLibrary.multiply(matrixCache1[0][0], mathLibrary.multiply(matrixCache1[3][1], matrixCache1[2][2])) -
						mathLibrary.multiply(matrixCache1[2][0], mathLibrary.multiply(matrixCache1[0][1], matrixCache1[3][2])) +
						mathLibrary.multiply(matrixCache1[0][0], mathLibrary.multiply(matrixCache1[2][1], matrixCache1[3][2]));
		result[0][2] = mathLibrary.multiply(matrixCache1[1][1], mathLibrary.multiply(matrixCache1[3][2], matrixCache1[0][3])) -
						mathLibrary.multiply(matrixCache1[3][1], mathLibrary.multiply(matrixCache1[1][2], matrixCache1[0][3])) +
						mathLibrary.multiply(matrixCache1[3][1], mathLibrary.multiply(matrixCache1[0][2], matrixCache1[1][3])) -
						mathLibrary.multiply(matrixCache1[0][1], mathLibrary.multiply(matrixCache1[3][2], matrixCache1[1][3])) -
						mathLibrary.multiply(matrixCache1[1][1], mathLibrary.multiply(matrixCache1[0][2], matrixCache1[3][3])) +
						mathLibrary.multiply(matrixCache1[0][1], mathLibrary.multiply(matrixCache1[1][2], matrixCache1[3][3]));
		result[1][2] = mathLibrary.multiply(matrixCache1[3][0], mathLibrary.multiply(matrixCache1[1][2], matrixCache1[0][3])) -
						mathLibrary.multiply(matrixCache1[1][0], mathLibrary.multiply(matrixCache1[3][2], matrixCache1[0][3])) -
						mathLibrary.multiply(matrixCache1[3][0], mathLibrary.multiply(matrixCache1[0][2], matrixCache1[1][3])) +
						mathLibrary.multiply(matrixCache1[0][0], mathLibrary.multiply(matrixCache1[3][2], matrixCache1[1][3])) +
						mathLibrary.multiply(matrixCache1[1][0], mathLibrary.multiply(matrixCache1[0][2], matrixCache1[3][3])) -
						mathLibrary.multiply(matrixCache1[0][0], mathLibrary.multiply(matrixCache1[1][2], matrixCache1[3][3]));
		result[2][2] = mathLibrary.multiply(matrixCache1[1][0], mathLibrary.multiply(matrixCache1[3][1], matrixCache1[0][3])) -
						mathLibrary.multiply(matrixCache1[3][0], mathLibrary.multiply(matrixCache1[1][1], matrixCache1[0][3])) +
						mathLibrary.multiply(matrixCache1[3][0], mathLibrary.multiply(matrixCache1[0][1], matrixCache1[1][3])) -
						mathLibrary.multiply(matrixCache1[0][0], mathLibrary.multiply(matrixCache1[3][1], matrixCache1[1][3])) -
						mathLibrary.multiply(matrixCache1[1][0], mathLibrary.multiply(matrixCache1[0][1], matrixCache1[3][3])) +
						mathLibrary.multiply(matrixCache1[0][0], mathLibrary.multiply(matrixCache1[1][1], matrixCache1[3][3]));
		result[3][2] = mathLibrary.multiply(matrixCache1[3][0], mathLibrary.multiply(matrixCache1[1][1], matrixCache1[0][2])) -
						mathLibrary.multiply(matrixCache1[1][0], mathLibrary.multiply(matrixCache1[3][1], matrixCache1[0][2])) -
						mathLibrary.multiply(matrixCache1[3][0], mathLibrary.multiply(matrixCache1[0][1], matrixCache1[1][2])) +
						mathLibrary.multiply(matrixCache1[0][0], mathLibrary.multiply(matrixCache1[3][1], matrixCache1[1][2])) +
						mathLibrary.multiply(matrixCache1[1][0], mathLibrary.multiply(matrixCache1[0][1], matrixCache1[3][2])) -
						mathLibrary.multiply(matrixCache1[0][0], mathLibrary.multiply(matrixCache1[1][1], matrixCache1[3][2]));
		result[0][3] = mathLibrary.multiply(matrixCache1[2][1], mathLibrary.multiply(matrixCache1[1][2], matrixCache1[0][3])) -
						mathLibrary.multiply(matrixCache1[1][1], mathLibrary.multiply(matrixCache1[2][2], matrixCache1[0][3])) -
						mathLibrary.multiply(matrixCache1[2][1], mathLibrary.multiply(matrixCache1[0][2], matrixCache1[1][3])) +
						mathLibrary.multiply(matrixCache1[0][1], mathLibrary.multiply(matrixCache1[2][2], matrixCache1[1][3])) +
						mathLibrary.multiply(matrixCache1[1][1], mathLibrary.multiply(matrixCache1[0][2], matrixCache1[2][3])) -
						mathLibrary.multiply(matrixCache1[0][1], mathLibrary.multiply(matrixCache1[1][2], matrixCache1[2][3]));
		result[1][3] = mathLibrary.multiply(matrixCache1[1][0], mathLibrary.multiply(matrixCache1[2][2], matrixCache1[0][3])) -
						mathLibrary.multiply(matrixCache1[2][0], mathLibrary.multiply(matrixCache1[1][2], matrixCache1[0][3])) +
						mathLibrary.multiply(matrixCache1[2][0], mathLibrary.multiply(matrixCache1[0][2], matrixCache1[1][3])) -
						mathLibrary.multiply(matrixCache1[0][0], mathLibrary.multiply(matrixCache1[2][2], matrixCache1[1][3])) -
						mathLibrary.multiply(matrixCache1[1][0], mathLibrary.multiply(matrixCache1[0][2], matrixCache1[2][3])) +
						mathLibrary.multiply(matrixCache1[0][0], mathLibrary.multiply(matrixCache1[1][2], matrixCache1[2][3]));
		result[2][3] = mathLibrary.multiply(matrixCache1[2][0], mathLibrary.multiply(matrixCache1[1][1], matrixCache1[0][3])) -
						mathLibrary.multiply(matrixCache1[1][0], mathLibrary.multiply(matrixCache1[2][1], matrixCache1[0][3])) -
						mathLibrary.multiply(matrixCache1[2][0], mathLibrary.multiply(matrixCache1[0][1], matrixCache1[1][3])) +
						mathLibrary.multiply(matrixCache1[0][0], mathLibrary.multiply(matrixCache1[2][1], matrixCache1[1][3])) +
						mathLibrary.multiply(matrixCache1[1][0], mathLibrary.multiply(matrixCache1[0][1], matrixCache1[2][3])) -
						mathLibrary.multiply(matrixCache1[0][0], mathLibrary.multiply(matrixCache1[1][1], matrixCache1[2][3]));
		result[3][3] = mathLibrary.multiply(matrixCache1[1][0], mathLibrary.multiply(matrixCache1[2][1], matrixCache1[0][2])) -
						mathLibrary.multiply(matrixCache1[2][0], mathLibrary.multiply(matrixCache1[1][1], matrixCache1[0][2])) +
						mathLibrary.multiply(matrixCache1[2][0], mathLibrary.multiply(matrixCache1[0][1], matrixCache1[1][2])) -
						mathLibrary.multiply(matrixCache1[0][0], mathLibrary.multiply(matrixCache1[2][1], matrixCache1[1][2])) -
						mathLibrary.multiply(matrixCache1[1][0], mathLibrary.multiply(matrixCache1[0][1], matrixCache1[2][2])) +
						mathLibrary.multiply(matrixCache1[0][0], mathLibrary.multiply(matrixCache1[1][1], matrixCache1[2][2]));
		divide(result, determinant, result);
		return result;
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
	public int[][] translate(int[][] matrix, int[] vector, int[][] result) {
		copy(matrixCache1, MATRIX_IDENTITY);
		matrixCache1[3][0] = vector[VECTOR_X];
		matrixCache1[3][1] = vector[VECTOR_Y];
		matrixCache1[3][2] = vector[VECTOR_Z];
		multiply(matrixCache1, matrix, result);
		return result;
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
	public int[][] scale(int[][] matrix, int[] vector, int[][] result) {
		copy(matrixCache1, MATRIX_IDENTITY);
		matrixCache1[0][0] = vector[VECTOR_X];
		matrixCache1[1][1] = vector[VECTOR_Y];
		matrixCache1[2][2] = vector[VECTOR_Z];
		multiply(matrixCache1, matrix, result);
		return result;
	}

	/**
	 * Sets result equals the matrix rotated around (0, 0, 0) at x axis by the given
	 * angle.
	 *
	 * @param matrix
	 * @param angle
	 * @param result
	 */
	public int[][] rotateX(int[][] matrix, int angle, int[][] result) {
		copy(matrixCache1, MATRIX_IDENTITY);
		int cos = mathLibrary.cos(angle);
		int sin = mathLibrary.sin(angle);
		matrixCache1[1][1] = cos;
		matrixCache1[1][2] = sin;
		matrixCache1[2][1] = -sin;
		matrixCache1[2][2] = cos;
		multiply(matrixCache1, matrix, result);
		return result;
	}

	/**
	 * Sets result equals the matrix rotated around (0, 0, 0) at y axis by the given
	 * angle.
	 *
	 * @param matrix
	 * @param angle
	 * @param result
	 */
	public int[][] rotateY(int[][] matrix, int angle, int[][] result) {
		copy(matrixCache1, MATRIX_IDENTITY);
		int cos = mathLibrary.cos(-angle);
		int sin = mathLibrary.sin(-angle);
		matrixCache1[0][0] = cos;
		matrixCache1[0][2] = -sin;
		matrixCache1[2][0] = sin;
		matrixCache1[2][2] = cos;
		multiply(matrixCache1, matrix, result);
		return result;
	}

	/**
	 * Sets result equals the matrix rotated around (0, 0, 0) at z axis by the given
	 * angle.
	 *
	 * @param matrix
	 * @param angle
	 * @param result
	 */
	public int[][] rotateZ(int[][] matrix, int angle, int[][] result) {
		copy(matrixCache1, MATRIX_IDENTITY);
		int cos = mathLibrary.cos(-angle);
		int sin = mathLibrary.sin(-angle);
		matrixCache1[0][0] = cos;
		matrixCache1[0][1] = sin;
		matrixCache1[1][0] = -sin;
		matrixCache1[1][1] = cos;
		multiply(matrixCache1, matrix, result);
		return result;
	}

	/**
	 * Sets result equals the matrix rotated around (0, 0, 0) at x, y and z axis by the
	 * given angles.
	 * 
	 * @param vector
	 * @param angles
	 * @param result
	 * @return
	 */
	public int[][] rotateXYZ(int[][] vector, int[] angles, int[][] result) {
		rotateX(vector, angles[VECTOR_X], result);
		rotateY(result, angles[VECTOR_Y], result);
		rotateZ(result, angles[VECTOR_Z], result);
		return result;
	}

	/**
	 * Sets result equals the matrix rotated around (0, 0, 0) at z, y and x axis by the
	 * given angles.
	 * 
	 * @param vector
	 * @param angles
	 * @param result
	 * @return
	 */
	public int[][] rotateZYX(int[][] vector, int[] angles, int[][] result) {
		rotateZ(vector, angles[VECTOR_Z], result);
		rotateY(result, angles[VECTOR_Y], result);
		rotateX(result, angles[VECTOR_X], result);
		return result;
	}

	/**
	 * Copies the value of matrix to the target.
	 * 
	 * @param target
	 * @param matrix
	 */
	public int[][] copy(int[][] target, int[][] matrix) {
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 4; j++) {
				target[i][j] = matrix[i][j];
			}
		}
		return target;
	}

	/**
	 * Returns if matrix1 is equals to matrix2.
	 * 
	 * @param matrix1
	 * @param matrix2
	 * @return
	 */
	public boolean equals(int[][] matrix1, int[][] matrix2) {
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 4; j++) {
				if (matrix1[i][j] != matrix2[i][j])
					return false;
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
	public String toString(int[][] matrix) {
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
