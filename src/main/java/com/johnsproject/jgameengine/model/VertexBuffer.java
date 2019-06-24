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
package com.johnsproject.jgameengine.model;

public class VertexBuffer {

	private final Vertex vertex;
	private final int[] location;
	private final int[] normal;
	private final Material material;
	private int lightColor;
	private final int[] worldLocation;
	
	public VertexBuffer(Vertex vertex) {
		this.vertex = vertex;
		this.location = vertex.getLocation().clone();
		this.normal = vertex.getNormal().clone();
		this.material = vertex.getMaterial();
		this.lightColor = 0;
		this.worldLocation = vertex.getLocation().clone();
	}
	
	public int[] getLocation() {
		return location;
	}
	
	public int[] getNormal() {
		return normal;
	}
	
	public Material getMaterial() {
		return material;
	}

	public int getLightColor() {
		return lightColor;
	}

	public void setLightColor(int lightColor) {
		this.lightColor = lightColor;
	}

	public int[] getWorldLocation() {
		return worldLocation;
	}

	public void reset() {
		for (int i = 0; i < location.length; i++) {
			location[i] = vertex.getLocation()[i];
			normal[i] = vertex.getNormal()[i];
			worldLocation[i] = 0;
		}
		lightColor = 0;
	}
	
}
