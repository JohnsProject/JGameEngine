
package com.johnsproject.jgameengine.shading;

import static com.johnsproject.jgameengine.util.FixedPointUtils.*;

import java.util.List;

import com.johnsproject.jgameengine.model.Camera;
import com.johnsproject.jgameengine.model.Frustum;
import com.johnsproject.jgameengine.model.FrustumType;
import com.johnsproject.jgameengine.model.Light;
import com.johnsproject.jgameengine.model.Texture;
import com.johnsproject.jgameengine.model.Transform;
import com.johnsproject.jgameengine.util.MatrixUtils;
import com.johnsproject.jgameengine.util.VectorUtils;

public class ForwardShaderBuffer implements ShaderBuffer {
	
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
	
	private boolean foundMainDirectionalLight;
	private long spotLightDistance;
	private long pointLightDistance;
	private Light shadowDirectionalLight;
	private Light shadowSpotLight;
	private Light shadowPointLight;
	
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
		this.pointLightFrustum = new Frustum(0, FP_ONE, 0, FP_ONE, FP_HALF, FP_ONE * 1000);
		this.pointLightFrustum.setFocalLength(FP_HALF >> 2);
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
			if(light.isCulled())
				continue;
			searchNearestLights(light, i, lightDistance);
		}
		initializeLightMatrices();
	}
	
	private void resetLightIndices() {
		directionalLightIndex = -1;
		spotLightIndex = -1;
		pointLightIndex = -1;
		foundMainDirectionalLight = false;
		spotLightDistance = Integer.MAX_VALUE;
		pointLightDistance = Integer.MAX_VALUE;
	}
	
	private void searchNearestLights(Light light, int lightIndex, long lightDistance) {
		switch (light.getType()) {
		case DIRECTIONAL:
			searchNearestDirectionalLight(light, lightIndex, lightDistance);
			break;
		case SPOT:
			searchNearestSpotLight(light, lightIndex, lightDistance);
			break;
		case POINT:
			searchNearestPointLight(light, lightIndex, lightDistance);
			break;
		}
	}
	
	private void searchNearestDirectionalLight(Light light, int lightIndex, long lightDistance) {
		if(!foundMainDirectionalLight) {
			directionalLightIndex = lightIndex;
			shadowDirectionalLight = light;
			if(light.isMain()) {
				foundMainDirectionalLight = true;
			}
		}
	}
	
	private void searchNearestSpotLight(Light light, int lightIndex, long lightDistance) {
		if(spotLightDistance == Integer.MIN_VALUE)
			return;
		if(lightDistance < spotLightDistance) {
			spotLightDistance = lightDistance;
			spotLightIndex = lightIndex;
			shadowSpotLight = light;
			if(light.isMain()) {
				spotLightDistance = Integer.MIN_VALUE;
			}
		}
	}
	
	private void searchNearestPointLight(Light light, int lightIndex, long lightDistance) {
		if(pointLightDistance == Integer.MIN_VALUE)
			return;
		if(lightDistance < pointLightDistance) {
			pointLightDistance = lightDistance;
			pointLightIndex = lightIndex;
			shadowPointLight = light;
			if(light.isMain()) {
				pointLightDistance = Integer.MIN_VALUE;
			}
		}
	}
	
	private void initializeLightMatrices() {
		if(camera.isMain()) {
			initializeDirectionalLightMatrix();
			initializeSpotLightMatrix();
			initializePointLightMatrix();
		}
	}
	
	private void initializeDirectionalLightMatrix() {
		if (directionalLightIndex != -1) {
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
		if (spotLightIndex != -1) {
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
	
	private void initializePointLightMatrix() {
		if (pointLightIndex != -1) {
			final int portWidth = pointShadowMaps[0].getWidth();
			final int portHeight = pointShadowMaps[0].getHeight();
			pointLightFrustum.setRenderTargetSize(portWidth, portHeight);
			for (int i = 0; i < pointShadowMaps.length; i++) {
				pointShadowMaps[i].fill(Integer.MAX_VALUE);
			}		
			final Transform transform = shadowPointLight.getTransform();
			final int[][] lightSpaceMatrix = transform.getSpaceEnterMatrix();
			final int[][] frustumProjectionMatrix = pointLightFrustum.getProjectionMatrix();
			final int fixedPoint90 = 90 << FP_BIT;	
			transform.setRotation(0, 0, 0);
			MatrixUtils.multiply(frustumProjectionMatrix, lightSpaceMatrix, projectionMatrix);
			MatrixUtils.multiply(projectionMatrix, lightSpaceMatrix, pointLightMatrices[0]);
			
			transform.rotateWorld(0, fixedPoint90, 0);
			MatrixUtils.multiply(frustumProjectionMatrix, lightSpaceMatrix, projectionMatrix);
			MatrixUtils.multiply(projectionMatrix, lightSpaceMatrix, pointLightMatrices[1]);
			
			transform.rotateWorld(0, fixedPoint90, 0);
			MatrixUtils.multiply(frustumProjectionMatrix, lightSpaceMatrix, projectionMatrix);
			MatrixUtils.multiply(projectionMatrix, lightSpaceMatrix, pointLightMatrices[2]);
			
			transform.rotateWorld(0, fixedPoint90, 0);
			MatrixUtils.multiply(frustumProjectionMatrix, lightSpaceMatrix, projectionMatrix);
			MatrixUtils.multiply(projectionMatrix, lightSpaceMatrix, pointLightMatrices[3]);
			
			transform.setRotation(0, 0, fixedPoint90);
			MatrixUtils.multiply(frustumProjectionMatrix, lightSpaceMatrix, projectionMatrix);
			MatrixUtils.multiply(projectionMatrix, lightSpaceMatrix, pointLightMatrices[4]);
			
			transform.setRotation(0, 0, -fixedPoint90);
			MatrixUtils.multiply(frustumProjectionMatrix, lightSpaceMatrix, projectionMatrix);
			MatrixUtils.multiply(projectionMatrix, lightSpaceMatrix, pointLightMatrices[5]);
			transform.setRotation(0, 0, 0);
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