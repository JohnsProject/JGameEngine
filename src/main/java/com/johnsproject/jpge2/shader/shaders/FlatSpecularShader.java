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
import com.johnsproject.jpge2.dto.Triangle;
import com.johnsproject.jpge2.dto.Vertex;
import com.johnsproject.jpge2.library.ColorLibrary;
import com.johnsproject.jpge2.library.GraphicsLibrary;
import com.johnsproject.jpge2.library.MathLibrary;
import com.johnsproject.jpge2.library.MatrixLibrary;
import com.johnsproject.jpge2.library.VectorLibrary;
import com.johnsproject.jpge2.shader.Shader;
import com.johnsproject.jpge2.shader.ShaderDataBuffer;
import com.johnsproject.jpge2.shader.databuffers.ForwardDataBuffer;
import com.johnsproject.jpge2.shader.ShaderProperties;

public class FlatSpecularShader implements Shader {
	
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
	
	private final int[] directionalLocation;	
	private final int[] spotLocation;
	
	private final int[][] viewMatrix;
	private final int[][] projectionMatrix;
	
	private final Triangle triangle;
	
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
		this.triangle = new Triangle();
		
		this.normalizedNormal = vectorLibrary.generate();
		this.lightLocation = vectorLibrary.generate();
		this.lightDirection = vectorLibrary.generate();
		this.viewDirection = vectorLibrary.generate();
		this.faceLocation = vectorLibrary.generate();
		this.viewMatrix = matrixLibrary.generate();
		this.projectionMatrix = matrixLibrary.generate();
		this.portedFrustum = new int[Camera.FRUSTUM_SIZE];
		
		this.directionalLocation = vectorLibrary.generate();
		this.spotLocation = vectorLibrary.generate();
	}
	
	public void update(ShaderDataBuffer shaderDataBuffer) {
		this.shaderData = (ForwardDataBuffer)shaderDataBuffer;
		this.lights = shaderData.getLights();
		this.frameBuffer = shaderData.getFrameBuffer();
		if (!shaderData.isSkyboxActive()) {
			frameBuffer.getColorBuffer().fill(0);
			frameBuffer.getDepthBuffer().fill(Integer.MAX_VALUE);
		}
	}
	
	public void setup(Camera camera) {
		this.camera = camera;
		graphicsLibrary.viewMatrix(viewMatrix, camera.getTransform());
		graphicsLibrary.portFrustum(camera.getFrustum(), frameBuffer.getWidth(), frameBuffer.getHeight(), portedFrustum);
		switch (camera.getType()) {
		case ORTHOGRAPHIC:
			graphicsLibrary.orthographicMatrix(projectionMatrix, portedFrustum);
			break;

		case PERSPECTIVE:
			graphicsLibrary.perspectiveMatrix(projectionMatrix, portedFrustum);
			break;
		}
	}

	public void vertex(int index, Vertex vertex) {	}

	public void geometry(Face face) {
		this.shaderProperties = (ShaderProperties)face.getMaterial().getProperties();
		int[] normal = face.getNormal();
		int[] location1 = face.getVertex(0).getLocation();
		int[] location2 = face.getVertex(1).getLocation();
		int[] location3 = face.getVertex(2).getLocation();
		vectorLibrary.add(location1, location2, faceLocation);
		vectorLibrary.add(faceLocation, location3, faceLocation);
		vectorLibrary.divide(faceLocation, 3 << FP_BITS, faceLocation);
		
		if (shaderData.getDirectionalLightIndex() != -1) {
			vectorLibrary.multiply(faceLocation, shaderData.getDirectionalLightMatrix(), directionalLocation);
			graphicsLibrary.viewport(directionalLocation, shaderData.getDirectionalLightFrustum(), directionalLocation);
		}
		
		if (shaderData.getSpotLightIndex() != -1) {
			vectorLibrary.multiply(faceLocation, shaderData.getSpotLightMatrix(), spotLocation);
			graphicsLibrary.viewport(spotLocation, shaderData.getSpotLightFrustum(), spotLocation);
		}
		
		lightColor = ColorLibrary.BLACK;		
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
				currentFactor = mathLibrary.divide(currentFactor, attenuation);
				break;
			case SPOT:				
				if (vectorLibrary.distance(cameraLocation, lightPosition) > shaderData.getLightRange())
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
				}
				break;
			}
			currentFactor = mathLibrary.multiply(currentFactor, light.getStrength());
			currentFactor = mathLibrary.multiply(currentFactor, 255);
			boolean inShadow = false;
			if (i == shaderData.getDirectionalLightIndex()) {
				inShadow = inShadow(directionalLocation, shaderData.getDirectionalShadowMap());
			}
			if ((i == shaderData.getSpotLightIndex()) & (currentFactor > 10)) {
				inShadow = inShadow(spotLocation, shaderData.getSpotShadowMap());
			}
			if(inShadow) {
				lightColor = colorLibrary.lerp(lightColor, light.getShadowColor(), 128);
			} else {
				lightColor = colorLibrary.lerp(lightColor, light.getColor(), currentFactor);
			}
		}
		color = shaderProperties.getDiffuseColor();
		for (int i = 0; i < face.getVertices().length; i++) {
			int[] vertexLocation = face.getVertices()[i].getLocation();
			vectorLibrary.multiply(vertexLocation, viewMatrix, vertexLocation);
			vectorLibrary.multiply(vertexLocation, projectionMatrix, vertexLocation);
			graphicsLibrary.viewport(vertexLocation, portedFrustum, vertexLocation);
		}
		texture = shaderProperties.getTexture();
		// set uv values that will be interpolated and fit uv into texture resolution
		if (texture != null) {
			int width = texture.getWidth() - 1;
			int height = texture.getHeight() - 1;
			triangle.getU()[0] = mathLibrary.multiply(face.getUV1()[VECTOR_X], width);
			triangle.getU()[1] = mathLibrary.multiply(face.getUV2()[VECTOR_X], width);
			triangle.getU()[2] = mathLibrary.multiply(face.getUV3()[VECTOR_X], width);
			triangle.getV()[0] = mathLibrary.multiply(face.getUV1()[VECTOR_Y], height);
			triangle.getV()[1] = mathLibrary.multiply(face.getUV2()[VECTOR_Y], height);
			triangle.getV()[2] = mathLibrary.multiply(face.getUV3()[VECTOR_Y], height);
		}
		vectorLibrary.copy(triangle.getLocation1(), face.getVertex(0).getLocation());
		vectorLibrary.copy(triangle.getLocation2(), face.getVertex(1).getLocation());
		vectorLibrary.copy(triangle.getLocation3(), face.getVertex(2).getLocation());
		graphicsLibrary.drawTexturedGouraudTriangle(triangle, portedFrustum, this);
	}

	public void fragment(int[] location) {
		if (texture != null) {
			int texel = texture.getPixel(triangle.getU()[3], triangle.getV()[3]);
			if (colorLibrary.getAlpha(texel) == 0) // discard pixel if alpha = 0
				return;
			modelColor = colorLibrary.multiplyColor(texel, lightColor);
		} else {
			modelColor = colorLibrary.multiplyColor(color, lightColor);
		}
		Texture colorBuffer = frameBuffer.getColorBuffer();
		Texture depthBuffer = frameBuffer.getDepthBuffer();
		if (depthBuffer.getPixel(location[VECTOR_X], location[VECTOR_Y]) > location[VECTOR_Z]) {
			depthBuffer.setPixel(location[VECTOR_X], location[VECTOR_Y], location[VECTOR_Z]);
			colorBuffer.setPixel(location[VECTOR_X], location[VECTOR_Y], modelColor);
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
		int attenuation = shaderData.getConstantAttenuation();
		attenuation += mathLibrary.multiply(distance, shaderData.getLinearAttenuation());
		attenuation += mathLibrary.multiply(mathLibrary.multiply(distance, distance), shaderData.getQuadraticAttenuation());
		return (attenuation >> FP_BITS) + 1;
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
