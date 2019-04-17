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

public class PhongSpecularShader implements Shader {

	private static final byte VECTOR_X = VectorProcessor.VECTOR_X;
	private static final byte VECTOR_Y = VectorProcessor.VECTOR_Y;
	private static final byte VECTOR_Z = VectorProcessor.VECTOR_Z;
	
	private static final byte FP_BITS = MathProcessor.FP_BITS;
	private static final int FP_ONE = MathProcessor.FP_ONE;
	
	private final int[] uvX = VectorProcessor.generate();
	private final int[] uvY = VectorProcessor.generate();

	private final int[][] modelMatrix = MatrixProcessor.generate();
	private final int[][] normalMatrix = MatrixProcessor.generate();
	private final int[][] viewMatrix = MatrixProcessor.generate();
	private final int[][] projectionMatrix = MatrixProcessor.generate();

	private final int[] fragmentLocation = VectorProcessor.generate();
	private final int[] normalizedNormal = VectorProcessor.generate();
	private final int[] lightDirection = VectorProcessor.generate();
	private final int[] viewDirection = VectorProcessor.generate();

	private final int[] viewDirectionX = VectorProcessor.generate();
	private final int[] viewDirectionY = VectorProcessor.generate();
	private final int[] viewDirectionZ = VectorProcessor.generate();
	private final int[] locationX = VectorProcessor.generate();
	private final int[] locationY = VectorProcessor.generate();
	private final int[] locationZ = VectorProcessor.generate();
	private final int[] normalX = VectorProcessor.generate();
	private final int[] normalY = VectorProcessor.generate();
	private final int[] normalZ = VectorProcessor.generate();

	private Material material;
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
		int[] normal = VectorProcessor.copy(vertex.getNormal(), vertex.getStartNormal());
		
		VectorProcessor.multiply(location, modelMatrix, location);
		locationX[index] = location[VECTOR_X];
		locationY[index] = location[VECTOR_Y];
		locationZ[index] = location[VECTOR_Z];
		
		VectorProcessor.subtract(camera.getTransform().getLocation(), location, viewDirection);
		VectorProcessor.normalize(viewDirection, viewDirection);
		viewDirectionX[index] = viewDirection[VECTOR_X];
		viewDirectionY[index] = viewDirection[VECTOR_Y];
		viewDirectionZ[index] = viewDirection[VECTOR_Z];
		
		VectorProcessor.multiply(location, viewMatrix, location);
		VectorProcessor.multiply(location, projectionMatrix, location);
		GraphicsProcessor.viewport(location, location);

		VectorProcessor.multiply(normal, normalMatrix, normal);
		VectorProcessor.normalize(normal, normalizedNormal);
		normalX[index] = normalizedNormal[VECTOR_X];
		normalY[index] = normalizedNormal[VECTOR_Y];
		normalZ[index] = normalizedNormal[VECTOR_Z];
	}

	public void geometry(Face face) {
		int[] location1 = face.getVertex(0).getLocation();
		int[] location2 = face.getVertex(1).getLocation();
		int[] location3 = face.getVertex(2).getLocation();

		material = face.getMaterial();

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

		viewDirection[VECTOR_X] = GraphicsProcessor.interpolate(viewDirectionX, barycentric);
		viewDirection[VECTOR_Y] = GraphicsProcessor.interpolate(viewDirectionY, barycentric);
		viewDirection[VECTOR_Z] = GraphicsProcessor.interpolate(viewDirectionZ, barycentric);
		
		fragmentLocation[VECTOR_X] = GraphicsProcessor.interpolate(locationX, barycentric);
		fragmentLocation[VECTOR_Y] = GraphicsProcessor.interpolate(locationY, barycentric);
		fragmentLocation[VECTOR_Z] = GraphicsProcessor.interpolate(locationZ, barycentric);

		normalizedNormal[VECTOR_X] = GraphicsProcessor.interpolate(normalX, barycentric);
		normalizedNormal[VECTOR_Y] = GraphicsProcessor.interpolate(normalY, barycentric);
		normalizedNormal[VECTOR_Z] = GraphicsProcessor.interpolate(normalZ, barycentric);

		int lightColor = ColorProcessor.WHITE;
		int lightFactor = 0;

		for (int i = 0; i < lights.size(); i++) {
			Light light = lights.get(i);
			int[] lightLocation = light.getTransform().getLocation();
			int currentFactor = 0;
			VectorProcessor.subtract(lightLocation, fragmentLocation, lightDirection);
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

		if (texture != null) {
			int u = GraphicsProcessor.interpolate(uvX, barycentric);
			int v = GraphicsProcessor.interpolate(uvY, barycentric);
			int texel = texture.getPixel(u, v);
			if (ColorProcessor.getAlpha(texel) == 0) // discard pixel if alpha = 0
				return;
			modelColor = ColorProcessor.lerp(ColorProcessor.BLACK, texel, lightFactor);
			modelColor = ColorProcessor.multiplyColor(modelColor, lightColor);
		} else {
			modelColor = ColorProcessor.lerp(ColorProcessor.BLACK, material.getColor(), lightFactor);
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
