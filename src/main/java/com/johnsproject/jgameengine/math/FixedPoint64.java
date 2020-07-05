package com.johnsproject.jgameengine.math;

public class FixedPoint64 implements FixedPoint {
	
	private long fixedPoint;
	
	public FixedPoint64() {
		fixedPoint = 0;
	}
	
	public FixedPoint64(double value) {
		fixedPoint = Math.round(value * FP_64_ONE);
	}
	
	public FixedPoint64(FixedPoint value) {
		fixedPoint = getPortedValue(value);
	}
	
	public FixedPoint set(FixedPoint value) {
		fixedPoint = getPortedValue(value);
		return this;
	}
	
	public FixedPoint add(FixedPoint value) {
		fixedPoint += getPortedValue(value);
		return this;
	}

	public FixedPoint subtract(FixedPoint value) {
		fixedPoint -= getPortedValue(value);
		return this;
	}

	public FixedPoint multiply(FixedPoint value) {
		multiply(fixedPoint, getPortedValue(value));
		return this;
	}

	public FixedPoint divide(FixedPoint value) {		
		long reciprocal = 1;
		reciprocal <<= 62;
		reciprocal /= getPortedValue(value);
		multiply(fixedPoint, reciprocal);
		return this;
	}
	
	private void multiply(long value1, long value2) {
		long intPart1 = value1 >> FP_64_BIT;
		long intPart2 = value2 >> FP_64_BIT;
		
		long fracPart1 = value1 & FP_64_MASK;
		long fracPart2 = value2 & FP_64_MASK;
	
		fixedPoint = 0;
		fixedPoint += (intPart1 * intPart2) << FP_64_BIT;
		fixedPoint += (intPart1 * fracPart2);
		fixedPoint += (intPart2 * fracPart1);
		fixedPoint += ((fracPart1 * fracPart2) >> FP_64_BIT) & FP_64_MASK;
	}
	
	private long getPortedValue(FixedPoint value) {
		if(value.getBits() == getBits()) {
			return value.getLongValue();
		} else {
			return value.getLongValue() << FP_64_TO_32;
		}		
	}

	public int toIntValue() {
		return (int) toLongValue();
	}

	public long toLongValue() {
		return fixedPoint >> FP_64_BIT;
	}
	
	public float toFloatValue() {
		return (float)toDoubleValue();
	}

	public double toDoubleValue() {
		return (double)fixedPoint / FP_64_ONE;
	}
	
	public int getIntValue() {
		return (int) (getLongValue() >> FP_64_TO_32);
	}

	public long getLongValue() {
		return fixedPoint;
	}

	public int getBits() {
		return 64;
	}
}
