/**
 * MIT License
 *
 * Copyright (c) 2018 John Salomon - John´s Project
 *  
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.johnsproject.jpge2.dto;

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
