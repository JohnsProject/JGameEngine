package com.johnsproject.jgameengine;

import java.awt.Canvas;
import java.awt.Component;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferStrategy;

import com.johnsproject.jgameengine.event.EngineEvent;
import com.johnsproject.jgameengine.event.EngineListener;
import com.johnsproject.jgameengine.model.FrameBuffer;

public class EngineWindow extends Frame implements EngineListener {

	private static final long serialVersionUID = 1L;

	private final Canvas canvas;
	private BufferStrategy bufferStrategy;
	private Graphics graphics;
	private FrameBuffer frameBuffer;
	
	public EngineWindow(FrameBuffer frameBuffer) {
		canvas = new Canvas();
		setLayout(null);
		setFrameBuffer(frameBuffer);
		setTitle("JGameEngine");
		addWindowListener(handleWindowClose());
		addComponentListener(handleWindowResize());
		add(canvas);
		setVisible(true);
	}
	
	private WindowAdapter handleWindowClose() {
		return new WindowAdapter() {
			public void windowClosing(WindowEvent we) {
				System.exit(0);
			}
		};
	}
	
	public ComponentAdapter handleWindowResize() {
		return new ComponentAdapter() {
		    public void componentResized(ComponentEvent componentEvent) {
		    	recreateCanvasBuffer();
		    }
		};
	}

	public void initialize(EngineEvent e) {}
	
	public void fixedUpdate(EngineEvent e) {}
	
	public void dynamicUpdate(EngineEvent e) {
		synchronized (canvas) {
			final int width = getWidth();
			final int height = getHeight();
			graphics.clearRect(0, 0, width, height);
			graphics.drawImage(frameBuffer.getImage(), 0, 0, width, height, null);
			bufferStrategy.show();
		}
	}
	
	@Override
	public void setVisible(boolean isVisible) {
		super.setVisible(isVisible);
		recreateCanvasBuffer();
	}
	
	private void recreateCanvasBuffer() {
	   	synchronized (canvas) {
	   		if(isVisible()) {
				canvas.setSize(getWidth(), getHeight());
				canvas.createBufferStrategy(2);
				if(bufferStrategy != null) {
					graphics.dispose();
					bufferStrategy.dispose();
				}
				bufferStrategy = canvas.getBufferStrategy();
				graphics = bufferStrategy.getDrawGraphics();
			}
		}
	}
	
	public void setFullscreen(boolean isFullscreen) {
		dispose();
		if(isFullscreen)
			setExtendedState(Frame.MAXIMIZED_BOTH);
		else
			setExtendedState(Frame.NORMAL);
		setVisible(true);
	}
	
	public boolean isFullscreen() {
		return getExtendedState() != Frame.NORMAL;
	}
	
	public void setBorders(boolean hasBorders) {
		dispose();
		setUndecorated(!hasBorders);
		setVisible(true);
	}
	
	public boolean hasBorders() {
		return !isUndecorated();
	}
	
	@Override
	public Component add(Component comp) {
		return super.add(comp, 0);
	}

	public void setFrameBuffer(FrameBuffer frameBuffer) {
		setSize(frameBuffer.getWidth(), frameBuffer.getHeight());
		this.frameBuffer = frameBuffer;
	}

	public FrameBuffer getFrameBuffer() {
		return frameBuffer;
	}
	
	public int getLayer() {
		return GRAPHICS_ENGINE_LAYER + 1;
	}

	public Canvas getCanvas() {
		return canvas;
	}
}