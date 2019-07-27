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

import com.johnsproject.jgameengine.event.EngineEvent;
import com.johnsproject.jgameengine.event.EngineListener;
import com.johnsproject.jgameengine.library.GraphicsLibrary;
import com.johnsproject.jgameengine.library.MatrixLibrary;
import com.johnsproject.jgameengine.library.VectorLibrary;
import com.johnsproject.jgameengine.model.AnimationFrame;
import com.johnsproject.jgameengine.model.Armature;
import com.johnsproject.jgameengine.model.Camera;
import com.johnsproject.jgameengine.model.Face;
import com.johnsproject.jgameengine.model.FrameBuffer;
import com.johnsproject.jgameengine.model.GeometryBuffer;
import com.johnsproject.jgameengine.model.Mesh;
import com.johnsproject.jgameengine.model.Model;
import com.johnsproject.jgameengine.model.Scene;
import com.johnsproject.jgameengine.model.Vertex;
import com.johnsproject.jgameengine.model.VertexBuffer;
import com.johnsproject.jgameengine.model.VertexGroup;
import com.johnsproject.jgameengine.shader.ForwardShaderBuffer;
import com.johnsproject.jgameengine.shader.Shader;
import com.johnsproject.jgameengine.shader.ShaderBuffer;
import com.johnsproject.jgameengine.shader.ShadowMappingShader;

public class GraphicsEngine implements EngineListener {
	
	private final List<Shader> preShaders;
	private ShaderBuffer shaderBuffer;
	private FrameBuffer frameBuffer;
	
	private final int[] modelMatrix;
	private final int[] normalMatrix;
	private final int[]	locationVector;
	private final int[]	normalVector;
	private final int[] multiplyVector;
	private final GraphicsLibrary graphicsLibrary;
	private final VectorLibrary vectorLibrary;
	
	public GraphicsEngine(FrameBuffer frameBuffer) {
		this.shaderBuffer = new ForwardShaderBuffer();
		this.preShaders = new ArrayList<Shader>();
		this.frameBuffer = frameBuffer;
		this.graphicsLibrary = new GraphicsLibrary();
		this.vectorLibrary = new VectorLibrary();
		this.modelMatrix = MatrixLibrary.generate();
		this.normalMatrix = MatrixLibrary.generate();
		this.locationVector = VectorLibrary.generate();
		this.normalVector = VectorLibrary.generate();
		this.multiplyVector = VectorLibrary.generate();
		addPreprocessingShader(new ShadowMappingShader());
	}
	
	public void start(EngineEvent e) { }
	
	public void fixedUpdate(EngineEvent e) { 
		Scene scene = e.getScene();
		for (int m = 0; m < scene.getModels().size(); m++) {
			scene.getModels().get(m).getArmature().nextFrame();
		}		
	}
	
	public void update(EngineEvent e) {
		Scene scene = e.getScene();
		frameBuffer.getColorBuffer().fill(0);
		frameBuffer.getDepthBuffer().fill(Integer.MAX_VALUE);
		frameBuffer.getStencilBuffer().fill(0);
		transformToWorld(scene);
		for (int c = 0; c < scene.getCameras().size(); c++) {
			final Camera camera = scene.getCameras().get(c);
			if(!camera.isActive())
				continue;
			shaderBuffer.setup(camera, scene.getLights(), frameBuffer);
			applyPreShaders(scene);
			for (int m = 0; m < scene.getModels().size(); m++) {
				final Model model = scene.getModels().get(m);
				if(!model.isActive())
					continue;
				final Mesh mesh = model.getMesh();
				for (int v = 0; v < mesh.getVertices().length; v++) {
					final Vertex vertex = mesh.getVertex(v);
					final Shader shader = vertex.getMaterial().getShader();
					vertex.getBuffer().reset();
					shader.setShaderBuffer(shaderBuffer);
					shader.vertex(vertex.getBuffer());
				}
				for (int f = 0; f < mesh.getFaces().length; f++) {
					final Face face = mesh.getFace(f);
					final Shader shader = face.getMaterial().getShader();
					face.getBuffer().reset();
					shader.setShaderBuffer(shaderBuffer);
					shader.geometry(face.getBuffer());
				}
			}
		}
	}
	
	private void transformToWorld(Scene scene) {
		for (int m = 0; m < scene.getModels().size(); m++) {
			final Model model = scene.getModels().get(m);
			if(!model.isActive())
				continue;
			final Mesh mesh = model.getMesh();
			final Armature armature = model.getArmature();
			final AnimationFrame animationFrame = armature.getCurrentAnimationFrame();
			graphicsLibrary.modelMatrix(modelMatrix, model.getTransform());
			graphicsLibrary.normalMatrix(normalMatrix, model.getTransform());
			for (int v = 0; v < mesh.getVertices().length; v++) {
				final Vertex vertex = mesh.getVertex(v);
				final VertexBuffer vertexBuffer = vertex.getBuffer();
				vertexBuffer.resetAll();
				final int[] worldLocation = vertexBuffer.getWorldLocation();
				final int[] normal = vertexBuffer.getNormal();
				animateVertex(armature, animationFrame, vertex, worldLocation, normal);
				vectorLibrary.matrixMultiply(worldLocation, modelMatrix, worldLocation);
				vectorLibrary.matrixMultiply(normal, normalMatrix, normal);
			}
			for (int f = 0; f < mesh.getFaces().length; f++) {
				GeometryBuffer geometryBuffer = mesh.getFace(f).getBuffer();
				geometryBuffer.resetAll();
				int[] worldNormal = geometryBuffer.getWorldNormal();
				vectorLibrary.matrixMultiply(worldNormal, normalMatrix, worldNormal);
			}
		}
	}
	
	private void animateVertex(Armature armature, AnimationFrame animationFrame, Vertex vertex, int[] worldLocation, int[] normal) {
		if(animationFrame != null) {
			vectorLibrary.copy(locationVector, VectorLibrary.VECTOR_ZERO);
			vectorLibrary.copy(normalVector, VectorLibrary.VECTOR_ZERO);
			for (int i = 0; i < armature.getVertexGroups().length; i++) {
				final VertexGroup vertexGroup = armature.getVertexGroup(i);
				final int boneWeight = vertexGroup.getWeight(vertex);
				if(boneWeight != -1) {
					int[] rotationMatrix = animationFrame.getBoneMatrix(vertexGroup.getBoneIndex());
					vectorLibrary.matrixMultiply(worldLocation, rotationMatrix, multiplyVector);
					vectorLibrary.multiply(multiplyVector, boneWeight, multiplyVector);
					vectorLibrary.add(locationVector, multiplyVector, locationVector);
					vectorLibrary.matrixMultiply(normal, rotationMatrix, multiplyVector);
					vectorLibrary.multiply(multiplyVector, boneWeight, multiplyVector);
					vectorLibrary.add(normalVector, multiplyVector, normalVector);
				}
			}
			vectorLibrary.copy(worldLocation, locationVector);
			vectorLibrary.copy(normal, normalVector);
		}
	}
	
	private void applyPreShaders(Scene scene) {
		for (int s = 0; s < preShaders.size(); s++) {
			final Shader shader = preShaders.get(s);
			for (int m = 0; m < scene.getModels().size(); m++) {
				final Model model = scene.getModels().get(m);
				if(!model.isActive())
					continue;
				final Mesh mesh = model.getMesh();
				for (int v = 0; v < mesh.getVertices().length; v++) {
					final Vertex vertex = mesh.getVertex(v);
					vertex.getBuffer().reset();
					shader.setShaderBuffer(shaderBuffer);
					shader.vertex(vertex.getBuffer());
				}
				for (int f = 0; f < mesh.getFaces().length; f++) {
					final Face face = mesh.getFace(f);
					face.getBuffer().reset();
					shader.setShaderBuffer(shaderBuffer);
					shader.geometry(face.getBuffer());
				}
			}
		}
	}

	public int getLayer() {
		return GRAPHICS_ENGINE_LAYER;
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
	
	public List<Shader> getPreprocessingShaders() {
		return preShaders;
	}
	
	public Shader getPreprocessingShader(int index) {
		return preShaders.get(index);
	}
	
	public void addPreprocessingShader(Shader shader) {
		preShaders.add(shader);
	}
}
