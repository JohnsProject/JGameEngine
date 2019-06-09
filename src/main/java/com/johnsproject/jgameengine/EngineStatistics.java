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
import java.awt.Graphics2D;
import java.util.List;

import com.johnsproject.jgameengine.dto.FrameBuffer;
import com.johnsproject.jgameengine.dto.Model;
import com.johnsproject.jgameengine.event.EngineListener;

public class EngineStatistics implements EngineListener {

	private static final int STATISTICS_X = 10;
	private static final int STATISTICS_Y = 30;
	private static final int STATISTICS_LINE = 13;
	private static final int STATISTICS_WIDTH = 160;
	private static final int STATISTICS_HEIGHT = 115;
	private static final Color STATISTICS_BACKROUND = new Color(230, 230, 230, 200);
	
	private final FrameBuffer frameBuffer;
	private long lastUpdateTime; 
	private long timeLastUpdate; 
	private long elapsed, fps;
	
	public EngineStatistics(FrameBuffer frameBuffer) {
		this.frameBuffer = frameBuffer;
	}
	
	public void start() {
		
	}

	public void update() {
		long currentTime = System.currentTimeMillis();
		if (currentTime - timeLastUpdate > 200) {
			elapsed = currentTime - lastUpdateTime;
			fps = 1000 / elapsed;
			timeLastUpdate = currentTime;
		}
		lastUpdateTime = currentTime;
		GraphicsEngine graphicsEnigne = null;
		List<EngineListener> engineListeners = Engine.getInstance().getEngineListeners(); 
		for (int i = 0; i < engineListeners.size(); i++) {
			EngineListener engineListener = engineListeners.get(i);
			if(engineListener instanceof GraphicsEngine) {
				graphicsEnigne = (GraphicsEngine) engineListener;
			}
		}
		long ramUsage = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) >> 20;
		int maxFPS = Engine.getInstance().getFixedUpdateRate();
		int frameBufferWidth = 0;
		int frameBufferHeight = 0;
		int shadersCount = 0;
		int verticesCount = 0;
		int trianglesCount = 0;
		if (graphicsEnigne != null) {
			frameBufferWidth = graphicsEnigne.getFrameBuffer().getWidth();
			frameBufferHeight = graphicsEnigne.getFrameBuffer().getHeight();
			shadersCount = graphicsEnigne.getShaders().size();
			List<Model> models = graphicsEnigne.getScene().getModels();
			for (int i = 0; i < models.size(); i++) {
				Model model = models.get(i);
				verticesCount += model.getMesh().getVertices().length;
				trianglesCount += model.getMesh().getFaces().length;
			}
		}
		Graphics2D g = frameBuffer.getImage().createGraphics();
		g.setColor(STATISTICS_BACKROUND);
		g.fillRect(STATISTICS_X, STATISTICS_Y, STATISTICS_WIDTH, STATISTICS_HEIGHT);
		g.setColor(Color.black);
		int currentLine = 1;
		g.drawString("====== Statistics ======", STATISTICS_X * 2 , STATISTICS_Y + STATISTICS_LINE * currentLine + 3);
		currentLine++;
		g.drawString("CPU time", STATISTICS_X * 2, STATISTICS_Y + STATISTICS_LINE * currentLine + 3);
		g.drawString(elapsed + " ms", STATISTICS_X * 11, STATISTICS_Y + STATISTICS_LINE * currentLine + 3);
		currentLine++;
		g.drawString("RAM usage", STATISTICS_X * 2, STATISTICS_Y + STATISTICS_LINE * currentLine + 3);
		g.drawString(ramUsage + " MB", STATISTICS_X * 11, STATISTICS_Y + STATISTICS_LINE * currentLine + 3);
		currentLine++;
		if (Engine.getInstance().limitUpdateRate()) {
			g.drawString("FPS", STATISTICS_X * 2, STATISTICS_Y + STATISTICS_LINE * currentLine + 3);
			g.drawString(fps + " / " + maxFPS, STATISTICS_X * 11, STATISTICS_Y + STATISTICS_LINE * currentLine + 3);
		} else {
			g.drawString("FPS", STATISTICS_X * 2, STATISTICS_Y + STATISTICS_LINE * currentLine + 3);
			g.drawString("" + fps, STATISTICS_X * 11, STATISTICS_Y + STATISTICS_LINE * currentLine + 3);
		}
		currentLine++;
		g.drawString("Framebuffer", STATISTICS_X * 2, STATISTICS_Y + STATISTICS_LINE * currentLine + 3);
		g.drawString(frameBufferWidth + "x" + frameBufferHeight, STATISTICS_X * 11, STATISTICS_Y + STATISTICS_LINE * currentLine + 3);
		currentLine++;
		g.drawString("Shaders", STATISTICS_X * 2, STATISTICS_Y + STATISTICS_LINE * currentLine + 3);
		g.drawString("" + shadersCount, STATISTICS_X * 11, STATISTICS_Y + STATISTICS_LINE * currentLine + 3);
		currentLine++;
		g.drawString("Vertices", STATISTICS_X * 2, STATISTICS_Y + STATISTICS_LINE * currentLine + 3);
		g.drawString("" + verticesCount, STATISTICS_X * 11, STATISTICS_Y + STATISTICS_LINE * currentLine + 3);
		currentLine++;
		g.drawString("Triangles", STATISTICS_X * 2, STATISTICS_Y + STATISTICS_LINE * currentLine + 3);
		g.drawString("" + trianglesCount, STATISTICS_X * 11, STATISTICS_Y + STATISTICS_LINE * currentLine + 3);
		g.dispose();
	}

	public void fixedUpdate() {
		
	}

	public int getPriority() {
		return 10000;
	}

}
