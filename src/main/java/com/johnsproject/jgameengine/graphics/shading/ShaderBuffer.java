package com.johnsproject.jgameengine.graphics.shading;

import java.util.List;

import com.johnsproject.jgameengine.graphics.Camera;
import com.johnsproject.jgameengine.graphics.Light;

public interface ShaderBuffer {

	public void initialize(Camera camera, List<Light> lights);
	
	public List<Light> getLights();
	
	public Camera getCamera();
	
}
