package com.johnsproject.jpge2.shaders;

import com.johnsproject.jpge2.dto.Face;
import com.johnsproject.jpge2.dto.Light;
import com.johnsproject.jpge2.dto.Material;
import com.johnsproject.jpge2.dto.Texture;
import com.johnsproject.jpge2.processing.ColorProcessor;
import com.johnsproject.jpge2.processing.GraphicsProcessor;
import com.johnsproject.jpge2.processing.GraphicsProcessor.Shader;
import com.johnsproject.jpge2.processing.MathProcessor;
import com.johnsproject.jpge2.processing.VectorProcessor;

public class FlatShader extends Shader {

	private static int[] uvX = VectorProcessor.generate();
	private static int[] uvY = VectorProcessor.generate();
	private static int color;
	private static int intensity;
	private static Texture texture;

	@Override
	public void geometry(Face face) {
		Material material = face.getMaterial();
		int[] normal = face.getNormal();
		int[] cameraLocation = camera.getTransform().getLocation();
		color = 0;
		for (int i = 0; i < lights.size(); i++) {
			Light light = lights.get(i);
			intensity = GraphicsProcessor.getPointLightFactor(face.getVertex1().getLocation(), normal, light.getTransform().getLocation(), cameraLocation, material);
			intensity += light.getStrength();
			int c = ColorProcessor.multiplyColor(light.getDiffuseColor(), material.getDiffuseColor());
			color = ColorProcessor.multiplyColor(c, color);
			color = ColorProcessor.multiply(color, intensity);
		}
		texture = material.getTexture();
		// set uv values that will be interpolated
		// uv is in normalized fixed point space between 0 - MathProcessor.FP_VALUE
		// multiply uv with texture size to get correct coordinates and divide by
		// MathProcessor.FP_VALUE
		if (texture != null) {
			uvX[0] = (face.getUV1()[vx] * texture.getWidth()) >> MathProcessor.FP_SHIFT;
			uvX[1] = (face.getUV2()[vx] * texture.getWidth()) >> MathProcessor.FP_SHIFT;
			uvX[2] = (face.getUV3()[vx] * texture.getWidth()) >> MathProcessor.FP_SHIFT;
			uvY[0] = (face.getUV1()[vy] * texture.getHeight()) >> MathProcessor.FP_SHIFT;
			uvY[1] = (face.getUV2()[vy] * texture.getHeight()) >> MathProcessor.FP_SHIFT;
			uvY[2] = (face.getUV3()[vy] * texture.getHeight()) >> MathProcessor.FP_SHIFT;
		}
	}

	@Override
	public int fragment(int[] location, int[] barycentric) {
		if (texture != null) {
			int u = GraphicsProcessor.interpolate(uvX, barycentric);
			int v = GraphicsProcessor.interpolate(uvY, barycentric);
			int texel = texture.getPixel(u, v);
			return ColorProcessor.multiply(texel, intensity);
		}
		return color;
	}

}
