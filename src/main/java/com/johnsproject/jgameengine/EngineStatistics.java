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

import java.awt.Color;
import java.awt.TextArea;
import java.util.List;

import com.johnsproject.jgameengine.event.EngineListener;
import com.johnsproject.jgameengine.model.Model;

public class EngineStatistics implements EngineListener {

	private static final int STATISTICS_X = 10;
	private static final int STATISTICS_Y = 30;
	private static final int STATISTICS_WIDTH = 160;
	private static final int STATISTICS_HEIGHT = 130;
	private static final Color STATISTICS_BACKROUND = Color.WHITE;
	
	private GraphicsEngine graphicsEngine;
	private TextArea textArea;
	private long lastUpdateTime; 
	private long timeLastUpdate; 
	private long elapsed, ups;
	
	public EngineStatistics(EngineWindow window) {
		this.textArea = new TextArea("", 0, 0, TextArea.SCROLLBARS_NONE);
		textArea.setLocation(STATISTICS_X, STATISTICS_Y);
		textArea.setSize(STATISTICS_WIDTH, STATISTICS_HEIGHT);
		textArea.setEditable(false);
		textArea.setBackground(STATISTICS_BACKROUND);
		window.add(textArea, 0);
	}
	
	public EngineStatistics() {
		
	}
	
	
	public void start() {
	}

	public void update() {
		long currentTime = System.currentTimeMillis();
		if (currentTime - timeLastUpdate > 200) {
			elapsed = currentTime - lastUpdateTime;
			ups = 1000 / elapsed;
			timeLastUpdate = currentTime;
		}
		lastUpdateTime = currentTime;
	}
	

	public void fixedUpdate() {
		if(textArea == null) {
			statsLog();
		} else {
			textArea.setSize(STATISTICS_WIDTH, STATISTICS_HEIGHT);
			statsUI();
		}
	}

	public int getLayer() {
		return GRAPHICS_ENGINE_LAYER;
	}	
	
	private void statsUI() {
		long ramUsage = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) >> 20;
		int maxUpdateRate = Engine.getInstance().getFixedUpdateRate();
		int frameBufferWidth = 0;
		int frameBufferHeight = 0;
		int shadersCount = 0;
		int verticesCount = 0;
		int trianglesCount = 0;
		if (graphicsEngine == null) {
			List<EngineListener> engineListeners = Engine.getInstance().getEngineListeners(); 
			for (int i = 0; i < engineListeners.size(); i++) {
				EngineListener engineListener = engineListeners.get(i);
				if(engineListener instanceof GraphicsEngine) {
					graphicsEngine = (GraphicsEngine) engineListener;
				}
			}
		} else {
			frameBufferWidth = graphicsEngine.getFrameBuffer().getWidth();
			frameBufferHeight = graphicsEngine.getFrameBuffer().getHeight();
			shadersCount += graphicsEngine.getPreprocessingShaders().size();
			shadersCount += graphicsEngine.getPostprocessingShaders().size();
			List<Model> models = graphicsEngine.getScene().getModels();
			for (int i = 0; i < models.size(); i++) {
				Model model = models.get(i);
				verticesCount += model.getMesh().getVertices().length;
				trianglesCount += model.getMesh().getFaces().length;
			}
		}
		String output = "";
		output += "====== Statistics ======" + "\n";
		output += "CPU time\t" + elapsed + " ms" + "\n";
		output += "RAM usage\t" + ramUsage + " MB" + "\n";
		if (Engine.getInstance().limitUpdateRate()) {
			output += "Updates / s\t" + ups + " / " + maxUpdateRate + "\n";
		} else {
			output += "Updates / s\t" + ups + "\n";
		}
		output += "Framebuffer\t" + frameBufferWidth + "x" + frameBufferHeight + "\n";
		output += "Shaders Count\t" + shadersCount + "\n";
		output += "Vertices\t\t" + verticesCount + "\n";
		output += "Triangles\t" + trianglesCount + "\n";
		textArea.setText(output);
	}
	
	private void statsLog() {
		long ramUsage = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) >> 20;
		int maxUpdateRate = Engine.getInstance().getFixedUpdateRate();
		int frameBufferWidth = 0;
		int frameBufferHeight = 0;
		int shadersCount = 0;
		int verticesCount = 0;
		int trianglesCount = 0;
		if (graphicsEngine == null) {
			List<EngineListener> engineListeners = Engine.getInstance().getEngineListeners(); 
			for (int i = 0; i < engineListeners.size(); i++) {
				EngineListener engineListener = engineListeners.get(i);
				if(engineListener instanceof GraphicsEngine) {
					graphicsEngine = (GraphicsEngine) engineListener;
				}
			}
		} else {
			frameBufferWidth = graphicsEngine.getFrameBuffer().getWidth();
			frameBufferHeight = graphicsEngine.getFrameBuffer().getHeight();
			shadersCount += graphicsEngine.getPreprocessingShaders().size();
			shadersCount += graphicsEngine.getPostprocessingShaders().size();
			List<Model> models = graphicsEngine.getScene().getModels();
			for (int i = 0; i < models.size(); i++) {
				Model model = models.get(i);
				verticesCount += model.getMesh().getVertices().length;
				trianglesCount += model.getMesh().getFaces().length;
			}
		}
		String output = "";
		output += "====== Statistics ======" + "\n";
		output += "CPU time\t" + elapsed + " ms" + "\n";
		output += "RAM usage\t" + ramUsage + " MB" + "\n";
		if (Engine.getInstance().limitUpdateRate()) {
			output += "Updates / s\t" + ups + " / " + maxUpdateRate + "\n";
		} else {
			output += "Updates / s\t" + ups + "\n";
		}
		output += "Framebuffer\t" + frameBufferWidth + "x" + frameBufferHeight + "\n";
		output += "Shaders Count\t" + shadersCount + "\n";
		output += "Vertices\t\t" + verticesCount + "\n";
		output += "Triangles\t" + trianglesCount + "\n";
		System.out.println(output);
	}
}
