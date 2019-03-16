package com.johnsproject.jpge2;

import com.johnsproject.jpge2.dto.Face;
import com.johnsproject.jpge2.dto.Light;
import com.johnsproject.jpge2.dto.Vertex;

public interface Shader {

	public void vertex(Vertex vertex);
	public void geometry(Face face, Light light);
	public int fragment(int x, int y, int z);
	
}
