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
	
	private final int[] pixel;
	private final int[] size;
	
	public Texture (BufferedImage bufferedImage){
		int width = bufferedImage.getWidth();
		int height = bufferedImage.getHeight();
		this.size = new int[] {width, height, width * height, 0};
		this.pixel = ((DataBufferInt)bufferedImage.getRaster().getDataBuffer()).getData();;
	}
	
	public Texture (int width, int height, int[] pixelBuffer){
		this.size = new int[] {width, height, width * height, 0};
		this.pixel = pixelBuffer;
	}
	
	public Texture (int width, int height){
		this.size = new int[] {width, height, width * height, 0};
		this.pixel = new int[size[2]];
	}
	
	public int[] getPixel() {
		return pixel;
	}
	
	public int getWidth() {
		return size[0];
	}
	
	public int getHeight() {
		return size[1];
	}
	
	public int getPixel(int x, int y) {
		x = x > 0 ? x : 0;
		x = x < size[0] ? x : size[0] - 1;
		y = y > 0 ? y : 0;
		y = y < size[1] ? y : size[1] - 1;
		return pixel[x + (y * size[0])];
	}
	
	public void setPixel(int x, int y, int value) {
		x = x > 0 ? x : 0;
		x = x < size[0] ? x : size[0] - 1;
		y = y > 0 ? y : 0;
		y = y < size[1] ? y : size[1] - 1;
		pixel[x + (y * size[0])] = value;
	}
	
	public void fill(int value) {
		int[] pixelBuffer = getPixel();
		for (int i = 0; i < pixelBuffer.length; i++) {
			pixelBuffer[i] = value;
		}
	}
}
