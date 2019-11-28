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
package com.johnsproject.jgameengine.math;

import static com.johnsproject.jgameengine.math.FixedPointMath.*;

/**
 * The MatrixLibrary class contains methods for generating matrices and performing matrix 
 * operations such as multiply, translate, scale, rotate.
 * 
 * @author John Ferraz Salomon
 */
public final class MatrixMath {
	
	public static final byte MATRIX_ROW_SIZE = 4;
	public static final byte MATRIX_COLUMN_SIZE = 4;
	public static final byte MATRIX_SIZE = 16;

	public static final int[] MATRIX_IDENTITY = new int[] {
		FP_ONE, 0, 0, 0,
		0, FP_ONE, 0, 0,
		0, 0, FP_ONE, 0,
		0, 0, 0, FP_ONE
	};

	private MatrixMath() { }

	/**
	 * Returns an identity matrix.
	 * 
	 * @return
	 */
	public static int[] indentityMatrix() {
		return new int[] {
			FP_ONE, 0, 0, 0,
			0, FP_ONE, 0, 0,
			0, 0, FP_ONE, 0,
			0, 0, 0, FP_ONE
		};
	}
	
	/**
	 * Returns the value at the given column and row.
	 * 
	 * @param matrix
	 * @param column
	 * @param row
	 * @return
	 */
	public static int get(int[] matrix, int column, int row) {
		return matrix[column + (row * MATRIX_ROW_SIZE)];
	}
	
	/**
	 * Sets the value at the given column and row equals value.
	 * 
	 * @param matrix
	 * @param column
	 * @param row
	 * @param value
	 * @return
	 */
	public static void set(int[] matrix, int column, int row, int value) {
		matrix[column + (row * MATRIX_ROW_SIZE)] = value;
	}
	
	/**
	 * Sets result equals the result of the addition of matrix1 and matrix2.
	 * 
	 * @param matrix1
	 * @param matrix2
	 * @param result
	 */
	public static int[] add(int[] matrix1, int[] matrix2, int[] result) {
		for (int i = 0; i < MATRIX_COLUMN_SIZE; i++) {
			for (int j = 0; j < MATRIX_ROW_SIZE; j++) {
				int res = get(matrix1, 0, j) + get(matrix2, i, 0);
				res += get(matrix1, 1, j) + get(matrix2, i, 1);
				res += get(matrix1, 2, j) + get(matrix2, i, 2);
				res += get(matrix1, 3, j) + get(matrix2, i, 3);
				set(result, i, j, res);
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
	public static int[] subtract(int[] matrix1, int[] matrix2, int[] result) {
		for (int i = 0; i < MATRIX_COLUMN_SIZE; i++) {
			for (int j = 0; j < MATRIX_ROW_SIZE; j++) {
				int res = get(matrix1, 0, j) - get(matrix2, i, 0);
				res += get(matrix1, 1, j) - get(matrix2, i, 1);
				res += get(matrix1, 2, j) - get(matrix2, i, 2);
				res += get(matrix1, 3, j) - get(matrix2, i, 3);
				set(result, i, j, res);
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
	public static int[] multiply(int[] matrix1, int[] matrix2, int[] result) {
		for (int i = 0; i < MATRIX_COLUMN_SIZE; i++) {
			for (int j = 0; j < MATRIX_ROW_SIZE; j++) {
				int res = FixedPointMath.multiply(get(matrix1, 0, j), get(matrix2, i, 0));
				res += FixedPointMath.multiply(get(matrix1, 1, j), get(matrix2, i, 1));
				res += FixedPointMath.multiply(get(matrix1, 2, j), get(matrix2, i, 2));
				res += FixedPointMath.multiply(get(matrix1, 3, j), get(matrix2, i, 3));
				set(result, i, j, res);
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
	public static int[] divide(int[] matrix1, int[] matrix2, int[] result) {
		for (int i = 0; i < MATRIX_COLUMN_SIZE; i++) {
			for (int j = 0; j < MATRIX_ROW_SIZE; j++) {
				int res = FixedPointMath.divide(get(matrix1, 0, j), get(matrix2, i, 0));
				res += FixedPointMath.divide(get(matrix1, 1, j), get(matrix2, i, 1));
				res += FixedPointMath.divide(get(matrix1, 2, j), get(matrix2, i, 2));
				res += FixedPointMath.divide(get(matrix1, 3, j), get(matrix2, i, 3));
				set(result, i, j, res);
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
	public static int[] add(int[] matrix, int value) {
		for (int i = 0; i < MATRIX_COLUMN_SIZE; i++) {
			for (int j = 0; j < MATRIX_ROW_SIZE; j++) {
				set(matrix, i, j, get(matrix, i, j) + value);
			}
		}
		return matrix;
	}
	
	/**
	 * Sets result equals the result of the subtraction of matrix by value.
	 * 
	 * @param matrix
	 * @param value
	 * @param result
	 */
	public static int[] subtract(int[] matrix, int value) {
		for (int i = 0; i < MATRIX_COLUMN_SIZE; i++) {
			for (int j = 0; j < MATRIX_ROW_SIZE; j++) {
				set(matrix, i, j, get(matrix, i, j) - value);
			}
		}
		return matrix;
	}
	
	/**
	 * Sets result equals the result of the multiplication of matrix by value.
	 * 
	 * @param matrix
	 * @param value
	 * @param result
	 */
	public static int[] multiply(int[] matrix, int value) {
		for (int i = 0; i < MATRIX_COLUMN_SIZE; i++) {
			for (int j = 0; j < MATRIX_ROW_SIZE; j++) {
				set(matrix, i, j, FixedPointMath.multiply(get(matrix, i, j), value));
			}
		}
		return matrix;
	}
	
	/**
	 * Sets result equals the result of the division of matrix by value.
	 * 
	 * @param matrix
	 * @param value
	 * @param result
	 */
	public static int[] divide(int[] matrix, int value) {
		for (int i = 0; i < MATRIX_COLUMN_SIZE; i++) {
			for (int j = 0; j < MATRIX_ROW_SIZE; j++) {
				set(matrix, i, j, FixedPointMath.divide(get(matrix, i, j), value));
			}
		}
		return matrix;
	}

	/**
	 * Sets result equals the transposed matrix.
	 * 
	 * @param matrix
	 * @param result
	 */
	public static int[] transpose(int[] matrix, int[] result) {
		for (int i = 0; i < MATRIX_COLUMN_SIZE; i++) {
			for (int j = 0; j < MATRIX_ROW_SIZE; j++) {
				set(result, i, j, get(matrix, j, i));
			}
		}
		return result;
	}
	
	/**
	 * Returns the determinant of the given matrix.
	 * 
	 * @param matrix
	 */
	public static int determinant(int[] matrix) {
		return	FixedPointMath.multiply(FixedPointMath.multiply(get(matrix, 3, 0), get(matrix, 2, 1)), FixedPointMath.multiply(get(matrix, 1, 2), get(matrix, 0, 3))) - 
				FixedPointMath.multiply(FixedPointMath.multiply(get(matrix, 2, 0), get(matrix, 3, 1)), FixedPointMath.multiply(get(matrix, 1, 2), get(matrix, 0, 3))) -
				FixedPointMath.multiply(FixedPointMath.multiply(get(matrix, 3, 0), get(matrix, 1, 1)), FixedPointMath.multiply(get(matrix, 2, 2), get(matrix, 0, 3))) + 
				FixedPointMath.multiply(FixedPointMath.multiply(get(matrix, 1, 0), get(matrix, 3, 1)), FixedPointMath.multiply(get(matrix, 2, 2), get(matrix, 0, 3))) +
				FixedPointMath.multiply(FixedPointMath.multiply(get(matrix, 2, 0), get(matrix, 1, 1)), FixedPointMath.multiply(get(matrix, 3, 2), get(matrix, 0, 3))) - 
				FixedPointMath.multiply(FixedPointMath.multiply(get(matrix, 1, 0), get(matrix, 2, 1)), FixedPointMath.multiply(get(matrix, 3, 2), get(matrix, 0, 3))) -
				FixedPointMath.multiply(FixedPointMath.multiply(get(matrix, 3, 0), get(matrix, 2, 1)), FixedPointMath.multiply(get(matrix, 0, 2), get(matrix, 1, 3))) + 
				FixedPointMath.multiply(FixedPointMath.multiply(get(matrix, 2, 0), get(matrix, 3, 1)), FixedPointMath.multiply(get(matrix, 0, 2), get(matrix, 1, 3))) +
				FixedPointMath.multiply(FixedPointMath.multiply(get(matrix, 3, 0), get(matrix, 0, 1)), FixedPointMath.multiply(get(matrix, 2, 2), get(matrix, 1, 3))) - 
				FixedPointMath.multiply(FixedPointMath.multiply(get(matrix, 0, 0), get(matrix, 3, 1)), FixedPointMath.multiply(get(matrix, 2, 2), get(matrix, 1, 3))) -
				FixedPointMath.multiply(FixedPointMath.multiply(get(matrix, 2, 0), get(matrix, 0, 1)), FixedPointMath.multiply(get(matrix, 3, 2), get(matrix, 1, 3))) + 
				FixedPointMath.multiply(FixedPointMath.multiply(get(matrix, 0, 0), get(matrix, 2, 1)), FixedPointMath.multiply(get(matrix, 3, 2), get(matrix, 1, 3))) +
				FixedPointMath.multiply(FixedPointMath.multiply(get(matrix, 3, 0), get(matrix, 1, 1)), FixedPointMath.multiply(get(matrix, 0, 2), get(matrix, 2, 3))) - 
				FixedPointMath.multiply(FixedPointMath.multiply(get(matrix, 1, 0), get(matrix, 3, 1)), FixedPointMath.multiply(get(matrix, 0, 2), get(matrix, 2, 3))) -
				FixedPointMath.multiply(FixedPointMath.multiply(get(matrix, 3, 0), get(matrix, 0, 1)), FixedPointMath.multiply(get(matrix, 1, 2), get(matrix, 2, 3))) + 
				FixedPointMath.multiply(FixedPointMath.multiply(get(matrix, 0, 0), get(matrix, 3, 1)), FixedPointMath.multiply(get(matrix, 1, 2), get(matrix, 2, 3))) +
				FixedPointMath.multiply(FixedPointMath.multiply(get(matrix, 1, 0), get(matrix, 0, 1)), FixedPointMath.multiply(get(matrix, 3, 2), get(matrix, 2, 3))) - 
				FixedPointMath.multiply(FixedPointMath.multiply(get(matrix, 0, 0), get(matrix, 1, 1)), FixedPointMath.multiply(get(matrix, 3, 2), get(matrix, 2, 3))) -
				FixedPointMath.multiply(FixedPointMath.multiply(get(matrix, 2, 0), get(matrix, 1, 1)), FixedPointMath.multiply(get(matrix, 0, 2), get(matrix, 3, 3))) + 
				FixedPointMath.multiply(FixedPointMath.multiply(get(matrix, 1, 0), get(matrix, 2, 1)), FixedPointMath.multiply(get(matrix, 0, 2), get(matrix, 3, 3))) +
				FixedPointMath.multiply(FixedPointMath.multiply(get(matrix, 2, 0), get(matrix, 0, 1)), FixedPointMath.multiply(get(matrix, 1, 2), get(matrix, 3, 3))) - 
				FixedPointMath.multiply(FixedPointMath.multiply(get(matrix, 0, 0), get(matrix, 2, 1)), FixedPointMath.multiply(get(matrix, 1, 2), get(matrix, 3, 3))) -
				FixedPointMath.multiply(FixedPointMath.multiply(get(matrix, 1, 0), get(matrix, 0, 1)), FixedPointMath.multiply(get(matrix, 2, 2), get(matrix, 3, 3))) + 
				FixedPointMath.multiply(FixedPointMath.multiply(get(matrix, 0, 0), get(matrix, 1, 1)), FixedPointMath.multiply(get(matrix, 2, 2), get(matrix, 3, 3)));
	}
	
	/**
	 * Sets result equals the inverse of matrix.
	 * 
	 * @param matrix
	 * @param result
	 */
	public static int[] inverse(int[] matrix, int[] result) {
		matrix = copy(result, matrix);
		int determinant = determinant(matrix) + 1;
		set(result, 0, 0, FixedPointMath.multiply(get(matrix, 2, 1), FixedPointMath.multiply(get(matrix, 3, 2), get(matrix, 1, 3))) -
						FixedPointMath.multiply(get(matrix, 3, 1), FixedPointMath.multiply(get(matrix, 2, 2), get(matrix, 1, 3))) +
						FixedPointMath.multiply(get(matrix, 3, 1), FixedPointMath.multiply(get(matrix, 1, 2), get(matrix, 2, 3))) -
						FixedPointMath.multiply(get(matrix, 1, 1), FixedPointMath.multiply(get(matrix, 3, 2), get(matrix, 2, 3))) -
						FixedPointMath.multiply(get(matrix, 2, 1), FixedPointMath.multiply(get(matrix, 1, 2), get(matrix, 3, 3))) +
						FixedPointMath.multiply(get(matrix, 1, 1), FixedPointMath.multiply(get(matrix, 2, 2), get(matrix, 3, 3))));
		set(result, 1, 0, FixedPointMath.multiply(get(matrix, 3, 0), FixedPointMath.multiply(get(matrix, 2, 2), get(matrix, 1, 3))) -
						FixedPointMath.multiply(get(matrix, 2, 0), FixedPointMath.multiply(get(matrix, 2, 3), get(matrix, 1, 3))) -
						FixedPointMath.multiply(get(matrix, 3, 0), FixedPointMath.multiply(get(matrix, 2, 1), get(matrix, 2, 3))) +
						FixedPointMath.multiply(get(matrix, 1, 0), FixedPointMath.multiply(get(matrix, 2, 3), get(matrix, 2, 3))) +
						FixedPointMath.multiply(get(matrix, 2, 0), FixedPointMath.multiply(get(matrix, 2, 1), get(matrix, 3, 3))) -
						FixedPointMath.multiply(get(matrix, 1, 0), FixedPointMath.multiply(get(matrix, 2, 2), get(matrix, 3, 3))));
		set(result, 2, 0, FixedPointMath.multiply(get(matrix, 2, 0), FixedPointMath.multiply(get(matrix, 3, 1), get(matrix, 1, 3))) -
						FixedPointMath.multiply(get(matrix, 3, 0), FixedPointMath.multiply(get(matrix, 2, 1), get(matrix, 1, 3))) +
						FixedPointMath.multiply(get(matrix, 3, 0), FixedPointMath.multiply(get(matrix, 1, 1), get(matrix, 2, 3))) -
						FixedPointMath.multiply(get(matrix, 1, 0), FixedPointMath.multiply(get(matrix, 3, 1), get(matrix, 2, 3))) -
						FixedPointMath.multiply(get(matrix, 2, 0), FixedPointMath.multiply(get(matrix, 1, 1), get(matrix, 3, 3))) +
						FixedPointMath.multiply(get(matrix, 1, 0), FixedPointMath.multiply(get(matrix, 2, 1), get(matrix, 3, 3))));
		set(result, 3, 0, FixedPointMath.multiply(get(matrix, 3, 0), FixedPointMath.multiply(get(matrix, 2, 1), get(matrix, 1, 2))) -
						FixedPointMath.multiply(get(matrix, 2, 0), FixedPointMath.multiply(get(matrix, 3, 1), get(matrix, 1, 2))) -
						FixedPointMath.multiply(get(matrix, 3, 0), FixedPointMath.multiply(get(matrix, 1, 1), get(matrix, 2, 2))) +
						FixedPointMath.multiply(get(matrix, 1, 0), FixedPointMath.multiply(get(matrix, 3, 1), get(matrix, 2, 2))) +
						FixedPointMath.multiply(get(matrix, 2, 0), FixedPointMath.multiply(get(matrix, 1, 1), get(matrix, 3, 2))) -
						FixedPointMath.multiply(get(matrix, 1, 0), FixedPointMath.multiply(get(matrix, 2, 1), get(matrix, 3, 2))));
		set(result, 0, 1, FixedPointMath.multiply(get(matrix, 3, 1), FixedPointMath.multiply(get(matrix, 2, 2), get(matrix, 0, 3))) -
						FixedPointMath.multiply(get(matrix, 2, 1), FixedPointMath.multiply(get(matrix, 3, 2), get(matrix, 0, 3))) -
						FixedPointMath.multiply(get(matrix, 3, 1), FixedPointMath.multiply(get(matrix, 0, 2), get(matrix, 2, 3))) +
						FixedPointMath.multiply(get(matrix, 0, 1), FixedPointMath.multiply(get(matrix, 3, 2), get(matrix, 2, 3))) +
						FixedPointMath.multiply(get(matrix, 2, 1), FixedPointMath.multiply(get(matrix, 0, 2), get(matrix, 3, 3))) -
						FixedPointMath.multiply(get(matrix, 0, 1), FixedPointMath.multiply(get(matrix, 2, 2), get(matrix, 3, 3))));
		set(result, 1, 1, FixedPointMath.multiply(get(matrix, 2, 0), FixedPointMath.multiply(get(matrix, 3, 2), get(matrix, 0, 3))) -
						FixedPointMath.multiply(get(matrix, 3, 0), FixedPointMath.multiply(get(matrix, 2, 2), get(matrix, 0, 3))) +
						FixedPointMath.multiply(get(matrix, 3, 0), FixedPointMath.multiply(get(matrix, 0, 2), get(matrix, 2, 3))) -
						FixedPointMath.multiply(get(matrix, 0, 0), FixedPointMath.multiply(get(matrix, 3, 2), get(matrix, 2, 3))) -
						FixedPointMath.multiply(get(matrix, 2, 0), FixedPointMath.multiply(get(matrix, 0, 2), get(matrix, 3, 3))) +
						FixedPointMath.multiply(get(matrix, 0, 0), FixedPointMath.multiply(get(matrix, 2, 2), get(matrix, 3, 3))));
		set(result, 2, 1, FixedPointMath.multiply(get(matrix, 3, 0), FixedPointMath.multiply(get(matrix, 2, 1), get(matrix, 0, 3))) -
						FixedPointMath.multiply(get(matrix, 2, 0), FixedPointMath.multiply(get(matrix, 3, 1), get(matrix, 0, 3))) -
						FixedPointMath.multiply(get(matrix, 3, 0), FixedPointMath.multiply(get(matrix, 0, 1), get(matrix, 2, 3))) +
						FixedPointMath.multiply(get(matrix, 0, 0), FixedPointMath.multiply(get(matrix, 3, 1), get(matrix, 2, 3))) +
						FixedPointMath.multiply(get(matrix, 2, 0), FixedPointMath.multiply(get(matrix, 0, 1), get(matrix, 3, 3))) -
						FixedPointMath.multiply(get(matrix, 0, 0), FixedPointMath.multiply(get(matrix, 2, 1), get(matrix, 3, 3))));
		set(result, 3, 1, FixedPointMath.multiply(get(matrix, 2, 0), FixedPointMath.multiply(get(matrix, 3, 1), get(matrix, 0, 2))) -
						FixedPointMath.multiply(get(matrix, 3, 0), FixedPointMath.multiply(get(matrix, 2, 1), get(matrix, 0, 2))) +
						FixedPointMath.multiply(get(matrix, 3, 0), FixedPointMath.multiply(get(matrix, 0, 1), get(matrix, 2, 2))) -
						FixedPointMath.multiply(get(matrix, 0, 0), FixedPointMath.multiply(get(matrix, 3, 1), get(matrix, 2, 2))) -
						FixedPointMath.multiply(get(matrix, 2, 0), FixedPointMath.multiply(get(matrix, 0, 1), get(matrix, 3, 2))) +
						FixedPointMath.multiply(get(matrix, 0, 0), FixedPointMath.multiply(get(matrix, 2, 1), get(matrix, 3, 2))));
		set(result, 0, 2, FixedPointMath.multiply(get(matrix, 1, 1), FixedPointMath.multiply(get(matrix, 3, 2), get(matrix, 0, 3))) -
						FixedPointMath.multiply(get(matrix, 3, 1), FixedPointMath.multiply(get(matrix, 1, 2), get(matrix, 0, 3))) +
						FixedPointMath.multiply(get(matrix, 3, 1), FixedPointMath.multiply(get(matrix, 0, 2), get(matrix, 1, 3))) -
						FixedPointMath.multiply(get(matrix, 0, 1), FixedPointMath.multiply(get(matrix, 3, 2), get(matrix, 1, 3))) -
						FixedPointMath.multiply(get(matrix, 1, 1), FixedPointMath.multiply(get(matrix, 0, 2), get(matrix, 3, 3))) +
						FixedPointMath.multiply(get(matrix, 0, 1), FixedPointMath.multiply(get(matrix, 1, 2), get(matrix, 3, 3))));
		set(result, 1, 2, FixedPointMath.multiply(get(matrix, 3, 0), FixedPointMath.multiply(get(matrix, 1, 2), get(matrix, 0, 3))) -
						FixedPointMath.multiply(get(matrix, 1, 0), FixedPointMath.multiply(get(matrix, 3, 2), get(matrix, 0, 3))) -
						FixedPointMath.multiply(get(matrix, 3, 0), FixedPointMath.multiply(get(matrix, 0, 2), get(matrix, 1, 3))) +
						FixedPointMath.multiply(get(matrix, 0, 0), FixedPointMath.multiply(get(matrix, 3, 2), get(matrix, 1, 3))) +
						FixedPointMath.multiply(get(matrix, 1, 0), FixedPointMath.multiply(get(matrix, 0, 2), get(matrix, 3, 3))) -
						FixedPointMath.multiply(get(matrix, 0, 0), FixedPointMath.multiply(get(matrix, 1, 2), get(matrix, 3, 3))));
		set(result, 2, 2, FixedPointMath.multiply(get(matrix, 1, 0), FixedPointMath.multiply(get(matrix, 3, 1), get(matrix, 0, 3))) -
						FixedPointMath.multiply(get(matrix, 3, 0), FixedPointMath.multiply(get(matrix, 1, 1), get(matrix, 0, 3))) +
						FixedPointMath.multiply(get(matrix, 3, 0), FixedPointMath.multiply(get(matrix, 0, 1), get(matrix, 1, 3))) -
						FixedPointMath.multiply(get(matrix, 0, 0), FixedPointMath.multiply(get(matrix, 3, 1), get(matrix, 1, 3))) -
						FixedPointMath.multiply(get(matrix, 1, 0), FixedPointMath.multiply(get(matrix, 0, 1), get(matrix, 3, 3))) +
						FixedPointMath.multiply(get(matrix, 0, 0), FixedPointMath.multiply(get(matrix, 1, 1), get(matrix, 3, 3))));
		set(result, 3, 2, FixedPointMath.multiply(get(matrix, 3, 0), FixedPointMath.multiply(get(matrix, 1, 1), get(matrix, 0, 2))) -
						FixedPointMath.multiply(get(matrix, 1, 0), FixedPointMath.multiply(get(matrix, 3, 1), get(matrix, 0, 2))) -
						FixedPointMath.multiply(get(matrix, 3, 0), FixedPointMath.multiply(get(matrix, 0, 1), get(matrix, 1, 2))) +
						FixedPointMath.multiply(get(matrix, 0, 0), FixedPointMath.multiply(get(matrix, 3, 1), get(matrix, 1, 2))) +
						FixedPointMath.multiply(get(matrix, 1, 0), FixedPointMath.multiply(get(matrix, 0, 1), get(matrix, 3, 2))) -
						FixedPointMath.multiply(get(matrix, 0, 0), FixedPointMath.multiply(get(matrix, 1, 1), get(matrix, 3, 2))));
		set(result, 0, 3, FixedPointMath.multiply(get(matrix, 2, 1), FixedPointMath.multiply(get(matrix, 1, 2), get(matrix, 0, 3))) -
						FixedPointMath.multiply(get(matrix, 1, 1), FixedPointMath.multiply(get(matrix, 2, 2), get(matrix, 0, 3))) -
						FixedPointMath.multiply(get(matrix, 2, 1), FixedPointMath.multiply(get(matrix, 0, 2), get(matrix, 1, 3))) +
						FixedPointMath.multiply(get(matrix, 0, 1), FixedPointMath.multiply(get(matrix, 2, 2), get(matrix, 1, 3))) +
						FixedPointMath.multiply(get(matrix, 1, 1), FixedPointMath.multiply(get(matrix, 0, 2), get(matrix, 2, 3))) -
						FixedPointMath.multiply(get(matrix, 0, 1), FixedPointMath.multiply(get(matrix, 1, 2), get(matrix, 2, 3))));
		set(result, 1, 3, FixedPointMath.multiply(get(matrix, 1, 0), FixedPointMath.multiply(get(matrix, 2, 2), get(matrix, 0, 3))) -
						FixedPointMath.multiply(get(matrix, 2, 0), FixedPointMath.multiply(get(matrix, 1, 2), get(matrix, 0, 3))) +
						FixedPointMath.multiply(get(matrix, 2, 0), FixedPointMath.multiply(get(matrix, 0, 2), get(matrix, 1, 3))) -
						FixedPointMath.multiply(get(matrix, 0, 0), FixedPointMath.multiply(get(matrix, 2, 2), get(matrix, 1, 3))) -
						FixedPointMath.multiply(get(matrix, 1, 0), FixedPointMath.multiply(get(matrix, 0, 2), get(matrix, 2, 3))) +
						FixedPointMath.multiply(get(matrix, 0, 0), FixedPointMath.multiply(get(matrix, 1, 2), get(matrix, 2, 3))));
		set(result, 2, 3, FixedPointMath.multiply(get(matrix, 2, 0), FixedPointMath.multiply(get(matrix, 1, 1), get(matrix, 0, 3))) -
						FixedPointMath.multiply(get(matrix, 1, 0), FixedPointMath.multiply(get(matrix, 2, 1), get(matrix, 0, 3))) -
						FixedPointMath.multiply(get(matrix, 2, 0), FixedPointMath.multiply(get(matrix, 0, 1), get(matrix, 1, 3))) +
						FixedPointMath.multiply(get(matrix, 0, 0), FixedPointMath.multiply(get(matrix, 2, 1), get(matrix, 1, 3))) +
						FixedPointMath.multiply(get(matrix, 1, 0), FixedPointMath.multiply(get(matrix, 0, 1), get(matrix, 2, 3))) -
						FixedPointMath.multiply(get(matrix, 0, 0), FixedPointMath.multiply(get(matrix, 1, 1), get(matrix, 2, 3))));
		set(result, 3, 3, FixedPointMath.multiply(get(matrix, 1, 0), FixedPointMath.multiply(get(matrix, 2, 1), get(matrix, 0, 2))) -
						FixedPointMath.multiply(get(matrix, 2, 0), FixedPointMath.multiply(get(matrix, 1, 1), get(matrix, 0, 2))) +
						FixedPointMath.multiply(get(matrix, 2, 0), FixedPointMath.multiply(get(matrix, 0, 1), get(matrix, 1, 2))) -
						FixedPointMath.multiply(get(matrix, 0, 0), FixedPointMath.multiply(get(matrix, 2, 1), get(matrix, 1, 2))) -
						FixedPointMath.multiply(get(matrix, 1, 0), FixedPointMath.multiply(get(matrix, 0, 1), get(matrix, 2, 2))) +
						FixedPointMath.multiply(get(matrix, 0, 0), FixedPointMath.multiply(get(matrix, 1, 1), get(matrix, 2, 2))));
		divide(result, determinant);
		return result;
	}

	/**
	 * Copies the value of matrix to the target.
	 * 
	 * @param target
	 * @param matrix
	 */
	public static int[] copy(int[] target, int[] matrix) {
		for (int i = 0; i < MATRIX_SIZE; i++) {
			target[i] = matrix[i];
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
	public static boolean equals(int[] matrix1, int[] matrix2) {
		for (int i = 0; i < MATRIX_SIZE; i++) {
			if (matrix1[i] != matrix2[i])
				return false;
		}
		return true;
	}

	/**
	 * Returns a string containing the data of the given matrix.
	 * 
	 * @param matrix
	 * @return
	 */
	public static String toString(int[] matrix) {
		String result = "";
		for (int i = 0; i < MATRIX_ROW_SIZE; i++) {
			result += '|';
			for (int j = 0; j < MATRIX_COLUMN_SIZE; j++) {
				result += FixedPointMath.toFloat(get(matrix, j, i)) + ",";
			}
			result += "|\n";
		}
		return result;
	}
}
