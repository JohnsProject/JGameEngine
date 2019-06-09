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
package com.johnsproject.jgameengine.dto;

import com.johnsproject.jgameengine.library.MathLibrary;

public class Camera extends SceneObject {
	
	public static final byte FRUSTUM_LEFT = 0;
	public static final byte FRUSTUM_RIGHT = 1;
	public static final byte FRUSTUM_TOP = 2;
	public static final byte FRUSTUM_BOTTOM = 3;
	public static final byte FRUSTUM_NEAR = 4;
	public static final byte FRUSTUM_FAR = 5;
	public static final byte FRUSTUM_SIZE = 6;
	
	private CameraType type;
	private final int[] frustum;

	public Camera(String name, Transform transform) {
		super(name, transform);
		this.frustum = new int[6];
		frustum[FRUSTUM_LEFT] = 0;
		frustum[FRUSTUM_RIGHT] = MathLibrary.FP_ONE;
		frustum[FRUSTUM_TOP] = 0;
		frustum[FRUSTUM_BOTTOM] = MathLibrary.FP_ONE;
		frustum[FRUSTUM_NEAR] = MathLibrary.FP_ONE;
		frustum[FRUSTUM_FAR] = MathLibrary.FP_ONE * 1000;
	}

	public int[] getFrustum() {
		return frustum;
	}

	public CameraType getType() {
		return type;
	}

	public void setType(CameraType type) {
		this.type = type;
	}
}
