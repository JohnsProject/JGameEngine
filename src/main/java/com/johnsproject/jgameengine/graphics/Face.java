package com.johnsproject.jgameengine.graphics;

import com.johnsproject.jgameengine.math.Point;
import com.johnsproject.jgameengine.math.Triangle;

public class Face {
		
	private final int index;
	private final Triangle triangle;
	private final Vertex[] vertices;
	private final int[] localNormal;
	private final int[] worldNormal;
	private final int[][] uvs;
	private final Material material;
	private int lightColor;
	
	public Face(int index, Vertex[] vertices, int[] normal, int[][] uvs, Material material) {
		this(index,
				vertices, 
				new Triangle(new Point[] {vertices[0].getPoint(), vertices[1].getPoint(), vertices[2].getPoint()}),
				normal,
				uvs,
				material);
	}
	
	public Face(int index, Vertex[] vertices, Triangle triangle, int[] normal, int[][] uvs, Material material) {
		this.index = index;
		this.vertices = vertices;
		this.triangle = triangle;
		this.localNormal = normal;
		this.worldNormal = normal.clone();
		this.uvs = uvs;
		this.material = material;
	}

	public int getIndex() {
		return index;
	}

	public Triangle getTriangle() {
		return triangle;
	}

	public Vertex getVertex(int index) {
		return vertices[index];
	}
	
	public Vertex[] getVertices() {
		return vertices;
	}
	
	public int[] getLocalNormal() {
		return localNormal;
	}

	public int[] getWorldNormal() {
		return worldNormal;
	}

	public int[] getUV(int index) {
		return uvs[index];
	}

	public int[][] getUVs() {
		return uvs;
	}

	public Material getMaterial() {
		return material;
	}
	
	/**
	 * Returns the light color of this {@link Face}.
	 * The light color is the color of all lights that reach and affect the illumination of
	 * this Face put together.
	 * 
	 * @return The light color of this Face.
	 */
	public int getLightColor() {
		return lightColor;
	}

	/**
	 * Sets the light color of this {@link Face}.
	 * The light color is the color of all lights that reach and affect the illumination of 
	 * this Vertex put together.
	 * 
	 * @param lightColor to set.
	 */
	public void setLightColor(int lightColor) {
		this.lightColor = lightColor;
	}
}
