package naitsirc98.beryl.examples.app1;

import naitsirc98.beryl.audio.AudioClip;
import naitsirc98.beryl.core.BerylFiles;
import naitsirc98.beryl.core.Time;
import naitsirc98.beryl.graphics.GraphicsFactory;
import naitsirc98.beryl.graphics.textures.Sampler;
import naitsirc98.beryl.graphics.textures.Texture2D;
import naitsirc98.beryl.images.PixelFormat;
import naitsirc98.beryl.materials.WaterMaterial;
import naitsirc98.beryl.meshes.StaticMesh;
import naitsirc98.beryl.meshes.views.WaterMeshView;
import naitsirc98.beryl.scenes.Entity;
import naitsirc98.beryl.scenes.Scene;
import naitsirc98.beryl.scenes.components.audio.AudioPlayer;
import naitsirc98.beryl.scenes.components.behaviours.UpdateBehaviour;
import naitsirc98.beryl.scenes.components.behaviours.UpdateMutableBehaviour;
import naitsirc98.beryl.scenes.components.math.Transform;
import naitsirc98.beryl.scenes.components.meshes.WaterMeshInstance;
import org.joml.Vector3f;

import static naitsirc98.beryl.scenes.EnhancedWaterUnit.ENHANCED_WATER_UNIT_0;
import static naitsirc98.beryl.util.Maths.clamp;
import static naitsirc98.beryl.util.Maths.radians;

public class Water {

    public static Entity create(Scene scene, float x, float y, float z, float scale) {

        Entity water = scene.newEntity("Water");
        water.add(Transform.class).position(x, y, z).rotateX(radians(90)).scale(scale);

        WaterMeshView waterMeshView = new WaterMeshView(StaticMesh.quad(), getWaterMaterial())
                .tiling(20)
                .waterColorStrength(0.03f)
                .distortionStrength(0.02f);
                //.distortionStrength(0.05f);

        waterMeshView.clipPlane(0, 1, 0, water.get(Transform.class).position().y() + 0.1f);
        water.add(WaterMeshInstance.class).meshView(waterMeshView);
        water.add(WaterController.class);

        scene.enhancedWater().setEnhancedWaterView(ENHANCED_WATER_UNIT_0, waterMeshView);

        addWaterAudioSource(scene, scale, 50, 445.163f, -5.879f, 319.965f);

        return water;
    }

    public static void addWaterAudioSource(Scene scene, float terrainSize, float maxDistance, float x, float y, float z) {

        Entity waterSound = scene.newEntity();

        AudioPlayer waterAudioPlayer = waterSound.add(AudioPlayer.class);
        waterAudioPlayer.source()
                .gain(0.7f)
                .position(new Vector3f(x, y, z))
                .referenceDistance(1.4f)
                .maxDistance(maxDistance)
                .rollOff(0.5f)
                .looping(true);
        waterAudioPlayer.play(AudioClip.get("water", audioClipParams -> {
            audioClipParams.audioFile("G:\\__inglesjavi\\waterm.ogg");
        }));
    }

    private static WaterMaterial getWaterMaterial() {

        return WaterMaterial.get("water", material -> {

            Texture2D dudv = GraphicsFactory.get().newTexture2D(BerylFiles.getString("textures/water/dudv.png"), PixelFormat.RGBA);
            Texture2D normalMap = GraphicsFactory.get().newTexture2D(BerylFiles.getString("textures/water/normalMap.png"), PixelFormat.RGBA);

            dudv.generateMipmaps();
            dudv.sampler().wrapMode(Sampler.WrapMode.REPEAT);
            dudv.sampler().minFilter(Sampler.MinFilter.LINEAR_MIPMAP_LINEAR);
            dudv.sampler().magFilter(Sampler.MagFilter.LINEAR);
            dudv.sampler().maxAnisotropy(16);
            dudv.sampler().lodBias(0);

            normalMap.generateMipmaps();
            normalMap.sampler().wrapMode(Sampler.WrapMode.REPEAT);
            normalMap.sampler().minFilter(Sampler.MinFilter.LINEAR_MIPMAP_LINEAR);
            normalMap.sampler().magFilter(Sampler.MagFilter.LINEAR);
            normalMap.sampler().maxAnisotropy(4);
            normalMap.sampler().lodBias(0);

            material.dudvMap(dudv).normalMap(normalMap);
        });
    }


    public static class WaterController extends UpdateBehaviour {

        private float normalMovementFactor;
        private float movementFactor;
        private float movement;

        @Override
        protected void onStart() {
            normalMovementFactor = movementFactor = 0.015f;//0.021f;
            movement = 0.0f;
        }

        public void setMovementFactor(float movementFactor) {
            this.movementFactor = clamp(normalMovementFactor, 1.0f, movementFactor);
        }

        @Override
        public void onUpdate() {

            movement += movementFactor * Time.IDEAL_DELTA_TIME;
            movementFactor = clamp(normalMovementFactor, 1.0f, movementFactor - 0.0001f * Time.IDEAL_DELTA_TIME);

            get(WaterMeshInstance.class).meshView().texturesOffset(movement);

            movement %= 1;
        }
    }
}
