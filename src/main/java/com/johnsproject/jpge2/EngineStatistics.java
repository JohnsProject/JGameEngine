package com.johnsproject.jpge2;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.List;

import com.johnsproject.jpge2.controller.GraphicsController;
import com.johnsproject.jpge2.dto.FrameBuffer;
import com.johnsproject.jpge2.dto.Model;
import com.johnsproject.jpge2.event.EngineListener;

public class EngineStatistics implements EngineListener{

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
		GraphicsController graphicsController = null;
		List<EngineListener> engineListeners = Engine.getInstance().getEngineListeners(); 
		for (int i = 0; i < engineListeners.size(); i++) {
			EngineListener engineListener = engineListeners.get(i);
			if(engineListener instanceof GraphicsController) {
				graphicsController = (GraphicsController) engineListener;
			}
		}
		long ramUsage = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) >> 20;
		int maxFPS = Engine.getInstance().getUpdateRate();
		int frameBufferWidth = 0;
		int frameBufferHeight = 0;
		int shadersCount = 0;
		int verticesCount = 0;
		int trianglesCount = 0;
		if (graphicsController != null) {
			frameBufferWidth = graphicsController.getFrameBuffer().getWidth();
			frameBufferHeight = graphicsController.getFrameBuffer().getHeight();
			shadersCount = graphicsController.getShaders().size();
			List<Model> models = graphicsController.getScene().getModels();
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
		g.drawString("FPS", STATISTICS_X * 2, STATISTICS_Y + STATISTICS_LINE * currentLine + 3);
		g.drawString(fps + " / " + maxFPS, STATISTICS_X * 11, STATISTICS_Y + STATISTICS_LINE * currentLine + 3);
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
