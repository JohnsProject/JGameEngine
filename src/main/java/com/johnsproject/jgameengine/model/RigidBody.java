package com.johnsproject.jgameengine.model;

import static com.johnsproject.jgameengine.util.VectorUtils.*;

import com.johnsproject.jgameengine.util.FixedPointUtils;
import com.johnsproject.jgameengine.util.MatrixUtils;
import com.johnsproject.jgameengine.util.VectorUtils;

public class RigidBody {
	
	private boolean kinematic;
	private int mass;
	private final int[] force;
	private final int[] torque;
	private final int[] linearVelocity;
	private final int[] angularVelocity;
	private final int[][] momentOfInertia;
	
	public RigidBody() {
		this.kinematic = false;
		this.mass = FixedPointUtils.FP_ONE;
		this.force = VectorUtils.emptyVector();
		this.torque = VectorUtils.emptyVector();
		this.linearVelocity = VectorUtils.emptyVector();
		this.angularVelocity = VectorUtils.emptyVector();
		this.momentOfInertia = MatrixUtils.indentityMatrix();
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

	public int[][] getMomentOfInertia() {
		return momentOfInertia;
	}
}
