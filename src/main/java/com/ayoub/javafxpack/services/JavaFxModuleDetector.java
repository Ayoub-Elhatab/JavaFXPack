package com.ayoub.javafxpack.services;


import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Detects which JavaFX modules a jar needs and
 * lists the modules the user can pick from.
 *
 * @author Ayoub Elhatab
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
     * Scans the class entries of a jar for JavaFX packages and maps them to module names.
     * Falls back to javafx.controls and javafx.fxml
     * when nothing is found or the jar can't be read.
     *
     * @param jarPath absolute path to the jar to scan
     * @return the suggested JavaFX module names
     */
    public Set<String> suggestModules(String jarPath){
        Set<String> found = new LinkedHashSet<>();

        try (JarFile jarFile = new JarFile(jarPath)) {
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
            // unreadable jar: the fallback below applies
        }

        if (found.isEmpty()) {
            found.add("javafx.controls");
            found.add("javafx.fxml");
        }

        return found;
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
