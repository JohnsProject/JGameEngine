package com.johnsproject.jgameengine.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.junit.Test;

import com.johnsproject.jgameengine.graphics.Color;
import com.johnsproject.jgameengine.graphics.Face;
import com.johnsproject.jgameengine.graphics.Material;
import com.johnsproject.jgameengine.graphics.Mesh;
import com.johnsproject.jgameengine.graphics.Vertex;
import com.johnsproject.jgameengine.math.Fixed;
import com.johnsproject.jgameengine.math.Vector;

public class OBJImporterTest {

	@Test
	public void parseMaterialsTest() throws Exception {
		final String data = readResource("TestOBJ.mtl");
		final List<Material> materials = OBJImporter.parseMaterials(data);
		assert(materials.size() == 2);
		
		Material material = materials.get(0);
		assert(material.getIndex() == 0);
		assertEquals(material.getName(), "Material");
		
		int shininess = Fixed.toFixed(17.647059);
		assert(material.getShininess() == shininess);
		
		int r = Math.round(0.8f * Color.COLOR_ONE);
		int g = Math.round(0.8f * Color.COLOR_ONE);
		int b = Math.round(0.8f * Color.COLOR_ONE);
		int color = Color.toColor(r, g, b);
		assert(material.getDiffuseColor() == color);
		
		r = Math.round(0.5f * Color.COLOR_ONE);
		g = Math.round(0.5f * Color.COLOR_ONE);
		b = Math.round(0.5f * Color.COLOR_ONE);
		color = Color.toColor(r, g, b);
		assert(material.getSpecularColor() == color);
		
		material = materials.get(1);
		assert(material.getIndex() == 1);
		assertEquals(material.getName(), "Material.001");
		
		shininess = Fixed.toFixed(96.078431);
		assert(material.getShininess() == shininess);
		
		r = Math.round(0.64f * Color.COLOR_ONE);
		g = Math.round(0.64f * Color.COLOR_ONE);
		b = Math.round(0.64f * Color.COLOR_ONE);
		color = Color.toColor(r, g, b);
		assert(material.getDiffuseColor() == color);
		
		r = Math.round(0.5f * Color.COLOR_ONE);
		g = Math.round(0.5f * Color.COLOR_ONE);
		b = Math.round(0.5f * Color.COLOR_ONE);
		color = Color.toColor(r, g, b);
		assert(material.getSpecularColor() == color);
	}
	
	@Test
	public void parseVerticesTest() throws Exception {
		final String data = readResource("TestOBJ.obj");
		final List<OBJImporter.VertexData> vertices = OBJImporter.parseVertices(data);
		assert(vertices.size() == 16);
		
		OBJImporter.VertexData vertex = vertices.get(0);
		int x = Fixed.toFixed(-4.198291);
		int y = Fixed.toFixed(2.563459);
		int z = Fixed.toFixed(-1.020053);
		assert(vertex.location[Vector.VECTOR_X] == x);
		assert(vertex.location[Vector.VECTOR_Y] == y);
		assert(vertex.location[Vector.VECTOR_Z] == z);
		
		vertex = vertices.get(3);
		x = Fixed.toFixed(-6.198290);
		y = Fixed.toFixed(2.563459);
		z = Fixed.toFixed(-1.020054);
		assert(vertex.location[Vector.VECTOR_X] == x);
		assert(vertex.location[Vector.VECTOR_Y] == y);
		assert(vertex.location[Vector.VECTOR_Z] == z);		
		
		vertex = vertices.get(15);
		x = Fixed.toFixed(-1);
		y = Fixed.toFixed(1);
		z = Fixed.toFixed(-1);
		assert(vertex.location[Vector.VECTOR_X] == x);
		assert(vertex.location[Vector.VECTOR_Y] == y);
		assert(vertex.location[Vector.VECTOR_Z] == z);		
	}
	

	@Test
	public void parseFaceNormalsTest() throws Exception {
		final String data = readResource("TestOBJ.obj");
		final List<OBJImporter.FaceNormal> faceNormals = OBJImporter.parseFaceNormals(data);
		assert(faceNormals.size() == 12);
		
		OBJImporter.FaceNormal faceNormal = faceNormals.get(0);
		int x = Fixed.toFixed(0);
		int y = Fixed.toFixed(-1);
		int z = Fixed.toFixed(0);
		assert(faceNormal.normal[Vector.VECTOR_X] == x);
		assert(faceNormal.normal[Vector.VECTOR_Y] == y);
		assert(faceNormal.normal[Vector.VECTOR_Z] == z);
		
		faceNormal = faceNormals.get(11);
		x = Fixed.toFixed(0);
		y = Fixed.toFixed(0);
		z = Fixed.toFixed(-1);
		assert(faceNormal.normal[Vector.VECTOR_X] == x);
		assert(faceNormal.normal[Vector.VECTOR_Y] == y);
		assert(faceNormal.normal[Vector.VECTOR_Z] == z);
	}
	
	@Test
	public void parseUVsTest() throws Exception {
		final String data = readResource("TestOBJ.obj");
		final List<OBJImporter.FaceUV> faceUVs = OBJImporter.parseFaceUVs(data);
		
		OBJImporter.FaceUV faceUV = faceUVs.get(0);
		int x = Fixed.toFixed(1);
		int y = Fixed.toFixed(0);
		assert(faceUV.uv[Vector.VECTOR_X] == x);
		assert(faceUV.uv[Vector.VECTOR_Y] == y);
		
		faceUV = faceUVs.get(39);
		x = Fixed.toFixed(1);
		y = Fixed.toFixed(1);
		assert(faceUV.uv[Vector.VECTOR_X] == x);
		assert(faceUV.uv[Vector.VECTOR_Y] == y);
	}
	
	@Test
	public void parseFacesTest() throws Exception {		
		final String materialData = readResource("TestOBJ.mtl");
		final String objectData = readResource("TestOBJ.obj");
		final List<Material> materials = OBJImporter.parseMaterials(materialData);
		final List<OBJImporter.FaceData> faces = OBJImporter.parseFaces(objectData, materials);
		assert(faces.size() == 24);
		
		OBJImporter.FaceData face = faces.get(0);
		assert(face.index == 0);
		assert(face.vertexIndices[0] == 1);
		assert(face.vertexIndices[1] == 3);
		assert(face.vertexIndices[2] == 0);
		assert(face.normalIndices[0] == 0);
		assert(face.normalIndices[1] == 0);
		assert(face.normalIndices[2] == 0);
		assert(face.uvIndices[0] == 0);
		assert(face.uvIndices[1] == 1);
		assert(face.uvIndices[2] == 2);
		assert(face.material == materials.get(1));
		
		face = faces.get(5);
		assert(face.index == 5);
		assert(face.vertexIndices[0] == 0);
		assert(face.vertexIndices[1] == 7);
		assert(face.vertexIndices[2] == 4);
		assert(face.normalIndices[0] == 5);
		assert(face.normalIndices[1] == 5);
		assert(face.normalIndices[2] == 5);
		assert(face.uvIndices[0] == 13);
		assert(face.uvIndices[1] == 14);
		assert(face.uvIndices[2] == 5);
		assert(face.material == materials.get(1));
		
		face = faces.get(23);
		assert(face.index == 23);
		assert(face.vertexIndices[0] == 8);
		assert(face.vertexIndices[1] == 11);
		assert(face.vertexIndices[2] == 15);
		assert(face.normalIndices[0] == 11);
		assert(face.normalIndices[1] == 11);
		assert(face.normalIndices[2] == 11);
		assert(face.uvIndices[0] == 33);
		assert(face.uvIndices[1] == 39);
		assert(face.uvIndices[2] == 34);
		assert(face.material == materials.get(0));
	}
	
	@Test
	public void parseMeshTest() throws Exception {
		final String materialData = readResource("TestOBJ.mtl");
		final String objectData = readResource("TestOBJ.obj");
		final Mesh mesh = OBJImporter.parseMesh(objectData, materialData);
		
		assert(mesh.getMaterials().length == 2);
		assert(mesh.getVertices().length == 16);
		assert(mesh.getFaces().length == 24);
		
		assertNotNull(mesh.getMaterial("Material"));
		assertNotNull(mesh.getMaterial("Material.001"));
		
		Vertex vertex = mesh.getVertex(0);
		assert(vertex.getIndex() == 0);
		int x = Fixed.toFixed(-4.198291);
		int y = Fixed.toFixed(2.563459);
		int z = Fixed.toFixed(-1.020053);
		assert(vertex.getLocalLocation()[Vector.VECTOR_X] == x);
		assert(vertex.getLocalLocation()[Vector.VECTOR_Y] == y);
		assert(vertex.getLocalLocation()[Vector.VECTOR_Z] == z);
		assert(vertex.getMaterial() == mesh.getMaterial("Material.001"));
		
		vertex = mesh.getVertex(15);
		assert(vertex.getIndex() == 15);
		x = Fixed.toFixed(-1);
		y = Fixed.toFixed(1);
		z = Fixed.toFixed(-1);
		assert(vertex.getLocalLocation()[Vector.VECTOR_X] == x);
		assert(vertex.getLocalLocation()[Vector.VECTOR_Y] == y);
		assert(vertex.getLocalLocation()[Vector.VECTOR_Z] == z);
		assert(vertex.getMaterial() == mesh.getMaterial("Material"));
		
		Face face = mesh.getFace(0);
		assert(face.getIndex() == 0);
		assert(face.getVertex(0) == mesh.getVertex(1));
		assert(face.getVertex(1) == mesh.getVertex(3));
		assert(face.getVertex(2) == mesh.getVertex(0));
		
		x = Fixed.toFixed(0);
		y = Fixed.toFixed(-1);
		z = Fixed.toFixed(0);
		assert(face.getLocalNormal()[Vector.VECTOR_X] == x);
		assert(face.getLocalNormal()[Vector.VECTOR_Y] == y);
		assert(face.getLocalNormal()[Vector.VECTOR_Z] == z);
		
		x = Fixed.toFixed(1);
		y = Fixed.toFixed(0);
		assert(face.getUV(0)[Vector.VECTOR_X] == x);
		assert(face.getUV(0)[Vector.VECTOR_Y] == y);
		
		x = Fixed.toFixed(0);
		y = Fixed.toFixed(1);
		assert(face.getUV(1)[Vector.VECTOR_X] == x);
		assert(face.getUV(1)[Vector.VECTOR_Y] == y);
		
		x = Fixed.toFixed(0);
		y = Fixed.toFixed(0);
		assert(face.getUV(2)[Vector.VECTOR_X] == x);
		assert(face.getUV(2)[Vector.VECTOR_Y] == y);
		
		assert(face.getMaterial() == mesh.getMaterial("Material.001"));
	}
	
	private String readResource(String path) throws IOException {
		final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		final InputStream inputStream = classLoader.getResourceAsStream(path);
		return FileUtil.readStream(inputStream);
	}
	
}
