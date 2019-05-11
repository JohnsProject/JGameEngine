package com.johnsproject.jpge2.controller;

import com.johnsproject.jpge2.Engine;
import com.johnsproject.jpge2.processor.CentralProcessor;

public class CentralController {

	private final GraphicsController graphicsController;
	private final InputController inputController;
	
	public CentralController(Engine engine, CentralProcessor processor) {
		inputController = new InputController();
		graphicsController = new GraphicsController(engine, processor);
	}

	public GraphicsController getGraphicsController() {
		return graphicsController;
	}

	public InputController getInputController() {
		return inputController;
	}	
}
