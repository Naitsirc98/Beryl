package naitsirc98.beryl.scenes.environment.skybox;

import naitsirc98.beryl.graphics.textures.Sampler;
import naitsirc98.beryl.graphics.textures.Texture;

public class SkyboxHelper {

    public static <T extends Texture> T setSkyboxTextureSamplerParameters(T texture) {

        if(texture == null) {
            return null;
        }

        texture.sampler()
                .wrapMode(Sampler.WrapMode.CLAMP_TO_EDGE)
                .magFilter(Sampler.MagFilter.LINEAR)
                .minFilter(Sampler.MinFilter.LINEAR_MIPMAP_LINEAR);

        return texture;
    }

}
