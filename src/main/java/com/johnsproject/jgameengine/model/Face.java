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

import com.johnsproject.jgameengine.math.VectorMath;
import com.johnsproject.jgameengine.shader.GeometryBuffer;

public class Face {
		
	private final int index;
	private final Vertex[] vertices;
	private final int[] normal;
	private final int[][] uvs;
	private final Material material;
	private GeometryBuffer buffer;

	public Face(int index, int[] normal, Vertex vertex1, Vertex vertex2, Vertex vertex3, Material material, int[] uv1, int[] uv2, int[] uv3) {
		this.index = index;
		this.vertices = new Vertex[3];
		this.vertices[0] = vertex1;
		this.vertices[1] = vertex2;
		this.vertices[2] = vertex3;
		this.normal = normal;
		this.uvs = new int[3][4];
		this.uvs[0] = uv1;
		this.uvs[1] = uv2;
		this.uvs[2] = uv3;
		this.material = material;
		this.buffer = new GeometryBuffer();
	}
	
	public Face(int index, int[] normal, Vertex vertex1, Vertex vertex2, Vertex vertex3, Material material) {
		this.index = index;
		this.vertices = new Vertex[3];
		this.vertices[0] = vertex1;
		this.vertices[1] = vertex2;
		this.vertices[2] = vertex3;
		this.normal = normal;
		this.uvs = new int[3][4];
		this.uvs[0] = VectorMath.toVector();
		this.uvs[1] = VectorMath.toVector();
		this.uvs[2] = VectorMath.toVector();
		this.material = material;
		this.buffer = new GeometryBuffer();
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

	public int[] getNormal() {
		return normal;
	}
	
	public int[] getUV(int index) {
		return uvs[index];
	}

	public int[][] getUVs() {
		return uvs;
	}

	public Material getMaterial() {
		return material;
	}

	public GeometryBuffer getBuffer() {
		return buffer;
	}

	public void setBuffer(GeometryBuffer buffer) {
		this.buffer = buffer;
	}
}
