package com.johnsproject.jpge2.controller;

import com.johnsproject.jpge2.Engine;

public class CentralController {

	private final GraphicsController graphicsController;
	private final InputController inputController;
	
	public CentralController(Engine engine) {
		inputController = new InputController();
		graphicsController = new GraphicsController();
		engine.addEngineListener(graphicsController);
		engine.addEngineListener(inputController);
	}

	public GraphicsController getGraphicsController() {
		return graphicsController;
	}

	public InputController getInputController() {
		return inputController;
	}	
}
