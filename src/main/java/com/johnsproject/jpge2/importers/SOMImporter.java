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

import com.johnsproject.jpge2.dto.Face;
import com.johnsproject.jpge2.dto.Material;
import com.johnsproject.jpge2.dto.Model;
import com.johnsproject.jpge2.dto.Transform;
import com.johnsproject.jpge2.dto.Vertex;
import com.johnsproject.jpge2.processors.ColorProcessor;
import com.johnsproject.jpge2.processors.FileProcessor;
import com.johnsproject.jpge2.processors.MathProcessor;
import com.johnsproject.jpge2.processors.VectorProcessor;

public class SOMImporter {

	private static final byte vx = VectorProcessor.VECTOR_X;
	private static final byte vy = VectorProcessor.VECTOR_Y;
	private static final byte vz = VectorProcessor.VECTOR_Z;
	
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
		Material[] materials = parseMaterials(rawData);
		Vertex[] vertices = parseVertices(rawData, materials);
		Face[] faces = parseFaces(rawData, vertices, materials);
		Model result = new Model("Model", new Transform(), vertices, faces, materials);
		System.gc();
		return result;
	}
	
	static Vertex[] parseVertices(String rawData, Material[] materials) throws IOException {
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
			vertices[i / 3] = new Vertex(i / 3, location, normal, materials[material]);
		}
		return vertices;
	}
	
	static Face[] parseFaces(String rawData, Vertex[] vertices, Material[] materials) throws IOException {
		String fCountData = rawData.split("fCount<")[1].split(">fCount", 2)[0];
		Face[] faces = new Face[getint(fCountData)];
		String[] fVertex1Data = rawData.split("fVertex1<")[1].split(">fVertex1", 2)[0].split(",");
		String[] fVertex2Data = rawData.split("fVertex2<")[1].split(">fVertex2", 2)[0].split(",");
		String[] fVertex3Data = rawData.split("fVertex3<")[1].split(">fVertex3", 2)[0].split(",");
		String[] fMaterialData = rawData.split("fMaterial<")[1].split(">fMaterial", 2)[0].split(",");
		String[] fNormalData = rawData.split("fNormal<")[1].split(">fNormal", 2)[0].split(",");
		String[] fUV1Data = rawData.split("fUV1<")[1].split(">fUV1", 2)[0].split(",");
		String[] fUV2Data = rawData.split("fUV2<")[1].split(">fUV2", 2)[0].split(",");
		String[] fUV3Data = rawData.split("fUV3<")[1].split(">fUV3", 2)[0].split(",");
		for (int i = 0; i < faces.length * 6; i += 6) {
			int vertex1 = getint(fVertex1Data[i / 6]);
			int vertex2 = getint(fVertex2Data[i / 6]);
			int vertex3 = getint(fVertex3Data[i / 6]);
			int material = getint(fMaterialData[i / 6]);
			int[] normal = VectorProcessor.generate();
			normal[vx] = (int)(getFloat(fNormalData[(i / 2) + vx]) * MathProcessor.FP_VALUE);
			normal[vy] = (int)(getFloat(fNormalData[(i / 2) + vy]) * MathProcessor.FP_VALUE);
			normal[vz] = (int)(getFloat(fNormalData[(i / 2) + vz]) * MathProcessor.FP_VALUE);
			int[] uv1 = VectorProcessor.generate();
			uv1[vx] = (int)(getFloat(fUV1Data[(i / 3) + vx]) * MathProcessor.FP_VALUE);
			uv1[vy] = (int)(getFloat(fUV1Data[(i / 3) + vy]) * MathProcessor.FP_VALUE);
			int[] uv2 = VectorProcessor.generate();
			uv2[vx] = (int)(getFloat(fUV2Data[(i / 3) + vx]) * MathProcessor.FP_VALUE);
			uv2[vy] = (int)(getFloat(fUV2Data[(i / 3) + vy]) * MathProcessor.FP_VALUE);
			int[] uv3 = VectorProcessor.generate();
			uv3[vx] = (int)(getFloat(fUV3Data[(i / 3) + vx]) * MathProcessor.FP_VALUE);
			uv3[vy] = (int)(getFloat(fUV3Data[(i / 3) + vy]) * MathProcessor.FP_VALUE);
			faces[i / 6] = new Face(i / 6, vertices[vertex1], vertices[vertex2], vertices[vertex3], materials[material], normal, uv1, uv2, uv3);
		}
		return faces;
	}
	
	static Material[] parseMaterials(String rawData){
		String mCountData = rawData.split("mCount<")[1].split(">mCount", 2)[0];
		Material[] materials = new Material[getint(mCountData)];
		String[] mDiffuseColorData = rawData.split("mDiffuseColor<")[1].split(">mDiffuseColor", 2)[0].split(",");
		String[] mDiffuseIntensityData = rawData.split("mDiffuseIntensity<")[1].split(">mDiffuseIntensity", 2)[0].split(",");
		String[] mSpecularIntensityData = rawData.split("mSpecularIntensity<")[1].split(">mSpecularIntensity", 2)[0].split(",");
		for (int i = 0; i < materials.length * 4; i+=4) {
			// * 256 to get int rgb values
			int r = (int)(getFloat(mDiffuseColorData[i]) * 256);
			int	g = (int)(getFloat(mDiffuseColorData[i+1]) * 256);
			int	b = (int)(getFloat(mDiffuseColorData[i+2]) * 256);
			int	a = (int)(getFloat(mDiffuseColorData[i+3]) * 256);
			int diffuseIntensity = (int)(getFloat(mDiffuseIntensityData[i / 4]) * MathProcessor.FP_VALUE);
			int specularIntensity = (int)(getFloat(mSpecularIntensityData[i / 4]) * MathProcessor.FP_VALUE);
			materials[i/4] = new Material(i/4, ColorProcessor.convert(r, g, b, a), diffuseIntensity, specularIntensity, 0, null);
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
