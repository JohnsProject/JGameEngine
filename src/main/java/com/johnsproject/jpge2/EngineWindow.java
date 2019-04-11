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
package com.johnsproject.jpge2;

import java.awt.Canvas;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferStrategy;

public class EngineWindow extends Frame implements EngineListener {

	private static final long serialVersionUID = 1L;

	private int width = 0;
	private int height = 0;
	private EnginePanel panel;

	public EngineWindow(int width, int height) {
		setSize(width, height);
		panel = new EnginePanel();
		this.setLayout(null);
		this.setResizable(false);
		this.setVisible(true);
		this.setTitle("JPGE2");
		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent we) {
				System.exit(0);
			}
		});
		this.createBufferStrategy(2);
		this.add(panel);
		Engine.getInstance().addEngineListener(this);
	}

	public EnginePanel getPanel() {
		return panel;
	}
	

	public void update() {
		panel.drawBuffer();
		if (this.getWidth() != width || this.getHeight() != height) {
			width = this.getWidth();
			height = this.getHeight();
			panel.setSize(width, height);
		}
	}

	public class EnginePanel extends Canvas {

		private static final long serialVersionUID = 1L;

		public void drawBuffer() {
			if (this.getBufferStrategy() == null) {
				this.createBufferStrategy(2);
			}
			BufferStrategy s = this.getBufferStrategy();
			Graphics g = s.getDrawGraphics();
			g.clearRect(0, 0, width, height);
			g.drawImage(Engine.getInstance().getOptions().getFrameBuffer().getImage(), 0, 0, null);
			s.show();
		}
	}

	public void start() {}
	
	public void fixedUpdate() {}
	
	public int getPriority() {
		return 10001;
	}
}
