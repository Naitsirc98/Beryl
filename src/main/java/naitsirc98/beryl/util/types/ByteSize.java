package naitsirc98.beryl.util.types;

import java.lang.annotation.*;

public interface ByteSize {

    int sizeof();

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @interface Static {

        int value();

    }

}
