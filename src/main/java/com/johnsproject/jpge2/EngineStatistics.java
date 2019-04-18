package com.johnsproject.jpge2;

import java.awt.Color;
import java.awt.Graphics2D;

public class EngineStatistics implements EngineListener{

	private static final int STATISTICS_X = 10;
	private static final int STATISTICS_Y = 30;
	private static final int STATISTICS_LINE = 15;
	private static final int STATISTICS_WIDTH = 160;
	private static final int STATISTICS_HEIGHT = 100;
	private static final Color STATISTICS_BACKROUND = new Color(200, 200, 200, 100);
	
	private long lastUpdateTime; 
	
	public EngineStatistics() {
		Engine.getInstance().addEngineListener(this);
	}
	
	public void start() {
		
	}

	public void update() {
		long currentTime = System.currentTimeMillis();
		long elapsed = currentTime - lastUpdateTime;
		lastUpdateTime = currentTime;
		long fps = 1000 / elapsed;
		long ramUsage = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) >> 20;
		EngineOptions options = Engine.getInstance().getOptions();
		int[] frameBufferSize = options.getFrameBuffer().getSize();
		int maxFPS = options.getUpdateRate();
		int shadersCount = options.getShaders().size();
		
		Graphics2D g = Engine.getInstance().getOptions().getFrameBuffer().getImage().createGraphics();
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
		g.drawString(frameBufferSize[0] + "x" + frameBufferSize[1], STATISTICS_X * 11, STATISTICS_Y + STATISTICS_LINE * currentLine + 3);
		currentLine++;
		g.drawString("Shaders", STATISTICS_X * 2, STATISTICS_Y + STATISTICS_LINE * currentLine + 3);
		g.drawString("" + shadersCount, STATISTICS_X * 11, STATISTICS_Y + STATISTICS_LINE * currentLine + 3);
		g.dispose();
	}

	public void fixedUpdate() {
		
	}

	public int getPriority() {
		return 10000;
	}

}
