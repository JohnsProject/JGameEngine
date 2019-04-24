package com.johnsproject.jpge2.dto;

import java.util.List;

import com.johnsproject.jpge2.shader.ShaderDataBuffer;

public class ShaderData implements ShaderDataBuffer {
	
	private FrameBuffer frameBuffer;
	private List<Light> lights;
	
	private int directionalLightIndex = -1;
	private int[][] directionalLightMatrix;
	private FrameBuffer directionalShadowMap;
	
	private int spotLightIndex = -1;
	private int[][] spotLightMatrix;
	private FrameBuffer spotShadowMap;

	public FrameBuffer getFrameBuffer() {
		return frameBuffer;
	}

	public void setFrameBuffer(FrameBuffer frameBuffer) {
		this.frameBuffer = frameBuffer;
	}

	public List<Light> getLights() {
		return lights;
	}

	public void setLights(List<Light> lights) {
		this.lights = lights;
	}

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

	public FrameBuffer getDirectionalShadowMap() {
		return directionalShadowMap;
	}

	public void setDirectionalShadowMap(FrameBuffer directionalShadowMap) {
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

	public FrameBuffer getSpotShadowMap() {
		return spotShadowMap;
	}

	public void setSpotShadowMap(FrameBuffer spotShadowMap) {
		this.spotShadowMap = spotShadowMap;
	}
}
