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

public class GraphicsBuffer {
	
	private int width;
	private int height;
	private int length;
	private BufferedImage frameBuffer;
	private int[] frameBufferData;
	private int[] depthBuffer;
	
	public GraphicsBuffer() {
		setSize(1, 1);
	}
	public GraphicsBuffer(int width, int height) {
		setSize(width, height);
	}
	
	public BufferedImage getFrameBuffer() {
		return frameBuffer;
	}
	
	public int[] getDepthBuffer() {
		return depthBuffer;
	}
	
	public void clearFrameBuffer() {
		for (int i = 0; i < frameBufferData.length; i++) {
			frameBufferData[i] = 0;
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
		this.frameBuffer = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB_PRE);
		this.frameBufferData = ((DataBufferInt)frameBuffer.getRaster().getDataBuffer()).getData();
		this.depthBuffer = new int[length];
	}
	
	public void setPixel(int x, int y, int z, int color) {
		// check if pixel is inside RenderBuffer
		if ((x >= 0 && x <= width) && (y >= 0 && y <= height)) {
			int pos = x + (y * width);
			if (pos < length) {
				// z test
				if (depthBuffer[pos] > z) {
					depthBuffer[pos] = z;
					frameBufferData[pos] = color;
				}
			}
		}
	}
	
	public int getPixel(int x, int y) {
		int pos = x + (y * width);
		if (pos < length) 
			return frameBufferData[pos];
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
