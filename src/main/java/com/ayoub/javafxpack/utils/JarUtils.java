package com.ayoub.javafxpack.utils;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.jar.JarFile;
import java.util.stream.Stream;

/**
 * Helpers for listing jars in a folder and recognising JavaFX jars.
 *
 * @author Ayoub Elhatab
 * LinkedIn: <a href="https://www.linkedin.com/in/ayoub-elhatab/">Ayoub Elhatab</a>
 */
public final class JarUtils {

    private JarUtils() {
    }

    /**
     * Lists the jar files directly inside a folder, sorted by name.
     *
     * @param dir the folder to list
     * @return the jar files, or an empty list if the folder doesn't exist
     * @throws IOException if the folder can't be read
     */
    public static List<Path> listJars(Path dir) throws IOException {
        if (dir == null || !Files.isDirectory(dir)) return List.of();
        try (Stream<Path> files = Files.list(dir)) {
            return files
                    .filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().toLowerCase().endsWith(".jar"))
                    .sorted()
                    .toList();
        }
    }

    /**
     * Tells whether a jar is part of JavaFX itself (from Maven, Gradle or the SDK).
     * These jars must not be packaged, because JavaFX is already in the runtime image.
     *
     * @param jar the jar to check
     * @return true if the file name starts with "javafx-" or the jar holds classes in the javafx package
     */
    public static boolean isJavaFxJar(Path jar) {
        String name = jar.getFileName().toString().toLowerCase();
        if (name.startsWith("javafx-") || name.startsWith("javafx.")) return true;
        try (JarFile jarFile = new JarFile(jar.toFile())) {
            return jarFile.stream().anyMatch(e -> e.getName().startsWith("javafx/"));
        } catch (IOException e) {
            return false;
        }
    }

}
