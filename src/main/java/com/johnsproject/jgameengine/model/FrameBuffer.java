package com.johnsproject.jgameengine.model;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

import com.johnsproject.jgameengine.util.ColorUtils;

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
		this.image = new BufferedImage(width, height, ColorUtils.COLOR_TYPE);
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
