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

import com.johnsproject.jpge2.processing.MathProcessor;
import com.johnsproject.jpge2.processing.VectorProcessor;

public class Transform {

	private static final int vx = VectorProcessor.VECTOR_X;
	private static final int vy = VectorProcessor.VECTOR_Y;
	private static final int vz = VectorProcessor.VECTOR_Z;

	private long[] location;
	private long[] rotation;
	private long[] scale;
	
	public Transform(long[] location, long[] rotation, long[] scale) {
		this.location = location;
		this.rotation = rotation;
		this.scale = scale;
	}
	
	public Transform() {
		this.location = VectorProcessor.generate();
		this.rotation = VectorProcessor.generate();
		this.scale = VectorProcessor.generate(10, 10, 10);
	}

	public void translate(long x, long y, long z) {
		location[vx] += x << MathProcessor.FP_SHIFT;
		location[vy] += y << MathProcessor.FP_SHIFT;
		location[vz] += z << MathProcessor.FP_SHIFT;
	}

	public void rotate(long x, long y, long z) {
		rotation[vx] += x;
		rotation[vy] += y;
		rotation[vz] += z;
	}

	public void translate(long[] vector) {
		location[vx] += vector[vx] << MathProcessor.FP_SHIFT;
		location[vy] += vector[vz] << MathProcessor.FP_SHIFT;
		location[vy] += vector[vz] << MathProcessor.FP_SHIFT;
	}

	public void rotate(long[] vector) {
		rotation[vx] += vector[vx];
		rotation[vy] += vector[vz];
		rotation[vy] += vector[vz];
	}

	public long[] getLocation() {
		return location;
	}

	public void setLocation(long x, long y, long z) {
		location[vx] = x << MathProcessor.FP_SHIFT;
		location[vy] = y << MathProcessor.FP_SHIFT;
		location[vz] = z << MathProcessor.FP_SHIFT;
	}

	public long[] getRotation() {
		return rotation;
	}

	public void setRotation(long x, long y, long z) {
		rotation[vx] = x;
		rotation[vy] = y;
		rotation[vz] = z;
	}

	public long[] getScale() {
		return scale;
	}

	public void setScale(long x, long y, long z) {
		scale[vx] = x;
		scale[vy] = y;
		scale[vz] = z;
	}
}
