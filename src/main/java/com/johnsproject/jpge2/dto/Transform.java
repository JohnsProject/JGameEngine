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

import com.johnsproject.jpge2.processors.MathProcessor;
import com.johnsproject.jpge2.processors.VectorProcessor;

public class Transform {

	private static final byte vx = VectorProcessor.VECTOR_X;
	private static final byte vy = VectorProcessor.VECTOR_Y;
	private static final byte vz = VectorProcessor.VECTOR_Z;

	private int[] location;
	private int[] rotation;
	private int[] scale;
	
	public Transform(int[] location, int[] rotation, int[] scale) {
		this.location = location;
		this.rotation = rotation;
		this.scale = scale;
	}
	
	public Transform() {
		this.location = VectorProcessor.generate();
		this.rotation = VectorProcessor.generate();
		this.scale = VectorProcessor.generate(10, 10, 10);
	}

	public void translate(int x, int y, int z) {
		location[vx] += x << MathProcessor.FP_SHIFT;
		location[vy] += y << MathProcessor.FP_SHIFT;
		location[vz] += z << MathProcessor.FP_SHIFT;
	}

	public void rotate(int x, int y, int z) {
		rotation[vx] += x;
		rotation[vy] += y;
		rotation[vz] += z;
	}

	public void translate(int[] vector) {
		location[vx] += vector[vx] << MathProcessor.FP_SHIFT;
		location[vy] += vector[vz] << MathProcessor.FP_SHIFT;
		location[vy] += vector[vz] << MathProcessor.FP_SHIFT;
	}

	public void rotate(int[] vector) {
		rotation[vx] += vector[vx];
		rotation[vy] += vector[vz];
		rotation[vy] += vector[vz];
	}

	public int[] getLocation() {
		return location;
	}

	public void setLocation(int x, int y, int z) {
		location[vx] = x << MathProcessor.FP_SHIFT;
		location[vy] = y << MathProcessor.FP_SHIFT;
		location[vz] = z << MathProcessor.FP_SHIFT;
	}

	public int[] getRotation() {
		return rotation;
	}

	public void setRotation(int x, int y, int z) {
		rotation[vx] = x;
		rotation[vy] = y;
		rotation[vz] = z;
	}

	public int[] getScale() {
		return scale;
	}

	public void setScale(int x, int y, int z) {
		scale[vx] = x;
		scale[vy] = y;
		scale[vz] = z;
	}
}
