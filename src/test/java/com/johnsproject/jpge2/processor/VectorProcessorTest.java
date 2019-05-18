package com.johnsproject.jpge2.processor;

import org.junit.Test;

import com.johnsproject.jpge2.library.MathLibrary;
import com.johnsproject.jpge2.library.MatrixLibrary;
import com.johnsproject.jpge2.library.VectorLibrary;

public class VectorProcessorTest {

	@Test
	public void multiplyMatrixTest() throws Exception {
		MatrixLibrary matrixProcessor = new MatrixLibrary();
		VectorLibrary vectorProcessor = new VectorLibrary();
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
		VectorLibrary vectorProcessor = new VectorLibrary();
		int[] vector = vectorProcessor.generate(6 << MathLibrary.FP_BITS, 3 << MathLibrary.FP_BITS, 2 << MathLibrary.FP_BITS);
		assert(vectorProcessor.magnitude(vector) == 7 << MathLibrary.FP_BITS);
	}
	
	@Test
	public void normalizeTest() throws Exception {
		VectorLibrary vectorProcessor = new VectorLibrary();
		int[] out = vectorProcessor.generate();
		int[] vector = vectorProcessor.generate(60 << MathLibrary.FP_BITS, 30 << MathLibrary.FP_BITS, 20 << MathLibrary.FP_BITS);
		vectorProcessor.normalize(vector, out);
		assert(out[0] == 877);
		assert(out[1] == 438);
		assert(out[2] == 292);
	}
	
	@Test
	public void rotateXTest() throws Exception {
		VectorLibrary vectorProcessor = new VectorLibrary();
		int[] out = vectorProcessor.generate();
		int[] vector = vectorProcessor.generate(100, 100, 100);
		vectorProcessor.rotateX(vector, 30 << MathLibrary.FP_BITS, out);
		assert(out[0] == 100);
		assert(out[1] == 37);
		assert(out[2] == 137);
		assert(out[3] == 1024);
	}
	
}
