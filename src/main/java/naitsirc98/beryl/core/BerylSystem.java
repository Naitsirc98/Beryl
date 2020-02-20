package naitsirc98.beryl.core;

public abstract class BerylSystem {

    protected void init() {}

    protected void terminate() {}


    public interface DebugReport {

        String report();

    }

}
