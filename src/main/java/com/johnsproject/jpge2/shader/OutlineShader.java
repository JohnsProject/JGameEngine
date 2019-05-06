package com.johnsproject.jpge2.shader;

import com.johnsproject.jpge2.dto.Camera;
import com.johnsproject.jpge2.dto.Face;
import com.johnsproject.jpge2.dto.FrameBuffer;
import com.johnsproject.jpge2.dto.Vertex;
import com.johnsproject.jpge2.primitive.Texture;
import com.johnsproject.jpge2.processor.CentralProcessor;
import com.johnsproject.jpge2.processor.ColorProcessor;

public class OutlineShader extends Shader {
	
	public OutlineShader(CentralProcessor centralProcessor) {
		super(centralProcessor);
	}

	@Override
	public void update(ShaderDataBuffer shaderDataBuffer) {
		FrameBuffer frameBuffer = shaderDataBuffer.getFrameBuffer();
		Texture colorBuffer = frameBuffer.getColorBuffer();
		Texture depthBuffer = frameBuffer.getDepthBuffer();
		int threshold = 15;
		int samples = 2;
		for (int y = samples; y < colorBuffer.getSize()[1] - samples; y++) {
			for (int x = samples; x < colorBuffer.getSize()[0] - samples; x++) {
				int cz = depthBuffer.getPixel(x, y);
				
				int z = depthBuffer.getPixel(x-1, y);
				if (Math.abs(z - cz) > threshold) {
					for (int s = 0; s < samples; s++) {
						colorBuffer.setPixel(x-s, y, ColorProcessor.BLACK);
					}
				}
				z = depthBuffer.getPixel(x+1, y);
				if (Math.abs(z - cz) > threshold) {
					for (int s = 0; s < samples; s++) {
						colorBuffer.setPixel(x+s, y, ColorProcessor.BLACK);
					}
				}
				z = depthBuffer.getPixel(x, y-1);
				if (Math.abs(z - cz) > threshold) {
					for (int s = 0; s < samples; s++) {
						colorBuffer.setPixel(x, y-s, ColorProcessor.BLACK);
					}
				}
				z = depthBuffer.getPixel(x, y+1);
				if (Math.abs(z - cz) > threshold) {
					for (int s = 0; s < samples; s++) {
						colorBuffer.setPixel(x, y+s, ColorProcessor.BLACK);
					}
				}
			}
		}
	}

	@Override
	public void setup(Camera camera) {
	}

	@Override
	public void vertex(int index, Vertex vertex) {
	}

	@Override
	public void geometry(Face face) {
	}

	@Override
	public void fragment(int[] location, int[] barycentric) {		
	}

}
