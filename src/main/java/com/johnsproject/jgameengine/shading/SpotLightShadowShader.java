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

public class SpotLightShadowShader extends ThreadedShader {

	public boolean isGlobal() {
		return true;
	}

	@Override
	public ThreadedVertexShader[] createVertexShaders(int count) {
		final ThreadedVertexShader[] shaders = new VertexShader[count];
		for (int i = 0; i < shaders.length; i++)
			shaders[i] = new VertexShader();
		return shaders;
	}

	@Override
	public ThreadedGeometryShader[] createGeometryShaders(int count) {
		final ThreadedGeometryShader[] shaders = new GeometryShader[count];
		for (int i = 0; i < shaders.length; i++)
			shaders[i] = new GeometryShader();
		return shaders;
	}

	private static class VertexShader extends ThreadedVertexShader {

		private ForwardShaderBuffer shaderBuffer;
		private Frustum lightFrustum;
		
		public void initialize(ShaderBuffer shaderBuffer) {
			this.shaderBuffer = (ForwardShaderBuffer) shaderBuffer;
			initialize();
		}
		
		private void initialize() {
			lightFrustum = shaderBuffer.getSpotLightFrustum();
		}

		public void vertex(Vertex vertex) {
			if(shaderBuffer.getShadowSpotLight() != null) {
				final int[] location = vertex.getLocation();
				VectorUtils.copy(location, vertex.getWorldLocation());
				VectorUtils.multiply(location, lightFrustum.getProjectionMatrix());
				TransformationUtils.screenportVector(location, lightFrustum);
			}
		}

		public ShaderBuffer getShaderBuffer() {
			return shaderBuffer;
		}
	}	
	
	private static class GeometryShader extends ThreadedGeometryShader {

		private ForwardShaderBuffer shaderBuffer;
		private final Rasterizer rasterizer;
		private Light light;
		private Frustum lightFrustum;
		private Texture shadowMap;
		private int shadowBias;
		
		
		public GeometryShader() {
			rasterizer = new Rasterizer(this);
		}
		
		public void initialize(ShaderBuffer shaderBuffer) {
			this.shaderBuffer = (ForwardShaderBuffer) shaderBuffer;
			initialize();
		}
		
		private void initialize() {
			light = shaderBuffer.getShadowSpotLight();
			lightFrustum = shaderBuffer.getSpotLightFrustum();
			shadowMap = shaderBuffer.getSpotShadowMap();
			if(light != null)
				shadowBias = light.getShadowBias() >> 10;
		}

		public void geometry(Face face) {
			if(light != null) {
				rasterizer.setFrustumCull(false);
				rasterizer.draw(face, lightFrustum);
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
		
	}
}
