package naitsirc98.beryl.examples.app1;


import naitsirc98.beryl.audio.AudioListener;
import naitsirc98.beryl.graphics.Graphics;
import naitsirc98.beryl.graphics.window.Window;
import naitsirc98.beryl.input.Gamepad;
import naitsirc98.beryl.input.Input;
import naitsirc98.beryl.input.Joystick;
import naitsirc98.beryl.input.Joystick.Axis;
import naitsirc98.beryl.input.Joystick.AxisDirection;
import naitsirc98.beryl.logging.Log;
import naitsirc98.beryl.scenes.Camera;
import naitsirc98.beryl.scenes.components.behaviours.LateBehaviour;
import org.joml.Vector3f;

import static naitsirc98.beryl.graphics.window.CursorType.DISABLED;
import static naitsirc98.beryl.graphics.window.CursorType.NORMAL;
import static naitsirc98.beryl.input.Input.*;
import static naitsirc98.beryl.input.Key.*;

public class CameraController extends LateBehaviour {

    private Camera camera;
    private Vector3f lastPosition;

    @Override
    protected void onStart() {
        Log.info("Initializing Camera controller...");
        camera = scene().camera();
        lastPosition = new Vector3f();
    }

    @Override
    public void onLateUpdate() {

        float amount = 2 / 60.0f;

        checkKeyboardMovement(amount);

        checkGamepadMovement(amount);

        if(isKeyTyped(KEY_ESCAPE)) {

            Window window = Window.get();

            if(window.cursorType() == NORMAL) {
                window.cursorType(DISABLED);
            } else {
                window.cursorType(NORMAL);
            }
        }

        if(isKeyTyped(KEY_V)) {
            Graphics.get().vsync(!Graphics.get().vsync());
        }

        // Window.get().focus();

        if(isKeyTyped(KEY_F1)) {
            Window.get().fullscreen();
        } else if(isKeyTyped(KEY_F2)) {
            Window.get().maximized();
        } else if(isKeyTyped(KEY_F3)) {
            Window.get().windowed();
        } else if(isKeyTyped(KEY_F4)) {
            Window.get().hide();
        } else if(isKeyTyped(KEY_F5)) {
            Window.get().show();
        }

        if(Input.isKeyPressed(KEY_P)) {
            Log.trace("Camera position: " + camera.position() + ", forward = " + camera.forward());
        }


        checkMouseLookAt();

        checkGamepadLookAt();

        AudioListener.get().position(camera.position());
        AudioListener.get().orientation(camera.forward(), camera.up());
        AudioListener.get().velocity(lastPosition.sub(camera.position()).negate());

        lastPosition.set(camera.position());
     }

    private void checkGamepadLookAt() {



    }

    private void checkMouseLookAt() {
        camera.lookAt(mouseX(), mouseY());
        camera.zoom(scrollY());
    }

    private void checkKeyboardMovement(float amount) {

        if(isKeyPressed(KEY_LEFT_SHIFT)) {
            amount *= 4.0f;
        }

        if(isKeyPressed(KEY_LEFT_ALT)) {
            amount *= 12.0f;
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

    }

    private void checkGamepadMovement(float amount) {

        Gamepad gamepad = Gamepad.of(Joystick.JOYSTICK_1);

        if(gamepad == null || !gamepad.isPresent()) {
            return;
        }

        if(gamepad.buttons().isPressed(Joystick.Button.BUTTON_A)) {
            amount *= 4.0f;
        }

        if(gamepad.buttons().isPressed(Joystick.Button.BUTTON_B)) {
            amount *= 12.0f;
        }

        if(gamepad.moved(Axis.AXIS_LEFT_Y, AxisDirection.UP)) {
            camera.move(Camera.Direction.FORWARD, amount);
        }
        if(gamepad.moved(Axis.AXIS_LEFT_Y, AxisDirection.DOWN)) {
            camera.move(Camera.Direction.BACKWARD, amount);
        }
        if(gamepad.moved(Axis.AXIS_LEFT_X, AxisDirection.LEFT)) {
            camera.move(Camera.Direction.LEFT, amount);
        }
        if(gamepad.moved(Axis.AXIS_LEFT_X, AxisDirection.RIGHT)) {
            camera.move(Camera.Direction.RIGHT, amount);
        }
        if(gamepad.axes().get(Joystick.Axis.AXIS_LEFT_TRIGGER) > 0.5f) {
            camera.move(Camera.Direction.UP, amount);
        }
        if(gamepad.axes().get(Joystick.Axis.AXIS_RIGHT_TRIGGER) > 0.5f) {
            camera.move(Camera.Direction.DOWN, amount);
        }

    }

    @Override
    protected void onDestroy() {
        Log.info("Destroying Camera controller...");
    }
}
