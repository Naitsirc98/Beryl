package naitsirc98.beryl.core;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public abstract class BerylSystem {

    protected abstract void init();

    protected abstract void terminate();

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    protected @interface Singleton {

    }
}
