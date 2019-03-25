package com.johnsproject.jpge2.processing;

public class VectorProcessor {

	public static final byte VECTOR_X = 0;
	public static final byte VECTOR_Y = 1;
	public static final byte VECTOR_Z = 2;
	public static final byte VECTOR_W = 3;
	
	public static final long[] VECTOR_UP = generate(0, MathProcessor.FP_VALUE, 0);
	public static final long[] VECTOR_DOWN = generate(0, -MathProcessor.FP_VALUE, 0);
	public static final long[] VECTOR_RIGHT = generate(MathProcessor.FP_VALUE, 0, 0);
	public static final long[] VECTOR_LEFT = generate(-MathProcessor.FP_VALUE, 0, 0);
	public static final long[] VECTOR_FORWARD = generate(0, 0, MathProcessor.FP_VALUE);
	public static final long[] VECTOR_BACK = generate(0, 0, -MathProcessor.FP_VALUE);
	public static final long[] VECTOR_ONE = generate(MathProcessor.FP_VALUE, MathProcessor.FP_VALUE, MathProcessor.FP_VALUE);
	public static final long[] VECTOR_ZERO = generate(0, 0, 0);
	
	
	private static final long[] vectorCache1 = VectorProcessor.generate();
	private static final long[] vectorCache2 = VectorProcessor.generate();
	
	/**
	 * Generates a vector using the given values and returns it.
	 * This vector can be used as location, rotation or scale vector.
	 * 
	 * @param x
	 * @param y
	 * @param z
	 * @return
	 */
	public static long[] generate(long x, long y, long z, long w) {
		return new long[] {x, y, z, w};
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
	public static long[] generate(long x, long y, long z) {
		return new long[] {x, y, z, MathProcessor.FP_VALUE};
	}

	/**
	 * Generates a vector using the given values and returns it.
	 * This vector can be used as location, rotation or scale vector.
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public static long[] generate(long x, long y) {
		return new long[] {x, y, 0, MathProcessor.FP_VALUE};
	}
	
	/**
	 * Generates a vector at (0,0,0) and returns it.
	 * This vector can be used as location, rotation or scale vector.
	 * 
	 * @return
	 */
	public static long[] generate() {
		return new long[] {0, 0, 0, 1};
	}

	/**
	 * Sets out equals the result of the scalar addition of vector1 and value.
	 * 
	 * @param vector1
	 * @param value
	 * @param out
	 */
	public static void add(long[] vector1, long value, long[] out) {
		out[VECTOR_X] = vector1[VECTOR_X] + value;
		out[VECTOR_Y] = vector1[VECTOR_Y] + value;
		out[VECTOR_Z] = vector1[VECTOR_Z] + value;
	}

	/**
	 * Sets out equals the result of the scalar subtraction of vector1 and value.
	 * 
	 * @param vector1
	 * @param value
	 * @param out
	 */
	public static void subtract(long[] vector1, long value, long[] out) {
		out[VECTOR_X] = vector1[VECTOR_X] - value;
		out[VECTOR_Y] = vector1[VECTOR_Y] - value;
		out[VECTOR_Z] = vector1[VECTOR_Z] - value;
	}

	/**
	 * Sets out equals the result of the scalar multiplication of vector1 and value.
	 * 
	 * @param vector1
	 * @param value
	 * @param out
	 */
	public static void multiply(long[] vector1, long value, long[] out) {
		out[VECTOR_X] = MathProcessor.multiply(vector1[VECTOR_X], value);
		out[VECTOR_Y] = MathProcessor.multiply(vector1[VECTOR_Y], value);
		out[VECTOR_Z] = MathProcessor.multiply(vector1[VECTOR_Z], value);
	}

	/**
	 * Sets out equals the result of the scalar division of vector1 and value.
	 * 
	 * @param vector1
	 * @param value
	 * @param out
	 */
	public static void divide(long[] vector1, long value, long[] out) {
		out[VECTOR_X] = MathProcessor.divide(vector1[VECTOR_X], value);
		out[VECTOR_Y] = MathProcessor.divide(vector1[VECTOR_Y], value);
		out[VECTOR_Z] = MathProcessor.divide(vector1[VECTOR_Z], value);
	}

	/**
	 * Sets out equals the result of the addition of vector1 and vector2.
	 * 
	 * @param vector1
	 * @param vector2
	 * @param out
	 */
	public static void add(long[] vector1, long[] vector2, long[] out) {
		out[VECTOR_X] = vector1[VECTOR_X] + vector2[VECTOR_X];
		out[VECTOR_Y] = vector1[VECTOR_Y] + vector2[VECTOR_Y];
		out[VECTOR_Z] = vector1[VECTOR_Z] + vector2[VECTOR_Z];
	}

	/**
	 * Sets out equals the result of the subtraction of vector1 and vector2.
	 * 
	 * @param vector1
	 * @param vector2
	 * @param out
	 */
	public static void subtract(long[] vector1, long[] vector2, long[] out) {
		out[VECTOR_X] = vector1[VECTOR_X] - vector2[VECTOR_X];
		out[VECTOR_Y] = vector1[VECTOR_Y] - vector2[VECTOR_Y];
		out[VECTOR_Z] = vector1[VECTOR_Z] - vector2[VECTOR_Z];
	}
	
	/**
	 * Sets out equals the result of the multiplication of vector1 and vector2.
	 * 
	 * @param vector1
	 * @param vector2
	 * @param out
	 */
	public static void multiply(long[] vector1, long[] vector2, long[] out) {
		out[VECTOR_X] = MathProcessor.multiply(vector1[VECTOR_X], vector2[VECTOR_X]);
		out[VECTOR_Y] = MathProcessor.multiply(vector1[VECTOR_Y], vector2[VECTOR_Y]);
		out[VECTOR_Z] = MathProcessor.multiply(vector1[VECTOR_Z], vector2[VECTOR_Z]);
	}

	/**
	 * Sets out equals the result of the division of vector1 and vector2.
	 * 
	 * @param vector1
	 * @param vector2
	 * @param out
	 */
	public static void divide(long[] vector1, long[] vector2, long[] out) {
		out[VECTOR_X] = MathProcessor.divide(vector1[VECTOR_X], vector2[VECTOR_X]);
		out[VECTOR_Y] = MathProcessor.divide(vector1[VECTOR_Y], vector2[VECTOR_Y]);
		out[VECTOR_Z] = MathProcessor.divide(vector1[VECTOR_Z], vector2[VECTOR_Z]);
	}

	/**
	 * Sets out equals the result of the multiplication of vector and matrix.
	 * 
	 * @param vector
	 * @param matrix
	 * @param out
	 */
	public static void multiply(long[] vector, long[][] matrix, long[] out) {
		// ensures that will return right values if vector is the same as out
		copy(vectorCache1, vector);
		for (int i = 0; i < 4; i++) {
			long result = MathProcessor.multiply(matrix[0][i], vectorCache1[VECTOR_X]);
			result += MathProcessor.multiply(matrix[1][i], vectorCache1[VECTOR_Y]);
			result += MathProcessor.multiply(matrix[2][i], vectorCache1[VECTOR_Z]);
			result += MathProcessor.multiply(matrix[3][i], vectorCache1[VECTOR_W]);
			out[i] = result;
		}
	}

	/**
	 * Returns the magnitude of the given vector.
	 * 
	 * @param vector
	 * @return
	 */
	public static long magnitude(long[] vector) {
		return MathProcessor.sqrt(dotProduct(vector, vector) >> MathProcessor.FP_SHIFT);
	}

	/**
	 * Returns the dot product of vector1 and vector2.
	 * 
	 * @param vector1
	 * @param vector2
	 * @return
	 */
	public static long dotProduct(long[] vector1, long[] vector2) {
		long x = MathProcessor.multiply(vector1[VECTOR_X], vector2[VECTOR_X]);
		long y = MathProcessor.multiply(vector1[VECTOR_Y], vector2[VECTOR_Y]);
		long z = MathProcessor.multiply(vector1[VECTOR_Z], vector2[VECTOR_Z]);
		return x + y + z;
	}

	/**
	 * Sets out equals the result of the cross product of vector1 and vector2.
	 * 
	 * @param vector1
	 * @param vector2
	 * @param out
	 */
	public static void crossProduct(long[] vector1, long[] vector2, long[] out) {
		// ensures that will return right values if vector is the same as out
		copy(vectorCache1, vector1);
		copy(vectorCache2, vector2);
		out[VECTOR_X] = MathProcessor.multiply(vectorCache1[VECTOR_Y], vectorCache2[VECTOR_Z]) - MathProcessor.multiply(vectorCache1[VECTOR_Z], vectorCache2[VECTOR_Y]);
		out[VECTOR_Y] = MathProcessor.multiply(vectorCache1[VECTOR_Z], vectorCache2[VECTOR_X]) - MathProcessor.multiply(vectorCache1[VECTOR_X], vectorCache2[VECTOR_Z]);
		out[VECTOR_Z] = MathProcessor.multiply(vectorCache1[VECTOR_X], vectorCache2[VECTOR_Y]) - MathProcessor.multiply(vectorCache1[VECTOR_Y], vectorCache2[VECTOR_X]);
	}
	
	/**
	 * Sets out equals the normalized vector.
	 * 
	 * @param vector
	 * @param out
	 */
	public static void normalize(long[] vector, long[] out) {
		long magnitude = magnitude(vector);
		if (magnitude != 0) {
			out[VECTOR_X] = MathProcessor.divide(vector[VECTOR_X], magnitude);
			out[VECTOR_Y] = MathProcessor.divide(vector[VECTOR_Y], magnitude);
			out[VECTOR_Z] = MathProcessor.divide(vector[VECTOR_Z], magnitude);
		}
	}
	
	/**
	 * Sets out equals the vector reflected across reflectionVector.
	 * 
	 * @param vector
	 * @param reflectionVector
	 * @param out
	 */
	public static void reflect(long[] vector, long[] reflectionVector, long[] out) {
		copy(vectorCache1, reflectionVector);
		long dot = 2 * dotProduct(vector, vectorCache1);
		multiply(vectorCache1, dot, vectorCache1);
		subtract(vector, vectorCache1, out);
	}

	/**
	 * Checks if vector1 is equal to vector2.
	 * 
	 * @param vector1
	 * @param vector2
	 * @return true if vector1 is equal to vector2 if not false.
	 */
	public static boolean equals(long[] vector1, long[] vector2) {
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
	public static void swap(long[] vector1, long[] vector2) {
		long tmp = 0;
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
	 * Inverts the sign of all values of the vector.
	 * 
	 * @param vector
	 */
	public static void invert(long[] vector) {
		vector[VECTOR_X] = -vector[VECTOR_X];
		vector[VECTOR_Y] = -vector[VECTOR_Y];
		vector[VECTOR_Z] = -vector[VECTOR_Z];
		vector[VECTOR_W] = -vector[VECTOR_W];
	}

	/**
	 * Copies the value of vector to the target.
	 * 
	 * @param vector vector with values.
	 * @param target target vector.
	 */
	public static void copy(long[] target, long[] vector) {
		target[VECTOR_X] = vector[VECTOR_X];
		target[VECTOR_Y] = vector[VECTOR_Y];
		target[VECTOR_Z] = vector[VECTOR_Z];
		target[VECTOR_W] = vector[VECTOR_W];
	}

	/**
	 * Returns a string containing the data of the given vector.
	 * 
	 * @param vector
	 * @return
	 */
	public static String toString(long[] vector) {
		String result = "(";
		for (int i = 0; i < vector.length; i++) {
			result += vector[i] + ", ";
		}
		result += ")";
		return result;
	}

}
