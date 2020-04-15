package naitsirc98.beryl.materials;

import naitsirc98.beryl.graphics.GraphicsFactory;
import naitsirc98.beryl.graphics.textures.Texture2D;

import java.util.HashMap;
import java.util.Map;

public interface WaterMaterial extends IMaterial {

    static WaterMaterial get(String name) {

        MaterialManager materialManager = MaterialManager.get();

        if(materialManager.exists(name)) {
            return materialManager.get(name);
        }

        GraphicsFactory graphicsFactory = GraphicsFactory.get();

        Map<Byte, Object> properties = new HashMap<>(2);

        properties.put(REFLECTION_MAP, graphicsFactory.newTexture2D());
        properties.put(REFRACTION_MAP, graphicsFactory.newTexture2D());

        return materialManager.create(name, Type.WATER_MATERIAL, properties);
    }

    Texture2D reflectionMap();
    Texture2D refractionMap();
}
