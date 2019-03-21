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
		// set uv values that will be interpolated
		// uv is in normalized fixed point space between 0 - MathProcessor.FP_VALUE
		// multiply uv with texture size to get correct coordinates and divide by MathProcessor.FP_VALUE
		uvX[0] = (face.getUV1()[vx] * texture.getWidth()) >> MathProcessor.FP_SHIFT;
		uvX[1] = (face.getUV2()[vx] * texture.getWidth()) >> MathProcessor.FP_SHIFT;
		uvX[2] = (face.getUV3()[vx] * texture.getWidth()) >> MathProcessor.FP_SHIFT;
		uvY[0] = (face.getUV1()[vy] * texture.getHeight()) >> MathProcessor.FP_SHIFT;
		uvY[1] = (face.getUV2()[vy] * texture.getHeight()) >> MathProcessor.FP_SHIFT;
		uvY[2] = (face.getUV3()[vy] * texture.getHeight()) >> MathProcessor.FP_SHIFT;
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
