package com.johnsproject.jgameengine.shader;

public interface Shader {
	
	public void vertex(VertexBuffer vertexBuffer);
	
	public void geometry(GeometryBuffer geometryBuffer);
	
	public void fragment(FragmentBuffer fragmentBuffer);

	public ShaderBuffer getShaderBuffer();

	public void setShaderBuffer(ShaderBuffer shaderBuffer);

	public void setProperties(ShaderProperties shaderProperties);
	
	public ShaderProperties getProperties();
}
