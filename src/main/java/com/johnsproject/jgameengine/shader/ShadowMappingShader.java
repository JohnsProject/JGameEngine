package com.johnsproject.jgameengine.shader;

import static com.johnsproject.jgameengine.math.VectorMath.*;

import com.johnsproject.jgameengine.math.FixedPointMath;
import com.johnsproject.jgameengine.math.TransformationMath;
import com.johnsproject.jgameengine.math.VectorMath;
import com.johnsproject.jgameengine.model.Texture;
import com.johnsproject.jgameengine.rasterizer.FlatRasterizer;

public class ShadowMappingShader implements Shader {
	
	private static final int DIRECTIONAL_BIAS = FixedPointMath.toFixedPoint(0.00005f);
	private static final int SPOT_BIAS = FixedPointMath.toFixedPoint(0.00025f);
	private static final int POINT_BIAS = FixedPointMath.toFixedPoint(0.00035f);
	
	private int shadowBias = 0;
	
	private ShadowMappingProperties shaderProperties;
	private ForwardShaderBuffer shaderBuffer;
	private final FlatRasterizer rasterizer;
	
	private Texture currentShadowMap;

	public ShadowMappingShader() {
		this.rasterizer = new FlatRasterizer(this);
		this.shaderProperties = new ShadowMappingProperties();
	}
	
	public void vertex(VertexBuffer vertexBuffer) { }

	public void geometry(GeometryBuffer geometryBuffer) {
		if(shaderProperties.directionalShadows() && (shaderBuffer.getDirectionalLightIndex() != -1)) {
			shadowBias = DIRECTIONAL_BIAS;
			currentShadowMap = shaderBuffer.getDirectionalShadowMap();
			transformVertices(geometryBuffer, shaderBuffer.getDirectionalLightMatrix(), shaderBuffer.getDirectionalLightFrustum());
			rasterizer.setFrustumCull(false);
			rasterizer.draw(geometryBuffer);
		}
		if(shaderProperties.spotShadows() && (shaderBuffer.getSpotLightIndex() != -1)) {
			shadowBias = SPOT_BIAS;
			currentShadowMap = shaderBuffer.getSpotShadowMap();
			transformVertices(geometryBuffer, shaderBuffer.getSpotLightMatrix(), shaderBuffer.getSpotLightFrustum());
			rasterizer.setFrustumCull(true);
			rasterizer.draw(geometryBuffer);
		}
		if(shaderProperties.pointShadows() && (shaderBuffer.getPointLightIndex() != -1)) {
			shadowBias = POINT_BIAS;
			for (int i = 0; i < shaderBuffer.getPointLightMatrices().length; i++) {
				currentShadowMap = shaderBuffer.getPointShadowMaps()[i];
				transformVertices(geometryBuffer, shaderBuffer.getPointLightMatrices()[i], shaderBuffer.getPointLightFrustum());
				rasterizer.setFrustumCull(true);
				rasterizer.draw(geometryBuffer);
			}
		}
	}
	
	private void transformVertices(GeometryBuffer geometryBuffer, int[][] lightMatrix, int[] lightFrustum) {
		for (int i = 0; i < geometryBuffer.getVertexBuffers().length; i++) {
			int[] location = geometryBuffer.getVertexBuffer(i).getLocation();
			VectorMath.copy(location, geometryBuffer.getVertexBuffer(i).getWorldLocation());
			VectorMath.multiply(location, lightMatrix);
			TransformationMath.screenportVector(location, lightFrustum);
		}
	}

	public void fragment(FragmentBuffer fragmentBuffer) {
		int x = fragmentBuffer.getLocation()[VECTOR_X];
		int y = fragmentBuffer.getLocation()[VECTOR_Y];
		int z = fragmentBuffer.getLocation()[VECTOR_Z] + shadowBias;
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
	
	public void setProperties(ShaderProperties shaderProperties) {
		this.shaderProperties = (ShadowMappingProperties) shaderProperties;
	}

	public ShaderProperties getProperties() {
		return shaderProperties;
	}
}
