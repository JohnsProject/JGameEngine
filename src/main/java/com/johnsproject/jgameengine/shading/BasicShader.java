package com.johnsproject.jgameengine.shading;

import static com.johnsproject.jgameengine.util.VectorUtils.VECTOR_X;
import static com.johnsproject.jgameengine.util.VectorUtils.VECTOR_Y;
import static com.johnsproject.jgameengine.util.VectorUtils.VECTOR_Z;

import com.johnsproject.jgameengine.model.Face;
import com.johnsproject.jgameengine.model.Texture;
import com.johnsproject.jgameengine.model.Vertex;
import com.johnsproject.jgameengine.rasterization.Rasterizer;
import com.johnsproject.jgameengine.util.TransformationUtils;
import com.johnsproject.jgameengine.util.VectorUtils;

public class BasicShader extends ThreadedShader {

	private ForwardShaderBuffer shaderBuffer;
	
	@Override
	public VertexWorker[] createVertexWorkers(int count) {
		final VertexWorker[] workers = new VertexWorker[count];
		for (int i = 0; i < workers.length; i++)
			workers[i] = new VertexShader();
		return workers;
	}

	@Override
	public GeometryWorker[] createGeometryWorkers(int count) {
		final GeometryWorker[] workers = new GeometryWorker[count];
		for (int i = 0; i < workers.length; i++)
			workers[i] = new GeometryShader();
		return workers;
	}

	public ShaderBuffer getShaderBuffer() {
		return shaderBuffer;
	}

	public void setShaderBuffer(ShaderBuffer shaderBuffer) {
		this.shaderBuffer = (ForwardShaderBuffer) shaderBuffer;
	}

	public boolean isGlobal() {
		return false;
	}	

	private static class VertexShader extends VertexWorker {
		
		private ForwardShaderBuffer shaderBuffer;

		public void vertex(Vertex vertex) {
			final int[] location = vertex.getLocation();
			VectorUtils.copy(location, vertex.getWorldLocation());
			VectorUtils.multiply(location, shaderBuffer.getCamera().getTransform().getSpaceEnterMatrix());
			VectorUtils.multiply(location, shaderBuffer.getCamera().getFrustum().getProjectionMatrix());
			TransformationUtils.screenportVector(location, shaderBuffer.getCamera().getFrustum());
		}

		public ShaderBuffer getShaderBuffer() {
			return shaderBuffer;
		}

		public void setShaderBuffer(ShaderBuffer shaderBuffer) {
			this.shaderBuffer = (ForwardShaderBuffer) shaderBuffer;
		}
	}
	
	private static class GeometryShader extends GeometryWorker {

		private ForwardShaderBuffer shaderBuffer;
		private final Rasterizer rasterizer = new Rasterizer(this);
		private int color;
		
		public void geometry(Face face) {
			color = face.getMaterial().getDiffuseColor();
			rasterizer.draw(face);
		}

		public void fragment() {
			final Texture depthBuffer = shaderBuffer.getCamera().getRenderTarget().getDepthBuffer();
			final Texture colorBuffer = shaderBuffer.getCamera().getRenderTarget().getColorBuffer();
			final int x = rasterizer.getLocation()[VECTOR_X];
			final int y = rasterizer.getLocation()[VECTOR_Y];
			final int z = rasterizer.getLocation()[VECTOR_Z];
			if (depthBuffer.getPixel(x, y) > z) {
				colorBuffer.setPixel(x, y, color);
				depthBuffer.setPixel(x, y, z);
			}
		}

		public ShaderBuffer getShaderBuffer() {
			return shaderBuffer;
		}

		public void setShaderBuffer(ShaderBuffer shaderBuffer) {
			this.shaderBuffer = (ForwardShaderBuffer) shaderBuffer;
		}
	}
}
