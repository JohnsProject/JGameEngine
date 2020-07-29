package com.johnsproject.jgameengine.model;

import static com.johnsproject.jgameengine.util.FixedPointUtils.FP_ONE;

public class Camera extends SceneObject {

	public static final String CAMERA_TAG = "Camera";
	
	private Frustum frustum;
	private FrameBuffer renderTarget;
	private int lightDistance;
	private boolean isMain;

	public Camera(String name, Transform transform) {
		super(name, transform);
		super.tag = CAMERA_TAG;
		super.rigidBody.setKinematic(true);
		this.renderTarget = null;
		this.frustum = new Frustum(0, FP_ONE, 0, FP_ONE, FP_ONE, FP_ONE * 1000);
		this.lightDistance = FP_ONE * 100;
		this.isMain = false;
	}
	
	public Camera(String name, Transform transform, Frustum frustum) {
		this(name, transform);
		this.frustum = frustum;
	}
	
	public Frustum getFrustum() {
		return frustum;
	}

	public FrameBuffer getRenderTarget() {
		return renderTarget;
	}

	public void setRenderTarget(FrameBuffer renderTarget) {
		if(this.renderTarget != renderTarget) {
			this.renderTarget = renderTarget;
			frustum.setRenderTargetSize(renderTarget.getWidth(), renderTarget.getHeight());
		}
	}

	/**
	 * Returns the max light to camera distance a light can have before the
	 * light gets culled and doesn't affect the rendering of this camera anymore.
	 * 
	 * @return The max light to camera distance. Default is 100.
	 */
	public int getMaxLightDistance() {
		return lightDistance;
	}

	/**
	 * Sets the max light to camera distance a light can have before the
	 * light gets culled and doesn't affect the rendering of this camera anymore.
	 * 
	 * @param lightDistance fixed point value. Default is 100.
	 */
	public void setMaxLightDistance(int lightDistance) {
		this.lightDistance = lightDistance;
	}

	public boolean isMain() {
		return isMain;
	}

	public void setMain(boolean isMain) {
		this.isMain = isMain;
	}
}
