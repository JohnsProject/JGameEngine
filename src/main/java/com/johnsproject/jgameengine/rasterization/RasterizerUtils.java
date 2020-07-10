package com.johnsproject.jgameengine.rasterization;

import static com.johnsproject.jgameengine.util.FixedPointUtils.FP_BIT;
import static com.johnsproject.jgameengine.util.VectorUtils.VECTOR_Z;

import com.johnsproject.jgameengine.util.FixedPointUtils;

final class RasterizerUtils {

	public static final byte INTERPOLATE_BIT = 5;
	public static final byte INTERPOLATE_ONE = 1 << INTERPOLATE_BIT;
	public static final byte FP_PLUS_INTERPOLATE_BIT = FP_BIT + INTERPOLATE_BIT;
	
	private RasterizerUtils() {}
	
	public static void divideOneByZ(int[] location0, int[] location1, int[] location2) {
		location0[VECTOR_Z] = FixedPointUtils.divide(INTERPOLATE_ONE, location0[VECTOR_Z]);
		location1[VECTOR_Z] = FixedPointUtils.divide(INTERPOLATE_ONE, location1[VECTOR_Z]);
		location2[VECTOR_Z] = FixedPointUtils.divide(INTERPOLATE_ONE, location2[VECTOR_Z]);
	}
	
	public static void zMultiply(int[] vector, int[] location0, int[] location1, int[] location2) {
		vector[0] = FixedPointUtils.multiply(vector[0], location0[VECTOR_Z]);
		vector[1] = FixedPointUtils.multiply(vector[1], location1[VECTOR_Z]);
		vector[2] = FixedPointUtils.multiply(vector[2], location2[VECTOR_Z]);
	}
	
	public static void swapVector(int[] vector, int currentIndex, int indexToSet) {
		int tmp = vector[currentIndex];
		vector[currentIndex] = vector[indexToSet];
		vector[indexToSet] = tmp;
	}
	
	public static void swapCache(int[] vector, int[] cache, int vectorIndex, int indexToSet) {
		int tmp = cache[vectorIndex];
		cache[vectorIndex] = vector[indexToSet];
		vector[indexToSet] = tmp;
	}
}
