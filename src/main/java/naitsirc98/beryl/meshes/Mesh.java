package naitsirc98.beryl.meshes;

import naitsirc98.beryl.materials.Material;
import naitsirc98.beryl.meshes.vertices.VertexData;

import static java.util.Objects.requireNonNull;

public class Mesh {

    private final VertexData vertexData;
    private final Material material;

    public Mesh(VertexData vertexData, Material material) {
        this.vertexData = requireNonNull(vertexData);
        this.material = requireNonNull(material);
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
