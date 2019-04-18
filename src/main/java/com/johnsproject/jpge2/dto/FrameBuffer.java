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

	private int[] size;
	private BufferedImage image;
	private int[] colorBuffer;
	private int[] depthBuffer;
	private byte[] stencilBuffer;

	public FrameBuffer(int[] size) {
		setSize(size);
	}

	public FrameBuffer(int width, int height) {
		this.size = new int[] {width, height, 0, 0};
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
	
	public byte[] getStencilBuffer() {
		return stencilBuffer;
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
	
	public void clearStencilBuffer() {
		for (int i = 0; i < depthBuffer.length; i++) {
			stencilBuffer[i] = 0;
		}
	}
	
	public void setSize(int[] size) {
		this.size = size;
		this.image = new BufferedImage(size[0], size[1], BufferedImage.TYPE_INT_ARGB_PRE);
		this.colorBuffer = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
		this.depthBuffer = new int[size[2]];
		this.stencilBuffer = new byte[size[2]];
	}

	public void setSize(int width, int height) {
		this.size[0] = width;
		this.size[1] = height;
		this.size[2] = width * height;
		this.image = new BufferedImage(size[0], size[1], BufferedImage.TYPE_INT_ARGB_PRE);
		this.colorBuffer = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
		this.depthBuffer = new int[size[2]];
		this.stencilBuffer = new byte[size[2]];
	}

	public void setPixel(int x, int y, int z, byte stencil, int color) {
		int pos = x + (y * size[0]);
		if (depthBuffer[pos] > z) {
			depthBuffer[pos] = z;
			stencilBuffer[pos] = stencil;
			colorBuffer[pos] = color;
		}
	}
	
	public void setColor(int x, int y, int color) {
		int pos = x + (y * size[0]);
		colorBuffer[pos] = color;
	}
	
	public void setDepth(int x, int y, int z) {
		int pos = x + (y * size[0]);
		depthBuffer[pos] = z;
	}
	
	public void setStencil(int x, int y, byte stencil) {
		int pos = x + (y * size[0]);
		stencilBuffer[pos] = stencil;
	}

	public int getColor(int x, int y) {
		int pos = x + (y * size[0]);
		return colorBuffer[pos];
	}

	public int getDepth(int x, int y) {
		int pos = x + (y * size[0]);
		return depthBuffer[pos];
	}
	
	public byte getStencil(int x, int y) {
		int pos = x + (y * size[0]);
		return stencilBuffer[pos];
	}

	public int[] getSize() {
		return size;
	}
}
