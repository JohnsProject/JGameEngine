package com.johnsproject.jgameengine.physics;

import com.johnsproject.jgameengine.Scene;
import com.johnsproject.jgameengine.SceneObject;
import com.johnsproject.jgameengine.event.EngineEvent;
import com.johnsproject.jgameengine.event.EngineListener;
import com.johnsproject.jgameengine.math.Fixed;
import com.johnsproject.jgameengine.math.Transform;
import com.johnsproject.jgameengine.math.Vector;

public final class PhysicsEngine implements EngineListener {
	
	public static final int FP_EARTH_GRAVITY = Fixed.toFixed(-9.81f);
	
	private final int[] vectorCache1;
	
	public PhysicsEngine() {
		this.vectorCache1 = Vector.emptyVector();
	}
	
	public void initialize(EngineEvent e) {}
	
	public void fixedUpdate(EngineEvent e) {}

	public void dynamicUpdate(EngineEvent e) {
		final Scene scene = e.getScene();
		for (int i = 0; i < scene.getSceneObjects().size(); i++) {
			final SceneObject sceneObject = scene.getSceneObjects().get(i);
			if(!sceneObject.isActive()) 
				return;

			final RigidBody rigidBody = sceneObject.getRigidBody();
			final Transform transform = sceneObject.getTransform();
			final int deltaTime = e.getDeltaTime() / 10;
			applyForces(rigidBody);
			applyLinearAcceleration(rigidBody, deltaTime);
			applyLinearVelocity(transform, rigidBody, deltaTime);
			applyAngularAcceleration(rigidBody, deltaTime);
			applyAngularVelocity(transform, rigidBody, deltaTime);
		}
	}
	
	private void applyForces(RigidBody rigidBody) {
		if(!rigidBody.isKinematic()) {
			final int gravity = Fixed.multiply(rigidBody.getMass(), FP_EARTH_GRAVITY);
			rigidBody.addForce(0, gravity, 0);
		}
	}
	
	private void applyLinearAcceleration(RigidBody rigidBody, int deltaTime) {
		final int[] linearAcceleration = Vector.copy(vectorCache1, rigidBody.getForce());
		Vector.divide(linearAcceleration, rigidBody.getMass());
		Vector.multiply(linearAcceleration, deltaTime);
		rigidBody.addLinearVelocity(linearAcceleration);
	}
	
	private void applyLinearVelocity(Transform transform, RigidBody rigidBody, int deltaTime) {
		final int[] linearVelocity = Vector.copy(vectorCache1, rigidBody.getLinearVelocity());
		Vector.multiply(linearVelocity, deltaTime);
		transform.worldTranslate(linearVelocity);
	}
	
	private void applyAngularAcceleration(RigidBody rigidBody, int deltaTime) {
		final int[] angularAcceleration = Vector.copy(vectorCache1, rigidBody.getTorque());
		Vector.multiply(angularAcceleration, Fixed.FP_DEGREE_RAD);
		Vector.divide(angularAcceleration, rigidBody.getMass());
		Vector.multiply(angularAcceleration, deltaTime);
		Vector.multiply(angularAcceleration, Fixed.FP_RAD_DEGREE);
		rigidBody.addAngularVelocity(angularAcceleration);
	}
	
	private void applyAngularVelocity(Transform transform, RigidBody rigidBody, int deltaTime) {
		final int[] angularVelocity = Vector.copy(vectorCache1, rigidBody.getAngularVelocity());
		Vector.multiply(angularVelocity, Fixed.FP_DEGREE_RAD);
		Vector.multiply(angularVelocity, deltaTime);
		Vector.multiply(angularVelocity, Fixed.FP_RAD_DEGREE);
		transform.worldRotate(angularVelocity);
	}
}
