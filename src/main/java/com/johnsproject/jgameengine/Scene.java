package com.johnsproject.jgameengine;

import java.util.ArrayList;
import java.util.List;

public final class Scene {
	
	private final List<SceneObject> sceneObjects = new ArrayList<SceneObject>();
	
	public List<SceneObject> getSceneObjects() {
		return sceneObjects;
	}
	
	public void addSceneObject(SceneObject sceneObject) {
		sceneObjects.add(sceneObject);
	}
	
	public void removeSceneObject(SceneObject sceneObject) {
		sceneObjects.remove(sceneObject);
	}
	
	public void removeSceneObjectWithName(String name) {
		removeSceneObject(getSceneObjectWithName(name));
	}
	
	/**
	 * @return The first scene object with the name found. Null if none.
	 */
	public SceneObject getSceneObjectWithName(String name) {
		SceneObject sceneObject = null;
		for (int i = 0; i < sceneObjects.size(); i++) {
			SceneObject possibleObj = sceneObjects.get(i);
			if(possibleObj.getName().equals(name)) {
				sceneObject = possibleObj;
				break;
			}
		}
		return sceneObject;
	}
	
	/**
	 * Fills the target list with scene objects with a component of the specified type. Clears the list first.
	 */
	public <T extends SceneObjectComponent > void getSceneObjectsWithComponent(Class<T> type, List<T> target) {
		target.clear();
		for (int i = 0; i < sceneObjects.size(); i++) {
			final SceneObject sceneObject = sceneObjects.get(i);
			if(sceneObject.isActive()) {
				final T component = sceneObject.getComponentWithType(type);
				if(component != null)
					target.add(component);
			}
		}
	}
}
