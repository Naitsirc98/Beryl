package naitsirc98.beryl.graphics.textures;

import naitsirc98.beryl.graphics.textures.Texture.Filter;
import naitsirc98.beryl.graphics.textures.Texture.WrapMode;

import static naitsirc98.beryl.graphics.textures.Texture.Filter.NEAREST;
import static naitsirc98.beryl.graphics.textures.Texture.WrapMode.REPEAT;

public final class SamplerInfo {

    private WrapMode wrapModeS = REPEAT;
    private WrapMode wrapModeT = REPEAT;
    private WrapMode wrapModeR = REPEAT;
    private Filter minFilter = NEAREST;
    private Filter magFilter = NEAREST;

    public SamplerInfo() {
    }

    public SamplerInfo(WrapMode wrapModeS, WrapMode wrapModeT, WrapMode wrapModeR, Filter minFilter, Filter magFilter) {
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

    public SamplerInfo filter(Filter filter) {
        minFilter = magFilter = filter;
        return this;
    }

    public Filter minFilter() {
        return minFilter;
    }

    public SamplerInfo minFilter(Filter minFilter) {
        this.minFilter = minFilter;
        return this;
    }

    public Filter magFilter() {
        return magFilter;
    }

    public SamplerInfo magFilter(Filter magFilter) {
        this.magFilter = magFilter;
        return this;
    }
}
