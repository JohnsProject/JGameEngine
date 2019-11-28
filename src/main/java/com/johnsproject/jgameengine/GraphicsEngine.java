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
import com.johnsproject.jgameengine.library.MatrixLibrary;
import com.johnsproject.jgameengine.library.TransformationLibrary;
import com.johnsproject.jgameengine.library.VectorLibrary;
import com.johnsproject.jgameengine.model.AnimationFrame;
import com.johnsproject.jgameengine.model.Armature;
import com.johnsproject.jgameengine.model.Camera;
import com.johnsproject.jgameengine.model.Face;
import com.johnsproject.jgameengine.model.FrameBuffer;
import com.johnsproject.jgameengine.model.Mesh;
import com.johnsproject.jgameengine.model.Model;
import com.johnsproject.jgameengine.model.Scene;
import com.johnsproject.jgameengine.model.Vertex;
import com.johnsproject.jgameengine.model.VertexGroup;
import com.johnsproject.jgameengine.shader.ForwardShaderBuffer;
import com.johnsproject.jgameengine.shader.GeometryBuffer;
import com.johnsproject.jgameengine.shader.Shader;
import com.johnsproject.jgameengine.shader.ShaderBuffer;
import com.johnsproject.jgameengine.shader.ShadowMappingShader;
import com.johnsproject.jgameengine.shader.VertexBuffer;

public class GraphicsEngine implements EngineListener {
	
	private final List<Shader> preShaders;
	private ShaderBuffer shaderBuffer;
	private FrameBuffer frameBuffer;
	
	private final int[] modelMatrix;
	private final int[] normalMatrix;
	private final int[]	locationVector;
	private final int[]	normalVector;
	private final int[] multiplyVector;
	
	private final int[] matrixCache1;
	private final int[] matrixCache2;
	
	public GraphicsEngine(FrameBuffer frameBuffer) {
		this.shaderBuffer = new ForwardShaderBuffer();
		this.preShaders = new ArrayList<Shader>();
		this.frameBuffer = frameBuffer;
		this.modelMatrix = MatrixLibrary.generate();
		this.matrixCache1 = MatrixLibrary.generate();
		this.matrixCache2 = MatrixLibrary.generate();
		this.normalMatrix = MatrixLibrary.generate();
		this.locationVector = VectorLibrary.generate();
		this.normalVector = VectorLibrary.generate();
		this.multiplyVector = VectorLibrary.generate();
		addPreprocessingShader(new ShadowMappingShader());
	}

	public void start(EngineEvent e) {
	
	}
	
	public void fixedUpdate(EngineEvent e) { 
		Scene scene = e.getScene();
		for (Model model : scene.getModels().values()) {
			final Armature armature = model.getArmature();
			if(armature != null) {
				armature.nextFrame();
			}
		}		
	}
	
	public void update(EngineEvent e) {
		Scene scene = e.getScene();
		frameBuffer.getColorBuffer().fill(0);
		frameBuffer.getDepthBuffer().fill(Integer.MAX_VALUE);
		frameBuffer.getStencilBuffer().fill(0);
		transformToWorld(scene);
		for (Camera camera : scene.getCameras().values()) {
			if(!camera.isActive())
				continue;
			shaderBuffer.setup(camera, scene.getLights().values(), frameBuffer);
			callShaders(scene, preShaders);
			for (Model model : scene.getModels().values()) {
				if(!model.isActive())
					continue;
				final Mesh mesh = model.getMesh();
				for (int v = 0; v < mesh.getVertices().length; v++) {
					final Vertex vertex = mesh.getVertex(v);
					final Shader shader = vertex.getMaterial().getShader();
					shader.setShaderBuffer(shaderBuffer);
					shader.vertex(vertex.getBuffer());
				}
				for (int f = 0; f < mesh.getFaces().length; f++) {
					final Face face = mesh.getFace(f);
					final Shader shader = face.getMaterial().getShader();
					shader.setShaderBuffer(shaderBuffer);
					shader.geometry(face.getBuffer());
				}
			}
		}
	}
	
	private void callShaders(Scene scene, List<Shader> shaders) {
		for (int s = 0; s < shaders.size(); s++) {
			final Shader shader = shaders.get(s);
			for (Model model : scene.getModels().values()) {
				if(!model.isActive())
					continue;
				final Mesh mesh = model.getMesh();
				for (int v = 0; v < mesh.getVertices().length; v++) {
					final Vertex vertex = mesh.getVertex(v);
					shader.setShaderBuffer(shaderBuffer);
					shader.vertex(vertex.getBuffer());
				}
				for (int f = 0; f < mesh.getFaces().length; f++) {
					final Face face = mesh.getFace(f);
					shader.setShaderBuffer(shaderBuffer);
					shader.geometry(face.getBuffer());
				}
			}
		}
	}
	
	private void transformToWorld(Scene scene) {
		for (Model model : scene.getModels().values()) {
			if(!model.isActive())
				continue;
			final Mesh mesh = model.getMesh();
			final Armature armature = model.getArmature();
			AnimationFrame animationFrame = null;
			if(armature != null) {
				animationFrame = armature.getCurrentAnimationFrame();
			}
			TransformationLibrary.modelMatrix(modelMatrix, model.getTransform(), matrixCache1, matrixCache2);
			TransformationLibrary.normalMatrix(normalMatrix, model.getTransform(), matrixCache1, matrixCache2);
			for (int v = 0; v < mesh.getVertices().length; v++) {
				final Vertex vertex = mesh.getVertex(v);
				final VertexBuffer vertexBuffer = vertex.getBuffer();
				final int[] worldLocation = vertexBuffer.getWorldLocation();
				final int[] worldNormal = vertexBuffer.getWorldNormal();
				VectorLibrary.copy(worldLocation, vertex.getLocation());
				VectorLibrary.copy(worldNormal, vertex.getNormal());
				animateVertex(armature, animationFrame, vertex, worldLocation, worldNormal);
				VectorLibrary.matrixMultiply(worldLocation, modelMatrix);
				VectorLibrary.matrixMultiply(worldNormal, normalMatrix);
			}
			for (int f = 0; f < mesh.getFaces().length; f++) {
				final Face face = mesh.getFace(f);
				final GeometryBuffer geometryBuffer = face.getBuffer();
				int[] worldNormal = geometryBuffer.getWorldNormal();
				VectorLibrary.copy(worldNormal, face.getNormal());
				VectorLibrary.matrixMultiply(worldNormal, normalMatrix);
				VectorLibrary.copy(geometryBuffer.getUV(0), face.getUV(0));
				VectorLibrary.copy(geometryBuffer.getUV(1), face.getUV(1));
				VectorLibrary.copy(geometryBuffer.getUV(2), face.getUV(2));
				geometryBuffer.getVertexBuffers()[0] = face.getVertex(0).getBuffer();
				geometryBuffer.getVertexBuffers()[1] = face.getVertex(1).getBuffer();
				geometryBuffer.getVertexBuffers()[2] = face.getVertex(2).getBuffer();
			}
		}
	}
	
	private void animateVertex(Armature armature, AnimationFrame animationFrame, Vertex vertex, int[] location, int[] normal) {
		if(animationFrame != null) {
			VectorLibrary.copy(locationVector, VectorLibrary.VECTOR_ZERO);
			VectorLibrary.copy(normalVector, VectorLibrary.VECTOR_ZERO);
			for (int i = 0; i < armature.getVertexGroups().length; i++) {
				final VertexGroup vertexGroup = armature.getVertexGroup(i);
				final int boneWeight = vertexGroup.getWeight(vertex);
				if(boneWeight != -1) {
					int[] rotationMatrix = animationFrame.getBoneMatrix(vertexGroup.getBoneIndex());
					VectorLibrary.copy(multiplyVector, location);
					VectorLibrary.matrixMultiply(multiplyVector, rotationMatrix);
					VectorLibrary.multiply(multiplyVector, boneWeight);
					VectorLibrary.add(locationVector, multiplyVector);
					VectorLibrary.copy(multiplyVector, normal);
					VectorLibrary.matrixMultiply(multiplyVector, rotationMatrix);
					VectorLibrary.multiply(multiplyVector, boneWeight);
					VectorLibrary.add(normalVector, multiplyVector);
				}
			}
			VectorLibrary.copy(location, locationVector);
			VectorLibrary.copy(normal, normalVector);
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
