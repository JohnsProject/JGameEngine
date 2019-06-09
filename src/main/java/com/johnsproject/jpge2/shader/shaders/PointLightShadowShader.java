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
package com.johnsproject.jpge2.shader.shaders;

import java.util.List;

import com.johnsproject.jpge2.dto.Camera;
import com.johnsproject.jpge2.dto.GeometryDataBuffer;
import com.johnsproject.jpge2.dto.Light;
import com.johnsproject.jpge2.dto.LightType;
import com.johnsproject.jpge2.dto.ShaderDataBuffer;
import com.johnsproject.jpge2.dto.Texture;
import com.johnsproject.jpge2.dto.Transform;
import com.johnsproject.jpge2.dto.VertexDataBuffer;
import com.johnsproject.jpge2.library.GraphicsLibrary;
import com.johnsproject.jpge2.library.MathLibrary;
import com.johnsproject.jpge2.library.MatrixLibrary;
import com.johnsproject.jpge2.library.VectorLibrary;
import com.johnsproject.jpge2.shader.FlatTriangle;
import com.johnsproject.jpge2.shader.Shader;
import com.johnsproject.jpge2.shader.databuffers.ForwardDataBuffer;

public class PointLightShadowShader implements Shader {

	private static final int LIGHT_RANGE = MathLibrary.FP_ONE * 1000;
	
	private static final byte VECTOR_X = VectorLibrary.VECTOR_X;
	private static final byte VECTOR_Y = VectorLibrary.VECTOR_Y;
	private static final byte VECTOR_Z = VectorLibrary.VECTOR_Z;

	private static final int FP_ONE = MathLibrary.FP_ONE;
	
	private static final short SHADOW_BIAS = 500;
	
	private final GraphicsLibrary graphicsLibrary;
	private final MatrixLibrary matrixLibrary;
	private final VectorLibrary vectorLibrary;

	private final FlatTriangle triangle;
	
	private int[][] viewMatrix;
	private int[][] projectionMatrix;
	private final int[][][] lightMatrices;
	
	private int[] lightFrustum;
	private final int[] portedFrustum;

	private final int[] location0Cache;
	private final int[] location1Cache;
	private final int[] location2Cache;
	
	private final Texture[] shadowMaps;
	private Texture currentShadowMap;
	private Transform lightTransform;

	private List<Light> lights;
	private ForwardDataBuffer shaderData;

	public PointLightShadowShader() {
		this.graphicsLibrary = new GraphicsLibrary();
		this.matrixLibrary = new MatrixLibrary();
		this.vectorLibrary = new VectorLibrary();
		this.triangle = new FlatTriangle(this);

		this.viewMatrix = matrixLibrary.generate();
		this.projectionMatrix = matrixLibrary.generate();
		this.lightMatrices = new int[6][4][4];
		
		this.location0Cache = vectorLibrary.generate();
		this.location1Cache = vectorLibrary.generate();
		this.location2Cache = vectorLibrary.generate();
		
		this.lightFrustum = new int[Camera.FRUSTUM_SIZE];
		lightFrustum[Camera.FRUSTUM_LEFT] = 0;
		lightFrustum[Camera.FRUSTUM_RIGHT] = FP_ONE;
		lightFrustum[Camera.FRUSTUM_TOP] = 0;
		lightFrustum[Camera.FRUSTUM_BOTTOM] = FP_ONE;
		lightFrustum[Camera.FRUSTUM_NEAR] = FP_ONE / 10;
		lightFrustum[Camera.FRUSTUM_FAR] = FP_ONE * 10000;
		this.portedFrustum = new int[Camera.FRUSTUM_SIZE];
		this.shadowMaps = new Texture[6];
		for (int i = 0; i < shadowMaps.length; i++) {
			shadowMaps[i] = new Texture(64, 64);
		}
	}
	
	public PointLightShadowShader(int width, int height) {
		this.graphicsLibrary = new GraphicsLibrary();
		this.matrixLibrary = new MatrixLibrary();
		this.vectorLibrary = new VectorLibrary();
		this.triangle = new FlatTriangle(this);

		this.viewMatrix = matrixLibrary.generate();
		this.projectionMatrix = matrixLibrary.generate();
		this.lightMatrices = new int[6][4][4];
		
		this.location0Cache = vectorLibrary.generate();
		this.location1Cache = vectorLibrary.generate();
		this.location2Cache = vectorLibrary.generate();
		
		this.lightFrustum = new int[Camera.FRUSTUM_SIZE];
		lightFrustum[Camera.FRUSTUM_LEFT] = 0;
		lightFrustum[Camera.FRUSTUM_RIGHT] = FP_ONE;
		lightFrustum[Camera.FRUSTUM_TOP] = 0;
		lightFrustum[Camera.FRUSTUM_BOTTOM] = FP_ONE;
		lightFrustum[Camera.FRUSTUM_NEAR] = FP_ONE / 10;
		lightFrustum[Camera.FRUSTUM_FAR] = FP_ONE * 10000;
		this.portedFrustum = new int[Camera.FRUSTUM_SIZE];
		this.shadowMaps = new Texture[6];
		for (int i = 0; i < shadowMaps.length; i++) {
			shadowMaps[i] = new Texture(width, height);
		}
	}
	
	public void update(ShaderDataBuffer shaderDataBuffer) {
		shaderData = (ForwardDataBuffer)shaderDataBuffer;
		this.lights = shaderData.getLights();
		if (shaderData.getPointLightIndex() == -1) {
			shaderData.setPointLightFrustum(portedFrustum);
			shaderData.setPointLightMatrices(lightMatrices);
			shaderData.setPointShadowMaps(shadowMaps);
		}
		graphicsLibrary.screenportFrustum(lightFrustum, shadowMaps[0].getWidth(), shadowMaps[0].getHeight(), portedFrustum);
		for (int i = 0; i < shadowMaps.length; i++) {
			shadowMaps[i].fill(Integer.MAX_VALUE);
		}
	}

	public void setup(Camera camera) {
		shaderData.setPointLightIndex(-1);
		if(lights.size() > 0) {
			int[] cameraLocation = camera.getTransform().getLocation();		
			int distance = Integer.MAX_VALUE;
			for (int i = 0; i < lights.size(); i++) {
				Light light = lights.get(i);
				lightTransform = light.getTransform();
				int[] lightPosition = lightTransform.getLocation();
				int dist = vectorLibrary.distance(cameraLocation, lightPosition);
				if ((light.getType() == LightType.POINT) & (dist < distance) & (dist < LIGHT_RANGE)) {
					distance = dist;
					shaderData.setPointLightIndex(i);
				}
			}
			if (shaderData.getPointLightIndex() == -1)
				return;		
			int[][] lightMatrix = lightMatrices[0];
			graphicsLibrary.viewMatrix(viewMatrix, lightTransform);
			graphicsLibrary.perspectiveMatrix(projectionMatrix, portedFrustum);
			matrixLibrary.multiply(projectionMatrix, viewMatrix, lightMatrix);
			lightTransform.rotate(0, 0, 90);
			lightMatrix = lightMatrices[1];
			graphicsLibrary.viewMatrix(viewMatrix, lightTransform);
			graphicsLibrary.perspectiveMatrix(projectionMatrix, portedFrustum);
			matrixLibrary.multiply(projectionMatrix, viewMatrix, lightMatrix);
			lightTransform.rotate(0, 0, 90);
			lightMatrix = lightMatrices[2];
			graphicsLibrary.viewMatrix(viewMatrix, lightTransform);
			graphicsLibrary.perspectiveMatrix(projectionMatrix, portedFrustum);
			matrixLibrary.multiply(projectionMatrix, viewMatrix, lightMatrix);
			lightTransform.rotate(0, 0, 90);
			lightMatrix = lightMatrices[3];
			graphicsLibrary.viewMatrix(viewMatrix, lightTransform);
			graphicsLibrary.perspectiveMatrix(projectionMatrix, portedFrustum);
			matrixLibrary.multiply(projectionMatrix, viewMatrix, lightMatrix);
			lightTransform.rotate(0, 0, -270);
			lightTransform.rotate(90, 0, 0);
			lightMatrix = lightMatrices[4];
			graphicsLibrary.viewMatrix(viewMatrix, lightTransform);
			graphicsLibrary.perspectiveMatrix(projectionMatrix, portedFrustum);
			matrixLibrary.multiply(projectionMatrix, viewMatrix, lightMatrix);
			lightTransform.rotate(-180, 0, 0);
			lightMatrix = lightMatrices[5];
			graphicsLibrary.viewMatrix(viewMatrix, lightTransform);
			graphicsLibrary.perspectiveMatrix(projectionMatrix, portedFrustum);
			matrixLibrary.multiply(projectionMatrix, viewMatrix, lightMatrix);
			lightTransform.rotate(90, 0, 0);
		}
	}
	
	public void vertex(VertexDataBuffer dataBuffer) {
	}

	public void geometry(GeometryDataBuffer dataBuffer) {
		if (shaderData.getPointLightIndex() == -1)
			return;	
		backup(dataBuffer);
		for (int i = 0; i < lightMatrices.length; i++) {
			currentShadowMap = shadowMaps[i];
			for (int j = 0; j < dataBuffer.getVertexDataBuffers().length; j++) {
				int[] vertexLocation = dataBuffer.getVertexDataBuffer(j).getLocation();
				vectorLibrary.multiply(vertexLocation, lightMatrices[i], vertexLocation);
				graphicsLibrary.screenportVector(vertexLocation, portedFrustum, vertexLocation);
			}
			graphicsLibrary.drawFlatTriangle(triangle, dataBuffer, portedFrustum);
			restore(dataBuffer);
		}
	}

	public void fragment(int[] location) {
		int x = location[VECTOR_X];
		int y = location[VECTOR_Y];
		int z = location[VECTOR_Z];
		if (currentShadowMap.getPixel(x, y) > z) {
			currentShadowMap.setPixel(x, y, z + SHADOW_BIAS);
		}
	}
	
	private void backup(GeometryDataBuffer dataBuffer) {
		vectorLibrary.copy(location0Cache, dataBuffer.getVertexDataBuffer(0).getLocation());
		vectorLibrary.copy(location1Cache, dataBuffer.getVertexDataBuffer(1).getLocation());
		vectorLibrary.copy(location2Cache, dataBuffer.getVertexDataBuffer(2).getLocation());
	}
	
	private void restore(GeometryDataBuffer dataBuffer) {
		vectorLibrary.copy(dataBuffer.getVertexDataBuffer(0).getLocation(), location0Cache);
		vectorLibrary.copy(dataBuffer.getVertexDataBuffer(1).getLocation(), location1Cache);
		vectorLibrary.copy(dataBuffer.getVertexDataBuffer(2).getLocation(), location2Cache);
	}

	public void terminate(ShaderDataBuffer shaderDataBuffer) {
		shaderData = (ForwardDataBuffer)shaderDataBuffer;
		shaderData.setPointLightIndex(-1);
		shaderData.setPointLightFrustum(null);
		shaderData.setPointLightMatrices(null);
	}	
}