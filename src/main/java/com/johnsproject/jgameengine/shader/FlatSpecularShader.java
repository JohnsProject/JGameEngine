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

import static com.johnsproject.jgameengine.math.FixedPointMath.*;
import static com.johnsproject.jgameengine.math.VectorMath.*;

import com.johnsproject.jgameengine.math.ColorMath;
import com.johnsproject.jgameengine.math.FixedPointMath;
import com.johnsproject.jgameengine.math.TransformationMath;
import com.johnsproject.jgameengine.math.VectorMath;
import com.johnsproject.jgameengine.model.Light;
import com.johnsproject.jgameengine.model.Texture;
import com.johnsproject.jgameengine.rasterizer.PerspectiveFlatRasterizer;

public class FlatSpecularShader  implements Shader {
	
	private static final int INITIAL_ATTENUATION = FP_ONE;
	private static final int LINEAR_ATTENUATION = FixedPointMath.toFixedPoint(0.045);
	private static final int QUADRATIC_ATTENUATION = FixedPointMath.toFixedPoint(0.0075);
	
	private SpecularProperties shaderProperties;
	private ForwardShaderBuffer shaderBuffer;
	private final PerspectiveFlatRasterizer rasterizer;

	private final int[] lightLocation;
	private final int[] lightDirection;
	private final int[] viewDirection;
	private final int[] faceLocation;
	private final int[] lightSpaceLocation;
	
	private int lightColor;
	
	public FlatSpecularShader() {
		this.rasterizer = new PerspectiveFlatRasterizer(this);
		this.shaderProperties = new SpecularProperties();
		this.lightLocation = VectorMath.emptyVector();
		this.lightDirection = VectorMath.emptyVector();
		this.viewDirection = VectorMath.emptyVector();
		this.faceLocation = VectorMath.emptyVector();
		this.lightSpaceLocation = VectorMath.emptyVector();
	}

	public void vertex(VertexBuffer vertexBuffer) {
		int[] location = vertexBuffer.getLocation();
		VectorMath.copy(location, vertexBuffer.getWorldLocation());
		VectorMath.multiply(location, shaderBuffer.getCamera().getTransform().getSpaceEnterMatrix());
		VectorMath.multiply(location, shaderBuffer.getCamera().getProjectionMatrix());
		TransformationMath.screenportVector(location, shaderBuffer.getCamera().getRenderTargetPortedFrustum());
	}

	public void geometry(GeometryBuffer geometryBuffer) {
		int[] normal = geometryBuffer.getWorldNormal();
		int[] location1 = geometryBuffer.getVertexBuffer(0).getWorldLocation();
		int[] location2 = geometryBuffer.getVertexBuffer(1).getWorldLocation();
		int[] location3 = geometryBuffer.getVertexBuffer(2).getWorldLocation();
		VectorMath.copy(faceLocation, location1);
		VectorMath.add(faceLocation, location2);
		VectorMath.add(faceLocation, location3);
		VectorMath.divide(faceLocation, 3 << FP_BIT);	
		lightColor = ColorMath.BLACK;		
		int[] cameraLocation = shaderBuffer.getCamera().getTransform().getLocation();		
		VectorMath.normalize(normal);
		VectorMath.copy(viewDirection, cameraLocation);
		VectorMath.subtract(viewDirection, faceLocation);
		VectorMath.normalize(viewDirection);
		boolean inShadow = false;
		int lightIndex = 0;
		for(int i = 0; i < shaderBuffer.getLights().size(); i++) {
			Light light = shaderBuffer.getLights().get(i);
			if(light.isCulled())
				continue;
			int currentFactor = 0;
			int attenuation = 0;
			int[] lightPosition = light.getTransform().getLocation();
			switch (light.getType()) {
			case DIRECTIONAL:
				VectorMath.copy(lightDirection, light.getDirection());
				VectorMath.invert(lightDirection);
				currentFactor = getLightFactor(normal, lightDirection, viewDirection);
				if (lightIndex == shaderBuffer.getDirectionalLightIndex()) {
					int[][] lightMatrix = shaderBuffer.getDirectionalLightMatrix();
					int[] lightFrustum = shaderBuffer.getDirectionalLightFrustum();
					Texture shadowMap = shaderBuffer.getDirectionalShadowMap();
					if(inShadow(faceLocation, lightMatrix, lightFrustum, shadowMap)) {
						currentFactor = ColorMath.multiplyColor(currentFactor, light.getShadowColor());
					}
				}
				break;
			case POINT:
				VectorMath.copy(lightLocation, lightPosition);
				VectorMath.subtract(lightLocation, faceLocation);
				// attenuation
				attenuation = getAttenuation(lightLocation);
				// other light values
				VectorMath.normalize(lightLocation);
				currentFactor = getLightFactor(normal, lightLocation, viewDirection);
				currentFactor = FixedPointMath.divide(currentFactor, attenuation);
				if ((lightIndex == shaderBuffer.getPointLightIndex()) && (currentFactor > 150)) {
					for (int j = 0; j < shaderBuffer.getPointLightMatrices().length; j++) {
						int[][] lightMatrix = shaderBuffer.getPointLightMatrices()[j];
						int[] lightFrustum = shaderBuffer.getPointLightFrustum();
						Texture shadowMap = shaderBuffer.getPointShadowMaps()[j];
						if(inShadow(faceLocation, lightMatrix, lightFrustum, shadowMap)) {
							currentFactor = ColorMath.multiplyColor(currentFactor, light.getShadowColor());
						}
					}
				}
				break;
			case SPOT:
				VectorMath.copy(lightDirection, light.getDirection());				
				VectorMath.copy(lightLocation, lightPosition);
				VectorMath.invert(lightDirection);
				VectorMath.subtract(lightLocation, faceLocation);
				// attenuation
				attenuation = getAttenuation(lightLocation);
				VectorMath.normalize(lightLocation);
				long theta = VectorMath.dotProduct(lightLocation, lightDirection);
				int phi = FixedPointMath.cos(light.getSpotSize() >> 1);
				if(theta > phi) {
					int intensity = -FixedPointMath.divide(phi - theta, light.getSpotSoftness() + 1);
					intensity = FixedPointMath.clamp(intensity, 1, FP_ONE);
					currentFactor = getLightFactor(normal, lightDirection, viewDirection);
					currentFactor = FixedPointMath.multiply(currentFactor, intensity * 2);
					currentFactor = FixedPointMath.divide(currentFactor, attenuation);
					if ((lightIndex == shaderBuffer.getSpotLightIndex()) && (currentFactor > 10)) {
						int[][] lightMatrix = shaderBuffer.getSpotLightMatrix();
						int[] lightFrustum = shaderBuffer.getSpotLightFrustum();
						Texture shadowMap = shaderBuffer.getSpotShadowMap();
						if(inShadow(faceLocation, lightMatrix, lightFrustum, shadowMap)) {
							currentFactor = ColorMath.multiplyColor(currentFactor, light.getShadowColor());
						}
					}
				}
				break;
			}
			currentFactor = FixedPointMath.multiply(currentFactor, light.getStrength());
			currentFactor = FixedPointMath.multiply(currentFactor, 255);
			if(inShadow) {
				lightColor = ColorMath.lerp(lightColor, light.getShadowColor(), 128);
			} else {
				lightColor = ColorMath.lerp(lightColor, light.getColor(), currentFactor);
			}
			lightIndex++;
		}
		Texture texture = shaderProperties.getTexture();
		if (texture == null) {
			rasterizer.draw(geometryBuffer);
		} else {
			rasterizer.perspectiveDraw(geometryBuffer, texture);
		}
	}

	public void fragment(FragmentBuffer fragmentBuffer) {
		Texture depthBuffer = shaderBuffer.getCamera().getRenderTarget().getDepthBuffer();
		Texture colorBuffer = shaderBuffer.getCamera().getRenderTarget().getColorBuffer();
		int x = fragmentBuffer.getLocation()[VECTOR_X];
		int y = fragmentBuffer.getLocation()[VECTOR_Y];
		int z = fragmentBuffer.getLocation()[VECTOR_Z];
		if (depthBuffer.getPixel(x, y) > z) {
			Texture texture = shaderProperties.getTexture();
			int color = shaderProperties.getDiffuseColor();
			if (texture != null) {
				int[] uv = fragmentBuffer.getUV();
				int texel = texture.getPixel(uv[VECTOR_X], uv[VECTOR_Y]);
				if (ColorMath.getAlpha(texel) == 0) // discard pixel if alpha = 0
					return;
				color = texel;
			}
			color = ColorMath.multiplyColor(color, lightColor);
			depthBuffer.setPixel(x, y, z);
			colorBuffer.setPixel(x, y, color);
		}
	}

	
	private int getLightFactor(int[] normal, int[] lightDirection, int[] viewDirection) {
		// diffuse
		int dotProduct = (int)VectorMath.dotProduct(normal, lightDirection);
		int diffuseFactor = Math.max(dotProduct, 0);
		diffuseFactor = FixedPointMath.multiply(diffuseFactor, shaderProperties.getDiffuseIntensity());
		// specular
		VectorMath.invert(lightDirection);
		TransformationMath.reflect(lightDirection, normal);
		dotProduct = (int)VectorMath.dotProduct(viewDirection, lightDirection);
		int specularFactor = Math.max(dotProduct, 0);
		specularFactor = FixedPointMath.pow(specularFactor, shaderProperties.getShininess());
		specularFactor = FixedPointMath.multiply(specularFactor, shaderProperties.getSpecularIntensity());
		// putting it all together...
		return diffuseFactor + specularFactor;
	}
	
	private int getAttenuation(int[] lightLocation) {
		// attenuation
		int distance = VectorMath.length(lightLocation);
		int attenuation = INITIAL_ATTENUATION;
		attenuation += FixedPointMath.multiply(distance, LINEAR_ATTENUATION);
		attenuation += FixedPointMath.multiply(FixedPointMath.multiply(distance, distance), QUADRATIC_ATTENUATION);
		return attenuation + 1;
	}
	
	private boolean inShadow(int[] location, int[][] lightMatrix, int[] lightFrustum, Texture shadowMap) {
		VectorMath.copy(lightSpaceLocation, location);
		VectorMath.multiply(lightSpaceLocation, lightMatrix);
		TransformationMath.screenportVector(lightSpaceLocation, lightFrustum);
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
