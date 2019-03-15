package com.johnsproject.jpge2.processing;

import com.johnsproject.jpge2.dto.Camera;
import com.johnsproject.jpge2.dto.Face;
import com.johnsproject.jpge2.dto.GraphicsBuffer;
import com.johnsproject.jpge2.dto.Model;
import com.johnsproject.jpge2.dto.Scene;
import com.johnsproject.jpge2.dto.Transform;

public class GraphicsProcessor {

	private static final int vx = VectorProcessor.VECTOR_X;
	private static final int vy = VectorProcessor.VECTOR_Y;
	private static final int vz = VectorProcessor.VECTOR_Z;
	private static final int vw = VectorProcessor.VECTOR_W;
	
	public static void process(Scene scene, GraphicsBuffer graphicsBuffer) {
		graphicsBuffer.clearFrameBuffer();
		graphicsBuffer.clearDepthBuffer();
		for (int i = 0; i < scene.getCameras().size(); i++) {
			Camera camera = scene.getCameras().get(i);
			int[][] view = worldToView(camera);
			int[][] orthographic = viewToPerspective(camera);
			int[][] screen = projectionToScreen(camera);
			for (int j = 0; j < scene.getModels().size(); j++) {
				Model model = scene.getModels().get(j);
				model.getTransform().translate(0, 100, 0);
				model.getTransform().rotate(1, 1, 0);
				model.getTransform().setScale(20, 20, 20);
				int[][] world = modelToWorld(model);
				for (int k = 0; k < model.getVertexes().length; k++) {
					model.getVertex(k).reset();
					int[] loc = model.getVertex(k).getLocation();
					VectorProcessor.multiply(loc, world, loc);
					VectorProcessor.multiply(loc, view, loc);
					VectorProcessor.multiply(loc, orthographic, loc);
					loc[vx] /= loc[vz];
					loc[vy] /= -loc[vz];
					VectorProcessor.multiply(loc, screen, loc);
				}
				for (int k = 0; k < model.getFaces().length; k++) {
					Face face = model.getFace(k);
					int[] loc1 = face.getVertex1().getLocation();
					int[] loc2 = face.getVertex2().getLocation();
					int[] loc3 = face.getVertex3().getLocation();
					drawLine(loc1[vx], loc1[vy], loc2[vx], loc2[vy], 0, 0, camera, graphicsBuffer);
					drawLine(loc2[vx], loc2[vy], loc3[vx], loc3[vy], 0, 0, camera, graphicsBuffer);
					drawLine(loc1[vx], loc1[vy], loc3[vx], loc3[vy], 0, 0, camera, graphicsBuffer);
				}
			}
		}
	}

	public static int[][] modelToWorld(Model model) {
		Transform transform = model.getTransform();
		int[] location = transform.getLocation();
		int[] rotation = transform.getRotation();
		int[] scale = transform.getScale();
		int[][] matrix = MatrixProcessor.generate();
		MatrixProcessor.rotateX(matrix, rotation[vx]);
		MatrixProcessor.rotateY(matrix, rotation[vy]);
		MatrixProcessor.rotateZ(matrix, rotation[vz]);
		MatrixProcessor.scale(matrix, scale[vx], scale[vy], scale[vz]);
		MatrixProcessor.translate(matrix, location[vx], location[vy], location[vz]);
		return matrix;
	}

	public static int[][] worldToView(Camera camera) {
		Transform transform = camera.getTransform();
		int[] location = transform.getLocation();
		int[] rotation = transform.getRotation();
		int[][] matrix = MatrixProcessor.generate();
		MatrixProcessor.rotateX(matrix, -rotation[vx]);
		MatrixProcessor.rotateY(matrix, -rotation[vy]);
		MatrixProcessor.rotateZ(matrix, -rotation[vz]);
		MatrixProcessor.translate(matrix, -location[vx], -location[vy], -location[vz]);
		return matrix;
	}
	
	public static int[][] viewToOrthographic(Camera camera) {
		int[][] matrix = MatrixProcessor.generate();
		matrix[0][0] = 1;
		matrix[1][1] = 1;
		matrix[2][2] = 0;
		matrix[3][2] = MathProcessor.FP_VALUE;
		return matrix;
	}
	
	public static int[][] viewToPerspective(Camera camera) {
		int[] frustum = camera.getViewFrustum();
		int[][] matrix = MatrixProcessor.generate();
		matrix[0][0] = frustum[vx] * 4;
		matrix[1][1] = frustum[vx] * 4;
		matrix[2][2] = 1;
		matrix[3][2] = frustum[vx] << MathProcessor.FP_SHIFT;
		return matrix;
	}
	
	public static int[][] projectionToScreen(Camera camera) {
		int[] canvas = camera.getCanvas();
		int[][] matrix = MatrixProcessor.generate();
		matrix[3][0] = (canvas[vz] >> 1) << MathProcessor.FP_SHIFT;
		matrix[3][1] = (canvas[vw] >> 1) << MathProcessor.FP_SHIFT;
		return matrix;
	}
	
	public static void drawLine(int x1, int y1, int x2, int y2, int z, int color, Camera camera, GraphicsBuffer graphicsBuffer) {
		int dx = Math.abs(x2 - x1), sx = x1 < x2 ? 1 : -1;
		int dy = -Math.abs(y2 - y1), sy = y1 < y2 ? 1 : -1;
		int err = dx + dy, e2; /* error value e_xy */

		while (true) {
			graphicsBuffer.setPixel(x1, y1, 1, ColorProcessor.convert(0, 0, 0));
			if (x1 == x2 && y1 == y2)
				break;
			e2 = 2 * err;
			if (e2 > dy) {
				err += dy;
				x1 += sx;
			} /* e_xy+e_x > 0 */
			if (e2 < dx) {
				err += dx;
				y1 += sy;
			} /* e_xy+e_y < 0 */
		}
	}
}
