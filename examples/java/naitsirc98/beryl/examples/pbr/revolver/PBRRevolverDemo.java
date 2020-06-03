package naitsirc98.beryl.examples.pbr.revolver;

import naitsirc98.beryl.core.BerylFiles;
import naitsirc98.beryl.examples.pbr.PBRDemo;
import naitsirc98.beryl.scenes.Scene;
import naitsirc98.beryl.scenes.environment.SceneEnvironment;
import naitsirc98.beryl.scenes.environment.skybox.Skybox;
import naitsirc98.beryl.scenes.environment.skybox.SkyboxFactory;
import org.joml.Vector3f;

public class PBRRevolverDemo extends PBRDemo {

    @Override
    protected void setSceneObjects(Scene scene) {
        CerberusRevolver.create(scene, new Vector3f(150, 0, 0), 0.85f);
    }

    @Override
    protected void setSceneEnvironment(Scene scene) {

        SceneEnvironment environment = scene.environment();

        Skybox skybox = SkyboxFactory.newSkyboxHDR(BerylFiles.getPath("textures/skybox/hdr/sunrise_beach_2k.hdr"));

        environment.skybox(skybox);
    }
}
