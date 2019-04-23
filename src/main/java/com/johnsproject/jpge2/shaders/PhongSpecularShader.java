package com.johnsproject.jpge2.shaders;

import java.util.List;

import com.johnsproject.jpge2.dto.Camera;
import com.johnsproject.jpge2.dto.Face;
import com.johnsproject.jpge2.dto.FrameBuffer;
import com.johnsproject.jpge2.dto.Light;
import com.johnsproject.jpge2.dto.Material;
import com.johnsproject.jpge2.dto.Model;
import com.johnsproject.jpge2.dto.Texture;
import com.johnsproject.jpge2.dto.Vertex;
import com.johnsproject.jpge2.processors.CentralProcessor;
import com.johnsproject.jpge2.processors.ColorProcessor;
import com.johnsproject.jpge2.processors.GraphicsProcessor;
import com.johnsproject.jpge2.processors.GraphicsProcessor.Shader;
import com.johnsproject.jpge2.processors.GraphicsProcessor.ShaderDataBuffer;
import com.johnsproject.jpge2.processors.MathProcessor;
import com.johnsproject.jpge2.processors.MatrixProcessor;
import com.johnsproject.jpge2.processors.VectorProcessor;

public class PhongSpecularShader extends Shader {

	private static final byte VECTOR_X = VectorProcessor.VECTOR_X;
	private static final byte VECTOR_Y = VectorProcessor.VECTOR_Y;
	private static final byte VECTOR_Z = VectorProcessor.VECTOR_Z;

	private static final byte FP_BITS = MathProcessor.FP_BITS;
	private static final int FP_ONE = MathProcessor.FP_ONE;

	private final MathProcessor mathProcessor;
	private final MatrixProcessor matrixProcessor;
	private final VectorProcessor vectorProcessor;
	private final ColorProcessor colorProcessor;
	private final GraphicsProcessor graphicsProcessor;

	private final int[][] modelMatrix;
	private final int[][] normalMatrix;
	private final int[][] viewMatrix;
	private final int[][] projectionMatrix;
	
	private int directionalLightIndex;
	private final int[][] directionalLightMatrix;
	private final FrameBuffer directionalShadowMap;
	
	private int spotLightIndex;
	private final int[][] spotLightMatrix;
	private final FrameBuffer spotShadowMap;

	private List<Light> lights;
	private FrameBuffer frameBuffer;

	public PhongSpecularShader(CentralProcessor centralProcessor) {
		super(centralProcessor);
		this.mathProcessor = centralProcessor.getMathProcessor();
		this.matrixProcessor = centralProcessor.getMatrixProcessor();
		this.vectorProcessor = centralProcessor.getVectorProcessor();
		this.colorProcessor = centralProcessor.getColorProcessor();
		this.graphicsProcessor = centralProcessor.getGraphicsProcessor();

		this.modelMatrix = matrixProcessor.generate();
		this.normalMatrix = matrixProcessor.generate();
		this.viewMatrix = matrixProcessor.generate();
		this.projectionMatrix = matrixProcessor.generate();
		
		this.directionalLightMatrix = matrixProcessor.generate();
		this.directionalShadowMap = new FrameBuffer(640, 640);
		
		this.spotLightMatrix = matrixProcessor.generate();
		this.spotShadowMap = new FrameBuffer(320, 320);

		new DirectionalPass(this);
		new SpotPass(this);
		new RenderPass(this);
	}

	@Override
	public void update(ShaderDataBuffer shaderDataBuffer) {
		this.lights = shaderDataBuffer.getLights();
		this.frameBuffer = shaderDataBuffer.getFrameBuffer();
		frameBuffer.clearColorBuffer();
		frameBuffer.clearDepthBuffer();
		directionalShadowMap.clearDepthBuffer();
		spotShadowMap.clearDepthBuffer();
		
		directionalLightIndex = -1;
		spotLightIndex = -1;
		
		int directional = Integer.MAX_VALUE;
		int spot = Integer.MAX_VALUE;
		for (int i = 0; i < lights.size(); i++) {
			Light light = lights.get(i);
			int distance = vectorProcessor.magnitude(light.getTransform().getLocation());
			switch (light.getType()) {
				case DIRECTIONAL:
					if (distance < directional) {
						directional = distance;
						directionalLightIndex = i;
					}
					break;
				case POINT:
					break;
				case SPOT:
					if (distance < spot) {
						spot = distance;
						spotLightIndex = i;
					}
					break;
			}
		}
	}

	private class DirectionalPass extends ShaderPass {

		private final int[] lightFrustum;

		private Camera camera;

		public DirectionalPass(Shader shader) {
			super(shader);
			this.lightFrustum = vectorProcessor.generate(30, 0, 10000);
		}

		@Override
		public void setup(Model model, Camera camera) {
			this.camera = camera;
			
			if (directionalLightIndex < 0)
				return;
			
			graphicsProcessor.setup(directionalShadowMap.getSize(), camera.getCanvas(), this);
			
			matrixProcessor.copy(viewMatrix, MatrixProcessor.MATRIX_IDENTITY);
			matrixProcessor.copy(projectionMatrix, MatrixProcessor.MATRIX_IDENTITY);
			matrixProcessor.copy(modelMatrix, MatrixProcessor.MATRIX_IDENTITY);
			
			graphicsProcessor.getModelMatrix(model.getTransform(), modelMatrix);
			graphicsProcessor.getViewMatrix(lights.get(directionalLightIndex).getTransform(), viewMatrix);
			graphicsProcessor.getOrthographicMatrix(lightFrustum, projectionMatrix);
			matrixProcessor.multiply(projectionMatrix, viewMatrix, directionalLightMatrix);
		}

		@Override
		public void vertex(int index, Vertex vertex) {
			if (directionalLightIndex < 0)
				return;
			int[] location = vertex.getLocation();
			vectorProcessor.multiply(location, modelMatrix, location);
			vectorProcessor.multiply(location, directionalLightMatrix, location);
			graphicsProcessor.viewport(location, location);
		}

		@Override
		public void geometry(Face face) {
			if (directionalLightIndex < 0)
				return;
			int[] location1 = face.getVertex(0).getLocation();
			int[] location2 = face.getVertex(1).getLocation();
			int[] location3 = face.getVertex(2).getLocation();

			if (!graphicsProcessor.isBackface(location1, location2, location3)
					&& graphicsProcessor.isInsideFrustum(location1, location2, location3, camera.getFrustum())) {
				graphicsProcessor.drawTriangle(location1, location2, location3);
			}
		}

		@Override
		public void fragment(int[] location, int[] barycentric) {
//			int color = (location[VECTOR_Z] + 100) >> 3;
//			color = colorProcessor.generate(color, color, color);
//			frameBuffer.setPixel(location[VECTOR_X], location[VECTOR_Y], location[VECTOR_Z] - 1000, (byte) 0, color);
			directionalShadowMap.setPixel(location[VECTOR_X], location[VECTOR_Y], location[VECTOR_Z], (byte) 0, 0);
		}

	}
	
	private class SpotPass extends ShaderPass {

		private final int[] lightFrustum;

		private Camera camera;

		public SpotPass(Shader shader) {
			super(shader);
			this.lightFrustum = vectorProcessor.generate(30, 0, 10000);
		}

		@Override
		public void setup(Model model, Camera camera) {
			this.camera = camera;
			
			if (spotLightIndex < 0)
				return;
			
			lightFrustum[0] = 45 - (lights.get(spotLightIndex).getSpotSize() >> (FP_BITS + 1));
			
			graphicsProcessor.setup(spotShadowMap.getSize(), camera.getCanvas(), this);
			
			matrixProcessor.copy(viewMatrix, MatrixProcessor.MATRIX_IDENTITY);
			matrixProcessor.copy(projectionMatrix, MatrixProcessor.MATRIX_IDENTITY);
			matrixProcessor.copy(modelMatrix, MatrixProcessor.MATRIX_IDENTITY);
			
			graphicsProcessor.getModelMatrix(model.getTransform(), modelMatrix);
			graphicsProcessor.getViewMatrix(lights.get(spotLightIndex).getTransform(), viewMatrix);
			graphicsProcessor.getPerspectiveMatrix(lightFrustum, projectionMatrix);
			matrixProcessor.multiply(projectionMatrix, viewMatrix, spotLightMatrix);
		}

		@Override
		public void vertex(int index, Vertex vertex) {
			if (spotLightIndex < 0)
				return;
			int[] location = vertex.getLocation();
			vectorProcessor.multiply(location, modelMatrix, location);
			vectorProcessor.multiply(location, spotLightMatrix, location);
			graphicsProcessor.viewport(location, location);
		}

		@Override
		public void geometry(Face face) {
			if (spotLightIndex < 0)
				return;
			int[] location1 = face.getVertex(0).getLocation();
			int[] location2 = face.getVertex(1).getLocation();
			int[] location3 = face.getVertex(2).getLocation();
			
			if (!graphicsProcessor.isBackface(location1, location2, location3)
					&& graphicsProcessor.isInsideFrustum(location1, location2, location3, camera.getFrustum())) {
				graphicsProcessor.drawTriangle(location1, location2, location3);
			}
		}

		@Override
		public void fragment(int[] location, int[] barycentric) {
//			int color = (location[VECTOR_Z] + 100) >> 3;
//			color = colorProcessor.generate(color, color, color);
//			frameBuffer.setPixel(location[VECTOR_X], location[VECTOR_Y], location[VECTOR_Z] - 1000, (byte) 0, color);
			spotShadowMap.setPixel(location[VECTOR_X], location[VECTOR_Y], location[VECTOR_Z], (byte) 0, 0);
		}

	}

	private class RenderPass extends ShaderPass {

		private final int[] uvX;
		private final int[] uvY;

		private final int[] fragmentLocation;
		private final int[] normalizedNormal;
		private final int[] lightLocation;
		private final int[] lightDirection;
		private final int[] viewDirection;

		private final int[] viewDirectionX;
		private final int[] viewDirectionY;
		private final int[] viewDirectionZ;
		private final int[] locationX;
		private final int[] locationY;
		private final int[] locationZ;
		private final int[] normalX;
		private final int[] normalY;
		private final int[] normalZ;

		private final int[] directionalLocation;
		private final int[] directionalLocationX;
		private final int[] directionalLocationY;
		private final int[] directionalLocationZ;
		
		private final int[] spotLocation;
		private final int[] spotLocationX;
		private final int[] spotLocationY;
		private final int[] spotLocationZ;

		private Material material;
		private int modelColor;
		private Texture texture;

		private Camera camera;

		public RenderPass(Shader shader) {
			super(shader);

			this.uvX = vectorProcessor.generate();
			this.uvY = vectorProcessor.generate();

			this.fragmentLocation = vectorProcessor.generate();
			this.normalizedNormal = vectorProcessor.generate();
			this.lightLocation = vectorProcessor.generate();
			this.lightDirection = vectorProcessor.generate();
			this.viewDirection = vectorProcessor.generate();

			this.viewDirectionX = vectorProcessor.generate();
			this.viewDirectionY = vectorProcessor.generate();
			this.viewDirectionZ = vectorProcessor.generate();
			this.locationX = vectorProcessor.generate();
			this.locationY = vectorProcessor.generate();
			this.locationZ = vectorProcessor.generate();
			this.normalX = vectorProcessor.generate();
			this.normalY = vectorProcessor.generate();
			this.normalZ = vectorProcessor.generate();

			this.directionalLocation = vectorProcessor.generate();
			this.directionalLocationX = vectorProcessor.generate();
			this.directionalLocationY = vectorProcessor.generate();
			this.directionalLocationZ = vectorProcessor.generate();
			
			this.spotLocation = vectorProcessor.generate();
			this.spotLocationX = vectorProcessor.generate();
			this.spotLocationY = vectorProcessor.generate();
			this.spotLocationZ = vectorProcessor.generate();
		}

		@Override
		public void setup(Model model, Camera camera) {
			this.camera = camera;

			graphicsProcessor.setup(frameBuffer.getSize(), camera.getCanvas(), this);

			matrixProcessor.copy(modelMatrix, MatrixProcessor.MATRIX_IDENTITY);
			matrixProcessor.copy(normalMatrix, MatrixProcessor.MATRIX_IDENTITY);
			matrixProcessor.copy(viewMatrix, MatrixProcessor.MATRIX_IDENTITY);
			matrixProcessor.copy(projectionMatrix, MatrixProcessor.MATRIX_IDENTITY);

			graphicsProcessor.getModelMatrix(model.getTransform(), modelMatrix);
			graphicsProcessor.getNormalMatrix(model.getTransform(), normalMatrix);
			graphicsProcessor.getViewMatrix(camera.getTransform(), viewMatrix);

			switch (camera.getType()) {
			case ORTHOGRAPHIC:
				graphicsProcessor.getOrthographicMatrix(camera.getFrustum(), projectionMatrix);
				break;

			case PERSPECTIVE:
				graphicsProcessor.getPerspectiveMatrix(camera.getFrustum(), projectionMatrix);
				break;
			}
		}

		@Override
		public void vertex(int index, Vertex vertex) {
			int[] location = vertex.getLocation();
			int[] normal = vertex.getNormal();

			vectorProcessor.multiply(location, modelMatrix, location);
			locationX[index] = location[VECTOR_X];
			locationY[index] = location[VECTOR_Y];
			locationZ[index] = location[VECTOR_Z];

			vectorProcessor.multiply(location, directionalLightMatrix, directionalLocation);
			graphicsProcessor.setup(directionalShadowMap.getSize(), camera.getCanvas(), this);
			graphicsProcessor.viewport(directionalLocation, directionalLocation);
			graphicsProcessor.setup(frameBuffer.getSize(), camera.getCanvas(), this);
			directionalLocationX[index] = directionalLocation[VECTOR_X];
			directionalLocationY[index] = directionalLocation[VECTOR_Y];
			directionalLocationZ[index] = directionalLocation[VECTOR_Z];
			
			vectorProcessor.multiply(location, spotLightMatrix, spotLocation);
			graphicsProcessor.setup(spotShadowMap.getSize(), camera.getCanvas(), this);
			graphicsProcessor.viewport(spotLocation, spotLocation);
			graphicsProcessor.setup(frameBuffer.getSize(), camera.getCanvas(), this);
			spotLocationX[index] = spotLocation[VECTOR_X];
			spotLocationY[index] = spotLocation[VECTOR_Y];
			spotLocationZ[index] = spotLocation[VECTOR_Z];

			vectorProcessor.subtract(camera.getTransform().getLocation(), location, viewDirection);
			vectorProcessor.normalize(viewDirection, viewDirection);
			viewDirectionX[index] = viewDirection[VECTOR_X];
			viewDirectionY[index] = viewDirection[VECTOR_Y];
			viewDirectionZ[index] = viewDirection[VECTOR_Z];

			vectorProcessor.multiply(location, viewMatrix, location);
			vectorProcessor.multiply(location, projectionMatrix, location);
			graphicsProcessor.viewport(location, location);

			vectorProcessor.multiply(normal, normalMatrix, normal);
			vectorProcessor.normalize(normal, normalizedNormal);
			normalX[index] = normalizedNormal[VECTOR_X];
			normalY[index] = normalizedNormal[VECTOR_Y];
			normalZ[index] = normalizedNormal[VECTOR_Z];
		}

		@Override
		public void geometry(Face face) {
			int[] location1 = face.getVertex(0).getLocation();
			int[] location2 = face.getVertex(1).getLocation();
			int[] location3 = face.getVertex(2).getLocation();

			material = face.getMaterial();

			if (!graphicsProcessor.isBackface(location1, location2, location3)
					&& graphicsProcessor.isInsideFrustum(location1, location2, location3, camera.getFrustum())) {
				texture = face.getMaterial().getTexture();
				// set uv values that will be interpolated and fit uv into texture resolution
				if (texture != null) {
					int width = texture.getWidth() - 1;
					int height = texture.getHeight() - 1;
					uvX[0] = mathProcessor.multiply(face.getUV1()[VECTOR_X], width);
					uvX[1] = mathProcessor.multiply(face.getUV2()[VECTOR_X], width);
					uvX[2] = mathProcessor.multiply(face.getUV3()[VECTOR_X], width);
					uvY[0] = mathProcessor.multiply(face.getUV1()[VECTOR_Y], height);
					uvY[1] = mathProcessor.multiply(face.getUV2()[VECTOR_Y], height);
					uvY[2] = mathProcessor.multiply(face.getUV3()[VECTOR_Y], height);
				}
				graphicsProcessor.drawTriangle(location1, location2, location3);
			}
		}

		@Override
		public void fragment(int[] location, int[] barycentric) {

			directionalLocation[VECTOR_X] = graphicsProcessor.interpolate(directionalLocationX, barycentric);
			directionalLocation[VECTOR_Y] = graphicsProcessor.interpolate(directionalLocationY, barycentric);
			directionalLocation[VECTOR_Z] = graphicsProcessor.interpolate(directionalLocationZ, barycentric);
			
			spotLocation[VECTOR_X] = graphicsProcessor.interpolate(spotLocationX, barycentric);
			spotLocation[VECTOR_Y] = graphicsProcessor.interpolate(spotLocationY, barycentric);
			spotLocation[VECTOR_Z] = graphicsProcessor.interpolate(spotLocationZ, barycentric);

			viewDirection[VECTOR_X] = graphicsProcessor.interpolate(viewDirectionX, barycentric);
			viewDirection[VECTOR_Y] = graphicsProcessor.interpolate(viewDirectionY, barycentric);
			viewDirection[VECTOR_Z] = graphicsProcessor.interpolate(viewDirectionZ, barycentric);
			
			fragmentLocation[VECTOR_X] = graphicsProcessor.interpolate(locationX, barycentric);
			fragmentLocation[VECTOR_Y] = graphicsProcessor.interpolate(locationY, barycentric);
			fragmentLocation[VECTOR_Z] = graphicsProcessor.interpolate(locationZ, barycentric);

			normalizedNormal[VECTOR_X] = graphicsProcessor.interpolate(normalX, barycentric);
			normalizedNormal[VECTOR_Y] = graphicsProcessor.interpolate(normalY, barycentric);
			normalizedNormal[VECTOR_Z] = graphicsProcessor.interpolate(normalZ, barycentric);

			int lightColor = ColorProcessor.WHITE;
			int lightFactor = 0;

			for (int i = 0; i < lights.size(); i++) {
				Light light = lights.get(i);
				int currentFactor = 0;
				int attenuation = 0;
				switch (light.getType()) {
				case DIRECTIONAL:
					vectorProcessor.invert(light.getDirection(), lightDirection);
					currentFactor = getLightFactor(normalizedNormal, lightDirection, viewDirection, material);
					break;
				case POINT:
					int[] loc = light.getTransform().getLocation();
					loc[VECTOR_X] = -loc[VECTOR_X];
					vectorProcessor.subtract(light.getTransform().getLocation(), fragmentLocation, lightLocation);
					loc[VECTOR_X] = -loc[VECTOR_X];
					// attenuation
					attenuation = getAttenuation(lightLocation);
					vectorProcessor.normalize(lightLocation, lightLocation);
					// other light values
					currentFactor = getLightFactor(normalizedNormal, lightLocation, viewDirection, material);
					currentFactor = (currentFactor << 8) / attenuation;
					break;
				case SPOT:
					vectorProcessor.invert(light.getDirection(), lightDirection);
					loc = light.getTransform().getLocation();
					loc[VECTOR_X] = -loc[VECTOR_X];
					vectorProcessor.subtract(loc, fragmentLocation, lightLocation);
					loc[VECTOR_X] = -loc[VECTOR_X];
					// attenuation
					attenuation = getAttenuation(lightLocation);
					vectorProcessor.normalize(lightLocation, lightLocation);
					int theta = vectorProcessor.dotProduct(lightLocation, lightDirection);
					int phi = mathProcessor.cos(light.getSpotSize() >> 1);
					if (theta > phi) {
						int intensity = -mathProcessor.divide(phi - theta, light.getSpotSoftness() + 1);
						intensity = mathProcessor.clamp(intensity, 1, FP_ONE);
						currentFactor = getLightFactor(normalizedNormal, lightLocation, viewDirection, material);
						currentFactor = (currentFactor * intensity) / attenuation;
					}
					break;
				}
				currentFactor = mathProcessor.multiply(currentFactor, light.getStrength());
				boolean inShadow = false;
				if (i == directionalLightIndex) {
					inShadow = inShadow(directionalLocation, directionalShadowMap);
					lightFactor += currentFactor;
				} else if ((i == spotLightIndex) && (currentFactor > 10)) {
					inShadow = inShadow(spotLocation, spotShadowMap);
				}
				if(inShadow) {
					lightColor = colorProcessor.lerp(lightColor, light.getShadowColor(), 128);
				} else {
					lightColor = colorProcessor.lerp(lightColor, light.getColor(), currentFactor);
					lightFactor += currentFactor;
				}
			}
			if (texture != null) {
				int u = graphicsProcessor.interpolate(uvX, barycentric);
				int v = graphicsProcessor.interpolate(uvY, barycentric);
				modelColor = texture.getPixel(u, v);
				if (colorProcessor.getAlpha(modelColor) == 0) // discard pixel if alpha = 0
					return;
			} else {
				modelColor = material.getColor();
			}
			modelColor = colorProcessor.lerp(ColorProcessor.BLACK, modelColor, lightFactor);
			modelColor = colorProcessor.multiplyColor(modelColor, lightColor);
			frameBuffer.setPixel(location[VECTOR_X], location[VECTOR_Y], location[VECTOR_Z], (byte) 0, modelColor);
		}

		private int getLightFactor(int[] normal, int[] lightDirection, int[] viewDirection, Material material) {
			// diffuse
			int dotProduct = vectorProcessor.dotProduct(normal, lightDirection);
			int diffuseFactor = Math.max(dotProduct, 0);
			diffuseFactor = mathProcessor.multiply(diffuseFactor, material.getDiffuseIntensity());
			// specular
			vectorProcessor.invert(lightDirection, lightDirection);
			vectorProcessor.reflect(lightDirection, normal, lightDirection);
			dotProduct = vectorProcessor.dotProduct(viewDirection, lightDirection);
			int specularFactor = Math.max(dotProduct, 0);
			specularFactor = mathProcessor.pow(specularFactor, material.getShininess() >> FP_BITS);
			specularFactor = mathProcessor.multiply(specularFactor, material.getSpecularIntensity());
			// putting it all together...
			return (diffuseFactor + specularFactor << 8) >> FP_BITS;
		}

		private int getAttenuation(int[] lightLocation) {
			// attenuation
			long distance = vectorProcessor.magnitude(lightLocation);
			int attenuation = FP_ONE;
			attenuation += mathProcessor.multiply(distance, 14000);
			attenuation += mathProcessor.multiply(mathProcessor.multiply(distance, distance), 90);
			attenuation >>= FP_BITS;
			return ((attenuation << 8) >> FP_BITS) + 1;
		}

		private boolean inShadow(int[] lightSpaceLocation, FrameBuffer shadowMap) {
			int x = lightSpaceLocation[VECTOR_X];
			int y = lightSpaceLocation[VECTOR_Y];
			x = mathProcessor.clamp(x, 0, shadowMap.getSize()[0] - 1);
			y = mathProcessor.clamp(y, 0, shadowMap.getSize()[1] - 1);
			int depth = shadowMap.getDepth(x, y);
			int bias = 50;
			return depth < lightSpaceLocation[VECTOR_Z] - bias;
		}
	}
}
