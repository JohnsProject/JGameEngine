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

import com.johnsproject.jgameengine.math.VectorMath;

public class GeometryBuffer {

	private final int[] worldNormal;
	private final int[][] uvs;
	private final VertexBuffer[] vertexBuffers;
	
	public GeometryBuffer() {
		this.worldNormal = VectorMath.emptyVector();
		this.uvs = new int[3][VectorMath.VECTOR_SIZE];
		this.uvs[0] = VectorMath.emptyVector();
		this.uvs[1] = VectorMath.emptyVector();
		this.uvs[2] = VectorMath.emptyVector();
		this.vertexBuffers = new VertexBuffer[3];
	}

	public int[] getWorldNormal() {
		return worldNormal;
	}
	
	public int[] getUV(int index) {
		return uvs[index];
	}

	public int[][] getUVs() {
		return uvs;
	}

	public VertexBuffer getVertexBuffer(int index) {
		return vertexBuffers[index];
	}
	
	public VertexBuffer[] getVertexBuffers() {
		return vertexBuffers;
	}
}
