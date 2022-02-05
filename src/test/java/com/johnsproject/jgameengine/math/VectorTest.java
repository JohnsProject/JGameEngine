package com.johnsproject.jgameengine.math;

import org.junit.Test;

public class VectorTest {

	@Test
	public void vectorValueOperationsTest() throws Exception {
		// 255 because 256 * 256 = 65536 and will cause overflow of integer part of fixed point
		for (int i = 1; i < 256; i++) {
			double precision = 0.000000000000000000000000000000000001;
			double value1 = i;
			int fpValue1 = Fixed.toFixed(i);
			int[] fpVector1 = Vector.toVector((float)i, (float)i, (float)i);
			double[] vector1 = toVector(fpVector1);
			// vector value operations
			// vector add
			Vector.add(fpVector1, fpValue1);
			add(vector1, value1);
			assertVector(vector1, toVector(fpVector1), precision);
			// vector subtract
			Vector.subtract(fpVector1, fpValue1);
			subtract(vector1, value1);
			assertVector(vector1, toVector(fpVector1), precision);
			// vector multiply
			Vector.multiply(fpVector1, fpValue1);
			multiply(vector1, value1);
			assertVector(vector1, toVector(fpVector1), precision);
			// vector divide
			Vector.divide(fpVector1, fpValue1);
			divide(vector1, value1);
			assertVector(vector1, toVector(fpVector1), precision);
		}
	}
	
	@Test
	public void vectorVectorOperationsTest() throws Exception {
		// 255 because 256 * 256 = 65536 and will cause overflow of integer part of fixed point
		for (int i = 1; i < 256; i++) {
			double precision = 0.000000000000000000000000000000000001;
			int[] fpVector1 = Vector.toVector((float)i, (float)i, (float)i);
			int[] fpVector2 = Vector.toVector((float)i, (float)i, (float)i);
			double[] vector1 = toVector(fpVector1);
			double[] vector2 = toVector(fpVector2);
			// vector value operations
			// vector add
			Vector.add(fpVector1, fpVector2);
			add(vector1, vector2);
			assertVector(vector1, toVector(fpVector1), precision);
			// vector subtract
			Vector.subtract(fpVector1, fpVector2);
			subtract(vector1, vector2);
			assertVector(vector1, toVector(fpVector1), precision);
			// vector multiply
			Vector.multiply(fpVector1, fpVector2);
			multiply(vector1, vector2);
			assertVector(vector1, toVector(fpVector1), precision);
			// vector divide
			Vector.divide(fpVector1, fpVector2);
			divide(vector1, vector2);
			assertVector(vector1, toVector(fpVector1), precision);
		}
	}
	
	@Test
	public void matrixMultiplyTest() throws Exception {
		int[] vector1 = Vector.toVector(3f, 6f, 9f);
		int[] resultVector = Vector.toVector(16f, 35f, 84f);
		int[][] matrix1 = Matrix.indentityMatrix();
		matrix1[0][0] = Fixed.toFixed(2);
		matrix1[1][1] = Fixed.toFixed(4);
		matrix1[2][2] = Fixed.toFixed(8);
		matrix1[3][0] = Fixed.toFixed(10);
		matrix1[3][1] = Fixed.toFixed(11);
		matrix1[3][2] = Fixed.toFixed(12);
		Vector.multiply(vector1, matrix1);
		assert(Vector.equals(vector1, resultVector));		
	}
	
	@Test
	public void dotProductTest() throws Exception {
		for (int i = 0; i < 256; i++) {
			double precision = 0.00000000000000001;
			int[] fpVector1 = Vector.toVector((float)i, (float)i, (float)i);
			int[] fpVector2 = Vector.toVector((float)i, (float)i, (float)i);
			double[] vector1 = toVector(fpVector1);
			double[] vector2 = toVector(fpVector2);
			double fpResult = Vector.dotProduct(fpVector1, fpVector2);
			double result = dotProduct(vector1, vector2);
			fpResult = Fixed.toDouble((long)fpResult);
			assert((fpResult >= result - precision) && (fpResult <= result + precision));	
		}
	}
	
	@Test
	public void crossProductTest() throws Exception {
		for (int i = 1; i < Fixed.FP_ONE; i++) {
			double precision = 0.0001;
			int[] fpVector1 = Vector.toVector((float)i, (float)i * 0.8f, (float)i * 0.65f);
			int[] fpVector2 = Vector.toVector(0.5f, 0.15f, 0.8f);
			double[] vector1 = toVector(fpVector1);
			double[] vector2 = toVector(fpVector2);
			int[] fpResult = Vector.emptyVector();
			Vector.crossProduct(fpVector1, fpVector2, fpResult);
			double[] result = crossProduct(vector1, vector2);
			assertVector(result, toVector(fpResult), precision);
		}
	}
	
	@Test
	public void lengthTest() throws Exception {
		for (int i = 0; i < 256; i++) {
			double precision = 0.01;
			int[] fpVector1 = Vector.toVector((float)i, (float)i, (float)i);
			double[] vector1 = toVector(fpVector1);
			double fpLenght = Vector.length(fpVector1);
			fpLenght = Fixed.toDouble((long)fpLenght);
			double length = length(vector1);
			assert((fpLenght >= length - precision) && (fpLenght <= length + precision));	
		}
	}
	
	@Test
	public void distanceTest() throws Exception {
		for (int i = 0; i < 256; i++) {
			double precision = 0.01;
			int[] fpVector1 = Vector.toVector((float)i, (float)i, (float)i);
			int[] fpVector2 = Vector.emptyVector();
			double[] vector1 = toVector(fpVector1);
			double[] vector2 = toVector(fpVector2);
			double fpDistance = Vector.distance(fpVector1, fpVector2);
			fpDistance = Fixed.toDouble((long)fpDistance);
			double distance = distance(vector1, vector2);
			assert((fpDistance >= distance - precision) && (fpDistance <= distance + precision));	
		}
	}
	
	@Test
	public void normalizeTest() throws Exception {
		for (int i = 1; i < 256; i++) {
			double precision = 0.01;
			int[] fpVector1 = Vector.toVector((float)i, (float)i, (float)i);
			double[] vector1 = toVector(fpVector1);
			Vector.normalize(fpVector1);
			normalize(vector1);
			assertVector(vector1, toVector(fpVector1), precision);
		}
	}	
	
	static double[] toVector(int[] fpVector) {
		double x = Fixed.toDouble(fpVector[Vector.VECTOR_X]);
		double y = Fixed.toDouble(fpVector[Vector.VECTOR_Y]);
		double z = Fixed.toDouble(fpVector[Vector.VECTOR_Z]);
		return toVector(x, y, z);
	}
	
	static double[] toVector(double x, double y, double z) {
		return new double[] {x, y, z, 1d};
	}
	
	static double[] emptyVector() {
		return toVector(0d, 0d, 0d);
	}
	
	static void add(double[] vector, double value) {
		vector[Vector.VECTOR_X] = vector[Vector.VECTOR_X] + value;
		vector[Vector.VECTOR_Y] = vector[Vector.VECTOR_Y] + value;
		vector[Vector.VECTOR_Z] = vector[Vector.VECTOR_Z] + value;
	}
	
	static void subtract(double[] vector, double value) {
		vector[Vector.VECTOR_X] = vector[Vector.VECTOR_X] - value;
		vector[Vector.VECTOR_Y] = vector[Vector.VECTOR_Y] - value;
		vector[Vector.VECTOR_Z] = vector[Vector.VECTOR_Z] - value;
	}
	
	static void multiply(double[] vector, double value) {
		vector[Vector.VECTOR_X] = vector[Vector.VECTOR_X] * value;
		vector[Vector.VECTOR_Y] = vector[Vector.VECTOR_Y] * value;
		vector[Vector.VECTOR_Z] = vector[Vector.VECTOR_Z] * value;
	}
	
	static void divide(double[] vector, double value) {
		vector[Vector.VECTOR_X] = vector[Vector.VECTOR_X] / value;
		vector[Vector.VECTOR_Y] = vector[Vector.VECTOR_Y] / value;
		vector[Vector.VECTOR_Z] = vector[Vector.VECTOR_Z] / value;
	}
	
	static void add(double[] vector1, double[] vector2) {
		vector1[Vector.VECTOR_X] = vector1[Vector.VECTOR_X] + vector2[Vector.VECTOR_X];
		vector1[Vector.VECTOR_Y] = vector1[Vector.VECTOR_Y] + vector2[Vector.VECTOR_Y];
		vector1[Vector.VECTOR_Z] = vector1[Vector.VECTOR_Z] + vector2[Vector.VECTOR_Z];
	}
	
	static void subtract(double[] vector1, double[] vector2) {
		vector1[Vector.VECTOR_X] = vector1[Vector.VECTOR_X] - vector2[Vector.VECTOR_X];
		vector1[Vector.VECTOR_Y] = vector1[Vector.VECTOR_Y] - vector2[Vector.VECTOR_Y];
		vector1[Vector.VECTOR_Z] = vector1[Vector.VECTOR_Z] - vector2[Vector.VECTOR_Z];
	}
	
	static void multiply(double[] vector1, double[] vector2) {
		vector1[Vector.VECTOR_X] = vector1[Vector.VECTOR_X] * vector2[Vector.VECTOR_X];
		vector1[Vector.VECTOR_Y] = vector1[Vector.VECTOR_Y] * vector2[Vector.VECTOR_Y];
		vector1[Vector.VECTOR_Z] = vector1[Vector.VECTOR_Z] * vector2[Vector.VECTOR_Z];
	}
	
	static void divide(double[] vector1, double[] vector2) {
		vector1[Vector.VECTOR_X] = vector1[Vector.VECTOR_X] / vector2[Vector.VECTOR_X];
		vector1[Vector.VECTOR_Y] = vector1[Vector.VECTOR_Y] / vector2[Vector.VECTOR_Y];
		vector1[Vector.VECTOR_Z] = vector1[Vector.VECTOR_Z] / vector2[Vector.VECTOR_Z];
	}

	static double dotProduct(double[] vector1, double[] vector2) {
		double x = vector1[Vector.VECTOR_X] * vector2[Vector.VECTOR_X];
		double y = vector1[Vector.VECTOR_Y] * vector2[Vector.VECTOR_Y];
		double z = vector1[Vector.VECTOR_Z] * vector2[Vector.VECTOR_Z];
		return x + y + z;
	}
	
	static double length(double[] vector) {
		return Math.sqrt(dotProduct(vector, vector));
	}
	
	static double distance(double[] vector1, double[] vector2) {
		double x = vector1[Vector.VECTOR_X] - vector2[Vector.VECTOR_X];
		double y = vector1[Vector.VECTOR_Y] - vector2[Vector.VECTOR_Y];
		double z = vector1[Vector.VECTOR_Z] - vector2[Vector.VECTOR_Z];
		return Math.sqrt(x * x + y * y + z * z);
	}
	
	static double[] crossProduct(double[] vector1, double[] vector2) {
		double[] result = emptyVector();
		result[Vector.VECTOR_X] = vector1[Vector.VECTOR_Y] * vector2[Vector.VECTOR_Z];
		result[Vector.VECTOR_Y] = vector1[Vector.VECTOR_Z] * vector2[Vector.VECTOR_X];
		result[Vector.VECTOR_Z] = vector1[Vector.VECTOR_X] * vector2[Vector.VECTOR_Y];
		result[Vector.VECTOR_X] -= vector1[Vector.VECTOR_Z] * vector2[Vector.VECTOR_Y];
		result[Vector.VECTOR_Y] -= vector1[Vector.VECTOR_X] * vector2[Vector.VECTOR_Z];
		result[Vector.VECTOR_Z] -= vector1[Vector.VECTOR_Y] * vector2[Vector.VECTOR_X];
		return result;
	}
	
	static void normalize(double[] vector) {
		double length = length(vector);
		divide(vector, length);
	}
	
	static void assertVector(double[] vector1, double[] vector2, double precision) {
		assert(vector1[Vector.VECTOR_X] >= vector2[Vector.VECTOR_X] - precision);
		assert(vector1[Vector.VECTOR_X] <= vector2[Vector.VECTOR_X] + precision);
		assert(vector1[Vector.VECTOR_Y] >= vector2[Vector.VECTOR_Y] - precision);
		assert(vector1[Vector.VECTOR_Y] <= vector2[Vector.VECTOR_Y] + precision);
		assert(vector1[Vector.VECTOR_Z] >= vector2[Vector.VECTOR_Z] - precision);
		assert(vector1[Vector.VECTOR_Z] <= vector2[Vector.VECTOR_Z] + precision);
	}
}
