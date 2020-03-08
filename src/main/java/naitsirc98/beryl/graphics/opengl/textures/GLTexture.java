package naitsirc98.beryl.graphics.opengl.textures;

import naitsirc98.beryl.graphics.opengl.GLObject;

import static org.lwjgl.opengl.GL11.glDeleteTextures;
import static org.lwjgl.opengl.GL45.glCreateTextures;

public abstract class GLTexture implements GLObject {

    private int handle;
    private final int target;

    public GLTexture(int target) {
        this.target = target;
        this.handle = glCreateTextures(target);
    }

    public final int target() {
        return target;
    }

    @Override
    public int handle() {
        return handle;
    }

    @Override
    public void free() {
        glDeleteTextures(handle);
        handle = NULL;
    }
}
