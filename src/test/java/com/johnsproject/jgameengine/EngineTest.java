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
import com.johnsproject.jgameengine.model.Light;
import com.johnsproject.jgameengine.model.Material;
import com.johnsproject.jgameengine.model.Mesh;
import com.johnsproject.jgameengine.model.Model;
import com.johnsproject.jgameengine.model.Scene;
import com.johnsproject.jgameengine.model.Texture;
import com.johnsproject.jgameengine.model.Transform;
import com.johnsproject.jgameengine.model.Vertex;
import com.johnsproject.jgameengine.shader.FlatSpecularShader;
import com.johnsproject.jgameengine.shader.GouraudSpecularShader;
import com.johnsproject.jgameengine.shader.PhongSpecularShader;
import com.johnsproject.jgameengine.shader.SpecularProperties;

import static com.johnsproject.jgameengine.library.MathLibrary.*;

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
		//EngineStatistics stats = new EngineStatistics(window);
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
		//Engine.getInstance().addEngineListener(stats);
		Engine.getInstance().start();
	}
	
	public void start(EngineEvent e) {
		
	}
	
	private Scene loadScene() {
		/*
		Scene scene = new Scene();
		// Create camera
		Camera camera = new Camera("MyCamera", new Transform());
		camera.getTransform().translate(0, 0, FP_ONE * 10);
		// Create light
		Light light = new Light("MyLight", new Transform());
		light.getTransform().translate(0, FP_ONE * 5, FP_ONE * 5);
		// Create mesh components
		int[][] materials = new int[][] {
			// a, r, g, b
			{255, 150, 150, 150},
		};
		int[][] vertices = new int[][] {
			// x, y, z, w, material
			{FP_ONE, FP_ONE, FP_ONE, FP_ONE, 0},
			{-FP_ONE, FP_ONE, FP_ONE, FP_ONE, 0},
			{FP_ONE, -FP_ONE, FP_ONE, FP_ONE, 0},
			{-FP_ONE, -FP_ONE, FP_ONE, FP_ONE, 0},
			{FP_ONE, FP_ONE, -FP_ONE, FP_ONE, 0},
			{-FP_ONE, FP_ONE, -FP_ONE, FP_ONE, 0},
			{FP_ONE, -FP_ONE, -FP_ONE, FP_ONE, 0},
			{-FP_ONE, -FP_ONE, -FP_ONE, FP_ONE, 0},
		};
		int[][] faces = new int[][] {
			// vertex1, vertex2, vertex3, material
			{0, 1, 2, 0},
			{1, 3, 2, 0},
			{6, 4, 0, 0},
			{0, 2, 6, 0},
			{7, 3, 1, 0},
			{1, 5, 7, 0},
			{5, 4, 6, 0},
			{6, 7, 5, 0},
		};
		// Create mesh
		Mesh mesh = new Mesh(vertices, faces, materials);
		// Create model
		Model model = new Model("MyModel", new Transform(), mesh);
		// Add scene objects
		scene.addCamera(camera);
		scene.addLight(light);
		scene.addModel(model);
		return scene;
		*/
		
		try {
			Scene scene = new SceneImporter().load("C:/Development/JGameEngineTests/Test.scene");
			//scene.getMainCamera().getTransform().setLocation(0, 0, FP_ONE * 10);
			//scene.getMainCamera().getTransform().setRotation(0, 0, 0);
			Texture texture = new Texture(new FileLibrary().loadImage("C:/Development/JGameEngineTests/JohnsProject.png"));
			for (Model model : scene.getModels().values()) {
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
					//properties.setTexture(texture);
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
		Model model = e.getScene().getModel("MyModel");
		if(model != null) {
			//model.getTransform().rotate(e.getDeltaTime(), 0, 0);
		}
	}
	
	public void fixedUpdate(EngineEvent e) {
		Model model = e.getScene().getModel("Ground");
		if(model != null) {
			model.getRigidBody().setTorque(0, 0, 10);
	//		model.getRigidBody().setAngularVelocity(0, 0, 1024);
	//		model.getRigidBody().setLinearVelocity(1024, 0, 0);
	//		model.getTransform().rotate(0, 0, 1024);
	//		model.getTransform().setLocation(1024 * 4, 0, 0);
		}
	}

	public int getLayer() {
		return DEFAULT_LAYER;
	}

	public void mouseDragged(MouseEvent e) {
		int y = -((e.getX() - (WINDOW_W >> 1)) >> 1) << FP_BIT;
		int x = -((e.getY() - (WINDOW_H >> 1)) >> 1) << FP_BIT;
		cameraTransform.setRotation(x, y, 0);
	}

	public void mouseMoved(MouseEvent e) {
	
	}

	public void keyTyped(KeyEvent e) {
		
	}

	
	public void keyPressed(KeyEvent e) {
		if(e.getKeyCode() == KeyEvent.VK_SHIFT) {
			speed = FP_ONE / 4;
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

	int startSpeed = FP_ONE / 10;
	int speed = startSpeed;
	public void keyDown(KeyEvent e) {
		if(e.getKeyCode() == KeyEvent.VK_W) {
			vectorLibrary.rotateXYZ(VectorLibrary.VECTOR_FORWARD, cameraTransform.getRotation(), cache);
			vectorLibrary.multiply(cache, speed, cache);
			cameraTransform.translate(cache);
		}
		if(e.getKeyCode() == KeyEvent.VK_A) {
			vectorLibrary.rotateXYZ(VectorLibrary.VECTOR_LEFT, cameraTransform.getRotation(), cache);
			vectorLibrary.multiply(cache, speed, cache);
			cameraTransform.translate(cache);
		}
		if(e.getKeyCode() == KeyEvent.VK_D) {
			vectorLibrary.rotateXYZ(VectorLibrary.VECTOR_RIGHT, cameraTransform.getRotation(), cache);
			vectorLibrary.multiply(cache, speed, cache);
			cameraTransform.translate(cache);
		}
		if(e.getKeyCode() == KeyEvent.VK_S) {
			vectorLibrary.rotateXYZ(VectorLibrary.VECTOR_BACK, cameraTransform.getRotation(), cache);
			vectorLibrary.multiply(cache, speed, cache);
			cameraTransform.translate(cache);
		}
		if(e.getKeyCode() == KeyEvent.VK_E) {
			vectorLibrary.rotateXYZ(VectorLibrary.VECTOR_UP, cameraTransform.getRotation(), cache);
			vectorLibrary.multiply(cache, speed, cache);
			cameraTransform.translate(cache);
		}
		if(e.getKeyCode() == KeyEvent.VK_Y) {
			vectorLibrary.rotateXYZ(VectorLibrary.VECTOR_DOWN, cameraTransform.getRotation(), cache);
			vectorLibrary.multiply(cache, speed, cache);
			cameraTransform.translate(cache);
		}
	}
}
