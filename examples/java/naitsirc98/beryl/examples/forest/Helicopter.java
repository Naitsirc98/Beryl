package naitsirc98.beryl.examples.forest;

import naitsirc98.beryl.audio.AudioClip;
import naitsirc98.beryl.core.BerylFiles;
import naitsirc98.beryl.core.Time;
import naitsirc98.beryl.lights.LightRange;
import naitsirc98.beryl.lights.SpotLight;
import naitsirc98.beryl.materials.Material;
import naitsirc98.beryl.materials.PhongMaterial;
import naitsirc98.beryl.meshes.StaticMesh;
import naitsirc98.beryl.meshes.models.StaticModel;
import naitsirc98.beryl.meshes.models.StaticModelLoader;
import naitsirc98.beryl.meshes.views.StaticMeshView;
import naitsirc98.beryl.scenes.Entity;
import naitsirc98.beryl.scenes.Scene;
import naitsirc98.beryl.scenes.components.audio.AudioPlayer;
import naitsirc98.beryl.scenes.components.behaviours.LateMutableBehaviour;
import naitsirc98.beryl.scenes.components.behaviours.UpdateBehaviour;
import naitsirc98.beryl.scenes.components.math.Transform;
import naitsirc98.beryl.scenes.components.meshes.StaticMeshInstance;
import naitsirc98.beryl.util.Color;
import naitsirc98.beryl.util.IColor;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import static naitsirc98.beryl.util.Maths.radians;

public class Helicopter {

    public static final String HELICOPTER_NAME = "Helicopter";

    private static StaticModel helicopterModel;

    static {

        Path modelPath = BerylFiles.getPath("models/helicopter.obj");

        helicopterModel = new StaticModelLoader().load(modelPath);
    }

    public static StaticModel getHelicopterModel() {
        return helicopterModel;
    }

    public static Entity create(Scene scene) {

        Entity helicopter = scene.newEntity();

        Transform transform = helicopter.add(Transform.class);

        helicopter.add(StaticMeshInstance.class).meshViews(getHelicopterBodyMeshViews());

        AudioPlayer audio = helicopter.add(AudioPlayer.class);

        audio.clip(AudioClip.get("helicopter", params -> params.audioFile(BerylFiles.getPath("audio/helicopter.ogg"))));

        audio.source()
                .position(transform.position())
                .referenceDistance(10)
                .rollOff(1.0f)
                .minGain(0.0f)
                .gain(25)
                .maxGain(25)
                .looping(true);

        createMainRotor(scene, transform);
        createTailRotor(scene, transform);

        transform.scale(0.3f);

        helicopter.add(HelicopterController.class);

        return helicopter;
    }

    private static void createMainRotor(Scene scene, Transform parent) {

        Entity rotor = scene.newEntity();

        Transform transform = rotor.add(Transform.class);

        rotor.add(StaticMeshInstance.class).meshView(getMainRotorMeshView());

        rotor.add(LateMutableBehaviour.class).onLateUpdate(self -> {
            Vector3f pos = new Vector3f(transform.position()).add(0.552f, 46.28f, 27.378f);
            transform.rotateY(radians(45)).rotateAroundY(Time.millis() * Time.IDEAL_DELTA_TIME * 2, pos.x, pos.y, pos.z);
        });

        parent.addChild(transform);
    }

    private static void createTailRotor(Scene scene, Transform parent) {

        Entity rotor = scene.newEntity();

        Transform transform = rotor.add(Transform.class);

        rotor.add(StaticMeshInstance.class).meshView(getTailRotorMeshView());

        rotor.add(LateMutableBehaviour.class).onLateUpdate(self -> {
            Vector3f pos = new Vector3f(transform.position()).add(-2.367f, 48.328f, -100.561f);
            transform.rotateY(radians(45)).rotateAroundX(Time.millis() * Time.IDEAL_DELTA_TIME * 2, pos.x, pos.y, pos.z);
        });

        parent.addChild(transform);
    }

    private static List<StaticMeshView> getHelicopterBodyMeshViews() {
        return getHelicopterModel().meshes().stream()
                .filter(mesh -> !mesh.name().contains("TailRotor") && !mesh.name().contains("Propeller"))
                .map(mesh -> new StaticMeshView(mesh, PhongMaterial.getFactory().getMaterial(mesh.name(), Helicopter::setupBodyMaterial)))
                .collect(Collectors.toList());
    }

    private static void setupBodyMaterial(PhongMaterial material) {
        if(material.name().contains("Glass")) {
            material.ambientColor(Color.colorBlack()).diffuseColor(Color.colorBlack());
        } else {
            IColor color = new Color(0.294f, 0.325f, 0.125f, 1.0f).intensify(0.5f);
            material.ambientColor(color).diffuseColor(color);
            material.shininess(1);
        }
    }

    private static StaticMeshView getMainRotorMeshView() {
        StaticMesh mesh = getHelicopterModel().mesh("PropellerGray");
        Material material = PhongMaterial.getFactory().getMaterial(mesh.name(), Helicopter::createMainRotorMaterial);
        return new StaticMeshView(mesh, material);
    }

    private static void createMainRotorMaterial(PhongMaterial material) {
        IColor color = new Color(0.1f, 0.1f, 0.1f, 1.0f);
        material.ambientColor(color).diffuseColor(color);
        material.shininess(1);
    }

    private static StaticMeshView getTailRotorMeshView() {
        StaticMesh mesh = getHelicopterModel().mesh("TailRotor");
        Material material = PhongMaterial.getFactory().getMaterial(mesh.name(), Helicopter::createTailRotorMaterial);
        return new StaticMeshView(mesh, material);
    }

    private static void createTailRotorMaterial(PhongMaterial material) {
        IColor color = new Color(0.1f, 0.1f, 0.1f, 1.0f);
        material.ambientColor(color).diffuseColor(color);
        material.shininess(1);
    }

    private static class HelicopterController extends UpdateBehaviour {

        private float speed;

        @Override
        protected void onStart() {
            get(Transform.class).rotateY(radians(45)).position(-3500, 160, -3500);
            speed = Time.IDEAL_DELTA_TIME * 76;
            get(AudioPlayer.class).source().position(get(Transform.class).position()).velocity(new Vector3f(36, 0, 36));
            get(AudioPlayer.class).source().play();
        }

        @Override
        public void onUpdate() {
            Transform t = get(Transform.class);
            t.translate(speed, 0, speed);
            AudioPlayer player = get(AudioPlayer.class);
            player.source().position(get(Transform.class).position()).velocity(new Vector3f(36, 0, 36));
            setTreeBouncingSpeed();
        }

        private void setTreeBouncingSpeed() {

            Vector3fc helicopterPosition = get(Transform.class).position();

            scene().entities().filter(e -> e.name().contains("Tree")).map(e -> e.get(Tree.TreeRandomBouncing.class)).forEach(tree -> {

                Vector3fc treePosition = tree.get(Transform.class).position();

                float distance = helicopterPosition.distance(treePosition);

                tree.setBouncingSpeed(200.0f / distance);
                tree.setBouncingLimit(distance / 160);

            });

            Entity water = scene().entity("Water");

            float waterDistortion = 44.0f / helicopterPosition.distance(new Vector3f(425.02f, -4.177f, 382.986f));

            water.get(Water.WaterController.class).setMovementFactor(waterDistortion);
        }
    }
}
