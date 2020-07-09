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
	private FrameBuffer frameBuffer;
	private int width;
	private int height;
	
	public EngineWindow(FrameBuffer frameBuffer) {
		panel = new EnginePanel();
		setFrameBuffer(frameBuffer);
		setVisible(true);
		setTitle("JGameEngine");
		addWindowListener(handleWindowClose());
		createBufferStrategy(2);
		add(panel);
		panel.setup();
	}
	
	private WindowAdapter handleWindowClose() {
		return new WindowAdapter() {
			public void windowClosing(WindowEvent we) {
				System.exit(0);
			}
		};
	}

	public void initialize(EngineEvent e) {}
	
	public void fixedUpdate(EngineEvent e) {}
	
	public void dynamicUpdate(EngineEvent e) {
		panel.drawBuffer();
		if (getWidth() != width || getHeight() != height) {
			width = getWidth();
			height = getHeight();
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
	
	public boolean hasBorders() {
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
