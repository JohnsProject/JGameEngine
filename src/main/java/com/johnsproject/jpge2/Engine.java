package com.johnsproject.jpge2;

import java.util.ArrayList;
import java.util.List;

import com.johnsproject.jpge2.dto.GraphicsBuffer;
import com.johnsproject.jpge2.dto.Scene;

public class Engine {

	private static Engine engine = new Engine();
	
	public static Engine getInstance() {
		return engine;
	}
	
	private Thread engineThread;
	private EnginePipeline pipeline;
	private EngineSettings settings;
	private GraphicsBuffer graphicsBuffer;
	private List<GraphicsBufferListener> graphicsBufferListeners = new ArrayList<GraphicsBufferListener>();
	private Scene scene;
	
	public Engine() {
		pipeline = new EnginePipeline();
		settings = new EngineSettings();
		graphicsBuffer = new GraphicsBuffer();
		scene = new Scene();
		engineThread = new Thread(new Runnable() {
			public void run() {
				while(true) {
					pipeline.call(scene, graphicsBuffer);
					for (int i = 0; i < graphicsBufferListeners.size(); i++) {
						graphicsBufferListeners.get(i).graphicsBufferUpdate(graphicsBuffer);
					}
					try {
						Thread.sleep(20);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		});
		engineThread.start();
	}

	public EnginePipeline getPipeline() {
		return pipeline;
	}

	public void setPipeline(EnginePipeline pipeline) {
		this.pipeline = pipeline;
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
}
