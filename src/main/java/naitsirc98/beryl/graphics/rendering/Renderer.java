package naitsirc98.beryl.graphics.rendering;

import naitsirc98.beryl.scenes.Scene;

public interface Renderer {

    void init();

    void terminate();

    void render(Scene scene);
}
