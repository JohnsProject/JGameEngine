package com.johnsproject.jpge2.processing;

import org.junit.Test;

import com.johnsproject.jpge2.dto.Face;
import com.johnsproject.jpge2.dto.Material;
import com.johnsproject.jpge2.dto.Model;
import com.johnsproject.jpge2.dto.Transform;
import com.johnsproject.jpge2.dto.Vertex;

public class GraphicsProcessorTest {

	
	@Test
	public void modelToWorldTest() throws Exception {
		Model model = new Model("", new Transform(), new Vertex[0], new Face[0], new Material[0]);
		model.getTransform().rotate(0, 0, 30);
		int[][] matrix = MatrixProcessor.generate();
		GraphicsProcessor.modelMatrix(matrix, model.getTransform());
		int[] out = VectorProcessor.generate();
		int[] vector = VectorProcessor.generate(100, 100, 100);
		VectorProcessor.multiply(vector, matrix, out);
//		// test x rotation 
//		assert(out[0] == 1000);
//		assert(out[1] == 366);
//		assert(out[2] == 1366);
//		assert(out[3] == 10);
//		// test y rotation
//		assert(out[0] == 1366);
//		assert(out[1] == 1000);
//		assert(out[2] == 366);
//		assert(out[3] == 10);
		// test z rotation
		assert(out[0] == 366);
		assert(out[1] == 1366);
		assert(out[2] == 1000);
		assert(out[3] == 1);
	}
	
}
