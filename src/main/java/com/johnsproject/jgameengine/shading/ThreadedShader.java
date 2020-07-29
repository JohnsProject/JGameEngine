package com.johnsproject.jgameengine.shading;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import com.johnsproject.jgameengine.model.Face;
import com.johnsproject.jgameengine.model.Vertex;

public abstract class ThreadedShader implements Shader {
	
	private final BlockingQueue<Vertex> vertexQueue;
	private final BlockingQueue<Face> geometryQueue;
	private final ThreadedVertexShader[] vertexShaders;
	private final ThreadedGeometryShader[] geometryShaders;
	
	public ThreadedShader() {
		final int coreCount = Runtime.getRuntime().availableProcessors();
		vertexQueue = new ArrayBlockingQueue<Vertex>(coreCount * 32);
		geometryQueue = new ArrayBlockingQueue<Face>(coreCount * 32);
		
		vertexShaders = createVertexShaders(coreCount);
		for (int i = 0; i < vertexShaders.length; i++) {
			vertexShaders[i].setQueue(vertexQueue);
			vertexShaders[i].start();
		}
		
		geometryShaders = createGeometryShaders(coreCount);
		for (int i = 0; i < geometryShaders.length; i++) {
			geometryShaders[i].setQueue(geometryQueue);
			geometryShaders[i].start();
		}
	}

	public abstract ThreadedVertexShader[] createVertexShaders(int count);
	
	public abstract ThreadedGeometryShader[] createGeometryShaders(int count);

	public void initialize(ShaderBuffer shaderBuffer) {		
		for (int i = 0; i < vertexShaders.length; i++)
			vertexShaders[i].initialize(shaderBuffer);
		
		for (int i = 0; i < geometryShaders.length; i++)
			geometryShaders[i].initialize(shaderBuffer);
	}
	
	public void vertex(Vertex vertex) {
		try {
			vertexQueue.put(vertex);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void geometry(Face face) {
		try {
			geometryQueue.put(face);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void fragment() {}
	
	public ShaderBuffer getShaderBuffer() {
		return vertexShaders[0].getShaderBuffer();
	}
	
	public void waitForVertexQueue() {
		for (int i = 0; i < vertexShaders.length; i++)
			if((!vertexQueue.isEmpty()) || (vertexShaders[i].getState() != Thread.State.WAITING))
				i = 0;
	}
	
	public void waitForGeometryQueue() {
		for (int i = 0; i < geometryShaders.length; i++)
			if((!geometryQueue.isEmpty()) || (geometryShaders[i].getState() != Thread.State.WAITING))
				i = 0;
	}
	
	protected static abstract class ThreadedVertexShader extends Thread implements Shader {

		private BlockingQueue<Vertex> queue;
		
		public ThreadedVertexShader() {
			super("VertexShaderThread");
		}
		
		public void waitForVertexQueue() { }
		
		public void geometry(Face face) { }
		
		public void waitForGeometryQueue() { }
		
		public void fragment() { }
		
		public void run() {
			while(true) {
				try {
					vertex(queue.take());
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

		private void setQueue(BlockingQueue<Vertex> queue) {
			this.queue = queue;
		}
		
		public boolean isGlobal() {
			return false;
		}
	}
	
	protected static abstract class ThreadedGeometryShader extends Thread implements Shader {

		private BlockingQueue<Face> queue;
		
		public ThreadedGeometryShader() {
			super("GeometryShaderThread");
		}
		
		public void vertex(Vertex vertex) { }
		
		public void waitForVertexQueue() { }

		public void waitForGeometryQueue() { }
		
		public void run() {
			while(true) {
				try {
					geometry(queue.take());
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

		private void setQueue(BlockingQueue<Face> queue) {
			this.queue = queue;
		}
		
		public boolean isGlobal() {
			return false;
		}
	}
}
