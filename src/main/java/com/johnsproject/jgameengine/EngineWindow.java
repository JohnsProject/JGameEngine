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

import java.awt.Canvas;
import java.awt.Frame;
import java.awt.Graphics2D;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferStrategy;

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

	public void update() {
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

	public void start() {}
	
	public void fixedUpdate() {}
	
	public int getPriority() {
		return 10001;
	}
}
