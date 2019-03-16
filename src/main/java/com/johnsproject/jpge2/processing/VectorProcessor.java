package com.johnsproject.jpge2.processing;

public class VectorProcessor {

	public static final byte VECTOR_X = 0;
	public static final byte VECTOR_Y = 1;
	public static final byte VECTOR_Z = 2;
	public static final byte VECTOR_W = 3;
	
	public static final int[] VECTOR_UP = generate(0, 1, 0);
	public static final int[] VECTOR_DOWN = generate(0, -1, 0);
	public static final int[] VECTOR_RIGHT = generate(1, 0, 0);
	public static final int[] VECTOR_LEFT = generate(-1, 0, 0);
	public static final int[] VECTOR_FORWARD = generate(0, 0, 1);
	public static final int[] VECTOR_BACK = generate(0, 0, -1);
	public static final int[] VECTOR_ONE = generate(1, 1, 1);
	public static final int[] VECTOR_ZERO = generate(0, 0, 0);
	
	
	private static final int[] vectorCache1 = VectorProcessor.generate();
	
	/**
	 * Generates a vector using the given values and returns it.
	 * This vector can be used as location, rotation or scale vector.
	 * 
	 * @param x
	 * @param y
	 * @param z
	 * @return
	 */
	public static int[] generate(int x, int y, int z, int w) {
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
	public static int[] generate(int x, int y, int z) {
		return new int[] {x, y, z, 1};
	}

	/**
	 * Generates a vector using the given values and returns it.
	 * This vector can be used as location, rotation or scale vector.
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public static int[] generate(int x, int y) {
		return new int[] {x, y, 0, 1};
	}
	
	/**
	 * Generates a vector at (0,0,0) and returns it.
	 * This vector can be used as location, rotation or scale vector.
	 * 
	 * @return
	 */
	public static int[] generate() {
		return new int[] {0, 0, 0, 1};
	}

	/**
	 * Sets out equals the result of the scalar addition of vector1 and value.
	 * 
	 * @param vector1
	 * @param value
	 * @param out
	 */
	public static void add(int[] vector1, int value, int[] out) {
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
	public static void subtract(int[] vector1, int value, int[] out) {
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
	public static void multiply(int[] vector1, int value, int[] out) {
		out[VECTOR_X] = vector1[VECTOR_X] * value;
		out[VECTOR_Y] = vector1[VECTOR_Y] * value;
		out[VECTOR_Z] = vector1[VECTOR_Z] * value;
	}

	/**
	 * Sets out equals the result of the scalar division of vector1 and value.
	 * 
	 * @param vector1
	 * @param value
	 * @param out
	 */
	public static void divide(int[] vector1, int value, int[] out) {
		out[VECTOR_X] = vector1[VECTOR_X] / value;
		out[VECTOR_Y] = vector1[VECTOR_Y] / value;
		out[VECTOR_Z] = vector1[VECTOR_Z] / value;
	}

	/**
	 * Sets out equals the result of the addition of vector1 and vector2.
	 * 
	 * @param vector1
	 * @param vector2
	 * @param out
	 */
	public static void add(int[] vector1, int[] vector2, int[] out) {
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
	public static void subtract(int[] vector1, int[] vector2, int[] out) {
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
	public static void multiply(int[] vector1, int[] vector2, int[] out) {
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
	public static void divide(int[] vector1, int[] vector2, int[] out) {
		out[VECTOR_X] = vector1[VECTOR_X] / vector2[VECTOR_X];
		out[VECTOR_Y] = vector1[VECTOR_Y] / vector2[VECTOR_Y];
		out[VECTOR_Z] = vector1[VECTOR_Z] / vector2[VECTOR_Z];
	}

	/**
	 * Sets out equals the result of the multiplication of vector and matrix.
	 * 
	 * @param vector
	 * @param matrix
	 * @param out
	 */
	public static void multiply(int[] vector, int[][] matrix, int[] out) {
		// ensures that will return right values if vector is the same as out
		copy(vectorCache1, vector);
		for (int i = 0; i < 4; i++) {
			int result = MathProcessor.multiply(matrix[0][i], vectorCache1[VECTOR_X]);
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
	public static int magnitude(int[] vector) {
		int x = MathProcessor.multiply(vector[VECTOR_X], vector[VECTOR_X]);
		int y = MathProcessor.multiply(vector[VECTOR_Y], vector[VECTOR_Y]);
		int z = MathProcessor.multiply(vector[VECTOR_Z], vector[VECTOR_Z]);
		return MathProcessor.sqrt(x + y + z);
	}

	/**
	 * Returns the dot product of vector1 and vector2.
	 * 
	 * @param vector1
	 * @param vector2
	 * @return
	 */
	public static int dotProduct(int[] vector1, int[] vector2) {
		int x = MathProcessor.multiply(vector1[VECTOR_X], vector2[VECTOR_X]);
		int y = MathProcessor.multiply(vector1[VECTOR_Y], vector2[VECTOR_Y]);
		int z = MathProcessor.multiply(vector1[VECTOR_Z], vector2[VECTOR_Z]);
		return x + y + z;
	}

	/**
	 * Sets out equals the result of the cross product of vector1 and vector2.
	 * 
	 * @param vector1
	 * @param vector2
	 * @param out
	 */
	public static void crossProduct(int[] vector1, int[] vector2, int[] out) {
		out[VECTOR_X] = MathProcessor.multiply(vector1[VECTOR_Y], vector2[VECTOR_Z]) - MathProcessor.multiply(vector1[VECTOR_Z], vector2[VECTOR_Y]);
		out[VECTOR_Y] = MathProcessor.multiply(vector1[VECTOR_Z], vector2[VECTOR_X]) - MathProcessor.multiply(vector1[VECTOR_X], vector2[VECTOR_Z]);
		out[VECTOR_Z] = MathProcessor.multiply(vector1[VECTOR_X], vector2[VECTOR_Y]) - MathProcessor.multiply(vector1[VECTOR_Y], vector2[VECTOR_X]);
	}
	
	public static void normalize(int[] a, int[] out) {
		int m = magnitude(a);
		if (m != 0) {
			out[VECTOR_X] = (a[VECTOR_X] << MathProcessor.FP_SHIFT) / m;
			out[VECTOR_Y] = (a[VECTOR_Y] << MathProcessor.FP_SHIFT) / m;
			out[VECTOR_Z] = (a[VECTOR_Z] << MathProcessor.FP_SHIFT) / m;
		}
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
	}

	/**
	 * Inverts the sign of all values of the vector.
	 * 
	 * @param vector
	 * @return vector with inverted sign.
	 */
	public static int[] invert(int[] vector) {
		vector[VECTOR_X] = -vector[VECTOR_X];
		vector[VECTOR_Y] = -vector[VECTOR_Y];
		vector[VECTOR_Z] = -vector[VECTOR_Z];
		return vector;
	}

	/**
	 * Copies the value of vector to the target.
	 * 
	 * @param vector vector with values.
	 * @param target target vector.
	 * @return target containing values of vector.
	 */
	public static int[] copy(int[] target, int[] vector) {
		target[VECTOR_X] = vector[VECTOR_X];
		target[VECTOR_Y] = vector[VECTOR_Y];
		target[VECTOR_Z] = vector[VECTOR_Z];
		return target;
	}

	/**
	 * Returns a string containing the data of the given vector.
	 * 
	 * @param vector
	 * @return string with data.
	 */
	public static String toString(int[] vector) {
		String result = "(";
		for (int i = 0; i < vector.length; i++) {
			result += vector[i] + ", ";
		}
		result += ")";
		return result;
	}

}
