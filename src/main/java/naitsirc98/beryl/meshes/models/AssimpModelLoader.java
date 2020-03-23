package naitsirc98.beryl.meshes.models;

import naitsirc98.beryl.graphics.GraphicsAPI;
import naitsirc98.beryl.meshes.vertices.VertexAttribute;
import naitsirc98.beryl.meshes.vertices.VertexAttributeList;
import naitsirc98.beryl.meshes.vertices.VertexData;
import naitsirc98.beryl.meshes.vertices.VertexLayout;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.lwjgl.PointerBuffer;
import org.lwjgl.assimp.*;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.Objects.requireNonNull;
import static naitsirc98.beryl.graphics.GraphicsAPI.VULKAN;
import static naitsirc98.beryl.meshes.vertices.VertexAttribute.*;
import static org.lwjgl.assimp.Assimp.*;
import static org.lwjgl.system.MemoryUtil.memAlloc;
import static org.lwjgl.system.MemoryUtil.memFree;

public class AssimpModelLoader implements ModelLoader {

    private static final int DEFAULT_FLAGS =
            aiProcess_OptimizeMeshes
                    | aiProcess_OptimizeGraph
                    | aiProcess_Triangulate
                    | aiProcess_JoinIdenticalVertices;

    private static final EnumMap<VertexAttribute, AttributeDataProcessor> ATTRIBUTE_DATA_PROCESSORS = createAttributeDataProcessors();


    @Override
    public Model load(Path path, VertexLayout vertexLayout) {

        if (vertexLayout == null) {
            throw new NullPointerException("VertexLayout cannot be null");
        }

        if (Files.notExists(path)) {
            throw new IllegalArgumentException("File " + path + " does not exists");
        }

        return loadAssimp(path, vertexLayout);
    }

    private Model loadAssimp(Path path, VertexLayout vertexLayout) {

        try (AIScene aiScene = aiImportFile(path.toString(), getAssimpFlags(vertexLayout))) {

            Model model = new Model(path, vertexLayout);

            AINode aiRoot = aiScene.mRootNode();

            if (aiRoot == null) {
                throw new IllegalStateException("Failed to load model file '" + path + "': Root node is null");
            }

            Model.Node modelRoot = model.newNode(aiRoot.mName().dataString(), aiRoot.mNumChildren(), aiRoot.mNumMeshes());
            modelRoot.transformation(matrix4fc(aiRoot.mTransformation()));

            processNode(aiScene, aiRoot, modelRoot, model);

            return model;
        }
    }

    private void processNode(AIScene aiScene, AINode aiNode, Model.Node modelNode, Model model) {

        processNodeMeshes(aiScene, aiNode, modelNode);

        processNodeChildren(aiScene, aiNode, modelNode, model);
    }

    private void processNodeChildren(AIScene aiScene, AINode aiNode, Model.Node modelNode, Model model) {

        if (aiNode.mNumChildren() == 0) {
            return;
        }

        PointerBuffer children = requireNonNull(aiNode.mChildren());

        for (int i = 0; i < children.remaining(); i++) {
            AINode aiChildNode = AINode.create(children.get(i));
            Model.Node childNode = modelNode.newChild(i,
                    aiChildNode.mName().dataString(), aiChildNode.mNumChildren(), aiChildNode.mNumMeshes());
            childNode.transformation(matrix4fc(aiChildNode.mTransformation()));
        }
    }

    private void processNodeMeshes(AIScene aiScene, AINode aiNode, Model.Node modelNode) {

        if (aiNode.mNumMeshes() == 0) {
            return;
        }

        PointerBuffer meshes = requireNonNull(aiScene.mMeshes());
        IntBuffer meshIndices = requireNonNull(aiNode.mMeshes());

        for (int i = 0; i < meshIndices.remaining(); i++) {
            final AIMesh aiMesh = AIMesh.create(meshes.get(meshIndices.get(i)));
            Model.Mesh nodeMesh = modelNode.newMesh(i, aiMesh.mName().dataString());
            processMeshVertexData(aiScene, aiMesh, nodeMesh);
        }
    }

    private void processMeshVertexData(AIScene aiScene, AIMesh aiMesh, Model.Mesh nodeMesh) {

        final VertexLayout vertexLayout = nodeMesh.vertexLayout();

        VertexData.Builder vertexDataBuilder = VertexData.builder(vertexLayout);

        IntStream.range(0, vertexLayout.bindings()).forEach(binding -> {

            VertexAttributeList attributeList = vertexLayout.attributeList(binding);

            ByteBuffer vertices = memAlloc(attributeList.sizeof());

            try {

                attributeList.forEach(attribute -> setAttributeData(aiMesh, attribute, vertices));

                vertexDataBuilder.vertices(binding, vertices);

            } finally {
                memFree(vertices);
            }
        });
    }

    private void setAttributeData(AIMesh aiMesh, VertexAttribute attribute, ByteBuffer vertices) {
        ATTRIBUTE_DATA_PROCESSORS.get(attribute).process(attribute, aiMesh, vertices);
    }

    private Matrix4fc matrix4fc(AIMatrix4x4 aiMatrix4) {
        return new Matrix4f(
                aiMatrix4.a1(), aiMatrix4.b1(), aiMatrix4.c1(), aiMatrix4.d1(),
                aiMatrix4.a2(), aiMatrix4.b2(), aiMatrix4.c2(), aiMatrix4.d2(),
                aiMatrix4.a3(), aiMatrix4.b3(), aiMatrix4.c3(), aiMatrix4.d3(),
                aiMatrix4.a4(), aiMatrix4.b4(), aiMatrix4.c4(), aiMatrix4.d4()
        );
    }

    private int getAssimpFlags(VertexLayout vertexLayout) {

        int flags = DEFAULT_FLAGS;

        Set<VertexAttribute> attributes = vertexLayout.attributeList().collect(Collectors.toSet());

        if (attributes.contains(NORMAL)) {
            flags |= aiProcess_GenNormals | aiProcess_GenSmoothNormals | aiProcess_ForceGenNormals;
        } else {
            flags |= aiProcess_DropNormals;
        }

        if (attributes.contains(TEXCOORDS)) {
            flags |= aiProcess_GenUVCoords;
        }

        if (attributes.contains(TANGENT) || attributes.contains(BITANGENT)) {
            flags |= aiProcess_CalcTangentSpace;
        }

        if (GraphicsAPI.get() == VULKAN) {
            flags |= aiProcess_ConvertToLeftHanded;
        }

        // TODO bones, etc ...

        return flags;
    }

    private interface AttributeDataProcessor {

        void process(VertexAttribute attribute, AIMesh aiMesh, ByteBuffer vertices);

    }

    private static EnumMap<VertexAttribute, AttributeDataProcessor> createAttributeDataProcessors() {

        EnumMap<VertexAttribute, AttributeDataProcessor> map = new EnumMap<>(VertexAttribute.class);

        map.put(POSITION2D, AssimpModelLoader::processPositionAttribute);
        map.put(POSITION3D, AssimpModelLoader::processPositionAttribute);
        map.put(POSITION4D, AssimpModelLoader::processPositionAttribute);

        return map;
    }

    private static void processPositionAttribute(VertexAttribute attribute, AIMesh aiMesh, ByteBuffer vertices) {

        if(aiMesh.mNumVertices() == 0) {
            throw new IllegalStateException("Number of positions is zero");
        }

        AIVector3D.Buffer positions = aiMesh.mVertices();

        for(int i = 0;i < positions.remaining();i++) {

            AIVector3D position = positions.get(i);

            vertices.putFloat(position.x());
            vertices.putFloat(position.y());

            if(attribute == POSITION3D || attribute == POSITION4D) {
                vertices.putFloat(position.z());
            }

            if(attribute == POSITION4D) {
                vertices.putFloat(1.0f);
            }
        }

    }

}
