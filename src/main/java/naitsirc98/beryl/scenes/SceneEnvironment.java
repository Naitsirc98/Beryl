package naitsirc98.beryl.scenes;

import naitsirc98.beryl.lights.DirectionalLight;
import naitsirc98.beryl.lights.PointLight;
import naitsirc98.beryl.lights.SpotLight;
import naitsirc98.beryl.util.Color;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;

import static naitsirc98.beryl.util.types.TypeUtils.getOrElse;

public final class SceneEnvironment {

    public static final int MAX_POINT_LIGHTS = 10;
    public static final int MAX_SPOT_LIGHTS = 10;

    public static final Color DEFAULT_AMBIENT_COLOR = new Color(0.2f, 0.2f, 0.2f, 1.0f);

    private DirectionalLight directionalLight;
    private PointLight[] pointLights;
    private SpotLight[] spotLights;

    private Color ambientColor;

    SceneEnvironment() {
        ambientColor = DEFAULT_AMBIENT_COLOR;
        pointLights = new PointLight[MAX_POINT_LIGHTS];
        spotLights = new SpotLight[MAX_SPOT_LIGHTS];
    }

    public DirectionalLight directionalLight() {
        return directionalLight;
    }

    public void directionalLight(DirectionalLight directionalLight) {
        this.directionalLight = directionalLight;
    }

    public PointLight pointLight(int index) {
        return pointLights[index];
    }

    public Stream<PointLight> pointLights() {
        return Arrays.stream(pointLights).filter(Objects::nonNull);
    }

    public int pointLightsCount() {
        return (int) pointLights().count();
    }

    public void pointLight(int index, PointLight pointLight) {
        pointLights[index] = pointLight;
    }

    public SpotLight spotLight(int index) {
        return spotLights[index];
    }

    public Stream<SpotLight> spotLights() {
        return Arrays.stream(spotLights).filter(Objects::nonNull);
    }

    public int spotLightsCount() {
        return (int) spotLights().count();
    }

    public void spotLight(int index, SpotLight spotLight) {
        spotLights[index] = spotLight;
    }

    public Color ambientColor() {
        return ambientColor;
    }

    public void ambientColor(Color ambientColor) {
        this.ambientColor = getOrElse(ambientColor, DEFAULT_AMBIENT_COLOR);
    }
}
