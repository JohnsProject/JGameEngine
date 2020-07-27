package com.johnsproject.jgameengine.shading;

import static com.johnsproject.jgameengine.util.VectorUtils.VECTOR_X;
import static com.johnsproject.jgameengine.util.VectorUtils.VECTOR_Y;
import static com.johnsproject.jgameengine.util.VectorUtils.VECTOR_Z;

import java.util.List;

import com.johnsproject.jgameengine.model.Camera;
import com.johnsproject.jgameengine.model.Face;
import com.johnsproject.jgameengine.model.FrameBuffer;
import com.johnsproject.jgameengine.model.Frustum;
import com.johnsproject.jgameengine.model.Light;
import com.johnsproject.jgameengine.model.Material;
import com.johnsproject.jgameengine.model.Texture;
import com.johnsproject.jgameengine.model.Vertex;
import com.johnsproject.jgameengine.rasterization.LinearRasterizer4;
import com.johnsproject.jgameengine.util.ColorUtils;
import com.johnsproject.jgameengine.util.FixedPointUtils;
import com.johnsproject.jgameengine.util.TransformationUtils;
import com.johnsproject.jgameengine.util.VectorUtils;

public class GouraudShader extends ThreadedShader {
	
	@Override
	public ThreadedVertexShader[] createVertexShaders(int count) {
		final ThreadedVertexShader[] shaders = new VertexShader[count];
		for (int i = 0; i < shaders.length; i++)
			shaders[i] = new VertexShader();
		return shaders;
	}

	@Override
	public ThreadedGeometryShader[] createGeometryShaders(int count) {
		final ThreadedGeometryShader[] shaders = new GeometryShader[count];
		for (int i = 0; i < shaders.length; i++)
			shaders[i] = new GeometryShader();
		return shaders;
	}
	
	public boolean isGlobal() {
		return false;
	}	

	private static class VertexShader extends ThreadedVertexShader {
		
		private ForwardShaderBuffer shaderBuffer;
		
		private Camera camera;
		private Frustum frustum;
		private List<Light> lights;
		
		private int[] lightDirection;
		private int[] viewDirection;

		public VertexShader() {
			this.lightDirection = VectorUtils.emptyVector();
			this.viewDirection = VectorUtils.emptyVector();
		}

		public void initialize(ShaderBuffer shaderBuffer) {
			this.shaderBuffer = (ForwardShaderBuffer) shaderBuffer;
			this.camera = shaderBuffer.getCamera();
			this.frustum = camera.getFrustum();
			this.lights = shaderBuffer.getLights();
		}
		
		public void vertex(Vertex vertex) {
			final Material material = vertex.getMaterial();
			final int[] location = vertex.getLocation();
			final int[] normal = vertex.getWorldNormal();
			VectorUtils.copy(location, vertex.getWorldLocation());
			
			vertex.setLightColor(calculateLights(location, normal, material));
			
			VectorUtils.multiply(location, camera.getTransform().getSpaceEnterMatrix());
			VectorUtils.multiply(location, frustum.getProjectionMatrix());
			TransformationUtils.screenportVector(location, frustum);
		}
		
		private int calculateLights(int[] location, int[] normal, Material material) {
			VectorUtils.copy(viewDirection, camera.getTransform().getLocation());
			VectorUtils.subtract(viewDirection, location);
			VectorUtils.normalize(viewDirection);
			VectorUtils.normalize(normal);
			int color = ColorUtils.BLACK;
			for (int i = 0; i < lights.size(); i++) {
				final Light light = lights.get(i);
				if(!light.isActive() || light.isCulled())
					continue;
				final int lighting = calculateLight(location, normal, material, light);
				final int ambient = light.getAmbientColor();
				color = ColorUtils.add(color, lighting);
				color = ColorUtils.add(color, ambient);
				color = ColorUtils.multiply(color, light.getIntensity());
			}
			return color;
		}
		
		private int calculateLight(int[] location, int[] normal, Material material, Light light) {
			switch (light.getType()) {
			case DIRECTIONAL:
				return calculateDirectionalLight(location, normal, material, light);
				
			case POINT:
				return calculatePointLight(location, normal, material, light);
				
			case SPOT:
				return calculateSpotLight(location, normal, material, light);
			}
			return ColorUtils.BLACK;
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
	}
	
	private static class GeometryShader extends ThreadedGeometryShader {

		private ForwardShaderBuffer shaderBuffer;
		private final LinearRasterizer4 rasterizer = new LinearRasterizer4(this);
		
		private Camera camera;
		private Frustum frustum;
		private FrameBuffer frameBuffer;
		
		private Frustum directionalLightFrustum;
		private Texture directionalLightShadowMap;
		private Frustum spotLightFrustum;
		private Texture spotLightShadowMap;
		
		private Material material;
		private Texture texture;
		private int texelColor;
		private int[] lightSpaceLocation;
		private boolean isInShadow;
		
		public GeometryShader() {
			this.lightSpaceLocation = VectorUtils.emptyVector();
		}

		public void initialize(ShaderBuffer shaderBuffer) {
			this.shaderBuffer = (ForwardShaderBuffer) shaderBuffer;
			this.camera = shaderBuffer.getCamera();
			this.frustum = camera.getFrustum();
			this.frameBuffer = camera.getRenderTarget();
			initialize();
		}
		
		private void initialize() {
			if(shaderBuffer.getShadowDirectionalLight() == null) {
				directionalLightFrustum = null;
				directionalLightShadowMap = null;
			} else {
				directionalLightFrustum = shaderBuffer.getDirectionalLightFrustum();
				directionalLightShadowMap = shaderBuffer.getDirectionalShadowMap();
			}
			if(shaderBuffer.getShadowSpotLight() == null) {
				spotLightFrustum = null;
				spotLightShadowMap = null;
			} else {
				spotLightFrustum = shaderBuffer.getSpotLightFrustum();
				spotLightShadowMap = shaderBuffer.getSpotShadowMap();
			}
		}
		
		public void geometry(Face face) {
			material = face.getMaterial();
			texture = material.getTexture();
			setUVs(face);
			setColors(face);
			setDirectionalLightSpaceVectors(face);
			setSpotLightSpaceVectors(face);
			rasterizer.linearDraw4(face, frustum);
		}

		private void setUVs(Face face) {
			if(texture != null) {
				// port uvs to texture space
				int u = face.getUV(0)[VECTOR_X] * texture.getWidth();
				int v = face.getUV(0)[VECTOR_Y] * texture.getHeight();
				rasterizer.setVector00(u, v, 0);
				u = face.getUV(1)[VECTOR_X] * texture.getWidth();
				v = face.getUV(1)[VECTOR_Y] * texture.getHeight();
				rasterizer.setVector01(u, v, 0);
				u = face.getUV(2)[VECTOR_X] * texture.getWidth();
				v = face.getUV(2)[VECTOR_Y] * texture.getHeight();
				rasterizer.setVector02(u, v, 0);
			}
		}
		
		private void setColors(Face face) {
			int r = ColorUtils.getRed(face.getVertex(0).getLightColor()) << FixedPointUtils.FP_BIT;
			int g = ColorUtils.getGreen(face.getVertex(0).getLightColor()) << FixedPointUtils.FP_BIT;
			int b = ColorUtils.getBlue(face.getVertex(0).getLightColor()) << FixedPointUtils.FP_BIT;
			rasterizer.setVector10(r, g, b);
			r = ColorUtils.getRed(face.getVertex(1).getLightColor()) << FixedPointUtils.FP_BIT;
			g = ColorUtils.getGreen(face.getVertex(1).getLightColor()) << FixedPointUtils.FP_BIT;
			b = ColorUtils.getBlue(face.getVertex(1).getLightColor()) << FixedPointUtils.FP_BIT;
			rasterizer.setVector11(r, g, b);
			r = ColorUtils.getRed(face.getVertex(2).getLightColor()) << FixedPointUtils.FP_BIT;
			g = ColorUtils.getGreen(face.getVertex(2).getLightColor()) << FixedPointUtils.FP_BIT;
			b = ColorUtils.getBlue(face.getVertex(2).getLightColor()) << FixedPointUtils.FP_BIT;
			rasterizer.setVector12(r, g, b);
		}
		
		private void setDirectionalLightSpaceVectors(Face face) {
			if(directionalLightShadowMap != null) {
				final int[][] lightMatrix = directionalLightFrustum.getProjectionMatrix();
				
				int[] worldLocation = face.getVertex(0).getWorldLocation();
				rasterizer.setVector20(transformToLightSpace(worldLocation, lightMatrix, directionalLightFrustum));
				
				worldLocation = face.getVertex(1).getWorldLocation();
				rasterizer.setVector21(transformToLightSpace(worldLocation, lightMatrix, directionalLightFrustum));
				
				worldLocation = face.getVertex(2).getWorldLocation();
				rasterizer.setVector22(transformToLightSpace(worldLocation, lightMatrix, directionalLightFrustum));
			}
		}
		
		private void setSpotLightSpaceVectors(Face face) {
			if(spotLightShadowMap != null) {
				final int[][] lightMatrix = spotLightFrustum.getProjectionMatrix();
				
				int[] worldLocation = face.getVertex(0).getWorldLocation();
				rasterizer.setVector30(transformToLightSpace(worldLocation, lightMatrix, spotLightFrustum));
				
				worldLocation = face.getVertex(1).getWorldLocation();
				rasterizer.setVector31(transformToLightSpace(worldLocation, lightMatrix, spotLightFrustum));
				
				worldLocation = face.getVertex(2).getWorldLocation();
				rasterizer.setVector32(transformToLightSpace(worldLocation, lightMatrix, spotLightFrustum));
			}
		}
		
		private int[] transformToLightSpace(int[] worldLocation, int[][] lightMatrix, Frustum lightFrustum) {
			VectorUtils.copy(lightSpaceLocation, worldLocation);
			VectorUtils.multiply(lightSpaceLocation, lightMatrix);
			TransformationUtils.screenportVector(lightSpaceLocation, lightFrustum);
			// The rasterizer will interpolate fixed point vectors but screen space vectors are not fixed point
			VectorUtils.multiply(lightSpaceLocation, FixedPointUtils.FP_ONE << FixedPointUtils.FP_BIT);
			return lightSpaceLocation;
		}
		
		public void fragment() {
			final Texture depthBuffer = frameBuffer.getDepthBuffer();
			final Texture colorBuffer = frameBuffer.getColorBuffer();
			final int x = rasterizer.getLocation()[VECTOR_X];
			final int y = rasterizer.getLocation()[VECTOR_Y];
			final int z = rasterizer.getLocation()[VECTOR_Z];
			if (depthBuffer.getPixel(x, y) > z) {
				
				final int[] uv = rasterizer.getVector0();
				texelColor = getFragmentTexelColor(uv);
				
				final int[] directionalLightSpaceLocation = rasterizer.getVector2();
				isInShadow = isFragmentInShadow(directionalLightSpaceLocation, directionalLightShadowMap);
				if(!isInShadow) {
					final int[] spotLightSpaceLocation = rasterizer.getVector3();
					isInShadow = isFragmentInShadow(spotLightSpaceLocation, spotLightShadowMap);
				}
				
				final int[] lightColorVector = rasterizer.getVector1();
				final int lightColor = getFragmentLightColor(lightColorVector);
				
				int color = ColorUtils.multiplyColor(lightColor, texelColor);
				if(isInShadow)
					color = ColorUtils.multiply(color, FixedPointUtils.FP_HALF);
				
				colorBuffer.setPixel(x, y, color);
				depthBuffer.setPixel(x, y, z);
			}
		}
		
		private int getFragmentTexelColor(int[] uv) {
			if(texture == null) {
				return ColorUtils.WHITE;
			} else {
				// The result will be, but pixels are not accessed with fixed point
				final int u = uv[VECTOR_X] >> FixedPointUtils.FP_BIT;
				final int v = uv[VECTOR_Y] >> FixedPointUtils.FP_BIT;
				return texture.getPixel(u, v);
			}
		}
		
		private boolean isFragmentInShadow(int[] lightSpaceLocation, Texture shadowMap) {
			if(shadowMap == null) {
				return false;
			} else {
				// The result will be, but pixels are not accessed with fixed point
				final int x = lightSpaceLocation[VECTOR_X] >> FixedPointUtils.FP_BIT;
				final int y = lightSpaceLocation[VECTOR_Y] >> FixedPointUtils.FP_BIT;
				final int depth = shadowMap.getPixel(x, y);
				return depth < lightSpaceLocation[VECTOR_Z] >> FixedPointUtils.FP_BIT;
			}
		}
		
		private int getFragmentLightColor(int[] color) {
			final int r = color[0] >> FixedPointUtils.FP_BIT;
			final int g = color[1] >> FixedPointUtils.FP_BIT;
			final int b = color[2] >> FixedPointUtils.FP_BIT;
			return ColorUtils.toColor(r, g, b);
		}

		public ShaderBuffer getShaderBuffer() {
			return shaderBuffer;
		}
	}
}
