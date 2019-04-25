package com.johnsproject.jpge2.processor;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.IOException;
import java.io.InputStream;

import com.johnsproject.jpge2.dto.Texture;

public class TextureProcessor {

	private FileProcessor fileProcessor;
	
	TextureProcessor(FileProcessor fileProcessor) {
		this.fileProcessor = fileProcessor;
	}
	
	public Texture generate(String imagePath) throws IOException {
		BufferedImage img = fileProcessor.loadImage(imagePath);
		int[] pixelBuffer = ((DataBufferInt)img.getRaster().getDataBuffer()).getData();
		return new Texture(img.getWidth(), img.getHeight(), pixelBuffer);
	}
	
	public Texture generate(InputStream stream) throws IOException {
		BufferedImage img = fileProcessor.loadImage(stream);
		int[] pixelBuffer = ((DataBufferInt)img.getRaster().getDataBuffer()).getData();
		return new Texture(img.getWidth(), img.getHeight(), pixelBuffer);
	}
	
	public void fill(int value, Texture texture) {
		int[] pixelBuffer = texture.getPixelBuffer();
		for (int i = 0; i < pixelBuffer.length; i++) {
			pixelBuffer[i] = value;
		}
	}
	
	public void fillUntil(int value, int width, int height, Texture texture) {
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				texture.setPixel(i, j, value);
			}
		}
	}
	
	public void copy(Texture target, Texture source) {
		for (int i = 0; i < target.getSize()[0]; i++) {
			for (int j = 0; j < target.getSize()[1]; j++) {
				target.setPixel(i, j, source.getPixel(i, j));
			}
		}
	}
}
