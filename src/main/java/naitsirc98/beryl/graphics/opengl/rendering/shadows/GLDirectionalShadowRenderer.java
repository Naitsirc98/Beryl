package naitsirc98.beryl.graphics.opengl.rendering.shadows;

import naitsirc98.beryl.core.BerylFiles;
import naitsirc98.beryl.graphics.opengl.GLContext;
import naitsirc98.beryl.graphics.opengl.rendering.renderers.GLMeshRenderer;
import naitsirc98.beryl.graphics.opengl.rendering.GLShadingPipeline;
import naitsirc98.beryl.graphics.opengl.rendering.renderers.GLRenderer;
import naitsirc98.beryl.graphics.opengl.shaders.GLShader;
import naitsirc98.beryl.graphics.opengl.shaders.GLShaderProgram;
import naitsirc98.beryl.graphics.opengl.textures.GLTexture2D;
import naitsirc98.beryl.graphics.rendering.shadows.ShadowCascade;
import naitsirc98.beryl.lights.DirectionalLight;
import naitsirc98.beryl.scenes.Camera;
import naitsirc98.beryl.scenes.Scene;

import java.util.Arrays;

import static java.lang.Math.min;
import static java.lang.Math.pow;
import static java.util.Objects.requireNonNull;
import static naitsirc98.beryl.graphics.ShaderStage.FRAGMENT_STAGE;
import static naitsirc98.beryl.graphics.ShaderStage.VERTEX_STAGE;
import static naitsirc98.beryl.graphics.opengl.rendering.shadows.GLShadowsInfo.MAX_SHADOW_CASCADES_COUNT;
import static naitsirc98.beryl.util.Maths.lerp;

public class GLDirectionalShadowRenderer {

    private final GLContext context;
    private final GLShadingPipeline shadingPipeline;
    private final GLShadowCascadeRenderer[] shadowCascadeRenderers;
    private GLTexture2D[] directionalDepthTextures;
    private ShadowCascade[] shadowCascades;

    public GLDirectionalShadowRenderer(GLContext context) {

        this.context = requireNonNull(context);

        shadingPipeline = createShadingPipeline();
        shadowCascadeRenderers = createShadowCascadeRenderers();
        directionalDepthTextures = getDirectionalDepthTextures();
        shadowCascades = getShadowCascades();
    }

    private ShadowCascade[] getShadowCascades() {

        ShadowCascade[] shadowCascades = new ShadowCascade[MAX_SHADOW_CASCADES_COUNT];

        for(int i = 0;i < MAX_SHADOW_CASCADES_COUNT;i++) {
            shadowCascades[i] = shadowCascadeRenderers[i].shadowCascade();
        }

        return shadowCascades;
    }

    public GLTexture2D[] dirShadowMaps() {
        return directionalDepthTextures;
    }

    public ShadowCascade[] shadowCascades() {
        return shadowCascades;
    }

    public void bakeDirectionalShadows(Scene scene, GLMeshRenderer meshRenderer) {

        shadingPipeline.setShadingModel(scene.renderInfo().shadingModel());

        DirectionalLight light = scene.environment().lighting().directionalLight();

        Camera camera = scene.camera();

        if(light == null) {
            return;
        }

        final float[] cascadeRanges = calculateCascadeRanges(scene, camera);

        for(int i = 0;i < MAX_SHADOW_CASCADES_COUNT;i++) {

            GLShadowCascadeRenderer shadowCascadeRenderer = shadowCascadeRenderers[i];

            shadowCascadeRenderer.render(scene, meshRenderer, light, cascadeRanges[i], cascadeRanges[i + 1]);
        }
    }

    private float[] calculateCascadeRanges(Scene scene, Camera camera) {

        final float maxDistance = scene.environment().lighting().shadowsMaxDistance();
        final float nearPlane = camera.nearPlane();
        final float farPlane = min(camera.farPlane(), maxDistance);

        float[] ranges = new float[MAX_SHADOW_CASCADES_COUNT + 1];

        float splitFactor = 0.75f;

        for(int i = 1; i < MAX_SHADOW_CASCADES_COUNT; i++) {

            float inv = (float) i / (float) MAX_SHADOW_CASCADES_COUNT;

            float a = nearPlane + (inv * (farPlane - nearPlane));

            float b = (float) (nearPlane * pow(farPlane / nearPlane, inv));

            float zFar = lerp(a, b, splitFactor);

            ranges[i] = zFar;
        }

        ranges[0] = nearPlane;

        ranges[MAX_SHADOW_CASCADES_COUNT] = farPlane;

        return ranges;
    }

    private GLShadowCascadeRenderer[] createShadowCascadeRenderers() {

        GLShadowCascadeRenderer[] shadowCascadeRenderers = new GLShadowCascadeRenderer[MAX_SHADOW_CASCADES_COUNT];

        for(int i = 0;i < MAX_SHADOW_CASCADES_COUNT;i++) {
            shadowCascadeRenderers[i] = new GLShadowCascadeRenderer(context, shadingPipeline);
        }

        return shadowCascadeRenderers;
    }

    private GLTexture2D[] getDirectionalDepthTextures() {

        GLTexture2D[] textures = new GLTexture2D[shadowCascadeRenderers.length];

        for(int i = 0;i < textures.length;i++) {
            textures[i] = shadowCascadeRenderers[i].depthTexture();
        }

        return textures;
    }

    public void terminate() {
        Arrays.stream(shadowCascadeRenderers).forEach(GLShadowCascadeRenderer::terminate);
    }

    private GLShadingPipeline createShadingPipeline() {
        return new GLShadingPipeline(createShader());
    }

    private GLShaderProgram createShader() {
        return new GLShaderProgram(context, "OpenGL dir shadows shader")
                .attach(new GLShader(context, VERTEX_STAGE).source(BerylFiles.getPath("shaders/depth/directional_depth.vert")))
                .attach(new GLShader(context, FRAGMENT_STAGE).source(BerylFiles.getPath("shaders/depth/depth.frag")))
                .link();
    }
}
