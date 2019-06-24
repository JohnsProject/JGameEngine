/**
 * MIT License
 *
 * Copyright (c) 2018 John Salomon - John´s Project
 *  
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.johnsproject.jgameengine.model;

import java.util.List;

public class ShaderBuffer {
	
	private FrameBuffer frameBuffer;
	private List<Light> lights;

	private int directionalLightIndex = -1;
	private int[] directionalLightFrustum;
	private int[] directionalLightMatrix;
	private Texture directionalShadowMap;
	
	private int spotLightIndex = -1;
	private int[] spotLightFrustum;
	private int[] spotLightMatrix;
	private Texture spotShadowMap;
	
	private int pointLightIndex = -1;
	private int[] pointLightFrustum;
	private int[][] pointLightMatrices;
	private Texture[] pointShadowMaps;
	
	private boolean earlyDepthBuffering = false;
	private Texture earlyDepthBuffer;
	
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

	public int[] getDirectionalLightMatrix() {
		return directionalLightMatrix;
	}

	public void setDirectionalLightMatrix(int[] directionalLightMatrix) {
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

	public int[] getSpotLightMatrix() {
		return spotLightMatrix;
	}

	public void setSpotLightMatrix(int[] spotLightMatrix) {
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

	public int[][] getPointLightMatrices() {
		return pointLightMatrices;
	}

	public void setPointLightMatrices(int[][] pointLightMatrices) {
		this.pointLightMatrices = pointLightMatrices;
	}

	public Texture[] getPointShadowMaps() {
		return pointShadowMaps;
	}

	public void setPointShadowMaps(Texture[] pointShadowMaps) {
		this.pointShadowMaps = pointShadowMaps;
	}

	public boolean isEarlyDepthBuffering() {
		return earlyDepthBuffering;
	}

	public void setEarlyDepthBuffering(boolean earlyDepthBuffering) {
		this.earlyDepthBuffering = earlyDepthBuffering;
	}

	public Texture getEarlyDepthBuffer() {
		return earlyDepthBuffer;
	}

	public void setEarlyDepthBuffer(Texture earlyDepthBuffer) {
		this.earlyDepthBuffer = earlyDepthBuffer;
	}
}