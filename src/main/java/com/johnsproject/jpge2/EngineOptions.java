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
package com.johnsproject.jpge2;

import java.util.ArrayList;
import java.util.List;

import com.johnsproject.jpge2.dto.FrameBuffer;
import com.johnsproject.jpge2.dto.Scene;
import com.johnsproject.jpge2.processors.GraphicsProcessor.Shader;
import com.johnsproject.jpge2.shaders.FlatSpecularShader;
import com.johnsproject.jpge2.shaders.GouraudSpecularShader;
import com.johnsproject.jpge2.shaders.PhongSpecularShader;

public class EngineOptions {
	
	private int updateRate;
	private int maxUpdateSkip;
	private FrameBuffer frameBuffer;
	private Scene scene;
	private List<Shader> shaders;
	
	public EngineOptions() {
		updateRate = 25;
		maxUpdateSkip = 10;
		frameBuffer = new FrameBuffer();
		scene = new Scene();
		shaders = new ArrayList<Shader>();
		shaders.add(new FlatSpecularShader());
	}
	
	public int getUpdateRate() {
		return updateRate;
	}
	
	public void setUpdateRate(int updateRate) {
		this.updateRate = updateRate;
	}
	
	public int getMaxUpdateSkip() {
		return maxUpdateSkip;
	}
	
	public void setMaxUpdateSkip(int maxUpdateSkip) {
		this.maxUpdateSkip = maxUpdateSkip;
	}	
	
	public FrameBuffer getFrameBuffer() {
		return frameBuffer;
	}

	public Scene getScene() {
		return scene;
	}

	public void setScene(Scene scene) {
		this.scene = scene;
	}
	
	public void addShader(Shader shader) {
		shaders.add(shader);
		sortShaders();
	}
	
	public void removeShader(Shader shader) {
		shaders.remove(shader);
		sortShaders();
	}
	
	public List<Shader> getShaders() {
		return shaders;
	}
	
	public Shader getShader(int index) {
		return shaders.get(index);
	}
	
	private void sortShaders() {
		for (int i = 0; i < shaders.size() - 1; i++) {
			int min_i = i;
			for (int j = i + 1; j < shaders.size(); j++) {
				if (shaders.get(j).getPass() < shaders.get(min_i).getPass()) {
					min_i = j;
				}
			}
			Shader temp = shaders.get(min_i);
			shaders.set(min_i, shaders.get(i));
			shaders.set(i, temp);
		}
	}
}
