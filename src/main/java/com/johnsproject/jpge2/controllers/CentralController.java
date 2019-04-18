package com.johnsproject.jpge2.controllers;

import com.johnsproject.jpge2.Engine;

public class CentralController {

	private final GraphicsController graphicsController;
	
	public CentralController(Engine engine) {
		graphicsController = new GraphicsController(engine);
	}

	public GraphicsController getGraphicsController() {
		return graphicsController;
	}	
}
