
package com.johnsproject.jgameengine.shading;

import static com.johnsproject.jgameengine.util.FixedPointUtils.*;

import java.util.List;

import com.johnsproject.jgameengine.model.Camera;
import com.johnsproject.jgameengine.model.Frustum;
import com.johnsproject.jgameengine.model.FrustumType;
import com.johnsproject.jgameengine.model.Light;
import com.johnsproject.jgameengine.model.Texture;
import com.johnsproject.jgameengine.model.Transform;
import com.johnsproject.jgameengine.util.FixedPointUtils;
import com.johnsproject.jgameengine.util.MatrixUtils;
import com.johnsproject.jgameengine.util.VectorUtils;

public class ForwardShaderBuffer implements ShaderBuffer {
	
	private static final int LIGHT_RANGE = FixedPointUtils.toFixedPoint(50000f);
	
	private Camera camera;
	private List<Light> lights;
	
	private final int[][] projectionMatrix;
	
	private int directionalLightIndex;
	private final Frustum directionalLightFrustum;
	private final Texture directionalShadowMap;
	
	private int spotLightIndex;
	private final Frustum spotLightFrustum;
	private final Texture spotShadowMap;
	
	private int pointLightIndex;
	private final Frustum pointLightFrustum;
	private final int[][][] pointLightMatrices;
	private final Texture[] pointShadowMaps;
	
	public ForwardShaderBuffer() {
		this.projectionMatrix = MatrixUtils.indentityMatrix();
		
		this.directionalLightIndex = -1;
		this.directionalLightFrustum = new Frustum(0, FP_ONE, 0, FP_ONE, FP_ONE, FP_ONE * 10000);
		this.directionalLightFrustum.setType(FrustumType.ORTHOGRAPHIC);
		this.directionalLightFrustum.setFocalLength(FP_ONE >> 3);
		this.directionalShadowMap = new Texture(512, 512);
		
		this.spotLightIndex = -1;
		this.spotLightFrustum = new Frustum(0, FP_ONE, 0, FP_ONE, FP_HALF, FP_ONE * 1000);
		this.spotLightFrustum.setFocalLength(FP_HALF);
		this.spotShadowMap = new Texture(256, 256);
		
		this.pointLightIndex = -1;
		this.pointLightFrustum = new Frustum(0, FP_ONE, 0, FP_ONE, 0, FP_ONE * 1000);
		this.pointLightFrustum.setFocalLength(FP_ONE >> 5);
		this.pointLightMatrices = new int[6][MatrixUtils.MATRIX_SIZE][MatrixUtils.MATRIX_SIZE];
		this.pointShadowMaps = new Texture[6];
		for (int i = 0; i < 6; i++) {
			pointLightMatrices[i] = MatrixUtils.indentityMatrix();
			pointShadowMaps[i] = new Texture(256, 256);
		}
	}

	public void setup(Camera camera, List<Light> lights) {
		this.camera = camera;
		this.lights = lights;
		shadowLightsSetup(camera, lights);
		renderSetup(camera, lights);
	}
	
	private void renderSetup(Camera camera, List<Light> lights) {
		for(int i = 0; i < lights.size(); i++) {
			Light light = lights.get(i);
			Transform lightTransform = light.getTransform();
			int[] lightPosition = lightTransform.getLocation();
			long dist = VectorUtils.squaredDistance(camera.getTransform().getLocation(), lightPosition);
			light.setCulled(dist > LIGHT_RANGE);
		}
	}
	
	private void shadowLightsSetup(Camera camera, List<Light> lights) {
		directionalLightIndex = -1;
		spotLightIndex = -1;
		pointLightIndex = -1;
		boolean foundMainDirectionalLight = false;
		long spotDistance = Integer.MAX_VALUE;
		long pointDistance = Integer.MAX_VALUE;
		Transform directionalLightTransform = null;
		Transform spotLightTransform = null;
		Transform pointLightTransform = null;
		int[] cameraLocation = camera.getTransform().getLocation();
		int lightIndex = 0;
		for(int i = 0; i < lights.size(); i++) {
			Light light = lights.get(i);
			if(light.isActive()) {
				Transform lightTransform = light.getTransform();
				int[] lightLocation = lightTransform.getLocation();
				long dist = VectorUtils.squaredDistance(cameraLocation, lightLocation);
				if(dist < LIGHT_RANGE) {
					switch (light.getType()) {
					case DIRECTIONAL:
						if(!foundMainDirectionalLight) {
							directionalLightIndex = lightIndex;
							directionalLightTransform = lightTransform;
							if(light.isMain()) {
								foundMainDirectionalLight = true;
							}
						}
						break;
					case SPOT:
						if((spotDistance != Integer.MIN_VALUE) && (dist < spotDistance)) {
							spotDistance = dist;
							spotLightIndex = lightIndex;
							spotLightTransform = lightTransform;
							if(light.isMain()) {
								spotDistance = Integer.MIN_VALUE;
							}
						}
						break;
					case POINT:
						if((pointDistance != Integer.MIN_VALUE) && (dist < pointDistance)) {
							pointDistance = dist;
							pointLightIndex = lightIndex;
							pointLightTransform = lightTransform;
							if(light.isMain()) {
								pointDistance = Integer.MIN_VALUE;
							}
						}
						break;					
					}
				}
				lightIndex++;
			}
		}
		if(camera.isMain()) {
			directionalSetup(camera, directionalLightTransform);
			spotSetup(camera, spotLightTransform);
			pointSetup(camera, pointLightTransform);
		}
	}
	
	private void directionalSetup(Camera camera, Transform lightTransform) {
		if (directionalLightIndex != -1) {
			int portWidth = directionalShadowMap.getWidth();
			int portHeight = directionalShadowMap.getHeight();
			directionalLightFrustum.setRenderTargetSize(portWidth, portHeight);
			directionalShadowMap.fill(Integer.MAX_VALUE);
			MatrixUtils.copy(projectionMatrix, directionalLightFrustum.getProjectionMatrix());
			MatrixUtils.multiply(projectionMatrix, lightTransform.getSpaceEnterMatrix(), directionalLightFrustum.getProjectionMatrix());
		}
	}
	
	private void spotSetup(Camera camera, Transform lightTransform) {
		if (spotLightIndex != -1) {
			int portWidth = spotShadowMap.getWidth();
			int portHeight = spotShadowMap.getHeight();
			spotLightFrustum.setRenderTargetSize(portWidth, portHeight);
			spotShadowMap.fill(Integer.MAX_VALUE);
			MatrixUtils.copy(projectionMatrix, spotLightFrustum.getProjectionMatrix());
			MatrixUtils.multiply(projectionMatrix, lightTransform.getSpaceEnterMatrix(), spotLightFrustum.getProjectionMatrix());
		}
	}
	
	private void pointSetup(Camera camera, Transform lightTransform) {
		if (pointLightIndex != -1) {
			int portWidth = pointShadowMaps[0].getWidth();
			int portHeight = pointShadowMaps[0].getHeight();
			pointLightFrustum.setRenderTargetSize(portWidth, portHeight);
			for (int i = 0; i < pointShadowMaps.length; i++) {
				pointShadowMaps[i].fill(Integer.MAX_VALUE);
			}
			MatrixUtils.copy(projectionMatrix, pointLightFrustum.getProjectionMatrix());
			final int fixedPoint90 = FixedPointUtils.toFixedPoint(90f);
			lightTransform.setRotation(0, 0, 0);
			MatrixUtils.multiply(projectionMatrix, lightTransform.getSpaceEnterMatrix(), pointLightMatrices[0]);
			lightTransform.rotate(fixedPoint90, 0, 0);
			MatrixUtils.multiply(projectionMatrix, lightTransform.getSpaceEnterMatrix(), pointLightMatrices[1]);
			lightTransform.rotate(fixedPoint90, 0, 0);
			MatrixUtils.multiply(projectionMatrix, lightTransform.getSpaceEnterMatrix(), pointLightMatrices[2]);
			lightTransform.rotate(fixedPoint90, 0, 0);
			MatrixUtils.multiply(projectionMatrix, lightTransform.getSpaceEnterMatrix(), pointLightMatrices[3]);
			lightTransform.setRotation(0, fixedPoint90, 0);
			MatrixUtils.multiply(projectionMatrix, lightTransform.getSpaceEnterMatrix(), pointLightMatrices[4]);
			lightTransform.setRotation(0, -fixedPoint90, 0);
			MatrixUtils.multiply(projectionMatrix, lightTransform.getSpaceEnterMatrix(), pointLightMatrices[5]);
			lightTransform.setRotation(0, 0, 0);
		}
	}
	
	public Camera getCamera() {
		return camera;
	}

	public List<Light> getLights() {
		return lights;
	}

	public int getDirectionalLightIndex() {
		return directionalLightIndex;
	}

	public Texture getDirectionalShadowMap() {
		return directionalShadowMap;
	}
	
	public Frustum getDirectionalLightFrustum() {
		return directionalLightFrustum;
	}
	
	public int getSpotLightIndex() {
		return spotLightIndex;
	}

	public Texture getSpotShadowMap() {
		return spotShadowMap;
	}

	public Frustum getSpotLightFrustum() {
		return spotLightFrustum;
	}

	public int getPointLightIndex() {
		return pointLightIndex;
	}

	public int[][][] getPointLightMatrices() {
		return pointLightMatrices;
	}

	public Texture[] getPointShadowMaps() {
		return pointShadowMaps;
	}
	
	public Frustum getPointLightFrustum() {
		return pointLightFrustum;
	}
}