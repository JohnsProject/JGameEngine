package com.johnsproject.jgameengine.shader;

import com.johnsproject.jgameengine.model.Face;
import com.johnsproject.jgameengine.model.Fragment;
import com.johnsproject.jgameengine.model.Vertex;

public interface Shader {
	
	public void vertex(Vertex vertex);
	
	public void geometry(Face face);
	
	public void fragment(Fragment fragment);

	public ShaderBuffer getShaderBuffer();

	public void setShaderBuffer(ShaderBuffer shaderBuffer);

	public void setProperties(ShaderProperties shaderProperties);
	
	public ShaderProperties getProperties();
}
