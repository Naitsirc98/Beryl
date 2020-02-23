package naitsirc98.beryl;

import naitsirc98.beryl.core.Beryl;
import naitsirc98.beryl.core.BerylApplication;
import naitsirc98.beryl.core.BerylConfiguration;
import naitsirc98.beryl.graphics.GraphicsAPI;
import naitsirc98.beryl.input.Input;
import naitsirc98.beryl.logging.Log;
import naitsirc98.beryl.scenes.Entity;
import naitsirc98.beryl.scenes.Scene;
import naitsirc98.beryl.scenes.SceneManager;
import naitsirc98.beryl.scenes.components.behaviours.Behaviour;

import java.util.Random;

import static naitsirc98.beryl.input.Key.KEY_W;


public class Main extends BerylApplication {

    private static final Random RAND = new Random(System.nanoTime());

    public static void main(String[] args) {

        Beryl.launch(new Main());

    }

    private Main() {

    }

    @Override
    protected void setConfiguration() {
        BerylConfiguration.DEBUG.set(true);
        BerylConfiguration.INTERNAL_DEBUG.set(true);
        // BerylConfiguration.INITIAL_TIME_VALUE.set(4000.0);
        BerylConfiguration.WINDOW_RESIZABLE.set(false);
        BerylConfiguration.GRAPHICS_API.set(GraphicsAPI.VULKAN);
    }

    @Override
    protected void onStart() {

        int count = RAND.nextInt(1) + 2;

        for(int i = 0;i < count;i++) {
            addScene();
        }

    }

    private void addScene() {

        Scene scene = new Scene();

        int count = RAND.nextInt(5000) + 5000;

        for(int i = 0;i < count;i++) {
            Entity entity = scene.newEntity(String.valueOf(i));
            entity.add(MyBehaviour.class).setCount(count);
        }

        SceneManager.addScene(scene);
    }

    @Override
    protected void onUpdate() {


    }

    private class MyBehaviour extends Behaviour {

        private int count;

        @Override
        protected void onUpdate() {

            if(Input.isKeyTyped(KEY_W)) {
                Log.trace("Hey!");
            }

            if(RAND.nextFloat() < 0.05f) {

                String name = String.valueOf(RAND.nextInt(count));

                if(entity().name().equals(name)) {
                    return;
                }

                scene().destroy(name);

            } else if(RAND.nextFloat() < 0.085f) {

                String name = String.valueOf(RAND.nextInt(count));

                if(scene().exists(name)) {
                    return;
                }

                scene().newEntity(name).add(MyBehaviour.class).setCount(count);
            }

        }

        public void setCount(int count) {
            this.count = count;
        }
    }

}
