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
import com.johnsproject.jpge2.processors.GraphicsProcessor.Shader;
import com.johnsproject.jpge2.processors.GraphicsProcessor.ShaderDataBuffer;

public class GraphicsController implements EngineListener {
	
	private Engine engine;
	
	GraphicsController(Engine engine) {
		this.engine = engine;
		engine.addEngineListener(this);
	}
	
	public void start() { }
	
	public void update() {
		EngineOptions options = engine.getOptions();
		Scene scene = options.getScene();
		FrameBuffer frameBuffer = options.getFrameBuffer();
		List<Shader> shaders = options.getShaders();
		int preShadersCount = options.getPreprocessingShadersCount();
		int postShadersCount = options.getPostprocessingShadersCount();
		for (int i = 0; i < shaders.size(); i++) {
			Shader shader = shaders.get(i);
			ShaderDataBuffer dataBuffer = shader.getDataBuffer();
			dataBuffer.setFrameBuffer(frameBuffer);
			dataBuffer.setLights(scene.getLights());
			shader.setDataBuffer(dataBuffer);
			for (int j = 0; j < scene.getCameras().size(); j++) {
				Camera camera = scene.getCameras().get(j);
				for (int k = 0; k < scene.getModels().size(); k++) {
					Model model = scene.getModels().get(k);
					shader.setup(model, camera);
					for (int l = 0; l < model.getFaces().length; l++) {
						Face face = model.getFace(l);
						if ((face.getMaterial().getShaderPass() == i - preShadersCount) || (i < preShadersCount) || (i > postShadersCount)) {
							for (int m = 0; m < face.getVertices().length; m++) {
								Vertex vertex = face.getVertices()[m];
								shader.vertex(m, vertex);
							}
							shader.geometry(face);
						}
					}
				}
			}
		}
	}
	
	public void fixedUpdate() { }

	public int getPriority() {
		return 10000;
	}

}
