package naitsirc98.beryl.meshes.models;

import java.util.function.Function;

@FunctionalInterface
public interface NameMapper extends Function<String, String> {

    @Override
    default String apply(String s) {
        return rename(s);
    }

    String rename(String originalName);

}
