package com.johnsproject.jgameengine.math;

import static com.johnsproject.jgameengine.math.Fixed.FP_ONE;

public class Frustum {

	private int left;
	private int right;
	private int top;
	private int bottom;
	private int near;
	private int far;
	
	private int renderTargetLeft;
	private int renderTargetRight;
	private int renderTargetTop;
	private int renderTargetBottom;
	
	private int renderTargetWidth;
	private int renderTargetHeight;

	private int focalLength;
	private int[][] projectionMatrix;
	
	private FrustumType type;
	
	public Frustum() {
		this.focalLength = FP_ONE;
		this.projectionMatrix = Matrix.indentityMatrix();
		this.type = FrustumType.PERSPECTIVE;
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
		renderTargetLeft = Fixed.multiply(renderTargetWidth, left);
		renderTargetRight = Fixed.multiply(renderTargetWidth, right);
		renderTargetTop = Fixed.multiply(renderTargetHeight, top);
		renderTargetBottom = Fixed.multiply(renderTargetHeight, bottom);
		recalculateProjectionMatrix();
	}
	
	private void recalculateProjectionMatrix() {
		switch (type) {
		case ORTHOGRAPHIC:
			Transformation.orthographicMatrix(projectionMatrix, this);
			break;

		case PERSPECTIVE:
			Transformation.perspectiveMatrix(projectionMatrix, this);
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
	
	public void setRenderTargetFrustum(int left, int right, int top, int bottom) {
		this.renderTargetLeft = left;
		this.renderTargetRight = right;
		this.renderTargetTop = top;
		this.renderTargetBottom = bottom;
	}
	
	public void setRenderTargetSize(int width, int height) {
		this.renderTargetWidth = width;
		this.renderTargetHeight = height;
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

	public void setType(FrustumType type) {
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

	public int getRenderTargetLeft() {
		return renderTargetLeft;
	}

	public int getRenderTargetRight() {
		return renderTargetRight;
	}

	public int getRenderTargetTop() {
		return renderTargetTop;
	}

	public int getRenderTargetBottom() {
		return renderTargetBottom;
	}

	public int getRenderTargetWidth() {
		return renderTargetWidth;
	}

	public int getRenderTargetHeight() {
		return renderTargetHeight;
	}

	public int[][] getProjectionMatrix() {
		return projectionMatrix;
	}

	public FrustumType getType() {
		return type;
	}
}