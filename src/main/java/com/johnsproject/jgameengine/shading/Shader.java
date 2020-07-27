package com.johnsproject.jgameengine.shading;

import com.johnsproject.jgameengine.model.Face;
import com.johnsproject.jgameengine.model.Vertex;

public interface Shader {
	
	void initialize(ShaderBuffer shaderBuffer);
	
	void vertex(Vertex vertex);
	
	/**
	 * Used to notify the {@link ThreadedShader} that all vertices have been shaded
	 * and it should wait until the queue is empty. No implementation is required.
	 */
	void waitForVertexQueue();
	
	void geometry(Face face);
	
	/**
	 * Used to notify the {@link ThreadedShader} that all faces have been shaded
	 * and it should wait until the queue is empty. No implementation is required.
	 */
	void waitForGeometryQueue();
	
	void fragment();

	ShaderBuffer getShaderBuffer();
	
	/**
	 * Is this shader a global shader?
	 * Global shaders are used on all Models.
	 * 
	 * @return If this shader is a global shader.
	 */
	boolean isGlobal();
}
