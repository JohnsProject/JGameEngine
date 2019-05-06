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

import com.johnsproject.jpge2.primitive.FPVector;

public class Face {
		
	private int index;
	private Vertex[] vertices;
	private FPVector normal;
	private FPVector uv1;
	private FPVector uv2;
	private FPVector uv3;
	private Material material;

	public Face(int index, Vertex vertex1, Vertex vertex2, Vertex vertex3, Material material, FPVector normal, FPVector uv1, FPVector uv2, FPVector uv3) {
		this.index = index;
		this.vertices = new Vertex[3];
		this.vertices[0] = vertex1;
		this.vertices[1] = vertex2;
		this.vertices[2] = vertex3;
		this.normal = normal;
		this.uv1 = uv1;
		this.uv2 = uv2;
		this.uv3 = uv3;
		this.material = material;
	}

	public int getIndex() {
		return index;
	}

	public Vertex getVertex(int index) {
		return vertices[index];
	}
	
	public Vertex[] getVertices() {
		return vertices;
	}

	public FPVector getNormal() {
		return normal;
	}
	
	public FPVector getUV1() {
		return uv1;
	}

	public FPVector getUV2() {
		return uv2;
	}

	public FPVector getUV3() {
		return uv3;
	}

	public Material getMaterial() {
		return material;
	}
}
