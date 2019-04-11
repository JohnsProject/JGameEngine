package com.johnsproject.jpge2;

import java.awt.Color;
import java.awt.Graphics2D;

public class EngineProfiler implements EngineListener{

	private static final int PROFILER_X = 10;
	private static final int PROFILER_Y = 30;
	private static final int PROFILER_LINE = 15;
	private static final int PROFILER_WIDTH = 160;
	private static final int PROFILER_HEIGHT = 100;
	private static final Color PROFILER_BACKROUND = new Color(200, 200, 200, 100);
	
	private long lastUpdateTime = 0; 
	
	public EngineProfiler() {
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
		int frameBufferWidth = options.getFrameBuffer().getWidth();
		int frameBufferHeight = options.getFrameBuffer().getHeight();
		int maxFPS = options.getUpdateRate();
		int shadersCount = options.getShaders().size();
		
		Graphics2D g = Engine.getInstance().getOptions().getFrameBuffer().getImage().createGraphics();
		g.setColor(PROFILER_BACKROUND);
		g.fillRect(PROFILER_X, PROFILER_Y, PROFILER_WIDTH, PROFILER_HEIGHT);
		g.setColor(Color.black);
		int currentLine = 1;
		g.drawString("======= Profiler =======", PROFILER_X * 2 , PROFILER_Y + PROFILER_LINE * currentLine + 3);
		currentLine++;
		g.drawString("CPU time", PROFILER_X * 2, PROFILER_Y + PROFILER_LINE * currentLine + 3);
		g.drawString(elapsed + " ms", PROFILER_X * 11, PROFILER_Y + PROFILER_LINE * currentLine + 3);
		currentLine++;
		g.drawString("RAM usage", PROFILER_X * 2, PROFILER_Y + PROFILER_LINE * currentLine + 3);
		g.drawString(ramUsage + " MB", PROFILER_X * 11, PROFILER_Y + PROFILER_LINE * currentLine + 3);
		currentLine++;
		g.drawString("FPS", PROFILER_X * 2, PROFILER_Y + PROFILER_LINE * currentLine + 3);
		g.drawString(fps + " / " + maxFPS, PROFILER_X * 11, PROFILER_Y + PROFILER_LINE * currentLine + 3);
		currentLine++;
		g.drawString("Framebuffer", PROFILER_X * 2, PROFILER_Y + PROFILER_LINE * currentLine + 3);
		g.drawString(frameBufferWidth + "x" + frameBufferHeight, PROFILER_X * 11, PROFILER_Y + PROFILER_LINE * currentLine + 3);
		currentLine++;
		g.drawString("Shaders", PROFILER_X * 2, PROFILER_Y + PROFILER_LINE * currentLine + 3);
		g.drawString("" + shadersCount, PROFILER_X * 11, PROFILER_Y + PROFILER_LINE * currentLine + 3);
		g.dispose();
	}

	public void fixedUpdate() {
		
	}

	public int getPriority() {
		return 10000;
	}

}
