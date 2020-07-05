package com.johnsproject.jgameengine;

import java.util.ArrayList;
import java.util.List;

import com.johnsproject.jgameengine.event.EngineEvent;
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
import com.johnsproject.jgameengine.shader.GeometryBuffer;
import com.johnsproject.jgameengine.shader.Shader;
import com.johnsproject.jgameengine.shader.ShaderBuffer;
import com.johnsproject.jgameengine.shader.ShadowMappingShader;
import com.johnsproject.jgameengine.shader.VertexBuffer;

public class GraphicsEngine extends EngineObject {

	public static final String NAME = "GraphicsEngine";
	public static final String TAG = "GraphicsEngine";
	public static final int LAYER = 2000;
	
	private final List<Shader> preShaders;
	private ShaderBuffer shaderBuffer;
	private FrameBuffer frameBuffer;
	private final int[]	locationVector;
	private final int[]	normalVector;
	private final int[] multiplyVector;
	
	public GraphicsEngine(FrameBuffer frameBuffer) {
		super(NAME, TAG, LAYER);
		this.shaderBuffer = new ForwardShaderBuffer();
		this.preShaders = new ArrayList<Shader>();
		this.frameBuffer = frameBuffer;
		this.locationVector = VectorMath.emptyVector();
		this.normalVector = VectorMath.emptyVector();
		this.multiplyVector = VectorMath.emptyVector();
		addPreprocessingShader(new ShadowMappingShader());
		setLayer(LAYER);
	}
	
	@Override
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
	
	@Override
	public void dynamicUpdate(EngineEvent e) {
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
			for (int i = 0; i < scene.getModels().size(); i++) {
				Model model = scene.getModels().get(i);
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
				final VertexBuffer vertexBuffer = vertex.getBuffer();
				final int[] worldLocation = vertexBuffer.getWorldLocation();
				final int[] worldNormal = vertexBuffer.getWorldNormal();
				VectorMath.copy(worldLocation, vertex.getLocation());
				VectorMath.copy(worldNormal, vertex.getNormal());
				animateVertex(armature, animationFrame, vertex, worldLocation, worldNormal);
				VectorMath.multiply(worldLocation, transform.getSpaceExitMatrix());
				VectorMath.multiply(worldNormal, transform.getSpaceExitNormalMatrix());
			}
			for (int f = 0; f < mesh.getFaces().length; f++) {
				final Face face = mesh.getFace(f);
				final GeometryBuffer geometryBuffer = face.getBuffer();
				int[] worldNormal = geometryBuffer.getWorldNormal();
				VectorMath.copy(worldNormal, face.getNormal());
				VectorMath.multiply(worldNormal, transform.getSpaceExitNormalMatrix());
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
					int[][] rotationMatrix = animationFrame.getBoneMatrix(vertexGroup.getBoneIndex());
					VectorMath.copy(multiplyVector, location);
					VectorMath.multiply(multiplyVector, rotationMatrix);
					VectorMath.multiply(multiplyVector, boneWeight);
					VectorMath.add(locationVector, multiplyVector);
					VectorMath.copy(multiplyVector, normal);
					VectorMath.multiply(multiplyVector, rotationMatrix);
					VectorMath.multiply(multiplyVector, boneWeight);
					VectorMath.add(normalVector, multiplyVector);
				}
			}
			VectorMath.copy(location, locationVector);
			VectorMath.copy(normal, normalVector);
		}
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
