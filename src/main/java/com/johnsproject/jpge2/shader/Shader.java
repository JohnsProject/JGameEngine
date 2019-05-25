package com.johnsproject.jpge2.shader;

import com.johnsproject.jpge2.dto.Camera;
import com.johnsproject.jpge2.dto.Face;
import com.johnsproject.jpge2.dto.Triangle;
import com.johnsproject.jpge2.dto.Vertex;
import com.johnsproject.jpge2.library.MathLibrary;
import com.johnsproject.jpge2.library.VectorLibrary;

public abstract class Shader {
	
	protected static final byte VECTOR_X = VectorLibrary.VECTOR_X;
	protected static final byte VECTOR_Y = VectorLibrary.VECTOR_Y;
	protected static final byte VECTOR_Z = VectorLibrary.VECTOR_Z;
	protected static final byte VECTOR_W = VectorLibrary.VECTOR_W;
	
	protected static final byte FP_BITS = MathLibrary.FP_BITS;
	protected static final int FP_ONE = MathLibrary.FP_ONE;
	protected static final int FP_HALF = MathLibrary.FP_HALF;
	
	protected final Triangle triangle;
	
	public Shader() {
		this.triangle = new Triangle();
	}
	
	public abstract void update(ShaderDataBuffer shaderDataBuffer);
	
	public abstract void setup(Camera camera);
	
	public abstract void vertex(int index, Vertex vertex);

	public abstract void geometry(Face face);

	public abstract void fragment(int[] location);

	public Triangle getTriangle() {
		return triangle;
	}
}
