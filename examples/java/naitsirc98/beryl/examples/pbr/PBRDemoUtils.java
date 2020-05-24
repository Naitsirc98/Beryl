package naitsirc98.beryl.examples.pbr;

import naitsirc98.beryl.graphics.GraphicsFactory;
import naitsirc98.beryl.graphics.textures.Sampler;
import naitsirc98.beryl.graphics.textures.Texture2D;
import naitsirc98.beryl.images.PixelFormat;
import naitsirc98.beryl.logging.Log;
import naitsirc98.beryl.materials.PBRMetallicMaterial;
import naitsirc98.beryl.util.Color;

import java.nio.file.Files;
import java.nio.file.Path;

public class PBRDemoUtils {

    public static PBRMetallicMaterial getPBRMetallicMaterialFromFolder(Path folder, String extension) {

        return PBRMetallicMaterial.getFactory().getMaterial(folder.toString(), material -> {

            material.setAlbedoMap(loadTexture(folder.resolve("albedo." + extension)));
            material.setNormalMap(loadTexture(folder.resolve("normal." + extension)));
            material.setMetallicMap(loadTexture(folder.resolve("metallic." + extension)));
            material.setRoughnessMap(loadTexture(folder.resolve("roughness." + extension)));
            material.setOcclusionMap(loadTexture(folder.resolve("ao." + extension)));
            material.setEmissiveMap(loadTexture(folder.resolve("emissive." + extension)));

            if(folder.toString().contains("mil")) {
                material.setEmissiveColor(Color.colorWhite().intensify(50));
            }
        });
    }

    public static Texture2D loadTexture(Path path) {

        if(!Files.exists(path)) {
            Log.warning("Failed to find texture " + path);
            return null;
        }

        Texture2D texture = GraphicsFactory.get().newTexture2D(path.toString(), PixelFormat.RGBA);

        texture.sampler()
                .wrapMode(Sampler.WrapMode.REPEAT)
                .minFilter(Sampler.MinFilter.LINEAR_MIPMAP_LINEAR)
                .magFilter(Sampler.MagFilter.LINEAR)
                .lodBias(-1);

        texture.generateMipmaps();

        return texture;
    }
}
