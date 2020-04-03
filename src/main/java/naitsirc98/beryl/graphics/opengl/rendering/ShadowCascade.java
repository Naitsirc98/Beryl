package naitsirc98.beryl.graphics.opengl.rendering;

import naitsirc98.beryl.graphics.window.Window;
import naitsirc98.beryl.lights.DirectionalLight;
import naitsirc98.beryl.scenes.components.camera.Camera;
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
    private final Vector3f[] frustumCorners;
    private final Vector3f frustumCenter;


    public ShadowCascade() {
        lightViewMatrix = new Matrix4f();
        lightProjectionMatrix = new Matrix4f();
        lightProjectionViewMatrix = new Matrix4f();
        frustumCorners = new Vector3f[FRUSTUM_CORNERS_COUNT];
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

    public Vector3fc[] frustumCorners() {
        return frustumCorners;
    }

    public Vector3fc frustumCenter() {
        return frustumCenter;
    }

    public void update(Camera camera, float zNear, float zFar, DirectionalLight light) {

        Matrix4f cameraProjectionViewMatrix = new Matrix4f();
        cameraProjectionViewMatrix.setPerspective(radians(45), Window.get().aspect(), zNear, zFar);
        cameraProjectionViewMatrix.mul(camera.viewMatrix());

        float maxZ = -Float.MAX_VALUE;
        float minZ = Float.MAX_VALUE;

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

        updateLightViewMatrix(light.direction(), lightPosition);

        updateLightProjectionMatrix();

        lightProjectionMatrix.mul(lightViewMatrix, lightProjectionViewMatrix);
    }

    private void updateLightViewMatrix(Vector3fc lightDirection, Vector3f lightPosition) {

        // lightViewMatrix.setLookAt(lightDirection, lightPosition, new Vector3f(0, 1, 0));
        // float lightAngleX = (float) Math.toDegrees(Math.acos(lightDirection.z()));
        // float lightAngleY = (float) Math.toDegrees(Math.asin(lightDirection.x()));

        lightViewMatrix.setLookAt(lightPosition.negate(), lightDirection, new Vector3f(0, 1, 0));

        // lightViewMatrix.rotationX(-radians(lightAngleX)).rotateY(-radians(lightAngleY)).translate(lightPosition);
    }

    private void updateLightProjectionMatrix() {

        // Now calculate frustum dimensions in light space
        float minX =  Float.MAX_VALUE;
        float maxX = -Float.MIN_VALUE;
        float minY =  Float.MAX_VALUE;
        float maxY = -Float.MIN_VALUE;
        float minZ =  Float.MAX_VALUE;
        float maxZ = -Float.MIN_VALUE;

        for(int i = 0;i < FRUSTUM_CORNERS_COUNT;i++) {

            Vector4f lightSpaceCorner = new Vector4f(frustumCorners[i], 1.0f).mul(lightViewMatrix);

            minX = Math.min(lightSpaceCorner.x, minX);
            maxX = Math.max(lightSpaceCorner.x, maxX);
            minY = Math.min(lightSpaceCorner.y, minY);
            maxY = Math.max(lightSpaceCorner.y, maxY);
            minZ = Math.min(lightSpaceCorner.z, minZ);
            maxZ = Math.max(lightSpaceCorner.z, maxZ);
        }

        float distanceZ = maxZ - minZ;

        lightProjectionMatrix.setOrtho(minX, maxX, minY, maxY, 0, distanceZ);
    }

}
