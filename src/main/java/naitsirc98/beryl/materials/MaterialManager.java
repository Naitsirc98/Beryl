package naitsirc98.beryl.materials;

import naitsirc98.beryl.assets.AssetManager;
import naitsirc98.beryl.assets.Assets;
import naitsirc98.beryl.graphics.buffers.StorageBuffer;
import naitsirc98.beryl.graphics.opengl.buffers.GLStorageBuffer;
import naitsirc98.beryl.logging.Log;
import naitsirc98.beryl.util.BitFlags;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import static naitsirc98.beryl.materials.IMaterial.*;
import static naitsirc98.beryl.util.Asserts.assertFalse;
import static naitsirc98.beryl.util.Asserts.assertTrue;
import static naitsirc98.beryl.util.handles.LongHandle.NULL;
import static org.lwjgl.opengl.ARBDirectStateAccess.glGetNamedBufferSubData;
import static org.lwjgl.opengl.GL11C.glFinish;
import static org.lwjgl.system.MemoryUtil.memAddress0;
import static org.lwjgl.system.libc.LibCString.nmemcpy;
import static org.lwjgl.system.libc.LibCString.nmemset;

public final class MaterialManager implements AssetManager<IMaterial> {

    private static final int BUFFER_INITIAL_CAPACITY = 16 * Material.SIZEOF;

    public static MaterialManager get() {
        return Assets.materialManager();
    }

    private AtomicInteger handleProvider;
    private Map<Integer, List<IMaterial>> materials;
    private Map<String, IMaterial> materialNames;
    private Queue<Number> recycleQueue;
    private StorageBuffer buffer;
    private long bufferSize;

    @Override
    public void init() {
        handleProvider = new AtomicInteger(0);
        materials = new ConcurrentHashMap<>();
        materialNames = new ConcurrentHashMap<>();
        recycleQueue = new ConcurrentLinkedQueue<>();
        buffer = StorageBuffer.create();
        buffer.allocate(BUFFER_INITIAL_CAPACITY);
        buffer.mapMemory();
        bufferSize = 0;
        putDefaults();
    }

    @SuppressWarnings("unchecked")
    public <T extends StorageBuffer> T buffer() {
        return (T) buffer;
    }

    @SuppressWarnings("unchecked")
    synchronized <T extends IMaterial> T create(String name, BitFlags flags, Map<Byte, Object> properties) {

        if(name == null) {
            Log.fatal("Material name cannot be null");
            return null;
        }

        if(materialNames.containsKey(name)) {
            Log.fatal("Material named " + name + " already exists");
            return null;
        }

        if(flags == null) {
            Log.fatal("Material flags cannot be null");
            return null;
        }

        if(properties == null || properties.isEmpty()) {
            Log.fatal("Material properties cannot be empty");
            return null;
        }

        Material material = new Material(handleProvider.getAndIncrement(), name, flags, properties);

        List<IMaterial> typeList = materials.computeIfAbsent(material.type(), k -> new ArrayList<>());

        long offset;
        int index;

        if(recycleQueue.isEmpty()) {
            offset = bufferSize;
            index = typeList.size();
            typeList.add(null);
            bufferSize += Material.SIZEOF;
        } else {
            offset = recycleQueue.element().longValue();
            index = recycleQueue.element().intValue();
        }

        material.setOffset(offset);
        material.setIndex(index);

        typeList.set(index, material);

        materialNames.put(name, material);

        copyMaterialToBuffer(material, offset);

        return (T) material;
    }

    @Override
    public int count() {
        return materialNames.size();
    }

    @Override
    public boolean exists(String name) {
        return materialNames.containsKey(name);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends IMaterial> T get(String name) {
        return (T) materialNames.get(name);
    }

    @Override
    public synchronized void destroy(IMaterial material) {

        assertFalse(material.destroyed());

        ((Material) material).destroy();

        final long offset = material.offset();
        final int index = material.index();

        List<IMaterial> typeMaterialList = materials.get(material.type());
        typeMaterialList.set(index, null);

        materialNames.remove(material.name());

        final long address = buffer.mappedMemory() + offset;

        // Clear the storage buffer as well
        assertTrue(address <= bufferSize - Material.SIZEOF);
        nmemset(address, 0, Material.SIZEOF);

        recycleQueue.add(offset);
        recycleQueue.add(index);
    }

    @Override
    public synchronized void destroyAll() {
        materialNames.values().forEach(material -> ((Material) material).destroy());
        nmemset(buffer.mappedMemory(), 0, bufferSize);
        recycleQueue.clear();
        materialNames.clear();
        materials.clear();
    }

    @Override
    public void terminate() {
        destroyAll();
        buffer.release();
    }

    private void copyMaterialToBuffer(Material material, long offset) {

        try(MemoryStack stack = MemoryStack.stackPush()) {

            ByteBuffer data = stack.calloc(Material.SIZEOF);

            if(material.flags().test(PHONG_MATERIAL_BIT)) {
                copyPhongMaterialToBuffer(material, data);
            } else if(material.flags().test(METALLIC_MATERIAL_BIT)) {
                copyMetallicMaterialToBuffer(material, data);
            } else if(material.flags().test(SPECULAR_MATERIAL_BIT)) {
                copySpecularMaterialToBuffer(material, data);
            }

            nmemcpy(buffer.mappedMemory() + offset, memAddress0(data), Material.SIZEOF);

            glFinish();

            var b = stack.calloc(data.capacity());

            glGetNamedBufferSubData(((GLStorageBuffer)buffer).handle(), offset, b);

            System.out.println("==>\n");

            System.out.println(b.getFloat() + ", " + b.getFloat() + ", " + b.getFloat() + ", " + b.getFloat());
            System.out.println(b.getFloat() + ", " + b.getFloat() + ", " + b.getFloat() + ", " + b.getFloat());
            System.out.println(b.getFloat() + ", " + b.getFloat() + ", " + b.getFloat() + ", " + b.getFloat());
            System.out.println(b.getFloat() + ", " + b.getFloat() + ", " + b.getFloat() + ", " + b.getFloat());

            System.out.println(b.getLong());
            System.out.println(b.getLong());
            System.out.println(b.getLong());
            System.out.println(b.getLong());
            System.out.println(b.getLong());
            System.out.println(b.getLong());

            System.out.println(b.getFloat());
            System.out.println(b.getFloat());
            System.out.println(b.getFloat());
            System.out.println(b.getFloat());
        }
    }

    private void copySpecularMaterialToBuffer(SpecularMaterial material, ByteBuffer data) {

        material.diffuseColor().getRGBA(data);
        material.specularColor().getRGBA(data);
        material.emissiveColor().getRGBA(data);
        // Padding
        data.putFloat(0).putFloat(0).putFloat(0).putFloat(0);

        data.putLong(material.diffuseMap().makeResident());
        data.putLong(material.specularGlossinessMap().makeResident());
        data.putLong(material.emissiveMap().makeResident());
        data.putLong(material.occlusionMap().makeResident());
        data.putLong(material.normalMap().makeResident());
        // Padding
        data.putLong(NULL);

        data.putFloat(material.alpha());
        data.putFloat(material.glossiness());
        data.putFloat(material.fresnel());
        // Padding
        data.putFloat(0);
    }

    private void copyMetallicMaterialToBuffer(MetallicMaterial material, ByteBuffer data) {

        material.color().getRGBA(data);
        material.emissiveColor().getRGBA(data);
        // Padding
        data.putFloat(0).putFloat(0).putFloat(0).putFloat(0);
        data.putFloat(0).putFloat(0).putFloat(0).putFloat(0);

        data.putLong(material.colorMap().makeResident());
        data.putLong(material.metallicRoughnessMap().makeResident());
        data.putLong(material.emissiveMap().makeResident());
        data.putLong(material.occlusionMap().makeResident());
        data.putLong(material.normalMap().makeResident());
        // Padding
        data.putLong(NULL);

        data.putFloat(material.alpha());
        data.putFloat(material.metallic());
        data.putFloat(material.roughness());
        data.putFloat(material.fresnel());
    }

    private void copyPhongMaterialToBuffer(PhongMaterial material, ByteBuffer data) {

        material.ambientColor().getRGBA(data);
        material.diffuseColor().getRGBA(data);
        material.specularColor().getRGBA(data);
        material.emissiveColor().getRGBA(data);

        data.putLong(material.ambientMap().makeResident());
        data.putLong(material.diffuseMap().makeResident());
        data.putLong(material.specularMap().makeResident());
        data.putLong(material.emissiveMap().makeResident());
        data.putLong(material.occlusionMap().makeResident());
        data.putLong(material.normalMap().makeResident());

        data.putFloat(material.alpha());
        data.putFloat(material.shininess());
        data.putFloat(material.reflectivity());
        data.putFloat(material.refractiveIndex());

        data.rewind();

        System.out.println(data.getFloat() + ", " + data.getFloat() + ", " + data.getFloat() + ", " + data.getFloat());
        System.out.println(data.getFloat() + ", " + data.getFloat() + ", " + data.getFloat() + ", " + data.getFloat());
        System.out.println(data.getFloat() + ", " + data.getFloat() + ", " + data.getFloat() + ", " + data.getFloat());
        System.out.println(data.getFloat() + ", " + data.getFloat() + ", " + data.getFloat() + ", " + data.getFloat());

        System.out.println(data.getLong());
        System.out.println(data.getLong());
        System.out.println(data.getLong());
        System.out.println(data.getLong());
        System.out.println(data.getLong());
        System.out.println(data.getLong());

        System.out.println(data.getFloat());
        System.out.println(data.getFloat());
        System.out.println(data.getFloat());
        System.out.println(data.getFloat());
    }

    private void putDefaults() {

        PhongMaterial.get(PhongMaterial.PHONG_MATERIAL_DEFAULT_NAME, builder -> {});

        // TODO...

    }
}
