package com.johnsproject.jgameengine;

import static org.junit.Assert.*;

import org.junit.Test;

public class EngineObjectTest {

	
	@Test
	public void getMainParentTest() throws Exception {
		EngineObject mainParent = new EngineObject();
		EngineObject parent = new EngineObject();
		EngineObject child = new EngineObject("Name", "Tag");
		mainParent.addChild(parent);
		parent.addChild(child);
		assertEquals(child.getMainParent(), mainParent);
	}	
	
	@Test
	public void addChildTest() throws Exception {
		EngineObject parent1 = new EngineObject();
		EngineObject parent2 = new EngineObject();
		EngineObject child = new EngineObject("Name", "Tag");
		parent1.addChild(child);
		MultipleParentException exception = null;
		try {
			parent2.addChild(child);
		} catch (MultipleParentException e) {
			exception = e;
		}
		assertNotNull(exception);
	}
	
	@Test
	public void removeChildTest() throws Exception {
		EngineObject parent1 = new EngineObject();
		EngineObject parent2 = new EngineObject();
		EngineObject child = new EngineObject("Name", "Tag");
		parent1.addChild(child);
		parent1.removeChild(child);
		MultipleParentException exception = null;
		try {
			parent2.addChild(child);
		} catch (MultipleParentException e) {
			exception = e;
		}
		assertNull(exception);
	}
	
	@Test
	public void getChildTest() throws Exception {
		EngineObject parent = new EngineObject();
		EngineObject child1 = new EngineObject("Name", "Tag");
		EngineObject child2 = new EngineObject("Name", "Tag");
		parent.addChild(child1);
		parent.addChild(child2);
		assertEquals(parent.getChildWithName("Name"), child1);
		assertEquals(parent.getChildWithTag("Tag"), child1);
		assertEquals(parent.getChildWithLayer(EngineObject.LAYER_DEFAULT), child1);
	}
}
