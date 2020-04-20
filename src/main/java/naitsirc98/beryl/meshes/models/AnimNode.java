package naitsirc98.beryl.meshes.models;

import naitsirc98.beryl.meshes.AnimMesh;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.lang.Integer.max;
import static java.util.stream.Collectors.joining;

public class AnimNode {

    private final AnimModel model;
    private final String name;
    private final AnimNode parent;
    private final AnimNode[] children;
    private final AnimMesh[] meshes;
    private final List<Matrix4fc> transformations;

    public AnimNode(AnimModel model, String name, AnimNode parent, int numChildren, int numMeshes) {
        this.model = model;
        this.name = name;
        this.parent = parent;
        children = new AnimNode[numChildren];
        meshes = new AnimMesh[numMeshes];
        transformations = new ArrayList<>();
    }

    public Matrix4f computeTransformationAt(int framePosition, Matrix4f dest) {

        if(parent != null) {
            parent.computeTransformationAt(framePosition, dest);
        }

        final int numTransformations = transformations.size();

        if(framePosition < numTransformations) {
            dest.mul(transformations.get(framePosition));
        } else if(numTransformations > 0) {
            dest.mul(transformations.get(numTransformations - 1));
        }

        return dest;
    }

    public AnimModel model() {
        return model;
    }

    public String name() {
        return name;
    }

    public AnimNode parent() {
        return parent;
    }

    public AnimNode[] children() {
        return children;
    }

    public AnimMesh[] meshes() {
        return meshes;
    }

    public List<Matrix4fc> transformations() {
        return transformations;
    }

    public int numAnimationKeyFrames() {
        return max(
                transformations.size(),
                Arrays.stream(children)
                .mapToInt(AnimNode::numAnimationKeyFrames)
                .reduce(Math::max)
                .orElse(0));
    }

    void addChild(int index, AnimNode child) {
        model.addNode(child);
        children[index] = child;
    }

    void addMesh(int index, AnimMesh mesh) {
        model.addMesh(mesh);
        meshes[index] = mesh;
    }

    void addTransformation(Matrix4fc transformation) {
        transformations.add(transformation);
    }

    @Override
    public String toString() {
        return toString("");
    }

    public String toString(String indentation) {
        String innerIndentation = indentation + "  ";
        String childrenStr = Arrays.stream(children()).map(node -> node.toString(innerIndentation + "  ")).collect(joining(",\n"));
        return String.format("%sAnimNode '%s' {\n%smeshes:[%s],\n%schildren: [%s]\n%s};",
                indentation, name(),
                innerIndentation, Arrays.stream(meshes()).map(AnimMesh::name).collect(joining(", ")),
                innerIndentation, childrenStr.isEmpty() ? "" : "\n" + childrenStr + "\n" + innerIndentation, indentation);
    }
}
