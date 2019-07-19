package com.johnsproject.jgameengine.model;

public class Armature {
	
	private final VertexGroup[] vertexGroups;
	private final Animation[] animations;
	private Animation currentAnimation;
	private int currentFrame;
	private boolean loopAnimation;
	
	public Armature(VertexGroup[] vertexGroups, Animation[] animations) {
		this.vertexGroups = vertexGroups;
		this.animations = animations;
		playAnimation(0, true);
	}

	public VertexGroup getVertexGroup(int index) {
		return vertexGroups[index];
	}
	
	public VertexGroup[] getVertexGroups() {
		return vertexGroups;
	}
	
	public Animation getAnimation(int index) {
		return animations[index];
	}
	
	public Animation[] getAnimations() {
		return animations;
	}
	
	public void playAnimation(String name, boolean loop) {
		for (int i = 0; i < animations.length; i++) {
			if(animations[i].getName().equals(name)) {
				playAnimation(i, loop);
			}
		}
	}
	
	public void playAnimation(int index, boolean loop) {
		loopAnimation = loop;
		currentAnimation = animations[index];
	}

	public Animation getCurrentAnimation() {
		return currentAnimation;
	}

	public AnimationFrame getCurrentAnimationFrame() {
		return currentAnimation.getFrame(getCurrentFrame());
	}
	
	public int getCurrentFrame() {
		if(currentFrame >= currentAnimation.getFrames().length-1) {
			currentFrame = 0;
		}
		return currentFrame;
	}

	public void nextFrame() {
		currentFrame++;
	}
	
	public boolean isPlaying() {
		return currentFrame < currentAnimation.getFrames().length;
	}
}
