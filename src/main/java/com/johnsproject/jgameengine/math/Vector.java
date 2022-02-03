package com.johnsproject.jgameengine.math;

import static com.johnsproject.jgameengine.math.FixedPoint.*;

/**
 * The Vector class contains methods for generating vectors and performing vector 
 * operations such as add, subtract, multiply, divide, cross product, normalize, rotate, swap.
 * 
 * @author John Ferraz Salomon
 */
public final class Vector {
	
	public static final byte VECTOR_X = 0;
	public static final byte VECTOR_Y = 1;
	public static final byte VECTOR_Z = 2;
	public static final byte VECTOR_W = 3;
	public static final byte VECTOR_SIZE = 4;
	
	public static final int[] VECTOR_UP = new int[] {0, FP_ONE, 0, 0};
	public static final int[] VECTOR_DOWN = new int[] {0, -FP_ONE, 0, 0};
	public static final int[] VECTOR_RIGHT = new int[] {FP_ONE, 0, 0, 0};
	public static final int[] VECTOR_LEFT = new int[] {-FP_ONE, 0, 0, 0};
	public static final int[] VECTOR_FORWARD = new int[] {0, 0, -FP_ONE, 0};
	public static final int[] VECTOR_BACK = new int[] {0, 0, FP_ONE, 0};
	public static final int[] VECTOR_ONE = new int[] {FP_ONE, FP_ONE, FP_ONE, FP_ONE};
	public static final int[] VECTOR_ZERO = new int[] {0, 0, 0, FP_ONE};
	
	private Vector() {}
	
	public static int[] toVector(double x, double y, double z, double w) {
		int fpX = FixedPoint.toFixedPoint(x);
		int fpY = FixedPoint.toFixedPoint(y);
		int fpZ = FixedPoint.toFixedPoint(z);
		int fpW = FixedPoint.toFixedPoint(w);
		return toVector(fpX, fpY, fpZ, fpW);
	}
	
	public static int[] toVector(double x, double y, double z) {
		int fpX = FixedPoint.toFixedPoint(x);
		int fpY = FixedPoint.toFixedPoint(y);
		int fpZ = FixedPoint.toFixedPoint(z);
		return toVector(fpX, fpY, fpZ);
	}
	
	public static int[] toVector(double x, double y) {
		int fpX = FixedPoint.toFixedPoint(x);
		int fpY = FixedPoint.toFixedPoint(y);
		return toVector(fpX, fpY);
	}
	
	/**
	 * Generates a vector using the given values and returns it.
	 * This vector can be used as location, rotation or scale vector.
	 * 
	 * @param x
	 * @param y
	 * @param z
	 * @return
	 */
	public static int[] toVector(int x, int y, int z, int w) {
		return new int[] {x, y, z, w};
	}
	
	/**
	 * Generates a vector using the given values and returns it.
	 * This vector can be used as location, rotation or scale vector.
	 *  
	 * @param x
	 * @param y
	 * @param z
	 * @return
	 */
	public static int[] toVector(int x, int y, int z) {
		return toVector(x, y, z, FP_ONE);
	}

	/**
	 * Generates a vector using the given values and returns it.
	 * This vector can be used as location, rotation or scale vector.
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public static int[] toVector(int x, int y) {
		return toVector(x, y, 0, FP_ONE);
	}
	
	/**
	 * Generates a vector at (0,0,0) and returns it.
	 * This vector can be used as location, rotation or scale vector.
	 * 
	 * @return
	 */
	public static int[] emptyVector() {
		return toVector(0, 0, 0, FP_ONE);
	}

	/**
	 * Sets result equals the result of the scalar addition of vector and value.
	 * 
	 * @param vector
	 * @param value
	 * @param result
	 */
	public static int[] add(int[] vector, int value) {
		vector[VECTOR_X] = vector[VECTOR_X] + value;
		vector[VECTOR_Y] = vector[VECTOR_Y] + value;
		vector[VECTOR_Z] = vector[VECTOR_Z] + value;
		return vector;
	}

	/**
	 * Sets result equals the result of the scalar subtraction of vector and value.
	 * 
	 * @param vector
	 * @param value
	 * @param result
	 */
	public static int[] subtract(int[] vector, int value) {
		vector[VECTOR_X] = vector[VECTOR_X] - value;
		vector[VECTOR_Y] = vector[VECTOR_Y] - value;
		vector[VECTOR_Z] = vector[VECTOR_Z] - value;
		return vector;
	}

	/**
	 * Sets result equals the result of the scalar multiplication of vector and value.
	 * 
	 * @param vector
	 * @param value
	 * @param result
	 */
	public static int[] multiply(int[] vector, int value) {
		vector[VECTOR_X] = FixedPoint.multiply(vector[VECTOR_X], value);
		vector[VECTOR_Y] = FixedPoint.multiply(vector[VECTOR_Y], value);
		vector[VECTOR_Z] = FixedPoint.multiply(vector[VECTOR_Z], value);
		return vector;
	}

	/**
	 * Sets result equals the result of the scalar division of vector and value.
	 * 
	 * @param vector
	 * @param value
	 * @param result
	 */
	public static int[] divide(int[] vector, int value) {
		vector[VECTOR_X] = FixedPoint.divide(vector[VECTOR_X], value);
		vector[VECTOR_Y] = FixedPoint.divide(vector[VECTOR_Y], value);
		vector[VECTOR_Z] = FixedPoint.divide(vector[VECTOR_Z], value);
		return vector;
	}

	/**
	 * Sets result equals the result of the addition of vector1 and vector2.
	 * 
	 * @param vector1
	 * @param vector2
	 * @param result
	 */
	public static int[] add(int[] vector1, int[] vector2) {
		vector1[VECTOR_X] = vector1[VECTOR_X] + vector2[VECTOR_X];
		vector1[VECTOR_Y] = vector1[VECTOR_Y] + vector2[VECTOR_Y];
		vector1[VECTOR_Z] = vector1[VECTOR_Z] + vector2[VECTOR_Z];
		return vector1;
	}

	/**
	 * Sets result equals the result of the subtraction of vector1 and vector2.
	 * 
	 * @param vector1
	 * @param vector2
	 * @param result
	 */
	public static int[] subtract(int[] vector1, int[] vector2) {
		vector1[VECTOR_X] = vector1[VECTOR_X] - vector2[VECTOR_X];
		vector1[VECTOR_Y] = vector1[VECTOR_Y] - vector2[VECTOR_Y];
		vector1[VECTOR_Z] = vector1[VECTOR_Z] - vector2[VECTOR_Z];
		return vector1;
	}
	
	/**
	 * Sets result equals the result of the multiplication of vector1 and vector2.
	 * 
	 * @param vector1
	 * @param vector2
	 * @param result
	 */
	public static int[] multiply(int[] vector1, int[] vector2) {
		vector1[VECTOR_X] = FixedPoint.multiply(vector1[VECTOR_X], vector2[VECTOR_X]);
		vector1[VECTOR_Y] = FixedPoint.multiply(vector1[VECTOR_Y], vector2[VECTOR_Y]);
		vector1[VECTOR_Z] = FixedPoint.multiply(vector1[VECTOR_Z], vector2[VECTOR_Z]);
		return vector1;
	}

	/**
	 * Sets result equals the result of the division of vector1 and vector2.
	 * 
	 * @param vector1
	 * @param vector2
	 * @param result
	 */
	public static int[] divide(int[] vector1, int[] vector2) {
		vector1[VECTOR_X] = FixedPoint.divide(vector1[VECTOR_X], vector2[VECTOR_X]);
		vector1[VECTOR_Y] = FixedPoint.divide(vector1[VECTOR_Y], vector2[VECTOR_Y]);
		vector1[VECTOR_Z] = FixedPoint.divide(vector1[VECTOR_Z], vector2[VECTOR_Z]);
		return vector1;
	}

	/**
	 * Sets result equals the result of the multiplication of vector and matrix.
	 * 
	 * @param vector
	 * @param matrix
	 * @param result
	 */
	public static int[] multiply(int[] vector, int[][] matrix) {
		final int x = vector[VECTOR_X];
		final int y = vector[VECTOR_Y];
		final int z = vector[VECTOR_Z];
		int res = FixedPoint.multiply(matrix[0][VECTOR_X], x);
		res += FixedPoint.multiply(matrix[1][VECTOR_X], y);
		res += FixedPoint.multiply(matrix[2][VECTOR_X], z);
		vector[VECTOR_X] = res + matrix[3][VECTOR_X];
		
		res = FixedPoint.multiply(matrix[0][VECTOR_Y], x);
		res += FixedPoint.multiply(matrix[1][VECTOR_Y], y);
		res += FixedPoint.multiply(matrix[2][VECTOR_Y], z);
		vector[VECTOR_Y] = res + matrix[3][VECTOR_Y];
		
		res = FixedPoint.multiply(matrix[0][VECTOR_Z], x);
		res += FixedPoint.multiply(matrix[1][VECTOR_Z], y);
		res += FixedPoint.multiply(matrix[2][VECTOR_Z], z);
		vector[VECTOR_Z] = res + matrix[3][VECTOR_Z];
		
		final int matrix33 = matrix[3][VECTOR_W];
		if(matrix33 != FP_ONE) {
			vector[VECTOR_W] = FP_ONE;
			res = FixedPoint.multiply(matrix[0][VECTOR_W], x);
			res += FixedPoint.multiply(matrix[1][VECTOR_W], y);
			res += FixedPoint.multiply(matrix[2][VECTOR_W], z);
			int w = res + matrix33;
			
			if(w != FP_ONE) {
				final int precisionBit = FP_BIT >> 1;
				w = FixedPoint.divide(FP_ONE << precisionBit, w == 0 ? 1 : w);
				vector[VECTOR_X] = FixedPoint.multiply(vector[VECTOR_X], w) >> precisionBit;
				vector[VECTOR_Y] = FixedPoint.multiply(vector[VECTOR_Y], w) >> precisionBit;
				vector[VECTOR_Z] = FixedPoint.multiply(vector[VECTOR_Z], w) >> precisionBit;
			}
		}
		return vector;
	}

	/**
	 * Returns the length of the given vector.
	 * 
	 * @param vector
	 * @return
	 */
	public static int length(int[] vector) {
		return FixedPoint.sqrt(squaredLength(vector));
	}
	
	public static long squaredLength(int[] vector) {
		// long is needed because it's squared and might cause overflow
		return dotProduct(vector, vector);
	}

	/**
	 * Returns the dot product of vector1 and vector2.
	 * 
	 * @param vector1
	 * @param vector2
	 * @return
	 */
	public static long dotProduct(int[] vector1, int[] vector2) {
		// long is needed because it vector1 and 2 might be the same so it would be squared and cause overflow
		long x = FixedPoint.multiply(vector1[VECTOR_X], vector2[VECTOR_X]);
		long y = FixedPoint.multiply(vector1[VECTOR_Y], vector2[VECTOR_Y]);
		long z = FixedPoint.multiply(vector1[VECTOR_Z], vector2[VECTOR_Z]);
		return x + y + z;
	}
	
	/**
	 * Returns the distance between vector1 and vector2.
	 * 
	 * @param vector1
	 * @param vector2
	 * @return
	 */
	public static int distance(int[] vector1, int[] vector2) {
		return FixedPoint.sqrt(squaredDistance(vector1, vector2));
	}
	
	/**
	 * Returns the averaged distance between vector1 and vector2.
	 * Averaged distance isn't the correct way to get the distace, 
	 * but its faster, its just
	 * <pre>
	 * result = vector2 - vector1
	 * return (abs(resultX) + abs(resultY) + abs(resultZ)) / 3
	 * </pre>
	 * To get correct distance use {@link #distance} method.
	 * 
	 * @param vector1
	 * @param vector2
	 * @return
	 */
	public static long squaredDistance(int[] vector1, int[] vector2) {
		// long is needed because it's squared and might cause overflow
		long x = vector1[VECTOR_X] - vector2[VECTOR_X];
		long y = vector1[VECTOR_Y] - vector2[VECTOR_Y];
		long z = vector1[VECTOR_Z] - vector2[VECTOR_Z];
		x = FixedPoint.multiply(x, x);
		y = FixedPoint.multiply(y, y);
		z = FixedPoint.multiply(z, z);
		return (x + y + z);
	}

	/**
	 * Sets result equals the result of the cross product of vector1 and vector2.
	 * 
	 * @param vector1
	 * @param vector2
	 * @param result
	 */
	public static int[] crossProduct(int[] vector1, int[] vector2, int[] result) {
		result[VECTOR_X] = FixedPoint.multiply(vector1[VECTOR_Y], vector2[VECTOR_Z]);
		result[VECTOR_Y] = FixedPoint.multiply(vector1[VECTOR_Z], vector2[VECTOR_X]);
		result[VECTOR_Z] = FixedPoint.multiply(vector1[VECTOR_X], vector2[VECTOR_Y]);
		result[VECTOR_X] -= FixedPoint.multiply(vector1[VECTOR_Z], vector2[VECTOR_Y]);
		result[VECTOR_Y] -= FixedPoint.multiply(vector1[VECTOR_X], vector2[VECTOR_Z]);
		result[VECTOR_Z] -= FixedPoint.multiply(vector1[VECTOR_Y], vector2[VECTOR_X]);
		return result;
	}
	
	/**
	 * Sets result equals the normalized vector.
	 * 
	 * @param vector
	 * @param result
	 */
	public static int[] normalize(int[] vector) {
		int magnitude = FixedPoint.divide(FP_ONE, length(vector) + 1);
		vector[VECTOR_X] = FixedPoint.multiply(vector[VECTOR_X], magnitude);
		vector[VECTOR_Y] = FixedPoint.multiply(vector[VECTOR_Y], magnitude);
		vector[VECTOR_Z] = FixedPoint.multiply(vector[VECTOR_Z], magnitude);
		return vector;
	}
	
	/**
	 * Checks if vector1 is equal to vector2.
	 * 
	 * @param vector1
	 * @param vector2
	 * @return true if vector1 is equal to vector2 if not false.
	 */
	public static boolean equals(int[] vector1, int[] vector2) {
		if (vector1[VECTOR_X] != vector2[VECTOR_X])
			return false;
		if (vector1[VECTOR_Y] != vector2[VECTOR_Y])
			return false;
		if (vector1[VECTOR_Z] != vector2[VECTOR_Z])
			return false;
		if (vector1[VECTOR_W] != vector2[VECTOR_W])
			return false;
		return true;
	}

	/**
	 * Swaps the values of vector1 and vector2.
	 * 
	 * @param vector1
	 * @param vector2
	 */
	public static void swap(int[] vector1, int[] vector2) {
		int tmp = 0;
		// swap x values
		tmp = vector1[VECTOR_X];
		vector1[VECTOR_X] = vector2[VECTOR_X];
		vector2[VECTOR_X] = tmp;
		// swap y values
		tmp = vector1[VECTOR_Y];
		vector1[VECTOR_Y] = vector2[VECTOR_Y];
		vector2[VECTOR_Y] = tmp;
		// swap z values
		tmp = vector1[VECTOR_Z];
		vector1[VECTOR_Z] = vector2[VECTOR_Z];
		vector2[VECTOR_Z] = tmp;
		// swap w values
		tmp = vector1[VECTOR_W];
		vector1[VECTOR_W] = vector2[VECTOR_W];
		vector2[VECTOR_W] = tmp;
	}

	/**
	 * Sets result equals the values of vector with inverted sign.
	 * 
	 * @param vector
	 * @param result
	 */
	public static int[] invert(int[] vector) {
		vector[VECTOR_X] = -vector[VECTOR_X];
		vector[VECTOR_Y] = -vector[VECTOR_Y];
		vector[VECTOR_Z] = -vector[VECTOR_Z];
		vector[VECTOR_W] = -vector[VECTOR_W];
		return vector;
	}

	/**
	 * Copies the value of vector to the target.
	 * 
	 * @param vector vector with values.
	 * @param target target vector.
	 */
	public static int[] copy(int[] target, int[] vector) {
		target[VECTOR_X] = vector[VECTOR_X];
		target[VECTOR_Y] = vector[VECTOR_Y];
		target[VECTOR_Z] = vector[VECTOR_Z];
		target[VECTOR_W] = vector[VECTOR_W];
		return target;
	}

	/**
	 * Returns a string containing the data of the given vector.
	 * 
	 * @param vector
	 * @return
	 */
	public static String toString(int[] vector) {
		String result = "(";
		result += FixedPoint.toDouble(vector[0]) + ", ";
		result += FixedPoint.toDouble(vector[1]) + ", ";
		result += FixedPoint.toDouble(vector[2]) + ", ";
		result += FixedPoint.toDouble(vector[3]);
		result += ")";
		return result;
	}

}
