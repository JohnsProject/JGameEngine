package com.johnsproject.jgameengine;

import com.johnsproject.jgameengine.event.EngineEvent;
import com.johnsproject.jgameengine.event.EngineListener;
import com.johnsproject.jgameengine.model.RigidBody;
import com.johnsproject.jgameengine.model.Scene;
import com.johnsproject.jgameengine.model.SceneObject;
import com.johnsproject.jgameengine.model.Transform;
import com.johnsproject.jgameengine.util.FixedPointUtils;
import com.johnsproject.jgameengine.util.VectorUtils;

public final class PhysicsEngine implements EngineListener {
	
	public static final int FP_EARTH_GRAVITY = FixedPointUtils.toFixedPoint(-9.81f);
	
	private final int[] vectorCache1;
	
	public PhysicsEngine() {
		this.vectorCache1 = VectorUtils.emptyVector();
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
			final int gravity = FixedPointUtils.multiply(rigidBody.getMass(), FP_EARTH_GRAVITY);
			rigidBody.addForce(0, gravity, 0);
		}
	}
	
	private void applyLinearAcceleration(RigidBody rigidBody, int deltaTime) {
		final int[] linearAcceleration = VectorUtils.copy(vectorCache1, rigidBody.getForce());
		VectorUtils.divide(linearAcceleration, rigidBody.getMass());
		VectorUtils.multiply(linearAcceleration, deltaTime);
		rigidBody.addLinearVelocity(linearAcceleration);
	}
	
	private void applyLinearVelocity(Transform transform, RigidBody rigidBody, int deltaTime) {
		final int[] linearVelocity = VectorUtils.copy(vectorCache1, rigidBody.getLinearVelocity());
		VectorUtils.multiply(linearVelocity, deltaTime);
		transform.worldTranslate(linearVelocity);
	}
	
	private void applyAngularAcceleration(RigidBody rigidBody, int deltaTime) {
		final int[] angularAcceleration = VectorUtils.copy(vectorCache1, rigidBody.getTorque());
		VectorUtils.multiply(angularAcceleration, FixedPointUtils.FP_DEGREE_RAD);
		VectorUtils.divide(angularAcceleration, rigidBody.getMass());
		VectorUtils.multiply(angularAcceleration, deltaTime);
		VectorUtils.multiply(angularAcceleration, FixedPointUtils.FP_RAD_DEGREE);
		rigidBody.addAngularVelocity(angularAcceleration);
	}
	
	private void applyAngularVelocity(Transform transform, RigidBody rigidBody, int deltaTime) {
		final int[] angularVelocity = VectorUtils.copy(vectorCache1, rigidBody.getAngularVelocity());
		VectorUtils.multiply(angularVelocity, FixedPointUtils.FP_DEGREE_RAD);
		VectorUtils.multiply(angularVelocity, deltaTime);
		VectorUtils.multiply(angularVelocity, FixedPointUtils.FP_RAD_DEGREE);
		transform.worldRotate(angularVelocity);
	}

	public int getLayer() {
		return PHYSICS_ENGINE_LAYER;
	}
}
