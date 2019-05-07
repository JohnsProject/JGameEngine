package com.johnsproject.jpge2.primitive;

import java.util.Arrays;

import com.johnsproject.jpge2.processor.MathProcessor;

public class Vector {
	private static final int FP_ONE = MathProcessor.FP_ONE;
	
	public static final byte VECTOR_SIZE = 4;
	
	public static final byte VECTOR_X = 0;
	public static final byte VECTOR_Y = 1;
	public static final byte VECTOR_Z = 2;
	public static final byte VECTOR_W = 3;
	
	public static final Vector VECTOR_UP = new Vector(0, 0, FP_ONE, FP_ONE);
	public static final Vector VECTOR_DOWN = new Vector(0, 0, -FP_ONE, FP_ONE);
	public static final Vector VECTOR_RIGHT = new Vector(-FP_ONE, 0, 0, FP_ONE);
	public static final Vector VECTOR_LEFT = new Vector(FP_ONE, 0, 0, FP_ONE);
	public static final Vector VECTOR_FORWARD = new Vector(0, FP_ONE, 0, FP_ONE);
	public static final Vector VECTOR_BACK = new Vector(0, -FP_ONE, 0, FP_ONE);
	public static final Vector VECTOR_ONE = new Vector(FP_ONE, FP_ONE, FP_ONE, FP_ONE);
	public static final Vector VECTOR_ZERO = new Vector(0, 0, 0, FP_ONE);
	
	private static final Vector CACHE = new Vector();
	
	private final int[] values;
	
	public Vector() {
		this.values = new int[VECTOR_SIZE];
		initialize(0, 0, 0, 0);
	}
	
	public Vector(int x, int y) {
		this.values = new int[VECTOR_SIZE];
		initialize(x, y, 0, 0);
	}
	
	public Vector(int x, int y, int z) {
		this.values = new int[VECTOR_SIZE];
		initialize(x, y, z, 0);
	}
	
	public Vector(int x, int y, int z, int w) {
		this.values = new int[VECTOR_SIZE];
		initialize(x, y, z, w);
	}
	
	private void initialize(int x, int y, int z, int w) {
		values[VECTOR_X] = x;
		values[VECTOR_Y] = y;
		values[VECTOR_Z] = z;
		values[VECTOR_W] = w;
	}
	
	public int getX() {
		return values[VECTOR_X];
	}
	
	public int getY() {
		return values[VECTOR_Y];
	}
	
	public int getZ() {
		return values[VECTOR_Z];
	}
	
	public int getW() {
		return values[VECTOR_W];
	}
	
	public int[] getValues() {
		return values;
	}

	public void add(int value) {
		values[VECTOR_X] += value;
		values[VECTOR_Y] += value;
		values[VECTOR_Z] += value;
	}
	
	public void subtract(int value) {
		values[VECTOR_X] -= value;
		values[VECTOR_Y] -= value;
		values[VECTOR_Z] -= value;
	}
	
	public void multiply(int value) {
		values[VECTOR_X] = MathProcessor.multiply(values[VECTOR_X], value);
		values[VECTOR_Y] = MathProcessor.multiply(values[VECTOR_Y], value);
		values[VECTOR_Z] = MathProcessor.multiply(values[VECTOR_Z], value);
	}
	
	public void divide(int value) {
		values[VECTOR_X] = MathProcessor.divide(values[VECTOR_X], value);
		values[VECTOR_Y] = MathProcessor.divide(values[VECTOR_Y], value);
		values[VECTOR_Z] = MathProcessor.divide(values[VECTOR_Z], value);
	}
	
	public void add(Vector vector) {
		int[] vectorValues = vector.getValues();
		values[VECTOR_X] += vectorValues[VECTOR_X];
		values[VECTOR_Y] += vectorValues[VECTOR_Y];
		values[VECTOR_Z] += vectorValues[VECTOR_Z];
	}
	
	public void subtract(Vector vector) {
		int[] vectorValues = vector.getValues();
		values[VECTOR_X] -= vectorValues[VECTOR_X];
		values[VECTOR_Y] -= vectorValues[VECTOR_Y];
		values[VECTOR_Z] -= vectorValues[VECTOR_Z];
	}
	
	public void multiply(Vector vector) {
		int[] vectorValues = vector.getValues();
		values[VECTOR_X] = MathProcessor.multiply(values[VECTOR_X], vectorValues[VECTOR_X]);
		values[VECTOR_Y] = MathProcessor.multiply(values[VECTOR_Y], vectorValues[VECTOR_Y]);
		values[VECTOR_Z] = MathProcessor.multiply(values[VECTOR_Z], vectorValues[VECTOR_Z]);
	}
	
	public void divide(Vector vector) {
		int[] vectorValues = vector.getValues();
		values[VECTOR_X] = MathProcessor.divide(values[VECTOR_X], vectorValues[VECTOR_X]);
		values[VECTOR_Y] = MathProcessor.divide(values[VECTOR_Y], vectorValues[VECTOR_Y]);
		values[VECTOR_Z] = MathProcessor.divide(values[VECTOR_Z], vectorValues[VECTOR_Z]);
	}
	
	public void multiply(Matrix matrix) {
		copy(CACHE);
		int[][] matrixValues = matrix.getValues();
		int[] cacheValues = CACHE.getValues();
		for (int i = 0; i < 4; i++) {
			long result = (long)matrixValues[0][i] * cacheValues[VECTOR_X];
			result += (long)matrixValues[1][i] * cacheValues[VECTOR_Y];
			result += (long)matrixValues[2][i] * cacheValues[VECTOR_Z];
			values[i] = (int)(MathProcessor.multiply(result, 1) + matrixValues[3][i]);
		}
	}
	
	public int dotProduct(Vector vector) {
		int[] vectorValues = vector.getValues();
		long x = (long)values[VECTOR_X] * vectorValues[VECTOR_X];
		long y = (long)values[VECTOR_Y] * vectorValues[VECTOR_Y];
		long z = (long)values[VECTOR_Z] * vectorValues[VECTOR_Z];
		return (int)MathProcessor.multiply(x + y + z, 1);
	}
	
	public int magnitude() {
		return MathProcessor.sqrt(dotProduct(this));
	}
	
	public int distance(Vector vector) {
		vector.copy(CACHE);
		vector.subtract(this);
		int result = vector.magnitude();
		CACHE.copy(vector);
		return result;
	}
	
	public void normalize() {
		int magnitude = magnitude();
		if (magnitude != 0) {
			values[VECTOR_X] = values[VECTOR_X] / magnitude;
			values[VECTOR_Y] = values[VECTOR_Y] / magnitude;
			values[VECTOR_Z] = values[VECTOR_Z] / magnitude;
		}
	}
	
	public void reflect(Vector reflectionVector) {
		reflectionVector.copy(CACHE);
		int dot = 2 * dotProduct(CACHE);
		CACHE.multiply(dot);
		subtract(CACHE);
	}
	
	public void rotateX(int angle) {
		copy(CACHE);
		int[] cacheValues = CACHE.getValues();
		int sin = MathProcessor.sin(angle);
		int cos = MathProcessor.cos(angle);
		values[VECTOR_Y] = MathProcessor.multiply(cacheValues[VECTOR_Y], cos);
		values[VECTOR_Y] -= MathProcessor.multiply(cacheValues[VECTOR_Z], sin);
		values[VECTOR_Z] = MathProcessor.multiply(cacheValues[VECTOR_Z], cos);
		values[VECTOR_Z] += MathProcessor.multiply(cacheValues[VECTOR_Y], sin);
	}
	
	public void rotateY(int angle) {
		copy(CACHE);
		int[] cacheValues = CACHE.getValues();
		int sin = MathProcessor.sin(angle);
		int cos = MathProcessor.cos(angle);
		values[VECTOR_X] = MathProcessor.multiply(cacheValues[VECTOR_X], cos);
		values[VECTOR_X] -= MathProcessor.multiply(cacheValues[VECTOR_Z], sin);
		values[VECTOR_Z] = MathProcessor.multiply(cacheValues[VECTOR_Z], cos);
		values[VECTOR_Z] += MathProcessor.multiply(cacheValues[VECTOR_X], sin);
	}
	
	public void rotateZ(int angle) {
		copy(CACHE);
		int[] cacheValues = CACHE.getValues();
		int sin = MathProcessor.sin(angle);
		int cos = MathProcessor.cos(angle);
		values[VECTOR_X] = MathProcessor.multiply(cacheValues[VECTOR_X], cos);
		values[VECTOR_X] -= MathProcessor.multiply(cacheValues[VECTOR_Y], sin);
		values[VECTOR_Y] = MathProcessor.multiply(cacheValues[VECTOR_Y], cos);
		values[VECTOR_Y] += MathProcessor.multiply(cacheValues[VECTOR_X], sin);
	}
	
	public void rotateXYZ(Vector angles) {
		rotateX(angles.getValues()[VECTOR_X]);
		rotateY(angles.getValues()[VECTOR_Y]);
		rotateZ(angles.getValues()[VECTOR_Z]);
	}
	
	public void rotateZYX(Vector angles) {
		rotateZ(angles.getValues()[VECTOR_Z]);
		rotateY(angles.getValues()[VECTOR_Y]);
		rotateX(angles.getValues()[VECTOR_X]);
	}
	
	public void invert() {
		values[VECTOR_X] = -values[VECTOR_X];
		values[VECTOR_Y] = -values[VECTOR_Y];
		values[VECTOR_Z] = -values[VECTOR_Z];
	}
	
	public void copy(Vector target) {
		int[] targetValues = target.getValues();
		for (int i = 0; i < targetValues.length; i++) {
			targetValues[i] = values[i];
		}
	}

	@Override
	public String toString() {
		return "FPVector (" + values[VECTOR_X] + ", " + values[VECTOR_Y] + ", " + values[VECTOR_Z] + ", " + values[VECTOR_W] + ")";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(values);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Vector other = (Vector) obj;
		if (!Arrays.equals(values, other.values))
			return false;
		return true;
	}

	@Override
	public Vector clone() {
		return new Vector(values[VECTOR_X], values[VECTOR_Y], values[VECTOR_Z], values[VECTOR_W]);
	}
}
