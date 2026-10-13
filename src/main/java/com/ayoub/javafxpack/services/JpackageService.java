package com.ayoub.javafxpack.services;


import com.ayoub.javafxpack.models.BuildResult;
import com.ayoub.javafxpack.models.ProjectConfig;
import com.ayoub.javafxpack.models.ToolchainInfo;
import com.ayoub.javafxpack.process.ProcessRunner;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Builds and runs the jpackage command
 * that creates the Windows exe installer.
 *
 * @author Ayoub Elhatab
 * LinkedIn: <a href="https://www.linkedin.com/in/ayoub-elhatab/">Ayoub Elhatab</a>
 */
public class JpackageService {

    private final ProcessRunner processRunner = new ProcessRunner();

    /**
     * Assembles the jpackage argument list for a Windows exe build.
     *
     * @param config the project settings
     * @param toolchain the detected tools (jpackage path)
     * @param runtimeImagePath path of the runtime image built by jlink
     * @return the full command
     */
    public List<String> buildCommand(ProjectConfig config, ToolchainInfo toolchain, String runtimeImagePath) {
        List<String> cmd = new ArrayList<>();

        cmd.add(toolchain.getJpackagePath());
        cmd.add("--type");
        cmd.add("exe");

        cmd.add("--name");
        cmd.add(config.getAppName());

        cmd.add("--app-version");
        cmd.add(config.getAppVersion());

        if (notBlank(config.getVendor())) {
            cmd.add("--vendor");
            cmd.add(config.getVendor());
        }

        File jar = new File(config.getJarPath());
        cmd.add("--input");
        cmd.add(jar.getParentFile().getAbsolutePath());

        cmd.add("--main-jar");
        cmd.add(jar.getName());

        cmd.add("--main-class");
        cmd.add(config.getMainClass());

        cmd.add("--dest");
        cmd.add(config.getOutputDir());

        if (notBlank(config.getIconPath())) {
            cmd.add("--icon");
            cmd.add(config.getIconPath());
        }

        cmd.add("--runtime-image");
        cmd.add(runtimeImagePath);

        cmd.add("--win-shortcut");
        cmd.add("--win-menu");
        cmd.add("--win-dir-chooser");

        if (!config.getJvmOptions().isEmpty()) {
            for (String opt : config.getJvmOptions()) {
                cmd.add("--java-options");
                cmd.add(opt);
            }
        }

        return cmd;
    }

    /**
     * Runs jpackage, streaming its output, and reports the outcome.
     *
     * @param config the project settings
     * @param toolchain the detected tools (jpackage path)
     * @param runtimeImagePath path of the runtime image built by jlink
     * @param onLine called for every line of command output, for the build log
     * @return the build result with success flag, exit code, exe path and full log
     */
    public BuildResult run(ProjectConfig config, ToolchainInfo toolchain, String runtimeImagePath, Consumer<String> onLine) {
        List<String> command = buildCommand(config, toolchain, runtimeImagePath);
        StringBuilder rawLog = new StringBuilder();

        onLine.accept("Running: " + String.join(" ", command));

        int exitCode;
        try {
            exitCode = processRunner.run(command, new File(config.getOutputDir()), line -> {
                rawLog.append(line).append(System.lineSeparator());
                onLine.accept(line);
            });
        } catch (IOException | InterruptedException e) {
            return BuildResult.builder()
                    .success(false)
                    .exitCode(-1)
                    .rawLog(rawLog + System.lineSeparator() + "Failed to launch jpackage: " + e.getMessage())
                    .build();
        }

        String expectedExeName = config.getAppName() + "-" + config.getAppVersion() + ".exe";
        File expectedExe = new File(config.getOutputDir(), expectedExeName);

        String outputPath = expectedExe.isFile() ? expectedExe.getAbsolutePath() : config.getOutputDir();

        return BuildResult.builder()
                .success(exitCode == 0)
                .exitCode(exitCode)
                .outputExePath(outputPath)
                .rawLog(rawLog.toString())
                .build();
    }

    /**
     * Checks whether a string has content.
     *
     * @param s the string to check
     * @return true if the string is neither null nor blank
     */
    private boolean notBlank(String s) {
        return s != null && !s.isBlank();
    }
}
