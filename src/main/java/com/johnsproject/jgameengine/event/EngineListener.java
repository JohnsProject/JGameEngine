package com.johnsproject.jgameengine.event;

public interface EngineListener {
	
	public static final int INPUT_ENGINE_LAYER = -100;
	public static final int DEFAULT_LAYER = 0;
	public static final int PHYSICS_ENGINE_LAYER = 99;
	public static final int GRAPHICS_ENGINE_LAYER = 100;
	
	public void initialize(EngineEvent e);
	
	public void fixedUpdate(EngineEvent e);
	
	public void dynamicUpdate(EngineEvent e);
	
	public int getLayer();
	
}
