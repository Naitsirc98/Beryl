package naitsirc98.beryl.scenes.environment.skybox;

import naitsirc98.beryl.graphics.GraphicsFactory;
import naitsirc98.beryl.graphics.textures.Cubemap;
import naitsirc98.beryl.images.Image;
import naitsirc98.beryl.images.ImageFactory;
import naitsirc98.beryl.images.PixelFormat;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;

import static java.util.stream.Collectors.toUnmodifiableMap;

public class SimpleSkyboxTextureLoader extends AbstractSkyboxTextureLoader {

    private static final Map<Cubemap.Face, String> DEFAULT_CUBEMAP_FACE_NAMES = Arrays.stream(Cubemap.Face.values())
            .collect(toUnmodifiableMap(Function.identity(), face -> face.name().toLowerCase()));


    private Map<Cubemap.Face, String> cubemapFaceNames;

    public SimpleSkyboxTextureLoader() {
        cubemapFaceNames = new EnumMap<>(Cubemap.Face.class);
        cubemapFaceNames.putAll(DEFAULT_CUBEMAP_FACE_NAMES);
    }

    @Override
    public Cubemap loadSkyboxTexture(String skyboxFolder) {

        Cubemap cubemap = GraphicsFactory.get().newCubemap();

        return setupCubemapFaces(cubemap, Paths.get(skyboxFolder), pixelFormat());
    }

    public Map<Cubemap.Face, String> cubemapFaceNames() {
        return cubemapFaceNames;
    }

    private Cubemap setupCubemapFaces(Cubemap cubemap, Path folder, PixelFormat pixelFormat) {

        boolean notAllocated = true;

        for(Cubemap.Face face : Cubemap.Face.values()) {

            if(!cubemapFaceNames.containsKey(face)) {
                throw new RuntimeException("No name provided for cubemap face " + face);
            }

            final String faceName = getImagePath(cubemapFaceNames.get(face));

            try(Image image = ImageFactory.newImage(folder.resolve(faceName).toString(), pixelFormat)) {

                final int width = image.width();
                final int height = image.height();

                if(notAllocated) {
                    cubemap.allocate(1, width, height, pixelFormat);
                    notAllocated = false;
                }

                cubemap.update(face, 0, 0, 0, width, height, pixelFormat, image.pixels());
            }
        }

        cubemap.generateMipmaps();

        return cubemap;
    }
}
