package com.johnsproject.jgameengine.model;

import static com.johnsproject.jgameengine.math.FixedPointMath.FP_ONE;

import com.johnsproject.jgameengine.math.MatrixMath;
import com.johnsproject.jgameengine.math.TransformationMath;

public class Camera extends SceneObject {

	public static final String CAMERA_TAG = "Camera";
	
	public static final byte FRUSTUM_LEFT = 0;
	public static final byte FRUSTUM_RIGHT = 1;
	public static final byte FRUSTUM_TOP = 2;
	public static final byte FRUSTUM_BOTTOM = 3;
	public static final byte FRUSTUM_NEAR = 4;
	public static final byte FRUSTUM_FAR = 5;
	public static final byte FRUSTUM_SIZE = 6;
	
	private final int[] frustum;
	private final int[] portedFrustum;
	private final int[][] projectionMatrix;
	private int focalLength;
	private CameraType type;
	private FrameBuffer renderTarget;
	private boolean isMain;

	public Camera(String name, Transform transform) {
		super(name, transform);
		super.tag = CAMERA_TAG;
		super.rigidBody.setKinematic(true);
		this.type = CameraType.PERSPECTIVE;
		this.focalLength = FP_ONE;
		this.projectionMatrix = MatrixMath.indentityMatrix();
		this.renderTarget = null;
		this.portedFrustum = new int[FRUSTUM_SIZE];
		this.frustum = new int[FRUSTUM_SIZE];
		this.frustum[FRUSTUM_LEFT] = 0;
		this.frustum[FRUSTUM_RIGHT] = FP_ONE;
		this.frustum[FRUSTUM_TOP] = 0;
		this.frustum[FRUSTUM_BOTTOM] = FP_ONE;
		this.frustum[FRUSTUM_NEAR] = FP_ONE;
		this.frustum[FRUSTUM_FAR] = FP_ONE * 1000;
		this.isMain = false;
	}

	private void recalculateFrustum() {
		for (int i = 0; i < Camera.FRUSTUM_SIZE; i++) {
			portedFrustum[i] = frustum[i];
		}
		TransformationMath.screenportFrustum(portedFrustum, renderTarget.getWidth(), renderTarget.getHeight());
		recalculateProjectionMatrix();
	}
	
	private void recalculateProjectionMatrix() {
		switch (type) {
		case ORTHOGRAPHIC:
			TransformationMath.orthographicMatrix(projectionMatrix, portedFrustum, focalLength);
			break;

		case PERSPECTIVE:
			TransformationMath.perspectiveMatrix(projectionMatrix, portedFrustum, focalLength);
			break;
		}
	}
	
	public void setFrustum(int left, int right, int top, int bottom, int near, int far) {
		frustum[FRUSTUM_LEFT] = left;
		frustum[FRUSTUM_RIGHT] = right;
		frustum[FRUSTUM_TOP] = top;
		frustum[FRUSTUM_BOTTOM] = bottom;
		frustum[FRUSTUM_NEAR] = near;
		frustum[FRUSTUM_FAR] = far;
		recalculateFrustum();
	}
	
	public void setFrustum(int index, int value) {
		frustum[index] = value;
		recalculateFrustum();
	}
	
	public int[] getFrustum() {
		return frustum;
	}
	
	public int[] getRenderTargetPortedFrustum() {
		return portedFrustum;
	}

	public CameraType getType() {
		return type;
	}

	public void setType(CameraType type) {
		if(!this.type.equals(type)) {
			this.type = type;
			recalculateProjectionMatrix();
		}
	}

	public int getFocalLength() {
		return focalLength;
	}

	public void setFocalLength(int focalLength) {
		if(this.focalLength != focalLength) {
			this.focalLength = focalLength;
			recalculateProjectionMatrix();
		}
	}

	public FrameBuffer getRenderTarget() {
		return renderTarget;
	}

	public void setRenderTarget(FrameBuffer renderTarget) {
		if(this.renderTarget != renderTarget) {
			this.renderTarget = renderTarget;
			recalculateFrustum();
		}
	}

	public int[][] getProjectionMatrix() {
		return projectionMatrix;
	}

	public boolean isMain() {
		return isMain;
	}

	public void setMain(boolean isMain) {
		this.isMain = isMain;
	}
}
