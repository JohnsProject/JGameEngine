package com.johnsproject.jpge2.controller;

import com.johnsproject.jpge2.Engine;
import com.johnsproject.jpge2.processor.CentralProcessor;

public class CentralController {

	private final GraphicsController graphicsController;
	
	public CentralController(Engine engine, CentralProcessor processor) {
		graphicsController = new GraphicsController(engine, processor);
	}

	public GraphicsController getGraphicsController() {
		return graphicsController;
	}	
}