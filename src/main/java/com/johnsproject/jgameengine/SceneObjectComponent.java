package com.johnsproject.jgameengine;

public abstract class SceneObjectComponent {
	
	protected boolean isActive = true;
	protected SceneObject owner;
	
	public boolean isActive() {
		return isActive;
	}
	
	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}
	
	public SceneObject getOwner() {
		return owner;
	}
	
	public void setOwner(SceneObject owner) {
		this.owner = owner;
	}

}
