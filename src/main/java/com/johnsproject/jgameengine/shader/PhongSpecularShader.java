package com.johnsproject.jgameengine.shader;

import com.johnsproject.jgameengine.library.ColorLibrary;
import com.johnsproject.jgameengine.library.MathLibrary;
import com.johnsproject.jgameengine.model.GeometryBuffer;
import com.johnsproject.jgameengine.model.Light;
import com.johnsproject.jgameengine.model.Texture;
import com.johnsproject.jgameengine.model.VertexBuffer;
import com.johnsproject.jgameengine.rasterizer.PerspectivePhongRasterizer;

public class PhongSpecularShader extends Shader {

	private static final int INITIAL_ATTENUATION = MathLibrary.FP_ONE;
	private static final int LINEAR_ATTENUATION = 14;
	private static final int QUADRATIC_ATTENUATION = 7;
	
	private static final int LIGHT_RANGE = MathLibrary.FP_ONE * 150;

	private SpecularProperties shaderProperties;
	private ForwardShaderBuffer shaderBuffer;
	private final PerspectivePhongRasterizer rasterizer;
	
	private final int[] lightDirection;
	private final int[] lightLocation;
	private final int[] viewDirection;
	private final int[] lightSpaceLocation;
	
	private int color;
	private int modelColor;
	private Texture texture;

	public PhongSpecularShader() {
		this.rasterizer = new PerspectivePhongRasterizer(this);
		this.shaderProperties = new SpecularProperties();
		this.lightDirection = vectorLibrary.generate();
		this.lightLocation = vectorLibrary.generate();
		this.viewDirection = vectorLibrary.generate();
		this.lightSpaceLocation = vectorLibrary.generate();
	}

	@Override
	public void vertex(VertexBuffer vertexBuffer) {
		int[] location = vertexBuffer.getLocation();
		int[] normal = vertexBuffer.getNormal();
		vectorLibrary.normalize(normal, normal);
		vectorLibrary.matrixMultiply(location, shaderBuffer.getViewMatrix(), location);
		vectorLibrary.matrixMultiply(location, shaderBuffer.getProjectionMatrix(), location);
		graphicsLibrary.screenportVector(location, shaderBuffer.getPortedFrustum(), location);
	}

	@Override
	public void geometry(GeometryBuffer geometryBuffer) {
		color = shaderProperties.getDiffuseColor();
		texture = shaderProperties.getTexture();
		VertexBuffer dataBuffer0 = geometryBuffer.getVertexDataBuffer(0);
		VertexBuffer dataBuffer1 = geometryBuffer.getVertexDataBuffer(1);
		VertexBuffer dataBuffer2 = geometryBuffer.getVertexDataBuffer(2);
		rasterizer.setLocation0(dataBuffer0.getLocation());
		rasterizer.setLocation1(dataBuffer1.getLocation());
		rasterizer.setLocation2(dataBuffer2.getLocation());
		rasterizer.setWorldLocation0(dataBuffer0.getWorldLocation());
		rasterizer.setWorldLocation1(dataBuffer1.getWorldLocation());
		rasterizer.setWorldLocation2(dataBuffer2.getWorldLocation());
		rasterizer.setNormal0(dataBuffer0.getNormal());
		rasterizer.setNormal1(dataBuffer1.getNormal());
		rasterizer.setNormal2(dataBuffer2.getNormal());
		if (texture == null) {
			graphicsLibrary.drawPhongTriangle(rasterizer, true, 1, shaderBuffer.getPortedFrustum());
		} else {
			rasterizer.setUV0(geometryBuffer.getUV(0), texture);
			rasterizer.setUV1(geometryBuffer.getUV(1), texture);
			rasterizer.setUV2(geometryBuffer.getUV(2), texture);
			graphicsLibrary.drawPerspectivePhongTriangle(rasterizer, true, 1, shaderBuffer.getPortedFrustum());
		}
	}

	@Override
	public void fragment(int[] location) {
		int x = location[VECTOR_X];
		int y = location[VECTOR_Y];
		int z = location[VECTOR_Z];
		int[] worldLocation = rasterizer.getWorldLocation();
		int[] normal = rasterizer.getNormal();
		int lightColor = ColorLibrary.BLACK;
		int[] cameraLocation = shaderBuffer.getCamera().getTransform().getLocation();	
		vectorLibrary.subtract(cameraLocation, worldLocation, viewDirection);
		vectorLibrary.normalize(viewDirection, viewDirection);
		for (int i = 0; i < shaderBuffer.getLights().size(); i++) {
			Light light = shaderBuffer.getLights().get(i);
			int currentFactor = 0;
			int attenuation = 0;
			int[] lightPosition = light.getTransform().getLocation();
			switch (light.getType()) {
			case DIRECTIONAL:
				vectorLibrary.invert(light.getDirection(), lightDirection);
				currentFactor = getLightFactor(normal, lightDirection, viewDirection);
				if (i == shaderBuffer.getDirectionalLightIndex()) {
					int[] lightMatrix = shaderBuffer.getDirectionalLightMatrix();
					int[] lightFrustum = shaderBuffer.getDirectionalLightFrustum();
					Texture shadowMap = shaderBuffer.getDirectionalShadowMap();
					if(inShadow(worldLocation, lightMatrix, lightFrustum, shadowMap)) {
						currentFactor = colorLibrary.multiplyColor(currentFactor, light.getShadowColor());
					}
				}
				break;
			case POINT:
				if (vectorLibrary.averagedDistance(cameraLocation, lightPosition) > LIGHT_RANGE)
					continue;
				vectorLibrary.subtract(lightPosition, worldLocation, lightLocation);
				attenuation = getAttenuation(lightLocation);
				vectorLibrary.normalize(lightLocation, lightLocation);
				currentFactor = getLightFactor(normal, lightLocation, viewDirection);
				currentFactor = mathLibrary.divide(currentFactor, attenuation);
				if ((i == shaderBuffer.getPointLightIndex()) && (currentFactor > 150)) {
					for (int j = 0; j < shaderBuffer.getPointLightMatrices().length; j++) {
						int[] lightMatrix = shaderBuffer.getPointLightMatrices()[j];
						int[] lightFrustum = shaderBuffer.getPointLightFrustum();
						Texture shadowMap = shaderBuffer.getPointShadowMaps()[j];
						if(inShadow(worldLocation, lightMatrix, lightFrustum, shadowMap)) {
							currentFactor = colorLibrary.multiplyColor(currentFactor, light.getShadowColor());
						}
					}
				}
				break;
			case SPOT:				
				if (vectorLibrary.averagedDistance(cameraLocation, lightPosition) > LIGHT_RANGE)
					continue;
				vectorLibrary.invert(light.getDirection(), lightDirection);
				vectorLibrary.subtract(lightPosition, worldLocation, lightLocation);
				attenuation = getAttenuation(lightLocation);
				vectorLibrary.normalize(lightLocation, lightLocation);
				int theta = vectorLibrary.dotProduct(lightLocation, lightDirection);
				int phi = mathLibrary.cos(light.getSpotSize() >> 1);
				if(theta > phi) {
					int intensity = -mathLibrary.divide(phi - theta, light.getSpotSoftness() + 1);
					intensity = mathLibrary.clamp(intensity, 1, FP_ONE);
					currentFactor = getLightFactor(normal, lightDirection, viewDirection);
					currentFactor = mathLibrary.multiply(currentFactor, intensity * 2);
					currentFactor = mathLibrary.divide(currentFactor, attenuation);
					if ((i == shaderBuffer.getSpotLightIndex()) && (currentFactor > 10)) {
						int[] lightMatrix = shaderBuffer.getSpotLightMatrix();
						int[] lightFrustum = shaderBuffer.getSpotLightFrustum();
						Texture shadowMap = shaderBuffer.getSpotShadowMap();
						if(inShadow(worldLocation, lightMatrix, lightFrustum, shadowMap)) {
							currentFactor = colorLibrary.multiplyColor(currentFactor, light.getShadowColor());
						}
					}
				}
				break;
			}
			currentFactor = mathLibrary.multiply(currentFactor, light.getStrength());
			currentFactor = mathLibrary.multiply(currentFactor, 255);
			lightColor = colorLibrary.lerp(lightColor, light.getColor(), currentFactor);
		}
		if (texture != null) {
			int[] uv = rasterizer.getUV();
			int texel = texture.getPixel(uv[VECTOR_X], uv[VECTOR_Y]);
			if (colorLibrary.getAlpha(texel) == 0) // discard pixel if alpha = 0
				return;
			color = texel;
		}
		modelColor = colorLibrary.multiplyColor(color, lightColor);
		Texture colorBuffer = shaderBuffer.getFrameBuffer().getColorBuffer();
		Texture depthBuffer = shaderBuffer.getFrameBuffer().getDepthBuffer();
		if (depthBuffer.getPixel(x, y) > z) {
			depthBuffer.setPixel(x, y, z);
			colorBuffer.setPixel(x, y, modelColor);
		}
	}

	private int getLightFactor(int[] normal, int[] lightDirection, int[] viewDirection) {
		// diffuse
		int dotProduct = vectorLibrary.dotProduct(normal, lightDirection);
		int diffuseFactor = Math.max(dotProduct, 0);
		diffuseFactor = mathLibrary.multiply(diffuseFactor, shaderProperties.getDiffuseIntensity());
		// specular
		vectorLibrary.invert(lightDirection, lightDirection);
		vectorLibrary.reflect(lightDirection, normal, lightDirection);
		dotProduct = vectorLibrary.dotProduct(viewDirection, lightDirection);
		int specularFactor = Math.max(dotProduct, 0);
		specularFactor = mathLibrary.pow(specularFactor, shaderProperties.getShininess());
		specularFactor = mathLibrary.multiply(specularFactor, shaderProperties.getSpecularIntensity());
		// putting it all together...
		return diffuseFactor + specularFactor;
	}
	
	private int getAttenuation(int[] lightLocation) {
		// attenuation
		int distance = vectorLibrary.length(lightLocation);
		int attenuation = INITIAL_ATTENUATION;
		attenuation += mathLibrary.multiply(distance, LINEAR_ATTENUATION);
		attenuation += mathLibrary.multiply(mathLibrary.multiply(distance, distance), QUADRATIC_ATTENUATION);
		return attenuation + 1;
	}
	
	private boolean inShadow(int[] location, int[] lightMatrix, int[] lightFrustum, Texture shadowMap) {
		vectorLibrary.matrixMultiply(location, lightMatrix, lightSpaceLocation);
		graphicsLibrary.screenportVector(lightSpaceLocation, lightFrustum, lightSpaceLocation);
		int x = lightSpaceLocation[VECTOR_X];
		int y = lightSpaceLocation[VECTOR_Y];
		int depth = shadowMap.getPixel(x, y);
		return depth < lightSpaceLocation[VECTOR_Z];
	}

	@Override
	public ShaderBuffer getShaderBuffer() {
		return shaderBuffer;
	}

	@Override
	public void setShaderBuffer(ShaderBuffer shaderBuffer) {
		this.shaderBuffer = (ForwardShaderBuffer) shaderBuffer;
	}

	@Override
	public void setProperties(ShaderProperties shaderProperties) {
		this.shaderProperties = (SpecularProperties) shaderProperties;
	}

	@Override
	public ShaderProperties getProperties() {
		return shaderProperties;
	}
}

