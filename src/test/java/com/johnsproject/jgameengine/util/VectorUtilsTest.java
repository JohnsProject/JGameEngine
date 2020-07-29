package com.johnsproject.jgameengine.util;

import org.junit.Test;

import com.johnsproject.jgameengine.util.FixedPointUtils;
import com.johnsproject.jgameengine.util.MatrixUtils;
import com.johnsproject.jgameengine.util.VectorUtils;

public class VectorUtilsTest {

	@Test
	public void vectorValueOperationsTest() throws Exception {
		// 255 because 256 * 256 = 65536 and will cause overflow of integer part of fixed point
		for (int i = 1; i < 256; i++) {
			double precision = 0.000000000000000000000000000000000001;
			double value1 = i;
			int fpValue1 = FixedPointUtils.toFixedPoint(i);
			int[] fpVector1 = VectorUtils.toVector((float)i, (float)i, (float)i);
			double[] vector1 = toVector(fpVector1);
			// vector value operations
			// vector add
			VectorUtils.add(fpVector1, fpValue1);
			add(vector1, value1);
			assertVector(vector1, toVector(fpVector1), precision);
			// vector subtract
			VectorUtils.subtract(fpVector1, fpValue1);
			subtract(vector1, value1);
			assertVector(vector1, toVector(fpVector1), precision);
			// vector multiply
			VectorUtils.multiply(fpVector1, fpValue1);
			multiply(vector1, value1);
			assertVector(vector1, toVector(fpVector1), precision);
			// vector divide
			VectorUtils.divide(fpVector1, fpValue1);
			divide(vector1, value1);
			assertVector(vector1, toVector(fpVector1), precision);
		}
	}
	
	@Test
	public void vectorVectorOperationsTest() throws Exception {
		// 255 because 256 * 256 = 65536 and will cause overflow of integer part of fixed point
		for (int i = 1; i < 256; i++) {
			double precision = 0.000000000000000000000000000000000001;
			int[] fpVector1 = VectorUtils.toVector((float)i, (float)i, (float)i);
			int[] fpVector2 = VectorUtils.toVector((float)i, (float)i, (float)i);
			double[] vector1 = toVector(fpVector1);
			double[] vector2 = toVector(fpVector2);
			// vector value operations
			// vector add
			VectorUtils.add(fpVector1, fpVector2);
			add(vector1, vector2);
			assertVector(vector1, toVector(fpVector1), precision);
			// vector subtract
			VectorUtils.subtract(fpVector1, fpVector2);
			subtract(vector1, vector2);
			assertVector(vector1, toVector(fpVector1), precision);
			// vector multiply
			VectorUtils.multiply(fpVector1, fpVector2);
			multiply(vector1, vector2);
			assertVector(vector1, toVector(fpVector1), precision);
			// vector divide
			VectorUtils.divide(fpVector1, fpVector2);
			divide(vector1, vector2);
			assertVector(vector1, toVector(fpVector1), precision);
		}
	}
	
	@Test
	public void matrixMultiplyTest() throws Exception {
		int[] vector1 = VectorUtils.toVector(3f, 6f, 9f);
		int[] resultVector = VectorUtils.toVector(16f, 35f, 84f);
		int[][] matrix1 = MatrixUtils.indentityMatrix();
		matrix1[0][0] = FixedPointUtils.toFixedPoint(2);
		matrix1[1][1] = FixedPointUtils.toFixedPoint(4);
		matrix1[2][2] = FixedPointUtils.toFixedPoint(8);
		matrix1[3][0] = FixedPointUtils.toFixedPoint(10);
		matrix1[3][1] = FixedPointUtils.toFixedPoint(11);
		matrix1[3][2] = FixedPointUtils.toFixedPoint(12);
		VectorUtils.multiply(vector1, matrix1);
		assert(VectorUtils.equals(vector1, resultVector));		
	}
	
	@Test
	public void dotProductTest() throws Exception {
		for (int i = 0; i < 256; i++) {
			double precision = 0.00000000000000001;
			int[] fpVector1 = VectorUtils.toVector((float)i, (float)i, (float)i);
			int[] fpVector2 = VectorUtils.toVector((float)i, (float)i, (float)i);
			double[] vector1 = toVector(fpVector1);
			double[] vector2 = toVector(fpVector2);
			double fpResult = VectorUtils.dotProduct(fpVector1, fpVector2);
			double result = dotProduct(vector1, vector2);
			fpResult = FixedPointUtils.toDouble((long)fpResult);
			assert((fpResult >= result - precision) && (fpResult <= result + precision));	
		}
	}
	
	@Test
	public void crossProductTest() throws Exception {
		for (int i = 1; i < FixedPointUtils.FP_ONE; i++) {
			double precision = 0.0001;
			int[] fpVector1 = VectorUtils.toVector((float)i, (float)i * 0.8f, (float)i * 0.65f);
			int[] fpVector2 = VectorUtils.toVector(0.5f, 0.15f, 0.8f);
			double[] vector1 = toVector(fpVector1);
			double[] vector2 = toVector(fpVector2);
			int[] fpResult = VectorUtils.emptyVector();
			VectorUtils.crossProduct(fpVector1, fpVector2, fpResult);
			double[] result = crossProduct(vector1, vector2);
			assertVector(result, toVector(fpResult), precision);
		}
	}
	
	@Test
	public void lengthTest() throws Exception {
		for (int i = 0; i < 256; i++) {
			double precision = 0.01;
			int[] fpVector1 = VectorUtils.toVector((float)i, (float)i, (float)i);
			double[] vector1 = toVector(fpVector1);
			double fpLenght = VectorUtils.length(fpVector1);
			fpLenght = FixedPointUtils.toDouble((long)fpLenght);
			double length = length(vector1);
			assert((fpLenght >= length - precision) && (fpLenght <= length + precision));	
		}
	}
	
	@Test
	public void distanceTest() throws Exception {
		for (int i = 0; i < 256; i++) {
			double precision = 0.01;
			int[] fpVector1 = VectorUtils.toVector((float)i, (float)i, (float)i);
			int[] fpVector2 = VectorUtils.emptyVector();
			double[] vector1 = toVector(fpVector1);
			double[] vector2 = toVector(fpVector2);
			double fpDistance = VectorUtils.distance(fpVector1, fpVector2);
			fpDistance = FixedPointUtils.toDouble((long)fpDistance);
			double distance = distance(vector1, vector2);
			assert((fpDistance >= distance - precision) && (fpDistance <= distance + precision));	
		}
	}
	
	@Test
	public void normalizeTest() throws Exception {
		for (int i = 1; i < 256; i++) {
			double precision = 0.01;
			int[] fpVector1 = VectorUtils.toVector((float)i, (float)i, (float)i);
			double[] vector1 = toVector(fpVector1);
			VectorUtils.normalize(fpVector1);
			normalize(vector1);
			assertVector(vector1, toVector(fpVector1), precision);
		}
	}	
	
	static double[] toVector(int[] fpVector) {
		double x = FixedPointUtils.toDouble(fpVector[VectorUtils.VECTOR_X]);
		double y = FixedPointUtils.toDouble(fpVector[VectorUtils.VECTOR_Y]);
		double z = FixedPointUtils.toDouble(fpVector[VectorUtils.VECTOR_Z]);
		return toVector(x, y, z);
	}
	
	static double[] toVector(double x, double y, double z) {
		return new double[] {x, y, z, 1d};
	}
	
	static double[] emptyVector() {
		return toVector(0d, 0d, 0d);
	}
	
	static void add(double[] vector, double value) {
		vector[VectorUtils.VECTOR_X] = vector[VectorUtils.VECTOR_X] + value;
		vector[VectorUtils.VECTOR_Y] = vector[VectorUtils.VECTOR_Y] + value;
		vector[VectorUtils.VECTOR_Z] = vector[VectorUtils.VECTOR_Z] + value;
	}
	
	static void subtract(double[] vector, double value) {
		vector[VectorUtils.VECTOR_X] = vector[VectorUtils.VECTOR_X] - value;
		vector[VectorUtils.VECTOR_Y] = vector[VectorUtils.VECTOR_Y] - value;
		vector[VectorUtils.VECTOR_Z] = vector[VectorUtils.VECTOR_Z] - value;
	}
	
	static void multiply(double[] vector, double value) {
		vector[VectorUtils.VECTOR_X] = vector[VectorUtils.VECTOR_X] * value;
		vector[VectorUtils.VECTOR_Y] = vector[VectorUtils.VECTOR_Y] * value;
		vector[VectorUtils.VECTOR_Z] = vector[VectorUtils.VECTOR_Z] * value;
	}
	
	static void divide(double[] vector, double value) {
		vector[VectorUtils.VECTOR_X] = vector[VectorUtils.VECTOR_X] / value;
		vector[VectorUtils.VECTOR_Y] = vector[VectorUtils.VECTOR_Y] / value;
		vector[VectorUtils.VECTOR_Z] = vector[VectorUtils.VECTOR_Z] / value;
	}
	
	static void add(double[] vector1, double[] vector2) {
		vector1[VectorUtils.VECTOR_X] = vector1[VectorUtils.VECTOR_X] + vector2[VectorUtils.VECTOR_X];
		vector1[VectorUtils.VECTOR_Y] = vector1[VectorUtils.VECTOR_Y] + vector2[VectorUtils.VECTOR_Y];
		vector1[VectorUtils.VECTOR_Z] = vector1[VectorUtils.VECTOR_Z] + vector2[VectorUtils.VECTOR_Z];
	}
	
	static void subtract(double[] vector1, double[] vector2) {
		vector1[VectorUtils.VECTOR_X] = vector1[VectorUtils.VECTOR_X] - vector2[VectorUtils.VECTOR_X];
		vector1[VectorUtils.VECTOR_Y] = vector1[VectorUtils.VECTOR_Y] - vector2[VectorUtils.VECTOR_Y];
		vector1[VectorUtils.VECTOR_Z] = vector1[VectorUtils.VECTOR_Z] - vector2[VectorUtils.VECTOR_Z];
	}
	
	static void multiply(double[] vector1, double[] vector2) {
		vector1[VectorUtils.VECTOR_X] = vector1[VectorUtils.VECTOR_X] * vector2[VectorUtils.VECTOR_X];
		vector1[VectorUtils.VECTOR_Y] = vector1[VectorUtils.VECTOR_Y] * vector2[VectorUtils.VECTOR_Y];
		vector1[VectorUtils.VECTOR_Z] = vector1[VectorUtils.VECTOR_Z] * vector2[VectorUtils.VECTOR_Z];
	}
	
	static void divide(double[] vector1, double[] vector2) {
		vector1[VectorUtils.VECTOR_X] = vector1[VectorUtils.VECTOR_X] / vector2[VectorUtils.VECTOR_X];
		vector1[VectorUtils.VECTOR_Y] = vector1[VectorUtils.VECTOR_Y] / vector2[VectorUtils.VECTOR_Y];
		vector1[VectorUtils.VECTOR_Z] = vector1[VectorUtils.VECTOR_Z] / vector2[VectorUtils.VECTOR_Z];
	}

	static double dotProduct(double[] vector1, double[] vector2) {
		double x = vector1[VectorUtils.VECTOR_X] * vector2[VectorUtils.VECTOR_X];
		double y = vector1[VectorUtils.VECTOR_Y] * vector2[VectorUtils.VECTOR_Y];
		double z = vector1[VectorUtils.VECTOR_Z] * vector2[VectorUtils.VECTOR_Z];
		return x + y + z;
	}
	
	static double length(double[] vector) {
		return Math.sqrt(dotProduct(vector, vector));
	}
	
	static double distance(double[] vector1, double[] vector2) {
		double x = vector1[VectorUtils.VECTOR_X] - vector2[VectorUtils.VECTOR_X];
		double y = vector1[VectorUtils.VECTOR_Y] - vector2[VectorUtils.VECTOR_Y];
		double z = vector1[VectorUtils.VECTOR_Z] - vector2[VectorUtils.VECTOR_Z];
		return Math.sqrt(x * x + y * y + z * z);
	}
	
	static double[] crossProduct(double[] vector1, double[] vector2) {
		double[] result = emptyVector();
		result[VectorUtils.VECTOR_X] = vector1[VectorUtils.VECTOR_Y] * vector2[VectorUtils.VECTOR_Z];
		result[VectorUtils.VECTOR_Y] = vector1[VectorUtils.VECTOR_Z] * vector2[VectorUtils.VECTOR_X];
		result[VectorUtils.VECTOR_Z] = vector1[VectorUtils.VECTOR_X] * vector2[VectorUtils.VECTOR_Y];
		result[VectorUtils.VECTOR_X] -= vector1[VectorUtils.VECTOR_Z] * vector2[VectorUtils.VECTOR_Y];
		result[VectorUtils.VECTOR_Y] -= vector1[VectorUtils.VECTOR_X] * vector2[VectorUtils.VECTOR_Z];
		result[VectorUtils.VECTOR_Z] -= vector1[VectorUtils.VECTOR_Y] * vector2[VectorUtils.VECTOR_X];
		return result;
	}
	
	static void normalize(double[] vector) {
		double length = length(vector);
		divide(vector, length);
	}
	
	static void assertVector(double[] vector1, double[] vector2, double precision) {
		assert(vector1[VectorUtils.VECTOR_X] >= vector2[VectorUtils.VECTOR_X] - precision);
		assert(vector1[VectorUtils.VECTOR_X] <= vector2[VectorUtils.VECTOR_X] + precision);
		assert(vector1[VectorUtils.VECTOR_Y] >= vector2[VectorUtils.VECTOR_Y] - precision);
		assert(vector1[VectorUtils.VECTOR_Y] <= vector2[VectorUtils.VECTOR_Y] + precision);
		assert(vector1[VectorUtils.VECTOR_Z] >= vector2[VectorUtils.VECTOR_Z] - precision);
		assert(vector1[VectorUtils.VECTOR_Z] <= vector2[VectorUtils.VECTOR_Z] + precision);
	}
}
