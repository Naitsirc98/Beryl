package naitsirc98.beryl.core;

import naitsirc98.beryl.logging.Log;
import naitsirc98.beryl.resources.ManagedResource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for getting Beryl internal resources
 */
public final class BerylFiles {

    private static BerylJarFileSystem jarFileSystem;

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
     * @param filename the path
     * @return the file
     */
    public static File getFile(String filename) {
        Path path = getPath(filename);
        return path == null ? null : path.toFile();
    }

    /**
     * Converts the specified {@link String} path into a resource {@link Path}
     *
     * @param path the path
     * @return the path
     */
    public static Path getPath(String path) {

        URI uri = getURI(path);

        return uri == null ? null : getPath(uri);
    }

    private static Path getPath(URI uri) {

        Path path;

        if(uri.getScheme().contains("jar")) {

            String[] uriParts = uri.toString().split("!");

            if(jarFileSystem == null) {
                jarFileSystem = new BerylJarFileSystem(uriParts[0]);
            }

            path = jarFileSystem.getPath(uriParts[1]);

        } else {
            path = Paths.get(uri);
        }

        return path;
    }

    /**
     * Gets the specified resource as an {@link InputStream}
     *
     * @param path the path
     * @return the stream
     */
    public static InputStream getInputStream(String path) {
        return BerylFiles.class.getResourceAsStream(normalize(path));
    }

    private static String normalize(String path) {
        return path.charAt(0) == File.pathSeparatorChar ? path : '/' + path;
    }

    private BerylFiles() {}

    private static class BerylJarFileSystem extends ManagedResource {

        private final FileSystem fileSystem;

        public BerylJarFileSystem(String root) {
            fileSystem = createFileSystem(root);
        }

        public Path getPath(String first, String... others) {
            return fileSystem.getPath(first, others).toAbsolutePath();
        }

        @Override
        protected void free() {
            try {
                fileSystem.close();
            } catch (IOException e) {
                Log.error("Failed to close file system " + fileSystem, e);
            }
        }

        private FileSystem createFileSystem(String root) {
            FileSystem fileSystem = null;
            try {
                Map<String, String> environment = new HashMap<>();
                environment.put("create", "true");
                environment.put("encoding", "UTF-8");
                fileSystem = FileSystems.newFileSystem(URI.create(root), environment, ClassLoader.getSystemClassLoader());
            } catch (Exception e) {
                Log.error("Failed to create JAR FileSystem", e);
            }
            return fileSystem;
        }
    }
}
