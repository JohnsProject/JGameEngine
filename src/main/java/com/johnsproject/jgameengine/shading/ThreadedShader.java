package com.johnsproject.jgameengine.shading;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.johnsproject.jgameengine.model.Face;
import com.johnsproject.jgameengine.model.Vertex;

public abstract class ThreadedShader implements Shader {

	protected static final int THREAD_COUNT = 16;
	
	private final ExecutorService executor;
	private ThreadedVertexShader[] vertexShaders;
	private ThreadedGeometryShader[] geometryShaders;
	
	public ThreadedShader() {
		executor = Executors.newFixedThreadPool(THREAD_COUNT);
		vertexShaders = createVertexShaders(THREAD_COUNT);
		geometryShaders = createGeometryShaders(THREAD_COUNT);
	}

	public abstract ThreadedVertexShader[] createVertexShaders(int count);
	
	public abstract ThreadedGeometryShader[] createGeometryShaders(int count);
	
	public void vertex(Vertex vertex) {
		ThreadedVertexShader shader = null;
		while(shader == null) {
			for (int i = 0; i < vertexShaders.length; i++)
				if(vertexShaders[i].isWaiting())
					shader = vertexShaders[i];
		}
		shader.setVertex(vertex);	
		executor.execute(shader);
	}

	public void geometry(Face face) {
		ThreadedGeometryShader shader = null;
		while(shader == null) {
			for (int i = 0; i < geometryShaders.length; i++)
				if(geometryShaders[i].isWaiting())
					shader = geometryShaders[i];
		}
		shader.setFace(face);	
		executor.execute(shader);
	}

	public void fragment() {
		
	}
	
	public ShaderBuffer getShaderBuffer() {
		return vertexShaders[0].getShaderBuffer();
	}

	public void setShaderBuffer(ShaderBuffer shaderBuffer) {
		for (int i = 0; i < vertexShaders.length; i++)
			vertexShaders[i].setShaderBuffer(shaderBuffer);
		
		for (int i = 0; i < geometryShaders.length; i++)
			geometryShaders[i].setShaderBuffer(shaderBuffer);
	}

	private static interface ThreadedShaderStage extends Runnable, Shader {

		boolean isWaiting();
		
	}
	
	protected static abstract class ThreadedVertexShader implements ThreadedShaderStage {

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
	
	protected static abstract class ThreadedGeometryShader implements ThreadedShaderStage {

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
