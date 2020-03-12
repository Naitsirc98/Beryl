package naitsirc98.beryl.meshes;

import naitsirc98.beryl.meshes.materials.Material;
import naitsirc98.beryl.meshes.vertices.VertexData;

public class Mesh {

    private final VertexData vertexData;
    private final Material material;

    public Mesh(VertexData vertexData, Material material) {
        this.vertexData = vertexData;
        this.material = material;
    }

    @SuppressWarnings("unchecked")
    public final <T extends VertexData> T vertexData() {
        return (T) vertexData;
    }

    @SuppressWarnings("unchecked")
    public final <T extends Material> T material() {
        return (T) material;
    }
}
