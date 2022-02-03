package com.johnsproject.jgameengine;

import java.util.ArrayList;

import com.johnsproject.jgameengine.graphics.Camera;
import com.johnsproject.jgameengine.graphics.Light;
import com.johnsproject.jgameengine.graphics.Model;

public class Scene {
	
	private Camera mainCamera;
	private Light mainLight;
	private final ArrayList<SceneObject> sceneObjects;
	private final ArrayList<Model> models;
	private final ArrayList<Camera> cameras;
	private final ArrayList<Light> lights;
	
	public Scene() {
		this.sceneObjects = new ArrayList<SceneObject>();
		this.models = new ArrayList<Model>();
		this.cameras = new ArrayList<Camera>();
		this.lights = new ArrayList<Light>();
	}
	
	public ArrayList<SceneObject> getSceneObjects() {
		return sceneObjects;
	}
	
	public SceneObject getSceneObject(String name) {
		for (int i = 0; i < sceneObjects.size(); i++) {
			SceneObject sceneObject = sceneObjects.get(i);
			if(sceneObject.getName().equals(name)) {
				return sceneObject;
			}
		}
		return null;
	}
	
	private void removeSceneObject(String name) {
		for (int i = 0; i < sceneObjects.size(); i++) {
			if(sceneObjects.get(i).getName().equals(name)) {
				sceneObjects.remove(i);
			}
		}
	}

	public void addModel(Model model){
		sceneObjects.add(model);
		models.add(model);
	}
	
	public void removeModel(String name){
		removeSceneObject(name);
		for (int i = 0; i < models.size(); i++) {
			if(models.get(i).getName().equals(name)) {
				models.remove(i);
			}
		}
	}
	
	public ArrayList<Model> getModels() {
		return models;
	}
	
	public Model getModel(String name) {
		return (Model)getSceneObject(name);
	}
	
	public void addLight(Light light){
		if(mainLight == null) {
			setMainDirectionalLight(light);
		}
		sceneObjects.add(light);
		lights.add(light);
	}
	
	public void removeLight(String name){
		removeSceneObject(name);
		for (int i = 0; i < lights.size(); i++) {
			if(lights.get(i).getName().equals(name)) {
				lights.remove(i);
			}
		}
	}
	
	public ArrayList<Light> getLights() {
		return lights;
	}
	
	public Light getLight(String name) {
		return (Light)getSceneObject(name);
	}
	
	public Light getMainDirectionalLight() {
		return mainLight;
	}

	public void setMainDirectionalLight(Light mainLight) {
		if(this.mainLight != null) {
			this.mainLight.setMain(false);
		}
		mainLight.setMain(true);
		this.mainLight = mainLight;
	}
	
	public void addCamera(Camera camera){
		if(mainCamera == null) {
			setMainCamera(camera);
		}
		sceneObjects.add(camera);
		cameras.add(camera);
	}
	
	public void removeCamera(String name){
		removeSceneObject(name);
		for (int i = 0; i < cameras.size(); i++) {
			if(cameras.get(i).getName().equals(name)) {
				cameras.remove(i);
			}
		}
	}

	public ArrayList<Camera> getCameras() {
		return cameras;
	}
	
	public Camera getCamera(String name) {
		return (Camera)getSceneObject(name);
	}

	public Camera getMainCamera() {
		return mainCamera;
	}

	public void setMainCamera(Camera mainCamera) {
		if(this.mainCamera != null) {
			this.mainCamera.setMain(false);
		}
		mainCamera.setMain(true);
		this.mainCamera = mainCamera;
	}
}
