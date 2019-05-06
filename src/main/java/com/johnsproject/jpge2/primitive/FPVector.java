package com.johnsproject.jpge2.primitive;

import java.util.Arrays;

import com.johnsproject.jpge2.processor.MathProcessor;

public class FPVector {
	private static final int FP_ONE = MathProcessor.FP_ONE;
	
	public static final byte VECTOR_SIZE = 4;
	
	public static final byte VECTOR_X = 0;
	public static final byte VECTOR_Y = 1;
	public static final byte VECTOR_Z = 2;
	public static final byte VECTOR_W = 3;
	
	public static final FPVector VECTOR_UP = new FPVector(0, 0, FP_ONE, FP_ONE);
	public static final FPVector VECTOR_DOWN = new FPVector(0, 0, -FP_ONE, FP_ONE);
	public static final FPVector VECTOR_RIGHT = new FPVector(-FP_ONE, 0, 0, FP_ONE);
	public static final FPVector VECTOR_LEFT = new FPVector(FP_ONE, 0, 0, FP_ONE);
	public static final FPVector VECTOR_FORWARD = new FPVector(0, FP_ONE, 0, FP_ONE);
	public static final FPVector VECTOR_BACK = new FPVector(0, -FP_ONE, 0, FP_ONE);
	public static final FPVector VECTOR_ONE = new FPVector(FP_ONE, FP_ONE, FP_ONE, FP_ONE);
	public static final FPVector VECTOR_ZERO = new FPVector(0, 0, 0, FP_ONE);
	
	private static final FPVector CACHE = new FPVector();
	
	private final int[] values;
	
	public FPVector() {
		this.values = new int[VECTOR_SIZE];
		initialize(0, 0, 0, 0);
	}
	
	public FPVector(int x, int y) {
		this.values = new int[VECTOR_SIZE];
		initialize(x, y, 0, 0);
	}
	
	public FPVector(int x, int y, int z) {
		this.values = new int[VECTOR_SIZE];
		initialize(x, y, z, 0);
	}
	
	public FPVector(int x, int y, int z, int w) {
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
	
	public void add(FPVector vector) {
		int[] vectorValues = vector.getValues();
		values[VECTOR_X] += vectorValues[VECTOR_X];
		values[VECTOR_Y] += vectorValues[VECTOR_Y];
		values[VECTOR_Z] += vectorValues[VECTOR_Z];
	}
	
	public void subtract(FPVector vector) {
		int[] vectorValues = vector.getValues();
		values[VECTOR_X] -= vectorValues[VECTOR_X];
		values[VECTOR_Y] -= vectorValues[VECTOR_Y];
		values[VECTOR_Z] -= vectorValues[VECTOR_Z];
	}
	
	public void multiply(FPVector vector) {
		int[] vectorValues = vector.getValues();
		values[VECTOR_X] = MathProcessor.multiply(values[VECTOR_X], vectorValues[VECTOR_X]);
		values[VECTOR_Y] = MathProcessor.multiply(values[VECTOR_Y], vectorValues[VECTOR_Y]);
		values[VECTOR_Z] = MathProcessor.multiply(values[VECTOR_Z], vectorValues[VECTOR_Z]);
	}
	
	public void divide(FPVector vector) {
		int[] vectorValues = vector.getValues();
		values[VECTOR_X] = MathProcessor.divide(values[VECTOR_X], vectorValues[VECTOR_X]);
		values[VECTOR_Y] = MathProcessor.divide(values[VECTOR_Y], vectorValues[VECTOR_Y]);
		values[VECTOR_Z] = MathProcessor.divide(values[VECTOR_Z], vectorValues[VECTOR_Z]);
	}
	
	public void multiply(FPMatrix matrix) {
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
	
	public int dotProduct(FPVector vector) {
		int[] vectorValues = vector.getValues();
		long x = (long)values[VECTOR_X] * vectorValues[VECTOR_X];
		long y = (long)values[VECTOR_Y] * vectorValues[VECTOR_Y];
		long z = (long)values[VECTOR_Z] * vectorValues[VECTOR_Z];
		return (int)MathProcessor.multiply(x + y + z, 1);
	}
	
	public int magnitude() {
		return MathProcessor.sqrt(dotProduct(this));
	}
	
	public int distance(FPVector vector) {
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
	
	public void reflect(FPVector reflectionVector) {
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
	
	public void rotateXYZ(FPVector angles) {
		rotateX(angles.getX());
		rotateY(angles.getY());
		rotateZ(angles.getZ());
	}
	
	public void rotateZYX(FPVector angles) {
		rotateZ(angles.getZ());
		rotateY(angles.getY());
		rotateX(angles.getX());
	}
	
	public void invert() {
		values[VECTOR_X] = -values[VECTOR_X];
		values[VECTOR_Y] = -values[VECTOR_Y];
		values[VECTOR_Z] = -values[VECTOR_Z];
	}
	
	public void copy(FPVector target) {
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
		FPVector other = (FPVector) obj;
		if (!Arrays.equals(values, other.values))
			return false;
		return true;
	}

	@Override
	public FPVector clone() {
		return new FPVector(values[VECTOR_X], values[VECTOR_Y], values[VECTOR_Z], values[VECTOR_W]);
	}
}
