package naitsirc98.beryl.examples.app1;

import naitsirc98.beryl.core.Beryl;
import naitsirc98.beryl.core.BerylApplication;
import naitsirc98.beryl.core.BerylConfiguration;
import naitsirc98.beryl.graphics.GraphicsAPI;
import naitsirc98.beryl.graphics.window.Window;
import naitsirc98.beryl.scenes.Entity;
import naitsirc98.beryl.scenes.Scene;
import naitsirc98.beryl.scenes.SceneManager;
import naitsirc98.beryl.scenes.components.math.Transform;

import java.util.Random;

public class App1 extends BerylApplication {

    private static final Random RAND = new Random(System.nanoTime());

    public static void main(String[] args) {

        Beryl.launch(new App1());

    }

    private App1() {

    }

    @Override
    protected void setConfiguration() {
        BerylConfiguration.DEBUG.set(true);
        BerylConfiguration.INTERNAL_DEBUG.set(true);
        // BerylConfiguration.INITIAL_TIME_VALUE.set(4000.0);
        BerylConfiguration.WINDOW_RESIZABLE.set(false);
        BerylConfiguration.GRAPHICS_API.set(GraphicsAPI.OPENGL);
    }

    @Override
    protected void onStart() {

        Window.get().center();

        int count = 1; // RAND.nextInt(1) + 2;

        for(int i = 0;i < count;i++) {
            addScene();
        }

    }

    private void addScene() {

        Scene scene = new Scene();

        int count = RAND.nextInt(5000) + 10000;

        Entity lastOne = null;

        for(int i = 0;i < count;i++) {
            Entity entity = scene.newEntity(String.valueOf(i));
            entity.add(Transform.class).position(i, i*2, i*3);
            if(lastOne != null && i%2 == 0) {
                entity.get(Transform.class).addChild(lastOne.get(Transform.class));
            }
            entity.add(MyBehaviour.class).setCount(count);
            lastOne = entity;
        }

        SceneManager.addScene(scene);
    }

    @Override
    protected void onUpdate() {


    }


}
