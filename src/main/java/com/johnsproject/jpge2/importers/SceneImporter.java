package com.johnsproject.jpge2.importers;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.johnsproject.jpge2.dto.Camera;
import com.johnsproject.jpge2.dto.Face;
import com.johnsproject.jpge2.dto.Light;
import com.johnsproject.jpge2.dto.Material;
import com.johnsproject.jpge2.dto.Model;
import com.johnsproject.jpge2.dto.Scene;
import com.johnsproject.jpge2.dto.Texture;
import com.johnsproject.jpge2.dto.Transform;
import com.johnsproject.jpge2.dto.Vertex;
import com.johnsproject.jpge2.processing.FileProcessor;
import com.johnsproject.jpge2.processing.MathProcessor;
import com.johnsproject.jpge2.processing.VectorProcessor;
import com.johnsproject.jpge2.processing.ColorProcessor;

public class SceneImporter {

	private static final int vx = VectorProcessor.VECTOR_X;
	private static final int vy = VectorProcessor.VECTOR_Y;
	private static final int vz = VectorProcessor.VECTOR_Z;
	
	public static Scene load(String path) throws IOException {
		String content = FileProcessor.readFile(path);
		return loadFromRaw(content);
	}

	public static Scene load(InputStream stream) throws IOException {
		String content = FileProcessor.readStream(stream);
		return loadFromRaw(content);
	}

	public static Scene loadFromRaw(String data) throws IOException {
		String sceneData = data.replace(" ", "").replace("\n", "");
		List<Model> models = parseModels(sceneData);
		List<Camera> cameras = parseCameras(sceneData);
		List<Light> lights = parseLights(sceneData);
		Scene scene = new Scene(models, cameras, lights);
		System.gc();
		return scene;
	}
	
	private static List<Model> parseModels(String sceneData) {
		List<Model> models = new ArrayList<Model>();
		String[] modelsData = sceneData.split("model<");
		for (int i = 1; i < modelsData.length; i++) {
			String modelData = modelsData[i].split(">model")[0];
			String name = modelData.split("name<")[1].split(">name")[0];
			Transform transform = parseTransform(modelData.split("transform<")[1].split(">transform")[0].split(","));
			Vertex[] vertices = parseVertices(modelData.split("vertex<"));
			Face[] faces = parseFaces(modelData.split("face<"));
			Material[] materials = parseMaterials(modelData.split("material<"));
			models.add(new Model(name, transform, vertices, faces, materials));
		}
		return models;
	}
	
	private static List<Camera> parseCameras(String sceneData) {
		List<Camera> cameras = new ArrayList<Camera>();
		String[] camerasData = sceneData.split("camera<");
		for (int i = 1; i < camerasData.length; i++) {
			String cameraData = camerasData[i].split(">camera")[0];
			String name = cameraData.split("name<")[1].split(">name")[0];
			String typeData = cameraData.split("type<")[1].split(">type")[0];
			Transform transform = parseTransform(cameraData.split("transform<")[1].split(">transform")[0].split(","));
			cameras.add(new Camera(name, transform, VectorProcessor.generate(0, 0, 1, 1)));
		}
		return cameras;
	}
	
	private static List<Light> parseLights(String sceneData) {
		List<Light> lights = new ArrayList<Light>();
		String[] lightsData = sceneData.split("light<");
		for (int i = 1; i < lightsData.length; i++) {
			String lightData = lightsData[i].split(">light")[0];
			String name = lightData.split("name<")[1].split(">name")[0];
			String typeData = lightData.split("type<")[1].split(">type")[0];
			Transform transform = parseTransform(lightData.split("transform<")[1].split(">transform")[0].split(","));
			int type = 0;
			if (typeData.equals("SUN"))
				type = Light.LIGHT_DIRECTIONAL;
			if (typeData.equals("POINT"))
				type = Light.LIGHT_POINT;
			Light light = new Light(name, transform);
			light.setType(type);
			lights.add(light);
		}
		return lights;
	}
	
	private static Transform parseTransform(String[] transformData) {
		int x = (int)(getFloat(transformData[vx]) * MathProcessor.FP_VALUE);
		int y = (int)(getFloat(transformData[vy]) * MathProcessor.FP_VALUE);
		int z = (int)(getFloat(transformData[vz]) * MathProcessor.FP_VALUE);
		int[] location = VectorProcessor.generate(x, y, z);
		x = (int)(getFloat(transformData[3 + vx]));
		y = (int)(getFloat(transformData[3 + vy]));
		z = (int)(getFloat(transformData[3 + vz]));
		int[] rotation = VectorProcessor.generate(x, y, z);
		x = (int)(getFloat(transformData[6 + vx]) * 10);
		y = (int)(getFloat(transformData[6 + vy]) * 10);
		z = (int)(getFloat(transformData[6 + vz]) * 10);
		int[] scale = VectorProcessor.generate(x, y, z);
		return new Transform(location, rotation, scale);
	}
	
	private static Vertex[] parseVertices(String[] verticesData) {
		Vertex[] vertices = new Vertex[verticesData.length - 1];
		for (int i = 0; i < verticesData.length - 1; i++) {
			String[] vertexData = verticesData[i + 1].split(">vertex")[0].split(",");
			int x = (int)(getFloat(vertexData[vx]) * MathProcessor.FP_VALUE);
			int y = (int)(getFloat(vertexData[vy]) * MathProcessor.FP_VALUE);
			int z = (int)(getFloat(vertexData[vz]) * MathProcessor.FP_VALUE);
			int[] location = VectorProcessor.generate(x, y, z);
			x = (int)(getFloat(vertexData[3 + vx]) * MathProcessor.FP_VALUE);
			y = (int)(getFloat(vertexData[3 + vy]) * MathProcessor.FP_VALUE);
			z = (int)(getFloat(vertexData[3 + vz]) * MathProcessor.FP_VALUE);
			int[] normal = VectorProcessor.generate(x, y, z);
			int material = getInt(vertexData[6]);
			vertices[i] = new Vertex(i, location, normal, material);
		}
		return vertices;
	}
	
	private static Face[] parseFaces(String[] facesData) {
		Face[] faces = new Face[facesData.length - 1];
		for (int i = 0; i < facesData.length - 1; i++) {
			String[] faceData = facesData[i + 1].split(">face")[0].split(",");
			int vertex1 = getInt(faceData[0]);
			int vertex2 = getInt(faceData[1]);
			int vertex3 = getInt(faceData[2]);
			int x = (int)(getFloat(faceData[3 + vx]) * MathProcessor.FP_VALUE);
			int y = (int)(getFloat(faceData[3 + vy]) * MathProcessor.FP_VALUE);
			int z = (int)(getFloat(faceData[3 + vz]) * MathProcessor.FP_VALUE);
			int[] normal = VectorProcessor.generate(x, y, z);
			x = (int)(getFloat(faceData[6 + vx]) * MathProcessor.FP_VALUE);
			y = (int)(getFloat(faceData[6 + vy]) * MathProcessor.FP_VALUE);
			int[] uv1 = VectorProcessor.generate(x, y);
			x = (int)(getFloat(faceData[8 + vx]) * MathProcessor.FP_VALUE);
			y = (int)(getFloat(faceData[8 + vy]) * MathProcessor.FP_VALUE);
			int[] uv2 = VectorProcessor.generate(x, y);
			x = (int)(getFloat(faceData[10 + vx]) * MathProcessor.FP_VALUE);
			y = (int)(getFloat(faceData[10 + vy]) * MathProcessor.FP_VALUE);
			int[] uv3 = VectorProcessor.generate(x, y);
			int material = getInt(faceData[12]);
			faces[i] = new Face(i, vertex1, vertex2, vertex3, material, normal, uv1, uv2, uv3);
		}
		return faces;
	}
	
	private static Material[] parseMaterials(String[] materialsData) {
		Material[] materials = new Material[materialsData.length - 1];
		for (int i = 0; i < materialsData.length - 1; i++) {
			String[] vertexData = materialsData[i + 1].split(">material")[0].split(",");
			int red = (int)(getFloat(vertexData[0]) * 256);
			int green = (int)(getFloat(vertexData[1]) * 256);
			int blue = (int)(getFloat(vertexData[2]) * 256);
			int alpha = (int)(getFloat(vertexData[3]) * 256);
			int diffuse = (int)(getFloat(vertexData[4]) * 1024);
			int specular = (int)(getFloat(vertexData[5]) * 1024);
			materials[i] = new Material(i, ColorProcessor.convert(red, green, blue, alpha), diffuse, specular, null);
		}
		return materials;
	}
	
	private static int getInt(String string) {
		return Integer.parseInt(string);
	}
	
	private static float getFloat(String string) {
		return Float.parseFloat(string);
	}
}
