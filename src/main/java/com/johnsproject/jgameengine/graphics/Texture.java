package com.johnsproject.jgameengine.graphics;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

public class Texture {
	
	private final int[] pixels;
	private final int[] size;
	
	public Texture (BufferedImage bufferedImage){
		int width = bufferedImage.getWidth();
		int height = bufferedImage.getHeight();
		this.size = new int[] {width, height, width * height, 0};
		this.pixels = ((DataBufferInt)bufferedImage.getRaster().getDataBuffer()).getData();
	}
	
	public Texture (int width, int height, int[] pixels){
		this.size = new int[] {width, height, width * height, 0};
		this.pixels = pixels;
	}
	
	public Texture (int width, int height){
		this.size = new int[] {width, height, width * height, 0};
		this.pixels = new int[size[2]];
	}
	
	public int[] getPixels() {
		return pixels;
	}
	
	public int getWidth() {
		return size[0];
	}
	
	public int getHeight() {
		return size[1];
	}
	
	public int getPixel(int x, int y) {
		x = x >= 0 ? x : 0;
		x = x < size[0] ? x : size[0] - 1;
		y = y >= 0 ? y : 0;
		y = y < size[1] ? y : size[1] - 1;
		return pixels[x + (y * size[0])];
	}
	
	public void setPixel(int x, int y, int value) {
		x = x >= 0 ? x : 0;
		x = x < size[0] ? x : size[0] - 1;
		y = y >= 0 ? y : 0;
		y = y < size[1] ? y : size[1] - 1;
		pixels[x + (y * size[0])] = value;
	}
	
	public void fill(int value) {
		int[] pixelBuffer = getPixels();
		for (int i = 0; i < pixelBuffer.length; i++) {
			pixelBuffer[i] = value;
		}
	}
}
