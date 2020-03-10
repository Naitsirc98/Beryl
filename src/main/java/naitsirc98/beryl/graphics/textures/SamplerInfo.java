package naitsirc98.beryl.graphics.textures;

import naitsirc98.beryl.graphics.textures.Texture.MagFilter;
import naitsirc98.beryl.graphics.textures.Texture.MinFilter;
import naitsirc98.beryl.graphics.textures.Texture.WrapMode;

import static naitsirc98.beryl.graphics.textures.Texture.MagFilter.NEAREST;
import static naitsirc98.beryl.graphics.textures.Texture.MinFilter.NEAREST_MIPMAP_NEAREST;
import static naitsirc98.beryl.graphics.textures.Texture.WrapMode.REPEAT;

public final class SamplerInfo {

    private WrapMode wrapModeS = REPEAT;
    private WrapMode wrapModeT = REPEAT;
    private WrapMode wrapModeR = REPEAT;
    private MinFilter minFilter = NEAREST_MIPMAP_NEAREST;
    private MagFilter magFilter = NEAREST;

    public SamplerInfo() {
    }

    public SamplerInfo(WrapMode wrapModeS, WrapMode wrapModeT, WrapMode wrapModeR,
                       MinFilter minFilter, MagFilter magFilter) {
        this.wrapModeS = wrapModeS;
        this.wrapModeT = wrapModeT;
        this.wrapModeR = wrapModeR;
        this.minFilter = minFilter;
        this.magFilter = magFilter;
    }

    public SamplerInfo wrapMode(WrapMode wrapMode) {
        wrapModeS = wrapModeT = wrapModeR = wrapMode;
        return this;
    }

    public WrapMode wrapModeS() {
        return wrapModeS;
    }

    public SamplerInfo wrapModeS(WrapMode wrapModeS) {
        this.wrapModeS = wrapModeS;
        return this;
    }

    public WrapMode wrapModeT() {
        return wrapModeT;
    }

    public SamplerInfo wrapModeT(WrapMode wrapModeT) {
        this.wrapModeT = wrapModeT;
        return this;
    }

    public WrapMode wrapModeR() {
        return wrapModeR;
    }

    public SamplerInfo wrapModeR(WrapMode wrapModeR) {
        this.wrapModeR = wrapModeR;
        return this;
    }

    public MinFilter minFilter() {
        return minFilter;
    }

    public SamplerInfo minFilter(MinFilter minFilter) {
        this.minFilter = minFilter;
        return this;
    }

    public MagFilter magFilter() {
        return magFilter;
    }

    public SamplerInfo magFilter(MagFilter magFilter) {
        this.magFilter = magFilter;
        return this;
    }
}
