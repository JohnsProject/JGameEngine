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
import com.johnsproject.jgameengine.event.EngineKeyListener;
import com.johnsproject.jgameengine.event.EngineListener;
import com.johnsproject.jgameengine.importer.SceneImporter;
import com.johnsproject.jgameengine.library.FileLibrary;
import com.johnsproject.jgameengine.library.MathLibrary;
import com.johnsproject.jgameengine.library.VectorLibrary;
import com.johnsproject.jgameengine.model.FrameBuffer;
import com.johnsproject.jgameengine.model.Scene;
import com.johnsproject.jgameengine.model.ShaderProperties;
import com.johnsproject.jgameengine.model.Texture;
import com.johnsproject.jgameengine.model.Transform;
import com.johnsproject.jgameengine.shader.EarlyDepthBufferShader;
import com.johnsproject.jgameengine.shader.FlatSpecularShader;
import com.johnsproject.jgameengine.shader.GouraudSpecularShader;
import com.johnsproject.jgameengine.shader.PhongSpecularShader;

public class EngineTest implements EngineListener, EngineKeyListener, MouseMotionListener {

	private int WINDOW_W;
	private int WINDOW_H;
	private int RENDER_W;
	private int RENDER_H;
	
	private final int[] cache;
	private Transform cameraTransform;
	
	private VectorLibrary vectorLibrary;
	private GraphicsEngine graphicsEngine;
	private InputEngine inputEngine;
	
	public static void main(String[] args) {
		new EngineTest();
	}
	
	EngineTest() {
//		GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
//		WINDOW_W = gd.getDisplayMode().getWidth();
//		WINDOW_H = gd.getDisplayMode().getHeight();
		WINDOW_W = 1024;
		WINDOW_H = 768;
		RENDER_W = (WINDOW_W * 100) / 100;
		RENDER_H = (WINDOW_H * 100) / 100;
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
		EngineStatistics stats = new EngineStatistics();
		graphicsEngine = new GraphicsEngine(loadScene(), frameBuffer);
		inputEngine = new InputEngine();
		inputEngine.addMouseMotionListener(this);
		inputEngine.addEngineKeyListener(this);
		cameraTransform = graphicsEngine.getScene().getCamera(0).getTransform();
		Engine.getInstance().addEngineListener(graphicsEngine);
		Engine.getInstance().addEngineListener(inputEngine);
		Engine.getInstance().addEngineListener(window);
		Engine.getInstance().addEngineListener(stats);
		graphicsEngine.getPreprocessingShaders().clear();
//		Engine.getInstance().limitUpdateRate(true);
//		graphicsEngine.removeShader(0);
//		graphicsEngine.addShader(new FlatSpecularShader());
//		graphicsEngine.addPreprocessingShader(new EarlyDepthBufferShader());
//		graphicsEngine.addShader(new PhongSpecularShader());
	}
	
	private Scene loadScene() {
		try {
			Scene scene = new SceneImporter().load("C:/Development/test.scene");
			Texture texture = new Texture(new FileLibrary().loadImage("C:/Development/JohnsProject.png"));
			for (int i = 0; i < scene.getModels().size(); i++) {
				((ShaderProperties)scene.getModel(i).getMesh().getMaterial(0).getProperties()).setTexture(texture);
			}
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
		int[] rotation = cameraTransform.getRotation();
		rotation[2] = -((e.getX() - (WINDOW_W >> 1)) >> 1) << MathLibrary.FP_BITS;
		rotation[0] = -(((e.getY() - (WINDOW_H >> 1)) >> 1) - 90) << MathLibrary.FP_BITS;
	}

	public void mouseMoved(MouseEvent e) {
	
	}

	public void keyTyped(KeyEvent e) {
		
	}

	
	public void keyPressed(KeyEvent e) {
		if(e.getKeyCode() == KeyEvent.VK_SHIFT) {
			speed = MathLibrary.FP_ONE / 4;
		}
		if(e.getKeyCode() == KeyEvent.VK_P) {
			Engine.getInstance().stop();
		}
	}

	public void keyReleased(KeyEvent e) {
		if(e.getKeyCode() == KeyEvent.VK_SHIFT) {
			speed = startSpeed;
		}if(e.getKeyCode() == KeyEvent.VK_P) {
			Engine.getInstance().start();
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
