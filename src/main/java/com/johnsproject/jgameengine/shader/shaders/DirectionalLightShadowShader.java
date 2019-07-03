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

import com.johnsproject.jgameengine.library.GraphicsLibrary;
import com.johnsproject.jgameengine.library.MathLibrary;
import com.johnsproject.jgameengine.library.MatrixLibrary;
import com.johnsproject.jgameengine.library.VectorLibrary;
import com.johnsproject.jgameengine.model.Camera;
import com.johnsproject.jgameengine.model.GeometryBuffer;
import com.johnsproject.jgameengine.model.Light;
import com.johnsproject.jgameengine.model.LightType;
import com.johnsproject.jgameengine.model.ShaderBuffer;
import com.johnsproject.jgameengine.model.Texture;
import com.johnsproject.jgameengine.model.Transform;
import com.johnsproject.jgameengine.model.VertexBuffer;
import com.johnsproject.jgameengine.shader.FlatTriangle;
import com.johnsproject.jgameengine.shader.Shader;

public class DirectionalLightShadowShader implements Shader {

	private static final int LIGHT_RANGE = MathLibrary.FP_ONE * 300;
	
	private static final byte VECTOR_X = VectorLibrary.VECTOR_X;
	private static final byte VECTOR_Y = VectorLibrary.VECTOR_Y;
	private static final byte VECTOR_Z = VectorLibrary.VECTOR_Z;
	
	private static final int FP_ONE = MathLibrary.FP_ONE;
	
	private static final short SHADOW_BIAS = 50;
	
	private final GraphicsLibrary graphicsLibrary;
	private final MatrixLibrary matrixLibrary;
	private final VectorLibrary vectorLibrary;

	private final FlatTriangle triangle;
	
	private final int[] viewMatrix;
	private final int[] projectionMatrix;
	private final int[] lightMatrix;
	
	private final int[] lightFrustum;
	private final int[] portedFrustum;
	
	private final Texture shadowMap;
	
	private List<Light> lights;
	private ShaderBuffer shaderBuffer;

	public DirectionalLightShadowShader() {
		this.graphicsLibrary = new GraphicsLibrary();
		this.matrixLibrary = new MatrixLibrary();
		this.vectorLibrary = new VectorLibrary();
		this.triangle = new FlatTriangle(this);

		this.viewMatrix = matrixLibrary.generate();
		this.projectionMatrix = matrixLibrary.generate();
		this.lightMatrix = matrixLibrary.generate();
		
		this.lightFrustum = new int[Camera.FRUSTUM_SIZE];
		lightFrustum[Camera.FRUSTUM_LEFT] = 0;
		lightFrustum[Camera.FRUSTUM_RIGHT] = FP_ONE;
		lightFrustum[Camera.FRUSTUM_TOP] = 0;
		lightFrustum[Camera.FRUSTUM_BOTTOM] = FP_ONE;
		lightFrustum[Camera.FRUSTUM_NEAR] = FP_ONE / 10;
		lightFrustum[Camera.FRUSTUM_FAR] = FP_ONE * 10000;
		this.portedFrustum = new int[Camera.FRUSTUM_SIZE];
		this.shadowMap = new Texture(64, 64);
	}
	
	public DirectionalLightShadowShader(int width, int height) {
		this.graphicsLibrary = new GraphicsLibrary();
		this.matrixLibrary = new MatrixLibrary();
		this.vectorLibrary = new VectorLibrary();
		this.triangle = new FlatTriangle(this);
		
		this.viewMatrix = matrixLibrary.generate();
		this.projectionMatrix = matrixLibrary.generate();
		this.lightMatrix = matrixLibrary.generate();
		
		this.lightFrustum = new int[Camera.FRUSTUM_SIZE];
		lightFrustum[Camera.FRUSTUM_LEFT] = 0;
		lightFrustum[Camera.FRUSTUM_RIGHT] = FP_ONE;
		lightFrustum[Camera.FRUSTUM_TOP] = 0;
		lightFrustum[Camera.FRUSTUM_BOTTOM] = FP_ONE;
		lightFrustum[Camera.FRUSTUM_NEAR] = FP_ONE;
		lightFrustum[Camera.FRUSTUM_FAR] = FP_ONE * 10000;
		this.portedFrustum = new int[Camera.FRUSTUM_SIZE];
		this.shadowMap = new Texture(width, height);
	}
	
	public void update(ShaderBuffer shaderBuffer) {
		this.shaderBuffer = shaderBuffer;
		lights = shaderBuffer.getLights();
		if (shaderBuffer.getDirectionalLightIndex() == -1) {
			shaderBuffer.setDirectionalLightFrustum(portedFrustum);
			shaderBuffer.setDirectionalLightMatrix(lightMatrix);
			shaderBuffer.setDirectionalShadowMap(shadowMap);
		}
		graphicsLibrary.screenportFrustum(lightFrustum, shadowMap.getWidth(), shadowMap.getHeight(), portedFrustum);
		shadowMap.fill(Integer.MAX_VALUE);
	}
	
	public void setup(Camera camera) {
		shaderBuffer.setDirectionalLightIndex(-1);
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
					shaderBuffer.setDirectionalLightIndex(i);
				}
			}
			if (shaderBuffer.getDirectionalLightIndex() == -1)
				return;
			lightTransform = lights.get(shaderBuffer.getDirectionalLightIndex()).getTransform();
			graphicsLibrary.viewMatrix(viewMatrix, lightTransform);
			graphicsLibrary.orthographicMatrix(projectionMatrix, portedFrustum);
			matrixLibrary.multiply(projectionMatrix, viewMatrix, lightMatrix);
		}
	}

	public void vertex(VertexBuffer vertexBuffer) {
		if (shaderBuffer.getDirectionalLightIndex() == -1)
			return;
		int[] location = vertexBuffer.getLocation();
		vectorLibrary.matrixMultiply(location, lightMatrix, location);
		graphicsLibrary.screenportVector(location, portedFrustum, location);
	}
	
	public void geometry(GeometryBuffer geometryBuffer) {
		if (shaderBuffer.getDirectionalLightIndex() == -1)
			return;
		triangle.setLocation0(geometryBuffer.getVertexDataBuffer(0).getLocation());
		triangle.setLocation1(geometryBuffer.getVertexDataBuffer(1).getLocation());
		triangle.setLocation2(geometryBuffer.getVertexDataBuffer(2).getLocation());
		if(graphicsLibrary.shoelace(triangle) > 0)
			graphicsLibrary.drawFlatTriangle(triangle, portedFrustum);
	}

	public void fragment(int[] location) {
		int x = location[VECTOR_X];
		int y = location[VECTOR_Y];
		int z = location[VECTOR_Z] + SHADOW_BIAS;
		if (shadowMap.getPixel(x, y) > z) {
			shadowMap.setPixel(x, y, z);
		}
	}

	public void terminate(ShaderBuffer shaderBuffer) {
		shaderBuffer.setDirectionalLightIndex(-1);
		shaderBuffer.setDirectionalLightFrustum(null);
		shaderBuffer.setDirectionalLightMatrix(null);
		shaderBuffer.setDirectionalShadowMap(null);
	}
}
