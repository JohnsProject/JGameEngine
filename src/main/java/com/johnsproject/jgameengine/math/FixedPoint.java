package com.johnsproject.jgameengine.math;

public interface FixedPoint {
	
	public static final byte FP_32_BIT = 15;
	public static final int FP_32_ONE = 1 << FP_32_BIT;
	public static final int FP_32_MASK = FP_32_ONE - 1;
	
	public static final byte FP_64_BIT = 31;
	public static final long FP_64_ONE = 1l << FP_64_BIT;
	public static final long FP_64_MASK = FP_64_ONE - 1;
	
	public static final byte FP_64_TO_32 = FP_64_BIT - FP_32_BIT;
	
	/**
	 * Copies the specified {@link FixedPoint} into this FixedPoint.
	 * 
	 * @param value Value to assign.
	 * @return This FixedPoint, for further operations.
	 */
	FixedPoint set(FixedPoint value);
	
	/**
	 * Adds the specified {@link FixedPoint} to this FixedPoint.
	 * 
	 * @param value Value to add.
	 * @return This FixedPoint, for further operations.
	 */
	FixedPoint add(FixedPoint value);
	
	/**
	 * Subtracts the specified {@link FixedPoint} from this FixedPoint.
	 * 
	 * @param value Value to subtract.
	 * @return This FixedPoint, for further operations.
	 */
	FixedPoint subtract(FixedPoint value);
	
	/**
	 * Multiplies the specified {@link FixedPoint} with this FixedPoint,
	 * and assigns the result to this FixedPoint.
	 * 
	 * @param value Value to multiply.
	 * @return This FixedPoint, for further operations.
	 */
	FixedPoint multiply(FixedPoint value);
	
	/**
	 * Divides the specified {@link FixedPoint} with this FixedPoint,
	 * and assigns the result to this FixedPoint.
	 * 
	 * <br><br>
	 * The Operation is
	 * <code> this/value </code>
	 * 
	 * @param value Value to divide.
	 * @return This FixedPoint, for further operations.
	 */
	FixedPoint divide(FixedPoint value);
	
	/**
	 * Calculates the absolute value of this {@link FixedPoint} and 
	 * assigns it to this FixedPoint.
	 * If this FixedPoint is negative it will be converted into a positive number.
	 * If this FixedPoint is positive nothing happens.
	 * 
	 * @return This FixedPoint, for further operations.
	 */
	FixedPoint toAbsolute();
	
	/**
	 * Inverts the sign of this {@link FixedPoint}.
	 * If this FixedPoint is positive it will be converted to a negative number.
	 * If this FixedPoint is negative it will be converted to a positive number.
	 * 
	 * @return This FixedPoint, for further operations.
	 */
	FixedPoint toInverse();
	
	FixedPoint toRadians();
	
	FixedPoint toDegrees();
	
	/**
	 * Calculates the sine of this {@link FixedPoint} and assigns it to this FixedPoint.
	 * The value should be in {@link #toDegrees() degrees}.
	 * 
	 * @return This FixedPoint, for further operations.
	 */
	FixedPoint toSine();
	
	/**
	 * Calculates the cosine of this {@link FixedPoint} and assigns it to this FixedPoint.
	 * The value should be in {@link #toDegrees() degrees}.
	 * 
	 * @return This FixedPoint, for further operations.
	 */
	FixedPoint toCosine();
	
	/**
	 * Calculates the tangent of this {@link FixedPoint} and assigns it to this FixedPoint.
	 * The value should be in {@link #toDegrees() degrees}.
	 * 
	 * @return This FixedPoint, for further operations.
	 */
	FixedPoint toTangent();
	
	/**
	 * Calculates the square root of this {@link FixedPoint} and assigns it
	 * to this FixedPoint.
	 * 
	 * @return This FixedPoint, for further operations.
	 */
	FixedPoint toSquareRoot();
	
	/**
	 * Returns the integer part of this {@link FixedPoint} packed into a int.
	 * 
	 * @return The integer part of this FixedPoint.
	 */
	int toIntValue();
	
	/**
	 * Returns the integer part of this {@link FixedPoint} packed into a long.
	 * 
	 * @return The integer part of this FixedPoint.
	 */
	long toLongValue();
	
	/**
	 * Returns the floating point representation of this {@link FixedPoint}
	 * packed into a float.
	 * 
	 * @return This FixedPoint packed into a float.
	 */
	float toFloatValue();
	
	/**
	 * Returns the floating point representation of this {@link FixedPoint}
	 * packed into a double.
	 * 
	 * @return This FixedPoint packed into a double.
	 */
	double toDoubleValue();
	
	/**
	 * Returns this {@link FixedPoint} packed into a int.
	 * 
	 * @return This FixedPoint packed into a int.
	 */
	int getIntValue();
	
	/**
	 * Returns this {@link FixedPoint} packed into a long.
	 * 
	 * @return This FixedPoint packed into a long.
	 */
	long getLongValue();
	
	/**
	 * Returns the number of bits used to store this {@link FixedPoint}.
	 * 
	 * @return The number of bits used to store this FixedPoint.
	 */
	int getBits();
	
}
