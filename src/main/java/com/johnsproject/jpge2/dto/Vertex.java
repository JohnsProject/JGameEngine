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
	private long[] startLocation;
	private long[] location;
	private long[] startNormal;
	private long[] normal;
	private int materialIndex;
	private Material material;
	private Model model;
	
	public Vertex(int index, long[] location, long[] normal, int material) {
		this.index = index;
		this.startLocation = location.clone();
		this.location = location;
		this.startNormal = normal.clone();
		this.normal = normal;
		this.materialIndex = material;
	}

	public long getIndex() {
		return index;
	}

	public Model getModel() {
		return model;
	}

	public void setModel(Model model) {
		this.material =  model.getMaterial(materialIndex);
		this.model = model;
	}

	public long[] getStartLocation() {
		return startLocation;
	}

	public long[] getLocation() {
		return location;
	}
	
	
	public void setLocation(long[] location) {
		this.location = location;
	}
	

	public void setLocation(long x, long y, long z) {
		this.location[vx] = x;
		this.location[vy] = y;
		this.location[vz] = z;
	}

	public void reset() {
		VectorProcessor.copy(location, startLocation);
		VectorProcessor.copy(normal, startNormal);
	}
	
	public long[] getNormal() {
		return normal;
	}

	public Material getMaterial() {
		return material;
	}
}
