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
package com.johnsproject.jgameengine.shader;

import com.johnsproject.jgameengine.model.Face;

public class GeometryBuffer {

	private final Face face;
	private final int[] normal;
	private final int[] worldNormal;
	private final int[][] uvs;
	private final VertexBuffer[] vertexDataBuffers;
	
	public GeometryBuffer(Face face) {
		this.face = face;
		this.normal = face.getNormal().clone();
		this.worldNormal = face.getNormal().clone();
		this.uvs = face.getUVs().clone();
		this.uvs[0] = face.getUV(0).clone();
		this.uvs[0] = face.getUV(1).clone();
		this.uvs[0] = face.getUV(2).clone();
		this.vertexDataBuffers = new VertexBuffer[3];
		this.vertexDataBuffers[0] = face.getVertex(0).getBuffer();
		this.vertexDataBuffers[1] = face.getVertex(1).getBuffer();
		this.vertexDataBuffers[2] = face.getVertex(2).getBuffer();
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

	public VertexBuffer getVertexDataBuffer(int index) {
		return vertexDataBuffers[index];
	}
	
	public VertexBuffer[] getVertexDataBuffers() {
		return vertexDataBuffers;
	}
	
	public int[] getWorldNormal() {
		return worldNormal;
	}
	
	public void reset() {
		for (int i = 0; i < normal.length; i++) {
			normal[i] = worldNormal[i];
			uvs[0][i] = face.getUV(0)[i];
			uvs[1][i] = face.getUV(1)[i];
			uvs[2][i] = face.getUV(2)[i];
		}
	}
	
	public void resetAll() {
		for (int i = 0; i < normal.length; i++) {
			worldNormal[i] = face.getNormal()[i];
			normal[i] = worldNormal[i];
			uvs[0][i] = face.getUV(0)[i];
			uvs[1][i] = face.getUV(1)[i];
			uvs[2][i] = face.getUV(2)[i];
		}
	}
}
