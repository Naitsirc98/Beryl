package naitsirc98.beryl.graphics.rendering;

import naitsirc98.beryl.scenes.Scene;

public interface APIRenderSystem {

    void init();

    void terminate();

    void begin();

    void prepare(Scene scene);

    void render(Scene scene);

    void end();
}
