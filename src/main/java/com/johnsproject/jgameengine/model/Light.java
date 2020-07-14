package com.johnsproject.jgameengine.model;

import com.johnsproject.jgameengine.util.ColorUtils;
import com.johnsproject.jgameengine.util.FixedPointUtils;
import com.johnsproject.jgameengine.util.VectorUtils;

public class Light extends SceneObject {
	
	public static final String LIGHT_TAG = "Light";
	
	private static final int DIRECTIONAL_BIAS = FixedPointUtils.toFixedPoint(0.00005f);
	private static final int SPOT_BIAS = FixedPointUtils.toFixedPoint(0.00025f);
	private static final int POINT_BIAS = FixedPointUtils.toFixedPoint(0.00035f);
	
	private LightType type;
	private int strength;
	private int color;
	private int ambientColor;
	private int[] direction;
	private int spotSize;
	private int spotSizeCos;
	private int innerSpotSize;
	private int innerSpotSizeCos;
	private int spotSoftness;
	private int constantAttenuation;
	private int linearAttenuation;
	private int quadraticAttenuation;
	private int shadowBias;
	private boolean hasShadow;
	private boolean isMain;
	
	public Light(String name, Transform transform) {
		super(name, transform);
		super.tag = LIGHT_TAG;
		super.rigidBody.setKinematic(true);
		this.type = LightType.DIRECTIONAL;
		this.strength = FixedPointUtils.FP_ONE;
		this.color = ColorUtils.WHITE;
		this.ambientColor = ColorUtils.toColor(30, 30, 30);
		this.direction = VectorUtils.VECTOR_DOWN;
		this.constantAttenuation = FixedPointUtils.toFixedPoint(1);
		this.linearAttenuation = FixedPointUtils.toFixedPoint(0.09);
		this.quadraticAttenuation = FixedPointUtils.toFixedPoint(0.032);
		this.shadowBias = DIRECTIONAL_BIAS;
		this.hasShadow = true;
		this.isMain = false;
		setSpotSize(FixedPointUtils.toFixedPoint(45));
		setInnerSpotSize(FixedPointUtils.toFixedPoint(35));
	}
	
	public LightType getType() {
		return type;
	}

	public void setType(LightType type) {
		this.type = type;
		if(hasShadowBiasDefaultValue()) {
			switch (type) {
			case DIRECTIONAL:
				shadowBias = DIRECTIONAL_BIAS;
				break;
			case SPOT:
				shadowBias = SPOT_BIAS;
				break;
			case POINT:
				shadowBias = POINT_BIAS;
				break;
			}
		}
	}
	
	private boolean hasShadowBiasDefaultValue() {
		return (shadowBias == DIRECTIONAL_BIAS)
				|| (shadowBias == SPOT_BIAS)
				|| (shadowBias == POINT_BIAS);
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

	public int getAmbientColor() {
		return ambientColor;
	}

	public void setAmbientColor(int ambientColor) {
		this.ambientColor = ambientColor;
	}

	public int[] getDirection() {
		return direction;
	}

	public void setDirection(int[] direction) {
		this.direction = direction;
	}
	
	/**
	 * Returns the cosine of the spot size of this {@link Light}.
	 * This is needed for lighting calculations.
	 * 
	 * @return The cosine of the spot size.
	 */
	public int getSpotSizeCosine() {
		return spotSizeCos;
	}

	public int getSpotSize() {
		return spotSize;
	}

	public void setSpotSize(int degrees) {
		this.spotSize = degrees;
		// divide by 2 so the size is the size of the whole spot
		this.spotSizeCos = FixedPointUtils.cos(degrees >> 1);
		calculateSpotSoftness();
	}
	
	/**
	 * Returns the cosine of the inner spot size of this {@link Light}.
	 * This is needed for lighting calculations.
	 * 
	 * @return The cosine of the inner spot size.
	 */
	public int getInnerSpotSizeCosine() {
		return innerSpotSizeCos;
	}
	
	public int getInnerSpotSize() {
		return innerSpotSize;
	}

	public void setInnerSpotSize(int degrees) {
		this.innerSpotSize = degrees;
		this.innerSpotSizeCos = FixedPointUtils.cos(degrees >> 1);
		calculateSpotSoftness();
	}
	
	/**
	 * Returns the difference between {@link #getInnerSpotSizeCosine()} and {@link #getSpotSizeCosine()}.
	 * This is needed for lighting calculations.
	 * 
	 * @return The difference between innerSpotSize and spotSize.
	 */
	public int getSpotSoftness() {
		return spotSoftness;
	}
	
	private void calculateSpotSoftness() {
		// + 1 because it can't be 0
		spotSoftness = (innerSpotSizeCos - spotSizeCos) + 1;
	}

	public int getConstantAttenuation() {
		return constantAttenuation;
	}

	public void setConstantAttenuation(int constantAttenuation) {
		this.constantAttenuation = constantAttenuation;
	}

	public int getLinearAttenuation() {
		return linearAttenuation;
	}

	public void setLinearAttenuation(int linearAttenuation) {
		this.linearAttenuation = linearAttenuation;
	}

	public int getQuadraticAttenuation() {
		return quadraticAttenuation;
	}

	public void setQuadraticAttenuation(int quadraticAttenuation) {
		this.quadraticAttenuation = quadraticAttenuation;
	}

	public int getShadowBias() {
		return shadowBias;
	}

	public void setShadowBias(int shadowBias) {
		this.shadowBias = shadowBias;
	}

	public boolean hasShadow() {
		return hasShadow;
	}

	public void setShadow(boolean hasShadow) {
		this.hasShadow = hasShadow;
	}

	public boolean isMain() {
		return isMain;
	}

	public void setMain(boolean isMain) {
		this.isMain = isMain;
	}
}
