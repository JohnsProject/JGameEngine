package com.johnsproject.jpge2.shader;

import com.johnsproject.jpge2.dto.Camera;
import com.johnsproject.jpge2.dto.Face;
import com.johnsproject.jpge2.dto.Model;
import com.johnsproject.jpge2.dto.Vertex;
import com.johnsproject.jpge2.processor.CentralProcessor;

public abstract class Shader {
	
	public Shader(CentralProcessor centralProcessor) {}
	
	public abstract void update(ShaderDataBuffer shaderDataBuffer);
	
	public abstract void setup(Model model, Camera camera);
	
	public abstract void vertex(int index, Vertex vertex);

	public abstract void geometry(Face face);

	public abstract void fragment(int[] location, int[] barycentric);
}
