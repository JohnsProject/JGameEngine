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
package com.johnsproject.jgameengine.shader;

import static com.johnsproject.jgameengine.math.FixedPointMath.*;

import java.util.Collection;

import com.johnsproject.jgameengine.math.FixedPointMath;
import com.johnsproject.jgameengine.math.MatrixMath;
import com.johnsproject.jgameengine.math.TransformationMath;
import com.johnsproject.jgameengine.math.VectorMath;
import com.johnsproject.jgameengine.model.Camera;
import com.johnsproject.jgameengine.model.Light;
import com.johnsproject.jgameengine.model.Texture;
import com.johnsproject.jgameengine.model.Transform;

public class ForwardShaderBuffer implements ShaderBuffer {
	
	private static final int LIGHT_RANGE = FixedPointMath.toFixedPoint(50000f);
	
	private Camera camera;
	private Collection<Light> lights;
	
	private final int[][] projectionMatrix;
	
	private int directionalLightIndex;
	private int directionalFocalLength;
	private final int[] directionalLightFrustum;
	private final int[] portedDirectionalLightFrustum;
	private final int[][] directionalLightMatrix;
	private final Texture directionalShadowMap;
	
	private int spotLightIndex;
	private int spotFocalLength;
	private final int[] spotLightFrustum;
	private final int[] portedSpotLightFrustum;
	private final int[][] spotLightMatrix;
	private final Texture spotShadowMap;
	
	private int pointLightIndex;
	private int pointFocalLength;
	private final int[] portedPointLightFrustum;
	private final int[] pointLightFrustum;
	private final int[][][] pointLightMatrices;
	private final Texture[] pointShadowMaps;
	
	public ForwardShaderBuffer() {
		this.projectionMatrix = MatrixMath.indentityMatrix();
		
		this.directionalLightIndex = -1;
		this.directionalFocalLength = FP_ONE >> 3;
		this.portedDirectionalLightFrustum = new int[Camera.FRUSTUM_SIZE];
		this.directionalLightFrustum = new int[Camera.FRUSTUM_SIZE];
		this.directionalLightFrustum[Camera.FRUSTUM_LEFT] = 0;
		this.directionalLightFrustum[Camera.FRUSTUM_RIGHT] = FP_ONE;
		this.directionalLightFrustum[Camera.FRUSTUM_TOP] = 0;
		this.directionalLightFrustum[Camera.FRUSTUM_BOTTOM] = FP_ONE;
		this.directionalLightFrustum[Camera.FRUSTUM_NEAR] = FP_ONE;
		this.directionalLightFrustum[Camera.FRUSTUM_FAR] = FP_ONE * 10000;
		this.directionalLightMatrix = MatrixMath.indentityMatrix();
		this.directionalShadowMap = new Texture(512, 512);
		
		this.spotLightIndex = -1;
		this.spotFocalLength = FP_HALF;
		this.portedSpotLightFrustum = new int[Camera.FRUSTUM_SIZE];
		this.spotLightFrustum = new int[Camera.FRUSTUM_SIZE];
		this.spotLightFrustum[Camera.FRUSTUM_LEFT] = 0;
		this.spotLightFrustum[Camera.FRUSTUM_RIGHT] = FP_ONE;
		this.spotLightFrustum[Camera.FRUSTUM_TOP] = 0;
		this.spotLightFrustum[Camera.FRUSTUM_BOTTOM] = FP_ONE;
		this.spotLightFrustum[Camera.FRUSTUM_NEAR] = FP_HALF;
		this.spotLightFrustum[Camera.FRUSTUM_FAR] = FP_ONE * 1000;
		this.spotLightMatrix = MatrixMath.indentityMatrix();
		this.spotShadowMap = new Texture(256, 256);
		
		this.pointLightIndex = -1;
		this.pointFocalLength = FP_ONE >> 5;
		this.portedPointLightFrustum = new int[Camera.FRUSTUM_SIZE];
		this.pointLightFrustum = new int[Camera.FRUSTUM_SIZE];
		this.pointLightFrustum[Camera.FRUSTUM_LEFT] = 0;
		this.pointLightFrustum[Camera.FRUSTUM_RIGHT] = FP_ONE;
		this.pointLightFrustum[Camera.FRUSTUM_TOP] = 0;
		this.pointLightFrustum[Camera.FRUSTUM_BOTTOM] = FP_ONE;
		this.pointLightFrustum[Camera.FRUSTUM_NEAR] = 0;
		this.pointLightFrustum[Camera.FRUSTUM_FAR] = FP_ONE * 1000;
		this.pointLightMatrices = new int[6][MatrixMath.MATRIX_SIZE][MatrixMath.MATRIX_SIZE];
		this.pointShadowMaps = new Texture[6];
		for (int i = 0; i < 6; i++) {
			pointLightMatrices[i] = MatrixMath.indentityMatrix();
			pointShadowMaps[i] = new Texture(256, 256);
		}
	}

	public void setup(Camera camera, Collection<Light> lights) {
		this.camera = camera;
		this.lights = lights;
		shadowLightsSetup(camera, lights);
		renderSetup(camera, lights);
	}
	
	private void renderSetup(Camera camera, Collection<Light> lights) {
		for (Light light: lights) {
			Transform lightTransform = light.getTransform();
			int[] lightPosition = lightTransform.getLocation();
			long dist = VectorMath.squaredDistance(camera.getTransform().getLocation(), lightPosition);
			light.setCulled(dist > LIGHT_RANGE);
		}
	}
	
	private void shadowLightsSetup(Camera camera, Collection<Light> lights) {
		directionalLightIndex = -1;
		spotLightIndex = -1;
		pointLightIndex = -1;
		boolean foundMainDirectionalLight = false;
		long spotDistance = Integer.MAX_VALUE;
		long pointDistance = Integer.MAX_VALUE;
		Transform directionalLightTransform = null;
		Transform spotLightTransform = null;
		Transform pointLightTransform = null;
		int[] cameraLocation = camera.getTransform().getLocation();
		int lightIndex = 0;
		for (Light light: lights) {
			if(light.isActive()) {
				Transform lightTransform = light.getTransform();
				int[] lightLocation = lightTransform.getLocation();
				long dist = VectorMath.squaredDistance(cameraLocation, lightLocation);
				if(dist < LIGHT_RANGE) {
					switch (light.getType()) {
					case DIRECTIONAL:
						if(!foundMainDirectionalLight) {
							directionalLightIndex = lightIndex;
							directionalLightTransform = lightTransform;
							if(light.getTag().equals(Light.MAIN_DIRECTIONAL_LIGHT_TAG)) {
								foundMainDirectionalLight = true;
							}
						}
						break;
					case SPOT:
						if((spotDistance != Integer.MIN_VALUE) && (dist < spotDistance)) {
							spotDistance = dist;
							spotLightIndex = lightIndex;
							spotLightTransform = lightTransform;
							if(light.getTag().equals(Light.MAIN_SPOT_LIGHT_TAG)) {
								spotDistance = Integer.MIN_VALUE;
							}
						}
						break;
					case POINT:
						if((pointDistance != Integer.MIN_VALUE) && (dist < pointDistance)) {
							pointDistance = dist;
							pointLightIndex = lightIndex;
							pointLightTransform = lightTransform;
							if(light.getTag().equals(Light.MAIN_POINT_LIGHT_TAG)) {
								pointDistance = Integer.MIN_VALUE;
							}
						}
						break;					
					}
				}
				lightIndex++;
			}
		}
		if(camera.getTag().equals(Camera.MAIN_CAMERA_TAG)) {
			directionalSetup(camera, directionalLightTransform);
			spotSetup(camera, spotLightTransform);
			pointSetup(camera, pointLightTransform);
		}
	}
	
	private void directionalSetup(Camera camera, Transform lightTransform) {
		if (directionalLightIndex != -1) {
			int portWidth = directionalShadowMap.getWidth();
			int portHeight = directionalShadowMap.getHeight();
			for (int i = 0; i < Camera.FRUSTUM_SIZE; i++) {
				portedDirectionalLightFrustum[i] = directionalLightFrustum[i];
			}
			TransformationMath.screenportFrustum(portedDirectionalLightFrustum, portWidth, portHeight);
			directionalShadowMap.fill(Integer.MAX_VALUE);
			TransformationMath.orthographicMatrix(projectionMatrix, portedDirectionalLightFrustum, directionalFocalLength);
			MatrixMath.multiply(projectionMatrix, lightTransform.getSpaceEnterMatrix(), directionalLightMatrix);
		}
	}
	
	private void spotSetup(Camera camera, Transform lightTransform) {
		if (spotLightIndex != -1) {
			int portWidth = spotShadowMap.getWidth();
			int portHeight = spotShadowMap.getHeight();
			for (int i = 0; i < Camera.FRUSTUM_SIZE; i++) {
				portedSpotLightFrustum[i] = spotLightFrustum[i];
			}
			TransformationMath.screenportFrustum(portedSpotLightFrustum, portWidth, portHeight);
			spotShadowMap.fill(Integer.MAX_VALUE);
			TransformationMath.perspectiveMatrix(projectionMatrix, portedSpotLightFrustum, spotFocalLength);
			MatrixMath.multiply(projectionMatrix, lightTransform.getSpaceEnterMatrix(), spotLightMatrix);
		}
	}
	
	private void pointSetup(Camera camera, Transform lightTransform) {
		if (pointLightIndex != -1) {
			int portWidth = pointShadowMaps[0].getWidth();
			int portHeight = pointShadowMaps[0].getHeight();
			for (int i = 0; i < Camera.FRUSTUM_SIZE; i++) {
				portedPointLightFrustum[i] = pointLightFrustum[i];
			}
			TransformationMath.screenportFrustum(portedPointLightFrustum, portWidth, portHeight);
			for (int i = 0; i < pointShadowMaps.length; i++) {
				pointShadowMaps[i].fill(Integer.MAX_VALUE);
			}
			TransformationMath.perspectiveMatrix(projectionMatrix, portedPointLightFrustum, pointFocalLength);
			final int fixedPoint90 = FixedPointMath.toFixedPoint(90f);
			lightTransform.setRotation(0, 0, 0);
			MatrixMath.multiply(projectionMatrix, lightTransform.getSpaceEnterMatrix(), pointLightMatrices[0]);
			lightTransform.rotate(fixedPoint90, 0, 0);
			MatrixMath.multiply(projectionMatrix, lightTransform.getSpaceEnterMatrix(), pointLightMatrices[1]);
			lightTransform.rotate(fixedPoint90, 0, 0);
			MatrixMath.multiply(projectionMatrix, lightTransform.getSpaceEnterMatrix(), pointLightMatrices[2]);
			lightTransform.rotate(fixedPoint90, 0, 0);
			MatrixMath.multiply(projectionMatrix, lightTransform.getSpaceEnterMatrix(), pointLightMatrices[3]);
			lightTransform.setRotation(0, fixedPoint90, 0);
			MatrixMath.multiply(projectionMatrix, lightTransform.getSpaceEnterMatrix(), pointLightMatrices[4]);
			lightTransform.setRotation(0, -fixedPoint90, 0);
			MatrixMath.multiply(projectionMatrix, lightTransform.getSpaceEnterMatrix(), pointLightMatrices[5]);
			lightTransform.setRotation(0, 0, 0);
		}
	}
	
	public Camera getCamera() {
		return camera;
	}

	public Collection<Light> getLights() {
		return lights;
	}

	public int getDirectionalLightIndex() {
		return directionalLightIndex;
	}

	public int[][] getDirectionalLightMatrix() {
		return directionalLightMatrix;
	}

	public Texture getDirectionalShadowMap() {
		return directionalShadowMap;
	}
	
	public int[] getDirectionalLightFrustum() {
		return portedDirectionalLightFrustum;
	}
	
	public int getSpotLightIndex() {
		return spotLightIndex;
	}

	public int[][] getSpotLightMatrix() {
		return spotLightMatrix;
	}

	public Texture getSpotShadowMap() {
		return spotShadowMap;
	}

	public int[] getSpotLightFrustum() {
		return portedSpotLightFrustum;
	}

	public int getPointLightIndex() {
		return pointLightIndex;
	}

	public int[][][] getPointLightMatrices() {
		return pointLightMatrices;
	}

	public Texture[] getPointShadowMaps() {
		return pointShadowMaps;
	}
	
	public int[] getPointLightFrustum() {
		return portedPointLightFrustum;
	}
}