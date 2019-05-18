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
package com.johnsproject.jpge2.library;

public class MatrixLibrary {

	private static final byte FP_BITS = MathLibrary.FP_BITS;
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
	 * Sets out equals the result of the addition of matrix1 and matrix2.
	 * 
	 * @param matrix1
	 * @param matrix2
	 * @param out
	 */
	public int[][] add(int[][] matrix1, int[][] matrix2, int[][] out) {
		// ensures that will return right values if matrix or matrix two is the same as out
		copy(matrixCache1, matrix1);
		copy(matrixCache2, matrix2);
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 4; j++) {
				int result = matrixCache1[0][j] + matrixCache2[i][0];
				result += matrixCache1[1][j] + matrixCache2[i][1];
				result += matrixCache1[2][j] + matrixCache2[i][2];
				result += matrixCache1[3][j] + matrixCache2[i][3];
				out[i][j] = result;
			}
		}
		return out;
	}

	/**
	 * Sets out equals the result of the subtraction of matrix1 and matrix2.
	 * 
	 * @param matrix1
	 * @param matrix2
	 * @param out
	 */
	public int[][] subtract(int[][] matrix1, int[][] matrix2, int[][] out) {
		// ensures that will return right values if matrix or matrix two is the same as out
		copy(matrixCache1, matrix1);
		copy(matrixCache2, matrix2);
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 4; j++) {
				int result = matrixCache1[0][j] - matrixCache2[i][0];
				result += matrixCache1[1][j] - matrixCache2[i][1];
				result += matrixCache1[2][j] - matrixCache2[i][2];
				result += matrixCache1[3][j] - matrixCache2[i][3];
				out[i][j] = result;
			}
		}
		return out;
	}

	/**
	 * Sets out equals the result of the multiplication of matrix1 and matrix2.
	 * 
	 * @param matrix1
	 * @param matrix2
	 * @param out
	 */
	public int[][] multiply(int[][] matrix1, int[][] matrix2, int[][] out) {
		// ensures that will return right values if matrix or matrix two is the same as out
		copy(matrixCache1, matrix1);
		copy(matrixCache2, matrix2);
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 4; j++) {
				long result = (long) matrixCache1[0][j] * matrixCache2[i][0];
				result += (long) matrixCache1[1][j] * matrixCache2[i][1];
				result += (long) matrixCache1[2][j] * matrixCache2[i][2];
				result += (long) matrixCache1[3][j] * matrixCache2[i][3];
				out[i][j] = (int) (result >> FP_BITS);
			}
		}
		return out;
	}

	/**
	 * Sets out equals the result of the division of matrix1 and matrix2.
	 * 
	 * @param matrix1
	 * @param matrix2
	 * @param out
	 */
	public int[][] divide(int[][] matrix1, int[][] matrix2, int[][] out) {
		// ensures that will return right values if matrix or matrix two is the same as out
		copy(matrixCache1, matrix1);
		copy(matrixCache2, matrix2);
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 4; j++) {
				int result = mathLibrary.divide(matrixCache1[0][j], matrixCache2[i][0]);
				result += mathLibrary.divide(matrixCache1[1][j], matrixCache2[i][1]);
				result += mathLibrary.divide(matrixCache1[2][j], matrixCache2[i][2]);
				result += mathLibrary.divide(matrixCache1[3][j], matrixCache2[i][3]);
				out[i][j] = result;
			}
		}
		return out;
	}

	/**
	 * Sets out equals the translated matrix.
	 *
	 * @param matrix
	 * @param x
	 * @param y
	 * @param z
	 * @param out
	 */
	public int[][] translate(int[][] matrix, int[] vector, int[][] out) {
		copy(matrixCache1, MATRIX_IDENTITY);
		matrixCache1[3][0] = vector[VECTOR_X];
		matrixCache1[3][1] = vector[VECTOR_Y];
		matrixCache1[3][2] = vector[VECTOR_Z];
		multiply(matrixCache1, matrix, out);
		return out;
	}

	/**
	 * Sets out equals the scaled matrix.
	 *
	 * @param matrix
	 * @param x
	 * @param y
	 * @param z
	 * @param out
	 */
	public int[][] scale(int[][] matrix, int[] vector, int[][] out) {
		copy(matrixCache1, MATRIX_IDENTITY);
		matrixCache1[0][0] = vector[VECTOR_X];
		matrixCache1[1][1] = vector[VECTOR_Y];
		matrixCache1[2][2] = vector[VECTOR_Z];
		multiply(matrixCache1, matrix, out);
		return out;
	}

	/**
	 * Sets out equals the matrix rotated around (0, 0, 0) at x axis by the given
	 * angle.
	 *
	 * @param matrix
	 * @param angle
	 * @param out
	 */
	public int[][] rotateX(int[][] matrix, int angle, int[][] out) {
		copy(matrixCache1, MATRIX_IDENTITY);
		int cos = mathLibrary.cos(angle);
		int sin = mathLibrary.sin(angle);
		matrixCache1[1][1] = cos;
		matrixCache1[1][2] = sin;
		matrixCache1[2][1] = -sin;
		matrixCache1[2][2] = cos;
		multiply(matrixCache1, matrix, out);
		return out;
	}

	/**
	 * Sets out equals the matrix rotated around (0, 0, 0) at y axis by the given
	 * angle.
	 *
	 * @param matrix
	 * @param angle
	 * @param out
	 */
	public int[][] rotateY(int[][] matrix, int angle, int[][] out) {
		copy(matrixCache1, MATRIX_IDENTITY);
		int cos = mathLibrary.cos(-angle);
		int sin = mathLibrary.sin(-angle);
		matrixCache1[0][0] = cos;
		matrixCache1[0][2] = -sin;
		matrixCache1[2][0] = sin;
		matrixCache1[2][2] = cos;
		multiply(matrixCache1, matrix, out);
		return out;
	}

	/**
	 * Sets out equals the matrix rotated around (0, 0, 0) at z axis by the given
	 * angle.
	 *
	 * @param matrix
	 * @param angle
	 * @param out
	 */
	public int[][] rotateZ(int[][] matrix, int angle, int[][] out) {
		copy(matrixCache1, MATRIX_IDENTITY);
		int cos = mathLibrary.cos(-angle);
		int sin = mathLibrary.sin(-angle);
		matrixCache1[0][0] = cos;
		matrixCache1[0][1] = sin;
		matrixCache1[1][0] = -sin;
		matrixCache1[1][1] = cos;
		multiply(matrixCache1, matrix, out);
		return out;
	}

	/**
	 * Sets out equals the matrix rotated around (0, 0, 0) at x, y and z axis by the
	 * given angles.
	 * 
	 * @param vector
	 * @param angles
	 * @param out
	 * @return
	 */
	public int[][] rotateXYZ(int[][] vector, int[] angles, int[][] out) {
		rotateX(vector, angles[VECTOR_X], out);
		rotateY(out, angles[VECTOR_Y], out);
		rotateZ(out, angles[VECTOR_Z], out);
		return out;
	}

	/**
	 * Sets out equals the matrix rotated around (0, 0, 0) at z, y and x axis by the
	 * given angles.
	 * 
	 * @param vector
	 * @param angles
	 * @param out
	 * @return
	 */
	public int[][] rotateZYX(int[][] vector, int[] angles, int[][] out) {
		rotateZ(vector, angles[VECTOR_Z], out);
		rotateY(out, angles[VECTOR_Y], out);
		rotateX(out, angles[VECTOR_X], out);
		return out;
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
