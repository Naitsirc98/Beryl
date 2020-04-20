package naitsirc98.beryl.scenes.components.animations;

import naitsirc98.beryl.animations.Animation;
import naitsirc98.beryl.animations.KeyFrame;
import naitsirc98.beryl.core.Time;
import naitsirc98.beryl.meshes.models.AnimModel;
import naitsirc98.beryl.scenes.Component;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.requireNonNull;
import static naitsirc98.beryl.util.Asserts.assertNotEquals;
import static naitsirc98.beryl.util.Maths.lerp;

public class Animator extends Component<Animator> {

    private AnimModel model;
    private Animation currentAnimation;
    private float animationTime;
    private boolean loop;
    private Map<Integer, Matrix4f> boneTransformations;

    private Animator() {
    }

    @Override
    protected void init() {
        super.init();
        animationTime = 0.0f;
        loop = true;
        boneTransformations = new HashMap<>();
    }

    public AnimModel model() {
        return model;
    }

    public Animator model(AnimModel model) {
        this.model = requireNonNull(model);
        currentAnimation = model.animations().iterator().next();
        return this;
    }

    public Animator currentAnimation(String animationName) {
        currentAnimation = model.animation(animationName);
        animationTime = 0.0f;
        return this;
    }

    public Animation currentAnimation() {
        return currentAnimation;
    }

    public boolean loop() {
        return loop;
    }

    public Animator loop(boolean loop) {
        this.loop = loop;
        return this;
    }

    @Override
    public Class<? extends Component> type() {
        return Animator.class;
    }

    @Override
    protected void onEnable() {

    }

    @Override
    protected void onDisable() {

    }

    @Override
    protected Animator self() {
        return this;
    }

    @Override
    protected void onDestroy() {
        model = null;
        boneTransformations = null;
    }

    public Map<Integer, Matrix4f> currentBoneTransformations() {
        return boneTransformations;
    }

    void update() {

        if(currentAnimation == null) {
            return;
        }

        increaseAnimationTime();


        computeCurrentAnimationPose();
    }

    private void computeCurrentAnimationPose() {

        final int nextFrameIndex = nextFrame();

        final KeyFrame previous = currentAnimation.frame(nextFrameIndex == 0 ? 0 : nextFrameIndex - 1);
        final KeyFrame next = currentAnimation.frame(nextFrameIndex);

        final float progression = getFrameProgression(previous, next);

        interpolateAnimationPoses(previous, next, progression);
    }

    private void interpolateAnimationPoses(KeyFrame previous, KeyFrame next, float progression) {

        for(int i = 0;i < previous.boneMatrices().length;i++) {

            final int boneID = model.bone(i).id();

            Matrix4fc previousTransformation = previous.boneMatrix(i);

            Matrix4fc nextTransformation = next.boneMatrix(i);

            Matrix4f currentTransformation = boneTransformations.get(boneID);

            if(currentTransformation == null) {
                currentTransformation = new Matrix4f();
                boneTransformations.put(boneID, currentTransformation);
            }

            interpolateBoneTransformation(previousTransformation, nextTransformation, progression, currentTransformation);
        }
    }

    private void interpolateBoneTransformation(Matrix4fc previous, Matrix4fc next, float progression, Matrix4f dest) {

        final Vector3f previousPosition = previous.getTranslation(new Vector3f());
        final Vector3f nextPosition = next.getTranslation(new Vector3f());
        final Quaternionf previousRotation = previous.getUnnormalizedRotation(new Quaternionf());
        final Quaternionf nextRotation = next.getUnnormalizedRotation(new Quaternionf());

        assertNotEquals(previousPosition, nextPosition);

        final Vector3f position = lerp(previousPosition, nextPosition, progression);
        final Quaternionf rotation = lerp(previousRotation, nextRotation, progression);

        dest.translation(position).rotate(rotation);
    }

    private float getFrameProgression(KeyFrame previous, KeyFrame next) {
        final float totalTime = next.time() - previous.time();
        final float currentTime = animationTime - previous.time();
        if(totalTime == 0.0f) {
            return 0.0f;
        }
        return currentTime / totalTime;
    }

    private void increaseAnimationTime() {
        animationTime += Time.deltaTime();
        if(loop && animationTime > currentAnimation.duration()) {
            animationTime %= currentAnimation.duration();
        }
    }

    private int nextFrame() {
        for(int i = 1;i < currentAnimation.frameCount();i++) {
            if(currentAnimation.frame(i).time() >= animationTime) {
                return i;
            }
        }
        return 1;
    }
}
