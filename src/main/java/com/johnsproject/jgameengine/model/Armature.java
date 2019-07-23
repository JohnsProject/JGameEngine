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
		if(currentAnimation == null) {
			return null;
		} else {
			return currentAnimation.getFrame(currentFrame);
		}
	}
	
	public int getCurrentFrame() {
		return currentFrame;
	}

	public void nextFrame() {
		currentFrame++;
		if(!isPlaying()) {
			if(loopAnimation) {
				currentFrame = 0;
			} else {
				stopPlaying();
			}
		}
	}
	
	public boolean isPlaying() {
		if(currentAnimation == null) {
			return false;
		} else {
			return currentFrame < currentAnimation.getFrames().length;
		}
	}
	
	public void stopPlaying() {
		currentFrame = -1;
		currentAnimation = null;
	}
}
