package com.johnsproject.jpge2.processing;

public class MathProcessor {

	// sine lookup table containing sine values from 0-90 degrees
	private static final short[] SIN_LUT = { 0, 572, 1144, 1715, 2286, 2856, 3425, 3993, 4560, 5126, 5690, 6252, 6813,
			7371, 7927, 8481, 9032, 9580, 10126, 10668, 11207, 11743, 12275, 12803, 13328, 13848, 14365, 14876, 15384,
			15886, 16384, 16877, 17364, 17847, 18324, 18795, 19261, 19720, 20174, 20622, 21063, 21498, 21926, 22348,
			22763, 23170, 23571, 23965, 24351, 24730, 25102, 25466, 25822, 26170, 26510, 26842, 27166, 27482, 27789,
			28088, 28378, 28660, 28932, 29197, 29452, 29698, 29935, 30163, 30382, 30592, 30792, 30983, 31164, 31336,
			31499, 31651, 31795, 31928, 32052, 32166, 32270, 32365, 32449, 32524, 32588, 32643, 32688, 32723, 32748,
			32763, 32767};

	/**
	 * FP_SHIFT and FP_VALUE are the values used to do integer to fixed point
	 * conversion by bit shifting. Bit shift left to convert to fixed point and
	 * right to get integer values.
	 */
	public static final long FP_SHIFT = 15, FP_VALUE = 1 << FP_SHIFT, FP_ROUND = 1 << (FP_SHIFT - 1);

	/**
	 * Returns the fixed point sine of the given angle.
	 * 
	 * @param angle
	 * @return
	 */
	public static long sin(long angle) {
		int a = (int) Math.abs(angle);
		int i = 0;
		int quadrant = 0;
		while (a >= i) {
			i += 90;
			quadrant++;
			if (quadrant > 4) {
				quadrant = 1;
			}
		}
		a = (a - i) + 90;
		switch (quadrant) {
		case 1:
			if (angle > 0)
				return SIN_LUT[a];
			return -(SIN_LUT[a]);
		case 2:
			if (angle > 0)
				return SIN_LUT[90 - a];
			return -(SIN_LUT[90 - a]);
		case 3:
			if (angle > 0)
				return -(SIN_LUT[a]);
			return (SIN_LUT[a]);
		case 4:
			if (angle > 0)
				return -(SIN_LUT[90 - a]);
			return (SIN_LUT[90 - a]);
		}
		return 0;
	}

	/**
	 * Returns the fixed point cosine of the given angle.
	 * 
	 * @param angle
	 * @return
	 */
	public static long cos(long angle) {
		int a = (int) Math.abs(angle);
		int i = 0;
		int quadrant = 0;
		while (a >= i) {
			i += 90;
			quadrant++;
			if (quadrant > 4) {
				quadrant = 1;
			}
		}
		a = (a - i) + 90;
		switch (quadrant) {
		case 1:
			return SIN_LUT[90 - a];
		case 2:
			return -(SIN_LUT[a]);
		case 3:
			return -(SIN_LUT[90 - a]);
		case 4:
			return (SIN_LUT[a]);
		}
		return 0;
	}

	/**
	 * Returns the fixed point tangent of the given angle.
	 * 
	 * @param angle
	 * @return
	 */
	public static long tan(long angle) {
		return angle != 89 ? ((sin(angle) << FP_SHIFT) / cos(angle)) : 15000;
	}

	/**
	 * Returns the given value in the range min-max.
	 * 
	 * @param value value to wrap.
	 * @param min
	 * @param max
	 * @return
	 */
	public static long wrap(long value, long min, long max) {
		long range = max - min;
		return (min + ((((value - min) % range) + range) % range));
	}

	/**
	 * Returns the given value from min to max. if value < min return min. if value
	 * > max return max.
	 * 
	 * @param value value to clamp.
	 * @param min
	 * @param max
	 * @return
	 */
	public static long clamp(long value, long min, long max) {
		if (value < min)
			return min;
		if (value > max)
			return max;
		return value;
	}

	private static long r = 545;

	/**
	 * Returns a pseudo generated random value.
	 * 
	 * @return
	 */
	public static long random() {
		r += r + (r & r);
		return r;
	}

	/**
	 * Returns a pseudo generated random value in the range min-max.
	 * 
	 * @param min lowest random value.
	 * @param max highest random value.
	 * @return
	 */
	public static long random(long min, long max) {
		r += r + (r & r);
		return min + (r & (max - min));
	}

	/**
	 * Returns the square root of the given number. If number < 0 the method returns
	 * 0.
	 * 
	 * @param number
	 * @return
	 */
	public static long sqrt(long number) {
		long res = 0;
		long add = 0x8000;
		long i;
		for (i = 0; i < 16; i++) {
			long temp = res | add;
			long g2 = temp * temp;
			if (number >= g2) {
				res = temp;
			}
			add >>= 1;
		}
		return res;
	}

	/**
	 * Returns the power of the given number.
	 * 
	 * @param base
	 * @param exp
	 * @return
	 */
	public static long pow(long base, long exp) {
		long result = base;
		for (int i = 1; i < exp; i++) {
			result = multiply(result, exp);
		}
		return result;
	}

	/**
	 * Returns the product of the multiplication of value1 and value2.
	 * 
	 * @param value1
	 * @param value2
	 * @return
	 */
	public static long multiply(long value1, long value2) {
		return (long) ((((long) value1 * (long) value2) + FP_ROUND) >> FP_SHIFT);
	}

	/**
	 * Returns the quotient of the division.
	 * 
	 * @param dividend
	 * @param divisor
	 * @return
	 */
	public static long divide(long dividend, long divisor) {
		if (divisor == 0)
			return 0;
		return (long) (((((long) (dividend + FP_ROUND) << FP_SHIFT) / divisor) + FP_ROUND) >> FP_SHIFT);
	}
}
