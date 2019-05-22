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
package com.johnsproject.jpge2.library;

/**
 * The MathProcessor class contains methods for performing fixed point math operations 
 * such as power, square root, multiply, divide, and some trigonometric functions.
 * 
 * @author John Ferraz Salomon
 *
 */
public class MathLibrary {
	
	// sine lookup table containing sine values from 0-90 degrees
	private static final short[] SIN_LUT = { 0, 18, 36, 54, 71, 89, 107, 125, 143, 160, 178, 195, 213, 230, 248, 265,
			282, 299, 316, 333, 350, 367, 384, 400, 416, 433, 449, 465, 481, 496, 512, 527, 543, 558, 573, 587, 602,
			616, 630, 644, 658, 672, 685, 698, 711, 724, 737, 749, 761, 773, 784, 796, 807, 818, 828, 839, 849, 859,
			868, 878, 887, 896, 904, 912, 920, 928, 935, 943, 949, 956, 962, 968, 974, 979, 984, 989, 994, 998, 1002,
			1005, 1008, 1011, 1014, 1016, 1018, 1020, 1022, 1023, 1023, 1024, 1024};

	private int random = 545;
	
	/**
	 * This is the bit representation of the default fixed point precision value. 
	 * That means that the 'point' is at the {@value #FP_BITS}th bit of the integer.
	 */
	public static final byte FP_BITS = 10;
	
	/**
	 * This is the integer representation of the default fixed point precision value. 
	 * It is the same as the fixed point '1'.
	 */
	public static final short FP_ONE = 1 << FP_BITS;
	
	/**
	 * It is the same as the fixed point '0.5'.
	 */
	public static final short FP_HALF = FP_ONE >> 1;
	
	public MathLibrary() {}
	
	/**
	 * Returns the fixed point representation of value.
	 * 
	 * @param value
	 * @return
	 */
	public int generate(float value) {
		return (int)(value * FP_ONE);
	}
	
	/**
	 * Returns the fixed point sine of the given angle.
	 * 
	 * @param angle in fixed point degrees.
	 * @return
	 */
	public int sin(int angle) {
		angle >>= FP_BITS;
		angle = ((angle % 360) + 360) % 360;
		int quadrant = (angle / 90) + 1;
		angle %= 90;
		if (angle >= 0) {
			if (quadrant == 1) {
				return SIN_LUT[angle];
			}
			if (quadrant == 2) {
				return SIN_LUT[90 - angle];
			}
			if (quadrant == 3) {
				return -SIN_LUT[angle];
			}
			if (quadrant == 4) {
				return -SIN_LUT[90 - angle];
			}
		} else {
			if (quadrant == 1) {
				return -SIN_LUT[angle];
			}
			if (quadrant == 2) {
				return -SIN_LUT[90 - angle];
			}
			if (quadrant == 3) {
				return SIN_LUT[angle];
			}
			if (quadrant == 4) {
				return SIN_LUT[90 - angle];
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
		angle >>= FP_BITS;
		angle = ((angle % 360) + 360) % 360;
		int quadrant = (angle / 90) + 1;
		angle %= 90;
		if (quadrant == 1) {
			return SIN_LUT[90 - angle];
		}
		if (quadrant == 2) {
			return -SIN_LUT[angle];
		}
		if (quadrant == 3) {
			return -SIN_LUT[90 - angle];
		}
		if (quadrant == 4) {
			return SIN_LUT[angle];
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
		return (sin(angle) << FP_BITS) / cos(angle);
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
		number >>= FP_BITS;
		int c = 0x8000;
		int g = 0x8000;

		if (g * g > number) {
			g ^= c;
		}
		c >>= 1;
		if (c == 0) {
			return g << FP_BITS;
		}
		g |= c;
		for (int i = 0; i < 15; i++) {
			if (g * g > number) {
				g ^= c;
			}
			c >>= 1;
			if (c == 0) {
				return g << FP_BITS;
			}
			g |= c;
		}
		return g << FP_BITS;
	}
	
	/**
	 * Returns the power of the given number.
	 * 
	 * @param base fixed point number.
	 * @param exp not fixed point number.
	 * @return fixed point result.
	 */
	public int pow(int base, int exp) {
		long lBase = base;
		long result = FP_ONE;
		while (exp != 0) {
			if ((exp & 1) == 1) {
				result = (result * lBase + FP_HALF) >> FP_BITS;
			}
			exp >>= 1;
			lBase = (lBase * lBase + FP_HALF) >> FP_BITS;
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
	public int multiply(int value1, int value2) {
		long a = value1;
		long b = value2;
		long result = a * b + FP_HALF;
		return (int) (result >> FP_BITS);
	}

	/**
	 * Returns the quotient of the division.
	 * 
	 * @param dividend fixed point number.
	 * @param divisor not fixed point number.
	 * @return fixed point result.
	 */
	public int divide(int dividend, int divisor) {
		long result = (long)dividend << FP_BITS;
		result += FP_HALF;
		result /= divisor;
		return (int) result;
	}
}
