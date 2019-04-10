package com.johnsproject.jpge2.controllers;

import java.util.List;

import com.johnsproject.jpge2.Engine;
import com.johnsproject.jpge2.EngineListener;
import com.johnsproject.jpge2.dto.Camera;
import com.johnsproject.jpge2.dto.Face;
import com.johnsproject.jpge2.dto.FrameBuffer;
import com.johnsproject.jpge2.dto.Model;
import com.johnsproject.jpge2.dto.Scene;
import com.johnsproject.jpge2.dto.Vertex;
import com.johnsproject.jpge2.processors.GraphicsProcessor.Shader;

public class GraphicsController implements EngineListener {
	
	public GraphicsController() {
		Engine.getInstance().addEngineListener(this);
	}
	
	public void start() { }
	
	public void update() {
		Scene scene = Engine.getInstance().getOptions().getScene();
		FrameBuffer frameBuffer = Engine.getInstance().getOptions().getFrameBuffer();
		List<Shader> shaders = Engine.getInstance().getOptions().getShaders();
		for (int i = 0; i < shaders.size(); i++) {
			shaders.get(i).main(scene.getLights(), frameBuffer);
		}
		for (int i = 0; i < scene.getCameras().size(); i++) {
			Camera camera = scene.getCameras().get(i);
			for (int j = 0; j < scene.getModels().size(); j++) {
				Model model = scene.getModels().get(j);
				for (int l = 0; l < model.getFaces().length; l++) {
					for (int k = 0; k < shaders.size(); k++) {
						shaders.get(k).setup(model, camera);
					}
					Face face = model.getFace(l);
					for (int k = 0; k < face.getVertices().length; k++) {
						Vertex vertex = face.getVertices()[k];
						shaders.get(vertex.getMaterial().getShaderPass()).vertex(k, vertex);
					}
					shaders.get(face.getMaterial().getShaderPass()).geometry(face);
				}
			}
		}
	}
	
	public void fixedUpdate() { }

	public int getPriority() {
		return 1000;
	}

}
