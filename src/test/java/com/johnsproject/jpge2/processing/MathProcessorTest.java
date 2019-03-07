package com.johnsproject.jpge2.processing;

import org.junit.Test;

public class MathProcessorTest {

	@Test
	public void genLookupTableTest() throws Exception {
		for (int angle = 0; angle < 91; angle++) {
			System.out.print((int)Math.round(Math.sin(Math.toRadians(angle)) * MathProcessor.FP_VALUE) + ", ");
		}
	}

	@Test
	public void sintest() throws Exception {
		for (int i = 0; i < 90; i++) {
			int angle = i;
			int precision = 1;
			int isin = MathProcessor.sin(angle);
			int sin = (int) Math.round(Math.sin(Math.toRadians(angle)) * MathProcessor.FP_VALUE);
			assert (isin >= sin - precision && isin <= sin + precision);
		}
	}

	@Test
	public void cosTest() throws Exception {
		for (int i = 0; i < 90; i++) {
			int angle = i;
			int precision = 1;
			int icos = MathProcessor.cos(angle);
			int cos = (int) Math.round(Math.cos(Math.toRadians(angle)) * MathProcessor.FP_VALUE);
			assert (icos >= cos - precision && icos <= cos + precision);
		}
	}

	@Test
	public void tanTest() throws Exception {
		for (int i = 0; i < 80; i++) {
			int angle = i;
			int precision = 20;
			int itan = MathProcessor.tan(angle);
			int tan = (int) Math.round(Math.tan(Math.toRadians(angle)) * MathProcessor.FP_VALUE);
			assert (itan >= tan - precision && itan <= tan + precision);
		}
	}

}
