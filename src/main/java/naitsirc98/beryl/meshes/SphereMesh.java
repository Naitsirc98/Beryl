package naitsirc98.beryl.meshes;

import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;

import static java.lang.Math.*;
import static naitsirc98.beryl.util.types.DataType.*;

public class SphereMesh {

    public static StaticMesh create(String name, int xSegments, int ySegments) {

        final int size = (xSegments + 1) * (ySegments + 1);

        ByteBuffer vertices = MemoryUtil.memAlloc((size * 3 + size * 2 + size * 3) * FLOAT32_SIZEOF);

        for(int y = 0;y <= ySegments;y++) {

            for(int x = 0;x <= xSegments;x++) {

                final float xSeg = (float)x / (float)xSegments;
                final float ySeg = (float)y / (float)ySegments;

                final float xPos = (float)(cos(xSeg * 2 * PI) * sin(ySeg * PI));
                final float yPos = (float)(cos(ySeg * PI));
                final float zPos = (float)(sin(xSeg * 2 * PI) * sin(ySeg * PI));

                // Position
                vertices.putFloat(xPos);
                vertices.putFloat(yPos);
                vertices.putFloat(zPos);
                // Normal
                vertices.putFloat(xPos);
                vertices.putFloat(yPos);
                vertices.putFloat(zPos);
                // UV
                vertices.putFloat(xSeg);
                vertices.putFloat(ySeg);
            }
        }

        final int indicesCount = ySegments * (xSegments + 1) * 2;

        ByteBuffer indices = MemoryUtil.memAlloc(indicesCount * UINT32_SIZEOF);

        for(int y = 0;y < ySegments;y++) {

            if(y % 2 == 0) {

                for(int x = 0;x <= xSegments;x++) {
                    indices.putInt(y * (xSegments + 1) + x);
                    indices.putInt((y + 1) * (xSegments + 1) + x);
                }

            } else {

                for(int x = xSegments;x >= 0;x--) {
                    indices.putInt((y + 1) * (xSegments + 1) + x);
                    indices.putInt(y * (xSegments + 1) + x);
                }

            }

        }

        return MeshManager.get().createStaticMesh(name, vertices.rewind(), indices.rewind());
    }
}
