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
package com.johnsproject.jpge2.processors;

/**
 * @author schnu
 *
 */
/**
 * @author schnu
 *
 */
public class MatrixProcessor {

	public static final int[][] IDENTITY = MatrixProcessor.generate();
	
	private static final int[][] matrixCache1 = MatrixProcessor.generate();
	private static final int[][] matrixCache2 = MatrixProcessor.generate();
	
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
	public static int[][] add(int[][] matrix1, int[][] matrix2, int[][] out) {
		// ensures that will return right values if matrix or matrix two is the same as out
		copy(matrixCache1, matrix1);
		copy(matrixCache2, matrix2);
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 4; j++) {
				out[i][j] = matrixCache1[i][j] + matrixCache2[i][j];
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
	public static int[][] multiply(int[][] matrix1, int[][] matrix2, int[][] out) {
		// ensures that will return right values if matrix or matrix two is the same as out
		copy(matrixCache1, matrix1);
		copy(matrixCache2, matrix2);
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 4; j++) {
				long result = (long)matrixCache1[0][j] * matrixCache2[i][0];
				result += (long)matrixCache1[1][j] * matrixCache2[i][1];
				result += (long)matrixCache1[2][j] * matrixCache2[i][2];
				result += (long)matrixCache1[3][j] * matrixCache2[i][3];
				out[i][j] = (int)MathProcessor.multiply(result, 1);
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
	public static int[][] translate(int[][] matrix, int x, int y, int z, int[][] out) {
		copy(matrixCache1, IDENTITY);
		matrixCache1[3][0] = x;
		matrixCache1[3][1] = y;
		matrixCache1[3][2] = z;
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
	public static int[][] scale(int[][] matrix, int x, int y, int z, int[][] out) {
		copy(matrixCache1, IDENTITY);
		matrixCache1[0][0] = x << MathProcessor.FP_SHIFT;
		matrixCache1[1][1] = y << MathProcessor.FP_SHIFT;
		matrixCache1[2][2] = z << MathProcessor.FP_SHIFT;
		multiply(matrixCache1, matrix, out);
		return out;
	}

	/**
	 * Sets out equals the x axis rotated matrix. 
	 *
	 * @param matrix
	 * @param angle
	 * @param out
	 */
	public static int[][] rotateX(int[][] matrix, int angle, int[][] out) {
		copy(matrixCache1, IDENTITY);
		int cos = MathProcessor.cos(angle);
		int sin = MathProcessor.sin(angle);
		matrixCache1[1][1] = cos;
		matrixCache1[1][2] = sin;
		matrixCache1[2][1] = -sin;
		matrixCache1[2][2] = cos;
		multiply(matrixCache1, matrix, out);
		return out;
	}
	
	/**
	 * Sets out equals the y axis rotated matrix. 
	 *
	 * @param matrix
	 * @param angle
	 * @param out
	 */
	public static int[][] rotateY(int[][] matrix, int angle, int[][] out) {
		copy(matrixCache1, IDENTITY);
		int cos = MathProcessor.cos(angle);
		int sin = MathProcessor.sin(angle);
		matrixCache1[0][0] = cos;
		matrixCache1[0][2] = -sin;
		matrixCache1[2][0] = sin;
		matrixCache1[2][2] = cos;
		multiply(matrixCache1, matrix, out);
		return out;
	}

	/**
	 * Sets out equals the z axis rotated matrix. 
	 *
	 * @param matrix
	 * @param angle
	 * @param out
	 */
	public static int[][] rotateZ(int[][] matrix, int angle, int[][] out) {
		copy(matrixCache1, IDENTITY);
		int cos = MathProcessor.cos(angle);
		int sin = MathProcessor.sin(angle);
		matrixCache1[0][0] = cos;
		matrixCache1[0][1] = sin;
		matrixCache1[1][0] = -sin;
		matrixCache1[1][1] = cos;
		multiply(matrixCache1, matrix, out);
		return out;
	}
	
	/**
	 * Copies the value of matrix to the target.
	 * 
	 * @param target
	 * @param matrix
	 */
	public static int[][] copy(int[][] target, int[][] matrix) {
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
