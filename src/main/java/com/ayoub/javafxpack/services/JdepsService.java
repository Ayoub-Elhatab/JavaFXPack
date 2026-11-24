package com.ayoub.javafxpack.services;


import com.ayoub.javafxpack.models.ToolchainInfo;
import com.ayoub.javafxpack.process.ProcessRunner;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

/**
 * Finds the JDK modules an application needs by running jdeps on the
 * main jar and all its dependency jars.
 *
 * @author Ayoub Elhatab
 * LinkedIn: <a href="https://www.linkedin.com/in/ayoub-elhatab/">Ayoub Elhatab</a>
 */
public class JdepsService {

    /** Modules used when jdeps is missing or fails; covers most desktop apps. */
    public static final List<String> FALLBACK_MODULES = List.of(
            "java.base", "java.desktop", "java.logging", "java.management", "java.naming",
            "java.net.http", "java.prefs", "java.scripting", "java.sql", "java.xml", "jdk.unsupported");

    private final ProcessRunner processRunner = new ProcessRunner();

    /**
     * Runs jdeps on every jar of the input folder and returns the JDK modules it reports.
     * JavaFX modules are left out, because they come from the selected checkboxes.
     *
     * @param toolchain the detected tools (jdeps path)
     * @param inputDir the staged input folder holding the main jar and its dependencies
     * @param onLine called with progress messages, for the build log
     * @return the JDK modules to add to the runtime image
     * @throws InterruptedException if the thread is interrupted while waiting for jdeps
     */
    public List<String> detectJdkModules(ToolchainInfo toolchain, File inputDir, Consumer<String> onLine) throws InterruptedException {
        if (toolchain.getJdepsPath() == null) {
            onLine.accept("jdeps not found: using the default JDK modules.");
            return FALLBACK_MODULES;
        }

        File[] jars = inputDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".jar"));
        if (jars == null || jars.length == 0) {
            return FALLBACK_MODULES;
        }

        List<String> command = new ArrayList<>(List.of(
                toolchain.getJdepsPath(),
                "--print-module-deps",
                "--ignore-missing-deps",
                "--multi-release", "base"));
        for (File jar : jars) {
            command.add(jar.getAbsolutePath());
        }

        List<String> output = new ArrayList<>();
        int exitCode;
        try {
            exitCode = processRunner.run(command, inputDir, output::add);
        } catch (IOException e) {
            onLine.accept("jdeps could not start (" + e.getMessage() + "): using the default JDK modules.");
            return FALLBACK_MODULES;
        }

        String lastLine = output.isEmpty() ? "" : output.get(output.size() - 1).trim();
        if (exitCode != 0 || !lastLine.matches("[\\w.]+(,[\\w.]+)*")) {
            output.forEach(onLine);
            onLine.accept("jdeps failed: using the default JDK modules.");
            return FALLBACK_MODULES;
        }

        List<String> modules = Arrays.stream(lastLine.split(","))
                .filter(m -> !m.startsWith("javafx."))
                .toList();
        onLine.accept("JDK modules found by jdeps: " + String.join(",", modules));
        return modules;
    }

}