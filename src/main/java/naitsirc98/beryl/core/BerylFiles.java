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
       URL url = BerylFiles.class.getResource(normalize(path));
       if(url == null) {
           Log.error("Failed to get resource " + path);
       }
       return url;
    }

    /**
     * Converts the specified {@link String} path into a resource {@link String} path
     *
     * @param path the path
     * @return the string
     */
    public static String getString(String path) {

        URI uri = getURI(path);

        if(uri == null) {
            return null;
        }

        String newPath = uri.getPath();

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
            URL url = getURL(path);
            return url == null ? null : url.toURI();
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
        URI uri = getURI(path);
        return uri == null ? null : Paths.get(uri);
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
