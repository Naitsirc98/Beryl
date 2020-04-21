package naitsirc98.beryl.scenes;

import naitsirc98.beryl.graphics.GraphicsFactory;
import naitsirc98.beryl.graphics.buffers.UniformBuffer;
import naitsirc98.beryl.lights.DirectionalLight;
import naitsirc98.beryl.lights.Light;
import naitsirc98.beryl.lights.PointLight;
import naitsirc98.beryl.lights.SpotLight;
import naitsirc98.beryl.resources.Resource;
import naitsirc98.beryl.util.Color;
import naitsirc98.beryl.util.IColor;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.util.List;

import static java.lang.Math.min;
import static java.util.Objects.requireNonNull;
import static naitsirc98.beryl.scenes.SceneLighting.MAX_POINT_LIGHTS;
import static naitsirc98.beryl.scenes.SceneLighting.MAX_SPOT_LIGHTS;
import static naitsirc98.beryl.util.Maths.roundUp2;
import static naitsirc98.beryl.util.types.DataType.*;
import static naitsirc98.beryl.util.types.TypeUtils.getOrElse;
import static org.lwjgl.system.MemoryStack.stackPush;

public final class SceneEnvironment implements Resource {

    public static final int DIRECTIONAL_LIGHT_OFFSET = 0;
    public static final int POINT_LIGHTS_OFFSET = roundUp2(Light.SIZEOF, VECTOR4_SIZEOF);
    public static final int SPOT_LIGHTS_OFFSET = roundUp2(POINT_LIGHTS_OFFSET + Light.SIZEOF * MAX_POINT_LIGHTS, VECTOR4_SIZEOF);
    public static final int AMBIENT_COLOR_OFFSET = roundUp2(SPOT_LIGHTS_OFFSET + Light.SIZEOF * MAX_SPOT_LIGHTS, VECTOR4_SIZEOF);
    public static final int FOG_OFFSET = roundUp2(AMBIENT_COLOR_OFFSET + Color.SIZEOF, VECTOR4_SIZEOF);
    public static final int POINT_LIGHTS_COUNT_OFFSET = 1728;//roundUp2(FOG_OFFSET + Fog.SIZEOF, INT32_SIZEOF);
    public static final int SPOT_LIGHTS_COUNT_OFFSET = 1744;//roundUp2(POINT_LIGHTS_COUNT_OFFSET + INT32_SIZEOF, INT32_SIZEOF);
    public static final int LIGHTS_BUFFER_SIZE = roundUp2(SPOT_LIGHTS_COUNT_OFFSET + INT32_SIZEOF, VECTOR4_SIZEOF);


    public static final Color DEFAULT_AMBIENT_COLOR = new Color(0.2f, 0.2f, 0.2f);

    public static final Color DEFAULT_CLEAR_COLOR = new Color(0.8f, 0.8f, 0.8f);

    private final SceneLighting lights;
    private final Fog fog;
    private final Color ambientColor;
    private final Color clearColor;
    private UniformBuffer lightsBuffer;
    private Skybox skybox;

    SceneEnvironment() {
        lights = new SceneLighting();
        ambientColor = DEFAULT_AMBIENT_COLOR;
        clearColor = DEFAULT_CLEAR_COLOR;
        fog = new Fog();
        fog.color(clearColor);
        lightsBuffer = GraphicsFactory.get().newUniformBuffer();
        lightsBuffer.allocate(LIGHTS_BUFFER_SIZE);
        lightsBuffer.mapMemory();
    }

    public SceneLighting lights() {
        return lights;
    }

    public Color ambientColor() {
        return ambientColor;
    }

    public SceneEnvironment ambientColor(IColor ambientColor) {
        this.ambientColor.set(requireNonNull(ambientColor));
        return this;
    }

    public Fog fog() {
        return fog;
    }

    public Color clearColor() {
        return clearColor;
    }

    public SceneEnvironment clearColor(Color clearColor) {
        this.clearColor.set(requireNonNull(clearColor));
        fog.color(clearColor);
        return this;
    }

    public Skybox skybox() {
        return skybox;
    }

    public SceneEnvironment skybox(Skybox skybox) {
        this.skybox = skybox;
        return this;
    }

    void update() {

        final DirectionalLight directionalLight = lights.directionalLight();
        final List<PointLight> pointLights = lights.pointLights();
        final List<SpotLight> spotLights = lights.spotLights();

        final int pointLightsCount = min(pointLights.size(), MAX_POINT_LIGHTS);
        final int spotLightsCount = min(spotLights.size(), MAX_SPOT_LIGHTS);

        try (MemoryStack stack = stackPush()) {

            ByteBuffer buffer = stack.calloc(LIGHTS_BUFFER_SIZE);

            if (directionalLight != null) {
                directionalLight.get(DIRECTIONAL_LIGHT_OFFSET, buffer);
            }

            if (pointLightsCount > 0) {
                for (int i = 0; i < pointLightsCount; i++) {
                    pointLights.get(i).get(POINT_LIGHTS_OFFSET + i * Light.SIZEOF, buffer);
                }
            }

            if (spotLightsCount > 0) {
                for (int i = 0; i < spotLightsCount; i++) {
                    spotLights.get(i).get(SPOT_LIGHTS_OFFSET + i * Light.SIZEOF, buffer);
                }
            }

            ambientColor.getRGBA(AMBIENT_COLOR_OFFSET, buffer);
            fog.get(FOG_OFFSET, buffer);
            buffer.putInt(POINT_LIGHTS_COUNT_OFFSET, pointLightsCount);
            buffer.putInt(SPOT_LIGHTS_COUNT_OFFSET, spotLightsCount);

            lightsBuffer.copy(0, buffer);
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends UniformBuffer> T buffer() {
        return (T) lightsBuffer;
    }

    @Override
    public void release() {
        lightsBuffer.release();
    }
}
