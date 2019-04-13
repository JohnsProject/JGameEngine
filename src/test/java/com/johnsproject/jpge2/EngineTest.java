package com.johnsproject.jpge2;

import java.io.IOException;

import com.johnsproject.jpge2.dto.Camera;
import com.johnsproject.jpge2.dto.Model;
import com.johnsproject.jpge2.dto.Scene;
import com.johnsproject.jpge2.dto.Texture;
import com.johnsproject.jpge2.dto.Transform;
import com.johnsproject.jpge2.importers.SOMImporter;
import com.johnsproject.jpge2.importers.SceneImporter;
import com.johnsproject.jpge2.processors.VectorProcessor;

public class EngineTest implements EngineListener{

	private static final int WINDOW_W = 640;
	private static final int WINDOW_H = 480;
	
	public static void main(String[] args) {
		new EngineTest();
	}
	
	EngineTest() {
		Engine.getInstance().addEngineListener(this);
	}
	
	public void start() {
		new EngineStatistics();
		Engine.getInstance().addEngineListener(new EngineWindow(WINDOW_W, WINDOW_H));
		Engine.getInstance().getOptions().getFrameBuffer().setSize(WINDOW_W, WINDOW_H);
//		useSOM();
		useScene();
		Engine.getInstance().getOptions().getScene().getCameras().get(0).setCanvas(0, 0, WINDOW_W, WINDOW_H);
	}

	static void useSOM() {
		try {
			Model model = SOMImporter.load("C:/Development/test.som");
			model.getMaterial(0).setTexture(new Texture("C:/Development/JohnsProject.png"));
			Engine.getInstance().getOptions().getScene().addCamera(new Camera("Default Camera", new Transform(), VectorProcessor.generate(0, 0, 1, 1)));
			Engine.getInstance().getOptions().getScene().getCameras().get(0).getTransform().translate(0, 0, 100);
			Engine.getInstance().getOptions().getScene().addModel(model);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	static void useScene() {
		try {
			Scene scene = SceneImporter.load("C:/Development/test.scene");
			Engine.getInstance().getOptions().setScene(scene);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void update() {
		
	}

	public void fixedUpdate() {
		for (int i = 0; i < Engine.getInstance().getOptions().getScene().getModels().size(); i++) {
			Engine.getInstance().getOptions().getScene().getModels().get(i).getTransform().rotate(0, 0, 2);
		}
	}

	public int getPriority() {
		return 0;
	}
}
