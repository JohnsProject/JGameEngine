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

public class FrameBuffer {

	private int width;
	private int height;
	private int length;
	private BufferedImage image;
	private int[] colorBuffer;
	private int[] depthBuffer;

	public FrameBuffer() {
		setSize(1, 1);
	}

	public FrameBuffer(int width, int height) {
		setSize(width, height);
	}

	public BufferedImage getImage() {
		return image;
	}
	
	public int[] getColorBuffer() {
		return colorBuffer;
	}

	public int[] getDepthBuffer() {
		return depthBuffer;
	}

	public void clearColorBuffer() {
		for (int i = 0; i < colorBuffer.length; i++) {
			colorBuffer[i] = 0;
		}
	}

	public void clearDepthBuffer() {
		for (int i = 0; i < depthBuffer.length; i++) {
			depthBuffer[i] = Integer.MAX_VALUE;
		}
	}

	public void setSize(int width, int height) {
		this.width = width;
		this.height = height;
		this.length = width * height;
		this.image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB_PRE);
		this.colorBuffer = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
		this.depthBuffer = new int[length];
	}

	public void setPixel(int x, int y, int z, int color) {
		int pos = x + (y * width);
		if (depthBuffer[pos] > z) {
			depthBuffer[pos] = z;
			colorBuffer[pos] = color;
		}
	}
	
	public void setPixel(int x, int y, int color) {
		int pos = x + (y * width);
		colorBuffer[pos] = color;
	}

	public int getPixel(int x, int y) {
		int pos = x + (y * width);
		if (pos < length)
			return colorBuffer[pos];
		return -1;
	}

	public int getPixelDepth(int x, int y) {
		int pos = x + (y * width);
		if (pos < length)
			return depthBuffer[pos];
		return -1;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}
}
