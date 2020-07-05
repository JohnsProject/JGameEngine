package com.johnsproject.jgameengine.model;

import com.johnsproject.jgameengine.math.FixedPointMath;
import com.johnsproject.jgameengine.math.VectorMath;

public class Light extends SceneObject {
	
	public static final String LIGHT_TAG = "Light";
	
	private LightType type;
	private int strength;
	private int color;
	private int shadowColor;
	private int[] direction;
	private int spotSize;
	private int spotSoftness;
	private boolean isMain;
	
	public Light(String name, Transform transform) {
		super(name, transform);
		super.tag = LIGHT_TAG;
		super.rigidBody.setKinematic(true);
		this.type = LightType.DIRECTIONAL;
		this.strength = 100 * FixedPointMath.FP_ONE;
		this.direction = VectorMath.VECTOR_DOWN;
		this.spotSize = 60 * FixedPointMath.FP_ONE;
		this.spotSoftness = 800;
		this.isMain = false;
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
	
	public boolean isMain() {
		return isMain;
	}

	public void setMain(boolean isMain) {
		this.isMain = isMain;
	}
}
