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
import com.johnsproject.jgameengine.math.MatrixMath;
import com.johnsproject.jgameengine.math.TransformationMath;
import com.johnsproject.jgameengine.math.VectorMath;
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
		this.modelMatrix = MatrixMath.indentityMatrix();
		this.matrixCache1 = MatrixMath.indentityMatrix();
		this.matrixCache2 = MatrixMath.indentityMatrix();
		this.normalMatrix = MatrixMath.indentityMatrix();
		this.locationVector = VectorMath.toVector();
		this.normalVector = VectorMath.toVector();
		this.multiplyVector = VectorMath.toVector();
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
			TransformationMath.modelMatrix(modelMatrix, model.getTransform(), matrixCache1, matrixCache2);
			TransformationMath.normalMatrix(normalMatrix, model.getTransform(), matrixCache1, matrixCache2);
			for (int v = 0; v < mesh.getVertices().length; v++) {
				final Vertex vertex = mesh.getVertex(v);
				final VertexBuffer vertexBuffer = vertex.getBuffer();
				final int[] worldLocation = vertexBuffer.getWorldLocation();
				final int[] worldNormal = vertexBuffer.getWorldNormal();
				VectorMath.copy(worldLocation, vertex.getLocation());
				VectorMath.copy(worldNormal, vertex.getNormal());
				animateVertex(armature, animationFrame, vertex, worldLocation, worldNormal);
				VectorMath.matrixMultiply(worldLocation, modelMatrix);
				VectorMath.matrixMultiply(worldNormal, normalMatrix);
			}
			for (int f = 0; f < mesh.getFaces().length; f++) {
				final Face face = mesh.getFace(f);
				final GeometryBuffer geometryBuffer = face.getBuffer();
				int[] worldNormal = geometryBuffer.getWorldNormal();
				VectorMath.copy(worldNormal, face.getNormal());
				VectorMath.matrixMultiply(worldNormal, normalMatrix);
				VectorMath.copy(geometryBuffer.getUV(0), face.getUV(0));
				VectorMath.copy(geometryBuffer.getUV(1), face.getUV(1));
				VectorMath.copy(geometryBuffer.getUV(2), face.getUV(2));
				geometryBuffer.getVertexBuffers()[0] = face.getVertex(0).getBuffer();
				geometryBuffer.getVertexBuffers()[1] = face.getVertex(1).getBuffer();
				geometryBuffer.getVertexBuffers()[2] = face.getVertex(2).getBuffer();
			}
		}
	}
	
	private void animateVertex(Armature armature, AnimationFrame animationFrame, Vertex vertex, int[] location, int[] normal) {
		if(animationFrame != null) {
			VectorMath.copy(locationVector, VectorMath.VECTOR_ZERO);
			VectorMath.copy(normalVector, VectorMath.VECTOR_ZERO);
			for (int i = 0; i < armature.getVertexGroups().length; i++) {
				final VertexGroup vertexGroup = armature.getVertexGroup(i);
				final int boneWeight = vertexGroup.getWeight(vertex);
				if(boneWeight != -1) {
					int[] rotationMatrix = animationFrame.getBoneMatrix(vertexGroup.getBoneIndex());
					VectorMath.copy(multiplyVector, location);
					VectorMath.matrixMultiply(multiplyVector, rotationMatrix);
					VectorMath.multiply(multiplyVector, boneWeight);
					VectorMath.add(locationVector, multiplyVector);
					VectorMath.copy(multiplyVector, normal);
					VectorMath.matrixMultiply(multiplyVector, rotationMatrix);
					VectorMath.multiply(multiplyVector, boneWeight);
					VectorMath.add(normalVector, multiplyVector);
				}
			}
			VectorMath.copy(location, locationVector);
			VectorMath.copy(normal, normalVector);
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
