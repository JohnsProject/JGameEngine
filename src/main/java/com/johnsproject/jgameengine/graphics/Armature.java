package com.johnsproject.jgameengine.graphics;

public class Armature {
	
	private final VertexGroup[] vertexGroups;
	private final Animation[] animations;
	private Animation currentAnimation;
	private int currentFrame;
	private int animationSpeed;
	private boolean loopAnimation;
	
	public Armature(VertexGroup[] vertexGroups, Animation[] animations) {
		this.vertexGroups = vertexGroups;
		this.animations = animations;
		this.animationSpeed = 1;
		this.currentFrame = 0;
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
			return currentAnimation.getFrame(getCurrentFrame());
		}
	}
	
	public int getCurrentFrame() {
		return currentFrame;
	}

	public void nextFrame() {
		currentFrame += animationSpeed;
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
			return getCurrentFrame() < currentAnimation.getFrames().length;
		}
	}
	
	public void stopPlaying() {
		currentFrame = 0;
		currentAnimation = null;
	}

	public int getAnimationSpeed() {
		return animationSpeed;
	}

	public void setAnimationSpeed(int animationSpeed) {
		this.animationSpeed = animationSpeed;
	}
}
