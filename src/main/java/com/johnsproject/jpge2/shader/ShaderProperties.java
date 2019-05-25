package com.johnsproject.jpge2.shader;

import com.johnsproject.jpge2.dto.Texture;

public class ShaderProperties {
	
	private int diffuseIntensity;
	private int diffuseColor;
	private int specularIntensity;
	private int shininess;
	private Texture texture;
	
	public ShaderProperties(int diffuseColor, int diffuseIntensity, int specularIntensity, int shininess, Texture texture) {
		this.diffuseIntensity = diffuseIntensity;
		this.diffuseColor = diffuseColor;
		this.specularIntensity = specularIntensity;
		this.shininess = shininess;
		this.texture = texture;
	}
	
	public int getDiffuseIntensity() {
		return diffuseIntensity;
	}
	public void setDiffuseIntensity(int diffuseIntensity) {
		this.diffuseIntensity = diffuseIntensity;
	}
	public int getDiffuseColor() {
		return diffuseColor;
	}
	public void setDiffuseColor(int diffuseColor) {
		this.diffuseColor = diffuseColor;
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
	public Texture getTexture() {
		return texture;
	}
	public void setTexture(Texture texture) {
		this.texture = texture;
	}
}
