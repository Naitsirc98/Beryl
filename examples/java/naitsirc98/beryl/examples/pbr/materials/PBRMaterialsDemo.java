package naitsirc98.beryl.examples.pbr.materials;

import naitsirc98.beryl.core.BerylFiles;
import naitsirc98.beryl.examples.pbr.PBRDemo;
import naitsirc98.beryl.scenes.Scene;
import naitsirc98.beryl.scenes.environment.SceneEnvironment;
import naitsirc98.beryl.scenes.environment.skybox.Skybox;
import naitsirc98.beryl.scenes.environment.skybox.SkyboxFactory;

public class PBRMaterialsDemo extends PBRDemo {

    @Override
    protected void setSceneObjects(Scene scene) {
        PBRSphere.create(scene, 0, 0, 0, BerylFiles.getPath("textures/plastic"));
        PBRSphere.create(scene, 60, 0, 0, BerylFiles.getPath("textures/grass"));
        PBRSphere.create(scene, 120, 0, 0, BerylFiles.getPath("textures/wall"));
        PBRSphere.create(scene, -60, 0, 0, BerylFiles.getPath("textures/rusted_iron"));
        PBRSphere.create(scene, -120, 0, 0, BerylFiles.getPath("textures/gold"));
    }

    @Override
    protected void setSceneEnvironment(Scene scene) {

        SceneEnvironment environment = scene.environment();

        Skybox skybox = SkyboxFactory.newSkyboxHDR(BerylFiles.getPath("textures/skybox/hdr/indoor.hdr"));

        skybox.prefilterLODBias(0.0f);

        environment.skybox(skybox);
    }
}
