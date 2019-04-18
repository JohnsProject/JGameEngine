package com.johnsproject.jpge2.processors;

import org.junit.Test;

import com.johnsproject.jpge2.processors.MathProcessor;
import com.johnsproject.jpge2.processors.MatrixProcessor;
import com.johnsproject.jpge2.processors.VectorProcessor;

public class VectorProcessorTest {

	@Test
	public void multiplyMatrixTest() throws Exception {
		MatrixProcessor matrixProcessor = new MatrixProcessor(new MathProcessor());
		VectorProcessor vectorProcessor = new VectorProcessor(new MathProcessor());
		int[] out = vectorProcessor.generate();
		int[][] matrix = matrixProcessor.generate();
		int[] vector = vectorProcessor.generate(6, 3, 2);
		vectorProcessor.multiply(vector, matrix, out);
		assert(out[0] == 6);
		assert(out[1] == 3);
		assert(out[2] == 2);
		assert(out[3] == 1024);
	}
	
	@Test
	public void magnitudeTest() throws Exception {
		VectorProcessor vectorProcessor = new VectorProcessor(new MathProcessor());
		int[] vector = vectorProcessor.generate(6 << MathProcessor.FP_BITS, 3 << MathProcessor.FP_BITS, 2 << MathProcessor.FP_BITS);
		assert(vectorProcessor.magnitude(vector) == 7 << MathProcessor.FP_BITS);
	}
	
	@Test
	public void normalizeTest() throws Exception {
		VectorProcessor vectorProcessor = new VectorProcessor(new MathProcessor());
		int[] out = vectorProcessor.generate();
		int[] vector = vectorProcessor.generate(60 << MathProcessor.FP_BITS, 30 << MathProcessor.FP_BITS, 20 << MathProcessor.FP_BITS);
		vectorProcessor.normalize(vector, out);
		assert(out[0] == 877);
		assert(out[1] == 438);
		assert(out[2] == 292);
	}
	
}
