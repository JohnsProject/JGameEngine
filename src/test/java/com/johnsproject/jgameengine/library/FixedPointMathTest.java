package com.johnsproject.jgameengine.library;

import org.junit.Test;

import com.johnsproject.jgameengine.math.FixedPointMath;

public class FixedPointMathTest {
	
	@Test
	public void genLookupTableTest() throws Exception {
		for (int angle = 0; angle < 91; angle++) {
			System.out.print((int)Math.round(Math.sin(Math.toRadians(angle)) * FixedPointMath.FP_ONE) + ", ");
		}
	}
	
	@Test
	public void test() throws Exception {
		System.out.println(1 << 6);
		// just tests with fixed point math
		assert(((2 << FixedPointMath.FP_BIT) + (2 << FixedPointMath.FP_BIT)) >> FixedPointMath.FP_BIT == 4);
		assert(((2 << FixedPointMath.FP_BIT) + (2 << FixedPointMath.FP_BIT) + (2 << FixedPointMath.FP_BIT)) >> FixedPointMath.FP_BIT == 6);
		assert(((4 << FixedPointMath.FP_BIT) - (2 << FixedPointMath.FP_BIT)) >> FixedPointMath.FP_BIT == 2);
		assert(((2 << FixedPointMath.FP_BIT) * (2 << FixedPointMath.FP_BIT)) >> (FixedPointMath.FP_BIT * 2) == 4);
		assert(((2 << FixedPointMath.FP_BIT) * 2) >> FixedPointMath.FP_BIT == 4);
		assert(((4 << FixedPointMath.FP_BIT) / 2) >> FixedPointMath.FP_BIT == 2);
		assert((1 << FixedPointMath.FP_BIT) / 500 == 2);
	}

	@Test
	public void sintest() throws Exception {
		FixedPointMath mathLibrary = new FixedPointMath();
		for (int i = 0; i < 90; i++) {
			int angle = i;
			int precision = 10;
			int isin = mathLibrary.sin(angle << FixedPointMath.FP_BIT);
			int sin = (int) Math.round(Math.sin(Math.toRadians(angle)) * FixedPointMath.FP_ONE);
			assert (isin >= sin - precision && isin <= sin + precision);
		}
	}

	@Test
	public void cosTest() throws Exception {
		FixedPointMath mathLibrary = new FixedPointMath();
		for (int i = 0; i < 90; i++) {
			int angle = i;
			int precision = 10;
			int icos = mathLibrary.cos(angle << FixedPointMath.FP_BIT);
			int cos = (int) Math.round(Math.cos(Math.toRadians(angle)) * FixedPointMath.FP_ONE);
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
		FixedPointMath mathLibrary = new FixedPointMath();
		assert(mathLibrary.divide(10 << FixedPointMath.FP_BIT, 2 << FixedPointMath.FP_BIT) == 5 << FixedPointMath.FP_BIT);
	}
	
	@Test
	public void powTest() throws Exception {
		FixedPointMath mathLibrary = new FixedPointMath();
		assert(mathLibrary.pow(5 << FixedPointMath.FP_BIT, 10 << FixedPointMath.FP_BIT) == 9765625 << FixedPointMath.FP_BIT);
	}
	
	@Test
	public void sqrtTest() throws Exception {
		FixedPointMath mathLibrary = new FixedPointMath();
		System.out.println((mathLibrary.sqrt(256)) + ", " + (Math.sqrt(((double)0.25)) * FixedPointMath.FP_ONE));
		System.out.println((mathLibrary.sqrt(512)) + ", " + (Math.sqrt(((double)0.5)) * FixedPointMath.FP_ONE));
		assert(mathLibrary.sqrt(25 << FixedPointMath.FP_BIT) == 5 << FixedPointMath.FP_BIT);
	}

}
