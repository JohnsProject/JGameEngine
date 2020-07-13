package com.johnsproject.jgameengine.model;

import com.johnsproject.jgameengine.shading.Shader;

public class Material {

	private final int index;
	private final String name;
	private Shader shader;
	private int shininess;
	private int diffuseColor;
	private int specularColor;
	private Texture texture;
	
	public Material(int index, String name) {
		this.index = index;
		this.name = name;
		this.shader = null;
		this.shininess = 0;
		this.texture = null;
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

	public int getShininess() {
		return shininess;
	}

	public void setShininess(int shininess) {
		this.shininess = shininess;
	}

	public int getDiffuseColor() {
		return diffuseColor;
	}

	public void setDiffuseColor(int diffuseColor) {
		this.diffuseColor = diffuseColor;
	}
	
	public int getSpecularColor() {
		return specularColor;
	}

	public void setSpecularColor(int specularColor) {
		this.specularColor = specularColor;
	}

	public Texture getTexture() {
		return texture;
	}

	public void setTexture(Texture texture) {
		this.texture = texture;
	}
}
