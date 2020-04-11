package naitsirc98.beryl.assets;

public interface AssetManager<T extends Asset> {

    void init();

    int count();

    boolean exists(String assetName);

    <K extends T> K get(String assetName);

    default void destroy(String assetName) {
        destroy(get(assetName));
    }

    void destroy(T asset);

    void destroyAll();

    void terminate();
}
