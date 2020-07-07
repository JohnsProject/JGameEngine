package com.johnsproject.jgameengine.model;

import static com.johnsproject.jgameengine.util.FixedPointUtils.FP_ONE;

public class Camera extends SceneObject {

	public static final String CAMERA_TAG = "Camera";
	
	private Frustum frustum;
	private FrameBuffer renderTarget;
	private boolean isMain;

	public Camera(String name, Transform transform) {
		super(name, transform);
		super.tag = CAMERA_TAG;
		super.rigidBody.setKinematic(true);
		this.frustum = new Frustum(0, FP_ONE, 0, FP_ONE, FP_ONE, FP_ONE * 1000);
		this.renderTarget = null;
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
		this.renderTarget = renderTarget;
		frustum.setRenderTargetSize(renderTarget.getWidth(), renderTarget.getHeight());
	}

	public boolean isMain() {
		return isMain;
	}

	public void setMain(boolean isMain) {
		this.isMain = isMain;
	}
}
