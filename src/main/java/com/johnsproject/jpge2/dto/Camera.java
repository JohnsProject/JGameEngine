/**
 * MIT License
 *
 * Copyright (c) 2018 John Salomon - John´s Project
 *  
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.johnsproject.jpge2.dto;

import com.johnsproject.jpge2.processing.GraphicsProcessor;
import com.johnsproject.jpge2.processing.MatrixProcessor;
import com.johnsproject.jpge2.processing.VectorProcessor;

public class Camera extends SceneObject {
	
	private static final int vx = VectorProcessor.VECTOR_X;
	private static final int vy = VectorProcessor.VECTOR_Y;
	private static final int vz = VectorProcessor.VECTOR_Z;
	private static final int vw = VectorProcessor.VECTOR_W;
	
	private long[] canvas;
	private long[] viewFrustum;
	private long[][] viewMatrix = MatrixProcessor.generate();
	private long[][] perspectiveMatrix = MatrixProcessor.generate();
	private long[][] orthographicMatrix = MatrixProcessor.generate();

	public Camera(String name, Transform transform, long[] canvas) {
		super(name, transform);
		this.canvas = canvas;
		this.viewFrustum = VectorProcessor.generate(60, 10, 1024);
	}

	public long[] getCanvas() {
		return canvas;
	}

	public void setCanvas(long[] canvas) {
		this.canvas = canvas;
	}
	
	public void setCanvas(long x, long y, long width, long height) {
		this.canvas[vx] = x;
		this.canvas[vy] = y;
		this.canvas[vz] = width;
		this.canvas[vw] = height;
	}

	public long[] getViewFrustum() {
		return viewFrustum;
	}

	public void setViewFrustum(long[] clippingPlanes) {
		this.viewFrustum = clippingPlanes;
	}
	
	public void setViewFrustum(long fov, long near, long far) {
		this.viewFrustum[vx] = fov;
		this.viewFrustum[vy] = near;
		this.viewFrustum[vz] = far;
	}

	public long[][] getViewMatrix() {
		if (this.hasChanged()) {
			GraphicsProcessor.viewMatrix(viewMatrix, this);
		}
		return viewMatrix;
	}

	public long[][] getPerspectiveMatrix() {
		if (this.hasChanged()) {
			GraphicsProcessor.perspectiveMatrix(perspectiveMatrix, this);
		}
		return perspectiveMatrix;
	}

	public long[][] getOrthographicMatrix() {
		if (this.hasChanged()) {
			GraphicsProcessor.orthographicMatrix(orthographicMatrix, this);
		}
		return orthographicMatrix;
	}
}
