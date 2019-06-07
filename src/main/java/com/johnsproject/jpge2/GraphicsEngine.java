package com.johnsproject.jpge2;

import java.util.ArrayList;
import java.util.List;

import com.johnsproject.jpge2.dto.Camera;
import com.johnsproject.jpge2.dto.Face;
import com.johnsproject.jpge2.dto.FrameBuffer;
import com.johnsproject.jpge2.dto.Mesh;
import com.johnsproject.jpge2.dto.Model;
import com.johnsproject.jpge2.dto.Scene;
import com.johnsproject.jpge2.dto.ShaderDataBuffer;
import com.johnsproject.jpge2.dto.Vertex;
import com.johnsproject.jpge2.event.EngineListener;
import com.johnsproject.jpge2.library.GraphicsLibrary;
import com.johnsproject.jpge2.library.MatrixLibrary;
import com.johnsproject.jpge2.library.VectorLibrary;
import com.johnsproject.jpge2.shader.Shader;
import com.johnsproject.jpge2.shader.databuffers.ForwardDataBuffer;
import com.johnsproject.jpge2.shader.shaders.DirectionalLightShadowShader;
import com.johnsproject.jpge2.shader.shaders.GouraudSpecularShader;
import com.johnsproject.jpge2.shader.shaders.PointLightShadowShader;
import com.johnsproject.jpge2.shader.shaders.SpotLightShadowShader;

public class GraphicsEngine implements EngineListener {
	
	private final int[] location0Cache;
	private final int[] location1Cache;
	private final int[] location2Cache;
	private final int[] normal0Cache;
	private final int[] normal1Cache;
	private final int[] normal2Cache;
	private final int[] normal3Cache;
	
	private int[][] modelMatrix;
	private int[][] normalMatrix;
	
	private final GraphicsLibrary graphicsLibrary;
	private final MatrixLibrary matrixLibrary;
	private final VectorLibrary vectorLibrary;
	
	private final List<Shader> shaders;
	private ShaderDataBuffer shaderDataBuffer;
	private FrameBuffer frameBuffer;
	private Scene scene;
	
	private int preShadersCount;
	private int shadersCount;
	private int postShadersCount;
	
	public GraphicsEngine(Scene scene, FrameBuffer frameBuffer) {
		this.graphicsLibrary = new GraphicsLibrary();
		this.matrixLibrary = new MatrixLibrary();
		this.vectorLibrary = new VectorLibrary();
		this.location0Cache = vectorLibrary.generate();
		this.location1Cache = vectorLibrary.generate();
		this.location2Cache = vectorLibrary.generate();
		this.normal0Cache = vectorLibrary.generate();
		this.normal1Cache = vectorLibrary.generate();
		this.normal2Cache = vectorLibrary.generate();
		this.normal3Cache = vectorLibrary.generate();
		this.modelMatrix = matrixLibrary.generate();
		this.normalMatrix = matrixLibrary.generate();
		
		this.shaderDataBuffer = new ForwardDataBuffer();
		this.shaders = new ArrayList<Shader>();
		this.scene = scene;
		this.frameBuffer = frameBuffer;
//		addPreprocessingShader(new DirectionalLightShadowShader());
//		addPreprocessingShader(new SpotLightShadowShader());
//		addPreprocessingShader(new PointLightShadowShader());
		addShader(new GouraudSpecularShader());
	}
	
	public void start() { }
	
	public void update() {
		shaderDataBuffer.setFrameBuffer(frameBuffer);
		shaderDataBuffer.setLights(scene.getLights());
		for (int s = 0; s < shaders.size(); s++) {
			Shader shader = shaders.get(s);
			shader.update(shaderDataBuffer);
			for (int c = 0; c < scene.getCameras().size(); c++) {
				Camera camera = scene.getCameras().get(c);
				shader.setup(camera);
				for (int m = 0; m < scene.getModels().size(); m++) {
					Model model = scene.getModels().get(m);
					Mesh mesh = model.getMesh();
					graphicsLibrary.modelMatrix(modelMatrix, model.getTransform());
					graphicsLibrary.normalMatrix(normalMatrix, model.getTransform());
					for (int f = 0; f < mesh.getFaces().length; f++) {
						Face face = mesh.getFace(f);
						if ((face.getMaterial().getShaderIndex() == s - preShadersCount)
								| (s < preShadersCount) | (s > preShadersCount + shadersCount)) {
							backup(face);
							for (int v = 0; v < face.getVertices().length; v++) {
								Vertex vertex = face.getVertices()[v];
								vectorLibrary.multiply(vertex.getLocation(), modelMatrix, vertex.getLocation());
								vectorLibrary.multiply(vertex.getNormal(), normalMatrix, vertex.getNormal());
								shader.vertex(v, vertex);
							}
							vectorLibrary.multiply(face.getNormal(), normalMatrix, face.getNormal());
							shader.geometry(face);
							restore(face);
						}
					}
				}
			}
		}
	}
	
	private void backup(Face face) {
		vectorLibrary.copy(location0Cache, face.getVertex(0).getLocation());
		vectorLibrary.copy(location1Cache, face.getVertex(1).getLocation());
		vectorLibrary.copy(location2Cache, face.getVertex(2).getLocation());
		vectorLibrary.copy(normal0Cache, face.getVertex(0).getNormal());
		vectorLibrary.copy(normal1Cache, face.getVertex(1).getNormal());
		vectorLibrary.copy(normal2Cache, face.getVertex(2).getNormal());
		vectorLibrary.copy(normal3Cache, face.getNormal());
	}
	
	private void restore(Face face) {
		vectorLibrary.copy(face.getVertex(0).getLocation(), location0Cache);
		vectorLibrary.copy(face.getVertex(1).getLocation(), location1Cache);
		vectorLibrary.copy(face.getVertex(2).getLocation(), location2Cache);
		vectorLibrary.copy(face.getVertex(0).getNormal(), normal0Cache);
		vectorLibrary.copy(face.getVertex(1).getNormal(), normal1Cache);
		vectorLibrary.copy(face.getVertex(2).getNormal(), normal2Cache);
		vectorLibrary.copy(face.getNormal(), normal3Cache);
	}
	
	public void fixedUpdate() { }

	public int getPriority() {
		return 10000;
	}

	public ShaderDataBuffer getShaderDataBuffer() {
		return shaderDataBuffer;
	}

	public void setShaderDataBuffer(ShaderDataBuffer shaderDataBuffer) {
		this.shaderDataBuffer = shaderDataBuffer;
	}
	
	public void setFrameBuffer(FrameBuffer frameBuffer) {
		this.frameBuffer = frameBuffer;
	}
	
	public FrameBuffer getFrameBuffer() {
		return frameBuffer;
	}
	
	public Scene getScene() {
		return scene;
	}

	public void setScene(Scene scene) {
		this.scene = scene;
	}
	
	public List<Shader> getShaders() {
		return shaders;
	}
	
	public Shader getPreprocessingShader(int index) {
		return shaders.get(index);
	}
	
	public Shader getShader(int index) {
		return shaders.get(preShadersCount + index);
	}
	
	public Shader getPostprocessingShader(int index) {
		return shaders.get(preShadersCount + shadersCount + index);
	}
	
	public void addPreprocessingShader(Shader shader) {
		preShadersCount++;
		shaders.add(shader);
		sortShaders(0);
	}
	
	public int addShader(Shader shader) {
		shadersCount++;
		shaders.add(shader);
		sortShaders(1);
		return preShadersCount + shadersCount;
	}
	
	public void addPostprocessingShader(Shader shader) {
		postShadersCount++;
		shaders.add(shader);
		sortShaders(2);
	}
	
	public void removePreprocessingShader(Shader shader) {
		preShadersCount--;
		shader.terminate(shaderDataBuffer);
		shaders.remove(shader);
		sortShaders(0);
	}
	
	public void removeShader(Shader shader) {
		shadersCount--;
		shader.terminate(shaderDataBuffer);
		shaders.remove(shader);
		sortShaders(1);
	}
	
	public void removePostprocessingShader(Shader shader) {
		postShadersCount--;
		shader.terminate(shaderDataBuffer);
		shaders.remove(shader);
		sortShaders(2);
	}
	
	public int getPreprocessingShadersCount() {
		return preShadersCount;
	}

	public int getShadersCount() {
		return shadersCount;
	}
	
	public int getPostprocessingShadersCount() {
		return postShadersCount;
	}

	private void sortShaders(int pass) {
		for (int i = 0; i < shaders.size() - 1; i++) {
			int min_i = i;
			for (int j = i + 1; j < shaders.size(); j++) {
				int currentPass = 0;
				if(min_i > preShadersCount) currentPass = 1;
				if(min_i > shadersCount) currentPass = 2;
				if (pass < currentPass) {
					min_i = j;
				}
			}
			Shader temp = shaders.get(min_i);
			shaders.set(min_i, shaders.get(i));
			shaders.set(i, temp);
		}
	}
}
