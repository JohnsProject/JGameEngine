package com.johnsproject.jgameengine.shading;

import com.johnsproject.jgameengine.model.Face;
import com.johnsproject.jgameengine.model.Fragment;
import com.johnsproject.jgameengine.model.Vertex;

public interface Shader {
	
	void vertex(Vertex vertex);
	
	void geometry(Face face);
	
	void fragment(Fragment fragment);

	ShaderBuffer getShaderBuffer();

	void setShaderBuffer(ShaderBuffer shaderBuffer);
	
	/**
	 * Is this shader a global shader?
	 * Global shaders are used on all Models.
	 * 
	 * @return If this shader is a global shader.
	 */
	boolean isGlobal();
}
