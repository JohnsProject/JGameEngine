package com.johnsproject.jgameengine;

import static com.johnsproject.jgameengine.util.FixedPointUtils.*;

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
import com.johnsproject.jgameengine.io.SceneImporter;
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
import com.johnsproject.jgameengine.util.FileUtils;
import com.johnsproject.jgameengine.util.FixedPointUtils;
import com.johnsproject.jgameengine.util.MatrixUtils;
import com.johnsproject.jgameengine.util.TransformationUtils;
import com.johnsproject.jgameengine.util.VectorUtils;

public class EngineRuntimeTest implements EngineListener, EngineKeyListener, MouseMotionListener {

	private int WINDOW_W;
	private int WINDOW_H;
	private int RENDER_W;
	private int RENDER_H;
	
	private final int[] cache;
	private Transform cameraTransform;

	private GraphicsEngine graphicsEngine;
	private InputEngine inputEngine;
	private PhysicsEngine physicsEngine;
	
	public static void main(String[] args) {
		new EngineRuntimeTest();
	}
	
	EngineRuntimeTest() {
		Engine.getInstance().setScene(loadScene());
//		GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
//		WINDOW_W = gd.getDisplayMode().getWidth();
//		WINDOW_H = gd.getDisplayMode().getHeight();
		WINDOW_W = 1024;
		WINDOW_H = 768;
		RENDER_W = (WINDOW_W * 100) / 100;
		RENDER_H = (WINDOW_H * 100) / 100;
		cache = VectorUtils.emptyVector();
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
			Scene scene = SceneImporter.load("C:/Development/JGameEngineTests/Test.scene");
			//scene.getMainCamera().getTransform().setLocation(0, 0, FP_ONE * 10);
			//scene.getMainCamera().getTransform().setRotation(0, 0, 0);
			Texture texture = new Texture(FileUtils.loadImage("C:/Development/JGameEngineTests/JohnsProject.png"));
			for (int m = 0; m < scene.getModels().size(); m++) {
				Model model = scene.getModels().get(m);
//				model.getRigidBody().setKinematic(true);
				if(model.getName().equals("Ground")) {
//					model.getRigidBody().setKinematic(true);
//					model.getRigidBody().setTorque(0, 0, 10); 
//					model.getRigidBody().addLinearVelocity(1024, 0, 0);
//					model.getRigidBody().setAngularVelocity(0, 0, 1024);
				}
				for (int j = 0; j < model.getMesh().getMaterials().length; j++) {
					Material material = model.getMesh().getMaterial(j);
					//material.setShader(new FlatSpecularShader());
					//material.setShader(new PhongSpecularShader());
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
		VectorUtils.copy(cache, VectorUtils.VECTOR_ZERO);
		if(e.getKeyCode() == KeyEvent.VK_W) {
			VectorUtils.copy(cache, VectorUtils.VECTOR_FORWARD);
		}
		if(e.getKeyCode() == KeyEvent.VK_A) {
			VectorUtils.copy(cache, VectorUtils.VECTOR_LEFT);
		}
		if(e.getKeyCode() == KeyEvent.VK_D) {
			VectorUtils.copy(cache, VectorUtils.VECTOR_RIGHT);
		}
		if(e.getKeyCode() == KeyEvent.VK_S) {
			VectorUtils.copy(cache, VectorUtils.VECTOR_BACK);
		}
		if(e.getKeyCode() == KeyEvent.VK_E) {
			VectorUtils.copy(cache, VectorUtils.VECTOR_UP);
		}
		if(e.getKeyCode() == KeyEvent.VK_Y) {
			VectorUtils.copy(cache, VectorUtils.VECTOR_DOWN);
		}
		if(!VectorUtils.equals(cache, VectorUtils.VECTOR_ZERO)) {
			TransformationUtils.rotateX(cache, cameraTransform.getRotation()[VectorUtils.VECTOR_X]);
			TransformationUtils.rotateY(cache, cameraTransform.getRotation()[VectorUtils.VECTOR_Y]);
			TransformationUtils.rotateZ(cache, cameraTransform.getRotation()[VectorUtils.VECTOR_Z]);
			VectorUtils.multiply(cache, speed);
			cameraTransform.translate(cache);
		}
	}
}
