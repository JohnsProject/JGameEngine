package com.johnsproject.jgameengine;

import com.johnsproject.jgameengine.event.EngineEvent;
import com.johnsproject.jgameengine.event.EngineListener;
import com.johnsproject.jgameengine.model.RigidBody;
import com.johnsproject.jgameengine.model.Scene;
import com.johnsproject.jgameengine.model.SceneObject;
import com.johnsproject.jgameengine.model.Transform;
import com.johnsproject.jgameengine.util.FixedPointUtils;
import com.johnsproject.jgameengine.util.VectorUtils;

public class PhysicsEngine implements EngineListener {
	
	public static final int FP_EARTH_GRAVITY = FixedPointUtils.toFixedPoint(-9.81f);
	
	private final int[] vectorCache1;
	
	public PhysicsEngine() {
		this.vectorCache1 = VectorUtils.emptyVector();
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
			int gravity = FixedPointUtils.multiply(rigidBody.getMass(), FP_EARTH_GRAVITY);
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
			int[] linearAcceleration = VectorUtils.copy(vectorCache1, rigidBody.getForce());
			VectorUtils.divide(linearAcceleration, rigidBody.getMass());
			VectorUtils.multiply(linearAcceleration, e.getDeltaTime());
			rigidBody.addLinearVelocity(linearAcceleration);
			int[] linearVelocity = VectorUtils.copy(vectorCache1, rigidBody.getLinearVelocity());
			VectorUtils.multiply(linearVelocity, e.getDeltaTime());
			transform.translateWorld(linearVelocity);
			int[] angularAcceleration = VectorUtils.copy(vectorCache1, rigidBody.getTorque());
			VectorUtils.divide(angularAcceleration, rigidBody.getMass());
			VectorUtils.multiply(angularAcceleration, e.getDeltaTime());
			rigidBody.addAngularVelocity(angularAcceleration);
			int[] angularVelocity = VectorUtils.copy(vectorCache1, rigidBody.getAngularVelocity());
			VectorUtils.multiply(angularVelocity, e.getDeltaTime());
			VectorUtils.multiply(angularVelocity, FixedPointUtils.FP_RAD_DEGREE);
			transform.rotateWorld(angularVelocity);
		}
	}

	public int getLayer() {
		return PHYSICS_ENGINE_LAYER;
	}

}
