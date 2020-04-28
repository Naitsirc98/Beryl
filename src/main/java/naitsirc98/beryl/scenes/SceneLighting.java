package naitsirc98.beryl.scenes;

import naitsirc98.beryl.lights.DirectionalLight;
import naitsirc98.beryl.lights.PointLight;
import naitsirc98.beryl.lights.SpotLight;

import java.util.ArrayList;
import java.util.List;

public final class SceneLighting {

    public static final int MAX_POINT_LIGHTS = 10;
    public static final int MAX_SPOT_LIGHTS = 10;

    private static final float DEFAULT_SHADOWS_MAX_DISTANCE = 1024;
    private static final int DEFAULT_SHADOW_MAP_SIZE = 2048;

    private DirectionalLight directionalLight;
    private final List<PointLight> pointLights;
    private final List<SpotLight> spotLights;
    private float shadowsMaxDistance;
    private int shadowMapSize;

    SceneLighting() {
        pointLights = new ArrayList<>();
        spotLights = new ArrayList<>();
        shadowsMaxDistance = DEFAULT_SHADOWS_MAX_DISTANCE;
        shadowMapSize = DEFAULT_SHADOW_MAP_SIZE;
    }

    public DirectionalLight directionalLight() {
        return directionalLight;
    }

    public SceneLighting directionalLight(DirectionalLight directionalLight) {
        this.directionalLight = directionalLight;
        return this;
    }

    public List<PointLight> pointLights() {
        return pointLights;
    }

    public List<SpotLight> spotLights() {
        return spotLights;
    }

    public float shadowsMaxDistance() {
        return shadowsMaxDistance;
    }

    public SceneLighting shadowsMaxDistance(float shadowsMaxDistance) {
        this.shadowsMaxDistance = shadowsMaxDistance;
        return this;
    }

    public int shadowMapSize() {
        return shadowMapSize;
    }

    public SceneLighting shadowMapSize(int shadowMapSize) {
        this.shadowMapSize = shadowMapSize;
        return this;
    }
}
