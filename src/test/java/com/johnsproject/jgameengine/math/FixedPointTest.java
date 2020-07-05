package com.johnsproject.jgameengine.math;

import static org.junit.Assert.*;

import org.junit.Test;

public class FixedPointTest {	

	@Test
	public void addTest() throws Exception {
		FixedPoint firstValue32 = new FixedPoint32(10000.222f);
		FixedPoint secondValue32 = new FixedPoint32(1000.222f);
		
		FixedPoint firstValue64 = new FixedPoint64(1000000000.222);
		FixedPoint secondValue64 = new FixedPoint64(10000.222);


		float firstResult32 = firstValue32.toFloatValue() + secondValue32.toFloatValue();
		firstValue32.add(secondValue32);
		
		double firstResult64 = firstValue64.toDoubleValue() + secondValue64.toDoubleValue();
		firstValue64.add(secondValue64);
		
		float secondResult32 = secondValue32.toFloatValue() + secondValue64.toFloatValue();
		secondValue32.add(secondValue64);
		
		double secondResult64 = secondValue64.toDoubleValue() + secondValue32.toFloatValue();
		secondValue64.add(secondValue32);

		assert(firstValue32.toFloatValue() >= firstResult32 - 0.001f);
		assert(firstValue32.toFloatValue() <= firstResult32 + 0.001f);
		
		assert(secondValue32.toFloatValue() >= secondResult32 - 0.001f);
		assert(secondValue32.toFloatValue() <= secondResult32 + 0.001f);
		
		assert(firstValue64.toDoubleValue() >= firstResult64 - 0.001);
		assert(firstValue64.toDoubleValue() <= firstResult64 + 0.001);

		assert(secondValue64.toDoubleValue() >= secondResult64 - 0.001);
		assert(secondValue64.toDoubleValue() <= secondResult64 + 0.001);
	}
	
	@Test
	public void subtractTest() throws Exception {
		FixedPoint firstValue32 = new FixedPoint32(10000.222f);
		FixedPoint secondValue32 = new FixedPoint32(1000.222f);
		
		FixedPoint firstValue64 = new FixedPoint64(1000000000.222);
		FixedPoint secondValue64 = new FixedPoint64(10000.222);


		float firstResult32 = firstValue32.toFloatValue() - secondValue32.toFloatValue();
		firstValue32.subtract(secondValue32);
		
		double firstResult64 = firstValue64.toDoubleValue() - secondValue64.toDoubleValue();
		firstValue64.subtract(secondValue64);
		
		float secondResult32 = secondValue32.toFloatValue() - secondValue64.toFloatValue();
		secondValue32.subtract(secondValue64);
		
		double secondResult64 = secondValue64.toDoubleValue() - secondValue32.toFloatValue();
		secondValue64.subtract(secondValue32);

		assert(firstValue32.toFloatValue() >= firstResult32 - 0.001f);
		assert(firstValue32.toFloatValue() <= firstResult32 + 0.001f);
		
		assert(secondValue32.toFloatValue() >= secondResult32 - 0.001f);
		assert(secondValue32.toFloatValue() <= secondResult32 + 0.001f);
		
		assert(firstValue64.toDoubleValue() >= firstResult64 - 0.001);
		assert(firstValue64.toDoubleValue() <= firstResult64 + 0.001);

		assert(secondValue64.toDoubleValue() >= secondResult64 - 0.001);
		assert(secondValue64.toDoubleValue() <= secondResult64 + 0.001);
	}	
	
	@Test
	public void multiplyTest() throws Exception {
		FixedPoint firstValue32 = new FixedPoint32(100.222f);
		FixedPoint secondValue32 = new FixedPoint32(100.222f);
		
		FixedPoint firstValue64 = new FixedPoint64(10000000.222);
		FixedPoint secondValue64 = new FixedPoint64(100.222);


		float firstResult32 = firstValue32.toFloatValue() * secondValue32.toFloatValue();
		firstValue32.multiply(secondValue32);
		
		double firstResult64 = firstValue64.toDoubleValue() * secondValue64.toDoubleValue();
		firstValue64.multiply(secondValue64);
		
		float secondResult32 = secondValue32.toFloatValue() * secondValue64.toFloatValue();
		secondValue32.multiply(secondValue64);
		
		double secondResult64 = secondValue64.toDoubleValue() * secondValue32.toFloatValue();
		secondValue64.multiply(secondValue32);

		assert(firstValue32.toFloatValue() >= firstResult32 - 0.01f);
		assert(firstValue32.toFloatValue() <= firstResult32 + 0.01f);
		
		assert(secondValue32.toFloatValue() >= secondResult32 - 0.01f);
		assert(secondValue32.toFloatValue() <= secondResult32 + 0.01f);

		assert(firstValue64.toDoubleValue() >= firstResult64 - 0.01);
		assert(firstValue64.toDoubleValue() <= firstResult64 + 0.01);

		assert(secondValue64.toDoubleValue() >= secondResult64 - 0.01);
		assert(secondValue64.toDoubleValue() <= secondResult64 + 0.01);
	}	
	
	@Test
	public void divideTest() throws Exception {
		FixedPoint firstValue32 = new FixedPoint32(10000.222f);
		FixedPoint secondValue32 = new FixedPoint32(100.222f);
		
		FixedPoint firstValue64 = new FixedPoint64(10000000.222);
		FixedPoint secondValue64 = new FixedPoint64(100.222);

		float firstResult32 = firstValue32.toFloatValue() / secondValue32.toFloatValue();
		firstValue32.divide(secondValue32);
		
		double firstResult64 = firstValue64.toDoubleValue() / secondValue64.toDoubleValue();
		firstValue64.divide(secondValue64);
		
		float secondResult32 = secondValue32.toFloatValue() / secondValue64.toFloatValue();
		secondValue32.divide(secondValue64);
		
		double secondResult64 = secondValue64.toDoubleValue() / secondValue32.toFloatValue();
		secondValue64.divide(secondValue32);

		assert(firstValue32.toFloatValue() >= firstResult32 - 0.01f);
		assert(firstValue32.toFloatValue() <= firstResult32 + 0.01f);
		
		assert(secondValue32.toFloatValue() >= secondResult32 - 0.01f);
		assert(secondValue32.toFloatValue() <= secondResult32 + 0.01f);

		assert(firstValue64.toDoubleValue() >= firstResult64 - 0.01);
		assert(firstValue64.toDoubleValue() <= firstResult64 + 0.01);

		assert(secondValue64.toDoubleValue() >= secondResult64 - 0.01);
		assert(secondValue64.toDoubleValue() <= secondResult64 + 0.01);
	}	
}
