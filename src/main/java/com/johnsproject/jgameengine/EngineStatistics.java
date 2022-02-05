package com.johnsproject.jgameengine;

import java.awt.Color;
import java.awt.Frame;
import java.awt.TextArea;
import java.util.List;

import com.johnsproject.jgameengine.event.EngineEvent;
import com.johnsproject.jgameengine.event.EngineListener;
import com.johnsproject.jgameengine.graphics.FrameBuffer;
import com.johnsproject.jgameengine.graphics.GraphicsEngine;
import com.johnsproject.jgameengine.graphics.Model;

public class EngineStatistics implements EngineListener {

	private static final int STATISTICS_X = 10;
	private static final int STATISTICS_Y = 30;
	private static final int STATISTICS_WIDTH = 180;
	private static final int STATISTICS_HEIGHT = 130;
	private static final Color STATISTICS_BACKROUND = Color.WHITE;
	
	private static final long BYTE_TO_MEGABYTE = 1024L * 1024L;
	
	private final TextArea textArea;
	private GraphicsEngine graphicsEngine;
	private long averageUpdates;
	private long loops;
	
	public EngineStatistics(Frame window) {
		this.textArea = new TextArea("", 0, 0, TextArea.SCROLLBARS_NONE);
		window.add(textArea);
	}
	
	public void initialize(EngineEvent e) { 
		textArea.setLocation(STATISTICS_X, STATISTICS_Y);
		textArea.setSize(STATISTICS_WIDTH, STATISTICS_HEIGHT);
		textArea.setEditable(false);
		textArea.setBackground(STATISTICS_BACKROUND);
	}
	
	public void fixedUpdate(EngineEvent e) {
		final String output = getOutput(e);
		textArea.setText(output);
	}

	public void dynamicUpdate(EngineEvent e) { }
	
	private String getOutput(EngineEvent e) {
		final List<SceneObject> models = e.getScene().getSceneObjects();		
		
		String output = "== ENGINE STATISTICS ==\n";
		output += getRAMUsage();
		output += getCPUTime(e.getElapsedUpdateTime() + 1);
		output += getFrameBufferSize();
		output += getVertexAndTriangleCount(models);
		return output;
	}
	
	private String getRAMUsage() {
		final Runtime runtime = Runtime.getRuntime();
		final long totalRAM = runtime.totalMemory() / BYTE_TO_MEGABYTE;
		final long usedRAM = (runtime.totalMemory() - runtime.freeMemory()) / BYTE_TO_MEGABYTE;
		return "RAM usage\t" + usedRAM + " / " + totalRAM + " MB\n";
	}
	
	private String getCPUTime(long elapsedTime) {
		final long updates = 1000 / elapsedTime;
		averageUpdates += updates;
		loops++;
		if(loops >= 100) {
			averageUpdates = averageUpdates / loops;
			loops = 1;
		}
		String cpuTime = "CPU time\t" + elapsedTime + " ms\n";
		cpuTime += "Updates / s\t" + updates + "\n";
		cpuTime += "Average U / s\t" + (averageUpdates / loops) + "\n";
		return cpuTime;
	}
	
	private String getFrameBufferSize() {
		if(graphicsEngine == null) {
			graphicsEngine = getGraphicsEngine();
		}
		final FrameBuffer frameBuffer = graphicsEngine.getFrameBuffer();
		return "Framebuffer\t" + frameBuffer.getWidth() + "x" + frameBuffer.getHeight() + "\n";
	}
	
	private GraphicsEngine getGraphicsEngine() {
		GraphicsEngine graphicsEngine = null;
		final List<EngineListener> engineListeners = Engine.getInstance().getEngineListeners(); 
		for (int i = 0; i < engineListeners.size(); i++) {
			final EngineListener engineListener = engineListeners.get(i);
			if(engineListener instanceof GraphicsEngine) {
				graphicsEngine = (GraphicsEngine) engineListener;
			}
		}
		return graphicsEngine;				
	}
	
	private String getVertexAndTriangleCount(List<SceneObject> sceneObjects) {
		int vertexCount = 0;
		int triangleCount = 0;
		for (int i = 0; i < sceneObjects.size(); i++) {
			Model model = sceneObjects.get(i).getComponentWithType(Model.class);
			if(model != null) {
				vertexCount += model.getMesh().getVertices().length;
				triangleCount += model.getMesh().getFaces().length;
			}
		}
		return "Vertices\t\t" + vertexCount + "\n" + 
				"Triangles\t" + triangleCount + "\n";
	}
}
