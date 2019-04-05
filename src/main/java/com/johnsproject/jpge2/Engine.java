package com.johnsproject.jpge2;

import java.util.ArrayList;
import java.util.List;

import com.johnsproject.jpge2.dto.GraphicsBuffer;
import com.johnsproject.jpge2.dto.Scene;
import com.johnsproject.jpge2.processing.GraphicsProcessor;

public class Engine {

	private static Engine engine = new Engine();
	
	public static Engine getInstance() {
		return engine;
	}
	
	private Thread engineThread;
	private EngineSettings settings;
	private GraphicsBuffer graphicsBuffer;
	private List<GraphicsBufferListener> graphicsBufferListeners = new ArrayList<GraphicsBufferListener>();
	private List<EngineListener> engineListeners = new ArrayList<EngineListener>();
	private Scene scene;
	
	public Engine() {
		settings = new EngineSettings();
		graphicsBuffer = new GraphicsBuffer();
		scene = new Scene();
		startEngineLoop();
	}
	
	private void startEngineLoop() {
		engineThread = new Thread(new Runnable() {
			long nextUpateTick = System.currentTimeMillis();
			long current = System.currentTimeMillis();
			int updateSkipRate = 0;
			int loops = 0;
			public void run() {
				while(true) {
					loops = 0;
					updateSkipRate = 1000 / getSettings().getUpdateRate();
					current = System.currentTimeMillis();
					while (current > nextUpateTick && loops < getSettings().getMaxUpdateSkip()) {
						for (int i = 0; i < engineListeners.size(); i++) {
							engineListeners.get(i).fixedUpdate();
						}
						nextUpateTick += updateSkipRate;
						loops++;
					}
					for (int i = 0; i < engineListeners.size(); i++) {
						engineListeners.get(i).update();
					}
					GraphicsProcessor.process(scene, graphicsBuffer);
					for (int i = 0; i < graphicsBufferListeners.size(); i++) {
						graphicsBufferListeners.get(i).graphicsBufferUpdate(graphicsBuffer);
					}
					long sleepTime = nextUpateTick - current;
					if (sleepTime >= 0) {
						try {
							Thread.sleep(sleepTime);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			}
		});
		engineThread.start();
	}

	public EngineSettings getSettings() {
		return settings;
	}

	public GraphicsBuffer getGraphicsBuffer() {
		return graphicsBuffer;
	}
	
	public Scene getScene() {
		return scene;
	}

	public void setScene(Scene scene) {
		this.scene = scene;
	}
	
	public void addGraphicsBufferListener(GraphicsBufferListener listener) {
		graphicsBufferListeners.add(listener);
	}
	
	public void removeGraphicsBufferListener(GraphicsBufferListener listener) {
		graphicsBufferListeners.remove(listener);
	}
	
	public void addEngineListener(EngineListener listener) {
		engineListeners.add(listener);
	}
	
	public void removeEngineListener(EngineListener listener) {
		engineListeners.remove(listener);
	}
}
