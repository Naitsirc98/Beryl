package naitsirc98.beryl.graphics.rendering.renderers;

import naitsirc98.beryl.graphics.window.Window;
import naitsirc98.beryl.lights.DirectionalLight;
import naitsirc98.beryl.scenes.Camera;
import org.joml.*;

import java.lang.Math;

import static java.lang.Math.*;
import static java.util.stream.IntStream.range;
import static naitsirc98.beryl.util.Maths.*;

public class ShadowCascade {

    private static final int FRUSTUM_CORNERS_COUNT = 8;

    private final Matrix4f lightViewMatrix;
    private final Matrix4f lightProjectionMatrix;
    private final Matrix4f lightProjectionViewMatrix;
    private final FrustumIntersection lightFrustum;
    private final Vector3f[] frustumCorners;
    private final Vector3f frustumCenter;
    private float farPlane;

    public ShadowCascade() {
        lightViewMatrix = new Matrix4f();
        lightProjectionMatrix = new Matrix4f();
        lightProjectionViewMatrix = new Matrix4f();
        frustumCorners = new Vector3f[FRUSTUM_CORNERS_COUNT];
        lightFrustum = new FrustumIntersection();
        range(0, FRUSTUM_CORNERS_COUNT).forEach(i -> frustumCorners[i] = new Vector3f());
        frustumCenter = new Vector3f();
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
        return frustumCenter;
    }

    public float farPlane() {
        return farPlane;
    }

    public void update(Camera camera, float zNear, float zFar, DirectionalLight light) {

        farPlane = zFar;

        Matrix4f cameraProjectionViewMatrix = new Matrix4f();
        cameraProjectionViewMatrix.setPerspective(camera.fov(), Window.get().aspect(), zNear, zFar);
        cameraProjectionViewMatrix.mul(camera.viewMatrix());

        float maxZ = Float.MIN_VALUE;
        float minZ = Float.MAX_VALUE;

        frustumCenter.set(0, 0, 0);

        for(int i = 0;i < FRUSTUM_CORNERS_COUNT;i++) {

            Vector3f corner = frustumCorners[i];

            cameraProjectionViewMatrix.frustumCorner(i, corner);

            frustumCenter.add(corner).div(FRUSTUM_CORNERS_COUNT);

            minZ = min(minZ, corner.z);
            maxZ = max(maxZ, corner.z);
        }

        float distance = maxZ - minZ;
        Vector3f lightPosInc = new Vector3f(light.direction()).mul(distance);
        Vector3f lightPosition = new Vector3f().set(frustumCenter).add(lightPosInc);

        updateLightViewMatrix(camera, light.direction(), lightPosition);

        updateLightProjectionMatrix();

        lightProjectionMatrix.mul(lightViewMatrix, lightProjectionViewMatrix);

        lightFrustum.set(lightProjectionViewMatrix);
    }

    private void updateLightViewMatrix(Camera camera, Vector3fc lightDirection, Vector3f lightPosition) {

        // lightViewMatrix.setLookAt(lightDirection, lightPosition, new Vector3f(0, 1, 0));
        final float lightAngleX = (float) Math.toDegrees(Math.acos(-lightDirection.z()));
        final float lightAngleY = (float) Math.toDegrees(Math.asin(lightDirection.x()));

        // lightViewMatrix.setLookAt(lightPosition.negate(new Vector3f()), lightDirection.normalize(new Vector3f()), camera.up());
        // lightViewMatrix.setLookAt(lightPosition, lightDirection, camera.up());

        lightViewMatrix.rotationX(radians(lightAngleX)).rotateY(radians(lightAngleY)).translate(lightPosition);
    }

    private void updateLightProjectionMatrix() {

        // Now calculate frustum dimensions in light space
        float minX =  Float.MAX_VALUE;
        float maxX = -Float.MIN_VALUE;
        float minY =  Float.MAX_VALUE;
        float maxY = -Float.MIN_VALUE;
        float minZ =  Float.MAX_VALUE;
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
