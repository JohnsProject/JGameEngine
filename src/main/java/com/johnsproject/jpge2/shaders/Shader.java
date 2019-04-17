package com.johnsproject.jpge2.shaders;

import java.util.List;

import com.johnsproject.jpge2.dto.Camera;
import com.johnsproject.jpge2.dto.Face;
import com.johnsproject.jpge2.dto.FrameBuffer;
import com.johnsproject.jpge2.dto.Light;
import com.johnsproject.jpge2.dto.Model;
import com.johnsproject.jpge2.dto.Vertex;

public interface Shader {
	
	public void update(List<Light> lights, FrameBuffer frameBuffer);
	
	public void setup(Model model, Camera camera);
	
	public void vertex(int index, Vertex vertex);

	public void geometry(Face face);

	public void fragment(int[] location, int[] barycentric);
	
}
