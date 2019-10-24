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

import com.johnsproject.jgameengine.library.MathLibrary;
import com.johnsproject.jgameengine.library.MatrixLibrary;
import com.johnsproject.jgameengine.library.VectorLibrary;

import static com.johnsproject.jgameengine.library.VectorLibrary.*;

public class RigidBody {
	
	private boolean kinematic;
	private int mass;
	private final int[] force;
	private final int[] torque;
	private final int[] linearVelocity;
	private final int[] angularVelocity;
	private final int[] momentOfInertia;
	
	public RigidBody() {
		this.kinematic = false;
		this.mass = MathLibrary.FP_ONE;
		this.force = VectorLibrary.generate();
		this.torque = VectorLibrary.generate();
		this.linearVelocity = VectorLibrary.generate();
		this.angularVelocity = VectorLibrary.generate();
		this.momentOfInertia = MatrixLibrary.generate();
	}

	public boolean isKinematic() {
		return kinematic;
	}

	public void setKinematic(boolean kinematic) {
		this.kinematic = kinematic;
	}

	public int getMass() {
		return mass;
	}

	public void setMass(int mass) {
		this.mass = mass;
	}

	public int[] getForce() {
		return force;
	}

	public int[] getTorque() {
		return torque;
	}
	
	public int[] getLinearVelocity() {
		return linearVelocity;
	}
	
	public int[] getAngularVelocity() {
		return angularVelocity;
	}

	public int[] getMomentOfInertia() {
		return momentOfInertia;
	}

	public void setForce(int x, int y, int z) {
		force[VECTOR_X] = x;
		force[VECTOR_Y] = y;
		force[VECTOR_Z] = z;
	}

	public void addForce(int x, int y, int z) {
		force[VECTOR_X] += x;
		force[VECTOR_Y] += y;
		force[VECTOR_Z] += z;
	}
	
	public void addForce(int[] vector) {
		force[VECTOR_X] += vector[VECTOR_X];
		force[VECTOR_Y] += vector[VECTOR_Y];
		force[VECTOR_Z] += vector[VECTOR_Z];
	}

	public void setTorque(int x, int y, int z) {
		torque[VECTOR_X] = x;
		torque[VECTOR_Y] = y;
		torque[VECTOR_Z] = z;
	}

	public void addTorque(int x, int y, int z) {
		torque[VECTOR_X] += x;
		torque[VECTOR_Y] += y;
		torque[VECTOR_Z] += z;
	}
	
	public void addTorque(int[] vector) {
		torque[VECTOR_X] += vector[VECTOR_X];
		torque[VECTOR_Y] += vector[VECTOR_Y];
		torque[VECTOR_Z] += vector[VECTOR_Z];
	}
	
	public void setLinearVelocity(int x, int y, int z) {
		linearVelocity[VECTOR_X] = x;
		linearVelocity[VECTOR_Y] = y;
		linearVelocity[VECTOR_Z] = z;
	}

	public void addLinearVelocity(int x, int y, int z) {
		linearVelocity[VECTOR_X] += x;
		linearVelocity[VECTOR_Y] += y;
		linearVelocity[VECTOR_Z] += z;
	}
	
	public void addLinearVelocity(int[] vector) {
		linearVelocity[VECTOR_X] += vector[VECTOR_X];
		linearVelocity[VECTOR_Y] += vector[VECTOR_Y];
		linearVelocity[VECTOR_Z] += vector[VECTOR_Z];
	}
	
	// needs to be converted to radians
	public void setAngularVelocity(int x, int y, int z) {
		angularVelocity[VECTOR_X] = x;
		angularVelocity[VECTOR_Y] = y;
		angularVelocity[VECTOR_Z] = z;
	}

	public void addAngularVelocity(int x, int y, int z) {
		angularVelocity[VECTOR_X] += x;
		angularVelocity[VECTOR_Y] += y;
		angularVelocity[VECTOR_Z] += z;
	}
	
	public void addAngularVelocity(int[] vector) {
		angularVelocity[VECTOR_X] += vector[VECTOR_X];
		angularVelocity[VECTOR_Y] += vector[VECTOR_Y];
		angularVelocity[VECTOR_Z] += vector[VECTOR_Z];
	}
}
