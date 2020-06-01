package naitsirc98.beryl.examples;

import naitsirc98.beryl.core.Beryl;
import naitsirc98.beryl.core.BerylApplication;
import naitsirc98.beryl.examples.forest.ForestGame;
import naitsirc98.beryl.examples.pbr.PBRDemo;
import naitsirc98.beryl.examples.space.SolarSystem;
import naitsirc98.beryl.examples.stresstest.StressTest;

public class Main {

    public static void main(String[] args) {

        BerylApplication app = new ForestGame();

        // BerylApplication app = new PBRDemo();

        // BerylApplication app = new StressTest();

        // BerylApplication app = new SolarSystem();

        Beryl.launch(app);
    }

}
