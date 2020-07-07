package com.johnsproject.jgameengine.model;

import static com.johnsproject.jgameengine.util.FixedPointUtils.FP_ONE;

import com.johnsproject.jgameengine.util.FixedPointUtils;
import com.johnsproject.jgameengine.util.MatrixUtils;
import com.johnsproject.jgameengine.util.TransformationUtils;

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
		this.projectionMatrix = MatrixUtils.indentityMatrix();
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
		renderTargetLeft = FixedPointUtils.multiply(renderTargetWidth, left);
		renderTargetRight = FixedPointUtils.multiply(renderTargetWidth, right);
		renderTargetTop = FixedPointUtils.multiply(renderTargetHeight, top);
		renderTargetBottom = FixedPointUtils.multiply(renderTargetHeight, bottom);
		recalculateProjectionMatrix();
	}
	
	private void recalculateProjectionMatrix() {
		switch (type) {
		case ORTHOGRAPHIC:
			TransformationUtils.orthographicMatrix(projectionMatrix, this);
			break;

		case PERSPECTIVE:
			TransformationUtils.perspectiveMatrix(projectionMatrix, this);
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
