package com.johnsproject.jpge2.processing;

import java.util.List;

import com.johnsproject.jpge2.dto.Camera;
import com.johnsproject.jpge2.dto.Face;
import com.johnsproject.jpge2.dto.GraphicsBuffer;
import com.johnsproject.jpge2.dto.Light;
import com.johnsproject.jpge2.dto.Material;
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
				int[][] worldMatrix = model.getModelMatrix();
				int[][] normalMatrix = model.getNormalMatrix();
				int[][] viewMatrix = camera.getViewMatrix();
				int[][] projectionMatrix = camera.getPerspectiveMatrix();
				for (int l = 0; l < model.getMaterials().length; l++) {
					model.getMaterial(l).getShader().setup(camera, scene.getLights());
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
					location[vx] = MathProcessor.divide(location[vx], location[vw]) + (camera.getCanvas()[vz] >> 1);
					location[vy] = MathProcessor.divide(location[vy], location[vw]) + (camera.getCanvas()[vw] >> 1);
				}
				for (int l = 0; l < model.getFaces().length; l++) {
					Face face = model.getFace(l);
					face.reset();
					int[] location1 = face.getVertex1().getLocation();
					int[] location2 = face.getVertex2().getLocation();
					int[] location3 = face.getVertex3().getLocation();
					// barycentric[vw] will be needed later
					if ((barycentric[vw] = barycentric(location1, location2, location3)) > 0) {
						int[] normal = face.getNormal();
						VectorProcessor.multiply(normal, normalMatrix, normal);
						face.getMaterial().getShader().geometry(face);
						drawFace(face, graphicsBuffer);
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
		MatrixProcessor.rotateY(matrix, -rotation[vy]);
		MatrixProcessor.rotateZ(matrix, -rotation[vz]);
		MatrixProcessor.scale(matrix, scale[vx], scale[vy], scale[vz]);
		MatrixProcessor.translate(matrix, -location[vx], location[vy], location[vz]);
		return matrix;
	}

	public static int[][] normalMatrix(int[][] matrix, Model model) {
		Transform transform = model.getTransform();
		int[] rotation = transform.getRotation();
		int[] scale = transform.getScale();
		MatrixProcessor.reset(matrix);
		MatrixProcessor.rotateX(matrix, rotation[vx]);
		MatrixProcessor.rotateY(matrix, -rotation[vy]);
		MatrixProcessor.rotateZ(matrix, -rotation[vz]);
		MatrixProcessor.scale(matrix, scale[vx], scale[vy], scale[vz]);
		return matrix;
	}

	public static int[][] viewMatrix(int[][] matrix, Camera camera) {
		Transform transform = camera.getTransform();
		int[] location = transform.getLocation();
		int[] rotation = transform.getRotation();
		MatrixProcessor.reset(matrix);
		MatrixProcessor.translate(matrix, location[vx], -location[vy], -location[vz]);
		MatrixProcessor.rotateX(matrix, -rotation[vx]);
		MatrixProcessor.rotateY(matrix, rotation[vy]);
		MatrixProcessor.rotateZ(matrix, rotation[vz]);
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
		matrix[0][0] = (frustum[vx] * frustum[vy]) << MathProcessor.FP_SHIFT;
		matrix[1][1] = (frustum[vx] * frustum[vy]) << MathProcessor.FP_SHIFT;
		matrix[2][2] = -10;
		matrix[2][3] = MathProcessor.FP_VALUE;
		return matrix;
	}

	// rasterization variables
	private static final int[] pixel = VectorProcessor.generate();
	private static final int[] barycentric = VectorProcessor.generate();
	private static final int[] depth = VectorProcessor.generate();

	public static void drawFace(Face face, GraphicsBuffer graphicsBuffer) {
		int[] location1 = face.getVertex1().getLocation();
		int[] location2 = face.getVertex2().getLocation();
		int[] location3 = face.getVertex3().getLocation();
		depth[0] = location1[vz];
		depth[1] = location2[vz];
		depth[2] = location3[vz];

		// compute boundig box of faces
		int minX = Math.min(location1[vx], Math.min(location2[vx], location3[vx]));
		int minY = Math.min(location1[vy], Math.min(location2[vy], location3[vy]));
		int maxX = Math.max(location1[vx], Math.max(location2[vx], location3[vx]));
		int maxY = Math.max(location1[vy], Math.max(location2[vy], location3[vy]));

		// clip against screen limits
		minX = Math.max(minX, 0);
		minY = Math.max(minY, 0);
		maxX = Math.min(maxX, graphicsBuffer.getWidth() - 1);
		maxY = Math.min(maxY, graphicsBuffer.getHeight() - 1);

		for (pixel[vy] = minY; pixel[vy] < maxY; pixel[vy]++) {
			boolean found = false;
			boolean usedLast = false;
			for (pixel[vx] = minX; pixel[vx] < maxX; pixel[vx]++) {
				// calculate barycentric coordinates
				barycentric[vx] = barycentric(location2, location3, pixel);
				barycentric[vy] = barycentric(location3, location1, pixel);
				barycentric[vz] = barycentric(location1, location2, pixel);
				if ((barycentric[vx] >= 0) && (barycentric[vy] >= 0) && (barycentric[vz] >= 0)) {
					pixel[vz] = interpolatDepth(depth, barycentric);
					int color = face.getMaterial().getShader().fragment(pixel, barycentric);
					graphicsBuffer.setPixel(pixel[vx], pixel[vy], pixel[vz], color);
					found = true;
					usedLast = true;
				} else if (found && usedLast) {
					break;
				}
			}
		}
	}

	private static int interpolatDepth(int[] values, int[] barycentric) {
		// 10 bits of precision are not enought
		final byte shift = MathProcessor.FP_SHIFT * 2;
		long dotProduct = ((long) barycentric[vx] << shift) / depth[0]
						+ ((long) barycentric[vy] << shift) / depth[1]
						+ ((long) barycentric[vz] << shift) / depth[2];
		return (int) (((long) barycentric[vw] << shift) / dotProduct);
	}

	public static int interpolate(int[] values, int[] barycentric) {
		long dotProduct = (((long) values[vx] << MathProcessor.FP_SHIFT) / depth[0]) * barycentric[vx]
				+ (((long) values[vy] << MathProcessor.FP_SHIFT) / depth[1]) * barycentric[vy]
				+ (((long) values[vz] << MathProcessor.FP_SHIFT) / depth[2]) * barycentric[vz];
		// normalize values
		return (int) ((((long) dotProduct * (long) pixel[vz]) / barycentric[vw]) >> MathProcessor.FP_SHIFT);
	}

	private static int barycentric(int[] vector1, int[] vector2, int[] vector3) {
		return (vector2[vx] - vector1[vx]) * (vector3[vy] - vector1[vy])
				- (vector3[vx] - vector1[vx]) * (vector2[vy] - vector1[vy]);
	}

	private static final int[] vectorCache1 = VectorProcessor.generate();

	public static int getDirectionalLightFactor(int[] normal, int[] lightLocation, int[] cameraLocation,
			Material material) {
		VectorProcessor.invert(lightLocation);
		// diffuse
		int dotProduct = VectorProcessor.dotProduct(normal, lightLocation);
		int diffuseFactor = Math.max(dotProduct, 0);
		diffuseFactor = MathProcessor.multiply(diffuseFactor, material.getDiffuseIntensity());
		// specular
		VectorProcessor.reflect(lightLocation, normal, vectorCache1);
		VectorProcessor.invert(vectorCache1);
		dotProduct = VectorProcessor.dotProduct(cameraLocation, vectorCache1);
		int specularFactor = Math.max(dotProduct, 0);
		specularFactor = MathProcessor.multiply(specularFactor, material.getSpecularIntensity());
		// putting it all together...
		return ((diffuseFactor + specularFactor) * 100) >> MathProcessor.FP_SHIFT;
	}

	public static int getPointLightFactor(int[] location, int[] normal, int[] lightLocation, int[] cameraLocation,
			Material material) {
		VectorProcessor.invert(lightLocation);
		// diffuse
		int dotProduct = VectorProcessor.dotProduct(normal, lightLocation);
		int diffuseFactor = Math.max(dotProduct, 0);
		diffuseFactor = MathProcessor.multiply(diffuseFactor, material.getDiffuseIntensity());
		// specular
		VectorProcessor.reflect(lightLocation, normal, vectorCache1);
		VectorProcessor.invert(vectorCache1);
		dotProduct = VectorProcessor.dotProduct(cameraLocation, vectorCache1);
		int specularFactor = Math.max(dotProduct, 0);
		specularFactor = MathProcessor.multiply(specularFactor, material.getSpecularIntensity());
		// attenuation
		VectorProcessor.subtract(lightLocation, location, vectorCache1);
		int distance = VectorProcessor.magnitude(vectorCache1);
		int attenuation = MathProcessor.FP_VALUE;
		attenuation += MathProcessor.multiply(distance, 50 << MathProcessor.FP_SHIFT);
		attenuation += MathProcessor.multiply(MathProcessor.multiply(distance, distance), 20 << MathProcessor.FP_SHIFT);
		attenuation = attenuation >> MathProcessor.FP_SHIFT;
		// putting it all together...
		return ((diffuseFactor + specularFactor) * 100) / attenuation;
	}

	public static abstract class Shader {

		protected static final int vx = VectorProcessor.VECTOR_X;
		protected static final int vy = VectorProcessor.VECTOR_Y;
		protected static final int vz = VectorProcessor.VECTOR_Z;
		protected static final int vw = VectorProcessor.VECTOR_W;

		protected static List<Light> lights;
		protected static Camera camera;

		private void setup(Camera camera, List<Light> lights) {
			this.lights = lights;
			this.camera = camera;
		}

		public abstract void geometry(Face face);

		public abstract int fragment(int[] location, int[] barycentric);
	}
}
