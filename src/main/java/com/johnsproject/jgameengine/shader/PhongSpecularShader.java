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
import com.johnsproject.jgameengine.library.VectorLibrary;
import com.johnsproject.jgameengine.model.Light;
import com.johnsproject.jgameengine.model.Texture;
import com.johnsproject.jgameengine.rasterizer.PerspectivePhongRasterizer;

import static com.johnsproject.jgameengine.library.VectorLibrary.*;
import static com.johnsproject.jgameengine.library.MathLibrary.*;

public class PhongSpecularShader extends Shader {

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

	@Override
	public void vertex(VertexBuffer vertexBuffer) {
		int[] location = vertexBuffer.getLocation();
		int[] normal = vertexBuffer.getWorldNormal();
		vectorLibrary.normalize(normal, normal);
		vectorLibrary.copy(location, vertexBuffer.getWorldLocation());
		vectorLibrary.matrixMultiply(location, shaderBuffer.getViewMatrix(), location);
		vectorLibrary.matrixMultiply(location, shaderBuffer.getProjectionMatrix(), location);
		graphicsLibrary.screenportVector(location, shaderBuffer.getPortedFrustum(), location);
	}

	@Override
	public void geometry(GeometryBuffer geometryBuffer) {
		color = shaderProperties.getDiffuseColor();
		texture = shaderProperties.getTexture();
		if (texture == null) {
			rasterizer.draw(geometryBuffer);
		} else {
			rasterizer.perspectiveDraw(geometryBuffer, texture);
		}
	}

	@Override
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
			vectorLibrary.subtract(cameraLocation, worldLocation, viewDirection);
			vectorLibrary.normalize(viewDirection, viewDirection);
			int lightIndex = 0;
			for (Light light: shaderBuffer.getLights()) {
				if(light.isCulled())
					continue;
				int currentFactor = 0;
				int attenuation = 0;
				int[] lightPosition = light.getTransform().getLocation();
				switch (light.getType()) {
				case DIRECTIONAL:
					vectorLibrary.invert(light.getDirection(), lightDirection);
					currentFactor = getLightFactor(normal, lightDirection, viewDirection);
					if (lightIndex == shaderBuffer.getDirectionalLightIndex()) {
						int[] lightMatrix = shaderBuffer.getDirectionalLightMatrix();
						int[] lightFrustum = shaderBuffer.getDirectionalLightFrustum();
						Texture shadowMap = shaderBuffer.getDirectionalShadowMap();
						if(inShadow(worldLocation, lightMatrix, lightFrustum, shadowMap)) {
							currentFactor = colorLibrary.multiplyColor(currentFactor, light.getShadowColor());
						}
					}
					break;
				case POINT:
					vectorLibrary.subtract(lightPosition, worldLocation, lightLocation);
					attenuation = getAttenuation(lightLocation);
					vectorLibrary.normalize(lightLocation, lightLocation);
					currentFactor = getLightFactor(normal, lightLocation, viewDirection);
					currentFactor = mathLibrary.divide(currentFactor, attenuation);
					if ((lightIndex == shaderBuffer.getPointLightIndex()) && (currentFactor > 150)) {
						int[] lightFrustum = shaderBuffer.getPointLightFrustum();
						for (int j = 0; j < shaderBuffer.getPointLightMatrices().length; j++) {
							int[] lightMatrix = shaderBuffer.getPointLightMatrices()[j];
							Texture shadowMap = shaderBuffer.getPointShadowMaps()[j];
							if(inShadow(worldLocation, lightMatrix, lightFrustum, shadowMap)) {
								currentFactor = colorLibrary.multiplyColor(currentFactor, light.getShadowColor());
							}
						}
					}
					break;
				case SPOT:			
					vectorLibrary.invert(light.getDirection(), lightDirection);
					vectorLibrary.subtract(lightPosition, worldLocation, lightLocation);
					attenuation = getAttenuation(lightLocation);
					vectorLibrary.normalize(lightLocation, lightLocation);
					int theta = vectorLibrary.dotProduct(lightLocation, lightDirection);
					int phi = mathLibrary.cos(light.getSpotSize() >> 1);
					if(theta > phi) {
						int intensity = -mathLibrary.divide(phi - theta, light.getSpotSoftness() + 1);
						intensity = mathLibrary.clamp(intensity, 1, FP_ONE);
						currentFactor = getLightFactor(normal, lightDirection, viewDirection);
						currentFactor = mathLibrary.multiply(currentFactor, intensity * 2);
						currentFactor = mathLibrary.divide(currentFactor, attenuation);
						if ((lightIndex == shaderBuffer.getSpotLightIndex()) && (currentFactor > 10)) {
							int[] lightMatrix = shaderBuffer.getSpotLightMatrix();
							int[] lightFrustum = shaderBuffer.getSpotLightFrustum();
							Texture shadowMap = shaderBuffer.getSpotShadowMap();
							if(inShadow(worldLocation, lightMatrix, lightFrustum, shadowMap)) {
								currentFactor = colorLibrary.multiplyColor(currentFactor, light.getShadowColor());
							}
						}
					}
					break;
				}
				currentFactor = mathLibrary.multiply(currentFactor, light.getStrength());
				currentFactor = mathLibrary.multiply(currentFactor, 255);
				lightColor = colorLibrary.lerp(lightColor, light.getColor(), currentFactor);
				lightIndex++;
			}
			if (texture != null) {
				int[] uv = fragmentBuffer.getUV();
				int texel = texture.getPixel(uv[VECTOR_X], uv[VECTOR_Y]);
				if (colorLibrary.getAlpha(texel) == 0) // discard pixel if alpha = 0
					return;
				color = texel;
			}
			modelColor = colorLibrary.multiplyColor(color, lightColor);
			depthBuffer.setPixel(x, y, z);
			colorBuffer.setPixel(x, y, modelColor);
		}
	}

	private int getLightFactor(int[] normal, int[] lightDirection, int[] viewDirection) {
		// diffuse
		int dotProduct = vectorLibrary.dotProduct(normal, lightDirection);
		int diffuseFactor = Math.max(dotProduct, 0);
		diffuseFactor = mathLibrary.multiply(diffuseFactor, shaderProperties.getDiffuseIntensity());
		// specular
		vectorLibrary.invert(lightDirection, lightDirection);
		vectorLibrary.reflect(lightDirection, normal, lightDirection);
		dotProduct = vectorLibrary.dotProduct(viewDirection, lightDirection);
		int specularFactor = Math.max(dotProduct, 0);
		specularFactor = mathLibrary.pow(specularFactor, shaderProperties.getShininess());
		specularFactor = mathLibrary.multiply(specularFactor, shaderProperties.getSpecularIntensity());
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

	@Override
	public ShaderBuffer getShaderBuffer() {
		return shaderBuffer;
	}

	@Override
	public void setShaderBuffer(ShaderBuffer shaderBuffer) {
		this.shaderBuffer = (ForwardShaderBuffer) shaderBuffer;
	}

	@Override
	public void setProperties(ShaderProperties shaderProperties) {
		this.shaderProperties = (SpecularProperties) shaderProperties;
	}

	@Override
	public ShaderProperties getProperties() {
		return shaderProperties;
	}
}

