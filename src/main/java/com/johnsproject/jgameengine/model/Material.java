package com.johnsproject.jgameengine.model;

import com.johnsproject.jgameengine.shader.Shader;

public class Material {

	private final int index;
	private final String name;
	private Shader shader;
	
	public Material(int index, String name, Shader shader) {
		this.index = index;
		this.name = name;
		this.shader = shader;
	}

	public int getIndex() {
		return index;
	}
	
	public String getName() {
		return name;
	}

	public Shader getShader() {
		return shader;
	}

	public void setShader(Shader shader) {
		this.shader = shader;
	}
}
