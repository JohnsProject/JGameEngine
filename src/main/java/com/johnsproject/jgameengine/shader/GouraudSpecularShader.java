package com.johnsproject.jgameengine.shader;

import static com.johnsproject.jgameengine.math.FixedPointMath.FP_ONE;
import static com.johnsproject.jgameengine.math.VectorMath.VECTOR_X;
import static com.johnsproject.jgameengine.math.VectorMath.VECTOR_Y;
import static com.johnsproject.jgameengine.math.VectorMath.VECTOR_Z;

import com.johnsproject.jgameengine.math.ColorMath;
import com.johnsproject.jgameengine.math.FixedPointMath;
import com.johnsproject.jgameengine.math.TransformationMath;
import com.johnsproject.jgameengine.math.VectorMath;
import com.johnsproject.jgameengine.model.Face;
import com.johnsproject.jgameengine.model.Fragment;
import com.johnsproject.jgameengine.model.Light;
import com.johnsproject.jgameengine.model.Texture;
import com.johnsproject.jgameengine.model.Vertex;
import com.johnsproject.jgameengine.rasterizer.PerspectiveGouraudRasterizer;

public class GouraudSpecularShader  implements Shader {

	private static final int INITIAL_ATTENUATION = FP_ONE;
	private static final int LINEAR_ATTENUATION = FixedPointMath.toFixedPoint(0.045);
	private static final int QUADRATIC_ATTENUATION = FixedPointMath.toFixedPoint(0.0075);
	
	private SpecularProperties shaderProperties;
	private ForwardShaderBuffer shaderBuffer;
	private final PerspectiveGouraudRasterizer rasterizer;

	private final int[] lightDirection;
	private final int[] lightLocation;
	private final int[] viewDirection;
	private final int[] lightSpaceLocation;

	public GouraudSpecularShader() {
		this.rasterizer = new PerspectiveGouraudRasterizer(this);
		this.shaderProperties = new SpecularProperties();
		this.lightDirection = VectorMath.emptyVector();
		this.lightLocation = VectorMath.emptyVector();
		this.viewDirection = VectorMath.emptyVector();
		this.lightSpaceLocation = VectorMath.emptyVector();
	}

	public void vertex(Vertex vertex) {
		int[] location = vertex.getLocation();
		int[] normal = vertex.getWorldNormal();
		int lightColor = ColorMath.BLACK;
		int[] cameraLocation = shaderBuffer.getCamera().getTransform().getLocation();
		VectorMath.copy(location, vertex.getWorldLocation());
		VectorMath.normalize(normal);
		VectorMath.copy(viewDirection, cameraLocation);
		VectorMath.subtract(viewDirection, location);
		VectorMath.normalize(viewDirection);
		int lightIndex = 0;
		for(int i = 0; i < shaderBuffer.getLights().size(); i++) {
			Light light = shaderBuffer.getLights().get(i);
			if(light.isCulled())
				continue;
			int currentFactor = 0;
			int attenuation = 0;
			int[] lightPosition = light.getTransform().getLocation();
			switch (light.getType()) {
			case DIRECTIONAL:
				VectorMath.copy(lightDirection, light.getDirection());
				VectorMath.invert(lightDirection);
				currentFactor = getLightFactor(normal, lightDirection, viewDirection);
				if (lightIndex == shaderBuffer.getDirectionalLightIndex()) {
					int[][] lightMatrix = shaderBuffer.getDirectionalLightMatrix();
					int[] lightFrustum = shaderBuffer.getDirectionalLightFrustum();
					Texture shadowMap = shaderBuffer.getDirectionalShadowMap();
					if(inShadow(location, lightMatrix, lightFrustum, shadowMap)) {
						currentFactor = ColorMath.multiplyColor(currentFactor, light.getShadowColor());
					}
				}
				break;
			case POINT:
				VectorMath.copy(lightLocation, lightPosition);
				VectorMath.subtract(lightLocation, location);
				attenuation = getAttenuation(lightLocation);
				VectorMath.normalize(lightLocation);
				currentFactor = getLightFactor(normal, lightLocation, viewDirection);
				currentFactor = FixedPointMath.divide(currentFactor, attenuation);
				if ((lightIndex == shaderBuffer.getPointLightIndex()) && (currentFactor > 150)) {
					for (int j = 0; j < shaderBuffer.getPointLightMatrices().length; j++) {
						int[][] lightMatrix = shaderBuffer.getPointLightMatrices()[j];
						int[] lightFrustum = shaderBuffer.getPointLightFrustum();
						Texture shadowMap = shaderBuffer.getPointShadowMaps()[j];
						if(inShadow(location, lightMatrix, lightFrustum, shadowMap)) {
							currentFactor = ColorMath.multiplyColor(currentFactor, light.getShadowColor());
						}
					}
				}
				break;
			case SPOT:				
				VectorMath.copy(lightDirection, light.getDirection());
				VectorMath.copy(lightLocation, lightPosition);
				VectorMath.invert(lightDirection);
				VectorMath.subtract(lightLocation, location);
				attenuation = getAttenuation(lightLocation);
				VectorMath.normalize(lightLocation);
				long theta = VectorMath.dotProduct(lightLocation, lightDirection);
				int phi = FixedPointMath.cos(light.getSpotSize() >> 1);
				if(theta > phi) {
					int intensity = -FixedPointMath.divide(phi - theta, light.getSpotSoftness() + 1);
					intensity = FixedPointMath.clamp(intensity, 1, FP_ONE);
					currentFactor = getLightFactor(normal, lightDirection, viewDirection);
					currentFactor = FixedPointMath.multiply(currentFactor, intensity * 2);
					currentFactor = FixedPointMath.divide(currentFactor, attenuation);
					if ((lightIndex == shaderBuffer.getSpotLightIndex()) && (currentFactor > 10)) {
						int[][] lightMatrix = shaderBuffer.getSpotLightMatrix();
						int[] lightFrustum = shaderBuffer.getSpotLightFrustum();
						Texture shadowMap = shaderBuffer.getSpotShadowMap();
						if(inShadow(location, lightMatrix, lightFrustum, shadowMap)) {
							currentFactor = ColorMath.multiplyColor(currentFactor, light.getShadowColor());
						}
					}
				}
				break;
			}
			currentFactor = FixedPointMath.multiply(currentFactor, light.getStrength());
			currentFactor = FixedPointMath.multiply(currentFactor, 255);
			lightColor = ColorMath.lerp(lightColor, light.getColor(), currentFactor);
			lightIndex++;
		}
		vertex.setShadedColor(lightColor);
		VectorMath.multiply(location, shaderBuffer.getCamera().getTransform().getSpaceEnterMatrix());
		VectorMath.multiply(location, shaderBuffer.getCamera().getProjectionMatrix());
		TransformationMath.screenportVector(location, shaderBuffer.getCamera().getRenderTargetPortedFrustum());
	}

	public void geometry(Face face) {
		Texture texture = shaderProperties.getTexture();
		if (texture == null) {
			rasterizer.draw(face);
		} else {
			rasterizer.perspectiveDraw(face, texture);
		}
	}

	public void fragment(Fragment fragment) {
		Texture depthBuffer = shaderBuffer.getCamera().getRenderTarget().getDepthBuffer();
		Texture colorBuffer = shaderBuffer.getCamera().getRenderTarget().getColorBuffer();
		final int x = fragment.getLocation()[VECTOR_X];
		final int y = fragment.getLocation()[VECTOR_Y];
		final int z = fragment.getLocation()[VECTOR_Z];
		if (depthBuffer.getPixel(x, y) > z) {
			int color = shaderProperties.getDiffuseColor();
			Texture texture = shaderProperties.getTexture();
			int lightColor = fragment.getColor();
			if (texture != null) {
				int[] uv = fragment.getUV();
				int texel = texture.getPixel(uv[VECTOR_X], uv[VECTOR_Y]);
				if (ColorMath.getAlpha(texel) == 0) // discard pixel if alpha = 0
					return;
				color = texel;
			}
			color = ColorMath.multiplyColor(color, lightColor);
			depthBuffer.setPixel(x, y, z);
			colorBuffer.setPixel(x, y, color);
		}
	}

	private int getLightFactor(int[] normal, int[] lightDirection, int[] viewDirection) {
		// diffuse
		int dotProduct = (int)VectorMath.dotProduct(normal, lightDirection);
		int diffuseFactor = Math.max(dotProduct, 0);
		diffuseFactor = FixedPointMath.multiply(diffuseFactor, shaderProperties.getDiffuseIntensity());
		// specular
		VectorMath.invert(lightDirection);
		TransformationMath.reflect(lightDirection, normal);
		dotProduct = (int)VectorMath.dotProduct(viewDirection, lightDirection);
		int specularFactor = Math.max(dotProduct, 0);
		specularFactor = FixedPointMath.pow(specularFactor, shaderProperties.getShininess());
		specularFactor = FixedPointMath.multiply(specularFactor, shaderProperties.getSpecularIntensity());
		// putting it all together...
		return diffuseFactor + specularFactor;
	}
	
	private int getAttenuation(int[] lightLocation) {
		// attenuation
		int distance = VectorMath.length(lightLocation);
		int attenuation = INITIAL_ATTENUATION;
		attenuation += FixedPointMath.multiply(distance, LINEAR_ATTENUATION);
		attenuation += FixedPointMath.multiply(FixedPointMath.multiply(distance, distance), QUADRATIC_ATTENUATION);
		return attenuation + 1;
	}
	
	private boolean inShadow(int[] location, int[][] lightMatrix, int[] lightFrustum, Texture shadowMap) {
		VectorMath.copy(lightSpaceLocation, location);
		VectorMath.multiply(lightSpaceLocation, lightMatrix);
		TransformationMath.screenportVector(lightSpaceLocation, lightFrustum);
		int x = lightSpaceLocation[VECTOR_X];
		int y = lightSpaceLocation[VECTOR_Y];
		int depth = shadowMap.getPixel(x, y);
		return depth < lightSpaceLocation[VECTOR_Z];
	}

	public ShaderBuffer getShaderBuffer() {
		return shaderBuffer;
	}

	public void setShaderBuffer(ShaderBuffer shaderBuffer) {
		this.shaderBuffer = (ForwardShaderBuffer) shaderBuffer;
	}

	public void setProperties(ShaderProperties shaderProperties) {
		this.shaderProperties = (SpecularProperties) shaderProperties;
	}

	public ShaderProperties getProperties() {
		return shaderProperties;
	}
}
