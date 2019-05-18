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
package com.johnsproject.jpge2;

import java.util.ArrayList;
import java.util.List;

import com.johnsproject.jpge2.controller.CentralController;
import com.johnsproject.jpge2.event.EngineListener;

public class Engine {

	private static Engine engine = new Engine();

	public static Engine getInstance() {
		return engine;
	}

	private Thread engineThread;
	private List<EngineListener> engineListeners;
	private CentralController controller;
	private int maxUpdateSkip;
	private int updateRate;
	
	private volatile boolean running;

	private Engine() {
		updateRate = 25;
		maxUpdateSkip = 10;
		engineListeners = new ArrayList<EngineListener>();
	}

	public void start() {
		this.controller = new CentralController(this);
		startEngineLoop();
	}
	
	public void stop() {
		running = false;
	}
	
	private void startEngineLoop() {
		running = true;
		
		engineThread = new Thread(new Runnable() {
			
			long nextUpateTick = System.currentTimeMillis();
			long current = System.currentTimeMillis();
			int updatesToCatchUp = 0;
			int loops = 0;

			public void run() {
				for (int i = 0; i < engineListeners.size(); i++) {
					engineListeners.get(i).start();
				}
				while (running) {
					loops = 0;
					updatesToCatchUp = 1000 / getUpdateRate();
					current = System.currentTimeMillis();
					while (current > nextUpateTick && loops < getMaxUpdateSkip()) {
						for (int i = 0; i < engineListeners.size(); i++) {
							engineListeners.get(i).fixedUpdate();
						}
						nextUpateTick += updatesToCatchUp;
						loops++;
					}
					for (int i = 0; i < engineListeners.size(); i++) {
						engineListeners.get(i).update();
					}
//					long sleepTime = nextUpateTick - current;
//					if (sleepTime >= 0) {
//						try {
//							Thread.sleep(sleepTime);
//						} catch (InterruptedException e) {
//							e.printStackTrace();
//						}
//					}
				}
			}
		});
		engineThread.start();
	}

	public void addEngineListener(EngineListener listener) {
		engineListeners.add(listener);
		sortListeners();
	}

	public void removeEngineListener(EngineListener listener) {
		engineListeners.remove(listener);
		sortListeners();
	}

	public CentralController getController() {
		return controller;
	}

	public int getUpdateRate() {
		return updateRate;
	}
	
	public void setUpdateRate(int updateRate) {
		this.updateRate = updateRate;
	}
	
	public int getMaxUpdateSkip() {
		return maxUpdateSkip;
	}
	
	public void setMaxUpdateSkip(int maxUpdateSkip) {
		this.maxUpdateSkip = maxUpdateSkip;
	}

	private void sortListeners() {
		for (int i = 0; i < engineListeners.size() - 1; i++) {
			int min_i = i;
			for (int j = i + 1; j < engineListeners.size(); j++) {
				if (engineListeners.get(j).getPriority() < engineListeners.get(min_i).getPriority()) {
					min_i = j;
				}
			}
			EngineListener temp = engineListeners.get(min_i);
			engineListeners.set(min_i, engineListeners.get(i));
			engineListeners.set(i, temp);
		}
	}
}
