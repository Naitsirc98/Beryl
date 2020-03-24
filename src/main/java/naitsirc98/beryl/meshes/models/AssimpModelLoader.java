package naitsirc98.beryl.meshes.models;

import naitsirc98.beryl.graphics.GraphicsAPI;
import naitsirc98.beryl.meshes.vertices.VertexAttribute;
import naitsirc98.beryl.meshes.vertices.VertexAttributeList;
import naitsirc98.beryl.meshes.vertices.VertexAttributeList.VertexAttributeIterator;
import naitsirc98.beryl.meshes.vertices.VertexData;
import naitsirc98.beryl.meshes.vertices.VertexLayout;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.assimp.*;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;
import static naitsirc98.beryl.graphics.GraphicsAPI.VULKAN;
import static naitsirc98.beryl.meshes.vertices.VertexAttribute.*;
import static naitsirc98.beryl.util.types.DataType.*;
import static org.lwjgl.assimp.Assimp.*;

public class AssimpModelLoader implements ModelLoader {

    private static final int DEFAULT_FLAGS = aiProcess_OptimizeMeshes
            | aiProcess_OptimizeGraph
            | aiProcess_Triangulate
            | aiProcess_FlipUVs
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

        AIScene aiScene = aiImportFile(path.toString(), getAssimpFlags(vertexLayout));

        try {

            Model model = new Model(path, vertexLayout);

            if(aiScene == null || aiScene.mRootNode() == null) {
                throw new IllegalStateException("Could not load model: " + aiGetErrorString());
            }

            AINode aiRoot = aiScene.mRootNode();

            Model.Node modelRoot = model.newNode(aiRoot.mName().dataString(), aiRoot.mNumChildren(), aiRoot.mNumMeshes());
            modelRoot.transformation(matrix4fc(aiRoot.mTransformation()));

            processNode(aiScene, aiRoot, modelRoot);

            return model;

        } finally {
            aiReleaseImport(aiScene);
        }
    }

    private void processNode(AIScene aiScene, AINode aiNode, Model.Node modelNode) {

        processNodeMeshes(aiScene, aiNode, modelNode);

        processNodeChildren(aiNode, modelNode);
    }

    private void processNodeChildren(AINode aiNode, Model.Node modelNode) {

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
            processMeshVertexData(aiMesh, nodeMesh);
        }
    }

    private void processMeshVertexData(AIMesh aiMesh, Model.Mesh nodeMesh) {

        final VertexLayout vertexLayout = nodeMesh.vertexLayout();

        VertexData.Builder vertexDataBuilder = VertexData.builder(vertexLayout);

        for(int i = 0;i < vertexLayout.bindings();i++) {

            VertexAttributeList attributeList = vertexLayout.attributeList(i);

            AttributeInfo attributeInfo = new AttributeInfo();
            attributeInfo.stride = attributeList.stride();
            attributeInfo.totalSizeof = attributeList.sizeof();

            ByteBuffer vertices = BufferUtils.createByteBuffer(aiMesh.mNumVertices() * attributeList.sizeof());
            ByteBuffer indices = getIndices(aiMesh);

            VertexAttributeIterator iterator = attributeList.iterator();

            while(iterator.hasNext()) {
                setAttributeData(aiMesh, attributeInfo.set(iterator.next(), iterator.offset()), vertices);
            }

            vertexDataBuilder.vertices(i, vertices.rewind());

            if(indices != null) {
                vertexDataBuilder.indices(indices.rewind(), INT32);
            }

        }

        nodeMesh.vertexData(vertexDataBuilder.build());
    }

    private ByteBuffer getIndices(AIMesh aiMesh) {

        final int numFaces = aiMesh.mNumFaces();

        if(numFaces == 0) {
            return null;
        }

        ByteBuffer indices = BufferUtils.createByteBuffer(numFaces * 3 * INT32_SIZEOF);

        AIFace.Buffer aiFaces = aiMesh.mFaces();

        for(int i = 0;i < numFaces;i++) {

            AIFace aiFace = aiFaces.get(i);

            IntBuffer faceIndices = aiFace.mIndices();

            for(int j = faceIndices.position();j < faceIndices.remaining();j++) {
                indices.putInt(faceIndices.get(j));
            }
        }

        return indices.rewind();
    }

    private void setAttributeData(AIMesh aiMesh, AttributeInfo attributeInfo, ByteBuffer vertices) {
        ATTRIBUTE_DATA_PROCESSORS.get(attributeInfo.attribute).process(attributeInfo, aiMesh, vertices);
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
            flags |= aiProcess_GenNormals | aiProcess_GenSmoothNormals;
        } else {
            flags |= aiProcess_DropNormals;
        }

        if (attributes.contains(TEXCOORDS2D)) {
            flags |= aiProcess_GenUVCoords;
        }

        if (attributes.contains(TANGENTS) || attributes.contains(BITANGENTS)) {
            flags |= aiProcess_CalcTangentSpace;
        }

        if (GraphicsAPI.get() == VULKAN) {
            flags |= aiProcess_ConvertToLeftHanded;
        }

        // TODO bones, etc ...

        return flags;
    }

    private static final class AttributeInfo {

        private VertexAttribute attribute;
        private int offset;
        private int stride;
        private int totalSizeof;

        public AttributeInfo set(VertexAttribute attribute, int offset) {
            this.attribute = attribute;
            this.offset = offset;
            return this;
        }
    }

    private interface AttributeDataProcessor {

        void process(AttributeInfo attributeInfo, AIMesh aiMesh, ByteBuffer vertices);

    }

    private static EnumMap<VertexAttribute, AttributeDataProcessor> createAttributeDataProcessors() {

        EnumMap<VertexAttribute, AttributeDataProcessor> map = new EnumMap<>(VertexAttribute.class);

        map.put(POSITION2D, AssimpModelLoader::processPositionAttribute);
        map.put(POSITION3D, AssimpModelLoader::processPositionAttribute);
        map.put(POSITION4D, AssimpModelLoader::processPositionAttribute);

        map.put(NORMAL, AssimpModelLoader::processNormalAttribute);

        map.put(TEXCOORDS2D, AssimpModelLoader::processTexCoordsAttribute);
        map.put(TEXCOORDS3D, AssimpModelLoader::processTexCoordsAttribute);

        map.put(TANGENTS, AssimpModelLoader::processTangentsAttribute);
        map.put(BITANGENTS, AssimpModelLoader::processBiTangentsAttribute);

        map.put(COLOR3D, AssimpModelLoader::processColorAttribute);
        map.put(COLOR4D, AssimpModelLoader::processColorAttribute);

        return map;
    }

    private static void processColorAttribute(AttributeInfo attributeInfo, AIMesh aiMesh, ByteBuffer vertices) {

        AIColor4D.Buffer colors = aiMesh.mColors(0);

        final int colorsCount = colors == null ? vertices.capacity() / attributeInfo.totalSizeof : colors.remaining();

        AIColor4D white = colors == null ? AIColor4D.create().set(1, 1, 1, 1) : null;

        final int stride = attributeInfo.stride;
        int offset = attributeInfo.offset;

        for(int i = 0;i < colorsCount;i++) {

            AIColor4D color = colors == null ? white : colors.get(i);

            vertices.putFloat(offset, color.r())
                    .putFloat(offset + FLOAT32_SIZEOF, color.g())
                    .putFloat(offset + FLOAT32_SIZEOF * 2, color.b());

            if(attributeInfo.attribute == COLOR4D) {
                vertices.putFloat(offset + FLOAT32_SIZEOF * 3, color.a());
            }

            offset += stride;
        }

    }

    private static void processBiTangentsAttribute(AttributeInfo attributeInfo, AIMesh aiMesh, ByteBuffer vertices) {

        AIVector3D.Buffer bitangents = aiMesh.mBitangents();

        if(bitangents.remaining() == 0) {
            throw new IllegalStateException("Number of bitangents is zero");
        }

        final int stride = attributeInfo.stride;
        int offset = attributeInfo.offset;

        for(int i = 0;i < bitangents.remaining();i++) {

            AIVector3D bitangent = bitangents.get(i);

            vertices.putFloat(offset, bitangent.x())
                    .putFloat(offset + FLOAT32_SIZEOF, bitangent.y())
                    .putFloat(offset + FLOAT32_SIZEOF * 2, bitangent.z());

            offset += stride;
        }

    }

    private static void processTangentsAttribute(AttributeInfo attributeInfo, AIMesh aiMesh, ByteBuffer vertices) {

        AIVector3D.Buffer tangents = aiMesh.mTangents();

        if(tangents.remaining() == 0) {
            throw new IllegalStateException("Number of tangents is zero");
        }

        final int stride = attributeInfo.stride;
        int offset = attributeInfo.offset;

        for(int i = 0;i < tangents.remaining();i++) {

            AIVector3D tangent = tangents.get(i);

            vertices.putFloat(offset, tangent.x())
                    .putFloat(offset + FLOAT32_SIZEOF, tangent.y())
                    .putFloat(offset + FLOAT32_SIZEOF * 2, tangent.z());

            offset += stride;
        }

    }

    private static void processTexCoordsAttribute(AttributeInfo attributeInfo, AIMesh aiMesh, ByteBuffer vertices) {

        AIVector3D.Buffer textureCoordinates = aiMesh.mTextureCoords(0);

        if(textureCoordinates == null) {
            throw new IllegalStateException("Number of texture coordinates is zero");
        }

        final int stride = attributeInfo.stride;
        int offset = attributeInfo.offset;

        for(int i = 0;i < textureCoordinates.remaining();i++) {

            AIVector3D texCoords = textureCoordinates.get(i);

            vertices.putFloat(offset, texCoords.x())
                    .putFloat(offset + FLOAT32_SIZEOF, texCoords.y());

            if(attributeInfo.attribute == TEXCOORDS3D) {
                vertices.putFloat(offset + FLOAT32_SIZEOF * 2, texCoords.z());
            }

            offset += stride;
        }

    }

    private static void processNormalAttribute(AttributeInfo attributeInfo, AIMesh aiMesh, ByteBuffer vertices) {

        AIVector3D.Buffer normals = aiMesh.mNormals();

        if(normals.remaining() == 0) {
            throw new IllegalStateException("Number of normals is zero");
        }

        final int stride = attributeInfo.stride;
        int offset = attributeInfo.offset;

        for(int i = 0;i < normals.remaining();i++) {

            AIVector3D normal = normals.get(i);

            vertices.putFloat(offset, normal.x())
                    .putFloat(offset + FLOAT32_SIZEOF, normal.y())
                    .putFloat(offset + FLOAT32_SIZEOF * 2, normal.z());

            offset += stride;
        }

    }

    private static void processPositionAttribute(AttributeInfo attributeInfo, AIMesh aiMesh, ByteBuffer vertices) {

        if(aiMesh.mNumVertices() == 0) {
            throw new IllegalStateException("Number of positions is zero");
        }

        AIVector3D.Buffer positions = aiMesh.mVertices();

        final VertexAttribute attribute = attributeInfo.attribute;
        final int stride = attributeInfo.stride;
        int offset = attributeInfo.offset;

        for(int i = 0;i < positions.remaining();i++) {

            AIVector3D position = positions.get(i);

            vertices.putFloat(offset, position.x())
                    .putFloat(offset + FLOAT32_SIZEOF, position.y());

            if(attribute == POSITION3D || attribute == POSITION4D) {
                vertices.putFloat(offset + FLOAT32_SIZEOF * 2, position.z());
            }

            if(attribute == POSITION4D) {
                vertices.putFloat(offset + FLOAT32_SIZEOF * 3, 1.0f);
            }

            offset += stride;
        }

    }

}
