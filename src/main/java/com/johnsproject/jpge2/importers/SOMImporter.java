package com.johnsproject.jpge2.importers;

import java.io.IOException;
import java.io.InputStream;

import com.johnsproject.jpge2.dto.Face;
import com.johnsproject.jpge2.dto.Material;
import com.johnsproject.jpge2.dto.Model;
import com.johnsproject.jpge2.dto.Texture;
import com.johnsproject.jpge2.dto.Transform;
import com.johnsproject.jpge2.dto.Vertex;
import com.johnsproject.jpge2.processing.FileProcessor;
import com.johnsproject.jpge2.processing.MathProcessor;
import com.johnsproject.jpge2.processing.VectorProcessor;
import com.johnsproject.jpge2.processing.ColorProcessor;

public class SOMImporter {

	private static final int vx = VectorProcessor.VECTOR_X;
	private static final int vy = VectorProcessor.VECTOR_Y;
	private static final int vz = VectorProcessor.VECTOR_Z;
	
	public static Model load(String path) throws IOException {
		String content = FileProcessor.readFile(path);
		return loadFromRaw(content);
	}

	public static Model load(InputStream stream) throws IOException {
		String content = FileProcessor.readStream(stream);
		return loadFromRaw(content);
	}

	public static Model loadFromRaw(String data) throws IOException {
		String rawData = data.replace(" ", "").replace("\n", "");
		Vertex[] vertexes = parseVertices(rawData);
		Face[] faces = parseFaces(rawData);
		Material[] materials = parseMaterials(rawData);
		Model result = new Model("Model", new Transform(), vertexes, faces, materials);
		System.gc();
		return result;
	}
	
	static Vertex[] parseVertices(String rawData) throws IOException {
		String vCountData = rawData.split("vCount<")[1].split(">vCount", 2)[0];
		Vertex[] vertices = new Vertex[getint(vCountData)];
		String[] vLocationData = rawData.split("vPosition<")[1].split(">vPosition", 2)[0].split(",");
		String[] vNormalData = rawData.split("vNormal<")[1].split(">vNormal", 2)[0].split(",");
		String[] vMaterialData = rawData.split("vMaterial<")[1].split(">vMaterial", 2)[0].split(",");
		for (int i = 0; i < vertices.length * 3; i += 3) {
			int[] location = VectorProcessor.generate();
			location[vx] = (int)(getFloat(vLocationData[i + vx]) * MathProcessor.FP_VALUE);
			location[vy] = (int)(getFloat(vLocationData[i + vy]) * MathProcessor.FP_VALUE);
			location[vz] = (int)(getFloat(vLocationData[i + vz]) * MathProcessor.FP_VALUE);
			int[] normal = VectorProcessor.generate();
			normal[vx] = (int)(getFloat(vNormalData[i + vx]) * MathProcessor.FP_VALUE);
			normal[vy] = (int)(getFloat(vNormalData[i + vy]) * MathProcessor.FP_VALUE);
			normal[vz] = (int)(getFloat(vNormalData[i + vz]) * MathProcessor.FP_VALUE);
			int material = getint(vMaterialData[i / 3]);
			vertices[i / 3] = new Vertex(i / 3, location, normal, material);
		}
		return vertices;
	}
	
	static Face[] parseFaces(String rawData) throws IOException {
		String fCountData = rawData.split("fCount<")[1].split(">fCount", 2)[0];
		Face[] faces = new Face[getint(fCountData)];
		String[] fVertex1Data = rawData.split("fVertex1<")[1].split(">fVertex1", 2)[0].split(",");
		String[] fVertex2Data = rawData.split("fVertex2<")[1].split(">fVertex2", 2)[0].split(",");
		String[] fVertex3Data = rawData.split("fVertex3<")[1].split(">fVertex3", 2)[0].split(",");
		String[] fMaterialData = rawData.split("fMaterial<")[1].split(">fMaterial", 2)[0].split(",");
		String[] fUV1Data = rawData.split("fUV1<")[1].split(">fUV1", 2)[0].split(",");
		String[] fUV2Data = rawData.split("fUV2<")[1].split(">fUV2", 2)[0].split(",");
		String[] fUV3Data = rawData.split("fUV3<")[1].split(">fUV3", 2)[0].split(",");
		for (int i = 0; i < faces.length * 2; i += 2) {
			int vertex1 = getint(fVertex1Data[i / 2]);
			int vertex2 = getint(fVertex2Data[i / 2]);
			int vertex3 = getint(fVertex3Data[i / 2]);
			int material = getint(fMaterialData[i / 2]);
			int[] uv1 = new int[2];
			uv1[vx] = (int)(getFloat(fUV1Data[i + vx]) * MathProcessor.FP_VALUE);
			uv1[vy] = (int)(getFloat(fUV1Data[i + vy]) * MathProcessor.FP_VALUE);
			int[] uv2 = new int[2];
			uv2[vx] = (int)(getFloat(fUV2Data[i + vx]) * MathProcessor.FP_VALUE);
			uv2[vy] = (int)(getFloat(fUV2Data[i + vy]) * MathProcessor.FP_VALUE);
			int[] uv3 = new int[2];
			uv3[vx] = (int)(getFloat(fUV3Data[i + vx]) * MathProcessor.FP_VALUE);
			uv3[vy] = (int)(getFloat(fUV3Data[i + vy]) * MathProcessor.FP_VALUE);
			faces[i / 2] = new Face(i / 2, vertex1, vertex2, vertex3, material, uv1, uv2, uv3);
		}
		return faces;
	}
	
	static Material[] parseMaterials(String rawData){
		String mCountData = rawData.split("mCount<")[1].split(">mCount", 2)[0];
		Material[] materials = new Material[getint(mCountData)];
		String[] mColorData = rawData.split("mColor<")[1].split(">mColor", 2)[0].split(",");
		if (materials.length > 0) {
			for (int i = 0; i < materials.length * 4; i+=4) {
				// *255 to get int rgb values
				int r = (int)(getFloat(mColorData[i]) * 255);
				int	g = (int)(getFloat(mColorData[i+1]) * 255);
				int	b = (int)(getFloat(mColorData[i+2]) * 255);
				int	a = (int)(getFloat(mColorData[i+3]) * 255);
				materials[i/4] = new Material(i/4, ColorProcessor.convert(r, g, b, a), new Texture(10, 10));
			}
		}else {
			materials = new Material[] {new Material(0, ColorProcessor.convert(0, 0, 0, 255), new Texture(10, 10))};
		}
		return materials;
	}
	
	private static int getint(String string) {
		return Integer.parseInt(string);
	}
	
	private static float getFloat(String string) {
		return Float.parseFloat(string);
	}
}
