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

import com.johnsproject.jgameengine.library.ColorLibrary;
import com.johnsproject.jgameengine.library.GraphicsLibrary;
import com.johnsproject.jgameengine.library.MathLibrary;
import com.johnsproject.jgameengine.library.MatrixLibrary;
import com.johnsproject.jgameengine.library.VectorLibrary;
import com.johnsproject.jgameengine.model.Camera;
import com.johnsproject.jgameengine.model.FrameBuffer;
import com.johnsproject.jgameengine.model.GeometryBuffer;
import com.johnsproject.jgameengine.model.Light;
import com.johnsproject.jgameengine.model.ShaderBuffer;
import com.johnsproject.jgameengine.model.ShaderProperties;
import com.johnsproject.jgameengine.model.Texture;
import com.johnsproject.jgameengine.model.VertexBuffer;
import com.johnsproject.jgameengine.shader.PerspectiveFlatTriangle;
import com.johnsproject.jgameengine.shader.Shader;

public class FlatSpecularShader implements Shader {
	
	private static final int INITIAL_ATTENUATION = MathLibrary.FP_ONE;
	private static final int LINEAR_ATTENUATION = 14;
	private static final int QUADRATIC_ATTENUATION = 7;
	
	private static final int LIGHT_RANGE = MathLibrary.FP_ONE * 150;
	
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

	private final int[] lightLocation;
	private final int[] lightDirection;
	private final int[] viewDirection;
	private final int[] faceLocation;
	private final int[] portedFrustum;
	private final int[][] vertexLocations;
	private final int[] lightSpaceLocation;
	
	private final int[] viewMatrix;
	private final int[] projectionMatrix;
	
	private final PerspectiveFlatTriangle triangle;
	
	private int color;
	private int lightColor;
	private int modelColor;
	private Texture texture;
	
	private Camera camera;	
	private List<Light> lights;
	private FrameBuffer frameBuffer;
	private ShaderBuffer shaderBuffer;
	private ShaderProperties shaderProperties;
	
	public FlatSpecularShader() {
		this.graphicsLibrary = new GraphicsLibrary();
		this.mathLibrary = new MathLibrary();
		this.matrixLibrary = new MatrixLibrary();
		this.vectorLibrary = new VectorLibrary();
		this.colorLibrary = new ColorLibrary();
		this.triangle = new PerspectiveFlatTriangle(this);
		this.lightLocation = vectorLibrary.generate();
		this.lightDirection = vectorLibrary.generate();
		this.viewDirection = vectorLibrary.generate();
		this.faceLocation = vectorLibrary.generate();
		this.vertexLocations = new int[3][4];
		this.viewMatrix = matrixLibrary.generate();
		this.projectionMatrix = matrixLibrary.generate();
		this.portedFrustum = new int[Camera.FRUSTUM_SIZE];
		this.lightSpaceLocation = vectorLibrary.generate();
	}
	
	public void update(ShaderBuffer shaderBuffer) {
		this.shaderBuffer = shaderBuffer;
		this.lights = shaderBuffer.getLights();
		this.frameBuffer = shaderBuffer.getFrameBuffer();
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
	}

	public void vertex(VertexBuffer vertexBuffer) {	}

	public void geometry(GeometryBuffer geometryBuffer) {
		this.shaderProperties = (ShaderProperties)geometryBuffer.getMaterial().getProperties();
		int[] normal = geometryBuffer.getNormal();
		int[] location1 = geometryBuffer.getVertexDataBuffer(0).getLocation();
		int[] location2 = geometryBuffer.getVertexDataBuffer(1).getLocation();
		int[] location3 = geometryBuffer.getVertexDataBuffer(2).getLocation();
		vectorLibrary.add(location1, location2, faceLocation);
		vectorLibrary.add(faceLocation, location3, faceLocation);
		vectorLibrary.divide(faceLocation, 3 << FP_BITS, faceLocation);	
		lightColor = ColorLibrary.BLACK;		
		int[] cameraLocation = camera.getTransform().getLocation();		
		vectorLibrary.normalize(normal, normal);
		vectorLibrary.subtract(cameraLocation, faceLocation, viewDirection);
		vectorLibrary.normalize(viewDirection, viewDirection);
		boolean inShadow = false;
		for (int i = 0; i < lights.size(); i++) {
			Light light = lights.get(i);
			int currentFactor = 0;
			int attenuation = 0;
			int[] lightPosition = light.getTransform().getLocation();
			switch (light.getType()) {
			case DIRECTIONAL:
				vectorLibrary.invert(light.getDirection(), lightDirection);
				currentFactor = getLightFactor(normal, lightDirection, viewDirection, shaderProperties);
				if (i == shaderBuffer.getDirectionalLightIndex()) {
					int[] lightMatrix = shaderBuffer.getDirectionalLightMatrix();
					int[] lightFrustum = shaderBuffer.getDirectionalLightFrustum();
					Texture shadowMap = shaderBuffer.getDirectionalShadowMap();
					if(inShadow(faceLocation, lightMatrix, lightFrustum, shadowMap)) {
						currentFactor = colorLibrary.multiplyColor(currentFactor, light.getShadowColor());
					}
				}
				break;
			case POINT:
				if (vectorLibrary.averagedDistance(cameraLocation, lightPosition) > LIGHT_RANGE)
					continue;
				vectorLibrary.subtract(lightPosition, faceLocation, lightLocation);
				// attenuation
				attenuation = getAttenuation(lightLocation);
				// other light values
				vectorLibrary.normalize(lightLocation, lightLocation);
				currentFactor = getLightFactor(normal, lightLocation, viewDirection, shaderProperties);
				currentFactor = mathLibrary.divide(currentFactor, attenuation);
				if ((i == shaderBuffer.getPointLightIndex()) && (currentFactor > 150)) {
					for (int j = 0; j < shaderBuffer.getPointLightMatrices().length; j++) {
						int[] lightMatrix = shaderBuffer.getPointLightMatrices()[j];
						int[] lightFrustum = shaderBuffer.getPointLightFrustum();
						Texture shadowMap = shaderBuffer.getPointShadowMaps()[j];
						if(inShadow(faceLocation, lightMatrix, lightFrustum, shadowMap)) {
							currentFactor = colorLibrary.multiplyColor(currentFactor, light.getShadowColor());
						}
					}
				}
				break;
			case SPOT:				
				if (vectorLibrary.averagedDistance(cameraLocation, lightPosition) > LIGHT_RANGE)
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
					currentFactor = getLightFactor(normal, lightDirection, viewDirection, shaderProperties);
					currentFactor = mathLibrary.multiply(currentFactor, intensity * 2);
					currentFactor = mathLibrary.divide(currentFactor, attenuation);
					if ((i == shaderBuffer.getSpotLightIndex()) && (currentFactor > 10)) {
						int[] lightMatrix = shaderBuffer.getSpotLightMatrix();
						int[] lightFrustum = shaderBuffer.getSpotLightFrustum();
						Texture shadowMap = shaderBuffer.getSpotShadowMap();
						if(inShadow(faceLocation, lightMatrix, lightFrustum, shadowMap)) {
							currentFactor = colorLibrary.multiplyColor(currentFactor, light.getShadowColor());
						}
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
		for (int i = 0; i < geometryBuffer.getVertexDataBuffers().length; i++) {
			int[] vertexLocation = geometryBuffer.getVertexDataBuffer(i).getLocation();
			vectorLibrary.copy(vertexLocations[i], vertexLocation);
			vectorLibrary.matrixMultiply(vertexLocation, viewMatrix, vertexLocation);
			vectorLibrary.matrixMultiply(vertexLocation, projectionMatrix, vertexLocation);
			graphicsLibrary.screenportVector(vertexLocation, portedFrustum, vertexLocation);
		}
		texture = shaderProperties.getTexture();
		triangle.setLocation0(location1);
		triangle.setLocation1(location2);
		triangle.setLocation2(location3);
		if(graphicsLibrary.shoelace(triangle) > 0) {
			if (texture == null) {
				graphicsLibrary.drawFlatTriangle(triangle, portedFrustum);
			} else {
				triangle.setUV0(geometryBuffer.getUV(0), texture);
				triangle.setUV1(geometryBuffer.getUV(1), texture);
				triangle.setUV2(geometryBuffer.getUV(2), texture);
				graphicsLibrary.drawPerspectiveFlatTriangle(triangle, portedFrustum);
			}
		}
		for (int i = 0; i < geometryBuffer.getVertexDataBuffers().length; i++) {
			int[] vertexLocation = geometryBuffer.getVertexDataBuffer(i).getLocation();
			vectorLibrary.copy(vertexLocation, vertexLocations[i]);
		}
	}

	public void fragment(int[] location) {
		if (texture != null) {
			int[] uv = triangle.getUV();
			int texel = texture.getPixel(uv[VECTOR_X], uv[VECTOR_Y]);
			if (colorLibrary.getAlpha(texel) == 0) // discard pixel if alpha = 0
				return;
			color = texel;
		}
		modelColor = colorLibrary.multiplyColor(color, lightColor);
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
		specularFactor = mathLibrary.pow(specularFactor, properties.getShininess());
		specularFactor = mathLibrary.multiply(specularFactor, properties.getSpecularIntensity());
		// putting it all together...
		return diffuseFactor + specularFactor;
	}
	
	private int getAttenuation(int[] lightLocation) {
		// attenuation
		int distance = vectorLibrary.length(lightLocation);
		int attenuation = INITIAL_ATTENUATION;
		attenuation += mathLibrary.multiply(distance, LINEAR_ATTENUATION);
		attenuation += mathLibrary.multiply(mathLibrary.multiply(distance, distance), QUADRATIC_ATTENUATION);
		return attenuation + 1;
	}
	
	private boolean inShadow(int[] location, int[] lightMatrix, int[] lightFrustum, Texture shadowMap) {
		vectorLibrary.matrixMultiply(location, lightMatrix, lightSpaceLocation);
		graphicsLibrary.screenportVector(lightSpaceLocation, lightFrustum, lightSpaceLocation);
		int x = lightSpaceLocation[VECTOR_X];
		int y = lightSpaceLocation[VECTOR_Y];
		int depth = shadowMap.getPixel(x, y);
		return depth < lightSpaceLocation[VECTOR_Z];
	}

	public void terminate(ShaderBuffer shaderBuffer) {
		
	}
}
