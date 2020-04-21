package naitsirc98.beryl.materials;

import naitsirc98.beryl.graphics.GraphicsFactory;
import naitsirc98.beryl.graphics.textures.Texture2D;
import naitsirc98.beryl.util.BitFlags;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public interface WaterMaterial extends IMaterial {

    static boolean exists(String name) {
        return MaterialManager.get().exists(name);
    }

    static WaterMaterial get(String name) {
        if(exists(name)) {
            return MaterialManager.get().get(name);
        }
        return MaterialManager.get().create(name, Type.WATER_MATERIAL, getDefaultProperties(), getDefaultFlags());
    }

    static WaterMaterial get(String name, Consumer<WaterMaterial> materialConfigurator) {
        if(exists(name)) {
            return get(name);
        }
        WaterMaterial material = get(name);
        materialConfigurator.accept(material);
        return material;
    }


    Texture2D reflectionMap();
    WaterMaterial reflectionMap(Texture2D reflectionMap);

    Texture2D refractionMap();
    WaterMaterial refractionMap(Texture2D refractionMap);

    Texture2D dudvMap();
    WaterMaterial dudvMap(Texture2D dudvMap);

    Texture2D normalMap();
    WaterMaterial normalMap(Texture2D normalMap);


    private static Map<Byte, Object> getDefaultProperties() {

        GraphicsFactory graphicsFactory = GraphicsFactory.get();

        Map<Byte, Object> properties = new HashMap<>(5);

        properties.put(REFLECTION_MAP, graphicsFactory.newTexture2D());
        properties.put(REFRACTION_MAP, graphicsFactory.newTexture2D());
        properties.put(DUDV_MAP, null);
        properties.put(NORMAL_MAP, null);

        return properties;
    }

    private static BitFlags getDefaultFlags() {
        return new BitFlags();
    }
}
