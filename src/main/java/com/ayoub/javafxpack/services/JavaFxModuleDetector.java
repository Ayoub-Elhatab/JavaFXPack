package com.ayoub.javafxpack.services;


import com.ayoub.javafxpack.utils.JarUtils;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Detects which JavaFX modules a jar needs and
 * lists the modules the user can pick from.
 *
 * @author Ayoub Elhatab
 * LinkedIn: <a href="https://www.linkedin.com/in/ayoub-elhatab/">Ayoub Elhatab</a>
 */
public class JavaFxModuleDetector {

    private static final Map<String, String> PACKAGE_TO_MODULE = new LinkedHashMap<>();

    static {
        PACKAGE_TO_MODULE.put("javafx/beans/", "javafx.base");
        PACKAGE_TO_MODULE.put("javafx/collections/", "javafx.base");
        PACKAGE_TO_MODULE.put("javafx/scene/", "javafx.graphics");
        PACKAGE_TO_MODULE.put("javafx/stage/", "javafx.graphics");
        PACKAGE_TO_MODULE.put("javafx/application/", "javafx.graphics");
        PACKAGE_TO_MODULE.put("javafx/scene/control/", "javafx.controls");
        PACKAGE_TO_MODULE.put("javafx/fxml/", "javafx.fxml");
        PACKAGE_TO_MODULE.put("javafx/scene/media/", "javafx.media");
        PACKAGE_TO_MODULE.put("javafx/scene/web/", "javafx.web");
        PACKAGE_TO_MODULE.put("javafx/embed/swing/", "javafx.swing");
    }

    /**
     * Scans the main jar and the dependency jars for JavaFX packages and maps them
     * to module names. JavaFX jars themselves are not scanned.
     * Falls back to javafx.controls and javafx.fxml
     * when nothing is found or no jar can be read.
     *
     * @param jarPath absolute path to the main jar
     * @param libsDir folder with the dependency jars, or null/blank for none
     * @return the suggested JavaFX module names
     */
    public Set<String> suggestModules(String jarPath, String libsDir){
        Set<String> found = new LinkedHashSet<>();

        List<Path> jars = new ArrayList<>();
        jars.add(Path.of(jarPath));
        if (libsDir != null && !libsDir.isBlank()) {
            try {
                for (Path lib : JarUtils.listJars(Path.of(libsDir))) {
                    if (!JarUtils.isJavaFxJar(lib)) jars.add(lib);
                }
            } catch (IOException e) {
                // unreadable folder: only the main jar is scanned
            }
        }

        for (Path jar : jars) {
            scanJar(jar, found);
        }

        if (found.isEmpty()) {
            found.add("javafx.controls");
            found.add("javafx.fxml");
        }

        return found;
    }

    /**
     * Adds the JavaFX modules used by one jar's classes and FXML files.
     *
     * @param jar the jar to scan
     * @param found the set the module names are added to
     */
    private void scanJar(Path jar, Set<String> found) {
        try (JarFile jarFile = new JarFile(jar.toFile())) {
            var entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String name = entry.getName();

                if (name.endsWith(".fxml")) found.add("javafx.fxml");
                if (!name.endsWith(".class")) continue;

                matchPackages(name, found);
                try (InputStream in = jarFile.getInputStream(entry)) {
                    matchPackages(new String(in.readAllBytes(), StandardCharsets.ISO_8859_1), found);
                }
            }
        }catch (IOException e) {
            // unreadable jar: skipped
        }
    }

    /**
     * Adds the module of every known JavaFX package found in the given text.
     *
     * @param text a jar entry name or the raw content of a class file
     * @param found the set collecting the detected module names
     */
    private void matchPackages(String text, Set<String> found) {
        PACKAGE_TO_MODULE.forEach((pkg, module) -> {
            if (text.contains(pkg)) found.add(module);
        });
    }

    /**
     * Returns every JavaFX module offered as a checkbox in the UI.
     *
     * @return the selectable JavaFX module names
     */
    public Set<String> allKnownModules() {
        return Collections.unmodifiableSet(new LinkedHashSet<>(PACKAGE_TO_MODULE.values()));
    }

}
