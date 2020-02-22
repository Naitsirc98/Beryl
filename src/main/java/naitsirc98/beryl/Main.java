package naitsirc98.beryl;

import naitsirc98.beryl.core.Beryl;
import naitsirc98.beryl.core.BerylApplication;
import naitsirc98.beryl.core.BerylConfiguration;
import naitsirc98.beryl.core.Log;
import naitsirc98.beryl.graphics.window.CursorType;
import naitsirc98.beryl.graphics.window.Window;
import naitsirc98.beryl.input.Gamepad;
import naitsirc98.beryl.input.Input;
import naitsirc98.beryl.input.Joystick;
import naitsirc98.beryl.scenes.Entity;
import naitsirc98.beryl.scenes.Scene;
import naitsirc98.beryl.scenes.SceneManager;
import naitsirc98.beryl.scenes.components.behaviours.Behaviour;

import static naitsirc98.beryl.input.Key.KEY_F1;
import static naitsirc98.beryl.input.Key.KEY_W;


public class Main extends BerylApplication {

    public static void main(String[] args) {

        Beryl.launch(new Main());

    }

    private Main() {

    }

    @Override
    protected void setConfiguration() {
        BerylConfiguration.DEBUG.set(true);
        BerylConfiguration.INTERNAL_DEBUG.set(true);
    }

    @Override
    protected void onStart() {

        Scene scene = SceneManager.newScene();

        Entity entity = Entity.create();

        entity.add(MyBehaviour.class);
    }

    @Override
    protected void onUpdate() {


    }

    private class MyBehaviour extends Behaviour {

        @Override
        protected void onUpdate() {

            if(Input.isKeyTyped(KEY_W)) {
                Log.trace("Hey!");
            }

        }
    }

}
