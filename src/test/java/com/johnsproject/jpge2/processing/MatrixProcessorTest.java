package com.johnsproject.jpge2.processing;

import org.junit.Test;

import com.johnsproject.jpge2.processors.MathProcessor;
import com.johnsproject.jpge2.processors.MatrixProcessor;
import com.johnsproject.jpge2.processors.VectorProcessor;

public class MatrixProcessorTest {
	
	@Test
	public void addTest() throws Exception {
		int[][] out = new int[4][4];
		int[][] matrix1 = MatrixProcessor.generate();
		int[][] matrix2 = MatrixProcessor.generate();
		MatrixProcessor.add(matrix1, matrix2, out);
		assert(out[0][0] == 2048);
		assert(out[1][1] == 2048);
		assert(out[2][2] == 2048);
		assert(out[3][3] == 2048);
	}
	
	@Test
	public void multiplyTest() throws Exception {
		int[][] out = new int[4][4];
		int[][] matrix1 = MatrixProcessor.generate();
		matrix1[3][0] = 4096;
		matrix1[3][1] = 4096 * 2;
		matrix1[3][2] = 4096 * 3;
		matrix1[3][3] = 4096;
		int[][] matrix2 = MatrixProcessor.generate();
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
		int[] out = VectorProcessor.generate();
		int[][] matrix = MatrixProcessor.generate();
		MatrixProcessor.translate(matrix, 2 << MathProcessor.FP_SHIFT, 3 << MathProcessor.FP_SHIFT, 4 << MathProcessor.FP_SHIFT, matrix);
		int[] vector = VectorProcessor.generate(4 << MathProcessor.FP_SHIFT, 3 << MathProcessor.FP_SHIFT, 2 << MathProcessor.FP_SHIFT);
		VectorProcessor.multiply(vector, matrix, out);
		assert(out[0] == 6 << MathProcessor.FP_SHIFT);
		assert(out[1] == 6 << MathProcessor.FP_SHIFT);
		assert(out[2] == 6 << MathProcessor.FP_SHIFT);
		assert(out[3] == 1024);
	}
	
	@Test
	public void scaleTest() throws Exception {
		int[] out = VectorProcessor.generate();
		int[][] matrix = MatrixProcessor.generate();
		MatrixProcessor.scale(matrix, 2, 3, 4, matrix);
		int[] vector = VectorProcessor.generate(1, 3, 5);
		VectorProcessor.multiply(vector, matrix, out);
		assert(out[0] == 2);
		assert(out[1] == 9);
		assert(out[2] == 20);
		assert(out[3] == 1024);
	}
	
	@Test
	public void rotateXTest() throws Exception {
		int[] out = VectorProcessor.generate();
		int[][] matrix = MatrixProcessor.generate();
		MatrixProcessor.rotateX(matrix, 30, matrix);
		int[] vector = VectorProcessor.generate(100, 100, 100);
		VectorProcessor.multiply(vector, matrix, out);
		assert(out[0] == 100);
		assert(out[1] == 37);
		assert(out[2] == 137);
		assert(out[3] == 1024);
	}
	
	@Test
	public void rotateYTest() throws Exception {
		int[] out = VectorProcessor.generate();
		int[][] matrix = MatrixProcessor.generate();
		MatrixProcessor.rotateY(matrix, 30, matrix);
		int[] vector = VectorProcessor.generate(100, 100, 100);
		VectorProcessor.multiply(vector, matrix, out);
		assert(out[0] == 137);
		assert(out[1] == 100);
		assert(out[2] == 37);
		assert(out[3] == 1024);
	}
	
	@Test
	public void rotateZTest() throws Exception {
		int[] out = VectorProcessor.generate();
		int[][] matrix = MatrixProcessor.generate();
		MatrixProcessor.rotateZ(matrix, 30, matrix);
		int[] vector = VectorProcessor.generate(100, 100, 100);
		VectorProcessor.multiply(vector, matrix, out);
		assert(out[0] == 37);
		assert(out[1] == 137);
		assert(out[2] == 100);
		assert(out[3] == 1024);
	}
}
