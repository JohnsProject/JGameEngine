package com.johnsproject.jpge2.shader;

import java.util.List;

import com.johnsproject.jpge2.dto.Camera;
import com.johnsproject.jpge2.dto.Face;
import com.johnsproject.jpge2.dto.Light;
import com.johnsproject.jpge2.dto.Light.LightType;
import com.johnsproject.jpge2.dto.ShaderData;
import com.johnsproject.jpge2.dto.Texture;
import com.johnsproject.jpge2.dto.Transform;
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
			shaderData.setDirectionalShadowMap(new Texture(320, 320));
		}
	}

	@Override
	public void setup(Camera camera) {
		this.camera = camera;
		Texture shadowMap = shaderData.getDirectionalShadowMap();
		// reset shadow map
		for (int i = 0; i < shadowMap.getPixels().length; i++) {
			shadowMap.getPixels()[i] = Integer.MAX_VALUE;
		}		
		shaderData.setDirectionalLightIndex(-1);
		
		int[] cameraLocation = camera.getTransform().getLocation();		
		int distance = Integer.MAX_VALUE;
		for (int i = 0; i < lights.size(); i++) {
			Light light = lights.get(i);
			int[] lightPosition = light.getTransform().getLocation();
			int dist = vectorProcessor.distance(cameraLocation, lightPosition);
			if ((light.getType() == LightType.DIRECTIONAL) && (dist < distance)) {
				distance = dist;
				shaderData.setDirectionalLightIndex(i);
			}
		}
		
		if (shaderData.getDirectionalLightIndex() < 0)
			return;
		
		graphicsProcessor.setup(shaderData.getDirectionalShadowMap().getSize(), camera.getCanvas(), this);
		
		matrixProcessor.copy(viewMatrix, MatrixProcessor.MATRIX_IDENTITY);
		matrixProcessor.copy(projectionMatrix, MatrixProcessor.MATRIX_IDENTITY);
		
		Transform transform = lights.get(shaderData.getDirectionalLightIndex()).getTransform();
		int[] direction = lights.get(shaderData.getDirectionalLightIndex()).getDirection();
		int[] location = transform.getLocation();
		int x = location[VECTOR_X];
		int y = location[VECTOR_Y];
		int z = location[VECTOR_Z];
		// place directional light 'infinitely' far away
		final int factor = 200;
		location[VECTOR_X] = direction[VECTOR_X] * factor + cameraLocation[VECTOR_X];
		location[VECTOR_Y] = direction[VECTOR_Y] * factor - (cameraLocation[VECTOR_Y] >> 3);
		location[VECTOR_Z] = -direction[VECTOR_Z] * factor;
		graphicsProcessor.getViewMatrix(transform, viewMatrix);
		graphicsProcessor.getOrthographicMatrix(lightFrustum, projectionMatrix);
		matrixProcessor.multiply(projectionMatrix, viewMatrix, shaderData.getDirectionalLightMatrix());
		location[VECTOR_X] = x;
		location[VECTOR_Y] = y;
		location[VECTOR_Z] = z;
	}

	@Override
	public void vertex(int index, Vertex vertex) {
		if (shaderData.getDirectionalLightIndex() < 0)
			return;
		int[] location = vertex.getLocation();
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
		Texture shadowMap = shaderData.getDirectionalShadowMap();
		if (shadowMap.getPixel(location[VECTOR_X], location[VECTOR_Y]) > location[VECTOR_Z]) {
			shadowMap.setPixel(location[VECTOR_X], location[VECTOR_Y], location[VECTOR_Z]);
		}
	}
}