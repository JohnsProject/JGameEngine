package com.johnsproject.jpge2;

import java.io.IOException;

import com.johnsproject.jpge2.dto.Camera;
import com.johnsproject.jpge2.dto.Model;
import com.johnsproject.jpge2.dto.Scene;
import com.johnsproject.jpge2.dto.Texture;
import com.johnsproject.jpge2.dto.Transform;
import com.johnsproject.jpge2.importers.SOMImporter;
import com.johnsproject.jpge2.importers.SceneImporter;
import com.johnsproject.jpge2.processors.MathProcessor;
import com.johnsproject.jpge2.processors.VectorProcessor;

public class EngineTest implements EngineListener {

	private static final int WINDOW_W = 640;
	private static final int WINDOW_H = 480;
	
	public static void main(String[] args) {
		new EngineTest();
	}
	
	EngineTest() {
		Engine.getInstance().start();
		Engine.getInstance().addEngineListener(this);
	}
	
	public void start() {
		new EngineStatistics();
		Engine.getInstance().addEngineListener(new EngineWindow(WINDOW_W, WINDOW_H));
		Engine.getInstance().getOptions().getFrameBuffer().setSize(WINDOW_W, WINDOW_H);
//		useSOM();
		useScene();
	}

	static void useSOM() {
		try {
			Model model = new SOMImporter(Engine.getInstance().getProcessor()).load("C:/Development/test.som");
			model.getMaterial(0).setTexture(new Texture("C:/Development/JohnsProject.png"));
			Engine.getInstance().getOptions().getScene().addCamera(new Camera("Default Camera", new Transform(new int[3], new int[3], new int[3])));
			Engine.getInstance().getOptions().getScene().getCameras().get(0).getTransform().translate(0, 0, MathProcessor.FP_ONE * 100);
			Engine.getInstance().getOptions().getScene().addModel(model);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	static void useScene() {
		try {
			Scene scene = new SceneImporter(Engine.getInstance().getProcessor()).load("C:/Development/test.scene");
			scene.getModel("Ground").getMaterial(0).setTexture(new Texture("C:/Development/JohnsProject.png"));
//			scene.getModels().get(0).getMaterial(0).setTexture(new Texture("C:/Development/JohnsProject.png"));
			Engine.getInstance().getOptions().setScene(scene);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void update() {
		
	}

	public void fixedUpdate() {
		for (int i = 0; i < Engine.getInstance().getOptions().getScene().getModels().size(); i++) {
//			Engine.getInstance().getOptions().getScene().getModels().get(i).getTransform().rotate(VectorProcessor.VECTOR_UP);
//			Engine.getInstance().getOptions().getScene().getModels().get(i).getTransform().translate(VectorProcessor.VECTOR_RIGHT);
		}
	}

	public int getPriority() {
		return 0;
	}
}
