package naitsirc98.beryl.examples;

import naitsirc98.beryl.core.Beryl;
import naitsirc98.beryl.core.BerylApplication;
import naitsirc98.beryl.examples.forest.day.ForestDay;
import naitsirc98.beryl.examples.forest.night.ForestNight;
import naitsirc98.beryl.examples.pbr.PBRDemo;
import naitsirc98.beryl.examples.pbr.materials.PBRMaterialsDemo;
import naitsirc98.beryl.examples.pbr.revolver.PBRRevolverDemo;
import naitsirc98.beryl.examples.room.RoomScene;
import naitsirc98.beryl.examples.space.SolarSystem;
import naitsirc98.beryl.examples.stresstest.StressTest;

public class Main {

    public static void main(String[] args) {

        BerylApplication app = new ForestDay();

        // BerylApplication app = new ForestNight();

        // BerylApplication app = new PBRRevolverDemo();

        // BerylApplication app = new PBRMaterialsDemo();

        // BerylApplication app = new StressTest();

        // BerylApplication app = new SolarSystem();

        // BerylApplication app = new RoomScene();

        Beryl.launch(app);
    }

}
