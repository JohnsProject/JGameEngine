package com.johnsproject.jgameengine;

import java.util.ArrayList;
import java.util.List;

import com.johnsproject.jgameengine.event.EngineEvent;
import com.johnsproject.jgameengine.event.EngineListener;
import com.johnsproject.jgameengine.math.FixedPointMath;
import com.johnsproject.jgameengine.model.Scene;

public final class Engine {

	private static Engine engine = new Engine();

	public static Engine getInstance() {
		return engine;
	}

	private final List<EngineListener> engineListeners;
	private Scene scene;
	private Thread engineThread;
	private int maxUpdateSkip;
	private int updateRate;
	private boolean limitUpdateRate;
	private volatile boolean running;

	private Engine() {
		this.running = false;
		this.updateRate = 30;
		this.maxUpdateSkip = 5;
		this.limitUpdateRate = false;
		this.engineListeners = new ArrayList<EngineListener>();
		startEngineLoop();
	}

	public void start() {
		running = true;
	}
	
	public void stop() {
		running = false;
	}
	
	private void startEngineLoop() {
		engineThread = new Thread(new Runnable() {
			public void run() {
				long currentTime = 0;
				long previousTime = 0;
				long elapsedTime = 0;
				long sleepTime = 0;
				int deltaTime = 0;
				int loops = 0;
				long updateTime = 1000 / getUpdateRate();
				while (true) {
					if (!running) {
						try {
							currentTime = 0;
							Thread.sleep(30);
							continue;
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					if(currentTime == 0) {
						currentTime = getTime();
					}
					elapsedTime = getTime() - previousTime;
					previousTime = getTime();
					updateTime = 1000 / getUpdateRate();
					final int listernerCount = engineListeners.size();
					EngineEvent event = new EngineEvent(scene, (int) elapsedTime, 0, 0);
					loops = 0;
					while (currentTime - getTime() < 0 && loops < getMaxUpdateSkip()) {
						for (int i = 0; i < listernerCount; i++) {
							engineListeners.get(i).fixedUpdate(event);
						}
						currentTime += updateTime;
						loops++;
					}
					deltaTime = loops << FixedPointMath.FP_BIT;
					event = new EngineEvent(scene, (int) elapsedTime, 0, deltaTime);
					for (int i = 0; i < listernerCount; i++) {
						engineListeners.get(i).update(event);
					}
					if(limitUpdateRate()) {
						sleepTime = updateTime - elapsedTime;
						if (sleepTime > 0) {
							try {
								Thread.sleep(sleepTime);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
					}
				}
			}
		});
		engineThread.setName("JGameEngine");
		engineThread.start();
	}
	
	private long getTime() {
		return System.currentTimeMillis();
	}

	public void addEngineListener(EngineListener listener) {
		listener.start(new EngineEvent(scene, 0, 0, 0));
		engineListeners.add(listener);
		sortListeners();
	}

	public void removeEngineListener(EngineListener listener) {
		engineListeners.remove(listener);
		sortListeners();
	}
	
	public List<EngineListener> getEngineListeners() {
		return engineListeners;
	}

	public int getUpdateRate() {
		return updateRate;
	}
	
	public void setUpdateRate(int updateRate) {
		this.updateRate = updateRate;
	}
	
	public boolean limitUpdateRate() {
		return limitUpdateRate;
	}

	public void limitUpdateRate(boolean limitUpdateRate) {
		this.limitUpdateRate = limitUpdateRate;
	}

	public int getMaxUpdateSkip() {
		return maxUpdateSkip;
	}
	
	public void setMaxUpdateSkip(int maxUpdateSkip) {
		this.maxUpdateSkip = maxUpdateSkip;
	}
	
	public Scene getScene() {
		return scene;
	}

	public void setScene(Scene scene) {
		this.scene = scene;
	}

	private void sortListeners() {
		final int listenerCount = engineListeners.size();
		for (int i = 0; i < listenerCount; i++) {
			int min_i = i;
			for (int j = i + 1; j < listenerCount; j++) {
				if (engineListeners.get(j).getLayer() < engineListeners.get(min_i).getLayer()) {
					min_i = j;
				}
			}
			EngineListener temp = engineListeners.get(min_i);
			engineListeners.set(min_i, engineListeners.get(i));
			engineListeners.set(i, temp);
		}
	}
}
