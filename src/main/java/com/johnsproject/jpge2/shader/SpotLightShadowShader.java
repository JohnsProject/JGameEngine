package com.johnsproject.jpge2.shader;

import java.util.List;

import com.johnsproject.jpge2.dto.Camera;
import com.johnsproject.jpge2.dto.Face;
import com.johnsproject.jpge2.dto.Light;
import com.johnsproject.jpge2.dto.Vertex;
import com.johnsproject.jpge2.primitive.Vector;
import com.johnsproject.jpge2.primitive.Texture;
import com.johnsproject.jpge2.dto.LightType;
import com.johnsproject.jpge2.processor.CentralProcessor;
import com.johnsproject.jpge2.processor.GraphicsProcessor;
import com.johnsproject.jpge2.processor.MathProcessor;
import com.johnsproject.jpge2.processor.MatrixProcessor;
import com.johnsproject.jpge2.processor.VectorProcessor;

public class SpotLightShadowShader extends Shader{

	private static final byte VECTOR_X = Vector.VECTOR_X;
	private static final byte VECTOR_Y = Vector.VECTOR_Y;
	private static final byte VECTOR_Z = Vector.VECTOR_Z;
	
	private static final byte FP_BITS = MathProcessor.FP_BITS;

	private final GraphicsProcessor graphicsProcessor;

	private final int[][] viewMatrix;
	private final int[][] projectionMatrix;
	private final int[][] lightMatrix;
	
	private final int[] lightFrustum;
	private final int[] portedCanvas;
	
	private final Texture shadowMap;

	private List<Light> lights;
	private ForwardDataBuffer shaderData;

	public SpotLightShadowShader(CentralProcessor centralProcessor) {
		super(centralProcessor);
		this.matrixProcessor = centralProcessor.getMatrixProcessor();
		this.vectorProcessor = centralProcessor.getVectorProcessor();
		this.graphicsProcessor = centralProcessor.getGraphicsProcessor();

		this.viewMatrix = matrixProcessor.generate();
		this.projectionMatrix = matrixProcessor.generate();
		this.lightMatrix = matrixProcessor.generate();
		
		this.lightFrustum = vectorProcessor.generate(30, 0, 10000);
		this.portedCanvas = vectorProcessor.generate();
		this.shadowMap = new Texture(320, 320);
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
			int dist = vectorProcessor.distance(cameraLocation, lightPosition);
			if ((light.getType() == LightType.SPOT) & (dist < distance) & (dist < shaderData.getLightRange())) {
				distance = dist;
				shaderData.setSpotLightIndex(i);
			}
		}
		
		if (shaderData.getSpotLightIndex() < 0)
			return;
		
		graphicsProcessor.portCanvas(camera.getCanvas(), shadowMap.getSize(), portedCanvas);
		
		lightFrustum[0] = 45 - (lights.get(shaderData.getSpotLightIndex()).getSpotSize() >> (FP_BITS + 1));
		
		matrixProcessor.copy(viewMatrix, MatrixProcessor.MATRIX_IDENTITY);
		matrixProcessor.copy(projectionMatrix, MatrixProcessor.MATRIX_IDENTITY);
		
		graphicsProcessor.getViewMatrix(lights.get(shaderData.getSpotLightIndex()).getTransform(), viewMatrix);
		graphicsProcessor.getPerspectiveMatrix(portedCanvas, lightFrustum, projectionMatrix);
		matrixProcessor.multiply(projectionMatrix, viewMatrix, lightMatrix);
	}

	@Override
	public void vertex(int index, Vertex vertex) {
		if (shaderData.getSpotLightIndex() < 0)
			return;
		int[] location = vertex.getLocation();
		vectorProcessor.multiply(location, lightMatrix, location);
		graphicsProcessor.viewport(location, portedCanvas, location);
	}

	@Override
	public void geometry(Face face) {
		if (shaderData.getSpotLightIndex() < 0)
			return;
		int[] location1 = face.getVertex(0).getLocation();
		int[] location2 = face.getVertex(1).getLocation();
		int[] location3 = face.getVertex(2).getLocation();
		
		if (!graphicsProcessor.isBackface(location1, location2, location3)
				&& graphicsProcessor.isInsideFrustum(location1, location2, location3, portedCanvas, lightFrustum)) {
			graphicsProcessor.drawTriangle(location1, location2, location3, portedCanvas, this);
		}
	}

	@Override
	public void fragment(int[] location, int[] barycentric) {
//		int color = (location[VECTOR_Z] + 100) >> 3;
//		color = colorProcessor.generate(color, color, color);
//		frameBuffer.setPixel(location[VECTOR_X], location[VECTOR_Y], location[VECTOR_Z] - 1000, (byte) 0, color);
		if (shadowMap.getPixel(location[VECTOR_X], location[VECTOR_Y]) > location[VECTOR_Z]) {
			shadowMap.setPixel(location[VECTOR_X], location[VECTOR_Y], location[VECTOR_Z]);
		}
	}
	
}
