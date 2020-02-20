package naitsirc98.beryl;

import naitsirc98.beryl.core.Beryl;
import naitsirc98.beryl.core.BerylApplication;
import naitsirc98.beryl.core.BerylConfiguration;


public class Main {

    public static void main(String[] args) {

        try {

            throw new RuntimeException(new NullPointerException());

        } catch(Throwable e) {
            e.printStackTrace();
            System.out.println(e.toString());
        }

        Beryl.launch(new BerylApplication.Builder().setConfiguration(Main::setConfig).build());

    }

    private static void setConfig() {
        BerylConfiguration.DEBUG.set(true);
        BerylConfiguration.INTERNAL_DEBUG.set(true);
    }

}
