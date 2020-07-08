package com.johnsproject.jgameengine;

import java.util.ArrayList;
import java.util.List;

import com.johnsproject.jgameengine.event.EngineEvent;
import com.johnsproject.jgameengine.event.EngineListener;
import com.johnsproject.jgameengine.model.Scene;
import com.johnsproject.jgameengine.util.FixedPointUtils;

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
	private long currentTime;
	private long previousTime;
	private long elapsedTime;
	private long updateTime;
	private int loops;

	private Engine() {
		this.running = false;
		this.updateRate = 30;
		this.maxUpdateSkip = 5;
		this.limitUpdateRate = false;
		this.engineListeners = new ArrayList<EngineListener>();
		startEngineLoop();
	}

	public void start() {
		currentTime = getTime();
		running = true;
	}
	
	public void stop() {
		running = false;
	}
	
	private void startEngineLoop() {
		engineThread = new Thread(new Runnable() {
			public void run() {
				updateEngine();
			}
		});
		engineThread.setName("JGameEngine");
		engineThread.start();
	}
	
	private void updateEngine() {
		while (true) {
			if (running) {
				elapsedTime = getTime() - previousTime;
				previousTime = getTime();
				updateTime = 1000 / updateRate;
				loops = 0;
				callFixedUpdate();
				callDynamicUpdate(loops << FixedPointUtils.FP_BIT);
				limitUpdateRateSleep(updateTime - elapsedTime);
			} else {
				sleep();
			}
		}
	}
	
	private void callFixedUpdate() {
		EngineEvent event = new EngineEvent(scene, (int) elapsedTime, 0, 0);
		while (((currentTime - getTime()) < 0) && (loops < maxUpdateSkip)) {
			for (int i = 0; i < engineListeners.size(); i++) {
				engineListeners.get(i).fixedUpdate(event);
			}
			currentTime += updateTime;
			loops++;
		}
	}
	
	private void callDynamicUpdate(int deltaTime) {
		EngineEvent event = new EngineEvent(scene, (int) elapsedTime, 0, deltaTime);
		for (int i = 0; i < engineListeners.size(); i++) {
			engineListeners.get(i).dynamicUpdate(event);
		}
	}
	
	private void limitUpdateRateSleep(long sleepTime) {
		if(limitUpdateRate) {
			if (sleepTime > 0) {
				try {
					Thread.sleep(sleepTime);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private void sleep() {
		try {
			Thread.sleep(30);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	private long getTime() {
		return System.currentTimeMillis();
	}

	public void addEngineListener(EngineListener listener) {
		listener.initialize(new EngineEvent(scene, 0, 0, 0));
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
