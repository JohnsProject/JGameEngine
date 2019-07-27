package com.johnsproject.jgameengine.model;

import com.johnsproject.jgameengine.library.MathLibrary;
import com.johnsproject.jgameengine.library.VectorLibrary;

public class RigidBody {

	private int mass;
	private final int[] force;
	private final int[] velocity;
	
	public RigidBody() {
		this.mass = MathLibrary.FP_ONE;
		this.force = VectorLibrary.generate();
		this.velocity = VectorLibrary.generate();
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

	public int[] getVelocity() {
		return velocity;
	}
}
