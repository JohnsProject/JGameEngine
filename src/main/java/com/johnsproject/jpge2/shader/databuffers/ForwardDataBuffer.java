package com.johnsproject.jpge2.shader.databuffers;

import java.util.List;

import com.johnsproject.jpge2.dto.FrameBuffer;
import com.johnsproject.jpge2.dto.Light;
import com.johnsproject.jpge2.dto.Texture;
import com.johnsproject.jpge2.shader.ShaderDataBuffer;

public class ForwardDataBuffer implements ShaderDataBuffer {
	
	private FrameBuffer frameBuffer;
	private List<Light> lights;
	
	private int directionalLightIndex = -1;
	private int[] directionalLightFrustum;
	private int[][] directionalLightMatrix;
	private Texture directionalShadowMap;
	
	private int spotLightIndex = -1;
	private int[] spotLightFrustum;
	private int[][] spotLightMatrix;
	private Texture spotShadowMap;

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
}
