package naitsirc98.beryl.meshes.models;

import naitsirc98.beryl.meshes.vertices.VertexData;
import naitsirc98.beryl.meshes.vertices.VertexLayout;
import org.joml.Matrix4fc;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public final class Model implements Iterable<Model.Node> {

    private final Path path;
    private final VertexLayout vertexLayout;
    private final List<Node> nodes;
    private final List<Mesh> meshes;

    Model(Path path, VertexLayout vertexLayout) {
        this.path = path;
        this.vertexLayout = vertexLayout;
        nodes = new ArrayList<>();
        meshes = new ArrayList<>();
    }

    synchronized Node newNode(String name, int childCount, int meshCount) {

        Node node = new Node(name, nodes.size(), childCount, meshCount);

        nodes.add(node);

        return node;
    }

    private synchronized Mesh newMesh(String name) {

        Mesh mesh = new Mesh(name, meshes.size());

        meshes.add(mesh);

        return mesh;
    }

    public Path path() {
        return path;
    }

    public VertexLayout vertexLayout() {
        return vertexLayout;
    }

    public int size() {
        return nodes.size();
    }

    public int meshCount() {
        return meshes.size();
    }

    public boolean containsNode(String name) {
        return nodes.stream().anyMatch(node -> Objects.equals(name, node.name));
    }

    public Node node(String name) {
        return nodes.stream().filter(node -> Objects.equals(name, node.name)).findAny().orElse(null);
    }

    public Node node(int index) {
        return nodes.get(index);
    }

    public boolean containsMesh(String name) {
        return meshes.stream().anyMatch(m -> Objects.equals(m.name, name));
    }

    public Mesh mesh(String name) {
        return meshes.stream().filter(m -> Objects.equals(m.name, name)).findAny().orElse(null);
    }

    public Mesh mesh(int index) {
        return meshes.get(index);
    }

    public Stream<Node> nodes() {
        return nodes.stream();
    }

    @Override
    public Iterator<Node> iterator() {
        return nodes.iterator();
    }

    public final class Node {

        private final String name;
        private final int index;
        private Matrix4fc transformation;
        private final int[] meshIndices;
        private final int[] childIndices;

        private Node(String name, int index, int childCount, int meshCount) {
            this.name = name;
            this.index = index;
            childIndices = new int[childCount];
            meshIndices = new int[meshCount];
        }

        public String name() {
            return name;
        }

        public VertexLayout vertexLayout() {
            return vertexLayout;
        }

        public Matrix4fc transformation() {
            return transformation;
        }

        public int index() {
            return index;
        }

        public int childCount() {
            return childIndices.length;
        }

        public Node child(int index) {
            return nodes.get(childIndices[index]);
        }

        public int meshCount() {
            return meshIndices.length;
        }

        public Mesh mesh(int index) {
            return Model.this.meshes.get(meshIndices[index]);
        }

        Node newChild(int index, String name, int childCount, int meshCount) {
            Node child = newNode(name, childCount, meshCount);
            childIndices[index] = child.index;
            return child;
        }

        void transformation(Matrix4fc transformation) {
            this.transformation = transformation;
        }

        Mesh newMesh(int index, String name) {
            Mesh mesh = Model.this.newMesh(name);
            meshIndices[index] = mesh.index;
            return mesh;
        }
    }

    public final class Mesh {

        private final String name;
        private VertexData vertexData;
        private final int index;

        private Mesh(String name, int index) {
            this.name = name;
            this.index = index;
        }

        public String name() {
            return name;
        }

        public int index() {
            return index;
        }

        public VertexData vertexData() {
            return vertexData;
        }

        public VertexLayout vertexLayout() {
            return Model.this.vertexLayout;
        }

        void vertexData(VertexData vertexData) {
            this.vertexData = vertexData;
        }
    }
}
