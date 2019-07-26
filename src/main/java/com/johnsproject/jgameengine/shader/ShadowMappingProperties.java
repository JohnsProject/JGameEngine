package com.johnsproject.jgameengine.shader;

public class ShadowMappingProperties implements ShaderProperties {
	
	private boolean directionalShadows;
	private boolean spotShadows;
	private boolean pointShadows;

	public ShadowMappingProperties() {
		this.directionalShadows = true;
		this.spotShadows = true;
		this.pointShadows = true;
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
