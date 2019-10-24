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

import java.util.ArrayList;
import java.util.List;

public class Scene {
	
	private Camera mainCamera;
	private final List<SceneObject> sceneObjects;
	private final List<Model> models;
	private final List<Camera> cameras;
	private final List<Light> lights;
	
	public Scene() {
		this.sceneObjects = new ArrayList<SceneObject>();
		this.models = new ArrayList<Model>();
		this.cameras = new ArrayList<Camera>();
		this.lights = new ArrayList<Light>();
	}
	
	public List<SceneObject> getSceneObjects() {
		return sceneObjects;
	}
	
	public SceneObject getSceneObject(int index) {
		return sceneObjects.get(index);
	}
	
	public SceneObject getSceneObject(String name) {
		for (int i = 0; i < sceneObjects.size(); i++) {
			SceneObject sceneObject = sceneObjects.get(i);
			if (sceneObject.getName().equals(name)) {
				return sceneObject;
			}
		}
		return null;
	}

	public void addModel(Model model){
		sceneObjects.add(model);
		models.add(model);
	}
	
	public void removeModel(Model model){
		sceneObjects.remove(model);
		models.remove(model);
	}
	
	public List<Model> getModels() {
		return models;
	}
	
	public Model getModel(int index) {
		return models.get(index);
	}
	
	public Model getModel(String name) {
		for (int i = 0; i < models.size(); i++) {
			Model model = models.get(i);
			if (model.getName().equals(name)) {
				return model;
			}
		}
		return null;
	}
	
	public void addLight(Light light){
		sceneObjects.add(light);
		lights.add(light);
	}
	
	public void removeLight(Light light){
		sceneObjects.remove(light);
		lights.remove(light);
	}
	
	public List<Light> getLights() {
		return lights;
	}
	
	public Light getLight(int index) {
		return lights.get(index);
	}
	
	public Light getLight(String name) {
		for (int i = 0; i < lights.size(); i++) {
			Light light = lights.get(i);
			if (light.getName().equals(name)) {
				return light;
			}
		}
		return null;
	}
	
	public void addCamera(Camera camera){
		if(mainCamera == null) {
			setMainCamera(camera);
		}
		sceneObjects.add(camera);
		cameras.add(camera);
	}
	
	public void removeCamera(Camera camera){
		sceneObjects.remove(camera);
		cameras.remove(camera);
	}

	public List<Camera> getCameras() {
		return cameras;
	}
	
	public Camera getCamera(int index) {
		return cameras.get(index);
	}
	
	public Camera getCamera(String name) {
		for (int i = 0; i < cameras.size(); i++) {
			Camera camera = cameras.get(i);
			if (camera.getName().equals(name)) {
				return camera;
			}
		}
		return null;
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
