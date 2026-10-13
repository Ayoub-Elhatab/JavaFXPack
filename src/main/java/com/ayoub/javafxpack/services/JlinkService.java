package com.ayoub.javafxpack.services;


import com.ayoub.javafxpack.models.ProjectConfig;
import com.ayoub.javafxpack.models.ToolchainInfo;
import com.ayoub.javafxpack.process.ProcessRunner;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Builds a trimmed custom Java runtime with jlink,
 * combining the JDK modules with the selected JavaFX modules.
 * The resulting image is bundled into the exe by jpackage.
 *
 * @author Ayoub Elhatab
 * LinkedIn: <a href="https://www.linkedin.com/in/ayoub-elhatab/">Ayoub Elhatab</a>
 */
public class JlinkService {

    private final ProcessRunner processRunner = new ProcessRunner();

    /**
     * Runs jlink to create the runtime image, replacing any previous one.
     *
     * @param config the project settings
     * @param toolchain the detected tools (jlink path and JDK home)
     * @param onLine called for every line of command output, for the build log
     * @return the absolute path of the generated runtime image folder
     * @throws IOException if jlink can't start or exits with an error
     * @throws InterruptedException if the thread is interrupted while waiting for jlink
     */
    public String buildRuntime(ProjectConfig config, ToolchainInfo toolchain, Consumer<String> onLine) throws IOException, InterruptedException {

        Path imageDir = Path.of(config.getOutputDir(), "runtime-image");
        deleteIfExists(imageDir.toFile());

        List<String> command = new ArrayList<>();
        command.add(toolchain.getJlinkPath());
        command.add("--module-path");
        command.add(toolchain.getJavaHome() + "\\jmods" + File.pathSeparator + config.getJavafxJmodsPath());
        command.add("--add-modules");
        command.add(String.join(",", config.getJavafxModules()));
        command.add("--output");
        command.add(imageDir.toString());
        command.add("--strip-debug");
        command.add("--no-header-files");
        command.add("--no-man-pages");
        command.add("--compress=2");

        onLine.accept("Running: " + String.join(" ", command));
        int exitCode = processRunner.run(command, new File(config.getOutputDir()), onLine);

        if (exitCode != 0) {
            throw new IOException("jlink failed with exit code " + exitCode);
        }

        return imageDir.toAbsolutePath().toString();
    }

    /**
     * Deletes a file or folder, including everything inside it.
     *
     * @param dir the file or folder to delete, ignored if it doesn't exist
     */
    private void deleteIfExists(File dir){
        if(!dir.exists()) return;
        File[] children = dir.listFiles();
        if(children != null){
            for(File child : children){
                deleteIfExists(child);
            }
        }
        dir.delete();
    }
}
