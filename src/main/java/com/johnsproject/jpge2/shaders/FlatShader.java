package com.johnsproject.jpge2.shaders;

import com.johnsproject.jpge2.Shader;
import com.johnsproject.jpge2.dto.Face;
import com.johnsproject.jpge2.dto.Vertex;
import com.johnsproject.jpge2.processing.ColorProcessor;
import com.johnsproject.jpge2.processing.MathProcessor;
import com.johnsproject.jpge2.processing.VectorProcessor;

public class FlatShader extends Shader {
	
	private static int color;
	
	public void vertex(Vertex vertex) {
		
	}

	public void geometry(Face face) {
		int dotProduct = VectorProcessor.dotProduct(face.getNormal(), light.getTransform().getLocation());
		int diffuseFactor = Math.max(dotProduct, 0) >> MathProcessor.FP_SHIFT;
		color = face.getMaterial().getColor();
		color = ColorProcessor.multiply(color, AMBIENT_FACTOR);
		color = ColorProcessor.multiply(color, diffuseFactor);
		color = ColorProcessor.multiplyColor(color, light.getColor());
	}

	public int fragment(int x, int y, int z) {
		return color;
	}

}
