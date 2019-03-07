package com.johnsproject.jpge2.processing;

import org.junit.Test;

public class VectorProcessorTest {

	@Test
	public void multiplyMatrixTest() throws Exception {
		int[] out = VectorProcessor.generate();
		int[][] matrix = MatrixProcessor.generate();
		int[] vector = VectorProcessor.generate(6, 3, 2);
		VectorProcessor.multiply(vector, matrix, out);
		assert(out[0] == 6);
		assert(out[1] == 3);
		assert(out[2] == 2);
		assert(out[3] == 1);
	}
	
}
