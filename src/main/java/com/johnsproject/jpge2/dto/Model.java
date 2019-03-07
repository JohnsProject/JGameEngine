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

import com.johnsproject.jpge2.processing.Shader;

public class Model extends SceneObject {
	
	private Vertex[] vertexes;
	private Face[] faces;
	private Material[] materials;
	private Shader shader;
	
	public Model (String name, Transform transform, Vertex[] vertexes, Face[] faces, Material[] materials) {
		super(name, transform);
		this.vertexes = vertexes;
		for (int i = 0; i < vertexes.length; i++) {
			if (vertexes[i] != null)
				vertexes[i].setModel(this);
		}
		this.faces = faces;
		for (int i = 0; i < faces.length; i++) {
			if (faces[i] != null)
				faces[i].setModel(this);
		}
		this.materials = materials;
		this.shader = new Shader();
	}
	
	public Vertex[] getVertexes(){
		return vertexes;
	}
	
	public Vertex getVertex(int index){
		return vertexes[index];
	}
	
	public Face[] getFaces() {
		return faces;
	}
	
	public Face getFace(int index) {
		return faces[index];
	}
	
	public Material[] getMaterials() {
		return materials;
	}
	
	public Material getMaterial(int index) {
		return materials[index];
	}

	public Shader getShader() {
		return shader;
	}

	public void setShader(Shader shader) {
		this.shader = shader;
	}
}
