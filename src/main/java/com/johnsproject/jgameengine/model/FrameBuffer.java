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
package com.johnsproject.jgameengine.model;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

import com.johnsproject.jgameengine.math.ColorMath;

public class FrameBuffer {

	private final int[] size;
	private final BufferedImage image;
	private final Texture colorBuffer;
	private final Texture depthBuffer;
	private final Texture stencilBuffer;

	public FrameBuffer(BufferedImage image) {
		this.size = new int[] {image.getWidth(), image.getHeight(), 0, 0};
		this.size[2] = size[0] * size[1];
		this.image = image;
		int[] pixelBuffer = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
		this.colorBuffer = new Texture(size[0], size[1], pixelBuffer);
		this.depthBuffer = new Texture(size[0], size[1]);
		this.stencilBuffer = new Texture(size[0], size[1]);
	}
	
	public FrameBuffer(int width, int height) {
		this.size = new int[] {width, height, 0, 0};
		this.size[2] = width * height;
		this.image = new BufferedImage(width, height, ColorMath.COLOR_TYPE);
		int[] pixelBuffer = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
		this.colorBuffer = new Texture(width, height, pixelBuffer);
		this.depthBuffer = new Texture(width, height);
		this.stencilBuffer = new Texture(width, height);
	}

	public BufferedImage getImage() {
		return image;
	}
	
	public Texture getColorBuffer() {
		return colorBuffer;
	}

	public Texture getDepthBuffer() {
		return depthBuffer;
	}
	
	public Texture getStencilBuffer() {
		return stencilBuffer;
	}

	public int getWidth() {
		return size[0];
	}
	
	public int getHeight() {
		return size[1];
	}
}
