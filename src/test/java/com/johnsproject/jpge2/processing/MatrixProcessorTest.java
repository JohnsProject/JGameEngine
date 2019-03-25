package com.johnsproject.jpge2.processing;

import org.junit.Test;

public class MatrixProcessorTest {
	
	@Test
	public void addTest() throws Exception {
		long[][] out = new long[4][4];
		long[][] matrix1 = MatrixProcessor.generate();
		long[][] matrix2 = MatrixProcessor.generate();
		MatrixProcessor.add(matrix1, matrix2, out);
		assert(out[0][0] == 2048);
		assert(out[1][1] == 2048);
		assert(out[2][2] == 2048);
		assert(out[3][3] == 2048);
	}
	
	@Test
	public void multiplyTest() throws Exception {
		long[][] out = new long[4][4];
		long[][] matrix1 = MatrixProcessor.generate();
		matrix1[3][0] = 4096;
		matrix1[3][1] = 4096 * 2;
		matrix1[3][2] = 4096 * 3;
		matrix1[3][3] = 4096;
		long[][] matrix2 = MatrixProcessor.generate();
		matrix2[0][0] = 4096 * 2;
		matrix2[1][1] = 4096 * 2;
		matrix2[2][2] = 4096 * 2;
		MatrixProcessor.multiply(matrix1, matrix2, out);
		assert(out[0][0] == 4096 * 2);
		assert(out[1][1] == 4096 * 2);
		assert(out[2][2] == 4096 * 2);
		assert(out[3][3] == 4096);
		assert(out[3][0] == 4096);
		assert(out[3][1] == 4096 * 2);
		assert(out[3][2] == 4096 * 3);
	}
	
	@Test
	public void translateTest() throws Exception {
		long[] out = VectorProcessor.generate();
		long[][] matrix = MatrixProcessor.generate();
		MatrixProcessor.translate(matrix, 2, 3, 4);
		long[] vector = VectorProcessor.generate();
		VectorProcessor.multiply(vector, matrix, out);
		assert(out[0] == 2);
		assert(out[1] == 3);
		assert(out[2] == 4);
		assert(out[3] == 1);
	}
	
	@Test
	public void scaleTest() throws Exception {
		long[] out = VectorProcessor.generate();
		long[][] matrix = MatrixProcessor.generate();
		MatrixProcessor.scale(matrix, 2, 3, 4);
		long[] vector = VectorProcessor.generate(1, 3, 5);
		VectorProcessor.multiply(vector, matrix, out);
		assert(out[0] == 2);
		assert(out[1] == 9);
		assert(out[2] == 20);
		assert(out[3] == 1);
	}
	
	@Test
	public void rotateXTest() throws Exception {
		long[] out = VectorProcessor.generate();
		long[][] matrix = MatrixProcessor.generate();
		MatrixProcessor.rotateX(matrix, 30);
		long[] vector = VectorProcessor.generate(100, 100, 100);
		VectorProcessor.multiply(vector, matrix, out);
		assert(out[0] == 100);
		assert(out[1] == 36);
		assert(out[2] == 136);
		assert(out[3] == 1);
	}
	
	@Test
	public void rotateYTest() throws Exception {
		long[] out = VectorProcessor.generate();
		long[][] matrix = MatrixProcessor.generate();
		MatrixProcessor.rotateY(matrix, 30);
		long[] vector = VectorProcessor.generate(100, 100, 100);
		VectorProcessor.multiply(vector, matrix, out);
		assert(out[0] == 136);
		assert(out[1] == 100);
		assert(out[2] == 36);
		assert(out[3] == 1);
	}
	
	@Test
	public void rotateZTest() throws Exception {
		long[] out = VectorProcessor.generate();
		long[][] matrix = MatrixProcessor.generate();
		MatrixProcessor.rotateZ(matrix, 30);
		long[] vector = VectorProcessor.generate(100, 100, 100);
		VectorProcessor.multiply(vector, matrix, out);
		assert(out[0] == 36);
		assert(out[1] == 136);
		assert(out[2] == 100);
		assert(out[3] == 1);
	}
}
