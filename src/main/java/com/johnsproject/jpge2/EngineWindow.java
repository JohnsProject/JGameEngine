package com.johnsproject.jpge2;

import java.awt.Canvas;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferStrategy;

import com.johnsproject.jpge2.dto.GraphicsBuffer;

public class EngineWindow extends Frame implements GraphicsBufferListener {

	private static final long serialVersionUID = 1L;

	private int width = 0;
	private int height = 0;
	private EnginePanel panel;
	private GraphicsBuffer graphicsBuffer;

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
	}

	public EnginePanel getPanel() {
		return panel;
	}

	public void graphicsBufferUpdate(GraphicsBuffer graphicsBuffer) {
		this.graphicsBuffer = graphicsBuffer;
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
			g.drawImage(graphicsBuffer.getFrameBuffer(), 0, 0, null);
			s.show();
		}
	}

}
