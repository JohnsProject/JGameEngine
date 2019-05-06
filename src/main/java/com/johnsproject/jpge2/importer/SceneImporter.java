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
package com.johnsproject.jpge2.importer;

import java.io.IOException;
import java.io.InputStream;

import com.johnsproject.jpge2.dto.Camera;
import com.johnsproject.jpge2.dto.CameraType;
import com.johnsproject.jpge2.dto.Face;
import com.johnsproject.jpge2.dto.Light;
import com.johnsproject.jpge2.dto.Material;
import com.johnsproject.jpge2.dto.Model;
import com.johnsproject.jpge2.dto.Scene;
import com.johnsproject.jpge2.dto.Transform;
import com.johnsproject.jpge2.dto.Vertex;
import com.johnsproject.jpge2.primitive.FPVector;
import com.johnsproject.jpge2.dto.LightType;
import com.johnsproject.jpge2.processor.ColorProcessor;
import com.johnsproject.jpge2.processor.FileProcessor;
import com.johnsproject.jpge2.processor.MathProcessor;
import com.johnsproject.jpge2.shader.SpecularShaderProperties;

public class SceneImporter {
	
	private static final byte VECTOR_X = FPVector.VECTOR_X;
	private static final byte VECTOR_Y = FPVector.VECTOR_Y;
	private static final byte VECTOR_Z = FPVector.VECTOR_Z;
	
	public SceneImporter() {}
	
	public Scene load(String path) throws IOException {
		String content = FileProcessor.readFile(path);
		return loadFromRaw(content);
	}

	public Scene load(InputStream stream) throws IOException {
		String content = FileProcessor.readStream(stream);
		return loadFromRaw(content);
	}

	public Scene loadFromRaw(String data) throws IOException {
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

	private Model[] parseModels(String sceneData) {
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

	private Camera[] parseCameras(String sceneData) {
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

	private Light[] parseLights(String sceneData) {
		String[] lightsData = sceneData.split("light<");
		Light[] lights = new Light[lightsData.length - 1];
		for (int i = 0; i < lightsData.length - 1; i++) {
			String lightData = lightsData[i + 1].split(">light")[0];
			String name = lightData.split("name<")[1].split(">name")[0];
			String typeData = lightData.split("type<")[1].split(">type")[0];
			String strengthData = lightData.split("strength<")[1].split(">strength")[0];
			String spotData = lightData.split("spot<")[1].split(">spot")[0];
			String blendData = lightData.split("blend<")[1].split(">blend")[0];
			String[] colorData = lightData.split("color<")[1].split(">color")[0].split(",");
			String[] shadowColorData = lightData.split("shadowColor<")[1].split(">shadowColor")[0].split(",");
			Transform transform = parseTransform(lightData.split("transform<")[1].split(">transform")[0].split(","));
			FPVector direction = FPVector.VECTOR_DOWN.clone();
			direction.rotateXYZ(transform.getRotation());
			Light light = new Light(name, transform);
			if (typeData.equals("SUN")) {
				light.setType(LightType.DIRECTIONAL);
			}
			if (typeData.equals("POINT")) {
				light.setType(LightType.POINT);
			}
			if (typeData.equals("SPOT")) {
				light.setType(LightType.SPOT);
			}
			light.setStrength(MathProcessor.generate(getFloat(strengthData)));
			int red = (int)(getFloat(colorData[0]) * 256);
			int green = (int)(getFloat(colorData[1]) * 256);
			int blue = (int)(getFloat(colorData[2]) * 256);
			light.setColor(ColorProcessor.generate(red, green, blue));
			red = (int)(getFloat(shadowColorData[0]) * 256);
			green = (int)(getFloat(shadowColorData[1]) * 256);
			blue = (int)(getFloat(shadowColorData[2]) * 256);
			light.setShadowColor(ColorProcessor.generate(red, green, blue));
			light.setSpotSize(MathProcessor.generate(getFloat(spotData)));
			light.setSpotSoftness(MathProcessor.generate(getFloat(blendData)));
			light.setDirection(direction);
			lights[i] = light;
		}
		return lights;
	}

	private Transform parseTransform(String[] transformData) {
		int x = MathProcessor.generate((getFloat(transformData[VECTOR_X]) * 10));
		int y = MathProcessor.generate((getFloat(transformData[VECTOR_Y]) * 10));
		int z = MathProcessor.generate((getFloat(transformData[VECTOR_Z]) * 10));
		FPVector location = new FPVector(-x, y, z);
		x = MathProcessor.generate(getFloat(transformData[3 + VECTOR_X]));
		y = MathProcessor.generate(getFloat(transformData[3 + VECTOR_Y]));
		z = MathProcessor.generate(getFloat(transformData[3 + VECTOR_Z]));
		FPVector rotation = new FPVector(x, y, z);
		x = MathProcessor.generate(getFloat(transformData[6 + VECTOR_X]) * 10);
		y = MathProcessor.generate(getFloat(transformData[6 + VECTOR_Y]) * 10);
		z = MathProcessor.generate(getFloat(transformData[6 + VECTOR_Z]) * 10);
		FPVector scale = new FPVector(x, y, z);
		return new Transform(location, rotation, scale);
	}

	private Vertex[] parseVertices(String[] verticesData, Material[] materials) {
		Vertex[] vertices = new Vertex[verticesData.length - 1];
		for (int i = 0; i < verticesData.length - 1; i++) {
			String[] vertexData = verticesData[i + 1].split(">vertex")[0].split(",");
			int x = MathProcessor.generate(getFloat(vertexData[VECTOR_X]));
			int y = MathProcessor.generate(getFloat(vertexData[VECTOR_Y]));
			int z = MathProcessor.generate(getFloat(vertexData[VECTOR_Z]));
			FPVector location = new FPVector(x, y, z);
			x = MathProcessor.generate(getFloat(vertexData[3 + VECTOR_X]));
			y = MathProcessor.generate(getFloat(vertexData[3 + VECTOR_Y]));
			z = MathProcessor.generate(getFloat(vertexData[3 + VECTOR_Z]));
			FPVector normal = new FPVector(x, y, z);
			int material = getInt(vertexData[6]);
			vertices[i] = new Vertex(i, location, normal, materials[material]);
		}
		return vertices;
	}

	private Face[] parseFaces(String[] facesData, Vertex[] vertices, Material[] materials) {
		Face[] faces = new Face[facesData.length - 1];
		for (int i = 0; i < facesData.length - 1; i++) {
			String[] faceData = facesData[i + 1].split(">face")[0].split(",");
			int vertex1 = getInt(faceData[0]);
			int vertex2 = getInt(faceData[1]);
			int vertex3 = getInt(faceData[2]);
			int x = MathProcessor.generate(getFloat(faceData[3 + VECTOR_X]));
			int y = MathProcessor.generate(getFloat(faceData[3 + VECTOR_Y]));
			int z = MathProcessor.generate(getFloat(faceData[3 + VECTOR_Z]));
			FPVector normal = new FPVector(x, y, z);
			x = MathProcessor.generate(getFloat(faceData[6 + VECTOR_X]));
			y = MathProcessor.generate(getFloat(faceData[6 + VECTOR_Y]));
			FPVector uv1 = new FPVector(x, y);
			x = MathProcessor.generate(getFloat(faceData[8 + VECTOR_X]));
			y = MathProcessor.generate(getFloat(faceData[8 + VECTOR_Y]));
			FPVector uv2 = new FPVector(x, y);
			x = MathProcessor.generate(getFloat(faceData[10 + VECTOR_X]));
			y = MathProcessor.generate(getFloat(faceData[10 + VECTOR_Y]));
			FPVector uv3 = new FPVector(x, y);
			int material = getInt(faceData[12]);
			faces[i] = new Face(i, vertices[vertex1], vertices[vertex2], vertices[vertex3], materials[material], normal, uv1, uv2, uv3);
		}
		return faces;
	}

	private Material[] parseMaterials(String[] materialsData) {
		Material[] materials = new Material[materialsData.length - 1];
		for (int i = 0; i < materialsData.length - 1; i++) {
			String[] materialData = materialsData[i + 1].split(">material")[0].split(",");
			String name = materialData[0];
			int red = (int)(getFloat(materialData[1]) * 256);
			int green = (int)(getFloat(materialData[2]) * 256);
			int blue = (int)(getFloat(materialData[3]) * 256);
			int alpha = (int)(getFloat(materialData[4]) * 256);
			int diffuse = MathProcessor.generate(getFloat(materialData[5]));
			int specular = MathProcessor.generate(getFloat(materialData[6]));
			int shininess = MathProcessor.generate(getFloat(materialData[7]) / 10);
			SpecularShaderProperties properties = new SpecularShaderProperties(ColorProcessor.generate(alpha, red, green, blue), diffuse, specular, shininess, null);
			materials[i] = new Material(i, name, 0, properties);
		}
		return materials;
	}

	private int getInt(String string) {
		return Integer.parseInt(string);
	}

	private float getFloat(String string) {
		return Float.parseFloat(string);
	}
}
