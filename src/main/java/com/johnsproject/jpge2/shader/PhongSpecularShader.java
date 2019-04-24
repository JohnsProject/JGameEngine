package com.johnsproject.jpge2.shader;

import java.util.List;

import com.johnsproject.jpge2.dto.Camera;
import com.johnsproject.jpge2.dto.Face;
import com.johnsproject.jpge2.dto.FrameBuffer;
import com.johnsproject.jpge2.dto.Light;
import com.johnsproject.jpge2.dto.Material;
import com.johnsproject.jpge2.dto.ShaderData;
import com.johnsproject.jpge2.dto.Texture;
import com.johnsproject.jpge2.dto.Vertex;
import com.johnsproject.jpge2.processor.CentralProcessor;
import com.johnsproject.jpge2.processor.ColorProcessor;
import com.johnsproject.jpge2.processor.GraphicsProcessor;
import com.johnsproject.jpge2.processor.MathProcessor;
import com.johnsproject.jpge2.processor.MatrixProcessor;
import com.johnsproject.jpge2.processor.VectorProcessor;

public class PhongSpecularShader extends Shader {

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

	private final int[][] viewMatrix;
	private final int[][] projectionMatrix;
	
	private final int[] uvX;
	private final int[] uvY;

	private final int[] fragmentLocation;
	private final int[] normalizedNormal;
	private final int[] lightLocation;
	private final int[] lightDirection;
	private final int[] viewDirection;

	private final int[] viewDirectionX;
	private final int[] viewDirectionY;
	private final int[] viewDirectionZ;
	private final int[] locationX;
	private final int[] locationY;
	private final int[] locationZ;
	private final int[] normalX;
	private final int[] normalY;
	private final int[] normalZ;

	private final int[] directionalLocation;
	private final int[] directionalLocationX;
	private final int[] directionalLocationY;
	private final int[] directionalLocationZ;
	
	private final int[] spotLocation;
	private final int[] spotLocationX;
	private final int[] spotLocationY;
	private final int[] spotLocationZ;

	private Material material;
	private int modelColor;
	private Texture texture;

	private Camera camera;	
	private List<Light> lights;
	private FrameBuffer frameBuffer;
	private ShaderData shaderData;

	public PhongSpecularShader(CentralProcessor centralProcessor) {
		super(centralProcessor);
		this.mathProcessor = centralProcessor.getMathProcessor();
		this.matrixProcessor = centralProcessor.getMatrixProcessor();
		this.vectorProcessor = centralProcessor.getVectorProcessor();
		this.colorProcessor = centralProcessor.getColorProcessor();
		this.graphicsProcessor = centralProcessor.getGraphicsProcessor();

		this.viewMatrix = matrixProcessor.generate();
		this.projectionMatrix = matrixProcessor.generate();

		this.uvX = vectorProcessor.generate();
		this.uvY = vectorProcessor.generate();

		this.fragmentLocation = vectorProcessor.generate();
		this.normalizedNormal = vectorProcessor.generate();
		this.lightLocation = vectorProcessor.generate();
		this.lightDirection = vectorProcessor.generate();
		this.viewDirection = vectorProcessor.generate();

		this.viewDirectionX = vectorProcessor.generate();
		this.viewDirectionY = vectorProcessor.generate();
		this.viewDirectionZ = vectorProcessor.generate();
		this.locationX = vectorProcessor.generate();
		this.locationY = vectorProcessor.generate();
		this.locationZ = vectorProcessor.generate();
		this.normalX = vectorProcessor.generate();
		this.normalY = vectorProcessor.generate();
		this.normalZ = vectorProcessor.generate();

		this.directionalLocation = vectorProcessor.generate();
		this.directionalLocationX = vectorProcessor.generate();
		this.directionalLocationY = vectorProcessor.generate();
		this.directionalLocationZ = vectorProcessor.generate();
		
		this.spotLocation = vectorProcessor.generate();
		this.spotLocationX = vectorProcessor.generate();
		this.spotLocationY = vectorProcessor.generate();
		this.spotLocationZ = vectorProcessor.generate();
	}

	@Override
	public void update(ShaderDataBuffer shaderDataBuffer) {
		this.shaderData = (ShaderData)shaderDataBuffer;		
		this.lights = shaderData.getLights();
		this.frameBuffer = shaderData.getFrameBuffer();
		frameBuffer.clearColorBuffer();
		frameBuffer.clearDepthBuffer();
	}

	@Override
	public void setup(Camera camera) {
		this.camera = camera;
		graphicsProcessor.setup(frameBuffer.getSize(), camera.getCanvas(), this);
		
		matrixProcessor.copy(viewMatrix, MatrixProcessor.MATRIX_IDENTITY);
		matrixProcessor.copy(projectionMatrix, MatrixProcessor.MATRIX_IDENTITY);
		
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
		int[] normal = vertex.getNormal();

		locationX[index] = location[VECTOR_X];
		locationY[index] = location[VECTOR_Y];
		locationZ[index] = location[VECTOR_Z];
		
		vectorProcessor.normalize(normal, normalizedNormal);
		normalX[index] = normalizedNormal[VECTOR_X];
		normalY[index] = normalizedNormal[VECTOR_Y];
		normalZ[index] = normalizedNormal[VECTOR_Z];

		if (shaderData.getDirectionalLightMatrix() != null) {
			vectorProcessor.multiply(location, shaderData.getDirectionalLightMatrix(), directionalLocation);
			graphicsProcessor.setup(shaderData.getDirectionalShadowMap().getSize(), camera.getCanvas(), this);
			graphicsProcessor.viewport(directionalLocation, directionalLocation);
			graphicsProcessor.setup(frameBuffer.getSize(), camera.getCanvas(), this);
			directionalLocationX[index] = directionalLocation[VECTOR_X];
			directionalLocationY[index] = directionalLocation[VECTOR_Y];
			directionalLocationZ[index] = directionalLocation[VECTOR_Z];
		}
		
		if (shaderData.getSpotLightMatrix() != null) {
			vectorProcessor.multiply(location, shaderData.getSpotLightMatrix(), spotLocation);
			graphicsProcessor.setup(shaderData.getSpotShadowMap().getSize(), camera.getCanvas(), this);
			graphicsProcessor.viewport(spotLocation, spotLocation);
			graphicsProcessor.setup(frameBuffer.getSize(), camera.getCanvas(), this);
			spotLocationX[index] = spotLocation[VECTOR_X];
			spotLocationY[index] = spotLocation[VECTOR_Y];
			spotLocationZ[index] = spotLocation[VECTOR_Z];
		}

		vectorProcessor.subtract(camera.getTransform().getLocation(), location, viewDirection);
		vectorProcessor.normalize(viewDirection, viewDirection);
		viewDirectionX[index] = viewDirection[VECTOR_X];
		viewDirectionY[index] = viewDirection[VECTOR_Y];
		viewDirectionZ[index] = viewDirection[VECTOR_Z];

		vectorProcessor.multiply(location, viewMatrix, location);
		vectorProcessor.multiply(location, projectionMatrix, location);
		graphicsProcessor.viewport(location, location);
	}

	@Override
	public void geometry(Face face) {
		int[] location1 = face.getVertex(0).getLocation();
		int[] location2 = face.getVertex(1).getLocation();
		int[] location3 = face.getVertex(2).getLocation();

		material = face.getMaterial();

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

		directionalLocation[VECTOR_X] = graphicsProcessor.interpolate(directionalLocationX, barycentric);
		directionalLocation[VECTOR_Y] = graphicsProcessor.interpolate(directionalLocationY, barycentric);
		directionalLocation[VECTOR_Z] = graphicsProcessor.interpolate(directionalLocationZ, barycentric);
		
		spotLocation[VECTOR_X] = graphicsProcessor.interpolate(spotLocationX, barycentric);
		spotLocation[VECTOR_Y] = graphicsProcessor.interpolate(spotLocationY, barycentric);
		spotLocation[VECTOR_Z] = graphicsProcessor.interpolate(spotLocationZ, barycentric);

		viewDirection[VECTOR_X] = graphicsProcessor.interpolate(viewDirectionX, barycentric);
		viewDirection[VECTOR_Y] = graphicsProcessor.interpolate(viewDirectionY, barycentric);
		viewDirection[VECTOR_Z] = graphicsProcessor.interpolate(viewDirectionZ, barycentric);
		
		fragmentLocation[VECTOR_X] = graphicsProcessor.interpolate(locationX, barycentric);
		fragmentLocation[VECTOR_Y] = graphicsProcessor.interpolate(locationY, barycentric);
		fragmentLocation[VECTOR_Z] = graphicsProcessor.interpolate(locationZ, barycentric);

		normalizedNormal[VECTOR_X] = graphicsProcessor.interpolate(normalX, barycentric);
		normalizedNormal[VECTOR_Y] = graphicsProcessor.interpolate(normalY, barycentric);
		normalizedNormal[VECTOR_Z] = graphicsProcessor.interpolate(normalZ, barycentric);

		int lightColor = ColorProcessor.WHITE;
		int lightFactor = 0;

		int[] cameraLocation = camera.getTransform().getLocation();
		
		for (int i = 0; i < lights.size(); i++) {
			Light light = lights.get(i);
			int currentFactor = 0;
			int attenuation = 0;
			int[] lightPosition = light.getTransform().getLocation();
			switch (light.getType()) {
			case DIRECTIONAL:
				vectorProcessor.invert(light.getDirection(), lightDirection);
				currentFactor = getLightFactor(normalizedNormal, lightDirection, viewDirection, material);
				break;
			case POINT:
				lightPosition[VECTOR_X] = -lightPosition[VECTOR_X];
				if (vectorProcessor.distance(cameraLocation, lightPosition) > shaderData.getLightRange())
					continue;
				vectorProcessor.subtract(lightPosition, fragmentLocation, lightLocation);
				lightPosition[VECTOR_X] = -lightPosition[VECTOR_X];
				// attenuation
				attenuation = getAttenuation(lightLocation);
				vectorProcessor.normalize(lightLocation, lightLocation);
				// other light values
				currentFactor = getLightFactor(normalizedNormal, lightLocation, viewDirection, material);
				currentFactor = (currentFactor << 8) / attenuation;
				break;
			case SPOT:
				vectorProcessor.invert(light.getDirection(), lightDirection);
				lightPosition[VECTOR_X] = -lightPosition[VECTOR_X];
				if (vectorProcessor.distance(cameraLocation, lightPosition) > shaderData.getLightRange())
					continue;
				vectorProcessor.subtract(lightPosition, fragmentLocation, lightLocation);
				lightPosition[VECTOR_X] = -lightPosition[VECTOR_X];
				// attenuation
				attenuation = getAttenuation(lightLocation);
				vectorProcessor.normalize(lightLocation, lightLocation);
				int theta = vectorProcessor.dotProduct(lightLocation, lightDirection);
				int phi = mathProcessor.cos(light.getSpotSize() >> 1);
				if (theta > phi) {
					int intensity = -mathProcessor.divide(phi - theta, light.getSpotSoftness() + 1);
					intensity = mathProcessor.clamp(intensity, 1, FP_ONE);
					currentFactor = getLightFactor(normalizedNormal, lightLocation, viewDirection, material);
					currentFactor = (currentFactor * intensity) / attenuation;
				}
				break;
			}
			currentFactor = mathProcessor.multiply(currentFactor, light.getStrength());
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
				lightColor = colorProcessor.lerp(lightColor, light.getShadowColor(), 128);
			} else {
				lightColor = colorProcessor.lerp(lightColor, light.getColor(), currentFactor);
				lightFactor += currentFactor;
			}
		}
		if (texture != null) {
			int u = graphicsProcessor.interpolate(uvX, barycentric);
			int v = graphicsProcessor.interpolate(uvY, barycentric);
			modelColor = texture.getPixel(u, v);
			if (colorProcessor.getAlpha(modelColor) == 0) // discard pixel if alpha = 0
				return;
		} else {
			modelColor = material.getColor();
		}
		modelColor = colorProcessor.lerp(ColorProcessor.BLACK, modelColor, lightFactor);
		modelColor = colorProcessor.multiplyColor(modelColor, lightColor);
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
		int attenuation = shaderData.getConstantAttenuation();
		attenuation += mathProcessor.multiply(distance, shaderData.getLinearAttenuation());
		attenuation += mathProcessor.multiply(mathProcessor.multiply(distance, distance), shaderData.getQuadraticAttenuation());
		attenuation >>= FP_BITS;
		return ((attenuation << 8) >> FP_BITS) + 1;
	}

	private boolean inShadow(int[] lightSpaceLocation, FrameBuffer shadowMap) {
		int x = lightSpaceLocation[VECTOR_X];
		int y = lightSpaceLocation[VECTOR_Y];
		x = mathProcessor.clamp(x, 0, shadowMap.getSize()[0] - 1);
		y = mathProcessor.clamp(y, 0, shadowMap.getSize()[1] - 1);
		int depth = shadowMap.getDepth(x, y);
		int bias = 50;
		return depth < lightSpaceLocation[VECTOR_Z] - bias;
	}
}
