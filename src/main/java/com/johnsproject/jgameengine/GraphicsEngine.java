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
package com.johnsproject.jgameengine;

import java.util.ArrayList;
import java.util.List;

import com.johnsproject.jgameengine.dto.Camera;
import com.johnsproject.jgameengine.dto.FrameBuffer;
import com.johnsproject.jgameengine.dto.GeometryDataBuffer;
import com.johnsproject.jgameengine.dto.Mesh;
import com.johnsproject.jgameengine.dto.Model;
import com.johnsproject.jgameengine.dto.Scene;
import com.johnsproject.jgameengine.dto.ShaderDataBuffer;
import com.johnsproject.jgameengine.dto.VertexDataBuffer;
import com.johnsproject.jgameengine.event.EngineListener;
import com.johnsproject.jgameengine.shader.Shader;
import com.johnsproject.jgameengine.shader.databuffers.ForwardDataBuffer;
import com.johnsproject.jgameengine.shader.shaders.DirectionalLightShadowShader;
import com.johnsproject.jgameengine.shader.shaders.GouraudShader;
import com.johnsproject.jgameengine.shader.shaders.PointLightShadowShader;
import com.johnsproject.jgameengine.shader.shaders.SpotLightShadowShader;

public class GraphicsEngine implements EngineListener {
	
	private final List<Shader> shaders;
	private ShaderDataBuffer shaderDataBuffer;
	private FrameBuffer frameBuffer;
	private Scene scene;
	
	private int preShadersCount;
	private int shadersCount;
	private int postShadersCount;
	
	public GraphicsEngine(Scene scene, FrameBuffer frameBuffer) {
		this.shaderDataBuffer = new ForwardDataBuffer();
		this.shaders = new ArrayList<Shader>();
		this.scene = scene;
		this.frameBuffer = frameBuffer;
		addPreprocessingShader(new DirectionalLightShadowShader());
		addPreprocessingShader(new SpotLightShadowShader());
		addPreprocessingShader(new PointLightShadowShader());
		addShader(new GouraudShader());
	}
	
	public void start() { }
	
	public void update() {
		shaderDataBuffer.setFrameBuffer(frameBuffer);
		shaderDataBuffer.setLights(scene.getLights());
		for (int s = 0; s < shaders.size(); s++) {
			Shader shader = shaders.get(s);
			shader.update(shaderDataBuffer);
			for (int c = 0; c < scene.getCameras().size(); c++) {
				Camera camera = scene.getCameras().get(c);
				shader.setup(camera);
				for (int m = 0; m < scene.getModels().size(); m++) {
					Model model = scene.getModels().get(m);
					Mesh mesh = model.getMesh();
					shader.setup(model);
					for (int v = 0; v < mesh.getVertices().length; v++) {
						VertexDataBuffer dataBuffer = mesh.getVertex(v).getDataBuffer();
						if ((dataBuffer.getMaterial().getShaderIndex() == s - preShadersCount)
								| (s < preShadersCount) | (s > preShadersCount + shadersCount)) {
							dataBuffer.reset();
							shader.vertex(dataBuffer);
						}
					}
					for (int f = 0; f < mesh.getFaces().length; f++) {
						GeometryDataBuffer dataBuffer = mesh.getFace(f).getDataBuffer();
						if ((dataBuffer.getMaterial().getShaderIndex() == s - preShadersCount)
								| (s < preShadersCount) | (s > preShadersCount + shadersCount)) {
							dataBuffer.reset();
							shader.geometry(dataBuffer);
						}
					}
				}
			}
		}
	}
	
	public void fixedUpdate() { }

	public int getPriority() {
		return 10000;
	}

	public ShaderDataBuffer getShaderDataBuffer() {
		return shaderDataBuffer;
	}

	public void setShaderDataBuffer(ShaderDataBuffer shaderDataBuffer) {
		this.shaderDataBuffer = shaderDataBuffer;
	}
	
	public void setFrameBuffer(FrameBuffer frameBuffer) {
		this.frameBuffer = frameBuffer;
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
	
	public List<Shader> getShaders() {
		return shaders;
	}
	
	public Shader getPreprocessingShader(int index) {
		return shaders.get(index);
	}
	
	public Shader getShader(int index) {
		return shaders.get(preShadersCount + index);
	}
	
	public Shader getPostprocessingShader(int index) {
		return shaders.get(preShadersCount + shadersCount + index);
	}
	
	public void addPreprocessingShader(Shader shader) {
		preShadersCount++;
		shaders.add(shader);
		sortShaders(0);
	}
	
	public int addShader(Shader shader) {
		shadersCount++;
		shaders.add(shader);
		sortShaders(1);
		return preShadersCount + shadersCount;
	}
	
	public void addPostprocessingShader(Shader shader) {
		postShadersCount++;
		shaders.add(shader);
		sortShaders(2);
	}
	
	public void removePreprocessingShader(Shader shader) {
		preShadersCount--;
		shader.terminate(shaderDataBuffer);
		shaders.remove(shader);
		sortShaders(0);
	}
	
	public void removeShader(Shader shader) {
		shadersCount--;
		shader.terminate(shaderDataBuffer);
		shaders.remove(shader);
		sortShaders(1);
	}
	
	public void removePostprocessingShader(Shader shader) {
		postShadersCount--;
		shader.terminate(shaderDataBuffer);
		shaders.remove(shader);
		sortShaders(2);
	}
	
	public int getPreprocessingShadersCount() {
		return preShadersCount;
	}

	public int getShadersCount() {
		return shadersCount;
	}
	
	public int getPostprocessingShadersCount() {
		return postShadersCount;
	}

	private void sortShaders(int pass) {
		for (int i = 0; i < shaders.size(); i++) {
			int min_i = i;
			for (int j = i + 1; j < shaders.size(); j++) {
				int currentPass = 0;
				if(j > preShadersCount) currentPass = 1;
				if(j > shadersCount) currentPass = 2;
				if (pass > currentPass) {
					min_i = j;
				}
			}
			Shader temp = shaders.get(min_i);
			shaders.set(min_i, shaders.get(i));
			shaders.set(i, temp);
		}
	}
}
