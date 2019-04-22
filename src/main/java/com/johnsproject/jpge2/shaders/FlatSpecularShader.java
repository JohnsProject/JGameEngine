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
import com.johnsproject.jpge2.processors.CentralProcessor;
import com.johnsproject.jpge2.processors.ColorProcessor;
import com.johnsproject.jpge2.processors.GraphicsProcessor;
import com.johnsproject.jpge2.processors.GraphicsProcessor.Shader;
import com.johnsproject.jpge2.processors.GraphicsProcessor.ShaderDataBuffer;
import com.johnsproject.jpge2.processors.MathProcessor;
import com.johnsproject.jpge2.processors.MatrixProcessor;
import com.johnsproject.jpge2.processors.VectorProcessor;

public class FlatSpecularShader extends Shader {

	private static final byte VECTOR_X = VectorProcessor.VECTOR_X;
	private static final byte VECTOR_Y = VectorProcessor.VECTOR_Y;
	private static final byte VECTOR_Z = VectorProcessor.VECTOR_Z;
	
	private static final byte FP_BITS = MathProcessor.FP_BITS;
	private static final int FP_ONE = MathProcessor.FP_ONE;
	
	private final MathProcessor mathProcessor;
	private final MatrixProcessor matrixProcessor;
	private final VectorProcessor vectorProcessor;
	private final ColorProcessor colorProcessor;
	private final GraphicsProcessor graphicsProcessor;
	
	private List<Light> lights;
	private FrameBuffer frameBuffer;
	
	public FlatSpecularShader(CentralProcessor centralProcessor) {
		super(centralProcessor);
		this.mathProcessor = centralProcessor.getMathProcessor();
		this.matrixProcessor = centralProcessor.getMatrixProcessor();
		this.vectorProcessor = centralProcessor.getVectorProcessor();
		this.colorProcessor = centralProcessor.getColorProcessor();
		this.graphicsProcessor = centralProcessor.getGraphicsProcessor();
		
		new ShaderPass1(this);
	}
	
	@Override
	public void update(ShaderDataBuffer shaderDataBuffer) {
		this.lights = shaderDataBuffer.getLights();
		this.frameBuffer = shaderDataBuffer.getFrameBuffer();
		frameBuffer.clearColorBuffer();
		frameBuffer.clearDepthBuffer();
	}

	private class ShaderPass1 extends ShaderPass {

		private final int[] uvX;
		private final int[] uvY;

		private final int[] normalizedNormal;
		private final int[] lightLocation;
		private final int[] lightDirection;
		private final int[] viewDirection;
		private final int[] faceLocation;
		
		private final int[][] modelMatrix;
		private final int[][] normalMatrix;
		private final int[][] viewMatrix;
		private final int[][] projectionMatrix;
		
		private int lightColor;
		private int lightFactor;
		private int modelColor;
		private Texture texture;
		
		private Camera camera;
		
		public ShaderPass1(Shader shader) {
			super(shader);
			
			this.uvX = vectorProcessor.generate();
			this.uvY = vectorProcessor.generate();

			this.normalizedNormal = vectorProcessor.generate();
			this.lightLocation = vectorProcessor.generate();
			this.lightDirection = vectorProcessor.generate();
			this.viewDirection = vectorProcessor.generate();
			this.faceLocation = vectorProcessor.generate();

			this.modelMatrix = matrixProcessor.generate();
			this.normalMatrix = matrixProcessor.generate();
			this.viewMatrix = matrixProcessor.generate();
			this.projectionMatrix = matrixProcessor.generate();
		}
		
		@Override
		public void setup(Model model, Camera camera) {
			this.camera = camera;
			
			graphicsProcessor.setup(frameBuffer.getSize(), camera.getCanvas(), this);
			
			matrixProcessor.copy(modelMatrix, MatrixProcessor.MATRIX_IDENTITY);
			matrixProcessor.copy(normalMatrix, MatrixProcessor.MATRIX_IDENTITY);
			matrixProcessor.copy(viewMatrix, MatrixProcessor.MATRIX_IDENTITY);
			matrixProcessor.copy(projectionMatrix, MatrixProcessor.MATRIX_IDENTITY);
	
			graphicsProcessor.getModelMatrix(model.getTransform(), modelMatrix);
			graphicsProcessor.getNormalMatrix(model.getTransform(), normalMatrix);
			graphicsProcessor.getViewMatrix(camera.getTransform(), viewMatrix);
	
			switch (camera.getType()) {
			case ORTHOGRAPHIC:
				graphicsProcessor.getOrthographicMatrix(camera.getFrustum(), projectionMatrix);
				break;
	
			case PERSPECTIVE:
				graphicsProcessor.getPerspectiveMatrix(camera.getFrustum(), projectionMatrix);
				break;
			}
		}
	
		@Override
		public void vertex(int index, Vertex vertex) {
			int[] location = vertex.getLocation();
			vectorProcessor.multiply(location, modelMatrix, location);
		}
	
		@Override
		public void geometry(Face face) {
			Material material = face.getMaterial();
			int[] normal = face.getNormal();
			int[] location1 = face.getVertex(0).getLocation();
			int[] location2 = face.getVertex(1).getLocation();
			int[] location3 = face.getVertex(2).getLocation();
			vectorProcessor.add(location1, location2, faceLocation);
			vectorProcessor.add(faceLocation, location3, faceLocation);
			vectorProcessor.divide(faceLocation, 3 << FP_BITS, faceLocation);
			
			lightColor = ColorProcessor.WHITE;
			lightFactor = 50;
	
			vectorProcessor.multiply(normal, normalMatrix, normal);
			vectorProcessor.subtract(camera.getTransform().getLocation(), faceLocation, viewDirection);
			// normalize values
			vectorProcessor.normalize(normal, normalizedNormal);
			vectorProcessor.normalize(viewDirection, viewDirection);
			for (int i = 0; i < lights.size(); i++) {
				Light light = lights.get(i);
				int currentFactor = 0;
				int attenuation = 0;
				switch (light.getType()) {
				case DIRECTIONAL:
					vectorProcessor.invert(light.getDirection(), lightDirection);
					currentFactor = getLightFactor(normalizedNormal, lightDirection, viewDirection, material);
					break;
				case POINT:
					vectorProcessor.subtract(light.getTransform().getLocation(), faceLocation, lightLocation);
					// attenuation
					attenuation = getAttenuation(lightLocation);
					// other light values
					vectorProcessor.normalize(lightLocation, lightLocation);
					currentFactor = getLightFactor(normalizedNormal, lightLocation, viewDirection, material);
					currentFactor = (currentFactor * 100) / attenuation;
					break;
				case SPOT:				
					vectorProcessor.invert(light.getDirection(), lightDirection);
					vectorProcessor.subtract(light.getTransform().getLocation(), faceLocation, lightLocation);
					// attenuation
					attenuation = getAttenuation(lightLocation);
					vectorProcessor.normalize(lightLocation, lightLocation);
					int theta = vectorProcessor.dotProduct(lightLocation, lightDirection);
					int phi = mathProcessor.cos(light.getSpotSize() >> 1);
					if(theta > phi) {
						int intensity = -mathProcessor.divide(phi - theta, light.getSpotSoftness() + 1);
						intensity = mathProcessor.clamp(intensity, 1, FP_ONE);
						currentFactor = getLightFactor(normalizedNormal, lightDirection, viewDirection, material);
						currentFactor = (currentFactor * intensity) / attenuation;
					}
					break;
				}
				currentFactor = mathProcessor.multiply(currentFactor, light.getStrength());
				lightColor = colorProcessor.lerp(lightColor, light.getDiffuseColor(), currentFactor);
				lightFactor += currentFactor;
			}
			modelColor = colorProcessor.lerp(ColorProcessor.BLACK, material.getColor(), lightFactor);
			modelColor = colorProcessor.multiplyColor(modelColor, lightColor);
			for (int i = 0; i < face.getVertices().length; i++) {
				int[] vertexLocation = face.getVertices()[i].getLocation();
				vectorProcessor.multiply(vertexLocation, viewMatrix, vertexLocation);
				vectorProcessor.multiply(vertexLocation, projectionMatrix, vertexLocation);
				graphicsProcessor.viewport(vertexLocation, vertexLocation);
			}
			if (!graphicsProcessor.isBackface(location1, location2, location3)
					&& graphicsProcessor.isInsideFrustum(location1, location2, location3, camera.getFrustum())) {
				texture = face.getMaterial().getTexture();
				// set uv values that will be interpolated and fit uv into texture resolution
				if (texture != null) {
					int width = texture.getWidth() - 1;
					int height = texture.getHeight() - 1;
					uvX[0] = mathProcessor.multiply(face.getUV1()[VECTOR_X], width);
					uvX[1] = mathProcessor.multiply(face.getUV2()[VECTOR_X], width);
					uvX[2] = mathProcessor.multiply(face.getUV3()[VECTOR_X], width);
					uvY[0] = mathProcessor.multiply(face.getUV1()[VECTOR_Y], height);
					uvY[1] = mathProcessor.multiply(face.getUV2()[VECTOR_Y], height);
					uvY[2] = mathProcessor.multiply(face.getUV3()[VECTOR_Y], height);
				}
				graphicsProcessor.drawTriangle(location1, location2, location3);
			}
		}
	
		@Override
		public void fragment(int[] location, int[] barycentric) {
			if (texture != null) {
				int u = graphicsProcessor.interpolate(uvX, barycentric);
				int v = graphicsProcessor.interpolate(uvY, barycentric);
				int texel = texture.getPixel(u, v);
				if (colorProcessor.getAlpha(texel) == 0) // discard pixel if alpha = 0
					return;
				modelColor = colorProcessor.lerp(ColorProcessor.BLACK, texel, lightFactor);
				modelColor = colorProcessor.multiplyColor(modelColor, lightColor);
			}
			frameBuffer.setPixel(location[VECTOR_X], location[VECTOR_Y], location[VECTOR_Z], (byte) 0, modelColor);
		}
	
		private int getLightFactor(int[] normal, int[] lightDirection, int[] viewDirection, Material material) {
			// diffuse
			int dotProduct = vectorProcessor.dotProduct(normal, lightDirection);
			int diffuseFactor = Math.max(dotProduct, 0);
			diffuseFactor = mathProcessor.multiply(diffuseFactor, material.getDiffuseIntensity());
			// specular
			vectorProcessor.invert(lightDirection, lightDirection);
			vectorProcessor.reflect(lightDirection, normal, lightDirection);
			dotProduct = vectorProcessor.dotProduct(viewDirection, lightDirection);
			int specularFactor = Math.max(dotProduct, 0);
			specularFactor = mathProcessor.pow(specularFactor, material.getShininess() >> FP_BITS);
			specularFactor = mathProcessor.multiply(specularFactor, material.getSpecularIntensity());
			// putting it all together...
			return (diffuseFactor + specularFactor << 8) >> FP_BITS;
		}
		
		private int getAttenuation(int[] lightLocation) {
			// attenuation
			long distance = vectorProcessor.magnitude(lightLocation);
			int attenuation = FP_ONE;
			attenuation += mathProcessor.multiply(distance, 3000);
			attenuation += mathProcessor.multiply(mathProcessor.multiply(distance, distance), 20);
			return attenuation >> FP_BITS;
		}
	}
}
