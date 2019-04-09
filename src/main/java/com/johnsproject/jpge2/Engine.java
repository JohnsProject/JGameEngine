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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.johnsproject.jpge2.dto.FrameBuffer;
import com.johnsproject.jpge2.dto.Scene;

public class Engine {

	private static Engine engine = new Engine();

	public static Engine getInstance() {
		return engine;
	}

	private Thread engineThread;
	private EngineSettings settings;
	private FrameBuffer frameBuffer;
	private List<EngineListener> engineListeners = new ArrayList<EngineListener>();
	private Scene scene;

	public Engine() {
		settings = new EngineSettings();
		frameBuffer = new FrameBuffer();
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
				// initialize the controllers
				new EngineControllersInitializer();
				for (int i = 0; i < engineListeners.size(); i++) {
					engineListeners.get(i).start();
				}
				while (true) {
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

	public FrameBuffer getFrameBuffer() {
		return frameBuffer;
	}

	public Scene getScene() {
		return scene;
	}

	public void setScene(Scene scene) {
		this.scene = scene;
	}

	public void addEngineListener(EngineListener listener) {
		engineListeners.add(listener);
		sortListeners();
	}

	public void removeEngineListener(EngineListener listener) {
		engineListeners.remove(listener);
		sortListeners();
	}

	private void sortListeners() {
		for (int i = 0; i < engineListeners.size() - 1; i++) {
			int min_idx = i;
			for (int j = i + 1; j < engineListeners.size(); j++)
				if (engineListeners.get(j).getPriority() < engineListeners.get(min_idx).getPriority())
					min_idx = j;

			EngineListener temp = engineListeners.get(min_idx);
			engineListeners.set(min_idx, engineListeners.get(i));
			engineListeners.set(i, temp);
		}
	}
}
