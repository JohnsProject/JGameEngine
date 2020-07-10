package com.johnsproject.jgameengine.rasterization;

import static com.johnsproject.jgameengine.util.VectorUtils.VECTOR_Z;
import static com.johnsproject.jgameengine.util.FixedPointUtils.FP_ONE;

import com.johnsproject.jgameengine.util.FixedPointUtils;

final class RasterizerUtils {
	
	private RasterizerUtils() {}
	
	public static void divideOneByZ(int[] location0, int[] location1, int[] location2) {
		location0[VECTOR_Z] = FixedPointUtils.divide(FP_ONE, location0[VECTOR_Z]);
		location1[VECTOR_Z] = FixedPointUtils.divide(FP_ONE, location1[VECTOR_Z]);
		location2[VECTOR_Z] = FixedPointUtils.divide(FP_ONE, location2[VECTOR_Z]);
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
