package com.johnsproject.jpge2.shader;

import com.johnsproject.jpge2.dto.Camera;
import com.johnsproject.jpge2.dto.Face;
import com.johnsproject.jpge2.dto.Vertex;
import com.johnsproject.jpge2.processor.CentralProcessor;

public abstract class Shader {
	
	private final int[][] variables;
	
	public Shader(CentralProcessor centralProcessor, int variablesCount) {
		this.variables = new int[variablesCount][4];
	}
	
	public abstract void update(ShaderDataBuffer shaderDataBuffer);
	
	public abstract void setup(Camera camera);
	
	public abstract void vertex(int index, Vertex vertex);

	public abstract void geometry(Face face);

	public abstract void fragment(int[] location, int[] barycentric);
	
	public int[] getVariable(int index) {
		return variables[index];
	}
	
	public int[][] getVariables() {
		return variables;
	}
}
