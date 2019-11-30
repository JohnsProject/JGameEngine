package com.johnsproject.jgameengine.math;

import org.junit.Test;

public class FixedPointMathTest {
	
//	@Test
//	public void genLookupTableTest() throws Exception {
//		for (int angle = 0; angle < 91; angle++) {
//			System.out.print((int)Math.round(Math.sin(Math.toRadians(angle)) * FixedPointMath.FP_ONE) + ", ");
//		}
//	}
	
	@Test
	public void toDegreeTest() throws Exception {
		for (int i = 1; i < 360; i++) {
			double precision = 0.001;
			int fpRad = FixedPointMath.toFixedPoint(Math.toRadians(i));
			double fpDegree = FixedPointMath.toDegrees(fpRad);
			fpDegree = FixedPointMath.toDouble((int)fpDegree);
			double mathDegree = i;
			assert((fpDegree >= mathDegree - precision) && (fpDegree <= mathDegree + precision));
		}
	}
	
	@Test
	public void toRadiansTest() throws Exception {
		for (int i = 1; i < 360; i++) {
			double precision = 0.1;
			int fpDegree = FixedPointMath.toFixedPoint(Math.toDegrees(i));
			double fpRad = FixedPointMath.toRadians(fpDegree);
			fpRad = FixedPointMath.toDouble((int)fpRad);
			double mathRad = i;
			assert((fpRad >= mathRad - precision) && (fpRad <= mathRad + precision));
		}
	}

	@Test
	public void sinTest() throws Exception {
		for (int i = 0; i < 360; i++) {
			double precision = 0.0001;
			int fpAngle = FixedPointMath.toFixedPoint(i);
			double fpSin = FixedPointMath.sin(fpAngle);
			fpSin = FixedPointMath.toDouble((int)fpSin);
			double sin = Math.sin(Math.toRadians(i));
			assert ((fpSin >= sin - precision) && (fpSin <= sin + precision));
		}
	}

	@Test
	public void cosTest() throws Exception {
		for (int i = 0; i < 360; i++) {
			double precision = 0.0001;
			int fpAngle = FixedPointMath.toFixedPoint(i);
			double fpCos = FixedPointMath.cos(fpAngle);
			fpCos = FixedPointMath.toDouble((int)fpCos);
			double cos = Math.cos(Math.toRadians(i));
			assert ((fpCos >= cos - precision) && (fpCos <= cos + precision));
		}
	}

	@Test
	public void tanTest() throws Exception {
		for (int i = 0; i < 90; i++) {
			double precision = 0.1;
			int fpAngle = FixedPointMath.toFixedPoint(i);
			double fpTan = FixedPointMath.tan(fpAngle);
			fpTan = FixedPointMath.toDouble((int)fpTan);
			double tan = Math.tan(Math.toRadians(i));
			assert ((fpTan >= tan - precision) && (fpTan <= tan + precision));
		}
	}
	
	@Test
	public void basicOperationsTest() throws Exception {
		// 255 because 256 * 256 = 65536 and will cause overflow of integer part of fixed point
		for (int i = 1; i < 256; i++) {
			double precision = 0.000000000000000000000000000000000001;
			int fpValue1 = FixedPointMath.toFixedPoint(i);
			int fpValue2 = FixedPointMath.toFixedPoint(i);
			double fpMultiply = FixedPointMath.multiply(fpValue1, fpValue2);
			double fpDivide = FixedPointMath.divide(fpValue1, fpValue2);
			fpMultiply = FixedPointMath.toDouble((int)fpMultiply);
			fpDivide = FixedPointMath.toDouble((int)fpDivide);
			double value1 = i;
			double value2 = i;
			double mathMultiply = value1 * value2;
			double mathDivide = value1 / value2;
			assert ((fpMultiply >= mathMultiply - precision) && (fpMultiply <= mathMultiply + precision));
			assert ((fpDivide >= mathDivide - precision) && (fpDivide <= mathDivide + precision));
		}
	}
	
	@Test
	public void normalizeTest() throws Exception {
		for (int i = 2; i < FixedPointMath.FP_ONE; i++) {
			int fpMin = FixedPointMath.toFixedPoint(i);
			int fpMax = FixedPointMath.toFixedPoint(i + 2);
			int fpValue = FixedPointMath.toFixedPoint(i + 5);
			int fpNormalizedValue = FixedPointMath.normalize(fpValue, fpMin, fpMax);
			assert(fpNormalizedValue >= fpMin && fpNormalizedValue <= fpMax);
		}
	}
	
	@Test
	public void clampTest() throws Exception {
		for (int i = 0; i < FixedPointMath.FP_ONE; i++) {
			int fpMin = FixedPointMath.toFixedPoint(i);
			int fpMax = FixedPointMath.toFixedPoint(i + 5);
			int fpValue = FixedPointMath.toFixedPoint(i + 10);
			int fpNormalizedValue = FixedPointMath.clamp(fpValue, fpMin, fpMax);
			assert(fpNormalizedValue >= fpMin && fpNormalizedValue <= fpMax);
		}
	}
	
	@Test
	public void randomTest() throws Exception {
		int lastRandomValue = 0;
		for (int i = 1; i < FixedPointMath.FP_ONE; i++) {
			int randomValue = FixedPointMath.random(lastRandomValue);
			lastRandomValue = randomValue;
			assert(randomValue != FixedPointMath.random(i));
		}
	}
	@Test
	public void minMaxRandomTest() throws Exception {
		int lastRandomValue = 0;
		for (int i = 1; i < FixedPointMath.FP_ONE; i++) {
			int fpMin = FixedPointMath.toFixedPoint(0);
			int fpMax = FixedPointMath.toFixedPoint(100);
			int randomValue = FixedPointMath.random(lastRandomValue, fpMin, fpMax);
			lastRandomValue = randomValue;
			assert(randomValue != FixedPointMath.random(i, fpMin, fpMax));
			assert((randomValue <= fpMax) && (randomValue >= fpMin));
			assert((randomValue <= fpMax) && (randomValue >= fpMin));
		}
	}
	
	@Test
	public void powTest() throws Exception {
		// max pow is 15 because because integer part of fixed point has 15 bits
		for (int i = 0; i < 16; i++) {
			double precision = 0.000000000001;
			int value = 2;
			int fpValue = FixedPointMath.toFixedPoint(value);
			double fpPow = FixedPointMath.pow(fpValue, FixedPointMath.toFixedPoint(i));
			fpPow = FixedPointMath.toDouble((int)fpPow);
			double mathPow = Math.pow(value, i);
			assert((fpPow >= mathPow - precision) && (fpPow <= mathPow + precision));
		}
	}
	
	@Test
	public void sqrtTest() throws Exception {
		for (int i = 1; i < FixedPointMath.FP_ONE; i++) {
			double precision = 0.01;
			double fpSqrt = FixedPointMath.sqrt(FixedPointMath.toFixedPoint(i));
			fpSqrt = FixedPointMath.toDouble((int)fpSqrt);
			double mathSqrt = Math.sqrt(i);
			assert((fpSqrt >= mathSqrt - precision) && (fpSqrt <= mathSqrt + precision));
		}
	}
}
