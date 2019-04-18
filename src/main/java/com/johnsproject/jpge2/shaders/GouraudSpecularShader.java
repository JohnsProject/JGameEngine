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
import com.johnsproject.jpge2.processors.MathProcessor;
import com.johnsproject.jpge2.processors.MatrixProcessor;
import com.johnsproject.jpge2.processors.VectorProcessor;

public class GouraudSpecularShader extends Shader {

	private static final byte VECTOR_X = VectorProcessor.VECTOR_X;
	private static final byte VECTOR_Y = VectorProcessor.VECTOR_Y;
	private static final byte VECTOR_Z = VectorProcessor.VECTOR_Z;
	
	private static final byte FP_BITS = MathProcessor.FP_BITS;
	private static final int FP_ONE = MathProcessor.FP_ONE;
	
	private final int[] uvX;
	private final int[] uvY;

	private final int[] normalizedNormal;
	private final int[] lightDirection;
	private final int[] lightLocation;
	private final int[] viewDirection;

	private final int[][] modelMatrix;
	private final int[][] normalMatrix;
	private final int[][] viewMatrix;
	private final int[][] projectionMatrix;

	private final int[] lightFactors;
	private final int[] lightColorR;
	private final int[] lightColorG;
	private final int[] lightColorB;

	private final MathProcessor mathProcessor;
	private final MatrixProcessor matrixProcessor;
	private final VectorProcessor vectorProcessor;
	private final ColorProcessor colorProcessor;
	private final GraphicsProcessor graphicsProcessor;
	
	private int color;
	private int modelColor;
	private Texture texture;

	private Camera camera;
	private List<Light> lights;
	private FrameBuffer frameBuffer;

	public GouraudSpecularShader(CentralProcessor centralProcessor) {
		super(centralProcessor);
		this.mathProcessor = centralProcessor.getMathProcessor();
		this.matrixProcessor = centralProcessor.getMatrixProcessor();
		this.vectorProcessor = centralProcessor.getVectorProcessor();
		this.colorProcessor = centralProcessor.getColorProcessor();
		this.graphicsProcessor = centralProcessor.getGraphicsProcessor();
		
		this.uvX = vectorProcessor.generate();
		this.uvY = vectorProcessor.generate();

		this.normalizedNormal = vectorProcessor.generate();
		this.lightDirection = vectorProcessor.generate();
		this.lightLocation = vectorProcessor.generate();
		this.viewDirection = vectorProcessor.generate();
		
		this.lightFactors = vectorProcessor.generate();
		this.lightColorR = vectorProcessor.generate();
		this.lightColorG = vectorProcessor.generate();
		this.lightColorB = vectorProcessor.generate();

		this.modelMatrix = matrixProcessor.generate();
		this.normalMatrix = matrixProcessor.generate();
		this.viewMatrix = matrixProcessor.generate();
		this.projectionMatrix = matrixProcessor.generate();
	}
	
	@Override
	public void update(List<Light> lights, FrameBuffer frameBuffer) {
		this.lights = lights;
		this.frameBuffer = frameBuffer;
		frameBuffer.clearColorBuffer();
		frameBuffer.clearDepthBuffer();
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
		Material material = vertex.getMaterial();
		int[] location = vectorProcessor.copy(vertex.getLocation(), vertex.getStartLocation());
		int[] normal = vectorProcessor.copy(vertex.getNormal(), vertex.getStartNormal());
		vectorProcessor.multiply(location, modelMatrix, location);
		vectorProcessor.multiply(normal, normalMatrix, normal);

		int lightColor = ColorProcessor.WHITE;
		int lightFactor = 0;

		vectorProcessor.subtract(camera.getTransform().getLocation(), location, viewDirection);
		// normalize values
		vectorProcessor.normalize(normal, normalizedNormal);
		vectorProcessor.normalize(viewDirection, viewDirection);
		for (int i = 0; i < lights.size(); i++) {
			Light light = lights.get(i);
			int currentFactor = 0;
			switch (light.getType()) {
			case DIRECTIONAL:
				vectorProcessor.invert(light.getDirection(), lightDirection);
				currentFactor = getLightFactor(normalizedNormal, lightDirection, viewDirection, material);
				break;
			case POINT:
				vectorProcessor.subtract(light.getTransform().getLocation(), location, lightLocation);
				// attenuation
				long distance = vectorProcessor.magnitude(lightLocation);
				int attenuation = FP_ONE;
				attenuation += mathProcessor.multiply(distance, 3000);
				attenuation += mathProcessor.multiply(mathProcessor.multiply(distance, distance), 20);
				attenuation = attenuation >> FP_BITS;
				// other light values
				vectorProcessor.normalize(lightLocation, lightLocation);
				currentFactor = getLightFactor(normalizedNormal, lightLocation, viewDirection, material);
				currentFactor = (currentFactor * 100) / attenuation;
				break;
			case SPOT:
				vectorProcessor.subtract(light.getTransform().getLocation(), location, lightLocation);
				vectorProcessor.normalize(lightLocation, lightLocation);
				
				vectorProcessor.invert(light.getDirection(), lightDirection);
				
				int dot = vectorProcessor.dotProduct(lightLocation, lightDirection);
				if(dot > mathProcessor.cos(30 << FP_BITS))
					currentFactor = getLightFactor(normalizedNormal, lightDirection, viewDirection, material);
				break;
			}
			currentFactor = mathProcessor.multiply(currentFactor, light.getStrength());
			lightColor = colorProcessor.lerp(lightColor, light.getDiffuseColor(), currentFactor);
			lightFactor += currentFactor;
		}
		lightFactors[index] = lightFactor;
		lightColorR[index] = colorProcessor.getRed(lightColor);
		lightColorG[index] = colorProcessor.getGreen(lightColor);
		lightColorB[index] = colorProcessor.getBlue(lightColor);
		vectorProcessor.multiply(location, viewMatrix, location);
		vectorProcessor.multiply(location, projectionMatrix, location);
		graphicsProcessor.viewport(location, location);
	}

	@Override
	public void geometry(Face face) {
		int[] location1 = face.getVertex(0).getLocation();
		int[] location2 = face.getVertex(1).getLocation();
		int[] location3 = face.getVertex(2).getLocation();

		color = face.getMaterial().getColor();

		if (!graphicsProcessor.isBackface(location1, location2, location3) && graphicsProcessor.isInsideFrustum(location1, location2, location3, camera.getFrustum())) {
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
		int lightFactor = graphicsProcessor.interpolate(lightFactors, barycentric);
		int r = graphicsProcessor.interpolate(lightColorR, barycentric);
		int g = graphicsProcessor.interpolate(lightColorG, barycentric);
		int b = graphicsProcessor.interpolate(lightColorB, barycentric);
		int lightColor = colorProcessor.generate(r, g, b);
		if (texture != null) {
			int u = graphicsProcessor.interpolate(uvX, barycentric);
			int v = graphicsProcessor.interpolate(uvY, barycentric);
			int texel = texture.getPixel(u, v);
			if (colorProcessor.getAlpha(texel) == 0) // discard pixel if alpha = 0
				return;
			modelColor = colorProcessor.lerp(ColorProcessor.BLACK, texel, lightFactor);
			modelColor = colorProcessor.multiplyColor(modelColor, lightColor);
		} else {
			modelColor = colorProcessor.lerp(ColorProcessor.BLACK, color, lightFactor);
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
		return mathProcessor.multiply(diffuseFactor + specularFactor, 100);
	}
}
