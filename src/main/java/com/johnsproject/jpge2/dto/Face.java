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

public class Face {
		
	private int index;
	private int vertex1Index;
	private int vertex2Index;
	private int vertex3Index;
	private Vertex[] vertices;
	private int[] startNormal;
	private int[] normal;
	private int[] uv1;
	private int[] uv2;
	private int[] uv3;
	private int materialIndex;
	private Material material;
	private Model model;	

	public Face(int index, int vertex1, int vertex2, int vertex3, int material, int[] normal, int[] uv1, int[] uv2, int[] uv3) {
		this.index = index;
		this.vertex1Index = vertex1;
		this.vertex2Index = vertex2;
		this.vertex3Index = vertex3;
		this.vertices = new Vertex[3];
		this.startNormal = normal.clone();
		this.normal = normal;
		this.uv1 = uv1;
		this.uv2 = uv2;
		this.uv3 = uv3;
		this.materialIndex = material;
	}

	public int getIndex() {
		return index;
	}

	public Model getModel() {
		return model;
	}

	public void setModel(Model model) {
		this.vertices[0] = model.getVertex(vertex1Index);
		this.vertices[1] = model.getVertex(vertex2Index);
		this.vertices[2] = model.getVertex(vertex3Index);
		this.material = model.getMaterial(materialIndex);
		this.model = model;
	}

	public Vertex getVertex1() {
		return vertices[0];
	}

	public Vertex getVertex2() {
		return vertices[1];
	}

	public Vertex getVertex3() {
		return vertices[2];
	}
	
	public Vertex[] getVertices() {
		return vertices;
	}

	public int[] getNormal() {
		return normal;
	}
	
	public void reset() {
		VectorProcessor.copy(normal, startNormal);
	}
	
	public int[] getUV1() {
		return uv1;
	}

	public int[] getUV2() {
		return uv2;
	}

	public int[] getUV3() {
		return uv3;
	}

	public Material getMaterial() {
		return material;
	}
}
