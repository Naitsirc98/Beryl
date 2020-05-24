package naitsirc98.beryl.graphics;

import naitsirc98.beryl.util.handles.IntHandle;

import static org.lwjgl.opengl.GL45.*;

public enum ShaderStage implements IntHandle {

    VERTEX_STAGE(GL_VERTEX_SHADER),
    TESSELATION_CONTROL_STAGE(GL_TESS_CONTROL_SHADER),
    TESSELATION_EVALUATION_STAGE(GL_TESS_EVALUATION_SHADER),
    GEOMETRY_STAGE(GL_GEOMETRY_SHADER),
    FRAGMENT_STAGE(GL_FRAGMENT_SHADER),
    COMPUTE_STAGE(GL_COMPUTE_SHADER);

    private final int handle;

    ShaderStage(int handle) {
        this.handle = handle;
    }

    @Override
    public int handle() {
        return handle;
    }
}
