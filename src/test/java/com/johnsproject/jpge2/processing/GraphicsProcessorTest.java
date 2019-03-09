package com.johnsproject.jpge2.processing;

import org.junit.Test;

import com.johnsproject.jpge2.dto.Face;
import com.johnsproject.jpge2.dto.Material;
import com.johnsproject.jpge2.dto.Model;
import com.johnsproject.jpge2.dto.Transform;
import com.johnsproject.jpge2.dto.Vertex;

public class GraphicsProcessorTest {
	
	@Test
	public void worldToViewTest() throws Exception {
		int[][] matrix = MatrixProcessor.generate();
		int[] out = VectorProcessor.generate();
		Model model = new Model("", new Transform(), new Vertex[0], new Face[0], new Material[0]);
		model.getTransform().rotate(0, 0, 30);
		GraphicsProcessor.modelToWorld(matrix, model);
		int[] vector = VectorProcessor.generate(100, 100, 100);
		VectorProcessor.multiply(vector, matrix, out);
		assert(out[0] == 36);
		assert(out[1] == 136);
		assert(out[2] == 100);
		assert(out[3] == 1);
	}
	
	@Test
	public void translateTest() throws Exception {
		int[][] matrix = MatrixProcessor.generate();
		int[] out = VectorProcessor.generate();
		GraphicsProcessor.translate(matrix, new int[] {2, 3, 4});
		int[] vector = VectorProcessor.generate();
		VectorProcessor.multiply(vector, matrix, out);
		assert(out[0] == 2);
		assert(out[1] == 3);
		assert(out[2] == 4);
		assert(out[3] == 1);
	}
	
	@Test
	public void scaleTest() throws Exception {
		int[][] matrix = MatrixProcessor.generate();
		int[] out = VectorProcessor.generate();
		GraphicsProcessor.scale(matrix, new int[] {2, 3, 4});
		int[] vector = VectorProcessor.generate(1, 3, 5);
		VectorProcessor.multiply(vector, matrix, out);
		assert(out[0] == 2);
		assert(out[1] == 9);
		assert(out[2] == 20);
		assert(out[3] == 1);
	}
	
	@Test
	public void rotateXTest() throws Exception {
		int[][] matrix = MatrixProcessor.generate();
		GraphicsProcessor.rotateX(matrix, 30);
		int[] out = VectorProcessor.generate();
		int[] vector = VectorProcessor.generate(100, 100, 100);
		VectorProcessor.multiply(vector, matrix, out);
		assert(out[0] == 100);
		assert(out[1] == 36);
		assert(out[2] == 136);
		assert(out[3] == 1);
	}
	
	@Test
	public void rotateYTest() throws Exception {
		int[][] matrix = MatrixProcessor.generate();
		int[] out = VectorProcessor.generate();
		GraphicsProcessor.rotateY(matrix, 30);
		int[] vector = VectorProcessor.generate(100, 100, 100);
		VectorProcessor.multiply(vector, matrix, out);
		assert(out[0] == 136);
		assert(out[1] == 100);
		assert(out[2] == 36);
		assert(out[3] == 1);
	}
	
	@Test
	public void rotateZTest() throws Exception {
		int[][] matrix = MatrixProcessor.generate();
		int[] out = VectorProcessor.generate();
		GraphicsProcessor.rotateZ(matrix, 30);
		int[] vector = VectorProcessor.generate(100, 100, 100);
		VectorProcessor.multiply(vector, matrix, out);
		assert(out[0] == 36);
		assert(out[1] == 136);
		assert(out[2] == 100);
		assert(out[3] == 1);
	}
	
}
