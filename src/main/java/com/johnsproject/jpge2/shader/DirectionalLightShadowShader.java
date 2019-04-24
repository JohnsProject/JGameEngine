package com.johnsproject.jpge2.shader;

import java.util.List;

import com.johnsproject.jpge2.dto.Camera;
import com.johnsproject.jpge2.dto.Face;
import com.johnsproject.jpge2.dto.FrameBuffer;
import com.johnsproject.jpge2.dto.Light;
import com.johnsproject.jpge2.dto.Light.LightType;
import com.johnsproject.jpge2.dto.Model;
import com.johnsproject.jpge2.dto.ShaderData;
import com.johnsproject.jpge2.dto.Vertex;
import com.johnsproject.jpge2.processor.CentralProcessor;
import com.johnsproject.jpge2.processor.GraphicsProcessor;
import com.johnsproject.jpge2.processor.MatrixProcessor;
import com.johnsproject.jpge2.processor.VectorProcessor;

public class DirectionalLightShadowShader extends Shader {

	private static final byte VECTOR_X = VectorProcessor.VECTOR_X;
	private static final byte VECTOR_Y = VectorProcessor.VECTOR_Y;
	private static final byte VECTOR_Z = VectorProcessor.VECTOR_Z;

	private final MatrixProcessor matrixProcessor;
	private final VectorProcessor vectorProcessor;
	private final GraphicsProcessor graphicsProcessor;

	private final int[][] modelMatrix;
	private final int[][] viewMatrix;
	private final int[][] projectionMatrix;
	
	private final int[] lightFrustum;

	private Camera camera;
	private List<Light> lights;
	private ShaderData shaderData;

	public DirectionalLightShadowShader(CentralProcessor centralProcessor) {
		super(centralProcessor);
		this.matrixProcessor = centralProcessor.getMatrixProcessor();
		this.vectorProcessor = centralProcessor.getVectorProcessor();
		this.graphicsProcessor = centralProcessor.getGraphicsProcessor();

		this.modelMatrix = matrixProcessor.generate();
		this.viewMatrix = matrixProcessor.generate();
		this.projectionMatrix = matrixProcessor.generate();
		this.lightFrustum = vectorProcessor.generate(30, 0, 10000);
	}
	
	@Override
	public void update(ShaderDataBuffer shaderDataBuffer) {
		this.shaderData = (ShaderData)shaderDataBuffer;
		
		this.lights = shaderData.getLights();
		if (shaderData.getDirectionalLightMatrix() == null) {
			shaderData.setDirectionalLightMatrix(matrixProcessor.generate());
			shaderData.setDirectionalShadowMap(new FrameBuffer(640, 640));
		}
		
		shaderData.getDirectionalShadowMap().clearDepthBuffer();		
		shaderData.setDirectionalLightIndex(-1);
		
		int distance = Integer.MAX_VALUE;
		for (int i = 0; i < lights.size(); i++) {
			Light light = lights.get(i);
			int dist = vectorProcessor.magnitude(light.getTransform().getLocation());
			if ((light.getType() == LightType.DIRECTIONAL) && (dist < distance)) {
				distance = dist;
				shaderData.setDirectionalLightIndex(i);
			}
		}
	}

	@Override
	public void setup(Model model, Camera camera) {
		this.camera = camera;
		
		if (shaderData.getDirectionalLightIndex() < 0)
			return;
		graphicsProcessor.setup(shaderData.getDirectionalShadowMap().getSize(), camera.getCanvas(), this);
		
		matrixProcessor.copy(viewMatrix, MatrixProcessor.MATRIX_IDENTITY);
		matrixProcessor.copy(projectionMatrix, MatrixProcessor.MATRIX_IDENTITY);
		matrixProcessor.copy(modelMatrix, MatrixProcessor.MATRIX_IDENTITY);
		
		graphicsProcessor.getModelMatrix(model.getTransform(), modelMatrix);
		graphicsProcessor.getViewMatrix(lights.get(shaderData.getDirectionalLightIndex()).getTransform(), viewMatrix);
		graphicsProcessor.getOrthographicMatrix(lightFrustum, projectionMatrix);
		matrixProcessor.multiply(projectionMatrix, viewMatrix, shaderData.getDirectionalLightMatrix());
	}

	@Override
	public void vertex(int index, Vertex vertex) {
		if (shaderData.getDirectionalLightIndex() < 0)
			return;
		int[] location = vertex.getLocation();
		vectorProcessor.multiply(location, modelMatrix, location);
		vectorProcessor.multiply(location, shaderData.getDirectionalLightMatrix(), location);
		graphicsProcessor.viewport(location, location);
	}

	@Override
	public void geometry(Face face) {
		if (shaderData.getDirectionalLightIndex() < 0)
			return;
		int[] location1 = face.getVertex(0).getLocation();
		int[] location2 = face.getVertex(1).getLocation();
		int[] location3 = face.getVertex(2).getLocation();
		
		if (!graphicsProcessor.isBackface(location1, location2, location3)
				&& graphicsProcessor.isInsideFrustum(location1, location2, location3, camera.getFrustum())) {
			graphicsProcessor.drawTriangle(location1, location2, location3);
		}
	}

	@Override
	public void fragment(int[] location, int[] barycentric) {
		shaderData.getDirectionalShadowMap().setPixel(location[VECTOR_X], location[VECTOR_Y], location[VECTOR_Z], (byte) 0, 0);
	}
}
