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

import com.johnsproject.jgameengine.library.VectorLibrary;

public class Transform {

	private static final byte VECTOR_X = VectorLibrary.VECTOR_X;
	private static final byte VECTOR_Y = VectorLibrary.VECTOR_Y;
	private static final byte VECTOR_Z = VectorLibrary.VECTOR_Z;
	
	private int[] location;
	private int[] rotation;
	private int[] scale;
	
	public Transform(int[] location, int[] rotation, int[] scale) {
		this.location = location;
		this.rotation = rotation;
		this.scale = scale;
	}
	
	public void translate(int x, int y, int z) {
		location[VECTOR_X] += x;
		location[VECTOR_Y] += y;
		location[VECTOR_Z] += z;
	}

	public void rotate(int x, int y, int z) {
		rotation[VECTOR_X] += x;
		rotation[VECTOR_Y] += y;
		rotation[VECTOR_Z] += z;
	}
	
	public void translate(int[] vector) {
		location[VECTOR_X] += vector[VECTOR_X];
		location[VECTOR_Y] += vector[VECTOR_Y];
		location[VECTOR_Z] += vector[VECTOR_Z];
	}

	public void rotate(int[] angles) {
		rotation[VECTOR_X] += angles[VECTOR_X];
		rotation[VECTOR_Y] += angles[VECTOR_Y];
		rotation[VECTOR_Z] += angles[VECTOR_Z];
	}

	public int[] getLocation() {
		return location;
	}

	public int[] getRotation() {
		return rotation;
	}

	public int[] getScale() {
		return scale;
	}
}