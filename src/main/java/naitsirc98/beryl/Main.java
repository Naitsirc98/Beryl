package naitsirc98.beryl;

import naitsirc98.beryl.core.Beryl;
import naitsirc98.beryl.core.BerylApplication;
import naitsirc98.beryl.core.BerylConfiguration;
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
        // BerylConfiguration.GRAPHICS_API.set(GraphicsAPI.OPENGL);
    }

    @Override
    protected void onStart() {

        int count = RAND.nextInt(9) + 2;

        for(int i = 0;i < count;i++) {
            addScene(RAND);
        }

    }

    private void addScene(Random rand) {

        Scene scene = new Scene();

        int count = rand.nextInt(5000) + 5000;

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
            }

        }

        public void setCount(int count) {
            this.count = count;
        }
    }

}
