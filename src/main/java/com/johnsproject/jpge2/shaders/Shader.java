package com.johnsproject.jpge2.shaders;

import java.util.List;

import com.johnsproject.jpge2.dto.Camera;
import com.johnsproject.jpge2.dto.Face;
import com.johnsproject.jpge2.dto.FrameBuffer;
import com.johnsproject.jpge2.dto.Light;
import com.johnsproject.jpge2.dto.Model;
import com.johnsproject.jpge2.dto.Vertex;

public abstract class Shader {
	
	public abstract void update(List<Light> lights, FrameBuffer frameBuffer);
	
	public abstract void setup(Model model, Camera camera);
	
	public abstract void vertex(int index, Vertex vertex);

	public abstract void geometry(Face face);

	public abstract void fragment(int[] location, int[] barycentric);
	
}
