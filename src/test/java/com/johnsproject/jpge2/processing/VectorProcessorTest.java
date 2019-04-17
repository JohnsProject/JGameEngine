package com.johnsproject.jpge2.processing;

import org.junit.Test;

import com.johnsproject.jpge2.processors.MathProcessor;
import com.johnsproject.jpge2.processors.MatrixProcessor;
import com.johnsproject.jpge2.processors.VectorProcessor;

public class VectorProcessorTest {

	@Test
	public void multiplyMatrixTest() throws Exception {
		int[] out = VectorProcessor.generate();
		int[][] matrix = MatrixProcessor.generateMatrix();
		int[] vector = VectorProcessor.generate(6, 3, 2);
		VectorProcessor.multiply(vector, matrix, out);
		assert(out[0] == 6);
		assert(out[1] == 3);
		assert(out[2] == 2);
		assert(out[3] == 1024);
	}
	
	@Test
	public void magnitudeTest() throws Exception {
		int[] vector = VectorProcessor.generate(6 << MathProcessor.FP_BITS, 3 << MathProcessor.FP_BITS, 2 << MathProcessor.FP_BITS);
		assert(VectorProcessor.magnitude(vector) == 7 << MathProcessor.FP_BITS);
	}
	
	@Test
	public void normalizeTest() throws Exception {
		int[] out = VectorProcessor.generate();
		int[] vector = VectorProcessor.generate(60 << MathProcessor.FP_BITS, 30 << MathProcessor.FP_BITS, 20 << MathProcessor.FP_BITS);
		VectorProcessor.normalize(vector, out);
		assert(out[0] == 877);
		assert(out[1] == 438);
		assert(out[2] == 292);
	}
	
}
