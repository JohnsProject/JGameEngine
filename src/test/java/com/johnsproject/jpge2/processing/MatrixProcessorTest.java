package com.johnsproject.jpge2.processing;

import org.junit.Test;

public class MatrixProcessorTest {
	
	@Test
	public void addTest() throws Exception {
		int[][] out = new int[4][4];
		int[][] matrix1 = MatrixProcessor.generate();
		int[][] matrix2 = MatrixProcessor.generate();
		MatrixProcessor.add(matrix1, matrix2, out);
		assert(out[0][0] == 8192);
		assert(out[1][1] == 8192);
		assert(out[2][2] == 8192);
		assert(out[3][3] == 8192);
	}
	
	@Test
	public void multiplyTest() throws Exception {
		int val = MathProcessor.FP_VALUE;
		int[][] out = new int[4][4];
		int[][] matrix1 = new int[][] {
			{1 * val, 1 * val, 1 * val, 1 * val},
			{2 * val, 2 * val, 2 * val, 2 * val},
			{3 * val, 3 * val, 3 * val, 3 * val},
			{4 * val, 4 * val, 4 * val, 4 * val}
		};
		int[][] matrix2 = new int[][] {
			{1 * val, 4 * val, 1 * val, 4 * val},
			{2 * val, 3 * val, 2 * val, 3 * val},
			{3 * val, 2 * val, 3 * val, 2 * val},
			{4 * val, 1 * val, 4 * val, 1 * val}
		};
		int[][] result = new int[][] {
			{28 * val, 28 * val, 28 * val, 28 * val},
			{26 * val, 26 * val, 26 * val, 26 * val},
			{24 * val, 24 * val, 24 * val, 24 * val},
			{22 * val, 22 * val, 22 * val, 22 * val}
		};
		MatrixProcessor.multiply(matrix1, matrix2, out);
		assert(MatrixProcessor.equals(out, result));
	}

}
