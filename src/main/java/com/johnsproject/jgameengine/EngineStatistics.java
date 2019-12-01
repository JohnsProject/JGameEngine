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

import com.johnsproject.jgameengine.event.EngineEvent;
import com.johnsproject.jgameengine.event.EngineListener;
import com.johnsproject.jgameengine.model.Model;

public class EngineStatistics implements EngineListener {

	private static final int STATISTICS_X = 10;
	private static final int STATISTICS_Y = 30;
	private static final int STATISTICS_WIDTH = 180;
	private static final int STATISTICS_HEIGHT = 130;
	private static final Color STATISTICS_BACKROUND = Color.WHITE;
	
	private GraphicsEngine graphicsEngine;
	private TextArea textArea;
	private long averageUpdates;
	private long loops;
	
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
	
	
	public void start(EngineEvent e) {
		
	}
	
	public void fixedUpdate(EngineEvent e) {
		String output = getOutput(e);
		if(textArea == null) {
			System.out.println(output);
		} else {
			textArea.setSize(STATISTICS_WIDTH, STATISTICS_HEIGHT);
			textArea.setText(output);
		}
	}

	public void update(EngineEvent e) {
		
	}

	public int getLayer() {
		return GRAPHICS_ENGINE_LAYER - 1;
	}
	
	private String getOutput(EngineEvent e) {
		final long ramUsage = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) >> 20;
		final int maxUpdateRate = Engine.getInstance().getUpdateRate();
		int frameBufferWidth = 0;
		int frameBufferHeight = 0;
		int verticesCount = 0;
		int trianglesCount = 0;
		long elapsedTime = e.getElapsedUpdateTime();
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
		}
		for (int i = 0; i < e.getScene().getModels().size(); i++) {
			Model model = e.getScene().getModels().get(i);
			verticesCount += model.getMesh().getVertices().length;
			trianglesCount += model.getMesh().getFaces().length;
		}
		String output = "";
		output += "======== Statistics ========" + "\n";
		output += "CPU time\t" + elapsedTime + " ms" + "\n";
		output += "RAM usage\t" + ramUsage + " MB" + "\n";
		elapsedTime += 1;
		if (Engine.getInstance().limitUpdateRate()) {
			elapsedTime += e.getSleepTime();
			output += "Updates / s\t" + (1000 / elapsedTime) + " / " + maxUpdateRate + "\n";
		} else {
			output += "Updates / s\t" + (1000 / elapsedTime) + "\n";
		}
		averageUpdates += 1000 / elapsedTime;
		loops++;
		output += "Average U / s\t" + (averageUpdates / loops) + "\n";
		if(loops >= 100) {
			averageUpdates = averageUpdates / loops;
			loops = 1;
		}
		output += "Framebuffer\t" + frameBufferWidth + "x" + frameBufferHeight + "\n";
		output += "Vertices\t\t" + verticesCount + "\n";
		output += "Triangles\t" + trianglesCount + "\n";
		return output;
	}
}
