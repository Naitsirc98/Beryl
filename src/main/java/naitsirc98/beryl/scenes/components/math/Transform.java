package naitsirc98.beryl.scenes.components.math;

import naitsirc98.beryl.logging.Log;
import naitsirc98.beryl.scenes.Component;
import org.joml.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * A Transform contains the position, scale and rotation of a {@link naitsirc98.beryl.scenes.SceneObject}.
 * 
 * A Transform may have children, which will be updated when the parent Transform changes.
 * 
 */
public final class Transform extends Component<Transform> {

    private Vector3f position;
    private Quaternionf rotation;
    private AxisAngle4f rotationAxis;
    private Vector3f scale;
    private Matrix4f modelMatrix;
    private Matrix4f normalMatrix; // Use Matrix4f to avoid alignment issues in shaders
    private Transform parent;
    private List<Transform> children;
    private boolean modified;

    private Transform() {

    }

    @Override
    protected void init() {
        super.init();
        position = new Vector3f();
        rotation = new Quaternionf();
        rotationAxis = rotation.get(new AxisAngle4f());
        scale = new Vector3f(1.0f);
        modelMatrix = new Matrix4f();
        normalMatrix = new Matrix4f();
        children = new ArrayList<>(0);
        identity();
    }

    /**
     * Sets this transform to the identity transform.
     *
     * @return this transform
     */
    public Transform identity() {
        assertNotDeleted();

        updateChildrenPosition(0, 0, 0);
        updateChildrenScale(1, 1, 1);
        updateChildrenRotation(0, 0, 0, 0);
        position.set(0.0f);
        rotation.identity();
        rotation.get(rotationAxis);
        scale.set(1.0f);
        modelMatrix.identity();
        normalMatrix.identity();
        modify();
        return this;
    }

    /**
     * Tells whether this transform is modified or not. A modified transform needs to update its matrices.
     *
     * @return true if this transform has been modified, false otherwise
     */
    public boolean modified() {
        assertNotDeleted();
        return modified;
    }

    /**
     * Returns the position
     *
     * @return the position
     */
    public Vector3fc position() {
        assertNotDeleted();
        return position;
    }

    /**
     * Sets the position of this transform
     *
     * @param x the x coordinate
     * @param y the y coordinate
     * @param z the z coordinate
     * @return this transform
     */
    public Transform position(float x, float y, float z) {
        assertNotDeleted();
        if(enabled()) {
            updateChildrenPosition(x, y, z);
            position.set(x, y, z);
            modify();
        }
        return this;
    }

    /**
     * Sets the position of this transform. The given vector is copied.
     *
     * @param position the position vector
     * @return this transform
     */
    public Transform position(Vector3fc position) {
        assertNotDeleted();
        return position(position.x(), position.y(), position.z());
    }

    /**
     * Translates this transform.
     *
     * @param x the x coordinate
     * @param y the y coordinate
     * @param z the z coordinate
     * @return this transform
     */
    public Transform translate(float x, float y, float z) {
        assertNotDeleted();
        if(enabled()) {
            updateChildrenPosition(position.x + x, position.y + y, position.z + z);
            position.add(x, y, z);
            modify();
        }
        return this;
    }

    /**
     * Translates this transform. The given vector is copied.
     *
     * @param translation the translation vector
     * @return this transform
     */
    public Transform translate(Vector3fc translation) {
        assertNotDeleted();
        return translate(translation.x(), translation.y(), translation.z());
    }

    /**
     * Returns the scale of this transform
     *
     * @return the scale
     */
    public Vector3fc scale() {
        assertNotDeleted();
        return scale;
    }

    /**
     * Scales this transform.
     *
     * @param x the x scale
     * @param y the y scale
     * @param z the z scale
     * @return this transform
     */
    public Transform scale(float x, float y, float z) {
        assertNotDeleted();
        if(enabled()) {
            updateChildrenScale(x, y, z);
            scale.set(x, y, z);
            modify();
        }
        return this;
    }

    /**
     * Scales this transform. The given vector is copied.
     *
     * @param scale the scale
     * @return this transform
     */
    public Transform scale(Vector3fc scale) {
        assertNotDeleted();
        return scale(scale.x(), scale.y(), scale.z());
    }

    /**
     * Scales this transform. All axis will be scale by the given value.
     *
     * @param xyz the xyz scale
     * @return this transform
     */
    public Transform scale(float xyz) {
        assertNotDeleted();
        return scale(xyz, xyz, xyz);
    }

    /**
     * Returns the rotation of this transform.
     *
     * @return the rotation
     */
    public Quaternionfc rotation() {
        assertNotDeleted();
        return rotation;
    }

    /**
     * Returns the rotation angle of this transform.
     *
     * @return the rotation angle, in radians
     */
    public float angle() {
        assertNotDeleted();
        return rotation.angle();
    }

    /**
     * Returns the rotation axis of this transform.
     *
     * @return the rotation axis
     */
    public AxisAngle4f rotationAxis() {
        assertNotDeleted();
        return rotationAxis;
    }

    /**
     * Rotates this transform by the given angle and by the given rotation axis. The rotation axis must be normalized.
     *
     * @param radians the rotation angle, in radians
     * @param x       the x rotation axis coordinate
     * @param y       the y rotation axis coordinate
     * @param z       the z rotation axis coordinate
     * @return this transform
     */
    public Transform rotate(float radians, float x, float y, float z) {
        assertNotDeleted();
        if(enabled()) {
            updateChildrenRotation(radians, x, y, z);
            rotationAxis.set(radians, x, y, z);
            rotation.set(rotationAxis);
            modify();
        }
        return this;
    }

    /**
     * Rotates this transform by the given angle and by the given rotation axis. The rotation axis must be normalized.
     *
     * @param radians the rotation angle, in radians
     * @param axis    the rotation axis
     * @return this transform
     */
    public Transform rotate(float radians, Vector3fc axis) {
        assertNotDeleted();
        return rotate(radians, axis.x(), axis.y(), axis.z());
    }

    /**
     * Rotates this transform by the given angle and by the given rotation axis. The rotation axis must be normalized.
     *
     * @param rotationAxis  the rotation axis
     * @return this transform
     */
    public Transform rotate(AxisAngle4f rotationAxis) {
        assertNotDeleted();
        return rotate(rotationAxis.angle, rotationAxis.x, rotationAxis.y, rotationAxis.z);
    }

    /**
     * Rotates this transform by the given quaternion. The given quaternion is copied.
     *
     * @param rotation the quaternion representing the desired rotation
     * @return this transform
     */
    public Transform rotate(Quaternionfc rotation) {
        assertNotDeleted();
        return rotate(rotation.angle(), rotation.x(), rotation.y(), rotation.z());
    }

    /**
     * Rotate this transform by the given angle in the x coordinate.
     *
     * @param radians the rotation angle, in radians
     * @return this transform
     */
    public Transform rotateX(float radians) {
        assertNotDeleted();
        if(enabled()) {
            updateChildrenRotation(radians, 1, 0, 0);
            rotation.rotateX(radians);
            modify();
        }
        return this;
    }

    /**
     * Rotate this transform by the given angle in the y coordinate.
     *
     * @param radians the rotation angle, in radians
     * @return this transform
     */
    public Transform rotateY(float radians) {
        assertNotDeleted();
        if(enabled()) {
            updateChildrenRotation(radians, 0, 1, 0);
            rotation.rotateY(radians);
            modify();
        }
        return this;
    }

    /**
     * Rotate this transform by the given angle in the z coordinate.
     *
     * @param radians the rotation angle, in radians
     * @return this transform
     */
    public Transform rotateZ(float radians) {
        assertNotDeleted();
        if(enabled()) {
            updateChildrenRotation(radians, 0, 0, 1);
            rotation.rotateZ(radians);
            modify();
        }
        return this;
    }


    /**
     * Apply the given transformations to this transform.
     *
     * @param transformation the transformation matrix
     * @return this transform
     */
    public Transform transformation(Matrix4fc transformation) {
        Vector3f vector = new Vector3f();
        Quaternionf quaternion = new Quaternionf();
        transformation.getTranslation(vector);
        position(vector);
        transformation.getScale(vector);
        scale(vector);
        transformation.getUnnormalizedRotation(quaternion);
        rotate(quaternion);
        return this;
    }

    /**
     * Returns the model matrix of this transform. If this transform is modified, then this matrix need to be updated.
     *
     * @return the model matrix.
     */
    public Matrix4fc modelMatrix() {
        assertNotDeleted();
        return modelMatrix;
    }

    /**
     * Returns the normal matrix of this transform. If this transform is modified, then this matrix need to be updated.
     *
     * @return the normal matrix.
     */
    public Matrix4fc normalMatrix() {
        assertNotDeleted();
        return normalMatrix;
    }

    /**
     * Returns the parent of this transform.
     *
     * @return the parent transform
     * */
    public Transform parent() {
        assertNotDeleted();
        return parent;
    }

    /**
     * Adds a child transform to this transform.
     *
     * @param child the child transform
     * @return true if the given child transform has been added, false otherwise.
     */
    public boolean addChild(Transform child) {
        assertNotDeleted();

        if(child == null) {
            Log.error("Cannot add a null child");
            return false;
        }

        if(child.parent == this) {
            Log.trace("The given transform is already a child of this transform");
            return false;
        }

        if(child.parent != null) {
            Log.error("The given transform already has a parent");
            return false;
        }

        if(child.scene() != scene()) {
            Log.error("Cannot add a transform child from another scene");
            return false;
        }

        child.parent = this;

        return children.add(child);
    }

    /**
     * Tells whether the specified transform is child of this transform or not.
     *
     * @return true if the given transform is a child of this transform, false otherwise
     * */
    public boolean hasChild(Transform child) {
        assertNotDeleted();

        if(child == null) {
            return false;
        }

        return child.parent == this;
    }

    /**
     * Removes the given child transform from this transform.
     *
     * @param child the child transform
     * @return true if the child transform was removed, false otherwise
     */
    public boolean removeChild(Transform child) {
        assertNotDeleted();

        if(!hasChild(child)) {
            return false;
        }

        return children.remove(child);
    }

    /**
     * Removes all children.
     */
    public void removeAllChildren() {
        for(Transform child : children) {
            child.parent = null;
        }
        children.clear();
    }

    /**
     * Returns a stream with all the children of this transform.
     *
     * @return this transform's children as stream
     * */
    public Stream<Transform> children() {
        assertNotDeleted();
        return children.stream();
    }

    private void modify() {
        modified = true;
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

        if(parent != null) {
            parent.removeChild(this);
        }
        parent = null;

        removeAllChildren();
        children = null;

        position = null;
        rotation = null;
        scale = null;
        modelMatrix = null;
        normalMatrix = null;
        rotationAxis = null;
    }

    @Override
    public Class<? extends Component> type() {
        return Transform.class;
    }

    /**
     * Updates this transform's matrices
     */
    void update() {
        modelMatrix.translation(position).rotate(rotation).scale(scale);
        // normalMatrix = transpose(inverse(mat3(model)))
        normalMatrix.set(modelMatrix).invert().transpose();
        normalMatrix._m30(0.0f)._m31(0.0f)._m32(0.0f);
        modified = false;
    }

    private void updateChildrenPosition(float newX, float newY, float newZ) {
        if(children.isEmpty()) {
            return;
        }

        final float deltaX = newX - position.x;
        final float deltaY = newY - position.y;
        final float deltaZ = newZ - position.z;

        for(Transform child : children) {
            if(child.enabled()) {
                child.translate(deltaX, deltaY, deltaZ);
            }
        }
    }

    private void updateChildrenScale(float newX, float newY, float newZ) {
        if(children.isEmpty()) {
            return;
        }

        final float deltaX = newX - scale.x;
        final float deltaY = newY - scale.y;
        final float deltaZ = newZ - scale.z;

        for(Transform child : children) {
            if(child.enabled()) {
                final Vector3fc s = child.scale;
                child.scale(s.x() + deltaX, s.y() + deltaY, s.z() + deltaZ);
            }
        }
    }

    private void updateChildrenRotation(float radians, float newX, float newY, float newZ) {
        if(children.isEmpty()) {
            return;
        }

        for(Transform child : children) {
            if(child.enabled()) {
                child.rotate(radians, newX, newY, newZ);
            }
        }
    }
}
