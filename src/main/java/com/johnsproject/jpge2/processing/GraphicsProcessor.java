package com.johnsproject.jpge2.processing;

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
					int[][] worldMatrix = model.getModelMatrix();
					int[][] normalMatrix = model.getNormalMatrix();
					int[][] viewMatrix = camera.getViewMatrix();
					int[][] projectionMatrix = camera.getPerspectiveMatrix();
					for (int l = 0; l < model.getMaterials().length; l++) {
						model.getMaterial(l).getShader().setup(camera, light);
					}
					for (int l = 0; l < model.getVertices().length; l++) {
						Vertex vertex = model.getVertex(l);
						vertex.reset();
						int[] location = vertex.getLocation();
						int[] normal = vertex.getNormal();
						VectorProcessor.multiply(location, worldMatrix, location);
						VectorProcessor.multiply(normal, normalMatrix, normal);
						VectorProcessor.multiply(location, viewMatrix, location);
						VectorProcessor.multiply(location, projectionMatrix, location);
						location[vx] = (location[vx] / location[vz]) + (camera.getCanvas()[vz] >> 1);
						location[vy] = (location[vy] / location[vz]) + (camera.getCanvas()[vw] >> 1);
					}
					for (int l = 0; l < model.getFaces().length; l++) {
						Face face = model.getFace(l);
						face.reset();
						if (!isBackface(face)) {
							int[] normal = face.getNormal();
							VectorProcessor.multiply(normal, normalMatrix, normal);
							face.getMaterial().getShader().geometry(face);
							drawFace(face, graphicsBuffer);
						}
					}
				}
			}
		}
	}

	public static int[][] worldMatrix(int[][] matrix, Model model) {
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
	
	public static int[][] normalMatrix(int[][] matrix, Model model) {
		Transform transform = model.getTransform();
		int[] rotation = transform.getRotation();
		int[] scale = transform.getScale();
		MatrixProcessor.reset(matrix);
		MatrixProcessor.rotateX(matrix, rotation[vx]);
		MatrixProcessor.rotateY(matrix, rotation[vy]);
		MatrixProcessor.rotateZ(matrix, rotation[vz]);
		MatrixProcessor.scale(matrix, scale[vx], scale[vy], scale[vz]);
		return matrix;
	}

	public static int[][] viewMatrix(int[][] matrix, Camera camera) {
		Transform transform = camera.getTransform();
		int[] location = transform.getLocation();
		int[] rotation = transform.getRotation();
		MatrixProcessor.reset(matrix);
		MatrixProcessor.translate(matrix, -location[vx], -location[vy], -location[vz]);
		MatrixProcessor.rotateX(matrix, -rotation[vx]);
		MatrixProcessor.rotateY(matrix, -rotation[vy]);
		MatrixProcessor.rotateZ(matrix, -rotation[vz]);
		return matrix;
	}

	public static int[][] orthographicMatrix(int[][] matrix, Camera camera) {
		MatrixProcessor.reset(matrix);
		matrix[0][0] = -1;
		matrix[1][1] = -1;
		matrix[2][2] = 0;
		matrix[3][2] = MathProcessor.FP_VALUE;
		return matrix;
	}

	public static int[][] perspectiveMatrix(int[][] matrix, Camera camera) {
		int[] frustum = camera.getViewFrustum();
		MatrixProcessor.reset(matrix);
		matrix[0][0] = -frustum[vx] * 4;
		matrix[1][1] = -frustum[vx] * 4;
		matrix[2][2] = -1;
		return matrix;
	}

	public static boolean isBackface(Face face) {
		int[] location1 = face.getVertex1().getLocation();
		int[] location2 = face.getVertex2().getLocation();
		int[] location3 = face.getVertex3().getLocation();
		// calculate area of face
		int area = (location2[vx] - location1[vx]) * (location3[vy] - location1[vy])
				- (location3[vx] - location1[vx]) * (location2[vy] - location1[vy]);
		// if its < 0 its a backface
		if (area > 0) return false;
		return true;
	}
	
	// vertex location values
	private static final int[] LOCATION_1 = VectorProcessor.generate();
	private static final int[] LOCATION_2 = VectorProcessor.generate();
	private static final int[] LOCATION_3 = VectorProcessor.generate();
	private static final int[] LOCATION_TEMP = VectorProcessor.generate();
	// empty values for interpolation that can be filled by the shaders
	private static final int MAX_VARYING = 1;
	private static final int[] VARYING_TEMP = new int[MAX_VARYING];
	private static final int[] VARYING_DELTAS = new int[MAX_VARYING];
	private static final int[] VARYING_DELTAS_1 = new int[MAX_VARYING];
	private static final int[] VARYING_DELTAS_2 = new int[MAX_VARYING];
	private static final int[] VARYING_DELTAS_3 = new int[MAX_VARYING];
	
	public static void drawFace(Face face, GraphicsBuffer graphicsBuffer) {
		Shader shader = face.getMaterial().getShader();
		VectorProcessor.copy(LOCATION_1, face.getVertex1().getLocation());
		VectorProcessor.copy(LOCATION_2, face.getVertex2().getLocation());
		VectorProcessor.copy(LOCATION_3, face.getVertex3().getLocation());
		// y sorting
		if (LOCATION_1[vy] > LOCATION_2[vy]) {
			VectorProcessor.copy(LOCATION_TEMP, LOCATION_1);
			VectorProcessor.copy(LOCATION_1, LOCATION_2);
			VectorProcessor.copy(LOCATION_2, LOCATION_TEMP);
			copyArray(VARYING_TEMP, Shader.VARYING_VERTEX_1);
			copyArray(Shader.VARYING_VERTEX_1, Shader.VARYING_VERTEX_2);
			copyArray(Shader.VARYING_VERTEX_2, VARYING_TEMP);
		}
		if (LOCATION_2[vy] > LOCATION_3[vy]) {
			VectorProcessor.copy(LOCATION_TEMP, LOCATION_2);
			VectorProcessor.copy(LOCATION_2, LOCATION_3);
			VectorProcessor.copy(LOCATION_3, LOCATION_TEMP);
			copyArray(VARYING_TEMP, Shader.VARYING_VERTEX_2);
			copyArray(Shader.VARYING_VERTEX_2, Shader.VARYING_VERTEX_3);
			copyArray(Shader.VARYING_VERTEX_3, VARYING_TEMP);
		}
		if (LOCATION_1[vy] > LOCATION_2[vy]) {
			VectorProcessor.copy(LOCATION_TEMP, LOCATION_1);
			VectorProcessor.copy(LOCATION_1, LOCATION_2);
			VectorProcessor.copy(LOCATION_2, LOCATION_TEMP);
			copyArray(VARYING_TEMP, Shader.VARYING_VERTEX_1);
			copyArray(Shader.VARYING_VERTEX_1, Shader.VARYING_VERTEX_2);
			copyArray(Shader.VARYING_VERTEX_2, VARYING_TEMP);
		}
		// reset deltas
		for (int i = 0; i < MAX_VARYING; i++) {
			VARYING_DELTAS[i] = 0;
			VARYING_DELTAS_1[i] = 0;
			VARYING_DELTAS_2[i] = 0;
			VARYING_DELTAS_3[i] = 0;
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
		int y2y1 = LOCATION_2[vy] - LOCATION_1[vy];
		int y3y1 = LOCATION_3[vy] - LOCATION_1[vy];
		int y3y2 = LOCATION_3[vy] - LOCATION_2[vy];
		if (y2y1 > 0) {
			dx1 = ((LOCATION_2[vx] - LOCATION_1[vx]) << MathProcessor.FP_SHIFT) / y2y1;
			dz1 = ((LOCATION_2[vz] - LOCATION_1[vz]) << MathProcessor.FP_SHIFT) / y2y1;
			for (int i = 0; i < MAX_VARYING; i++) {
				VARYING_DELTAS_1[i] = ((Shader.VARYING_VERTEX_2[i] - Shader.VARYING_VERTEX_1[i]) << MathProcessor.FP_SHIFT) / y2y1;
			}
		}
		if (y3y1 > 0) {
			dx2 = ((LOCATION_3[vx] - LOCATION_1[vx]) << MathProcessor.FP_SHIFT) / y3y1;
			dz2 = ((LOCATION_3[vz] - LOCATION_1[vz]) << MathProcessor.FP_SHIFT) / y3y1;
			for (int i = 0; i < MAX_VARYING; i++) {
				VARYING_DELTAS_2[i] = ((Shader.VARYING_VERTEX_3[i] - Shader.VARYING_VERTEX_1[i]) << MathProcessor.FP_SHIFT) / y3y1;
			}
		}
		if (y3y2 > 0) {
			dx3 = ((LOCATION_3[vx] - LOCATION_2[vx]) << MathProcessor.FP_SHIFT) / y3y2;
			dz3 = ((LOCATION_3[vz] - LOCATION_2[vz]) << MathProcessor.FP_SHIFT) / y3y2;
			for (int i = 0; i < MAX_VARYING; i++) {
				VARYING_DELTAS_3[i] = ((Shader.VARYING_VERTEX_3[i] - Shader.VARYING_VERTEX_2[i]) << MathProcessor.FP_SHIFT) / y3y2;
			}
		}
		for (int i = 0; i < MAX_VARYING; i++) {
			VARYING_TEMP[i] = Shader.VARYING_VERTEX_1[i] << MathProcessor.FP_SHIFT;
		}
		int sx = LOCATION_1[vx] << MathProcessor.FP_SHIFT;
		int ex = LOCATION_1[vx] << MathProcessor.FP_SHIFT;
		int sz = LOCATION_1[vz] << MathProcessor.FP_SHIFT;
		int sy = LOCATION_1[vy];
		int dxdx = dx1 - dx2;
		if (dxdx > 0) {
			dz = ((dz1 - dz2) << MathProcessor.FP_SHIFT) / dxdx;
			for (int i = 0; i < MAX_VARYING; i++) {
				VARYING_DELTAS[i] = ((VARYING_DELTAS_2[i] - VARYING_DELTAS_1[i]) << MathProcessor.FP_SHIFT) / dxdx;
			}
		}
		for (; sy < LOCATION_2[vy]; sy++) {
			drawHorizontalLine(sx >> MathProcessor.FP_SHIFT, ex >> MathProcessor.FP_SHIFT, sz, dz, sy, shader, graphicsBuffer);
			sx += dx2;
			ex += dx1;
			sz += dz2;
			for (int i = 0; i < MAX_VARYING; i++) {
				VARYING_TEMP[i] += VARYING_DELTAS_2[i];
			}
		}
		ex = LOCATION_2[vx] << MathProcessor.FP_SHIFT;
		dxdx = dx3 - dx2;
		if (dxdx > 0) {
			dz = ((dz3 - dz2) << MathProcessor.FP_SHIFT) / dxdx;
			for (int i = 0; i < MAX_VARYING; i++) {
				VARYING_DELTAS[i] = ((VARYING_DELTAS_3[i] - VARYING_DELTAS_2[i]) << MathProcessor.FP_SHIFT) / dxdx;
			}
		}
		for (; sy < LOCATION_3[vy]; sy++) {
			drawHorizontalLine(sx >> MathProcessor.FP_SHIFT, ex >> MathProcessor.FP_SHIFT, sz, dz, sy, shader, graphicsBuffer);
			sx += dx2;
			ex += dx3;
			sz += dz2;
			for (int i = 0; i < MAX_VARYING; i++) {
				VARYING_TEMP[i] += VARYING_DELTAS_2[i];
			}
		}
	}

	private static void drawHorizontalLine(int sx, int ex, int sz, int dz, int sy, Shader shader, GraphicsBuffer graphicsBuffer) {
		if (sx < ex) {
			for (; sx < ex; sx++) {
				int color = shader.fragment(sx, sy, sz >> MathProcessor.FP_SHIFT);
				if (color != 0)
					graphicsBuffer.setPixel(sx, sy, sz >> MathProcessor.FP_SHIFT, color);
				sz += dz;
				for (int i = 0; i < MAX_VARYING; i++) {
					VARYING_TEMP[i] += VARYING_DELTAS_2[i];
					Shader.VARYING_CURRENT[i] = VARYING_TEMP[i] >> MathProcessor.FP_SHIFT;
				}
			}
		} else {
			for (; ex < sx; ex++) {
				int color = shader.fragment(ex, sy, sz >> MathProcessor.FP_SHIFT);
				if (color != 0)
					graphicsBuffer.setPixel(ex, sy, sz >> MathProcessor.FP_SHIFT, color);
				sz -= dz;
				for (int i = 0; i < MAX_VARYING; i++) {
					VARYING_TEMP[i] -= VARYING_DELTAS_2[i];
					Shader.VARYING_CURRENT[i] = VARYING_TEMP[i] >> MathProcessor.FP_SHIFT;
				}
			}
		}
	}
	
	private static void copyArray(int[] target, int[] source) {
		for (int i = 0; i < target.length; i++) {
			target[i] = source[i];
		}
	}
	
	public static abstract class Shader {

		protected static final int vx = VectorProcessor.VECTOR_X;
		protected static final int vy = VectorProcessor.VECTOR_Y;
		protected static final int vz = VectorProcessor.VECTOR_Z;
		protected static final int vw = VectorProcessor.VECTOR_W;
		protected static final int[] VARYING_VERTEX_1 = new int[MAX_VARYING];
		protected static final int[] VARYING_VERTEX_2 = new int[MAX_VARYING];
		protected static final int[] VARYING_VERTEX_3 = new int[MAX_VARYING];
		protected static final int[] VARYING_CURRENT = new int[MAX_VARYING];
		
		protected Light light;
		protected Camera camera;
			
		private void setup(Camera camera, Light light) {
			this.light = light;
			this.camera = camera;
		}
		
		public abstract void geometry(Face face);
		public abstract int fragment(int x, int y, int z);
		
	}
}
