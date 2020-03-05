package naitsirc98.beryl.examples.app1;

import naitsirc98.beryl.scenes.components.behaviours.Behaviour;
import naitsirc98.beryl.scenes.components.camera.Camera;
import naitsirc98.beryl.scenes.components.math.Transform;

import java.util.Random;

class MyBehaviour extends Behaviour {

    private static final Random RAND = new Random(System.nanoTime());

    @Override
    protected void onUpdate() {

        if(RAND.nextInt() % 20 == 0) {
            get(Transform.class).translate(RAND.nextFloat(), 0, 0);
        }

        if(RAND.nextInt() % 50 == 0) {
            get(Camera.class).lookAt(RAND.nextFloat(), RAND.nextFloat());
        }

    }
}
