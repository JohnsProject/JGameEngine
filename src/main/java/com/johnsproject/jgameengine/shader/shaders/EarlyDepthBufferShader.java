package com.johnsproject.jgameengine.shader.shaders;

import com.johnsproject.jgameengine.dto.Camera;
import com.johnsproject.jgameengine.dto.FrameBuffer;
import com.johnsproject.jgameengine.dto.GeometryDataBuffer;
import com.johnsproject.jgameengine.dto.Model;
import com.johnsproject.jgameengine.dto.ShaderDataBuffer;
import com.johnsproject.jgameengine.dto.Texture;
import com.johnsproject.jgameengine.dto.VertexDataBuffer;
import com.johnsproject.jgameengine.library.GraphicsLibrary;
import com.johnsproject.jgameengine.library.MatrixLibrary;
import com.johnsproject.jgameengine.library.VectorLibrary;
import com.johnsproject.jgameengine.shader.FlatTriangle;
import com.johnsproject.jgameengine.shader.Shader;

public class EarlyDepthBufferShader implements Shader {
	
	private static final byte DEPTH_BIAS = 50;
	
	private static final byte VECTOR_X = VectorLibrary.VECTOR_X;
	private static final byte VECTOR_Y = VectorLibrary.VECTOR_Y;
	private static final byte VECTOR_Z = VectorLibrary.VECTOR_Z;
	
	private final GraphicsLibrary graphicsLibrary;
	private final MatrixLibrary matrixLibrary;
	private final VectorLibrary vectorLibrary;

	private final int[] portedFrustum;
	
	private final int[] modelMatrix;
	private final int[] normalMatrix;
	private final int[] viewMatrix;
	private final int[] projectionMatrix;
	
	private final FlatTriangle triangle;

	private FrameBuffer frameBuffer;
	
	private Texture depthBuffer;

	public EarlyDepthBufferShader() {
		this.graphicsLibrary = new GraphicsLibrary();
		this.matrixLibrary = new MatrixLibrary();
		this.vectorLibrary = new VectorLibrary();
		this.triangle = new FlatTriangle(this);
		this.portedFrustum = new int[Camera.FRUSTUM_SIZE];
		this.modelMatrix = matrixLibrary.generate();
		this.normalMatrix = matrixLibrary.generate();
		this.viewMatrix = matrixLibrary.generate();
		this.projectionMatrix = matrixLibrary.generate();
	}
	
	public void update(ShaderDataBuffer shaderDataBuffer) {
		this.frameBuffer = shaderDataBuffer.getFrameBuffer();
		if(!shaderDataBuffer.isEarlyDepthBuffering()) {
			final int width = frameBuffer.getWidth();
			final int height = frameBuffer.getHeight();
			depthBuffer = new Texture(width, height);
			shaderDataBuffer.setEarlyDepthBuffering(true);
			shaderDataBuffer.setEarlyDepthBuffer(depthBuffer);
		}
		depthBuffer.fill(Integer.MAX_VALUE);
	}

	public void setup(Camera camera) {
		graphicsLibrary.viewMatrix(viewMatrix, camera.getTransform());
		graphicsLibrary.screenportFrustum(camera.getFrustum(), frameBuffer.getWidth(), frameBuffer.getHeight(), portedFrustum);
		switch (camera.getType()) {
		case ORTHOGRAPHIC:
			graphicsLibrary.orthographicMatrix(projectionMatrix, portedFrustum);
			break;

		case PERSPECTIVE:
			graphicsLibrary.perspectiveMatrix(projectionMatrix, portedFrustum);
			break;
		}
	}
	
	public void setup(Model model) {
		graphicsLibrary.modelMatrix(modelMatrix, model.getTransform());
		graphicsLibrary.normalMatrix(normalMatrix, model.getTransform());
	}

	public void vertex(VertexDataBuffer dataBuffer) {
		int[] location = dataBuffer.getLocation();
		vectorLibrary.matrixMultiply(location, modelMatrix, location);
		vectorLibrary.matrixMultiply(location, viewMatrix, location);
		vectorLibrary.matrixMultiply(location, projectionMatrix, location);
		graphicsLibrary.screenportVector(location, portedFrustum, location);
	}

	public void geometry(GeometryDataBuffer dataBuffer) {
		VertexDataBuffer dataBuffer0 = dataBuffer.getVertexDataBuffer(0);
		VertexDataBuffer dataBuffer1 = dataBuffer.getVertexDataBuffer(1);
		VertexDataBuffer dataBuffer2 = dataBuffer.getVertexDataBuffer(2);
		triangle.setLocation0(dataBuffer0.getLocation());
		triangle.setLocation1(dataBuffer1.getLocation());
		triangle.setLocation2(dataBuffer2.getLocation());
		if(graphicsLibrary.shoelace(triangle) > 0) {
			graphicsLibrary.drawFlatTriangle(triangle, portedFrustum);
		}
	}

	public void fragment(int[] location) {
		int x = location[VECTOR_X];
		int y = location[VECTOR_Y];
		int z = location[VECTOR_Z];
		if (depthBuffer.getPixel(x, y) > z) {
			depthBuffer.setPixel(x, y, z + DEPTH_BIAS);
		}
	}

	public void terminate(ShaderDataBuffer shaderDataBuffer) {
		shaderDataBuffer.setEarlyDepthBuffering(false);
		shaderDataBuffer.setEarlyDepthBuffer(null);
	}
}