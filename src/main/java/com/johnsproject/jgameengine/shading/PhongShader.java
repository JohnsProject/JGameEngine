package com.johnsproject.jgameengine.shading;

import static com.johnsproject.jgameengine.util.VectorUtils.VECTOR_X;
import static com.johnsproject.jgameengine.util.VectorUtils.VECTOR_Y;
import static com.johnsproject.jgameengine.util.VectorUtils.VECTOR_Z;

import com.johnsproject.jgameengine.model.Camera;
import com.johnsproject.jgameengine.model.Face;
import com.johnsproject.jgameengine.model.Fragment;
import com.johnsproject.jgameengine.model.Light;
import com.johnsproject.jgameengine.model.Material;
import com.johnsproject.jgameengine.model.Texture;
import com.johnsproject.jgameengine.model.Vertex;
import com.johnsproject.jgameengine.rasterization.PerspectivePhongRasterizer;
import com.johnsproject.jgameengine.util.ColorUtils;
import com.johnsproject.jgameengine.util.FixedPointUtils;
import com.johnsproject.jgameengine.util.TransformationUtils;
import com.johnsproject.jgameengine.util.VectorUtils;

public class PhongShader implements Shader {

	private ForwardShaderBuffer shaderBuffer;
	private final PerspectivePhongRasterizer rasterizer;
	
	private int[] lightDirection;
	private int[] viewDirection;
	
	public PhongShader() {
		this.rasterizer = new PerspectivePhongRasterizer(this);
		this.lightDirection = VectorUtils.emptyVector();
		this.viewDirection = VectorUtils.emptyVector();
	}
	
	public void vertex(Vertex vertex) {
		final int[] location = vertex.getLocation();
		VectorUtils.copy(location, vertex.getWorldLocation());
		VectorUtils.multiply(location, shaderBuffer.getCamera().getTransform().getSpaceEnterMatrix());
		VectorUtils.multiply(location, shaderBuffer.getCamera().getFrustum().getProjectionMatrix());
		TransformationUtils.screenportVector(location, shaderBuffer.getCamera().getFrustum());
	}

	public void geometry(Face face) {
		rasterizer.draw(face);
	}

	public void fragment(Fragment fragment) {
		final Camera camera = shaderBuffer.getCamera();
		final Texture depthBuffer = camera.getRenderTarget().getDepthBuffer();
		final Texture colorBuffer = camera.getRenderTarget().getColorBuffer();
		final Material material = fragment.getMaterial();		
		final int x = fragment.getLocation()[VECTOR_X];
		final int y = fragment.getLocation()[VECTOR_Y];
		final int z = fragment.getLocation()[VECTOR_Z];
		if (depthBuffer.getPixel(x, y) > z) {
			
			final int[] location = fragment.getWorldLocation();
			final int[] normal = fragment.getWorldNormal();
			VectorUtils.normalize(normal);
			
			final int color = calculateLights(location, normal, material);
			colorBuffer.setPixel(x, y, color);
			depthBuffer.setPixel(x, y, z);
		}
	}
	
	private int calculateLights(int[] location, int[] normal, Material material) {
		VectorUtils.copy(viewDirection, shaderBuffer.getCamera().getTransform().getLocation());
		VectorUtils.subtract(viewDirection, location);
		VectorUtils.normalize(viewDirection);
		int color = ColorUtils.BLACK;
		for (int i = 0; i < shaderBuffer.getLights().size(); i++) {
			final Light light = shaderBuffer.getLights().get(i);
			if(!light.isActive() || light.isCulled())
				continue;
			switch (light.getType()) {
			case DIRECTIONAL:
				final int directional = calculateDirectionalLight(location, normal, material, light);
				color = ColorUtils.add(color, directional);
				break;
				
			case POINT:
				final int point = calculatePointLight(location, normal, material, light);
				color = ColorUtils.add(color, point);
				break;
				
			case SPOT:
				final int spot = calculateSpotLight(location, normal, material, light);
				color = ColorUtils.add(color, spot);
				break;
			}
			color = ColorUtils.multiply(color, light.getStrength());
			color = ColorUtils.add(color, light.getAmbientColor());
		}
		return color;
	}
	
	private int calculateDirectionalLight(int[] location, int[] normal, Material material, Light light) {
		int color = 0;
		calculateDirectionalLightDirection(light);
		int diffuse = calculateDiffuseColor(normal, material, light);
		color = ColorUtils.add(color, diffuse);
		if(material.getShininess() > 0) {
			int specular = calculateSpecularColor(location, normal, material, light);
			color = ColorUtils.add(color, specular);
		}
		return color;
	}
	
	private int calculatePointLight(int[] location, int[] normal, Material material, Light light) {
		int color = 0;
		calculateLightDirection(location, light);
		final int attenuation = calculateAttenuation(location, light);
		int diffuse = calculateDiffuseColor(normal, material, light);
		diffuse = ColorUtils.multiply(diffuse, attenuation);
		color = ColorUtils.add(color, diffuse);
		if(material.getShininess() > 0) {
			int specular = calculateSpecularColor(location, normal, material, light);
			specular = ColorUtils.multiply(specular, attenuation);
			color = ColorUtils.add(color, specular);
		}
		return color;
	}
	
	private int calculateSpotLight(int[] location, int[] normal, Material material, Light light) {
		int color = 0;
		calculateLightDirection(location, light);
		final int spotIntensity = calculateSpotIntensity(light);
		final int attenuation = calculateAttenuation(location, light);
		final int lightIntensity = FixedPointUtils.multiply(attenuation, spotIntensity);
		int diffuse = calculateDiffuseColor(normal, material, light);
		diffuse = ColorUtils.multiply(diffuse, lightIntensity);
		color = ColorUtils.add(color, diffuse);
		if(material.getShininess() > 0) {
			int specular = calculateSpecularColor(location, normal, material, light);
			specular = ColorUtils.multiply(specular, lightIntensity);
			color = ColorUtils.add(color, specular);
		}
		return color;
	}
	
	private void calculateDirectionalLightDirection(Light light) {
		VectorUtils.copy(lightDirection, light.getDirection());
		VectorUtils.invert(lightDirection);
	}
	
	private void calculateLightDirection(int[] location, Light light) {
		VectorUtils.copy(lightDirection, light.getTransform().getLocation());
		VectorUtils.subtract(lightDirection, location);
		VectorUtils.normalize(lightDirection);
	}
	
	private int calculateDiffuseColor(int[] normal, Material material, Light light) {
		int diffuseIntesity = (int)VectorUtils.dotProduct(normal, lightDirection);
		diffuseIntesity = Math.max(diffuseIntesity, 0);
		int diffuse = ColorUtils.multiply(material.getDiffuseColor(), diffuseIntesity);
		diffuse = ColorUtils.multiplyColor(diffuse, light.getColor());
		return diffuse;
	}
	
	private int calculateSpecularColor(int[] location, int[] normal, Material material, Light light) {
		VectorUtils.invert(lightDirection);
		TransformationUtils.reflect(lightDirection, normal);
		int specularIntensity = (int)VectorUtils.dotProduct(viewDirection, lightDirection);
		specularIntensity = Math.max(specularIntensity, 0);
		specularIntensity = FixedPointUtils.pow(specularIntensity, material.getShininess());
		int specular = ColorUtils.multiply(material.getSpecularColor(), specularIntensity);
		specular = ColorUtils.multiplyColor(specular, light.getColor());
		return specular;
	}
	
	private int calculateAttenuation(int[] location, Light light) {
		final int lightConstant = light.getConstantAttenuation();
		final int lightLinear = light.getLinearAttenuation();
		final int lightQuadratic = light.getQuadraticAttenuation();
		final int distance = VectorUtils.distance(light.getTransform().getLocation(), location);
		final int distanceSquared = FixedPointUtils.multiply(distance, distance);
		int attenuation = lightConstant;
		attenuation += FixedPointUtils.multiply(lightLinear, distance);
		attenuation += FixedPointUtils.multiply(lightQuadratic, distanceSquared);
		attenuation = FixedPointUtils.divide(FixedPointUtils.FP_ONE, attenuation);
		return attenuation;
	}
	
	private int calculateSpotIntensity(Light light) {
		int[] direction = VectorUtils.copy(viewDirection, light.getDirection());
		VectorUtils.invert(direction);
		int theta = (int)VectorUtils.dotProduct(lightDirection, direction);
		int intesity = theta - light.getSpotSizeCosine();
		intesity = FixedPointUtils.divide(intesity, light.getSpotSoftness());
		intesity = FixedPointUtils.clamp(intesity, 0, FixedPointUtils.FP_ONE);
		return intesity;
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
