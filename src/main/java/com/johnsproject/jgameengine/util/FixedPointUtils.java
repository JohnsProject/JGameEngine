package com.johnsproject.jgameengine.util;

/**
 * The FixedPointUtils class contains methods for generating fixed point numbers and 
 * performing fixed point math operations such as power, square root, multiply, 
 * divide, and some trigonometric functions.
 * 
 * @author John Ferraz Salomon
 */
public final class FixedPointUtils {
	
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
	
	public static final int FP_DEGREE_RAD = toFixedPoint(Math.PI / 180.0f);
	public static final int FP_RAD_DEGREE = toFixedPoint(180.0f / Math.PI);
	
	private static final short[] sinLUT = new short[] {
			0, 572, 1144, 1715, 2286, 2856, 3425, 3993, 4560, 5126, 5690, 6252, 6813, 7371, 7927, 8481,
			9032, 9580, 10126, 10668, 11207, 11743, 12275, 12803, 13328, 13848, 14365, 14876, 15384,
			15886, 16384, 16877, 17364, 17847, 18324, 18795, 19261, 19720, 20174, 20622, 21063, 21498,
			21926, 22348, 22763, 23170, 23571, 23965, 24351, 24730, 25102, 25466, 25822, 26170, 26510,
			26842, 27166, 27482, 27789, 28088, 28378, 28660, 28932, 29197, 29452, 29698, 29935, 30163,
			30382, 30592, 30792, 30983, 31164, 31336, 31499, 31651, 31795, 31928, 32052, 32166, 32270,
			32365, 32449, 32524, 32588, 32643, 32688, 32723, 32748, 32763, 32767
	};
	
	private FixedPointUtils() { }
	
	/**
	 * Returns the fixed point representation of value.
	 * 
	 * @param value
	 * @return
	 */
	public static int toFixedPoint(String value) {
		return toFixedPoint(Float.parseFloat(value));
	}
	
	/**
	 * Returns the fixed point representation of value.
	 * 
	 * @param value
	 * @return
	 */
	public static int toFixedPoint(double value) {
		return (int)Math.round(value * FP_ONE);
	}
	
	/**
	 * Returns the floating point representation of value.
	 * 
	 * @param value fixed point value.
	 * @return
	 */
	public static double toDouble(long value) {
		return (double)value / FP_ONE;
	}
	
	/**
	 * Returns the degrees converted to radians.
	 * 
	 * @param degrees
	 * @return
	 */
	public static int toRadians(int degrees) {
		return multiply(degrees, FP_DEGREE_RAD);
	}
	
	
	/**
	 * Returns the radians converted to degrees.
	 * 
	 * @param value
	 * @return
	 */
	public static int toDegrees(int radians) {
		return multiply(radians, FP_RAD_DEGREE);
	}
	
	/**
	 * Returns the fixed point sine of the specified angle.
	 * 
	 * @param degrees
	 * @return The sine of the specified angle.
	 */
	public static int sin(int degrees) {
		degrees >>= FP_BIT;
		degrees = ((degrees % 360) + 360) % 360;
		final int quadrant = degrees;
		degrees %= 90;
		if (quadrant < 90) {
			return sinLUT[degrees];
		}
		if (quadrant < 180) {
			return sinLUT[90 - degrees];
		}
		if (quadrant < 270) {
			return -sinLUT[degrees];
		}
		if (quadrant < 360) {
			return -sinLUT[90 - degrees];
		}
		return 0;
	}

	/**
	 * Returns the fixed point cosine of the specified angle.
	 * 
	 * @param degrees
	 * @return The cosine of the specified angle.
	 */
	public static int cos(int degrees) {
		degrees >>= FP_BIT;
		degrees = ((degrees % 360) + 360) % 360;
		final int quadrant = degrees;
		degrees %= 90;
		if (quadrant < 90) {
			return sinLUT[90 - degrees];
		}
		if (quadrant < 180) {
			return -sinLUT[degrees];
		}
		if (quadrant < 270) {
			return -sinLUT[90 - degrees];
		}
		if (quadrant < 360) {
			return sinLUT[degrees];
		}
		return 0;
	}

	/**
	 * Returns the fixed point tangent of the specified angle.
	 * 
	 * @param degrees
	 * @return The tangent of the specified angle.
	 */
	public static int tan(int degrees) {
		// no need to convert to long, the range of sine is 0 - 1
		return (sin(degrees) << FP_BIT) / cos(degrees);
	}
	
	/**
	 * Returns the fixed point angle in degrees of the specified fixed point sine.
	 * The angle is in the range -90 - 90.
 	 * 
	 * @param sine
	 * @return The angle of the specified sine.
	 */
	public static int asin(int sine) {
		final int absSine = Math.abs(sine);
		for (int i = 0; i < sinLUT.length; i++) {
			if(absSine == sinLUT[i]) {
				final int degrees = i << FP_BIT;
				if(sine > 0)
					return degrees;
				else
					return -degrees;
			}
		}
		return 0;
	}

	/**
	 * Returns the fixed point angle in degrees of the specified fixed point cosine.
	 * The angle is in the range 0 - 180.
 	 * 
	 * @param cosine
	 * @return The angle of the specified cosine.
	 */
	public static int acos(int cosine) {
		final int absCosine = Math.abs(cosine);
		for (int i = 0; i < sinLUT.length; i++) {
			if(absCosine == sinLUT[i]) {
				if(cosine < 0)
					return (i + 90) << FP_BIT;
				else
					return (90 - i) << FP_BIT;
			}
		}
		return 0;
	}
	
	/**
	 * Returns the given value in the range min-max.
	 * 
	 * @param value value to wrap.
	 * @param min
	 * @param max
	 * @return
	 */
	public static int normalize(int value, int min, int max) {
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
	public static int clamp(int value, int min, int max) {
		return Math.min(max, Math.max(value, min));
	}

	/**
	 * Returns a pseudo generated random value.
	 * 
	 * @return
	 */
	public static int random(int seed) {
		seed += seed + (seed & seed);
		return seed;
	}

	/**
	 * Returns a pseudo generated random value in the range min-max.
	 * 
	 * @param min lowest random value.
	 * @param max highest random value.
	 * @return
	 */
	public static int random(int seed, int min, int max) {
		return normalize(random(seed), min, max);
	}
	
	/**
	 * Returns the square root of the given number. If number < 0 the method returns
	 * 0.
	 * 
	 * @param number fixed point number.
	 * @return fixed point result.
	 */
	public static int sqrt(long number) {
		// integral part
		int num = (int)(number >> FP_BIT);
		if(num < 0) {
			return 0;
		}
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
	public static int pow(long base, int exp) {
		exp >>= FP_BIT;
		long result = FP_ONE;
		while (exp != 0) {
			if ((exp & 1) == 1) {
				result = (result * base + FP_HALF) >> FP_BIT;
			}
			exp >>= 1;
			base = (base * base + FP_HALF) >> FP_BIT;
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
	public static int multiply(long value1, long value2) {
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
	public static int divide(long dividend, long divisor) {
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
	public static String toString(int value) {
		float floatValue = (float)value / FP_ONE;
		return "" + floatValue;
	}
}
