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
package com.johnsproject.jgameengine.model;

import com.johnsproject.jgameengine.math.FixedPointMath;
import com.johnsproject.jgameengine.math.VectorMath;

public class Light extends SceneObject {
	
	public static final String LIGHT_TAG = "Light";
	public static final String MAIN_DIRECTIONAL_LIGHT_TAG = "MainDirectionalLight";
	public static final String MAIN_SPOT_LIGHT_TAG = "MainSpotLight";
	public static final String MAIN_POINT_LIGHT_TAG = "MainPointLight";
	
	private LightType type;
	private int strength;
	private int color;
	private int shadowColor;
	private int[] direction;
	private int spotSize;
	private int spotSoftness;
	
	public Light(String name, Transform transform) {
		super(name, transform);
		super.tag = LIGHT_TAG;
		super.rigidBody.setKinematic(true);
		this.type = LightType.DIRECTIONAL;
		this.strength = 100 * FixedPointMath.FP_ONE;
		this.direction = VectorMath.VECTOR_DOWN;
		this.spotSize = 60 * FixedPointMath.FP_ONE;
		this.spotSoftness = 800;
	}

	public LightType getType() {
		return type;
	}

	public void setType(LightType type) {
		this.type = type;
	}

	public int getStrength() {
		return strength;
	}

	public void setStrength(int strength) {
		this.strength = strength;
	}

	public int getColor() {
		return color;
	}

	public void setColor(int color) {
		this.color = color;
	}

	public int getShadowColor() {
		return shadowColor;
	}

	public void setShadowColor(int shadowColor) {
		this.shadowColor = shadowColor;
	}

	public int[] getDirection() {
		return direction;
	}

	public void setDirection(int[] direction) {
		this.direction = direction;
	}

	public int getSpotSize() {
		return spotSize;
	}

	public void setSpotSize(int spotSize) {
		this.spotSize = spotSize;
	}

	public int getSpotSoftness() {
		return spotSoftness;
	}

	public void setSpotSoftness(int spotSoftness) {
		this.spotSoftness = spotSoftness;
	}
}
