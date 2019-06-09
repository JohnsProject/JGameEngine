package com.johnsproject.jpge2.shader;

import com.johnsproject.jpge2.dto.Camera;
import com.johnsproject.jpge2.dto.GeometryDataBuffer;
import com.johnsproject.jpge2.dto.ShaderDataBuffer;
import com.johnsproject.jpge2.dto.VertexDataBuffer;

public interface Shader {
	
	public void update(ShaderDataBuffer dataBuffer);
	
	public void setup(Camera camera);
	
	public void vertex(VertexDataBuffer dataBuffer);

	public void geometry(GeometryDataBuffer dataBuffer);

	public void fragment(int[] location);
	
	public void terminate(ShaderDataBuffer shaderDataBuffer);
	
}
