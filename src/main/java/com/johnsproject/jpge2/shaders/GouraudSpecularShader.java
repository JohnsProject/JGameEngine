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
import com.johnsproject.jpge2.processors.MatrixProcessor;
import com.johnsproject.jpge2.processors.VectorProcessor;
import com.johnsproject.jpge2.processors.GraphicsProcessor.Shader;

public class GouraudSpecularShader implements Shader {

	private final byte vx = VectorProcessor.VECTOR_X;
	private final byte vy = VectorProcessor.VECTOR_Y;
	private final byte vz = VectorProcessor.VECTOR_Z;

	private final int[] uvX = VectorProcessor.generate();
	private final int[] uvY = VectorProcessor.generate();

	private final int[] normalizedNormal = VectorProcessor.generate();
	private final int[] lightDirection = VectorProcessor.generate();
	private final int[] viewDirection = VectorProcessor.generate();

	private final int[][] modelMatrix = MatrixProcessor.generate();
	private final int[][] normalMatrix = MatrixProcessor.generate();
	private final int[][] viewMatrix = MatrixProcessor.generate();
	private final int[][] projectionMatrix = MatrixProcessor.generate();

	private final int[] lightFactors = VectorProcessor.generate();
	private final int[] lightColorR = VectorProcessor.generate();
	private final int[] lightColorG = VectorProcessor.generate();
	private final int[] lightColorB = VectorProcessor.generate();

	private int color;
	private int modelColor;
	private Texture texture;

	private Camera camera;
	private List<Light> lights;
	private FrameBuffer frameBuffer;

	private boolean verticesInside = true;

	public void update(List<Light> lights, FrameBuffer frameBuffer) {
		this.lights = lights;
		this.frameBuffer = frameBuffer;
		frameBuffer.clearColorBuffer();
		frameBuffer.clearDepthBuffer();
	}

	public void setup(Model model, Camera camera) {
		this.camera = camera;

		MatrixProcessor.copy(modelMatrix, MatrixProcessor.IDENTITY);
		MatrixProcessor.copy(normalMatrix, MatrixProcessor.IDENTITY);
		MatrixProcessor.copy(viewMatrix, MatrixProcessor.IDENTITY);
		MatrixProcessor.copy(projectionMatrix, MatrixProcessor.IDENTITY);

		GraphicsProcessor.getModelMatrix(model.getTransform(), modelMatrix);
		GraphicsProcessor.getNormalMatrix(model.getTransform(), normalMatrix);
		GraphicsProcessor.getViewMatrix(camera.getTransform(), viewMatrix);

		switch (camera.getType()) {
		case ORTHOGRAPHIC:
			GraphicsProcessor.getOrthographicMatrix(camera.getCanvas(), camera.getFrustum(), projectionMatrix);
			break;

		case PERSPECTIVE:
			GraphicsProcessor.getPerspectiveMatrix(camera.getCanvas(), camera.getFrustum(), projectionMatrix);
			break;
		}
	}

	public void vertex(int index, Vertex vertex) {
		Material material = vertex.getMaterial();
		int[] location = VectorProcessor.copy(vertex.getLocation(), vertex.getStartLocation());
		int[] normal = VectorProcessor.copy(vertex.getNormal(), vertex.getStartNormal());
		VectorProcessor.multiply(location, modelMatrix, location);
		VectorProcessor.multiply(normal, normalMatrix, normal);

		int lightColor = ColorProcessor.WHITE;
		int lightFactor = 0;

		VectorProcessor.subtract(camera.getTransform().getLocation(), location, viewDirection);
		// normalize values
		VectorProcessor.normalize(normal, normalizedNormal);
		VectorProcessor.normalize(viewDirection, viewDirection);
		for (int i = 0; i < lights.size(); i++) {
			Light light = lights.get(i);
			int[] lightLocation = light.getTransform().getLocation();
			int currentFactor = 0;
			VectorProcessor.subtract(lightLocation, location, lightDirection);
			switch (light.getType()) {
			case DIRECTIONAL:
				VectorProcessor.normalize(lightDirection, lightDirection);
				currentFactor = getLightFactor(light, normalizedNormal, lightDirection, viewDirection, material);
				break;
			case POINT:
				// attenuation
				long distance = VectorProcessor.magnitude(lightDirection);
				int attenuation = MathProcessor.FP_VALUE;
				attenuation += MathProcessor.multiply(distance, 3000);
				attenuation += MathProcessor.multiply(MathProcessor.multiply(distance, distance), 20);
				attenuation = attenuation >> MathProcessor.FP_SHIFT;
				// other light values
				VectorProcessor.normalize(lightDirection, lightDirection);
				currentFactor = getLightFactor(light, normalizedNormal, lightDirection, viewDirection, material);
				currentFactor = (currentFactor * 100) / attenuation;
				break;
			}
			lightColor = ColorProcessor.lerp(lightColor, light.getDiffuseColor(), currentFactor);
			lightFactor += currentFactor;
		}
		lightFactors[index] = lightFactor;
		lightColorR[index] = ColorProcessor.getRed(lightColor);
		lightColorG[index] = ColorProcessor.getGreen(lightColor);
		lightColorB[index] = ColorProcessor.getBlue(lightColor);
		VectorProcessor.multiply(location, viewMatrix, location);
		VectorProcessor.multiply(location, projectionMatrix, location);
		GraphicsProcessor.viewport(location, camera.getCanvas(), location);
		if ((location[vz] < camera.getFrustum()[1]) || (location[vz] > camera.getFrustum()[2]))
			verticesInside = false;
	}

	public void geometry(Face face) {
		int[] location1 = face.getVertex(0).getLocation();
		int[] location2 = face.getVertex(1).getLocation();
		int[] location3 = face.getVertex(2).getLocation();

		color = face.getMaterial().getColor();

		if ((GraphicsProcessor.barycentric(location1, location2, location3) > 0) && verticesInside) {
			texture = face.getMaterial().getTexture();
			// set uv values that will be interpolated and fit uv into texture resolution
			if (texture != null) {
				int width = texture.getWidth() - 1;
				int height = texture.getHeight() - 1;
				uvX[0] = MathProcessor.multiply(face.getUV1()[vx], width);
				uvX[1] = MathProcessor.multiply(face.getUV2()[vx], width);
				uvX[2] = MathProcessor.multiply(face.getUV3()[vx], width);
				uvY[0] = MathProcessor.multiply(face.getUV1()[vy], height);
				uvY[1] = MathProcessor.multiply(face.getUV2()[vy], height);
				uvY[2] = MathProcessor.multiply(face.getUV3()[vy], height);
			}
			GraphicsProcessor.drawTriangle(location1, location2, location3, camera.getCanvas(), this);
		}
		verticesInside = true;
	}

	public void fragment(int[] location, int[] barycentric) {
		int lightFactor = GraphicsProcessor.interpolate(lightFactors, barycentric);
		int r = GraphicsProcessor.interpolate(lightColorR, barycentric);
		int g = GraphicsProcessor.interpolate(lightColorG, barycentric);
		int b = GraphicsProcessor.interpolate(lightColorB, barycentric);
		int lightColor = ColorProcessor.convert(r, g, b);
		if (texture != null) {
			int u = GraphicsProcessor.interpolate(uvX, barycentric);
			int v = GraphicsProcessor.interpolate(uvY, barycentric);
			int texel = texture.getPixel(u, v);
			if (ColorProcessor.getAlpha(texel) == 0) // discard pixel if alpha = 0
				return;
			modelColor = ColorProcessor.lerp(ColorProcessor.BLACK, texel, lightFactor);
			modelColor = ColorProcessor.multiplyColor(modelColor, lightColor);
		} else {
			modelColor = ColorProcessor.lerp(ColorProcessor.BLACK, color, lightFactor);
			modelColor = ColorProcessor.multiplyColor(modelColor, lightColor);
		}
		frameBuffer.setPixel(location[vx], location[vy], location[vz], (byte) 0, modelColor);
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
		return ((diffuseFactor + specularFactor + light.getStrength()) * 100) >> MathProcessor.FP_SHIFT;
	}
}
