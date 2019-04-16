/**
 * MIT License
 *
 * Copyright (c) 2018 John Salomon - John´s Project
 *  
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.johnsproject.jpge2.importers;

import java.io.IOException;
import java.io.InputStream;

import com.johnsproject.jpge2.dto.Camera;
import com.johnsproject.jpge2.dto.Camera.CameraType;
import com.johnsproject.jpge2.dto.Face;
import com.johnsproject.jpge2.dto.Light;
import com.johnsproject.jpge2.dto.Material;
import com.johnsproject.jpge2.dto.Model;
import com.johnsproject.jpge2.dto.Scene;
import com.johnsproject.jpge2.dto.Transform;
import com.johnsproject.jpge2.dto.Vertex;
import com.johnsproject.jpge2.dto.Light.LightType;
import com.johnsproject.jpge2.processors.ColorProcessor;
import com.johnsproject.jpge2.processors.FileProcessor;
import com.johnsproject.jpge2.processors.MathProcessor;
import com.johnsproject.jpge2.processors.VectorProcessor;

public class SceneImporter {

	private static final byte vx = VectorProcessor.VECTOR_X;
	private static final byte vy = VectorProcessor.VECTOR_Y;
	private static final byte vz = VectorProcessor.VECTOR_Z;

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
		Scene scene = new Scene();
		Model[] models = parseModels(sceneData);
		Camera[] cameras = parseCameras(sceneData);
		Light[] lights = parseLights(sceneData);
		for (int i = 0; i < models.length; i++) {
			scene.addModel(models[i]);
		}
		for (int i = 0; i < cameras.length; i++) {
			scene.addCamera(cameras[i]);
		}
		for (int i = 0; i < lights.length; i++) {
			scene.addLight(lights[i]);
		}
		System.gc();
		return scene;
	}

	private static Model[] parseModels(String sceneData) {
		String[] modelsData = sceneData.split("model<");
		Model[] models = new Model[modelsData.length - 1];
		for (int i = 0; i < modelsData.length - 1; i++) {
			String modelData = modelsData[i + 1].split(">model")[0];
			String name = modelData.split("name<")[1].split(">name")[0];
			Transform transform = parseTransform(modelData.split("transform<")[1].split(">transform")[0].split(","));
			Material[] materials = parseMaterials(modelData.split("material<"));
			Vertex[] vertices = parseVertices(modelData.split("vertex<"), materials);
			Face[] faces = parseFaces(modelData.split("face<"), vertices, materials);
			models[i] = new Model(name, transform, vertices, faces, materials);
		}
		return models;
	}

	private static Camera[] parseCameras(String sceneData) {
		String[] camerasData = sceneData.split("camera<");
		Camera[] cameras = new Camera[camerasData.length - 1];
		for (int i = 0; i < camerasData.length - 1; i++) {
			String cameraData = camerasData[i + 1].split(">camera")[0];
			String name = cameraData.split("name<")[1].split(">name")[0];
			String typeData = cameraData.split("type<")[1].split(">type")[0];
			Transform transform = parseTransform(cameraData.split("transform<")[1].split(">transform")[0].split(","));
			Camera camera = new Camera(name, transform);
			if (typeData.equals("ORTHO"))
				camera.setType(CameraType.ORTHOGRAPHIC);
			if (typeData.equals("PERSP"))
				camera.setType(CameraType.PERSPECTIVE);
			cameras[i] = camera;
		}
		return cameras;
	}

	private static Light[] parseLights(String sceneData) {
		String[] lightsData = sceneData.split("light<");
		Light[] lights = new Light[lightsData.length - 1];
		for (int i = 0; i < lightsData.length - 1; i++) {
			String lightData = lightsData[i + 1].split(">light")[0];
			String name = lightData.split("name<")[1].split(">name")[0];
			String typeData = lightData.split("type<")[1].split(">type")[0];
			String strengthData = lightData.split("strength<")[1].split(">strength")[0];
			String[] colorData = lightData.split("color<")[1].split(">color")[0].split(",");
			int red = (int)(getFloat(colorData[0]) * 256);
			int green = (int)(getFloat(colorData[1]) * 256);
			int blue = (int)(getFloat(colorData[2]) * 256);
			Transform transform = parseTransform(lightData.split("transform<")[1].split(">transform")[0].split(","));
			Light light = new Light(name, transform);
			if (typeData.equals("SUN"))
				light.setType(LightType.DIRECTIONAL);
			if (typeData.equals("POINT"))
				light.setType(LightType.POINT);
			light.setStrength((int)(getFloat(strengthData) * MathProcessor.FP_VALUE));
			light.setDiffuseColor(ColorProcessor.convert(red, green, blue));
			lights[i] = light;
		}
		return lights;
	}

	private static Transform parseTransform(String[] transformData) {
		int x = (int)((getFloat(transformData[vx]) * 10) * MathProcessor.FP_VALUE);
		int y = (int)((getFloat(transformData[vy]) * 10) * MathProcessor.FP_VALUE);
		int z = (int)((getFloat(transformData[vz]) * 10) * MathProcessor.FP_VALUE);
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

	private static Vertex[] parseVertices(String[] verticesData, Material[] materials) {
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
			vertices[i] = new Vertex(i, location, normal, materials[material]);
		}
		return vertices;
	}

	private static Face[] parseFaces(String[] facesData, Vertex[] vertices, Material[] materials) {
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
			faces[i] = new Face(i, vertices[vertex1], vertices[vertex2], vertices[vertex3], materials[material], normal, uv1, uv2, uv3);
		}
		return faces;
	}

	private static Material[] parseMaterials(String[] materialsData) {
		Material[] materials = new Material[materialsData.length - 1];
		for (int i = 0; i < materialsData.length - 1; i++) {
			String[] materialData = materialsData[i + 1].split(">material")[0].split(",");
			String name = materialData[0];
			int red = (int)(getFloat(materialData[1]) * 256);
			int green = (int)(getFloat(materialData[2]) * 256);
			int blue = (int)(getFloat(materialData[3]) * 256);
			int alpha = (int)(getFloat(materialData[4]) * 256);
			int diffuse = (int)(getFloat(materialData[5]) * MathProcessor.FP_VALUE);
			int specular = (int)(getFloat(materialData[6]) * MathProcessor.FP_VALUE);
			int shininess = (int)(getFloat(materialData[7]) / 10);
			materials[i] = new Material(i, name, ColorProcessor.convert(red, green, blue, alpha), diffuse, specular, shininess, null);
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
