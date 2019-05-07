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

import com.johnsproject.jpge2.primitive.Vector;

public class Transform {

	private Vector location;
	private Vector rotation;
	private Vector scale;
	
	public Transform() {
		this.location = new Vector();
		this.rotation = new Vector();
		this.scale = new Vector();
	}
	
	public Transform(Vector location, Vector rotation, Vector scale) {
		this.location = location;
		this.rotation = rotation;
		this.scale = scale;
	}
	
	public void translate(int x, int y, int z) {
		int[] locationValues = location.getValues();
		locationValues[0] += x;
		locationValues[1] += y;
		locationValues[2] += z;
	}

	public void rotate(int x, int y, int z) {
		int[] rotationValues = rotation.getValues();
		rotationValues[0] += x;
		rotationValues[1] += y;
		rotationValues[2] += z;
	}
	
	public void translate(Vector vector) {
		location.add(vector);
	}

	public void rotate(Vector angles) {
		rotation.add(angles);
	}

	public Vector getLocation() {
		return location;
	}

	public Vector getRotation() {
		return rotation;
	}

	public Vector getScale() {
		return scale;
	}
}
