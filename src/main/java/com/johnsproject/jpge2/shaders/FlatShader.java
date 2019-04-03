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

	private static final int[] uvX = VectorProcessor.generate();
	private static final int[] uvY = VectorProcessor.generate();
	private static int color;
	private static int intensity;
	private static Texture texture;

	private static final int[] faceLocation = VectorProcessor.generate();
	private static final int[] normalizedNormal = VectorProcessor.generate();
	private static final int[] lightLocation = VectorProcessor.generate();
	
	@Override
	public void geometry(Face face) {
		Material material = face.getMaterial();
		int[] normal = face.getNormal();
		// normalize values
		VectorProcessor.normalize(normal, normalizedNormal);
		// get location of face
		VectorProcessor.add(face.getVertex1().getLocation(), face.getVertex2().getLocation(), faceLocation);
		VectorProcessor.add(faceLocation, face.getVertex3().getLocation(), faceLocation);
		VectorProcessor.divide(faceLocation, 3, faceLocation);
		color = material.getDiffuseColor();
		intensity = 0;
		for (int i = 0; i < lights.size(); i++) {
			Light light = lights.get(i);
			VectorProcessor.copy(lightLocation, light.getTransform().getLocation());
			switch (light.getType()) {
			case Light.LIGHT_DIRECTIONAL:
				intensity += GraphicsProcessor.getDirectionalLightFactor(faceLocation, normalizedNormal, lightLocation, material);
				break;
			case Light.LIGHT_POINT:
				intensity += GraphicsProcessor.getPointLightFactor(faceLocation, normalizedNormal, lightLocation, material);
				break;
			}
			intensity += light.getStrength();
			int c = ColorProcessor.multiplyColor(light.getDiffuseColor(), color);
			color = ColorProcessor.multiplyColor(c, color);
			color = ColorProcessor.multiply(color, intensity);
		}
		texture = material.getTexture();
		// set uv values that will be interpolated
		// uv is in normalized fixed point space between 0 - MathProcessor.FP_VALUE
		// multiply uv with texture size to get correct coordinates and divide by
		// MathProcessor.FP_VALUE
		if (texture != null) {
			uvX[0] = MathProcessor.multiply(face.getUV1()[vx], texture.getWidth());
			uvX[1] = MathProcessor.multiply(face.getUV2()[vx], texture.getWidth());
			uvX[2] = MathProcessor.multiply(face.getUV3()[vx], texture.getWidth());
			uvY[0] = MathProcessor.multiply(face.getUV1()[vy], texture.getHeight());
			uvY[1] = MathProcessor.multiply(face.getUV2()[vy], texture.getHeight());
			uvY[2] = MathProcessor.multiply(face.getUV3()[vy], texture.getHeight());
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
