package com.johnsproject.jgameengine.math;

import org.junit.Test;

import com.johnsproject.jgameengine.math.FixedPoint;

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
			int fpRad = FixedPoint.toFixedPoint(Math.toRadians(i));
			double fpDegree = FixedPoint.toDegrees(fpRad);
			fpDegree = FixedPoint.toDouble((int)fpDegree);
			double mathDegree = i;
			assert((fpDegree >= mathDegree - precision) && (fpDegree <= mathDegree + precision));
		}
	}
	
	@Test
	public void toRadiansTest() throws Exception {
		for (int i = 1; i < 360; i++) {
			double precision = 0.1;
			int fpDegree = FixedPoint.toFixedPoint(Math.toDegrees(i));
			double fpRad = FixedPoint.toRadians(fpDegree);
			fpRad = FixedPoint.toDouble((int)fpRad);
			double mathRad = i;
			assert((fpRad >= mathRad - precision) && (fpRad <= mathRad + precision));
		}
	}

	@Test
	public void sinTest() throws Exception {
		for (int i = 0; i < 361; i++) {
			double precision = 0.0001;
			int fpAngle = FixedPoint.toFixedPoint(i);
			double fpSin = FixedPoint.sin(fpAngle);
			fpSin = FixedPoint.toDouble((int)fpSin);
			double sin = Math.sin(Math.toRadians(i));
			assert ((fpSin >= sin - precision) && (fpSin <= sin + precision));
		}
		for (int i = 0; i > -361; i--) {
			double precision = 0.0001;
			int fpAngle = FixedPoint.toFixedPoint(i);
			double fpSin = FixedPoint.sin(fpAngle);
			fpSin = FixedPoint.toDouble((int)fpSin);
			double sin = Math.sin(Math.toRadians(i));
			assert ((fpSin >= sin - precision) && (fpSin <= sin + precision));
		}
	}

	@Test
	public void cosTest() throws Exception {
		for (int i = 0; i < 361; i++) {
			double precision = 0.0001;
			int fpAngle = FixedPoint.toFixedPoint(i);
			double fpCos = FixedPoint.cos(fpAngle);
			fpCos = FixedPoint.toDouble((int)fpCos);
			double cos = Math.cos(Math.toRadians(i));
			assert ((fpCos >= cos - precision) && (fpCos <= cos + precision));
		}
		for (int i = 0; i > -361; i--) {
			double precision = 0.0001;
			int fpAngle = FixedPoint.toFixedPoint(i);
			double fpCos = FixedPoint.cos(fpAngle);
			fpCos = FixedPoint.toDouble((int)fpCos);
			double cos = Math.cos(Math.toRadians(i));
			assert ((fpCos >= cos - precision) && (fpCos <= cos + precision));
		}
	}

	@Test
	public void tanTest() throws Exception {
		for (int i = 0; i < 90; i++) {
			double precision = 0.1;
			int fpAngle = FixedPoint.toFixedPoint(i);
			double fpTan = FixedPoint.tan(fpAngle);
			fpTan = FixedPoint.toDouble((int)fpTan);
			double tan = Math.tan(Math.toRadians(i));
			assert ((fpTan >= tan - precision) && (fpTan <= tan + precision));
		}
	}
	
	@Test
	public void asinTest() throws Exception {
		for (int i = 0; i < 361; i++) {
			double precision = 0.0001;
			double fpAngle = FixedPoint.toFixedPoint(i);
			int fpSin = FixedPoint.sin((int)fpAngle);
			fpAngle = FixedPoint.asin(fpSin);
			fpAngle = FixedPoint.toDouble((int)fpAngle);
			double sin = Math.sin(Math.toRadians(i));		
			double angle = Math.toDegrees(Math.asin(sin));
			assert ((fpAngle >= angle - precision) && (fpAngle <= angle + precision));
		}
		for (int i = 0; i > -361; i--) {
			double precision = 0.0001;
			double fpAngle = FixedPoint.toFixedPoint(i);
			int fpSin = FixedPoint.sin((int)fpAngle);
			fpAngle = FixedPoint.asin(fpSin);
			fpAngle = FixedPoint.toDouble((int)fpAngle);
			double sin = Math.sin(Math.toRadians(i));		
			double angle = Math.toDegrees(Math.asin(sin));
			assert ((fpAngle >= angle - precision) && (fpAngle <= angle + precision));
		}
	}
	
	@Test
	public void acosTest() throws Exception {
		for (int i = 0; i < 361; i++) {
			double precision = 0.0001;
			double fpAngle = FixedPoint.toFixedPoint(i);
			int fpCos = FixedPoint.cos((int)fpAngle);
			fpAngle = FixedPoint.acos(fpCos);
			fpAngle = FixedPoint.toDouble((int)fpAngle);
			double cos = Math.cos(Math.toRadians(i));
			double angle = Math.toDegrees(Math.acos(cos));
			assert ((fpAngle >= angle - precision) && (fpAngle <= angle + precision));
		}
		for (int i = 0; i > -361; i--) {
			double precision = 0.0001;
			double fpAngle = FixedPoint.toFixedPoint(i);
			int fpCos = FixedPoint.cos((int)fpAngle);
			fpAngle = FixedPoint.acos(fpCos);
			fpAngle = FixedPoint.toDouble((int)fpAngle);
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
			int fpValue1 = FixedPoint.toFixedPoint(i);
			int fpValue2 = FixedPoint.toFixedPoint(i);
			double fpMultiply = FixedPoint.multiply(fpValue1, fpValue2);
			double fpDivide = FixedPoint.divide(fpValue1, fpValue2);
			fpMultiply = FixedPoint.toDouble((int)fpMultiply);
			fpDivide = FixedPoint.toDouble((int)fpDivide);
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
		for (int i = 2; i < FixedPoint.FP_ONE; i++) {
			int fpMin = FixedPoint.toFixedPoint(i);
			int fpMax = FixedPoint.toFixedPoint(i + 2);
			int fpValue = FixedPoint.toFixedPoint(i + 5);
			int fpNormalizedValue = FixedPoint.normalize(fpValue, fpMin, fpMax);
			assert(fpNormalizedValue >= fpMin && fpNormalizedValue <= fpMax);
		}
	}
	
	@Test
	public void clampTest() throws Exception {
		for (int i = 0; i < FixedPoint.FP_ONE; i++) {
			int fpMin = FixedPoint.toFixedPoint(i);
			int fpMax = FixedPoint.toFixedPoint(i + 5);
			int fpValue = FixedPoint.toFixedPoint(i + 10);
			int fpNormalizedValue = FixedPoint.clamp(fpValue, fpMin, fpMax);
			assert(fpNormalizedValue >= fpMin && fpNormalizedValue <= fpMax);
		}
	}
	
	@Test
	public void randomTest() throws Exception {
		int lastRandomValue = 0;
		for (int i = 1; i < FixedPoint.FP_ONE; i++) {
			int randomValue = FixedPoint.random(lastRandomValue);
			lastRandomValue = randomValue;
			assert(randomValue != FixedPoint.random(i));
		}
	}
	@Test
	public void minMaxRandomTest() throws Exception {
		int lastRandomValue = 0;
		for (int i = 1; i < FixedPoint.FP_ONE; i++) {
			int fpMin = FixedPoint.toFixedPoint(0);
			int fpMax = FixedPoint.toFixedPoint(100);
			int randomValue = FixedPoint.random(lastRandomValue, fpMin, fpMax);
			lastRandomValue = randomValue;
			assert(randomValue != FixedPoint.random(i, fpMin, fpMax));
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
			int fpValue = FixedPoint.toFixedPoint(value);
			double fpPow = FixedPoint.pow(fpValue, FixedPoint.toFixedPoint(i));
			fpPow = FixedPoint.toDouble((int)fpPow);
			double mathPow = Math.pow(value, i);
			assert((fpPow >= mathPow - precision) && (fpPow <= mathPow + precision));
		}
	}
	
	@Test
	public void sqrtTest() throws Exception {
		for (int i = 1; i < FixedPoint.FP_ONE; i++) {
			double precision = 0.01;
			double fpSqrt = FixedPoint.sqrt(FixedPoint.toFixedPoint(i));
			fpSqrt = FixedPoint.toDouble((int)fpSqrt);
			double mathSqrt = Math.sqrt(i);
			assert((fpSqrt >= mathSqrt - precision) && (fpSqrt <= mathSqrt + precision));
		}
	}
}
