/**
 * MIT License
 *
 * Copyright (c) 2018 John Salomon - John´s Project
 *  
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software withresult restriction, including withresult limitation the rights
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

public class VectorLibrary {

	private static final int FP_ONE = MathLibrary.FP_ONE;
	
	public static final byte VECTOR_X = 0;
	public static final byte VECTOR_Y = 1;
	public static final byte VECTOR_Z = 2;
	public static final byte VECTOR_W = 3;
	
	public static final int[] VECTOR_UP = new int[] {0, 0, FP_ONE, FP_ONE};
	public static final int[] VECTOR_DOWN = new int[] {0, 0, -FP_ONE, FP_ONE};
	public static final int[] VECTOR_RIGHT = new int[] {-FP_ONE, 0, 0, FP_ONE};
	public static final int[] VECTOR_LEFT = new int[] {FP_ONE, 0, 0, FP_ONE};
	public static final int[] VECTOR_FORWARD = new int[] {0, FP_ONE, 0, FP_ONE};
	public static final int[] VECTOR_BACK = new int[] {0, -FP_ONE, 0, FP_ONE};
	public static final int[] VECTOR_ONE = new int[] {FP_ONE, FP_ONE, FP_ONE, FP_ONE};
	public static final int[] VECTOR_ZERO = new int[] {0, 0, 0, FP_ONE};
	
	
	private final int[] vectorCache1 = generate();
	private final int[] vectorCache2 = generate();
	
	private final MathLibrary mathLibrary;
	
	public VectorLibrary() {
		this.mathLibrary = new MathLibrary();
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
	public int[] generate(int x, int y, int z, int w) {
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
	public int[] generate(int x, int y, int z) {
		return new int[] {x, y, z, FP_ONE};
	}

	/**
	 * Generates a vector using the given values and returns it.
	 * This vector can be used as location, rotation or scale vector.
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public int[] generate(int x, int y) {
		return new int[] {x, y, 0, FP_ONE};
	}
	
	/**
	 * Generates a vector at (0,0,0) and returns it.
	 * This vector can be used as location, rotation or scale vector.
	 * 
	 * @return
	 */
	public int[] generate() {
		return new int[] {0, 0, 0, FP_ONE};
	}

	/**
	 * Sets result equals the result of the scalar addition of vector1 and value.
	 * 
	 * @param vector1
	 * @param value
	 * @param result
	 */
	public int[] add(int[] vector1, int value, int[] result) {
		result[VECTOR_X] = vector1[VECTOR_X] + value;
		result[VECTOR_Y] = vector1[VECTOR_Y] + value;
		result[VECTOR_Z] = vector1[VECTOR_Z] + value;
		return result;
	}

	/**
	 * Sets result equals the result of the scalar subtraction of vector1 and value.
	 * 
	 * @param vector1
	 * @param value
	 * @param result
	 */
	public int[] subtract(int[] vector1, int value, int[] result) {
		result[VECTOR_X] = vector1[VECTOR_X] - value;
		result[VECTOR_Y] = vector1[VECTOR_Y] - value;
		result[VECTOR_Z] = vector1[VECTOR_Z] - value;
		return result;
	}

	/**
	 * Sets result equals the result of the scalar multiplication of vector1 and value.
	 * 
	 * @param vector1
	 * @param value
	 * @param result
	 */
	public int[] multiply(int[] vector1, int value, int[] result) {
		result[VECTOR_X] = mathLibrary.multiply(vector1[VECTOR_X], value);
		result[VECTOR_Y] = mathLibrary.multiply(vector1[VECTOR_Y], value);
		result[VECTOR_Z] = mathLibrary.multiply(vector1[VECTOR_Z], value);
		return result;
	}

	/**
	 * Sets result equals the result of the scalar division of vector1 and value.
	 * 
	 * @param vector1
	 * @param value
	 * @param result
	 */
	public int[] divide(int[] vector1, int value, int[] result) {
		result[VECTOR_X] = mathLibrary.divide(vector1[VECTOR_X], value);
		result[VECTOR_Y] = mathLibrary.divide(vector1[VECTOR_Y], value);
		result[VECTOR_Z] = mathLibrary.divide(vector1[VECTOR_Z], value);
		return result;
	}

	/**
	 * Sets result equals the result of the addition of vector1 and vector2.
	 * 
	 * @param vector1
	 * @param vector2
	 * @param result
	 */
	public int[] add(int[] vector1, int[] vector2, int[] result) {
		result[VECTOR_X] = vector1[VECTOR_X] + vector2[VECTOR_X];
		result[VECTOR_Y] = vector1[VECTOR_Y] + vector2[VECTOR_Y];
		result[VECTOR_Z] = vector1[VECTOR_Z] + vector2[VECTOR_Z];
		return result;
	}

	/**
	 * Sets result equals the result of the subtraction of vector1 and vector2.
	 * 
	 * @param vector1
	 * @param vector2
	 * @param result
	 */
	public int[] subtract(int[] vector1, int[] vector2, int[] result) {
		result[VECTOR_X] = vector1[VECTOR_X] - vector2[VECTOR_X];
		result[VECTOR_Y] = vector1[VECTOR_Y] - vector2[VECTOR_Y];
		result[VECTOR_Z] = vector1[VECTOR_Z] - vector2[VECTOR_Z];
		return result;
	}
	
	/**
	 * Sets result equals the result of the multiplication of vector1 and vector2.
	 * 
	 * @param vector1
	 * @param vector2
	 * @param result
	 */
	public int[] multiply(int[] vector1, int[] vector2, int[] result) {
		result[VECTOR_X] = mathLibrary.multiply(vector1[VECTOR_X], vector2[VECTOR_X]);
		result[VECTOR_Y] = mathLibrary.multiply(vector1[VECTOR_Y], vector2[VECTOR_Y]);
		result[VECTOR_Z] = mathLibrary.multiply(vector1[VECTOR_Z], vector2[VECTOR_Z]);
		return result;
	}

	/**
	 * Sets result equals the result of the division of vector1 and vector2.
	 * 
	 * @param vector1
	 * @param vector2
	 * @param result
	 */
	public int[] divide(int[] vector1, int[] vector2, int[] result) {
		result[VECTOR_X] = mathLibrary.divide(vector1[VECTOR_X], vector2[VECTOR_X]);
		result[VECTOR_Y] = mathLibrary.divide(vector1[VECTOR_Y], vector2[VECTOR_Y]);
		result[VECTOR_Z] = mathLibrary.divide(vector1[VECTOR_Z], vector2[VECTOR_Z]);
		return result;
	}

	/**
	 * Sets result equals the result of the multiplication of vector and matrix.
	 * 
	 * @param vector
	 * @param matrix
	 * @param result
	 */
	public int[] multiply(int[] vector, int[][] matrix, int[] result) {
		// ensures that will return right values if vector is the same as result
		copy(vectorCache1, vector);
		for (int i = 0; i < 4; i++) {
			int res = mathLibrary.multiply(matrix[0][i], vectorCache1[VECTOR_X]);
			res += mathLibrary.multiply(matrix[1][i], vectorCache1[VECTOR_Y]);
			res += mathLibrary.multiply(matrix[2][i], vectorCache1[VECTOR_Z]);
			result[i] = res + matrix[3][i];
		}
		return result;
	}

	/**
	 * Returns the magnitude of the given vector.
	 * 
	 * @param vector
	 * @return
	 */
	public int magnitude(int[] vector) {
		return mathLibrary.sqrt(dotProduct(vector, vector));
	}

	/**
	 * Returns the dot product of vector1 and vector2.
	 * 
	 * @param vector1
	 * @param vector2
	 * @return
	 */
	public int dotProduct(int[] vector1, int[] vector2) {
		int x = mathLibrary.multiply(vector1[VECTOR_X], vector2[VECTOR_X]);
		int y = mathLibrary.multiply(vector1[VECTOR_Y], vector2[VECTOR_Y]);
		int z = mathLibrary.multiply(vector1[VECTOR_Z], vector2[VECTOR_Z]);
		return (int)((x + y + z));
	}
	
	/**
	 * Returns the dot product of vector1 and vector2.
	 * 
	 * @param vector1
	 * @param vector2
	 * @return
	 */
	public int distance(int[] vector1, int[] vector2) {
		subtract(vector2, vector1, vectorCache1);
		return magnitude(vectorCache1);
	}

	/**
	 * Sets result equals the result of the cross product of vector1 and vector2.
	 * 
	 * @param vector1
	 * @param vector2
	 * @param result
	 */
	public int[] crossProduct(int[] vector1, int[] vector2, int[] result) {
		// ensures that will return right values if vector is the same as result
		copy(vectorCache1, vector1);
		copy(vectorCache2, vector2);
		result[VECTOR_X] = mathLibrary.multiply(vectorCache1[VECTOR_Y], vectorCache2[VECTOR_Z]);
		result[VECTOR_Y] = mathLibrary.multiply(vectorCache1[VECTOR_Z], vectorCache2[VECTOR_X]);
		result[VECTOR_Z] = mathLibrary.multiply(vectorCache1[VECTOR_X], vectorCache2[VECTOR_Y]);
		result[VECTOR_X] -= mathLibrary.multiply(vectorCache1[VECTOR_Z], vectorCache2[VECTOR_Y]);
		result[VECTOR_Y] -= mathLibrary.multiply(vectorCache1[VECTOR_X], vectorCache2[VECTOR_Z]);
		result[VECTOR_Z] -= mathLibrary.multiply(vectorCache1[VECTOR_Y], vectorCache2[VECTOR_X]);
		return result;
	}
	
	/**
	 * Sets result equals the normalized vector.
	 * 
	 * @param vector
	 * @param result
	 */
	public int[] normalize(int[] vector, int[] result) {
		int magnitude = magnitude(vector) + 1;
		result[VECTOR_X] = mathLibrary.divide(vector[VECTOR_X], magnitude);
		result[VECTOR_Y] = mathLibrary.divide(vector[VECTOR_Y], magnitude);
		result[VECTOR_Z] = mathLibrary.divide(vector[VECTOR_Z], magnitude);
		return result;
	}
	
	/**
	 * Sets result equals the vector reflected across reflectionVector.
	 * 
	 * @param vector
	 * @param reflectionVector
	 * @param result
	 */
	public int[] reflect(int[] vector, int[] reflectionVector, int[] result) {
		copy(vectorCache1, reflectionVector);
		int dot = 2 * dotProduct(vector, vectorCache1);
		multiply(vectorCache1, dot, vectorCache1);
		subtract(vector, vectorCache1, result);
		return result;
	}

	/**
	 * Sets result equals the vector rotated around (0, 0, 0) at x axis by the given angle.
	 * 
	 * @param vector
	 * @param angle
	 * @param result
	 * @return
	 */
	public int[] rotateX(int[] vector, int angle, int[] result) {
		// ensures that will return right values if vector is the same as result
		copy(vectorCache1, vector);
		int sin = mathLibrary.sin(angle);
		int cos = mathLibrary.cos(angle);
		result[VECTOR_X] = vectorCache1[VECTOR_X];
		result[VECTOR_Y] = mathLibrary.multiply(vectorCache1[VECTOR_Y], cos);
		result[VECTOR_Y] -= mathLibrary.multiply(vectorCache1[VECTOR_Z], sin);
		result[VECTOR_Z] = mathLibrary.multiply(vectorCache1[VECTOR_Z], cos);
		result[VECTOR_Z] += mathLibrary.multiply(vectorCache1[VECTOR_Y], sin);
		return result;
	}
	
	/**
	 * Sets result equals the vector rotated around (0, 0, 0) at y axis by the given angle.
	 * 
	 * @param vector
	 * @param angle
	 * @param result
	 * @return
	 */
	public int[] rotateY(int[] vector, int angle, int[] result) {
		// ensures that will return right values if vector is the same as result
		copy(vectorCache1, vector);
		int sin = mathLibrary.sin(-angle);
		int cos = mathLibrary.cos(-angle);
		result[VECTOR_X] = mathLibrary.multiply(vectorCache1[VECTOR_X], cos);
		result[VECTOR_X] -= mathLibrary.multiply(vectorCache1[VECTOR_Z], sin);
		result[VECTOR_Y] = vectorCache1[VECTOR_Y];
		result[VECTOR_Z] = mathLibrary.multiply(vectorCache1[VECTOR_Z], cos);
		result[VECTOR_Z] += mathLibrary.multiply(vectorCache1[VECTOR_X], sin);
		return result;
	}
	
	/**
	 * Sets result equals the vector rotated around (0, 0, 0) at z axis by the given angle.
	 * 
	 * @param vector
	 * @param angle
	 * @param result
	 * @return
	 */
	public int[] rotateZ(int[] vector, int angle, int[] result) {
		// ensures that will return right values if vector is the same as result
		copy(vectorCache1, vector);
		int sin = mathLibrary.sin(-angle);
		int cos = mathLibrary.cos(-angle);
		result[VECTOR_X] = mathLibrary.multiply(vectorCache1[VECTOR_X], cos);
		result[VECTOR_X] -= mathLibrary.multiply(vectorCache1[VECTOR_Y], sin);
		result[VECTOR_Y] = mathLibrary.multiply(vectorCache1[VECTOR_Y], cos);
		result[VECTOR_Y] += mathLibrary.multiply(vectorCache1[VECTOR_X], sin);
		result[VECTOR_Z] = vectorCache1[VECTOR_Z];
		return result;
	}
	
	/**
	 * Sets result equals the vector rotated around (0, 0, 0) at x, y and z axis by the given angles.
	 * 
	 * @param vector
	 * @param angles
	 * @param result
	 * @return
	 */
	public int[] rotateXYZ(int[] vector, int[] angles, int[] result) {
		rotateX(vector, angles[VECTOR_X], result);
		rotateY(result, angles[VECTOR_Y], result);
		rotateZ(result, angles[VECTOR_Z], result);
		return result;
	}
	
	/**
	 * Sets result equals the vector rotated around (0, 0, 0) at z, y and x axis by the given angles.
	 * 
	 * @param vector
	 * @param angles
	 * @param result
	 * @return
	 */
	public int[] rotateZYX(int[] vector, int[] angles, int[] result) {
		rotateZ(vector, angles[VECTOR_Z], result);
		rotateY(result, angles[VECTOR_Y], result);
		rotateX(result, angles[VECTOR_X], result);
		return result;
	}
	
	/**
	 * Checks if vector1 is equal to vector2.
	 * 
	 * @param vector1
	 * @param vector2
	 * @return true if vector1 is equal to vector2 if not false.
	 */
	public boolean equals(int[] vector1, int[] vector2) {
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
	public void swap(int[] vector1, int[] vector2) {
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
	public int[] invert(int[] vector, int[] result) {
		result[VECTOR_X] = -vector[VECTOR_X];
		result[VECTOR_Y] = -vector[VECTOR_Y];
		result[VECTOR_Z] = -vector[VECTOR_Z];
		result[VECTOR_W] = -vector[VECTOR_W];
		return result;
	}

	/**
	 * Copies the value of vector to the target.
	 * 
	 * @param vector vector with values.
	 * @param target target vector.
	 */
	public int[] copy(int[] target, int[] vector) {
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
	public String toString(int[] vector) {
		String result = "(";
		for (int i = 0; i < 4; i++) {
			result += vector[i] + ", ";
		}
		result += ")";
		return result;
	}

}
