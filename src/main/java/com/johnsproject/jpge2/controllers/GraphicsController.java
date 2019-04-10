package com.johnsproject.jpge2.controllers;

import com.johnsproject.jpge2.Engine;
import com.johnsproject.jpge2.EngineListener;
import com.johnsproject.jpge2.dto.Camera;
import com.johnsproject.jpge2.dto.Face;
import com.johnsproject.jpge2.dto.FrameBuffer;
import com.johnsproject.jpge2.dto.Material;
import com.johnsproject.jpge2.dto.Model;
import com.johnsproject.jpge2.dto.Scene;
import com.johnsproject.jpge2.dto.Vertex;

public class GraphicsController implements EngineListener {
	
	public GraphicsController() {
		Engine.getInstance().addEngineListener(this);
	}
	
	public void start() { }
	
	public void update() {
		Scene scene = Engine.getInstance().getOptions().getScene();
		FrameBuffer frameBuffer = Engine.getInstance().getOptions().getFrameBuffer();
		frameBuffer.clearColorBuffer();
		frameBuffer.clearDepthBuffer();
		for (int i = 0; i < scene.getCameras().size(); i++) {
			Camera camera = scene.getCameras().get(i);
			for (int j = 0; j < scene.getModels().size(); j++) {
				Model model = scene.getModels().get(j);
				for (int l = 0; l < model.getMaterials().length; l++) {
					Material material = model.getMaterials()[i];
					material.getShader().setup(model, camera, scene.getLights(), frameBuffer);
				}
				for (int l = 0; l < model.getFaces().length; l++) {
					Face face = model.getFace(l);
					face.reset();
					for (int k = 0; k < face.getVertices().length; k++) {
						Vertex vertex = face.getVertices()[k];
						vertex.reset();
						vertex.getMaterial().getShader().vertex(k, vertex);
					}
					face.getMaterial().getShader().geometry(face);
				}
			}
		}
	}
	
	public void fixedUpdate() { }

	public int getPriority() {
		return 1000;
	}

}
