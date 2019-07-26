package com.johnsproject.jgameengine.shader;

import com.johnsproject.jgameengine.model.GeometryBuffer;
import com.johnsproject.jgameengine.model.Texture;
import com.johnsproject.jgameengine.model.VertexBuffer;
import com.johnsproject.jgameengine.rasterizer.FlatRasterizer;

public class ShadowMappingShader extends Shader {
	
	private static final short SHADOW_BIAS = 500;
	
	private ShadowMappingProperties shaderProperties;
	private ForwardShaderBuffer shaderBuffer;
	private final FlatRasterizer rasterizer;
	
	private Texture currentShadowMap;

	public ShadowMappingShader() {
		this.rasterizer = new FlatRasterizer(this);
		this.shaderProperties = new ShadowMappingProperties();
	}
	
	@Override
	public void vertex(VertexBuffer vertexBuffer) {
	}

	@Override
	public void geometry(GeometryBuffer geometryBuffer) {
		if(shaderProperties.directionalShadows()) {
			if (shaderBuffer.getDirectionalLightIndex() != -1) {
				currentShadowMap = shaderBuffer.getDirectionalShadowMap();
				for (int i = 0; i < geometryBuffer.getVertexDataBuffers().length; i++) {
					geometryBuffer.getVertexDataBuffer(i).reset();
					int[] vertexLocation = geometryBuffer.getVertexDataBuffer(i).getLocation();
					vectorLibrary.matrixMultiply(vertexLocation, shaderBuffer.getDirectionalLightMatrix(), vertexLocation);
					graphicsLibrary.screenportVector(vertexLocation, shaderBuffer.getDirectionalLightFrustum(), vertexLocation);
				}
				rasterizer.setLocation0(geometryBuffer.getVertexDataBuffer(0).getLocation());
				rasterizer.setLocation1(geometryBuffer.getVertexDataBuffer(1).getLocation());
				rasterizer.setLocation2(geometryBuffer.getVertexDataBuffer(2).getLocation());
				graphicsLibrary.drawFlatTriangle(rasterizer, true, 1, shaderBuffer.getDirectionalLightFrustum());
			}
		}
		
		if(shaderProperties.spotShadows()) {
			if (shaderBuffer.getSpotLightIndex() != -1) {
				currentShadowMap = shaderBuffer.getSpotShadowMap();
				for (int i = 0; i < geometryBuffer.getVertexDataBuffers().length; i++) {
					geometryBuffer.getVertexDataBuffer(i).reset();
					int[] vertexLocation = geometryBuffer.getVertexDataBuffer(i).getLocation();
					vectorLibrary.matrixMultiply(vertexLocation, shaderBuffer.getSpotLightMatrix(), vertexLocation);
					graphicsLibrary.screenportVector(vertexLocation, shaderBuffer.getSpotLightFrustum(), vertexLocation);
				}
				rasterizer.setLocation0(geometryBuffer.getVertexDataBuffer(0).getLocation());
				rasterizer.setLocation1(geometryBuffer.getVertexDataBuffer(1).getLocation());
				rasterizer.setLocation2(geometryBuffer.getVertexDataBuffer(2).getLocation());
				graphicsLibrary.drawFlatTriangle(rasterizer, true, 1, shaderBuffer.getSpotLightFrustum());
			}
		}
		if(shaderProperties.pointShadows()) {
			if (shaderBuffer.getPointLightIndex() != -1) {
				int[][] lightMatrices = shaderBuffer.getPointLightMatrices();
				for (int i = 0; i < lightMatrices.length; i++) {
					currentShadowMap = shaderBuffer.getPointShadowMaps()[i];
					for (int j = 0; j < geometryBuffer.getVertexDataBuffers().length; j++) {
						geometryBuffer.getVertexDataBuffer(j).reset();
						int[] vertexLocation = geometryBuffer.getVertexDataBuffer(j).getLocation();
						vectorLibrary.matrixMultiply(vertexLocation, lightMatrices[i], vertexLocation);
						graphicsLibrary.screenportVector(vertexLocation, shaderBuffer.getPointLightFrustum(), vertexLocation);
					}
					rasterizer.setLocation0(geometryBuffer.getVertexDataBuffer(0).getLocation());
					rasterizer.setLocation1(geometryBuffer.getVertexDataBuffer(1).getLocation());
					rasterizer.setLocation2(geometryBuffer.getVertexDataBuffer(2).getLocation());
					graphicsLibrary.drawFlatTriangle(rasterizer, true, 1, shaderBuffer.getPointLightFrustum());
				}
			}
		}
	}

	@Override
	public void fragment(int[] location) {
		int x = location[VECTOR_X];
		int y = location[VECTOR_Y];
		int z = location[VECTOR_Z] + SHADOW_BIAS;
		if (currentShadowMap.getPixel(x, y) > z) {
			currentShadowMap.setPixel(x, y, z);
		}
	}

	@Override
	public ShaderBuffer getShaderBuffer() {
		return shaderBuffer;
	}

	@Override
	public void setShaderBuffer(ShaderBuffer shaderBuffer) {
		this.shaderBuffer = (ForwardShaderBuffer) shaderBuffer;
	}

	@Override
	public void setProperties(ShaderProperties shaderProperties) {
		this.shaderProperties = (ShadowMappingProperties) shaderProperties;
	}

	@Override
	public ShaderProperties getProperties() {
		return shaderProperties;
	}
}
