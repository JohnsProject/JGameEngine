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
import com.johnsproject.jgameengine.dto.GeometryBuffer;
import com.johnsproject.jgameengine.dto.Mesh;
import com.johnsproject.jgameengine.dto.Model;
import com.johnsproject.jgameengine.dto.Scene;
import com.johnsproject.jgameengine.dto.ShaderBuffer;
import com.johnsproject.jgameengine.dto.VertexBuffer;
import com.johnsproject.jgameengine.event.EngineListener;
import com.johnsproject.jgameengine.shader.Shader;
import com.johnsproject.jgameengine.shader.shaders.DirectionalLightShadowShader;
import com.johnsproject.jgameengine.shader.shaders.GouraudSpecularShader;
import com.johnsproject.jgameengine.shader.shaders.PointLightShadowShader;
import com.johnsproject.jgameengine.shader.shaders.SpotLightShadowShader;

public class GraphicsEngine implements EngineListener {
	
	private final List<Shader> shaders;
	private ShaderBuffer shaderDataBuffer;
	private FrameBuffer frameBuffer;
	private Scene scene;
	
	private int preShadersCount;
	private int shadersCount;
	private int postShadersCount;
	
	public GraphicsEngine(Scene scene, FrameBuffer frameBuffer) {
		this.shaderDataBuffer = new ShaderBuffer();
		this.shaders = new ArrayList<Shader>();
		this.scene = scene;
		this.frameBuffer = frameBuffer;
		addPreprocessingShader(new DirectionalLightShadowShader());
		addPreprocessingShader(new SpotLightShadowShader());
		addPreprocessingShader(new PointLightShadowShader());
		addShader(new GouraudSpecularShader());
	}
	
	public void start() { }
	
	public void update() {
		frameBuffer.getColorBuffer().fill(0);
		frameBuffer.getDepthBuffer().fill(Integer.MAX_VALUE);
		frameBuffer.getStencilBuffer().fill(0);
		shaderDataBuffer.setFrameBuffer(frameBuffer);
		shaderDataBuffer.setLights(scene.getLights());
		for (int s = 0; s < shaders.size(); s++) {
			final Shader shader = shaders.get(s);
			shader.update(shaderDataBuffer);
			final int shaderIndex = s - preShadersCount;
			final boolean preShader = s < preShadersCount;
			final boolean postShader = s > preShadersCount + shadersCount;
			for (int c = 0; c < scene.getCameras().size(); c++) {
				final Camera camera = scene.getCameras().get(c);
				shader.setup(camera);
				for (int m = 0; m < scene.getModels().size(); m++) {
					final Model model = scene.getModels().get(m);
					final Mesh mesh = model.getMesh();
					shader.setup(model);
					for (int v = 0; v < mesh.getVertices().length; v++) {
						final VertexBuffer vertexBuffer = mesh.getVertex(v).getBuffer();
						if ((vertexBuffer.getMaterial().getShaderIndex() == shaderIndex) | preShader | postShader) {
							vertexBuffer.reset();
							shader.vertex(vertexBuffer);
						}
					}
					for (int f = 0; f < mesh.getFaces().length; f++) {
						GeometryBuffer geometryBuffer = mesh.getFace(f).getBuffer();
						if ((geometryBuffer.getMaterial().getShaderIndex() == shaderIndex) | preShader | postShader) {
							geometryBuffer.reset();
							shader.geometry(geometryBuffer);
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

	public ShaderBuffer getShaderDataBuffer() {
		return shaderDataBuffer;
	}

	public void setShaderDataBuffer(ShaderBuffer shaderDataBuffer) {
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
	
	public int addPreprocessingShader(Shader shader) {
		final int index = preShadersCount;
		shaders.add(shader);
		sortShaders(index, 0);
		preShadersCount++;
		return index;
	}
	
	public int addShader(Shader shader) {
		final int index = preShadersCount + shadersCount;
		shaders.add(shader);
		sortShaders(index, 1);
		shadersCount++;
		return index;
	}
	
	public int addPostprocessingShader(Shader shader) {
		final int index = preShadersCount + shadersCount + postShadersCount;
		postShadersCount++;
		shaders.add(shader);
		sortShaders(index, 2);
		postShadersCount++;
		return index;
	}
	
	public boolean removePreprocessingShader(Shader shader) {
		preShadersCount--;
		shader.terminate(shaderDataBuffer);
		return shaders.remove(shader);
	}
	
	public boolean removeShader(Shader shader) {
		shadersCount--;
		shader.terminate(shaderDataBuffer);
		return shaders.remove(shader);
	}
	
	public boolean removePostprocessingShader(Shader shader) {
		postShadersCount--;
		shader.terminate(shaderDataBuffer);
		return shaders.remove(shader);
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

	private void sortShaders(int index, int pass) {
		final int shaderCount = shaders.size();
		for (int i = shaderCount-1; i > -1; i--) {
			int currentPass = 0;
			if(i > preShadersCount) currentPass = 1;
			if(i > preShadersCount + shadersCount) currentPass = 2;
			if (currentPass > pass) {
				Shader temp = shaders.get(index);
				shaders.set(index, shaders.get(i));
				shaders.set(i, temp);
				index = i;
			}
		}
	}
}
