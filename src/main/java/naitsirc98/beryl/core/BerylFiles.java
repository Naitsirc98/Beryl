package naitsirc98.beryl.core;

import naitsirc98.beryl.logging.Log;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Utility class for getting Beryl internal resources
 */
public final class BerylFiles {

    /**
     * Converts the specified {@link String} path into a resource {@link URL}
     *
     * @param path the path
     * @return the url
     */
    public static URL getURL(String path) {
       return BerylFiles.class.getResource(normalize(path));
    }

    /**
     * Converts the specified {@link String} path into a resource {@link String} path
     *
     * @param path the path
     * @return the string
     */
    public static String getString(String path) {
        String newPath = getURI(path).getPath();
        if(newPath.charAt(0) == '/') {
            return newPath.substring(1);
        }
        return newPath;
    }

    /**
     * Converts the specified {@link String} path into a resource {@link URI}
     *
     * @param path the path
     * @return the uri
     */
    public static URI getURI(String path) {
        try {
            return getURL(path).toURI();
        } catch (URISyntaxException | NullPointerException e) {
            Log.error("Failed to create URI from URL", e);
        }
        return null;
    }

    /**
     * Converts the specified {@link String} path into a resource {@link File}
     *
     * @param path the path
     * @return the file
     */
    public static File getFile(String path) {
        return new File(getString(path));
    }

    /**
     * Converts the specified {@link String} path into a resource {@link Path}
     *
     * @param path the path
     * @return the path
     */
    public static Path getPath(String path) {
        return Paths.get(getURI(path));
    }

    /**
     * Gets the specified resource as an {@link InputStream}
     *
     * @param path the path
     * @return the stream
     */
    public static InputStream getStream(String path) {
        return BerylFiles.class.getResourceAsStream(normalize(path));
    }

    private static String normalize(String path) {
        return path.charAt(0) == File.pathSeparatorChar ? path : '/' + path;
    }

    private BerylFiles() {}
}
