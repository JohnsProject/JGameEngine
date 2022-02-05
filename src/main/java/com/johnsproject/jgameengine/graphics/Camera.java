package com.johnsproject.jgameengine.graphics;

import static com.johnsproject.jgameengine.math.Fixed.FP_ONE;

import com.johnsproject.jgameengine.SceneObjectComponent;
import com.johnsproject.jgameengine.math.Frustum;
import com.johnsproject.jgameengine.math.Transform;

public class Camera extends SceneObjectComponent {
	
	private Frustum frustum;
	private FrameBuffer renderTarget;
	private int lightDistance;
	private boolean isMain;
	private Transform transform;

	public Camera() {
		this.renderTarget = null;
		this.frustum = new Frustum(0, FP_ONE, 0, FP_ONE, FP_ONE, FP_ONE * 1000);
		this.lightDistance = FP_ONE * 100;
		this.isMain = false;
	}
	
	public Camera(Frustum frustum) {
		this();
		this.frustum = frustum;
	}
	
	public Transform getTransform() {
		if(transform == null)
			transform = owner.getComponentWithType(Transform.class);

		if(transform == null) {
			transform = new Transform();
			owner.addComponent(transform);
		}
		
		return transform;
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
