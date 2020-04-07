package naitsirc98.beryl.graphics;

import naitsirc98.beryl.util.handles.IntHandle;

import static naitsirc98.beryl.graphics.GraphicsAPI.chooseByAPI;
import static org.lwjgl.opengl.GL45.*;
import static org.lwjgl.vulkan.VK10.*;

public enum ShaderStage implements IntHandle {

    VERTEX_STAGE(chooseByAPI(VK_SHADER_STAGE_VERTEX_BIT, GL_VERTEX_SHADER)),
    TESSELATION_CONTROL_STAGE(chooseByAPI(VK_SHADER_STAGE_TESSELLATION_CONTROL_BIT, GL_TESS_CONTROL_SHADER)),
    TESSELATION_EVALUATION_STAGE(chooseByAPI(VK_SHADER_STAGE_TESSELLATION_EVALUATION_BIT, GL_TESS_EVALUATION_SHADER)),
    GEOMETRY_STAGE(chooseByAPI(VK_SHADER_STAGE_GEOMETRY_BIT, GL_GEOMETRY_SHADER)),
    FRAGMENT_STAGE(chooseByAPI(VK_SHADER_STAGE_FRAGMENT_BIT, GL_FRAGMENT_SHADER)),
    COMPUTE_STAGE(chooseByAPI(VK_PIPELINE_STAGE_COMPUTE_SHADER_BIT, GL_COMPUTE_SHADER));

    private final int handle;

    ShaderStage(int handle) {
        this.handle = handle;
    }

    @Override
    public int handle() {
        return handle;
    }
}
