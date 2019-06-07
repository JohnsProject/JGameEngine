package com.johnsproject.jpge2.shader.shaders;

import java.util.List;

import com.johnsproject.jpge2.dto.Camera;
import com.johnsproject.jpge2.dto.Face;
import com.johnsproject.jpge2.dto.Light;
import com.johnsproject.jpge2.dto.Texture;
import com.johnsproject.jpge2.dto.Transform;
import com.johnsproject.jpge2.dto.Vertex;
import com.johnsproject.jpge2.library.GraphicsLibrary;
import com.johnsproject.jpge2.library.MathLibrary;
import com.johnsproject.jpge2.library.MatrixLibrary;
import com.johnsproject.jpge2.library.VectorLibrary;
import com.johnsproject.jpge2.dto.LightType;
import com.johnsproject.jpge2.dto.ShaderDataBuffer;
import com.johnsproject.jpge2.shader.FlatTriangle;
import com.johnsproject.jpge2.shader.Shader;
import com.johnsproject.jpge2.shader.databuffers.ForwardDataBuffer;

public class SpotLightShadowShader implements Shader {

	private static final int LIGHT_RANGE = MathLibrary.FP_ONE * 1000;
	
	private static final byte VECTOR_X = VectorLibrary.VECTOR_X;
	private static final byte VECTOR_Y = VectorLibrary.VECTOR_Y;
	private static final byte VECTOR_Z = VectorLibrary.VECTOR_Z;

	private static final int FP_ONE = MathLibrary.FP_ONE;
	
	private static final short SHADOW_BIAS = 500;
	
	private final GraphicsLibrary graphicsLibrary;
	private final MatrixLibrary matrixLibrary;
	private final VectorLibrary vectorLibrary;

	private final FlatTriangle triangle;
	
	private int[][] viewMatrix;
	private int[][] projectionMatrix;
	private final int[][] lightMatrix;
	
	private int[] lightFrustum;
	private final int[] portedFrustum;

	private final Texture shadowMap;

	private List<Light> lights;
	private ForwardDataBuffer shaderData;

	public SpotLightShadowShader() {
		this.graphicsLibrary = new GraphicsLibrary();
		this.matrixLibrary = new MatrixLibrary();
		this.vectorLibrary = new VectorLibrary();
		this.triangle = new FlatTriangle(this);

		this.viewMatrix = matrixLibrary.generate();
		this.projectionMatrix = matrixLibrary.generate();
		this.lightMatrix = matrixLibrary.generate();
		
		this.lightFrustum = new int[Camera.FRUSTUM_SIZE];
		lightFrustum[Camera.FRUSTUM_LEFT] = 0;
		lightFrustum[Camera.FRUSTUM_RIGHT] = FP_ONE;
		lightFrustum[Camera.FRUSTUM_TOP] = 0;
		lightFrustum[Camera.FRUSTUM_BOTTOM] = FP_ONE;
		lightFrustum[Camera.FRUSTUM_NEAR] = FP_ONE / 10;
		lightFrustum[Camera.FRUSTUM_FAR] = FP_ONE * 10000;
		this.portedFrustum = new int[Camera.FRUSTUM_SIZE];
		this.shadowMap = new Texture(64, 64);
	}
	
	public SpotLightShadowShader(int width, int height) {
		this.graphicsLibrary = new GraphicsLibrary();
		this.matrixLibrary = new MatrixLibrary();
		this.vectorLibrary = new VectorLibrary();
		this.triangle = new FlatTriangle(this);

		this.viewMatrix = matrixLibrary.generate();
		this.projectionMatrix = matrixLibrary.generate();
		this.lightMatrix = matrixLibrary.generate();
		
		this.lightFrustum = new int[Camera.FRUSTUM_SIZE];
		lightFrustum[Camera.FRUSTUM_LEFT] = 0;
		lightFrustum[Camera.FRUSTUM_RIGHT] = FP_ONE;
		lightFrustum[Camera.FRUSTUM_TOP] = 0;
		lightFrustum[Camera.FRUSTUM_BOTTOM] = FP_ONE;
		lightFrustum[Camera.FRUSTUM_NEAR] = FP_ONE / 10;
		lightFrustum[Camera.FRUSTUM_FAR] = FP_ONE * 10000;
		this.portedFrustum = new int[Camera.FRUSTUM_SIZE];
		this.shadowMap = new Texture(width, height);
	}
	
	public void update(ShaderDataBuffer shaderDataBuffer) {
		shaderData = (ForwardDataBuffer)shaderDataBuffer;
		this.lights = shaderData.getLights();
		if (shaderData.getSpotLightIndex() == -1) {
			shaderData.setSpotLightFrustum(portedFrustum);
			shaderData.setSpotLightMatrix(lightMatrix);
			shaderData.setSpotShadowMap(shadowMap);
		}
		graphicsLibrary.portFrustum(lightFrustum, shadowMap.getWidth(), shadowMap.getHeight(), portedFrustum);
	}

	public void setup(Camera camera) {
		// reset shadow map
		shadowMap.fill(Integer.MAX_VALUE);
		shaderData.setSpotLightIndex(-1);
		if(lights.size() > 0) {
			Transform lightTransform = lights.get(0).getTransform();
			int[] cameraLocation = camera.getTransform().getLocation();		
			int distance = Integer.MAX_VALUE;
			for (int i = 0; i < lights.size(); i++) {
				Light light = lights.get(i);
				lightTransform = light.getTransform();
				int[] lightPosition = lightTransform.getLocation();
				int dist = vectorLibrary.distance(cameraLocation, lightPosition);
				if ((light.getType() == LightType.SPOT) & (dist < distance) & (dist < LIGHT_RANGE)) {
					distance = dist;
					shaderData.setSpotLightIndex(i);
				}
			}
			if (shaderData.getSpotLightIndex() == -1)
				return;
			graphicsLibrary.viewMatrix(viewMatrix, lightTransform);
			graphicsLibrary.perspectiveMatrix(projectionMatrix, portedFrustum);
			matrixLibrary.multiply(projectionMatrix, viewMatrix, lightMatrix);
		}
	}
	
	public void vertex(int index, Vertex vertex) {
		if (shaderData.getSpotLightIndex() == -1)
			return;
		int[] location = vertex.getLocation();
		vectorLibrary.multiply(location, lightMatrix, location);
		graphicsLibrary.viewport(location, portedFrustum, location);
	}

	public void geometry(Face face) {
		if (shaderData.getSpotLightIndex() == -1)
			return;
		graphicsLibrary.drawTriangle(triangle, face, portedFrustum);
	}

	public void fragment(int[] location) {
		int x = location[VECTOR_X];
		int y = location[VECTOR_Y];
		int z = location[VECTOR_Z];
		if (shadowMap.getPixel(x, y) > z) {
			shadowMap.setPixel(x, y, z + SHADOW_BIAS);
		}
	}

	public void terminate(ShaderDataBuffer shaderDataBuffer) {
		shaderData = (ForwardDataBuffer)shaderDataBuffer;
		shaderData.setSpotLightIndex(-1);
		shaderData.setSpotLightFrustum(null);
		shaderData.setSpotLightMatrix(null);
		shaderData.setSpotShadowMap(null);
	}	
}
