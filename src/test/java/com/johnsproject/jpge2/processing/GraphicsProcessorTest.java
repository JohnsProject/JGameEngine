package com.johnsproject.jpge2.processing;

import org.junit.Test;

public class GraphicsProcessorTest {

	@Test
	public void translateTest() throws Exception {
		int[] out = VectorProcessor.generate();
		int[][] matrix = GraphicsProcessor.translate(2, 3, 4);
		int[] vector = VectorProcessor.generate();
		VectorProcessor.multiply(vector, matrix, out);
		assert(out[0] == 2);
		assert(out[1] == 3);
		assert(out[2] == 4);
		assert(out[3] == 1);
	}
	
	@Test
	public void scaleTest() throws Exception {
		int[] out = VectorProcessor.generate();
		int[][] matrix = GraphicsProcessor.scale(2, 3, 4);
		int[] vector = VectorProcessor.generate(1, 3, 5);
		VectorProcessor.multiply(vector, matrix, out);
		assert(out[0] == 2);
		assert(out[1] == 9);
		assert(out[2] == 20);
		assert(out[3] == 1);
	}
	
	@Test
	public void rotateXTest() throws Exception {
		int[] out = VectorProcessor.generate();
		int[][] matrix = GraphicsProcessor.rotateX(30);
		int[] vector = VectorProcessor.generate(100, 100, 100);
		VectorProcessor.multiply(vector, matrix, out);
		assert(out[0] == 100);
		assert(out[1] == 36);
		assert(out[2] == 136);
		assert(out[3] == 1);
	}
	
	@Test
	public void rotateYTest() throws Exception {
		int[] out = VectorProcessor.generate();
		int[][] matrix = GraphicsProcessor.rotateY(30);
		int[] vector = VectorProcessor.generate(100, 100, 100);
		VectorProcessor.multiply(vector, matrix, out);
		assert(out[0] == 136);
		assert(out[1] == 100);
		assert(out[2] == 36);
		assert(out[3] == 1);
	}
	
	@Test
	public void rotateZTest() throws Exception {
		int[] out = VectorProcessor.generate();
		int[][] matrix = GraphicsProcessor.rotateZ(30);
		int[] vector = VectorProcessor.generate(100, 100, 100);
		VectorProcessor.multiply(vector, matrix, out);
		assert(out[0] == 36);
		assert(out[1] == 136);
		assert(out[2] == 100);
		assert(out[3] == 1);
	}
	
}
