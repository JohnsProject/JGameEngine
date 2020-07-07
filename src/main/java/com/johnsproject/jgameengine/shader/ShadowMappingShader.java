package com.johnsproject.jgameengine.shader;

import static com.johnsproject.jgameengine.math.VectorMath.VECTOR_X;
import static com.johnsproject.jgameengine.math.VectorMath.VECTOR_Y;
import static com.johnsproject.jgameengine.math.VectorMath.VECTOR_Z;

import com.johnsproject.jgameengine.math.FixedPointMath;
import com.johnsproject.jgameengine.math.TransformationMath;
import com.johnsproject.jgameengine.math.VectorMath;
import com.johnsproject.jgameengine.model.Face;
import com.johnsproject.jgameengine.model.Fragment;
import com.johnsproject.jgameengine.model.Texture;
import com.johnsproject.jgameengine.model.Vertex;
import com.johnsproject.jgameengine.rasterizer.FlatRasterizer;

public class ShadowMappingShader implements Shader {
	
	private static final int DIRECTIONAL_BIAS = FixedPointMath.toFixedPoint(0.00005f);
	private static final int SPOT_BIAS = FixedPointMath.toFixedPoint(0.00025f);
	private static final int POINT_BIAS = FixedPointMath.toFixedPoint(0.00035f);
	
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
		if(directionalShadows && (shaderBuffer.getDirectionalLightIndex() != -1)) {
			shadowBias = DIRECTIONAL_BIAS;
			currentShadowMap = shaderBuffer.getDirectionalShadowMap();
			transformVertices(face, shaderBuffer.getDirectionalLightMatrix(), shaderBuffer.getDirectionalLightFrustum());
			rasterizer.setFrustumCull(false);
			rasterizer.draw(face);
		}
		if(spotShadows && (shaderBuffer.getSpotLightIndex() != -1)) {
			shadowBias = SPOT_BIAS;
			currentShadowMap = shaderBuffer.getSpotShadowMap();
			transformVertices(face, shaderBuffer.getSpotLightMatrix(), shaderBuffer.getSpotLightFrustum());
			rasterizer.setFrustumCull(true);
			rasterizer.draw(face);
		}
		if(pointShadows && (shaderBuffer.getPointLightIndex() != -1)) {
			shadowBias = POINT_BIAS;
			for (int i = 0; i < shaderBuffer.getPointLightMatrices().length; i++) {
				currentShadowMap = shaderBuffer.getPointShadowMaps()[i];
				transformVertices(face, shaderBuffer.getPointLightMatrices()[i], shaderBuffer.getPointLightFrustum());
				rasterizer.setFrustumCull(true);
				rasterizer.draw(face);
			}
		}
	}
	
	private void transformVertices(Face face, int[][] lightMatrix, int[] lightFrustum) {
		for (int i = 0; i < face.getVertices().length; i++) {
			int[] location = face.getVertex(i).getLocation();
			VectorMath.copy(location, face.getVertex(i).getWorldLocation());
			VectorMath.multiply(location, lightMatrix);
			TransformationMath.screenportVector(location, lightFrustum);
		}
	}

	public void fragment(Fragment fragment) {
		final int x = fragment.getLocation()[VECTOR_X];
		final int y = fragment.getLocation()[VECTOR_Y];
		final int z = fragment.getLocation()[VECTOR_Z] + shadowBias;
		if (currentShadowMap.getPixel(x, y) > z) {
			currentShadowMap.setPixel(x, y, z);
			// debug shadow maps
			/*if(shadowBias == DIRECTIONAL_BIAS) {
				int depth = z >> 1;
				int color = ColorLibrary.generate(depth, depth, depth);
				shaderBuffer.getFrameBuffer().getColorBuffer().setPixel(x, y, color);				
			}*/
			/*if(shadowBias == SPOT_BIAS) {
				int depth = z >> 1;
				int color = ColorLibrary.generate(depth, depth, depth);
				shaderBuffer.getFrameBuffer().getColorBuffer().setPixel(x, y, color);				
			}*/
			// fix rasterizer culling to fix point shadows
			/*if(shadowBias == POINT_BIAS) {
				for (int i = 0; i < shaderBuffer.getPointShadowMaps().length; i++) {
					if(shaderBuffer.getPointShadowMaps()[i] == currentShadowMap) {
						int frameBufferX = x + (currentShadowMap.getWidth() * (i + 1));
						int frameBufferY = y;
						if(i >= 3) {
							frameBufferX = x + (currentShadowMap.getWidth() * ((i - 5) + 1));
							frameBufferY += currentShadowMap.getHeight();
						}
						int depth = z >> 1;
						int color = ColorLibrary.generate(depth, depth, depth);
						shaderBuffer.getFrameBuffer().getColorBuffer().setPixel(frameBufferX, frameBufferY, color);	
					}
				}	
			}*/
		}
	}

	public ShaderBuffer getShaderBuffer() {
		return shaderBuffer;
	}

	public void setShaderBuffer(ShaderBuffer shaderBuffer) {
		this.shaderBuffer = (ForwardShaderBuffer) shaderBuffer;
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
