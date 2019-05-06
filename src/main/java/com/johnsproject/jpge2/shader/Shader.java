package com.johnsproject.jpge2.shader;

import com.johnsproject.jpge2.dto.Camera;
import com.johnsproject.jpge2.dto.Face;
import com.johnsproject.jpge2.dto.Vertex;
import com.johnsproject.jpge2.primitive.FPVector;

public abstract class Shader {
	
	public abstract void update(ShaderDataBuffer shaderDataBuffer);
	
	public abstract void setup(Camera camera);
	
	public abstract void vertex(int index, Vertex vertex);

	public abstract void geometry(Face face);

	public abstract void fragment(FPVector location);
}
