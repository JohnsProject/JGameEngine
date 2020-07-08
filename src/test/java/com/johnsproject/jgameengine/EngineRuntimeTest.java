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
import com.johnsproject.jgameengine.shading.FlatSpecularShader;
import com.johnsproject.jgameengine.shading.ForwardShaderBuffer;
import com.johnsproject.jgameengine.shading.GouraudSpecularShader;
import com.johnsproject.jgameengine.shading.PhongSpecularShader;
import com.johnsproject.jgameengine.util.FileUtils;
import com.johnsproject.jgameengine.util.FixedPointUtils;
import com.johnsproject.jgameengine.util.MatrixUtils;
import com.johnsproject.jgameengine.util.TransformationUtils;
import com.johnsproject.jgameengine.util.VectorUtils;

@SuppressWarnings("unused")
public class EngineRuntimeTest implements EngineListener, EngineKeyListener, MouseMotionListener {

	private static final boolean SHOW_ENGINE_STATISTICS = true;
	private static final boolean SHOW_DIRECTIONAL_LIGHT_SHADOW_MAP = false;
	private static final boolean SHOW_SPOT_LIGHT_SHADOW_MAP = false;
	private static final boolean SHOW_POINT_LIGHT_SHADOW_MAP = false;
	
	private static final int WINDOW_W = 1024;
	private static final int WINDOW_H = 768;
	private static final int RENDER_W = (WINDOW_W * 100) / 100;
	private static final int RENDER_H = (WINDOW_H * 100) / 100;
	
	private static final int CAMERA_TRANSLATION_SPEED = FP_ONE / 10;

	private final FrameBuffer frameBuffer;
	private final EngineWindow window;
	private final GraphicsEngine graphicsEngine;
	private final InputEngine inputEngine;
	private final PhysicsEngine physicsEngine;
	private final Scene scene;
	
	private Transform cameraTransform;
	private int[] cameraTranslation;
	private int cameraTranslationSpeed;
	
	public static void main(String[] args) {
		new EngineRuntimeTest();
	}
	
	EngineRuntimeTest() {
		frameBuffer = new FrameBuffer(RENDER_W, RENDER_H);
		window = new EngineWindow(frameBuffer);
		graphicsEngine = new GraphicsEngine(frameBuffer);
		inputEngine = new InputEngine();
		physicsEngine = new PhysicsEngine();
		scene = loadScene();
		Engine.getInstance().addEngineListener(this);
		Engine.getInstance().addEngineListener(graphicsEngine);
		Engine.getInstance().addEngineListener(inputEngine);
//		Engine.getInstance().addEngineListener(physicsEngine);
		Engine.getInstance().addEngineListener(window);
		if(SHOW_ENGINE_STATISTICS) {
			final EngineStatistics stats = new EngineStatistics(window);
			Engine.getInstance().addEngineListener(stats);
		}
		Engine.getInstance().start();	
	}
	
	public void initialize(EngineEvent e) {
		window.setSize(WINDOW_W, WINDOW_H);
		inputEngine.addMouseMotionListener(this);
		inputEngine.addEngineKeyListener(this);
		Engine.getInstance().setScene(scene);
		cameraTransform = scene.getMainCamera().getTransform();
		cameraTranslation = VectorUtils.emptyVector();
		cameraTranslationSpeed = CAMERA_TRANSLATION_SPEED;
//		graphicsEngine.setDefaultShader(graphicsEngine.getShader(1)); // FlatSpecularShader
//		graphicsEngine.setDefaultShader(graphicsEngine.getShader(3)); // PhongSpecularShader
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
					//material.setShader(graphicsEngine.getShader(1)); // FlatSpecularShader
					//material.setShader(graphicsEngine.getShader(3)); // PhongSpecularShader
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

	public void dynamicUpdate(EngineEvent e) {		
		final ForwardShaderBuffer shaderBuffer = (ForwardShaderBuffer) graphicsEngine.getShaderBuffer();
		if(SHOW_DIRECTIONAL_LIGHT_SHADOW_MAP || SHOW_SPOT_LIGHT_SHADOW_MAP) {
			Texture shadowMap = null;
			if(SHOW_DIRECTIONAL_LIGHT_SHADOW_MAP) {
				shadowMap = shaderBuffer.getDirectionalShadowMap();
			}
			else if(SHOW_SPOT_LIGHT_SHADOW_MAP) {
				shadowMap = shaderBuffer.getSpotShadowMap();
			}
			for (int y = 0; y < shadowMap.getHeight(); y++) {
				for (int x = 0; x < shadowMap.getWidth(); x++) {
					int depth = shadowMap.getPixel(x, y) >> 1;
					int color = com.johnsproject.jgameengine.util.ColorUtils.toColor(depth, depth, depth);
					if(shaderBuffer.getCamera() != null)
						shaderBuffer.getCamera().getRenderTarget().getColorBuffer().setPixel(x, y, color);		
				}
			}
		}
		else if(SHOW_POINT_LIGHT_SHADOW_MAP) {
			final Texture[] shadowMaps = shaderBuffer.getPointShadowMaps();
			for (int i = 0; i < shadowMaps.length; i++) {
				final Texture shadowMap = shadowMaps[i];
				for (int y = 0; y < shadowMap.getHeight(); y++) {
					for (int x = 0; x < shadowMap.getWidth(); x++) {
						int frameBufferX = x + (shadowMap.getWidth() * i);
						int frameBufferY = y;
						if(i >= 3) {
							frameBufferX = x + (shadowMap.getWidth() * (i - 3));
							frameBufferY += shadowMap.getHeight();
						}
						int depth = shadowMap.getPixel(x, y) >> 1;
						int color = com.johnsproject.jgameengine.util.ColorUtils.toColor(depth, depth, depth);
						shaderBuffer.getCamera().getRenderTarget().getColorBuffer().setPixel(frameBufferX, frameBufferY, color);		
					}
				}
			}
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
		if(SHOW_DIRECTIONAL_LIGHT_SHADOW_MAP || SHOW_SPOT_LIGHT_SHADOW_MAP || SHOW_POINT_LIGHT_SHADOW_MAP) {
			return GRAPHICS_ENGINE_LAYER + 1;
		}
		return DEFAULT_LAYER;
	}

	public void mouseDragged(MouseEvent e) {
		final int mouseX = (int)inputEngine.getMouseLocation().getX();
		final int mouseY = (int)inputEngine.getMouseLocation().getY();
		final int x = -((mouseY - (WINDOW_H >> 1)) >> 1) << FP_BIT;
		final int y = -((mouseX - (WINDOW_W >> 1)) >> 1) << FP_BIT;
		cameraTransform.setRotation(x, y, 0);
	}

	public void mouseMoved(MouseEvent e) {
	
	}

	public void keyTyped(KeyEvent e) {
		
	}

	public void keyPressed(KeyEvent e) {
		if(e.getKeyCode() == KeyEvent.VK_SHIFT) {
			cameraTranslationSpeed = CAMERA_TRANSLATION_SPEED * 2;
		}
		if(e.getKeyCode() == KeyEvent.VK_P) {
			Engine.getInstance().stop();
		}
	}

	public void keyReleased(KeyEvent e) {
		if(e.getKeyCode() == KeyEvent.VK_SHIFT) {
			cameraTranslationSpeed = CAMERA_TRANSLATION_SPEED / 2;
		}
		if(e.getKeyCode() == KeyEvent.VK_P) {
			Engine.getInstance().start();
		}
	}
	
	public void keyHold(KeyEvent e) {
		VectorUtils.copy(cameraTranslation, VectorUtils.VECTOR_ZERO);
		if(e.getKeyCode() == KeyEvent.VK_W) {
			VectorUtils.copy(cameraTranslation, VectorUtils.VECTOR_FORWARD);
		}
		if(e.getKeyCode() == KeyEvent.VK_A) {
			VectorUtils.copy(cameraTranslation, VectorUtils.VECTOR_LEFT);
		}
		if(e.getKeyCode() == KeyEvent.VK_D) {
			VectorUtils.copy(cameraTranslation, VectorUtils.VECTOR_RIGHT);
		}
		if(e.getKeyCode() == KeyEvent.VK_S) {
			VectorUtils.copy(cameraTranslation, VectorUtils.VECTOR_BACK);
		}
		if(e.getKeyCode() == KeyEvent.VK_E) {
			VectorUtils.copy(cameraTranslation, VectorUtils.VECTOR_UP);
		}
		if(e.getKeyCode() == KeyEvent.VK_Y) {
			VectorUtils.copy(cameraTranslation, VectorUtils.VECTOR_DOWN);
		}
		if(!VectorUtils.equals(cameraTranslation, VectorUtils.VECTOR_ZERO)) {
			VectorUtils.multiply(cameraTranslation, cameraTranslationSpeed);
			cameraTransform.translateLocal(cameraTranslation);
		}
	}
}
