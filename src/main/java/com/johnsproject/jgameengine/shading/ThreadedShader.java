package com.johnsproject.jgameengine.shading;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.johnsproject.jgameengine.model.Face;
import com.johnsproject.jgameengine.model.Vertex;

public abstract class ThreadedShader implements Shader {

	protected static final int THREAD_COUNT = 16;
	private final ExecutorService executor;
	private final BlockingQueue<GeometryWorker> queue;
	
	public ThreadedShader() {
		executor = Executors.newFixedThreadPool(THREAD_COUNT);
		queue = new ArrayBlockingQueue<GeometryWorker>(THREAD_COUNT * 2);
		
		final GeometryWorker[] geometryWorkers = createGeometryWorkers(THREAD_COUNT * 2);
		for (int i = 0; i < geometryWorkers.length; i++)
			queue.add(geometryWorkers[i]);
	}
	
	public void vertex(Vertex vertex) {
		threadedVertex(vertex);
	}

	public void geometry(Face face) {
		GeometryWorker worker = null;
		try {
			worker = queue.poll(10, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		worker.setQueue(queue);
		worker.setShaderBuffer(getShaderBuffer());
		worker.setFace(face);
		executor.execute(worker);
	}

	public void fragment() {
		
	}
	
	public abstract void threadedVertex(Vertex vertex);
	public abstract GeometryWorker[] createGeometryWorkers(int count);
	
	protected static abstract class GeometryWorker implements Runnable, Shader {

		private BlockingQueue<GeometryWorker> queue;
		private Face face;
		
		public void vertex(Vertex vertex) { }
		
		public void run() {
			geometry(face);
			queue.add(this);
		}

		public void setQueue(BlockingQueue<GeometryWorker> queue) {
			this.queue = queue;
		}

		public void setFace(Face face) {
			this.face = face;
		}
	}
}
