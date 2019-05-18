package com.johnsproject.jpge2;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;

import com.johnsproject.jpge2.controller.GraphicsController;
import com.johnsproject.jpge2.dto.Camera;
import com.johnsproject.jpge2.dto.FrameBuffer;
import com.johnsproject.jpge2.dto.Model;
import com.johnsproject.jpge2.dto.Scene;
import com.johnsproject.jpge2.dto.Texture;
import com.johnsproject.jpge2.dto.Transform;
import com.johnsproject.jpge2.importer.SOMImporter;
import com.johnsproject.jpge2.importer.SceneImporter;
import com.johnsproject.jpge2.library.ColorLibrary;
import com.johnsproject.jpge2.library.MathLibrary;
import com.johnsproject.jpge2.library.VectorLibrary;
import com.johnsproject.jpge2.shader.properties.SpecularShaderProperties;
import com.johnsproject.jpge2.util.FileUtil;

public class EngineTest implements EngineListener, MouseMotionListener, KeyListener {

	private static final int WINDOW_W = 1024;
	private static final int WINDOW_H = 768;
	private static final int RENDER_W = 640;
	private static final int RENDER_H = 480;
	
	private VectorLibrary vectorLibrary;
	private GraphicsController graphicsController;
	
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
		new EngineStatistics();
		graphicsController = Engine.getInstance().getController().getGraphicsController();
		BufferedImage image = new BufferedImage(RENDER_W, RENDER_H, ColorLibrary.COLOR_TYPE);
		graphicsController.setFrameBuffer(new FrameBuffer(image));
		EngineWindow window = new EngineWindow(graphicsController.getFrameBuffer());
		window.setSize(WINDOW_W, WINDOW_H);
		Engine.getInstance().addEngineListener(window);
//		useSOM();
		useScene();
		Engine.getInstance().getController().getInputController().addMouseMotionListener(this);
		Engine.getInstance().getController().getInputController().addKeyListener(this);
	}

	void useSOM() {
		try {
			Model model = new SOMImporter().load("C:/Development/test.som");
			Texture texture = new Texture(FileUtil.loadImage("C:/Development/JohnsProject.png"));
			((SpecularShaderProperties)model.getMesh().getMaterial(0).getProperties()).setTexture(texture);
			graphicsController.getScene().addCamera(new Camera("Default Camera", new Transform(new int[3], new int[3], new int[3])));
			graphicsController.getScene().getCameras().get(0).getTransform().translate(0, 0, MathLibrary.FP_ONE * 100);
			graphicsController.getScene().addModel(model);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	void useScene() {
		try {
			Scene scene = new SceneImporter().load("C:/Development/test.scene");
//			Texture texture = new Texture(FileUtil.loadImage("C:\\Users\\schnu\\Downloads\\qwichvr1jf28-dungeon\\prison/Pris"));
//			((SpecularShaderProperties)scene.getModel("Cube").getMesh().getMaterial(0).getProperties()).setTexture(texture);
//			scene.getModels().get(0).getMaterial(0).setTexture(new Texture("C:/Development/JohnsProject.png"));
			graphicsController.setScene(scene);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void update() {
		
	}

	private final int[] cache;
	
	public void fixedUpdate() {
		if (move) {
			Transform transform = graphicsController.getScene().getCamera(0).getTransform();
			vectorLibrary.rotateXYZ(VectorLibrary.VECTOR_DOWN, transform.getRotation(), cache);
			vectorLibrary.multiply(cache, 2 << MathLibrary.FP_BITS, cache);
			transform.translate(cache);
		}
//		for (int i = 0; i < Engine.getInstance().getScene().getModels().size(); i++) {
//			Engine.getInstance().getScene().getModels().get(i).getTransform().rotate(0, 0, 1000);
//			Engine.getInstance().getScene().getModels().get(i).getTransform().translate(VectorProcessor.VECTOR_RIGHT);
//		}
	}

	public int getPriority() {
		return 0;
	}

	public void mouseDragged(MouseEvent e) {
		
	}

	public void mouseMoved(MouseEvent e) {
		Transform transform = graphicsController.getScene().getCamera(0).getTransform();
		int[] rotation = transform.getRotation();
		rotation[2] = -((e.getX() - (WINDOW_W >> 1)) >> 1) << 10;
		rotation[0] = -(((e.getY() - (WINDOW_H >> 1)) >> 1) - 90) << 10;
	}

	public void keyTyped(KeyEvent e) {
		
	}

	
	boolean move = false;
	public void keyPressed(KeyEvent e) {
		move = true;
	}

	public void keyReleased(KeyEvent e) {
		 move = false;
	}
}
