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

import com.johnsproject.jgameengine.library.ColorLibrary;
import com.johnsproject.jgameengine.library.MathLibrary;
import com.johnsproject.jgameengine.library.TransformationLibrary;
import com.johnsproject.jgameengine.library.VectorLibrary;
import com.johnsproject.jgameengine.model.Light;
import com.johnsproject.jgameengine.model.Texture;
import com.johnsproject.jgameengine.rasterizer.PerspectivePhongRasterizer;

import static com.johnsproject.jgameengine.library.VectorLibrary.*;
import static com.johnsproject.jgameengine.library.MathLibrary.*;

public class PhongSpecularShader  implements Shader {

	private static final int INITIAL_ATTENUATION = FP_ONE;
	private static final int LINEAR_ATTENUATION = MathLibrary.generate(0.045);
	private static final int QUADRATIC_ATTENUATION = MathLibrary.generate(0.0075);

	private SpecularProperties shaderProperties;
	private ForwardShaderBuffer shaderBuffer;
	private final PerspectivePhongRasterizer rasterizer;
	
	private final int[] lightDirection;
	private final int[] lightLocation;
	private final int[] viewDirection;
	private final int[] lightSpaceLocation;
	
	private int color;
	private int modelColor;
	private Texture texture;

	public PhongSpecularShader() {
		this.rasterizer = new PerspectivePhongRasterizer(this);
		this.shaderProperties = new SpecularProperties();
		this.lightDirection = VectorLibrary.generate();
		this.lightLocation = VectorLibrary.generate();
		this.viewDirection = VectorLibrary.generate();
		this.lightSpaceLocation = VectorLibrary.generate();
	}

	public void vertex(VertexBuffer vertexBuffer) {
		int[] location = vertexBuffer.getLocation();
		int[] normal = vertexBuffer.getWorldNormal();
		VectorLibrary.normalize(normal);
		VectorLibrary.copy(location, vertexBuffer.getWorldLocation());
		VectorLibrary.matrixMultiply(location, shaderBuffer.getViewMatrix());
		VectorLibrary.matrixMultiply(location, shaderBuffer.getProjectionMatrix());
		TransformationLibrary.screenportVector(location, shaderBuffer.getPortedFrustum(), location);
	}

	public void geometry(GeometryBuffer geometryBuffer) {
		color = shaderProperties.getDiffuseColor();
		texture = shaderProperties.getTexture();
		if (texture == null) {
			rasterizer.draw(geometryBuffer);
		} else {
			rasterizer.perspectiveDraw(geometryBuffer, texture);
		}
	}

	public void fragment(FragmentBuffer fragmentBuffer) {
		int x = fragmentBuffer.getLocation()[VECTOR_X];
		int y = fragmentBuffer.getLocation()[VECTOR_Y];
		int z = fragmentBuffer.getLocation()[VECTOR_Z];
		Texture colorBuffer = shaderBuffer.getFrameBuffer().getColorBuffer();
		Texture depthBuffer = shaderBuffer.getFrameBuffer().getDepthBuffer();
		if (depthBuffer.getPixel(x, y) > z) {
			int[] worldLocation = fragmentBuffer.getWorldLocation();
			int[] normal = fragmentBuffer.getWorldNormal();
			int lightColor = ColorLibrary.BLACK;
			int[] cameraLocation = shaderBuffer.getCamera().getTransform().getLocation();
			VectorLibrary.copy(viewDirection, cameraLocation);
			VectorLibrary.subtract(viewDirection, worldLocation);
			VectorLibrary.normalize(viewDirection);
			int lightIndex = 0;
			for (Light light: shaderBuffer.getLights()) {
				if(light.isCulled())
					continue;
				int currentFactor = 0;
				int attenuation = 0;
				int[] lightPosition = light.getTransform().getLocation();
				switch (light.getType()) {
				case DIRECTIONAL:
					VectorLibrary.copy(lightDirection, light.getDirection());
					VectorLibrary.invert(lightDirection);
					currentFactor = getLightFactor(normal, lightDirection, viewDirection);
					if (lightIndex == shaderBuffer.getDirectionalLightIndex()) {
						int[] lightMatrix = shaderBuffer.getDirectionalLightMatrix();
						int[] lightFrustum = shaderBuffer.getDirectionalLightFrustum();
						Texture shadowMap = shaderBuffer.getDirectionalShadowMap();
						if(inShadow(worldLocation, lightMatrix, lightFrustum, shadowMap)) {
							currentFactor = ColorLibrary.multiplyColor(currentFactor, light.getShadowColor());
						}
					}
					break;
				case POINT:
					VectorLibrary.copy(lightLocation, lightPosition);
					VectorLibrary.subtract(lightLocation, worldLocation);
					attenuation = getAttenuation(lightLocation);
					VectorLibrary.normalize(lightLocation);
					currentFactor = getLightFactor(normal, lightLocation, viewDirection);
					currentFactor = MathLibrary.divide(currentFactor, attenuation);
					if ((lightIndex == shaderBuffer.getPointLightIndex()) && (currentFactor > 150)) {
						int[] lightFrustum = shaderBuffer.getPointLightFrustum();
						for (int j = 0; j < shaderBuffer.getPointLightMatrices().length; j++) {
							int[] lightMatrix = shaderBuffer.getPointLightMatrices()[j];
							Texture shadowMap = shaderBuffer.getPointShadowMaps()[j];
							if(inShadow(worldLocation, lightMatrix, lightFrustum, shadowMap)) {
								currentFactor = ColorLibrary.multiplyColor(currentFactor, light.getShadowColor());
							}
						}
					}
					break;
				case SPOT:			
					VectorLibrary.copy(lightDirection, light.getDirection());
					VectorLibrary.invert(lightDirection);
					VectorLibrary.copy(lightLocation, lightPosition);
					VectorLibrary.subtract(lightLocation, worldLocation);
					attenuation = getAttenuation(lightLocation);
					VectorLibrary.normalize(lightLocation);
					int theta = VectorLibrary.dotProduct(lightLocation, lightDirection);
					int phi = MathLibrary.cos(light.getSpotSize() >> 1);
					if(theta > phi) {
						int intensity = -MathLibrary.divide(phi - theta, light.getSpotSoftness() + 1);
						intensity = MathLibrary.clamp(intensity, 1, FP_ONE);
						currentFactor = getLightFactor(normal, lightDirection, viewDirection);
						currentFactor = MathLibrary.multiply(currentFactor, intensity * 2);
						currentFactor = MathLibrary.divide(currentFactor, attenuation);
						if ((lightIndex == shaderBuffer.getSpotLightIndex()) && (currentFactor > 10)) {
							int[] lightMatrix = shaderBuffer.getSpotLightMatrix();
							int[] lightFrustum = shaderBuffer.getSpotLightFrustum();
							Texture shadowMap = shaderBuffer.getSpotShadowMap();
							if(inShadow(worldLocation, lightMatrix, lightFrustum, shadowMap)) {
								currentFactor = ColorLibrary.multiplyColor(currentFactor, light.getShadowColor());
							}
						}
					}
					break;
				}
				currentFactor = MathLibrary.multiply(currentFactor, light.getStrength());
				currentFactor = MathLibrary.multiply(currentFactor, 255);
				lightColor = ColorLibrary.lerp(lightColor, light.getColor(), currentFactor);
				lightIndex++;
			}
			if (texture != null) {
				int[] uv = fragmentBuffer.getUV();
				int texel = texture.getPixel(uv[VECTOR_X], uv[VECTOR_Y]);
				if (ColorLibrary.getAlpha(texel) == 0) // discard pixel if alpha = 0
					return;
				color = texel;
			}
			modelColor = ColorLibrary.multiplyColor(color, lightColor);
			depthBuffer.setPixel(x, y, z);
			colorBuffer.setPixel(x, y, modelColor);
		}
	}

	private int getLightFactor(int[] normal, int[] lightDirection, int[] viewDirection) {
		// diffuse
		int dotProduct = VectorLibrary.dotProduct(normal, lightDirection);
		int diffuseFactor = Math.max(dotProduct, 0);
		diffuseFactor = MathLibrary.multiply(diffuseFactor, shaderProperties.getDiffuseIntensity());
		// specular
		VectorLibrary.invert(lightDirection);
		TransformationLibrary.reflect(lightDirection, normal);
		dotProduct = VectorLibrary.dotProduct(viewDirection, lightDirection);
		int specularFactor = Math.max(dotProduct, 0);
		specularFactor = MathLibrary.pow(specularFactor, shaderProperties.getShininess());
		specularFactor = MathLibrary.multiply(specularFactor, shaderProperties.getSpecularIntensity());
		// putting it all together...
		return diffuseFactor + specularFactor;
	}
	
	private int getAttenuation(int[] lightLocation) {
		// attenuation
		int distance = VectorLibrary.length(lightLocation);
		int attenuation = INITIAL_ATTENUATION;
		attenuation += MathLibrary.multiply(distance, LINEAR_ATTENUATION);
		attenuation += MathLibrary.multiply(MathLibrary.multiply(distance, distance), QUADRATIC_ATTENUATION);
		return attenuation + 1;
	}
	
	private boolean inShadow(int[] location, int[] lightMatrix, int[] lightFrustum, Texture shadowMap) {
		VectorLibrary.copy(lightSpaceLocation, location);
		VectorLibrary.matrixMultiply(lightSpaceLocation, lightMatrix);
		TransformationLibrary.screenportVector(lightSpaceLocation, lightFrustum, lightSpaceLocation);
		int x = lightSpaceLocation[VECTOR_X];
		int y = lightSpaceLocation[VECTOR_Y];
		int depth = shadowMap.getPixel(x, y);
		return depth < lightSpaceLocation[VECTOR_Z];
	}

	public ShaderBuffer getShaderBuffer() {
		return shaderBuffer;
	}

	public void setShaderBuffer(ShaderBuffer shaderBuffer) {
		this.shaderBuffer = (ForwardShaderBuffer) shaderBuffer;
	}

	public void setProperties(ShaderProperties shaderProperties) {
		this.shaderProperties = (SpecularProperties) shaderProperties;
	}
	
	public ShaderProperties getProperties() {
		return shaderProperties;
	}
}

