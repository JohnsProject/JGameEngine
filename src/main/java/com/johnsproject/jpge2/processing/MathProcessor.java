package com.johnsproject.jpge2.processing;

public class MathProcessor {

	// sine lookup table containing sine values from 0-90 degrees
	private static final short[] SIN_LUT = { 0, 18, 36, 54, 71, 89, 107, 125, 143, 160, 178, 195, 213, 230, 248, 265,
			282, 299, 316, 333, 350, 367, 384, 400, 416, 433, 449, 465, 481, 496, 512, 527, 543, 558, 573, 587, 602,
			616, 630, 644, 658, 672, 685, 698, 711, 724, 737, 749, 761, 773, 784, 796, 807, 818, 828, 839, 849, 859,
			868, 878, 887, 896, 904, 912, 920, 928, 935, 943, 949, 956, 962, 968, 974, 979, 984, 989, 994, 998, 1002,
			1005, 1008, 1011, 1014, 1016, 1018, 1020, 1022, 1023, 1023, 1024, 1024 };

	/**
	 * FP_SHIFT and FP_VALUE are the values used to do integer to fixed point
	 * conversion by bit shifting. Bit shift left to convert to fixed point and
	 * right to get integer values.
	 */
	public static final int FP_SHIFT = 10, FP_VALUE = 1 << FP_SHIFT, FP_ROUND = 1 << (FP_SHIFT - 1);

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
	 * @param min
	 * @param max
	 * @return
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
	 * @param min
	 * @param max
	 * @return
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
	 * @return
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
	 * @return
	 */
	public static int random(int min, int max) {
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
	 * @param base
	 * @param exp
	 * @return
	 */
	public static int pow(int base, int exp) {
		int result = base;
		for (int i = 1; i < exp; i++) {
			result = result * result;
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
	public static int multiply(int value1, int value2) {
		return (int) ((((long) value1 * (long) value2) + FP_ROUND) >> FP_SHIFT);
	}

	/**
	 * Returns the quotient of the division.
	 * 
	 * @param dividend
	 * @param divisor
	 * @return
	 */
	public static int divide(int dividend, int divisor) {
		if (divisor == 0)
			return 0;
		return (int) (((((long) dividend << FP_SHIFT) / divisor) + FP_ROUND) >> FP_SHIFT);
	}
}
