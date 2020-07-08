package com.johnsproject.jgameengine;

import java.awt.Canvas;
import java.awt.Frame;
import java.awt.Graphics2D;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferStrategy;

import com.johnsproject.jgameengine.event.EngineEvent;
import com.johnsproject.jgameengine.event.EngineListener;
import com.johnsproject.jgameengine.model.FrameBuffer;

public class EngineWindow extends Frame implements EngineListener {

	private static final long serialVersionUID = 1L;

	private final EnginePanel panel;
	private int width;
	private int height;
	private FrameBuffer frameBuffer;
	
	public EngineWindow(FrameBuffer frameBuffer) {
		setFrameBuffer(frameBuffer);
		this.setVisible(true);
		this.setTitle("JGameEngine");
		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent we) {
				System.exit(0);
			}
		});
		this.createBufferStrategy(2);
		panel = new EnginePanel();
		this.add(panel);
		panel.setup();
	}

	public void initialize(EngineEvent e) {}
	
	public void fixedUpdate(EngineEvent e) {}
	
	public void dynamicUpdate(EngineEvent e) {
		panel.drawBuffer();
		if (this.getWidth() != width || this.getHeight() != height) {
			width = this.getWidth();
			height = this.getHeight();
			panel.setSize(width, height);
		}
	}
	
	public void setFullscreen(boolean fullscreen) {
		dispose();
		if(fullscreen) {
			setExtendedState(Frame.MAXIMIZED_BOTH);
		} else {
			setExtendedState(Frame.NORMAL);
		}
		setVisible(true);
	}
	
	public boolean isFullscreen() {
		if(getExtendedState() == Frame.NORMAL) {
			return false;
		}
		return true;
	}
	
	public void setBorders(boolean borders) {
		dispose();
		setUndecorated(!borders);
		setVisible(true);
	}
	
	public boolean usingBorders() {
		return !isUndecorated();
	}
	
	public void setFrameBuffer(FrameBuffer frameBuffer) {
		setSize(frameBuffer.getWidth(), frameBuffer.getHeight());
		this.frameBuffer = frameBuffer;
	}

	public FrameBuffer getFrameBuffer() {
		return frameBuffer;
	}

	public Canvas getCanvas() {
		return panel;
	}
	
	private class EnginePanel extends Canvas {

		private static final long serialVersionUID = 1L;

		private BufferStrategy bufferStrategy;
		
		private void setup() {
			if (bufferStrategy == null) {
				this.createBufferStrategy(2);
			}
			bufferStrategy = this.getBufferStrategy();
		}
		
		public void drawBuffer() {
			Graphics2D graphics = (Graphics2D) bufferStrategy.getDrawGraphics();
			graphics.clearRect(0, 0, width, height);
			graphics.drawImage(frameBuffer.getImage(), 0, 0, width, height, null);
			bufferStrategy.show();
		}
	}
	
	public int getLayer() {
		return GRAPHICS_ENGINE_LAYER + 1;
	}
}
