package com.johnsproject.jgameengine.shader;

import com.johnsproject.jgameengine.math.ColorMath;
import com.johnsproject.jgameengine.math.FixedPointMath;
import com.johnsproject.jgameengine.model.Texture;

public class SpecularProperties implements ShaderProperties {

	private int diffuseIntensity;
	private int diffuseColor;
	private int specularIntensity;
	private int shininess;
	private Texture texture;
	
	public SpecularProperties() {
		diffuseIntensity = FixedPointMath.FP_ONE;
		diffuseIntensity = ColorMath.WHITE;
		specularIntensity = FixedPointMath.FP_ONE;
		shininess = 0;
		texture = null;
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
