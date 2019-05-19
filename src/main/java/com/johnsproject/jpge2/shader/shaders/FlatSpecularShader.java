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
import com.johnsproject.jpge2.dto.Face;
import com.johnsproject.jpge2.dto.FrameBuffer;
import com.johnsproject.jpge2.dto.Light;
import com.johnsproject.jpge2.dto.Texture;
import com.johnsproject.jpge2.dto.Vertex;
import com.johnsproject.jpge2.library.ColorLibrary;
import com.johnsproject.jpge2.library.GraphicsLibrary;
import com.johnsproject.jpge2.library.MathLibrary;
import com.johnsproject.jpge2.library.MatrixLibrary;
import com.johnsproject.jpge2.library.VectorLibrary;
import com.johnsproject.jpge2.shader.Shader;
import com.johnsproject.jpge2.shader.ShaderDataBuffer;
import com.johnsproject.jpge2.shader.databuffers.ForwardDataBuffer;
import com.johnsproject.jpge2.shader.properties.SpecularShaderProperties;

public class FlatSpecularShader extends Shader {
	
	private final GraphicsLibrary graphicsLibrary;
	private final MathLibrary mathLibrary;
	private final MatrixLibrary matrixLibrary;
	private final VectorLibrary vectorLibrary;
	private final ColorLibrary colorLibrary;
	
	private final int[] uvX;
	private final int[] uvY;

	private final int[] normalizedNormal;
	private final int[] lightLocation;
	private final int[] lightDirection;
	private final int[] viewDirection;
	private final int[] faceLocation;
	private final int[] portedCanvas;
	
	private final int[] directionalLocation;	
	private final int[] spotLocation;
	
	private final int[][] viewMatrix;
	private final int[][] projectionMatrix;
	
	private int lightColor;
	private int lightFactor;
	private int modelColor;
	private Texture texture;
	
	private Camera camera;	
	private List<Light> lights;
	private FrameBuffer frameBuffer;
	private ForwardDataBuffer shaderData;
	private SpecularShaderProperties shaderProperties;
	
	public FlatSpecularShader() {
		super(2);
		this.graphicsLibrary = new GraphicsLibrary();
		this.mathLibrary = new MathLibrary();
		this.matrixLibrary = new MatrixLibrary();
		this.vectorLibrary = new VectorLibrary();
		this.colorLibrary = new ColorLibrary();
		
		this.uvX = getVariable(0);
		this.uvY = getVariable(1);

		this.normalizedNormal = vectorLibrary.generate();
		this.lightLocation = vectorLibrary.generate();
		this.lightDirection = vectorLibrary.generate();
		this.viewDirection = vectorLibrary.generate();
		this.faceLocation = vectorLibrary.generate();
		this.portedCanvas = vectorLibrary.generate();
		this.viewMatrix = matrixLibrary.generate();
		this.projectionMatrix = matrixLibrary.generate();
		
		this.directionalLocation = vectorLibrary.generate();
		this.spotLocation = vectorLibrary.generate();
	}
	
	@Override
	public void update(ShaderDataBuffer shaderDataBuffer) {
		this.shaderData = (ForwardDataBuffer)shaderDataBuffer;
		this.lights = shaderData.getLights();
		this.frameBuffer = shaderData.getFrameBuffer();
		frameBuffer.getColorBuffer().fill(0);
		frameBuffer.getDepthBuffer().fill(Integer.MAX_VALUE);
	}
	
	@Override
	public void setup(Camera camera) {
		this.camera = camera;
		graphicsLibrary.viewMatrix(viewMatrix, camera.getTransform());
		graphicsLibrary.portCanvas(camera.getCanvas(), frameBuffer.getWidth(), frameBuffer.getHeight(), portedCanvas);
		switch (camera.getType()) {
		case ORTHOGRAPHIC:
			graphicsLibrary.orthographicMatrix(projectionMatrix, camera.getFrustum());
			break;

		case PERSPECTIVE:
			graphicsLibrary.perspectiveMatrix(projectionMatrix, camera.getFrustum());
			break;
		}
	}

	@Override
	public void vertex(int index, Vertex vertex) {	}

	@Override
	public void geometry(Face face) {
		this.shaderProperties = (SpecularShaderProperties)face.getMaterial().getProperties();
		int[] normal = face.getNormal();
		int[] location1 = face.getVertex(0).getLocation();
		int[] location2 = face.getVertex(1).getLocation();
		int[] location3 = face.getVertex(2).getLocation();
		vectorLibrary.add(location1, location2, faceLocation);
		vectorLibrary.add(faceLocation, location3, faceLocation);
		vectorLibrary.divide(faceLocation, 3 << FP_BITS, faceLocation);
		
		if (shaderData.getDirectionalLightIndex() > 0) {
			vectorLibrary.multiply(faceLocation, shaderData.getDirectionalLightMatrix(), directionalLocation);
			graphicsLibrary.viewport(directionalLocation, shaderData.getDirectionalLightCanvas(), directionalLocation);
		}
		
		if (shaderData.getSpotLightIndex() > 0) {
			vectorLibrary.multiply(faceLocation, shaderData.getSpotLightMatrix(), spotLocation);
			graphicsLibrary.viewport(spotLocation, shaderData.getSpotLightCanvas(), spotLocation);
		}
		
		lightColor = ColorLibrary.WHITE;
		lightFactor = 50;
		
		int[] cameraLocation = camera.getTransform().getLocation();		
		vectorLibrary.subtract(cameraLocation, faceLocation, viewDirection);
		// normalize values
		vectorLibrary.normalize(normal, normalizedNormal);
		vectorLibrary.normalize(viewDirection, viewDirection);
		for (int i = 0; i < lights.size(); i++) {
			Light light = lights.get(i);
			int currentFactor = 0;
			int attenuation = 0;
			int[] lightPosition = light.getTransform().getLocation();
			switch (light.getType()) {
			case DIRECTIONAL:
				if (vectorLibrary.distance(cameraLocation, lightPosition) > shaderData.getLightRange())
					continue;
				vectorLibrary.invert(light.getDirection(), lightDirection);
				currentFactor = getLightFactor(normalizedNormal, lightDirection, viewDirection, shaderProperties);
				break;
			case POINT:
				if (vectorLibrary.distance(cameraLocation, lightPosition) > shaderData.getLightRange())
					continue;
				vectorLibrary.subtract(lightPosition, faceLocation, lightLocation);
				// attenuation
				attenuation = getAttenuation(lightLocation);
				// other light values
				vectorLibrary.normalize(lightLocation, lightLocation);
				currentFactor = getLightFactor(normalizedNormal, lightLocation, viewDirection, shaderProperties);
				currentFactor = (currentFactor << 8) / attenuation;
				break;
			case SPOT:				
				vectorLibrary.invert(light.getDirection(), lightDirection);
				if (vectorLibrary.distance(cameraLocation, lightPosition) > shaderData.getLightRange())
					continue;
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
					currentFactor = (currentFactor * intensity) / attenuation;
				}
				break;
			}
			currentFactor = mathLibrary.multiply(currentFactor, light.getStrength());
			boolean inShadow = false;
			if (i == shaderData.getDirectionalLightIndex()) {
				inShadow = inShadow(directionalLocation, shaderData.getDirectionalShadowMap());
				lightFactor += currentFactor;
			} else if ((i == shaderData.getSpotLightIndex()) & (currentFactor > 10)) {
				inShadow = inShadow(spotLocation, shaderData.getSpotShadowMap());
			}
			if(inShadow) {
				lightColor = colorLibrary.lerp(lightColor, light.getShadowColor(), 128);
			} else {
				lightColor = colorLibrary.lerp(lightColor, light.getColor(), currentFactor);
				lightFactor += currentFactor;
			}
		}
		modelColor = colorLibrary.lerp(ColorLibrary.BLACK, shaderProperties.getDiffuseColor(), lightFactor);
		modelColor = colorLibrary.multiplyColor(modelColor, lightColor);
		for (int i = 0; i < face.getVertices().length; i++) {
			int[] vertexLocation = face.getVertices()[i].getLocation();
			vectorLibrary.multiply(vertexLocation, viewMatrix, vertexLocation);
			vectorLibrary.multiply(vertexLocation, projectionMatrix, vertexLocation);
			graphicsLibrary.viewport(vertexLocation, portedCanvas, vertexLocation);
		}
		texture = shaderProperties.getTexture();
		// set uv values that will be interpolated and fit uv into texture resolution
		if (texture != null) {
			int width = texture.getWidth() - 1;
			int height = texture.getHeight() - 1;
			uvX[0] = mathLibrary.multiply(face.getUV1()[VECTOR_X], width);
			uvX[1] = mathLibrary.multiply(face.getUV2()[VECTOR_X], width);
			uvX[2] = mathLibrary.multiply(face.getUV3()[VECTOR_X], width);
			uvY[0] = mathLibrary.multiply(face.getUV1()[VECTOR_Y], height);
			uvY[1] = mathLibrary.multiply(face.getUV2()[VECTOR_Y], height);
			uvY[2] = mathLibrary.multiply(face.getUV3()[VECTOR_Y], height);
		}
		graphicsLibrary.drawTriangle(location1, location2, location3, portedCanvas, camera.getFrustum(), this);
	}

	@Override
	public void fragment(int[] location) {
		if (texture != null) {
			int texel = texture.getPixel(uvX[3], uvY[3]);
			if (colorLibrary.getAlpha(texel) == 0) // discard pixel if alpha = 0
				return;
			modelColor = colorLibrary.lerp(ColorLibrary.BLACK, texel, lightFactor);
			modelColor = colorLibrary.multiplyColor(modelColor, lightColor);
		}
		Texture colorBuffer = frameBuffer.getColorBuffer();
		Texture depthBuffer = frameBuffer.getDepthBuffer();
		if (depthBuffer.getPixel(location[VECTOR_X], location[VECTOR_Y]) > location[VECTOR_Z]) {
			depthBuffer.setPixel(location[VECTOR_X], location[VECTOR_Y], location[VECTOR_Z]);
			colorBuffer.setPixel(location[VECTOR_X], location[VECTOR_Y], modelColor);
		}
	}

	private int getLightFactor(int[] normal, int[] lightDirection, int[] viewDirection, SpecularShaderProperties properties) {
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
		return (diffuseFactor + specularFactor << 8) >> FP_BITS;
	}
	
	private int getAttenuation(int[] lightLocation) {
		// attenuation
		long distance = vectorLibrary.magnitude(lightLocation);
		int attenuation = shaderData.getConstantAttenuation();
		attenuation += mathLibrary.multiply(distance, shaderData.getLinearAttenuation());
		attenuation += mathLibrary.multiply(mathLibrary.multiply(distance, distance), shaderData.getQuadraticAttenuation());
		attenuation >>= FP_BITS;
		return (attenuation << 8) >> FP_BITS;
	}
	
	private boolean inShadow(int[] lightSpaceLocation, Texture shadowMap) {
		int x = lightSpaceLocation[VECTOR_X];
		int y = lightSpaceLocation[VECTOR_Y];
		x = mathLibrary.clamp(x, 0, shadowMap.getWidth() - 1);
		y = mathLibrary.clamp(y, 0, shadowMap.getHeight() - 1);
		int depth = shadowMap.getPixel(x, y);
		return depth < lightSpaceLocation[VECTOR_Z];
	}
}
