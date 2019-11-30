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

import static com.johnsproject.jgameengine.math.VectorMath.*;

import com.johnsproject.jgameengine.math.MatrixMath;
import com.johnsproject.jgameengine.math.TransformationMath;
import com.johnsproject.jgameengine.math.VectorMath;

public class Transform {
	
	private final int[] location;
	private final int[] rotation;
	private final int[] scale;
	
	private final int[] matrixCache1;
	private final int[] matrixCache2;
	private final int[] spaceEnterMatrix;
	private final int[] spaceEnterNormalMatrix;
	private final int[] spaceExitMatrix;
	private final int[] spaceExitNormalMatrix;
	
	public Transform() {
		this(VectorMath.emptyVector(), VectorMath.emptyVector(), VectorMath.VECTOR_ONE.clone());
	}
	
	public Transform(int[] location, int[] rotation, int[] scale) {
		this.location = location;
		this.rotation = rotation;
		this.scale = scale;
		this.matrixCache1 = MatrixMath.indentityMatrix();
		this.matrixCache2 = MatrixMath.indentityMatrix();
		this.spaceEnterMatrix = MatrixMath.indentityMatrix();
		this.spaceEnterNormalMatrix = MatrixMath.indentityMatrix();
		this.spaceExitMatrix = MatrixMath.indentityMatrix();
		this.spaceExitNormalMatrix = MatrixMath.indentityMatrix();
		recalculateMatrices();
	}
	
	private void recalculateMatrices() {
		TransformationMath.spaceExitMatrix(matrixCache1, this, matrixCache2, spaceExitMatrix);
		TransformationMath.spaceExitNormalMatrix(matrixCache1, this, matrixCache2, spaceExitNormalMatrix);
		TransformationMath.spaceEnterMatrix(matrixCache1, this, matrixCache2, spaceEnterMatrix);
		TransformationMath.spaceEnterNormalMatrix(matrixCache1, this, matrixCache2, spaceEnterNormalMatrix);
	}
	
	public void setLocation(int x, int y, int z) {
		location[VECTOR_X] = x;
		location[VECTOR_Y] = y;
		location[VECTOR_Z] = z;
		recalculateMatrices();
	}

	public void setRotation(int x, int y, int z) {
		rotation[VECTOR_X] = x;
		rotation[VECTOR_Y] = y;
		rotation[VECTOR_Z] = z;
		recalculateMatrices();
	}
	
	public void setScale(int x, int y, int z) {
		scale[VECTOR_X] = x;
		scale[VECTOR_Y] = y;
		scale[VECTOR_Z] = z;
		recalculateMatrices();
	}
	
	public void translate(int x, int y, int z) {
		setLocation(location[VECTOR_X] + x, location[VECTOR_Y] + y, location[VECTOR_Z] + z);
	}

	public void rotate(int x, int y, int z) {
		setRotation(rotation[VECTOR_X] + x, rotation[VECTOR_Y] + y, rotation[VECTOR_Z] + z);
	}
	
	public void scale(int x, int y, int z) {
		setScale(scale[VECTOR_X] + x, scale[VECTOR_Y] + y, scale[VECTOR_Z] + z);
	}
	
	public void translate(int[] vector) {
		translate(vector[VECTOR_X], vector[VECTOR_Y], vector[VECTOR_Z]);
	}

	public void rotate(int[] angles) {
		rotate(angles[VECTOR_X], angles[VECTOR_Y], angles[VECTOR_Z]);
	}
	
	public void scale(int[] vector) {
		scale(vector[VECTOR_X], vector[VECTOR_Y], vector[VECTOR_Z]);
	}

	public int[] getLocation() {
		return location;
	}

	public int[] getRotation() {
		return rotation;
	}

	public int[] getScale() {
		return scale;
	}

	public int[] getSpaceEnterMatrix() {
		return spaceEnterMatrix;
	}

	public int[] getSpaceEnterNormalMatrix() {
		return spaceEnterNormalMatrix;
	}

	public int[] getSpaceExitMatrix() {
		return spaceExitMatrix;
	}

	public int[] getSpaceExitNormalMatrix() {
		return spaceExitNormalMatrix;
	}
}
