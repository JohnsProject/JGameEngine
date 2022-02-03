package com.johnsproject.jgameengine.graphics;

import com.johnsproject.jgameengine.SceneObject;
import com.johnsproject.jgameengine.math.Transform;

public class Model extends SceneObject {
	
	public static final String MODEL_TAG = "Model";
	
	private final Mesh mesh;
	private final Armature armature;
	
	public Model (String name, Transform transform, Mesh mesh) {
		super(name, transform);
		super.tag = MODEL_TAG;
		this.mesh = mesh;
		this.armature = null;
	}
	
	public Model (String name, Transform transform, Mesh mesh, Armature armature) {
		super(name, transform);
		super.tag = MODEL_TAG;
		this.mesh = mesh;
		this.armature = armature;
	}

	public Mesh getMesh() {
		return mesh;
	}
	
	public Armature getArmature() {
		return armature;
	}
}
