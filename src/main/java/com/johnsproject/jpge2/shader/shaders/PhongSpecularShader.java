package com.johnsproject.jpge2.shader.shaders;

import java.util.List;

import com.johnsproject.jpge2.dto.Camera;
import com.johnsproject.jpge2.dto.Face;
import com.johnsproject.jpge2.dto.FrameBuffer;
import com.johnsproject.jpge2.dto.Light;
import com.johnsproject.jpge2.dto.Texture;
import com.johnsproject.jpge2.dto.Vertex;
import com.johnsproject.jpge2.processor.CentralProcessor;
import com.johnsproject.jpge2.processor.ColorProcessor;
import com.johnsproject.jpge2.processor.GraphicsProcessor;
import com.johnsproject.jpge2.processor.MathProcessor;
import com.johnsproject.jpge2.processor.MatrixProcessor;
import com.johnsproject.jpge2.processor.VectorProcessor;
import com.johnsproject.jpge2.shader.Shader;
import com.johnsproject.jpge2.shader.ShaderDataBuffer;
import com.johnsproject.jpge2.shader.databuffers.ForwardDataBuffer;
import com.johnsproject.jpge2.shader.properties.SpecularShaderProperties;

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
	private final int[] portedCanvas;

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

	private int modelColor;
	private Texture texture;

	private Camera camera;
	private List<Light> lights;
	private FrameBuffer frameBuffer;
	private ForwardDataBuffer shaderData;
	private SpecularShaderProperties shaderProperties;

	public PhongSpecularShader(CentralProcessor centralProcessor) {
		super(centralProcessor, 19);
		this.mathProcessor = centralProcessor.getMathProcessor();
		this.matrixProcessor = centralProcessor.getMatrixProcessor();
		this.vectorProcessor = centralProcessor.getVectorProcessor();
		this.colorProcessor = centralProcessor.getColorProcessor();
		this.graphicsProcessor = centralProcessor.getGraphicsProcessor();

		this.viewMatrix = matrixProcessor.generate();
		this.projectionMatrix = matrixProcessor.generate();

		this.uvX = getVariable(0);
		this.uvY = getVariable(1);
		
		this.viewDirectionX = getVariable(2);
		this.viewDirectionY = getVariable(3);
		this.viewDirectionZ = getVariable(4);
		this.locationX = getVariable(5);
		this.locationY = getVariable(6);
		this.locationZ = getVariable(7);
		this.normalX = getVariable(8);
		this.normalY = getVariable(9);
		this.normalZ = getVariable(10);

		this.directionalLocation = getVariable(11);
		this.directionalLocationX = getVariable(12);
		this.directionalLocationY = getVariable(13);
		this.directionalLocationZ = getVariable(14);

		this.spotLocation = getVariable(15);
		this.spotLocationX = getVariable(16);
		this.spotLocationY = getVariable(17);
		this.spotLocationZ = getVariable(18);

		this.fragmentLocation = vectorProcessor.generate();
		this.normalizedNormal = vectorProcessor.generate();
		this.lightLocation = vectorProcessor.generate();
		this.lightDirection = vectorProcessor.generate();
		this.viewDirection = vectorProcessor.generate();
		this.portedCanvas = vectorProcessor.generate();
	}

	@Override
	public void update(ShaderDataBuffer shaderDataBuffer) {
		this.shaderData = (ForwardDataBuffer) shaderDataBuffer;
		this.lights = shaderData.getLights();
		this.frameBuffer = shaderData.getFrameBuffer();
		frameBuffer.getColorBuffer().fill(0);
		frameBuffer.getDepthBuffer().fill(Integer.MAX_VALUE);
	}

	@Override
	public void setup(Camera camera) {
		this.camera = camera;

		matrixProcessor.copy(viewMatrix, MatrixProcessor.MATRIX_IDENTITY);
		matrixProcessor.copy(projectionMatrix, MatrixProcessor.MATRIX_IDENTITY);

		graphicsProcessor.getViewMatrix(camera.getTransform(), viewMatrix);

		graphicsProcessor.portCanvas(camera.getCanvas(), frameBuffer.getSize(), portedCanvas);
		
		switch (camera.getType()) {
		case ORTHOGRAPHIC:
			graphicsProcessor.getOrthographicMatrix(portedCanvas, camera.getFrustum(), projectionMatrix);
			break;

		case PERSPECTIVE:
			graphicsProcessor.getPerspectiveMatrix(portedCanvas, camera.getFrustum(), projectionMatrix);
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
			graphicsProcessor.viewport(directionalLocation, shaderData.getDirectionalLightCanvas(), directionalLocation);
			directionalLocationX[index] = directionalLocation[VECTOR_X];
			directionalLocationY[index] = directionalLocation[VECTOR_Y];
			directionalLocationZ[index] = directionalLocation[VECTOR_Z];
		}

		if (shaderData.getSpotLightMatrix() != null) {
			vectorProcessor.multiply(location, shaderData.getSpotLightMatrix(), spotLocation);
			graphicsProcessor.viewport(spotLocation, shaderData.getSpotLightCanvas(), spotLocation);
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
		graphicsProcessor.viewport(location, portedCanvas, location);
	}

	@Override
	public void geometry(Face face) {
		int[] location1 = face.getVertex(0).getLocation();
		int[] location2 = face.getVertex(1).getLocation();
		int[] location3 = face.getVertex(2).getLocation();

		this.shaderProperties = (SpecularShaderProperties)face.getMaterial().getProperties();

		if (!graphicsProcessor.isBackface(location1, location2, location3)
				&& graphicsProcessor.isInsideFrustum(location1, location2, location3, portedCanvas, camera.getFrustum())) {
			texture = shaderProperties.getTexture();
			// set uv values that will be interpolated and fit uv into texture resolution
			if (texture != null) {
				int width = texture.getSize()[0] - 1;
				int height = texture.getSize()[1] - 1;
				uvX[0] = mathProcessor.multiply(face.getUV1()[VECTOR_X], width);
				uvX[1] = mathProcessor.multiply(face.getUV2()[VECTOR_X], width);
				uvX[2] = mathProcessor.multiply(face.getUV3()[VECTOR_X], width);
				uvY[0] = mathProcessor.multiply(face.getUV1()[VECTOR_Y], height);
				uvY[1] = mathProcessor.multiply(face.getUV2()[VECTOR_Y], height);
				uvY[2] = mathProcessor.multiply(face.getUV3()[VECTOR_Y], height);
			}
			graphicsProcessor.drawTriangle(location1, location2, location3, portedCanvas, this);
		}
	}

	@Override
	public void fragment(int[] location, int[] barycentric) {		
		directionalLocation[VECTOR_X] = directionalLocationX[3];
		directionalLocation[VECTOR_Y] = directionalLocationY[3];
		directionalLocation[VECTOR_Z] = directionalLocationZ[3];

		spotLocation[VECTOR_X] = spotLocationX[3];
		spotLocation[VECTOR_Y] = spotLocationY[3];
		spotLocation[VECTOR_Z] = spotLocationZ[3];

		viewDirection[VECTOR_X] = viewDirectionX[3];
		viewDirection[VECTOR_Y] = viewDirectionY[3];
		viewDirection[VECTOR_Z] = viewDirectionZ[3];

		fragmentLocation[VECTOR_X] = locationX[3];
		fragmentLocation[VECTOR_Y] = locationY[3];
		fragmentLocation[VECTOR_Z] = locationZ[3];

		normalizedNormal[VECTOR_X] = normalX[3];
		normalizedNormal[VECTOR_Y] = normalY[3];
		normalizedNormal[VECTOR_Z] = normalZ[3];

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
				if (vectorProcessor.distance(cameraLocation, lightPosition) > shaderData.getLightRange())
					continue;
				vectorProcessor.invert(light.getDirection(), lightDirection);
				currentFactor = getLightFactor(normalizedNormal, lightDirection, viewDirection, shaderProperties);
				break;
			case POINT:
				if (vectorProcessor.distance(cameraLocation, lightPosition) > shaderData.getLightRange())
					continue;
				vectorProcessor.subtract(lightPosition, fragmentLocation, lightLocation);
				// attenuation
				attenuation = getAttenuation(lightLocation);
				vectorProcessor.normalize(lightLocation, lightLocation);
				// other light values
				currentFactor = getLightFactor(normalizedNormal, lightLocation, viewDirection, shaderProperties);
				currentFactor = (currentFactor << 8) / attenuation;
				break;
			case SPOT:
				vectorProcessor.invert(light.getDirection(), lightDirection);
				if (vectorProcessor.distance(cameraLocation, lightPosition) > shaderData.getLightRange())
					continue;
				vectorProcessor.subtract(lightPosition, fragmentLocation, lightLocation);
				// attenuation
				attenuation = getAttenuation(lightLocation);
				vectorProcessor.normalize(lightLocation, lightLocation);
				int theta = vectorProcessor.dotProduct(lightLocation, lightDirection);
				int phi = mathProcessor.cos(light.getSpotSize() >> 1);
				if (theta > phi) {
					int intensity = -mathProcessor.divide(phi - theta, light.getSpotSoftness() + 1);
					intensity = mathProcessor.clamp(intensity, 1, FP_ONE);
					currentFactor = getLightFactor(normalizedNormal, lightLocation, viewDirection, shaderProperties);
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
			if (inShadow) {
				lightColor = colorProcessor.lerp(lightColor, light.getShadowColor(), 128);
			} else {
				lightColor = colorProcessor.lerp(lightColor, light.getColor(), currentFactor);
				lightFactor += currentFactor;
			}
		}
		if (texture != null) {
			modelColor = texture.getPixel(uvX[3], uvY[3]);
			if (colorProcessor.getAlpha(modelColor) == 0) // discard pixel if alpha = 0
				return;
		} else {
			modelColor = shaderProperties.getDiffuseColor();
		}
		modelColor = colorProcessor.lerp(ColorProcessor.BLACK, modelColor, lightFactor);
		modelColor = colorProcessor.multiplyColor(modelColor, lightColor);
		Texture colorBuffer = frameBuffer.getColorBuffer();
		Texture depthBuffer = frameBuffer.getDepthBuffer();
		if (depthBuffer.getPixel(location[VECTOR_X], location[VECTOR_Y]) > location[VECTOR_Z]) {
			depthBuffer.setPixel(location[VECTOR_X], location[VECTOR_Y], location[VECTOR_Z]);
			colorBuffer.setPixel(location[VECTOR_X], location[VECTOR_Y], modelColor);
		}
	}

	private int getLightFactor(int[] normal, int[] lightDirection, int[] viewDirection, SpecularShaderProperties properties) {
		// diffuse
		int dotProduct = vectorProcessor.dotProduct(normal, lightDirection);
		int diffuseFactor = Math.max(dotProduct, 0);
		diffuseFactor = mathProcessor.multiply(diffuseFactor, properties.getDiffuseIntensity());
		// specular
		vectorProcessor.invert(lightDirection, lightDirection);
		vectorProcessor.reflect(lightDirection, normal, lightDirection);
		dotProduct = vectorProcessor.dotProduct(viewDirection, lightDirection);
		int specularFactor = Math.max(dotProduct, 0);
		specularFactor = mathProcessor.pow(specularFactor, properties.getShininess() >> FP_BITS);
		specularFactor = mathProcessor.multiply(specularFactor, properties.getSpecularIntensity());
		// putting it all together...
		return (diffuseFactor + specularFactor << 8) >> FP_BITS;
	}

	private int getAttenuation(int[] lightLocation) {
		// attenuation
		long distance = vectorProcessor.magnitude(lightLocation);
		int attenuation = shaderData.getConstantAttenuation();
		attenuation += mathProcessor.multiply(distance, shaderData.getLinearAttenuation());
		attenuation += mathProcessor.multiply(mathProcessor.multiply(distance, distance),
				shaderData.getQuadraticAttenuation());
		attenuation >>= FP_BITS;
		return ((attenuation << 8) >> FP_BITS) + 1;
	}

	private boolean inShadow(int[] lightSpaceLocation, Texture shadowMap) {
		int x = lightSpaceLocation[VECTOR_X];
		int y = lightSpaceLocation[VECTOR_Y];
		x = mathProcessor.clamp(x, 0, shadowMap.getSize()[0] - 1);
		y = mathProcessor.clamp(y, 0, shadowMap.getSize()[1] - 1);
		int depth = shadowMap.getPixel(x, y);
		// int color = (depth + 100) >> 5;
		// color = colorProcessor.generate(color, color, color);
		// frameBuffer.setPixel(x, y, depth - 1000, (byte) 0, color);
		int bias = 50;
		return depth < lightSpaceLocation[VECTOR_Z] - bias;
	}
}
