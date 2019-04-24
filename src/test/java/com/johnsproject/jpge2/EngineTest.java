package com.johnsproject.jpge2;

import java.io.IOException;

import com.johnsproject.jpge2.controller.GraphicsController;
import com.johnsproject.jpge2.dto.Camera;
import com.johnsproject.jpge2.dto.Model;
import com.johnsproject.jpge2.dto.Scene;
import com.johnsproject.jpge2.dto.Texture;
import com.johnsproject.jpge2.dto.Transform;
import com.johnsproject.jpge2.importer.SOMImporter;
import com.johnsproject.jpge2.importer.SceneImporter;
import com.johnsproject.jpge2.processor.MathProcessor;
import com.johnsproject.jpge2.processor.VectorProcessor;

public class EngineTest implements EngineListener {

	private static final int WINDOW_W = 800;
	private static final int WINDOW_H = 640;
	
	public static void main(String[] args) {
		new EngineTest();
	}
	
	EngineTest() {
		Engine.getInstance().start();
		Engine.getInstance().addEngineListener(this);
	}
	
	public void start() {
		new EngineStatistics();
		GraphicsController graphicsController = Engine.getInstance().getController().getGraphicsController();
		graphicsController.getFrameBuffer().setSize(WINDOW_W, WINDOW_H);
		Engine.getInstance().addEngineListener(new EngineWindow(graphicsController.getFrameBuffer()));
//		useSOM();
		useScene();
	}

	static void useSOM() {
		try {
			Model model = new SOMImporter(Engine.getInstance().getProcessor()).load("C:/Development/test.som");
			model.getMaterial(0).setTexture(new Texture("C:/Development/JohnsProject.png"));
			Engine.getInstance().getScene().addCamera(new Camera("Default Camera", new Transform(new int[3], new int[3], new int[3])));
			Engine.getInstance().getScene().getCameras().get(0).getTransform().translate(0, 0, MathProcessor.FP_ONE * 100);
			Engine.getInstance().getScene().addModel(model);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	static void useScene() {
		try {
			Scene scene = new SceneImporter(Engine.getInstance().getProcessor()).load("C:/Development/test.scene");
			scene.getModel("Ground").getMaterial(0).setTexture(new Texture("C:/Development/JohnsProject.png"));
//			scene.getModels().get(0).getMaterial(0).setTexture(new Texture("C:/Development/JohnsProject.png"));
			Engine.getInstance().setScene(scene);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void update() {
		
	}

	public void fixedUpdate() {
		for (int i = 0; i < Engine.getInstance().getScene().getModels().size(); i++) {
//			Engine.getInstance().getScene().getModels().get(i).getTransform().rotate(VectorProcessor.VECTOR_UP);
//			Engine.getInstance().getScene().getModels().get(i).getTransform().translate(VectorProcessor.VECTOR_RIGHT);
		}
	}

	public int getPriority() {
		return 0;
	}
}
