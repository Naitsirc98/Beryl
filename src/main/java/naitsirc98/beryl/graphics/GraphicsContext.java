package naitsirc98.beryl.graphics;

import naitsirc98.beryl.resources.Resource;
import naitsirc98.beryl.scenes.environment.skybox.pbr.SkyboxPBRTextureFactory;

public interface GraphicsContext extends Resource {

    void init();

    boolean vsync();

    void vsync(boolean vsync);

    GraphicsMapper mapper();

    GraphicsFactory graphicsFactory();

    SkyboxPBRTextureFactory skyboxPBRTextureFactory();
}
