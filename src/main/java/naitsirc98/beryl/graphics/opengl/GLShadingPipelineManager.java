package naitsirc98.beryl.graphics.opengl;

import naitsirc98.beryl.core.BerylFiles;
import naitsirc98.beryl.graphics.opengl.rendering.GLShadingPipeline;
import naitsirc98.beryl.graphics.opengl.shaders.GLShader;
import naitsirc98.beryl.graphics.opengl.shaders.GLShaderProgram;
import naitsirc98.beryl.graphics.rendering.ShadingModel;
import naitsirc98.beryl.resources.Resource;
import naitsirc98.beryl.scenes.SceneRenderInfo;

import java.util.EnumMap;
import java.util.Map;

import static naitsirc98.beryl.graphics.ShaderStage.FRAGMENT_STAGE;
import static naitsirc98.beryl.graphics.ShaderStage.VERTEX_STAGE;

public class GLShadingPipelineManager implements Resource {

    private final Map<ShadingModel, GLShadingPipeline> shadingPipelines;

    public GLShadingPipelineManager() {
        shadingPipelines = new EnumMap<>(ShadingModel.class);
    }

    public GLShadingPipeline getShadingPipeline(SceneRenderInfo renderInfo) {
        GLShadingPipeline shadingPipeline = shadingPipelines.get(renderInfo.getShadingModel());
        shadingPipeline.setShadowsEnabled(renderInfo.areShadowsEnabled());
        return shadingPipeline;
    }

    @Override
    public void release() {
        shadingPipelines.values().forEach(GLShadingPipeline::release);
        shadingPipelines.clear();
    }

    public void init() {
        shadingPipelines.put(ShadingModel.PHONG, createPhongShadingPipeline());
        shadingPipelines.put(ShadingModel.PBR_METALLIC, createPBRMetallicShadingPipeline());
    }

    private GLShadingPipeline createPhongShadingPipeline() {
        return new GLShadingPipeline(new GLShaderProgram("OpenGL Phong shader")
                .attach(new GLShader(VERTEX_STAGE).source(BerylFiles.getPath("shaders/phong/phong.vert")))
                .attach(new GLShader(FRAGMENT_STAGE).source(BerylFiles.getPath("shaders/phong/phong.frag")))
                .link(), ShadingModel.PHONG);
    }

    private GLShadingPipeline createPBRMetallicShadingPipeline() {
        return new GLShadingPipeline(new GLShaderProgram("OpenGL PBR Metallic shader")
                .attach(new GLShader(VERTEX_STAGE).source(BerylFiles.getPath("shaders/pbr/metallic/pbr_metallic.vert")))
                .attach(new GLShader(FRAGMENT_STAGE).source(BerylFiles.getPath("shaders/pbr/metallic/pbr_metallic.frag")))
                .link(), ShadingModel.PBR_METALLIC);
    }

}
