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
package com.johnsproject.jpge2.shaders;

import java.util.List;

import com.johnsproject.jpge2.dto.Camera;
import com.johnsproject.jpge2.dto.Face;
import com.johnsproject.jpge2.dto.FrameBuffer;
import com.johnsproject.jpge2.dto.Light;
import com.johnsproject.jpge2.dto.Material;
import com.johnsproject.jpge2.dto.Model;
import com.johnsproject.jpge2.dto.Texture;
import com.johnsproject.jpge2.dto.Vertex;
import com.johnsproject.jpge2.processors.ColorProcessor;
import com.johnsproject.jpge2.processors.GraphicsProcessor;
import com.johnsproject.jpge2.processors.GraphicsProcessor.Shader;
import com.johnsproject.jpge2.processors.MathProcessor;
import com.johnsproject.jpge2.processors.MatrixProcessor;
import com.johnsproject.jpge2.processors.VectorProcessor;

public class FlatSpecularShader implements Shader {

	private static final byte VECTOR_X = VectorProcessor.VECTOR_X;
	private static final byte VECTOR_Y = VectorProcessor.VECTOR_Y;
	private static final byte VECTOR_Z = VectorProcessor.VECTOR_Z;
	
	private static final byte FP_BITS = MathProcessor.FP_BITS;
	private static final int FP_ONE = MathProcessor.FP_ONE;
	
	private final int[] uvX = VectorProcessor.generate();
	private final int[] uvY = VectorProcessor.generate();

	private final int[] normalizedNormal = VectorProcessor.generate();
	private final int[] lightDirection = VectorProcessor.generate();
	private final int[] viewDirection = VectorProcessor.generate();
	private final int[] faceLocation = VectorProcessor.generate();

	private final int[][] modelMatrix = MatrixProcessor.generate();
	private final int[][] normalMatrix = MatrixProcessor.generate();
	private final int[][] viewMatrix = MatrixProcessor.generate();
	private final int[][] projectionMatrix = MatrixProcessor.generate();

	private int lightColor;
	private int lightFactor;
	private int modelColor;
	private Texture texture;

	private Camera camera;
	private List<Light> lights;
	private FrameBuffer frameBuffer;

	public void update(List<Light> lights, FrameBuffer frameBuffer) {
		this.lights = lights;
		this.frameBuffer = frameBuffer;
		frameBuffer.clearColorBuffer();
		frameBuffer.clearDepthBuffer();
	}

	public void setup(Model model, Camera camera) {
		this.camera = camera;
		
		GraphicsProcessor.setup(frameBuffer.getSize(), camera.getCanvas(), this);
		
		MatrixProcessor.copy(modelMatrix, MatrixProcessor.MATRIX_IDENTITY);
		MatrixProcessor.copy(normalMatrix, MatrixProcessor.MATRIX_IDENTITY);
		MatrixProcessor.copy(viewMatrix, MatrixProcessor.MATRIX_IDENTITY);
		MatrixProcessor.copy(projectionMatrix, MatrixProcessor.MATRIX_IDENTITY);

		GraphicsProcessor.getModelMatrix(model.getTransform(), modelMatrix);
		GraphicsProcessor.getNormalMatrix(model.getTransform(), normalMatrix);
		GraphicsProcessor.getViewMatrix(camera.getTransform(), viewMatrix);

		switch (camera.getType()) {
		case ORTHOGRAPHIC:
			GraphicsProcessor.getOrthographicMatrix(camera.getFrustum(), projectionMatrix);
			break;

		case PERSPECTIVE:
			GraphicsProcessor.getPerspectiveMatrix(camera.getFrustum(), projectionMatrix);
			break;
		}
	}

	public void vertex(int index, Vertex vertex) {
		int[] location = VectorProcessor.copy(vertex.getLocation(), vertex.getStartLocation());
		VectorProcessor.multiply(location, modelMatrix, location);
	}

	public void geometry(Face face) {
		Material material = face.getMaterial();
		int[] normal = VectorProcessor.copy(face.getNormal(), face.getStartNormal());
		int[] location1 = face.getVertex(0).getLocation();
		int[] location2 = face.getVertex(1).getLocation();
		int[] location3 = face.getVertex(2).getLocation();
		VectorProcessor.add(location1, location2, faceLocation);
		VectorProcessor.add(faceLocation, location3, faceLocation);
		VectorProcessor.divide(faceLocation, 3 << FP_BITS, faceLocation);
		lightColor = ColorProcessor.WHITE;
		lightFactor = 0;

		VectorProcessor.multiply(normal, normalMatrix, normal);
		VectorProcessor.subtract(camera.getTransform().getLocation(), faceLocation, viewDirection);
		// normalize values
		VectorProcessor.normalize(normal, normalizedNormal);
		VectorProcessor.normalize(viewDirection, viewDirection);
		for (int i = 0; i < lights.size(); i++) {
			Light light = lights.get(i);
			int[] lightLocation = light.getTransform().getLocation();
			int currentFactor = 0;
			VectorProcessor.subtract(lightLocation, faceLocation, lightDirection);
			switch (light.getType()) {
			case DIRECTIONAL:
				VectorProcessor.normalize(lightDirection, lightDirection);
				currentFactor = getLightFactor(light, normalizedNormal, lightDirection, viewDirection, material);
				break;
			case POINT:
				// attenuation
				long distance = VectorProcessor.magnitude(lightDirection);
				int attenuation = FP_ONE;
				attenuation += MathProcessor.multiply(distance, 3000);
				attenuation += MathProcessor.multiply(MathProcessor.multiply(distance, distance), 20);
				attenuation = attenuation >> FP_BITS;
				// other light values
				VectorProcessor.normalize(lightDirection, lightDirection);
				currentFactor = getLightFactor(light, normalizedNormal, lightDirection, viewDirection, material);
				currentFactor = (currentFactor * 100) / attenuation;
				break;
			}
			lightColor = ColorProcessor.lerp(lightColor, light.getDiffuseColor(), currentFactor);
			lightFactor += currentFactor;
		}
		modelColor = ColorProcessor.lerp(ColorProcessor.BLACK, material.getColor(), lightFactor);
		modelColor = ColorProcessor.multiplyColor(modelColor, lightColor);
		for (int i = 0; i < face.getVertices().length; i++) {
			int[] vertexLocation = face.getVertices()[i].getLocation();
			VectorProcessor.multiply(vertexLocation, viewMatrix, vertexLocation);
			VectorProcessor.multiply(vertexLocation, projectionMatrix, vertexLocation);
			GraphicsProcessor.viewport(vertexLocation, vertexLocation);
		}
		if (!GraphicsProcessor.isBackface(location1, location2, location3)
				&& GraphicsProcessor.isInsideFrustum(location1, location2, location3, camera.getFrustum())) {
			texture = face.getMaterial().getTexture();
			// set uv values that will be interpolated and fit uv into texture resolution
			if (texture != null) {
				int width = texture.getWidth() - 1;
				int height = texture.getHeight() - 1;
				uvX[0] = MathProcessor.multiply(face.getUV1()[VECTOR_X], width);
				uvX[1] = MathProcessor.multiply(face.getUV2()[VECTOR_X], width);
				uvX[2] = MathProcessor.multiply(face.getUV3()[VECTOR_X], width);
				uvY[0] = MathProcessor.multiply(face.getUV1()[VECTOR_Y], height);
				uvY[1] = MathProcessor.multiply(face.getUV2()[VECTOR_Y], height);
				uvY[2] = MathProcessor.multiply(face.getUV3()[VECTOR_Y], height);
			}
			GraphicsProcessor.drawTriangle(location1, location2, location3);
		}
	}

	public void fragment(int[] location, int[] barycentric) {
		if (texture != null) {
			int u = GraphicsProcessor.interpolate(uvX, barycentric);
			int v = GraphicsProcessor.interpolate(uvY, barycentric);
			int texel = texture.getPixel(u, v);
			if (ColorProcessor.getAlpha(texel) == 0) // discard pixel if alpha = 0
				return;
			modelColor = ColorProcessor.lerp(ColorProcessor.BLACK, texel, lightFactor);
			modelColor = ColorProcessor.multiplyColor(modelColor, lightColor);
		}
		frameBuffer.setPixel(location[VECTOR_X], location[VECTOR_Y], location[VECTOR_Z], (byte) 0, modelColor);
	}

	private int getLightFactor(Light light, int[] normal, int[] lightDirection, int[] viewDirection,
			Material material) {
		// diffuse
		int dotProduct = VectorProcessor.dotProduct(normal, lightDirection);
		int diffuseFactor = Math.max(dotProduct, 0);
		diffuseFactor = MathProcessor.multiply(diffuseFactor, material.getDiffuseIntensity());
		// specular
		VectorProcessor.invert(lightDirection, lightDirection);
		VectorProcessor.reflect(lightDirection, normal, lightDirection);
		dotProduct = VectorProcessor.dotProduct(viewDirection, lightDirection);
		int specularFactor = Math.max(dotProduct, 0);
		specularFactor = MathProcessor.pow(specularFactor, material.getShininess());
		specularFactor = MathProcessor.multiply(specularFactor, material.getSpecularIntensity());
		// putting it all together...
		return ((diffuseFactor + specularFactor + light.getStrength()) * 100) >> FP_BITS;
	}
}
