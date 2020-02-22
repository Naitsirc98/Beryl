package naitsirc98.beryl;

import naitsirc98.beryl.core.Beryl;
import naitsirc98.beryl.core.BerylApplication;
import naitsirc98.beryl.core.BerylConfiguration;
import naitsirc98.beryl.core.Log;
import naitsirc98.beryl.input.Input;
import naitsirc98.beryl.scenes.Entity;
import naitsirc98.beryl.scenes.Scene;
import naitsirc98.beryl.scenes.SceneManager;
import naitsirc98.beryl.scenes.components.behaviours.Behaviour;

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

        double start = System.nanoTime();

        for(int i = 0;i < 100000;i++) {
            Entity entity = scene.newEntity();
            entity.add(MyBehaviour.class);
        }

        System.out.println("time = " + ((System.nanoTime() - start) / 1e6) + "ms");

    }

    @Override
    protected void onUpdate() {


    }

    private class MyBehaviour extends Behaviour {

        private double dummy;

        @Override
        protected void onUpdate() {

            dummy = Math.sin(Math.random());

            if(Input.isKeyTyped(KEY_W)) {
                Log.trace("Hey!");
            }

        }
    }

}
