package com.johnsproject.jgameengine.util;

import org.junit.Test;

import com.johnsproject.jgameengine.util.FixedPointUtils;

public class FixedPointUtilsTest {
	
//	@Test
//	public void genLookupTableTest() throws Exception {
//		for (int angle = 0; angle < 91; angle++) {
//			System.out.print((int)Math.round(Math.sin(Math.toRadians(angle)) * FixedPointUtils.FP_ONE) + ", ");
//		}
//	}
	
	@Test
	public void toDegreeTest() throws Exception {
		for (int i = 1; i < 360; i++) {
			double precision = 0.001;
			int fpRad = FixedPointUtils.toFixedPoint(Math.toRadians(i));
			double fpDegree = FixedPointUtils.toDegrees(fpRad);
			fpDegree = FixedPointUtils.toDouble((int)fpDegree);
			double mathDegree = i;
			assert((fpDegree >= mathDegree - precision) && (fpDegree <= mathDegree + precision));
		}
	}
	
	@Test
	public void toRadiansTest() throws Exception {
		for (int i = 1; i < 360; i++) {
			double precision = 0.1;
			int fpDegree = FixedPointUtils.toFixedPoint(Math.toDegrees(i));
			double fpRad = FixedPointUtils.toRadians(fpDegree);
			fpRad = FixedPointUtils.toDouble((int)fpRad);
			double mathRad = i;
			assert((fpRad >= mathRad - precision) && (fpRad <= mathRad + precision));
		}
	}

	@Test
	public void sinTest() throws Exception {
		for (int i = 0; i < 360; i++) {
			double precision = 0.0001;
			int fpAngle = FixedPointUtils.toFixedPoint(i);
			double fpSin = FixedPointUtils.sin(fpAngle);
			fpSin = FixedPointUtils.toDouble((int)fpSin);
			double sin = Math.sin(Math.toRadians(i));
			assert ((fpSin >= sin - precision) && (fpSin <= sin + precision));
		}
	}

	@Test
	public void cosTest() throws Exception {
		for (int i = 0; i < 360; i++) {
			double precision = 0.0001;
			int fpAngle = FixedPointUtils.toFixedPoint(i);
			double fpCos = FixedPointUtils.cos(fpAngle);
			fpCos = FixedPointUtils.toDouble((int)fpCos);
			double cos = Math.cos(Math.toRadians(i));
			assert ((fpCos >= cos - precision) && (fpCos <= cos + precision));
		}
	}

	@Test
	public void tanTest() throws Exception {
		for (int i = 0; i < 90; i++) {
			double precision = 0.1;
			int fpAngle = FixedPointUtils.toFixedPoint(i);
			double fpTan = FixedPointUtils.tan(fpAngle);
			fpTan = FixedPointUtils.toDouble((int)fpTan);
			double tan = Math.tan(Math.toRadians(i));
			assert ((fpTan >= tan - precision) && (fpTan <= tan + precision));
		}
	}
	
	@Test
	public void basicOperationsTest() throws Exception {
		// 255 because 256 * 256 = 65536 and will cause overflow of integer part of fixed point
		for (int i = 1; i < 256; i++) {
			double precision = 0.000000000000000000000000000000000001;
			int fpValue1 = FixedPointUtils.toFixedPoint(i);
			int fpValue2 = FixedPointUtils.toFixedPoint(i);
			double fpMultiply = FixedPointUtils.multiply(fpValue1, fpValue2);
			double fpDivide = FixedPointUtils.divide(fpValue1, fpValue2);
			fpMultiply = FixedPointUtils.toDouble((int)fpMultiply);
			fpDivide = FixedPointUtils.toDouble((int)fpDivide);
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
		for (int i = 2; i < FixedPointUtils.FP_ONE; i++) {
			int fpMin = FixedPointUtils.toFixedPoint(i);
			int fpMax = FixedPointUtils.toFixedPoint(i + 2);
			int fpValue = FixedPointUtils.toFixedPoint(i + 5);
			int fpNormalizedValue = FixedPointUtils.normalize(fpValue, fpMin, fpMax);
			assert(fpNormalizedValue >= fpMin && fpNormalizedValue <= fpMax);
		}
	}
	
	@Test
	public void clampTest() throws Exception {
		for (int i = 0; i < FixedPointUtils.FP_ONE; i++) {
			int fpMin = FixedPointUtils.toFixedPoint(i);
			int fpMax = FixedPointUtils.toFixedPoint(i + 5);
			int fpValue = FixedPointUtils.toFixedPoint(i + 10);
			int fpNormalizedValue = FixedPointUtils.clamp(fpValue, fpMin, fpMax);
			assert(fpNormalizedValue >= fpMin && fpNormalizedValue <= fpMax);
		}
	}
	
	@Test
	public void randomTest() throws Exception {
		int lastRandomValue = 0;
		for (int i = 1; i < FixedPointUtils.FP_ONE; i++) {
			int randomValue = FixedPointUtils.random(lastRandomValue);
			lastRandomValue = randomValue;
			assert(randomValue != FixedPointUtils.random(i));
		}
	}
	@Test
	public void minMaxRandomTest() throws Exception {
		int lastRandomValue = 0;
		for (int i = 1; i < FixedPointUtils.FP_ONE; i++) {
			int fpMin = FixedPointUtils.toFixedPoint(0);
			int fpMax = FixedPointUtils.toFixedPoint(100);
			int randomValue = FixedPointUtils.random(lastRandomValue, fpMin, fpMax);
			lastRandomValue = randomValue;
			assert(randomValue != FixedPointUtils.random(i, fpMin, fpMax));
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
			int fpValue = FixedPointUtils.toFixedPoint(value);
			double fpPow = FixedPointUtils.pow(fpValue, FixedPointUtils.toFixedPoint(i));
			fpPow = FixedPointUtils.toDouble((int)fpPow);
			double mathPow = Math.pow(value, i);
			assert((fpPow >= mathPow - precision) && (fpPow <= mathPow + precision));
		}
	}
	
	@Test
	public void sqrtTest() throws Exception {
		for (int i = 1; i < FixedPointUtils.FP_ONE; i++) {
			double precision = 0.01;
			double fpSqrt = FixedPointUtils.sqrt(FixedPointUtils.toFixedPoint(i));
			fpSqrt = FixedPointUtils.toDouble((int)fpSqrt);
			double mathSqrt = Math.sqrt(i);
			assert((fpSqrt >= mathSqrt - precision) && (fpSqrt <= mathSqrt + precision));
		}
	}
}
