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

import com.johnsproject.jpge2.processing.VectorProcessor;

public class Vertex {
	
	private static final int vx = VectorProcessor.VECTOR_X;
	private static final int vy = VectorProcessor.VECTOR_Y;
	private static final int vz = VectorProcessor.VECTOR_Z;
	
	private int index;
	private int[] startLocation;
	private int[] location;
	private int[] normal;
	private int material;
	private Model model;
	
	public Vertex(int index, int[] location, int[] normal, int material) {
		this.index = index;
		this.startLocation = location.clone();
		this.location = location;
		this.normal = normal;
		this.material = material;
	}

	public int getIndex() {
		return index;
	}

	public Model getModel() {
		return model;
	}

	public void setModel(Model model) {
		this.model = model;
	}

	public int[] getStartLocation() {
		return startLocation;
	}

	public int[] getLocation() {
		return location;
	}
	
	
	public void setLocation(int[] location) {
		this.location = location;
	}
	

	public void setLocation(int x, int y, int z) {
		this.location[vx] = x;
		this.location[vy] = y;
		this.location[vz] = z;
	}

	public void reset() {
		this.location[vx] = startLocation[vx];
		this.location[vy] = startLocation[vy];
		this.location[vz] = startLocation[vz];
	}
	
	public int[] getNormal() {
		return normal;
	}

	public Material getMaterial() {
		return model.getMaterial(material);
	}
}
