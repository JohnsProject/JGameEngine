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
import com.johnsproject.jgameengine.event.EngineEvent;
import com.johnsproject.jgameengine.event.EngineKeyListener;
import com.johnsproject.jgameengine.event.EngineListener;
import com.johnsproject.jgameengine.importer.SceneImporter;
import com.johnsproject.jgameengine.library.FileLibrary;
import com.johnsproject.jgameengine.library.MathLibrary;
import com.johnsproject.jgameengine.library.VectorLibrary;
import com.johnsproject.jgameengine.model.Camera;
import com.johnsproject.jgameengine.model.FrameBuffer;
import com.johnsproject.jgameengine.model.Material;
import com.johnsproject.jgameengine.model.Model;
import com.johnsproject.jgameengine.model.Scene;
import com.johnsproject.jgameengine.model.Texture;
import com.johnsproject.jgameengine.model.Transform;
import com.johnsproject.jgameengine.shader.FlatSpecularShader;
import com.johnsproject.jgameengine.shader.GouraudSpecularShader;
import com.johnsproject.jgameengine.shader.PhongSpecularShader;
import com.johnsproject.jgameengine.shader.SpecularProperties;

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
	private PhysicsEngine physicsEngine;
	
	public static void main(String[] args) {
		new EngineTest();
	}
	
	EngineTest() {
		Engine.getInstance().setScene(loadScene());
//		GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
//		WINDOW_W = gd.getDisplayMode().getWidth();
//		WINDOW_H = gd.getDisplayMode().getHeight();
		WINDOW_W = 1024;
		WINDOW_H = 768;
		RENDER_W = (WINDOW_W * 100) / 100;
		RENDER_H = (WINDOW_H * 100) / 100;
		this.vectorLibrary = new VectorLibrary();
		cache = VectorLibrary.generate();
		FrameBuffer frameBuffer = new FrameBuffer(RENDER_W, RENDER_H);
		EngineWindow window = new EngineWindow(frameBuffer);
		EngineStatistics stats = new EngineStatistics(window);
		graphicsEngine = new GraphicsEngine(frameBuffer);
		inputEngine = new InputEngine();
		physicsEngine = new PhysicsEngine();
		window.setSize(WINDOW_W, WINDOW_H);
//		window.setFullscreen(true);
//		window.setBorders(false);
		inputEngine.addMouseMotionListener(this);
		inputEngine.addEngineKeyListener(this);
		cameraTransform = Engine.getInstance().getScene().getMainCamera().getTransform();
//		graphicsEngine.getPreprocessingShaders().clear();
//		Engine.getInstance().limitUpdateRate(true);
		Engine.getInstance().addEngineListener(this);
		Engine.getInstance().addEngineListener(graphicsEngine);
		Engine.getInstance().addEngineListener(inputEngine);
//		Engine.getInstance().addEngineListener(physicsEngine);
		Engine.getInstance().addEngineListener(window);
		Engine.getInstance().addEngineListener(stats);
		Engine.getInstance().start();
	}
	
	public void start(EngineEvent e) {
		
	}
	
	private Scene loadScene() {
		try {
			Scene scene = new SceneImporter().load("E:/Development/Blender/Test.scene");
			Texture texture = new Texture(new FileLibrary().loadImage("E:/Development/Blender/JohnsProject.png"));
			for (int i = 0; i < scene.getModels().size(); i++) {
				Model model = scene.getModel(i);
//				model.getRigidBody().setKinematic(true);
				if(model.getName().equals("Ground")) {
//					model.getRigidBody().setKinematic(true);
//					model.getRigidBody().setTorque(0, 0, 10); 
//					model.getRigidBody().addLinearVelocity(1024, 0, 0);
//					model.getRigidBody().setAngularVelocity(0, 0, 1024);
				}
				for (int j = 0; j < model.getMesh().getMaterials().length; j++) {
					Material material = model.getMesh().getMaterial(j);
					SpecularProperties properties = (SpecularProperties)material.getShader().getProperties();
					//material.setShader(new FlatSpecularShader());
					//material.setShader(new PhongSpecularShader());
					material.getShader().setProperties(properties);
					properties.setTexture(texture);
				}
				model.getArmature().playAnimation("Walk", true);
			}
			return scene;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new Scene();
	}

	public void update(EngineEvent e) {
		
	}
	
	public void fixedUpdate(EngineEvent e) {
		for (int i = 0; i < e.getScene().getModels().size(); i++) {
			Model model = e.getScene().getModel(i);
			if(model.getName().equals("Ground")) {
//				model.getRigidBody().setTorque(0, 0, 10);
//				model.getRigidBody().setAngularVelocity(0, 0, 1024);
//				model.getRigidBody().setLinearVelocity(1024, 0, 0);
//				model.getTransform().rotate(0, 0, 1024);
//				model.getTransform().setLocation(1024 * 4, 0, 0);
			}
		}
	}

	public int getLayer() {
		return DEFAULT_LAYER;
	}

	public void mouseDragged(MouseEvent e) {
		int[] rotation = cameraTransform.getRotation();
		rotation[2] = -((e.getX() - (WINDOW_W >> 1)) >> 1) << MathLibrary.FP_BIT;
		rotation[0] = -(((e.getY() - (WINDOW_H >> 1)) >> 1) - 90) << MathLibrary.FP_BIT;
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

	int startSpeed = MathLibrary.FP_ONE * 10;
	int speed = startSpeed;
	public void keyDown(KeyEvent e) {
		for (int i = 0; i < Engine.getInstance().getScene().getModels().size(); i++) {
			Model model = Engine.getInstance().getScene().getModel(i);
			if(model.getName().equals("Ground")) {
				model.getRigidBody().addForce(0, 0, 10024);
//				model.getRigidBody().setTorque(0, 0, 10);
//				model.getRigidBody().setAngularVelocity(0, 0, 1024);
//				model.getRigidBody().setLinearVelocity(1024, 0, 0);
//				model.getTransform().rotate(0, 0, 1024);
//				model.getTransform().setLocation(1024 * 4, 0, 0);
			}
		}
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
