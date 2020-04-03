package naitsirc98.beryl.meshes;

import naitsirc98.beryl.graphics.rendering.PrimitiveTopology;
import naitsirc98.beryl.materials.Material;
import naitsirc98.beryl.meshes.vertices.VertexData;
import naitsirc98.beryl.meshes.vertices.VertexLayout;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;

import static naitsirc98.beryl.graphics.rendering.PrimitiveTopology.TRIANGLE_STRIP;
import static naitsirc98.beryl.meshes.vertices.VertexLayout.VERTEX_LAYOUT_3D;
import static naitsirc98.beryl.util.types.DataType.FLOAT32_SIZEOF;

public class PrimitiveMeshes {

    public static Mesh createQuadMesh(Material material) {

        try(MemoryStack stack = MemoryStack.stackPush()) {

            ByteBuffer vertices = stack.malloc(8 * 4 * FLOAT32_SIZEOF);

            // Position        // Normals   //Texture coordinates
            vertices.putFloat(-1.0f).putFloat(1.0f).putFloat(0.0f).putFloat(0).putFloat(0).putFloat(-1).putFloat(0.0f).putFloat(1.0f);
            vertices.putFloat(-1.0f).putFloat(-1.0f).putFloat(0.0f).putFloat(0).putFloat(0).putFloat(-1).putFloat(0.0f).putFloat(0.0f);
            vertices.putFloat(1.0f).putFloat(1.0f).putFloat(0.0f).putFloat(0).putFloat(0).putFloat(-1).putFloat(1.0f).putFloat(1.0f);
            vertices.putFloat(1.0f).putFloat(-1.0f).putFloat(0.0f).putFloat(0).putFloat(0).putFloat(-1).putFloat(1.0f).putFloat(0.0f);

            return new Mesh(VertexData.builder(VERTEX_LAYOUT_3D, TRIANGLE_STRIP).vertices(0, vertices.rewind()).build(), material);
        }
    }


}
