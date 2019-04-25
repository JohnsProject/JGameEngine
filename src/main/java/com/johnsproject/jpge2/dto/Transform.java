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

public class Transform {

	private int[] location;
	private int[] rotation;
	private int[] scale;
	
	public Transform(int[] location, int[] rotation, int[] scale) {
		this.location = location;
		this.rotation = rotation;
		this.scale = scale;
	}
	
	public void translate(int x, int y, int z) {
		location[0] += x;
		location[1] += y;
		location[2] += z;
	}

	public void rotate(int x, int y, int z) {
		rotation[0] += x;
		rotation[1] += y;
		rotation[2] += z;
	}
	
	public void translate(int[] vector) {
		location[0] += vector[0];
		location[1] += vector[1];
		location[2] += vector[2];
	}

	public void rotate(int[] angles) {
		rotation[0] += angles[0];
		rotation[1] += angles[1];
		rotation[2] += angles[2];
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
