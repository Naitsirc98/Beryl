package naitsirc98.beryl.examples;

import naitsirc98.beryl.core.Beryl;
import naitsirc98.beryl.core.BerylApplication;
import naitsirc98.beryl.examples.forest.ForestGame;
import naitsirc98.beryl.examples.pbr.PBRDemo;

public class Main {

    public static void main(String[] args) {

        //BerylApplication app = new ForestGame();

        BerylApplication app = new PBRDemo();

        Beryl.launch(app);
    }

}
