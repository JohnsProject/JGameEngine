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

public class VertexGroup {

	private final int boneIndex;
	private final Vertex[] vertices;
	private final int[] weights;
	
	public VertexGroup(int boneIndex, Vertex[] vertices, int[] weights) {
		this.boneIndex = boneIndex;
		this.vertices = vertices;
		this.weights = weights;
	}
	
	public int getBoneIndex() {
		return boneIndex;
	}
	
	public Vertex getVertex(int index) {
		return vertices[index];
	}
	
	public Vertex[] getVertices() {
		return vertices;
	}
	
	public int getWeight(Vertex vertex) {
		for (int i = 0; i < vertices.length; i++) {
			if(vertices[i].getIndex() == vertex.getIndex()) {
				return getWeight(i);
			}
		}
		return -1;
	}
	
	public int getWeight(int index) {
		return weights[index];
	}
	
	public int[] getWeights() {
		return weights;
	}
}
