/**
 * MIT License
 *
 * Copyright (c) 2018 John Salomon - John´s Project
 *  
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.johnsproject.jgameengine.library;

/**
 * The MathLibrary class contains methods for generating fixed point numbers and 
 * performing fixed point math operations such as power, square root, multiply, 
 * divide, and some trigonometric functions.
 * 
 * @author John Ferraz Salomon
 */
public class MathLibrary {
	

	private int random = 545;
	
	/**
	 * This is the bit representation of the default fixed point precision value. 
	 * That means that the 'point' is at the {@value #FP_BIT}th bit of the integer.
	 */
	public static final byte FP_BIT = 15;
	
	/**
	 * This is the integer representation of the default fixed point precision value. 
	 * It is the same as the fixed point '1'.
	 */
	public static final int FP_ONE = 1 << FP_BIT;
	
	/**
	 * It is the same as the fixed point '0.5'.
	 */
	public static final int FP_HALF = FP_ONE >> 1;
	
	public static final int FP_DEGREE_RAD = generate((float) (Math.PI / 180.0f));
	public static final int FP_RAD_DEGREE = generate((float) (180.0f / Math.PI));
	
	private int[] sinLUT;
	
	public MathLibrary() {
		sinLUT = new int[91];
		for (int angle = 0; angle < sinLUT.length; angle++) {
			sinLUT[angle] = generate(Math.sin(Math.toRadians(angle)));
		}
	}
	
	/**
	 * Returns the fixed point representation of value.
	 * 
	 * @param value
	 * @return
	 */
	public static int generate(double value) {
		return (int)(value * FP_ONE);
	}
	
	/**
	 * Returns the fixed point representation of value.
	 * 
	 * @param value
	 * @return
	 */
	public static int generate(float value) {
		return (int)(value * FP_ONE);
	}
	
	/**
	 * Returns the floating point representation of value.
	 * 
	 * @param value fixed point value.
	 * @return
	 */
	public static float generate(int value) {
		return (float)value / FP_ONE;
	}
	
	/**
	 * Returns the degrees converted to radians.
	 * 
	 * @param degrees
	 * @return
	 */
	public int toRadians(int degrees) {
		return multiply(degrees, FP_DEGREE_RAD);
	}
	
	
	/**
	 * Returns the radians converted to degrees.
	 * 
	 * @param value
	 * @return
	 */
	public int toDegrees(int radians) {
		return multiply(radians, FP_RAD_DEGREE);
	}
	
	/**
	 * Returns the fixed point sine of the given angle.
	 * 
	 * @param angle in fixed point degrees.
	 * @return
	 */
	public int sin(int angle) {
		angle >>= FP_BIT;
		angle = ((angle % 360) + 360) % 360;
		int quadrant = (angle / 90) + 1;
		angle %= 90;
		if (angle >= 0) {
			if (quadrant == 1) {
				return sinLUT[angle];
			}
			if (quadrant == 2) {
				return sinLUT[90 - angle];
			}
			if (quadrant == 3) {
				return -sinLUT[angle];
			}
			if (quadrant == 4) {
				return -sinLUT[90 - angle];
			}
		} else {
			if (quadrant == 1) {
				return -sinLUT[angle];
			}
			if (quadrant == 2) {
				return -sinLUT[90 - angle];
			}
			if (quadrant == 3) {
				return sinLUT[angle];
			}
			if (quadrant == 4) {
				return sinLUT[90 - angle];
			}
		}
		return 0;
	}

	/**
	 * Returns the fixed point cosine of the given angle.
	 * 
	 * @param angle in fixed point degrees.
	 * @return
	 */
	public int cos(int angle) {
		angle >>= FP_BIT;
		angle = ((angle % 360) + 360) % 360;
		int quadrant = (angle / 90) + 1;
		angle %= 90;
		if (quadrant == 1) {
			return sinLUT[90 - angle];
		}
		if (quadrant == 2) {
			return -sinLUT[angle];
		}
		if (quadrant == 3) {
			return -sinLUT[90 - angle];
		}
		if (quadrant == 4) {
			return sinLUT[angle];
		}
		return 0;
	}

	/**
	 * Returns the fixed point tangent of the given angle.
	 * 
	 * @param angle in fixed point degrees.
	 * @return
	 */
	public int tan(int angle) {
		return (sin(angle) << FP_BIT) / cos(angle);
	}

	/**
	 * Returns the given value in the range min-max.
	 * 
	 * @param value value to wrap.
	 * @param min
	 * @param max
	 * @return
	 */
	public int normalize(int value, int min, int max) {
		int range = max - min;
		return (min + ((((value - min) % range) + range) % range));
	}

	/**
	 * Returns the given value from min to max. if value < min return min. if value
	 * > max return max.
	 * 
	 * @param value
	 * @param min
	 * @param max
	 * @return
	 */
	public int clamp(int value, int min, int max) {
		return Math.min(max, Math.max(value, min));
	}

	/**
	 * Returns a pseudo generated random value.
	 * 
	 * @return
	 */
	public int random() {
		random += random + (random & random);
		return random;
	}

	/**
	 * Returns a pseudo generated random value in the range min-max.
	 * 
	 * @param min lowest random value.
	 * @param max highest random value.
	 * @return
	 */
	public int random(int min, int max) {
		random += random + (random & random);
		return normalize(random, min, max);
	}
	
	/**
	 * Returns the square root of the given number. If number < 0 the method returns
	 * 0.
	 * 
	 * @param number fixed point number.
	 * @return fixed point result.
	 */
	public int sqrt(int number) {
		// integral part
		int num = number >> FP_BIT;
		int c = 1 << 15;
		int g = c;
		if (g * g > num) {
			g ^= c;
		}
		c >>= 1;
		if (c != 0) {
			g |= c;
		}
		for (int i = 0; i < 15; i++) {
			if (g * g > num) {
				g ^= c;
			}
			c >>= 1;
			if (c == 0) {
				break;
			}
			g |= c;
		}
		long result = (g << FP_BIT);
		// fractional part
		final short increment = FP_ONE >> 7;
		for (; (result * result + FP_HALF) >> FP_BIT < number; result += increment);
		result -= increment;
		return (int)result;
	}
	
	/**
	 * Returns the power of the given number.
	 * 
	 * @param base fixed point number.
	 * @param exp fixed point number.
	 * @return fixed point result.
	 */
	public int pow(int base, int exp) {
		exp >>= FP_BIT;
		long lBase = base;
		long result = FP_ONE;
		while (exp != 0) {
			if ((exp & 1) == 1) {
				result = (result * lBase + FP_HALF) >> FP_BIT;
			}
			exp >>= 1;
			lBase = (lBase * lBase + FP_HALF) >> FP_BIT;
		}
		return (int) result;
	}

	/**
	 * Returns the product of the multiplication of value1 and value2.
	 * 
	 * @param value1 fixed point number.
	 * @param value2 fixed point number.
	 * @return fixed point result.
	 */
	public int multiply(long value1, long value2) {
		long result = value1 * value2 + FP_HALF;
		return (int) (result >> FP_BIT);
	}

	/**
	 * Returns the quotient of the division.
	 * 
	 * @param dividend fixed point number.
	 * @param divisor not fixed point number.
	 * @return fixed point result.
	 */
	public int divide(long dividend, long divisor) {
		long result = dividend << FP_BIT;
		result /= divisor;
		return (int) result;
	}
	
	/**
	 * Returns a string containing the data of the given fixed point value.
	 * 
	 * @param vector fixed point number.
	 * @return
	 */
	public String toString(int value) {
		float floatValue = (float)value / FP_ONE;
		return "" + floatValue;
	}
}
