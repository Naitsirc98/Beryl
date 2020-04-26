package naitsirc98.beryl.graphics.rendering.renderers;

import naitsirc98.beryl.graphics.window.Window;
import naitsirc98.beryl.lights.DirectionalLight;
import naitsirc98.beryl.scenes.Camera;
import org.joml.*;

import java.lang.Math;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.util.stream.IntStream.range;
import static naitsirc98.beryl.util.Maths.*;

public class ShadowCascade {

    private static final int FRUSTUM_CORNERS_COUNT = 8;

    private final Matrix4f lightViewMatrix;
    private final Matrix4f lightProjectionMatrix;
    private final Matrix4f lightProjectionViewMatrix;
    private final FrustumIntersection lightFrustum;
    private final Vector3f[] frustumCorners;
    private final Vector3f centroid;
    private float farPlane;

    public ShadowCascade() {
        lightViewMatrix = new Matrix4f();
        lightProjectionMatrix = new Matrix4f();
        lightProjectionViewMatrix = new Matrix4f();
        frustumCorners = new Vector3f[FRUSTUM_CORNERS_COUNT];
        lightFrustum = new FrustumIntersection();
        range(0, FRUSTUM_CORNERS_COUNT).forEach(i -> frustumCorners[i] = new Vector3f());
        centroid = new Vector3f();
    }

    public Matrix4fc lightViewMatrix() {
        return lightViewMatrix;
    }

    public Matrix4fc lightProjectionMatrix() {
        return lightProjectionMatrix;
    }

    public Matrix4fc lightProjectionViewMatrix() {
        return lightProjectionViewMatrix;
    }

    public FrustumIntersection lightFrustum() {
        return lightFrustum;
    }

    public Vector3fc[] frustumCorners() {
        return frustumCorners;
    }

    public Vector3fc frustumCenter() {
        return centroid;
    }

    public float farPlane() {
        return farPlane;
    }

    public void update(Camera camera, float zNear, float zFar, DirectionalLight light) {

        farPlane = zFar;

        Matrix4f cameraProjectionViewMatrix = new Matrix4f();
        cameraProjectionViewMatrix.setPerspective(camera.fov(), camera.viewport().aspect(), zNear, zFar);
        cameraProjectionViewMatrix.mul(camera.viewMatrix());

        float maxZ = Float.MIN_VALUE;
        float minZ = Float.MAX_VALUE;

        centroid.set(0, 0, 0);

        for(int i = 0;i < FRUSTUM_CORNERS_COUNT;i++) {

            Vector3f corner = frustumCorners[i].set(0, 0, 0);

            cameraProjectionViewMatrix.frustumCorner(i, corner);

            centroid.add(corner).div(FRUSTUM_CORNERS_COUNT);

            minZ = min(minZ, corner.z);
            maxZ = max(maxZ, corner.z);
        }

        // Go back from the centroid up to max.z - min.z in the direction of light
        Vector3fc lightDirection = new Vector3f(light.direction()).negate().normalize();

        Vector3f lightPosInc = new Vector3f().set(lightDirection);

        float distance = maxZ - minZ;

        lightPosInc.mul(distance);

        Vector3f lightPosition = new Vector3f();
        lightPosition.set(centroid).add(lightPosInc);

        updateLightViewMatrix(camera, lightDirection, lightPosition);

        updateLightProjectionMatrix();

        lightProjectionMatrix.mul(lightViewMatrix, lightProjectionViewMatrix);

        lightFrustum.set(lightProjectionViewMatrix);
    }

    private void updateLightViewMatrix(Camera camera, Vector3fc lightDirection, Vector3f lightPosition) {

        // lightViewMatrix.setLookAt(lightDirection, lightPosition, new Vector3f(0, 1, 0));
        final float lightAngleX = acos(lightDirection.z());
        final float lightAngleY = asin(lightDirection.x());

        // lightViewMatrix.setLookAt(lightPosition.x, lightPosition.y, lightPosition.z, -lightDirection.x(), -lightDirection.y(), -lightDirection.z(), 0, 1, 0);

        // lightViewMatrix.setLookAt(lightPosition.negate(), lightDirection, camera.up());

        lightViewMatrix.rotationX(lightAngleX).rotateY(lightAngleY).translate(lightPosition.negate());
    }

    private void updateLightProjectionMatrix() {

        // Now calculate frustum dimensions in light space
        float minX = Float.MAX_VALUE;
        float maxX = -Float.MIN_VALUE;
        float minY = Float.MAX_VALUE;
        float maxY = -Float.MIN_VALUE;
        float minZ = Float.MAX_VALUE;
        float maxZ = -Float.MIN_VALUE;

        Vector4f lightSpaceCorner = new Vector4f();

        for(int i = 0;i < FRUSTUM_CORNERS_COUNT;i++) {

            lightSpaceCorner.set(frustumCorners[i], 1.0f).mul(lightViewMatrix);

            minX = min(lightSpaceCorner.x, minX);
            maxX = max(lightSpaceCorner.x, maxX);
            minY = min(lightSpaceCorner.y, minY);
            maxY = max(lightSpaceCorner.y, maxY);
            minZ = min(lightSpaceCorner.z, minZ);
            maxZ = max(lightSpaceCorner.z, maxZ);
        }

        float distanceZ = maxZ - minZ;

        lightProjectionMatrix.setOrtho(minX, maxX, minY, maxY, 0, distanceZ);
    }

}
