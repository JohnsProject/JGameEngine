package com.johnsproject.jpge2.shaders;

import com.johnsproject.jpge2.Shader;
import com.johnsproject.jpge2.dto.Face;
import com.johnsproject.jpge2.dto.Light;
import com.johnsproject.jpge2.dto.Vertex;
import com.johnsproject.jpge2.processing.ColorProcessor;
import com.johnsproject.jpge2.processing.MathProcessor;
import com.johnsproject.jpge2.processing.VectorProcessor;

public class FlatShader implements Shader{

	private static final int vx = VectorProcessor.VECTOR_X;
	private static final int vy = VectorProcessor.VECTOR_Y;
	private static final int vz = VectorProcessor.VECTOR_Z;
	private static final int vw = VectorProcessor.VECTOR_W;

	private static final int AMBIENT_FACTOR = 10;

	private static int[] lightDirection = VectorProcessor.generate();
	private static int[] lightLocation = VectorProcessor.generate();
	private static int color;
	private static int[] normal = VectorProcessor.generate();
	
	public void vertex(Vertex vertex) {
		
	}

	public void geometry(Face face, Light light) {
		VectorProcessor.copy(normal, face.getNormal());
		VectorProcessor.copy(lightLocation, light.getTransform().getLocation());
		color = face.getMaterial().getColor();
		color = ColorProcessor.multiply(color, AMBIENT_FACTOR);
	}

	public int fragment(int x, int y, int z) {
		lightDirection[vx] = x;
		lightDirection[vy] = y;
		lightDirection[vz] = z;
		VectorProcessor.subtract(lightLocation, lightDirection, lightDirection);
		int diffuseFactor = Math.max(VectorProcessor.dotProduct(normal, lightDirection), 0) >> MathProcessor.FP_SHIFT;
		return ColorProcessor.multiply(color, diffuseFactor);
	}

}
