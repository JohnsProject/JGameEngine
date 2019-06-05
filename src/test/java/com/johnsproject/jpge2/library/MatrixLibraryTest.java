package com.johnsproject.jpge2.library;

import org.junit.Test;

import com.johnsproject.jpge2.library.MatrixLibrary;

public class MatrixLibraryTest {
	
	@Test
	public void addTest() throws Exception {
		MatrixLibrary matrixProcessor = new MatrixLibrary();
		int[][] out = new int[4][4];
		int[][] matrix1 = matrixProcessor.generate();
		int[][] matrix2 = matrixProcessor.generate();
		matrixProcessor.add(matrix1, matrix2, out);
		assert(out[0][0] == 2048);
		assert(out[1][1] == 2048);
		assert(out[2][2] == 2048);
		assert(out[3][3] == 2048);
	}
	
	@Test
	public void multiplyTest() throws Exception {
		MatrixLibrary matrixProcessor = new MatrixLibrary();
		int[][] out = new int[4][4];
		int[][] matrix1 = matrixProcessor.generate();
		matrix1[3][0] = 4096;
		matrix1[3][1] = 4096 * 2;
		matrix1[3][2] = 4096 * 3;
		matrix1[3][3] = 4096;
		int[][] matrix2 = matrixProcessor.generate();
		matrix2[0][0] = 4096 * 2;
		matrix2[1][1] = 4096 * 2;
		matrix2[2][2] = 4096 * 2;
		matrixProcessor.multiply(matrix1, matrix2, out);
		assert(out[0][0] == 4096 * 2);
		assert(out[1][1] == 4096 * 2);
		assert(out[2][2] == 4096 * 2);
		assert(out[3][3] == 4096);
		assert(out[3][0] == 4096);
		assert(out[3][1] == 4096 * 2);
		assert(out[3][2] == 4096 * 3);
	}
}
