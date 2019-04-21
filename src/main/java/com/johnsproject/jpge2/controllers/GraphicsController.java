package com.johnsproject.jpge2.controllers;

import java.util.List;

import com.johnsproject.jpge2.Engine;
import com.johnsproject.jpge2.EngineListener;
import com.johnsproject.jpge2.EngineOptions;
import com.johnsproject.jpge2.dto.Camera;
import com.johnsproject.jpge2.dto.Face;
import com.johnsproject.jpge2.dto.FrameBuffer;
import com.johnsproject.jpge2.dto.Model;
import com.johnsproject.jpge2.dto.Scene;
import com.johnsproject.jpge2.dto.Vertex;
import com.johnsproject.jpge2.processors.CentralProcessor;
import com.johnsproject.jpge2.processors.VectorProcessor;
import com.johnsproject.jpge2.processors.GraphicsProcessor.Shader;
import com.johnsproject.jpge2.processors.GraphicsProcessor.ShaderDataBuffer;

public class GraphicsController implements EngineListener {
	
	private final int[] location0Cache;
	private final int[] location1Cache;
	private final int[] location2Cache;
	private final int[] normal0Cache;
	private final int[] normal1Cache;
	private final int[] normal2Cache;
	private final int[] normal3Cache;
	
	private final Engine engine;
	private final VectorProcessor vectorProcessor;
	
	private ShaderDataBuffer shaderDataBuffer;
	
	GraphicsController(Engine engine, CentralProcessor processor) {
		this.engine = engine;
		this.vectorProcessor = processor.getVectorProcessor();
		this.location0Cache = vectorProcessor.generate();
		this.location1Cache = vectorProcessor.generate();
		this.location2Cache = vectorProcessor.generate();
		this.normal0Cache = vectorProcessor.generate();
		this.normal1Cache = vectorProcessor.generate();
		this.normal2Cache = vectorProcessor.generate();
		this.normal3Cache = vectorProcessor.generate();
		this.shaderDataBuffer = new ShaderDataBuffer();
		engine.addEngineListener(this);
	}
	
	public void start() { }
	
	public void update() {
		EngineOptions options = engine.getOptions();
		Scene scene = options.getScene();
		FrameBuffer frameBuffer = options.getFrameBuffer();
		List<Shader> shaders = options.getShaders();
		shaderDataBuffer.setFrameBuffer(frameBuffer);
		shaderDataBuffer.setLights(scene.getLights());
		for (int i = 0; i < shaders.size(); i++) {
			Shader shader = shaders.get(i);
			shader.update(shaderDataBuffer);
			for (int j = 0; j < scene.getCameras().size(); j++) {
				Camera camera = scene.getCameras().get(j);
				for (int k = 0; k < scene.getModels().size(); k++) {
					Model model = scene.getModels().get(k);
					shader.setup(model, camera);
					for (int l = 0; l < model.getFaces().length; l++) {
						Face face = model.getFace(l);
						if (face.getMaterial().getShaderPass() == i) {
							backup(face);
							for (int m = 0; m < face.getVertices().length; m++) {
								Vertex vertex = face.getVertices()[m];
								shader.vertex(m, vertex);
							}
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
}
