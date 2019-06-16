package com.johnsproject.jgameengine.library;

import org.junit.Test;

import com.johnsproject.jgameengine.library.MatrixLibrary;

public class MatrixLibraryTest {
	
	@Test
	public void multiplyTest() throws Exception {
		MatrixLibrary matrixLibrary = new MatrixLibrary();
		int[] out = new int[16];
		int[] matrix1 = matrixLibrary.generate();
		matrixLibrary.set(matrix1, 3, 0, 4096);
		matrixLibrary.set(matrix1, 3, 1, 4096 * 2);
		matrixLibrary.set(matrix1, 3, 2, 4096 * 3);
		matrixLibrary.set(matrix1, 3, 3, 4096);
		int[] matrix2 = matrixLibrary.generate();
		matrixLibrary.set(matrix2, 0, 0, 4096 * 2);
		matrixLibrary.set(matrix2, 1, 1, 4096 * 2);
		matrixLibrary.set(matrix2, 2, 2, 4096 * 2);
		matrixLibrary.multiply(matrix1, matrix2, out);
		assert(matrixLibrary.get(out, 0, 0) == 4096 * 2);
		assert(matrixLibrary.get(out, 1, 1) == 4096 * 2);
		assert(matrixLibrary.get(out, 2, 2) == 4096 * 2);
		assert(matrixLibrary.get(out, 3, 3) == 4096);
		assert(matrixLibrary.get(out, 3, 0) == 4096);
		assert(matrixLibrary.get(out, 3, 1) == 4096 * 2);
		assert(matrixLibrary.get(out, 3, 2) == 4096 * 3);
	}
}
