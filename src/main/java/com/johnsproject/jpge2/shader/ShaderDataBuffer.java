package com.johnsproject.jpge2.shader;

import java.util.List;

import com.johnsproject.jpge2.dto.FrameBuffer;
import com.johnsproject.jpge2.dto.Light;

public interface ShaderDataBuffer {

	public FrameBuffer getFrameBuffer();
	
	public void setFrameBuffer(FrameBuffer frameBuffer);
	
	public List<Light> getLights();
	
	public void setLights(List<Light> lights);
	
}