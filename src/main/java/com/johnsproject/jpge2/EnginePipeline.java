package com.johnsproject.jpge2;

import com.johnsproject.jpge2.dto.GraphicsBuffer;
import com.johnsproject.jpge2.dto.Scene;
import com.johnsproject.jpge2.processing.GraphicsProcessor;

public class EnginePipeline {

	public void call(Scene scene, GraphicsBuffer graphicsBuffer) {
		GraphicsProcessor.process(scene, graphicsBuffer);
	}
	
}
