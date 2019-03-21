package com.johnsproject.jpge2.shaders;

import com.johnsproject.jpge2.dto.Face;
import com.johnsproject.jpge2.dto.Material;
import com.johnsproject.jpge2.dto.Texture;
import com.johnsproject.jpge2.processing.ColorProcessor;
import com.johnsproject.jpge2.processing.GraphicsProcessor;
import com.johnsproject.jpge2.processing.GraphicsProcessor.Shader;
import com.johnsproject.jpge2.processing.MathProcessor;
import com.johnsproject.jpge2.processing.VectorProcessor;

public class FlatShader extends Shader {
	
	private static int[] vectorCache1 = VectorProcessor.generate();
	private static int[] vectorCache2 = VectorProcessor.generate();
	private static int[] uvX = VectorProcessor.generate();
	private static int[] uvY = VectorProcessor.generate();
	private static int color;
	private static int intensity;
	private static Texture texture;

	@Override
	public void geometry(Face face) {
		int[] normal = face.getNormal();
		int[] lightPosition = light.getTransform().getLocation();
		int[] cameraPosition = camera.getTransform().getLocation();
		Material material = face.getMaterial();
		// diffuse
		VectorProcessor.normalize(normal, vectorCache1);
		VectorProcessor.normalize(lightPosition, vectorCache2);
		int dotProduct = VectorProcessor.dotProduct(vectorCache1, vectorCache2);
		int diffuseFactor = Math.max(dotProduct, 0);
		diffuseFactor = (diffuseFactor * material.getDiffuseIntensity()) >> MathProcessor.FP_SHIFT;
		// specular
		VectorProcessor.reflect(vectorCache2, vectorCache1, vectorCache2);
		VectorProcessor.invert(vectorCache2);
		VectorProcessor.normalize(cameraPosition, vectorCache1);
		dotProduct = VectorProcessor.dotProduct(vectorCache1, vectorCache2);
		int specularFactor = Math.max(dotProduct, 0);
		specularFactor = (specularFactor * material.getSpecularIntensity()) >> MathProcessor.FP_SHIFT;
		// putting it all together...
		intensity = light.getStrength() + diffuseFactor + specularFactor;
		color = ColorProcessor.multiplyColor(light.getDiffuseColor(), material.getDiffuseColor());
		color = ColorProcessor.multiply(color, intensity);
		texture = material.getTexture();
		uvX[0] = face.getUV1()[vx];
		uvX[1] = face.getUV2()[vx];
		uvX[2] = face.getUV3()[vx];
		uvY[0] = face.getUV1()[vy];
		uvY[1] = face.getUV2()[vy];
		uvY[2] = face.getUV3()[vy];
	}

	@Override
	public int fragment(int[] barycentric) {
		if (texture != null) {
			int u = GraphicsProcessor.interpolate(uvX, barycentric);
			int v = GraphicsProcessor.interpolate(uvY, barycentric);
			int texel = texture.getPixel(u, v);
			return ColorProcessor.multiply(texel, intensity);
		}
		return color;
	}

}
