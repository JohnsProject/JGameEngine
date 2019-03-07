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
	
	private static final int FOV = 60;

	private static final int[][] transformMatrix = MatrixProcessor.generate();	
	private static final int[][] spaceMatrix = MatrixProcessor.generate();

	
	public static void process(Scene scene, GraphicsBuffer graphicsBuffer) {
		graphicsBuffer.clearFrameBuffer();
		graphicsBuffer.clearDepthBuffer();
		for (int i = 0; i < scene.getCameras().size(); i++) {
			Camera camera = scene.getCameras().get(i);
//			int[][] view = worldToView(camera);
			for (int j = 0; j < scene.getModels().size(); j++) {
				Model model = scene.getModels().get(j);
//				model.getTransform().setLocation(0, 13000, 0);
				model.getTransform().rotate(0, 0, 1);
//				model.getTransform().setScale(2, 2, 2);
				int[][] world = modelToWorld(model);
				for (int k = 0; k < model.getVertexes().length; k++) {
					model.getVertex(k).reset();
					int[] loc = model.getVertex(k).getLocation();
					VectorProcessor.multiply(loc, world, loc);
//					VectorProcessor.multiply(loc, view, loc);
//					loc[vx] = (FOV * loc[vx]) / (FOV + loc[vz]) + (graphicsBuffer.getWidth() >> 1);
//					loc[vy] = (FOV * loc[vy]) / (FOV + loc[vz]) + (graphicsBuffer.getHeight() >> 1);
					loc[vx] = ((loc[vx] * FOV) >> MathProcessor.FP_SHIFT) + (graphicsBuffer.getWidth() >> 1);
					loc[vy] = ((loc[vy] * FOV) >> MathProcessor.FP_SHIFT) + (graphicsBuffer.getHeight() >> 1);
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

	public static void drawLine(int x1, int y1, int x2, int y2, int z, int color, Camera camera,
			GraphicsBuffer graphicsBuffer) {
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

	public static int[][] modelToWorld(Model model) {
		Transform transform = model.getTransform();
		int[] location = transform.getLocation();
		int[] rotation = transform.getRotation();
		int[] scale = transform.getScale();
		MatrixProcessor.copy(spaceMatrix, rotateX(rotation[vx]));
		MatrixProcessor.multiply(rotateY(rotation[vy]), spaceMatrix, spaceMatrix);
		MatrixProcessor.multiply(rotateZ(rotation[vz]), spaceMatrix, spaceMatrix);
		MatrixProcessor.multiply(scale(scale[vx], scale[vy], scale[vz]), spaceMatrix, spaceMatrix);
		MatrixProcessor.multiply(translate(location[vx], location[vy], location[vz]), spaceMatrix, spaceMatrix);
		return spaceMatrix;
	}

	public static int[][] worldToView(Camera camera) {
		Transform transform = camera.getTransform();
		int[] location = transform.getLocation();
		int[] rotation = transform.getRotation();
		MatrixProcessor.copy(spaceMatrix, rotateX(-rotation[vx]));
		MatrixProcessor.multiply(rotateY(-rotation[vy]), spaceMatrix, spaceMatrix);
		MatrixProcessor.multiply(rotateZ(-rotation[vz]), spaceMatrix, spaceMatrix);
		MatrixProcessor.multiply(translate(-location[vx], -location[vy], -location[vz]), spaceMatrix, spaceMatrix);
		return spaceMatrix;
	}

	public static int[][] translate(int x, int y, int z) {
		MatrixProcessor.reset(transformMatrix);
		transformMatrix[3][0] = x << MathProcessor.FP_SHIFT;
		transformMatrix[3][1] = y << MathProcessor.FP_SHIFT;
		transformMatrix[3][2] = z << MathProcessor.FP_SHIFT;
		return transformMatrix;
	}

	public static int[][] scale(int x, int y, int z) {
		MatrixProcessor.reset(transformMatrix);
		transformMatrix[0][0] = x << MathProcessor.FP_SHIFT;
		transformMatrix[1][1] = y << MathProcessor.FP_SHIFT;
		transformMatrix[2][2] = z << MathProcessor.FP_SHIFT;
		return transformMatrix;
	}

	public static int[][] rotateX(int angle) {
		MatrixProcessor.reset(transformMatrix);
		int cos = MathProcessor.cos(angle);
		int sin = MathProcessor.sin(angle);
		transformMatrix[1][1] = cos;
		transformMatrix[1][2] = sin;
		transformMatrix[2][1] = -sin;
		transformMatrix[2][2] = cos;
		return transformMatrix;
	}

	public static int[][] rotateY(int angle) {
		MatrixProcessor.reset(transformMatrix);
		int cos = MathProcessor.cos(angle);
		int sin = MathProcessor.sin(angle);
		transformMatrix[0][0] = cos;
		transformMatrix[0][2] = -sin;
		transformMatrix[2][0] = sin;
		transformMatrix[2][2] = cos;
		return transformMatrix;
	}

	public static int[][] rotateZ(int angle) {
		MatrixProcessor.reset(transformMatrix);
		int cos = MathProcessor.cos(angle);
		int sin = MathProcessor.sin(angle);
		transformMatrix[0][0] = cos;
		transformMatrix[0][1] = sin;
		transformMatrix[1][0] = -sin;
		transformMatrix[1][1] = cos;
		return transformMatrix;
	}
}
