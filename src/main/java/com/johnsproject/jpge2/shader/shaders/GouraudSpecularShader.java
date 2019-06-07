package com.johnsproject.jpge2.shader.shaders;

import java.util.List;

import com.johnsproject.jpge2.dto.Camera;
import com.johnsproject.jpge2.dto.Face;
import com.johnsproject.jpge2.dto.FrameBuffer;
import com.johnsproject.jpge2.dto.Light;
import com.johnsproject.jpge2.dto.ShaderDataBuffer;
import com.johnsproject.jpge2.dto.ShaderProperties;
import com.johnsproject.jpge2.dto.Texture;
import com.johnsproject.jpge2.dto.Vertex;
import com.johnsproject.jpge2.library.ColorLibrary;
import com.johnsproject.jpge2.library.GraphicsLibrary;
import com.johnsproject.jpge2.library.MathLibrary;
import com.johnsproject.jpge2.library.MatrixLibrary;
import com.johnsproject.jpge2.library.VectorLibrary;
import com.johnsproject.jpge2.shader.Shader;
import com.johnsproject.jpge2.shader.databuffers.ForwardDataBuffer;
import com.johnsproject.jpge2.shader.PerspectiveGouraudTriangle;

public class GouraudSpecularShader implements Shader {

	private static final int INITIAL_ATTENUATION = MathLibrary.FP_ONE;
	private static final int LINEAR_ATTENUATION = (MathLibrary.FP_ONE * 14) / 10;
	private static final int QUADRATIC_ATTENUATION = (MathLibrary.FP_ONE * 7) / 10;
	
	private static final int LIGHT_RANGE = MathLibrary.FP_ONE * 1000;
	
	private static final byte VECTOR_X = VectorLibrary.VECTOR_X;
	private static final byte VECTOR_Y = VectorLibrary.VECTOR_Y;
	private static final byte VECTOR_Z = VectorLibrary.VECTOR_Z;
	
	private static final byte FP_BITS = MathLibrary.FP_BITS;
	private static final int FP_ONE = MathLibrary.FP_ONE;
	
	private final GraphicsLibrary graphicsLibrary;
	private final MathLibrary mathLibrary;
	private final MatrixLibrary matrixLibrary;
	private final VectorLibrary vectorLibrary;
	private final ColorLibrary colorLibrary;

	private final int[] normalizedNormal;
	private final int[] lightDirection;
	private final int[] lightLocation;
	private final int[] viewDirection;
	private final int[] portedFrustum;
	private final int[] colors;
	
	private final int[][] viewMatrix;
	private final int[][] projectionMatrix;

	private final int[] lightSpaceLocation;
	
	private final PerspectiveGouraudTriangle triangle;
	
	private int color;
	private int modelColor;
	private Texture texture;

	private Camera camera;	
	private List<Light> lights;
	private FrameBuffer frameBuffer;
	private ForwardDataBuffer shaderData;
	private ShaderProperties shaderProperties;

	public GouraudSpecularShader() {
		this.graphicsLibrary = new GraphicsLibrary();
		this.mathLibrary = new MathLibrary();
		this.matrixLibrary = new MatrixLibrary();
		this.vectorLibrary = new VectorLibrary();
		this.colorLibrary = new ColorLibrary();
		this.triangle = new PerspectiveGouraudTriangle(this);

		this.normalizedNormal = vectorLibrary.generate();
		this.lightDirection = vectorLibrary.generate();
		this.lightLocation = vectorLibrary.generate();
		this.viewDirection = vectorLibrary.generate();
		this.colors = vectorLibrary.generate();
		this.portedFrustum = new int[Camera.FRUSTUM_SIZE];

		this.viewMatrix = matrixLibrary.generate();
		this.projectionMatrix = matrixLibrary.generate();
		
		this.lightSpaceLocation = vectorLibrary.generate();
	}
	
	public void update(ShaderDataBuffer shaderDataBuffer) {
		this.shaderData = (ForwardDataBuffer)shaderDataBuffer;
		this.lights = shaderData.getLights();
		this.frameBuffer = shaderData.getFrameBuffer();
		frameBuffer.getColorBuffer().fill(0);
		frameBuffer.getDepthBuffer().fill(Integer.MAX_VALUE);
	}

	public void setup(Camera camera) {
		this.camera = camera;
		graphicsLibrary.viewMatrix(viewMatrix, camera.getTransform());
		graphicsLibrary.portFrustum(camera.getFrustum(), frameBuffer.getWidth(), frameBuffer.getHeight(), portedFrustum);
		switch (camera.getType()) {
		case ORTHOGRAPHIC:
			graphicsLibrary.orthographicMatrix(projectionMatrix, portedFrustum);
			break;

		case PERSPECTIVE:
			graphicsLibrary.perspectiveMatrix(projectionMatrix, portedFrustum);
			break;
		}
	}

	// VertexShaderInput and Output and only loop through all vertices once
	public void vertex(int index, Vertex vertex) {
		this.shaderProperties = (ShaderProperties)vertex.getMaterial().getProperties();
		int[] location = vertex.getLocation();
		int[] normal = vertex.getNormal();
		int lightColor = ColorLibrary.BLACK;
		int[] cameraLocation = camera.getTransform().getLocation();	
		vectorLibrary.subtract(cameraLocation, location, viewDirection);
		// normalize values
		vectorLibrary.normalize(normal, normalizedNormal);
		vectorLibrary.normalize(viewDirection, viewDirection);
		for (int i = 0; i < lights.size(); i++) {
			Light light = lights.get(i);
			int currentFactor = 0;
			int attenuation = 0;
			int[] lightPosition = light.getTransform().getLocation();
			boolean inShadow = false;
			switch (light.getType()) {
			case DIRECTIONAL:
				if (vectorLibrary.distance(cameraLocation, lightPosition) > LIGHT_RANGE)
					continue;
				vectorLibrary.invert(light.getDirection(), lightDirection);
				currentFactor = getLightFactor(normalizedNormal, lightDirection, viewDirection, shaderProperties);
				if (i == shaderData.getDirectionalLightIndex()) {
					vectorLibrary.multiply(location, shaderData.getDirectionalLightMatrix(), lightSpaceLocation);
					graphicsLibrary.viewport(lightSpaceLocation, shaderData.getDirectionalLightFrustum(), lightSpaceLocation);
					inShadow = inShadow(lightSpaceLocation, shaderData.getDirectionalShadowMap());
				}
				break;
			case POINT:
				if (vectorLibrary.distance(cameraLocation, lightPosition) > LIGHT_RANGE)
					continue;
				vectorLibrary.subtract(lightPosition, location, lightLocation);
				// attenuation
				attenuation = getAttenuation(lightLocation);
				// other light values
				vectorLibrary.normalize(lightLocation, lightLocation);
				currentFactor = getLightFactor(normalizedNormal, lightLocation, viewDirection, shaderProperties);
				currentFactor = mathLibrary.divide(currentFactor, attenuation);
				if ((i == shaderData.getPointLightIndex()) && (currentFactor > 100)) {
					for (int j = 0; j < shaderData.getPointLightMatrices().length; j++) {
						vectorLibrary.multiply(location, shaderData.getPointLightMatrices()[j], lightSpaceLocation);
						graphicsLibrary.viewport(lightSpaceLocation, shaderData.getPointLightFrustum(), lightSpaceLocation);
						inShadow = inShadow(lightSpaceLocation, shaderData.getPointShadowMaps()[j]);
					}
				}
				break;
			case SPOT:				
				if (vectorLibrary.distance(cameraLocation, lightPosition) > LIGHT_RANGE)
					continue;
				vectorLibrary.invert(light.getDirection(), lightDirection);
				vectorLibrary.subtract(lightPosition, location, lightLocation);
				// attenuation
				attenuation = getAttenuation(lightLocation);
				vectorLibrary.normalize(lightLocation, lightLocation);
				int theta = vectorLibrary.dotProduct(lightLocation, lightDirection);
				int phi = mathLibrary.cos(light.getSpotSize() >> 1);
				if(theta > phi) {
					int intensity = -mathLibrary.divide(phi - theta, light.getSpotSoftness() + 1);
					intensity = mathLibrary.clamp(intensity, 1, FP_ONE);
					currentFactor = getLightFactor(normalizedNormal, lightDirection, viewDirection, shaderProperties);
					currentFactor = mathLibrary.multiply(currentFactor, intensity * 2);
					currentFactor = mathLibrary.divide(currentFactor, attenuation);
					if ((i == shaderData.getSpotLightIndex()) && (currentFactor > 10)) {
						vectorLibrary.multiply(location, shaderData.getSpotLightMatrix(), lightSpaceLocation);
						graphicsLibrary.viewport(lightSpaceLocation, shaderData.getSpotLightFrustum(), lightSpaceLocation);
						inShadow = inShadow(lightSpaceLocation, shaderData.getSpotShadowMap());
					}
				}
				break;
			}
			currentFactor = mathLibrary.multiply(currentFactor, light.getStrength());
			currentFactor = mathLibrary.multiply(currentFactor, 255);
			if(inShadow) {
				lightColor = colorLibrary.lerp(lightColor, light.getShadowColor(), 128);
			} else {
				lightColor = colorLibrary.lerp(lightColor, light.getColor(), currentFactor);
			}
		}
		colors[index] = lightColor;
		vectorLibrary.multiply(location, viewMatrix, location);
		vectorLibrary.multiply(location, projectionMatrix, location);
		graphicsLibrary.viewport(location, portedFrustum, location);
	}

	public void geometry(Face face) {
		color = shaderProperties.getDiffuseColor();
		texture = shaderProperties.getTexture();
		if (texture == null) {
			graphicsLibrary.drawTriangle(triangle, face, colors, portedFrustum);
		} else {
			graphicsLibrary.drawTriangle(triangle, face, texture, colors, portedFrustum);
		}
	}

	public void fragment(int[] location) {
		int lightColor = colorLibrary.generate(triangle.getRed()[3], triangle.getGreen()[3], triangle.getBlue()[3]);
		if (texture != null) {
			int u = triangle.getU()[3];
			int v = triangle.getV()[3];
			int texel = texture.getPixel(u, v);
			if (colorLibrary.getAlpha(texel) == 0) // discard pixel if alpha = 0
				return;
			modelColor = colorLibrary.multiplyColor(texel, lightColor);
		} else {
			modelColor = colorLibrary.multiplyColor(color, lightColor);
		}
		Texture colorBuffer = frameBuffer.getColorBuffer();
		Texture depthBuffer = frameBuffer.getDepthBuffer();
		int x = location[VECTOR_X];
		int y = location[VECTOR_Y];
		int z = location[VECTOR_Z];
		if (depthBuffer.getPixel(x, y) > z) {
			depthBuffer.setPixel(x, y, z);
			colorBuffer.setPixel(x, y, modelColor);
		}
	}

	private int getLightFactor(int[] normal, int[] lightDirection, int[] viewDirection, ShaderProperties properties) {
		// diffuse
		int dotProduct = vectorLibrary.dotProduct(normal, lightDirection);
		int diffuseFactor = Math.max(dotProduct, 0);
		diffuseFactor = mathLibrary.multiply(diffuseFactor, properties.getDiffuseIntensity());
		// specular
		vectorLibrary.invert(lightDirection, lightDirection);
		vectorLibrary.reflect(lightDirection, normal, lightDirection);
		dotProduct = vectorLibrary.dotProduct(viewDirection, lightDirection);
		int specularFactor = Math.max(dotProduct, 0);
		specularFactor = mathLibrary.pow(specularFactor, properties.getShininess() >> FP_BITS);
		specularFactor = mathLibrary.multiply(specularFactor, properties.getSpecularIntensity());
		// putting it all together...
		return diffuseFactor + specularFactor;
	}
	
	private int getAttenuation(int[] lightLocation) {
		// attenuation
		int distance = vectorLibrary.magnitude(lightLocation);
		int attenuation = INITIAL_ATTENUATION;
		attenuation += mathLibrary.multiply(distance, LINEAR_ATTENUATION);
		attenuation += mathLibrary.multiply(mathLibrary.multiply(distance, distance), QUADRATIC_ATTENUATION);
		return (attenuation >> FP_BITS) + 1;
	}
	
	private boolean inShadow(int[] lightSpaceLocation, Texture shadowMap) {
		int x = lightSpaceLocation[VECTOR_X];
		int y = lightSpaceLocation[VECTOR_Y];
		int depth1 = shadowMap.getPixel(x, y);
		int depth2 = shadowMap.getPixel(x+1, y);
		int depth3 = shadowMap.getPixel(x-1, y);
//		int color = (lightSpaceLocation[VECTOR_Z] + 100) >> 3;
//		color = new ColorLibrary().generate(color, color, color);
//		frameBuffer.getColorBuffer().setPixel(lightSpaceLocation[VECTOR_X], lightSpaceLocation[VECTOR_Y], color);
		return (depth1 < lightSpaceLocation[VECTOR_Z]) | (depth2 < lightSpaceLocation[VECTOR_Z]) | (depth3 < lightSpaceLocation[VECTOR_Z]);
	}

	public void terminate(ShaderDataBuffer shaderDataBuffer) {
		
	}
}
