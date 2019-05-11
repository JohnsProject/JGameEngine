/**
 * MIT License
 *
 * Copyright (c) 2018 John Salomon - John´s Project
 *  
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.johnsproject.jpge2.dto;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

public class Texture {
	
	private int[] pixelBuffer;
	private int[] size;
	
	public Texture (BufferedImage bufferedImage){
		int width = bufferedImage.getWidth();
		int height = bufferedImage.getHeight();
		this.size = new int[] {width, height, width * height, 0};
		this.pixelBuffer = ((DataBufferInt)bufferedImage.getRaster().getDataBuffer()).getData();;
	}
	
	public Texture (int width, int height, int[] pixelBuffer){
		this.size = new int[] {width, height, width * height, 0};
		this.pixelBuffer = pixelBuffer;
	}
	
	public Texture (int width, int height){
		this.size = new int[] {width, height, width * height, 0};
		this.pixelBuffer = new int[size[2]];
	}
	
	public int[] getPixelBuffer() {
		return pixelBuffer;
	}
	
	public int[] getSize(){
		return size;
	}
	
	public int getPixel(int x, int y) {
		return pixelBuffer[x + (y * size[0])];
	}
	
	public void setPixel(int x, int y, int value) {
		pixelBuffer[x + (y * size[0])] = value;
	}
	
	public void fill(int value) {
		int[] pixelBuffer = getPixelBuffer();
		for (int i = 0; i < pixelBuffer.length; i++) {
			pixelBuffer[i] = value;
		}
	}
	
	public void fillUntil(int value, int width, int height) {
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				setPixel(i, j, value);
			}
		}
	}
	
	public void copy(Texture target) {
		for (int i = 0; i < target.getSize()[0]; i++) {
			for (int j = 0; j < target.getSize()[1]; j++) {
				target.setPixel(i, j, getPixel(i, j));
			}
		}
	}
}
