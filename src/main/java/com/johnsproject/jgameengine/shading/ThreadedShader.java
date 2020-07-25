package com.johnsproject.jgameengine.shading;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.johnsproject.jgameengine.model.Face;
import com.johnsproject.jgameengine.model.Vertex;

public abstract class ThreadedShader implements Shader {

	protected static final int THREAD_COUNT = 16;
	private final ExecutorService executor;
	private VertexWorker[] vertexWorkers;
	private GeometryWorker[] geometryWorkers;
	
	public ThreadedShader() {
		executor = Executors.newFixedThreadPool(THREAD_COUNT);
		vertexWorkers = createVertexWorkers(THREAD_COUNT);
		geometryWorkers = createGeometryWorkers(THREAD_COUNT);
	}
	
	public void vertex(Vertex vertex) {
		VertexWorker worker = null;
		while(worker == null) {
			for (int i = 0; i < vertexWorkers.length; i++)
				if(vertexWorkers[i].isWaiting())
					worker = vertexWorkers[i];
		}
		worker.setShaderBuffer(getShaderBuffer());
		worker.setVertex(vertex);	
		executor.execute(worker);
	}

	public void geometry(Face face) {
		GeometryWorker worker = null;
		while(worker == null) {
			for (int i = 0; i < geometryWorkers.length; i++)
				if(geometryWorkers[i].isWaiting())
					worker = geometryWorkers[i];
		}
		worker.setShaderBuffer(getShaderBuffer());
		worker.setFace(face);	
		executor.execute(worker);
	}

	public void fragment() {
		
	}
	
	public abstract VertexWorker[] createVertexWorkers(int count);
	public abstract GeometryWorker[] createGeometryWorkers(int count);
	
	private static interface ShaderWorker extends Runnable, Shader {

		boolean isWaiting();
		
	}
	
	protected static abstract class VertexWorker implements ShaderWorker {

		private Vertex vertex;
		
		public void geometry(Face face) { }
		
		public void fragment() { }
		
		public void run() {
			vertex(vertex);
			vertex = null;
		}

		public void setVertex(Vertex vertex) {
			this.vertex = vertex;
		}
		
		public boolean isWaiting() {
			return vertex == null;
		}
		
		public boolean isGlobal() {
			return false;
		}
	}
	
	protected static abstract class GeometryWorker implements ShaderWorker {

		private Face face;
		
		public void vertex(Vertex vertex) { }
		
		public void run() {
			geometry(face);
			face = null;
		}

		public void setFace(Face face) {
			this.face = face;
		}
		
		public boolean isWaiting() {
			return face == null;
		}
		
		public boolean isGlobal() {
			return false;
		}
	}
}
