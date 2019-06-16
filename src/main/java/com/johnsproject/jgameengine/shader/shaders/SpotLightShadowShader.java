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
package com.johnsproject.jgameengine.shader.shaders;

import java.util.List;

import com.johnsproject.jgameengine.dto.Camera;
import com.johnsproject.jgameengine.dto.GeometryDataBuffer;
import com.johnsproject.jgameengine.dto.Light;
import com.johnsproject.jgameengine.dto.LightType;
import com.johnsproject.jgameengine.dto.Model;
import com.johnsproject.jgameengine.dto.ShaderDataBuffer;
import com.johnsproject.jgameengine.dto.Texture;
import com.johnsproject.jgameengine.dto.Transform;
import com.johnsproject.jgameengine.dto.VertexDataBuffer;
import com.johnsproject.jgameengine.library.GraphicsLibrary;
import com.johnsproject.jgameengine.library.MathLibrary;
import com.johnsproject.jgameengine.library.MatrixLibrary;
import com.johnsproject.jgameengine.library.VectorLibrary;
import com.johnsproject.jgameengine.shader.FlatTriangle;
import com.johnsproject.jgameengine.shader.Shader;
import com.johnsproject.jgameengine.shader.databuffers.ForwardDataBuffer;

public class SpotLightShadowShader implements Shader {

	private static final int LIGHT_RANGE = MathLibrary.FP_ONE * 1000;
	
	private static final byte VECTOR_X = VectorLibrary.VECTOR_X;
	private static final byte VECTOR_Y = VectorLibrary.VECTOR_Y;
	private static final byte VECTOR_Z = VectorLibrary.VECTOR_Z;

	private static final int FP_ONE = MathLibrary.FP_ONE;
	
	private static final short SHADOW_BIAS = 50;
	
	private final GraphicsLibrary graphicsLibrary;
	private final MatrixLibrary matrixLibrary;
	private final VectorLibrary vectorLibrary;

	private final FlatTriangle triangle;
	
	private int[] modelMatrix;
	private int[] projectionMatrix;
	private final int[] lightMatrix;
	
	private int[] lightFrustum;
	private final int[] portedFrustum;

	private final Texture shadowMap;

	private List<Light> lights;
	private ForwardDataBuffer shaderData;

	public SpotLightShadowShader() {
		this.graphicsLibrary = new GraphicsLibrary();
		this.matrixLibrary = new MatrixLibrary();
		this.vectorLibrary = new VectorLibrary();
		this.triangle = new FlatTriangle(this);

		this.modelMatrix = matrixLibrary.generate();
		this.projectionMatrix = matrixLibrary.generate();
		this.lightMatrix = matrixLibrary.generate();
		
		this.lightFrustum = new int[Camera.FRUSTUM_SIZE];
		lightFrustum[Camera.FRUSTUM_LEFT] = 0;
		lightFrustum[Camera.FRUSTUM_RIGHT] = FP_ONE;
		lightFrustum[Camera.FRUSTUM_TOP] = 0;
		lightFrustum[Camera.FRUSTUM_BOTTOM] = FP_ONE;
		lightFrustum[Camera.FRUSTUM_NEAR] = FP_ONE / 5;
		lightFrustum[Camera.FRUSTUM_FAR] = FP_ONE * 10000;
		this.portedFrustum = new int[Camera.FRUSTUM_SIZE];
		this.shadowMap = new Texture(64, 64);
	}
	
	public SpotLightShadowShader(int width, int height) {
		this.graphicsLibrary = new GraphicsLibrary();
		this.matrixLibrary = new MatrixLibrary();
		this.vectorLibrary = new VectorLibrary();
		this.triangle = new FlatTriangle(this);

		this.modelMatrix = matrixLibrary.generate();
		this.projectionMatrix = matrixLibrary.generate();
		this.lightMatrix = matrixLibrary.generate();
		
		this.lightFrustum = new int[Camera.FRUSTUM_SIZE];
		lightFrustum[Camera.FRUSTUM_LEFT] = 0;
		lightFrustum[Camera.FRUSTUM_RIGHT] = FP_ONE;
		lightFrustum[Camera.FRUSTUM_TOP] = 0;
		lightFrustum[Camera.FRUSTUM_BOTTOM] = FP_ONE;
		lightFrustum[Camera.FRUSTUM_NEAR] = FP_ONE / 5;
		lightFrustum[Camera.FRUSTUM_FAR] = FP_ONE * 10000;
		this.portedFrustum = new int[Camera.FRUSTUM_SIZE];
		this.shadowMap = new Texture(width, height);
	}
	
	public void update(ShaderDataBuffer shaderDataBuffer) {
		shaderData = (ForwardDataBuffer)shaderDataBuffer;
		this.lights = shaderData.getLights();
		if (shaderData.getSpotLightIndex() == -1) {
			shaderData.setSpotLightFrustum(portedFrustum);
			shaderData.setSpotLightMatrix(lightMatrix);
			shaderData.setSpotShadowMap(shadowMap);
		}
		graphicsLibrary.screenportFrustum(lightFrustum, shadowMap.getWidth(), shadowMap.getHeight(), portedFrustum);
		shadowMap.fill(Integer.MAX_VALUE);
	}

	public void setup(Camera camera) {
		shaderData.setSpotLightIndex(-1);
		if(lights.size() > 0) {
			Transform lightTransform = lights.get(0).getTransform();
			int[] cameraLocation = camera.getTransform().getLocation();		
			int distance = Integer.MAX_VALUE;
			for (int i = 0; i < lights.size(); i++) {
				Light light = lights.get(i);
				lightTransform = light.getTransform();
				int[] lightPosition = lightTransform.getLocation();
				int dist = vectorLibrary.distance(cameraLocation, lightPosition);
				if ((light.getType() == LightType.SPOT) & (dist < distance) & (dist < LIGHT_RANGE)) {
					distance = dist;
					shaderData.setSpotLightIndex(i);
				}
			}
			if (shaderData.getSpotLightIndex() == -1)
				return;
			graphicsLibrary.viewMatrix(modelMatrix, lightTransform);
			graphicsLibrary.perspectiveMatrix(projectionMatrix, portedFrustum);
			matrixLibrary.multiply(projectionMatrix, modelMatrix, lightMatrix);
		}
	}
	
	public void setup(Model model) {
		graphicsLibrary.modelMatrix(modelMatrix, model.getTransform());
	}
	
	public void vertex(VertexDataBuffer dataBuffer) {
		if (shaderData.getSpotLightIndex() == -1)
			return;
		int[] location = dataBuffer.getLocation();
		vectorLibrary.matrixMultiply(location, modelMatrix, location);
		vectorLibrary.matrixMultiply(location, lightMatrix, location);
		graphicsLibrary.screenportVector(location, portedFrustum, location);
	}

	public void geometry(GeometryDataBuffer dataBuffer) {
		if (shaderData.getSpotLightIndex() == -1)
			return;
		graphicsLibrary.drawFlatTriangle(triangle, dataBuffer, portedFrustum);
	}

	public void fragment(int[] location) {
		int x = location[VECTOR_X];
		int y = location[VECTOR_Y];
		int z = location[VECTOR_Z];
		if (shadowMap.getPixel(x, y) > z) {
			shadowMap.setPixel(x, y, z + SHADOW_BIAS);
		}
	}

	public void terminate(ShaderDataBuffer shaderDataBuffer) {
		shaderData = (ForwardDataBuffer)shaderDataBuffer;
		shaderData.setSpotLightIndex(-1);
		shaderData.setSpotLightFrustum(null);
		shaderData.setSpotLightMatrix(null);
		shaderData.setSpotShadowMap(null);
	}	
}
