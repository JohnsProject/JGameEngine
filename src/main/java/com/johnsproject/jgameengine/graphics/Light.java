package com.johnsproject.jgameengine.graphics;

import com.johnsproject.jgameengine.SceneObject;
import com.johnsproject.jgameengine.math.Fixed;
import com.johnsproject.jgameengine.math.Transform;
import com.johnsproject.jgameengine.math.Transformation;
import com.johnsproject.jgameengine.math.Vector;

public class Light extends SceneObject {
	
	public static final String LIGHT_TAG = "Light";
	
	private static final int DIRECTIONAL_BIAS = Fixed.toFixed(0.05f);
	private static final int SPOT_BIAS = Fixed.toFixed(2f);
	
	private LightType type;
	private int intensity;
	private int color;
	private int ambientColor;
	private final int[] directionRotation;
	private final int[] direction;
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
		this.intensity = Fixed.FP_ONE;
		this.color = Color.WHITE;
		this.ambientColor = Color.toColor(30, 30, 30);
		this.directionRotation = Vector.emptyVector();
		this.direction = Vector.VECTOR_FORWARD.clone();
		this.constantAttenuation = Fixed.toFixed(1);
		this.linearAttenuation = Fixed.toFixed(0.09);
		this.quadraticAttenuation = Fixed.toFixed(0.032);
		this.shadowBias = DIRECTIONAL_BIAS;
		this.hasShadow = true;
		this.isMain = false;
		setSpotSize(Fixed.toFixed(45));
		setInnerSpotSize(Fixed.toFixed(35));
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
			default:
				break;
			}
		}
	}
	
	private boolean hasShadowBiasDefaultValue() {
		return (shadowBias == DIRECTIONAL_BIAS)
				|| (shadowBias == SPOT_BIAS);
	}

	public int getIntensity() {
		return intensity;
	}

	public void setIntensity(int strength) {
		this.intensity = strength;
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

	/**
	 * Returns the direction of this {@link Light}.
	 * The direction is calculated based on the light's rotation.
	 * 
	 * @return The direction of this Light.
	 */
	public int[] getDirection() {
		if(!Vector.equals(directionRotation, transform.getRotation())) {
			synchronized (directionRotation) {
				Vector.copy(directionRotation, transform.getRotation());
				Vector.copy(direction, Vector.VECTOR_FORWARD);
				Transformation.rotateX(direction, directionRotation[Vector.VECTOR_X]);
				Transformation.rotateY(direction, directionRotation[Vector.VECTOR_Y]);
				Transformation.rotateZ(direction, directionRotation[Vector.VECTOR_Z]);
			}
		}
		return direction;
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
		this.spotSizeCos = Fixed.cos(degrees >> 1);
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
		this.innerSpotSizeCos = Fixed.cos(degrees >> 1);
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
