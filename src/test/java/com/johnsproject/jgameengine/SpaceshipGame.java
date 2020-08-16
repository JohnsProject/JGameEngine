package com.johnsproject.jgameengine;

import static com.johnsproject.jgameengine.util.FixedPointUtils.FP_BIT;
import static com.johnsproject.jgameengine.util.FixedPointUtils.FP_HALF;
import static com.johnsproject.jgameengine.util.VectorUtils.VECTOR_X;
import static com.johnsproject.jgameengine.util.VectorUtils.VECTOR_Y;
import static com.johnsproject.jgameengine.util.VectorUtils.VECTOR_Z;

import java.awt.event.KeyEvent;
import java.io.IOException;

import com.johnsproject.jgameengine.event.EngineEvent;
import com.johnsproject.jgameengine.event.EngineListener;
import com.johnsproject.jgameengine.io.OBJImporter;
import com.johnsproject.jgameengine.model.Camera;
import com.johnsproject.jgameengine.model.FrameBuffer;
import com.johnsproject.jgameengine.model.Light;
import com.johnsproject.jgameengine.model.Mesh;
import com.johnsproject.jgameengine.model.Model;
import com.johnsproject.jgameengine.model.Scene;
import com.johnsproject.jgameengine.model.Texture;
import com.johnsproject.jgameengine.model.Transform;
import com.johnsproject.jgameengine.shading.BasicThreadedShader;
import com.johnsproject.jgameengine.shading.DirectionalLightShadowShader;
import com.johnsproject.jgameengine.shading.FlatShader;
import com.johnsproject.jgameengine.shading.GouraudShader;
import com.johnsproject.jgameengine.shading.PhongShader;
import com.johnsproject.jgameengine.util.FileUtils;
import com.johnsproject.jgameengine.util.FixedPointUtils;
import com.johnsproject.jgameengine.util.VectorUtils;

@SuppressWarnings("unused")
public class SpaceshipGame implements EngineListener {

	private static final int WINDOW_WIDTH = 1920;
	private static final int WINDOW_HEIGHT = 1080;
	
	private final FrameBuffer frameBuffer;
	private final EngineWindow window;
	private final GraphicsEngine graphicsEngine;
	private final EngineStatistics engineStats;
	private final InputEngine inputEngine = new InputEngine();
	private final Scene scene = new Scene();
	
	private Transform cameraTransform;
	private Transform lightTransform;
	private Transform spaceshipTransform;
	
	private Model spaceshipFireModel;
	private Transform spaceshipFireTransform;
	
	private int spaceshipFireScale = 2000;
	
	public static void main(String[] args) {
		int width = WINDOW_WIDTH;
		int height = WINDOW_HEIGHT;
		int scaling = 100;
		for (int i = 0; i < args.length; i++) {
			final String setting = args[i];
			if(setting.contains("window")) {
				final String[] resolution = setting.replace("window", "").split("x");
				width = Integer.parseInt(resolution[0]);
				height = Integer.parseInt(resolution[1]);
			}
			else if(setting.contains("render")) {
				scaling = Integer.parseInt(setting.replace("render", ""));
			}
		}
		new SpaceshipGame(width, height, scaling);
	}

	public SpaceshipGame(int width, int height, int scaling) {		
		frameBuffer = new FrameBuffer((width * scaling) / 100, (height * scaling) / 100);
		window = new EngineWindow(frameBuffer);
		graphicsEngine = new GraphicsEngine(frameBuffer);
		engineStats = new EngineStatistics(window);
		window.setSize(width, height);
		
		Engine.getInstance().setScene(scene);
		Engine.getInstance().addEngineListener(this);
		Engine.getInstance().addEngineListener(graphicsEngine);
		Engine.getInstance().addEngineListener(inputEngine);
		Engine.getInstance().addEngineListener(window);
		Engine.getInstance().addEngineListener(engineStats);
		Engine.getInstance().start();
	}

	public void initialize(EngineEvent e) {
		window.setTitle("Spaceship Game");
		
		// remove unused shaders to save performance
		graphicsEngine.getShaders().clear();
		graphicsEngine.addShader(new DirectionalLightShadowShader());
		graphicsEngine.addShader(new GouraudShader());
		graphicsEngine.setDefaultShader(graphicsEngine.getShader(1));
		try {
			final ClassLoader classLoader = this.getClass().getClassLoader();
			loadTerrain(classLoader);
			loadSpaceshipFire(classLoader);
			loadSpaceship(classLoader);
			createCamera();
			createLight();
		} catch (IOException ioE) {
			ioE.printStackTrace();
		}
		moveCameraToSpaceship();
	}
	
	private void loadTerrain(ClassLoader classLoader) throws IOException {
		final Model terrainModel = OBJImporter.parseResource(classLoader, "SpaceshipGame/Terrain.obj");
		scene.addModel(terrainModel);
		
		terrainModel.getTransform().setScale(3 << FP_BIT, 3 << FP_BIT, 3 << FP_BIT);
		
		final Texture texture = new Texture(FileUtils.loadImage(this.getClass().getResourceAsStream("/JohnsProjectLogo.png")));
		terrainModel.getMesh().getMaterial(0).setTexture(texture);
	}
	
	private void loadSpaceshipFire(ClassLoader classLoader) throws IOException {
		spaceshipFireModel = OBJImporter.parseResource(classLoader, "SpaceshipGame/SpaceshipFire.obj");
		scene.addModel(spaceshipFireModel);
		
		spaceshipFireTransform = spaceshipFireModel.getTransform();
		spaceshipFireTransform.setScale(FP_HALF, FP_HALF, FP_HALF);
		
		// the basic shader has no illumination so it will have a glow like effect
		final BasicThreadedShader basicShader = new BasicThreadedShader();
		graphicsEngine.addShader(basicShader);
		spaceshipFireModel.getMesh().getMaterial(0).setShader(basicShader);
	}
	
	private void loadSpaceship(ClassLoader classLoader) throws IOException {
		final Model spaceshipModel = OBJImporter.parseResource(classLoader, "SpaceshipGame/Spaceship.obj");
		scene.addModel(spaceshipModel);
		
		spaceshipTransform = spaceshipModel.getTransform();
		spaceshipTransform.worldTranslate(0, 5 << FP_BIT, 0);
		spaceshipTransform.setScale(FP_HALF, FP_HALF, FP_HALF);
	}
	
	private void createCamera() {
		cameraTransform = new Transform();
		cameraTransform.worldRotate(-45 << FP_BIT, 0, 0);
		final Camera camera = new Camera("Camera", cameraTransform);
		scene.addCamera(camera);
	}
	
	private void createLight() {
		lightTransform = new Transform();
		lightTransform.worldRotate(-60 << FP_BIT, 25 << FP_BIT, 0);
		final Light directionalLight = new Light("DirectionalLight", lightTransform);
		scene.addLight(directionalLight);
	}
	
	public void fixedUpdate(EngineEvent e) {
		spaceshipFireScale = -spaceshipFireScale;
		spaceshipFireTransform.worldScale(spaceshipFireScale, spaceshipFireScale, spaceshipFireScale);
	}

	public void dynamicUpdate(EngineEvent e) {
		final boolean isAnyMoveKeyPressed = inputEngine.isKeyPressed(KeyEvent.VK_W) || inputEngine.isKeyPressed(KeyEvent.VK_S)
				|| inputEngine.isKeyPressed(KeyEvent.VK_A) || inputEngine.isKeyPressed(KeyEvent.VK_D);
		
		if(isAnyMoveKeyPressed) {
			moveSpaceshipWithKeyboard(e.getDeltaTime());
			moveFireToSpaceship();
			moveCameraToSpaceship();
			moveLightToCamera();
		}
		spaceshipFireModel.setActive(isAnyMoveKeyPressed);
	}
	
	private void moveSpaceshipWithKeyboard(int deltaTime) {
		if(inputEngine.isKeyPressed(KeyEvent.VK_W) && inputEngine.isKeyPressed(KeyEvent.VK_A)) {
			spaceshipTransform.setRotation(0, 45 << FP_BIT, 0);
		}
		else if(inputEngine.isKeyPressed(KeyEvent.VK_W) && inputEngine.isKeyPressed(KeyEvent.VK_D)) {
			spaceshipTransform.setRotation(0, -45 << FP_BIT, 0);
		}
		else if(inputEngine.isKeyPressed(KeyEvent.VK_S) && inputEngine.isKeyPressed(KeyEvent.VK_A)) {
			spaceshipTransform.setRotation(0, 135 << FP_BIT, 0);
		}
		else if(inputEngine.isKeyPressed(KeyEvent.VK_S) && inputEngine.isKeyPressed(KeyEvent.VK_D)) {
			spaceshipTransform.setRotation(0, -135 << FP_BIT, 0);
		}
		else if(inputEngine.isKeyPressed(KeyEvent.VK_W)) {
			spaceshipTransform.setRotation(0, 0, 0);
		}
		else if(inputEngine.isKeyPressed(KeyEvent.VK_S)) {
			spaceshipTransform.setRotation(0, 180 << FP_BIT, 0);
		}
		else if(inputEngine.isKeyPressed(KeyEvent.VK_A)) {
			spaceshipTransform.setRotation(0, 90 << FP_BIT, 0);
		}
		else if(inputEngine.isKeyPressed(KeyEvent.VK_D)) {
			spaceshipTransform.setRotation(0, -90 << FP_BIT, 0);
		}
		spaceshipTransform.localTranslate(0, 0, -FixedPointUtils.multiply(FP_HALF, deltaTime));
	}

	
	private void moveFireToSpaceship() {		
		spaceshipFireTransform.setLocation(0, 0, 4 << FP_BIT);
		VectorUtils.multiply(spaceshipFireTransform.getLocation(), spaceshipTransform.getSpaceExitMatrix());
		
		final int[] spaceshipRotation = spaceshipTransform.getRotation();
		spaceshipFireTransform.setRotation(spaceshipRotation[VECTOR_X], spaceshipRotation[VECTOR_Y], spaceshipRotation[VECTOR_Z]);
	}
	
	private void moveCameraToSpaceship() {
		final int[] spaceshipLocation = spaceshipTransform.getLocation();
		cameraTransform.setLocation(spaceshipLocation[VECTOR_X], spaceshipLocation[VECTOR_Y], spaceshipLocation[VECTOR_Z]);
		cameraTransform.worldTranslate(0, 20 << FP_BIT, 15 << FP_BIT);
	}
	
	private void moveLightToCamera() {
		final int[] cameraLocation = cameraTransform.getLocation();
		lightTransform.setLocation(cameraLocation[VECTOR_X], cameraLocation[VECTOR_Y], cameraLocation[VECTOR_Z]);
	}

	public int getLayer() {
		return DEFAULT_LAYER;
	}
}