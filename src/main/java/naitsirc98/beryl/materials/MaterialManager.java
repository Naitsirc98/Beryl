package naitsirc98.beryl.materials;

import naitsirc98.beryl.assets.AssetManager;
import naitsirc98.beryl.graphics.buffers.StorageBuffer;
import naitsirc98.beryl.graphics.textures.Texture;
import naitsirc98.beryl.logging.Log;
import naitsirc98.beryl.util.BitFlags;
import naitsirc98.beryl.util.types.Singleton;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import static naitsirc98.beryl.materials.IMaterial.Type;
import static naitsirc98.beryl.util.Asserts.assertFalse;
import static naitsirc98.beryl.util.handles.LongHandle.NULL;
import static naitsirc98.beryl.util.types.DataType.INT32_SIZEOF;

public final class MaterialManager implements AssetManager<IMaterial> {

    private static final int BUFFER_INITIAL_CAPACITY = 1000 * IMaterial.SIZEOF;

    @Singleton
    private static MaterialManager instance;

    public static MaterialManager get() {
        return instance;
    }

    private AtomicInteger handleProvider;
    private Map<Type, List<IMaterial>> materials;
    private Map<String, IMaterial> materialNames;
    private Queue<Number> recycleQueue;
    private StorageBuffer buffer;
    private long bufferSize;
    private Queue<IMaterial> modifiedMaterials;

    @Override
    public void init() {
        handleProvider = new AtomicInteger(0);
        materials = new ConcurrentHashMap<>();
        materialNames = new ConcurrentHashMap<>();
        recycleQueue = new ConcurrentLinkedQueue<>();
        buffer = StorageBuffer.create();
        buffer.allocate(BUFFER_INITIAL_CAPACITY);
        bufferSize = 0;
        modifiedMaterials = new ArrayDeque<>();
        putDefaults();
    }

    @SuppressWarnings("unchecked")
    public <T extends StorageBuffer> T buffer() {
        return (T) buffer;
    }

    @SuppressWarnings("unchecked")
    synchronized <T extends IMaterial> T create(String name, Type type, Map<Byte, Object> properties, BitFlags flags) {

        if(name == null) {
            Log.fatal("Material name cannot be null");
            return null;
        }

        if(materialNames.containsKey(name)) {
            Log.fatal("Material named " + name + " already exists");
            return null;
        }

        if(type == null) {
            Log.fatal("Material type cannot be null");
            return null;
        }

        if(properties == null || properties.isEmpty()) {
            Log.fatal("Material properties cannot be empty");
            return null;
        }

        Material material = new Material(handleProvider.getAndIncrement(), name, type, properties, flags);

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

        recycleQueue.add(offset);
        recycleQueue.add(index);
    }

    @Override
    public synchronized void destroyAll() {
        materialNames.values().forEach(material -> ((Material) material).destroy());
        recycleQueue.clear();
        materialNames.clear();
        materials.clear();
    }

    @Override
    public void terminate() {
        destroyAll();
        buffer.release();
    }

    public void update() {
        while(!modifiedMaterials.isEmpty()) {
            Material material = (Material) modifiedMaterials.poll();
            copyMaterialToBuffer(material, material.offset());
            material.markUpdated();
        }
    }

    void setModified(IMaterial material) {
        modifiedMaterials.add(material);
    }

    private void copyMaterialToBuffer(Material material, long offset) {

        try(MemoryStack stack = MemoryStack.stackPush()) {

            ByteBuffer data = stack.calloc(Material.SIZEOF);

            switch(material.type()) {

                case PHONG_MATERIAL:
                    copyPhongMaterialToBuffer(material, data);
                    break;
                case WATER_MATERIAL:
                    data.putInt(IMaterial.SIZEOF - INT32_SIZEOF, material.flags());
                    break;
                default:
                    Log.fatal("Unknown material type: " + material.type());
                    return;
            }

            buffer.update(offset, data.rewind());
        }
    }

    private void copyPhongMaterialToBuffer(PhongMaterial material, ByteBuffer data) {

        material.ambientColor().getRGBA(data);
        material.diffuseColor().getRGBA(data);
        material.specularColor().getRGBA(data);
        material.emissiveColor().getRGBA(data);

        // Phong materials use resident textures
        data.putLong(handle(material.ambientMap()));
        data.putLong(handle(material.diffuseMap()));
        data.putLong(handle(material.specularMap()));
        data.putLong(handle(material.emissiveMap()));
        data.putLong(handle(material.occlusionMap()));
        data.putLong(handle(material.normalMap()));

        data.putFloat(material.textureTiling().x()).putFloat(material.textureTiling().y());

        data.putFloat(material.alpha());
        data.putFloat(material.shininess());
        data.putFloat(material.reflectivity());
        data.putFloat(material.refractiveIndex());

        data.putInt(material.flags());
    }

    private long handle(Texture texture) {
        return texture == null ? NULL : texture.makeResident();
    }

    private void putDefaults() {

        PhongMaterial.get(PhongMaterial.PHONG_MATERIAL_DEFAULT_NAME);

        // TODO...
    }

}
