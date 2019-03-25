package com.johnsproject.jpge2.processing;

import org.junit.Test;

public class MathProcessorTest {

	@Test
	public void genLookupTableTest() throws Exception {
		for (int angle = 0; angle < 91; angle++) {
			System.out.print((long)Math.round(Math.sin(Math.toRadians(angle)) * MathProcessor.FP_VALUE) + ", ");
		}
	}
	
	@Test
	public void test() throws Exception {
		// just tests with fixed point math
		assert(((2 << MathProcessor.FP_SHIFT) + (2 << MathProcessor.FP_SHIFT)) >> MathProcessor.FP_SHIFT == 4);
		assert(((2 << MathProcessor.FP_SHIFT) + (2 << MathProcessor.FP_SHIFT) + (2 << MathProcessor.FP_SHIFT)) >> MathProcessor.FP_SHIFT == 6);
		assert(((4 << MathProcessor.FP_SHIFT) - (2 << MathProcessor.FP_SHIFT)) >> MathProcessor.FP_SHIFT == 2);
		assert(((2 << MathProcessor.FP_SHIFT) * (2 << MathProcessor.FP_SHIFT)) >> (MathProcessor.FP_SHIFT * 2) == 4);
		assert(((2 << MathProcessor.FP_SHIFT) * 2) >> MathProcessor.FP_SHIFT == 4);
		assert(((4 << MathProcessor.FP_SHIFT) / 2) >> MathProcessor.FP_SHIFT == 2);
	}

	@Test
	public void sintest() throws Exception {
		for (int i = 0; i < 90; i++) {
			long angle = i;
			long precision = 1;
			long isin = MathProcessor.sin(angle);
			long sin = (long) Math.round(Math.sin(Math.toRadians(angle)) * MathProcessor.FP_VALUE);
			assert (isin >= sin - precision && isin <= sin + precision);
		}
	}

	@Test
	public void cosTest() throws Exception {
		for (int i = 0; i < 90; i++) {
			long angle = i;
			long precision = 1;
			long icos = MathProcessor.cos(angle);
			long cos = (long) Math.round(Math.cos(Math.toRadians(angle)) * MathProcessor.FP_VALUE);
			assert (icos >= cos - precision && icos <= cos + precision);
		}
	}

	@Test
	public void tanTest() throws Exception {
		for (int i = 0; i < 80; i++) {
			long angle = i;
			long precision = 20;
			long itan = MathProcessor.tan(angle);
			long tan = (long) Math.round(Math.tan(Math.toRadians(angle)) * MathProcessor.FP_VALUE);
			assert (itan >= tan - precision && itan <= tan + precision);
		}
	}

}
