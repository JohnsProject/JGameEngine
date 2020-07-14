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
	
	public void vertex(Vertex vertex) { }

	public void geometry(Face face) {
		renderForDirectionalLight(face);
		renderForSpotLight(face);
		renderForPointLight(face);
	}
	
	private void renderForDirectionalLight(Face face) {
		final Light directionalLight = shaderBuffer.getShadowDirectionalLight();
		if(directionalLight != null) {
			shadowBias = directionalLight.getShadowBias();
			shadowMap = shaderBuffer.getDirectionalShadowMap();
			final Frustum frustum = shaderBuffer.getDirectionalLightFrustum();
			transformVertices(face, frustum.getProjectionMatrix(), frustum);
			rasterizer.setFrustumCull(false);
			rasterizer.draw(face);
		}
	}
	
	private void renderForSpotLight(Face face) {
		final Light spotLight = shaderBuffer.getShadowSpotLight();
		if(spotLight != null) {
			shadowBias = spotLight.getShadowBias();
			shadowMap = shaderBuffer.getSpotShadowMap();
			final Frustum frustum = shaderBuffer.getSpotLightFrustum();
			transformVertices(face, frustum.getProjectionMatrix(), frustum);
			rasterizer.setFrustumCull(true);
			rasterizer.draw(face);
		}
	}
	
	private void renderForPointLight(Face face) {
		final Light pointLight = shaderBuffer.getShadowPointLight();
		if(pointLight != null) {
			shadowBias = pointLight.getShadowBias();
			final Frustum frustum = shaderBuffer.getPointLightFrustum();
			for (int i = 0; i < shaderBuffer.getPointLightMatrices().length; i++) {
				shadowMap = shaderBuffer.getPointShadowMaps()[i];
				transformVertices(face, shaderBuffer.getPointLightMatrices()[i], frustum);
				rasterizer.setFrustumCull(true);
				rasterizer.draw(face);
			}
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
