package com.johnsproject.jgameengine.shading;

import static com.johnsproject.jgameengine.util.VectorUtils.VECTOR_X;
import static com.johnsproject.jgameengine.util.VectorUtils.VECTOR_Y;
import static com.johnsproject.jgameengine.util.VectorUtils.VECTOR_Z;

import com.johnsproject.jgameengine.model.Face;
import com.johnsproject.jgameengine.model.Frustum;
import com.johnsproject.jgameengine.model.Light;
import com.johnsproject.jgameengine.model.Texture;
import com.johnsproject.jgameengine.model.Vertex;
import com.johnsproject.jgameengine.rasterization.Rasterizer;
import com.johnsproject.jgameengine.util.TransformationUtils;
import com.johnsproject.jgameengine.util.VectorUtils;

public class ShadowMappingShader implements Shader {
	
	private ForwardShaderBuffer shaderBuffer;
	private final Rasterizer rasterizer;
	
	private int shadowBias;
	private Texture shadowMap;

	public ShadowMappingShader() {
		this.rasterizer = new Rasterizer(this);
	}
	
	public void vertex(Vertex vertex) {}
	
	public void waitForVertexQueue() {}
	
	public void geometry(Face face) {
		renderForDirectionalLight(face);
		renderForSpotLight(face);
	}
	
	public void waitForGeometryQueue() {}
	
	private void renderForDirectionalLight(Face face) {
		final Light directionalLight = shaderBuffer.getShadowDirectionalLight();
		if(directionalLight != null) {
			final Frustum frustum = shaderBuffer.getDirectionalLightFrustum();
			transformVertices(face, frustum.getProjectionMatrix(), frustum);
			shadowBias = directionalLight.getShadowBias() >> 10;
			shadowMap = shaderBuffer.getDirectionalShadowMap();
			rasterizer.setFrustumCull(false);
			rasterizer.draw(face, frustum);
		}
	}
	
	private void renderForSpotLight(Face face) {
		final Light spotLight = shaderBuffer.getShadowSpotLight();
		if(spotLight != null) {
			final Frustum frustum = shaderBuffer.getSpotLightFrustum();
			transformVertices(face, frustum.getProjectionMatrix(), frustum);
			shadowBias = spotLight.getShadowBias() >> 10;
			shadowMap = shaderBuffer.getSpotShadowMap();
			rasterizer.setFrustumCull(true);
			rasterizer.draw(face, frustum);
		}
	}
	
	private void transformVertices(Face face, int[][] lightMatrix, Frustum lightFrustum) {
		for (int i = 0; i < face.getVertices().length; i++) {
			int[] location = face.getVertex(i).getLocation();
			VectorUtils.copy(location, face.getVertex(i).getWorldLocation());
			VectorUtils.multiply(location, lightMatrix);
			TransformationUtils.screenportVector(location, lightFrustum);
		}
	}

	public void fragment() {
		final int x = rasterizer.getLocation()[VECTOR_X];
		final int y = rasterizer.getLocation()[VECTOR_Y];
		final int z = rasterizer.getLocation()[VECTOR_Z] + shadowBias;
		if (shadowMap.getPixel(x, y) > z) {
			shadowMap.setPixel(x, y, z);
		}
	}

	public ShaderBuffer getShaderBuffer() {
		return shaderBuffer;
	}

	public void setShaderBuffer(ShaderBuffer shaderBuffer) {
		this.shaderBuffer = (ForwardShaderBuffer) shaderBuffer;
	}
	
	public boolean isGlobal() {
		return true;
	}
}
