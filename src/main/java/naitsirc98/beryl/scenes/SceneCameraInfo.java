package naitsirc98.beryl.scenes;

import naitsirc98.beryl.graphics.GraphicsFactory;
import naitsirc98.beryl.graphics.buffers.UniformBuffer;
import naitsirc98.beryl.resources.Resource;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;

import static naitsirc98.beryl.util.types.DataType.MATRIX4_SIZEOF;
import static naitsirc98.beryl.util.types.DataType.VECTOR4_SIZEOF;
import static org.lwjgl.system.MemoryStack.stackPush;

public final class SceneCameraInfo implements Resource {

    public static final int CAMERA_BUFFER_SIZE = MATRIX4_SIZEOF + VECTOR4_SIZEOF;
    public static final int CAMERA_BUFFER_PROJECTION_VIEW_OFFSET = 0;
    public static final int CAMERA_BUFFER_CAMERA_POSITION_OFFSET = MATRIX4_SIZEOF;

    public static final int FRUSTUM_BUFFER_SIZE = MATRIX4_SIZEOF + 6 * VECTOR4_SIZEOF;
    public static final int FRUSTUM_BUFFER_PROJECTION_VIEW_OFFSET = 0;
    public static final int FRUSTUM_BUFFER_PLANES_OFFSET = MATRIX4_SIZEOF;


    private final UniformBuffer cameraBuffer;
    private final UniformBuffer frustumBuffer;

    SceneCameraInfo() {

        cameraBuffer = GraphicsFactory.get().newUniformBuffer();
        cameraBuffer.allocate(CAMERA_BUFFER_SIZE);
        cameraBuffer.mapMemory();

        frustumBuffer = GraphicsFactory.get().newUniformBuffer();
        frustumBuffer.allocate(FRUSTUM_BUFFER_SIZE);
        frustumBuffer.mapMemory();
    }

    @SuppressWarnings("unchecked")
    public <T extends UniformBuffer> T cameraBuffer() {
        return (T) cameraBuffer;
    }

    @SuppressWarnings("unchecked")
    public <T extends UniformBuffer> T frustumBuffer() {
        return (T) frustumBuffer;
    }

    @Override
    public void release() {
        cameraBuffer.release();
        frustumBuffer.release();
    }

    public void update(Camera camera) {
        updateCameraBuffer(camera);
        updateFrustumBuffer(camera);
    }

    private void updateCameraBuffer(Camera camera) {

        try (MemoryStack stack = stackPush()) {

            ByteBuffer buffer = stack.calloc(CAMERA_BUFFER_SIZE);

            camera.projectionViewMatrix().get(CAMERA_BUFFER_PROJECTION_VIEW_OFFSET, buffer);
            camera.position().get(CAMERA_BUFFER_CAMERA_POSITION_OFFSET, buffer);

            cameraBuffer.copy(0, buffer);
        }
    }

    private void updateFrustumBuffer(Camera camera) {

        try (MemoryStack stack = stackPush()) {

            ByteBuffer buffer = stack.calloc(FRUSTUM_BUFFER_SIZE);

            camera.projectionViewMatrix().get(FRUSTUM_BUFFER_PROJECTION_VIEW_OFFSET, buffer);

            for (int i = 0; i < 6; i++) {
                camera.frustumPlanes()[i].get(FRUSTUM_BUFFER_PLANES_OFFSET + i * VECTOR4_SIZEOF, buffer);
            }

            frustumBuffer.copy(0, buffer);
        }
    }
}
