package com.johnsproject.jgameengine.io;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.johnsproject.jgameengine.model.Face;
import com.johnsproject.jgameengine.model.Material;
import com.johnsproject.jgameengine.model.Mesh;
import com.johnsproject.jgameengine.model.Vertex;
import com.johnsproject.jgameengine.util.ColorUtils;
import com.johnsproject.jgameengine.util.FileUtils;
import com.johnsproject.jgameengine.util.FixedPointUtils;
import com.johnsproject.jgameengine.util.VectorUtils;

public final class OBJImporter {


	private static final String OBJECT_FILE = ".obj";
	private static final String MATERIAL_FILE = ".mtl";
	
	private static final String KEYWORD_COMMENT = "#";
	
	private static final String KEYWORD_MATERIAL = "newmtl";
	private static final String KEYWORD_MATERIAL_SHININESS = "Ns";
	private static final String KEYWORD_MATERIAL_AMBIENT = "Ka";
	private static final String KEYWORD_MATERIAL_DIFFUSE = "Kd";
	private static final String KEYWORD_MATERIAL_SPECULAR = "Ks";
	
	private static final String KEYWORD_VERTEX = "v";

	private static final String KEYWORD_FACE = "f";
	private static final String KEYWORD_FACE_SEPARATOR = "/";
	private static final String KEYWORD_FACE_MATERIAL = "usemtl";
	private static final String KEYWORD_FACE_NORMAL = "vn";
	private static final String KEYWORD_FACE_UV = "vt";
	
	static class VertexData {
		
		public int index;
		public int[] location;
		public int materialIndex;
		
	}
	
	static class FaceNormal {
		
		public int index;
		public int[] normal;
		
	}
	
	static class FaceUV {
		
		public int index;
		public int[] uv;
		
	}
	
	static class FaceData {
		
		public int index;
		public int[] normalIndices;
		public int[] uvIndices;
		public int[] vertexIndices;
		public Material material;
		
	}
	
	private OBJImporter() { }
	
	/**
	 * Parses the <code>.obj</code> file at the specified path and returns a {@link Model}.
	 * 
	 * @param path to the file.
	 * @return A Model representation of the data in the specified path.
	 * @throws IOException If the file does not exist, is a directory rather than a regular file,
	 * or for some other reason cannot be opened for reading.
	 */
	public static Mesh parse(String path) throws IOException {
		final String materialData = FileUtils.readFile(path.replace(OBJECT_FILE, MATERIAL_FILE));
		final String objectData = FileUtils.readFile(path);
		return parseMesh(objectData, materialData);
	}
	
	static Mesh parseMesh(String objectData, String materialData) {
		final List<Material> materialsData = parseMaterials(materialData);
		final List<VertexData> verticesData = parseVertices(objectData);
		final List<FaceNormal> faceNormals = parseFaceNormals(objectData);
		final List<FaceUV> faceUVs = parseFaceUVs(objectData);
		final List<FaceData> facesData = parseFaces(objectData, materialsData);
		final Material[] materials = materialsData.toArray(new Material[0]);
		final Vertex[] vertices = createVertices(verticesData, facesData, materials);
		final Face[] faces = createFaces(facesData, vertices, faceNormals, faceUVs);
		return new Mesh(vertices, faces, materials);
	}
	
	static List<Material> parseMaterials(String data) {
		final List<Material> materials = new ArrayList<Material>();
		final String[] lines = getLines(data);
		Material material = null;
		for (String line : lines) {
			if(line.contains(KEYWORD_COMMENT))
				continue;
			final String[] lineData = getLineData(line);
			if(material != null) {
				final boolean isMaterialParsed = parseMaterial(lineData, material);
				if(isMaterialParsed) {
					materials.add(material);
					material = null;
				}
			}
			if(lineData[0].equals(KEYWORD_MATERIAL)) {
				material = new Material(materials.size(), lineData[1]);
			}
		}
		return materials;
	}
	
	private static boolean parseMaterial(String[] lineData, Material material) {
		if(lineData[0].equals(KEYWORD_MATERIAL_SHININESS)) {
			material.setShininess(FixedPointUtils.toFixedPoint(lineData[1]));
		} 
		else if(lineData[0].equals(KEYWORD_MATERIAL_DIFFUSE)) {
			material.setDiffuseColor(parseColor(lineData));
		}
		else if(lineData[0].equals(KEYWORD_MATERIAL_SPECULAR)) {
			material.setSpecularColor(parseColor(lineData));
		}
		else if(!lineData[0].equals(KEYWORD_MATERIAL_AMBIENT)) {
			return true;
		}
		return false;
	}
	
	static List<VertexData> parseVertices(String data) {
		final List<VertexData> vertices = new ArrayList<VertexData>();
		final String[] lines = getLines(data);
		for (String line : lines) {
			if(line.contains(KEYWORD_COMMENT))
				continue;
			final String[] lineData = getLineData(line);
			if(lineData[0].equals(KEYWORD_VERTEX)) {
				final VertexData vertex = new VertexData();
				vertex.index = vertices.size();
				vertex.location = parseVector(lineData);
				vertices.add(vertex);
			}
		}
		return vertices;		
	}
	
	static List<FaceNormal> parseFaceNormals(String data) {
		final List<FaceNormal> faceNormals = new ArrayList<FaceNormal>();
		final String[] lines = getLines(data);
		for (String line : lines) {
			if(line.contains(KEYWORD_COMMENT))
				continue;
			final String[] lineData = getLineData(line);
			if(lineData[0].equals(KEYWORD_FACE_NORMAL)) {
				final FaceNormal faceNormal = new FaceNormal();
				faceNormal.index = faceNormals.size();
				faceNormal.normal = parseVector(lineData);
				faceNormals.add(faceNormal);
			}
		}
		return faceNormals;		
	}
	
	static List<FaceUV> parseFaceUVs(String data) {
		final List<FaceUV> faceUVs = new ArrayList<FaceUV>();
		final String[] lines = getLines(data);
		for (String line : lines) {
			if(line.contains(KEYWORD_COMMENT))
				continue;
			final String[] lineData = getLineData(line);
			if(lineData[0].equals(KEYWORD_FACE_UV)) {
				final FaceUV faceUV = new FaceUV();
				faceUV.index = faceUVs.size();
				faceUV.uv = parseVector(lineData);
				faceUVs.add(faceUV);
			}
		}
		return faceUVs;		
	}
	
	static List<FaceData> parseFaces(String data, List<Material> materials) {
		final List<FaceData> faces = new ArrayList<FaceData>();
		final String[] lines = getLines(data);
		Material material = null;
		for (String line : lines) {
			if(line.contains(KEYWORD_COMMENT))
				continue;
			final String[] lineData = getLineData(line);
			if(lineData[0].equals(KEYWORD_FACE_MATERIAL)) {
				material = getMaterial(materials, lineData[1]);
			}
			else if(lineData[0].equals(KEYWORD_FACE)) {
				final FaceData face = new FaceData();
				face.index = faces.size();
				face.normalIndices = new int[3];
				face.uvIndices = new int[3];
				face.vertexIndices = new int[3];
				parseFaceVertex(0, lineData[1], face);
				parseFaceVertex(1, lineData[2], face);
				parseFaceVertex(2, lineData[3], face);
				face.material = material;
				faces.add(face);
			}
		}
		return faces;		
	}
	
	private static void parseFaceVertex(int vertexIndex, String vertexData, FaceData face) {
		face.normalIndices[vertexIndex] = -1;
		face.uvIndices[vertexIndex] = -1;
		if(vertexData.contains(KEYWORD_FACE_SEPARATOR)) {
			final String[] vertex = vertexData.split(KEYWORD_FACE_SEPARATOR);
			// -1 as the indices in the OBJ file start at 1
			face.vertexIndices[vertexIndex] = Integer.parseInt(vertex[0]) - 1;
			if(!vertex[1].isEmpty())
				face.uvIndices[vertexIndex] = Integer.parseInt(vertex[1]) - 1;
			if(vertex.length == 3)
				face.normalIndices[vertexIndex] = Integer.parseInt(vertex[2]) - 1;
		} else {
			face.vertexIndices[vertexIndex] = Integer.parseInt(vertexData) - 1;
		}
	}
	
	private static int parseColor(String[] lineData) {
		final int r = Math.round(Float.parseFloat(lineData[1]) * ColorUtils.COLOR_ONE);
		final int g = Math.round(Float.parseFloat(lineData[2]) * ColorUtils.COLOR_ONE);
		final int b = Math.round(Float.parseFloat(lineData[3]) * ColorUtils.COLOR_ONE);
		return ColorUtils.toColor(r, g, b);
	}
	
	private static int[] parseVector(String[] lineData) {
		final int x = FixedPointUtils.toFixedPoint(lineData[1]);
		final int y = FixedPointUtils.toFixedPoint(lineData[2]);
		if(lineData.length == 3) {
			return VectorUtils.toVector(x, y);
		}
		final int z = FixedPointUtils.toFixedPoint(lineData[3]);
		return VectorUtils.toVector(x, y, z);
	}
	
	private static Vertex[] createVertices(List<VertexData> verticesData, List<FaceData> faces, Material[] materials) {
		for (FaceData face : faces) {
			final int materialIndex = face.material.getIndex();
			verticesData.get(face.vertexIndices[0]).materialIndex = materialIndex;
			verticesData.get(face.vertexIndices[1]).materialIndex = materialIndex;
			verticesData.get(face.vertexIndices[2]).materialIndex = materialIndex;
		}
		final Vertex[] vertices = new Vertex[verticesData.size()];
		for (int i = 0; i < vertices.length; i++) {
			final VertexData vertexData = verticesData.get(i);
			// clone the array so the object has no references and gets garbage collected
			final int[] location = vertexData.location.clone();
			final Material material = materials[vertexData.materialIndex];
			final Vertex vertex = new Vertex(i, location, material);
			vertices[i] = vertex;
		}
		return vertices;
	}
	
	private static Face[] createFaces(List<FaceData> facesData, Vertex[] faceVertices, List<FaceNormal> faceNormals, List<FaceUV> faceUVs) {
		final Face[] faces = new Face[facesData.size()];
		for (int i = 0; i < faces.length; i++) {
			final FaceData faceData = facesData.get(i);
			final Vertex[] vertices = new Vertex[3];
			vertices[0] = faceVertices[faceData.vertexIndices[0]];
			vertices[1] = faceVertices[faceData.vertexIndices[1]];
			vertices[2] = faceVertices[faceData.vertexIndices[2]];
			// clone the array so the object has no references and gets garbage collected
			int[] normal = VectorUtils.emptyVector();
			if(faceData.normalIndices[0] >= 0) {
				normal = faceNormals.get(faceData.normalIndices[0]).normal.clone();
			}
			final int[][] uvs = new int[3][VectorUtils.VECTOR_SIZE];
			if(faceData.uvIndices[0] >= 0) {
				uvs[0] = faceUVs.get(faceData.uvIndices[0]).uv.clone();
			}
			if(faceData.uvIndices[1] >= 0) {
				uvs[1] = faceUVs.get(faceData.uvIndices[1]).uv.clone();
			}
			if(faceData.uvIndices[2] >= 0) {
				uvs[2] = faceUVs.get(faceData.uvIndices[2]).uv.clone();
			}
			final Face face = new Face(i, vertices, normal, uvs, faceData.material);
			faces[i] = face;
		}
		return faces;
	}
	
	private static Material getMaterial(List<Material> materials, String name) {
		for (Material material : materials) {
			if(material.getName().equals(name))
				return material;
		}
		return null;
	}
	
	private static String[] getLineData(String line) {
		// remove multiple white spaces to avoid empty strings in the array
		return line.replace("  ", " ").split(" ");
	}
	
	private static String[] getLines(String data) {
		return data.replace("\r", "").split("\n");
	}	
}