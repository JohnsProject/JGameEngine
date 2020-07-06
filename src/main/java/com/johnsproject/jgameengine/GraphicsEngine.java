package com.johnsproject.jgameengine;

import java.util.ArrayList;
import java.util.List;

import com.johnsproject.jgameengine.event.EngineEvent;
import com.johnsproject.jgameengine.event.EngineListener;
import com.johnsproject.jgameengine.math.VectorMath;
import com.johnsproject.jgameengine.model.AnimationFrame;
import com.johnsproject.jgameengine.model.Armature;
import com.johnsproject.jgameengine.model.Camera;
import com.johnsproject.jgameengine.model.Face;
import com.johnsproject.jgameengine.model.FrameBuffer;
import com.johnsproject.jgameengine.model.Mesh;
import com.johnsproject.jgameengine.model.Model;
import com.johnsproject.jgameengine.model.Scene;
import com.johnsproject.jgameengine.model.Transform;
import com.johnsproject.jgameengine.model.Vertex;
import com.johnsproject.jgameengine.model.VertexGroup;
import com.johnsproject.jgameengine.shader.ForwardShaderBuffer;
import com.johnsproject.jgameengine.shader.Shader;
import com.johnsproject.jgameengine.shader.ShaderBuffer;
import com.johnsproject.jgameengine.shader.ShadowMappingShader;

public class GraphicsEngine implements EngineListener {
	
	private final List<Shader> preShaders;
	private ShaderBuffer shaderBuffer;
	private FrameBuffer frameBuffer;
	private final int[]	locationVector;
	private final int[]	normalVector;
	private final int[] multiplyVector;
	
	public GraphicsEngine(FrameBuffer frameBuffer) {
		this.shaderBuffer = new ForwardShaderBuffer();
		this.preShaders = new ArrayList<Shader>();
		this.frameBuffer = frameBuffer;
		this.locationVector = VectorMath.emptyVector();
		this.normalVector = VectorMath.emptyVector();
		this.multiplyVector = VectorMath.emptyVector();
		addPreprocessingShader(new ShadowMappingShader());
	}

	public void start(EngineEvent e) {
	
	}
	
	public void fixedUpdate(EngineEvent e) { 
		Scene scene = e.getScene();
		for (int i = 0; i < scene.getModels().size(); i++) {
			Model model = scene.getModels().get(i);
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
		for (int c = 0; c < scene.getCameras().size(); c++) {
			Camera camera = scene.getCameras().get(c);
			if(!camera.isActive())
				continue;
			if(camera.getRenderTarget() == null) {
				camera.setRenderTarget(frameBuffer);
			}
			shaderBuffer.setup(camera, scene.getLights());
			callShaders(scene, preShaders);
			for (int m = 0; m < scene.getModels().size(); m++) {
				Model model = scene.getModels().get(m);
				if(!model.isActive())
					continue;
				final Mesh mesh = model.getMesh();
				for (int v = 0; v < mesh.getVertices().length; v++) {
					final Vertex vertex = mesh.getVertex(v);
					final Shader shader = vertex.getMaterial().getShader();
					shader.setShaderBuffer(shaderBuffer);
					shader.vertex(vertex);
				}
				for (int f = 0; f < mesh.getFaces().length; f++) {
					final Face face = mesh.getFace(f);
					final Shader shader = face.getMaterial().getShader();
					shader.setShaderBuffer(shaderBuffer);
					shader.geometry(face);
				}
			}
		}
	}
	
	private void transformToWorld(Scene scene) {
		for (int i = 0; i < scene.getModels().size(); i++) {
			Model model = scene.getModels().get(i);
			if(!model.isActive())
				continue;
			final Mesh mesh = model.getMesh();
			final Armature armature = model.getArmature();
			AnimationFrame animationFrame = null;
			if(armature != null) {
				animationFrame = armature.getCurrentAnimationFrame();
			}
			final Transform transform = model.getTransform();
			for (int v = 0; v < mesh.getVertices().length; v++) {
				final Vertex vertex = mesh.getVertex(v);
				VectorMath.copy(vertex.getWorldLocation(), vertex.getLocalLocation());
				VectorMath.copy(vertex.getWorldNormal(), vertex.getLocalNormal());
				animateVertex(armature, animationFrame, vertex);
				VectorMath.multiply(vertex.getWorldLocation(), transform.getSpaceExitMatrix());
				VectorMath.multiply(vertex.getWorldNormal(), transform.getSpaceExitNormalMatrix());
			}
			for (int f = 0; f < mesh.getFaces().length; f++) {
				final Face face = mesh.getFace(f);
				VectorMath.copy(face.getWorldNormal(), face.getLocalNormal());
				VectorMath.multiply(face.getWorldNormal(), transform.getSpaceExitNormalMatrix());
			}
		}
	}
	
	private void animateVertex(Armature armature, AnimationFrame animationFrame, Vertex vertex) {
		if(animationFrame != null) {
			VectorMath.copy(locationVector, VectorMath.VECTOR_ZERO);
			VectorMath.copy(normalVector, VectorMath.VECTOR_ZERO);
			for (int i = 0; i < armature.getVertexGroups().length; i++) {
				final VertexGroup vertexGroup = armature.getVertexGroup(i);
				final int boneWeight = vertexGroup.getWeight(vertex);
				if(boneWeight != -1) {
					int[][] boneMatrix = animationFrame.getBoneMatrix(vertexGroup.getBoneIndex());
					VectorMath.copy(multiplyVector, vertex.getWorldLocation());
					VectorMath.multiply(multiplyVector, boneMatrix);
					VectorMath.multiply(multiplyVector, boneWeight);
					VectorMath.add(locationVector, multiplyVector);
					VectorMath.copy(multiplyVector, vertex.getWorldNormal());
					VectorMath.multiply(multiplyVector, boneMatrix);
					VectorMath.multiply(multiplyVector, boneWeight);
					VectorMath.add(normalVector, multiplyVector);
				}
			}
			VectorMath.copy(vertex.getWorldLocation(), locationVector);
			VectorMath.copy(vertex.getWorldNormal(), normalVector);
		}
	}
	
	private void callShaders(Scene scene, List<Shader> shaders) {
		for (int s = 0; s < shaders.size(); s++) {
			final Shader shader = shaders.get(s);
			for (int i = 0; i < scene.getModels().size(); i++) {
				Model model = scene.getModels().get(i);
				if(!model.isActive())
					continue;
				final Mesh mesh = model.getMesh();
				for (int v = 0; v < mesh.getVertices().length; v++) {
					final Vertex vertex = mesh.getVertex(v);
					shader.setShaderBuffer(shaderBuffer);
					shader.vertex(vertex);
				}
				for (int f = 0; f < mesh.getFaces().length; f++) {
					final Face face = mesh.getFace(f);
					shader.setShaderBuffer(shaderBuffer);
					shader.geometry(face);
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
