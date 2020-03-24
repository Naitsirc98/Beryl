package naitsirc98.beryl.core;

import naitsirc98.beryl.logging.Log;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class BerylFiles {

    public static URL getURL(String path) {
       return BerylFiles.class.getResource(normalize(path));
    }

    public static String getString(String path) {
        String newPath = getURI(path).getPath();
        if(newPath.charAt(0) == '/') {
            return newPath.substring(1);
        }
        return newPath;
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
        return BerylFiles.class.getResourceAsStream(normalize(path));
    }

    private static String normalize(String path) {
        return path.charAt(0) == File.pathSeparatorChar ? path : '/' + path;
    }

    private BerylFiles() {}
}
