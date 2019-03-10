package com.johnsproject.jpge2.processing;

public class MathProcessor {

	// sine lookup table containing sine values from 0-90 degrees
	private static final short[] SIN_LUT = { 0, 71, 143, 214, 286, 357, 428, 499, 570, 641, 711, 782, 852, 921, 991,
			1060, 1129, 1198, 1266, 1334, 1401, 1468, 1534, 1600, 1666, 1731, 1796, 1860, 1923, 1986, 2048, 2110, 2171,
			2231, 2290, 2349, 2408, 2465, 2522, 2578, 2633, 2687, 2741, 2793, 2845, 2896, 2946, 2996, 3044, 3091, 3138,
			3183, 3228, 3271, 3314, 3355, 3396, 3435, 3474, 3511, 3547, 3582, 3617, 3650, 3681, 3712, 3742, 3770, 3798,
			3824, 3849, 3873, 3896, 3917, 3937, 3956, 3974, 3991, 4006, 4021, 4034, 4046, 4056, 4065, 4074, 4080, 4086,
			4090, 4094, 4095, 4096 };

	/**
	 * FP_SHIFT and FP_VALUE are the values used to do integer to fixed point
	 * conversion by bit shifting. Bit shift left to convert to fixed point and
	 * right to get integer values.
	 */
	public static final int FP_SHIFT = 12, FP_VALUE = 1 << FP_SHIFT, FP_ROUND = 1 << (FP_SHIFT - 1);

	/**
	 * Returns the fixed point sine of the given angle.
	 * 
	 * @param angle
	 * @return
	 */
	public static int sin(int angle) {
		int a = Math.abs(angle);
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
	public static int cos(int angle) {
		int a = Math.abs(angle);
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
	public static int tan(int angle) {
		return angle != 89 ? ((sin(angle) << FP_SHIFT) / cos(angle)) : 15000;
	}

	/**
	 * Returns the given value in the range min-max.
	 * 
	 * @param value value to wrap.
	 * @param min   min value.
	 * @param max   max value.
	 * @return value in the range min-max.
	 */
	public static int wrap(int value, int min, int max) {
		int range = max - min;
		return (min + ((((value - min) % range) + range) % range));
	}

	/**
	 * Returns the given value from min to max. if value < min return min. if value
	 * > max return max.
	 * 
	 * @param value value to clamp.
	 * @param min   min value.
	 * @param max   max value.
	 * @return clamped value.
	 */
	public static int clamp(int value, int min, int max) {
		if (value < min)
			return min;
		if (value > max)
			return max;
		return value;
	}

	private static int r = 545;

	/**
	 * Returns a pseudo generated random value.
	 * 
	 * @return a pseudo generated random value.
	 */
	public static int random() {
		r += r + (r & r);
		return r;
	}

	/**
	 * Returns a pseudo generated random value in the range min-max.
	 * 
	 * @param min lowest random value.
	 * @param max highest random value.
	 * @return a pseudo generated random value.
	 */
	public static int random(int min, int max) {
		r += r + (r & r);
		return min + (r & (max - min));
	}

	/**
	 * Returns the square root of the given number. If number < 0 the method returns
	 * 0.
	 * 
	 * @param number number.
	 * @return square root of the given number.
	 */
	public static int sqrt(int number) {
		int res = 0;
		int add = 0x8000;
		int i;
		for (i = 0; i < 16; i++) {
			int temp = res | add;
			int g2 = temp * temp;
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
	 * @param base base.
	 * @param exp  exp.
	 * @return power of the given number.
	 */
	public static int pow(int base, int exp) {
		int result = base;
		for (int i = 1; i < exp; i++) {
			result *= base;
		}
		return result;
	}
}
