package com.johnsproject.jgameengine.shading;

import static com.johnsproject.jgameengine.util.FixedPointUtils.FP_ONE;
import static com.johnsproject.jgameengine.util.VectorUtils.VECTOR_X;
import static com.johnsproject.jgameengine.util.VectorUtils.VECTOR_Y;
import static com.johnsproject.jgameengine.util.VectorUtils.VECTOR_Z;

import com.johnsproject.jgameengine.model.Camera;
import com.johnsproject.jgameengine.model.Face;
import com.johnsproject.jgameengine.model.Frustum;
import com.johnsproject.jgameengine.model.Light;
import com.johnsproject.jgameengine.model.Texture;
import com.johnsproject.jgameengine.model.Vertex;
import com.johnsproject.jgameengine.rasterization.Rasterizer;
import com.johnsproject.jgameengine.util.MatrixUtils;
import com.johnsproject.jgameengine.util.TransformationUtils;
import com.johnsproject.jgameengine.util.VectorUtils;

public class DirectionalLightShadowShader extends ThreadedShader {

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
			lightFrustum = shaderBuffer.getDirectionalLightFrustum();
		}

		public void vertex(Vertex vertex) {
			if(shaderBuffer.getShadowDirectionalLight() != null) {
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
		private final Rasterizer rasterizer = new Rasterizer(this);
		private Light light;
		private Frustum lightFrustum;
		private Texture shadowMap;
		private int shadowBias;
		
		private Frustum cameraFrustum;
		private int[][] cameraMatrix = MatrixUtils.indentityMatrix();
		private int renderTargetLeft;
		private int renderTargetRight;
		private int renderTargetTop;
		private int renderTargetBottom;
		private int[] location0 = VectorUtils.emptyVector();
		private int[] location1 = VectorUtils.emptyVector();
		private int[] location2 = VectorUtils.emptyVector();
		
		public void initialize(ShaderBuffer shaderBuffer) {
			this.shaderBuffer = (ForwardShaderBuffer) shaderBuffer;
			initialize();
		}
		
		private void initialize() {
			light = shaderBuffer.getShadowDirectionalLight();
			lightFrustum = shaderBuffer.getDirectionalLightFrustum();
			shadowMap = shaderBuffer.getDirectionalShadowMap();
			if(light != null) {
				shadowBias = light.getShadowBias() >> 10;
				final Camera camera = shaderBuffer.getCamera();
				cameraFrustum = camera.getFrustum();
				final int[][] cameraSpaceMatrix = camera.getTransform().getSpaceEnterMatrix();
				final int[][] projectionMatrix = cameraFrustum.getProjectionMatrix();
				
				MatrixUtils.multiply(projectionMatrix, cameraSpaceMatrix, cameraMatrix);
				
				final int tolerance = 1024;
				renderTargetLeft = cameraFrustum.getRenderTargetLeft() - tolerance;
				renderTargetRight = cameraFrustum.getRenderTargetRight() + tolerance;
				renderTargetTop = cameraFrustum.getRenderTargetTop() - tolerance;
				renderTargetBottom = cameraFrustum.getRenderTargetBottom() + tolerance;
			}
		}

		public void geometry(Face face) {
			if((light != null) && isInCameraView(face)) {
				rasterizer.setFrustumCull(false);
				rasterizer.draw(face, lightFrustum);
			}
		}
		
		private boolean isInCameraView(Face face) {
			VectorUtils.copy(location0, face.getVertex(0).getWorldLocation());
			VectorUtils.multiply(location0, cameraMatrix);
			TransformationUtils.screenportVector(location0, cameraFrustum);
			
			VectorUtils.copy(location1, face.getVertex(1).getWorldLocation());
			VectorUtils.multiply(location1, cameraMatrix);
			TransformationUtils.screenportVector(location1, cameraFrustum);
			
			VectorUtils.copy(location2, face.getVertex(2).getWorldLocation());
			VectorUtils.multiply(location2, cameraMatrix);
			TransformationUtils.screenportVector(location2, cameraFrustum);
			
			final boolean insideWidth1 = (location0[VECTOR_X] > renderTargetLeft) && (location0[VECTOR_X] < renderTargetRight);
			final boolean insideWidth2 = (location1[VECTOR_X] > renderTargetLeft) && (location1[VECTOR_X] < renderTargetRight);
			final boolean insideWidth3 = (location2[VECTOR_X] > renderTargetLeft) && (location2[VECTOR_X] < renderTargetRight);
			final boolean insideHeight1 = (location0[VECTOR_Y] > renderTargetTop) && (location0[VECTOR_Y] < renderTargetBottom);
			final boolean insideHeight2 = (location1[VECTOR_Y] > renderTargetTop) && (location1[VECTOR_Y] < renderTargetBottom);
			final boolean insideHeight3 = (location2[VECTOR_Y] > renderTargetTop) && (location2[VECTOR_Y] < renderTargetBottom);
			final boolean insideDepth1 = (location0[VECTOR_Z] > 0) && (location0[VECTOR_Z] < FP_ONE);
			final boolean insideDepth2 = (location1[VECTOR_Z] > 0) && (location1[VECTOR_Z] < FP_ONE);
			final boolean insideDepth3 = (location2[VECTOR_Z] > 0) && (location2[VECTOR_Z] < FP_ONE);
			if ((!insideDepth1 && !insideDepth2 && !insideDepth3) 
					|| (!insideHeight1 && !insideHeight2 && !insideHeight3)
						|| (!insideWidth1 && !insideWidth2 && !insideWidth3)) {
							return false;
			}
			return true;
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
