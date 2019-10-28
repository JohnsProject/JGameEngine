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
import com.johnsproject.jgameengine.model.Texture;
import com.johnsproject.jgameengine.model.Transform;

import static com.johnsproject.jgameengine.library.MathLibrary.*;

public class ForwardShaderBuffer implements ShaderBuffer {
	
	private static final int LIGHT_RANGE = MathLibrary.generate(150);
	
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
	private int directionalFocalLength;
	private final int[] directionalLightFrustum;
	private final int[] portedDirectionalLightFrustum;
	private final int[] directionalLightMatrix;
	private final Texture directionalShadowMap;
	
	private int spotLightIndex;
	private int spotFocalLength;
	private final int[] spotLightFrustum;
	private final int[] portedSpotLightFrustum;
	private final int[] spotLightMatrix;
	private final Texture spotShadowMap;
	
	private int pointLightIndex;
	private int pointFocalLength;
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
		this.directionalFocalLength = FP_ONE >> 3;
		this.portedDirectionalLightFrustum = new int[Camera.FRUSTUM_SIZE];
		this.directionalLightFrustum = new int[Camera.FRUSTUM_SIZE];
		this.directionalLightFrustum[Camera.FRUSTUM_LEFT] = 0;
		this.directionalLightFrustum[Camera.FRUSTUM_RIGHT] = FP_ONE;
		this.directionalLightFrustum[Camera.FRUSTUM_TOP] = 0;
		this.directionalLightFrustum[Camera.FRUSTUM_BOTTOM] = FP_ONE;
		this.directionalLightFrustum[Camera.FRUSTUM_NEAR] = FP_ONE;
		this.directionalLightFrustum[Camera.FRUSTUM_FAR] = FP_ONE * 10000;
		this.directionalLightMatrix = MatrixLibrary.generate();
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
		this.spotLightMatrix = MatrixLibrary.generate();
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
		this.pointLightMatrices = new int[6][MatrixLibrary.MATRIX_SIZE];
		this.pointShadowMaps = new Texture[6];
		for (int i = 0; i < pointShadowMaps.length; i++) {
			pointShadowMaps[i] = new Texture(256, 256);
		}
	}
	
	public void setup(Camera camera, List<Light> lights, FrameBuffer frameBuffer) {
		this.camera = camera;
		this.lights = lights;
		this.frameBuffer = frameBuffer;		
		shadowLightsSetup(camera, lights);
		if(camera.getTag().equals(Camera.MAIN_CAMERA_TAG)) {
			directionalSetup(camera, lights, frameBuffer);
			spotSetup(camera, lights, frameBuffer);
			pointSetup(camera, lights, frameBuffer);
		}
		renderSetup(camera, lights, frameBuffer);
	}
	
	private void renderSetup(Camera camera, List<Light> lights, FrameBuffer frameBuffer) {
		int portWidth = frameBuffer.getWidth();
		int portHeight = frameBuffer.getHeight();
		graphicsLibrary.viewMatrix(viewMatrix, camera.getTransform());
		graphicsLibrary.screenportFrustum(camera.getFrustum(), portWidth, portHeight, portedFrustum);
		switch (camera.getType()) {
		case ORTHOGRAPHIC:
			graphicsLibrary.orthographicMatrix(projectionMatrix, camera.getFocalLength());
			break;

		case PERSPECTIVE:
			graphicsLibrary.perspectiveMatrix(projectionMatrix, camera.getFocalLength());
			break;
		}
		for (int i = 0; i < lights.size(); i++) {
			Light light = lights.get(i);
			Transform lightTransform = light.getTransform();
			int[] lightPosition = lightTransform.getLocation();
			int dist = vectorLibrary.averagedDistance(camera.getTransform().getLocation(), lightPosition);
			light.setCulled(dist > LIGHT_RANGE);
		}
	}
	
	private void shadowLightsSetup(Camera camera, List<Light> lights) {
		directionalLightIndex = -1;
		spotLightIndex = -1;
		pointLightIndex = -1;
		int[] cameraLocation = camera.getTransform().getLocation();		
		int distance = Integer.MAX_VALUE;
		for (int i = 0; i < lights.size(); i++) {
			Light light = lights.get(i);
			if(light.isActive()) {
				Transform lightTransform = light.getTransform();
				int[] lightLocation = lightTransform.getLocation();
				int dist = vectorLibrary.averagedDistance(cameraLocation, lightLocation);
				if((dist < distance) && (dist < LIGHT_RANGE)) {
					switch (light.getType()) {
					case DIRECTIONAL:
						distance = dist;
						directionalLightIndex = i;
						break;
					case SPOT:
						distance = dist;
						spotLightIndex = i;
						break;
					case POINT:
						distance = dist;
						pointLightIndex = i;
						break;					
					}
				}
			}
		}
	}
	
	private void directionalSetup(Camera camera, List<Light> lights, FrameBuffer frameBuffer) {
		int portWidth = directionalShadowMap.getWidth();
		int portHeight = directionalShadowMap.getHeight();
		graphicsLibrary.screenportFrustum(directionalLightFrustum, portWidth, portHeight, portedDirectionalLightFrustum);
		directionalShadowMap.fill(Integer.MAX_VALUE);
		if (directionalLightIndex != -1) {
			Transform lightTransform = lights.get(directionalLightIndex).getTransform();
			graphicsLibrary.viewMatrix(viewMatrix, lightTransform);
			graphicsLibrary.orthographicMatrix(projectionMatrix, directionalFocalLength);
			matrixLibrary.multiply(projectionMatrix, viewMatrix, directionalLightMatrix);
		}
	}
	
	private void spotSetup(Camera camera, List<Light> lights, FrameBuffer frameBuffer) {
		int portWidth = spotShadowMap.getWidth();
		int portHeight = spotShadowMap.getHeight();
		graphicsLibrary.screenportFrustum(spotLightFrustum, portWidth, portHeight, portedSpotLightFrustum);
		spotShadowMap.fill(Integer.MAX_VALUE);
		if (spotLightIndex != -1) {
			Transform lightTransform = lights.get(spotLightIndex).getTransform();
			graphicsLibrary.viewMatrix(viewMatrix, lightTransform);
			graphicsLibrary.perspectiveMatrix(projectionMatrix, spotFocalLength);
			matrixLibrary.multiply(projectionMatrix, viewMatrix, spotLightMatrix);
		}
	}
	
	private void pointSetup(Camera camera, List<Light> lights, FrameBuffer frameBuffer) {
		int portWidth = pointShadowMaps[0].getWidth();
		int portHeight = pointShadowMaps[0].getHeight();
		graphicsLibrary.screenportFrustum(pointLightFrustum, portWidth, portHeight, portedPointLightFrustum);
		for (int i = 0; i < pointShadowMaps.length; i++) {
			pointShadowMaps[i].fill(Integer.MAX_VALUE);
		}
		if (pointLightIndex != -1) {
			final int fixedPoint90 = MathLibrary.generate(90);
			Transform lightTransform = lights.get(pointLightIndex).getTransform();
			lightTransform.setRotation(0, fixedPoint90, 0);
			graphicsLibrary.viewMatrix(viewMatrix, lightTransform);
			graphicsLibrary.perspectiveMatrix(projectionMatrix, pointFocalLength);
			matrixLibrary.multiply(projectionMatrix, viewMatrix, pointLightMatrices[0]);
			lightTransform.rotate(0, 0, fixedPoint90);
			graphicsLibrary.viewMatrix(viewMatrix, lightTransform);
			graphicsLibrary.perspectiveMatrix(projectionMatrix, pointFocalLength);
			matrixLibrary.multiply(projectionMatrix, viewMatrix, pointLightMatrices[1]);
			lightTransform.rotate(0, 0, fixedPoint90);
			graphicsLibrary.viewMatrix(viewMatrix, lightTransform);
			graphicsLibrary.perspectiveMatrix(projectionMatrix, pointFocalLength);
			matrixLibrary.multiply(projectionMatrix, viewMatrix, pointLightMatrices[2]);
			lightTransform.rotate(0, 0, fixedPoint90);
			graphicsLibrary.viewMatrix(viewMatrix, lightTransform);
			graphicsLibrary.perspectiveMatrix(projectionMatrix, pointFocalLength);
			matrixLibrary.multiply(projectionMatrix, viewMatrix, pointLightMatrices[3]);
			lightTransform.rotate(0, 0, -fixedPoint90 * 3);
			lightTransform.rotate(0, fixedPoint90, 0);
			graphicsLibrary.viewMatrix(viewMatrix, lightTransform);
			graphicsLibrary.perspectiveMatrix(projectionMatrix, pointFocalLength);
			matrixLibrary.multiply(projectionMatrix, viewMatrix, pointLightMatrices[4]);
			lightTransform.rotate(0, -fixedPoint90 * 2, 0);
			graphicsLibrary.viewMatrix(viewMatrix, lightTransform);
			graphicsLibrary.perspectiveMatrix(projectionMatrix, pointFocalLength);
			matrixLibrary.multiply(projectionMatrix, viewMatrix, pointLightMatrices[5]);
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