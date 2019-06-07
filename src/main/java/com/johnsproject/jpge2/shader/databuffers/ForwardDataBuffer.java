package com.johnsproject.jpge2.shader.databuffers;

import com.johnsproject.jpge2.dto.ShaderDataBuffer;
import com.johnsproject.jpge2.dto.Texture;

public class ForwardDataBuffer extends ShaderDataBuffer {
	
	private int directionalLightIndex = -1;
	private int[] directionalLightFrustum;
	private int[][] directionalLightMatrix;
	private Texture directionalShadowMap;
	
	private int spotLightIndex = -1;
	private int[] spotLightFrustum;
	private int[][] spotLightMatrix;
	private Texture spotShadowMap;
	
	private int pointLightIndex = -1;
	private int[] pointLightFrustum;
	private int[][][] pointLightMatrices;
	private Texture[] pointShadowMaps;

	public int getDirectionalLightIndex() {
		return directionalLightIndex;
	}

	public void setDirectionalLightIndex(int directionalLightIndex) {
		this.directionalLightIndex = directionalLightIndex;
	}

	public int[][] getDirectionalLightMatrix() {
		return directionalLightMatrix;
	}

	public void setDirectionalLightMatrix(int[][] directionalLightMatrix) {
		this.directionalLightMatrix = directionalLightMatrix;
	}

	public Texture getDirectionalShadowMap() {
		return directionalShadowMap;
	}

	public void setDirectionalShadowMap(Texture directionalShadowMap) {
		this.directionalShadowMap = directionalShadowMap;
	}

	public int getSpotLightIndex() {
		return spotLightIndex;
	}

	public void setSpotLightIndex(int spotLightIndex) {
		this.spotLightIndex = spotLightIndex;
	}

	public int[][] getSpotLightMatrix() {
		return spotLightMatrix;
	}

	public void setSpotLightMatrix(int[][] spotLightMatrix) {
		this.spotLightMatrix = spotLightMatrix;
	}

	public Texture getSpotShadowMap() {
		return spotShadowMap;
	}

	public void setSpotShadowMap(Texture spotShadowMap) {
		this.spotShadowMap = spotShadowMap;
	}
	
	public int[] getDirectionalLightFrustum() {
		return directionalLightFrustum;
	}

	public void setDirectionalLightFrustum(int[] directionalLightFrusum) {
		this.directionalLightFrustum = directionalLightFrusum;
	}

	public int[] getSpotLightFrustum() {
		return spotLightFrustum;
	}

	public void setSpotLightFrustum(int[] spotLightFrustum) {
		this.spotLightFrustum = spotLightFrustum;
	}

	public int getPointLightIndex() {
		return pointLightIndex;
	}

	public void setPointLightIndex(int pointLightIndex) {
		this.pointLightIndex = pointLightIndex;
	}

	public int[] getPointLightFrustum() {
		return pointLightFrustum;
	}

	public void setPointLightFrustum(int[] pointLightFrustum) {
		this.pointLightFrustum = pointLightFrustum;
	}

	public int[][][] getPointLightMatrices() {
		return pointLightMatrices;
	}

	public void setPointLightMatrices(int[][][] pointLightMatrices) {
		this.pointLightMatrices = pointLightMatrices;
	}

	public Texture[] getPointShadowMaps() {
		return pointShadowMaps;
	}

	public void setPointShadowMaps(Texture[] pointShadowMaps) {
		this.pointShadowMaps = pointShadowMaps;
	}
}
