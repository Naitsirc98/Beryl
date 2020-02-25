package naitsirc98.beryl.examples.app1;

import naitsirc98.beryl.input.Input;
import naitsirc98.beryl.input.MouseButton;
import naitsirc98.beryl.logging.Log;
import naitsirc98.beryl.scenes.Entity;
import naitsirc98.beryl.scenes.components.behaviours.Behaviour;
import naitsirc98.beryl.scenes.components.camera.Camera;
import naitsirc98.beryl.scenes.components.math.Transform;

import java.util.Random;

import static naitsirc98.beryl.input.Key.*;

class MyBehaviour extends Behaviour {

    private static final Random RAND = new Random(System.nanoTime());

    private static boolean sceneModificationEnabled;

    private int count;

    @Override
    protected void onUpdate() {

        if(RAND.nextInt() % 20 == 0) {
            get(Transform.class).translate(RAND.nextFloat(), 0, 0);
        }

        if(RAND.nextInt() % 50 == 0) {
            get(Camera.class).lookAt(RAND.nextFloat(), RAND.nextFloat());
        }

        if(sceneModificationEnabled) {
            addOrRemoveRandomly();
        }

        if(Input.isKeyTyped(KEY_Q)) {
            sceneModificationEnabled = true;
        } else if(Input.isKeyTyped(KEY_W)) {
            sceneModificationEnabled = false;
        }

    }

    private void addOrRemoveRandomly() {

        if(RAND.nextFloat() < 0.001f) {

            String name = String.valueOf(RAND.nextInt(count));

            if(entity().name().equals(name)) {
                return;
            }

            scene().destroy(name);

        }

        if(RAND.nextFloat() < 0.001f) {

            String name = String.valueOf(RAND.nextInt(count));

            if(scene().exists(name)) {
                return;
            }

            ++count;

            Entity entity = scene().newEntity(name);
            entity.add(MyBehaviour.class).setCount(count);
            entity.add(Transform.class).position(RAND.nextFloat(), RAND.nextFloat(), RAND.nextFloat());
        }
    }

    public void setCount(int count) {
        this.count = count;
    }
}
