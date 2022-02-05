package com.johnsproject.jgameengine.math;

import org.junit.Test;

public class FixedPointTest {
	
//	@Test
//	public void genLookupTableTest() throws Exception {
//		for (int angle = 0; angle < 91; angle++) {
//			System.out.print((int)Math.round(Math.sin(Math.toRadians(angle)) * FixedPointUtils.FP_ONE) + ", ");
//		}
//	}
	
//	@Test
//	public void genLookupTableTest() throws Exception {
//		for (int angle = 0; angle < 361; angle++) {
//			System.out.print((int)Math.round(Math.cos(Math.toRadians(angle)) * FixedPointUtils.FP_ONE) + ", ");
//		}
//	}
	
	@Test
	public void toDegreeTest() throws Exception {
		for (int i = 1; i < 360; i++) {
			double precision = 0.001;
			int fpRad = Fixed.toFixed(Math.toRadians(i));
			double fpDegree = Fixed.toDegrees(fpRad);
			fpDegree = Fixed.toDouble((int)fpDegree);
			double mathDegree = i;
			assert((fpDegree >= mathDegree - precision) && (fpDegree <= mathDegree + precision));
		}
	}
	
	@Test
	public void toRadiansTest() throws Exception {
		for (int i = 1; i < 360; i++) {
			double precision = 0.1;
			int fpDegree = Fixed.toFixed(Math.toDegrees(i));
			double fpRad = Fixed.toRadians(fpDegree);
			fpRad = Fixed.toDouble((int)fpRad);
			double mathRad = i;
			assert((fpRad >= mathRad - precision) && (fpRad <= mathRad + precision));
		}
	}

	@Test
	public void sinTest() throws Exception {
		for (int i = 0; i < 361; i++) {
			double precision = 0.0001;
			int fpAngle = Fixed.toFixed(i);
			double fpSin = Fixed.sin(fpAngle);
			fpSin = Fixed.toDouble((int)fpSin);
			double sin = Math.sin(Math.toRadians(i));
			assert ((fpSin >= sin - precision) && (fpSin <= sin + precision));
		}
		for (int i = 0; i > -361; i--) {
			double precision = 0.0001;
			int fpAngle = Fixed.toFixed(i);
			double fpSin = Fixed.sin(fpAngle);
			fpSin = Fixed.toDouble((int)fpSin);
			double sin = Math.sin(Math.toRadians(i));
			assert ((fpSin >= sin - precision) && (fpSin <= sin + precision));
		}
	}

	@Test
	public void cosTest() throws Exception {
		for (int i = 0; i < 361; i++) {
			double precision = 0.0001;
			int fpAngle = Fixed.toFixed(i);
			double fpCos = Fixed.cos(fpAngle);
			fpCos = Fixed.toDouble((int)fpCos);
			double cos = Math.cos(Math.toRadians(i));
			assert ((fpCos >= cos - precision) && (fpCos <= cos + precision));
		}
		for (int i = 0; i > -361; i--) {
			double precision = 0.0001;
			int fpAngle = Fixed.toFixed(i);
			double fpCos = Fixed.cos(fpAngle);
			fpCos = Fixed.toDouble((int)fpCos);
			double cos = Math.cos(Math.toRadians(i));
			assert ((fpCos >= cos - precision) && (fpCos <= cos + precision));
		}
	}

	@Test
	public void tanTest() throws Exception {
		for (int i = 0; i < 90; i++) {
			double precision = 0.1;
			int fpAngle = Fixed.toFixed(i);
			double fpTan = Fixed.tan(fpAngle);
			fpTan = Fixed.toDouble((int)fpTan);
			double tan = Math.tan(Math.toRadians(i));
			assert ((fpTan >= tan - precision) && (fpTan <= tan + precision));
		}
	}
	
	@Test
	public void asinTest() throws Exception {
		for (int i = 0; i < 361; i++) {
			double precision = 0.0001;
			double fpAngle = Fixed.toFixed(i);
			int fpSin = Fixed.sin((int)fpAngle);
			fpAngle = Fixed.asin(fpSin);
			fpAngle = Fixed.toDouble((int)fpAngle);
			double sin = Math.sin(Math.toRadians(i));		
			double angle = Math.toDegrees(Math.asin(sin));
			assert ((fpAngle >= angle - precision) && (fpAngle <= angle + precision));
		}
		for (int i = 0; i > -361; i--) {
			double precision = 0.0001;
			double fpAngle = Fixed.toFixed(i);
			int fpSin = Fixed.sin((int)fpAngle);
			fpAngle = Fixed.asin(fpSin);
			fpAngle = Fixed.toDouble((int)fpAngle);
			double sin = Math.sin(Math.toRadians(i));		
			double angle = Math.toDegrees(Math.asin(sin));
			assert ((fpAngle >= angle - precision) && (fpAngle <= angle + precision));
		}
	}
	
	@Test
	public void acosTest() throws Exception {
		for (int i = 0; i < 361; i++) {
			double precision = 0.0001;
			double fpAngle = Fixed.toFixed(i);
			int fpCos = Fixed.cos((int)fpAngle);
			fpAngle = Fixed.acos(fpCos);
			fpAngle = Fixed.toDouble((int)fpAngle);
			double cos = Math.cos(Math.toRadians(i));
			double angle = Math.toDegrees(Math.acos(cos));
			assert ((fpAngle >= angle - precision) && (fpAngle <= angle + precision));
		}
		for (int i = 0; i > -361; i--) {
			double precision = 0.0001;
			double fpAngle = Fixed.toFixed(i);
			int fpCos = Fixed.cos((int)fpAngle);
			fpAngle = Fixed.acos(fpCos);
			fpAngle = Fixed.toDouble((int)fpAngle);
			double cos = Math.cos(Math.toRadians(i));
			double angle = Math.toDegrees(Math.acos(cos));
			assert ((fpAngle >= angle - precision) && (fpAngle <= angle + precision));
		}
	}
	
	@Test
	public void basicOperationsTest() throws Exception {
		// 255 because 256 * 256 = 65536 and will cause overflow of integer part of fixed point
		for (int i = 1; i < 256; i++) {
			double precision = 0.000000000000000000000000000000000001;
			int fpValue1 = Fixed.toFixed(i);
			int fpValue2 = Fixed.toFixed(i);
			double fpMultiply = Fixed.multiply(fpValue1, fpValue2);
			double fpDivide = Fixed.divide(fpValue1, fpValue2);
			fpMultiply = Fixed.toDouble((int)fpMultiply);
			fpDivide = Fixed.toDouble((int)fpDivide);
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
		for (int i = 2; i < Fixed.FP_ONE; i++) {
			int fpMin = Fixed.toFixed(i);
			int fpMax = Fixed.toFixed(i + 2);
			int fpValue = Fixed.toFixed(i + 5);
			int fpNormalizedValue = Fixed.normalize(fpValue, fpMin, fpMax);
			assert(fpNormalizedValue >= fpMin && fpNormalizedValue <= fpMax);
		}
	}
	
	@Test
	public void clampTest() throws Exception {
		for (int i = 0; i < Fixed.FP_ONE; i++) {
			int fpMin = Fixed.toFixed(i);
			int fpMax = Fixed.toFixed(i + 5);
			int fpValue = Fixed.toFixed(i + 10);
			int fpNormalizedValue = Fixed.clamp(fpValue, fpMin, fpMax);
			assert(fpNormalizedValue >= fpMin && fpNormalizedValue <= fpMax);
		}
	}
	
	@Test
	public void randomTest() throws Exception {
		int lastRandomValue = 0;
		for (int i = 1; i < Fixed.FP_ONE; i++) {
			int randomValue = Fixed.random(lastRandomValue);
			lastRandomValue = randomValue;
			assert(randomValue != Fixed.random(i));
		}
	}
	@Test
	public void minMaxRandomTest() throws Exception {
		int lastRandomValue = 0;
		for (int i = 1; i < Fixed.FP_ONE; i++) {
			int fpMin = Fixed.toFixed(0);
			int fpMax = Fixed.toFixed(100);
			int randomValue = Fixed.random(lastRandomValue, fpMin, fpMax);
			lastRandomValue = randomValue;
			assert(randomValue != Fixed.random(i, fpMin, fpMax));
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
			int fpValue = Fixed.toFixed(value);
			double fpPow = Fixed.pow(fpValue, Fixed.toFixed(i));
			fpPow = Fixed.toDouble((int)fpPow);
			double mathPow = Math.pow(value, i);
			assert((fpPow >= mathPow - precision) && (fpPow <= mathPow + precision));
		}
	}
	
	@Test
	public void sqrtTest() throws Exception {
		for (int i = 1; i < Fixed.FP_ONE; i++) {
			double precision = 0.01;
			double fpSqrt = Fixed.sqrt(Fixed.toFixed(i));
			fpSqrt = Fixed.toDouble((int)fpSqrt);
			double mathSqrt = Math.sqrt(i);
			assert((fpSqrt >= mathSqrt - precision) && (fpSqrt <= mathSqrt + precision));
		}
	}
}
