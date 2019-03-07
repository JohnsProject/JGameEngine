package com.johnsproject.jpge2;

import java.io.IOException;

import com.johnsproject.jpge2.dto.Camera;
import com.johnsproject.jpge2.dto.Transform;
import com.johnsproject.jpge2.importers.SOMImporter;

public class EngineTest {

	private static final int WINDOW_W = 720;
	private static final int WINDOW_H = 480;
	
	public static void main(String[] args) {
		Engine.getInstance().addGraphicsBufferListener(new EngineWindow(WINDOW_W, WINDOW_H));
		Engine.getInstance().getGraphicsBuffer().setSize(WINDOW_W, WINDOW_H);
		try {
			Engine.getInstance().getScene().addModel(SOMImporter.load("C:/Development/test.som"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		Camera camera = new Camera("Cam", new Transform(new int[] {0, 0, -20}, new int[3], new int[3]), new int[2], new int[] {WINDOW_W, WINDOW_H});
		Engine.getInstance().getScene().addCamera(camera);
	}

}
