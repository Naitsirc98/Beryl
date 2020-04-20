package naitsirc98.beryl.animations;

import org.joml.Matrix4fc;

public final class KeyFrame {

    private final Matrix4fc[] boneMatrices;

    public KeyFrame(Matrix4fc[] boneMatrices) {
        this.boneMatrices = boneMatrices;
    }

    public Matrix4fc[] boneMatrices() {
        return boneMatrices;
    }

    public Matrix4fc boneMatrix(int index) {
        return boneMatrices[index];
    }
}
