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
import com.johnsproject.jgameengine.model.Material;
import com.johnsproject.jgameengine.model.Mesh;
import com.johnsproject.jgameengine.model.Model;
import com.johnsproject.jgameengine.model.Scene;
import com.johnsproject.jgameengine.model.Transform;
import com.johnsproject.jgameengine.model.Vertex;
import com.johnsproject.jgameengine.model.VertexGroup;
import com.johnsproject.jgameengine.shader.FlatSpecularShader;
import com.johnsproject.jgameengine.shader.ForwardShaderBuffer;
import com.johnsproject.jgameengine.shader.GouraudSpecularShader;
import com.johnsproject.jgameengine.shader.PhongSpecularShader;
import com.johnsproject.jgameengine.shader.Shader;
import com.johnsproject.jgameengine.shader.ShaderBuffer;
import com.johnsproject.jgameengine.shader.ShadowMappingShader;

public class GraphicsEngine implements EngineListener {
	
	private Shader defaultShader;
	private final List<Shader> shaders;
	private ShaderBuffer shaderBuffer;
	private FrameBuffer frameBuffer;
	private final int[]	locationVector;
	private final int[]	normalVector;
	private final int[] multiplyVector;
	
	public GraphicsEngine(FrameBuffer frameBuffer) {
		this.shaderBuffer = new ForwardShaderBuffer();
		this.shaders = new ArrayList<Shader>();
		this.frameBuffer = frameBuffer;
		this.locationVector = VectorMath.emptyVector();
		this.normalVector = VectorMath.emptyVector();
		this.multiplyVector = VectorMath.emptyVector();
		defaultShader = new GouraudSpecularShader();
		addShader(new ShadowMappingShader());
		addShader(new FlatSpecularShader());
		addShader(defaultShader);
		addShader(new PhongSpecularShader());
	}

	public void start(EngineEvent e) { }
	
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
		localToWorldSpace(scene);
		renderForEachCamera(scene);
	}
	
	private void localToWorldSpace(Scene scene) {
		for (int i = 0; i < scene.getModels().size(); i++) {
			Model model = scene.getModels().get(i);
			if(!model.isActive())
				continue;
			final Mesh mesh = model.getMesh();
			final Armature armature = model.getArmature();
			final Transform transform = model.getTransform();
			transformVertices(mesh, transform, armature);
			transformFaces(mesh, transform);
		}
	}
	
	private void transformVertices(Mesh mesh, Transform transform, Armature armature) {
		AnimationFrame animationFrame = null;
		if(armature != null) {
			animationFrame = armature.getCurrentAnimationFrame();
		}
		for (int v = 0; v < mesh.getVertices().length; v++) {
			final Vertex vertex = mesh.getVertex(v);
			VectorMath.copy(vertex.getWorldLocation(), vertex.getLocalLocation());
			VectorMath.copy(vertex.getWorldNormal(), vertex.getLocalNormal());
			animateVertex(armature, animationFrame, vertex);
			VectorMath.multiply(vertex.getWorldLocation(), transform.getSpaceExitMatrix());
			VectorMath.multiply(vertex.getWorldNormal(), transform.getSpaceExitNormalMatrix());
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
					final int[][] boneMatrix = animationFrame.getBoneMatrix(vertexGroup.getBoneIndex());
					applyBone(vertex, boneWeight, boneMatrix);
				}
			}
			VectorMath.copy(vertex.getWorldLocation(), locationVector);
			VectorMath.copy(vertex.getWorldNormal(), normalVector);
		}
	}
	
	private void applyBone(Vertex vertex, int boneWeight, int[][] boneMatrix) {
		VectorMath.copy(multiplyVector, vertex.getWorldLocation());
		VectorMath.multiply(multiplyVector, boneMatrix);
		VectorMath.multiply(multiplyVector, boneWeight);
		VectorMath.add(locationVector, multiplyVector);
		VectorMath.copy(multiplyVector, vertex.getWorldNormal());
		VectorMath.multiply(multiplyVector, boneMatrix);
		VectorMath.multiply(multiplyVector, boneWeight);
		VectorMath.add(normalVector, multiplyVector);
	}
	
	private void transformFaces(Mesh mesh, Transform transform) {
		for (int f = 0; f < mesh.getFaces().length; f++) {
			final Face face = mesh.getFace(f);
			VectorMath.copy(face.getWorldNormal(), face.getLocalNormal());
			VectorMath.multiply(face.getWorldNormal(), transform.getSpaceExitNormalMatrix());
		}
	}
	
	private void renderForEachCamera(Scene scene) {
		for (int c = 0; c < scene.getCameras().size(); c++) {
			Camera camera = scene.getCameras().get(c);
			if(!camera.isActive())
				continue;
			if(camera.getRenderTarget() == null) {
				camera.setRenderTarget(frameBuffer);
			}
			shaderBuffer.setup(camera, scene.getLights());
			renderModels(scene);
		}
	}
	
	private void renderModels(Scene scene) {
		for (int s = 0; s < shaders.size(); s++) {
			Shader shader = shaders.get(s);
			shader.setShaderBuffer(shaderBuffer);
			for (int m = 0; m < scene.getModels().size(); m++) {
				Model model = scene.getModels().get(m);
				if(!model.isActive())
					continue;
				final Mesh mesh = model.getMesh();
				shadeVertices(mesh, shader);
				shaderFaces(mesh, shader);
			}
		}
	}
	
	private void shadeVertices(Mesh mesh, Shader shader) {
		for (int v = 0; v < mesh.getVertices().length; v++) {
			final Vertex vertex = mesh.getVertex(v);
			final Material material = vertex.getMaterial();
			if(canUseShader(shader, material)) {
				shader.vertex(vertex);
			}
		}
	}
	
	private void shaderFaces(Mesh mesh, Shader shader) {
		for (int f = 0; f < mesh.getFaces().length; f++) {
			final Face face = mesh.getFace(f);
			final Material material = face.getMaterial();
			if(canUseShader(shader, material)) {
				shader.geometry(face);
			}
		}
	}
	
	private boolean canUseShader(Shader shader, Material material) {
		return ((material.getShader() == null) && shader.equals(defaultShader))
		|| shader.equals(material.getShader())
		|| shader.isGlobal();
	}

	public int getLayer() {
		return GRAPHICS_ENGINE_LAYER;
	}

	public ShaderBuffer getShaderBuffer() {
		return shaderBuffer;
	}

	public void setShaderBuffer(ShaderBuffer shaderBuffer) {
		this.shaderBuffer = shaderBuffer;
	}
	
	public void setFrameBuffer(FrameBuffer frameBuffer) {
		this.frameBuffer = frameBuffer;
	}
	
	public FrameBuffer getFrameBuffer() {
		return frameBuffer;
	}
	
	public List<Shader> getShaders() {
		return shaders;
	}
	
	public Shader getShader(int index) {
		return shaders.get(index);
	}
	
	public void addShader(Shader shader) {
		shaders.add(shader);
	}

	public Shader getDefaultShader() {
		return defaultShader;
	}

	public void setDefaultShader(Shader defaultShader) {
		this.defaultShader = defaultShader;
	}
}