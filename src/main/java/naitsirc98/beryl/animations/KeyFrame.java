package naitsirc98.beryl.animations;

import org.joml.Matrix4fc;

import java.util.Arrays;

public final class KeyFrame {

    private final float time;
    private final Matrix4fc[] boneMatrices;

    public KeyFrame(float time, Matrix4fc[] boneMatrices) {
        this.time = time;
        this.boneMatrices = boneMatrices;
    }

    public float time() {
        return time;
    }

    public Matrix4fc[] boneMatrices() {
        return boneMatrices;
    }

    public Matrix4fc boneMatrix(int index) {
        return boneMatrices[index];
    }

    @Override
    public String toString() {
        return "KeyFrame{" +
                "time=" + time +
                ", boneMatrices=" + Arrays.toString(boneMatrices) +
                '}';
    }
}
