package com.johnsproject.jgameengine.event;

import com.johnsproject.jgameengine.model.Scene;

public class EngineEvent {

	private final Scene scene;
	private final long elapsedUpdateTime;
	private final long sleepTime;
	
	public EngineEvent(Scene scene, long elapsedUpdateTime, long sleepTime) {
		this.scene = scene;
		this.elapsedUpdateTime = elapsedUpdateTime;
		this.sleepTime = sleepTime;
	}

	public Scene getScene() {
		return scene;
	}

	public long getElapsedUpdateTime() {
		return elapsedUpdateTime;
	}

	public long getSleepTime() {
		return sleepTime;
	}
}
