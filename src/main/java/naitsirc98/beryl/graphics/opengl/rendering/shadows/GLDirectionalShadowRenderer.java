package naitsirc98.beryl.graphics.opengl.rendering.shadows;

import naitsirc98.beryl.core.BerylFiles;
import naitsirc98.beryl.graphics.opengl.shaders.GLShader;
import naitsirc98.beryl.graphics.opengl.shaders.GLShaderProgram;
import naitsirc98.beryl.graphics.opengl.textures.GLTexture2D;
import naitsirc98.beryl.graphics.rendering.renderers.ShadowCascade;
import naitsirc98.beryl.lights.DirectionalLight;
import naitsirc98.beryl.scenes.Camera;
import naitsirc98.beryl.scenes.Scene;

import java.util.Arrays;

import static naitsirc98.beryl.graphics.ShaderStage.FRAGMENT_STAGE;
import static naitsirc98.beryl.graphics.ShaderStage.VERTEX_STAGE;
import static naitsirc98.beryl.graphics.opengl.rendering.shadows.GLShadowsInfo.MAX_SHADOW_CASCADES_COUNT;

public class GLDirectionalShadowRenderer {

    private final GLShaderProgram shader;
    private final GLShadowCascadeRenderer[] shadowCascadeRenderers;
    // TODO
    private GLTexture2D[] directionalDepthTextures;
    private ShadowCascade[] shadowCascades;

    public GLDirectionalShadowRenderer() {
        shader = createShader();
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

    private GLShaderProgram createShader() {
        return new GLShaderProgram()
                .attach(new GLShader(VERTEX_STAGE).source(BerylFiles.getPath("shaders/depth/directional_depth.vert")))
                .attach(new GLShader(FRAGMENT_STAGE).source(BerylFiles.getPath("shaders/depth/depth.frag")))
                .link();
    }

    public GLTexture2D[] dirShadowMaps() {
        return directionalDepthTextures;
    }

    public ShadowCascade[] shadowCascades() {
        return shadowCascades;
    }

    public void bakeDirectionalShadows(Scene scene) {

        DirectionalLight light = scene.environment().lighting().directionalLight();

        Camera camera = scene.camera();

        if(light == null) {
            return;
        }

        final float[] cascadeRanges = {
                camera.nearPlane(),
                camera.farPlane() / 20.0f,
                camera.farPlane() / 10.0f,
                camera.farPlane()
        };

        for(int i = 0;i < MAX_SHADOW_CASCADES_COUNT;i++) {

            GLShadowCascadeRenderer shadowCascadeRenderer = shadowCascadeRenderers[i];

            shadowCascadeRenderer.render(scene, light, camera.nearPlane(), cascadeRanges[i+1]);
        }
    }

    private GLShadowCascadeRenderer[] createShadowCascadeRenderers() {

        GLShadowCascadeRenderer[] shadowCascadeRenderers = new GLShadowCascadeRenderer[MAX_SHADOW_CASCADES_COUNT];

        for(int i = 0;i < MAX_SHADOW_CASCADES_COUNT;i++) {
            shadowCascadeRenderers[i] = new GLShadowCascadeRenderer(shader);
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
}
