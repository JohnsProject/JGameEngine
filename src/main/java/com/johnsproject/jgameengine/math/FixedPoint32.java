package com.johnsproject.jgameengine.math;

public class FixedPoint32 implements FixedPoint {

	private int fixedPoint;
	
	public FixedPoint32() {
		fixedPoint = 0;
	}
	
	public FixedPoint32(float value) {
		fixedPoint = Math.round(value * FP_32_ONE);
	}
	
	public FixedPoint32(FixedPoint value) {
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
		long result = (long)fixedPoint * getPortedValue(value);
		fixedPoint = (int) (result >> FP_32_BIT);
		return this;
	}

	public FixedPoint divide(FixedPoint value) {
		long result = (long)fixedPoint << FP_32_BIT;
		fixedPoint = (int) (result / getPortedValue(value));
		return this;
	}
	
	private int getPortedValue(FixedPoint value) {
		if(value.getBits() == getBits()) {
			return value.getIntValue();
		} else {
			return (int)(value.getLongValue() >> FP_64_TO_32);
		}		
	}
	
	public int toIntValue() {
		return fixedPoint >> FP_32_BIT;
	}

	public long toLongValue() {
		return toIntValue();
	}
	
	public float toFloatValue() {
		return (float)fixedPoint / FP_32_ONE;
	}

	public double toDoubleValue() {
		return toFloatValue();
	}

	public int getIntValue() {
		return fixedPoint;
	}

	public long getLongValue() {
		return getIntValue();
	}

	public int getBits() {
		return 32;
	}
}
