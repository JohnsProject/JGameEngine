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
package com.johnsproject.jgameengine;

import com.johnsproject.jgameengine.event.EngineEvent;
import com.johnsproject.jgameengine.event.EngineListener;
import com.johnsproject.jgameengine.library.MathLibrary;
import com.johnsproject.jgameengine.library.PhysicsLibrary;
import com.johnsproject.jgameengine.library.VectorLibrary;
import com.johnsproject.jgameengine.model.RigidBody;
import com.johnsproject.jgameengine.model.Scene;
import com.johnsproject.jgameengine.model.SceneObject;
import com.johnsproject.jgameengine.model.Transform;

public class PhysicsEngine implements EngineListener {
	
//	private static final byte VECTOR_X = VectorLibrary.VECTOR_X;
//	private static final byte VECTOR_Y = VectorLibrary.VECTOR_Y;
	private static final byte VECTOR_Z = VectorLibrary.VECTOR_Z;
//	private static final byte VECTOR_W = VectorLibrary.VECTOR_W;
	
	private final MathLibrary mathLibrary;
	private final VectorLibrary vectorLibrary;
	
	private final int[] vectorCache1;
	
	public PhysicsEngine() {
		this.mathLibrary = new MathLibrary();
		this.vectorLibrary = new VectorLibrary();
		this.vectorCache1 = VectorLibrary.generate();
	}
	
	public void start(EngineEvent e) {
		
	}
	
	public void fixedUpdate(EngineEvent e) {
		final Scene scene = e.getScene();
		for (int i = 0; i < scene.getSceneObjects().size(); i++) {
			final SceneObject sceneObject = scene.getSceneObject(i);
			final RigidBody rigidBody = sceneObject.getRigidBody();
			final Transform transform = sceneObject.getTransform();
			rigidBody.getForce()[VECTOR_Z] = mathLibrary.multiply(rigidBody.getMass(), -PhysicsLibrary.FP_EARTH_GRAVITY);
			int[] linearAcceleration = vectorLibrary.divide(rigidBody.getForce(), rigidBody.getMass(), vectorCache1);
			int[] linearVelocity = vectorLibrary.add(rigidBody.getLinearVelocity(), linearAcceleration, rigidBody.getLinearVelocity());
			vectorLibrary.add(transform.getLocation(), linearVelocity, transform.getLocation());
			int[] angularAcceleration = vectorLibrary.divide(rigidBody.getTorque(), rigidBody.getMass(), vectorCache1);
			int[] angularVelocity = vectorLibrary.add(rigidBody.getAngularVelocity(), angularAcceleration, rigidBody.getAngularVelocity());
			int[] angularVelocityDegrees = vectorLibrary.multiply(angularVelocity, MathLibrary.FP_PI180, vectorCache1);
			vectorLibrary.add(transform.getRotation(), angularVelocityDegrees, transform.getRotation());
		}
	}

	public void update(EngineEvent e) {
		
	}

	public int getLayer() {
		return PHYSICS_ENGINE_LAYER;
	}

}
