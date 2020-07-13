package com.johnsproject.jgameengine.shading;

import static com.johnsproject.jgameengine.util.VectorUtils.VECTOR_X;
import static com.johnsproject.jgameengine.util.VectorUtils.VECTOR_Y;
import static com.johnsproject.jgameengine.util.VectorUtils.VECTOR_Z;

import com.johnsproject.jgameengine.model.Face;
import com.johnsproject.jgameengine.model.Fragment;
import com.johnsproject.jgameengine.model.Frustum;
import com.johnsproject.jgameengine.model.Texture;
import com.johnsproject.jgameengine.model.Vertex;
import com.johnsproject.jgameengine.rasterization.FlatRasterizer;
import com.johnsproject.jgameengine.util.FixedPointUtils;
import com.johnsproject.jgameengine.util.TransformationUtils;
import com.johnsproject.jgameengine.util.VectorUtils;

public class ShadowMappingShader implements Shader {
	
	private static final int DIRECTIONAL_BIAS = FixedPointUtils.toFixedPoint(0.00005f);
	private static final int SPOT_BIAS = FixedPointUtils.toFixedPoint(0.00025f);
	private static final int POINT_BIAS = FixedPointUtils.toFixedPoint(0.00035f);
	
	private int shadowBias = 0;
	
	private ForwardShaderBuffer shaderBuffer;
	private final FlatRasterizer rasterizer;
	
	private Texture currentShadowMap;
	
	private boolean directionalShadows;
	private boolean spotShadows;
	private boolean pointShadows;

	public ShadowMappingShader() {
		this.directionalShadows = true;
		this.spotShadows = true;
		this.pointShadows = true;
		this.rasterizer = new FlatRasterizer(this);
	}
	
	public void vertex(Vertex vertex) { }

	public void geometry(Face face) {
		renderForDirectionalLight(face);
		renderForSpotLight(face);
		renderForPointLight(face);
	}
	
	private void renderForDirectionalLight(Face face) {
		if(directionalShadows && (shaderBuffer.getDirectionalLightIndex() != -1)) {
			shadowBias = DIRECTIONAL_BIAS;
			currentShadowMap = shaderBuffer.getDirectionalShadowMap();
			final Frustum frustum = shaderBuffer.getDirectionalLightFrustum();
			transformVertices(face, frustum.getProjectionMatrix(), frustum);
			rasterizer.setFrustumCull(false);
			rasterizer.draw(face);
		}
	}
	
	private void renderForSpotLight(Face face) {
		if(spotShadows && (shaderBuffer.getSpotLightIndex() != -1)) {
			shadowBias = SPOT_BIAS;
			currentShadowMap = shaderBuffer.getSpotShadowMap();
			final Frustum frustum = shaderBuffer.getSpotLightFrustum();
			transformVertices(face, frustum.getProjectionMatrix(), frustum);
			rasterizer.setFrustumCull(true);
			rasterizer.draw(face);
		}
	}
	
	private void renderForPointLight(Face face) {
		if(pointShadows && (shaderBuffer.getPointLightIndex() != -1)) {
			shadowBias = POINT_BIAS;
			final Frustum frustum = shaderBuffer.getPointLightFrustum();
			for (int i = 0; i < shaderBuffer.getPointLightMatrices().length; i++) {
				currentShadowMap = shaderBuffer.getPointShadowMaps()[i];
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

	public void fragment(Fragment fragment) {
		final int x = fragment.getLocation()[VECTOR_X];
		final int y = fragment.getLocation()[VECTOR_Y];
		final int z = fragment.getLocation()[VECTOR_Z] + shadowBias;
		if (currentShadowMap.getPixel(x, y) > z) {
			currentShadowMap.setPixel(x, y, z);
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

	public boolean directionalShadows() {
		return directionalShadows;
	}

	public void directionalShadows(boolean directionalShadows) {
		this.directionalShadows = directionalShadows;
	}

	public boolean spotShadows() {
		return spotShadows;
	}

	public void spotShadows(boolean spotShadows) {
		this.spotShadows = spotShadows;
	}

	public boolean pointShadows() {
		return pointShadows;
	}

	public void pointShadows(boolean pointShadows) {
		this.pointShadows = pointShadows;
	}
}
