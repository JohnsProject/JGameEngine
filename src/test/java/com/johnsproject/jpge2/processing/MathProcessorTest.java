package com.johnsproject.jpge2.processing;

import org.junit.Test;

import com.johnsproject.jpge2.processors.MathProcessor;

public class MathProcessorTest {

	@Test
	public void genLookupTableTest() throws Exception {
		for (int angle = 0; angle < 91; angle++) {
			System.out.print((int)Math.round(Math.sin(Math.toRadians(angle)) * MathProcessor.FP_ONE) + ", ");
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
			int angle = i;
			int precision = 1;
			int isin = MathProcessor.sin(angle);
			int sin = (int) Math.round(Math.sin(Math.toRadians(angle)) * MathProcessor.FP_ONE);
			assert (isin >= sin - precision && isin <= sin + precision);
		}
	}

	@Test
	public void cosTest() throws Exception {
		for (int i = 0; i < 90; i++) {
			int angle = i;
			int precision = 1;
			int icos = MathProcessor.cos(angle);
			int cos = (int) Math.round(Math.cos(Math.toRadians(angle)) * MathProcessor.FP_ONE);
			assert (icos >= cos - precision && icos <= cos + precision);
		}
	}

	@Test
	public void tanTest() throws Exception {
		for (int i = 0; i < 90; i++) {
			int angle = i;
			int precision = 411;
			int itan = MathProcessor.tan(angle);
			int tan = (int) Math.round(Math.tan(Math.toRadians(angle)) * MathProcessor.FP_ONE);
			assert (itan >= tan - precision && itan <= tan + precision);
		}
	}
	
	@Test
	public void divideTest() throws Exception {
		assert(MathProcessor.divide(10 << MathProcessor.FP_SHIFT, 2 << MathProcessor.FP_SHIFT) == 5 << MathProcessor.FP_SHIFT);
	}
	
	@Test
	public void powTest() throws Exception {
		assert(MathProcessor.pow(5 << MathProcessor.FP_SHIFT, 2) == 25 << MathProcessor.FP_SHIFT);
	}
	
	@Test
	public void sqrtTest() throws Exception {
		System.out.println(MathProcessor.sqrt(25 << MathProcessor.FP_SHIFT));
		assert(MathProcessor.sqrt(25 << MathProcessor.FP_SHIFT) == 5 << MathProcessor.FP_SHIFT);
	}

}
