package com.johnsproject.jpge2;

import java.io.IOException;

import com.johnsproject.jpge2.dto.Camera;
import com.johnsproject.jpge2.dto.Model;
import com.johnsproject.jpge2.dto.Scene;
import com.johnsproject.jpge2.dto.Texture;
import com.johnsproject.jpge2.dto.Transform;
import com.johnsproject.jpge2.importers.SOMImporter;
import com.johnsproject.jpge2.importers.SceneImporter;
import com.johnsproject.jpge2.processing.VectorProcessor;

public class EngineTest {

	private static final int WINDOW_W = 840;
	private static final int WINDOW_H = 640;
	
	public static void main(String[] args) {
		Engine.getInstance().addGraphicsBufferListener(new EngineWindow(WINDOW_W, WINDOW_H));
		Engine.getInstance().getGraphicsBuffer().setSize(WINDOW_W, WINDOW_H);
//		useSOM();
		useScene();
		Engine.getInstance().getScene().getCameras().get(0).setCanvas(0, 0, WINDOW_W, WINDOW_H);
		new Thread(new Runnable() {
			
			public void run() {
				while(true) {
//					Engine.getInstance().getScene().getModels().get(0).getTransform().rotate(1, 1, 0);
					try {
						Thread.sleep(32);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}).start();
	}

	static void useSOM() {
		try {
			Model model = SOMImporter.load("C:/Development/test.som");
			model.getMaterial(0).setTexture(new Texture("C:/Development/JohnsProject.png"));
			Engine.getInstance().getScene().addCamera(new Camera("Default Camera", new Transform(), VectorProcessor.generate(0, 0, 1, 1)));
			Engine.getInstance().getScene().getCameras().get(0).getTransform().translate(0, 0, 100);
			Engine.getInstance().getScene().addModel(model);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	static void useScene() {
		try {
			Scene scene = SceneImporter.load("C:/Development/test.scene");
			Engine.getInstance().setScene(scene);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
