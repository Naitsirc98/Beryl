package naitsirc98.beryl.materials;

import naitsirc98.beryl.graphics.GraphicsFactory;
import naitsirc98.beryl.graphics.textures.Texture2D;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public interface WaterMaterial extends IMaterial {

    static WaterMaterial get(String name, Consumer<Builder> builderConsumer) {

        MaterialManager materialManager = MaterialManager.get();

        if(materialManager.exists(name)) {
            return materialManager.get(name);
        }

        Builder builder = new Builder();
        builderConsumer.accept(builder);

        GraphicsFactory graphicsFactory = GraphicsFactory.get();

        Map<Byte, Object> properties = new HashMap<>(5);

        properties.put(REFLECTION_MAP, graphicsFactory.newTexture2D());
        properties.put(REFRACTION_MAP, graphicsFactory.newTexture2D());
        properties.put(DUDV_MAP, builder.dudvMap);
        properties.put(NORMAL_MAP, builder.normalMap);

        return materialManager.create(name, Type.WATER_MATERIAL, properties);
    }

    Texture2D reflectionMap();
    Texture2D refractionMap();
    Texture2D dudvMap();
    Texture2D normalMap();


    final class Builder {

        private Texture2D dudvMap;
        private Texture2D normalMap;

        public Builder() {
        }

        public Builder dudvMap(Texture2D dudvMap) {
            this.dudvMap = dudvMap;
            return this;
        }

        public Builder normalMap(Texture2D normalMap) {
            this.normalMap = normalMap;
            return this;
        }
    }
}
