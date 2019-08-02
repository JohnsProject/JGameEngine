/**
 * MIT License
 *
 * Copyright (c) 2018 John Salomon - John´s Project
 *  
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.johnsproject.jgameengine;

import java.util.ArrayList;
import java.util.List;

import com.johnsproject.jgameengine.event.EngineEvent;
import com.johnsproject.jgameengine.event.EngineListener;
import com.johnsproject.jgameengine.model.Scene;

public class Engine {

	private static Engine engine = new Engine();

	public static Engine getInstance() {
		return engine;
	}

	private Scene scene;
	private Thread engineThread;
	private final List<EngineListener> engineListeners;
	private int maxUpdateSkip;
	private int updateRate;
	private boolean limitUpdateRate;
	private volatile boolean running;

	private Engine() {
		running = false;
		updateRate = 30;
		maxUpdateSkip = 10;
		limitUpdateRate = true;
		engineListeners = new ArrayList<EngineListener>();
		startEngineLoop();
	}

	public void start() {
		running = true;
	}
	
	public void stop() {
		running = false;
	}
	
	private void startEngineLoop() {
		running = true;
		engineThread = new Thread(new Runnable() {
			public void run() {
				long nextUpdateTime = System.currentTimeMillis();
				long currentTime = 0;
				long elapsedTime = 0;
				long sleepTime = 0;
				final int timePerUpdate = 1000 / getFixedUpdateRate();
				int loops = 0;
				while (true) {
					if (!running) {
						try {
							Thread.sleep(30);
							continue;
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					loops = 0;
					currentTime = System.currentTimeMillis();
					final int listernerCount = engineListeners.size();
					final EngineEvent event = new EngineEvent(scene, elapsedTime, sleepTime);
					while (currentTime > nextUpdateTime && loops < getMaxUpdateSkip()) {
						for (int i = 0; i < listernerCount; i++) {
							engineListeners.get(i).fixedUpdate(event);
						}
						nextUpdateTime += timePerUpdate;
						loops++;
					}
					for (int i = 0; i < listernerCount; i++) {
						engineListeners.get(i).update(event);
					}
					elapsedTime = System.currentTimeMillis() - currentTime;
					if((loops == 1) & limitUpdateRate()) {
						sleepTime = nextUpdateTime - currentTime;
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
		engineThread.setName("EngineThread");
		engineThread.start();
	}

	public void addEngineListener(EngineListener listener) {
		listener.start(new EngineEvent(scene, 0, 0));
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

	public int getFixedUpdateRate() {
		return updateRate;
	}
	
	public void setFixedUpdateRate(int updateRate) {
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
