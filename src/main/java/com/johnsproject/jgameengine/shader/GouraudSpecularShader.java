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
import com.johnsproject.jgameengine.rasterizer.PerspectiveGouraudRasterizer;

public class GouraudSpecularShader implements Shader {

	private static final int INITIAL_ATTENUATION = MathLibrary.FP_ONE;
	private static final int LINEAR_ATTENUATION = 14;
	private static final int QUADRATIC_ATTENUATION = 7;
	
	private static final int LIGHT_RANGE = MathLibrary.FP_ONE * 150;
	
	private static final byte VECTOR_X = VectorLibrary.VECTOR_X;
	private static final byte VECTOR_Y = VectorLibrary.VECTOR_Y;
	private static final byte VECTOR_Z = VectorLibrary.VECTOR_Z;
	
	private static final int FP_ONE = MathLibrary.FP_ONE;
	
	private final GraphicsLibrary graphicsLibrary;
	private final MathLibrary mathLibrary;
	private final MatrixLibrary matrixLibrary;
	private final VectorLibrary vectorLibrary;
	private final ColorLibrary colorLibrary;

	private final int[] lightDirection;
	private final int[] lightLocation;
	private final int[] viewDirection;
	private final int[] portedFrustum;
	private final int[] lightSpaceLocation;
	
	private final int[] viewMatrix;
	private final int[] projectionMatrix;
	
	private final PerspectiveGouraudRasterizer triangle;
	
	private int color;
	private int modelColor;
	private Texture texture;

	private Camera camera;	
	private List<Light> lights;
	private FrameBuffer frameBuffer;
	private ShaderBuffer shaderBuffer;

	public GouraudSpecularShader() {
		this.graphicsLibrary = new GraphicsLibrary();
		this.mathLibrary = new MathLibrary();
		this.matrixLibrary = new MatrixLibrary();
		this.vectorLibrary = new VectorLibrary();
		this.colorLibrary = new ColorLibrary();
		this.triangle = new PerspectiveGouraudRasterizer(this);
		this.lightDirection = vectorLibrary.generate();
		this.lightLocation = vectorLibrary.generate();
		this.viewDirection = vectorLibrary.generate();
		this.portedFrustum = new int[Camera.FRUSTUM_SIZE];
		this.viewMatrix = matrixLibrary.generate();
		this.projectionMatrix = matrixLibrary.generate();
		this.lightSpaceLocation = vectorLibrary.generate();
	}
	
	public void update(ShaderBuffer shaderBuffer) {
		this.shaderBuffer = shaderBuffer;
		this.lights = shaderBuffer.getLights();
		this.frameBuffer = shaderBuffer.getFrameBuffer();
		
		// debug shadow map
//		Texture shadowMap = shaderBuffer.getDirectionalShadowMap();
//		for (int x = 0; x < shadowMap.getWidth(); x++) {
//			for (int y = 0; y < shadowMap.getHeight(); y++) {
//				int depth = shadowMap.getPixel(x, y);
//				int color = (depth + 100) >> 3;
//				color = colorLibrary.generate(color, color, color);
//				frameBuffer.getColorBuffer().setPixel(x, y, color);
//			}
//		}
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

	public void vertex(VertexBuffer vertexBuffer) {
		ShaderProperties shaderProperties = (ShaderProperties)vertexBuffer.getMaterial().getProperties();
		int[] location = vertexBuffer.getLocation();
		int[] normal = vertexBuffer.getNormal();
		int lightColor = ColorLibrary.BLACK;
		int[] cameraLocation = camera.getTransform().getLocation();	
		vectorLibrary.normalize(normal, normal);
		vectorLibrary.subtract(cameraLocation, location, viewDirection);
		vectorLibrary.normalize(viewDirection, viewDirection);
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
					if(inShadow(location, lightMatrix, lightFrustum, shadowMap)) {
						currentFactor = colorLibrary.multiplyColor(currentFactor, light.getShadowColor());
					}
				}
				break;
			case POINT:
				if (vectorLibrary.averagedDistance(cameraLocation, lightPosition) > LIGHT_RANGE)
					continue;
				vectorLibrary.subtract(lightPosition, location, lightLocation);
				attenuation = getAttenuation(lightLocation);
				vectorLibrary.normalize(lightLocation, lightLocation);
				currentFactor = getLightFactor(normal, lightLocation, viewDirection, shaderProperties);
				currentFactor = mathLibrary.divide(currentFactor, attenuation);
				if ((i == shaderBuffer.getPointLightIndex()) && (currentFactor > 150)) {
					for (int j = 0; j < shaderBuffer.getPointLightMatrices().length; j++) {
						int[] lightMatrix = shaderBuffer.getPointLightMatrices()[j];
						int[] lightFrustum = shaderBuffer.getPointLightFrustum();
						Texture shadowMap = shaderBuffer.getPointShadowMaps()[j];
						if(inShadow(location, lightMatrix, lightFrustum, shadowMap)) {
							currentFactor = colorLibrary.multiplyColor(currentFactor, light.getShadowColor());
						}
					}
				}
				break;
			case SPOT:				
				if (vectorLibrary.averagedDistance(cameraLocation, lightPosition) > LIGHT_RANGE)
					continue;
				vectorLibrary.invert(light.getDirection(), lightDirection);
				vectorLibrary.subtract(lightPosition, location, lightLocation);
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
						if(inShadow(location, lightMatrix, lightFrustum, shadowMap)) {
							currentFactor = colorLibrary.multiplyColor(currentFactor, light.getShadowColor());
						}
					}
				}
				break;
			}
			currentFactor = mathLibrary.multiply(currentFactor, light.getStrength());
			currentFactor = mathLibrary.multiply(currentFactor, 255);
			lightColor = colorLibrary.lerp(lightColor, light.getColor(), currentFactor);
		}
		vertexBuffer.setLightColor(lightColor);
		vectorLibrary.matrixMultiply(location, viewMatrix, location);
		vectorLibrary.matrixMultiply(location, projectionMatrix, location);
		graphicsLibrary.screenportVector(location, portedFrustum, location);
	}

	public void geometry(GeometryBuffer geometryBuffer) {
		ShaderProperties shaderProperties = (ShaderProperties)geometryBuffer.getMaterial().getProperties();
		color = shaderProperties.getDiffuseColor();
		texture = shaderProperties.getTexture();
		VertexBuffer dataBuffer0 = geometryBuffer.getVertexDataBuffer(0);
		VertexBuffer dataBuffer1 = geometryBuffer.getVertexDataBuffer(1);
		VertexBuffer dataBuffer2 = geometryBuffer.getVertexDataBuffer(2);
		triangle.setLocation0(dataBuffer0.getLocation());
		triangle.setLocation1(dataBuffer1.getLocation());
		triangle.setLocation2(dataBuffer2.getLocation());
		triangle.setColor0(dataBuffer0.getLightColor());
		triangle.setColor1(dataBuffer1.getLightColor());
		triangle.setColor2(dataBuffer2.getLightColor());
		if (texture == null) {
			graphicsLibrary.drawGouraudTriangle(triangle, true, 1, portedFrustum);
		} else {
			triangle.setUV0(geometryBuffer.getUV(0), texture);
			triangle.setUV1(geometryBuffer.getUV(1), texture);
			triangle.setUV2(geometryBuffer.getUV(2), texture);
			graphicsLibrary.drawPerspectiveGouraudTriangle(triangle, true, 1, portedFrustum);
		}
	}

	public void fragment(int[] location) {
		int lightColor = triangle.getColor();
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
