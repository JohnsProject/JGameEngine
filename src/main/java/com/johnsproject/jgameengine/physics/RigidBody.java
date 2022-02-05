package com.johnsproject.jgameengine.physics;

import static com.johnsproject.jgameengine.math.Vector.*;

import com.johnsproject.jgameengine.SceneObjectComponent;
import com.johnsproject.jgameengine.math.Fixed;
import com.johnsproject.jgameengine.math.Matrix;
import com.johnsproject.jgameengine.math.Transform;
import com.johnsproject.jgameengine.math.Vector;

public class RigidBody extends SceneObjectComponent {
	
	private boolean kinematic;
	private int mass;
	private final int[] force;
	private final int[] torque;
	private final int[] linearVelocity;
	private final int[] angularVelocity;
	private final int[][] momentOfInertia;
	private Transform transform;
	
	public RigidBody() {
		this.kinematic = false;
		this.mass = Fixed.FP_ONE;
		this.force = Vector.emptyVector();
		this.torque = Vector.emptyVector();
		this.linearVelocity = Vector.emptyVector();
		this.angularVelocity = Vector.emptyVector();
		this.momentOfInertia = Matrix.indentityMatrix();
	}
	
	public Transform getTransform() {
		if(transform == null)
			transform = owner.getComponentWithType(Transform.class);
		
		if(transform == null) {
			transform = new Transform();
			owner.addComponent(transform);
		}
		
		return transform;
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
