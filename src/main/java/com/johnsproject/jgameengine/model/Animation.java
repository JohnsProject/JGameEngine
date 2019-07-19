package com.johnsproject.jgameengine.model;

public class Animation {

	private final String name;
	private final AnimationFrame[] frames;
	
	public Animation(String name, AnimationFrame[] frames) {
		this.name = name;
		this.frames = frames;
	}
	
	public String getName() {
		return name;
	}
	
	public AnimationFrame getFrame(int index) {
		return frames[index];
	}
	
	public AnimationFrame[] getFrames() {
		return frames;
	}
}
