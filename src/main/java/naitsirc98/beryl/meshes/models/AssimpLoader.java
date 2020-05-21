package naitsirc98.beryl.meshes.models;

import naitsirc98.beryl.graphics.GraphicsFactory;
import naitsirc98.beryl.graphics.textures.Texture2D;
import naitsirc98.beryl.images.PixelFormat;
import naitsirc98.beryl.materials.PhongMaterial;
import naitsirc98.beryl.meshes.Mesh;
import naitsirc98.beryl.meshes.StaticMesh;
import naitsirc98.beryl.util.Color;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.assimp.*;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.file.Path;
import java.util.Map;
import java.util.function.BiConsumer;

import static java.util.Objects.requireNonNull;
import static naitsirc98.beryl.graphics.textures.Texture.Quality.HIGH;
import static naitsirc98.beryl.util.Asserts.assertTrue;
import static naitsirc98.beryl.util.types.DataType.UINT32_SIZEOF;
import static naitsirc98.beryl.util.types.TypeUtils.getOrElse;
import static org.lwjgl.assimp.Assimp.*;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.memAlloc;

public abstract class AssimpLoader {

    protected ByteBuffer getIndices(AIMesh aiMesh) {

        final int numFaces = aiMesh.mNumFaces();

        if (numFaces == 0) {
            return null;
        }

        ByteBuffer indices = memAlloc(numFaces * 3 * UINT32_SIZEOF);

        AIFace.Buffer aiFaces = aiMesh.mFaces();

        for (int i = 0; i < numFaces; i++) {

            AIFace aiFace = aiFaces.get(i);

            IntBuffer faceIndices = aiFace.mIndices();

            assertTrue(faceIndices.remaining() == 3);

            for (int j = faceIndices.position(); j < faceIndices.remaining(); j++) {
                indices.putInt(faceIndices.get(j));
            }
        }

        return indices.rewind();
    }

    protected Matrix4fc matrix4fc(AIMatrix4x4 aiMatrix4) {
        return new Matrix4f(
                aiMatrix4.a1(), aiMatrix4.b1(), aiMatrix4.c1(), aiMatrix4.d1(),
                aiMatrix4.a2(), aiMatrix4.b2(), aiMatrix4.c2(), aiMatrix4.d2(),
                aiMatrix4.a3(), aiMatrix4.b3(), aiMatrix4.c3(), aiMatrix4.d3(),
                aiMatrix4.a4(), aiMatrix4.b4(), aiMatrix4.c4(), aiMatrix4.d4()
        );
    }

    protected void processTexCoordsAttribute(AIMesh aiMesh, StaticVertexHandler handler, ByteBuffer vertices, int stride) {

        AIVector3D.Buffer textureCoordinates = aiMesh.mTextureCoords(0);

        if (textureCoordinates == null) {
            return;
            // throw new IllegalStateException("Number of texture coordinates is zero");
        }

        int offset = StaticMesh.VERTEX_TEXCOORDS_OFFSET;

        Vector2f texCoords = new Vector2f();

        for (int i = 0; i < textureCoordinates.remaining(); i++) {

            AIVector3D aiTexCoords = textureCoordinates.get(i);

            texCoords.set(aiTexCoords.x(), aiTexCoords.y());

            handler.mapTextureCoords(texCoords).get(offset, vertices);

            offset += stride;
        }

    }

    protected void processNormalAttribute(AIMesh aiMesh, StaticVertexHandler handler, ByteBuffer vertices, int stride) {

        AIVector3D.Buffer normals = requireNonNull(aiMesh.mNormals());

        if (normals.remaining() == 0) {
            throw new IllegalStateException("Number of normals is zero");
        }

        int offset = Mesh.VERTEX_NORMAL_OFFSET;

        Vector3f normal = new Vector3f();

        for (int i = 0; i < normals.remaining(); i++) {

            AIVector3D aiNormal = normals.get(i);

            normal.set(aiNormal.x(), aiNormal.y(), aiNormal.z());

            handler.mapNormal(normal).get(offset, vertices);

            offset += stride;
        }

    }

    protected void processPositionAttribute(AIMesh aiMesh, StaticVertexHandler handler, ByteBuffer vertices, int stride) {

        if(aiMesh.mNumVertices() == 0) {
            throw new IllegalStateException("Number of positions is zero");
        }

        AIVector3D.Buffer positions = aiMesh.mVertices();

        int offset = 0;

        Vector3f position = new Vector3f();

        for(int i = 0; i < positions.remaining(); i++) {

            AIVector3D aiPosition = positions.get(i);

            position.set(aiPosition.x(), aiPosition.y(), aiPosition.z());

            handler.mapPosition(position).get(offset, vertices);

            offset += stride;
        }
    }

    protected PhongMaterial loadMaterial(AIScene aiScene, AIMesh aiMesh, String meshName, Path texturesDir, Map<String, Texture2D> texturesCache) {

        if(aiScene.mNumMaterials() == 0) {
            return PhongMaterial.getDefault();
        }

        if(PhongMaterial.exists(meshName)) {
            return PhongMaterial.get(meshName);
        }

        PhongMaterial material = PhongMaterial.get(meshName);

        AIMaterial aiMaterial = AIMaterial.createSafe(aiScene.mMaterials().get(aiMesh.mMaterialIndex()));

        loadColorAndMap(texturesDir, aiMaterial, AI_MATKEY_COLOR_AMBIENT, aiTextureType_AMBIENT, texturesCache,
                (color, map) -> material.setAmbientColor(color).setAmbientMap(getOrElse(map, Texture2D.whiteTexture())));

        loadColorAndMap(texturesDir, aiMaterial, AI_MATKEY_COLOR_DIFFUSE, AI_MATKEY_GLTF_PBRMETALLICROUGHNESS_BASE_COLOR_TEXTURE, texturesCache,
                (color, map) -> material.setDiffuseColor(color).setDiffuseMap(getOrElse(map, Texture2D.whiteTexture())));

        loadColorAndMap(texturesDir, aiMaterial, AI_MATKEY_COLOR_SPECULAR, aiTextureType_SPECULAR, texturesCache,
                (color, map) -> material.setSpecularColor(color).setDiffuseMap(getOrElse(map, Texture2D.whiteTexture())));

        return material;
    }

    private void loadColorAndMap(Path texturesDir, AIMaterial aiMaterial, String colorKey, int textureType, Map<String, Texture2D> texturesCache,
                                 BiConsumer<Color, Texture2D> consumer) {

        try(MemoryStack stack = stackPush()) {

            Color color = Color.colorWhite();

            AIColor4D aiColor = AIColor4D.mallocStack(stack);

            if(aiGetMaterialColor(aiMaterial, colorKey, aiTextureType_NONE, 0, aiColor) == AI_TRUE) {
                color.set(aiColor.r(), aiColor.g(), aiColor.b(), aiColor.a());
            }

            consumer.accept(color, loadTexture(texturesDir, aiMaterial, textureType, texturesCache));
        }
    }

    private Texture2D loadTexture(Path texturesDir, AIMaterial aiMaterial, int textureType, Map<String, Texture2D> texturesCache) {

        try(MemoryStack stack = stackPush()) {

            AIString path = AIString.mallocStack(stack);

            if(aiGetMaterialTexture(aiMaterial, textureType, 0, path, (IntBuffer) null, null, null, null, null, null) != aiReturn_SUCCESS) {
                return null;
            }

            final String texturePath = texturesDir.resolve(path.dataString()).toString();

            if(texturesCache.containsKey(texturePath)) {
                return texturesCache.get(texturePath);
            }

            Texture2D texture = GraphicsFactory.get().newTexture2D(texturePath, PixelFormat.RGBA);
            texture.setQuality(HIGH);

            texturesCache.put(texturePath, texture);

            return texture;
        }
    }

}
