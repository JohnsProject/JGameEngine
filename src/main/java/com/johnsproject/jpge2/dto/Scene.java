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
package com.johnsproject.jpge2.dto;

import java.util.ArrayList;
import java.util.List;

import com.johnsproject.jpge2.processing.VectorProcessor;

public class Scene {
	
	private final List<Model> models = new ArrayList<Model>();
	private final List<Camera> cameras = new ArrayList<Camera>();
	private final List<Light> lights = new ArrayList<Light>();
	
	public Scene() {
		cameras.add(new Camera("Default Camera", new Transform(), VectorProcessor.generate(0, 0, 1, 1)));
		lights.add(new Light("Default Light", new Transform()));
	}
	
	public void addModel(Model model){
		models.add(model);
	}
	
	public void removeModel(Model model){
		models.remove(model);
	}

	public List<Model> getModels() {
		return models;
	}
	
	public void addLight(Light light){
		lights.add(light);
	}
	
	public void removeLight(Light light){
		lights.remove(light);
	}
	
	public List<Light> getLights() {
		return lights;
	}
	
	public void addCamera(Camera camera){
		cameras.add(camera);
	}
	
	public void removeCamera(Camera camera){
		cameras.remove(camera);
	}

	public List<Camera> getCameras() {
		return cameras;
	}
}
