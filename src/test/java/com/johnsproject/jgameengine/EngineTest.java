package com.johnsproject.jgameengine;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.io.IOException;

import com.johnsproject.jgameengine.Engine;
import com.johnsproject.jgameengine.EngineStatistics;
import com.johnsproject.jgameengine.EngineWindow;
import com.johnsproject.jgameengine.GraphicsEngine;
import com.johnsproject.jgameengine.InputEngine;
import com.johnsproject.jgameengine.dto.FrameBuffer;
import com.johnsproject.jgameengine.dto.Scene;
import com.johnsproject.jgameengine.dto.ShaderProperties;
import com.johnsproject.jgameengine.dto.Texture;
import com.johnsproject.jgameengine.dto.Transform;
import com.johnsproject.jgameengine.event.EngineKeyListener;
import com.johnsproject.jgameengine.event.EngineListener;
import com.johnsproject.jgameengine.importer.SceneImporter;
import com.johnsproject.jgameengine.library.FileLibrary;
import com.johnsproject.jgameengine.library.MathLibrary;
import com.johnsproject.jgameengine.library.VectorLibrary;
import com.johnsproject.jgameengine.shader.shaders.FlatSpecularShader;
import com.johnsproject.jgameengine.shader.shaders.GouraudSpecularShader;
import com.johnsproject.jgameengine.shader.shaders.PhongSpecularShader;

public class EngineTest implements EngineListener, EngineKeyListener, MouseMotionListener {

	private static final int WINDOW_W = 1024;
	private static final int WINDOW_H = 768;
	private static final int RENDER_W = (WINDOW_W * 100) / 100;
	private static final int RENDER_H = (WINDOW_H * 100) / 100;
	
	private final int[] cache;
	private Transform cameraTransform;
	
	private VectorLibrary vectorLibrary;
	private GraphicsEngine graphicsEngine;
	private InputEngine inputEngine;
	
	public static void main(String[] args) {
		new EngineTest();
	}
	
	EngineTest() {
		this.vectorLibrary = new VectorLibrary();
		cache = vectorLibrary.generate();
		Engine.getInstance().addEngineListener(this);
		Engine.getInstance().start();
	}
	
	public void start() {
		FrameBuffer frameBuffer = new FrameBuffer(RENDER_W, RENDER_H);
		EngineWindow window = new EngineWindow(frameBuffer);
		window.setSize(WINDOW_W, WINDOW_H);
//		window.setFullscreen(true);
//		window.setBorders(false);
		EngineStatistics stats = new EngineStatistics(frameBuffer);
		graphicsEngine = new GraphicsEngine(loadScene(), frameBuffer);
		inputEngine = new InputEngine();
		inputEngine.addMouseMotionListener(this);
		inputEngine.addEngineKeyListener(this);
		cameraTransform = graphicsEngine.getScene().getCamera(0).getTransform();
		Engine.getInstance().addEngineListener(graphicsEngine);
		Engine.getInstance().addEngineListener(inputEngine);
		Engine.getInstance().addEngineListener(window);
		Engine.getInstance().addEngineListener(stats);
		for (int i = graphicsEngine.getPreprocessingShadersCount(); i > 0; i--) {
			graphicsEngine.removePreprocessingShader(graphicsEngine.getPreprocessingShader(i));
		}
		graphicsEngine.removeShader(graphicsEngine.getShader(0));
		graphicsEngine.addShader(new PhongSpecularShader());
	}
	
	private Scene loadScene() {
		try {
			Scene scene = new SceneImporter().load("C:/Development/test.scene");
			Texture texture = new Texture(new FileLibrary().loadImage("C:/Development/JohnsProject.png"));
			((ShaderProperties)scene.getModel("Ground").getMesh().getMaterial(0).getProperties()).setTexture(texture);
			return scene;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new Scene();
	}

	public void update() {
		
	}
	
	public void fixedUpdate() {
		
	}

	public int getPriority() {
		return 0;
	}

	public void mouseDragged(MouseEvent e) {
		
	}

	public void mouseMoved(MouseEvent e) {
		int[] rotation = cameraTransform.getRotation();
//		rotation[2] = -((e.getX() - (WINDOW_W >> 1)) >> 1) << MathLibrary.FP_BITS;
//		rotation[0] = -(((e.getY() - (WINDOW_H >> 1)) >> 1) - 90) << MathLibrary.FP_BITS;
	}

	public void keyTyped(KeyEvent e) {
		
	}

	
	public void keyPressed(KeyEvent e) {
		if(e.getKeyCode() == KeyEvent.VK_SHIFT) {
			speed = MathLibrary.FP_ONE / 4;
		}
	}

	public void keyReleased(KeyEvent e) {
		if(e.getKeyCode() == KeyEvent.VK_SHIFT) {
			speed = startSpeed;
		}
	}

	int startSpeed = MathLibrary.FP_ONE * 2;
	int speed = startSpeed;
	public void keyDown(KeyEvent e) {
		if(e.getKeyCode() == KeyEvent.VK_W) {
			vectorLibrary.rotateXYZ(VectorLibrary.VECTOR_DOWN, cameraTransform.getRotation(), cache);
			vectorLibrary.divide(cache, speed, cache);
			cameraTransform.translate(cache);
		}
		if(e.getKeyCode() == KeyEvent.VK_A) {
			vectorLibrary.rotateXYZ(VectorLibrary.VECTOR_LEFT, cameraTransform.getRotation(), cache);
			vectorLibrary.divide(cache, speed, cache);
			cameraTransform.translate(cache);
		}
		if(e.getKeyCode() == KeyEvent.VK_D) {
			vectorLibrary.rotateXYZ(VectorLibrary.VECTOR_RIGHT, cameraTransform.getRotation(), cache);
			vectorLibrary.divide(cache, speed, cache);
			cameraTransform.translate(cache);
		}
		if(e.getKeyCode() == KeyEvent.VK_S) {
			vectorLibrary.rotateXYZ(VectorLibrary.VECTOR_UP, cameraTransform.getRotation(), cache);
			vectorLibrary.divide(cache, speed, cache);
			cameraTransform.translate(cache);
		}
	}
}
