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

import com.johnsproject.jgameengine.library.ColorLibrary;
import com.johnsproject.jgameengine.library.GraphicsLibrary;
import com.johnsproject.jgameengine.library.MathLibrary;
import com.johnsproject.jgameengine.library.MatrixLibrary;
import com.johnsproject.jgameengine.library.VectorLibrary;
import com.johnsproject.jgameengine.model.GeometryBuffer;
import com.johnsproject.jgameengine.model.VertexBuffer;

public abstract class Shader {
	
	protected static final byte VECTOR_X = VectorLibrary.VECTOR_X;
	protected static final byte VECTOR_Y = VectorLibrary.VECTOR_Y;
	protected static final byte VECTOR_Z = VectorLibrary.VECTOR_Z;
	
	protected static final byte FP_BITS = MathLibrary.FP_BITS;
	protected static final int FP_ONE = MathLibrary.FP_ONE;
	protected static final int FP_HALF = MathLibrary.FP_HALF;
	
	protected final GraphicsLibrary graphicsLibrary;
	protected final MathLibrary mathLibrary;
	protected final MatrixLibrary matrixLibrary;
	protected final VectorLibrary vectorLibrary;
	protected final ColorLibrary colorLibrary;
	
	public Shader() {
		this.graphicsLibrary = new GraphicsLibrary();
		this.mathLibrary = new MathLibrary();
		this.matrixLibrary = new MatrixLibrary();
		this.vectorLibrary = new VectorLibrary();
		this.colorLibrary = new ColorLibrary();
	}
	
	public abstract void vertex(VertexBuffer vertexBuffer);
	
	public abstract void geometry(GeometryBuffer geometryBuffer);
	
	public abstract void fragment(int[] location);

	public abstract ShaderBuffer getShaderBuffer();

	public abstract void setShaderBuffer(ShaderBuffer shaderBuffer);

	public abstract void setProperties(ShaderProperties shaderProperties);
	
	public abstract ShaderProperties getProperties();
}
