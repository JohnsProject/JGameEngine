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

import com.johnsproject.jgameengine.event.EngineListener;
import com.johnsproject.jgameengine.library.GraphicsLibrary;
import com.johnsproject.jgameengine.library.MatrixLibrary;
import com.johnsproject.jgameengine.library.VectorLibrary;
import com.johnsproject.jgameengine.model.Camera;
import com.johnsproject.jgameengine.model.FrameBuffer;
import com.johnsproject.jgameengine.model.GeometryBuffer;
import com.johnsproject.jgameengine.model.Mesh;
import com.johnsproject.jgameengine.model.Model;
import com.johnsproject.jgameengine.model.Scene;
import com.johnsproject.jgameengine.model.ShaderBuffer;
import com.johnsproject.jgameengine.model.VertexBuffer;
import com.johnsproject.jgameengine.shader.Shader;
import com.johnsproject.jgameengine.shader.shaders.DirectionalLightShadowShader;
import com.johnsproject.jgameengine.shader.shaders.GouraudSpecularShader;
import com.johnsproject.jgameengine.shader.shaders.PointLightShadowShader;
import com.johnsproject.jgameengine.shader.shaders.SpotLightShadowShader;

public class GraphicsEngine implements EngineListener {
	
	private final List<Shader> preShaders;
	private final List<Shader> shaders;
	private final List<Shader> postShaders;
	private ShaderBuffer shaderBuffer;
	private FrameBuffer frameBuffer;
	private Scene scene;
	
	private final int[] modelMatrix;
	private final int[] normalMatrix;
	private final GraphicsLibrary graphicsLibrary;
	private final MatrixLibrary matrixLibrary;
	private final VectorLibrary vectorLibrary;
	
	public GraphicsEngine(Scene scene, FrameBuffer frameBuffer) {
		this.shaderBuffer = new ShaderBuffer();
		this.preShaders = new ArrayList<Shader>();
		this.shaders = new ArrayList<Shader>();
		this.postShaders = new ArrayList<Shader>();
		this.scene = scene;
		this.frameBuffer = frameBuffer;
		this.graphicsLibrary = new GraphicsLibrary();
		this.matrixLibrary = new MatrixLibrary();
		this.vectorLibrary = new VectorLibrary();
		this.modelMatrix = matrixLibrary.generate();
		this.normalMatrix = matrixLibrary.generate();
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
		shaderBuffer.setFrameBuffer(frameBuffer);
		shaderBuffer.setLights(scene.getLights());
		for (int m = 0; m < scene.getModels().size(); m++) {
			final Model model = scene.getModels().get(m);
			final Mesh mesh = model.getMesh();
			graphicsLibrary.modelMatrix(modelMatrix, model.getTransform());
			graphicsLibrary.normalMatrix(normalMatrix, model.getTransform());
			for (int v = 0; v < mesh.getVertices().length; v++) {
				final VertexBuffer vertexBuffer = mesh.getVertex(v).getBuffer();
				vertexBuffer.resetAll();
				int[] worldLocation = vertexBuffer.getWorldLocation();
				int[] worldNormal = vertexBuffer.getWorldNormal();
				vectorLibrary.matrixMultiply(worldLocation, modelMatrix, worldLocation);
				vectorLibrary.matrixMultiply(worldNormal, normalMatrix, worldNormal);
			}
			for (int f = 0; f < mesh.getFaces().length; f++) {
				GeometryBuffer geometryBuffer = mesh.getFace(f).getBuffer();
				geometryBuffer.resetAll();
				int[] worldNormal = geometryBuffer.getWorldNormal();
				vectorLibrary.matrixMultiply(worldNormal, normalMatrix, worldNormal);
			}
		}
		for (int s = 0; s < preShaders.size(); s++) {
			useShader(preShaders.get(s), s, true);
		}
		for (int s = 0; s < shaders.size(); s++) {
			useShader(shaders.get(s), s, false);
		}
		for (int s = 0; s < postShaders.size(); s++) {
			useShader(postShaders.get(s), s, true);
		}
	}
	
	public void useShader(Shader shader, int shaderIndex, boolean mustUse) {
		shader.update(shaderBuffer);
		for (int c = 0; c < scene.getCameras().size(); c++) {
			final Camera camera = scene.getCameras().get(c);
			shader.setup(camera);
			for (int m = 0; m < scene.getModels().size(); m++) {
				final Model model = scene.getModels().get(m);
				final Mesh mesh = model.getMesh();
				for (int v = 0; v < mesh.getVertices().length; v++) {
					final VertexBuffer vertexBuffer = mesh.getVertex(v).getBuffer();
					if ((vertexBuffer.getMaterial().getShaderIndex() == shaderIndex) | mustUse) {
						vertexBuffer.reset();
						shader.vertex(vertexBuffer);
					}
				}
				for (int f = 0; f < mesh.getFaces().length; f++) {
					GeometryBuffer geometryBuffer = mesh.getFace(f).getBuffer();
					if ((geometryBuffer.getMaterial().getShaderIndex() == shaderIndex) | mustUse) {
						geometryBuffer.reset();
						shader.geometry(geometryBuffer);
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
		return shaderBuffer;
	}

	public void setShaderDataBuffer(ShaderBuffer shaderDataBuffer) {
		this.shaderBuffer = shaderDataBuffer;
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
	
	public List<Shader> getPreprocessingShaders() {
		return preShaders;
	}
	
	public List<Shader> getShaders() {
		return shaders;
	}
	
	public List<Shader> getPostprocessingShaders() {
		return postShaders;
	}
	
	public Shader getPreprocessingShader(int index) {
		return preShaders.get(index);
	}
	
	public Shader getShader(int index) {
		return shaders.get(index);
	}
	
	public Shader getPostprocessingShader(int index) {
		return postShaders.get(index);
	}
	
	public void addPreprocessingShader(Shader shader) {
		preShaders.add(shader);
	}
	
	public void addShader(Shader shader) {
		shaders.add(shader);
	}
	
	public void addPostprocessingShader(Shader shader) {
		postShaders.add(shader);
	}

	public void removePreprocessingShader(int index) {
		Shader shader = getPreprocessingShader(index);
		removePreprocessingShader(shader);
	}
	
	public void removeShader(int index) {
		Shader shader = getShader(index);
		removeShader(shader);
	}
	
	public void removePostprocessingShader(int index) {
		Shader shader = getPostprocessingShader(index);
		removePostprocessingShader(shader);
	}
	
	public boolean removePreprocessingShader(Shader shader) {
		shader.terminate(shaderBuffer);
		return preShaders.remove(shader);
	}
	
	public boolean removeShader(Shader shader) {
		shader.terminate(shaderBuffer);
		return shaders.remove(shader);
	}
	
	public boolean removePostprocessingShader(Shader shader) {
		shader.terminate(shaderBuffer);
		return postShaders.remove(shader);
	}
}
