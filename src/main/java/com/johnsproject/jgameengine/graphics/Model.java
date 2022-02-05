package com.johnsproject.jgameengine.graphics;

import com.johnsproject.jgameengine.SceneObjectComponent;
import com.johnsproject.jgameengine.math.Transform;

public class Model extends SceneObjectComponent {

	private final Mesh mesh;
	private final Armature armature;
	private Transform transform;
	private boolean isCulled;
	
	public Model (Mesh mesh) {
		this.mesh = mesh;
		this.armature = null;
	}
	
	public Model (Mesh mesh, Armature armature) {
		this.mesh = mesh;
		this.armature = armature;
	}
	
	public Transform getTransform() {
		if(transform == null)
			transform = owner.getComponentWithType(Transform.class);

		if(transform == null) {
			transform = new Transform();
			owner.addComponent(transform);
		}
		
		return transform;
	}

	public Mesh getMesh() {
		return mesh;
	}
	
	public Armature getArmature() {
		return armature;
	}

	public boolean isCulled() {
		return isCulled;
	}

	public void setCulled(boolean isCulled) {
		this.isCulled = isCulled;
	}
}
