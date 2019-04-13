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

import com.johnsproject.jpge2.processors.VectorProcessor;

public class Camera extends SceneObject {

	private static final int vx = VectorProcessor.VECTOR_X;
	private static final int vy = VectorProcessor.VECTOR_Y;

	public enum CameraType {
		ORTHOGRAPHIC,
		PERSPECTIVE
	}
	
	private CameraType type;
	private int[] canvas;
	private int[] frustum;

	public Camera(String name, Transform transform, int[] canvas) {
		super(name, transform);
		this.canvas = canvas;
		this.frustum = VectorProcessor.generate(60, 100, 10000);
	}

	public int[] getCanvas() {
		return canvas;
	}

	public void setCanvas(int[] canvas) {
		this.canvas = canvas;
	}

	public void setCanvas(int x, int y, int width, int height) {
		this.canvas[vx] = x;
		this.canvas[vy] = y;
		this.canvas[2] = width;
		this.canvas[3] = height;
	}

	public int[] getFrustum() {
		return frustum;
	}

	public void setFrustum(int[] frustum) {
		this.frustum = frustum;
	}

	public void setFrustum(int fov, int near, int far) {
		this.frustum[0] = fov;
		this.frustum[1] = near;
		this.frustum[2] = far;
	}

	public CameraType getType() {
		return type;
	}

	public void setType(CameraType type) {
		this.type = type;
	}
}
