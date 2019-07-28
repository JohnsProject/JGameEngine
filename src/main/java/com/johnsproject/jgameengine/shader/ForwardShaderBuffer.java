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

import java.util.List;

import com.johnsproject.jgameengine.library.GraphicsLibrary;
import com.johnsproject.jgameengine.library.MathLibrary;
import com.johnsproject.jgameengine.library.MatrixLibrary;
import com.johnsproject.jgameengine.library.VectorLibrary;
import com.johnsproject.jgameengine.model.Camera;
import com.johnsproject.jgameengine.model.FrameBuffer;
import com.johnsproject.jgameengine.model.Light;
import com.johnsproject.jgameengine.model.LightType;
import com.johnsproject.jgameengine.model.Texture;
import com.johnsproject.jgameengine.model.Transform;

public class ForwardShaderBuffer implements ShaderBuffer {
	
	private static final int LIGHT_RANGE = MathLibrary.FP_ONE * 300;
	
	private static final int FP_ONE = MathLibrary.FP_ONE;
	
	private final GraphicsLibrary graphicsLibrary;
	private final MatrixLibrary matrixLibrary;
	private final VectorLibrary vectorLibrary;
	
	private Camera camera;
	private FrameBuffer frameBuffer;
	private List<Light> lights;

	private final int[] viewMatrix;
	private final int[] projectionMatrix;
	private final int[] portedFrustum;
	
	private int directionalLightIndex;
	private final int[] directionalLightFrustum;
	private final int[] portedDirectionalLightFrustum;
	private final int[] directionalLightMatrix;
	private final Texture directionalShadowMap;
	
	private int spotLightIndex;
	private final int[] spotLightFrustum;
	private final int[] portedSpotLightFrustum;
	private final int[] spotLightMatrix;
	private final Texture spotShadowMap;
	
	private int pointLightIndex;
	private final int[] portedPointLightFrustum;
	private final int[] pointLightFrustum;
	private final int[][] pointLightMatrices;
	private final Texture[] pointShadowMaps;
	
	public ForwardShaderBuffer() {
		this.graphicsLibrary = new GraphicsLibrary();
		this.matrixLibrary = new MatrixLibrary();
		this.vectorLibrary = new VectorLibrary();
		
		this.viewMatrix = MatrixLibrary.generate();
		this.projectionMatrix = MatrixLibrary.generate();
		this.portedFrustum = new int[Camera.FRUSTUM_SIZE];
		
		this.directionalLightIndex = -1;
		this.portedDirectionalLightFrustum = new int[Camera.FRUSTUM_SIZE];
		this.directionalLightFrustum = new int[Camera.FRUSTUM_SIZE];
		this.directionalLightFrustum[Camera.FRUSTUM_LEFT] = 0;
		this.directionalLightFrustum[Camera.FRUSTUM_RIGHT] = FP_ONE;
		this.directionalLightFrustum[Camera.FRUSTUM_TOP] = 0;
		this.directionalLightFrustum[Camera.FRUSTUM_BOTTOM] = FP_ONE;
		this.directionalLightFrustum[Camera.FRUSTUM_NEAR] = FP_ONE / 15;
		this.directionalLightFrustum[Camera.FRUSTUM_FAR] = FP_ONE * 10000;
		this.directionalLightMatrix = MatrixLibrary.generate();
		this.directionalShadowMap = new Texture(128, 128);
		
		this.spotLightIndex = -1;
		this.portedSpotLightFrustum = new int[Camera.FRUSTUM_SIZE];
		this.spotLightFrustum = new int[Camera.FRUSTUM_SIZE];
		this.spotLightFrustum[Camera.FRUSTUM_LEFT] = 0;
		this.spotLightFrustum[Camera.FRUSTUM_RIGHT] = FP_ONE;
		this.spotLightFrustum[Camera.FRUSTUM_TOP] = 0;
		this.spotLightFrustum[Camera.FRUSTUM_BOTTOM] = FP_ONE;
		this.spotLightFrustum[Camera.FRUSTUM_NEAR] = FP_ONE / 50;
		this.spotLightFrustum[Camera.FRUSTUM_FAR] = FP_ONE * 10000;
		this.spotLightMatrix = MatrixLibrary.generate();
		this.spotShadowMap = new Texture(64, 64);
		
		this.pointLightIndex = -1;
		this.portedPointLightFrustum = new int[Camera.FRUSTUM_SIZE];
		this.pointLightFrustum = new int[Camera.FRUSTUM_SIZE];
		this.pointLightFrustum[Camera.FRUSTUM_LEFT] = 0;
		this.pointLightFrustum[Camera.FRUSTUM_RIGHT] = FP_ONE;
		this.pointLightFrustum[Camera.FRUSTUM_TOP] = 0;
		this.pointLightFrustum[Camera.FRUSTUM_BOTTOM] = FP_ONE;
		this.pointLightFrustum[Camera.FRUSTUM_NEAR] = FP_ONE / 50;
		this.pointLightFrustum[Camera.FRUSTUM_FAR] = FP_ONE * 10000;
		this.pointLightMatrices = new int[6][MatrixLibrary.MATRIX_SIZE];
		this.pointShadowMaps = new Texture[6];
		for (int i = 0; i < pointShadowMaps.length; i++) {
			pointShadowMaps[i] = new Texture(64, 64);
		}
	}
	
	public void setup(Camera camera, List<Light> lights, FrameBuffer frameBuffer) {
		this.camera = camera;
		this.lights = lights;
		this.frameBuffer = frameBuffer;		
		if(camera.getTag().equals(Camera.MAIN_CAMERA_TAG)) {
			directionalSetup(camera, lights, frameBuffer);
			spotSetup(camera, lights, frameBuffer);
			pointSetup(camera, lights, frameBuffer);
		}
		usualSetup(camera, lights, frameBuffer);
	}
	
	private void usualSetup(Camera camera, List<Light> lights, FrameBuffer frameBuffer) {
		int portWidth = frameBuffer.getWidth();
		int portHeight = frameBuffer.getHeight();
		graphicsLibrary.viewMatrix(viewMatrix, camera.getTransform());
		graphicsLibrary.screenportFrustum(camera.getFrustum(), portWidth, portHeight, portedFrustum);
		switch (camera.getType()) {
		case ORTHOGRAPHIC:
			graphicsLibrary.orthographicMatrix(projectionMatrix, portedFrustum);
			break;

		case PERSPECTIVE:
			graphicsLibrary.perspectiveMatrix(projectionMatrix, portedFrustum);
			break;
		}
	}
	
	private void directionalSetup(Camera camera, List<Light> lights, FrameBuffer frameBuffer) {
		int portWidth = directionalShadowMap.getWidth();
		int portHeight = directionalShadowMap.getHeight();
		graphicsLibrary.screenportFrustum(directionalLightFrustum, portWidth, portHeight, portedDirectionalLightFrustum);
		directionalShadowMap.fill(Integer.MAX_VALUE);
		directionalLightIndex = -1;
		if(lights.size() > 0) {
			Transform lightTransform = lights.get(0).getTransform();
			int[] cameraLocation = camera.getTransform().getLocation();		
			int distance = Integer.MAX_VALUE;
			for (int i = 0; i < lights.size(); i++) {
				Light light = lights.get(i);
				lightTransform = light.getTransform();
				int[] lightPosition = lightTransform.getLocation();
				int dist = vectorLibrary.averagedDistance(cameraLocation, lightPosition);
				if ((light.getType() == LightType.DIRECTIONAL) & (dist < distance) & (dist < LIGHT_RANGE)) {
					distance = dist;
					directionalLightIndex = i;
				}
			}
			if (directionalLightIndex != -1) {
				lightTransform = lights.get(directionalLightIndex).getTransform();
				graphicsLibrary.viewMatrix(viewMatrix, lightTransform);
				graphicsLibrary.orthographicMatrix(projectionMatrix, portedDirectionalLightFrustum);
				matrixLibrary.multiply(projectionMatrix, viewMatrix, directionalLightMatrix);
			}
		}	
	}
	
	private void spotSetup(Camera camera, List<Light> lights, FrameBuffer frameBuffer) {
		int portWidth = spotShadowMap.getWidth();
		int portHeight = spotShadowMap.getHeight();
		graphicsLibrary.screenportFrustum(spotLightFrustum, portWidth, portHeight, portedSpotLightFrustum);
		spotShadowMap.fill(Integer.MAX_VALUE);
		spotLightIndex = -1;
		if(lights.size() > 0) {
			Transform lightTransform = lights.get(0).getTransform();
			int[] cameraLocation = camera.getTransform().getLocation();		
			int distance = Integer.MAX_VALUE;
			for (int i = 0; i < lights.size(); i++) {
				Light light = lights.get(i);
				lightTransform = light.getTransform();
				int[] lightPosition = lightTransform.getLocation();
				int dist = vectorLibrary.averagedDistance(cameraLocation, lightPosition);
				if ((light.getType() == LightType.SPOT) & (dist < distance) & (dist < LIGHT_RANGE)) {
					distance = dist;
					spotLightIndex = i;
				}
			}
			if (spotLightIndex != -1) {
				lightTransform = lights.get(spotLightIndex).getTransform();
				graphicsLibrary.viewMatrix(viewMatrix, lightTransform);
				graphicsLibrary.perspectiveMatrix(projectionMatrix, portedSpotLightFrustum);
				matrixLibrary.multiply(projectionMatrix, viewMatrix, spotLightMatrix);
			}
		}
	}
	
	private void pointSetup(Camera camera, List<Light> lights, FrameBuffer frameBuffer) {
		int portWidth = pointShadowMaps[0].getWidth();
		int portHeight = pointShadowMaps[0].getHeight();
		graphicsLibrary.screenportFrustum(pointLightFrustum, portWidth, portHeight, portedPointLightFrustum);
		for (int i = 0; i < pointShadowMaps.length; i++) {
			pointShadowMaps[i].fill(Integer.MAX_VALUE);
		}
		pointLightIndex = -1;
		if(lights.size() > 0) {
			int[] cameraLocation = camera.getTransform().getLocation();		
			int distance = Integer.MAX_VALUE;
			for (int i = 0; i < lights.size(); i++) {
				Light light = lights.get(i);
				Transform lightTransform = light.getTransform();
				int[] lightLocation = lightTransform.getLocation();
				int dist = vectorLibrary.averagedDistance(cameraLocation, lightLocation);
				if ((light.getType() == LightType.POINT) & (dist < distance) & (dist < LIGHT_RANGE)) {
					distance = dist;
					pointLightIndex = i;
				}
			}
			if (pointLightIndex != -1) {
				Transform lightTransform = lights.get(pointLightIndex).getTransform();
				graphicsLibrary.viewMatrix(viewMatrix, lightTransform);
				graphicsLibrary.perspectiveMatrix(projectionMatrix, portedPointLightFrustum);
				matrixLibrary.multiply(projectionMatrix, viewMatrix, pointLightMatrices[0]);
				lightTransform.rotate(0, 0, 90);
				graphicsLibrary.viewMatrix(viewMatrix, lightTransform);
				graphicsLibrary.perspectiveMatrix(projectionMatrix, portedPointLightFrustum);
				matrixLibrary.multiply(projectionMatrix, viewMatrix, pointLightMatrices[1]);
				lightTransform.rotate(0, 0, 90);
				graphicsLibrary.viewMatrix(viewMatrix, lightTransform);
				graphicsLibrary.perspectiveMatrix(projectionMatrix, portedPointLightFrustum);
				matrixLibrary.multiply(projectionMatrix, viewMatrix, pointLightMatrices[2]);
				lightTransform.rotate(0, 0, 90);
				graphicsLibrary.viewMatrix(viewMatrix, lightTransform);
				graphicsLibrary.perspectiveMatrix(projectionMatrix, portedPointLightFrustum);
				matrixLibrary.multiply(projectionMatrix, viewMatrix, pointLightMatrices[3]);
				lightTransform.rotate(0, 0, -270);
				lightTransform.rotate(90, 0, 0);
				graphicsLibrary.viewMatrix(viewMatrix, lightTransform);
				graphicsLibrary.perspectiveMatrix(projectionMatrix, portedPointLightFrustum);
				matrixLibrary.multiply(projectionMatrix, viewMatrix, pointLightMatrices[4]);
				lightTransform.rotate(-180, 0, 0);
				graphicsLibrary.viewMatrix(viewMatrix, lightTransform);
				graphicsLibrary.perspectiveMatrix(projectionMatrix, portedPointLightFrustum);
				matrixLibrary.multiply(projectionMatrix, viewMatrix, pointLightMatrices[5]);
				lightTransform.rotate(90, 0, 0);
			}
		}
	}
	
	public Camera getCamera() {
		return camera;
	}
	
	public FrameBuffer getFrameBuffer() {
		return frameBuffer;
	}

	public List<Light> getLights() {
		return lights;
	}
	
	public int[] getViewMatrix() {
		return viewMatrix;
	}

	public int[] getProjectionMatrix() {
		return projectionMatrix;
	}

	public int[] getPortedFrustum() {
		return portedFrustum;
	}

	public int getDirectionalLightIndex() {
		return directionalLightIndex;
	}

	public int[] getDirectionalLightMatrix() {
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

	public int[] getSpotLightMatrix() {
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

	public int[][] getPointLightMatrices() {
		return pointLightMatrices;
	}

	public Texture[] getPointShadowMaps() {
		return pointShadowMaps;
	}
	
	public int[] getPointLightFrustum() {
		return portedPointLightFrustum;
	}
}