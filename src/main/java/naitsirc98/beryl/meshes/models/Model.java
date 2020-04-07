package naitsirc98.beryl.meshes.models;

import naitsirc98.beryl.meshes.Mesh;
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
import static org.lwjgl.system.MemoryUtil.memCalloc;
import static org.lwjgl.system.MemoryUtil.memFree;

public final class Model extends ManagedResource {

    private final Path path;
    private final List<Node> nodes;
    private final LookupTable<String, Node> nodeNames;
    private final List<LoadedMesh> loadedMeshes;
    private final LookupTable<String, LoadedMesh> meshNames;

    Model(Path path, int meshCount) {
        super(false);
        this.path = requireNonNull(path);
        nodes = new ArrayList<>();
        nodeNames = new LookupTable<>();
        loadedMeshes = new ArrayList<>(meshCount);
        meshNames = new LookupTable<>();
        track();
    }

    public String name() {
        return path.getName(path.getNameCount()-1).toString();
    }

    public Path path() {
        return path;
    }

    public Node root() {
        return nodes.get(0);
    }

    public int nodeCount() {
        return nodes.size();
    }

    public int meshCount() {
        return loadedMeshes.size();
    }

    public Node node(int index) {
        return nodes.get(index);
    }

    public Node node(String name) {
        return nodeNames.valueOf(name);
    }

    public LoadedMesh loadedMesh(int index) {
        return loadedMeshes.get(index);
    }

    public LoadedMesh loadedMesh(String name) {
        return meshNames.valueOf(name);
    }

    public String nameOf(Node node) {
        return nodeNames.keyOf(node);
    }

    public String nameOf(LoadedMesh loadedMesh) {
        return meshNames.keyOf(loadedMesh);
    }

    public Collection<Node> nodes() {
        return Collections.unmodifiableCollection(nodes);
    }

    public Collection<LoadedMesh> meshes() {
        return Collections.unmodifiableCollection(loadedMeshes);
    }

    synchronized Node newNode(String name, int numChildren, int numMeshes) {

        Node node = new Node(nodes.size(), numChildren, numMeshes);

        nodes.add(node);
        nodeNames.put(name, node);

        return node;
    }

    synchronized LoadedMesh newMesh(String name, Mesh mesh) {

        LoadedMesh loadedMesh = new LoadedMesh(loadedMeshes.size(), mesh);

        loadedMeshes.add(loadedMesh);
        meshNames.put(name, loadedMesh);

        return loadedMesh;
    }

    @Override
    protected void free() {
        loadedMeshes.clear();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Model model = (Model) o;
        return Objects.equals(path, model.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path);
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();

        builder.append("Model '").append(name()).append("' {\n").append("  Path: ").append("\"").append(path).append("\"").append('\n');
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

        public LoadedMesh mesh(int index) {
            return loadedMeshes.get(indices[meshIndicesOffset + index]);
        }

        public Stream<Node> children() {
            return Arrays.stream(indices, 0, meshIndicesOffset).mapToObj(nodes::get);
        }

        public Stream<LoadedMesh> meshes() {
            return Arrays.stream(indices, meshIndicesOffset, indices.length).mapToObj(loadedMeshes::get);
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

        synchronized void addMesh(int index, LoadedMesh loadedMesh) {
            indices[meshIndicesOffset + index] = loadedMesh.index;
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
                    innerIndentation, meshes().map(LoadedMesh::name).collect(joining(", ")),
                    innerIndentation, childrenStr.isEmpty() ? "" : "\n" + childrenStr + "\n" + innerIndentation, indentation);
        }
    }

    public final class LoadedMesh {

        private final int index;
        private final Mesh mesh;

        public LoadedMesh(int index, Mesh mesh) {
            this.index = index;
            this.mesh = mesh;
        }

        public int index() {
            return index;
        }

        public String name() {
            return nameOf(this);
        }

        public Mesh mesh() {
            return mesh;
        }
    }
}
