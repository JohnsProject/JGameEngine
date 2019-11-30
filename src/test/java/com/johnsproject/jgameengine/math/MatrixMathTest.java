package com.johnsproject.jgameengine.math;


import static com.johnsproject.jgameengine.math.MatrixMath.MATRIX_SIZE;

import org.junit.Test;

import com.johnsproject.jgameengine.math.FixedPointMath;
import com.johnsproject.jgameengine.math.MatrixMath;

public class MatrixMathTest {
	
	@Test
	public void matrixValueOperationsTest() throws Exception {
		// 255 because 256 * 256 = 65536 and will cause overflow of integer part of fixed point
		for (int i = 1; i < 256; i++) {
			double precision = 0.000000000000000000000000000000000001;
			double value1 = i;
			int fpValue1 = FixedPointMath.toFixedPoint(i);
			int[][] fpMatrix1 = MatrixMath.indentityMatrix();
			// fill matrix
			MatrixMath.add(fpMatrix1, fpValue1);
			double[][] matrix1 = toMatrix(fpMatrix1);
			// matrix add
			MatrixMath.add(fpMatrix1, fpValue1);
			add(matrix1, value1);
			assertMatrix(matrix1, toMatrix(fpMatrix1), precision);
			// matrix subtract
			MatrixMath.subtract(fpMatrix1, fpValue1);
			subtract(matrix1, value1);
			assertMatrix(matrix1, toMatrix(fpMatrix1), precision);
			// matrix multiply
			MatrixMath.multiply(fpMatrix1, fpValue1);
			multiply(matrix1, value1);
			assertMatrix(matrix1, toMatrix(fpMatrix1), precision);
			// matrix divide
			MatrixMath.divide(fpMatrix1, fpValue1);
			divide(matrix1, value1);
			assertMatrix(matrix1, toMatrix(fpMatrix1), precision);
		}
	}
	
	@Test
	public void matrixMatrixOperationsTest() throws Exception {
		// 127 because 128 * 128 = 16384 and 16384 * 4 = 65536 will cause overflow of integer part of fixed point
		for (int i = 1; i < 128; i++) {
			double precision = 0.0001;
			int fpValue = FixedPointMath.toFixedPoint(i);
			int[][] fpMatrix1 = MatrixMath.indentityMatrix();
			int[][] fpMatrix2 = MatrixMath.indentityMatrix();
			int[][] fpResult = MatrixMath.indentityMatrix();
			// fill matrix
			MatrixMath.add(fpMatrix1, fpValue);
			MatrixMath.add(fpMatrix2, fpValue);
			double[][] matrix1 = toMatrix(fpMatrix1);
			double[][] matrix2 = toMatrix(fpMatrix2);
			double[][] result = toMatrix(fpResult);
			// matrix add
			MatrixMath.add(fpMatrix1, fpMatrix2, fpResult);
			result = add(matrix1, matrix2);
			assertMatrix(result, toMatrix(fpResult), precision);
			// matrix subtract
			MatrixMath.subtract(fpMatrix1, fpMatrix2, fpResult);
			result = subtract(matrix1, matrix2);
			assertMatrix(result, toMatrix(fpResult), precision);
			// matrix multiply
			MatrixMath.multiply(fpMatrix1, fpMatrix2, fpResult);
			result = multiply(matrix1, matrix2);
			assertMatrix(result, toMatrix(fpResult), precision);
			// matrix divide
			MatrixMath.divide(fpMatrix1, fpMatrix2, fpResult);
			result = divide(matrix1, matrix2);
			assertMatrix(result, toMatrix(fpResult), precision);
		}
	}
	
	@Test
	public void determinantTest() throws Exception {
		for (int i = 1; i < 16; i++) {
			double precision = 0.000000001;
			int fpValue = FixedPointMath.toFixedPoint(i);
			int[][] fpMatrix1 = MatrixMath.indentityMatrix();
			fpMatrix1[0][0] = fpValue;
			fpMatrix1[1][1] = fpValue;
			fpMatrix1[2][2] = fpValue;
			fpMatrix1[3][3] = fpValue;
			double[][] matrix1 = toMatrix(fpMatrix1);
			double fpResult = MatrixMath.determinant(fpMatrix1);
			fpResult = FixedPointMath.toDouble((long)fpResult);
			double result = determinant(matrix1);
			assert((fpResult >= result - precision) && (fpResult <= result + precision));
		}
	}
	
	@Test
	public void inverseTest() throws Exception {
		for (int i = 1; i < 16; i++) {
			double precision = 0.0001;
			int fpValue = FixedPointMath.toFixedPoint(i);
			int[][] fpMatrix1 = MatrixMath.indentityMatrix();
			fpMatrix1[0][0] = fpValue;
			fpMatrix1[1][1] = fpValue;
			fpMatrix1[2][2] = fpValue;
			fpMatrix1[3][3] = fpValue;
			double[][] matrix1 = toMatrix(fpMatrix1);
			int[][] fpResult = MatrixMath.indentityMatrix();
			MatrixMath.inverse(fpMatrix1, fpResult);
			double[][] result = inverse(matrix1);
			assertMatrix(result, toMatrix(fpResult), precision);
		}
	}
	
	static void assertMatrix(double[][] matrix1, double[][] matrix2, double precision) {
		for (int i = 0; i < MATRIX_SIZE; i++) {
			for (int j = 0; j < MATRIX_SIZE; j++) {
				assert(matrix1[i][j] >= matrix2[i][j] - precision);
				assert(matrix1[i][j] <= matrix2[i][j] + precision);
			}
		}
	}
	
	public static String toString(double[][] matrix) {
		String result = "";
		for (int i = 0; i < MATRIX_SIZE; i++) {
			result += '|';
			for (int j = 0; j < MATRIX_SIZE; j++) {
				result += matrix[j][i] + ",";
			}
			result += "|\n";
		}
		return result;
	}
	
	static double[][] identityMatrix() {
		return new double[][] {
			{1d, 0, 0, 0},
			{0, 1d, 0, 0},
			{0, 0, 1d, 0},
			{0, 0, 0, 1d}
		};
	}
	
	static double[][] toMatrix(int[][] matrix) {
		double[][] result = new double[MATRIX_SIZE][MATRIX_SIZE];
		for (int i = 0; i < MATRIX_SIZE; i++) {
			for (int j = 0; j < MATRIX_SIZE; j++) {
				result[i][j] = FixedPointMath.toDouble(matrix[i][j]);
			}
		}
		return result;
	}
	
	static void add(double[][] matrix1, double value) {
		for (int i = 0; i < MATRIX_SIZE; i++) {
			for (int j = 0; j < MATRIX_SIZE; j++) {
				matrix1[i][j] += value;
			}
		}
	}
	
	static void subtract(double[][] matrix1, double value) {
		for (int i = 0; i < MATRIX_SIZE; i++) {
			for (int j = 0; j < MATRIX_SIZE; j++) {
				matrix1[i][j] -= value;
			}
		}
	}
	
	static void multiply(double[][] matrix1, double value) {
		for (int i = 0; i < MATRIX_SIZE; i++) {
			for (int j = 0; j < MATRIX_SIZE; j++) {
				matrix1[i][j] *= value;
			}
		}
	}
	
	static void divide(double[][] matrix1, double value) {
		for (int i = 0; i < MATRIX_SIZE; i++) {
			for (int j = 0; j < MATRIX_SIZE; j++) {
				matrix1[i][j] /= value;
			}
		}
	}
	
	static double[][] add(double[][] matrix1, double[][] matrix2) {
		double[][] result = new double[MATRIX_SIZE][MATRIX_SIZE];
		for (int i = 0; i < MATRIX_SIZE; i++) {
			for (int j = 0; j < MATRIX_SIZE; j++) {
				double res = matrix1[0][j] + matrix2[i][0];
				res += matrix1[1][j] + matrix2[i][1];
				res += matrix1[2][j] + matrix2[i][2];
				res += matrix1[3][j] + matrix2[i][3];
				result[i][j] = res;
			}
		}
		return result;
	}
	
	static double[][] subtract(double[][] matrix1, double[][] matrix2) {
		double[][] result = new double[MATRIX_SIZE][MATRIX_SIZE];
		for (int i = 0; i < MATRIX_SIZE; i++) {
			for (int j = 0; j < MATRIX_SIZE; j++) {
				double res = matrix1[0][j] - matrix2[i][0];
				res += matrix1[1][j] - matrix2[i][1];
				res += matrix1[2][j] - matrix2[i][2];
				res += matrix1[3][j] - matrix2[i][3];
				result[i][j] = res;
			}
		}
		return result;
	}
	
	static double[][] multiply(double[][] matrix1, double[][] matrix2) {
		double[][] result = new double[MATRIX_SIZE][MATRIX_SIZE];
		for (int i = 0; i < MATRIX_SIZE; i++) {
			for (int j = 0; j < MATRIX_SIZE; j++) {
				double res = matrix1[0][j] * matrix2[i][0];
				res += matrix1[1][j] * matrix2[i][1];
				res += matrix1[2][j] * matrix2[i][2];
				res += matrix1[3][j] * matrix2[i][3];
				result[i][j] = res;
			}
		}
		return result;
	}
	
	static double[][] divide(double[][] matrix1, double[][] matrix2) {
		double[][] result = new double[MATRIX_SIZE][MATRIX_SIZE];
		for (int i = 0; i < MATRIX_SIZE; i++) {
			for (int j = 0; j < MATRIX_SIZE; j++) {
				double res = matrix1[0][j] / matrix2[i][0];
				res += matrix1[1][j] / matrix2[i][1];
				res += matrix1[2][j] / matrix2[i][2];
				res += matrix1[3][j] / matrix2[i][3];
				result[i][j] = res;
			}
		}
		return result;
	}
	
	static double determinant(double[][] matrix) {
		return	matrix[3][0] * matrix[2][1] * matrix[1][2] * matrix[0][3] - 
				matrix[2][0] * matrix[3][1] * matrix[1][2] * matrix[0][3] -
				matrix[3][0] * matrix[1][1] * matrix[2][2] * matrix[0][3] + 
				matrix[1][0] * matrix[3][1] * matrix[2][2] * matrix[0][3] +
				matrix[2][0] * matrix[1][1] * matrix[3][2] * matrix[0][3] - 
				matrix[1][0] * matrix[2][1] * matrix[3][2] * matrix[0][3] -
				matrix[3][0] * matrix[2][1] * matrix[0][2] * matrix[1][3] + 
				matrix[2][0] * matrix[3][1] * matrix[0][2] * matrix[1][3] +
				matrix[3][0] * matrix[0][1] * matrix[2][2] * matrix[1][3] - 
				matrix[0][0] * matrix[3][1] * matrix[2][2] * matrix[1][3] -
				matrix[2][0] * matrix[0][1] * matrix[3][2] * matrix[1][3] + 
				matrix[0][0] * matrix[2][1] * matrix[3][2] * matrix[1][3] +
				matrix[3][0] * matrix[1][1] * matrix[0][2] * matrix[2][3] - 
				matrix[1][0] * matrix[3][1] * matrix[0][2] * matrix[2][3] -
				matrix[3][0] * matrix[0][1] * matrix[1][2] * matrix[2][3] + 
				matrix[0][0] * matrix[3][1] * matrix[1][2] * matrix[2][3] +
				matrix[1][0] * matrix[0][1] * matrix[3][2] * matrix[2][3] - 
				matrix[0][0] * matrix[1][1] * matrix[3][2] * matrix[2][3] -
				matrix[2][0] * matrix[1][1] * matrix[0][2] * matrix[3][3] + 
				matrix[1][0] * matrix[2][1] * matrix[0][2] * matrix[3][3] +
				matrix[2][0] * matrix[0][1] * matrix[1][2] * matrix[3][3] - 
				matrix[0][0] * matrix[2][1] * matrix[1][2] * matrix[3][3] -
				matrix[1][0] * matrix[0][1] * matrix[2][2] * matrix[3][3] + 
				matrix[0][0] * matrix[1][1] * matrix[2][2] * matrix[3][3];
	}
	
	static double[][] inverse(double[][] matrix) {
		double[][] result = identityMatrix();
		double determinant = determinant(matrix);
		result[0][0] = matrix[2][1] * matrix[3][2] * matrix[1][3] -
						matrix[3][1] * matrix[2][2] * matrix[1][3] +
						matrix[3][1] * matrix[1][2] * matrix[2][3] -
						matrix[1][1] * matrix[3][2] * matrix[2][3] -
						matrix[2][1] * matrix[1][2] * matrix[3][3] +
						matrix[1][1] * matrix[2][2] * matrix[3][3];
		result[1][0] = matrix[3][0] * matrix[2][2] * matrix[1][3] -
						matrix[2][0] * matrix[2][3] * matrix[1][3] -
						matrix[3][0] * matrix[2][1] * matrix[2][3] +
						matrix[1][0] * matrix[2][3] * matrix[2][3] +
						matrix[2][0] * matrix[2][1] * matrix[3][3] -
						matrix[1][0] * matrix[2][2] * matrix[3][3];
		result[2][0] = matrix[2][0] * matrix[3][1] * matrix[1][3] -
						matrix[3][0] * matrix[2][1] * matrix[1][3] +
						matrix[3][0] * matrix[1][1] * matrix[2][3] -
						matrix[1][0] * matrix[3][1] * matrix[2][3] -
						matrix[2][0] * matrix[1][1] * matrix[3][3] +
						matrix[1][0] * matrix[2][1] * matrix[3][3];
		result[3][0] = matrix[3][0] * matrix[2][1] * matrix[1][2] -
						matrix[2][0] * matrix[3][1] * matrix[1][2] -
						matrix[3][0] * matrix[1][1] * matrix[2][2] +
						matrix[1][0] * matrix[3][1] * matrix[2][2] +
						matrix[2][0] * matrix[1][1] * matrix[3][2] -
						matrix[1][0] * matrix[2][1] * matrix[3][2];
		result[0][1] = matrix[3][1] * matrix[2][2] * matrix[0][3] -
						matrix[2][1] * matrix[3][2] * matrix[0][3] -
						matrix[3][1] * matrix[0][2] * matrix[2][3] +
						matrix[0][1] * matrix[3][2] * matrix[2][3] +
						matrix[2][1] * matrix[0][2] * matrix[3][3] -
						matrix[0][1] * matrix[2][2] * matrix[3][3];
		result[1][1] = matrix[2][0] * matrix[3][2] * matrix[0][3] -
						matrix[3][0] * matrix[2][2] * matrix[0][3] +
						matrix[3][0] * matrix[0][2] * matrix[2][3] -
						matrix[0][0] * matrix[3][2] * matrix[2][3] -
						matrix[2][0] * matrix[0][2] * matrix[3][3] +
						matrix[0][0] * matrix[2][2] * matrix[3][3];
		result[2][1] = matrix[3][0] * matrix[2][1] * matrix[0][3] -
						matrix[2][0] * matrix[3][1] * matrix[0][3] -
						matrix[3][0] * matrix[0][1] * matrix[2][3] +
						matrix[0][0] * matrix[3][1] * matrix[2][3] +
						matrix[2][0] * matrix[0][1] * matrix[3][3] -
						matrix[0][0] * matrix[2][1] * matrix[3][3];
		result[3][1] = matrix[2][0] * matrix[3][1] * matrix[0][2] -
						matrix[3][0] * matrix[2][1] * matrix[0][2] +
						matrix[3][0] * matrix[0][1] * matrix[2][2] -
						matrix[0][0] * matrix[3][1] * matrix[2][2] -
						matrix[2][0] * matrix[0][1] * matrix[3][2] +
						matrix[0][0] * matrix[2][1] * matrix[3][2];
		result[0][2] = matrix[1][1] * matrix[3][2] * matrix[0][3] -
						matrix[3][1] * matrix[1][2] * matrix[0][3] +
						matrix[3][1] * matrix[0][2] * matrix[1][3] -
						matrix[0][1] * matrix[3][2] * matrix[1][3] -
						matrix[1][1] * matrix[0][2] * matrix[3][3] +
						matrix[0][1] * matrix[1][2] * matrix[3][3];
		result[1][2] = matrix[3][0] * matrix[1][2] * matrix[0][3] -
						matrix[1][0] * matrix[3][2] * matrix[0][3] -
						matrix[3][0] * matrix[0][2] * matrix[1][3] +
						matrix[0][0] * matrix[3][2] * matrix[1][3] +
						matrix[1][0] * matrix[0][2] * matrix[3][3] -
						matrix[0][0] * matrix[1][2] * matrix[3][3];
		result[2][2] = matrix[1][0] * matrix[3][1] * matrix[0][3] -
						matrix[3][0] * matrix[1][1] * matrix[0][3] +
						matrix[3][0] * matrix[0][1] * matrix[1][3] -
						matrix[0][0] * matrix[3][1] * matrix[1][3] -
						matrix[1][0] * matrix[0][1] * matrix[3][3] +
						matrix[0][0] * matrix[1][1] * matrix[3][3];
		result[3][2] = matrix[3][0] * matrix[1][1] * matrix[0][2] -
						matrix[1][0] * matrix[3][1] * matrix[0][2] -
						matrix[3][0] * matrix[0][1] * matrix[1][2] +
						matrix[0][0] * matrix[3][1] * matrix[1][2] +
						matrix[1][0] * matrix[0][1] * matrix[3][2] -
						matrix[0][0] * matrix[1][1] * matrix[3][2];
		result[0][3] = matrix[2][1] * matrix[1][2] * matrix[0][3] -
						matrix[1][1] * matrix[2][2] * matrix[0][3] -
						matrix[2][1] * matrix[0][2] * matrix[1][3] +
						matrix[0][1] * matrix[2][2] * matrix[1][3] +
						matrix[1][1] * matrix[0][2] * matrix[2][3] -
						matrix[0][1] * matrix[1][2] * matrix[2][3];
		result[1][3] = matrix[1][0] * matrix[2][2] * matrix[0][3] -
						matrix[2][0] * matrix[1][2] * matrix[0][3] +
						matrix[2][0] * matrix[0][2] * matrix[1][3] -
						matrix[0][0] * matrix[2][2] * matrix[1][3] -
						matrix[1][0] * matrix[0][2] * matrix[2][3] +
						matrix[0][0] * matrix[1][2] * matrix[2][3];
		result[2][3] = matrix[2][0] * matrix[1][1] * matrix[0][3] -
						matrix[1][0] * matrix[2][1] * matrix[0][3] -
						matrix[2][0] * matrix[0][1] * matrix[1][3] +
						matrix[0][0] * matrix[2][1] * matrix[1][3] +
						matrix[1][0] * matrix[0][1] * matrix[2][3] -
						matrix[0][0] * matrix[1][1] * matrix[2][3];
		result[3][3] = matrix[1][0] * matrix[2][1] * matrix[0][2] -
						matrix[2][0] * matrix[1][1] * matrix[0][2] +
						matrix[2][0] * matrix[0][1] * matrix[1][2] -
						matrix[0][0] * matrix[2][1] * matrix[1][2] -
						matrix[1][0] * matrix[0][1] * matrix[2][2] +
						matrix[0][0] * matrix[1][1] * matrix[2][2];
		divide(result, determinant);
		return result;
	}
}
