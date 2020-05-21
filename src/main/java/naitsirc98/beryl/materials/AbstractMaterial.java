package naitsirc98.beryl.materials;

import naitsirc98.beryl.graphics.GraphicsFactory;
import naitsirc98.beryl.graphics.textures.Texture2D;
import naitsirc98.beryl.util.BitFlags;
import org.joml.Vector2f;
import org.joml.Vector2fc;

public abstract class AbstractMaterial implements Material {

    static final Texture2D BLANK_TEXTURE = GraphicsFactory.get().blankTexture2D();

    private static final Vector2fc DEFAULT_TILING = new Vector2f(1.0f, 1.0f);


    private final String name;
    private int handle = Integer.MIN_VALUE;
    private final Vector2f tiling;
    private final BitFlags flags;
    private transient boolean destroyed;
    protected transient MaterialManager materialManager;

    public AbstractMaterial(String name) {
        this.name = name;
        flags = new BitFlags();
        tiling = new Vector2f(DEFAULT_TILING);
    }

    @Override
    public Vector2fc tiling() {
        return tiling;
    }

    @Override
    public Material tiling(float x, float y) {
        tiling.set(x, y);
        return this;
    }

    @Override
    public int flags() {
        return flags.get();
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
        if(!destroyed) {
            destroyed = true;
            materialManager.markDestroyed(this);
        }
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

    void setHandle(int handle) {
        this.handle = handle;
    }

    void setMaterialManager(MaterialManager materialManager) {
        this.materialManager = materialManager;
    }
}
