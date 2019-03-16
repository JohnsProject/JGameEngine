package com.johnsproject.jpge2.shaders;

import com.johnsproject.jpge2.Shader;
import com.johnsproject.jpge2.dto.Face;
import com.johnsproject.jpge2.dto.Light;
import com.johnsproject.jpge2.dto.Vertex;
import com.johnsproject.jpge2.processing.ColorProcessor;
import com.johnsproject.jpge2.processing.VectorProcessor;

public class FlatShader implements Shader{

	private static final int vx = VectorProcessor.VECTOR_X;
	private static final int vy = VectorProcessor.VECTOR_Y;
	private static final int vz = VectorProcessor.VECTOR_Z;
	private static final int vw = VectorProcessor.VECTOR_W;

	public void vertex(Vertex vertex) {
		
	}

	public void geometry(Face face, Light light) {
		
	}

	public int fragment(int x, int y, int z) {
		return ColorProcessor.convert(100, 100, 100);
	}

}
