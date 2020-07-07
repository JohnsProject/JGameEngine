package com.johnsproject.jgameengine.io;

import static com.johnsproject.jgameengine.util.VectorUtils.*;

import java.io.IOException;
import java.io.InputStream;

import com.johnsproject.jgameengine.model.Face;
import com.johnsproject.jgameengine.model.Material;
import com.johnsproject.jgameengine.model.Mesh;
import com.johnsproject.jgameengine.model.Model;
import com.johnsproject.jgameengine.model.Transform;
import com.johnsproject.jgameengine.model.Vertex;
import com.johnsproject.jgameengine.util.ColorUtils;
import com.johnsproject.jgameengine.util.FileUtils;
import com.johnsproject.jgameengine.util.FixedPointUtils;
import com.johnsproject.jgameengine.util.VectorUtils;

/**
 * The SOMImporter class imports .som (Scene Object Mesh) files exported 
 * by Blender SOMExporter included in the Exporters folder.
 * 
 * @author John Ferraz Salomon
 *
 */
public final class SOMImporter {
	
	private SOMImporter() {	}
	
	/**
	 * Loads the .som file at the given path and returns a {@link Model} 
	 * containing the data of the file.
	 * 
	 * @param path
	 * @return
	 * @throws IOException
	 */
	public static Model load(String path) throws IOException {
		String content = FileUtils.readFile(path);
		return loadFromRaw(content);
	}

	/**
	 * Loads the .som file content from the given {@link InputStream} and returns a {@link Model} 
	 * containing the data of the stream.
	 * 
	 * @param stream
	 * @return
	 * @throws IOException
	 */
	public static Model load(InputStream stream) throws IOException {
		String content = FileUtils.readStream(stream);
		return loadFromRaw(content);
	}

	/**
	 * Loads the .som file content from the given string and returns a {@link Model} 
	 * containing the data of the string.
	 * 
	 * @param data string containing data of .som file.
	 * @return
	 * @throws IOException
	 */
	public static Model loadFromRaw(String data) throws IOException {
		String rawData = data.replace(" ", "").replace("\n", "");
		Material[] materials = parseMaterials(rawData);
		Vertex[] vertices = parseVertices(rawData, materials);
		Face[] faces = parseFaces(rawData, vertices, materials);
		int[] location = VectorUtils.emptyVector();
		int[] rotation = VectorUtils.emptyVector();
		int one = FixedPointUtils.FP_ONE;
		int[] scale = VectorUtils.toVector(one, one, one);
		Transform transform = new Transform(location, rotation, scale);
		Mesh mesh = new Mesh(vertices, faces, materials);
		Model result = new Model("Model", transform, mesh);
		System.gc();
		return result;
	}
	
	private static Vertex[] parseVertices(String rawData, Material[] materials) throws IOException {
		String vCountData = rawData.split("vCount<")[1].split(">vCount", 2)[0];
		Vertex[] vertices = new Vertex[getint(vCountData)];
		String[] vLocationData = rawData.split("vPosition<")[1].split(">vPosition", 2)[0].split(",");
		String[] vNormalData = rawData.split("vNormal<")[1].split(">vNormal", 2)[0].split(",");
		String[] vMaterialData = rawData.split("vMaterial<")[1].split(">vMaterial", 2)[0].split(",");
		for (int i = 0; i < vertices.length * 3; i += 3) {
			int[] location = VectorUtils.emptyVector();
			location[VECTOR_X] = -FixedPointUtils.toFixedPoint(getFloat(vLocationData[i + VECTOR_X]));
			location[VECTOR_Y] = -FixedPointUtils.toFixedPoint(getFloat(vLocationData[i + VECTOR_Y]));
			location[VECTOR_Z] = -FixedPointUtils.toFixedPoint(getFloat(vLocationData[i + VECTOR_Z]));
			int[] normal = VectorUtils.emptyVector();
			normal[VECTOR_X] = FixedPointUtils.toFixedPoint(getFloat(vNormalData[i + VECTOR_X]));
			normal[VECTOR_Y] = FixedPointUtils.toFixedPoint(getFloat(vNormalData[i + VECTOR_Y]));
			normal[VECTOR_Z] = FixedPointUtils.toFixedPoint(getFloat(vNormalData[i + VECTOR_Z]));
			int material = getint(vMaterialData[i / 3]);
			vertices[i / 3] = new Vertex(i / 3, location, normal, materials[material]);
		}
		return vertices;
	}
	
	private static Face[] parseFaces(String rawData, Vertex[] vertices, Material[] materials) throws IOException {
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
			final int vertex1 = getint(fVertex1Data[i / 6]);
			final int vertex2 = getint(fVertex2Data[i / 6]);
			final int vertex3 = getint(fVertex3Data[i / 6]);
			final Vertex[] faceVertices = new Vertex[] {vertices[vertex1], vertices[vertex2], vertices[vertex3]};
			final int[] normal = VectorUtils.emptyVector();
			normal[VECTOR_X] = FixedPointUtils.toFixedPoint(getFloat(fNormalData[(i / 2) + VECTOR_X]));
			normal[VECTOR_Y] = FixedPointUtils.toFixedPoint(getFloat(fNormalData[(i / 2) + VECTOR_Y]));
			normal[VECTOR_Z] = FixedPointUtils.toFixedPoint(getFloat(fNormalData[(i / 2) + VECTOR_Z]));
			final int[][] uvs = new int[3][];
			uvs[0] = VectorUtils.emptyVector();
			uvs[0][VECTOR_X] = FixedPointUtils.toFixedPoint(getFloat(fUV1Data[(i / 3) + VECTOR_X]));
			uvs[0][VECTOR_Y] = FixedPointUtils.toFixedPoint(getFloat(fUV1Data[(i / 3) + VECTOR_Y]));
			uvs[1] = VectorUtils.emptyVector();
			uvs[1][VECTOR_X] = FixedPointUtils.toFixedPoint(getFloat(fUV2Data[(i / 3) + VECTOR_X]));
			uvs[1][VECTOR_Y] = FixedPointUtils.toFixedPoint(getFloat(fUV2Data[(i / 3) + VECTOR_Y]));
			uvs[2] = VectorUtils.emptyVector();
			uvs[2][VECTOR_X] = FixedPointUtils.toFixedPoint(getFloat(fUV3Data[(i / 3) + VECTOR_X]));
			uvs[2][VECTOR_Y] = FixedPointUtils.toFixedPoint(getFloat(fUV3Data[(i / 3) + VECTOR_Y]));
			final int material = getint(fMaterialData[i / 6]);
			faces[i / 6] = new Face(i / 6, faceVertices, normal, uvs, materials[material]);
		}
		return faces;
	}
	
	private static Material[] parseMaterials(String rawData){
		String mCountData = rawData.split("mCount<")[1].split(">mCount", 2)[0];
		Material[] materials = new Material[getint(mCountData)];
		String[] mDiffuseColorData = rawData.split("mDiffuseColor<")[1].split(">mDiffuseColor", 2)[0].split(",");
		String[] mDiffuseIntensityData = rawData.split("mDiffuseIntensity<")[1].split(">mDiffuseIntensity", 2)[0].split(",");
		String[] mSpecularIntensityData = rawData.split("mSpecularIntensity<")[1].split(">mSpecularIntensity", 2)[0].split(",");
		for (int i = 0; i < materials.length * 4; i+=4) {
			// * 256 to get int rgb values
			int r = FixedPointUtils.toFixedPoint(getFloat(mDiffuseColorData[i]) * 256);
			int	g = FixedPointUtils.toFixedPoint(getFloat(mDiffuseColorData[i+1]) * 256);
			int	b = FixedPointUtils.toFixedPoint(getFloat(mDiffuseColorData[i+2]) * 256);
			int	a = FixedPointUtils.toFixedPoint(getFloat(mDiffuseColorData[i+3]) * 256);
			int diffuseIntensity = FixedPointUtils.toFixedPoint(getFloat(mDiffuseIntensityData[i / 4]));
			int specularIntensity = FixedPointUtils.toFixedPoint(getFloat(mSpecularIntensityData[i / 4]));
			Material material = new Material(i/4, "Material");
			material.setDiffuseColor(ColorUtils.toColor(a, r, g, b));
			material.setDiffuseIntensity(diffuseIntensity);
			material.setSpecularIntensity(specularIntensity);
			materials[i/4] = material;
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
