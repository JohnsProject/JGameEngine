package com.johnsproject.jpge2.dto;

import java.util.List;

public class ShaderDataBuffer {
	
	private FrameBuffer frameBuffer;
	private List<Light> lights;
	
	public FrameBuffer getFrameBuffer() {
		return frameBuffer;
	}

	public void setFrameBuffer(FrameBuffer frameBuffer) {
		this.frameBuffer = frameBuffer;
	}

	public List<Light> getLights() {
		return lights;
	}

	public void setLights(List<Light> lights) {
		this.lights = lights;
	}
	
}