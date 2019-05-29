package com.johnsproject.jpge2.shader;

import com.johnsproject.jpge2.dto.Camera;
import com.johnsproject.jpge2.dto.Face;
import com.johnsproject.jpge2.dto.Vertex;

public interface Shader {
	
	public void update(ShaderDataBuffer shaderDataBuffer);
	
	public void setup(Camera camera);
	
	public void vertex(int index, Vertex vertex);

	public void geometry(Face face);

	public void fragment(int[] location);
}
