package com.johnsproject.jgameengine;

import java.util.ArrayList;
import java.util.List;

import com.johnsproject.jgameengine.event.EngineEvent;
import com.johnsproject.jgameengine.event.EngineListener;
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
import com.johnsproject.jgameengine.shading.DirectionalLightShadowShader;
import com.johnsproject.jgameengine.shading.ForwardShaderBuffer;
import com.johnsproject.jgameengine.shading.GouraudShader;
import com.johnsproject.jgameengine.shading.Shader;
import com.johnsproject.jgameengine.shading.ShaderBuffer;
import com.johnsproject.jgameengine.shading.SpotLightShadowShader;
import com.johnsproject.jgameengine.util.VectorUtils;

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
		this.locationVector = VectorUtils.emptyVector();
		this.normalVector = VectorUtils.emptyVector();
		this.multiplyVector = VectorUtils.emptyVector();
		defaultShader = new GouraudShader();
		addShader(new DirectionalLightShadowShader());
		addShader(new SpotLightShadowShader());
		addShader(defaultShader);
	}

	public void initialize(EngineEvent e) {}
	
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
	
	public void dynamicUpdate(EngineEvent e) {
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
			VectorUtils.copy(vertex.getWorldLocation(), vertex.getLocalLocation());
			VectorUtils.copy(vertex.getWorldNormal(), VectorUtils.VECTOR_ZERO);
			animateVertex(armature, animationFrame, vertex);
			VectorUtils.multiply(vertex.getWorldLocation(), transform.getSpaceExitMatrix());
		}
	}
	
	private void animateVertex(Armature armature, AnimationFrame animationFrame, Vertex vertex) {
		if(animationFrame != null) {
			VectorUtils.copy(locationVector, VectorUtils.VECTOR_ZERO);
			VectorUtils.copy(normalVector, VectorUtils.VECTOR_ZERO);
			for (int i = 0; i < armature.getVertexGroups().length; i++) {
				final VertexGroup vertexGroup = armature.getVertexGroup(i);
				final int boneWeight = vertexGroup.getWeight(vertex);
				if(boneWeight != -1) {
					final int[][] boneMatrix = animationFrame.getBoneMatrix(vertexGroup.getBoneIndex());
					applyBone(vertex, boneWeight, boneMatrix);
				}
			}
			VectorUtils.copy(vertex.getWorldLocation(), locationVector);
			VectorUtils.copy(vertex.getWorldNormal(), normalVector);
		}
	}
	
	private void applyBone(Vertex vertex, int boneWeight, int[][] boneMatrix) {
		VectorUtils.copy(multiplyVector, vertex.getWorldLocation());
		VectorUtils.multiply(multiplyVector, boneMatrix);
		VectorUtils.multiply(multiplyVector, boneWeight);
		VectorUtils.add(locationVector, multiplyVector);
		VectorUtils.copy(multiplyVector, vertex.getWorldNormal());
		VectorUtils.multiply(multiplyVector, boneMatrix);
		VectorUtils.multiply(multiplyVector, boneWeight);
		VectorUtils.add(normalVector, multiplyVector);
	}
	
	private void transformFaces(Mesh mesh, Transform transform) {
		for (int f = 0; f < mesh.getFaces().length; f++) {
			final Face face = mesh.getFace(f);
			VectorUtils.copy(face.getWorldNormal(), face.getLocalNormal());
			VectorUtils.multiply(face.getWorldNormal(), transform.getSpaceExitNormalMatrix());
			// calculate vertex normals, just add the face the normals of the faces this vertex is a part of
			VectorUtils.add(face.getVertex(0).getWorldNormal(), face.getWorldNormal());
			VectorUtils.add(face.getVertex(1).getWorldNormal(), face.getWorldNormal());
			VectorUtils.add(face.getVertex(2).getWorldNormal(), face.getWorldNormal());			
		}
	}
	
	private void renderForEachCamera(Scene scene) {
		for (int c = 0; c < scene.getCameras().size(); c++) {
			Camera camera = scene.getCameras().get(c);
			if(!camera.isActive())
				continue;
			camera.setRenderTarget(frameBuffer);
			shaderBuffer.initialize(camera, scene.getLights());
			renderModels(scene);
		}
	}
	
	private void renderModels(Scene scene) {
		for (int s = 0; s < shaders.size(); s++) {
			Shader shader = shaders.get(s);
			shader.initialize(shaderBuffer);
			for (int m = 0; m < scene.getModels().size(); m++) {
				Model model = scene.getModels().get(m);
				if(!model.isActive() || model.isCulled())
					continue;
				final Mesh mesh = model.getMesh();
				shadeVertices(mesh, shader);
				shader.waitForVertexQueue();
				
				shadeFaces(mesh, shader);
				shader.waitForGeometryQueue();
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
	
	private void shadeFaces(Mesh mesh, Shader shader) {
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

	public ShaderBuffer getShaderBuffer() {
		return shaderBuffer;
	}

	public void initialize(ShaderBuffer shaderBuffer) {
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
	
	public void removeShader(Shader shader) {
		shaders.remove(shader);
	}

	public Shader getDefaultShader() {
		return defaultShader;
	}

	public void setDefaultShader(Shader defaultShader) {
		this.defaultShader = defaultShader;
	}
}