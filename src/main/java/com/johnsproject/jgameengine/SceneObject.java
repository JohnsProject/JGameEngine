package com.johnsproject.jgameengine;

import com.johnsproject.jgameengine.math.Transform;
import com.johnsproject.jgameengine.physics.RigidBody;

public class SceneObject {
	
	protected String tag;
	protected boolean active;
	protected boolean culled;
	protected final String name;
	protected final Transform transform;
	protected final RigidBody rigidBody;
	
	public SceneObject(String name, Transform transform) {
		this.tag = "";
		this.name = name;
		this.transform = transform;
		this.active = true;
		this.culled = false;
		this.rigidBody = new RigidBody();
	}

	public Transform getTransform() {
		return this.transform;
	}

	public String getName() {
		return name;
	}
	
	public RigidBody getRigidBody() {
		return rigidBody;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
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
}
