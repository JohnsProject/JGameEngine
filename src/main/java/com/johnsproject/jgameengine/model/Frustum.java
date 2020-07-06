package com.johnsproject.jgameengine.model;

import static com.johnsproject.jgameengine.math.FixedPointMath.FP_ONE;

import com.johnsproject.jgameengine.math.MatrixMath;
import com.johnsproject.jgameengine.math.TransformationMath;

public class Frustum {

	private int left;
	private int right;
	private int top;
	private int bottom;
	private int near;
	private int far;
	
	private int screenLeft;
	private int screenRight;
	private int screenTop;
	private int screenBottom;
	
	private int screenWidth;
	private int screenHeight;

	private int focalLength;
	private int[][] projectionMatrix;
	
	private CameraType type;
	
	public Frustum() {
		this.focalLength = FP_ONE;
		this.projectionMatrix = MatrixMath.indentityMatrix();
		this.type = CameraType.PERSPECTIVE;
	}

	public Frustum(int left, int right, int top, int bottom, int near, int far) {
		this();
		this.left = left;
		this.right = right;
		this.top = top;
		this.bottom = bottom;
		this.near = near;
		this.far = far;
		recalculateFrustum();
	}

	private void recalculateFrustum() {
		TransformationMath.screenportFrustum(this);
		recalculateProjectionMatrix();
	}
	
	private void recalculateProjectionMatrix() {
		switch (type) {
		case ORTHOGRAPHIC:
			TransformationMath.orthographicMatrix(projectionMatrix, this);
			break;

		case PERSPECTIVE:
			TransformationMath.perspectiveMatrix(projectionMatrix, this);
			break;
		}
	}

	public void setFrustum(int left, int right, int top, int bottom, int near, int far) {
		this.left = left;
		this.right = right;
		this.top = top;
		this.bottom = bottom;
		this.near = near;
		this.far = far;
		recalculateFrustum();
	}
	
	public void setScreenFrustum(int left, int right, int top, int bottom) {
		this.screenLeft = left;
		this.screenRight = right;
		this.screenTop = top;
		this.screenBottom = bottom;
	}
	
	public void setScreenSize(int width, int height) {
		this.screenWidth = width;
		this.screenHeight = height;
		recalculateFrustum();
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

	public void setType(CameraType type) {
		if(!this.type.equals(type)) {
			this.type = type;
			recalculateProjectionMatrix();
		}
	}

	public int getLeft() {
		return left;
	}

	public int getRight() {
		return right;
	}

	public int getTop() {
		return top;
	}

	public int getBottom() {
		return bottom;
	}

	public int getNear() {
		return near;
	}

	public int getFar() {
		return far;
	}

	public int getScreenLeft() {
		return screenLeft;
	}

	public int getScreenRight() {
		return screenRight;
	}

	public int getScreenTop() {
		return screenTop;
	}

	public int getScreenBottom() {
		return screenBottom;
	}

	public int getScreenWidth() {
		return screenWidth;
	}

	public int getScreenHeight() {
		return screenHeight;
	}

	public int[][] getProjectionMatrix() {
		return projectionMatrix;
	}

	public CameraType getType() {
		return type;
	}
}
