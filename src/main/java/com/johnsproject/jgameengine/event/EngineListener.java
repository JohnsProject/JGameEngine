package com.johnsproject.jgameengine.event;

public interface EngineListener {
	
	public void initialize(EngineEvent e);
	
	public void fixedUpdate(EngineEvent e);
	
	public void dynamicUpdate(EngineEvent e);
	
}
