package com.johnsproject.jgameengine.shading;

import static com.johnsproject.jgameengine.util.VectorUtils.VECTOR_X;
import static com.johnsproject.jgameengine.util.VectorUtils.VECTOR_Y;
import static com.johnsproject.jgameengine.util.VectorUtils.VECTOR_Z;

import com.johnsproject.jgameengine.model.Camera;
import com.johnsproject.jgameengine.model.Face;
import com.johnsproject.jgameengine.model.Frustum;
import com.johnsproject.jgameengine.model.Material;
import com.johnsproject.jgameengine.model.Texture;
import com.johnsproject.jgameengine.model.Vertex;
import com.johnsproject.jgameengine.rasterization.LinearRasterizer2;
import com.johnsproject.jgameengine.util.ColorUtils;
import com.johnsproject.jgameengine.util.FixedPointUtils;
import com.johnsproject.jgameengine.util.TransformationUtils;
import com.johnsproject.jgameengine.util.VectorUtils;

/**
 * The BasicShader is a shader that only contains the core functionality needed to show models on the screen,
 * it draws the models without illumination using it's diffuse color or texture and depth tests each fragment before drawing it.
 * It's purpose is to help people learn how the shaders work.
 * 
 * @author John Ferraz Salomon
 */
public class BasicShader implements Shader {

	private ForwardShaderBuffer shaderBuffer;
	
	// rasterizer used to draw the faces
	private LinearRasterizer2 rasterizer;	
	// camera that the graphics engine is currently rendering to
	private Camera camera;
	// frustum that vertices will be projected to
	private Frustum frustum;
	// the color of the face to draw
	private int diffuseColor;
	// texture of the face to draw
	private Texture texture;
	
	public BasicShader() {
		rasterizer = new LinearRasterizer2(this);
	}

	public void initialize(ShaderBuffer shaderBuffer) {
		this.shaderBuffer = (ForwardShaderBuffer) shaderBuffer;
		this.camera = shaderBuffer.getCamera();
		this.frustum = camera.getFrustum();
	}
	
	public void vertex(Vertex vertex) {
		final int[] location = vertex.getLocation();
		// reset the location of the vertex
		VectorUtils.copy(location, vertex.getWorldLocation());
		// transform the vertex to camera space
		VectorUtils.multiply(location, camera.getTransform().getSpaceEnterMatrix());
		// transform the vertex to screen space
		VectorUtils.multiply(location, frustum.getProjectionMatrix());
		// port the vertex to the center of the screen
		TransformationUtils.screenportVector(location, frustum);
	}

	// nothing to do here
	public void waitForVertexQueue() {}

	public void geometry(Face face) {
		final Material material = face.getMaterial();
		diffuseColor = material.getDiffuseColor();
		texture = material.getTexture();
		// set the texture space location of the vertices to the rasterizer so they're interpolated
		setUVs(face);
		// draw face
		rasterizer.linearDraw2(face, frustum);
	}

	private void setUVs(Face face) {
		if(texture != null) {
			// port uvs to texture space, don't use FixedPointUtils.multiply as the rasterizer interpolates fixed point vectors
			int[] uv = face.getUV(0);
			int u = uv[VECTOR_X] * texture.getWidth();
			int v = uv[VECTOR_Y] * texture.getHeight();
			rasterizer.setVector00(u, v, 0);
			
			uv = face.getUV(1);
			u = uv[VECTOR_X] * texture.getWidth();
			v = uv[VECTOR_Y] * texture.getHeight();
			rasterizer.setVector01(u, v, 0);
			
			uv = face.getUV(2);
			u = uv[VECTOR_X] * texture.getWidth();
			v = uv[VECTOR_Y] * texture.getHeight();
			rasterizer.setVector02(u, v, 0);
		}
	}
	
	// nothing to do here
	public void waitForGeometryQueue() {}

	public void fragment() {
		final Texture depthBuffer = shaderBuffer.getCamera().getRenderTarget().getDepthBuffer();
		final Texture colorBuffer = shaderBuffer.getCamera().getRenderTarget().getColorBuffer();
		// get the location of this fragment in screen space
		final int x = rasterizer.getLocation()[VECTOR_X];
		final int y = rasterizer.getLocation()[VECTOR_Y];
		final int z = rasterizer.getLocation()[VECTOR_Z];
		// test if there is a fragment front of this fragment, if not draw this fragment
		if (depthBuffer.getPixel(x, y) > z) {
			// get location of this fragment in texture space
			final int[] uv = rasterizer.getVector0();
			// get texture color of this fragment
			final int textureColor = getFragmentTexelColor(uv);
			// calculate the color of this fragment
			final int color = ColorUtils.multiplyColor(textureColor, diffuseColor);
			// update the color and depth buffers
			colorBuffer.setPixel(x, y, color);
			depthBuffer.setPixel(x, y, z);
		}
	}
	
	private int getFragmentTexelColor(int[] uv) {
		if(texture == null) {
			return ColorUtils.WHITE;
		} else {
			// The result will be, but pixels are not accessed with fixed point
			final int u = uv[VECTOR_X] >> FixedPointUtils.FP_BIT;
			final int v = uv[VECTOR_Y] >> FixedPointUtils.FP_BIT;
			return texture.getPixel(u, v);
		}
	}

	public ShaderBuffer getShaderBuffer() {
		return shaderBuffer;
	}

	public boolean isGlobal() {
		// global shaders are applied to all models
		return false;
	}

}
