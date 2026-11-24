package com.ayoub.javafxpack.services;


import com.ayoub.javafxpack.models.ProjectConfig;
import com.ayoub.javafxpack.utils.JarUtils;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Prepares the folder jpackage packages as the application: the main jar
 * plus the dependency jars, without JavaFX jars and without anything else
 * that sits next to the main jar.
 *
 * @author Ayoub Elhatab
 * LinkedIn: <a href="https://www.linkedin.com/in/ayoub-elhatab/">Ayoub Elhatab</a>
 */
public class InputStagingService {

    /**
     * Creates a clean {@code input} folder inside the output folder and copies
     * the main jar and the dependency jars into it.
     *
     * @param config the project settings (main jar and optional dependencies folder)
     * @param onLine called with progress messages, for the build log
     * @return the input folder to pass to jdeps and jpackage
     * @throws IOException if a file can't be deleted or copied
     */
    public File stage(ProjectConfig config, Consumer<String> onLine) throws IOException {
        Path inputDir = Path.of(config.getOutputDir(), "input");
        deleteRecursively(inputDir);
        Files.createDirectories(inputDir);

        Path mainJar = Path.of(config.getJarPath());
        Files.copy(mainJar, inputDir.resolve(mainJar.getFileName()), StandardCopyOption.REPLACE_EXISTING);

        if (config.getLibsDir() == null || config.getLibsDir().isBlank()) {
            onLine.accept("No dependencies folder set: only the main jar is packaged.");
            return inputDir.toFile();
        }

        int copied = 0;
        int skipped = 0;
        for (Path lib : JarUtils.listJars(Path.of(config.getLibsDir()))) {
            if (lib.getFileName().equals(mainJar.getFileName())) continue;
            if (JarUtils.isJavaFxJar(lib)) {
                skipped++;
                continue;
            }
            Files.copy(lib, inputDir.resolve(lib.getFileName()), StandardCopyOption.REPLACE_EXISTING);
            copied++;
        }
        onLine.accept("Dependencies: " + copied + " jar(s) copied, "
                + skipped + " JavaFX jar(s) skipped (JavaFX is in the runtime image).");
        return inputDir.toFile();
    }

    /**
     * Deletes a folder and everything in it, if it exists.
     *
     * @param dir the folder to delete
     * @throws IOException if the folder can't be read
     */
    private void deleteRecursively(Path dir) throws IOException {
        if (!Files.exists(dir)) return;
        try (Stream<Path> paths = Files.walk(dir)) {
            paths.sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
        }
    }

}