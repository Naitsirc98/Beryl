package naitsirc98.beryl.scenes;

import naitsirc98.beryl.lights.DirectionalLight;
import naitsirc98.beryl.lights.PointLight;
import naitsirc98.beryl.lights.SpotLight;
import naitsirc98.beryl.util.Color;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static naitsirc98.beryl.util.types.TypeUtils.getOrElse;

public final class SceneEnvironment {

    private static final int LIGHTS_BUFFER_SIZE = 1712;

    public static final int MAX_POINT_LIGHTS = 10;
    public static final int MAX_SPOT_LIGHTS = 10;

    public static final Color DEFAULT_AMBIENT_COLOR = new Color(0.2f, 0.2f, 0.2f);

    public static final Color DEFAULT_CLEAR_COLOR = new Color(0.8f, 0.8f, 0.8f);

    private DirectionalLight directionalLight;
    private final PointLight[] pointLights;
    private final SpotLight[] spotLights;

    private final Fog fog;

    private Color ambientColor;

    private Color clearColor;


    SceneEnvironment() {
        ambientColor = DEFAULT_AMBIENT_COLOR;
        pointLights = new PointLight[MAX_POINT_LIGHTS];
        spotLights = new SpotLight[MAX_SPOT_LIGHTS];
        clearColor = DEFAULT_CLEAR_COLOR;
        fog = new Fog();
        fog.color(clearColor);
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

    public Fog fog() {
        return fog;
    }

    public Color clearColor() {
        return clearColor;
    }

    public SceneEnvironment clearColor(Color clearColor) {
        this.clearColor = requireNonNull(clearColor);
        fog.color(clearColor);
        return this;
    }
}
