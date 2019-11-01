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

import java.util.Hashtable;

public class Scene {
	
	private Camera mainCamera;
	private final Hashtable<String, SceneObject> sceneObjects;
	private final Hashtable<String, Model> models;
	private final Hashtable<String, Camera> cameras;
	private final Hashtable<String, Light> lights;
	
	public Scene() {
		this.sceneObjects = new Hashtable<String, SceneObject>();
		this.models = new Hashtable<String, Model>();
		this.cameras = new Hashtable<String, Camera>();
		this.lights = new Hashtable<String, Light>();
	}
	
	public Hashtable<String, SceneObject> getSceneObjects() {
		return sceneObjects;
	}
	
	public SceneObject getSceneObject(String name) {
		return sceneObjects.get(name);
	}

	public void addModel(Model model){
		sceneObjects.put(model.getName(), model);
		models.put(model.getName(), model);
	}
	
	public void removeModel(String name){
		sceneObjects.remove(name);
		models.remove(name);
	}
	
	public Hashtable<String, Model> getModels() {
		return models;
	}
	
	public Model getModel(String name) {
		return models.get(name);
	}
	
	public void addLight(Light light){
		sceneObjects.put(light.getName(), light);
		lights.put(light.getName(), light);
	}
	
	public void removeLight(String name){
		sceneObjects.remove(name);
		lights.remove(name);
	}
	
	public Hashtable<String, Light> getLights() {
		return lights;
	}
	
	public Light getLight(String name) {
		return lights.get(name);
	}
	
	public void addCamera(Camera camera){
		if(mainCamera == null) {
			setMainCamera(camera);
		}
		sceneObjects.put(camera.getName(), camera);
		cameras.put(camera.getName(), camera);
	}
	
	public void removeCamera(String name){
		sceneObjects.remove(name);
		cameras.remove(name);
	}

	public Hashtable<String, Camera> getCameras() {
		return cameras;
	}
	
	public Camera getCamera(String name) {
		return cameras.get(name);
	}

	public Camera getMainCamera() {
		return mainCamera;
	}

	public void setMainCamera(Camera mainCamera) {
		if(this.mainCamera != null) {
			this.mainCamera.setTag(Camera.CAMERA_TAG);
		}
		mainCamera.setTag(Camera.MAIN_CAMERA_TAG);
		this.mainCamera = mainCamera;
	}
}
