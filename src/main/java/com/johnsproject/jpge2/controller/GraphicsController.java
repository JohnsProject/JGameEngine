package com.johnsproject.jpge2.controller;

import java.util.ArrayList;
import java.util.List;

import com.johnsproject.jpge2.Engine;
import com.johnsproject.jpge2.EngineListener;
import com.johnsproject.jpge2.dto.Camera;
import com.johnsproject.jpge2.dto.Face;
import com.johnsproject.jpge2.dto.FrameBuffer;
import com.johnsproject.jpge2.dto.Model;
import com.johnsproject.jpge2.dto.Scene;
import com.johnsproject.jpge2.dto.Vertex;
import com.johnsproject.jpge2.processor.CentralProcessor;
import com.johnsproject.jpge2.processor.GraphicsProcessor;
import com.johnsproject.jpge2.processor.MatrixProcessor;
import com.johnsproject.jpge2.processor.VectorProcessor;
import com.johnsproject.jpge2.shader.Shader;
import com.johnsproject.jpge2.shader.ShaderDataBuffer;
import com.johnsproject.jpge2.shader.databuffers.ForwardDataBuffer;
import com.johnsproject.jpge2.shader.shaders.DirectionalLightShadowShader;
import com.johnsproject.jpge2.shader.shaders.FlatSpecularShader;
import com.johnsproject.jpge2.shader.shaders.GouraudSpecularShader;
import com.johnsproject.jpge2.shader.shaders.PhongSpecularShader;
import com.johnsproject.jpge2.shader.shaders.SpotLightShadowShader;

public class GraphicsController implements EngineListener {
	
	private final int[] location0Cache;
	private final int[] location1Cache;
	private final int[] location2Cache;
	private final int[] normal0Cache;
	private final int[] normal1Cache;
	private final int[] normal2Cache;
	private final int[] normal3Cache;
	
	private final int[][] modelMatrix;
	private final int[][] normalMatrix;
	
	private final Engine engine;
	private final VectorProcessor vectorProcessor;
	private final MatrixProcessor matrixProcessor;
	private final GraphicsProcessor graphicsProcessor;
	
	private final List<Shader> shaders;
	private ShaderDataBuffer shaderDataBuffer;
	private FrameBuffer frameBuffer;
	
	private int preShadersCount;
	private int shadersCount;
	private int postShadersCount;
	
	GraphicsController(Engine engine, CentralProcessor processor) {
		this.engine = engine;
		this.vectorProcessor = processor.getVectorProcessor();
		this.matrixProcessor = processor.getMatrixProcessor();
		this.graphicsProcessor = processor.getGraphicsProcessor();
		this.location0Cache = vectorProcessor.generate();
		this.location1Cache = vectorProcessor.generate();
		this.location2Cache = vectorProcessor.generate();
		this.normal0Cache = vectorProcessor.generate();
		this.normal1Cache = vectorProcessor.generate();
		this.normal2Cache = vectorProcessor.generate();
		this.normal3Cache = vectorProcessor.generate();
		
		this.modelMatrix = matrixProcessor.generate();
		this.normalMatrix = matrixProcessor.generate();
		
		this.shaderDataBuffer = new ForwardDataBuffer();
		shaders = new ArrayList<Shader>();
		engine.addEngineListener(this);
		
		addPreprocessingShader(new SpotLightShadowShader(processor));
		addPreprocessingShader(new DirectionalLightShadowShader(processor));
		addShader(new FlatSpecularShader(processor));
//		addPostprocessingShader(new FXAAShader(processor));
	}
	
	public void start() { }
	
	public void update() {
		Scene scene = engine.getScene();
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
					matrixProcessor.copy(modelMatrix, MatrixProcessor.MATRIX_IDENTITY);
					matrixProcessor.copy(normalMatrix, MatrixProcessor.MATRIX_IDENTITY);
					graphicsProcessor.getModelMatrix(model.getTransform(), modelMatrix);
					graphicsProcessor.getNormalMatrix(model.getTransform(), normalMatrix);
					for (int f = 0; f < model.getFaces().length; f++) {
						Face face = model.getFace(f);
						if ((face.getMaterial().getShaderIndex() == s - preShadersCount)
								| (s < preShadersCount) | (s > preShadersCount + shadersCount)) {
							backup(face);
							for (int v = 0; v < face.getVertices().length; v++) {
								Vertex vertex = face.getVertices()[v];
								vectorProcessor.multiply(vertex.getLocation(), modelMatrix, vertex.getLocation());
								vectorProcessor.multiply(vertex.getNormal(), normalMatrix, vertex.getNormal());
								shader.vertex(v, vertex);
							}
							vectorProcessor.multiply(face.getNormal(), normalMatrix, face.getNormal());
							shader.geometry(face);
							restore(face);
						}
					}
				}
			}
		}
	}
	
	private void backup(Face face) {
		vectorProcessor.copy(location0Cache, face.getVertex(0).getLocation());
		vectorProcessor.copy(location1Cache, face.getVertex(1).getLocation());
		vectorProcessor.copy(location2Cache, face.getVertex(2).getLocation());
		vectorProcessor.copy(normal0Cache, face.getVertex(0).getNormal());
		vectorProcessor.copy(normal1Cache, face.getVertex(1).getNormal());
		vectorProcessor.copy(normal2Cache, face.getVertex(2).getNormal());
		vectorProcessor.copy(normal3Cache, face.getNormal());
	}
	
	private void restore(Face face) {
		vectorProcessor.copy(face.getVertex(0).getLocation(), location0Cache);
		vectorProcessor.copy(face.getVertex(1).getLocation(), location1Cache);
		vectorProcessor.copy(face.getVertex(2).getLocation(), location2Cache);
		vectorProcessor.copy(face.getVertex(0).getNormal(), normal0Cache);
		vectorProcessor.copy(face.getVertex(1).getNormal(), normal1Cache);
		vectorProcessor.copy(face.getVertex(2).getNormal(), normal2Cache);
		vectorProcessor.copy(face.getNormal(), normal3Cache);
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
	
	public List<Shader> getShaders() {
		return shaders;
	}
	
	public Shader getShader(int index) {
		return shaders.get(index);
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
		shaders.remove(shader);
		sortShaders(0);
	}
	
	public void removeShader(Shader shader) {
		shadersCount--;
		shaders.remove(shader);
		sortShaders(1);
	}
	
	public void removePostprocessingShader(Shader shader) {
		postShadersCount--;
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
