package com.johnsproject.jpge2;

import java.io.IOException;

import com.johnsproject.jpge2.dto.Camera;
import com.johnsproject.jpge2.dto.Transform;
import com.johnsproject.jpge2.importers.SOMImporter;
import com.johnsproject.jpge2.processing.VectorProcessor;

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
		Transform camTransform = new Transform(VectorProcessor.generate(0, 0, 9000), VectorProcessor.generate(), VectorProcessor.generate()); 
		Camera camera = new Camera("Cam", camTransform, VectorProcessor.generate(0, 0, WINDOW_W, WINDOW_H));
		Engine.getInstance().getScene().addCamera(camera);
	}

}
