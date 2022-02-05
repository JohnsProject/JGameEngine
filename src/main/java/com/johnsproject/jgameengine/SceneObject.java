package com.johnsproject.jgameengine;

import java.util.HashMap;
import java.util.Map;

public final class SceneObject {
	
	private String tag = "";
	private boolean isActive = true;
	private boolean culled = false;
	private final String name;
	private final Map<Class<?>, SceneObjectComponent> components = new HashMap<Class<?>, SceneObjectComponent>();
	
	public SceneObject(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public boolean isActive() {
		return isActive;
	}

	public void setActive(boolean active) {
		this.isActive = active;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public boolean isCulled() {
		return culled;
	}

	public void setCulled(boolean culled) {
		this.culled = culled;
	}
	
	public void addComponent(SceneObjectComponent component) {
		this.components.put(component.getClass(), component);
		component.setOwner(this);
	}
	
	public void removeComponent(SceneObjectComponent component) {
		removeComponentWithType(component.getClass());
	}
	
	public <T extends SceneObjectComponent> void removeComponentWithType(Class<T> type) {
		SceneObjectComponent component = this.components.remove(type);
		component.setOwner(null);
	}
	
	@SuppressWarnings("unchecked")
	public <T extends SceneObjectComponent> T getComponentWithType(Class<T> type) {
		return (T)components.get(type);
	}
}
