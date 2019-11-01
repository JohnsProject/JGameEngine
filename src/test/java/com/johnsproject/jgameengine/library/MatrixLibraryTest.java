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
	
	@Test
	public void multiplyTest2() throws Exception {
		MatrixLibrary matrixLibrary = new MatrixLibrary();
		int[] out = matrixLibrary.generate();
		int[] matrix1 = matrixLibrary.generate();
		matrixLibrary.copy(matrix1, MatrixLibrary.MATRIX_IDENTITY);
		matrixLibrary.rotateX(matrix1, 50 << MathLibrary.FP_BIT, out);
		System.out.println(matrixLibrary.toString(out));
		matrixLibrary.copy(matrix1, MatrixLibrary.MATRIX_IDENTITY);
		matrixLibrary.rotateY(matrix1, 50 << MathLibrary.FP_BIT, out);
		System.out.println(matrixLibrary.toString(out));
		matrixLibrary.copy(matrix1, MatrixLibrary.MATRIX_IDENTITY);
		matrixLibrary.rotateZ(matrix1, 50 << MathLibrary.FP_BIT, out);
		System.out.println(matrixLibrary.toString(out));
	}
	
//	@Test
//	public void inverseTest() throws Exception {
//		MatrixLibrary matrixLibrary = new MatrixLibrary();
//		int[] out = new int[16];
//		int[] matrix1 = matrixLibrary.generate();
//		matrixLibrary.set(matrix1, 3, 0, 4096);
//		matrixLibrary.set(matrix1, 1, 1, 4096 * 2);
//		matrixLibrary.set(matrix1, 0, 2, 4096 * 3);
//		matrixLibrary.set(matrix1, 2, 3, 4096);
//		int[] matrix2 = matrixLibrary.generate();
//		matrixLibrary.inverse(matrix1, matrix2);
//		matrixLibrary.multiply(matrix1, matrix2, out);
//		System.out.println(matrixLibrary.toString(matrix1));
//		System.out.println(matrixLibrary.toString(matrix2));
//		System.out.println(matrixLibrary.toString(out));
//		assert(matrixLibrary.equals(out, MatrixLibrary.MATRIX_IDENTITY));
//	}
}
