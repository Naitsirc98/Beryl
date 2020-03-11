package naitsirc98.beryl.meshes.materials;

import naitsirc98.beryl.graphics.textures.Texture2D;
import naitsirc98.beryl.util.Color;

public interface Material {

    ShadingModel shadingModel();

    class ColorMapProperty {

        private Color color;
        private Texture2D map;

        public ColorMapProperty() {

        }

        public Color color() {
            return color;
        }

        public ColorMapProperty color(Color color) {
            this.color = color;
            return this;
        }

        public Texture2D map() {
            return map;
        }

        public ColorMapProperty map(Texture2D map) {
            this.map = map;
            return this;
        }
    }
}
