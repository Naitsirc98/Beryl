package naitsirc98.beryl.examples.app1;


import naitsirc98.beryl.core.Time;
import naitsirc98.beryl.graphics.window.Window;
import naitsirc98.beryl.logging.Log;
import naitsirc98.beryl.scenes.components.behaviours.Behaviour;
import naitsirc98.beryl.scenes.components.camera.Camera;

import static naitsirc98.beryl.graphics.window.CursorType.DISABLED;
import static naitsirc98.beryl.graphics.window.CursorType.NORMAL;
import static naitsirc98.beryl.input.Input.*;
import static naitsirc98.beryl.input.Key.*;

public class CameraController extends Behaviour {

    private Camera camera;

    @Override
    protected void onStart() {
        Log.info("Initializing Camera controller...");
        camera = scene().camera();
    }

    @Override
    protected void onUpdate() {

        float amount = 1 / 60.0f;

        if(isKeyPressed(KEY_LEFT_SHIFT)) {
            amount *= 2.5f;
        }

        if(isKeyPressed(KEY_LEFT_ALT)) {
            amount *= 10.0f;
        }

        if(isKeyPressed(KEY_W)) {
            camera.move(Camera.Direction.FORWARD, amount);
        }
        if(isKeyPressed(KEY_S)) {
            camera.move(Camera.Direction.BACKWARD, amount);
        }
        if(isKeyPressed(KEY_A)) {
            camera.move(Camera.Direction.LEFT, amount);
        }
        if(isKeyPressed(KEY_D)) {
            camera.move(Camera.Direction.RIGHT, amount);
        }
        if(isKeyPressed(KEY_SPACE)) {
            camera.move(Camera.Direction.UP, amount);
        }
        if(isKeyPressed(KEY_LEFT_CONTROL)) {
            camera.move(Camera.Direction.DOWN, amount);
        }

        if(isKeyTyped(KEY_ESCAPE)) {

            Window window = Window.get();

            if(window.cursorType() == NORMAL) {
                window.cursorType(DISABLED);
            } else {
                window.cursorType(NORMAL);
            }
        }

        if(isKeyTyped(KEY_F1)) {
            Window.get().fullscreen();
        }

        camera.lookAt(mouseX(), mouseY());
        camera.zoom(scrollY());
     }

    @Override
    protected void onDestroy() {
        Log.info("Destroying Camera controller...");
    }
}
