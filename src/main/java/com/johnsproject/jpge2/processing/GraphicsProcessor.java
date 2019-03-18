package com.johnsproject.jpge2.processing;

import com.johnsproject.jpge2.Shader;
import com.johnsproject.jpge2.dto.Camera;
import com.johnsproject.jpge2.dto.Face;
import com.johnsproject.jpge2.dto.GraphicsBuffer;
import com.johnsproject.jpge2.dto.Light;
import com.johnsproject.jpge2.dto.Model;
import com.johnsproject.jpge2.dto.Scene;
import com.johnsproject.jpge2.dto.Transform;
import com.johnsproject.jpge2.dto.Vertex;

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
			for (int j = 0; j < scene.getModels().size(); j++) {
				Model model = scene.getModels().get(j);
				for (int k = 0; k < scene.getLights().size(); k++) {
					Light light = scene.getLights().get(k);
					int[][] world = model.getModelMatrix();
					int[][] view = camera.getViewMatrix();
					int[][] projection = camera.getPerspectiveMatrix();
					for (int l = 0; l < model.getMaterials().length; l++) {
						model.getMaterial(l).getShader().setup(camera, light);
					}
					for (int l = 0; l < model.getVertices().length; l++) {
						Vertex vertex = model.getVertex(l);
						vertex.reset();
						int[] location = vertex.getLocation();
						int[] normal = vertex.getNormal();
						VectorProcessor.multiply(location, world, location);
						VectorProcessor.multiply(normal, world, normal);
						vertex.getMaterial().getShader().vertex(vertex);
						VectorProcessor.multiply(location, view, location);
						VectorProcessor.multiply(location, projection, location);
						location[vx] = (location[vx] / location[vz]) + (camera.getCanvas()[vz] >> 1);
						location[vy] = (location[vy] / location[vz]) + (camera.getCanvas()[vw] >> 1);
					}
					for (int l = 0; l < model.getFaces().length; l++) {
						Face face = model.getFace(l);
						face.reset();
						int[] normal = face.getNormal();
						VectorProcessor.multiply(normal, world, normal);
						face.getMaterial().getShader().geometry(face);
						drawFace(face, graphicsBuffer);
					}
				}
			}
		}
	}

	public static int[][] modelToWorldMatrix(int[][] matrix, Model model) {
		Transform transform = model.getTransform();
		int[] location = transform.getLocation();
		int[] rotation = transform.getRotation();
		int[] scale = transform.getScale();
		MatrixProcessor.reset(matrix);
		MatrixProcessor.rotateX(matrix, rotation[vx]);
		MatrixProcessor.rotateY(matrix, rotation[vy]);
		MatrixProcessor.rotateZ(matrix, rotation[vz]);
		MatrixProcessor.scale(matrix, scale[vx], scale[vy], scale[vz]);
		MatrixProcessor.translate(matrix, location[vx], location[vy], location[vz]);
		return matrix;
	}

	public static int[][] worldToViewMatrix(int[][] matrix, Camera camera) {
		Transform transform = camera.getTransform();
		int[] location = transform.getLocation();
		int[] rotation = transform.getRotation();
		MatrixProcessor.reset(matrix);
		MatrixProcessor.translate(matrix, -location[vx], -location[vy], location[vz]);
		MatrixProcessor.rotateX(matrix, -rotation[vx]);
		MatrixProcessor.rotateY(matrix, -rotation[vy]);
		MatrixProcessor.rotateZ(matrix, -rotation[vz]);
		return matrix;
	}

	public static int[][] viewToOrthographicMatrix(int[][] matrix, Camera camera) {
		MatrixProcessor.reset(matrix);
		matrix[0][0] = -1;
		matrix[1][1] = -1;
		matrix[2][2] = 0;
		matrix[3][2] = MathProcessor.FP_VALUE;
		return matrix;
	}

	public static int[][] viewToPerspectiveMatrix(int[][] matrix, Camera camera) {
		int[] frustum = camera.getViewFrustum();
		MatrixProcessor.reset(matrix);
		matrix[0][0] = -frustum[vx] * 4;
		matrix[1][1] = -frustum[vx] * 4;
		matrix[2][2] = -1;
		return matrix;
	}

	private static final int[] location1 = VectorProcessor.generate();
	private static final int[] location2 = VectorProcessor.generate();
	private static final int[] location3 = VectorProcessor.generate();
	private static final int[] temp = VectorProcessor.generate();
	
	public static void drawFace(Face face, GraphicsBuffer graphicsBuffer) {
		Shader shader = face.getMaterial().getShader();
		VectorProcessor.copy(location1, face.getVertex1().getLocation());
		VectorProcessor.copy(location2, face.getVertex2().getLocation());
		VectorProcessor.copy(location3, face.getVertex3().getLocation());
		// y sorting
		if (location1[vy] > location2[vy]) {
			VectorProcessor.copy(temp, location1);
			VectorProcessor.copy(location1, location2);
			VectorProcessor.copy(location2, temp);
		}
		if (location2[vy] > location3[vy]) {
			VectorProcessor.copy(temp, location2);
			VectorProcessor.copy(location2, location3);
			VectorProcessor.copy(location3, temp);
		}
		if (location1[vy] > location2[vy]) {
			VectorProcessor.copy(temp, location1);
			VectorProcessor.copy(location1, location2);
			VectorProcessor.copy(location2, temp);
		}
		// delta x (how much x changes for each y value)
		int dx1 = 0;
		int dx2 = 0;
		int dx3 = 0;
		// delta z (how much z changes for each x value)
		int dz = 0;
		// delta z (how much z changes for each y value)
		int dz1 = 0;
		int dz2 = 0;
		int dz3 = 0;
		// y distance
		int y2y1 = location2[vy] - location1[vy];
		int y3y1 = location3[vy] - location1[vy];
		int y3y2 = location3[vy] - location2[vy];
		if (y2y1 > 0) {
			dx1 = ((location2[vx] - location1[vx]) << MathProcessor.FP_SHIFT) / y2y1;
			dz1 = ((location2[vz] - location1[vz]) << MathProcessor.FP_SHIFT) / y2y1;
		}
		if (y3y1 > 0) {
			dx2 = ((location3[vx] - location1[vx]) << MathProcessor.FP_SHIFT) / y3y1;
			dz2 = ((location3[vz] - location1[vz]) << MathProcessor.FP_SHIFT) / y3y1;
		}
		if (y3y2 > 0) {
			dx3 = ((location3[vx] - location2[vx]) << MathProcessor.FP_SHIFT) / y3y2;
			dz3 = ((location3[vz] - location2[vz]) << MathProcessor.FP_SHIFT) / y3y2;
		}
		int sx = location1[vx] << MathProcessor.FP_SHIFT;
		int ex = location1[vx] << MathProcessor.FP_SHIFT;
		int sz = location1[vz] << MathProcessor.FP_SHIFT;
		int sy = location1[vy];
		int dxdx = dx1 - dx2;
		if (dxdx > 0) {
			dz = ((dz1 - dz2) << MathProcessor.FP_SHIFT) / dxdx;
		}
		for (; sy < location2[vy]; sy++) {
			drawHorizontalLine(sx >> MathProcessor.FP_SHIFT, ex >> MathProcessor.FP_SHIFT, sz, dz, sy, shader, graphicsBuffer);
			sx += dx2;
			ex += dx1;
			sz += dz2;
		}
		ex = location2[vx] << MathProcessor.FP_SHIFT;
		dxdx = dx3 - dx2;
		if (dxdx > 0) {
			dz = ((dz3 - dz2) << MathProcessor.FP_SHIFT) / dxdx;
		}
		for (; sy < location3[vy]; sy++) {
			drawHorizontalLine(sx >> MathProcessor.FP_SHIFT, ex >> MathProcessor.FP_SHIFT, sz, dz, sy, shader, graphicsBuffer);
			sx += dx2;
			ex += dx3;
			sz += dz2;
		}
	}

	private static void drawHorizontalLine(int sx, int ex, int sz, int dz, int sy, Shader shader, GraphicsBuffer graphicsBuffer) {
		if (sx < ex) {
			for (; sx < ex; sx++) {
				int color = shader.fragment(sx, sy, sz >> MathProcessor.FP_SHIFT);
				if (color != 0)
					graphicsBuffer.setPixel(sx, sy, sz >> MathProcessor.FP_SHIFT, color);
				sz += dz;
			}
		} else {
			for (; ex < sx; ex++) {
				int color = shader.fragment(ex, sy, sz >> MathProcessor.FP_SHIFT);
				if (color != 0)
					graphicsBuffer.setPixel(ex, sy, sz >> MathProcessor.FP_SHIFT, color);
				sz -= dz;
			}
		}
	}
}
