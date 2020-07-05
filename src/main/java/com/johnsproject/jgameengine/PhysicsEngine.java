package com.johnsproject.jgameengine;

import com.johnsproject.jgameengine.event.EngineEvent;
import com.johnsproject.jgameengine.event.EngineListener;
import com.johnsproject.jgameengine.math.FixedPointMath;
import com.johnsproject.jgameengine.math.VectorMath;
import com.johnsproject.jgameengine.model.RigidBody;
import com.johnsproject.jgameengine.model.Scene;
import com.johnsproject.jgameengine.model.SceneObject;
import com.johnsproject.jgameengine.model.Transform;

public class PhysicsEngine implements EngineListener {
	
	public static final int FP_EARTH_GRAVITY = FixedPointMath.toFixedPoint(-9.81f);
	
	private final int[] vectorCache1;
	
	public PhysicsEngine() {
		this.vectorCache1 = VectorMath.emptyVector();
	}
	
	public void start(EngineEvent e) {
		
	}
	
	public void fixedUpdate(EngineEvent e) {
//		final Scene scene = e.getScene();
//		for (int i = 0; i < scene.getSceneObjects().size(); i++) {
//			final SceneObject sceneObject = scene.getSceneObject(i);
//			if(!sceneObject.isActive()) 
//				return;
//			final RigidBody rigidBody = sceneObject.getRigidBody();
//			final Transform transform = sceneObject.getTransform();
//			applyForces(sceneObject);
//			int[] linearAcceleration = VectorLibrary.divide(rigidBody.getForce(), rigidBody.getMass(), vectorCache1);
////			VectorLibrary.multiply(linearAcceleration, e.getDeltaTime(), linearAcceleration);
//			int[] linearVelocity = VectorLibrary.add(rigidBody.getLinearVelocity(), linearAcceleration, rigidBody.getLinearVelocity());
//			VectorLibrary.add(transform.getLocation(), linearVelocity, transform.getLocation());
//			int[] angularAcceleration = VectorLibrary.divide(rigidBody.getTorque(), rigidBody.getMass(), vectorCache1);
////			VectorLibrary.multiply(angularAcceleration, e.getDeltaTime(), angularAcceleration);
//			VectorLibrary.multiply(angularAcceleration, MathLibrary.FP_RAD_DEGREE, angularAcceleration);
//			int[] angularVelocity = VectorLibrary.add(rigidBody.getAngularVelocity(), angularAcceleration, rigidBody.getAngularVelocity());
//			VectorLibrary.add(transform.getRotation(), angularVelocity, transform.getRotation());
//		}
	}
	
	private void applyForces(SceneObject sceneObject) {
		final RigidBody rigidBody = sceneObject.getRigidBody();
		if(!rigidBody.isKinematic()) {
			int gravity = FixedPointMath.multiply(rigidBody.getMass(), FP_EARTH_GRAVITY);
			rigidBody.setForce(0, 0, gravity);
		}
	}

	public void update(EngineEvent e) {
		final Scene scene = e.getScene();
		for (int i = 0; i < scene.getSceneObjects().size(); i++) {
			SceneObject sceneObject = scene.getSceneObjects().get(i);
			if(!sceneObject.isActive()) 
				return;
			final RigidBody rigidBody = sceneObject.getRigidBody();
			final Transform transform = sceneObject.getTransform();
			applyForces(sceneObject);
			int[] linearAcceleration = VectorMath.copy(vectorCache1, rigidBody.getForce());
			VectorMath.divide(linearAcceleration, rigidBody.getMass());
			VectorMath.multiply(linearAcceleration, e.getDeltaTime());
			rigidBody.addLinearVelocity(linearAcceleration);
			int[] linearVelocity = VectorMath.copy(vectorCache1, rigidBody.getLinearVelocity());
			VectorMath.multiply(linearVelocity, e.getDeltaTime());
			transform.translate(linearVelocity);
			int[] angularAcceleration = VectorMath.copy(vectorCache1, rigidBody.getTorque());
			VectorMath.divide(angularAcceleration, rigidBody.getMass());
			VectorMath.multiply(angularAcceleration, e.getDeltaTime());
			rigidBody.addAngularVelocity(angularAcceleration);
			int[] angularVelocity = VectorMath.copy(vectorCache1, rigidBody.getAngularVelocity());
			VectorMath.multiply(angularVelocity, e.getDeltaTime());
			VectorMath.multiply(angularVelocity, FixedPointMath.FP_RAD_DEGREE);
			transform.rotate(angularVelocity);
		}
	}

	public int getLayer() {
		return PHYSICS_ENGINE_LAYER;
	}

}
