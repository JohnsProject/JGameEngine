package com.johnsproject.jpge2.controller;

import java.util.ArrayList;
import java.util.List;

import com.johnsproject.jpge2.Engine;
import com.johnsproject.jpge2.EngineListener;
import com.johnsproject.jpge2.dto.Camera;
import com.johnsproject.jpge2.dto.Face;
import com.johnsproject.jpge2.dto.FrameBuffer;
import com.johnsproject.jpge2.dto.Model;
import com.johnsproject.jpge2.dto.Scene;
import com.johnsproject.jpge2.dto.Vertex;
import com.johnsproject.jpge2.primitive.FPMatrix;
import com.johnsproject.jpge2.primitive.FPVector;
import com.johnsproject.jpge2.processor.GraphicsProcessor;
import com.johnsproject.jpge2.shader.DirectionalLightShadowShader;
import com.johnsproject.jpge2.shader.FlatSpecularShader;
import com.johnsproject.jpge2.shader.ForwardDataBuffer;
import com.johnsproject.jpge2.shader.GouraudSpecularShader;
import com.johnsproject.jpge2.shader.OutlineShader;
import com.johnsproject.jpge2.shader.PhongSpecularShader;
import com.johnsproject.jpge2.shader.Shader;
import com.johnsproject.jpge2.shader.ShaderDataBuffer;
import com.johnsproject.jpge2.shader.SpotLightShadowShader;

public class GraphicsController implements EngineListener {
	
	private final FPVector location0Cache;
	private final FPVector location1Cache;
	private final FPVector location2Cache;
	private final FPVector normal0Cache;
	private final FPVector normal1Cache;
	private final FPVector normal2Cache;
	private final FPVector normal3Cache;
	
	private final FPMatrix modelMatrix;
	private final FPMatrix normalMatrix;
	
	private final Engine engine;
	
	private final List<Shader> shaders;
	private ShaderDataBuffer shaderDataBuffer;
	private FrameBuffer frameBuffer;
	
	private int preShadersCount;
	private int shadersCount;
	private int postShadersCount;
	
	GraphicsController(Engine engine) {
		this.engine = engine;
		this.location0Cache = new FPVector();
		this.location1Cache = new FPVector();
		this.location2Cache = new FPVector();
		this.normal0Cache = new FPVector();
		this.normal1Cache = new FPVector();
		this.normal2Cache = new FPVector();
		this.normal3Cache = new FPVector();
		
		this.modelMatrix = new FPMatrix();
		this.normalMatrix = new FPMatrix();
		
		this.shaderDataBuffer = new ForwardDataBuffer();
		shaders = new ArrayList<Shader>();
		engine.addEngineListener(this);
		
//		addPreprocessingShader(new SpotLightShadowShader());
//		addPreprocessingShader(new DirectionalLightShadowShader());
		addShader(new FlatSpecularShader());
//		addPostprocessingShader(new OutlineShader(processor));
	}
	
	public void start() { }
	
	public void update() {
		Scene scene = engine.getScene();
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
					FPMatrix.MATRIX_IDENTITY.copy(modelMatrix);
					FPMatrix.MATRIX_IDENTITY.copy(normalMatrix);
					GraphicsProcessor.getModelMatrix(model.getTransform(), modelMatrix);
					GraphicsProcessor.getNormalMatrix(model.getTransform(), normalMatrix);
					for (int f = 0; f < model.getFaces().length; f++) {
						Face face = model.getFace(f);
						if ((face.getMaterial().getShaderIndex() == s - preShadersCount)
								| (s < preShadersCount) | (s > preShadersCount + shadersCount)) {
							backup(face);
							for (int v = 0; v < face.getVertices().length; v++) {
								Vertex vertex = face.getVertices()[v];
								vertex.getLocation().multiply(modelMatrix);
								vertex.getNormal().multiply(normalMatrix);
								shader.vertex(v, vertex);
							}
							face.getNormal().multiply(normalMatrix);
							shader.geometry(face);
							restore(face);
						}
					}
				}
			}
		}
	}
	
	private void backup(Face face) {
		face.getVertex(0).getLocation().copy(location0Cache);
		face.getVertex(1).getLocation().copy(location1Cache);
		face.getVertex(2).getLocation().copy(location2Cache);
		face.getVertex(0).getNormal().copy(normal0Cache);
		face.getVertex(1).getNormal().copy(normal1Cache);
		face.getVertex(2).getNormal().copy(normal2Cache);
		face.getNormal().copy(normal3Cache);
	}
	
	private void restore(Face face) {
		location0Cache.copy(face.getVertex(0).getLocation());
		location1Cache.copy(face.getVertex(1).getLocation());
		location2Cache.copy(face.getVertex(2).getLocation());
		normal0Cache.copy(face.getVertex(0).getNormal());
		normal1Cache.copy(face.getVertex(1).getNormal());
		normal2Cache.copy(face.getVertex(2).getNormal());
		normal3Cache.copy(face.getNormal());
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
	
	public List<Shader> getShaders() {
		return shaders;
	}
	
	public Shader getShader(int index) {
		return shaders.get(index);
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
		shaders.remove(shader);
		sortShaders(0);
	}
	
	public void removeShader(Shader shader) {
		shadersCount--;
		shaders.remove(shader);
		sortShaders(1);
	}
	
	public void removePostprocessingShader(Shader shader) {
		postShadersCount--;
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
		for (int i = 0; i < shaders.size() - 1; i++) {
			int min_i = i;
			for (int j = i + 1; j < shaders.size(); j++) {
				int currentPass = 0;
				if(min_i > preShadersCount) currentPass = 1;
				if(min_i > shadersCount) currentPass = 2;
				if (pass < currentPass) {
					min_i = j;
				}
			}
			Shader temp = shaders.get(min_i);
			shaders.set(min_i, shaders.get(i));
			shaders.set(i, temp);
		}
}
}
