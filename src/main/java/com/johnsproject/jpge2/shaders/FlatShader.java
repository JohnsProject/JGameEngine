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
import com.johnsproject.jpge2.processors.MathProcessor;
import com.johnsproject.jpge2.processors.VectorProcessor;
import com.johnsproject.jpge2.processors.GraphicsProcessor.Shader;

public class FlatShader implements Shader {

	private static final int vx = VectorProcessor.VECTOR_X;
	private static final int vy = VectorProcessor.VECTOR_Y;
	private static final int vz = VectorProcessor.VECTOR_Z;
	
	private static final int[] uvX = VectorProcessor.generate();
	private static final int[] uvY = VectorProcessor.generate();

	private static final int[] normalizedNormal = VectorProcessor.generate();
	private static final int[] lightDirection = VectorProcessor.generate();
	private static final int[] viewDirection = VectorProcessor.generate();
	private static final int[] faceLocation = VectorProcessor.generate();
	
	private static int lightColor;
	private static int lightFactor;
	private static int modelColor;
	private static Texture texture;

	private static int[][] modelMatrix;
	private static int[][] normalMatrix;
	private static int[][] viewMatrix;
	private static int[][] projectionMatrix;
	
	private static Camera camera;
	private static List<Light> lights;
	private static FrameBuffer frameBuffer;

	public void setup(Model model, Camera camera, List<Light> lights, FrameBuffer frameBuffer) {
		FlatShader.modelMatrix = model.getModelMatrix();
		FlatShader.normalMatrix = model.getNormalMatrix();
		FlatShader.viewMatrix = camera.getViewMatrix();
		FlatShader.projectionMatrix = camera.getProjectionMatrix();
		FlatShader.camera = camera;
		FlatShader.lights = lights;
		FlatShader.frameBuffer = frameBuffer;
	}
	
	public void vertex(int index, Vertex vertex) {
		int[] location = vertex.getLocation();
		VectorProcessor.multiply(location, modelMatrix, location);
	}

	public void geometry(Face face) {
		Material material = face.getMaterial();
		int[] normal = face.getNormal();
		int[] location1 = face.getVertex1().getLocation();
		int[] location2 = face.getVertex2().getLocation();
		int[] location3 = face.getVertex3().getLocation();
		VectorProcessor.add(location1, location2, faceLocation);
		VectorProcessor.add(faceLocation, location3, faceLocation);
		VectorProcessor.divide(faceLocation, 3, faceLocation);
		
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
				int distance = VectorProcessor.magnitude(lightDirection);
				int attenuation = MathProcessor.FP_VALUE;
				attenuation += MathProcessor.multiply(distance, 140000);
				attenuation += MathProcessor.multiply(MathProcessor.multiply(distance, distance), 7000);
				attenuation = attenuation >> MathProcessor.FP_SHIFT;
				// other light values
				VectorProcessor.normalize(lightDirection, lightDirection);
				currentFactor = getLightFactor(light, normalizedNormal, lightDirection, viewDirection, material);
				// * 10000 here because attenuation also has * 100
				currentFactor = (currentFactor * 10000) / attenuation;
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
			GraphicsProcessor.viewport(vertexLocation, camera.getCanvas(), camera.getFrustum());
			if ((vertexLocation[vz] < camera.getFrustum()[1]) || (vertexLocation[vz] > camera.getFrustum()[2]))
				return;
		}
		if (GraphicsProcessor.barycentric(location1, location2, location3) > 0) {
			texture = face.getMaterial().getTexture();
			// set uv values that will be interpolated
			// uv is in normalized fixed point space between 0 - MathProcessor.FP_VALUE
			// multiply uv with texture size to get correct coordinates and divide by
			// MathProcessor.FP_VALUE
			if (texture != null) {
				uvX[0] = MathProcessor.multiply(face.getUV1()[vx], texture.getWidth());
				uvX[1] = MathProcessor.multiply(face.getUV2()[vx], texture.getWidth());
				uvX[2] = MathProcessor.multiply(face.getUV3()[vx], texture.getWidth());
				uvY[0] = MathProcessor.multiply(face.getUV1()[vy], texture.getHeight());
				uvY[1] = MathProcessor.multiply(face.getUV2()[vy], texture.getHeight());
				uvY[2] = MathProcessor.multiply(face.getUV3()[vy], texture.getHeight());
			}
			GraphicsProcessor.drawTriangle(location1, location2, location3, this, frameBuffer);
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
		frameBuffer.setPixel(location[vx], location[vy], location[vz], (byte) 0, modelColor);
	}

	private int getLightFactor(Light light, int[] normal, int[] lightDirection, int[] viewDirection, Material material) {
		// diffuse
		int dotProduct = VectorProcessor.dotProduct(normal, lightDirection);
		int diffuseFactor = Math.max(dotProduct, 0);
		diffuseFactor = MathProcessor.multiply(diffuseFactor, material.getDiffuseIntensity());
		// specular
		VectorProcessor.invert(lightDirection, lightDirection);
		VectorProcessor.reflect(lightDirection, normal, lightDirection);
		dotProduct = VectorProcessor.dotProduct(viewDirection, lightDirection);
		int specularFactor = Math.max(dotProduct, 0);
		specularFactor = MathProcessor.multiply(specularFactor, material.getSpecularIntensity());
		// putting it all together...
		return ((diffuseFactor + specularFactor + light.getStrength()) * 100) >> MathProcessor.FP_SHIFT;
	}
}
