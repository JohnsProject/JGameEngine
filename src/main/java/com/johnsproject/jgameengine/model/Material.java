package com.johnsproject.jgameengine.model;

import com.johnsproject.jgameengine.shading.Shader;
import com.johnsproject.jgameengine.util.ColorUtils;
import com.johnsproject.jgameengine.util.FixedPointUtils;

public class Material {

	private final int index;
	private final String name;
	private Shader shader;
	private int diffuseIntensity;
	private int shininess;
	private int ambientColor;
	private int diffuseColor;
	private int specularColor;
	private int specularIntensity;
	private Texture texture;
	
	public Material(int index, String name) {
		this.index = index;
		this.name = name;
		this.shader = null;
		this.diffuseIntensity = FixedPointUtils.FP_ONE;
		this.diffuseIntensity = ColorUtils.WHITE;
		this.specularIntensity = FixedPointUtils.FP_ONE;
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
	
	public int getDiffuseIntensity() {
		return diffuseIntensity;
	}

	public void setDiffuseIntensity(int diffuseIntensity) {
		this.diffuseIntensity = diffuseIntensity;
	}

	public int getSpecularIntensity() {
		return specularIntensity;
	}

	public void setSpecularIntensity(int specularIntensity) {
		this.specularIntensity = specularIntensity;
	}

	public int getShininess() {
		return shininess;
	}

	public void setShininess(int shininess) {
		this.shininess = shininess;
	}

	public int getAmbientColor() {
		return ambientColor;
	}

	public void setAmbientColor(int ambientColor) {
		this.ambientColor = ambientColor;
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
