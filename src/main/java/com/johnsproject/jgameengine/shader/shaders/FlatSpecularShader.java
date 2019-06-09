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
import com.johnsproject.jgameengine.dto.FrameBuffer;
import com.johnsproject.jgameengine.dto.GeometryDataBuffer;
import com.johnsproject.jgameengine.dto.Light;
import com.johnsproject.jgameengine.dto.Model;
import com.johnsproject.jgameengine.dto.ShaderDataBuffer;
import com.johnsproject.jgameengine.dto.ShaderProperties;
import com.johnsproject.jgameengine.dto.Texture;
import com.johnsproject.jgameengine.dto.VertexDataBuffer;
import com.johnsproject.jgameengine.library.ColorLibrary;
import com.johnsproject.jgameengine.library.GraphicsLibrary;
import com.johnsproject.jgameengine.library.MathLibrary;
import com.johnsproject.jgameengine.library.MatrixLibrary;
import com.johnsproject.jgameengine.library.VectorLibrary;
import com.johnsproject.jgameengine.shader.PerspectiveFlatTriangle;
import com.johnsproject.jgameengine.shader.Shader;
import com.johnsproject.jgameengine.shader.databuffers.ForwardDataBuffer;

public class FlatSpecularShader implements Shader {
	
	private static final int INITIAL_ATTENUATION = MathLibrary.FP_ONE;
	private static final int LINEAR_ATTENUATION = (MathLibrary.FP_ONE * 14) / 10;
	private static final int QUADRATIC_ATTENUATION = (MathLibrary.FP_ONE * 7) / 10;
	
	private static final int LIGHT_RANGE = MathLibrary.FP_ONE * 1000;
	
	private static final byte VECTOR_X = VectorLibrary.VECTOR_X;
	private static final byte VECTOR_Y = VectorLibrary.VECTOR_Y;
	private static final byte VECTOR_Z = VectorLibrary.VECTOR_Z;
	
	private static final byte FP_BITS = MathLibrary.FP_BITS;
	private static final int FP_ONE = MathLibrary.FP_ONE;
	
	private final GraphicsLibrary graphicsLibrary;
	private final MathLibrary mathLibrary;
	private final MatrixLibrary matrixLibrary;
	private final VectorLibrary vectorLibrary;
	private final ColorLibrary colorLibrary;

	private final int[] normalizedNormal;
	private final int[] lightLocation;
	private final int[] lightDirection;
	private final int[] viewDirection;
	private final int[] faceLocation;
	private final int[] portedFrustum;
	private final int[][] vertexLocations;
	private final int[] lightSpaceLocation;
	
	private final int[][] modelMatrix;
	private final int[][] normalMatrix;
	private final int[][] viewMatrix;
	private final int[][] projectionMatrix;
	
	private final PerspectiveFlatTriangle triangle;
	
	private int color;
	private int lightColor;
	private int modelColor;
	private Texture texture;
	
	private Camera camera;	
	private List<Light> lights;
	private FrameBuffer frameBuffer;
	private ForwardDataBuffer shaderData;
	private ShaderProperties shaderProperties;
	
	public FlatSpecularShader() {
		this.graphicsLibrary = new GraphicsLibrary();
		this.mathLibrary = new MathLibrary();
		this.matrixLibrary = new MatrixLibrary();
		this.vectorLibrary = new VectorLibrary();
		this.colorLibrary = new ColorLibrary();
		this.triangle = new PerspectiveFlatTriangle(this);
		this.normalizedNormal = vectorLibrary.generate();
		this.lightLocation = vectorLibrary.generate();
		this.lightDirection = vectorLibrary.generate();
		this.viewDirection = vectorLibrary.generate();
		this.faceLocation = vectorLibrary.generate();
		this.vertexLocations = new int[3][4];
		this.modelMatrix = matrixLibrary.generate();
		this.normalMatrix = matrixLibrary.generate();
		this.viewMatrix = matrixLibrary.generate();
		this.projectionMatrix = matrixLibrary.generate();
		this.portedFrustum = new int[Camera.FRUSTUM_SIZE];
		this.lightSpaceLocation = vectorLibrary.generate();
	}
	
	public void update(ShaderDataBuffer shaderDataBuffer) {
		this.shaderData = (ForwardDataBuffer)shaderDataBuffer;
		this.lights = shaderData.getLights();
		this.frameBuffer = shaderData.getFrameBuffer();
		frameBuffer.getColorBuffer().fill(0);
		frameBuffer.getDepthBuffer().fill(Integer.MAX_VALUE);
	}
	
	public void setup(Camera camera) {
		this.camera = camera;
		graphicsLibrary.viewMatrix(viewMatrix, camera.getTransform());
		graphicsLibrary.screenportFrustum(camera.getFrustum(), frameBuffer.getWidth(), frameBuffer.getHeight(), portedFrustum);
		switch (camera.getType()) {
		case ORTHOGRAPHIC:
			graphicsLibrary.orthographicMatrix(projectionMatrix, portedFrustum);
			break;

		case PERSPECTIVE:
			graphicsLibrary.perspectiveMatrix(projectionMatrix, portedFrustum);
			break;
		}
		matrixLibrary.multiply(projectionMatrix, viewMatrix, projectionMatrix);
	}
	
	public void setup(Model model) {
		graphicsLibrary.modelMatrix(modelMatrix, model.getTransform());
		graphicsLibrary.normalMatrix(normalMatrix, model.getTransform());
	}

	public void vertex(VertexDataBuffer dataBuffer) {
		int[] location = dataBuffer.getLocation();
		vectorLibrary.multiply(location, modelMatrix, location);
	}

	public void geometry(GeometryDataBuffer dataBuffer) {
		this.shaderProperties = (ShaderProperties)dataBuffer.getMaterial().getProperties();
		int[] normal = dataBuffer.getNormal();
		int[] location1 = dataBuffer.getVertexDataBuffer(0).getLocation();
		int[] location2 = dataBuffer.getVertexDataBuffer(1).getLocation();
		int[] location3 = dataBuffer.getVertexDataBuffer(2).getLocation();
		vectorLibrary.add(location1, location2, faceLocation);
		vectorLibrary.add(faceLocation, location3, faceLocation);
		vectorLibrary.divide(faceLocation, 3 << FP_BITS, faceLocation);	
		vectorLibrary.multiply(normal, normalMatrix, normal);
		lightColor = ColorLibrary.BLACK;		
		int[] cameraLocation = camera.getTransform().getLocation();		
		vectorLibrary.subtract(cameraLocation, faceLocation, viewDirection);
		// normalize values
		vectorLibrary.normalize(normal, normalizedNormal);
		vectorLibrary.normalize(viewDirection, viewDirection);
		boolean inShadow = false;
		for (int i = 0; i < lights.size(); i++) {
			Light light = lights.get(i);
			int currentFactor = 0;
			int attenuation = 0;
			int[] lightPosition = light.getTransform().getLocation();
			switch (light.getType()) {
			case DIRECTIONAL:
				if (vectorLibrary.distance(cameraLocation, lightPosition) > LIGHT_RANGE)
					continue;
				vectorLibrary.invert(light.getDirection(), lightDirection);
				currentFactor = getLightFactor(normalizedNormal, lightDirection, viewDirection, shaderProperties);
				if (i == shaderData.getDirectionalLightIndex()) {
					vectorLibrary.multiply(faceLocation, shaderData.getDirectionalLightMatrix(), lightSpaceLocation);
					graphicsLibrary.screenportVector(lightSpaceLocation, shaderData.getDirectionalLightFrustum(), lightSpaceLocation);
					inShadow = inShadow(lightSpaceLocation, shaderData.getDirectionalShadowMap());
				}
				break;
			case POINT:
				if (vectorLibrary.distance(cameraLocation, lightPosition) > LIGHT_RANGE)
					continue;
				vectorLibrary.subtract(lightPosition, faceLocation, lightLocation);
				// attenuation
				attenuation = getAttenuation(lightLocation);
				// other light values
				vectorLibrary.normalize(lightLocation, lightLocation);
				currentFactor = getLightFactor(normalizedNormal, lightLocation, viewDirection, shaderProperties);
				currentFactor = mathLibrary.divide(currentFactor, attenuation);
				if ((i == shaderData.getPointLightIndex()) && (currentFactor > 100)) {
					for (int j = 0; j < shaderData.getPointLightMatrices().length; j++) {
						vectorLibrary.multiply(faceLocation, shaderData.getPointLightMatrices()[j], lightSpaceLocation);
						graphicsLibrary.screenportVector(lightSpaceLocation, shaderData.getPointLightFrustum(), lightSpaceLocation);
						inShadow = inShadow(lightSpaceLocation, shaderData.getPointShadowMaps()[j]);
					}
				}
				break;
			case SPOT:				
				if (vectorLibrary.distance(cameraLocation, lightPosition) > LIGHT_RANGE)
					continue;
				vectorLibrary.invert(light.getDirection(), lightDirection);
				vectorLibrary.subtract(lightPosition, faceLocation, lightLocation);
				// attenuation
				attenuation = getAttenuation(lightLocation);
				vectorLibrary.normalize(lightLocation, lightLocation);
				int theta = vectorLibrary.dotProduct(lightLocation, lightDirection);
				int phi = mathLibrary.cos(light.getSpotSize() >> 1);
				if(theta > phi) {
					int intensity = -mathLibrary.divide(phi - theta, light.getSpotSoftness() + 1);
					intensity = mathLibrary.clamp(intensity, 1, FP_ONE);
					currentFactor = getLightFactor(normalizedNormal, lightDirection, viewDirection, shaderProperties);
					currentFactor = mathLibrary.multiply(currentFactor, intensity * 2);
					currentFactor = mathLibrary.divide(currentFactor, attenuation);
					if ((i == shaderData.getSpotLightIndex()) && (currentFactor > 10)) {
						vectorLibrary.multiply(faceLocation, shaderData.getSpotLightMatrix(), lightSpaceLocation);
						graphicsLibrary.screenportVector(lightSpaceLocation, shaderData.getSpotLightFrustum(), lightSpaceLocation);
						inShadow = inShadow(lightSpaceLocation, shaderData.getSpotShadowMap());
					}
				}
				break;
			}
			currentFactor = mathLibrary.multiply(currentFactor, light.getStrength());
			currentFactor = mathLibrary.multiply(currentFactor, 255);
			if(inShadow) {
				lightColor = colorLibrary.lerp(lightColor, light.getShadowColor(), 128);
			} else {
				lightColor = colorLibrary.lerp(lightColor, light.getColor(), currentFactor);
			}
		}
		color = shaderProperties.getDiffuseColor();
		for (int i = 0; i < dataBuffer.getVertexDataBuffers().length; i++) {
			int[] vertexLocation = dataBuffer.getVertexDataBuffer(i).getLocation();
			vectorLibrary.copy(vertexLocations[i], vertexLocation);
			vectorLibrary.multiply(vertexLocation, projectionMatrix, vertexLocation);
			graphicsLibrary.screenportVector(vertexLocation, portedFrustum, vertexLocation);
		}
		texture = shaderProperties.getTexture();
		if (texture == null) {
			graphicsLibrary.drawFlatTriangle(triangle, dataBuffer, portedFrustum);
		} else {
			graphicsLibrary.drawPerspectiveFlatTriangle(triangle, dataBuffer, texture, portedFrustum);
		}
		for (int i = 0; i < dataBuffer.getVertexDataBuffers().length; i++) {
			int[] vertexLocation = dataBuffer.getVertexDataBuffer(i).getLocation();
			vectorLibrary.copy(vertexLocation, vertexLocations[i]);
		}
	}

	public void fragment(int[] location) {
		if (texture != null) {
			int u = triangle.getU()[3];
			int v = triangle.getV()[3];
			int texel = texture.getPixel(u, v);
			if (colorLibrary.getAlpha(texel) == 0) // discard pixel if alpha = 0
				return;
			modelColor = colorLibrary.multiplyColor(texel, lightColor);
		} else {
			modelColor = colorLibrary.multiplyColor(color, lightColor);
		}
		Texture colorBuffer = frameBuffer.getColorBuffer();
		Texture depthBuffer = frameBuffer.getDepthBuffer();
		int x = location[VECTOR_X];
		int y = location[VECTOR_Y];
		int z = location[VECTOR_Z];
		if (depthBuffer.getPixel(x, y) > z) {
			depthBuffer.setPixel(x, y, z);
			colorBuffer.setPixel(x, y, modelColor);
		}
	}

	private int getLightFactor(int[] normal, int[] lightDirection, int[] viewDirection, ShaderProperties properties) {
		// diffuse
		int dotProduct = vectorLibrary.dotProduct(normal, lightDirection);
		int diffuseFactor = Math.max(dotProduct, 0);
		diffuseFactor = mathLibrary.multiply(diffuseFactor, properties.getDiffuseIntensity());
		// specular
		vectorLibrary.invert(lightDirection, lightDirection);
		vectorLibrary.reflect(lightDirection, normal, lightDirection);
		dotProduct = vectorLibrary.dotProduct(viewDirection, lightDirection);
		int specularFactor = Math.max(dotProduct, 0);
		specularFactor = mathLibrary.pow(specularFactor, properties.getShininess() >> FP_BITS);
		specularFactor = mathLibrary.multiply(specularFactor, properties.getSpecularIntensity());
		// putting it all together...
		return diffuseFactor + specularFactor;
	}
	
	private int getAttenuation(int[] lightLocation) {
		// attenuation
		int distance = vectorLibrary.magnitude(lightLocation);
		int attenuation = INITIAL_ATTENUATION;
		attenuation += mathLibrary.multiply(distance, LINEAR_ATTENUATION);
		attenuation += mathLibrary.multiply(mathLibrary.multiply(distance, distance), QUADRATIC_ATTENUATION);
		return (attenuation >> FP_BITS) + 1;
	}
	
	private boolean inShadow(int[] lightSpaceLocation, Texture shadowMap) {
		int x = lightSpaceLocation[VECTOR_X];
		int y = lightSpaceLocation[VECTOR_Y];
		int depth = shadowMap.getPixel(x, y);
		return depth < lightSpaceLocation[VECTOR_Z];
	}

	public void terminate(ShaderDataBuffer shaderDataBuffer) {
		
	}
}
