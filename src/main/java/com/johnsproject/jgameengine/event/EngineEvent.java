package com.johnsproject.jgameengine.event;

import com.johnsproject.jgameengine.Scene;

public class EngineEvent {

	private final Scene scene;
	private final int elapsedUpdateTime;
	private final int sleepTime;
	private final int deltaTime;
	
	public EngineEvent(Scene scene, int elapsedUpdateTime, int sleepTime, int deltaTime) {
		this.scene = scene;
		this.elapsedUpdateTime = elapsedUpdateTime;
		this.sleepTime = sleepTime;
		this.deltaTime = deltaTime;
	}

	public Scene getScene() {
		return scene;
	}

	public int getElapsedUpdateTime() {
		return elapsedUpdateTime;
	}

	public int getSleepTime() {
		return sleepTime;
	}

	public int getDeltaTime() {
		return deltaTime;
	}
}
