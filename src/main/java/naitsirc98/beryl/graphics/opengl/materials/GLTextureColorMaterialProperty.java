package naitsirc98.beryl.graphics.opengl.materials;

import naitsirc98.beryl.graphics.opengl.textures.GLTexture2D;
import naitsirc98.beryl.util.Color;
import org.lwjgl.system.NativeResource;

import static naitsirc98.beryl.util.types.TypeUtils.getOrElse;

public final class GLTextureColorMaterialProperty implements NativeResource {

    private Color color = Color.NONE;
    private GLTexture2D texture;

    GLTextureColorMaterialProperty() {

    }

    public Color color() {
        return color;
    }

    public void color(Color color) {
        this.color = getOrElse(color, Color.NONE);
    }

    public GLTexture2D texture() {
        return texture;
    }

    public void texture(GLTexture2D texture) {
        this.texture = texture;
    }

    @Override
    public void free() {
        texture.free();
        texture = null;
        color = null;
    }
}
