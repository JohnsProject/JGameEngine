package com.johnsproject.jpge2;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;

import com.johnsproject.jpge2.controller.GraphicsController;
import com.johnsproject.jpge2.controller.InputController;
import com.johnsproject.jpge2.dto.FrameBuffer;
import com.johnsproject.jpge2.dto.Scene;
import com.johnsproject.jpge2.dto.Texture;
import com.johnsproject.jpge2.dto.Transform;
import com.johnsproject.jpge2.event.EngineKeyListener;
import com.johnsproject.jpge2.event.EngineListener;
import com.johnsproject.jpge2.importer.SceneImporter;
import com.johnsproject.jpge2.library.ColorLibrary;
import com.johnsproject.jpge2.library.FileLibrary;
import com.johnsproject.jpge2.library.MathLibrary;
import com.johnsproject.jpge2.library.VectorLibrary;
import com.johnsproject.jpge2.shader.ShaderProperties;
import com.johnsproject.jpge2.shader.shaders.DirectionalLightShadowShader;
import com.johnsproject.jpge2.shader.shaders.FlatSpecularShader;
import com.johnsproject.jpge2.shader.shaders.GouraudSpecularShader;
import com.johnsproject.jpge2.shader.shaders.SpotLightShadowShader;

public class EngineTest implements EngineListener, EngineKeyListener, MouseMotionListener {

	private static final int WINDOW_W = 1024;
	private static final int WINDOW_H = 720;
	private static final int RENDER_W = 1024;
	private static final int RENDER_H = 720;
	
	private final int[] cache;
	private Transform cameraTransform;
	
	private VectorLibrary vectorLibrary;
	private GraphicsController graphicsController;
	private InputController inputController;
	
	public static void main(String[] args) {
		new EngineTest();
	}
	
	EngineTest() {
		Engine.getInstance().start();
		Engine.getInstance().addEngineListener(this);
		this.vectorLibrary = new VectorLibrary();
		cache = vectorLibrary.generate();
	}
	
	public void start() {
		BufferedImage image = new BufferedImage(RENDER_W, RENDER_H, ColorLibrary.COLOR_TYPE);
		FrameBuffer frameBuffer = new FrameBuffer(image);
		EngineWindow window = new EngineWindow(frameBuffer);
		graphicsController = new GraphicsController(loadScene(), frameBuffer);
		inputController = new InputController();
		Engine.getInstance().addEngineListener(graphicsController);
		Engine.getInstance().addEngineListener(inputController);
		Engine.getInstance().addEngineListener(window);
		Engine.getInstance().addEngineListener(new EngineStatistics(frameBuffer));
		window.setSize(WINDOW_W, WINDOW_H);
		inputController.addMouseMotionListener(this);
		inputController.addEngineKeyListener(this);
		cameraTransform = graphicsController.getScene().getCamera(0).getTransform();
		graphicsController.removeShader(graphicsController.getShader(0));
		graphicsController.addPreprocessingShader(new DirectionalLightShadowShader());
		graphicsController.addPreprocessingShader(new SpotLightShadowShader());
		graphicsController.addShader(new FlatSpecularShader());
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
		rotation[2] = -((e.getX() - (WINDOW_W >> 1)) >> 1) << MathLibrary.FP_BITS;
		rotation[0] = -(((e.getY() - (WINDOW_H >> 1)) >> 1) - 90) << MathLibrary.FP_BITS;
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

	int startSpeed = MathLibrary.FP_ONE / 2;
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
