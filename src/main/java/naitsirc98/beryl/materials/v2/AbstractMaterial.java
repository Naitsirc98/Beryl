package naitsirc98.beryl.materials.v2;

import naitsirc98.beryl.graphics.GraphicsFactory;
import naitsirc98.beryl.graphics.textures.Texture2D;
import naitsirc98.beryl.util.BitFlags;
import org.joml.Vector2f;
import org.joml.Vector2fc;

public abstract class AbstractMaterial implements Material {

    static final Texture2D BLANK_TEXTURE = GraphicsFactory.get().blankTexture2D();

    private static final Vector2f DEFAULT_TILING = new Vector2f(1.0f, 1.0f);


    private final String name;
    private final int handle;
    private Vector2f tiling;
    private final MaterialStorageInfo storageInfo;
    private final BitFlags flags;
    private transient boolean destroyed;
    private transient boolean modified;

    public AbstractMaterial(String name, int handle) {
        this.name = name;
        this.handle = handle;
        flags = new BitFlags();
        storageInfo = new MaterialStorageInfo(sizeof());
    }

    @Override
    public Vector2fc tiling() {
        return tiling == null ? DEFAULT_TILING : tiling;
    }

    @Override
    public Material tiling(float x, float y) {

        if(tiling == null) {
            tiling = new Vector2f(x, y);
        } else {
            tiling.set(x, y);
        }

        markModified();

        return this;
    }

    @Override
    public MaterialStorageInfo storageInfo() {
        return storageInfo;
    }

    @Override
    public int flags() {
        return flags.get();
    }

    @Override
    public boolean modified() {
        return modified;
    }

    @Override
    public boolean destroyed() {
        return destroyed;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public int handle() {
        return handle;
    }

    protected void destroy() {
        destroyed = true;
    }

    protected void markModified() {
        modified = true;
    }

    protected void setFlag(int flag) {
        flags.enable(flag);
    }

    protected void removeFlag(int flag) {
        flags.disable(flag);
    }

    protected void updateTexturesUseCount(Texture2D oldTexture, Texture2D newTexture) {

        if(oldTexture != null) {
            oldTexture.decrementUseCount();
        }

        if(newTexture != null) {
            newTexture.incrementUseCount();
        }
    }

    protected Texture2D getMapOrDefault(Texture2D map) {
        return map == null ? BLANK_TEXTURE : map;
    }
}
