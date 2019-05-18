package com.johnsproject.jpge2.shader.shaders;

import java.util.List;

import com.johnsproject.jpge2.dto.Camera;
import com.johnsproject.jpge2.dto.Face;
import com.johnsproject.jpge2.dto.Light;
import com.johnsproject.jpge2.dto.Texture;
import com.johnsproject.jpge2.dto.Transform;
import com.johnsproject.jpge2.dto.Vertex;
import com.johnsproject.jpge2.library.GraphicsLibrary;
import com.johnsproject.jpge2.library.MatrixLibrary;
import com.johnsproject.jpge2.library.VectorLibrary;
import com.johnsproject.jpge2.dto.LightType;
import com.johnsproject.jpge2.shader.Shader;
import com.johnsproject.jpge2.shader.ShaderDataBuffer;
import com.johnsproject.jpge2.shader.databuffers.ForwardDataBuffer;

public class SpotLightShadowShader extends Shader {

	private static final byte SHADOW_BIAS = 50;
	
	private final GraphicsLibrary graphicsLibrary;
	private final MatrixLibrary matrixLibrary;
	private final VectorLibrary vectorLibrary;

	private int[][] viewMatrix;
	private int[][] projectionMatrix;
	private final int[][] lightMatrix;
	
	private int[] lightFrustum;
	private final int[] portedCanvas;

	private final Texture shadowMap;

	private List<Light> lights;
	private ForwardDataBuffer shaderData;

	public SpotLightShadowShader() {
		super(0);
		this.graphicsLibrary = new GraphicsLibrary();
		this.matrixLibrary = new MatrixLibrary();
		this.vectorLibrary = new VectorLibrary();

		this.viewMatrix = matrixLibrary.generate();
		this.projectionMatrix = matrixLibrary.generate();
		this.lightMatrix = matrixLibrary.generate();
		
		this.lightFrustum = vectorLibrary.generate(30, 0, 100000);
		this.portedCanvas = vectorLibrary.generate();
		this.shadowMap = new Texture(320, 320);
	}
	
	public SpotLightShadowShader(int width, int height) {
		super(0);
		this.graphicsLibrary = new GraphicsLibrary();
		this.matrixLibrary = new MatrixLibrary();
		this.vectorLibrary = new VectorLibrary();

		this.viewMatrix = matrixLibrary.generate();
		this.projectionMatrix = matrixLibrary.generate();
		this.lightMatrix = matrixLibrary.generate();
		
		this.lightFrustum = vectorLibrary.generate(30, 0, 100000);
		this.portedCanvas = vectorLibrary.generate();
		this.shadowMap = new Texture(width, height);
	}
	
	@Override
	public void update(ShaderDataBuffer shaderDataBuffer) {
		shaderData = (ForwardDataBuffer)shaderDataBuffer;
		
		this.lights = shaderData.getLights();
		
		if (shaderData.getSpotLightMatrix() == null) {
			shaderData.setSpotLightCanvas(portedCanvas);
			shaderData.setSpotLightMatrix(lightMatrix);
			shaderData.setSpotShadowMap(shadowMap);
		}
	}

	@Override
	public void setup(Camera camera) {
		// reset shadow map
		for (int i = 0; i < shadowMap.getPixelBuffer().length; i++) {
			shadowMap.getPixelBuffer()[i] = Integer.MAX_VALUE;
		}		
		shaderData.setSpotLightIndex(-1);
		
		int[] cameraLocation = camera.getTransform().getLocation();		
		int distance = Integer.MAX_VALUE;
		for (int i = 0; i < lights.size(); i++) {
			Light light = lights.get(i);
			int[] lightPosition = light.getTransform().getLocation();
			int dist = vectorLibrary.distance(cameraLocation, lightPosition);
			if ((light.getType() == LightType.SPOT) & (dist < distance) & (dist < shaderData.getLightRange())) {
				distance = dist;
				shaderData.setSpotLightIndex(i);
			}
		}
		
		if (shaderData.getSpotLightIndex() < 0)
			return;
		
		graphicsLibrary.portCanvas(camera.getCanvas(), shadowMap.getWidth(), shadowMap.getHeight(), portedCanvas);
		
		lightFrustum[0] = 45 - (lights.get(shaderData.getSpotLightIndex()).getSpotSize() >> (FP_BITS + 1));
		
		matrixLibrary.copy(viewMatrix, MatrixLibrary.MATRIX_IDENTITY);
		matrixLibrary.copy(projectionMatrix, MatrixLibrary.MATRIX_IDENTITY);
		
		Transform lightTransform = lights.get(shaderData.getSpotLightIndex()).getTransform();
		graphicsLibrary.viewMatrix(viewMatrix, lightTransform);
		graphicsLibrary.perspectiveMatrix(projectionMatrix, lightFrustum);
		matrixLibrary.multiply(projectionMatrix, viewMatrix, lightMatrix);
	}

	@Override
	public void vertex(int index, Vertex vertex) {
		if (shaderData.getSpotLightIndex() < 0)
			return;
		int[] location = vertex.getLocation();
		vectorLibrary.multiply(location, lightMatrix, location);
		graphicsLibrary.viewport(location, portedCanvas, location);
	}

	@Override
	public void geometry(Face face) {
		if (shaderData.getSpotLightIndex() < 0)
			return;
		int[] location1 = face.getVertex(0).getLocation();
		int[] location2 = face.getVertex(1).getLocation();
		int[] location3 = face.getVertex(2).getLocation();
		
		if (!graphicsLibrary.isBackface(location1, location2, location3)
				&& graphicsLibrary.isInsideFrustum(location1, location2, location3, portedCanvas, lightFrustum)) {
			graphicsLibrary.drawTriangle(location1, location2, location3, portedCanvas, this);
		}
	}

	@Override
	public void fragment(int[] location) {
//		int color = (location[VECTOR_Z] + 100) >> 3;
//		color = colorProcessor.generate(color, color, color);
//		frameBuffer.setPixel(location[VECTOR_X], location[VECTOR_Y], location[VECTOR_Z] - 1000, (byte) 0, color);
		if (shadowMap.getPixel(location[VECTOR_X], location[VECTOR_Y]) > location[VECTOR_Z]) {
			shadowMap.setPixel(location[VECTOR_X], location[VECTOR_Y], location[VECTOR_Z] + SHADOW_BIAS);
		}
	}
	
}