package com.johnsproject.jgameengine;

import com.johnsproject.jgameengine.event.EngineEvent;
import com.johnsproject.jgameengine.math.FixedPointMath;
import com.johnsproject.jgameengine.model.Scene;

public final class Engine {

	private static Engine engine = new Engine();

	public static Engine getInstance() {
		return engine;
	}

	private EngineObject mainObject;
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
		this.mainObject = null;
		this.running = false;
		this.updateRate = 30;
		this.maxUpdateSkip = 5;
		this.limitUpdateRate = false;
		startEngineLoop();
	}

	public void resume() {
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
				callDynamicUpdate(loops << FixedPointMath.FP_BIT);
				limitUpdateRateSleep(updateTime - elapsedTime);
			} else {
				sleep();
			}
		}
	}
	
	private void callFixedUpdate() {
		EngineEvent event = new EngineEvent(scene, (int) elapsedTime, 0, 0);
		while (((currentTime - getTime()) < 0) && (loops < maxUpdateSkip)) {
			for (int i = 0; i < 10000; i++) {
				if(mainObject.hasLayer(i)) {
					mainObject.fixedUpdate(event);
				}
				callFixedUpdate(mainObject, event, i);
			}
			currentTime += updateTime;
			loops++;
		}
	}
	
	private void callFixedUpdate(EngineObject engineObject, EngineEvent event, int layer) {
		if(engineObject.hasChildren()) {
			for (int i = 0; i < engineObject.getChildrenCount(); i++) {
				final EngineObject childObject = engineObject.getChild(i);
				if(childObject.hasLayer(layer)) {
					childObject.fixedUpdate(event);
				}
				if(childObject.hasChildren() && childObject.hasChildWithLayer(layer)) {
					callFixedUpdate(childObject, event, layer);
				}
			}
		}
	}
	
	private void callDynamicUpdate(int deltaTime) {
		EngineEvent event = new EngineEvent(scene, (int) elapsedTime, 0, deltaTime);
		for (int i = 0; i < 10000; i++) {
			if(mainObject.hasLayer(i)) {
				mainObject.dynamicUpdate(event);
			}
			callDynamicUpdate(mainObject, event, i);
		}
	}
	
	private void callDynamicUpdate(EngineObject engineObject, EngineEvent event, int layer) {
		if(engineObject.hasChildren()) {
			for (int i = 0; i < engineObject.getChildrenCount(); i++) {
				final EngineObject childObject = engineObject.getChild(i);
				if(childObject.hasLayer(layer)) {
					childObject.dynamicUpdate(event);
				}
				if(childObject.hasChildren() && childObject.hasChildWithLayer(layer)) {
					callDynamicUpdate(childObject, event, layer);
				}
			}
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
	
	public void callInitialize() {
		mainObject.initialize();
		callInitialize(mainObject);
	}
	
	private void callInitialize(EngineObject engineObject) {
		if(engineObject.hasChildren()) {
			for (int i = 0; i < engineObject.getChildrenCount(); i++) {
				final EngineObject childObject = engineObject.getChild(i);
				childObject.initialize();
				if(childObject.hasChildren()) {
					callInitialize(childObject);
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

	public void setMainObject(EngineObject mainObject) {
		this.mainObject = mainObject;
		callInitialize();
		resume();
	}
	
	public EngineObject getMainObject() {
		return mainObject;
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
}
