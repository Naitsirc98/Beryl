package naitsirc98.beryl.scenes;

import naitsirc98.beryl.lights.DirectionalLight;
import naitsirc98.beryl.lights.PointLight;
import naitsirc98.beryl.lights.SpotLight;

import java.util.ArrayList;
import java.util.List;

public final class SceneLighting {

    public static final int MAX_POINT_LIGHTS = 10;
    public static final int MAX_SPOT_LIGHTS = 10;


    private DirectionalLight directionalLight;
    private final List<PointLight> pointLights;
    private final List<SpotLight> spotLights;

    SceneLighting() {
        pointLights = new ArrayList<>();
        spotLights = new ArrayList<>();
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
}
