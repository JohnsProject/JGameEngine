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
package com.johnsproject.jpge2.shader;

import java.util.List;

import com.johnsproject.jpge2.dto.Camera;
import com.johnsproject.jpge2.dto.Face;
import com.johnsproject.jpge2.dto.FrameBuffer;
import com.johnsproject.jpge2.dto.Light;
import com.johnsproject.jpge2.dto.Vertex;
import com.johnsproject.jpge2.primitive.Matrix;
import com.johnsproject.jpge2.primitive.Vector;
import com.johnsproject.jpge2.primitive.Texture;
import com.johnsproject.jpge2.processor.ColorProcessor;
import com.johnsproject.jpge2.processor.GraphicsProcessor;
import com.johnsproject.jpge2.processor.MathProcessor;

public class FlatSpecularShader extends Shader {
	
	private static final byte FP_BITS = MathProcessor.FP_BITS;
	private static final int FP_ONE = MathProcessor.FP_ONE;
	
	private final Vector uvX;
	private final Vector uvY;

	private final Vector normalizedNormal;
	private final Vector lightLocation;
	private final Vector lightDirection;
	private final Vector viewDirection;
	private final Vector faceLocation;
	private final int[] portedCanvas;
	
	private final Matrix viewMatrix;
	private final Matrix projectionMatrix;
	
	private final Vector directionalLocation;	
	private final Vector spotLocation;
	
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
		
		this.uvX = new Vector();
		this.uvY = new Vector();

		this.normalizedNormal = new Vector();
		this.lightLocation = new Vector();
		this.lightDirection = new Vector();
		this.viewDirection = new Vector();
		this.faceLocation = new Vector();
		this.portedCanvas = new int[4];

		this.viewMatrix = new Matrix();
		this.projectionMatrix = new Matrix();
		
		this.directionalLocation = new Vector();
		this.spotLocation = new Vector();
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
		
		Matrix.MATRIX_IDENTITY.copy(viewMatrix);
		Matrix.MATRIX_IDENTITY.copy(projectionMatrix);
		
		GraphicsProcessor.getViewMatrix(camera.getTransform(), viewMatrix);

		GraphicsProcessor.portCanvas(camera.getCanvas(), frameBuffer.getWidth(), frameBuffer.getHeight(), portedCanvas);
		
		switch (camera.getType()) {
		case ORTHOGRAPHIC:
			GraphicsProcessor.getOrthographicMatrix(portedCanvas, camera.getFrustum(), projectionMatrix);
			break;

		case PERSPECTIVE:
			GraphicsProcessor.getPerspectiveMatrix(portedCanvas, camera.getFrustum(), projectionMatrix);
			break;
		}
	}

	@Override
	public void vertex(int index, Vertex vertex) {	}

	@Override
	public void geometry(Face face) {
		this.shaderProperties = (SpecularShaderProperties)face.getMaterial().getProperties();
		Vector normal = face.getNormal();
		Vector location1 = face.getVertex(0).getLocation();
		Vector location2 = face.getVertex(1).getLocation();
		Vector location3 = face.getVertex(2).getLocation();
		Vector.VECTOR_ZERO.copy(faceLocation);
		faceLocation.add(location1);
		faceLocation.add(location2);
		faceLocation.add(location3);
		faceLocation.divide(3 << FP_BITS);
		
		if (shaderData.getDirectionalLightMatrix() != null) {
			faceLocation.multiply(shaderData.getDirectionalLightMatrix());
			GraphicsProcessor.viewport(directionalLocation, shaderData.getDirectionalLightCanvas(), directionalLocation);
		}
		
		if (shaderData.getSpotLightMatrix() != null) {
			faceLocation.multiply(shaderData.getSpotLightMatrix());
			GraphicsProcessor.viewport(spotLocation, shaderData.getSpotLightCanvas(), spotLocation);
		}
		
		lightColor = ColorProcessor.WHITE;
		lightFactor = 50;
		
		Vector cameraLocation = camera.getTransform().getLocation();	
		cameraLocation.copy(viewDirection);
		viewDirection.subtract(faceLocation);
		// normalize values
		normal.copy(normalizedNormal);
		normalizedNormal.normalize();
		viewDirection.normalize();
		for (int i = 0; i < lights.size(); i++) {
			Light light = lights.get(i);
			int currentFactor = 0;
			int attenuation = 0;
			Vector lightPosition = light.getTransform().getLocation();
			switch (light.getType()) {
			case DIRECTIONAL:
				if (cameraLocation.distance(lightPosition) > shaderData.getLightRange())
					continue;
				light.getDirection().copy(lightDirection);
				lightDirection.invert();
				currentFactor = getLightFactor(normalizedNormal, lightDirection, viewDirection, shaderProperties);
				break;
			case POINT:
				if (cameraLocation.distance(lightPosition) > shaderData.getLightRange())
					continue;
				lightPosition.copy(lightLocation);
				lightLocation.subtract(faceLocation);
				// attenuation
				attenuation = getAttenuation(lightLocation);
				// other light values
				lightLocation.normalize();
				currentFactor = getLightFactor(normalizedNormal, lightLocation, viewDirection, shaderProperties);
				currentFactor = (currentFactor << 8) / attenuation;
				break;
			case SPOT:				
				light.getDirection().copy(lightDirection);
				lightDirection.invert();
				if (cameraLocation.distance(lightPosition) > shaderData.getLightRange())
					continue;
				lightPosition.copy(lightLocation);
				lightLocation.subtract(faceLocation);
				// attenuation
				attenuation = getAttenuation(lightLocation);
				lightLocation.normalize();
				int theta = lightLocation.dotProduct(lightDirection);
				int phi = MathProcessor.cos(light.getSpotSize() >> 1);
				if(theta > phi) {
					int intensity = -MathProcessor.divide(phi - theta, light.getSpotSoftness() + 1);
					intensity = MathProcessor.clamp(intensity, 1, FP_ONE);
					currentFactor = getLightFactor(normalizedNormal, lightDirection, viewDirection, shaderProperties);
					currentFactor = (currentFactor * intensity) / attenuation;
				}
				break;
			}
			currentFactor = MathProcessor.multiply(currentFactor, light.getStrength());
			boolean inShadow = false;
			if (i == shaderData.getDirectionalLightIndex()) {
				if (shaderData.getDirectionalLightMatrix() != null) {
					inShadow = inShadow(directionalLocation, shaderData.getDirectionalShadowMap());
					lightFactor += currentFactor;
				}
			} else if ((i == shaderData.getSpotLightIndex()) && (currentFactor > 10)) {
				if (shaderData.getSpotLightMatrix() != null) {
					inShadow = inShadow(spotLocation, shaderData.getSpotShadowMap());
				}
			}
			if(inShadow) {
				lightColor = ColorProcessor.lerp(lightColor, light.getShadowColor(), 128);
			} else {
				lightColor = ColorProcessor.lerp(lightColor, light.getColor(), currentFactor);
				lightFactor += currentFactor;
			}
		}
		modelColor = ColorProcessor.lerp(ColorProcessor.BLACK, shaderProperties.getDiffuseColor(), lightFactor);
		modelColor = ColorProcessor.multiplyColor(modelColor, lightColor);
		for (int i = 0; i < face.getVertices().length; i++) {
			Vector vertexLocation = face.getVertices()[i].getLocation();
			vertexLocation.multiply(viewMatrix);
			vertexLocation.multiply(projectionMatrix);
			GraphicsProcessor.viewport(vertexLocation, portedCanvas, vertexLocation);
		}
//		if (!GraphicsProcessor.isBackface(location1, location2, location3)) {
			texture = shaderProperties.getTexture();
			// set uv values that will be interpolated and fit uv into texture resolution
			if (texture != null) {
				int width = texture.getWidth()- 1;
				int height = texture.getHeight() - 1;
				uvX.getValues()[0] = MathProcessor.multiply(face.getUV1().getX(), width);
				uvX.getValues()[1] = MathProcessor.multiply(face.getUV2().getX(), width);
				uvX.getValues()[2] = MathProcessor.multiply(face.getUV3().getX(), width);
				uvY.getValues()[0] = MathProcessor.multiply(face.getUV1().getY(), height);
				uvY.getValues()[1] = MathProcessor.multiply(face.getUV2().getY(), height);
				uvY.getValues()[2] = MathProcessor.multiply(face.getUV3().getY(), height);
			}
			GraphicsProcessor.drawTriangle(location1, location2, location3, portedCanvas, this);
//		}
	}

	@Override
	public void fragment(Vector location) {
		if (texture != null) {
			int u = GraphicsProcessor.interpolate(uvX);
			int v = GraphicsProcessor.interpolate(uvY);
			int texel = texture.getPixel(u, v);
			if (ColorProcessor.getAlpha(texel) == 0) // discard pixel if alpha = 0
				return;
			modelColor = ColorProcessor.lerp(ColorProcessor.BLACK, texel, lightFactor);
			modelColor = ColorProcessor.multiplyColor(modelColor, lightColor);
		}
		Texture colorBuffer = frameBuffer.getColorBuffer();
		Texture depthBuffer = frameBuffer.getDepthBuffer();
//		if (depthBuffer.getPixel(location.getX(), location.getY()) > location.getZ()) {
			depthBuffer.setPixel(location.getX(), location.getY(), location.getZ());
			colorBuffer.setPixel(location.getX(), location.getY(), modelColor);
//		}
	}

	private int getLightFactor(Vector normal, Vector lightDirection, Vector viewDirection, SpecularShaderProperties properties) {
		// diffuse
		int dotProduct = normal.dotProduct(lightDirection);
		int diffuseFactor = Math.max(dotProduct, 0);
		diffuseFactor = MathProcessor.multiply(diffuseFactor, properties.getDiffuseIntensity());
		// specular
		lightDirection.invert();
		lightDirection.reflect(normal);
		dotProduct = viewDirection.dotProduct(lightDirection);
		int specularFactor = Math.max(dotProduct, 0);
		specularFactor = MathProcessor.pow(specularFactor, properties.getShininess() >> FP_BITS);
		specularFactor = MathProcessor.multiply(specularFactor, properties.getSpecularIntensity());
		// putting it all together...
		return (diffuseFactor + specularFactor << 8) >> FP_BITS;
	}
	
	private int getAttenuation(Vector lightLocation) {
		// attenuation
		long distance = lightLocation.magnitude();
		int attenuation = shaderData.getConstantAttenuation();
		attenuation += MathProcessor.multiply(distance, shaderData.getLinearAttenuation());
		attenuation += MathProcessor.multiply(MathProcessor.multiply(distance, distance), shaderData.getQuadraticAttenuation());
		attenuation >>= FP_BITS;
		return (attenuation << 8) >> FP_BITS;
	}
	
	private boolean inShadow(Vector lightSpaceLocation, Texture shadowMap) {
		int x = lightSpaceLocation.getX();
		int y = lightSpaceLocation.getY();
		x = MathProcessor.clamp(x, 0, shadowMap.getWidth() - 1);
		y = MathProcessor.clamp(y, 0, shadowMap.getHeight() - 1);
		int depth = shadowMap.getPixel(x, y);
		int bias = 50;
		return depth < lightSpaceLocation.getZ() - bias;
	}
}
