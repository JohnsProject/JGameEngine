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

public class PhongSpecularShader extends Shader {

	private final int[] uvX = generate();
	private final int[] uvY = generate();

	private final int[][] modelMatrix = generateMatrix();
	private final int[][] normalMatrix = generateMatrix();
	private final int[][] viewMatrix = generateMatrix();
	private final int[][] projectionMatrix = generateMatrix();

	private final int[] fragmentLocation = generate();
	private final int[] normalizedNormal = generate();
	private final int[] lightDirection = generate();
	private final int[] viewDirection = generate();

	private final int[] viewDirectionX = generate();
	private final int[] viewDirectionY = generate();
	private final int[] viewDirectionZ = generate();
	private final int[] locationX = generate();
	private final int[] locationY = generate();
	private final int[] locationZ = generate();
	private final int[] normalX = generate();
	private final int[] normalY = generate();
	private final int[] normalZ = generate();

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

		setup(frameBuffer.getSize(), camera.getCanvas(), this);
		
		copy(modelMatrix, MATRIX_IDENTITY);
		copy(normalMatrix, MATRIX_IDENTITY);
		copy(viewMatrix, MATRIX_IDENTITY);
		copy(projectionMatrix, MATRIX_IDENTITY);

		getModelMatrix(model.getTransform(), modelMatrix);
		getNormalMatrix(model.getTransform(), normalMatrix);
		getViewMatrix(camera.getTransform(), viewMatrix);

		switch (camera.getType()) {
		case ORTHOGRAPHIC:
			getOrthographicMatrix(camera.getFrustum(), projectionMatrix);
			break;

		case PERSPECTIVE:
			getPerspectiveMatrix(camera.getFrustum(), projectionMatrix);
			break;
		}
	}

	public void vertex(int index, Vertex vertex) {
		int[] location = copy(vertex.getLocation(), vertex.getStartLocation());
		int[] normal = copy(vertex.getNormal(), vertex.getStartNormal());
		
		multiply(location, modelMatrix, location);
		locationX[index] = location[VECTOR_X];
		locationY[index] = location[VECTOR_Y];
		locationZ[index] = location[VECTOR_Z];
		
		subtract(camera.getTransform().getLocation(), location, viewDirection);
		normalize(viewDirection, viewDirection);
		viewDirectionX[index] = viewDirection[VECTOR_X];
		viewDirectionY[index] = viewDirection[VECTOR_Y];
		viewDirectionZ[index] = viewDirection[VECTOR_Z];
		
		multiply(location, viewMatrix, location);
		multiply(location, projectionMatrix, location);
		viewport(location, location);

		multiply(normal, normalMatrix, normal);
		normalize(normal, normalizedNormal);
		normalX[index] = normalizedNormal[VECTOR_X];
		normalY[index] = normalizedNormal[VECTOR_Y];
		normalZ[index] = normalizedNormal[VECTOR_Z];
	}

	public void geometry(Face face) {
		int[] location1 = face.getVertex(0).getLocation();
		int[] location2 = face.getVertex(1).getLocation();
		int[] location3 = face.getVertex(2).getLocation();

		material = face.getMaterial();

		if (!isBackface(location1, location2, location3) && isInsideFrustum(location1, location2, location3, camera.getFrustum())) {
			texture = face.getMaterial().getTexture();
			// set uv values that will be interpolated and fit uv into texture resolution
			if (texture != null) {
				int width = texture.getWidth() - 1;
				int height = texture.getHeight() - 1;
				uvX[0] = multiply(face.getUV1()[VECTOR_X], width);
				uvX[1] = multiply(face.getUV2()[VECTOR_X], width);
				uvX[2] = multiply(face.getUV3()[VECTOR_X], width);
				uvY[0] = multiply(face.getUV1()[VECTOR_Y], height);
				uvY[1] = multiply(face.getUV2()[VECTOR_Y], height);
				uvY[2] = multiply(face.getUV3()[VECTOR_Y], height);
			}
			drawTriangle(location1, location2, location3);
		}
	}

	public void fragment(int[] location, int[] barycentric) {

		viewDirection[VECTOR_X] = interpolate(viewDirectionX, barycentric);
		viewDirection[VECTOR_Y] = interpolate(viewDirectionY, barycentric);
		viewDirection[VECTOR_Z] = interpolate(viewDirectionZ, barycentric);
		
		fragmentLocation[VECTOR_X] = interpolate(locationX, barycentric);
		fragmentLocation[VECTOR_Y] = interpolate(locationY, barycentric);
		fragmentLocation[VECTOR_Z] = interpolate(locationZ, barycentric);

		normalizedNormal[VECTOR_X] = interpolate(normalX, barycentric);
		normalizedNormal[VECTOR_Y] = interpolate(normalY, barycentric);
		normalizedNormal[VECTOR_Z] = interpolate(normalZ, barycentric);

		int lightColor = ColorProcessor.WHITE;
		int lightFactor = 0;

		for (int i = 0; i < lights.size(); i++) {
			Light light = lights.get(i);
			int[] lightLocation = light.getTransform().getLocation();
			int currentFactor = 0;
			subtract(lightLocation, fragmentLocation, lightDirection);
			switch (light.getType()) {
			case DIRECTIONAL:
				normalize(lightDirection, lightDirection);
				currentFactor = getLightFactor(light, normalizedNormal, lightDirection, viewDirection, material);
				break;
			case POINT:
				// attenuation
				long distance = magnitude(lightDirection);
				int attenuation = FP_ONE;
				attenuation += multiply(distance, 3000);
				attenuation += multiply(multiply(distance, distance), 20);
				attenuation = attenuation >> FP_BITS;
				// other light values
				normalize(lightDirection, lightDirection);
				currentFactor = getLightFactor(light, normalizedNormal, lightDirection, viewDirection, material);
				currentFactor = (currentFactor * 100) / attenuation;
				break;
			}
			lightColor = ColorProcessor.lerp(lightColor, light.getDiffuseColor(), currentFactor);
			lightFactor += currentFactor;
		}

		if (texture != null) {
			int u = interpolate(uvX, barycentric);
			int v = interpolate(uvY, barycentric);
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
		int dotProduct = dotProduct(normal, lightDirection);
		int diffuseFactor = Math.max(dotProduct, 0);
		diffuseFactor = multiply(diffuseFactor, material.getDiffuseIntensity());
		// specular
		invert(lightDirection, lightDirection);
		reflect(lightDirection, normal, lightDirection);
		dotProduct = dotProduct(viewDirection, lightDirection);
		int specularFactor = Math.max(dotProduct, 0);
		specularFactor = pow(specularFactor, material.getShininess());
		specularFactor = multiply(specularFactor, material.getSpecularIntensity());
		// putting it all together...
		return ((diffuseFactor + specularFactor + light.getStrength()) * 100) >> FP_BITS;
	}
}
