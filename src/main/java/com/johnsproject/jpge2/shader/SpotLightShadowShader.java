package com.johnsproject.jpge2.shader;

import java.util.List;

import com.johnsproject.jpge2.dto.Camera;
import com.johnsproject.jpge2.dto.Face;
import com.johnsproject.jpge2.dto.FrameBuffer;
import com.johnsproject.jpge2.dto.Light;
import com.johnsproject.jpge2.dto.Model;
import com.johnsproject.jpge2.dto.ShaderData;
import com.johnsproject.jpge2.dto.Vertex;
import com.johnsproject.jpge2.dto.Light.LightType;
import com.johnsproject.jpge2.processor.CentralProcessor;
import com.johnsproject.jpge2.processor.GraphicsProcessor;
import com.johnsproject.jpge2.processor.MathProcessor;
import com.johnsproject.jpge2.processor.MatrixProcessor;
import com.johnsproject.jpge2.processor.VectorProcessor;

public class SpotLightShadowShader extends Shader{

	private static final byte VECTOR_X = VectorProcessor.VECTOR_X;
	private static final byte VECTOR_Y = VectorProcessor.VECTOR_Y;
	private static final byte VECTOR_Z = VectorProcessor.VECTOR_Z;
	
	private static final byte FP_BITS = MathProcessor.FP_BITS;

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

	public SpotLightShadowShader(CentralProcessor centralProcessor) {
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
		shaderData = (ShaderData)shaderDataBuffer;
		
		this.lights = shaderData.getLights();
		
		if (shaderData.getSpotLightMatrix() == null) {
			shaderData.setSpotLightMatrix(matrixProcessor.generate());
			shaderData.setSpotShadowMap(new FrameBuffer(240, 240));
		}
	}

	@Override
	public void setup(Camera camera) {
		this.camera = camera;
		shaderData.getSpotShadowMap().clearDepthBuffer();		
		shaderData.setSpotLightIndex(-1);
		
		int[] cameraLocation = camera.getTransform().getLocation();		
		int distance = Integer.MAX_VALUE;
		for (int i = 0; i < lights.size(); i++) {
			Light light = lights.get(i);
			int[] lightPosition = light.getTransform().getLocation();
			int dist = vectorProcessor.distance(cameraLocation, lightPosition);
			if ((light.getType() == LightType.SPOT) && (dist < distance)) {
				distance = dist;
				shaderData.setSpotLightIndex(i);
			}
		}
		
		if (shaderData.getSpotLightIndex() < 0)
			return;
		
		lightFrustum[0] = 45 - (lights.get(shaderData.getSpotLightIndex()).getSpotSize() >> (FP_BITS + 1));
		
		graphicsProcessor.setup(shaderData.getSpotShadowMap().getSize(), camera.getCanvas(), this);
		
		matrixProcessor.copy(viewMatrix, MatrixProcessor.MATRIX_IDENTITY);
		matrixProcessor.copy(projectionMatrix, MatrixProcessor.MATRIX_IDENTITY);
		
		graphicsProcessor.getViewMatrix(lights.get(shaderData.getSpotLightIndex()).getTransform(), viewMatrix);
		graphicsProcessor.getPerspectiveMatrix(lightFrustum, projectionMatrix);
		matrixProcessor.multiply(projectionMatrix, viewMatrix, shaderData.getSpotLightMatrix());
	}
	
	@Override
	public void setup(Model model) {
		if (shaderData.getSpotLightIndex() < 0)
			return;
		matrixProcessor.copy(modelMatrix, MatrixProcessor.MATRIX_IDENTITY);
		graphicsProcessor.getModelMatrix(model.getTransform(), modelMatrix);
	}

	@Override
	public void vertex(int index, Vertex vertex) {
		if (shaderData.getSpotLightIndex() < 0)
			return;
		int[] location = vertex.getLocation();
		vectorProcessor.multiply(location, modelMatrix, location);
		vectorProcessor.multiply(location, shaderData.getSpotLightMatrix(), location);
		graphicsProcessor.viewport(location, location);
	}

	@Override
	public void geometry(Face face) {
		if (shaderData.getSpotLightIndex() < 0)
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
//		int color = (location[VECTOR_Z] + 100) >> 3;
//		color = colorProcessor.generate(color, color, color);
//		frameBuffer.setPixel(location[VECTOR_X], location[VECTOR_Y], location[VECTOR_Z] - 1000, (byte) 0, color);
		shaderData.getSpotShadowMap().setPixel(location[VECTOR_X], location[VECTOR_Y], location[VECTOR_Z], (byte) 0, 0);
	}
	
}
