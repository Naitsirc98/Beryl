package naitsirc98.beryl.core;

import java.util.Objects;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.Arrays.stream;
import static naitsirc98.beryl.util.TypeUtils.initSingleton;
import static naitsirc98.beryl.util.TypeUtils.newInstance;

public class BerylSystemManager extends BerylSystem {

    private final BerylSystem[] systems;

    public BerylSystemManager() {
        systems = new BerylSystem[] {
                allocate(Log.class),
                allocate(Time.class)
        };
    }

    @Override
    public void init() {
        // Initialize systems in order
        stream(systems).forEach(BerylSystem::init);
    }

    @Override
    public void terminate() {
        // Terminate systems in reverse order
        reverseOrder(systems).forEach(BerylSystem::terminate);
    }

    private Stream<BerylSystem> reverseOrder(BerylSystem[] systems) {
        return IntStream.range(0, systems.length)
                .boxed()
                .map(index -> systems[systems.length - index - 1])
                .filter(Objects::nonNull);
    }

    private <T extends BerylSystem> T allocate(Class<T> clazz) {
        final T system = newInstance(clazz);
        initSingleton(clazz, system);
        return system;
    }
}
