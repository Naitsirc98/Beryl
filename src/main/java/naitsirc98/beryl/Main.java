package naitsirc98.beryl;

import naitsirc98.beryl.core.Beryl;
import naitsirc98.beryl.core.BerylApplication;
import naitsirc98.beryl.core.BerylConfiguration;
import naitsirc98.beryl.graphics.window.CursorType;
import naitsirc98.beryl.graphics.window.Window;
import naitsirc98.beryl.input.Gamepad;
import naitsirc98.beryl.input.Input;
import naitsirc98.beryl.input.Joystick;

import static naitsirc98.beryl.input.Key.KEY_F1;


public class Main extends BerylApplication {

    public static void main(String[] args) {

        Beryl.launch(new Main());

    }

    private Gamepad gamepad;

    private Main() {

    }

    @Override
    protected void setConfiguration() {
        BerylConfiguration.DEBUG.set(true);
        BerylConfiguration.INTERNAL_DEBUG.set(true);
    }

    @Override
    protected void onStart() {

        gamepad = Gamepad.of(Joystick.JOYSTICK_1);

    }

    @Override
    protected void onUpdate() {

        if(gamepad.axes().get(Joystick.Axis.AXIS_RIGHT_TRIGGER) >= 0.5f) {
            System.out.println("Shoot!");
        }

        if(Input.isKeyPressed(KEY_F1)) {
            Window.get().cursorType(CursorType.DISABLED);
        }

    }
}
