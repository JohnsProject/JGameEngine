package com.johnsproject.jpge2.controllers;

import com.johnsproject.jpge2.Engine;
import com.johnsproject.jpge2.EngineListener;
import com.johnsproject.jpge2.dto.Camera;
import com.johnsproject.jpge2.dto.Face;
import com.johnsproject.jpge2.dto.GraphicsBuffer;
import com.johnsproject.jpge2.dto.Model;
import com.johnsproject.jpge2.dto.Scene;
import com.johnsproject.jpge2.dto.Vertex;
import com.johnsproject.jpge2.processors.GraphicsProcessor;
import com.johnsproject.jpge2.processors.GraphicsProcessor.Shader;

public class GraphicsController implements EngineListener {
	
	public GraphicsController() {
		Engine.getInstance().addEngineListener(this);
	}
	
	public void update() {
		Scene scene = Engine.getInstance().getScene();
		GraphicsBuffer graphicsBuffer = Engine.getInstance().getGraphicsBuffer();
		graphicsBuffer.clearFrameBuffer();
		graphicsBuffer.clearDepthBuffer();
		for (int i = 0; i < scene.getCameras().size(); i++) {
			Camera camera = scene.getCameras().get(i);
			for (int j = 0; j < scene.getModels().size(); j++) {
				Model model = scene.getModels().get(j);
				for (int l = 0; l < model.getMaterials().length; l++) {
					Shader.model = model;
					Shader.camera = camera;
					Shader.lights = scene.getLights();
				}
				for (int l = 0; l < model.getFaces().length; l++) {
					Face face = model.getFace(l);
					face.reset();
					for (int k = 0; k < face.getVertices().length; k++) {
						Vertex vertex = face.getVertices()[k];
						vertex.reset();
						vertex.getMaterial().getShader().vertex(vertex);
					}
					if (face.getMaterial().getShader().geometry(face)) {
						GraphicsProcessor.drawFace(face, graphicsBuffer);
					}
				}
			}
		}
	}

	public void fixedUpdate() {
		
	}

	public int getPriority() {
		return 1000;
	}

}
