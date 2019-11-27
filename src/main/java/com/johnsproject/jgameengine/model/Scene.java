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

import java.util.HashMap;

public class Scene {
	
	private Camera mainCamera;
	private Light mainLight;
	private final HashMap<String, SceneObject> sceneObjects;
	private final HashMap<String, Model> models;
	private final HashMap<String, Camera> cameras;
	private final HashMap<String, Light> lights;
	
	public Scene() {
		this.sceneObjects = new HashMap<String, SceneObject>();
		this.models = new HashMap<String, Model>();
		this.cameras = new HashMap<String, Camera>();
		this.lights = new HashMap<String, Light>();
	}
	
	public HashMap<String, SceneObject> getSceneObjects() {
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
	
	public HashMap<String, Model> getModels() {
		return models;
	}
	
	public Model getModel(String name) {
		return models.get(name);
	}
	
	public void addLight(Light light){
		if(mainLight == null) {
			setMainDirectionalLight(light);
		}
		sceneObjects.put(light.getName(), light);
		lights.put(light.getName(), light);
	}
	
	public void removeLight(String name){
		sceneObjects.remove(name);
		lights.remove(name);
	}
	
	public HashMap<String, Light> getLights() {
		return lights;
	}
	
	public Light getLight(String name) {
		return lights.get(name);
	}
	
	public Light getMainDirectionalLight() {
		return mainLight;
	}

	public void setMainDirectionalLight(Light mainLight) {
		if(this.mainLight != null) {
			this.mainLight.setTag(Light.LIGHT_TAG);
		}
		mainLight.setTag(Light.MAIN_DIRECTIONAL_LIGHT_TAG);
		this.mainLight = mainLight;
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

	public HashMap<String, Camera> getCameras() {
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
