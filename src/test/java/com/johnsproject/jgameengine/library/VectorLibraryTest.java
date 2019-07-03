package com.johnsproject.jgameengine.library;

import org.junit.Test;

import com.johnsproject.jgameengine.library.MathLibrary;
import com.johnsproject.jgameengine.library.MatrixLibrary;
import com.johnsproject.jgameengine.library.VectorLibrary;

public class VectorLibraryTest {

	@Test
	public void multiplyMatrixTest() throws Exception {
		MatrixLibrary matrixLibrary = new MatrixLibrary();
		VectorLibrary vectorLibrary = new VectorLibrary();
		int[] out = vectorLibrary.generate();
		int[] matrix = matrixLibrary.generate();
		int[] vector = vectorLibrary.generate(6 << MathLibrary.FP_BITS, 3 << MathLibrary.FP_BITS, 2 << MathLibrary.FP_BITS);
		vectorLibrary.multiply(vector, matrix, out);
		assert(out[0] == 6 << MathLibrary.FP_BITS);
		assert(out[1] == 3 << MathLibrary.FP_BITS);
		assert(out[2] == 2 << MathLibrary.FP_BITS);
		assert(out[3] == 1024);
	}
	
	@Test
	public void magnitudeTest() throws Exception {
		VectorLibrary vectorProcessor = new VectorLibrary();
		int[] vector = vectorProcessor.generate(6 << MathLibrary.FP_BITS, 3 << MathLibrary.FP_BITS, 2 << MathLibrary.FP_BITS);
		assert(vectorProcessor.length(vector) == 7 << MathLibrary.FP_BITS);
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
}
