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
import com.johnsproject.jgameengine.model.GeometryBuffer;
import com.johnsproject.jgameengine.model.Light;
import com.johnsproject.jgameengine.model.LightType;
import com.johnsproject.jgameengine.model.ShaderBuffer;
import com.johnsproject.jgameengine.model.Texture;
import com.johnsproject.jgameengine.model.Transform;
import com.johnsproject.jgameengine.model.VertexBuffer;
import com.johnsproject.jgameengine.rasterizer.FlatRasterizer;

public class PointLightShadowShader implements Shader {

	private static final int LIGHT_RANGE = MathLibrary.FP_ONE * 300;
	
	private static final byte VECTOR_X = VectorLibrary.VECTOR_X;
	private static final byte VECTOR_Y = VectorLibrary.VECTOR_Y;
	private static final byte VECTOR_Z = VectorLibrary.VECTOR_Z;

	private static final int FP_ONE = MathLibrary.FP_ONE;
	
	private static final short SHADOW_BIAS = 500;
	
	private final GraphicsLibrary graphicsLibrary;
	private final MatrixLibrary matrixLibrary;
	private final VectorLibrary vectorLibrary;

	private final FlatRasterizer rasterizer;
	
	private int[] viewMatrix;
	private int[] projectionMatrix;
	private final int[][] lightMatrices;
	
	private int[] lightFrustum;
	private final int[] portedFrustum;

	private final int[] location0Cache;
	private final int[] location1Cache;
	private final int[] location2Cache;
	
	private final Texture[] shadowMaps;
	private Texture currentShadowMap;
	private Transform lightTransform;

	private List<Light> lights;
	private ShaderBuffer shaderBuffer;

	public PointLightShadowShader() {
		this.graphicsLibrary = new GraphicsLibrary();
		this.matrixLibrary = new MatrixLibrary();
		this.vectorLibrary = new VectorLibrary();
		this.rasterizer = new FlatRasterizer(this);

		this.viewMatrix = matrixLibrary.generate();
		this.projectionMatrix = matrixLibrary.generate();
		this.lightMatrices = new int[6][16];
		
		this.location0Cache = vectorLibrary.generate();
		this.location1Cache = vectorLibrary.generate();
		this.location2Cache = vectorLibrary.generate();
		
		this.lightFrustum = new int[Camera.FRUSTUM_SIZE];
		lightFrustum[Camera.FRUSTUM_LEFT] = 0;
		lightFrustum[Camera.FRUSTUM_RIGHT] = FP_ONE;
		lightFrustum[Camera.FRUSTUM_TOP] = 0;
		lightFrustum[Camera.FRUSTUM_BOTTOM] = FP_ONE;
		lightFrustum[Camera.FRUSTUM_NEAR] = FP_ONE / 50;
		lightFrustum[Camera.FRUSTUM_FAR] = FP_ONE * 10000;
		this.portedFrustum = new int[Camera.FRUSTUM_SIZE];
		this.shadowMaps = new Texture[6];
		for (int i = 0; i < shadowMaps.length; i++) {
			shadowMaps[i] = new Texture(64, 64);
		}
	}
	
	public void update(ShaderBuffer shaderBuffer) {
		this.shaderBuffer = shaderBuffer;
		this.lights = shaderBuffer.getLights();
		if (shaderBuffer.getPointLightIndex() == -1) {
			shaderBuffer.setPointLightFrustum(portedFrustum);
			shaderBuffer.setPointLightMatrices(lightMatrices);
			shaderBuffer.setPointShadowMaps(shadowMaps);
		}
		graphicsLibrary.screenportFrustum(lightFrustum, shadowMaps[0].getWidth(), shadowMaps[0].getHeight(), portedFrustum);
		for (int i = 0; i < shadowMaps.length; i++) {
			shadowMaps[i].fill(Integer.MAX_VALUE);
		}
	}

	public void setup(Camera camera) {
		shaderBuffer.setPointLightIndex(-1);
		if(lights.size() > 0) {
			int[] cameraLocation = camera.getTransform().getLocation();		
			int distance = Integer.MAX_VALUE;
			for (int i = 0; i < lights.size(); i++) {
				Light light = lights.get(i);
				lightTransform = light.getTransform();
				int[] lightPosition = lightTransform.getLocation();
				int dist = vectorLibrary.averagedDistance(cameraLocation, lightPosition);
				if ((light.getType() == LightType.POINT) & (dist < distance) & (dist < LIGHT_RANGE)) {
					distance = dist;
					shaderBuffer.setPointLightIndex(i);
				}
			}
			if (shaderBuffer.getPointLightIndex() == -1)
				return;		
			lightTransform = lights.get(shaderBuffer.getPointLightIndex()).getTransform();
			int[] lightMatrix = lightMatrices[0];
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
	
	public void vertex(VertexBuffer vertexBuffer) {
	}

	public void geometry(GeometryBuffer geometryBuffer) {
		if (shaderBuffer.getPointLightIndex() == -1)
			return;	
		backup(geometryBuffer);
		for (int i = 0; i < lightMatrices.length; i++) {
			currentShadowMap = shadowMaps[i];
			for (int j = 0; j < geometryBuffer.getVertexDataBuffers().length; j++) {
				int[] vertexLocation = geometryBuffer.getVertexDataBuffer(j).getLocation();
				vectorLibrary.matrixMultiply(vertexLocation, lightMatrices[i], vertexLocation);
				graphicsLibrary.screenportVector(vertexLocation, portedFrustum, vertexLocation);
			}
			rasterizer.setLocation0(geometryBuffer.getVertexDataBuffer(0).getLocation());
			rasterizer.setLocation1(geometryBuffer.getVertexDataBuffer(1).getLocation());
			rasterizer.setLocation2(geometryBuffer.getVertexDataBuffer(2).getLocation());
			graphicsLibrary.drawFlatTriangle(rasterizer, true, 1, portedFrustum);
			restore(geometryBuffer);
		}
	}

	public void fragment(int[] location) {
		int x = location[VECTOR_X];
		int y = location[VECTOR_Y];
		int z = location[VECTOR_Z] + SHADOW_BIAS;
		if (currentShadowMap.getPixel(x, y) > z) {
			currentShadowMap.setPixel(x, y, z);
		}
	}
	
	private void backup(GeometryBuffer geometryBuffer) {
		vectorLibrary.copy(location0Cache, geometryBuffer.getVertexDataBuffer(0).getLocation());
		vectorLibrary.copy(location1Cache, geometryBuffer.getVertexDataBuffer(1).getLocation());
		vectorLibrary.copy(location2Cache, geometryBuffer.getVertexDataBuffer(2).getLocation());
	}
	
	private void restore(GeometryBuffer geometryBuffer) {
		vectorLibrary.copy(geometryBuffer.getVertexDataBuffer(0).getLocation(), location0Cache);
		vectorLibrary.copy(geometryBuffer.getVertexDataBuffer(1).getLocation(), location1Cache);
		vectorLibrary.copy(geometryBuffer.getVertexDataBuffer(2).getLocation(), location2Cache);
	}

	public void terminate(ShaderBuffer shaderBuffer) {
		shaderBuffer.setPointLightIndex(-1);
		shaderBuffer.setPointLightFrustum(null);
		shaderBuffer.setPointLightMatrices(null);
	}	
}