package com.johnsproject.jgameengine.library;

import org.junit.Test;

import com.johnsproject.jgameengine.library.MathLibrary;

public class MathLibraryTest {
	
	@Test
	public void genLookupTableTest() throws Exception {
		for (int angle = 0; angle < 91; angle++) {
			System.out.print((int)Math.round(Math.sin(Math.toRadians(angle)) * MathLibrary.FP_ONE) + ", ");
		}
	}
	
	@Test
	public void test() throws Exception {
		// just tests with fixed point math
		assert(((2 << MathLibrary.FP_BITS) + (2 << MathLibrary.FP_BITS)) >> MathLibrary.FP_BITS == 4);
		assert(((2 << MathLibrary.FP_BITS) + (2 << MathLibrary.FP_BITS) + (2 << MathLibrary.FP_BITS)) >> MathLibrary.FP_BITS == 6);
		assert(((4 << MathLibrary.FP_BITS) - (2 << MathLibrary.FP_BITS)) >> MathLibrary.FP_BITS == 2);
		assert(((2 << MathLibrary.FP_BITS) * (2 << MathLibrary.FP_BITS)) >> (MathLibrary.FP_BITS * 2) == 4);
		assert(((2 << MathLibrary.FP_BITS) * 2) >> MathLibrary.FP_BITS == 4);
		assert(((4 << MathLibrary.FP_BITS) / 2) >> MathLibrary.FP_BITS == 2);
		assert((1 << MathLibrary.FP_BITS) / 500 == 2);
	}

	@Test
	public void sintest() throws Exception {
		MathLibrary mathLibrary = new MathLibrary();
		for (int i = 0; i < 90; i++) {
			int angle = i;
			int precision = 10;
			int isin = mathLibrary.sin(angle << MathLibrary.FP_BITS);
			int sin = (int) Math.round(Math.sin(Math.toRadians(angle)) * MathLibrary.FP_ONE);
			assert (isin >= sin - precision && isin <= sin + precision);
		}
	}

	@Test
	public void cosTest() throws Exception {
		MathLibrary mathLibrary = new MathLibrary();
		for (int i = 0; i < 90; i++) {
			int angle = i;
			int precision = 10;
			int icos = mathLibrary.cos(angle << MathLibrary.FP_BITS);
			int cos = (int) Math.round(Math.cos(Math.toRadians(angle)) * MathLibrary.FP_ONE);
			assert (icos >= cos - precision && icos <= cos + precision);
		}
	}

//	@Test
//	public void tanTest() throws Exception {
//		MathLibrary mathLibrary = new MathLibrary();
//		for (int i = 0; i < 90; i++) {
//			int angle = i;
//			int precision = 1000;
//			int itan = mathLibrary.tan(angle << MathLibrary.FP_BITS);
//			int tan = (int) Math.round(Math.tan(Math.toRadians(angle)) * MathLibrary.FP_ONE);
//			assert (itan >= tan - precision && itan <= tan + precision);
//		}
//	}
	
	@Test
	public void divideTest() throws Exception {
		MathLibrary mathLibrary = new MathLibrary();
		assert(mathLibrary.divide(10 << MathLibrary.FP_BITS, 2 << MathLibrary.FP_BITS) == 5 << MathLibrary.FP_BITS);
	}
	
	@Test
	public void powTest() throws Exception {
		MathLibrary mathLibrary = new MathLibrary();
		assert(mathLibrary.pow(5 << MathLibrary.FP_BITS, 10 << MathLibrary.FP_BITS) == 9765625 << MathLibrary.FP_BITS);
	}
	
	@Test
	public void sqrtTest() throws Exception {
		MathLibrary mathLibrary = new MathLibrary();
		System.out.println((mathLibrary.sqrt(256)) + ", " + (Math.sqrt(((double)0.25)) * MathLibrary.FP_ONE));
		System.out.println((mathLibrary.sqrt(512)) + ", " + (Math.sqrt(((double)0.5)) * MathLibrary.FP_ONE));
		assert(mathLibrary.sqrt(25 << MathLibrary.FP_BITS) == 5 << MathLibrary.FP_BITS);
	}

}
