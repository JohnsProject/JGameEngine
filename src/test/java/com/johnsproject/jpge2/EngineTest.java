package com.johnsproject.jpge2;

import java.io.IOException;

import com.johnsproject.jpge2.dto.Model;
import com.johnsproject.jpge2.dto.Texture;
import com.johnsproject.jpge2.importers.SOMImporter;

public class EngineTest {

	private static final int WINDOW_W = 840;
	private static final int WINDOW_H = 640;
	
	public static void main(String[] args) {
		Engine.getInstance().addGraphicsBufferListener(new EngineWindow(WINDOW_W, WINDOW_H));
		Engine.getInstance().getGraphicsBuffer().setSize(WINDOW_W, WINDOW_H);
		Engine.getInstance().getScene().getCameras().get(0).setCanvas(0, 0, WINDOW_W, WINDOW_H);
		try {
			Model model = SOMImporter.load("C:/Development/test.som");
			model.getMaterial(0).setTexture(new Texture("C:/Development/JohnsProject.png"));
			Engine.getInstance().getScene().addModel(model);
		} catch (IOException e) {
			e.printStackTrace();
		}
		Engine.getInstance().getScene().getModels().get(0).getTransform().translate(0, 0, 0);
		Engine.getInstance().getScene().getCameras().get(0).getTransform().translate(0, 0, 120);
		Engine.getInstance().getScene().getCameras().get(0).getTransform().rotate(0, 0, 0);
		Engine.getInstance().getScene().getLights().get(0).getTransform().translate(5, 0, 0);
		new Thread(new Runnable() {
			
			public void run() {
				while(true) {
					Engine.getInstance().getScene().getModels().get(0).getTransform().rotate(1, 1, 0);
					try {
						Thread.sleep(32);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}).start();
	}

}
