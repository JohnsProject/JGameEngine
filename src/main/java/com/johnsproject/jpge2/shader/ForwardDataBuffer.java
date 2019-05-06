package com.johnsproject.jpge2.shader;

import java.util.List;

import com.johnsproject.jpge2.dto.FrameBuffer;
import com.johnsproject.jpge2.dto.Light;
import com.johnsproject.jpge2.primitive.FPMatrix;
import com.johnsproject.jpge2.primitive.Texture;
import com.johnsproject.jpge2.processor.MathProcessor;

public class ForwardDataBuffer implements ShaderDataBuffer {
	
	private FrameBuffer frameBuffer;
	private List<Light> lights;
	
	private int directionalLightIndex = -1;
	private int[] directionalLightCanvas;
	private FPMatrix directionalLightMatrix;
	private Texture directionalShadowMap;
	
	private int spotLightIndex = -1;
	private int[] spotLightCanvas;
	private FPMatrix spotLightMatrix;
	private Texture spotShadowMap;
	
	private int constantAttenuation = MathProcessor.FP_ONE;
	private int linearAttenuation = 14000;
	private int quadraticAttenuation = 90;
	
	private int lightRange = MathProcessor.FP_ONE * 1000;

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

	public FPMatrix getDirectionalLightMatrix() {
		return directionalLightMatrix;
	}

	public void setDirectionalLightMatrix(FPMatrix directionalLightMatrix) {
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

	public FPMatrix getSpotLightMatrix() {
		return spotLightMatrix;
	}

	public void setSpotLightMatrix(FPMatrix spotLightMatrix) {
		this.spotLightMatrix = spotLightMatrix;
	}

	public Texture getSpotShadowMap() {
		return spotShadowMap;
	}

	public void setSpotShadowMap(Texture spotShadowMap) {
		this.spotShadowMap = spotShadowMap;
	}
	
	public int[] getDirectionalLightCanvas() {
		return directionalLightCanvas;
	}

	public void setDirectionalLightCanvas(int[] directionalLightCanvas) {
		this.directionalLightCanvas = directionalLightCanvas;
	}

	public int[] getSpotLightCanvas() {
		return spotLightCanvas;
	}

	public void setSpotLightCanvas(int[] spotLightCanvas) {
		this.spotLightCanvas = spotLightCanvas;
	}

	public int getConstantAttenuation() {
		return constantAttenuation;
	}

	public void setConstantAttenuation(int constantAttenuation) {
		this.constantAttenuation = constantAttenuation;
	}

	public int getLinearAttenuation() {
		return linearAttenuation;
	}

	public void setLinearAttenuation(int linearAttenuation) {
		this.linearAttenuation = linearAttenuation;
	}

	public int getQuadraticAttenuation() {
		return quadraticAttenuation;
	}

	public void setQuadraticAttenuation(int quadraticAttenuation) {
		this.quadraticAttenuation = quadraticAttenuation;
	}

	public int getLightRange() {
		return lightRange;
	}

	public void setLightRange(int lightRange) {
		this.lightRange = lightRange;
	}
}
