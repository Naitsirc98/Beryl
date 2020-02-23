package naitsirc98.beryl.scenes.components.math;

import naitsirc98.beryl.scenes.Component;
import org.joml.*;

import java.util.ArrayList;
import java.util.List;

public final class Transform extends Component<Transform> {

    private Vector3f position;
    private Quaternionf rotation;
    private Vector3f rotationAxis;
    private Vector3f scale;
    private Matrix4f modelMatrix;
    private Matrix3f normalMatrix;
    private List<Transform> children;
    private boolean modified;
    private boolean dynamic;

    private Transform() {

    }

    @Override
    protected void init() {
        super.init();
        position = new Vector3f();
        rotation = new Quaternionf();
        rotationAxis = new Vector3f();
        scale = new Vector3f(1.0f);
        modelMatrix = new Matrix4f();
        normalMatrix = new Matrix3f();
        children = new ArrayList<>(0);
        modified = true;
        dynamic = false;
    }

    public Transform identity() {
        updateChildrenPosition(0, 0, 0);
        updateChildrenScale(1, 1, 1);
        updateChildrenRotation(0, 0, 0, 0);
        position.set(0.0f);
        rotation.identity();
        rotationAxis.set(0.0f);
        scale.set(1.0f);
        modelMatrix.identity();
        normalMatrix.identity();
        modify();
        return this;
    }

    public boolean dynamic() {
        return dynamic;
    }

    public Transform dynamic(boolean dynamic) {
        this.dynamic = dynamic;
        return this;
    }

    public boolean modified() {
        return modified;
    }

    public Vector3fc position() {
        return position;
    }

    public Transform position(float x, float y, float z) {
        updateChildrenPosition(x, y, z);
        position.set(x, y, z);
        modify();
        return this;
    }

    public Transform position(Vector3fc position) {
        return position(position.x(), position.y(), position.z());
    }

    public Transform translate(float x, float y, float z) {
        updateChildrenPosition(position.x + x, position.y + y, position.z + z);
        position.add(x, y, z);
        modify();
        return this;
    }

    public Transform translate(Vector3fc translation) {
        return translate(translation.x(), translation.y(), translation.z());
    }

    public Vector3fc scale() {
        return scale;
    }

    public float scaleX() {
        return scale.x;
    }

    public float scaleY() {
        return scale.y;
    }

    public float scaleZ() {
        return scale.z;
    }

    public Transform scale(float x, float y, float z) {
        updateChildrenScale(x, y, z);
        scale.set(x, y, z);
        modify();
        return this;
    }

    public Transform scale(Vector3fc scale) {
        return scale(scale.x(), scale.y(), scale.z());
    }

    public Transform scale(float xyz) {
        return scale(xyz, xyz, xyz);
    }

    public Quaternionfc rotation() {
        return rotation;
    }

    public float angle() {
        return rotation.angle();
    }

    public Vector3fc rotationAxis() {
        return rotationAxis;
    }

    public Transform rotate(float radians, float x, float y, float z) {
        updateChildrenRotation(radians, x, y, z);
        rotation.setAngleAxis(radians, x, y, z);
        rotationAxis.set(rotation.x, rotation.y, rotation.z);
        modify();
        return this;
    }

    public Transform rotate(float radians, Vector3fc axis) {
        return rotate(radians, axis.x(), axis.y(), axis.z());
    }

    public Transform rotate(Quaternionfc rotation) {
        return rotate(rotation.angle(), rotation.x(), rotation.y(), rotation.z());
    }

    public Transform rotateX(float radians) {
        updateChildrenRotation(radians, 1, 0, 0);
        rotation.rotateX(radians);
        modify();
        return this;
    }

    public Transform rotateY(float radians) {
        updateChildrenRotation(radians, 0, 1, 0);
        rotation.rotateY(radians);
        modify();
        return this;
    }

    public Transform rotateZ(float radians) {
        updateChildrenRotation(radians, 0, 0, 1);
        rotation.rotateZ(radians);
        modify();
        return this;
    }

    public Matrix4fc modelMatrix() {
        return modelMatrix;
    }

    public Matrix3fc normalMatrix() {
        return normalMatrix;
    }

    public boolean addChild(Transform child) {
        if(!children.contains(child)) {
            return children.add(child);
        }
        return false;
    }

    public void addChild(Transform child, int index) {
        if(children.contains(child) && children.indexOf(child) != index) {
            children.remove(index);
        }
        children.add(index, child);
    }

    public Transform child(int index) {
        return children.get(index);
    }

    public boolean removeChild(Transform child) {
        return children.remove(child);
    }

    public Transform removeChild(int index) {
        return children.remove(index);
    }

    private void modify() {
        if(!modified) {
            manager().markModified(this);
            modified = true;
        }
    }

    @Override
    protected TransformManager manager() {
        return (TransformManager) super.manager();
    }

    @Override
    protected void onEnable() {

    }

    @Override
    protected void onDisable() {

    }

    @Override
    protected Transform self() {
        return this;
    }

    @Override
    protected void onDestroy() {
        position = null;
        rotation = null;
        scale = null;
        modelMatrix = null;
        normalMatrix = null;
        rotationAxis = null;
        children = null;
    }

    @Override
    public Class<? extends Component> type() {
        return Transform.class;
    }

    void update() {
        if(modified) {
            modelMatrix.translation(position).rotate(rotation).scale(scale);
            // normalMatrix = transpose(inverse(mat3(model)))
            modelMatrix.get3x3(normalMatrix).invert().transpose();
            modified = false;
        }
    }

    private void updateChildrenPosition(float newX, float newY, float newZ) {
        final float deltaX = newX - position.x;
        final float deltaY = newY - position.y;
        final float deltaZ = newZ - position.z;
        for(Transform child : children) {
            child.translate(deltaX, deltaY, deltaZ);
        }
    }

    private void updateChildrenScale(float newX, float newY, float newZ) {
        final float deltaX = newX - scale.x;
        final float deltaY = newY - scale.y;
        final float deltaZ = newZ - scale.z;
        for(Transform child : children) {
            final Vector3fc s = child.scale;
            child.scale(s.x() + deltaX, s.y() + deltaY, s.z() + deltaZ);
        }
    }

    private void updateChildrenRotation(float radians, float newX, float newY, float newZ) {
        final float deltaRads = radians - angle();
        final float deltaX = newX - rotationAxis.x;
        final float deltaY = newY - rotationAxis.y;
        final float deltaZ = newZ - rotationAxis.z;
        for(Transform child : children) {
            child.rotate(child.angle() + deltaRads, deltaX, deltaY, deltaZ);
        }
    }
}
