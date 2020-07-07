package com.johnsproject.jgameengine.shading;

import static com.johnsproject.jgameengine.util.FixedPointUtils.FP_ONE;
import static com.johnsproject.jgameengine.util.VectorUtils.VECTOR_X;
import static com.johnsproject.jgameengine.util.VectorUtils.VECTOR_Y;
import static com.johnsproject.jgameengine.util.VectorUtils.VECTOR_Z;

import com.johnsproject.jgameengine.model.Face;
import com.johnsproject.jgameengine.model.Fragment;
import com.johnsproject.jgameengine.model.Frustum;
import com.johnsproject.jgameengine.model.Light;
import com.johnsproject.jgameengine.model.Material;
import com.johnsproject.jgameengine.model.Texture;
import com.johnsproject.jgameengine.model.Vertex;
import com.johnsproject.jgameengine.rasterization.PerspectiveGouraudRasterizer;
import com.johnsproject.jgameengine.util.ColorUtils;
import com.johnsproject.jgameengine.util.FixedPointUtils;
import com.johnsproject.jgameengine.util.TransformationUtils;
import com.johnsproject.jgameengine.util.VectorUtils;

public class GouraudSpecularShader  implements Shader {

	private static final int INITIAL_ATTENUATION = FP_ONE;
	private static final int LINEAR_ATTENUATION = FixedPointUtils.toFixedPoint(0.045);
	private static final int QUADRATIC_ATTENUATION = FixedPointUtils.toFixedPoint(0.0075);
	
	private ForwardShaderBuffer shaderBuffer;
	private final PerspectiveGouraudRasterizer rasterizer;

	private final int[] lightDirection;
	private final int[] lightLocation;
	private final int[] viewDirection;
	private final int[] lightSpaceLocation;
	
	private Material material;

	public GouraudSpecularShader() {
		this.rasterizer = new PerspectiveGouraudRasterizer(this);
		this.lightDirection = VectorUtils.emptyVector();
		this.lightLocation = VectorUtils.emptyVector();
		this.viewDirection = VectorUtils.emptyVector();
		this.lightSpaceLocation = VectorUtils.emptyVector();
	}

	public void vertex(Vertex vertex) {
		material = vertex.getMaterial();
		int[] location = vertex.getLocation();
		int[] normal = vertex.getWorldNormal();
		int lightColor = ColorUtils.BLACK;
		int[] cameraLocation = shaderBuffer.getCamera().getTransform().getLocation();
		VectorUtils.copy(location, vertex.getWorldLocation());
		VectorUtils.normalize(normal);
		VectorUtils.copy(viewDirection, cameraLocation);
		VectorUtils.subtract(viewDirection, location);
		VectorUtils.normalize(viewDirection);
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
				VectorUtils.copy(lightDirection, light.getDirection());
				VectorUtils.invert(lightDirection);
				currentFactor = getLightFactor(normal, lightDirection, viewDirection);
				if (lightIndex == shaderBuffer.getDirectionalLightIndex()) {
					final Frustum lightFrustum = shaderBuffer.getDirectionalLightFrustum();
					final int[][] lightMatrix = lightFrustum.getProjectionMatrix();
					Texture shadowMap = shaderBuffer.getDirectionalShadowMap();
					if(inShadow(location, lightMatrix, lightFrustum, shadowMap)) {
						currentFactor = ColorUtils.multiplyColor(currentFactor, light.getShadowColor());
					}
				}
				break;
			case POINT:
				VectorUtils.copy(lightLocation, lightPosition);
				VectorUtils.subtract(lightLocation, location);
				attenuation = getAttenuation(lightLocation);
				VectorUtils.normalize(lightLocation);
				currentFactor = getLightFactor(normal, lightLocation, viewDirection);
				currentFactor = FixedPointUtils.divide(currentFactor, attenuation);
				if ((lightIndex == shaderBuffer.getPointLightIndex()) && (currentFactor > 150)) {
					final Frustum lightFrustum = shaderBuffer.getPointLightFrustum();
					for (int j = 0; j < shaderBuffer.getPointLightMatrices().length; j++) {
						int[][] lightMatrix = shaderBuffer.getPointLightMatrices()[j];
						Texture shadowMap = shaderBuffer.getPointShadowMaps()[j];
						if(inShadow(location, lightMatrix, lightFrustum, shadowMap)) {
							currentFactor = ColorUtils.multiplyColor(currentFactor, light.getShadowColor());
						}
					}
				}
				break;
			case SPOT:				
				VectorUtils.copy(lightDirection, light.getDirection());
				VectorUtils.copy(lightLocation, lightPosition);
				VectorUtils.invert(lightDirection);
				VectorUtils.subtract(lightLocation, location);
				attenuation = getAttenuation(lightLocation);
				VectorUtils.normalize(lightLocation);
				long theta = VectorUtils.dotProduct(lightLocation, lightDirection);
				int phi = FixedPointUtils.cos(light.getSpotSize() >> 1);
				if(theta > phi) {
					int intensity = -FixedPointUtils.divide(phi - theta, light.getSpotSoftness() + 1);
					intensity = FixedPointUtils.clamp(intensity, 1, FP_ONE);
					currentFactor = getLightFactor(normal, lightDirection, viewDirection);
					currentFactor = FixedPointUtils.multiply(currentFactor, intensity * 2);
					currentFactor = FixedPointUtils.divide(currentFactor, attenuation);
					if ((lightIndex == shaderBuffer.getSpotLightIndex()) && (currentFactor > 10)) {
						final Frustum lightFrustum = shaderBuffer.getSpotLightFrustum();
						final int[][] lightMatrix = lightFrustum.getProjectionMatrix();
						Texture shadowMap = shaderBuffer.getSpotShadowMap();
						if(inShadow(location, lightMatrix, lightFrustum, shadowMap)) {
							currentFactor = ColorUtils.multiplyColor(currentFactor, light.getShadowColor());
						}
					}
				}
				break;
			}
			currentFactor = FixedPointUtils.multiply(currentFactor, light.getStrength());
			currentFactor = FixedPointUtils.multiply(currentFactor, 255);
			lightColor = ColorUtils.lerp(lightColor, light.getColor(), currentFactor);
			lightIndex++;
		}
		vertex.setShadedColor(lightColor);
		VectorUtils.multiply(location, shaderBuffer.getCamera().getTransform().getSpaceEnterMatrix());
		VectorUtils.multiply(location, shaderBuffer.getCamera().getFrustum().getProjectionMatrix());
		TransformationUtils.viewportVector(location, shaderBuffer.getCamera().getFrustum());
	}

	public void geometry(Face face) {
		material = face.getMaterial();
		Texture texture = material.getTexture();
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
			int color = material.getDiffuseColor();
			Texture texture = material.getTexture();
			int lightColor = fragment.getColor();
			if (texture != null) {
				int[] uv = fragment.getUV();
				int texel = texture.getPixel(uv[VECTOR_X], uv[VECTOR_Y]);
				if (ColorUtils.getAlpha(texel) == 0) // discard pixel if alpha = 0
					return;
				color = texel;
			}
			color = ColorUtils.multiplyColor(color, lightColor);
			depthBuffer.setPixel(x, y, z);
			colorBuffer.setPixel(x, y, color);
		}
	}

	private int getLightFactor(int[] normal, int[] lightDirection, int[] viewDirection) {
		// diffuse
		int dotProduct = (int)VectorUtils.dotProduct(normal, lightDirection);
		int diffuseFactor = Math.max(dotProduct, 0);
		diffuseFactor = FixedPointUtils.multiply(diffuseFactor, material.getDiffuseIntensity());
		// specular
		VectorUtils.invert(lightDirection);
		TransformationUtils.reflect(lightDirection, normal);
		dotProduct = (int)VectorUtils.dotProduct(viewDirection, lightDirection);
		int specularFactor = Math.max(dotProduct, 0);
		specularFactor = FixedPointUtils.pow(specularFactor, material.getShininess());
		specularFactor = FixedPointUtils.multiply(specularFactor, material.getSpecularIntensity());
		// putting it all together...
		return diffuseFactor + specularFactor;
	}
	
	private int getAttenuation(int[] lightLocation) {
		// attenuation
		int distance = VectorUtils.length(lightLocation);
		int attenuation = INITIAL_ATTENUATION;
		attenuation += FixedPointUtils.multiply(distance, LINEAR_ATTENUATION);
		attenuation += FixedPointUtils.multiply(FixedPointUtils.multiply(distance, distance), QUADRATIC_ATTENUATION);
		return attenuation + 1;
	}
	
	private boolean inShadow(int[] location, int[][] lightMatrix, Frustum lightFrustum, Texture shadowMap) {
		VectorUtils.copy(lightSpaceLocation, location);
		VectorUtils.multiply(lightSpaceLocation, lightMatrix);
		TransformationUtils.viewportVector(lightSpaceLocation, lightFrustum);
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
	
	public boolean isGlobal() {
		return false;
	}
}
