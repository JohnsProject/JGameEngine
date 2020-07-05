package com.johnsproject.jgameengine.model;

public class AnimationFrame {

	private final int[][][] boneMatrices;
	
	public AnimationFrame(int[][][] boneMatrices) {
		this.boneMatrices = boneMatrices;
	}

	public int[][] getBoneMatrix(int index) {
		return boneMatrices[index];
	}
	
	public int[][][] getBoneMatrices() {
		return boneMatrices;
	}
}
