package naitsirc98.beryl.meshes.models;

import naitsirc98.beryl.meshes.vertices.VertexData;
import naitsirc98.beryl.meshes.vertices.VertexLayout;
import naitsirc98.beryl.resources.ManagedResource;
import naitsirc98.beryl.util.collections.LookupTable;
import org.joml.Matrix4fc;
import org.lwjgl.system.NativeResource;

import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;
import static naitsirc98.beryl.graphics.rendering.PrimitiveTopology.TRIANGLES;
import static naitsirc98.beryl.util.types.DataType.INT32;
import static org.lwjgl.system.MemoryUtil.memFree;

public final class Model extends ManagedResource {

    private final Path path;
    private final VertexLayout vertexLayout;
    private final List<Node> nodes;
    private final LookupTable<String, Node> nodeNames;
    private final List<Mesh> meshes;
    private final LookupTable<String, Mesh> meshNames;

    Model(Path path, VertexLayout vertexLayout, int meshCount) {
        super(false);
        this.path = requireNonNull(path);
        this.vertexLayout = requireNonNull(vertexLayout);
        nodes = new ArrayList<>();
        nodeNames = new LookupTable<>();
        meshes = new ArrayList<>(meshCount);
        meshNames = new LookupTable<>();
        track();
    }

    public String name() {
        return path.getName(path.getNameCount()-1).toString();
    }

    public Path path() {
        return path;
    }

    public VertexLayout vertexLayout() {
        return vertexLayout;
    }

    public Node root() {
        return nodes.get(0);
    }

    public int nodeCount() {
        return nodes.size();
    }

    public int meshCount() {
        return meshes.size();
    }

    public Node node(int index) {
        return nodes.get(index);
    }

    public Node node(String name) {
        return nodeNames.valueOf(name);
    }

    public Mesh mesh(int index) {
        return meshes.get(index);
    }

    public Mesh mesh(String name) {
        return meshNames.valueOf(name);
    }

    public String nameOf(Node node) {
        return nodeNames.keyOf(node);
    }

    public String nameOf(Mesh mesh) {
        return meshNames.keyOf(mesh);
    }

    public Collection<Node> nodes() {
        return Collections.unmodifiableCollection(nodes);
    }

    public Collection<Mesh> meshes() {
        return Collections.unmodifiableCollection(meshes);
    }

    synchronized Node newNode(String name, int numChildren, int numMeshes) {

        Node node = new Node(nodes.size(), numChildren, numMeshes);

        nodes.add(node);
        nodeNames.put(name, node);

        return node;
    }

    synchronized Mesh newMesh(String name, ByteBuffer[] vertices, ByteBuffer indices) {

        Mesh mesh = new Mesh(meshes.size(), vertices, indices);

        meshes.add(mesh);
        meshNames.put(name, mesh);

        return mesh;
    }

    @Override
    protected void free() {
        meshes().stream().filter(Objects::nonNull).forEach(Mesh::free);
        meshes.clear();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Model model = (Model) o;
        return path.equals(model.path) &&
                vertexLayout.equals(model.vertexLayout);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path, vertexLayout);
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();

        builder.append("Model '").append(name()).append("' {\n").append("  Path: ").append("\"").append(path).append("\"").append('\n');
        builder.append("  VertexLayout: ").append(vertexLayout).append('\n');
        builder.append("  Structure:\n").append(root().toString("    ")).append('\n');

        return builder.append("  };").toString();
    }

    public final class Node implements NativeResource {

        private final int index;
        private final int meshIndicesOffset;
        private int[] indices;
        private Matrix4fc transformation;

        public Node(int index, int numChildren, int numMeshes) {
            this.index = index;
            meshIndicesOffset = numChildren;
            indices = new int[numChildren + numMeshes];
        }

        public int index() {
            return index;
        }

        public String name() {
            return nameOf(this);
        }

        public Matrix4fc transformation() {
            return transformation;
        }

        public int numChildren() {
            return meshIndicesOffset;
        }

        public int numMeshes() {
            return indices.length - meshIndicesOffset;
        }

        public Node child(int index) {
            return nodes.get(indices[index]);
        }

        public Mesh mesh(int index) {
            return meshes.get(indices[meshIndicesOffset + index]);
        }

        public Stream<Node> children() {
            return Arrays.stream(indices, 0, meshIndicesOffset).mapToObj(nodes::get);
        }

        public Stream<Mesh> meshes() {
            return Arrays.stream(indices, meshIndicesOffset, indices.length).mapToObj(meshes::get);
        }

        @Override
        public void free() {
            indices = null;
        }

        void transformation(Matrix4fc transformation) {
            this.transformation = transformation;
        }

        synchronized void addChild(int index, Node child) {
            indices[index] = child.index;
        }

        synchronized void addMesh(int index, Mesh mesh) {
            indices[meshIndicesOffset + index] = mesh.index;
        }

        @Override
        public String toString() {
            return toString("");
        }

        public String toString(String indentation) {
            String innerIndentation = indentation + "  ";
            String childrenStr = children().map(node -> node.toString(innerIndentation + "  ")).collect(joining(",\n"));
            return String.format("%sNode[%d] '%s' {\n%smeshes:[%s],\n%schildren: [%s]\n%s};",
                    indentation, index, name(),
                    innerIndentation, meshes().map(Mesh::name).collect(joining(", ")),
                    innerIndentation, childrenStr.isEmpty() ? "" : "\n" + childrenStr + "\n" + innerIndentation, indentation);
        }
    }

    public final class Mesh implements NativeResource {

        private final int index;
        private ByteBuffer[] vertices;
        private ByteBuffer indices;

        Mesh(int index, ByteBuffer[] vertices, ByteBuffer indices) {
            this.index = index;
            this.vertices = vertices;
            this.indices = indices;
        }

        public int index() {
            return index;
        }

        public String name() {
            return nameOf(this);
        }

        public VertexLayout vertexLayout() {
            return vertexLayout;
        }

        public ByteBuffer[] vertices() {
            return vertices;
        }

        public ByteBuffer indices() {
            return indices;
        }

        public VertexData createVertexData() {

            VertexData.Builder builder = VertexData.builder(vertexLayout, TRIANGLES);

            for(int i = 0;i < vertices.length;i++) {
                builder.vertices(i, vertices[i]);
            }

            if(indices != null) {
                builder.indices(indices, INT32);
            }

            return builder.build();
        }

        @Override
        public void free() {

            for(ByteBuffer buffer : vertices) {
                memFree(buffer);
            }

            memFree(indices);

            vertices = null;
            indices = null;
        }
    }
}
