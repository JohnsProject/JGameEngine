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
package com.johnsproject.jgameengine.importer;

import java.io.IOException;
import java.io.InputStream;

import com.johnsproject.jgameengine.library.ColorLibrary;
import com.johnsproject.jgameengine.library.FileLibrary;
import com.johnsproject.jgameengine.library.MathLibrary;
import com.johnsproject.jgameengine.library.MatrixLibrary;
import com.johnsproject.jgameengine.library.VectorLibrary;
import com.johnsproject.jgameengine.model.Animation;
import com.johnsproject.jgameengine.model.AnimationFrame;
import com.johnsproject.jgameengine.model.Armature;
import com.johnsproject.jgameengine.model.Camera;
import com.johnsproject.jgameengine.model.CameraType;
import com.johnsproject.jgameengine.model.Face;
import com.johnsproject.jgameengine.model.Light;
import com.johnsproject.jgameengine.model.LightType;
import com.johnsproject.jgameengine.model.Material;
import com.johnsproject.jgameengine.model.Mesh;
import com.johnsproject.jgameengine.model.Model;
import com.johnsproject.jgameengine.model.Scene;
import com.johnsproject.jgameengine.model.Transform;
import com.johnsproject.jgameengine.model.Vertex;
import com.johnsproject.jgameengine.model.VertexGroup;
import com.johnsproject.jgameengine.shader.GouraudSpecularShader;
import com.johnsproject.jgameengine.shader.SpecularProperties;

/**
 * The SceneImporter class imports .scene files exported 
 * by Blender SceneExporter included in the Exporters folder.
 * 
 * @author John Ferraz Salomon
 *
 */
public class SceneImporter {
	
	private static final byte VECTOR_X = VectorLibrary.VECTOR_X;
	private static final byte VECTOR_Y = VectorLibrary.VECTOR_Y;
	private static final byte VECTOR_Z = VectorLibrary.VECTOR_Z;

	private final VectorLibrary vectorLibrary;
	private final MatrixLibrary matrixLibrary;
	
	public SceneImporter() {
		this.vectorLibrary = new VectorLibrary();
		this.matrixLibrary = new MatrixLibrary();
	}
	
	/**
	 * Loads the .scene file at the given path and returns a {@link Scene} 
	 * containing the data of the file.
	 * 
	 * @param path
	 * @return
	 * @throws IOException
	 */
	public Scene load(String path) throws IOException {
		String content = new FileLibrary().readFile(path);
		return loadFromRaw(content);
	}

	/**
	 * Loads the .scene file content from the given {@link InputStream} and returns a {@link Scene} 
	 * containing the data of the file.
	 * 
	 * @param stream
	 * @return
	 * @throws IOException
	 */
	public Scene load(InputStream stream) throws IOException {
		String content = new FileLibrary().readStream(stream);
		return loadFromRaw(content);
	}

	/**
	 * Loads the .scene file content from the given string and returns a {@link Scene} 
	 * containing the data of the string.
	 * 
	 * @param stream
	 * @return
	 * @throws IOException
	 */
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
			Material[] materials = parseMaterials(modelData.split("material<")[1].split(">material")[0].split("><"));
			Vertex[] vertices = parseVertices(modelData.split("vertex<")[1].split(">vertex")[0].split("><"), materials);
			Face[] faces = parseFaces(modelData.split("face<")[1].split(">face")[0].split("><"), vertices, materials);
			VertexGroup[] vertexGroups = parseVertexGroups(modelData.split("vertexGroup<")[1].split(">vertexGroup")[0].split("><"), vertices);
			Animation[] animations = parseAnimations(modelData.split("animation<")[1].split(">animation")[0].split("><"));
			Mesh mesh = new Mesh(vertices, faces, materials);
			Armature armature = new Armature(vertexGroups, animations);
			models[i] = new Model(name, transform, mesh, armature);
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
			int[] direction = VectorLibrary.generate(0, 0, 0, 0);
			vectorLibrary.rotateXYZ(VectorLibrary.VECTOR_DOWN, transform.getRotation(), direction);
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
			light.setStrength(MathLibrary.generate(Float.parseFloat(strengthData)));
			int red = (int)(Float.parseFloat(colorData[0]) * 256);
			int green = (int)(Float.parseFloat(colorData[1]) * 256);
			int blue = (int)(Float.parseFloat(colorData[2]) * 256);
			light.setColor(ColorLibrary.generate(red, green, blue));
			red = (int)(Float.parseFloat(shadowColorData[0]) * 256);
			green = (int)(Float.parseFloat(shadowColorData[1]) * 256);
			blue = (int)(Float.parseFloat(shadowColorData[2]) * 256);
			light.setShadowColor(ColorLibrary.generate(red, green, blue));
			light.setSpotSize(MathLibrary.generate(Float.parseFloat(spotData)));
			light.setSpotSoftness(MathLibrary.generate(Float.parseFloat(blendData)));
			light.setDirection(direction);
			lights[i] = light;
		}
		return lights;
	}

	private Transform parseTransform(String[] transformData) {
		int x = MathLibrary.generate(Float.parseFloat(transformData[VECTOR_X]));
		int y = MathLibrary.generate(Float.parseFloat(transformData[VECTOR_Y]));
		int z = MathLibrary.generate(Float.parseFloat(transformData[VECTOR_Z]));
		int[] location = VectorLibrary.generate(-x, y, z);
		x = MathLibrary.generate(Float.parseFloat(transformData[3 + VECTOR_X]));
		y = MathLibrary.generate(Float.parseFloat(transformData[3 + VECTOR_Y]));
		z = MathLibrary.generate(Float.parseFloat(transformData[3 + VECTOR_Z]));
		int[] rotation = VectorLibrary.generate(x, y, z);
		x = MathLibrary.generate(Float.parseFloat(transformData[6 + VECTOR_X]));
		y = MathLibrary.generate(Float.parseFloat(transformData[6 + VECTOR_Y]));
		z = MathLibrary.generate(Float.parseFloat(transformData[6 + VECTOR_Z]));
		int[] scale = VectorLibrary.generate(x, y, z);
		return new Transform(location, rotation, scale);
	}

	private Vertex[] parseVertices(String[] verticesData, Material[] materials) {
		Vertex[] vertices = new Vertex[verticesData.length];
		for (int i = 0; i < vertices.length; i++) {
			String[] vertexData = verticesData[i].split(",");
			int x = MathLibrary.generate(Float.parseFloat(vertexData[VECTOR_X]));
			int y = MathLibrary.generate(Float.parseFloat(vertexData[VECTOR_Y]));
			int z = MathLibrary.generate(Float.parseFloat(vertexData[VECTOR_Z]));
			int[] location = VectorLibrary.generate(-x, y, z);
			x = MathLibrary.generate(Float.parseFloat(vertexData[3 + VECTOR_X]));
			y = MathLibrary.generate(Float.parseFloat(vertexData[3 + VECTOR_Y]));
			z = MathLibrary.generate(Float.parseFloat(vertexData[3 + VECTOR_Z]));
			int[] normal = VectorLibrary.generate(-x, y, z, 0);
			int material = Integer.parseInt(vertexData[6]);
			vertices[i] = new Vertex(i, location, normal, materials[material]);
		}
		return vertices;
	}

	private Face[] parseFaces(String[] facesData, Vertex[] vertices, Material[] materials) {
		Face[] faces = new Face[facesData.length];
		for (int i = 0; i < faces.length; i++) {
			String[] faceData = facesData[i].split(",");
			int vertex1 = Integer.parseInt(faceData[0]);
			int vertex2 = Integer.parseInt(faceData[1]);
			int vertex3 = Integer.parseInt(faceData[2]);
			int x = MathLibrary.generate(Float.parseFloat(faceData[3 + VECTOR_X]));
			int y = MathLibrary.generate(Float.parseFloat(faceData[3 + VECTOR_Y]));
			int z = MathLibrary.generate(Float.parseFloat(faceData[3 + VECTOR_Z]));
			int[] normal = VectorLibrary.generate(-x, y, z, 0);
			x = MathLibrary.generate(Float.parseFloat(faceData[6 + VECTOR_X]));
			y = MathLibrary.generate(Float.parseFloat(faceData[6 + VECTOR_Y]));
			int[] uv1 = VectorLibrary.generate(x, MathLibrary.FP_ONE - y);
			x = MathLibrary.generate(Float.parseFloat(faceData[8 + VECTOR_X]));
			y = MathLibrary.generate(Float.parseFloat(faceData[8 + VECTOR_Y]));
			int[] uv2 = VectorLibrary.generate(x, MathLibrary.FP_ONE - y);
			x = MathLibrary.generate(Float.parseFloat(faceData[10 + VECTOR_X]));
			y = MathLibrary.generate(Float.parseFloat(faceData[10 + VECTOR_Y]));
			int[] uv3 = VectorLibrary.generate(x, MathLibrary.FP_ONE - y);
			int material = Integer.parseInt(faceData[12]);
			faces[i] = new Face(i, vertices[vertex1], vertices[vertex2], vertices[vertex3], materials[material], normal, uv1, uv2, uv3);
		}
		return faces;
	}

	private Material[] parseMaterials(String[] materialsData) {
		Material[] materials = new Material[materialsData.length];
		for (int i = 0; i < materials.length; i++) {
			String[] materialData = materialsData[i].split(",");
			String name = materialData[0];
			int red = (int)(Float.parseFloat(materialData[1]) * 256);
			int green = (int)(Float.parseFloat(materialData[2]) * 256);
			int blue = (int)(Float.parseFloat(materialData[3]) * 256);
			int alpha = (int)(Float.parseFloat(materialData[4]) * 256);
			int diffuse = MathLibrary.generate(Float.parseFloat(materialData[5]));
			int specular = MathLibrary.generate(Float.parseFloat(materialData[6]));
			int shininess = MathLibrary.generate(Float.parseFloat(materialData[7]) / 10);
			GouraudSpecularShader shader = new GouraudSpecularShader();
			SpecularProperties properties = (SpecularProperties) shader.getProperties();
			properties.setDiffuseColor(ColorLibrary.generate(alpha, red, green, blue));
			properties.setDiffuseIntensity(diffuse);
			properties.setSpecularIntensity(specular);
			properties.setShininess(shininess);
			materials[i] = new Material(i, name, shader);
		}
		return materials;
	}
	
	private VertexGroup[] parseVertexGroups(String[] vertexGroupsData, Vertex[] meshVertices) {
		VertexGroup[] vertexGroups = new VertexGroup[vertexGroupsData.length];
		for (int i = 0; i < vertexGroups.length; i++) {
			String[] vertexGroupData = vertexGroupsData[i].split(",");
			int boneIndex = Integer.parseInt(vertexGroupData[0]);
			int vertexCount = Integer.parseInt(vertexGroupData[1]);
			Vertex[] vertices = new Vertex[vertexCount];
			for (int j = 0; j < vertexCount; j++) {
				vertices[j] = meshVertices[Integer.parseInt(vertexGroupData[j + 2])];
			}
			int[] weights = new int[vertexCount];
			for (int j = 0; j < vertexCount; j++) {
				weights[j] = MathLibrary.generate(Float.parseFloat(vertexGroupData[j + vertexCount + 2]));
			}
			vertexGroups[i] = new VertexGroup(boneIndex, vertices, weights);
		}
		return vertexGroups;
	}
	
	private Animation[] parseAnimations(String[] animationsData) {
		Animation[] animations = new Animation[animationsData.length];
		for (int i = 0; i < animations.length; i++) {
			String[] animationData = animationsData[i].split(",");
			String name = animationData[0];
			int bonesCount = Integer.parseInt(animationData[1]);
			int framesCount = Integer.parseInt(animationData[2]);
			AnimationFrame[] frames = new AnimationFrame[framesCount];
			for (int f = 3, fi = 0; f < animationData.length; f += bonesCount * 9, fi++) {
				int[][] boneRotationMatrices = new int[bonesCount][MatrixLibrary.MATRIX_SIZE];
				for (int b = f, bi = 0; b < f + bonesCount * 9; b += 9, bi++) {
					int x = MathLibrary.generate(Float.parseFloat(animationData[b + VECTOR_X]));
					int y = MathLibrary.generate(Float.parseFloat(animationData[b + VECTOR_Y]));
					int z = MathLibrary.generate(Float.parseFloat(animationData[b + VECTOR_Z]));
					int[] location = VectorLibrary.generate(-x, y, z);
					x = MathLibrary.generate(Float.parseFloat(animationData[b + 3 + VECTOR_X]));
					y = MathLibrary.generate(Float.parseFloat(animationData[b + 3 + VECTOR_Y]));
					z = MathLibrary.generate(Float.parseFloat(animationData[b + 3 + VECTOR_Z]));
					int[] rotation = VectorLibrary.generate(x, y, z);
					x = MathLibrary.generate(Float.parseFloat(animationData[b + 6 + VECTOR_X]));
					y = MathLibrary.generate(Float.parseFloat(animationData[b + 6 + VECTOR_Y]));
					z = MathLibrary.generate(Float.parseFloat(animationData[b + 6 + VECTOR_Z]));
					int[] scale = VectorLibrary.generate(x, y, z);
					int[] boneRotationMatrix = MatrixLibrary.generate();
					matrixLibrary.scale(boneRotationMatrix, scale, boneRotationMatrix);
					matrixLibrary.rotateXYZ(boneRotationMatrix, rotation, boneRotationMatrix);
					matrixLibrary.translate(boneRotationMatrix, location, boneRotationMatrix);
					boneRotationMatrices[bi] = boneRotationMatrix;
				}
				frames[fi] = new AnimationFrame(boneRotationMatrices);
			}
			animations[i] = new Animation(name, frames);
		}
		return animations;
	}
}
