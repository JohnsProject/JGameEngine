package com.johnsproject.jpge2;

import com.johnsproject.jpge2.dto.Camera;
import com.johnsproject.jpge2.dto.Face;
import com.johnsproject.jpge2.dto.Light;
import com.johnsproject.jpge2.dto.Vertex;
import com.johnsproject.jpge2.processing.VectorProcessor;

public abstract class Shader {

	protected static final int vx = VectorProcessor.VECTOR_X;
	protected static final int vy = VectorProcessor.VECTOR_Y;
	protected static final int vz = VectorProcessor.VECTOR_Z;
	protected static final int vw = VectorProcessor.VECTOR_W;
	
	protected Light light;
	protected Camera camera;
	
	public void setup(Camera camera, Light light) {
		this.light = light;
		this.camera = camera;
	}
	
	public abstract void vertex(Vertex vertex);
	public abstract void geometry(Face face);
	public abstract int fragment(int x, int y, int z);
	
}
