package com.johnsproject.jgameengine.graphics.shading;

import static com.johnsproject.jgameengine.math.FixedPoint.FP_BIT;
import static com.johnsproject.jgameengine.math.FixedPoint.FP_HALF;
import static com.johnsproject.jgameengine.math.FixedPoint.FP_ONE;
import static com.johnsproject.jgameengine.math.Vector.VECTOR_X;
import static com.johnsproject.jgameengine.math.Vector.VECTOR_Y;
import static com.johnsproject.jgameengine.math.Vector.VECTOR_Z;

import java.util.List;

import com.johnsproject.jgameengine.graphics.Camera;
import com.johnsproject.jgameengine.graphics.Color;
import com.johnsproject.jgameengine.graphics.Face;
import com.johnsproject.jgameengine.graphics.FrameBuffer;
import com.johnsproject.jgameengine.graphics.Light;
import com.johnsproject.jgameengine.graphics.Material;
import com.johnsproject.jgameengine.graphics.Texture;
import com.johnsproject.jgameengine.graphics.Vertex;
import com.johnsproject.jgameengine.graphics.rasterization.LinearRasterizer4;
import com.johnsproject.jgameengine.math.FixedPoint;
import com.johnsproject.jgameengine.math.Frustum;
import com.johnsproject.jgameengine.math.Transformation;
import com.johnsproject.jgameengine.math.Vector;

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
		
		private final int[] lightDirection;
		private final int[] viewDirection;

		public VertexShader() {
			this.lightDirection = Vector.emptyVector();
			this.viewDirection = Vector.emptyVector();
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
			Vector.copy(location, vertex.getWorldLocation());
			
			vertex.setLightColor(calculateLights(location, normal, material));
			
			Vector.multiply(location, camera.getTransform().getSpaceEnterMatrix());
			Vector.multiply(location, frustum.getProjectionMatrix());
			Transformation.screenportVector(location, frustum);
		}
		
		private int calculateLights(int[] location, int[] normal, Material material) {
			Vector.copy(viewDirection, camera.getTransform().getLocation());
			Vector.subtract(viewDirection, location);
			Vector.normalize(viewDirection);
			Vector.normalize(normal);
			int color = Color.BLACK;
			for (int i = 0; i < lights.size(); i++) {
				final Light light = lights.get(i);
				if(!light.isActive() || light.isCulled())
					continue;
				final int ambient = light.getAmbientColor();
				int lighting = calculateLight(location, normal, material, light);
				lighting = Color.multiply(lighting, light.getIntensity());
				color = Color.add(color, lighting);
				color = Color.add(color, ambient);
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
			return Color.BLACK;
		}
		
		private int calculateDirectionalLight(int[] location, int[] normal, Material material, Light light) {
			int color = 0;
			calculateDirectionalLightDirection(light);
			int diffuse = calculateDiffuseColor(normal, material, light);
			color = Color.add(color, diffuse);
			if(material.getShininess() > 0) {
				int specular = calculateSpecularColor(location, normal, material, light);
				color = Color.add(color, specular);
			}
			return color;
		}
		
		private int calculatePointLight(int[] location, int[] normal, Material material, Light light) {
			int color = 0;
			calculateLightDirection(location, light);
			final int attenuation = calculateAttenuation(location, light);
			int diffuse = calculateDiffuseColor(normal, material, light);
			diffuse = Color.multiply(diffuse, attenuation);
			color = Color.add(color, diffuse);
			if(material.getShininess() > 0) {
				int specular = calculateSpecularColor(location, normal, material, light);
				specular = Color.multiply(specular, attenuation);
				color = Color.add(color, specular);
			}
			return color;
		}
		
		private int calculateSpotLight(int[] location, int[] normal, Material material, Light light) {
			int color = 0;
			calculateLightDirection(location, light);
			final int spotIntensity = calculateSpotIntensity(light);
			final int attenuation = calculateAttenuation(location, light);
			final int lightIntensity = FixedPoint.multiply(attenuation, spotIntensity);
			int diffuse = calculateDiffuseColor(normal, material, light);
			diffuse = Color.multiply(diffuse, lightIntensity);
			color = Color.add(color, diffuse);
			if(material.getShininess() > 0) {
				int specular = calculateSpecularColor(location, normal, material, light);
				specular = Color.multiply(specular, lightIntensity);
				color = Color.add(color, specular);
			}
			return color;
		}
		
		private void calculateDirectionalLightDirection(Light light) {
			Vector.copy(lightDirection, light.getDirection());
			Vector.invert(lightDirection);
		}
		
		private void calculateLightDirection(int[] location, Light light) {
			Vector.copy(lightDirection, light.getTransform().getLocation());
			Vector.subtract(lightDirection, location);
			Vector.normalize(lightDirection);
		}
		
		private int calculateDiffuseColor(int[] normal, Material material, Light light) {
			int diffuseIntesity = (int)Vector.dotProduct(normal, lightDirection);
			diffuseIntesity = Math.max(diffuseIntesity, 0);
			int diffuse = Color.multiply(material.getDiffuseColor(), diffuseIntesity);
			diffuse = Color.multiplyColor(diffuse, light.getColor());
			return diffuse;
		}
		
		private int calculateSpecularColor(int[] location, int[] normal, Material material, Light light) {
			Vector.invert(lightDirection);
			Transformation.reflect(lightDirection, normal);
			int specularIntensity = (int)Vector.dotProduct(viewDirection, lightDirection);
			specularIntensity = Math.max(specularIntensity, 0);
			specularIntensity = FixedPoint.pow(specularIntensity, material.getShininess());
			int specular = Color.multiply(material.getSpecularColor(), specularIntensity);
			specular = Color.multiplyColor(specular, light.getColor());
			return specular;
		}
		
		private int calculateAttenuation(int[] location, Light light) {
			final int lightConstant = light.getConstantAttenuation();
			final int lightLinear = light.getLinearAttenuation();
			final int lightQuadratic = light.getQuadraticAttenuation();
			final int distance = Vector.distance(light.getTransform().getLocation(), location);
			final int distanceSquared = FixedPoint.multiply(distance, distance);
			int attenuation = lightConstant;
			attenuation += FixedPoint.multiply(lightLinear, distance);
			attenuation += FixedPoint.multiply(lightQuadratic, distanceSquared);
			attenuation = FixedPoint.divide(FP_ONE, attenuation);
			return attenuation;
		}
		
		private int calculateSpotIntensity(Light light) {
			int[] direction = Vector.copy(viewDirection, light.getDirection());
			Vector.invert(direction);
			int theta = (int)Vector.dotProduct(lightDirection, direction);
			int intesity = theta - light.getSpotSizeCosine();
			intesity = FixedPoint.divide(intesity, light.getSpotSoftness());
			intesity = FixedPoint.clamp(intesity, 0, FP_ONE);
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
		
		private Texture texture;
		private int texelColor;
		private final int[] lightSpaceLocation;
		private boolean isInShadow;
		
		public GeometryShader() {
			this.lightSpaceLocation = Vector.emptyVector();
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
			final Material material = face.getMaterial();
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
			int r = Color.getRed(face.getVertex(0).getLightColor()) << FP_BIT;
			int g = Color.getGreen(face.getVertex(0).getLightColor()) << FP_BIT;
			int b = Color.getBlue(face.getVertex(0).getLightColor()) << FP_BIT;
			rasterizer.setVector10(r, g, b);
			r = Color.getRed(face.getVertex(1).getLightColor()) << FP_BIT;
			g = Color.getGreen(face.getVertex(1).getLightColor()) << FP_BIT;
			b = Color.getBlue(face.getVertex(1).getLightColor()) << FP_BIT;
			rasterizer.setVector11(r, g, b);
			r = Color.getRed(face.getVertex(2).getLightColor()) << FP_BIT;
			g = Color.getGreen(face.getVertex(2).getLightColor()) << FP_BIT;
			b = Color.getBlue(face.getVertex(2).getLightColor()) << FP_BIT;
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
			Vector.copy(lightSpaceLocation, worldLocation);
			Vector.multiply(lightSpaceLocation, lightMatrix);
			Transformation.screenportVector(lightSpaceLocation, lightFrustum);
			// The rasterizer will interpolate fixed point vectors but screen space vectors are not fixed point
			Vector.multiply(lightSpaceLocation, FP_ONE << FP_BIT);
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
				
				int color = Color.multiplyColor(lightColor, texelColor);
				if(isInShadow)
					color = Color.multiply(color, FP_HALF);
				
				colorBuffer.setPixel(x, y, color);
				depthBuffer.setPixel(x, y, z);
			}
		}
		
		private int getFragmentTexelColor(int[] uv) {
			if(texture == null) {
				return Color.WHITE;
			} else {
				// The result will be, but pixels are not accessed with fixed point
				final int u = uv[VECTOR_X] >> FP_BIT;
				final int v = uv[VECTOR_Y] >> FP_BIT;
				return texture.getPixel(u, v);
			}
		}
		
		private boolean isFragmentInShadow(int[] lightSpaceLocation, Texture shadowMap) {
			if(shadowMap == null) {
				return false;
			} else {
				// The result will be, but pixels are not accessed with fixed point
				final int x = lightSpaceLocation[VECTOR_X] >> FP_BIT;
				final int y = lightSpaceLocation[VECTOR_Y] >> FP_BIT;
				final int depth = shadowMap.getPixel(x, y);
				return depth < lightSpaceLocation[VECTOR_Z] >> FP_BIT;
			}
		}
		
		private int getFragmentLightColor(int[] color) {
			final int r = color[0] >> FP_BIT;
			final int g = color[1] >> FP_BIT;
			final int b = color[2] >> FP_BIT;
			return Color.toColor(r, g, b);
		}

		public ShaderBuffer getShaderBuffer() {
			return shaderBuffer;
		}
	}
}
