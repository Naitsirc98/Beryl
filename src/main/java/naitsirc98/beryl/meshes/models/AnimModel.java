package naitsirc98.beryl.meshes.models;

import naitsirc98.beryl.animations.Animation;
import naitsirc98.beryl.meshes.Bone;
import naitsirc98.beryl.meshes.Mesh;
import naitsirc98.beryl.meshes.AnimMesh;
import naitsirc98.beryl.util.collections.LookupTable;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;

public class AnimModel {

    private final Path path;
    private final List<AnimMesh> meshes;
    private final LookupTable<String, AnimMesh> meshNames;
    private final List<AnimNode> nodes;
    private final LookupTable<String, AnimNode> nodeNames;
    private final List<Bone> bones;
    private final LinkedHashMap<String, Animation> animations;

    public AnimModel(Path path) {
        this.path = path;
        this.meshes = new ArrayList<>();
        meshNames = new LookupTable<>();
        this.nodes = new ArrayList<>();
        this.nodeNames = new LookupTable<>();
        bones = new ArrayList<>();
        animations = new LinkedHashMap<>();
    }

    public Path path() {
        return path;
    }

    public List<AnimMesh> meshes() {
        return meshes;
    }

    public List<AnimNode> nodes() {
        return nodes;
    }

    public String nameOf(AnimMesh mesh) {
        return meshNames.keyOf(mesh);
    }

    public AnimMesh mesh(int index) {
        return meshes.get(index);
    }

    public AnimMesh mesh(String name) {
        return meshNames.valueOf(name);
    }

    public String nameOf(AnimNode node) {
        return nodeNames.keyOf(node);
    }

    public AnimNode node(int index) {
        return nodes.get(index);
    }

    public AnimNode node(String name) {
        return nodeNames.valueOf(name);
    }

    public List<Bone> bones() {
        return bones;
    }

    public Bone bone(int index) {
        return bones.get(index);
    }

    public Collection<Animation> animations() {
        return animations.values();
    }

    public Animation animation(String name) {
        return animations.get(name);
    }

    public boolean released() {
        return meshes.stream().anyMatch(Mesh::released);
    }

    void addNode(AnimNode node) {
        nodes.add(node);
        nodeNames.put(node.name(), node);
    }

    void addMesh(AnimMesh mesh) {
        meshes.add(mesh);
        meshNames.put(mesh.name(), mesh);
    }

    void addBone(Bone bone) {
        bones.add(bone);
        /*
        if(!bones.contains(bone)) {
            bones.add(bone);
        }
         */
    }

    void addAnimation(Animation animation) {
        animations.put(animation.name(), animation);
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();

        builder.append("Model '").append(path).append("' {\n").append("  Path: ").append("\"").append(path).append("\"").append('\n');
        builder.append("  Structure:\n").append(nodes.get(0).toString("    ")).append('\n');

        return builder.append("  };").toString();
    }
}
