package com.johnsproject.jgameengine.model;

public class AnimationFrame {

	private final int[][] boneWorldMatrices;
	private final int[][] boneNormalMatrices;
	
	public AnimationFrame(int[][] boneWorldMatrices, int[][] boneNormalMatrices) {
		this.boneWorldMatrices = boneWorldMatrices;
		this.boneNormalMatrices = boneNormalMatrices;
	}

	public int[] getBoneWorldMatrix(int index) {
		return boneWorldMatrices[index];
	}
	
	public int[][] getBoneWorldMatrices() {
		return boneWorldMatrices;
	}
	
	public int[] getBoneNormalMatrix(int index) {
		return boneNormalMatrices[index];
	}
	
	public int[][] getBoneNormalMatrices() {
		return boneNormalMatrices;
	}
}
