package naitsirc98.beryl.resources;

import naitsirc98.beryl.logging.Log;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class Resources {

    public static URL getURL(String path) {
       return Resources.class.getResource(normalize(path));
    }

    public static String getString(String path) {
        return getURL(path).toExternalForm();
    }

    public static URI getURI(String path) {
        try {
            return getURL(path).toURI();
        } catch (URISyntaxException | NullPointerException e) {
            Log.error("Failed to create URI from URL", e);
        }
        return null;
    }

    public static File getFile(String path) {
        return new File(getString(path));
    }

    public static Path getPath(String path) {
        return Paths.get(getURI(path));
    }

    public static InputStream getStream(String path) {
        return Resources.class.getResourceAsStream(normalize(path));
    }

    private static String normalize(String path) {
        return path.startsWith("/") ? path : '/' + path;
    }

    private Resources() {}
}
