package com.johnsproject.jpge2;

import java.awt.Graphics;

import javax.swing.JFrame;
import javax.swing.JPanel;

import com.johnsproject.jpge2.dto.GraphicsBuffer;

public class EngineWindow extends JFrame implements GraphicsBufferListener{

			private static final long serialVersionUID = 1L;
			
			private int width = 0;
			private int height = 0;
			private EnginePanel panel;
			private GraphicsBuffer graphicsBuffer;
			
			public EngineWindow (int width, int height){
				setSize(width, height);
				panel = new EnginePanel();
				this.setLayout(null);
				this.setResizable(false);
				this.setVisible(true);
				this.setTitle("JPGE2");
				this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				this.add(panel);
			}
			
			public EnginePanel getPanel() {
				return panel;
			}
			
			public void graphicsBufferUpdate(GraphicsBuffer graphicsBuffer) {
				this.graphicsBuffer = graphicsBuffer;
				panel.repaint();
				if (this.getWidth() != width || this.getHeight() != height) {
					width = this.getWidth();
					height = this.getHeight();
					panel.setSize(width, height);
				}
			}
			
			public class EnginePanel extends JPanel{
				
				private static final long serialVersionUID = 1L;
				
				@Override
				protected void paintComponent(Graphics g) {
					super.paintComponent(g);
					g.drawImage(graphicsBuffer.getFrameBuffer(), 0, 0, width, height, null);
				}
			}
	
}
