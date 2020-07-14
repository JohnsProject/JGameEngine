
package com.johnsproject.jgameengine.shading;

import static com.johnsproject.jgameengine.util.FixedPointUtils.*;

import java.util.List;

import com.johnsproject.jgameengine.model.Camera;
import com.johnsproject.jgameengine.model.Frustum;
import com.johnsproject.jgameengine.model.FrustumType;
import com.johnsproject.jgameengine.model.Light;
import com.johnsproject.jgameengine.model.Texture;
import com.johnsproject.jgameengine.util.MatrixUtils;
import com.johnsproject.jgameengine.util.VectorUtils;

public class ForwardShaderBuffer implements ShaderBuffer {
	
	private Camera camera;
	private List<Light> lights;
	
	private final int[][] projectionMatrix;

	private Light shadowDirectionalLight;
	private final Frustum directionalLightFrustum;
	private final Texture directionalShadowMap;

	private Light shadowSpotLight;
	private final Frustum spotLightFrustum;
	private final Texture spotShadowMap;
	
	private boolean foundMainDirectionalLight;
	private long spotLightDistance;
	
	public ForwardShaderBuffer() {
		this.projectionMatrix = MatrixUtils.indentityMatrix();
		
		this.directionalLightFrustum = new Frustum(0, FP_ONE, 0, FP_ONE, FP_ONE, FP_ONE * 10000);
		this.directionalLightFrustum.setType(FrustumType.ORTHOGRAPHIC);
		this.directionalLightFrustum.setFocalLength(FP_ONE >> 3);
		this.directionalShadowMap = new Texture(1024, 1024);
		
		this.spotLightFrustum = new Frustum(0, FP_ONE, 0, FP_ONE, FP_HALF, FP_ONE * 1000);
		this.spotLightFrustum.setFocalLength(FP_HALF);
		this.spotShadowMap = new Texture(512, 512);
	}

	public void setup(Camera camera, List<Light> lights) {
		this.camera = camera;
		this.lights = lights;
		final int[] cameraLocation = camera.getTransform().getLocation();
		long maxLightDistance = camera.getMaxLightDistance();
		// square the far distance because square distance calculation is used to save performance
		maxLightDistance = (maxLightDistance * maxLightDistance) >> FP_BIT;
		resetLightIndices();
		for(int i = 0; i < lights.size(); i++) {
			final Light light = lights.get(i);
			if(!light.isActive())
				continue;
			final int[] lightLocation = light.getTransform().getLocation();
			final long lightDistance = VectorUtils.squaredDistance(lightLocation, cameraLocation);
			light.setCulled(lightDistance > maxLightDistance);
			if(light.isCulled() || !light.hasShadow())
				continue;
			searchNearestLights(light, lightDistance);
		}
		initializeLightMatrices();
	}
	
	private void resetLightIndices() {
		shadowDirectionalLight = null;
		shadowSpotLight = null;
		foundMainDirectionalLight = false;
		spotLightDistance = Integer.MAX_VALUE;
	}
	
	private void searchNearestLights(Light light, long lightDistance) {
		switch (light.getType()) {
		case DIRECTIONAL:
			searchNearestDirectionalLight(light, lightDistance);
			break;
		case SPOT:
			searchNearestSpotLight(light, lightDistance);
			break;
		default:
			break;
		}
	}
	
	private void searchNearestDirectionalLight(Light light, long lightDistance) {
		if(!foundMainDirectionalLight) {
			shadowDirectionalLight = light;
			if(light.isMain()) {
				foundMainDirectionalLight = true;
			}
		}
	}
	
	private void searchNearestSpotLight(Light light, long lightDistance) {
		if(spotLightDistance == Integer.MIN_VALUE)
			return;
		if(lightDistance < spotLightDistance) {
			spotLightDistance = lightDistance;
			shadowSpotLight = light;
			if(light.isMain()) {
				spotLightDistance = Integer.MIN_VALUE;
			}
		}
	}
	
	private void initializeLightMatrices() {
		if(camera.isMain()) {
			initializeDirectionalLightMatrix();
			initializeSpotLightMatrix();
		}
	}
	
	private void initializeDirectionalLightMatrix() {
		if (shadowDirectionalLight != null) {
			final int portWidth = directionalShadowMap.getWidth();
			final int portHeight = directionalShadowMap.getHeight();
			directionalLightFrustum.setRenderTargetSize(portWidth, portHeight);
			directionalShadowMap.fill(Integer.MAX_VALUE);
			final int[][] lightSpaceMatrix = shadowDirectionalLight.getTransform().getSpaceEnterMatrix();
			final int[][] frustumProjectionMatrix = directionalLightFrustum.getProjectionMatrix();
			MatrixUtils.copy(projectionMatrix, directionalLightFrustum.getProjectionMatrix());
			MatrixUtils.multiply(projectionMatrix, lightSpaceMatrix, frustumProjectionMatrix);
		}
	}
	
	private void initializeSpotLightMatrix() {
		if (shadowSpotLight != null) {
			final int portWidth = spotShadowMap.getWidth();
			final int portHeight = spotShadowMap.getHeight();
			spotLightFrustum.setRenderTargetSize(portWidth, portHeight);
			spotShadowMap.fill(Integer.MAX_VALUE);
			final int[][] lightSpaceMatrix = shadowSpotLight.getTransform().getSpaceEnterMatrix();
			final int[][] frustumProjectionMatrix = spotLightFrustum.getProjectionMatrix();
			MatrixUtils.copy(projectionMatrix, spotLightFrustum.getProjectionMatrix());
			MatrixUtils.multiply(projectionMatrix, lightSpaceMatrix, frustumProjectionMatrix);
		}
	}
	
	public Camera getCamera() {
		return camera;
	}

	public List<Light> getLights() {
		return lights;
	}

	public Light getShadowDirectionalLight() {
		return shadowDirectionalLight;
	}

	public Texture getDirectionalShadowMap() {
		return directionalShadowMap;
	}
	
	public Frustum getDirectionalLightFrustum() {
		return directionalLightFrustum;
	}
	
	public Light getShadowSpotLight() {
		return shadowSpotLight;
	}

	public Texture getSpotShadowMap() {
		return spotShadowMap;
	}

	public Frustum getSpotLightFrustum() {
		return spotLightFrustum;
	}
}