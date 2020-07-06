package com.johnsproject.jgameengine.shader;

import com.johnsproject.jgameengine.model.Vertex;

public interface Shader {
	
	public void vertex(Vertex vertex);
	
	public void geometry(GeometryBuffer geometryBuffer);
	
	public void fragment(FragmentBuffer fragmentBuffer);

	public ShaderBuffer getShaderBuffer();

	public void setShaderBuffer(ShaderBuffer shaderBuffer);

	public void setProperties(ShaderProperties shaderProperties);
	
	public ShaderProperties getProperties();
}
