package com.johnsproject.jgameengine;

import java.util.ArrayList;
import java.util.List;

public class EngineObject {

	public static final int LAYER_DEFAULT = 1000;
	
	private EngineObject parent;
	private String name;
	private String tag;
	private int layer;
	private final List<EngineObject> children;

	/**
	 * Creates a new EngineObject with parent equals null, name and tag equals
	 * empty string and the default layer.
	 */
	public EngineObject() {
		parent = null;
		name = "";
		tag = "";
		layer = LAYER_DEFAULT;
		children = new ArrayList<EngineObject>();
	}
	
	/**
	 * Creates a new EngineObject with parent equals null, the specified name
	 * and tag and the default layer.
	 * 
	 * @param name The name of this EngineObject.
	 * @param tag The tag of this EngineObject.
	 */
	public EngineObject(String name, String tag) {
		parent = null;
		this.name = name;
		this.tag = tag;
		layer = LAYER_DEFAULT;
		children = new ArrayList<EngineObject>();
	}
	
	/**
	 * This method is called by the {@link Engine} if this {@link EngineObject} is added
	 * as the child of another EngineObject or set as the
	 * {@link Engine#setMainObject main object}.
	 */
	public void initialize() { }
	
	/**
	 * This method is called by the {@link Engine} if {@link Engine#setMainObject}
	 * gets called and this {@link EngineObject} is a child of the main object.
	 */
	public void start() {}
	
	/**
	 * This method is called by the {@link Engine} at a fixed time step. It's only called
	 * if this {@link EngineObject} is a child of the
	 * {@link Engine#setMainObject main object}.
	 */
	public void fixedUpdate() {}
	
	/**
	 * This method is called by the {@link Engine} at a time step that depends on the
	 * frames per second. It's only called if this {@link EngineObject} is a child of the
	 * {@link Engine#setMainObject main object}.
	 */
	public void dynamicUpdate() {}
	
	/**
	 * Returns the parent {@link EngineObject} of this EngineObject or null
	 * if there is none.
	 * 
	 * @return The parent EngineObject or null.
	 */
	public EngineObject getParent() {
		return parent;
	}
	
	public boolean hasParent() {
		return parent != null;
	}
	
	private void setParent(EngineObject parent) {
		this.parent = parent;
	}
	
	/**
	 * Returns the main parent of this {@link EngineObject}.
	 * The main parent is the parent of all parents this EngineObject is a child of.
	 * 
	 * @return the main parent of this EngineObject.
	 */
	public EngineObject getMainParent() {
		EngineObject rootParent = getParent();
		while (rootParent.hasParent()) {
			rootParent = rootParent.getParent();
		}
		return rootParent;
	}
	
	/**
	 * Returns the name of this {@link EngineObject} or empty string if there is none. Never null.
	 * 
	 * @return The name of this EngineObject.
	 */
	public String getName() {
		return name;
	}
	
	public boolean hasName(String name) {
		return this.name.equals(name);
	}

	/**
	 * Sets the specified name (empty string if the name is null) as the name of this {@link EngineObject}.
	 * 
	 * @param name Name to set.
	 */
	public void setName(String name) {
		if(name == null) {
			this.name = "";
		} else {
			this.name = name;
		}
	}
	
	/**
	 * Returns the tag of this {@link EngineObject} or empty string if there is none. Never null.
	 * 
	 * @return The tag of this EngineObject.
	 */
	public String getTag() {
		return tag;
	}
	
	public boolean hasTag(String tag) {
		return this.tag.equals(tag);
	}

	/**
	 * Sets the specified tag (empty string if the tag is null) as the tag of this {@link EngineObject}.
	 * 
	 * @param tag Tag to set.
	 */
	public void setTag(String tag) {
		if(tag == null) {
			this.tag = "";
		} else {
			this.tag = tag;			
		}
	}

	public int getLayer() {
		return layer;
	}
	
	public boolean hasLayer(int layer) {
		return this.layer == layer;
	}

	public void setLayer(int layer) {
		this.layer = layer;
	}

	/**
	 * Returns the list of children of this {@link EngineObject}.
	 * Never null.
	 * 
	 * @return List of children.
	 */
	public List<EngineObject> getChildren() {
		return children;
	}
	
	public boolean hasChildren() {
		return children.size() > 0;
	}
	
	/**
	 * Adds the child to the children list of this {@link EngineObject}
	 * and sets this as the parent of the child.
	 * 
	 * @param child Child to add.
	 * @throws MultipleParentException If the specified child already
	 * is a child of another EngineObject.
	 */
	public void addChild(EngineObject child) throws MultipleParentException {
		if(child.hasParent()) {
			throw new MultipleParentException(child);
		} else {
			child.setParent(this);
			child.initialize();
			children.add(child);
		}
	}
	
	/**
	 * Removes the child of the children list of this {@link EngineObject}
	 * and sets the parent of the child to null.
	 * 
	 * @param child Child to remove.
	 */
	public void removeChild(EngineObject component) {
		component.setParent(null);
		children.remove(component);
	}
	
	/**
	 * Returns the first child with the name found in the children
	 * list of this {@link EngineObject} or null. Note that the children list is a
	 * {@link ArrayList} so the first child added with that name will
	 * also be the first found.
	 * <br><br>
	 * This method will not search in the children lists of the children of
	 * this EngineObject.
	 * 
	 * @param name Name of the child.
	 * @return First child with the name.
	 */
	public EngineObject getChildWithName(String name) {
		if(hasChildren()) {
			// for avoids the creation of a Iterator object, generating less garbage
			for (int i = 0; i < children.size(); i++) {
				EngineObject child = children.get(i);
				if(child.hasName(name)) {
					return child;
				}
			}
		}
		return null;
	}
	
	/**
	 * Returns the first child with the tag found in the children
	 * list of this {@link EngineObject} or null. Note that the children list is a
	 * {@link ArrayList} so the first child added with that tag will
	 * also be the first found.
	 * <br><br>
	 * This method will not search in the children lists of the children of
	 * this EngineObject.
	 * 
	 * @param tag Tag of the child.
	 * @return First child with the tag.
	 */
	public EngineObject getChildWithTag(String tag) {
		if(hasChildren()) {
			for (int i = 0; i < children.size(); i++) {
				EngineObject child = children.get(i);
				if(child.hasTag(tag)) {
					return child;
				}
			}
		}
		return null;
	}
	
	/**
	 * Returns the first child with the layer found in the children
	 * list of this {@link EngineObject} or null. Note that the children list is a
	 * {@link ArrayList} so the first child added with that layer will
	 * also be the first found.
	 * <br><br>
	 * This method will not search in the children lists of the children of
	 * this EngineObject.
	 * 
	 * @param layer Layer of the child.
	 * @return First child with the layer.
	 */
	public EngineObject getChildWithLayer(int layer) {
		if(hasChildren()) {
			for (int i = 0; i < children.size(); i++) {
				EngineObject child = children.get(i);
				if(child.hasLayer(layer)) {
					return child;
				}
			}
		}
		return null;
	}
	
	
}
