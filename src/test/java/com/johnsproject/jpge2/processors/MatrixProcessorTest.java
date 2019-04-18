package com.johnsproject.jpge2.processors;

import org.junit.Test;

public class MatrixProcessorTest {
	
	@Test
	public void addTest() throws Exception {
		MatrixProcessor matrixProcessor = new MatrixProcessor(new MathProcessor());
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
		MatrixProcessor matrixProcessor = new MatrixProcessor(new MathProcessor());
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
	
	@Test
	public void translateTest() throws Exception {
		MatrixProcessor matrixProcessor = new MatrixProcessor(new MathProcessor());
		VectorProcessor vectorProcessor = new VectorProcessor(new MathProcessor());
		int[] out = vectorProcessor.generate();
		int[][] matrix = matrixProcessor.generate();
		matrixProcessor.translate(matrix, 2 << MathProcessor.FP_BITS, 3 << MathProcessor.FP_BITS, 4 << MathProcessor.FP_BITS, matrix);
		int[] vector = vectorProcessor.generate(4 << MathProcessor.FP_BITS, 3 << MathProcessor.FP_BITS, 2 << MathProcessor.FP_BITS);
		vectorProcessor.multiply(vector, matrix, out);
		assert(out[0] == 6 << MathProcessor.FP_BITS);
		assert(out[1] == 6 << MathProcessor.FP_BITS);
		assert(out[2] == 6 << MathProcessor.FP_BITS);
		assert(out[3] == 1024);
	}
	
	@Test
	public void scaleTest() throws Exception {
		MatrixProcessor matrixProcessor = new MatrixProcessor(new MathProcessor());
		VectorProcessor vectorProcessor = new VectorProcessor(new MathProcessor());
		int[] out = vectorProcessor.generate();
		int[][] matrix = matrixProcessor.generate();
		matrixProcessor.scale(matrix, 2, 3, 4, matrix);
		int[] vector = vectorProcessor.generate(1, 3, 5);
		vectorProcessor.multiply(vector, matrix, out);
		assert(out[0] == 2);
		assert(out[1] == 9);
		assert(out[2] == 20);
		assert(out[3] == 1024);
	}
	
	@Test
	public void rotateXTest() throws Exception {
		MatrixProcessor matrixProcessor = new MatrixProcessor(new MathProcessor());
		VectorProcessor vectorProcessor = new VectorProcessor(new MathProcessor());
		int[] out = vectorProcessor.generate();
		int[][] matrix = matrixProcessor.generate();
		matrixProcessor.rotateX(matrix, 30, matrix);
		int[] vector = vectorProcessor.generate(100, 100, 100);
		vectorProcessor.multiply(vector, matrix, out);
		assert(out[0] == 100);
		assert(out[1] == 37);
		assert(out[2] == 137);
		assert(out[3] == 1024);
	}
	
	@Test
	public void rotateYTest() throws Exception {
		MatrixProcessor matrixProcessor = new MatrixProcessor(new MathProcessor());
		VectorProcessor vectorProcessor = new VectorProcessor(new MathProcessor());
		int[] out = vectorProcessor.generate();
		int[][] matrix = matrixProcessor.generate();
		matrixProcessor.rotateY(matrix, 30, matrix);
		int[] vector = vectorProcessor.generate(100, 100, 100);
		vectorProcessor.multiply(vector, matrix, out);
		assert(out[0] == 137);
		assert(out[1] == 100);
		assert(out[2] == 37);
		assert(out[3] == 1024);
	}
	
	@Test
	public void rotateZTest() throws Exception {
		MatrixProcessor matrixProcessor = new MatrixProcessor(new MathProcessor());
		VectorProcessor vectorProcessor = new VectorProcessor(new MathProcessor());
		int[] out = vectorProcessor.generate();
		int[][] matrix = matrixProcessor.generate();
		matrixProcessor.rotateZ(matrix, 30, matrix);
		int[] vector = vectorProcessor.generate(100, 100, 100);
		vectorProcessor.multiply(vector, matrix, out);
		assert(out[0] == 37);
		assert(out[1] == 137);
		assert(out[2] == 100);
		assert(out[3] == 1024);
	}
}
