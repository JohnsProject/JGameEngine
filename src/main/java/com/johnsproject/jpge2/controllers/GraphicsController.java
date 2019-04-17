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

public class GraphicsController implements EngineListener {
	
	public GraphicsController(Engine engine) {
		engine.addEngineListener(this);
	}
	
	public void start() { }
	
	public void update() {
		Scene scene = Engine.getInstance().getOptions().getScene();
		EngineOptions options = Engine.getInstance().getOptions();
		FrameBuffer frameBuffer = options.getFrameBuffer();
		List<Shader> shaders = options.getShaders();
		int preShadersCount = options.getPreprocessingShadersCount();
		int postShadersCount = options.getPostprocessingShadersCount();
		for (int i = 0; i < shaders.size(); i++) {
			Shader shader = shaders.get(i);
			shader.update(scene.getLights(), frameBuffer);
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
