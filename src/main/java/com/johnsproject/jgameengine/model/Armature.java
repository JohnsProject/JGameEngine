/**
 * MIT License
 *
 * Copyright (c) 2018 John Salomon - John´s Project
 *  
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.johnsproject.jgameengine.model;

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
